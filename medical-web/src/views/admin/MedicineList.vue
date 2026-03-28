<template>
  <div class="medicine-list-page">
    <div class="page-header">
      <div class="header-left">
        <i class="fa-solid fa-pills page-icon"></i>
        <div>
          <h2 class="page-title">药品列表</h2>
          <p class="page-desc">
            查询药品编码、分类、规格与库存信息
            <span v-if="stockWarningSwitch" class="page-desc-badge">当前：仅显示库存预警</span>
          </p>
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
              class="filter-select filter-select-wide"
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

          <el-select
              v-model="statusFilter"
              placeholder="状态"
              clearable
              size="large"
              class="filter-select"
              style="width: 120px"
              @change="loadData"
          >
            <el-option :value="1" label="在用" />
            <el-option :value="0" label="停用" />
          </el-select>

          <span class="stock-warning-switch-wrap">
            <el-switch v-model="stockWarningSwitch" @change="onStockWarningChange" />
            <span class="stock-warning-label">仅看库存预警</span>
          </span>
        </div>
        <el-button class="add-user-btn" @click="openCreateDialog">
          <i class="fa-solid fa-plus"></i>
          新增药品
        </el-button>
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
          <el-table-column prop="name" label="药品名称" min-width="140">
            <template #default="{ row }">
              <span class="cell-name-link" @click="openDetail(row)">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="commonName" label="通用名" min-width="120" />
          <el-table-column prop="categoryName" label="分类" width="130">
            <template #default="{ row }">
              {{ row.categoryName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="spec" label="规格" min-width="120" />
          <el-table-column prop="unit" label="单位" width="64" align="center" />
          <el-table-column prop="manufacturer" label="生产厂家" min-width="160" />
          <el-table-column prop="approvalNo" label="批准文号" min-width="130" />
          <el-table-column prop="unitPrice" label="零售价" width="88" align="right">
            <template #default="{ row }">
              {{ formatMoney(row.unitPrice) }}
            </template>
          </el-table-column>
          <el-table-column prop="stockQuantity" label="库存" width="72" align="center" />
          <el-table-column prop="minStock" label="最低库存" width="88" align="center" />
          <el-table-column prop="status" label="状态" width="72" align="center">
            <template #default="{ row }">
              <span :class="['status-dot', row.status === 1 ? 'enabled' : 'disabled']"></span>
              <span class="status-text">{{ row.status === 1 ? '在用' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdTime" label="创建时间" width="160" align="center">
            <template #default="{ row }">
              <span class="cell-time">{{ row.createdTime ?? '-' }}</span>
            </template>
          </el-table-column>
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

    <!-- 新增药品 -->
    <el-dialog
        v-model="createDialogVisible"
        width="780px"
        :append-to-body="true"
        :modal-append-to-body="true"
        class="medicine-dialog edit-dialog create-medicine-dialog"
        :close-on-click-modal="false"
        align-center
        @close="resetCreateForm"
    >
      <template #header>
        <div class="edit-dialog-header">
          <i class="fa-solid fa-capsules dialog-icon"></i>
          <div>
            <span class="dialog-title">{{ isEditMode ? '编辑药品' : '新增药品' }}</span>
            <span class="dialog-subtitle">
              {{ isEditMode ? '修改药品信息（药品编码不可更改）' : '填写编码、分类、规格与价格等信息' }}
            </span>
          </div>
        </div>
      </template>
      <el-form
          ref="createFormRef"
          :model="createForm"
          :rules="createRules"
          label-position="top"
          class="edit-form create-medicine-form"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="药品编码" prop="medicineCode" required>
              <el-input
                  v-model="createForm.medicineCode"
                  placeholder="唯一编码，如 MED000999"
                  maxlength="50"
                  clearable
                  :disabled="isEditMode"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="药品名称" prop="name" required>
              <el-input v-model="createForm.name" placeholder="商品名/制剂名" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="通用名" prop="commonName">
              <el-input v-model="createForm.commonName" placeholder="选填" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="药品分类" prop="categoryId" required>
              <el-select v-model="createForm.categoryId" placeholder="请选择" filterable style="width: 100%">
                <el-option
                    v-for="c in categoryOptions"
                    :key="c.categoryId"
                    :label="c.name"
                    :value="c.categoryId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="规格" prop="spec" required>
              <el-input v-model="createForm.spec" placeholder="如 0.25g*24粒" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit" required>
              <el-input v-model="createForm.unit" placeholder="盒/瓶/支等" maxlength="20" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="生产厂家" prop="manufacturer" required>
          <el-input v-model="createForm.manufacturer" placeholder="生产厂家全称" maxlength="200" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="批准文号" prop="approvalNo">
              <el-input v-model="createForm.approvalNo" placeholder="选填" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="createForm.status">
                <el-radio :label="1">在用</el-radio>
                <el-radio :label="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="零售价" prop="unitPrice" required>
              <el-input-number v-model="createForm.unitPrice" :min="0" :precision="2" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成本价" prop="costPrice">
              <el-input-number v-model="createForm.costPrice" :min="0" :precision="2" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="库存数量" prop="stockQuantity" required>
              <el-input-number v-model="createForm.stockQuantity" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最低库存" prop="minStock" required>
              <el-input-number v-model="createForm.minStock" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="edit-dialog-footer">
          <el-button class="btn-cancel" @click="createDialogVisible = false">取消</el-button>
          <el-button class="btn-save" :loading="createSubmitting" @click="submitMedicineForm">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 药品详情 -->
    <el-dialog
        v-model="detailDialogVisible"
        width="640px"
        :append-to-body="true"
        :modal-append-to-body="true"
        class="medicine-dialog edit-dialog"
        :close-on-click-modal="false"
        align-center
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
          <el-descriptions-item label="批准文号" :span="2">{{ detail.approvalNo ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="零售价">{{ formatMoney(detail.unitPrice) }}</el-descriptions-item>
          <el-descriptions-item label="成本价">{{ formatMoney(detail.costPrice) }}</el-descriptions-item>
          <el-descriptions-item label="库存数量">{{ detail.stockQuantity ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="最低库存">{{ detail.minStock ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ detail.status === 1 ? '在用' : detail.status === 0 ? '停用' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createdTime ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ detail.updatedTime ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <div class="edit-dialog-footer">
          <el-button class="btn-cancel" @click="detailDialogVisible = false">关闭</el-button>
          <el-button class="btn-save" :disabled="detailLoading || !detail.medicineId" @click="openEditFromDetail">
            编辑
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getMedicinePage,
  getMedicineStockWarning,
  getMedicineCategories,
  createMedicine,
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
const statusFilter = ref(null)
const categoryOptions = ref([])
const stockWarningSwitch = ref(false)

const createDialogVisible = ref(false)
const isEditMode = ref(false)
const editingMedicineId = ref(null)
const createFormRef = ref(null)
const createSubmitting = ref(false)
const createForm = reactive({
  medicineCode: '',
  name: '',
  commonName: '',
  categoryId: null,
  spec: '',
  unit: '',
  manufacturer: '',
  approvalNo: '',
  unitPrice: 0,
  costPrice: undefined,
  stockQuantity: 0,
  minStock: 10,
  status: 1,
  remark: ''
})

const createRules = {
  medicineCode: [{ required: true, message: '请输入药品编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  spec: [{ required: true, message: '请输入规格', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  manufacturer: [{ required: true, message: '请输入生产厂家', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请填写零售价', trigger: 'change' }],
  stockQuantity: [{ required: true, message: '请填写库存', trigger: 'change' }],
  minStock: [{ required: true, message: '请填写最低库存', trigger: 'change' }]
}

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

const isStockWarningRow = (row) => {
  const sq = row.stockQuantity
  const ms = row.minStock
  if (sq == null || ms == null) return false
  return Number(sq) <= Number(ms)
}

const tableRowClassName = ({ row, rowIndex }) => {
  const parts = []
  if (rowIndex % 2 === 1) parts.push('striped-row')
  if (isStockWarningRow(row)) parts.push('row-stock-warning')
  return parts.join(' ')
}

const onStockWarningChange = () => {
  currentPage.value = 1
  loadData()
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
    categoryId: categoryIdFilter.value ?? undefined,
    status: statusFilter.value ?? undefined
  }
  try {
    const res = stockWarningSwitch.value
        ? await getMedicineStockWarning(params)
        : await getMedicinePage(params)
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const resetCreateForm = () => {
  isEditMode.value = false
  editingMedicineId.value = null
  createForm.medicineCode = ''
  createForm.name = ''
  createForm.commonName = ''
  createForm.categoryId = null
  createForm.spec = ''
  createForm.unit = ''
  createForm.manufacturer = ''
  createForm.approvalNo = ''
  createForm.unitPrice = 0
  createForm.costPrice = undefined
  createForm.stockQuantity = 0
  createForm.minStock = 10
  createForm.status = 1
  createForm.remark = ''
  createFormRef.value?.resetFields()
}

const fillFormFromDetail = (d) => {
  createForm.medicineCode = d.medicineCode ?? ''
  createForm.name = d.name ?? ''
  createForm.commonName = d.commonName ?? ''
  createForm.categoryId = d.categoryId ?? null
  createForm.spec = d.spec ?? ''
  createForm.unit = d.unit ?? ''
  createForm.manufacturer = d.manufacturer ?? ''
  createForm.approvalNo = d.approvalNo ?? ''
  createForm.unitPrice = d.unitPrice != null ? Number(d.unitPrice) : 0
  createForm.costPrice = d.costPrice != null ? Number(d.costPrice) : undefined
  createForm.stockQuantity = d.stockQuantity != null ? Number(d.stockQuantity) : 0
  createForm.minStock = d.minStock != null ? Number(d.minStock) : 10
  createForm.status = d.status === 0 ? 0 : 1
  createForm.remark = d.remark ?? ''
}

const openCreateDialog = () => {
  resetCreateForm()
  createDialogVisible.value = true
}

const openEditFromDetail = () => {
  const d = detail.value
  if (!d?.medicineId) return
  isEditMode.value = true
  editingMedicineId.value = d.medicineId
  fillFormFromDetail(d)
  detailDialogVisible.value = false
  createDialogVisible.value = true
}

const submitMedicineForm = async () => {
  try {
    await createFormRef.value?.validate()
  } catch {
    return
  }
  createSubmitting.value = true
  const body = {
    name: createForm.name.trim(),
    commonName: createForm.commonName?.trim() || undefined,
    categoryId: createForm.categoryId,
    spec: createForm.spec.trim(),
    unit: createForm.unit.trim(),
    manufacturer: createForm.manufacturer.trim(),
    approvalNo: createForm.approvalNo?.trim() || undefined,
    unitPrice: createForm.unitPrice,
    costPrice: createForm.costPrice,
    stockQuantity: createForm.stockQuantity,
    minStock: createForm.minStock,
    status: createForm.status,
    remark: createForm.remark?.trim() || undefined
  }
  try {
    if (isEditMode.value && editingMedicineId.value != null) {
      await updateMedicine(editingMedicineId.value, body)
      ElMessage.success('保存成功')
    } else {
      await createMedicine({
        medicineCode: createForm.medicineCode.trim(),
        ...body
      })
      ElMessage.success('新增成功')
      currentPage.value = 1
    }
    createDialogVisible.value = false
    resetCreateForm()
    loadData()
  } catch {
    // 拦截器已提示
  } finally {
    createSubmitting.value = false
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

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
/* 样式保持不变 */
.medicine-list-page {
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

.page-desc-badge {
  margin-left: 10px;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: 600;
  color: #b45309;
  background: rgba(255, 183, 120, 0.35);
  border-radius: 6px;
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
  max-width: 1100px;
  flex: 1;
  min-width: 220px;
}

.stock-warning-switch-wrap {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 4px;
  white-space: nowrap;
}

.stock-warning-label {
  font-size: 13px;
  color: #5c4a32;
  user-select: none;
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

.add-user-btn {
  background: linear-gradient(135deg, #e8a54b, #d48232);
  border: none;
  color: #fff;
  border-radius: 10px;
  padding: 10px 18px;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(212, 130, 50, 0.3);
  flex-shrink: 0;
}

.add-user-btn:hover {
  background: linear-gradient(135deg, #f0b55c, #e08d3a);
  color: #fff;
  box-shadow: 0 5px 18px rgba(212, 130, 50, 0.4);
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

.data-table :deep(.row-stock-warning td) {
  background: rgba(255, 200, 150, 0.35) !important;
}

.data-table :deep(.el-table__row.row-stock-warning:hover > td) {
  background: rgba(255, 183, 120, 0.45) !important;
}

.cell-code {
  font-family: 'SF Mono', 'Cascadia Code', 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  font-weight: 600;
  color: #8b5a2b;
}

.cell-time {
  color: #5c4a32;
  font-size: 13px;
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
.medicine-dialog.edit-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 252, 250, 0.98);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 40px rgba(61, 41, 20, 0.15), 0 0 0 1px rgba(139, 90, 43, 0.08);
}

.medicine-dialog.edit-dialog :deep(.el-dialog__header) {
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

.medicine-dialog.edit-dialog :deep(.el-dialog__body) {
  padding: 20px 24px 8px;
}

.create-medicine-dialog :deep(.el-dialog__body) {
  max-height: min(520px, calc(100vh - 200px));
  overflow-y: auto;
  padding: 12px 24px 4px;
}

.edit-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.create-medicine-dialog .create-medicine-form :deep(.el-form-item) {
  margin-bottom: 14px;
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

.edit-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 1px solid rgba(139, 90, 43, 0.2);
  background: rgba(255, 255, 255, 0.8);
}

.medicine-dialog.edit-dialog :deep(.el-dialog__footer) {
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

.btn-save {
  border-radius: 10px;
  padding: 10px 24px;
  border: none;
  color: #fff;
  background: linear-gradient(135deg, #e8a54b, #d48232);
  box-shadow: 0 4px 14px rgba(212, 130, 50, 0.3);
}

.detail-body {
  min-height: 120px;
}

.detail-desc :deep(.el-descriptions__label) {
  width: 100px;
  color: #5c4a32;
}
</style>