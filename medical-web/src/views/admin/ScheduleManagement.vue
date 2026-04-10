<template>
  <div class="schedule-page">
    <div class="page-header">
      <div class="header-left">
        <i class="fa-solid fa-calendar-days page-icon"></i>
        <div>
          <h2 class="page-title">排班管理</h2>
          <p class="page-desc">维护医生排班，患者端“我要预约”会实时读取这里的数据。</p>
        </div>
      </div>
    </div>

    <div class="content-card">
      <div class="toolbar">
        <el-select v-model="deptId" placeholder="科室" clearable filterable style="width: 180px" @change="handleDeptChange">
          <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.name" :value="d.deptId" />
        </el-select>
        <el-select v-model="doctorId" placeholder="医生" clearable filterable style="width: 180px" @change="loadData">
          <el-option v-for="d in doctorOptions" :key="d.userId" :label="`${d.name || d.username} (${d.username})`" :value="d.userId" />
        </el-select>
        <el-date-picker v-model="dateFilter" type="date" value-format="YYYY-MM-DD" placeholder="排班日期" @change="loadData" />
        <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="loadData">
          <el-option :value="1" label="可预约" />
          <el-option :value="0" label="停诊" />
        </el-select>
        <el-button class="add-btn" @click="openCreate">新增排班</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" class="data-table">
        <el-table-column prop="scheduleId" label="ID" width="72" />
        <el-table-column prop="deptName" label="科室" min-width="120" />
        <el-table-column prop="doctorName" label="医生" min-width="120">
          <template #default="{ row }">{{ row.doctorName || '-' }}{{ row.doctorTitle ? `（${row.doctorTitle}）` : '' }}</template>
        </el-table-column>
        <el-table-column prop="scheduleDate" label="日期" width="120" />
        <el-table-column prop="scheduleTimeSlot" label="时段" width="120" />
        <el-table-column prop="totalSlots" label="总号源" width="90" />
        <el-table-column prop="bookedSlots" label="已约" width="80" />
        <el-table-column prop="remainingSlots" label="剩余" width="80" />
        <el-table-column prop="statusText" label="状态" width="90" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeRow(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑排班' : '新增排班'" width="520px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="医生" prop="doctorId">
          <el-select v-model="form.doctorId" filterable style="width: 100%">
            <el-option v-for="d in doctorOptions" :key="d.userId" :label="`${d.name || d.username} (${d.username})`" :value="d.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="scheduleDate">
          <el-date-picker v-model="form.scheduleDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时段" prop="timeSlot">
          <el-select v-model="form.timeSlot" style="width: 100%">
            <el-option v-for="slot in timeSlotOptions" :key="slot" :label="slot" :value="slot" />
          </el-select>
        </el-form-item>
        <el-form-item label="总号源" prop="totalSlots">
          <el-input-number v-model="form.totalSlots" :min="1" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option :value="1" label="可预约" />
            <el-option :value="0" label="停诊" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDeptOptions,
  getUserPage,
  getScheduleList,
  createSchedule,
  updateSchedule,
  deleteSchedule
} from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const deptId = ref(null)
const doctorId = ref(null)
const dateFilter = ref('')
const statusFilter = ref(null)

const deptOptions = ref([])
const doctorOptions = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const form = reactive({
  doctorId: null,
  scheduleDate: '',
  timeSlot: '08:00-09:00',
  totalSlots: 20,
  status: 1,
  remark: ''
})

const timeSlotOptions = ['08:00-09:00', '09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00']

const rules = {
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时段', trigger: 'change' }],
  totalSlots: [{ required: true, message: '请输入总号源', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const loadDepts = async () => {
  const list = await getDeptOptions()
  deptOptions.value = Array.isArray(list) ? list : []
}

const loadDoctors = async () => {
  const res = await getUserPage({
    current: 1,
    size: 200,
    roleCode: 'DOCTOR',
    status: 1,
    deptId: deptId.value ?? undefined
  })
  doctorOptions.value = res?.list || []
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getScheduleList({
      current: currentPage.value,
      size: pageSize.value,
      deptId: deptId.value ?? undefined,
      doctorId: doctorId.value ?? undefined,
      date: dateFilter.value || undefined,
      status: statusFilter.value ?? undefined
    })
    tableData.value = res?.list || []
    total.value = res?.total || 0
  } finally {
    loading.value = false
  }
}

const handleDeptChange = async () => {
  doctorId.value = null
  await loadDoctors()
  currentPage.value = 1
  loadData()
}

const openCreate = () => {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  editingId.value = row.scheduleId
  form.doctorId = row.doctorId
  form.scheduleDate = row.scheduleDate
  form.timeSlot = row.scheduleTimeSlot
  form.totalSlots = row.totalSlots
  form.status = row.status
  form.remark = row.remark || ''
  dialogVisible.value = true
}

const resetForm = () => {
  form.doctorId = null
  form.scheduleDate = ''
  form.timeSlot = '08:00-09:00'
  form.totalSlots = 20
  form.status = 1
  form.remark = ''
  formRef.value?.resetFields()
}

const submitForm = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    const payload = {
      doctorId: form.doctorId,
      scheduleDate: form.scheduleDate,
      timeSlot: form.timeSlot,
      totalSlots: form.totalSlots,
      status: form.status,
      remark: form.remark || undefined
    }
    if (isEdit.value && editingId.value) {
      await updateSchedule(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createSchedule(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

const removeRow = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除排班（${row.scheduleDate} ${row.scheduleTimeSlot}）吗？`, '删除确认', {
      type: 'warning'
    })
  } catch {
    return
  }
  await deleteSchedule(row.scheduleId)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(async () => {
  await loadDepts()
  await loadDoctors()
  await loadData()
})
</script>

<style scoped>
.schedule-page { padding: 24px 28px 32px; }
.page-header { margin-bottom: 20px; }
.header-left { display: flex; align-items: center; gap: 14px; }
.page-icon {
  width: 48px; height: 48px; line-height: 48px; text-align: center; font-size: 22px; color: #fff;
  background: linear-gradient(135deg, #8b6f47, #6b4f2a); border-radius: 12px;
}
.page-title { margin: 0; font-size: 20px; font-weight: 700; color: #2c1810; }
.page-desc { margin: 4px 0 0; font-size: 13px; color: #5c4a32; }
.content-card {
  background: rgba(255,252,248,.95); border-radius: 16px; padding: 20px 22px 24px;
  box-shadow: 0 8px 40px rgba(61,41,20,.08), 0 0 0 1px rgba(139,90,43,.08);
}
.toolbar { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 16px; align-items: center; }
.add-btn { margin-left: auto; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
