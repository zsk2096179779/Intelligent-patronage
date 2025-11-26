import type { RouteRecordRaw } from 'vue-router'

export const strategyConsoleRoutes: RouteRecordRaw[] = [
  {
    path: '/strategy/management',
    name: 'StrategyManagement',
    component: () => import('@/modules/strategy-console/pages/StrategyManagement.vue'),
    meta: { title: '策略管理', requiresAuth: true, roles: ['STAFF'] }
  },
  {
    path: '/strategy/creation',
    name: 'StrategyCreation',
    component: () => import('@/modules/strategy-console/pages/StrategyCreation.vue'),
    meta: { title: '策略创建', requiresAuth: true, roles: ['STAFF'] }
  },
  {
    path: '/strategy/monitoring',
    name: 'StrategyMonitoring',
    component: () => import('@/modules/strategy-console/pages/StrategyMonitoring.vue'),
    meta: { title: '策略监控', requiresAuth: true, roles: ['STAFF', 'AUDITOR'] }
  },
  {
    path: '/strategy/rebalance',
    name: 'RebalanceSetting',
    component: () => import('@/modules/strategy-console/pages/RebalanceSetting.vue'),
    meta: { title: '再平衡配置', requiresAuth: true, roles: ['STAFF'] }
  },
  {
    path: '/strategy/management/detail/:strategyId',
    name: 'StrategyDetail',
    component: () => import('@/modules/strategy-console/pages/Detail.vue'),
    meta: { title: '策略详情', requiresAuth: true, roles: ['STAFF'] },
    props: true
  }
]

