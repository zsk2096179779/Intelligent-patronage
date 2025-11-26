/**
 * Axios 请求配置
 * 支持 Session 认证
 */
import axios from 'axios'
import type { AxiosInstance, AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getApiUrl } from '@/config/api'
import { useAuthStore } from '@/stores/auth'

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 10000,
  withCredentials: true, // 重要：允许发送 Cookie（Session）
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 可以在这里添加 token 等
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response
  },
  (error: AxiosError) => {
    const requestUrl = error.config?.url || ''
    
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.clearUserInfo()
      
      // 对于 /api/auth/user 接口，401 是正常的未登录状态，不需要显示错误和跳转
      if (requestUrl.includes('/api/auth/user')) {
        // 静默处理，直接返回错误，不显示消息，不跳转
        return Promise.reject(error)
      }
      
      // 对于登录接口的 401，不自动跳转（用户已经在登录页），让登录函数自己处理错误消息
      if (requestUrl.includes('/api/auth/login')) {
        // 直接返回错误，不显示消息，不跳转，让具体的登录函数处理
        return Promise.reject(error)
      }
      
      // 其他接口的 401 错误，显示错误消息并跳转到登录页
      ElMessage.error('未登录或登录已过期，请重新登录')
      router.push('/login')
    } else if (error.response?.status === 403) {
      // 对于注册和登录接口的 403，可能是后端配置问题
      if (requestUrl.includes('/api/auth/register') || requestUrl.includes('/api/auth/login')) {
        // 不显示通用错误，让具体的错误处理函数显示更详细的错误信息
        // 或者显示更友好的提示
        const errorMessage = error.response?.data?.message || '后端服务配置错误，请联系管理员'
        ElMessage.error(errorMessage)
      } else {
        // 其他接口的 403 错误
        ElMessage.error('权限不足，无法访问')
      }
    }
    return Promise.reject(error)
  }
)

export default request

