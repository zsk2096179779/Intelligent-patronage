/**
 * API 配置
 */
export const API_CONFIG = {
  // 后端服务基础URL（开发环境使用代理，生产环境使用完整URL）
  BASE_URL: import.meta.env.PROD ? 'http://localhost:8080' : '',
  
  // API 端点
  ENDPOINTS: {
    // 策略组合（获取全部）- 根据后端接口文档
    STRATEGY_COMBINATION_ALL: '/combos',
    // 创建策略组合
    STRATEGY_COMBINATION_CREATE: '/api/strategy-combination',
    STRATEGY_COMBINATION_APPROVE: (id: number) => `/api/strategy-combination/${id}/approve`,
    STRATEGY_COMBINATION_REJECT: (id: number) => `/api/strategy-combination/${id}/reject`,
    // 获取策略列表
    STRATEGIES_LIST: '/api/strategies',
    // 组合创建流程相关接口
    UPDATE_BASIC_INFO: (id: number) => `/api/strategy-combination/${id}/basic-info`,
    SAVE_PRODUCT_PARAMS: (id: number) => `/api/strategy-combination/${id}/product-params`,
    GET_PRODUCT_PARAMS: (id: number) => `/api/strategy-combination/${id}/product-params`,
    SAVE_HOLDINGS: (id: number) => `/api/strategy-combination/${id}/holdings`,
    GET_HOLDINGS: (id: number) => `/api/strategy-combination/${id}/holdings`,
    SUBMIT_FOR_REVIEW: (id: number) => `/api/strategy-combination/${id}/submit`,
    // 基金相关接口
    FUNDS_LIST: '/api/funds',
    FUND_BY_CODE: (fundCode: string) => `/api/funds/${fundCode}`,
  }
}

/**
 * 获取完整的API URL
 */
export const getApiUrl = (endpoint: string): string => {
  // 开发环境使用代理（相对路径），生产环境使用完整URL
  if (API_CONFIG.BASE_URL) {
    return `${API_CONFIG.BASE_URL}${endpoint}`
  }
  return endpoint
}

