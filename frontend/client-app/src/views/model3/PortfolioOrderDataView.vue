<template>
  <div class="main-view">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-bar"></div>
          <span class="header-title">用户订购数据</span>
        </div>
      </template>
      <PortfolioOrderData />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import PortfolioOrderData from '../../components/model3/PortfolioOrderData.vue'
</script>

<style scoped>
.main-view {
  padding: 32px 0;
  background: #f7f8fa;
  min-height: 100vh;
}

.main-card {
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.06);
  margin: 0 auto;
  max-width: 1400px;
  padding: 24px 32px 32px 32px;
}

.card-header {
  display: flex;
  align-items: center;
}

.header-bar {
  width: 4px;
  height: 22px;
  background: #409EFF;
  border-radius: 2px;
  margin-right: 10px;
}

.header-title {
  font-size: 16px;
  font-weight: bold;
  color: #222;
}
</style>

