<template>
  <div class="stock-warning-page">
    <div class="page-header">
      <div class="header-left">
        <i class="fa-solid fa-triangle-exclamation page-icon page-icon-warning"></i>
        <div>
          <h2 class="page-title">库存预警</h2>
          <p class="page-desc">当前库存低于或等于最低库存的药品，请及时补货</p>
        </div>
      </div>
    </div>

    <div class="content-card">
      <div class="toolbar">
        <div class="search-wrap">
          <i class="fa-solid fa-magnifying-glass search-icon"></i>
          <el-input
              v-model="keyword"
              placeholder="搜索药品名称、通用名或编码"
              clearable
              class="search-input"
              @clear="loadData"
              @keyup.enter="loadData"
          />
          <el-button class="search-btn" @click="loadData">
            <i class="fa-solid fa-search"></i>
            搜索
          </el-button>

          <el-select
              v-model="categoryIdFilter"
              placeholder="药品分类"
              clearable
              filterable
              size="large"
              class="filter-select"
              style="width: 200px"
              @change="loadData"
          >
            <el-option
                v-for="c in categoryOptions"
                :key="c.categoryId"
                :label="c.name"
                :value="c.categoryId"
            />
          </el-select>
        </div>
      </div>

      <div class="table-wrap" v-loading="loading" element-loading-text="加载中...">
        <el-table
            :data="tableData"
            class="data-table"
            :header-cell-style="headerCellStyle"
            :row-class-name="tableRowClassName"
        >
          <el-table-column prop="medicineCode" label="药品编码" width="118" align="center">
            <template #default="{ row }">
              <span class="cell-code">{{ row.medicineCode }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="药品名称" min-width="140" align="center">
            <template #default="{ row }">
              <span class="cell-name-link" @click="openDetail(row)">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="commonName" label="通用名" min-width="120" align="center"/>
          <el-table-column prop="categoryName" label="分类" width="120" align="center"/>
          <el-table-column prop="spec" label="规格" min-width="120" align="center"/>
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{ row }">
              <span :class="['status-dot', row.status === 1 ? 'enabled' : 'disabled']"></span>
              <span class="status-text">{{ row.status === 1 ? '在用' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="stockQuantity" label="库存" width="90" align="center" />
          <el-table-column prop="minStock" label="最低库存" width="100" align="center" />
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @size-change="loadData"
              @current-change="loadData"
          />
        </div>
      </div>
    </div>

    <!-- 编辑药品对话框（只允许修改库存相关字段） -->
    <el-dialog
        v-model="editDialogVisible"
        width="600px"
        class="stock-warning-dialog edit-dialog"
        :close-on-click-modal="false"
        align-center
        :append-to-body="true"
        :lock-scroll="true"
        :modal-append-to-body="true"
        @close="resetEditForm"
    >
      <template #header>
        <div class="edit-dialog-header">
          <i class="fa-solid fa-boxes-stacked dialog-icon"></i>
          <div>
            <span class="dialog-title">编辑库存</span>
            <span class="dialog-subtitle">修改库存数量和最低库存阈值</span>
          </div>
        </div>
      </template>
      <el-form
          ref="editFormRef"
          :model="editForm"
          :rules="editRules"
          label-position="top"
          class="edit-form"
      >
        <el-form-item label="药品名称">
          <el-input v-model="editForm.name" disabled class="input-disabled" />
        </el-form-item>
        <el-form-item label="药品编码">
          <el-input v-model="editForm.medicineCode" disabled class="input-disabled" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="库存数量" prop="stockQuantity" required>
              <el-input-number
                  v-model="editForm.stockQuantity"
                  :min="0"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入库存数量"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最低库存" prop="minStock" required>
              <el-input-number
                  v-model="editForm.minStock"
                  :min="0"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入最低库存阈值"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="editForm.remark" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="edit-dialog-footer">
          <el-button class="btn-cancel" @click="editDialogVisible = false">取消</el-button>
          <el-button class="btn-save" :loading="editSubmitting" @click="submitEdit">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 药品详情对话框 -->
    <el-dialog
        v-model="detailDialogVisible"
        width="640px"
        class="stock-warning-dialog edit-dialog"
        :close-on-click-modal="false"
        align-center
        :append-to-body="true"
        :lock-scroll="true"
        :modal-append-to-body="true"
        @close="resetDetail"
    >
      <template #header>
        <div class="edit-dialog-header">
          <i class="fa-solid fa-file-medical dialog-icon"></i>
          <div>
            <span class="dialog-title">药品详情</span>
            <span class="dialog-subtitle">{{ detail.name || '-' }}</span>
          </div>
        </div>
      </template>
      <div v-loading="detailLoading" class="detail-body">
        <el-descriptions :column="2" border size="small" class="detail-desc">
          <el-descriptions-item label="药品编码">{{ detail.medicineCode ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="药品名称">{{ detail.name ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="通用名">{{ detail.commonName ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ detail.categoryName ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="规格">{{ detail.spec ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="单位">{{ detail.unit ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="生产厂家" :span="2">{{ detail.manufacturer ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="零售价">{{ formatMoney(detail.unitPrice) }}</el-descriptions-item>
          <el-descriptions-item label="成本价">{{ formatMoney(detail.costPrice) }}</el-descriptions-item>
          <el-descriptions-item label="库存数量">{{ detail.stockQuantity ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="最低库存">{{ detail.minStock ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <span :class="['status-dot', detail.status === 1 ? 'enabled' : 'disabled']"></span>
            <span class="status-text">{{ detail.status === 1 ? '在用' : '停用' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createdTime ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ detail.updatedTime ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <div class="edit-dialog-footer">
          <el-button class="btn-cancel" @click="detailDialogVisible = false">关闭</el-button>
          <el-button class="btn-save" @click="openEditFromDetail">编辑库存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getMedicineStockWarning,
  getMedicineCategories,
  updateMedicine,
  getMedicineDetail
} from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const keyword = ref('')
const categoryIdFilter = ref(null)
const categoryOptions = ref([])

// 编辑对话框
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editSubmitting = ref(false)
const editForm = reactive({
  medicineId: null,
  medicineCode: '',
  name: '',
  stockQuantity: 0,
  minStock: 0,
  remark: ''
})

const editRules = {
  stockQuantity: [
    { required: true, message: '请输入库存数量', trigger: 'change' },
    { type: 'number', min: 0, message: '库存数量不能小于0', trigger: 'change' }
  ],
  minStock: [
    { required: true, message: '请输入最低库存', trigger: 'change' },
    { type: 'number', min: 0, message: '最低库存不能小于0', trigger: 'change' }
  ]
}

// 详情对话框
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detail = ref({})

const headerCellStyle = {
  background: 'rgba(139, 90, 43, 0.08)',
  color: '#5c4a32',
  fontWeight: '600',
  fontSize: '13px',
  borderBottom: '1px solid rgba(139, 90, 43, 0.15)'
}

const tableRowClassName = ({ rowIndex }) => {
  return rowIndex % 2 === 1 ? 'striped-row' : ''
}

const formatMoney = (v) => {
  if (v === null || v === undefined) return '-'
  const n = Number(v)
  if (Number.isNaN(n)) return '-'
  return n.toFixed(2)
}

const loadCategories = async () => {
  try {
    const list = await getMedicineCategories()
    categoryOptions.value = list || []
  } catch {
    categoryOptions.value = []
  }
}

const loadData = async () => {
  loading.value = true
  const params = {
    current: currentPage.value,
    size: pageSize.value,
    keyword: keyword.value || undefined,
    categoryId: categoryIdFilter.value ?? undefined
  }
  try {
    const res = await getMedicineStockWarning(params)
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const resetEditForm = () => {
  editForm.medicineId = null
  editForm.medicineCode = ''
  editForm.name = ''
  editForm.stockQuantity = 0
  editForm.minStock = 0
  editForm.remark = ''
  editFormRef.value?.resetFields()
}

const openEditDialog = (row) => {
  editForm.medicineId = row.medicineId
  editForm.medicineCode = row.medicineCode
  editForm.name = row.name
  editForm.stockQuantity = row.stockQuantity
  editForm.minStock = row.minStock
  editForm.remark = row.remark || ''
  editDialogVisible.value = true
}

const submitEdit = async () => {
  try {
    await editFormRef.value?.validate()
  } catch {
    return
  }
  editSubmitting.value = true
  try {
    // 获取当前药品的完整信息
    const fullDetail = await getMedicineDetail(editForm.medicineId)

    // 构建完整的更新数据（保留原有信息，只修改库存相关字段）
    const updateData = {
      name: fullDetail.name,
      commonName: fullDetail.commonName,
      categoryId: fullDetail.categoryId,
      spec: fullDetail.spec,
      unit: fullDetail.unit,
      manufacturer: fullDetail.manufacturer,
      approvalNo: fullDetail.approvalNo,
      unitPrice: fullDetail.unitPrice,
      costPrice: fullDetail.costPrice,
      status: fullDetail.status,
      // 使用编辑表单中修改的值
      stockQuantity: editForm.stockQuantity,
      minStock: editForm.minStock,
      remark: editForm.remark?.trim() || undefined
    }

    await updateMedicine(editForm.medicineId, updateData)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadData()
  } catch (error) {
    // 拦截器已提示
    console.error('编辑失败:', error)
  } finally {
    editSubmitting.value = false
  }
}

const resetDetail = () => {
  detail.value = {}
}

const openDetail = async (row) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  detail.value = {}
  try {
    const data = await getMedicineDetail(row.medicineId)
    detail.value = data || {}
  } catch {
    detail.value = {}
  } finally {
    detailLoading.value = false
  }
}

const openEditFromDetail = () => {
  const d = detail.value
  if (!d?.medicineId) return
  editForm.medicineId = d.medicineId
  editForm.medicineCode = d.medicineCode
  editForm.name = d.name
  editForm.stockQuantity = d.stockQuantity
  editForm.minStock = d.minStock
  editForm.remark = d.remark || ''
  detailDialogVisible.value = false
  editDialogVisible.value = true
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.stock-warning-page {
  padding: 24px 28px 32px;
  min-height: 100%;
}

.page-header {
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.page-icon {
  width: 48px;
  height: 48px;
  line-height: 48px;
  text-align: center;
  font-size: 22px;
  color: #fff;
  background: linear-gradient(135deg, #e8a54b, #d48232);
  border-radius: 12px;
  box-shadow: 0 4px 14px rgba(212, 130, 50, 0.35);
}

.page-icon-warning {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  box-shadow: 0 4px 14px rgba(217, 119, 6, 0.4);
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #2c1810;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
}

.page-desc {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #5c4a32;
}

.content-card {
  border-radius: 16px;
  background: rgba(255, 252, 250, 0.55);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 24px rgba(61, 41, 20, 0.1);
  overflow: hidden;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding: 18px 24px;
  border-bottom: 1px solid rgba(139, 90, 43, 0.1);
}

.search-wrap {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  max-width: 760px;
  flex: 1;
  min-width: 220px;
}

.search-icon {
  color: #8b5a2b;
  font-size: 16px;
  opacity: 0.8;
}

.search-input {
  flex: 1;
  min-width: 160px;
}

.search-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.7);
  border-radius: 10px;
  border: 1px solid rgba(139, 90, 43, 0.2);
  box-shadow: none;
}

.search-input :deep(.el-input__wrapper:hover),
.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(232, 165, 75, 0.5);
  box-shadow: 0 0 0 1px rgba(232, 165, 75, 0.2);
}

.filter-select :deep(.el-select__wrapper) {
  border-radius: 10px !important;
  background: rgba(255, 255, 255, 0.7) !important;
  border: 1.5px solid rgba(100, 70, 40, 0.5) !important;
  box-shadow: none !important;
}

.filter-select :deep(.el-select__wrapper:hover),
.filter-select :deep(.el-select__wrapper.is-focus) {
  border-color: rgba(232, 165, 75, 0.7) !important;
  box-shadow: 0 0 0 2px rgba(232, 165, 75, 0.15) !important;
}

.search-btn {
  background: linear-gradient(135deg, #e8a54b, #d48232);
  border: none;
  color: #fff;
  border-radius: 10px;
  padding: 10px 18px;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(212, 130, 50, 0.3);
}

.search-btn:hover {
  background: linear-gradient(135deg, #f0b55c, #e08d3a);
  color: #fff;
  box-shadow: 0 5px 18px rgba(212, 130, 50, 0.4);
}

.table-wrap {
  padding: 0 24px 24px;
}

.data-table {
  --el-table-border-color: rgba(139, 90, 43, 0.12);
  --el-table-header-bg-color: transparent;
  background: transparent !important;
  width: 100%;
}

.data-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.data-table :deep(.el-table th.el-table__cell) {
  padding: 12px 0;
}

.data-table :deep(.el-table td.el-table__cell) {
  padding: 10px 0;
  border-bottom: 1px solid rgba(139, 90, 43, 0.06);
}

.data-table :deep(.el-table__row:hover > td) {
  background: rgba(232, 165, 75, 0.08) !important;
}

.data-table :deep(.striped-row td) {
  background: rgba(255, 250, 245, 0.5) !important;
}

.cell-code {
  font-family: 'SF Mono', 'Cascadia Code', 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  font-weight: 600;
  color: #8b5a2b;
}

.cell-name-link {
  color: #d48232;
  font-weight: 600;
  cursor: pointer;
}

.cell-name-link:hover {
  color: #e8a54b;
  text-decoration: underline;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}

.status-dot.enabled {
  background: #52c41a;
  box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.3);
}

.status-dot.disabled {
  background: #ff4d4f;
  box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.3);
}

.status-text {
  font-size: 13px;
  color: #5c4a32;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.pagination-wrap :deep(.el-pagination) {
  --el-pagination-button-bg-color: rgba(255, 255, 255, 0.8);
  --el-pagination-hover-color: #e8a54b;
}

.pagination-wrap :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #e8a54b, #d48232);
}

.pagination-wrap :deep(.el-pagination .el-pagination__total) {
  color: #5c4a32;
  font-weight: 500;
}

/* 对话框样式 */
.stock-warning-dialog.edit-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 252, 250, 0.98);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 40px rgba(61, 41, 20, 0.15), 0 0 0 1px rgba(139, 90, 43, 0.08);
}

.stock-warning-dialog.edit-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  margin: 0;
  border-bottom: 1px solid rgba(139, 90, 43, 0.12);
  background: rgba(255, 250, 245, 0.5);
}

.edit-dialog-header {
  display: flex;
  align-items: center;
  gap: 14px;
}

.dialog-icon {
  width: 44px;
  height: 44px;
  line-height: 44px;
  text-align: center;
  font-size: 20px;
  color: #fff;
  background: linear-gradient(135deg, #e8a54b, #d48232);
  border-radius: 12px;
  box-shadow: 0 4px 14px rgba(212, 130, 50, 0.3);
}

.dialog-title {
  display: block;
  font-size: 17px;
  font-weight: 700;
  color: #2c1810;
}

.dialog-subtitle {
  font-size: 12px;
  color: #5c4a32;
  margin-top: 2px;
}

.stock-warning-dialog.edit-dialog :deep(.el-dialog__body) {
  padding: 20px 24px 8px;
}

.edit-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.edit-form :deep(.el-form-item__label) {
  color: #5c4a32;
  font-weight: 500;
  font-size: 13px;
}

.edit-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  border: 1px solid rgba(139, 90, 43, 0.2);
  background: rgba(255, 255, 255, 0.8);
  box-shadow: none;
}

.edit-form :deep(.el-input__wrapper:hover),
.edit-form :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(232, 165, 75, 0.5);
  box-shadow: 0 0 0 1px rgba(232, 165, 75, 0.15);
}

.edit-form :deep(.el-input.input-disabled .el-input__wrapper) {
  background: rgba(245, 242, 238, 0.8);
  border-color: rgba(139, 90, 43, 0.12);
  color: #5c4a32;
}

.edit-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 1px solid rgba(139, 90, 43, 0.2);
  background: rgba(255, 255, 255, 0.8);
}

.stock-warning-dialog.edit-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 24px;
  border-top: 1px solid rgba(139, 90, 43, 0.08);
}

.edit-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel {
  border-radius: 10px;
  padding: 10px 20px;
  border: 1px solid rgba(139, 90, 43, 0.3);
  color: #5c4a32;
  background: rgba(255, 255, 255, 0.8);
}

.btn-cancel:hover {
  border-color: rgba(232, 165, 75, 0.5);
  color: #8b5a2b;
  background: rgba(255, 250, 245, 0.9);
}

.btn-save {
  border-radius: 10px;
  padding: 10px 24px;
  border: none;
  color: #fff;
  background: linear-gradient(135deg, #e8a54b, #d48232);
  box-shadow: 0 4px 14px rgba(212, 130, 50, 0.3);
}

.btn-save:hover {
  background: linear-gradient(135deg, #f0b55c, #e08d3a);
  color: #fff;
  box-shadow: 0 5px 18px rgba(212, 130, 50, 0.4);
}

.detail-body {
  min-height: 120px;
}

.detail-desc :deep(.el-descriptions__label) {
  width: 100px;
  color: #5c4a32;
}
</style>