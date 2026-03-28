<template>
  <div class="role-management-page">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="header-left">
        <i class="fa-solid fa-list-check page-icon"></i>
        <div>
          <h2 class="page-title">角色管理</h2>
          <p class="page-desc">管理系统角色与权限配置</p>
        </div>
      </div>
    </div>

    <!-- 主内容卡片 -->
    <div class="content-card">
      <div class="toolbar">
        <div class="search-wrap">
          <i class="fa-solid fa-magnifying-glass search-icon"></i>
          <el-input
            v-model="keyword"
            placeholder="搜索角色代码或名称"
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
            v-model="statusFilter"
            placeholder="状态"
            clearable
            size="large"
            class="filter-select"
            style="width: 120px"
            @change="loadData"
          >
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </div>
        <el-button v-if="isSuperAdmin" class="add-user-btn" @click="openCreateDialog">
          <i class="fa-solid fa-plus"></i>
          新增角色
        </el-button>
      </div>

      <div class="table-wrap" v-loading="loading" element-loading-text="加载中...">
        <el-table
          :data="tableData"
          class="data-table"
          :header-cell-style="headerCellStyle"
          :row-class-name="tableRowClassName"
        >
          <el-table-column prop="roleId" label="角色ID" width="72" align="center">
            <template #default="{ row }">
              <span class="cell-id">{{ row.roleId }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="roleCode" label="角色代码" min-width="160">
            <template #default="{ row }">
              <span class="cell-role-code">{{ row.roleCode }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="roleName" label="角色名称" min-width="120">
            <template #default="{ row }">
              <span class="cell-name">{{ row.roleName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="88" align="center">
            <template #default="{ row }">
              <span :class="['status-dot', row.status === 1 ? 'enabled' : 'disabled']"></span>
              <span class="status-text">{{ row.status === 1 ? '启用' : '禁用' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdTime" label="创建时间" width="165" align="center">
            <template #default="{ row }">
              <span class="cell-time">{{ row.createdTime }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="isSuperAdmin ? 220 : 160" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openEditDialog(row)">
                <i class="fa-solid fa-pen"></i> 编辑
              </el-button>
              <el-button
                link
                :type="row.status === 1 ? 'warning' : 'success'"
                size="small"
                @click="toggleStatus(row)"
              >
                <i :class="row.status === 1 ? 'fa-solid fa-ban' : 'fa-solid fa-check'"></i>
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button
                v-if="isSuperAdmin && row.roleCode !== 'SUPER_ADMIN'"
                link
                type="danger"
                size="small"
                @click="confirmDeleteRole(row)"
              >
                <i class="fa-solid fa-trash"></i> 删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="loadData"
            @current-change="loadData"
          />
        </div>
      </div>
    </div>

    <!-- 编辑角色对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      width="440px"
      class="edit-dialog"
      :close-on-click-modal="false"
      align-center
      @close="resetEditForm"
    >
      <template #header>
        <div class="edit-dialog-header">
          <i class="fa-solid fa-pen-to-square dialog-icon"></i>
          <div>
            <span class="dialog-title">编辑角色信息</span>
            <span class="dialog-subtitle">修改角色名称、描述等</span>
          </div>
        </div>
      </template>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-position="top" class="edit-form">
        <el-form-item label="角色代码">
          <el-input v-model="editForm.roleCode" disabled class="input-disabled" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName" required>
          <el-input v-model="editForm.roleName" placeholder="请输入角色名称" maxlength="50" show-word-limit class="form-input" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入描述" maxlength="200" show-word-limit class="form-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="edit-dialog-footer">
          <el-button class="btn-cancel" @click="editDialogVisible = false">取消</el-button>
          <el-button class="btn-save" :loading="editSubmitting" @click="submitEdit">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新增角色对话框（仅超级管理员） -->
    <el-dialog
      v-if="isSuperAdmin"
      v-model="createDialogVisible"
      width="440px"
      class="edit-dialog"
      :close-on-click-modal="false"
      align-center
      @close="resetCreateForm"
    >
      <template #header>
        <div class="edit-dialog-header">
          <i class="fa-solid fa-user-shield dialog-icon"></i>
          <div>
            <span class="dialog-title">新增角色</span>
            <span class="dialog-subtitle">角色代码保存后将转为大写，且不可重复</span>
          </div>
        </div>
      </template>
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-position="top"
        class="edit-form"
      >
        <el-form-item label="角色代码" prop="roleCode" required>
          <el-input
            v-model="createForm.roleCode"
            placeholder="字母、数字、下划线，如 custom_role"
            maxlength="50"
            show-word-limit
            class="form-input"
          />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName" required>
          <el-input
            v-model="createForm.roleName"
            placeholder="请输入角色名称"
            maxlength="50"
            show-word-limit
            class="form-input"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="选填"
            maxlength="200"
            show-word-limit
            class="form-input"
          />
        </el-form-item>
        <el-form-item label="初始状态" prop="status">
          <el-radio-group v-model="createForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="edit-dialog-footer">
          <el-button class="btn-cancel" @click="createDialogVisible = false">取消</el-button>
          <el-button class="btn-save" :loading="createSubmitting" @click="submitCreate">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRolePage, createRole, updateRole, updateRoleStatus, deleteRole } from '@/api/admin'

const userInfo = computed(() => {
  try {
    return JSON.parse(sessionStorage.getItem('userInfo') || '{}')
  } catch {
    return {}
  }
})

/** 与后端 ROLE_SUPER_ADMIN 对应，登录信息中的 roles 为角色代码列表 */
const isSuperAdmin = computed(() => {
  const roles = userInfo.value?.roles
  return Array.isArray(roles) && roles.includes('SUPER_ADMIN')
})

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const statusFilter = ref(null)

const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editSubmitting = ref(false)
const editForm = ref({
  roleId: null,
  roleCode: '',
  roleName: '',
  description: ''
})
const editRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const createDialogVisible = ref(false)
const createFormRef = ref(null)
const createSubmitting = ref(false)
const createForm = ref({
  roleCode: '',
  roleName: '',
  description: '',
  status: 1
})
const createRules = {
  roleCode: [
    { required: true, message: '请输入角色代码', trigger: 'blur' },
    {
      pattern: /^[A-Za-z0-9_]+$/,
      message: '仅支持字母、数字与下划线',
      trigger: 'blur'
    },
    { min: 2, max: 50, message: '长度为 2～50 个字符', trigger: 'blur' }
  ],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

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

const resetCreateForm = () => {
  createForm.value = { roleCode: '', roleName: '', description: '', status: 1 }
  createFormRef.value?.resetFields()
}

const openCreateDialog = () => {
  createDialogVisible.value = true
  nextTick(() => {
    createForm.value = { roleCode: '', roleName: '', description: '', status: 1 }
    createFormRef.value?.resetFields()
  })
}

const submitCreate = async () => {
  try {
    await createFormRef.value?.validate()
  } catch {
    return
  }
  createSubmitting.value = true
  try {
    await createRole({
      roleCode: createForm.value.roleCode.trim(),
      roleName: createForm.value.roleName.trim(),
      description: createForm.value.description?.trim() || undefined,
      status: createForm.value.status
    })
    ElMessage.success('新增角色成功')
    createDialogVisible.value = false
    currentPage.value = 1
    loadData()
  } catch (e) {
    // 错误已由 request 拦截器处理
  } finally {
    createSubmitting.value = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRolePage({
      current: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value ?? undefined
    })
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const openEditDialog = (row) => {
  editForm.value = {
    roleId: row.roleId,
    roleCode: row.roleCode,
    roleName: row.roleName || '',
    description: row.description || ''
  }
  editDialogVisible.value = true
}

const resetEditForm = () => {
  editForm.value = { roleId: null, roleCode: '', roleName: '', description: '' }
  editFormRef.value?.resetFields()
}

const submitEdit = async () => {
  try {
    await editFormRef.value?.validate()
  } catch {
    return
  }
  editSubmitting.value = true
  try {
    await updateRole(editForm.value.roleId, {
      roleName: editForm.value.roleName,
      description: editForm.value.description
    })
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadData()
  } catch (e) {
    // 错误已由 request 拦截器处理
  } finally {
    editSubmitting.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(
      `确定要${action}角色「${row.roleName || row.roleCode}」吗？`,
      '修改状态',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }
  try {
    await updateRoleStatus(row.roleId, newStatus)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {
    // 错误已由 request 拦截器处理
  }
}

const confirmDeleteRole = async (row) => {
  try {
    await ElMessageBox.confirm(
      `删除将解除所有用户的该角色关联，且不可恢复。确定删除角色「${row.roleName || row.roleCode}」吗？`,
      '删除角色',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
  } catch {
    return
  }
  try {
    await deleteRole(row.roleId)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // 错误已由 request 拦截器处理
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.role-management-page {
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

.table-wrap {
  padding: 0 24px 24px;
}

.data-table {
  --el-table-border-color: rgba(139, 90, 43, 0.12);
  --el-table-header-bg-color: transparent;
  background: transparent !important;
}

.data-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.data-table :deep(.el-table th.el-table__cell) {
  padding: 14px 0;
}

.data-table :deep(.el-table td.el-table__cell) {
  padding: 12px 0;
  border-bottom: 1px solid rgba(139, 90, 43, 0.06);
}

.data-table :deep(.el-table__row:hover > td) {
  background: rgba(232, 165, 75, 0.08) !important;
}

.data-table :deep(.striped-row td) {
  background: rgba(255, 250, 245, 0.5) !important;
}

.data-table :deep(.striped-row:hover > td) {
  background: rgba(232, 165, 75, 0.08) !important;
}

.cell-id {
  font-weight: 600;
  color: #8b5a2b;
}

.cell-role-code {
  font-family: 'SF Mono', 'Cascadia Code', 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.5px;
  color: #2c1810;
}

.cell-name {
  font-weight: 600;
  color: #2c1810;
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

/* 操作列按钮 */
.data-table :deep(.el-button.is-link) {
  padding: 4px 8px;
  font-size: 13px;
}
.data-table :deep(.el-button.is-link[type="primary"]) {
  color: #d48232;
}
.data-table :deep(.el-button.is-link[type="primary"]:hover) {
  color: #e8a54b;
}
.data-table :deep(.el-button.is-link.el-button--danger) {
  color: #f56c6c;
}
.data-table :deep(.el-button.is-link.el-button--danger:hover) {
  color: #f89898;
}

/* 编辑对话框 */
.edit-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 252, 250, 0.98);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 40px rgba(61, 41, 20, 0.15), 0 0 0 1px rgba(139, 90, 43, 0.08);
}
.edit-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  margin: 0;
  border-bottom: 1px solid rgba(139, 90, 43, 0.12);
  background: rgba(255, 250, 245, 0.5);
}
.edit-dialog :deep(.el-dialog__headerbtn) {
  width: 36px;
  height: 36px;
  top: 18px;
  right: 20px;
  color: #8b5a2b;
}
.edit-dialog :deep(.el-dialog__headerbtn:hover) {
  color: #d48232;
  background: rgba(232, 165, 75, 0.15);
  border-radius: 8px;
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
.edit-dialog :deep(.el-dialog__body) {
  padding: 24px 24px 8px;
}
.edit-form :deep(.el-form-item) {
  margin-bottom: 18px;
}
.edit-form :deep(.el-form-item__label) {
  color: #5c4a32;
  font-weight: 500;
  font-size: 13px;
  padding-bottom: 6px;
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
.edit-form :deep(.el-input.input-disabled .el-input__wrapper) {
  background: rgba(245, 242, 238, 0.8);
  border-color: rgba(139, 90, 43, 0.12);
}
.edit-dialog :deep(.el-dialog__footer) {
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
</style>
