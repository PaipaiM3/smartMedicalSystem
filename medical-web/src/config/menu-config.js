/**
 * 菜单配置 - 参考 book-library，按角色区分
 */

// 超级管理员/医院管理员菜单
export const adminMenuItems = [
  {
    index: '0',
    title: '主页',
    icon: 'fa-solid fa-house',
    url: '/admin/dashboard'
  },
  {
    index: '1',
    title: '科室管理',
    icon: 'fa-solid fa-building',
    children: [
      { index: '1-1', label: '科室列表', icon: 'fa-solid fa-list', url: '/admin/dept' }
    ]
  },
  {
    index: '2',
    title: '医生管理',
    icon: 'fa-solid fa-user-doctor',
    children: [
      { index: '2-1', label: '医生列表', icon: 'fa-solid fa-user-doctor', url: '/admin/doctor' },
      { index: '2-2', label: '护士列表', icon: 'fa-solid fa-user-nurse', url: '/admin/nurse' }
    ]
  },
  {
    index: '3',
    title: '患者管理',
    icon: 'fa-solid fa-users',
    children: [
      { index: '3-1', label: '患者列表', icon: 'fa-solid fa-user', url: '/admin/patient' }
    ]
  },
  {
    index: '4',
    title: '排班预约',
    icon: 'fa-solid fa-calendar-days',
    children: [
      { index: '4-1', label: '排班管理', icon: 'fa-solid fa-calendar', url: '/admin/schedule' },
      { index: '4-2', label: '预约管理', icon: 'fa-solid fa-clock', url: '/admin/appointment' }
    ]
  },
  {
    index: '5',
    title: '药品管理',
    icon: 'fa-solid fa-pills',
    children: [
      { index: '5-1', label: '药品列表', icon: 'fa-solid fa-list', url: '/admin/medicine' },
      { index: '5-2', label: '库存预警', icon: 'fa-solid fa-triangle-exclamation', url: '/admin/medicine-stock-warning' }
    ]
  },
  {
    index: '6',
    title: '系统管理',
    icon: 'fa-solid fa-cog',
    children: [
      { index: '6-1', label: '用户管理', icon: 'fa-solid fa-users', url: '/admin/system/user' },
      { index: '6-2', label: '角色管理', icon: 'fa-solid fa-list', url: '/admin/system/role' }
    ]
  },
  {
    index: '7',
    title: '用户中心',
    icon: 'fa-solid fa-user-circle',
    children: [
      { index: '7-1', label: '修改密码', icon: 'fa-solid fa-key', url: '/user/password' }
    ]
  }
]

// 医生菜单
export const doctorMenuItems = [
  { index: '0', title: '工作台', icon: 'fa-solid fa-house', url: '/doctor/dashboard' },
  {
    index: '1',
    title: '门诊接诊',
    icon: 'fa-solid fa-stethoscope',
    children: [
      { index: '1-1', label: '我的排班', icon: 'fa-solid fa-calendar', url: '/doctor/schedule' },
      { index: '1-2', label: '待诊队列', icon: 'fa-solid fa-list', url: '/doctor/queue' }
    ]
  },
  {
    index: '2',
    title: '病历处方',
    icon: 'fa-solid fa-file-medical',
    children: [
      { index: '2-1', label: '病历书写', icon: 'fa-solid fa-pen', url: '/doctor/medical-record' },
      { index: '2-2', label: '处方开立', icon: 'fa-solid fa-prescription', url: '/doctor/prescription' }
    ]
  }
]

// 患者菜单
export const patientMenuItems = [
  { index: '0', title: '首页', icon: 'fa-solid fa-house', url: '/patient/dashboard' },
  {
    index: '1',
    title: '预约挂号',
    icon: 'fa-solid fa-calendar-check',
    children: [
      { index: '1-1', label: '我要预约', icon: 'fa-solid fa-calendar-plus', url: '/patient/appointment' },
      { index: '1-2', label: '我的预约', icon: 'fa-solid fa-clock', url: '/patient/my-appointment' }
    ]
  },
  {
    index: '2',
    title: '个人中心',
    icon: 'fa-solid fa-user-circle',
    children: [
      { index: '2-1', label: '我的病历', icon: 'fa-solid fa-file-medical', url: '/patient/medical-record' },
      { index: '2-2', label: '我的处方', icon: 'fa-solid fa-prescription', url: '/patient/prescription' }
    ]
  }
]

// 挂号/收费端（与 /api/reception/** 权限一致）
export const receptionMenuItems = [
  { index: '0', title: '工作台', icon: 'fa-solid fa-house', url: '/reception/dashboard' },
  {
    index: '1',
    title: '挂号业务',
    icon: 'fa-solid fa-clipboard-list',
    children: [
      { index: '1-1', label: '预约挂号', icon: 'fa-solid fa-calendar-plus', url: '/reception/appointment' },
      { index: '1-2', label: '患者建档', icon: 'fa-solid fa-user-plus', url: '/reception/patient-register' }
    ]
  },
  {
    index: '2',
    title: '收费业务',
    icon: 'fa-solid fa-cash-register',
    children: [
      { index: '2-1', label: '收费', icon: 'fa-solid fa-money-bill-wave', url: '/reception/payment' },
      { index: '2-2', label: '退费', icon: 'fa-solid fa-rotate-left', url: '/reception/refund' }
    ]
  },
  {
    index: '3',
    title: '用户中心',
    icon: 'fa-solid fa-user-circle',
    children: [{ index: '3-1', label: '修改密码', icon: 'fa-solid fa-key', url: '/user/password' }]
  }
]

// 药房/护士端（与 /api/nurse/** 权限一致）
export const nurseMenuItems = [
  { index: '0', title: '工作台', icon: 'fa-solid fa-house', url: '/nurse/dashboard' },
  {
    index: '1',
    title: '发药业务',
    icon: 'fa-solid fa-pills',
    children: [
      { index: '1-1', label: '待发药', icon: 'fa-solid fa-list', url: '/nurse/prescription' },
      { index: '1-2', label: '发药确认', icon: 'fa-solid fa-circle-check', url: '/nurse/dispense' }
    ]
  },
  {
    index: '2',
    title: '库存',
    icon: 'fa-solid fa-warehouse',
    children: [{ index: '2-1', label: '药品盘点', icon: 'fa-solid fa-boxes-stacked', url: '/nurse/inventory' }]
  },
  {
    index: '3',
    title: '用户中心',
    icon: 'fa-solid fa-user-circle',
    children: [{ index: '3-1', label: '修改密码', icon: 'fa-solid fa-key', url: '/user/password' }]
  }
]

/** 未识别角色时仅保留安全入口，避免误显管理端菜单 */
export const minimalMenuItems = [
  {
    index: '0',
    title: '用户中心',
    icon: 'fa-solid fa-user-circle',
    children: [{ index: '0-1', label: '修改密码', icon: 'fa-solid fa-key', url: '/user/password' }]
  }
]

/**
 * 解析登录后的默认首页（与 getMenuByRole 角色优先级一致）
 */
export function resolveDefaultHomePath() {
  let roles = []
  try {
    const u = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
    roles = u.roles || []
  } catch {
    roles = []
  }
  if (roles.includes('SUPER_ADMIN') || roles.includes('ADMIN')) return '/admin/dashboard'
  if (roles.includes('DOCTOR')) return '/doctor/dashboard'
  if (roles.includes('PATIENT')) return '/patient/dashboard'
  if (roles.includes('RECEPTIONIST')) return '/reception/dashboard'
  if (roles.includes('NURSE')) return '/nurse/dashboard'
  return '/admin/dashboard'
}

export function getMenuByRole(roles) {
  if (!roles || !roles.length) return minimalMenuItems
  if (roles.includes('SUPER_ADMIN') || roles.includes('ADMIN')) return adminMenuItems
  if (roles.includes('DOCTOR')) return doctorMenuItems
  if (roles.includes('PATIENT')) return patientMenuItems
  if (roles.includes('RECEPTIONIST')) return receptionMenuItems
  if (roles.includes('NURSE')) return nurseMenuItems
  return minimalMenuItems
}
