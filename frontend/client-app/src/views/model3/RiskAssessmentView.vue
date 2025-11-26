<template>
  <div class="risk-page">
    <!-- 顶部标题 -->
    <header class="risk-header">
      <h2>风险承受能力评估</h2>
      <p class="sub">
        根据监管要求，在购买智能投顾组合产品前，需要完成风险承受能力评估。
        请根据您的真实情况作答，评估结果将影响您可购买的产品范围。
      </p>
    </header>

    <!-- 当前测评状态 -->
    <el-card class="status-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>当前评估状态</span>
          <el-tag
            v-if="riskStatus"
            :type="riskStatus.expired ? 'warning' : 'success'"
          >
            {{
              !riskStatus.hasAssessment
                ? '尚未评估'
                : riskStatus.expired
                  ? '已过期'
                  : '有效'
            }}
          </el-tag>
        </div>
      </template>

      <div v-if="statusLoading" class="loading-area">
        <el-skeleton :rows="2" animated />
      </div>

      <div v-else>
        <div v-if="riskStatus?.hasAssessment" class="status-info">
          <p>
            <strong>风险等级：</strong>
            <span v-if="riskStatus.riskLevel">
              {{ riskStatus.riskLevel }}
            </span>
            <span v-else>—</span>
          </p>
          <p>
            <strong>评估日期：</strong>
            {{ formatDateTime(riskStatus.assessmentDate) }}
          </p>
          <p>
            <strong>有效期至：</strong>
            {{ formatDateTime(riskStatus.expireDate) }}
            <span v-if="!riskStatus.expired && riskStatus.daysUntilExpire > 0">
              （剩余 {{ riskStatus.daysUntilExpire }} 天）
            </span>
          </p>

          <el-alert
            v-if="riskStatus.expired"
            type="warning"
            show-icon
            title="当前风险评估已过期，请重新完成评估。"
            class="mt-2"
          />
          <el-alert
            v-else
            type="success"
            show-icon
            title="当前评估结果有效，可直接用于产品风险匹配。"
            class="mt-2"
          />

          <div class="status-actions">
            <el-button type="primary" @click="restartAssessment">
              重新评估
            </el-button>
          </div>
        </div>

        <div v-else class="status-info">
          <el-alert
            type="info"
            show-icon
            title="您尚未完成风险承受能力评估"
            description="请根据下方问卷真实填写您的投资经验、风险偏好等信息，完成评估后即可进行产品签约操作。"
          />
        </div>
      </div>
    </el-card>

    <!-- 问卷区域 -->
    <el-card class="question-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>风险评估问卷</span>
          <small v-if="questionnaireVersion" class="version">
            版本：{{ questionnaireVersion }}
          </small>
        </div>
      </template>

      <div v-if="questionLoading" class="loading-area">
        <el-skeleton :rows="6" animated />
      </div>

      <div v-else>
        <el-alert
          type="warning"
          show-icon
          title="请您谨慎、如实作答"
          description="本问卷仅用于评估您的风险承受能力，不构成任何投资建议。您的信息将严格按照相关规定进行保密。"
          class="mb-2"
        />

        <el-form class="question-form">
          <div
            v-for="(q, index) in questions"
            :key="q.id"
            class="question-item"
          >
            <div class="question-title">
              <span class="q-index">{{ index + 1 }}.</span>
              <span class="q-text">{{ q.questionText }}</span>
              <span class="q-required">*</span>
            </div>

            <!-- 单选题 -->
            <el-radio-group
              v-if="q.questionType === 'single'"
              v-model="formAnswers[q.id]"
              class="option-group"
            >
              <el-radio
                v-for="opt in q.options"
                :key="opt.value"
                :label="opt.value"
              >
                {{ opt.label }}
              </el-radio>
            </el-radio-group>

            <!-- 多选题 -->
            <el-checkbox-group
              v-else
              v-model="formAnswers[q.id]"
              class="option-group"
            >
              <el-checkbox
                v-for="opt in q.options"
                :key="opt.value"
                :label="opt.value"
              >
                {{ opt.label }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </el-form>

        <div class="form-actions">
          <el-button @click="goBack">返回</el-button>
          <el-button
            type="primary"
            :loading="submitting"
            @click="handleSubmit"
          >
            提交评估
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 评估结果展示 -->
    <el-card
      v-if="submitResult"
      class="result-card"
      shadow="never"
    >
      <template #header>
        <div class="card-header">
          <span>本次评估结果</span>
        </div>
      </template>

      <div class="result-main">
        <div class="risk-level">
          <div class="level-code">
            {{ submitResult.riskLevel || '—' }}
          </div>
          <div class="level-label">
            {{ submitResult.riskLabel || '风险等级' }}
          </div>
        </div>

        <div class="result-info">
          <p>
            <strong>总得分：</strong>{{ submitResult.totalScore ?? '—' }}
          </p>
          <p>
            <strong>评估时间：</strong>
            {{ formatDateTime(submitResult.completedAt) }}
          </p>
          <p>
            <strong>有效期至：</strong>
            {{ formatDateTime(submitResult.expireAt) }}
          </p>
          <p class="desc">
            {{ submitResult.description }}
          </p>
        </div>
      </div>

      <div class="result-actions">
        <el-button type="primary" @click="goToCombination">
          返回组合产品，继续签约
        </el-button>
        <el-button text type="primary" @click="restartAssessment">
          重新评估
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getApiUrl, API_CONFIG } from '../../config/api'

const router = useRouter()

// === 类型定义 ===
interface OptionItem {
  value: string
  label: string
  score: number
}

interface QuestionItem {
  id: number
  questionCode: string
  questionText: string
  questionType: 'single' | 'multiple' | string
  category?: string
  orderNum?: number
  required?: boolean
  options: OptionItem[]
}

interface RiskStatus {
  hasAssessment: boolean
  riskLevel?: string | null
  assessmentDate?: string | null
  expireDate?: string | null
  expired: boolean
  daysUntilExpire: number
}

interface RiskSubmitResult {
  assessmentNo: string
  totalScore: number
  riskLevel: string
  riskLabel: string
  description: string
  completedAt?: string | null
  expireAt?: string | null
}

// === 状态 ===
const statusLoading = ref(false)
const questionLoading = ref(false)
const submitting = ref(false)

const riskStatus = ref<RiskStatus | null>(null)
const questions = ref<QuestionItem[]>([])
const questionnaireVersion = ref<string>('')

const assessmentNo = ref<string>('') // 本次测评流水号

// 每题的答案：单选 -> string; 多选 -> string[]
const formAnswers = reactive<Record<number, any>>({})

const submitResult = ref<RiskSubmitResult | null>(null)

// === 接口调用 ===

// 获取当前风险评估状态
const fetchRiskStatus = async () => {
  statusLoading.value = true
  try {
    const res = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.RISK_STATUS))
    const raw = res.data
    const data = raw.data ?? raw
    riskStatus.value = data as RiskStatus
  } catch (e) {
    console.error('获取风险评估状态失败', e)
    ElMessage.error('获取风险评估状态失败，请稍后重试')
  } finally {
    statusLoading.value = false
  }
}

// 获取问卷
const fetchQuestionnaire = async () => {
  questionLoading.value = true
  try {
    const res = await axios.get(
      getApiUrl(API_CONFIG.ENDPOINTS.RISK_QUESTIONNAIRE)
    )
    const raw = res.data
    const data = raw.data ?? raw
    questionnaireVersion.value = data.version || '1.0'
    questions.value = (data.questions || []) as QuestionItem[]

    // 重置答案
    Object.keys(formAnswers).forEach(k => delete formAnswers[Number(k)])
  } catch (e) {
    console.error('获取问卷失败', e)
    ElMessage.error('获取风险评估问卷失败，请稍后重试')
  } finally {
    questionLoading.value = false
  }
}

// 开始测评（获取 assessmentNo）
const startAssessmentIfNeeded = async () => {
  if (assessmentNo.value) return

  const res = await axios.post(
    getApiUrl(API_CONFIG.ENDPOINTS.RISK_START),
    {}
  )
  const raw = res.data
  const data = raw.data ?? raw
  assessmentNo.value = data.assessmentNo
}

// 提交测评
const handleSubmit = async () => {
  if (!questions.value.length) {
    ElMessage.warning('暂无问卷题目，请稍后重试')
    return
  }

  // 1) 校验每题都有答案
  for (const q of questions.value) {
    const ans = formAnswers[q.id]
    if (q.questionType === 'single') {
      if (!ans) {
        ElMessage.warning(`请先完成第 ${q.orderNum || ''} 题`)
        return
      }
    } else {
      if (!ans || !Array.isArray(ans) || ans.length === 0) {
        ElMessage.warning(`请先完成第 ${q.orderNum || ''} 题`)
        return
      }
    }
  }

  submitting.value = true
  try {
    // 2) 确保有 assessmentNo
    await startAssessmentIfNeeded()

    // 3) 构造答案列表
    const answersPayload = questions.value.map(q => {
      const selected = formAnswers[q.id]
      let answerStr = ''
      let score = 0

      if (q.questionType === 'single') {
        answerStr = selected as string
        const opt = q.options.find(o => o.value === answerStr)
        if (opt && typeof opt.score === 'number') {
          score = opt.score
        }
      } else {
        const selectedArr: string[] = Array.isArray(selected) ? selected : []
        answerStr = selectedArr.join(',')
        for (const val of selectedArr) {
          const opt = q.options.find(o => o.value === val)
          if (opt && typeof opt.score === 'number') {
            score += opt.score
          }
        }
      }

      return {
        questionId: q.id,
        questionCode: q.questionCode,
        answer: answerStr,
        score
      }
    })

    // 4) 调用提交接口
    const res = await axios.post(
      getApiUrl(API_CONFIG.ENDPOINTS.RISK_SUBMIT),
      {
        assessmentNo: assessmentNo.value,
        answers: answersPayload
      }
    )

    const raw = res.data
    const data = raw.data ?? raw
    submitResult.value = data as RiskSubmitResult

    ElMessage.success('风险评估提交成功')

    // 再刷新一下状态，让签约前置检查用的是最新结果
    await fetchRiskStatus()
  } catch (e) {
    console.error('提交风险评估失败', e)
    if (axios.isAxiosError(e)) {
      ElMessage.error(
        e.response?.data?.message || '提交风险评估失败，请稍后重试'
      )
    } else {
      ElMessage.error('提交风险评估失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

// 重新评估：清答案 & 清 assessmentNo & 清结果
const restartAssessment = () => {
  assessmentNo.value = ''
  submitResult.value = null
  Object.keys(formAnswers).forEach(k => delete formAnswers[Number(k)])
  // 不强制重新拉问卷，通常问卷版本不变；如需强刷，可取消注释：
  // fetchQuestionnaire()
}

// 返回上一页 / 组合列表
const goBack = () => {
  router.back()
}

// 评估完返回组合页面继续签约
const goToCombination = () => {
  router.push('/market')
}

// 工具函数：格式化时间
const formatDateTime = (value?: string | null) => {
  if (!value) return '—'
  try {
    const normalized = value.replace(' ', 'T')
    const d = new Date(normalized)
    if (Number.isNaN(d.getTime())) return value
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hh = String(d.getHours()).padStart(2, '0')
    const mm = String(d.getMinutes()).padStart(2, '0')
    return `${y}-${m}-${day} ${hh}:${mm}`
  } catch {
    return value || '—'
  }
}

onMounted(() => {
  fetchRiskStatus()
  fetchQuestionnaire()
})
</script>

<style scoped>
.risk-page {
  max-width: 900px;
  margin: 24px auto;
  padding: 0 16px 32px;
}

.risk-header {
  margin-bottom: 16px;
}

.risk-header h2 {
  margin: 0 0 8px;
}

.risk-header .sub {
  margin: 0;
  color: #666;
  font-size: 13px;
}

.status-card,
.question-card,
.result-card {
  margin-top: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.version {
  color: #999;
  font-size: 12px;
}

.loading-area {
  padding: 12px 0;
}

.status-info p {
  margin: 4px 0;
}

.status-actions {
  margin-top: 12px;
}

.mt-2 {
  margin-top: 12px;
}

.mb-2 {
  margin-bottom: 12px;
}

.question-form {
  margin-top: 8px;
}

.question-item {
  padding: 12px 0;
  border-bottom: 1px dashed #eee;
}

.question-item:last-child {
  border-bottom: none;
}

.question-title {
  display: flex;
  align-items: baseline;
  margin-bottom: 6px;
}

.q-index {
  margin-right: 4px;
}

.q-text {
  font-weight: 500;
}

.q-required {
  color: #f56c6c;
  margin-left: 4px;
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-actions {
  margin-top: 16px;
  text-align: right;
}

.result-main {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.risk-level {
  min-width: 120px;
  text-align: center;
}

.level-code {
  font-size: 32px;
  font-weight: 600;
  line-height: 1.2;
}

.level-label {
  margin-top: 4px;
  color: #666;
}

.result-info p {
  margin: 4px 0;
}

.result-info .desc {
  margin-top: 8px;
  color: #555;
  font-size: 13px;
}

.result-actions {
  margin-top: 12px;
  text-align: right;
}
</style>
