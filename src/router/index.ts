import { createRouter, createWebHistory } from 'vue-router'
import StrategyCombinationAuditView from '../views/model3/StrategyCombinationAuditView.vue'
import StrategyCombinationSimpleCreateView from '../views/model3/StrategyCombinationSimpleCreateView.vue'
import StrategyCombinationConfigureListView from '../views/model3/StrategyCombinationConfigureListView.vue'
import StrategyCombinationConfigureView from '../views/model3/StrategyCombinationConfigureView.vue'
import StrategyCombinationMarketplaceView from '../views/model3/StrategyCombinationMarketplaceView.vue'

const routes = [
  {
    path: '/audit',
    name: 'audit',
    component: StrategyCombinationAuditView,
    meta: { title: '策略组合审核' }
  },
  {
    path: '/',
    redirect: '/audit'
  },
  {
    path: '/combination/create',
    name: 'combination-create',
    component: StrategyCombinationSimpleCreateView,
    meta: { title: '创建策略组合' }
  },
  {
    path: '/combination/configure',
    name: 'combination-configure-list',
    component: StrategyCombinationConfigureListView,
    meta: { title: '组合配置管理' }
  },
  {
    path: '/combination/configure/:id',
    name: 'combination-configure',
    component: StrategyCombinationConfigureView,
    meta: { title: '组合详细配置' }
  },
  {
    path: '/market',
    name: 'combination-market',
    component: StrategyCombinationMarketplaceView,
    meta: { title: '组合产品订购' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router
