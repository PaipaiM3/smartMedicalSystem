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
