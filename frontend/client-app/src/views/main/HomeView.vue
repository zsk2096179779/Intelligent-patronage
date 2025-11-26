<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const goToDashboard = () => {
  router.push('/dashboard')
}

onMounted(() => {
  // 3秒后自动跳转到仪表盘
  setTimeout(() => {
    router.push('/dashboard')
  }, 3000)
})
</script>

<template>
  <div class="home">
    <el-result
      icon="success"
      title="欢迎使用智能投顾系统"
    >
      <template #extra>
        <el-button type="primary" @click="goToDashboard">立即前往</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.home {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
</style>
