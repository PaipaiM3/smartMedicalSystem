import request from '@/utils/request'

/**
 * 用户分页
 */
export function getUserPage(params) {
  return request({
    url: '/admin/user/page',
    method: 'get',
    params
  })
}

/**
 * 新增用户（含角色）
 */
export function createUser(data) {
  return request({
    url: '/admin/user',
    method: 'post',
    data
  })
}

/**
 * 更新用户信息
 */
export function updateUser(id, data) {
  return request({
    url: `/admin/user/${id}`,
    method: 'put',
    data
  })
}

/**
 * 修改用户状态
 */
export function updateUserStatus(id, status) {
  return request({
    url: `/admin/user/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 删除用户（同时解除角色关联）
 */
export function deleteUser(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'delete'
  })
}

/**
 * 新增角色
 */
export function createRole(data) {
  return request({
    url: '/admin/role',
    method: 'post',
    data
  })
}

/**
 * 删除角色（解除用户关联后删除）
 */
export function deleteRole(id) {
  return request({
    url: `/admin/role/${id}`,
    method: 'delete'
  })
}

/**
 * 更新角色信息
 */
export function updateRole(id, data) {
  return request({
    url: `/admin/role/${id}`,
    method: 'put',
    data
  })
}

/**
 * 修改角色状态
 */
export function updateRoleStatus(id, status) {
  return request({
    url: `/admin/role/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 角色分页
 */
export function getRolePage(params) {
  return request({
    url: '/admin/role/page',
    method: 'get',
    params
  })
}

/**
 * 角色列表
 */
export function getRoleList(params) {
  return request({
    url: '/admin/role/list',
    method: 'get',
    params
  })
}

/**
 * 药品分页
 */
export function getMedicinePage(params) {
  return request({
    url: '/admin/medicine/page',
    method: 'get',
    params
  })
}

/**
 * 库存预警分页（当前库存 ≤ 最低库存）
 */
export function getMedicineStockWarning(params) {
  return request({
    url: '/admin/medicine/stock-warning',
    method: 'get',
    params
  })
}

/**
 * 药品分类（下拉）
 */
export function getMedicineCategories() {
  return request({
    url: '/admin/medicine/categories',
    method: 'get'
  })
}

/**
 * 新增药品
 */
export function createMedicine(data) {
  return request({
    url: '/admin/medicine',
    method: 'post',
    data
  })
}

/**
 * 更新药品
 */
export function updateMedicine(id, data) {
  return request({
    url: `/admin/medicine/${id}`,
    method: 'put',
    data
  })
}

/**
 * 药品详情
 */
export function getMedicineDetail(id) {
  return request({
    url: `/admin/medicine/${id}`,
    method: 'get'
  })
}

/** 科室分页 */
export function getDeptPage(params) {
  return request({
    url: '/admin/dept/page',
    method: 'get',
    params
  })
}

/** 科室树（启用） */
export function getDeptTree() {
  return request({
    url: '/admin/dept/tree',
    method: 'get'
  })
}

/** 科室下拉（启用，平铺） */
export function getDeptOptions() {
  return request({
    url: '/admin/dept/options',
    method: 'get'
  })
}

export function getDeptDetail(id) {
  return request({
    url: `/admin/dept/${id}`,
    method: 'get'
  })
}

export function createDept(data) {
  return request({
    url: '/admin/dept',
    method: 'post',
    data
  })
}

export function updateDept(id, data) {
  return request({
    url: `/admin/dept/${id}`,
    method: 'put',
    data
  })
}

export function deleteDept(id) {
  return request({
    url: `/admin/dept/${id}`,
    method: 'delete'
  })
}

// ==================== 管理端排班 ====================

/** 排班分页/列表 */
export function getScheduleList(params) {
  return request({
    url: '/admin/schedule/list',
    method: 'get',
    params
  })
}

/** 新增排班 */
export function createSchedule(data) {
  return request({
    url: '/admin/schedule',
    method: 'post',
    data
  })
}

/** 更新排班 */
export function updateSchedule(id, data) {
  return request({
    url: `/admin/schedule/${id}`,
    method: 'put',
    data
  })
}

/** 删除排班 */
export function deleteSchedule(id) {
  return request({
    url: `/admin/schedule/${id}`,
    method: 'delete'
  })
}

// ==================== 管理端预约 ====================

/** 预约分页 */
export function getAdminAppointmentPage(params) {
  return request({
    url: '/admin/appointment/page',
    method: 'get',
    params
  })
}

/** 预约详情 */
export function getAdminAppointmentDetail(id) {
  return request({
    url: `/admin/appointment/${id}`,
    method: 'get'
  })
}

/** 管理端取消预约 */
export function cancelAdminAppointment(id) {
  return request({
    url: `/admin/appointment/${id}/cancel`,
    method: 'put'
  })
}

// ==================== 排班相关 ====================

/**
 * 获取医生可预约日期
 */
export function getAvailableDates(userId) {
  return request({
    url: '/patient/schedule/available-dates',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取医生某天排班时段
 */
export function getScheduleSlots(userId, date) {
  return request({
    url: '/patient/schedule/slots',
    method: 'get',
    params: { userId, date }
  })
}

// ==================== 预约相关 ====================

/**
 * 创建预约
 */
export function createAppointment(data) {
  return request({
    url: '/patient/appointment/create',
    method: 'post',
    data
  })
}

/**
 * 取消预约
 */
export function cancelAppointment(appointmentId) {
  return request({
    url: `/patient/appointment/cancel/${appointmentId}`,
    method: 'put'
  })
}

/**
 * 获取我的预约列表
 */
export function getMyAppointments() {
  return request({
    url: '/patient/appointment/my',
    method: 'get'
  })
}

/**
 * 获取预约详情
 */
export function getAppointmentDetail(appointmentId) {
  return request({
    url: `/patient/appointment/${appointmentId}`,
    method: 'get'
  })
}

