/**
 * src/utils/llm.ts
 * 智能投顾核心逻辑 - 路由与上下文增强版
 */
import request from '@/utils/request'
import { getApiUrl, API_CONFIG } from '@/config/api'
import { useAiContextStore } from '@/stores/aiContext' // 确保你创建了这个 Store
import type { RouteLocationNormalizedLoaded } from 'vue-router'

// 定义类型
export type Role = 'system' | 'user' | 'assistant'
export interface Message { role: Role; content: string }
export type RiskLevel = 'conservative' | 'balanced' | 'aggressive'

/**
 * 1. 路由业务含义映射表
 * 根据你提供的 router/index.ts 进行精准映射
 */
const PAGE_MAP: Record<string, string> = {
  '/login': '用户登录页',
  '/register': '用户注册页',
  '/market': '组合产品订购市场 (浏览和购买策略组合)',
  '/subscription/purchased': '已购组合产品 (查看已持有的策略)',
  '/combination/create': '策略组合创建页 (Staff权限)',
  '/combination/configure': '组合配置列表页 (Staff权限)',
  '/portfolio/order-data': '用户订购数据查看页 (员工和管理员可见，查看已上架组合产品的用户订购数据)',
  '/risk/assessment': '风险承受能力评估页 (必须完成才能交易)',
  '/subscription/my-orders': '我的订阅订单记录',
  '/otc/open': '场外(OTC)账户开通页',
  '/audit': '策略组合审核页 (Auditor权限)'
}

/**
 * 辅助函数：解析当前页面名称
 * 处理静态路由和动态路由 (如 /combination/configure/123)
 */
const getPageName = (route: RouteLocationNormalizedLoaded): string => {
  const path = route.path

  // 1. 精确匹配
  if (PAGE_MAP[path]) return PAGE_MAP[path]

  // 2. 模糊匹配/动态路由处理
  if (path.startsWith('/combination/configure/')) {
    return '组合详细配置页 (正在编辑某个具体策略)'
  }

  return '未知页面'
}

/**
 * 2. 生成系统提示词 (System Prompt)
 */
const createSystemPrompt = (risk: RiskLevel, contextInfo: string): string => {
  return `你是一名专业的金融智能投顾助手。

【当前环境信息】
${contextInfo}

【用户画像】
用户风险偏好：${risk === 'conservative' ? '保守型' : risk === 'balanced' ? '均衡型' : '激进型'}。

【回答原则】
1. **上下文感知**：请根据用户所在的页面提供针对性帮助。例如在"OTC开通页"遇到困难，请解释OTC开户流程或报错原因。
2. **错误诊断**：如果【当前环境信息】中包含最近的报错信息，请优先解释该错误并给出解决方案。
3. **合规性**：回答简练专业，不承诺绝对收益，涉及投资建议时请提示风险。`
}

/**
 * 3. 组装上下文 (Context Builder)
 */
export const buildContext = (
  history: Message[],
  question: string,
  risk: RiskLevel,
  route: RouteLocationNormalizedLoaded
): Message[] => {
  // A. 获取页面信息
  const pageName = getPageName(route)
  let contextInfo = `用户当前所在位置：${pageName} (路径: ${route.path})`

  // B. 获取最近的报错信息 (从 Pinia Store)
  try {
    const aiStore = useAiContextStore()
    const recentError = aiStore.getRecentError() // 获取2分钟内的报错
    if (recentError) {
      contextInfo += `\n\n[系统检测到用户刚刚遇到了错误]
错误码：${recentError.code}
错误信息：${recentError.message}
(请在回答中安抚用户并解释此错误的原因)`
    }
  } catch (e) {
    console.warn('AiContextStore not initialized')
  }

  // C. 生成 System Message
  const systemMsg: Message = {
    role: 'system',
    content: createSystemPrompt(risk, contextInfo)
  }

  // D. 组装 User Message
  const userMsg: Message = {
    role: 'user',
    content: question
  }

  // E. 截取历史记录 (保留最近 6 条，防止Token溢出)
  const recentHistory = history.slice(-6)

  return [systemMsg, ...recentHistory, userMsg]
}

/**
 * 4. 调用后端 LLM 接口
 */
export const callLLM = async (messages: Message[]) => {
  try {
    // 这里的 timeout 设置长一点，防止思考时间过长导致前端超时
    const response = await request.post(getApiUrl(API_CONFIG.ENDPOINTS.LLM), {
      messages: messages,
      stream: false
    }, { timeout: 60000 }) // 60秒超时

    const replyText = response.data?.reply || response.data?.content || '抱歉，智能助手暂时无法连接。'
    return replyText
  } catch (error: any) {
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      throw new Error('请求超时，请重试')
    }
    throw error
  }
}

export default { callLLM, buildContext }
