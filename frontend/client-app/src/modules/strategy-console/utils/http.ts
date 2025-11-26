import axios, {
  type AxiosInstance,
  type AxiosResponse,
  type AxiosError,
  type InternalAxiosRequestConfig
} from 'axios'

// 统一的 API 前缀（结合 Vite 代理）
const apiPrefix = import.meta.env.VITE_API_PREFIX || '/api'

// 从 URL 或 LocalStorage 读取并保存用户 token
function getToken(): string | null {
  try {
    const urlParams = new URLSearchParams(window.location.search)
    const tokenFromUrl = urlParams.get('token')
    if (tokenFromUrl) {
      localStorage.setItem('USER_TOKEN', tokenFromUrl)
    }
  } catch (_) {}
  return localStorage.getItem('USER_TOKEN')
}

const http: AxiosInstance = axios.create({
  baseURL: apiPrefix,
  withCredentials: true, // 适配 Spring Session (JDBC) 跨域时的 Cookie
  // Spring Security CSRF 支持：自动从 `XSRF-TOKEN` Cookie 发送 `X-XSRF-TOKEN` 头
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：注入 Authorization
http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：处理 401 未授权错误
let isRedirecting = false // 防止重复重定向

http.interceptors.response.use(
  (response: AxiosResponse) => {
    // 正常响应直接返回
    return response
  },
  (error: AxiosError) => {
    // 处理错误响应
    if (error.response) {
      const status = error.response.status
      
      // 401 未授权：清除 token 并重定向到登录页
      if (status === 401 && !isRedirecting) {
        isRedirecting = true
        
        // 清除本地存储的 token
        localStorage.removeItem('USER_TOKEN')
        
        // 如果当前不在登录页，则重定向到登录页
        if (window.location.pathname !== '/') {
          // 使用 window.location 进行重定向（更可靠）
          window.location.href = '/'
        }
        
        // 显示错误提示
        const errorMessage = error.response.data?.message || '未授权或令牌失效，请重新登录'
        console.error('认证失败:', errorMessage)
      }
    }
    
    // 继续抛出错误，让调用方可以处理
    return Promise.reject(error)
  }
)

export default http