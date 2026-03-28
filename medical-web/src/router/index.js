import { createRouter, createWebHistory } from 'vue-router'
import Placeholder from '@/views/Placeholder.vue'

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
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'admin/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      { path: 'admin/dept', component: Placeholder, meta: { title: '科室管理' } },
      { path: 'admin/doctor', component: Placeholder, meta: { title: '医生管理' } },
      { path: 'admin/patient', component: Placeholder, meta: { title: '患者管理' } },
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
      { path: 'patient/prescription', component: Placeholder, meta: { title: '我的处方' } }
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
