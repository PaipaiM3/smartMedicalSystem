<template>
  <div class="user-management-page">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="header-left">
        <i class="fa-solid fa-users page-icon"></i>
        <div>
          <h2 class="page-title">用户管理</h2>
          <p class="page-desc">管理系统用户与角色分配</p>
        </div>
      </div>
    </div>

    <!-- 主内容卡片 -->
    <div class="content-card">
      <!-- 搜索栏 -->
      <div class="toolbar">
        <div class="search-wrap">
          <i class="fa-solid fa-magnifying-glass search-icon"></i>
          <el-input
            v-model="keyword"
            placeholder="搜索用户名、姓名或手机号"
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

          <el-select
            v-model="roleCodeFilter"
            placeholder="角色"
            clearable
            size="large"
            class="filter-select"
            style="width: 220px"
            @change="loadData"
          >
            <el-option
              v-for="r in roleOptions"
              :key="r.roleId"
              :label="`${r.roleName} (${r.roleCode})`"
              :value="r.roleCode"
            />
          </el-select>
        </div>
        <el-button class="add-user-btn" @click="openCreateDialog">
          <i class="fa-solid fa-user-plus"></i>
          新增用户
        </el-button>
      </div>

      <!-- 表格 -->
      <div class="table-wrap" v-loading="loading" element-loading-text="加载中...">
        <el-table
          :data="tableData"
          class="data-table"
          :header-cell-style="headerCellStyle"
          :row-class-name="tableRowClassName"
        >
          <el-table-column prop="userId" label="用户ID" width="72" align="center">
            <template #default="{ row }">
              <span class="cell-id">{{ row.userId }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" min-width="100">
            <template #default="{ row }">
              <span class="cell-username clickable" @click="openUserDetail(row)">
                {{ row.username }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" min-width="90" />
          <el-table-column prop="mobilePhone" label="手机号" min-width="120">
            <template #default="{ row }">
              <span class="cell-phone">{{ row.mobilePhone || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
          <el-table-column label="角色" min-width="200">
            <template #default="{ row }">
              <div class="role-tags">
                <span
                  v-for="r in (row.roleNames || [])"
                  :key="r"
                  class="role-tag"
                >
                  {{ r }}
                </span>
                <span v-if="!row.roleNames?.length" class="no-role">-</span>
              </div>
            </template>
          </el-table-column>
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
          <el-table-column label="操作" width="220" align="center" fixed="right">
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
              <el-button link type="danger" size="small" @click="confirmDelete(row)">
                <i class="fa-solid fa-trash"></i> 删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
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

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      width="520px"
      class="user-mgmt-dialog create-dialog"
      :close-on-click-modal="false"
      align-center
      @close="resetCreateForm"
    >
      <template #header>
        <div class="edit-dialog-header">
          <i class="fa-solid fa-user-plus dialog-icon"></i>
          <div>
            <span class="dialog-title">新增用户</span>
            <span class="dialog-subtitle">填写账号信息并分配角色</span>
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
        <el-form-item label="用户名" prop="username" required>
          <el-input
            v-model="createForm.username"
            placeholder="3～32 个字符"
            maxlength="32"
            clearable
            class="form-input"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password" required>
          <el-input
            v-model="createForm.password"
            type="password"
            placeholder="至少 6 位"
            show-password
            maxlength="64"
            class="form-input"
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" required>
          <el-input
            v-model="createForm.confirmPassword"
            type="password"
            placeholder="再次输入密码"
            show-password
            maxlength="64"
            class="form-input"
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input
            v-model="createForm.name"
            placeholder="留空则默认与用户名相同"
            maxlength="50"
            show-word-limit
            class="form-input"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="mobilePhone">
          <el-input
            v-model="createForm.mobilePhone"
            placeholder="选填，11 位手机号"
            maxlength="11"
            clearable
            class="form-input"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="createForm.email"
            placeholder="选填"
            maxlength="100"
            clearable
            class="form-input"
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds" required>
          <el-select
            v-model="createForm.roleIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择角色（可多选）"
            class="role-select"
          >
            <el-option
              v-for="r in roleOptions"
              :key="r.roleId"
              :label="`${r.roleName} (${r.roleCode})`"
              :value="r.roleId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="edit-dialog-footer">
          <el-button class="btn-cancel" @click="createDialogVisible = false">取消</el-button>
          <el-button class="btn-save" :loading="createSubmitting" @click="submitCreate">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="editDialogVisible"
      width="520px"
      class="user-mgmt-dialog"
      :close-on-click-modal="false"
      align-center
      @close="resetEditForm"
    >
      <template #header>
        <div class="edit-dialog-header">
          <i class="fa-solid fa-pen-to-square dialog-icon"></i>
          <div>
            <span class="dialog-title">编辑用户信息</span>
            <span class="dialog-subtitle">修改姓名、联系方式与角色</span>
          </div>
        </div>
      </template>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-position="top" class="edit-form">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled class="input-disabled" />
        </el-form-item>
        <el-form-item label="姓名" prop="name" required>
          <el-input v-model="editForm.name" placeholder="请输入姓名" maxlength="50" show-word-limit class="form-input" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobilePhone">
          <el-input v-model="editForm.mobilePhone" placeholder="请输入手机号" maxlength="20" class="form-input" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" maxlength="100" class="form-input" />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds" required>
          <el-select
            v-model="editForm.roleIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择角色（可多选）"
            class="role-select"
          >
            <el-option
              v-for="r in roleOptions"
              :key="r.roleId"
              :label="`${r.roleName} (${r.roleCode})`"
              :value="r.roleId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="edit-dialog-footer">
          <el-button class="btn-cancel" @click="editDialogVisible = false">取消</el-button>
          <el-button class="btn-save" :loading="editSubmitting" @click="submitEdit">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 用户详情对话框（预留字段：结合开发文档） -->
    <el-dialog
      v-model="userDetailDialogVisible"
      width="760px"
      class="user-mgmt-dialog"
      :close-on-click-modal="false"
      align-center
      @close="resetUserDetail"
    >
      <template #header>
        <div class="edit-dialog-header">
          <i class="fa-solid fa-user-tie dialog-icon"></i>
          <div>
            <span class="dialog-title">用户详情</span>
            <span class="dialog-subtitle">预留字段展示（系统用户 / 患者 / 医生）</span>
          </div>
        </div>
      </template>

      <div class="detail-content">
        <el-descriptions title="" :column="2" border size="small" class="detail-desc">
          <el-descriptions-item label="用户ID">{{ userDetail.userId ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ userDetail.username ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ userDetail.name ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <span class="status-text">
              {{ userDetail.status === 1 ? '启用' : userDetail.status === 0 ? '禁用' : '-' }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userDetail.mobilePhone ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ userDetail.email ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ userDetail.createdTime ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <div class="role-tags">
              <span v-for="r in (userDetail.roleNames || [])" :key="r" class="role-tag">
                {{ r }}
              </span>
              <span v-if="!userDetail.roleNames?.length" class="no-role">-</span>
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="isPatient">
          <el-divider content-position="left">患者信息（patient）</el-divider>
          <el-descriptions :column="2" border size="small" class="detail-desc">
            <el-descriptions-item label="patient_no">-（预留）</el-descriptions-item>
            <el-descriptions-item label="gender">-（预留）</el-descriptions-item>
            <el-descriptions-item label="birth_date">-（预留）</el-descriptions-item>
            <el-descriptions-item label="phone">-（预留）</el-descriptions-item>
            <el-descriptions-item label="id_card">-（预留）</el-descriptions-item>
            <el-descriptions-item label="address">-（预留）</el-descriptions-item>
            <el-descriptions-item label="blood_type">-（预留）</el-descriptions-item>
            <el-descriptions-item label="status">-（预留）</el-descriptions-item>
            <el-descriptions-item label="allergy_history">-（预留）</el-descriptions-item>
            <el-descriptions-item label="chronic_diseases">-（预留）</el-descriptions-item>
            <el-descriptions-item label="emergency_contact">-（预留）</el-descriptions-item>
            <el-descriptions-item label="emergency_phone">-（预留）</el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-else-if="isDoctor">
          <el-divider content-position="left">医生信息（doctor）</el-divider>
          <el-descriptions :column="2" border size="small" class="detail-desc">
            <el-descriptions-item label="doctor_no">-（预留）</el-descriptions-item>
            <el-descriptions-item label="dept_id">-（预留）</el-descriptions-item>
            <el-descriptions-item label="title">-（预留）</el-descriptions-item>
            <el-descriptions-item label="specialty">-（预留）</el-descriptions-item>
            <el-descriptions-item label="phone">-（预留）</el-descriptions-item>
            <el-descriptions-item label="consultation_fee">-（预留）</el-descriptions-item>
            <el-descriptions-item label="status">-（预留）</el-descriptions-item>
            <el-descriptions-item label="sort_order">-（预留）</el-descriptions-item>
            <el-descriptions-item label="introduction">-（预留）</el-descriptions-item>
            <el-descriptions-item label="remark">-（预留）</el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-else-if="isMedicalStaff">
          <el-divider content-position="left">医务人员信息（护士/药师）</el-divider>
          <el-descriptions :column="2" border size="small" class="detail-desc">
            <el-descriptions-item label="头像URL">-（预留）</el-descriptions-item>
            <el-descriptions-item label="科室/部门ID">-（预留）</el-descriptions-item>
            <el-descriptions-item label="上次登录IP">-（预留）</el-descriptions-item>
            <el-descriptions-item label="上次登录时间">-（预留）</el-descriptions-item>
            <el-descriptions-item label="备注">-（预留）</el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-else class="detail-empty">
          <el-divider content-position="left">员工信息（sys_user）</el-divider>
          <el-descriptions :column="2" border size="small" class="detail-desc">
            <el-descriptions-item label="头像URL">-（预留）</el-descriptions-item>
            <el-descriptions-item label="科室/部门ID">-（预留）</el-descriptions-item>
            <el-descriptions-item label="上次登录IP">-（预留）</el-descriptions-item>
            <el-descriptions-item label="上次登录时间">-（预留）</el-descriptions-item>
            <el-descriptions-item label="备注">-（预留）</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <template #footer>
        <div class="edit-dialog-footer">
          <el-button class="btn-cancel" @click="userDetailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserPage, getRoleList, createUser, updateUser, updateUserStatus, deleteUser } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const statusFilter = ref(null)
const roleCodeFilter = ref('')

const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editSubmitting = ref(false)
const editForm = ref({
  userId: null,
  username: '',
  name: '',
  mobilePhone: '',
  email: '',
  roleIds: []
})
const editRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleIds: [
    {
      type: 'array',
      required: true,
      message: '请至少选择一个角色',
      trigger: 'change'
    },
    {
      type: 'array',
      min: 1,
      message: '请至少选择一个角色',
      trigger: 'change'
    }
  ]
}

const userDetailDialogVisible = ref(false)
const userDetail = reactive({
  userId: null,
  username: '',
  name: '',
  email: '',
  mobilePhone: '',
  status: null,
  createdTime: '',
  roleNames: []
})

const roleText = computed(() => (userDetail.roleNames || []).join(' '))
const isPatient = computed(() => roleText.value.includes('患者'))
const isDoctor = computed(() => roleText.value.includes('医生'))
const isMedicalStaff = computed(() => roleText.value.includes('护士') || roleText.value.includes('药师'))

const resetUserDetail = () => {
  userDetail.userId = null
  userDetail.username = ''
  userDetail.name = ''
  userDetail.email = ''
  userDetail.mobilePhone = ''
  userDetail.status = null
  userDetail.createdTime = ''
  userDetail.roleNames = []
}

const createDialogVisible = ref(false)
const createFormRef = ref(null)
const createSubmitting = ref(false)
const roleOptions = ref([])
const createForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  mobilePhone: '',
  email: '',
  roleIds: []
})

const createRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 32, message: '用户名为 3～32 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码为 6～64 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== createForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  name: [{ max: 50, message: '姓名最多 50 个字符', trigger: 'blur' }],
  mobilePhone: [
    {
      validator: (_rule, value, callback) => {
        if (!value || String(value).trim() === '') {
          callback()
          return
        }
        if (!/^1\d{10}$/.test(String(value).trim())) {
          callback(new Error('请输入 11 位有效手机号'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    {
      validator: (_rule, value, callback) => {
        if (!value || String(value).trim() === '') {
          callback()
          return
        }
        const v = String(value).trim()
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) {
          callback(new Error('邮箱格式不正确'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  roleIds: [
    {
      type: 'array',
      required: true,
      message: '请至少选择一个角色',
      trigger: 'change'
    },
    {
      type: 'array',
      min: 1,
      message: '请至少选择一个角色',
      trigger: 'change'
    }
  ]
}

const loadRoleOptions = async () => {
  try {
    const list = await getRoleList()
    roleOptions.value = (list || []).filter((r) => r.status === 1)
  } catch {
    roleOptions.value = []
  }
}

const openCreateDialog = async () => {
  await loadRoleOptions()
  createDialogVisible.value = true
}

const resetCreateForm = () => {
  createForm.username = ''
  createForm.password = ''
  createForm.confirmPassword = ''
  createForm.name = ''
  createForm.mobilePhone = ''
  createForm.email = ''
  createForm.roleIds = []
  createFormRef.value?.resetFields()
}

const submitCreate = async () => {
  try {
    await createFormRef.value?.validate()
  } catch {
    return
  }
  createSubmitting.value = true
  try {
    const payload = {
      username: createForm.username.trim(),
      password: createForm.password,
      name: createForm.name.trim() || undefined,
      mobilePhone: createForm.mobilePhone.trim() || undefined,
      email: createForm.email.trim() || undefined,
      roleIds: [...createForm.roleIds]
    }
    await createUser(payload)
    ElMessage.success('新增用户成功')
    createDialogVisible.value = false
    loadData()
  } catch (e) {
    // 错误已由 request 拦截器处理
  } finally {
    createSubmitting.value = false
  }
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

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserPage({
      current: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value ?? undefined,
      roleCode: roleCodeFilter.value || undefined
    })
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const openEditDialog = async (row) => {
  await loadRoleOptions()
  editForm.value = {
    userId: row.userId,
    username: row.username,
    name: row.name || '',
    mobilePhone: row.mobilePhone || '',
    email: row.email || '',
    roleIds: Array.isArray(row.roleIds) ? [...row.roleIds] : []
  }
  editDialogVisible.value = true
}

const openUserDetail = (row) => {
  userDetail.userId = row.userId
  userDetail.username = row.username
  userDetail.name = row.name || ''
  userDetail.email = row.email || ''
  userDetail.mobilePhone = row.mobilePhone || ''
  userDetail.status = row.status
  userDetail.createdTime = row.createdTime || ''
  userDetail.roleNames = row.roleNames || []
  userDetailDialogVisible.value = true
}

const resetEditForm = () => {
  editForm.value = {
    userId: null,
    username: '',
    name: '',
    mobilePhone: '',
    email: '',
    roleIds: []
  }
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
    await updateUser(editForm.value.userId, {
      name: editForm.value.name,
      mobilePhone: editForm.value.mobilePhone,
      email: editForm.value.email,
      roleIds: [...editForm.value.roleIds]
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
      `确定要${action}用户「${row.name || row.username}」吗？`,
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
    await updateUserStatus(row.userId, newStatus)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {
    // 错误已由 request 拦截器处理
  }
}

const confirmDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `删除后不可恢复，确定删除用户「${row.name || row.username}」吗？`,
      '删除用户',
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
    await deleteUser(row.userId)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // 错误已由 request 拦截器处理
  }
}

onMounted(() => {
  loadRoleOptions()
  loadData()
})
</script>

<style scoped>
.user-management-page {
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

/* 筛选下拉：沿用搜索输入框的毛玻璃样式 */
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

.cell-username {
  font-weight: 500;
  color: #2c1810;
}

.cell-username.clickable {
  cursor: pointer;
  text-decoration: underline;
  text-decoration-color: rgba(212, 130, 50, 0.6);
}

.cell-phone,
.cell-time {
  color: #5c4a32;
  font-size: 13px;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.role-tag {
  display: inline-block;
  padding: 4px 10px;
  font-size: 12px;
  color: #8b5a2b;
  background: rgba(232, 165, 75, 0.2);
  border-radius: 8px;
  border: 1px solid rgba(212, 130, 50, 0.25);
}

.no-role {
  color: #999;
  font-size: 12px;
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

/* 用户表单对话框（新增 / 编辑） */
.user-mgmt-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 252, 250, 0.98);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 40px rgba(61, 41, 20, 0.15), 0 0 0 1px rgba(139, 90, 43, 0.08);
}
.user-mgmt-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  margin: 0;
  border-bottom: 1px solid rgba(139, 90, 43, 0.12);
  background: rgba(255, 250, 245, 0.5);
}
.user-mgmt-dialog :deep(.el-dialog__headerbtn) {
  width: 36px;
  height: 36px;
  top: 18px;
  right: 20px;
  color: #8b5a2b;
}
.user-mgmt-dialog :deep(.el-dialog__headerbtn:hover) {
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

.user-mgmt-dialog :deep(.el-dialog__body) {
  padding: 24px 24px 8px;
  max-height: calc(100vh - 220px);
  overflow-y: auto;
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
.edit-form :deep(.el-form-item.is-required .el-form-item__label::before) {
  color: #d48232;
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

.edit-form :deep(.role-select) {
  width: 100%;
}

.edit-form :deep(.role-select .el-input__wrapper) {
  border-radius: 10px;
  border: 1px solid rgba(139, 90, 43, 0.2);
  background: rgba(255, 255, 255, 0.8);
  box-shadow: none;
}

.user-mgmt-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 24px;
  border-top: 1px solid rgba(139, 90, 43, 0.08);
}

.detail-empty {
  padding: 8px 4px 0 4px;
  color: #5c4a32;
  font-size: 13px;
  opacity: 0.9;
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
