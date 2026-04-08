import { createRouter, createWebHistory } from 'vue-router'
import Placeholder from '@/views/Placeholder.vue'
import { resolveDefaultHomePath } from '@/config/menu-config'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: () => ({ path: resolveDefaultHomePath() }),
    children: [
      {
        path: 'admin/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'admin/dept',
        component: () => import('@/views/admin/DeptList.vue'),
        meta: { title: '科室列表' }
      },
      {
        path: 'admin/doctor',
        component: () => import('@/views/admin/StaffList.vue'),
        meta: { title: '医生列表', staffRoleCode: 'DOCTOR' }
      },
      {
        path: 'admin/nurse',
        component: () => import('@/views/admin/StaffList.vue'),
        meta: { title: '护士列表', staffRoleCode: 'NURSE' }
      },
      {
        path: 'admin/patient',
        component: () => import('@/views/admin/StaffList.vue'),
        meta: { title: '患者列表', staffRoleCode: 'PATIENT' }
      },
      { path: 'admin/schedule', component: Placeholder, meta: { title: '排班管理' } },
      { path: 'admin/appointment', component: Placeholder, meta: { title: '预约管理' } },
      { path: 'admin/medicine', component: () => import('@/views/admin/MedicineList.vue'), meta: { title: '药品列表' } },
      {
        path: 'admin/medicine-stock-warning',
        name: 'MedicineStockWarning',
        component: () => import('@/views/admin/MedicineStockWarning.vue'),
        meta: { title: '库存预警', stockWarningOnly: true }
      },
      { path: 'admin/system/user', component: () => import('@/views/admin/UserManagement.vue'), meta: { title: '用户管理' } },
      { path: 'admin/system/role', component: () => import('@/views/admin/RoleManagement.vue'), meta: { title: '角色管理' } },
      { path: 'user/password', component: Placeholder, meta: { title: '修改密码' } },
      { path: 'doctor/dashboard', component: Placeholder, meta: { title: '医生工作台' } },
      { path: 'doctor/schedule', component: Placeholder, meta: { title: '我的排班' } },
      { path: 'doctor/queue', component: Placeholder, meta: { title: '待诊队列' } },
      { path: 'doctor/medical-record', component: Placeholder, meta: { title: '病历书写' } },
      { path: 'doctor/prescription', component: Placeholder, meta: { title: '处方开立' } },
      { path: 'patient/dashboard', component: Placeholder, meta: { title: '患者首页' } },
      { path: 'patient/appointment', component: Placeholder, meta: { title: '我要预约' } },
      { path: 'patient/my-appointment', component: Placeholder, meta: { title: '我的预约' } },
      { path: 'patient/medical-record', component: Placeholder, meta: { title: '我的病历' } },
      { path: 'patient/prescription', component: Placeholder, meta: { title: '我的处方' } },
      { path: 'reception/dashboard', component: Placeholder, meta: { title: '挂号工作台' } },
      { path: 'reception/appointment', component: Placeholder, meta: { title: '预约挂号' } },
      { path: 'reception/patient-register', component: Placeholder, meta: { title: '患者建档' } },
      { path: 'reception/payment', component: Placeholder, meta: { title: '收费' } },
      { path: 'reception/refund', component: Placeholder, meta: { title: '退费' } },
      { path: 'nurse/dashboard', component: Placeholder, meta: { title: '护士工作台' } },
      { path: 'nurse/prescription', component: Placeholder, meta: { title: '待发药' } },
      { path: 'nurse/dispense', component: Placeholder, meta: { title: '发药确认' } },
      { path: 'nurse/inventory', component: Placeholder, meta: { title: '药品盘点' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + '智能医疗服务管理系统'
  if (to.meta.guest) {
    next()
    return
  }
  const userInfo = sessionStorage.getItem('userInfo')
  if (!userInfo && to.path !== '/login') {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
