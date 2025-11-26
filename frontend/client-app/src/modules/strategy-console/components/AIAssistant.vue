<template>
  <teleport to="body">
    <div
      class="ai-float-entry"
      :class="{ 'ai-float-entry--open': panelVisible }"
      @click="togglePanel"
    >
      <el-icon><ChatRound /></el-icon>
    </div>

    <transition name="ai-panel">
      <div v-if="panelVisible" class="ai-panel" ref="panelRef">
        <div class="ai-panel__header">
          <div class="ai-panel__title">
            <el-icon><Cpu /></el-icon>
            <span>智能助理</span>
          </div>
          <div class="ai-panel__actions">
            <el-tooltip content="清空对话">
              <el-icon class="ai-panel__icon" @click.stop="clearMessages">
                <Delete />
              </el-icon>
            </el-tooltip>
            <el-icon class="ai-panel__icon" @click.stop="togglePanel">
              <Close />
            </el-icon>
          </div>
        </div>

        <div class="ai-panel__body" ref="scrollRef">
          <template v-if="messages.length">
            <div
              v-for="(msg, idx) in messages"
              :key="idx"
              class="ai-msg"
              :class="msg.role === 'user' ? 'ai-msg--user' : 'ai-msg--bot'"
            >
              <div class="ai-msg__avatar">
                <el-icon v-if="msg.role === 'user'"><UserFilled /></el-icon>
                <el-icon v-else><Cpu /></el-icon>
              </div>
              <div class="ai-msg__bubble">
                <span>{{ msg.content }}</span>
              </div>
            </div>
          </template>
          <el-empty v-else description="你好，我可以帮你解答策略相关问题" />
        </div>

        <div class="ai-panel__input">
          <el-input
            v-model="draft"
            type="textarea"
            :rows="2"
            resize="none"
            placeholder="向大模型提问..."
            @keyup.enter.exact.prevent="sendMessage"
          />
          <div class="ai-panel__footer">
            <span class="ai-panel__hint">回车发送，Shift+Enter 换行</span>
            <el-button
              type="primary"
              size="small"
              :loading="sending"
              :disabled="!draft.trim()"
              @click="sendMessage"
            >
              提问
            </el-button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatRound,
  Close,
  Cpu,
  Delete,
  UserFilled
} from '@element-plus/icons-vue'
import http from '@/modules/strategy-console/utils/http'

const panelVisible = ref(false)
const messages = ref([])
const draft = ref('')
const sending = ref(false)
const scrollRef = ref(null)
const panelRef = ref(null)

const togglePanel = () => {
  panelVisible.value = !panelVisible.value
  if (panelVisible.value) {
    nextTick(() => scrollToBottom())
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (scrollRef.value) {
      scrollRef.value.scrollTop = scrollRef.value.scrollHeight
    }
  })
}

watch(
  () => messages.value.length,
  () => scrollToBottom()
)

const clearMessages = () => {
  messages.value = []
}

const sendMessage = async () => {
  if (!draft.value.trim() || sending.value) return

  const question = draft.value.trim()
  messages.value.push({ role: 'user', content: question })
  draft.value = ''
  sending.value = true

  try {
    const { data } = await http.post('/llm/query', { question })
    const answer =
      data?.answer ||
      data?.data?.answer ||
      '后端未返回回答内容，请稍后重试。'
    messages.value.push({ role: 'assistant', content: answer })
  } catch (error) {
    console.error('调用大模型失败:', error)
    messages.value.push({
      role: 'assistant',
      content: '提问失败，请稍后重试。'
    })
    ElMessage.error(error.response?.data?.message || '提问失败')
  } finally {
    sending.value = false
  }
}
</script>

<style scoped>
.ai-float-entry {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #337ecc);
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 10px 24px rgba(64, 158, 255, 0.4);
  cursor: pointer;
  z-index: 3000;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.ai-float-entry--open {
  transform: scale(0.9);
  box-shadow: 0 2px 10px rgba(64, 158, 255, 0.2);
}

.ai-float-entry:hover {
  transform: translateY(-2px);
}

.ai-panel {
  position: fixed;
  right: 24px;
  bottom: 90px;
  width: 340px;
  max-height: 460px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 30px 60px rgba(15, 23, 42, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 3100;
}

.ai-panel-enter-from,
.ai-panel-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.ai-panel-enter-active,
.ai-panel-leave-active {
  transition: all 0.2s ease;
}

.ai-panel__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid #f0f0f0;
  background: #f7f9fc;
}

.ai-panel__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1f2d3d;
}

.ai-panel__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-panel__icon {
  cursor: pointer;
  color: #909399;
  transition: color 0.2s;
}

.ai-panel__icon:hover {
  color: #409eff;
}

.ai-panel__body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #fbfbfc;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ai-msg {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.ai-msg--user {
  flex-direction: row-reverse;
}

.ai-msg__avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #ecf5ff;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #409eff;
}

.ai-msg--user .ai-msg__avatar {
  background: #409eff;
  color: #fff;
}

.ai-msg__bubble {
  max-width: 75%;
  padding: 10px 14px;
  border-radius: 12px;
  line-height: 1.5;
  font-size: 13px;
  color: #303133;
  background: #fff;
  border: 1px solid #ebeef5;
}

.ai-msg--user .ai-msg__bubble {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

.ai-panel__input {
  padding: 14px 16px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ai-panel__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ai-panel__hint {
  font-size: 12px;
  color: #a0a3b1;
}

@media (max-width: 768px) {
  .ai-panel {
    right: 12px;
    left: 12px;
    width: auto;
  }

  .ai-float-entry {
    right: 12px;
    bottom: 12px;
  }
}
</style>

