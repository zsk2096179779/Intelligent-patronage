/**
 * API 配置
 */
export const API_CONFIG = {
  // 后端服务基础URL（开发环境使用代理，生产环境使用完整URL）
  BASE_URL: import.meta.env.PROD ? 'http://localhost:8080' : '',

  // API 端点
  ENDPOINTS: {
    // 认证相关接口
    AUTH_LOGIN: '/api/auth/login',
    AUTH_REGISTER: '/api/auth/register',
    AUTH_LOGOUT: '/api/auth/logout',
    AUTH_USER: '/api/auth/user',
    AUTH_CHECK_USERNAME: '/api/auth/check-username',
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
    OTC_STATUS: '/api/investor/otc/status',
    RISK_STATUS: '/api/risk-assessment/status',
    RISK_MATCH_CHECK: '/api/risk-assessment/match-check',
    OTC_OPEN: '/api/investor/otc/open',
    INVESTOR_PROFILE: '/api/investor/profile',
    RISK_QUESTIONNAIRE: '/api/risk-assessment/questionnaire',
    RISK_START: '/api/risk-assessment/start',
    RISK_SUBMIT: '/api/risk-assessment/submit',

    // LLM 智能投顾接口（后端代理到 OpenAI / 本地模型等）
    LLM: '/api/llm',

    AGREEMENT_LIST: '/api/agreements/list',
    AGREEMENT_DETAIL: (id: number) => `/api/agreements/detail/${id}`,
    AGREEMENT_SIGN_BATCH: '/api/agreements/sign-batch',

    SUBSCRIPTION_CREATE: '/api/subscription/create',
    SUBSCRIPTION_DETAIL: (orderNo: string) => `/api/subscription/detail/${orderNo}`,
    SUBSCRIPTION_UPDATE: (orderNo: string) => `/api/subscription/update/${orderNo}`,
    SUBSCRIPTION_SIGNATURE: (orderNo: string) => `/api/subscription/signature/${orderNo}`,
    SUBSCRIPTION_SUBMIT: (orderNo: string) => `/api/subscription/submit/${orderNo}`,
    SUBSCRIPTION_DIRECT_SUBMIT: '/api/subscription/submit',
    SUBSCRIPTION_CONFIRM_RISK_MISMATCH: (orderNo: string) =>
      `/api/subscription/confirm-risk-mismatch/${orderNo}`,
    SUBSCRIPTION_CANCEL: (orderNo: string) => `/api/subscription/cancel/${orderNo}`,
    SUBSCRIPTION_MY_ORDERS: '/api/subscription/my-orders',
    SUBSCRIPTION_PURCHASED_PORTFOLIOS: '/api/subscription/purchased-portfolios',
    SUBSCRIPTION_CHECK_PURCHASED: (portfolioId: number | string) =>
      `/api/subscription/check-purchased?portfolioId=${portfolioId}`
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

