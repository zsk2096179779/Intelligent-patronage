import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAiContextStore = defineStore('aiContext', () => {
  // 存储最近一次报错的信息
  const lastError = ref<{ code: number; message: string; timestamp: number } | null>(null)

  // 设置错误（给 request.ts 调用）
  const setLastError = (code: number, message: string) => {
    lastError.value = {
      code,
      message,
      timestamp: Date.now()
    }
  }

  // 获取最近且“新鲜”的错误（比如 2 分钟内的错误才算，太久的就不提了）
  const getRecentError = () => {
    if (!lastError.value) return null
    // 如果错误发生在 2 分钟前，就认为过期了，不发送给 AI
    if (Date.now() - lastError.value.timestamp > 120 * 1000) {
      lastError.value = null
      return null
    }
    return lastError.value
  }

  return { lastError, setLastError, getRecentError }
})
