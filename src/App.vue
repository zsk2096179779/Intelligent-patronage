<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()

const getPageTitle = () => {
  // 优先取当前路由的meta.title
  return route.meta?.title || '策略组合审核'
}
</script>

<template>
  <div id="app">
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
          <el-menu-item index="/audit">
            <span>策略组合审核</span>
          </el-menu-item>
          <el-menu-item index="/combination/create">
            <span>创建策略组合</span>
          </el-menu-item>
          <el-menu-item index="/combination/configure">
            <span>组合配置管理</span>
          </el-menu-item>
          <el-menu-item index="/market">
            <span>组合产品订购</span>
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
                <span class="username">张三</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>个人资料</el-dropdown-item>
                  <el-dropdown-item>退出登录</el-dropdown-item>
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
  margin: 0 8px;
  font-size: 14px;
  color: #374151;
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
