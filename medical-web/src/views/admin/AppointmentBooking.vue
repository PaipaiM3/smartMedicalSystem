<template>
  <div class="appointment-page">
    <div class="page-header">
      <div class="header-left">
        <i class="fa-solid fa-calendar-check page-icon"></i>
        <div>
          <h2 class="page-title">预约挂号</h2>
          <p class="page-desc">选择科室和医生，在线预约门诊服务</p>
        </div>
      </div>
    </div>

    <!-- 步骤条 -->
    <div class="steps-container">
      <div class="step-item" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
        <div class="step-number">1</div>
        <div class="step-label">选择科室</div>
      </div>
      <div class="step-line" :class="{ active: currentStep > 1 }"></div>
      <div class="step-item" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
        <div class="step-number">2</div>
        <div class="step-label">选择医生</div>
      </div>
      <div class="step-line" :class="{ active: currentStep > 2 }"></div>
      <div class="step-item" :class="{ active: currentStep >= 3, completed: currentStep > 3 }">
        <div class="step-number">3</div>
        <div class="step-label">选择时间</div>
      </div>
      <div class="step-line" :class="{ active: currentStep > 3 }"></div>
      <div class="step-item" :class="{ active: currentStep >= 4 }">
        <div class="step-number">4</div>
        <div class="step-label">确认预约</div>
      </div>
    </div>

    <div class="content-card">
      <!-- 步骤1：选择科室 -->
      <div v-show="currentStep === 1" class="step-content">
        <div class="search-bar">
          <el-input
              v-model="deptKeyword"
              placeholder="搜索科室名称"
              clearable
              prefix-icon="Search"
              class="search-input"
              @clear="loadDeptList"
              @keyup.enter="loadDeptList"
          />
        </div>
        <div class="dept-grid" v-loading="deptLoading">
          <div
              v-for="dept in deptList"
              :key="dept.deptId"
              class="dept-card"
              @click="selectDept(dept)"
          >
            <div class="dept-icon">
              <i class="fa-solid fa-hospital-user"></i>
            </div>
            <div class="dept-info">
              <div class="dept-name">{{ dept.name }}</div>
              <div class="dept-code">{{ dept.code || '--' }}</div>
            </div>
          </div>
          <div v-if="deptList.length === 0 && !deptLoading" class="empty-state">
            <i class="fa-solid fa-folder-open"></i>
            <p>暂无科室数据</p>
          </div>
        </div>
      </div>

      <!-- 步骤2：选择医生 -->
      <div v-show="currentStep === 2" class="step-content">
        <div class="step-header">
          <el-button link @click="goBackStep" class="back-btn">
            <i class="fa-solid fa-arrow-left"></i> 返回选科室
          </el-button>
          <div class="selected-info">
            当前科室：<span class="highlight">{{ selectedDept?.name }}</span>
          </div>
        </div>
        <div class="doctor-list" v-loading="doctorLoading">
          <div
              v-for="doctor in doctorList"
              :key="doctor.doctorId"
              class="doctor-card"
              @click="selectDoctor(doctor)"
          >
            <div class="doctor-avatar">
              <i class="fa-solid fa-user-md"></i>
            </div>
            <div class="doctor-info">
              <div class="doctor-name">{{ doctor.name }}</div>
              <div class="doctor-title">{{ doctor.title || '医师' }}</div>
              <div class="doctor-specialty">{{ doctor.specialty || '全科' }}</div>
            </div>
            <div class="doctor-fee">
              <span class="fee">¥{{ doctor.consultationFee || 50 }}</span>
              <span class="fee-label">挂号费</span>
            </div>
          </div>
          <div v-if="doctorList.length === 0 && !doctorLoading" class="empty-state">
            <i class="fa-solid fa-user-slash"></i>
            <p>该科室暂无医生</p>
          </div>
        </div>
      </div>

      <!-- 步骤3：选择时间 -->
      <div v-show="currentStep === 3" class="step-content">
        <div class="step-header">
          <el-button link @click="goBackStep" class="back-btn">
            <i class="fa-solid fa-arrow-left"></i> 返回选医生
          </el-button>
          <div class="selected-info">
            医生：<span class="highlight">{{ selectedDoctor?.name }}</span>
          </div>
        </div>
        <div class="date-selector">
          <div class="date-label">选择就诊日期</div>
          <div class="date-list" v-loading="dateLoading">
            <div
                v-for="date in availableDates"
                :key="date.value"
                class="date-item"
                :class="{ active: selectedDate === date.value }"
                @click="selectDate(date)"
            >
              <div class="date-week">{{ date.week }}</div>
              <div class="date-day">{{ date.day }}</div>
            </div>
            <div v-if="availableDates.length === 0 && !dateLoading" class="empty-state small">
              <i class="fa-solid fa-calendar-xmark"></i>
              <p>暂无可用日期</p>
            </div>
          </div>
        </div>
        <div class="time-selector" v-if="selectedDate">
          <div class="time-label">选择就诊时段</div>
          <div class="time-list" v-loading="scheduleLoading">
            <div
                v-for="slot in timeSlots"
                :key="slot.scheduleId"
                class="time-item"
                :class="{
                active: selectedScheduleId === slot.scheduleId,
                disabled: !slot.available
              }"
                @click="selectTimeSlot(slot)"
            >
              <div class="time-range">{{ slot.timeSlot }}</div>
              <div class="remaining">剩余{{ slot.remaining }}个号</div>
            </div>
            <div v-if="timeSlots.length === 0 && !scheduleLoading" class="empty-state small">
              <i class="fa-solid fa-clock"></i>
              <p>当天暂无排班</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 步骤4：确认预约 -->
      <div v-show="currentStep === 4" class="step-content">
        <div class="step-header">
          <el-button link @click="goBackStep" class="back-btn">
            <i class="fa-solid fa-arrow-left"></i> 返回修改
          </el-button>
        </div>
        <div class="confirm-card">
          <div class="confirm-title">
            <i class="fa-solid fa-clipboard-list"></i>
            预约信息确认
          </div>
          <div class="confirm-info">
            <div class="info-row">
              <div class="info-label">就诊科室：</div>
              <div class="info-value">{{ selectedDept?.name }}</div>
            </div>
            <div class="info-row">
              <div class="info-label">就诊医生：</div>
              <div class="info-value">{{ selectedDoctor?.name }}（{{ selectedDoctor?.title || '医师' }}）</div>
            </div>
            <div class="info-row">
              <div class="info-label">就诊时间：</div>
              <div class="info-value">{{ selectedDate }} {{ selectedTimeSlot }}</div>
            </div>
            <div class="info-row">
              <div class="info-label">挂号费用：</div>
              <div class="info-value fee">¥{{ selectedDoctor?.consultationFee || 50 }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="step-footer">
        <el-button v-if="currentStep > 1" class="btn-prev" @click="goBackStep">
          <i class="fa-solid fa-chevron-left"></i> 上一步
        </el-button>
        <el-button
            v-if="currentStep < 4"
            class="btn-next"
            :disabled="!canNext"
            @click="goNextStep"
        >
          下一步 <i class="fa-solid fa-chevron-right"></i>
        </el-button>
        <el-button
            v-if="currentStep === 4"
            class="btn-submit"
            :loading="submitting"
            @click="submitAppointment"
        >
          <i class="fa-solid fa-check"></i> 确认预约
        </el-button>
      </div>
    </div>

    <!-- 预约成功弹窗 -->
    <el-dialog
        v-model="successVisible"
        width="420px"
        class="success-dialog"
        :append-to-body="true"
        :modal-append-to-body="true"
        :close-on-click-modal="false"
        :show-close="false"
        align-center
    >
      <div class="success-content">
        <div class="success-icon">
          <i class="fa-solid fa-circle-check"></i>
        </div>
        <div class="success-title">预约成功！</div>
        <div class="success-info">
          <p>预约单号：{{ appointmentNo }}</p>
          <p>请按时到院就诊</p>
        </div>
        <div class="success-actions">
          <el-button class="btn-view" @click="viewMyAppointments">查看我的预约</el-button>
          <el-button class="btn-continue" @click="resetAndContinue">继续预约</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDeptOptions, getUserPage ,getAvailableDates, getScheduleSlots, createAppointment} from '@/api/admin'

const router = useRouter()

// 步骤控制
const currentStep = ref(1)

// 科室相关
const deptKeyword = ref('')
const deptList = ref([])
const deptLoading = ref(false)
const selectedDept = ref(null)

// 医生相关
const doctorList = ref([])
const doctorLoading = ref(false)
const selectedDoctor = ref(null)

// 排班相关
const availableDates = ref([])
const dateLoading = ref(false)
const selectedDate = ref('')
const selectedScheduleId = ref(null)
const selectedTimeSlot = ref('')
const timeSlots = ref([])
const scheduleLoading = ref(false)

// 预约提交
const submitting = ref(false)
const successVisible = ref(false)
const appointmentNo = ref('')

// 星期映射
const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

// 判断下一步是否可用
const canNext = computed(() => {
  if (currentStep.value === 1) return selectedDept.value !== null
  if (currentStep.value === 2) return selectedDoctor.value !== null
  if (currentStep.value === 3) return selectedDate.value && selectedScheduleId.value
  return false
})

// 加载科室列表
const loadDeptList = async () => {
  deptLoading.value = true
  try {
    const res = await getDeptOptions()
    let list = res.data || res || []
    // /admin/dept/options 已在后端按启用状态过滤，这里不再二次依赖 status 字段
    if (deptKeyword.value) {
      const kw = deptKeyword.value.toLowerCase()
      list = list.filter(d =>
          d.name?.toLowerCase().includes(kw) ||
          d.code?.toLowerCase().includes(kw)
      )
    }
    deptList.value = list
  } catch (error) {
    console.error('加载科室失败:', error)
    ElMessage.error('加载科室失败')
  } finally {
    deptLoading.value = false
  }
}

// 选择科室
const selectDept = (dept) => {
  selectedDept.value = dept
  currentStep.value = 2
  loadDoctorList()
}

// 加载医生列表
const loadDoctorList = async () => {
  if (!selectedDept.value) return
  doctorLoading.value = true
  try {
    const res = await getUserPage({
      current: 1,
      size: 100,
      deptId: selectedDept.value.deptId,
      status: 1,
      roleCode: 'DOCTOR'
    })
    doctorList.value = res.data?.list || res.list || []
  } catch (error) {
    console.error('加载医生失败:', error)
    ElMessage.error('加载医生失败')
  } finally {
    doctorLoading.value = false
  }
}

// 选择医生
const selectDoctor = async (doctor) => {
  selectedDoctor.value = doctor
  currentStep.value = 3
  await loadAvailableDates()
}

// 加载可预约日期
const loadAvailableDates = async () => {
  if (!selectedDoctor.value) return
  dateLoading.value = true
  try {
    const res = await getAvailableDates(selectedDoctor.value.userId)
    const dates = res.data || res || []
    // 转换为前端需要的格式
    availableDates.value = dates.map(dateStr => {
      const date = new Date(dateStr)
      return {
        value: dateStr,
        week: weekdays[date.getDay()],
        day: `${date.getMonth() + 1}/${date.getDate()}`
      }
    })
    // 默认选中第一个日期
    if (availableDates.value.length > 0) {
      selectedDate.value = availableDates.value[0].value
      await loadScheduleSlots()
    } else {
      selectedDate.value = ''
      timeSlots.value = []
    }
  } catch (error) {
    console.error('加载可预约日期失败:', error)
    ElMessage.error('加载可预约日期失败')
  } finally {
    dateLoading.value = false
  }
}

// 选择日期
const selectDate = async (date) => {
  selectedDate.value = date.value
  selectedScheduleId.value = null
  selectedTimeSlot.value = ''
  await loadScheduleSlots()
}

// 加载排班时段
const loadScheduleSlots = async () => {
  if (!selectedDoctor.value || !selectedDate.value) return
  scheduleLoading.value = true
  try {
    const res = await getScheduleSlots(selectedDoctor.value.userId, selectedDate.value)
    const slots = res.data || res || []
    timeSlots.value = slots
    // 如果有可用时段，默认选中第一个
    const firstAvailable = slots.find(slot => slot.available)
    if (firstAvailable) {
      selectedScheduleId.value = firstAvailable.scheduleId
      selectedTimeSlot.value = firstAvailable.timeSlot
    }
  } catch (error) {
    console.error('加载排班时段失败:', error)
    ElMessage.error('加载排班时段失败')
  } finally {
    scheduleLoading.value = false
  }
}

// 选择时段
const selectTimeSlot = (slot) => {
  if (!slot.available) {
    ElMessage.warning('该时段号源已满')
    return
  }
  selectedScheduleId.value = slot.scheduleId
  selectedTimeSlot.value = slot.timeSlot
}

// 上一步
const goBackStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

// 下一步
const goNextStep = () => {
  if (currentStep.value === 3) {
    if (!selectedScheduleId.value) {
      ElMessage.warning('请选择就诊时段')
      return
    }
  }
  if (currentStep.value < 4) {
    currentStep.value++
  }
}

// 提交预约
const submitAppointment = async () => {
  submitting.value = true
  try {
    const res = await createAppointment({
      scheduleId: selectedScheduleId.value,
    })

    appointmentNo.value = res || 'APT' + Date.now()
    successVisible.value = true
  } catch (error) {
    console.error('预约失败:', error)
    ElMessage.error(error.message || '预约失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 查看我的预约
const viewMyAppointments = () => {
  successVisible.value = false
  router.push('/patient/my-appointment')
}

// 重置并继续预约
const resetAndContinue = () => {
  successVisible.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  currentStep.value = 1
  selectedDept.value = null
  selectedDoctor.value = null
  selectedDate.value = ''
  selectedScheduleId.value = null
  selectedTimeSlot.value = ''
  availableDates.value = []
  timeSlots.value = []
}

onMounted(() => {
  loadDeptList()
})
</script>

<style scoped>
/* 样式保持不变，省略... */
.appointment-page {
  padding: 24px 28px 32px;
  min-height: 100%;
  background: #f5f0e8;
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
  background: linear-gradient(135deg, #8b6f47, #6b4f2a);
  border-radius: 12px;
  box-shadow: 0 4px 14px rgba(107, 79, 42, 0.35);
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #2c1810;
}

.page-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: #5c4a32;
}

/* 步骤条 */
.steps-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28px;
  background: #fff;
  padding: 20px 40px;
  border-radius: 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.step-number {
  width: 36px;
  height: 36px;
  line-height: 36px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  border-radius: 50%;
  background: #e8e0d5;
  color: #8b6f47;
  transition: all 0.3s;
}

.step-item.active .step-number {
  background: linear-gradient(135deg, #e8a54b, #d48232);
  color: #fff;
  box-shadow: 0 4px 12px rgba(232, 165, 75, 0.35);
}

.step-item.completed .step-number {
  background: #67c23a;
  color: #fff;
}

.step-label {
  font-size: 13px;
  color: #8b7a68;
}

.step-item.active .step-label {
  color: #d48232;
  font-weight: 500;
}

.step-line {
  width: 80px;
  height: 2px;
  background: #e8e0d5;
  margin: 0 12px 24px;
}

.step-line.active {
  background: linear-gradient(90deg, #d48232, #e8a54b);
}

/* 内容卡片 */
.content-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px 28px;
  box-shadow: 0 8px 30px rgba(61, 41, 20, 0.08);
  min-height: 500px;
}

.step-content {
  min-height: 400px;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0e4d4;
}

.back-btn {
  color: #8b6f47;
  font-size: 14px;
}

.back-btn:hover {
  color: #d48232;
}

.selected-info {
  font-size: 14px;
  color: #6b5a48;
}

.highlight {
  color: #d48232;
  font-weight: 600;
}

/* 科室网格 */
.search-bar {
  margin-bottom: 24px;
}

.search-input {
  width: 280px;
}

.dept-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.dept-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: #fefaf5;
  border: 1px solid #f0e4d4;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.dept-card:hover {
  border-color: #e8a54b;
  box-shadow: 0 4px 12px rgba(232, 165, 75, 0.15);
  transform: translateY(-2px);
}

.dept-icon {
  width: 48px;
  height: 48px;
  line-height: 48px;
  text-align: center;
  font-size: 24px;
  background: linear-gradient(135deg, #f5e6d8, #f0e0d0);
  border-radius: 12px;
  color: #d48232;
}

.dept-info {
  flex: 1;
}

.dept-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c1810;
  margin-bottom: 4px;
}

.dept-code {
  font-size: 12px;
  color: #b0a088;
}

/* 医生列表 */
.doctor-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.doctor-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #fefaf5;
  border: 1px solid #f0e4d4;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.doctor-card:hover {
  border-color: #e8a54b;
  background: #fff8f0;
}

.doctor-avatar {
  width: 56px;
  height: 56px;
  line-height: 56px;
  text-align: center;
  font-size: 28px;
  background: linear-gradient(135deg, #e8f4f0, #d4e8e0);
  border-radius: 50%;
  color: #2c7a5e;
}

.doctor-info {
  flex: 1;
}

.doctor-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c1810;
  margin-bottom: 4px;
}

.doctor-title {
  font-size: 13px;
  color: #d48232;
  margin-bottom: 4px;
}

.doctor-specialty {
  font-size: 12px;
  color: #8b7a68;
}

.doctor-fee {
  text-align: center;
  padding: 8px 16px;
  background: #f5e6d8;
  border-radius: 24px;
}

.fee {
  font-size: 18px;
  font-weight: 700;
  color: #d48232;
}

.fee-label {
  font-size: 11px;
  color: #8b7a68;
  margin-left: 4px;
}

/* 日期选择 */
.date-selector, .time-selector {
  margin-bottom: 28px;
}

.date-label, .time-label {
  font-size: 14px;
  font-weight: 600;
  color: #2c1810;
  margin-bottom: 14px;
}

.date-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.date-item {
  width: 80px;
  padding: 12px 8px;
  text-align: center;
  background: #fefaf5;
  border: 1px solid #f0e4d4;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.date-item:hover {
  border-color: #e8a54b;
}

.date-item.active {
  background: linear-gradient(135deg, #e8a54b, #d48232);
  border-color: transparent;
  color: #fff;
}

.date-item.active .date-week,
.date-item.active .date-day {
  color: #fff;
}

.date-week {
  font-size: 13px;
  margin-bottom: 6px;
  color: #8b7a68;
}

.date-day {
  font-size: 16px;
  font-weight: 600;
  color: #2c1810;
}

/* 时段选择 */
.time-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.time-item {
  width: 110px;
  padding: 12px;
  text-align: center;
  background: #fefaf5;
  border: 1px solid #f0e4d4;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.time-item:hover:not(.disabled) {
  border-color: #e8a54b;
}

.time-item.active:not(.disabled) {
  background: linear-gradient(135deg, #e8a54b, #d48232);
  border-color: transparent;
  color: #fff;
}

.time-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f5f0ea;
}

.time-range {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.remaining {
  font-size: 11px;
  color: #8b7a68;
}

.time-item.active .remaining {
  color: rgba(255, 255, 255, 0.8);
}

/* 确认信息 */
.confirm-card {
  background: #fefaf5;
  border-radius: 16px;
  padding: 24px;
}

.confirm-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c1810;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0e4d4;
  margin-bottom: 20px;
}

.confirm-title i {
  color: #d48232;
  margin-right: 8px;
}

.confirm-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: flex;
  align-items: baseline;
}

.info-label {
  width: 100px;
  color: #8b7a68;
  font-size: 14px;
}

.info-value {
  flex: 1;
  color: #2c1810;
  font-size: 14px;
}

.info-value.fee {
  font-size: 18px;
  font-weight: 700;
  color: #d48232;
}

/* 底部按钮 */
.step-footer {
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #f0e4d4;
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.btn-prev {
  border-radius: 10px;
  border-color: #e0d4c4;
}

.btn-next, .btn-submit {
  border-radius: 10px;
  background: linear-gradient(135deg, #e8a54b, #d48232);
  border: none;
  color: #fff;
}

.btn-next:hover, .btn-submit:hover {
  background: linear-gradient(135deg, #d48232, #c07020);
  color: #fff;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #b0a088;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-state.small {
  padding: 40px 20px;
}

/* 成功弹窗 */
.success-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.success-content {
  text-align: center;
  padding: 32px 24px;
}

.success-icon {
  font-size: 64px;
  color: #67c23a;
  margin-bottom: 16px;
}

.success-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c1810;
  margin-bottom: 16px;
}

.success-info {
  color: #8b7a68;
  margin-bottom: 24px;
}

.success-info p {
  margin: 4px 0;
}

.success-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-view {
  border-radius: 10px;
  background: #f0e4d4;
  border: none;
  color: #8b6f47;
}

.btn-continue {
  border-radius: 10px;
  background: linear-gradient(135deg, #e8a54b, #d48232);
  border: none;
  color: #fff;
}
</style>