<template>
  <div v-if = "isVisible" class="ai-chat-container">
    <!-- 1. 悬浮球 (折叠状态) -->
    <transition name="fade">
      <div v-show="!isOpen" class="ai-bubble-btn" @click="toggleChat(true)">
        <el-icon class="icon-pulse"><ChatDotRound /></el-icon>
        <span class="label">智能投顾</span>
      </div>
    </transition>

    <!-- 2. 聊天窗口卡片 (展开状态) -->
    <transition name="slide-up">
      <div v-show="isOpen" class="chat-window">
        <!-- 头部：标题与关闭 -->
        <div class="chat-header">
          <div class="header-left">
            <div class="avatar-circle">
              <el-icon><Service /></el-icon>
            </div>
            <div class="header-info">
              <div class="title">智能投顾助手</div>
              <div class="status">
                <span class="dot"></span> 在线
              </div>
            </div>
          </div>
          <el-icon class="close-btn" @click="toggleChat(false)"><Close /></el-icon>
        </div>

        <!-- 内容区：消息列表 -->
        <div class="chat-body" ref="scrollRef">
          <div class="system-notice">
            <span>AI生成内容仅供参考，不构成投资建议</span>
          </div>

          <div v-for="(msg, index) in messages" :key="index" :class="['message-row', msg.role]">
            <!-- 机器人头像 -->
            <div v-if="msg.role === 'assistant'" class="msg-avatar bot">
              <el-icon><Cpu /></el-icon>
            </div>

            <!-- 消息气泡 -->
            <div class="msg-content">
              <div class="bubble">
                {{ msg.content }}
              </div>
            </div>

            <!-- 用户头像 -->
            <div v-if="msg.role === 'user'" class="msg-avatar user">
              <el-icon><User /></el-icon>
            </div>
          </div>

          <!-- Loading 动画 -->
          <div v-if="loading" class="message-row assistant">
            <div class="msg-avatar bot"><el-icon><Cpu /></el-icon></div>
            <div class="msg-content">
              <div class="bubble typing">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部：工具栏与输入框 -->
        <div class="chat-footer">
          <div class="tool-bar">
            <span class="label">风险偏好:</span>
            <el-radio-group v-model="risk" size="small" class="risk-radio">
              <el-radio-button label="conservative">保守</el-radio-button>
              <el-radio-button label="balanced">均衡</el-radio-button>
              <el-radio-button label="aggressive">激进</el-radio-button>
            </el-radio-group>
          </div>

          <div class="input-area">
            <el-input
              v-model="input"
              type="textarea"
              :rows="2"
              placeholder="请输入您的问题（例如：现在的行情适合买入吗？）"
              resize="none"
              @keydown.enter.prevent="handleSend"
            />
            <el-button type="primary" circle class="send-btn" :disabled="loading || !input.trim()" @click="handleSend">
              <el-icon><Position /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import {ref, reactive, nextTick, watch, computed} from 'vue'
import { useRoute } from 'vue-router'
import { ChatDotRound, Close, Service, User, Cpu, Position } from '@element-plus/icons-vue'
import { callLLM, buildContext, type Message, type RiskLevel } from '@/utils/llm'
import {useAuthStore} from "@/stores/auth.ts";

// --- 状态管理 ---
const route = useRoute()
const authStore = useAuthStore()

const isOpen = ref(false)
const loading = ref(false)
const input = ref('')
const risk = ref<RiskLevel>('balanced')
const scrollRef = ref<HTMLElement | null>(null)

/**
 * 计算是否显示助手
 * 条件：1. 已登录  2. 角色必须是 USER
 */
const isVisible = computed(() => {
  // 确保 userInfo 存在且 role 为 USER
  // 注意：根据你的 auth.ts 实现，role 可能是大写 'USER' 也可能是小写，这里建议做一下兼容或者严格匹配
  // 假设你的 store 里是 isLoggedIn 和 userInfo.role
  if (!authStore.isLoggedIn || !authStore.userInfo) return false

  return authStore.userInfo.role === 'USER'
})

// 监听可见性变化：如果用户退出了登录，或者切换了账号，强制关闭聊天窗
watch(isVisible, (newVal) => {
  if (!newVal) {
    isOpen.value = false
  }
})



// 本地只存储显示的对话记录（不包含 System Prompt， System Prompt 每次请求时动态生成）
const messages = reactive<Message[]>([
  { role: 'assistant', content: '您好！我是您的专属智能投顾。请问有什么可以帮您？您可以告诉我您的投资目标，我会为您提供建议。' }
])

// --- 交互逻辑 ---

// 切换开关
const toggleChat = (val: boolean) => {
  isOpen.value = val
  if (val) scrollToBottom()
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (scrollRef.value) {
    scrollRef.value.scrollTop = scrollRef.value.scrollHeight + 100
  }
}

// 发送消息核心逻辑
const handleSend = async () => {
  const userText = input.value.trim()
  if (!userText || loading.value) return

  // 1. UI 立刻显示用户消息
  messages.push({ role: 'user', content: userText })
  input.value = ''
  loading.value = true
  scrollToBottom()

  try {
    // 3. 关键点：将 route 传入 buildContext
    // 注意：这里传入的是响应式的 route 对象，llm.ts 里会读取它的 .path 属性
    const payload = buildContext(messages, userText, risk.value, route)

    const reply = await callLLM(payload)
    messages.push({ role: 'assistant', content: reply })
  } catch (err: any) {
    console.error(err)
    const errMsg = err.message || '网络连接异常'
    messages.push({ role: 'assistant', content: `[系统消息] ${errMsg}` })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
/*
  这里使用了原生 CSS，没有使用 SCSS，
  所以不需要安装 sass-embedded，直接复制即可运行。
*/

/* 全局容器 */
.ai-chat-container {
  position: fixed;
  right: 32px;
  bottom: 32px;
  z-index: 2000;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 悬浮按钮 */
.ai-bubble-btn {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #409eff, #337ecc);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.ai-bubble-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.4);
}
.ai-bubble-btn .label {
  font-size: 10px;
  margin-top: 2px;
}
.ai-bubble-btn .el-icon {
  font-size: 26px;
}

/* 聊天主窗口 */
.chat-window {
  width: 380px;
  height: 600px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

/* 头部 */
.chat-header {
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #f0f2f5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.avatar-circle {
  width: 40px;
  height: 40px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}
.header-info .title {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}
.header-info .status {
  font-size: 12px;
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 4px;
}
.header-info .status .dot {
  width: 6px;
  height: 6px;
  background: #67c23a;
  border-radius: 50%;
}
.close-btn {
  cursor: pointer;
  font-size: 20px;
  color: #909399;
  padding: 4px;
  border-radius: 4px;
}
.close-btn:hover {
  background: #f2f3f5;
  color: #606266;
}

/* 消息区域 */
.chat-body {
  flex: 1;
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.chat-body::-webkit-scrollbar {
  width: 6px;
}
.chat-body::-webkit-scrollbar-thumb {
  background: #dce0e6;
  border-radius: 3px;
}

.system-notice {
  text-align: center;
  font-size: 12px;
  color: #909399;
  margin: 0 auto;
  margin-bottom: 10px;
}
.system-notice span {
  background: #ebeef5;
  padding: 4px 12px;
  border-radius: 12px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 100%;
}
.message-row.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 20px;
}
.msg-avatar.bot {
  background: #fff;
  color: #409eff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.msg-avatar.user {
  background: #409eff;
  color: #fff;
}

.msg-content {
  max-width: 80%;
}
.bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap; /* 保留换行符 */
  position: relative;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.message-row.assistant .bubble {
  background: #fff;
  color: #303133;
  border-top-left-radius: 2px;
}
.message-row.user .bubble {
  background: #409eff;
  color: #fff;
  border-top-right-radius: 2px;
}

/* 正在输入动画 */
.typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 16px !important;
}
.typing span {
  width: 6px;
  height: 6px;
  background: #909399;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}
.typing span:nth-child(1) { animation-delay: -0.32s; }
.typing span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 底部输入区 */
.chat-footer {
  background: #fff;
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
}
.tool-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.tool-bar .label {
  font-size: 12px;
  color: #606266;
}

/* 覆盖 Element 样式微调 */
.chat-footer :deep(.el-radio-button__inner) {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 0;
}
.chat-footer :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 4px 0 0 4px;
}
.chat-footer :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 4px 4px 0;
}

.input-area {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}
.input-area :deep(.el-textarea__inner) {
  background: #f5f7fa;
  border: none;
  border-radius: 8px;
  padding: 10px;
  box-shadow: none;
}
.input-area :deep(.el-textarea__inner):focus {
  background: #fff;
  box-shadow: 0 0 0 1px #409eff;
}

/* 动画效果 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.slide-up-enter-active, .slide-up-leave-active { transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1); }
.slide-up-enter-from, .slide-up-leave-to { transform: translateY(40px); opacity: 0; }

.icon-pulse { animation: pulse 2.5s infinite; }
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.15); }
  100% { transform: scale(1); }
}
</style>
