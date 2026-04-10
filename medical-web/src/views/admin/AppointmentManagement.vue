<template>
  <div class="appointment-page">
    <div class="page-header">
      <div class="header-left">
        <i class="fa-solid fa-clock page-icon"></i>
        <div>
          <h2 class="page-title">预约管理</h2>
          <p class="page-desc">统一查看预约记录，支持筛选、详情查看与取消待就诊预约。</p>
        </div>
      </div>
    </div>

    <div class="content-card">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          clearable
          placeholder="预约单号/时段"
          style="width: 220px"
          @clear="loadData"
          @keyup.enter="loadData"
        />
        <el-select v-model="deptId" clearable filterable placeholder="科室" style="width: 160px" @change="handleDeptChange">
          <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.name" :value="d.deptId" />
        </el-select>
        <el-select v-model="doctorId" clearable filterable placeholder="医生" style="width: 180px" @change="loadData">
          <el-option v-for="d in doctorOptions" :key="d.userId" :label="`${d.name || d.username} (${d.username})`" :value="d.userId" />
        </el-select>
        <el-date-picker v-model="dateFilter" type="date" value-format="YYYY-MM-DD" placeholder="就诊日期" @change="loadData" />
        <el-select v-model="statusFilter" clearable placeholder="状态" style="width: 120px" @change="loadData">
          <el-option :value="1" label="待就诊" />
          <el-option :value="2" label="已就诊" />
          <el-option :value="3" label="已取消" />
          <el-option :value="4" label="爽约" />
        </el-select>
      </div>

      <el-table :data="tableData" v-loading="loading" class="data-table">
        <el-table-column prop="appointmentNo" label="预约单号" min-width="165" />
        <el-table-column prop="deptName" label="科室" width="120" />
        <el-table-column prop="doctorName" label="医生" min-width="120">
          <template #default="{ row }">{{ row.doctorName || '-' }}{{ row.doctorTitle ? `（${row.doctorTitle}）` : '' }}</template>
        </el-table-column>
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column label="就诊时间" width="190">
          <template #default="{ row }">{{ row.appointmentDate }} {{ row.timeSlot }}</template>
        </el-table-column>
        <el-table-column prop="feeAmount" label="费用" width="90">
          <template #default="{ row }">¥{{ row.feeAmount ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="100" />
        <el-table-column prop="createdTime" label="创建时间" width="165" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="danger" :disabled="row.status !== 1" @click="cancelRow(row)">取消</el-button>
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

    <el-dialog v-model="detailVisible" title="预约详情" width="560px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="预约单号" :span="2">{{ detail.appointmentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ detail.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ detail.doctorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="就诊日期">{{ detail.appointmentDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="时段">{{ detail.timeSlot || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.statusText || '-' }}</el-descriptions-item>
        <el-descriptions-item label="费用">¥{{ detail.feeAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">{{ detail.paid === 1 ? '已支付' : '未支付' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detail.createdTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cancelAdminAppointment,
  getAdminAppointmentDetail,
  getAdminAppointmentPage,
  getDeptOptions,
  getUserPage
} from '@/api/admin'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const keyword = ref('')
const deptId = ref(null)
const doctorId = ref(null)
const dateFilter = ref('')
const statusFilter = ref(null)

const deptOptions = ref([])
const doctorOptions = ref([])

const detailVisible = ref(false)
const detail = reactive({
  appointmentId: null,
  appointmentNo: '',
  patientName: '',
  doctorName: '',
  doctorTitle: '',
  deptName: '',
  appointmentDate: '',
  timeSlot: '',
  status: null,
  statusText: '',
  feeAmount: null,
  paid: 0,
  createdTime: ''
})

const loadDepts = async () => {
  const list = await getDeptOptions()
  deptOptions.value = Array.isArray(list) ? list : []
}

const loadDoctors = async () => {
  const res = await getUserPage({
    current: 1,
    size: 200,
    status: 1,
    roleCode: 'DOCTOR',
    deptId: deptId.value ?? undefined
  })
  doctorOptions.value = res?.list || []
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminAppointmentPage({
      current: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value ?? undefined,
      deptId: deptId.value ?? undefined,
      doctorId: doctorId.value ?? undefined,
      date: dateFilter.value || undefined
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

const openDetail = async (row) => {
  const d = await getAdminAppointmentDetail(row.appointmentId)
  detail.appointmentId = d.appointmentId
  detail.appointmentNo = d.appointmentNo || ''
  detail.patientName = d.patientName || ''
  detail.doctorName = d.doctorName || ''
  detail.doctorTitle = d.doctorTitle || ''
  detail.deptName = d.deptName || ''
  detail.appointmentDate = d.appointmentDate || ''
  detail.timeSlot = d.timeSlot || ''
  detail.status = d.status ?? null
  detail.statusText = d.statusText || ''
  detail.feeAmount = d.feeAmount ?? null
  detail.paid = d.paid ?? 0
  detail.createdTime = d.createdTime || ''
  detailVisible.value = true
}

const cancelRow = async (row) => {
  if (row.status !== 1) return
  try {
    await ElMessageBox.confirm(`确定取消预约「${row.appointmentNo}」吗？`, '取消预约', { type: 'warning' })
  } catch {
    return
  }
  await cancelAdminAppointment(row.appointmentId)
  ElMessage.success('已取消预约')
  loadData()
}

onMounted(async () => {
  await loadDepts()
  await loadDoctors()
  await loadData()
})
</script>

<style scoped>
.appointment-page { padding: 24px 28px 32px; min-height: 100%; }
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
.toolbar { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
