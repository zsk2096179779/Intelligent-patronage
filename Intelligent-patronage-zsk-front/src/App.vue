<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessageBox } from 'element-plus'
import AiAssistantBubble from '@/components/AiAssistantBubble.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const getPageTitle = () => {
  // 优先取当前路由的meta.title
  return route.meta?.title || '智能投顾系统'
}

// 判断是否为公开页面（登录/注册页）
const isPublicPage = computed(() => {
  return route.path === '/login' || route.path === '/register'
})

// 根据角色获取菜单项
const getMenuItems = () => {
  const userRole = authStore.userInfo?.role

  const allMenus = [
    {
      index: '/market',
      title: '组合产品订购',
      roles: ['USER', 'STAFF', 'AUDITOR']
    },
    {
      index: '/subscription/purchased',
      title: '已购组合',
      roles: ['USER']
    },
    {
      index: '/combination/create',
      title: '创建策略组合',
      roles: ['STAFF']  // 只有工作人员可以创建
    },
    {
      index: '/combination/configure',
      title: '组合配置管理',
      roles: ['STAFF']  // 只有工作人员可以配置
    },
    {
      index: '/portfolio/order-data',
      title: '用户订购数据',
      roles: ['STAFF', 'AUDITOR']  // 员工和审核员可以查看
    },
    {
      index: '/audit',
      title: '策略组合审核',
      roles: ['AUDITOR']  // 只有审核员可以审核
    }
  ]

  if (!userRole) return []

  return allMenus.filter(menu => menu.roles.includes(userRole))
}

// 处理登出
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await authStore.logout()
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}

const menuItems = computed(() => getMenuItems())
</script>

<template>
  <div id="app">
    <!-- 登录/注册页面：不显示侧边栏和顶部导航 -->
    <template v-if="isPublicPage">
      <router-view />
    </template>

    <!-- 已登录页面：显示完整布局 -->
    <template v-else>
      <el-container class="app-container">
        <!-- 侧边栏 -->
        <el-aside width="250px" class="sidebar">
          <div class="logo-container">
            <img src="@/assets/logo.svg" alt="Logo" class="logo" />
            <h2 class="app-title">智能投顾</h2>
          </div>

          <el-menu
            :default-active="route.path"
            class="sidebar-menu"
            router
            background-color="#001529"
            text-color="#fff"
            active-text-color="#409EFF"
          >
            <el-menu-item
              v-for="item in menuItems"
              :key="item.index"
              :index="item.index"
            >
              <span>{{ item.title }}</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 主内容区域 -->
        <el-container>
          <!-- 顶部导航栏 -->
          <el-header class="header">
            <div class="header-left">
              <el-breadcrumb separator="/">
                <el-breadcrumb-item>智能投顾系统</el-breadcrumb-item>
                <el-breadcrumb-item>{{ getPageTitle() }}</el-breadcrumb-item>
              </el-breadcrumb>
            </div>

            <div class="header-right">
              <el-dropdown>
                <span class="user-info">
                  <el-avatar size="small" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
                  <span class="username">{{ authStore.userInfo?.username || '用户' }}</span>
                  <span class="user-role">（{{ authStore.userInfo?.roleName || '' }}）</span>
                  <el-icon><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </el-header>

          <!-- 主内容 -->
          <el-main class="main-content">
            <router-view />
          </el-main>
        </el-container>
      </el-container>
    </template>
    <AiAssistantBubble />
  </div>
</template>

<style scoped>
.app-container {
  height: 100vh;
}

.sidebar {
  background-color: #001529;
  color: white;
  overflow: hidden;
}

.logo-container {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #1f2937;
}

.logo {
  width: 40px;
  height: 40px;
  margin-bottom: 10px;
}

.app-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.sidebar-menu {
  border: none;
  margin-top: 20px;
  background: #001529;
}

.header {
  background-color: white;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f3f4f6;
}

.username {
  margin: 0 4px 0 8px;
  font-size: 14px;
  color: #374151;
}

.user-role {
  margin: 0 8px 0 0;
  font-size: 12px;
  color: #909399;
}

.main-content {
  background-color: #f9fafb;
  padding: 20px;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 200px !important;
  }

  .app-title {
    font-size: 16px;
  }
}
</style>
