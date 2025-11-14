import { createRouter, createWebHistory, type RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import StrategyCombinationAuditView from '../views/model3/StrategyCombinationAuditView.vue'
import StrategyCombinationSimpleCreateView from '../views/model3/StrategyCombinationSimpleCreateView.vue'
import StrategyCombinationConfigureListView from '../views/model3/StrategyCombinationConfigureListView.vue'
import StrategyCombinationConfigureView from '../views/model3/StrategyCombinationConfigureView.vue'
import StrategyCombinationMarketplaceView from '../views/model3/StrategyCombinationMarketplaceView.vue'
import LoginView from '../views/auth/LoginView.vue'
import RegisterView from '../views/auth/RegisterView.vue'

const routes = [
  // 公开路由（不需要登录）
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: { title: '注册', requiresAuth: false }
  },
  // 用户路由（USER）
  {
    path: '/market',
    name: 'combination-market',
    component: StrategyCombinationMarketplaceView,
    meta: { title: '组合产品订购', requiresAuth: true, roles: ['USER', 'STAFF', 'AUDITOR'] }
  },
  // 工作人员路由（STAFF）
  {
    path: '/combination/create',
    name: 'combination-create',
    component: StrategyCombinationSimpleCreateView,
    meta: { title: '创建策略组合', requiresAuth: true, roles: ['STAFF'] }
  },
  {
    path: '/combination/configure',
    name: 'combination-configure-list',
    component: StrategyCombinationConfigureListView,
    meta: { title: '组合配置管理', requiresAuth: true, roles: ['STAFF'] }
  },
  {
    path: '/combination/configure/:id',
    name: 'combination-configure',
    component: StrategyCombinationConfigureView,
    meta: { title: '组合详细配置', requiresAuth: true, roles: ['STAFF'] }
  },
  // 审核人员路由（AUDITOR）
  {
    path: '/audit',
    name: 'audit',
    component: StrategyCombinationAuditView,
    meta: { title: '策略组合审核', requiresAuth: true, roles: ['AUDITOR'] }
  },
  // 默认重定向
  {
    path: '/',
    redirect: (to) => {
      const authStore = useAuthStore()
      const userInfo = authStore.userInfo
      
      if (!userInfo) {
        return '/login'
      }
      
      // 根据角色重定向到对应首页
      const roleRoutes: Record<string, string> = {
        'USER': '/market',
        'STAFF': '/market',
        'AUDITOR': '/audit'
      }
      
      return roleRoutes[userInfo.role] || '/login'
    }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach(async (to: RouteLocationNormalized, from: RouteLocationNormalized, next) => {
  const authStore = useAuthStore()
  const requiresAuth = to.meta.requiresAuth !== false
  const allowedRoles = to.meta.roles as string[] | undefined
  
  // 公开路由，不需要登录
  if (!requiresAuth) {
    // 如果已登录，重定向到对应角色的首页
    if (authStore.isLoggedIn && authStore.userInfo) {
      const roleRoutes: Record<string, string> = {
        'USER': '/market',
        'STAFF': '/market',
        'AUDITOR': '/audit'
      }
      const redirectPath = roleRoutes[authStore.userInfo.role] || '/login'
      next(redirectPath)
      return
    }
    next()
    return
  }
  
  // 需要登录的路由
  if (!authStore.isLoggedIn || !authStore.userInfo) {
    // 尝试获取当前用户信息
    const hasUser = await authStore.getCurrentUser()
    if (!hasUser || !authStore.userInfo) {
      next('/login')
      return
    }
  }
  
  // 检查角色权限
  if (allowedRoles && allowedRoles.length > 0) {
    const userRole = authStore.userInfo?.role
    if (!userRole || !allowedRoles.includes(userRole)) {
      // 权限不足，重定向到对应角色的首页
      const roleRoutes: Record<string, string> = {
        'USER': '/market',
        'STAFF': '/market',
        'AUDITOR': '/audit'
      }
      const redirectPath = roleRoutes[userRole || 'USER'] || '/login'
      next(redirectPath)
      return
    }
  }
  
  next()
})

export default router
