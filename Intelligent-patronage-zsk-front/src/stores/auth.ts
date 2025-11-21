/**
 * 用户认证状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'
import { getApiUrl } from '@/config/api'
import { API_CONFIG } from '@/config/api'
import { ElMessage } from 'element-plus'

export interface UserInfo {
  id: number
  username: string
  email?: string
  phone?: string
  role: 'USER' | 'STAFF' | 'AUDITOR'
  roleName: string
}

export const useAuthStore = defineStore('auth', () => {
  const userInfo = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => !!userInfo.value)

  // 设置用户信息
  const setUserInfo = (info: UserInfo | null) => {
    userInfo.value = info
  }

  // 清除用户信息
  const clearUserInfo = () => {
    userInfo.value = null
  }

  // 登录
  const login = async (username: string, password: string): Promise<boolean> => {
    try {
      const response = await request.post(getApiUrl(API_CONFIG.ENDPOINTS.AUTH_LOGIN), {
        username,
        password
      })

      if (response.data.code === 200) {
        setUserInfo(response.data.data)
        ElMessage.success('登录成功')
        return true
      } else {
        ElMessage.error(response.data.message || '登录失败')
        return false
      }
    } catch (error: any) {
      // 处理不同类型的错误
      if (error.response?.status === 401) {
        // 401 表示用户名或密码错误
        const message = error.response?.data?.message || '用户名或密码错误，请检查后重试'
        ElMessage.error(message)
      } else if (error.response?.status === 403) {
        // 403 表示权限问题
        const message = error.response?.data?.message || '登录接口权限配置错误，请联系管理员'
        ElMessage.error(message)
      } else {
        // 其他错误
        const message = error.response?.data?.message || '登录失败，请稍后重试'
        ElMessage.error(message)
      }
      return false
    }
  }

  // 注册
  const register = async (data: {
    username: string
    password: string
    email?: string
    phone?: string
  }): Promise<boolean> => {
    try {
      const response = await request.post(getApiUrl(API_CONFIG.ENDPOINTS.AUTH_REGISTER), data)

      if (response.data.code === 200) {
        ElMessage.success('注册成功，请登录')
        return true
      } else {
        ElMessage.error(response.data.message || '注册失败')
        return false
      }
    } catch (error: any) {
      // 处理不同类型的错误
      if (error.response?.status === 403) {
        const message = error.response?.data?.message || '注册接口权限配置错误，请联系管理员检查后端配置'
        ElMessage.error(message)
      } else if (error.response?.status === 400) {
        const message = error.response?.data?.message || '注册信息格式错误，请检查输入'
        ElMessage.error(message)
      } else {
        const message = error.response?.data?.message || '注册失败，请稍后重试'
        ElMessage.error(message)
      }
      return false
    }
  }

  // 登出
  const logout = async () => {
    try {
      await request.post(getApiUrl(API_CONFIG.ENDPOINTS.AUTH_LOGOUT))
    } catch (error) {
      console.error('登出失败', error)
    } finally {
      clearUserInfo()
      ElMessage.success('已退出登录')
    }
  }

  // 获取当前用户信息
  const getCurrentUser = async (): Promise<boolean> => {
    try {
      const response = await request.get(getApiUrl(API_CONFIG.ENDPOINTS.AUTH_USER))
      if (response.data.code === 200) {
        setUserInfo(response.data.data)
        return true
      }
      clearUserInfo()
      return false
    } catch (error: any) {
      // 401 是正常的未登录状态，不需要显示错误
      if (error.response?.status === 401) {
        clearUserInfo()
        return false
      }
      // 其他错误也静默处理
      clearUserInfo()
      return false
    }
  }

  // 检查用户名是否可用
  const checkUsername = async (username: string): Promise<boolean> => {
    try {
      const response = await request.get(getApiUrl(API_CONFIG.ENDPOINTS.AUTH_CHECK_USERNAME), {
        params: { username }
      })
      if (response.data.code === 200) {
        return response.data.data?.available === true
      }
      return false
    } catch (error) {
      return false
    }
  }

  return {
    userInfo,
    isLoggedIn,
    setUserInfo,
    clearUserInfo,
    login,
    register,
    logout,
    getCurrentUser,
    checkUsername
  }
})

