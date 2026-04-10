<template>
  <div class="my-appointment-page">
    <div class="page-header">
      <div class="header-left">
        <i class="fa-solid fa-clock-rotate-left page-icon"></i>
        <div>
          <h2 class="page-title">我的预约</h2>
          <p class="page-desc">查看个人预约记录，支持取消待就诊预约与查看详情。</p>
        </div>
      </div>
      <el-button class="go-book-btn" @click="router.push('/patient/appointment')">
        <i class="fa-solid fa-calendar-plus"></i>
        去预约
      </el-button>
    </div>

    <div class="content-card">
      <div class="toolbar">
        <el-select v-model="statusFilter" clearable placeholder="预约状态" style="width: 140px" @change="applyFilters">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索预约单号/医生/科室"
          style="width: 300px"
          @clear="applyFilters"
          @keyup.enter="applyFilters"
        >
          <template #prefix><i class="fa-solid fa-search"></i></template>
        </el-input>
      </div>

      <el-table :data="pagedData" v-loading="loading" class="data-table">
        <el-table-column prop="appointmentNo" label="预约单号" min-width="170" />
        <el-table-column prop="deptName" label="科室" width="120">
          <template #default="{ row }">{{ row.deptName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="doctorName" label="医生" min-width="130">
          <template #default="{ row }">
            {{ row.doctorName || '-' }}{{ row.doctorTitle ? `（${row.doctorTitle}）` : '' }}
          </template>
        </el-table-column>
        <el-table-column label="就诊时间" width="200">
          <template #default="{ row }">{{ row.appointmentDate }} {{ row.timeSlot }}</template>
        </el-table-column>
        <el-table-column prop="feeAmount" label="费用" width="90">
          <template #default="{ row }">¥{{ row.feeAmount ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-tag', statusClass(row.status)]">{{ row.statusText || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
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
          :total="filteredData.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="onPageSizeChange"
        />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="预约详情" width="560px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="预约单号" :span="2">{{ detail.appointmentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ detail.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ detail.doctorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="医生职称">{{ detail.doctorTitle || '-' }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="就诊日期">{{ detail.appointmentDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="时段">{{ detail.timeSlot || '-' }}</el-descriptions-item>
        <el-descriptions-item label="排队号">{{ detail.queueNo ?? '-' }}</el-descriptions-item>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelAppointment, getAppointmentDetail, getMyAppointments } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const filteredData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const statusFilter = ref(null)
const keyword = ref('')

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
  queueNo: null,
  status: null,
  statusText: '',
  feeAmount: null,
  paid: 0,
  createdTime: ''
})

const statusOptions = [
  { value: 1, label: '待就诊' },
  { value: 2, label: '已就诊' },
  { value: 3, label: '已取消' },
  { value: 4, label: '爽约' }
]

const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

const statusClass = (status) => {
  if (status === 1) return 'pending'
  if (status === 2) return 'done'
  if (status === 3) return 'cancelled'
  if (status === 4) return 'missed'
  return ''
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyAppointments()
    list.value = Array.isArray(res) ? res : []
    applyFilters()
  } finally {
    loading.value = false
  }
}

const applyFilters = () => {
  let data = [...list.value]
  if (statusFilter.value != null) {
    data = data.filter((x) => x.status === statusFilter.value)
  }
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    data = data.filter((x) =>
      String(x.appointmentNo || '').toLowerCase().includes(kw) ||
      String(x.doctorName || '').toLowerCase().includes(kw) ||
      String(x.deptName || '').toLowerCase().includes(kw)
    )
  }
  filteredData.value = data
  currentPage.value = 1
}

const onPageSizeChange = () => {
  currentPage.value = 1
}

const cancelRow = async (row) => {
  if (row.status !== 1) return
  try {
    await ElMessageBox.confirm(`确定取消预约「${row.appointmentNo}」吗？`, '取消预约', { type: 'warning' })
  } catch {
    return
  }
  await cancelAppointment(row.appointmentId)
  ElMessage.success('已取消预约')
  await loadData()
}

const openDetail = async (row) => {
  const d = await getAppointmentDetail(row.appointmentId)
  detail.appointmentId = d.appointmentId
  detail.appointmentNo = d.appointmentNo || ''
  detail.patientName = d.patientName || ''
  detail.doctorName = d.doctorName || ''
  detail.doctorTitle = d.doctorTitle || ''
  detail.deptName = d.deptName || ''
  detail.appointmentDate = d.appointmentDate || ''
  detail.timeSlot = d.timeSlot || ''
  detail.queueNo = d.queueNo ?? null
  detail.status = d.status ?? null
  detail.statusText = d.statusText || ''
  detail.feeAmount = d.feeAmount ?? null
  detail.paid = d.paid ?? 0
  detail.createdTime = d.createdTime || ''
  detailVisible.value = true
}

onMounted(loadData)
</script>

<style scoped>
.my-appointment-page { padding: 24px 28px 32px; min-height: 100%; }
.page-header { margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.header-left { display: flex; align-items: center; gap: 14px; }
.page-icon {
  width: 48px; height: 48px; line-height: 48px; text-align: center; font-size: 22px; color: #fff;
  background: linear-gradient(135deg, #8b6f47, #6b4f2a); border-radius: 12px;
}
.page-title { margin: 0; font-size: 20px; font-weight: 700; color: #2c1810; }
.page-desc { margin: 4px 0 0; font-size: 13px; color: #5c4a32; }
.go-book-btn { border-radius: 10px; }
.content-card {
  background: rgba(255,252,248,.95); border-radius: 16px; padding: 20px 22px 24px;
  box-shadow: 0 8px 40px rgba(61,41,20,.08), 0 0 0 1px rgba(139,90,43,.08);
}
.toolbar { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
.status-tag { font-size: 12px; padding: 2px 8px; border-radius: 12px; color: #fff; }
.status-tag.pending { background: #e6a23c; }
.status-tag.done { background: #67c23a; }
.status-tag.cancelled { background: #909399; }
.status-tag.missed { background: #f56c6c; }
</style>
