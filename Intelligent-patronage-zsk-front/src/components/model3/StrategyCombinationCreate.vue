<template>
  <div class="combination-create">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ portfolioId ? '组合详细配置' : '产品组合创建' }}</h2>
        <p class="page-subtitle">{{ portfolioId ? '对已创建的组合进行详细配置，完成后提交审核' : '按照步骤完成组合配置，完成后提交审核' }}</p>
      </div>
    </div>

    <!-- 步骤指示器 -->
    <el-steps :active="portfolioId ? currentStep - 2 : currentStep - 1" finish-status="success" class="steps-indicator">
      <el-step v-if="!portfolioId" title="选择策略" description="选择策略并创建组合" />
      <el-step title="基础信息" description="配置组合基本信息" />
      <el-step title="产品参数" description="设置费率、金额限制等" />
      <el-step title="基金持仓" description="配置基金持仓及权重" />
      <el-step title="预览确认" description="确认信息并提交审核" />
    </el-steps>

    <!-- 步骤内容区域 -->
    <el-card class="step-content-card" shadow="never">
      <!-- 步骤1: 选择策略（仅在没有portfolioId时显示） -->
      <div v-if="currentStep === 1 && !portfolioId" class="step-panel">
        <h3 class="step-title">选择策略</h3>
        <el-form
          ref="step1FormRef"
          :model="step1Form"
          :rules="step1FormRules"
          label-width="120px"
          label-position="right"
        >
          <el-form-item label="组合名称" prop="name">
            <el-input
              v-model="step1Form.name"
              placeholder="请输入组合名称"
              maxlength="50"
              show-word-limit
              style="max-width: 500px;"
            />
          </el-form-item>

          <el-form-item label="风险等级" prop="riskLevel">
            <el-select
              v-model="step1Form.riskLevel"
              placeholder="请选择风险等级"
              clearable
              filterable
              style="max-width: 500px;"
            >
              <el-option
                v-for="item in riskLevelOptions"
                :key="item"
                :label="item"
                :value="item"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="关联策略" prop="strategyRefId">
            <el-select
              v-model="step1Form.strategyRefId"
              placeholder="请选择策略"
              clearable
              filterable
              :loading="strategiesLoading"
              style="max-width: 500px;"
            >
              <el-option
                v-for="strategy in strategiesList"
                :key="strategy.strategyRefId"
                :label="formatStrategyLabel(strategy)"
                :value="strategy.strategyRefId"
              >
                <div class="strategy-option">
                  <div class="strategy-name">{{ strategy.strategyName }}</div>
                  <div class="strategy-info">
                    <span>RefID: {{ strategy.strategyRefId }}</span>
                    <span v-if="strategy.strategyType">类型: {{ strategy.strategyType }}</span>
                  </div>
                </div>
              </el-option>
            </el-select>
            <div class="form-tip">从已存在的策略中选择</div>
          </el-form-item>

          <el-form-item label="策略类型">
            <el-input
              :model-value="selectedStrategy?.strategyType || '—'"
              placeholder="自动根据策略带入"
              disabled
              style="max-width: 500px;"
            />
          </el-form-item>

          <el-form-item label="组合简介">
            <el-input
              v-model="step1Form.summary"
              type="textarea"
              :rows="3"
              placeholder="请输入组合简介（可选）"
              maxlength="200"
              show-word-limit
              style="max-width: 500px;"
            />
          </el-form-item>

          <el-form-item label="目标客户">
            <el-input
              v-model="step1Form.targetInvestor"
              placeholder="请输入目标客户（可选）"
              maxlength="50"
              style="max-width: 500px;"
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤2: 基础信息 -->
      <div v-if="currentStep === 2" class="step-panel">
        <div class="step-header">
          <h3 class="step-title">配置基础信息</h3>
          <el-alert
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 24px;"
          >
            <template #title>
              <span>此步骤为可选步骤，您可以修改组合的基础信息，也可以直接跳过进入下一步。</span>
            </template>
          </el-alert>
        </div>
        <el-form
          ref="step2FormRef"
          :model="step2Form"
          label-width="120px"
          label-position="right"
        >
          <el-form-item label="组合名称">
            <el-input
              v-model="step2Form.name"
              placeholder="请输入组合名称"
              maxlength="50"
              show-word-limit
              style="max-width: 500px;"
            />
          </el-form-item>

          <el-form-item label="风险等级">
            <el-select
              v-model="step2Form.riskLevel"
              placeholder="请选择风险等级"
              clearable
              filterable
              style="max-width: 500px;"
            >
              <el-option
                v-for="item in riskLevelOptions"
                :key="item"
                :label="item"
                :value="item"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="策略类型">
            <el-input
              v-model="step2Form.strategyType"
              placeholder="请输入策略类型"
              maxlength="50"
              style="max-width: 500px;"
            />
          </el-form-item>

          <el-form-item label="组合简介">
            <el-input
              v-model="step2Form.summary"
              type="textarea"
              :rows="3"
              placeholder="请输入组合简介"
              maxlength="200"
              show-word-limit
              style="max-width: 500px;"
            />
          </el-form-item>

          <el-form-item label="目标客户">
            <el-input
              v-model="step2Form.targetInvestor"
              placeholder="请输入目标客户"
              maxlength="50"
              style="max-width: 500px;"
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤3: 产品参数 -->
      <div v-if="currentStep === 3" class="step-panel">
        <h3 class="step-title">配置产品参数</h3>
        <el-form
          ref="step3FormRef"
          :model="step3Form"
          :rules="step3FormRules"
          label-width="140px"
          label-position="right"
        >
          <el-form-item label="最低投资额(元)" prop="minInvestAmount">
            <el-input-number
              v-model="step3Form.minInvestAmount"
              :min="0"
              :precision="2"
              :step="1000"
              controls-position="right"
              style="width: 100%; max-width: 500px;"
            />
          </el-form-item>

          <el-form-item label="最高投资额(元)" prop="maxInvestAmount">
            <el-input-number
              v-model="step3Form.maxInvestAmount"
              :min="0"
              :precision="2"
              :step="10000"
              controls-position="right"
              style="width: 100%; max-width: 500px;"
            />
          </el-form-item>

          <el-form-item label="申购费率(%)" prop="subscriptionFee">
            <el-input-number
              v-model="step3Form.subscriptionFee"
              :min="0"
              :max="100"
              :precision="2"
              :step="0.1"
              controls-position="right"
              style="width: 100%; max-width: 500px;"
            />
          </el-form-item>

          <el-form-item label="赎回费率(%)" prop="redemptionFee">
            <el-input-number
              v-model="step3Form.redemptionFee"
              :min="0"
              :max="100"
              :precision="2"
              :step="0.1"
              controls-position="right"
              style="width: 100%; max-width: 500px;"
            />
          </el-form-item>

          <el-form-item label="管理费率(%)" prop="managementFee">
            <el-input-number
              v-model="step3Form.managementFee"
              :min="0"
              :max="100"
              :precision="2"
              :step="0.1"
              controls-position="right"
              style="width: 100%; max-width: 500px;"
            />
          </el-form-item>

          <el-form-item label="开放日规则">
            <el-input
              v-model="step3Form.openDayRule"
              placeholder="例如：工作日开放"
              maxlength="100"
              style="max-width: 500px;"
            />
          </el-form-item>

          <el-form-item label="赎回规则">
            <el-input
              v-model="step3Form.redemptionRule"
              placeholder="例如：T+1赎回"
              maxlength="100"
              style="max-width: 500px;"
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤4: 基金持仓 -->
      <div v-if="currentStep === 4" class="step-panel">
        <h3 class="step-title">配置基金持仓</h3>
        <div class="holdings-header">
          <el-button type="primary" @click="showAddFundDialog" :icon="Plus">添加基金</el-button>
          <div class="weight-sum" :class="weightSumClass">
            权重总和: {{ weightSum.toFixed(2) }}%
            <span v-if="isWeightValid">✓</span>
            <span v-else-if="weightSum > 100">(超出)</span>
            <span v-else>(不足)</span>
          </div>
        </div>

        <el-table :data="step4Holdings" border stripe style="margin-top: 16px;">
          <el-table-column label="基金代码" width="140">
            <template #default="scope">
              {{ scope.row.fundCodeDisplay || scope.row.fundCode || '—' }}
            </template>
          </el-table-column>
          <el-table-column prop="fundName" label="基金名称" min-width="200" show-overflow-tooltip />
          <el-table-column label="权重(%)" width="150">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.weight"
                :min="0"
                :max="100"
                :precision="2"
                :step="1"
                controls-position="right"
                style="width: 100%;"
                @change="updateWeightSum"
              />
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="150">
            <template #default="scope">
              <el-input
                v-model="scope.row.remark"
                placeholder="备注"
                maxlength="50"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button
                type="danger"
                size="small"
                @click="removeHolding(scope.$index)"
                :icon="Delete"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="step4Holdings.length === 0" class="empty-holdings">
          <el-empty description="暂无持仓，请添加基金" />
        </div>
      </div>

      <!-- 步骤5: 预览确认 -->
      <div v-if="currentStep === 5" class="step-panel">
        <h3 class="step-title">预览确认</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="组合名称">{{ previewData.name || '—' }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">{{ previewData.riskLevel || '—' }}</el-descriptions-item>
          <el-descriptions-item label="策略类型">{{ previewData.strategyType || '—' }}</el-descriptions-item>
          <el-descriptions-item label="目标客户">{{ previewData.targetInvestor || '—' }}</el-descriptions-item>
          <el-descriptions-item label="组合简介" :span="2">{{ previewData.summary || '—' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>产品参数</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="最低投资额">{{ formatCurrency(previewData.minInvestAmount) }}</el-descriptions-item>
          <el-descriptions-item label="最高投资额">{{ formatCurrency(previewData.maxInvestAmount) }}</el-descriptions-item>
          <el-descriptions-item label="申购费率">{{ formatPercent(previewData.subscriptionFee) }}</el-descriptions-item>
          <el-descriptions-item label="赎回费率">{{ formatPercent(previewData.redemptionFee) }}</el-descriptions-item>
          <el-descriptions-item label="管理费率">{{ formatPercent(previewData.managementFee) }}</el-descriptions-item>
          <el-descriptions-item label="开放日规则">{{ previewData.openDayRule || '—' }}</el-descriptions-item>
          <el-descriptions-item label="赎回规则">{{ previewData.redemptionRule || '—' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>基金持仓</el-divider>
        <el-table :data="previewData.holdings" border stripe>
          <el-table-column label="基金代码" width="140">
            <template #default="scope">
              {{ scope.row.fundCodeDisplay || scope.row.fundCode || '—' }}
            </template>
          </el-table-column>
          <el-table-column prop="fundName" label="基金名称" min-width="200" />
          <el-table-column label="权重(%)" width="120" align="right">
            <template #default="scope">
              {{ scope.row.weight?.toFixed(2) || '0.00' }}
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" />
        </el-table>
      </div>
    </el-card>

    <!-- 操作按钮 -->
    <div class="step-actions">
      <el-button v-if="currentStep > (portfolioId ? 2 : 1)" @click="prevStep">上一步</el-button>
      <el-button 
        v-if="currentStep === 2" 
        text 
        @click="handleStep2(true)"
      >
        跳过此步
      </el-button>
      <el-button v-if="currentStep < 5" type="primary" @click="nextStep" :loading="saving">
        {{ currentStep === 1 ? '创建并下一步' : currentStep === 2 ? '保存并下一步' : '保存并下一步' }}
      </el-button>
      <el-button v-if="currentStep === 5" type="success" @click="submitForReview" :loading="submitting">
        提交审核
      </el-button>
      <el-button v-if="currentStep < 5 && currentStep > 1" text @click="saveDraft">保存草稿</el-button>
    </div>

    <!-- 添加基金对话框 -->
    <el-dialog v-model="addFundDialogVisible" title="添加基金" width="600px" @open="handleDialogOpen">
      <el-form :model="newFund" label-width="100px">
        <el-form-item label="选择基金" required>
          <el-select
            v-model="newFund.fundCode"
            placeholder="请选择或搜索基金"
            filterable
            remote
            :remote-method="searchFunds"
            :loading="fundsLoading"
            clearable
            style="width: 100%;"
            @change="handleFundCodeChange"
          >
            <el-option
              v-for="fund in fundsList"
              :key="fund.fundCode"
              :label="`${fund.fundCodeDisplay} - ${fund.fundName || '未知基金'}`"
              :value="fund.fundCode"
            >
              <span class="fund-option-text">{{ fund.fundCodeDisplay }} - {{ fund.fundName || '未知基金' }}</span>
            </el-option>
          </el-select>
          <div class="form-tip">支持输入关键词搜索基金代码或名称</div>
        </el-form-item>
        <el-form-item label="基金代码">
          <el-input :model-value="newFund.fundCodeDisplay || newFund.fundCode" disabled />
        </el-form-item>
        <el-form-item label="基金名称">
          <el-input :model-value="newFund.fundName" disabled />
        </el-form-item>
        <el-form-item label="权重(%)" required>
          <el-input-number
            v-model="newFund.weight"
            :min="0"
            :max="100"
            :precision="2"
            :step="1"
            controls-position="right"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="newFund.remark" placeholder="备注（可选）" maxlength="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addFundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addFund" :disabled="!newFund.fundCode || !newFund.fundName">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { getApiUrl, API_CONFIG } from '../../config/api'

// 策略数据类型
interface Strategy {
  strategyId?: number
  strategyRefId: number
  strategyName: string
  strategyType?: string
  description?: string
}

// 持仓数据类型
interface Holding {
  fundCode: string // 原始基金代码（提交后端）
  fundCodeDisplay: string // 显示用基金代码
  fundName: string
  weight: number
  remark?: string
}

// 基金数据类型
interface Fund {
  fundCode: string // 原始基金代码
  fundCodeDisplay: string // 显示用基金代码
  fundName: string
  [key: string]: any
}

interface HoldingDisplay {
  fundCode: string
  fundName: string
  weight: number
  remark?: string
}

type PayloadItem = {
  type: 'decimal' | 'percent'
  data: Array<{
    fundCode: string
    fundName: string
    weight: number
    remark?: string
  }>
}

const router = useRouter()
const riskLevelOptions = ['低', '中低', '中', '中高', '高']

function normalizeFundCodeRaw(value: string | number | null | undefined): string {
  if (value === null || value === undefined) return ''
  return String(value).trim()
}

function formatFundCodeDisplay(value: string): string {
  if (!value) return ''
  return value.replace(/\.0+$/, '')
}

function convertWeightToPercent(value: number | string | null | undefined): number {
  let num = Number(value) || 0
  if (Math.abs(num) <= 1) {
    num = num * 100
  }
  return Math.round(num * 100) / 100
}

// 步骤管理
const props = defineProps<{
  portfolioId?: number | null
}>()

const currentStep = ref(1)
const portfolioId = ref<number | null>(props.portfolioId || null)
const saving = ref(false)
const submitting = ref(false)

// 策略列表
const strategiesList = ref<Strategy[]>([])
const strategiesLoading = ref(false)

// 基金列表
const fundsList = ref<Fund[]>([])
const fundsLoading = ref(false)
const fundsSearchKeyword = ref('')

// 表单引用
const step1FormRef = ref<FormInstance>()
const step2FormRef = ref<FormInstance>()
const step3FormRef = ref<FormInstance>()

// 步骤1表单
const step1Form = reactive({
  name: '',
  riskLevel: '',
  strategyType: '',
  strategyRefId: undefined as number | undefined,
  summary: '',
  targetInvestor: ''
})

const validateStrategyRefId = (_rule: any, value: number | undefined, callback: (error?: Error) => void) => {
  if (value === undefined || value === null) {
    callback(new Error('请选择策略'))
    return
  }
  callback()
}

const step1FormRules: FormRules = {
  name: [
    { required: true, message: '请输入组合名称', trigger: 'blur' },
    { min: 2, max: 50, message: '组合名称长度需在 2-50 个字符之间', trigger: 'blur' }
  ],
  strategyRefId: [{ validator: validateStrategyRefId, trigger: 'change' }]
}

// 步骤2表单
const step2Form = reactive({
  name: '',
  riskLevel: '',
  strategyType: '',
  summary: '',
  targetInvestor: '',
  strategyId: undefined as number | undefined
})

// 步骤3表单
const step3Form = reactive({
  minInvestAmount: undefined as number | undefined,
  maxInvestAmount: undefined as number | undefined,
  subscriptionFee: undefined as number | undefined,
  redemptionFee: undefined as number | undefined,
  managementFee: undefined as number | undefined,
  openDayRule: '',
  redemptionRule: ''
})

const validateMaxAmount = (_rule: any, value: number | undefined, callback: (error?: Error) => void) => {
  if (step3Form.minInvestAmount && value && value <= step3Form.minInvestAmount) {
    callback(new Error('最高投资额必须大于最低投资额'))
    return
  }
  callback()
}

const step3FormRules: FormRules = {
  maxInvestAmount: [{ validator: validateMaxAmount, trigger: 'change' }]
}

// 步骤4持仓
const step4Holdings = ref<Holding[]>([])
const addFundDialogVisible = ref(false)
const newFund = reactive({
  fundCode: '',
  fundCodeDisplay: '',
  fundName: '',
  weight: 0,
  remark: ''
})

// 预览数据
const previewData = computed(() => ({
  name: step2Form.name || step1Form.name,
  riskLevel: step2Form.riskLevel || step1Form.riskLevel,
  strategyType: step2Form.strategyType || step1Form.strategyType,
  summary: step2Form.summary || step1Form.summary,
  targetInvestor: step2Form.targetInvestor || step1Form.targetInvestor,
  minInvestAmount: step3Form.minInvestAmount,
  maxInvestAmount: step3Form.maxInvestAmount,
  subscriptionFee: step3Form.subscriptionFee,
  redemptionFee: step3Form.redemptionFee,
  managementFee: step3Form.managementFee,
  openDayRule: step3Form.openDayRule,
  redemptionRule: step3Form.redemptionRule,
  holdings: step4Holdings.value
}))

// 权重总和
const weightSum = computed(() => {
  const sum = step4Holdings.value.reduce((sum, h) => sum + (Number(h.weight) || 0), 0)
  // 处理浮点数精度问题，保留2位小数
  return Math.round(sum * 100) / 100
})

const isWeightValid = computed(() => {
  // 允许0.01的误差，处理浮点数精度问题
  return Math.abs(weightSum.value - 100) <= 0.01
})

const weightSumClass = computed(() => {
  if (isWeightValid.value) return 'weight-valid'
  if (weightSum.value > 100) return 'weight-error'
  return 'weight-warning'
})

// 选中的策略
const selectedStrategy = computed(() =>
  strategiesList.value.find(strategy => strategy.strategyRefId === step1Form.strategyRefId)
)

watch(selectedStrategy, (strategy) => {
  step1Form.strategyType = strategy?.strategyType || ''
})

// 格式化函数
const formatStrategyLabel = (strategy: Strategy) => {
  const pieces = [`${strategy.strategyName}`]
  pieces.push(`RefID: ${strategy.strategyRefId}`)
  if (strategy.strategyType) {
    pieces.push(`类型: ${strategy.strategyType}`)
  }
  return pieces.join(' | ')
}

const formatCurrency = (value: number | undefined) => {
  if (value === undefined || value === null) return '—'
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(value)
}

const formatPercent = (value: number | undefined) => {
  if (value === undefined || value === null) return '—'
  return `${value.toFixed(2)}%`
}

// 获取策略列表
const normalizeStrategy = (item: any): Strategy | null => {
  const strategyRefId = Number(
    item.strategyRefId ??
      item.strategy_ref_id ??
      item.strategyId ??
      item.strategy_id ??
      item.id
  )

  if (!strategyRefId || Number.isNaN(strategyRefId)) {
    return null
  }

  const strategyIdValue = item.strategyId ?? item.strategy_id
  const strategyName =
    item.strategyName ?? item.strategy_name ?? item.name ?? `策略${strategyRefId}`
  const strategyType =
    item.strategyType ?? item.strategy_type ?? item.type ?? ''
  const description = item.description ?? item.remark ?? ''
  return {
    strategyId: strategyIdValue ? Number(strategyIdValue) : undefined,
    strategyRefId,
    strategyName,
    strategyType,
    description
  }
}

const fetchStrategies = async () => {
  strategiesLoading.value = true
  try {
    const response = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.STRATEGIES_LIST))

    let strategies: Strategy[] = []
    if (response.data && response.data.code === 200 && response.data.data) {
      if (Array.isArray(response.data.data)) {
        strategies = response.data.data
          .map(normalizeStrategy)
          .filter((item: Strategy | null): item is Strategy => item !== null)
      }
    } else if (Array.isArray(response.data)) {
      strategies = response.data
        .map(normalizeStrategy)
        .filter((item: Strategy | null): item is Strategy => item !== null)
    }

    if (!Array.isArray(strategies) || strategies.length === 0) {
      ElMessage.warning('后端未返回策略列表，请确认接口已实现并返回数据')
    } else {
      strategiesList.value = strategies.sort((a, b) => a.strategyRefId - b.strategyRefId)
    }
  } catch (error) {
    console.error('获取策略列表失败', error)
    if (axios.isAxiosError(error)) {
      ElMessage.error(error.response?.data?.message || '获取策略列表失败，请检查后端接口')
    } else {
      ElMessage.error('获取策略列表失败，请稍后重试')
    }
  } finally {
    strategiesLoading.value = false
  }
}

// 步骤1: 创建组合
const handleStep1 = async () => {
  if (!step1FormRef.value) return
  try {
    await step1FormRef.value.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    const payload = {
      name: step1Form.name.trim(),
      riskLevel: step1Form.riskLevel || undefined,
      strategyType: selectedStrategy.value?.strategyType || undefined,
      strategyRefId: step1Form.strategyRefId,
      summary: step1Form.summary || undefined,
      targetInvestor: step1Form.targetInvestor || undefined
    }

    const response = await axios.post(
      getApiUrl(API_CONFIG.ENDPOINTS.STRATEGY_COMBINATION_CREATE),
      payload
    )

    const success =
      (response.data && response.data.code === 200) ||
      response.status === 200 ||
      response.status === 201

    if (success) {
      portfolioId.value = response.data?.data?.portfolioId || response.data?.portfolioId
      // 同步到步骤2表单
      step2Form.name = step1Form.name
      step2Form.riskLevel = step1Form.riskLevel
      step2Form.strategyType = step1Form.strategyType
      step2Form.summary = step1Form.summary
      step2Form.targetInvestor = step1Form.targetInvestor
      step2Form.strategyId = selectedStrategy.value?.strategyRefId

      ElMessage.success('组合创建成功')
      currentStep.value = 2
    } else {
      ElMessage.error(response.data?.message || '创建失败')
    }
  } catch (error) {
    console.error('创建策略组合失败', error)
    if (axios.isAxiosError(error)) {
      ElMessage.error(error.response?.data?.message || '创建失败，请稍后重试')
    } else {
      ElMessage.error('创建失败，请稍后重试')
    }
  } finally {
    saving.value = false
  }
}

// 步骤2: 保存基础信息（可选）
const handleStep2 = async (skipSave = false) => {
  if (!portfolioId.value) {
    ElMessage.error('组合ID不存在，请重新创建')
    return
  }

  // 如果跳过保存，直接进入下一步
  if (skipSave) {
    currentStep.value = 3
    return
  }

  saving.value = true
  try {
    const payload = {
      name: step2Form.name.trim(),
      riskLevel: step2Form.riskLevel || undefined,
      strategyType: step2Form.strategyType || undefined,
      summary: step2Form.summary || undefined,
      targetInvestor: step2Form.targetInvestor || undefined,
      strategyId: step2Form.strategyId
    }

    const response = await axios.put(
      getApiUrl(API_CONFIG.ENDPOINTS.UPDATE_BASIC_INFO(portfolioId.value)),
      payload
    )

    if (response.data?.code === 200 || response.status === 200) {
      ElMessage.success('基础信息保存成功')
      currentStep.value = 3
    } else {
      ElMessage.error(response.data?.message || '保存失败')
    }
  } catch (error) {
    console.error('保存基础信息失败', error)
    if (axios.isAxiosError(error)) {
      ElMessage.error(error.response?.data?.message || '保存失败，请稍后重试')
    } else {
      ElMessage.error('保存失败，请稍后重试')
    }
  } finally {
    saving.value = false
  }
}

// 步骤3: 保存产品参数
const handleStep3 = async () => {
  if (!portfolioId.value) {
    ElMessage.error('组合ID不存在，请重新创建')
    return
  }

  if (!step3FormRef.value) return
  try {
    await step3FormRef.value.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    const payload = {
      minInvestAmount: step3Form.minInvestAmount,
      maxInvestAmount: step3Form.maxInvestAmount,
      subscriptionFee: step3Form.subscriptionFee,
      redemptionFee: step3Form.redemptionFee,
      managementFee: step3Form.managementFee,
      openDayRule: step3Form.openDayRule || undefined,
      redemptionRule: step3Form.redemptionRule || undefined
    }

    const response = await axios.post(
      getApiUrl(API_CONFIG.ENDPOINTS.SAVE_PRODUCT_PARAMS(portfolioId.value)),
      payload
    )

    if (response.data?.code === 200 || response.status === 200) {
      ElMessage.success('产品参数保存成功')
      currentStep.value = 4
    } else {
      ElMessage.error(response.data?.message || '保存失败')
    }
  } catch (error) {
    console.error('保存产品参数失败', error)
    if (axios.isAxiosError(error)) {
      ElMessage.error(error.response?.data?.message || '保存失败，请稍后重试')
    } else {
      ElMessage.error('保存失败，请稍后重试')
    }
  } finally {
    saving.value = false
  }
}

// 步骤4: 保存持仓
const handleStep4 = async () => {
  if (!portfolioId.value) {
    ElMessage.error('组合ID不存在，请重新创建')
    return
  }

  // 验证权重（在保存前再次验证，确保精度）
  const totalWeight = step4Holdings.value.reduce((sum, h) => {
    const w = Number(h.weight) || 0
    return sum + w
  }, 0)
  const roundedTotal = Math.round(totalWeight * 100) / 100
  
  if (Math.abs(roundedTotal - 100) > 0.01) {
    ElMessage.error(`持仓权重总和必须为100%，当前为 ${roundedTotal.toFixed(2)}%`)
    return
  }

  if (step4Holdings.value.length === 0) {
    ElMessage.warning('请至少添加一个基金持仓')
    return
  }

  saving.value = true
  try {
    // 预处理持仓数据，生成百分比和小数两种形式
    const formattedHoldings = step4Holdings.value.map((holding) => {
      const rawFundCode = normalizeFundCodeRaw(holding.fundCode)
      const fundName = String(holding.fundName || '')
      const remark = holding.remark ? String(holding.remark) : undefined
      const displayCode = holding.fundCodeDisplay || formatFundCodeDisplay(rawFundCode)

      let percentValue = Number(holding.weight) || 0

      if (percentValue < 0) percentValue = 0
      if (percentValue > 100) percentValue = 100
      percentValue = Math.round(percentValue * 100) / 100

      const decimalValue = Math.round((percentValue / 100) * 10000) / 10000

      return {
        fundCodeRaw: rawFundCode,
        fundCodeDisplay: displayCode,
        fundName,
        remark,
        percentValue,
        decimalValue
      }
    })

    const missingFundCode = formattedHoldings.find(h => !h.fundCodeRaw)
    if (missingFundCode) {
      ElMessage.error('存在基金代码为空的持仓，请重新选择基金')
      saving.value = false
      return
    }

    const totalWeightPercent = formattedHoldings.reduce((sum, h) => sum + h.percentValue, 0)
    const roundedTotalPercent = Math.round(totalWeightPercent * 100) / 100

    console.log('持仓权重总和(百分比):', totalWeightPercent, '四舍五入:', roundedTotalPercent)

    if (Math.abs(roundedTotalPercent - 100) > 0.01) {
      ElMessage.error(`持仓权重总和必须为100%，当前为 ${roundedTotalPercent.toFixed(2)}%，请检查权重配置`)
      saving.value = false
      return
    }

    const convertedWeightSum = formattedHoldings.reduce((sum, h) => sum + h.decimalValue, 0)
    const roundedConvertedSum = Math.round(convertedWeightSum * 10000) / 10000

    console.log('持仓权重总和(小数):', convertedWeightSum, '四舍五入:', roundedConvertedSum)

    if (Math.abs(roundedConvertedSum - 1) > 0.0001) {
      ElMessage.error(`权重转换后总和必须为1.0，当前为 ${roundedConvertedSum.toFixed(4)}，请检查权重配置`)
      saving.value = false
      return
    }

    // 构建两套待发送的 payload：优先使用小数，必要时降级为百分比
    const payloads: PayloadItem[] = [
      {
        type: 'decimal',
        data: formattedHoldings.map(h => ({
          fundCode: h.fundCodeRaw,
          fundName: h.fundName,
          weight: h.decimalValue,
          remark: h.remark
        }))
      },
      {
        type: 'percent',
        data: formattedHoldings.map(h => ({
          fundCode: h.fundCodeRaw,
          fundName: h.fundName,
          weight: h.percentValue,
          remark: h.remark
        }))
      }
    ]

    let lastError: unknown = null

    for (const item of payloads) {
      try {
        console.log(`保存持仓请求数据（${item.type}）:`, {
          portfolioId: portfolioId.value,
          holdings: item.data
        })

        const response = await axios.post(
          getApiUrl(API_CONFIG.ENDPOINTS.SAVE_HOLDINGS(portfolioId.value)),
          item.data,
          {
            headers: {
              'Content-Type': 'application/json'
            }
          }
        )

        console.log('保存持仓响应:', response.data)

        if (response.data?.code === 200 || response.status === 200) {
          ElMessage.success('持仓保存成功')
          currentStep.value = 5
          return
        }

        // 非 200 响应，抛出错误以进入下一次尝试
        lastError = response.data
        throw new Error(response.data?.message || '保存失败')
      } catch (error) {
        lastError = error

        if (axios.isAxiosError(error)) {
          const errorMessage = error.response?.data?.message || error.message || ''
          console.warn(`保存持仓失败（${item.type} 尝试）:`, errorMessage)

          // 如果还有备用 payload（百分比）且错误提示和权重相关，则继续下一次尝试
          const shouldRetry =
            item.type === 'decimal' &&
            payloads[1] &&
            error.response?.status === 400 &&
            typeof errorMessage === 'string' &&
            errorMessage.includes('100')

          if (shouldRetry) {
            console.info('检测到后端要求权重总和为100%，尝试以百分比形式重试...')
            continue
          }
        }

        // 其他情况直接抛出，停止后续尝试
        throw error
      }
    }

    // 如果所有尝试都失败，抛出最后一次错误
    throw lastError ?? new Error('保存失败')
  } catch (error) {
    console.error('保存持仓失败', error)
    if (axios.isAxiosError(error)) {
      const errorMessage = error.response?.data?.message ||
        error.response?.data?.error ||
        error.response?.data ||
        error.message ||
        '保存失败，请稍后重试'
      console.error('后端返回错误详情:', {
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data
      })
      ElMessage.error(`保存失败: ${errorMessage}`)
    } else {
      ElMessage.error('保存失败，请稍后重试')
    }
  } finally {
    saving.value = false
  }
}

// 提交审核
const submitForReview = async () => {
  if (!portfolioId.value) {
    ElMessage.error('组合ID不存在，请重新创建')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确定要提交审核吗？提交后将无法修改，等待审核人员审批。',
      '确认提交',
      {
        confirmButtonText: '确定提交',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  submitting.value = true
  try {
    const response = await axios.post(
      getApiUrl(API_CONFIG.ENDPOINTS.SUBMIT_FOR_REVIEW(portfolioId.value))
    )

    if (response.data?.code === 200 || response.status === 200) {
      ElMessage.success('提交审核成功，等待审核人员审批')
      // 如果是从配置页面来的，跳转到配置列表，否则跳转到审核页面
      if (props.portfolioId) {
        router.push('/combination/configure')
      } else {
        router.push('/audit')
      }
    } else {
      ElMessage.error(response.data?.message || '提交失败')
    }
  } catch (error) {
    console.error('提交审核失败', error)
    if (axios.isAxiosError(error)) {
      ElMessage.error(error.response?.data?.message || '提交失败，请稍后重试')
    } else {
      ElMessage.error('提交失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

// 加载预览数据
const loadPreviewData = async () => {
  if (!portfolioId.value) return

  try {
    // 加载产品参数
    try {
      const paramsResponse = await axios.get(
        getApiUrl(API_CONFIG.ENDPOINTS.GET_PRODUCT_PARAMS(portfolioId.value))
      )
      if (paramsResponse.data?.code === 200 && paramsResponse.data?.data) {
        const data = paramsResponse.data.data
        step3Form.minInvestAmount = data.minInvestAmount
        step3Form.maxInvestAmount = data.maxInvestAmount
        step3Form.subscriptionFee = data.subscriptionFee
        step3Form.redemptionFee = data.redemptionFee
        step3Form.managementFee = data.managementFee
        step3Form.openDayRule = data.openDayRule || ''
        step3Form.redemptionRule = data.redemptionRule || ''
      }
    } catch (error) {
      console.warn('加载产品参数失败', error)
    }

    // 加载持仓
    try {
      const holdingsResponse = await axios.get(
        getApiUrl(API_CONFIG.ENDPOINTS.GET_HOLDINGS(portfolioId.value))
      )
      if (holdingsResponse.data?.code === 200 && holdingsResponse.data?.data) {
        const holdings = holdingsResponse.data.data
        if (Array.isArray(holdings)) {
          step4Holdings.value = holdings.map((h: any) => ({
            fundCode: normalizeFundCodeRaw(h.fundCode || h.fund_code || ''),
            fundCodeDisplay: formatFundCodeDisplay(h.fundCode || h.fund_code || ''),
            fundName: h.fundName || h.fund_name || '',
            weight: convertWeightToPercent(h.weight),
            remark: h.remark || ''
          }))
        }
      }
    } catch (error) {
      console.warn('加载持仓失败', error)
    }
  } catch (error) {
    console.error('加载预览数据失败', error)
  }
}

// 监听步骤变化，进入步骤5时加载数据
watch(currentStep, (newStep) => {
  if (newStep === 5) {
    loadPreviewData()
  }
})

// 步骤导航
const nextStep = async () => {
  if (currentStep.value === 1 && !portfolioId.value) {
    await handleStep1()
  } else if (currentStep.value === 2) {
    // 步骤2可选，可以跳过
    await handleStep2(false) // false表示保存，true表示跳过
  } else if (currentStep.value === 3) {
    await handleStep3()
  } else if (currentStep.value === 4) {
    await handleStep4()
  }
}

const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

// 获取基金列表
const normalizeFund = (item: any): Fund | null => {
  const fundCodeRaw = item.fundCode ?? item.fund_code ?? item.code
  const fundCode = normalizeFundCodeRaw(fundCodeRaw)
  const fundCodeDisplay = formatFundCodeDisplay(fundCodeRaw)
  const fundName = item.fundName ?? item.fund_name ?? item.name

  if (!fundCode) {
    return null
  }

  return {
    ...item,
    fundCode,
    fundCodeDisplay,
    fundName: fundName || `基金${fundCode}`
  }
}

const fetchFunds = async (keyword?: string) => {
  fundsLoading.value = true
  try {
    const url = getApiUrl(API_CONFIG.ENDPOINTS.FUNDS_LIST)
    const params = keyword ? { keyword } : {}
    
    const response = await axios.get(url, { params })

    let funds: Fund[] = []
    if (response.data && response.data.code === 200 && response.data.data) {
      if (Array.isArray(response.data.data)) {
        funds = response.data.data
          .map(normalizeFund)
          .filter((item: Fund | null): item is Fund => item !== null)
      }
    } else if (Array.isArray(response.data)) {
      funds = response.data
        .map(normalizeFund)
        .filter((item: Fund | null): item is Fund => item !== null)
    }

    fundsList.value = funds
  } catch (error) {
    console.error('获取基金列表失败', error)
    if (axios.isAxiosError(error)) {
      ElMessage.error(error.response?.data?.message || '获取基金列表失败，请检查后端接口')
    } else {
      ElMessage.error('获取基金列表失败，请稍后重试')
    }
  } finally {
    fundsLoading.value = false
  }
}

// 搜索基金（远程搜索）
const searchFunds = (keyword: string) => {
  fundsSearchKeyword.value = keyword
  if (keyword) {
    fetchFunds(keyword)
  } else {
    fetchFunds()
  }
}

// 根据基金代码获取基金详情
const fetchFundByCode = async (fundCode: string) => {
  if (!fundCode) return null

  try {
    const response = await axios.get(
      getApiUrl(API_CONFIG.ENDPOINTS.FUND_BY_CODE(fundCode))
    )

    let fund: Fund | null = null
    if (response.data && response.data.code === 200 && response.data.data) {
      fund = normalizeFund(response.data.data)
    } else if (response.data) {
      fund = normalizeFund(response.data)
    }

    return fund
  } catch (error) {
    console.error('获取基金详情失败', error)
    return null
  }
}

// 持仓管理
const showAddFundDialog = () => {
  newFund.fundCode = ''
  newFund.fundCodeDisplay = ''
  newFund.fundName = ''
  newFund.weight = 0
  newFund.remark = ''
  addFundDialogVisible.value = true
}

const handleDialogOpen = () => {
  // 对话框打开时加载基金列表
  if (fundsList.value.length === 0) {
    fetchFunds()
  }
}

const handleFundCodeChange = async (fundCode: string) => {
  const sanitizedCode = normalizeFundCodeRaw(fundCode)
  if (!sanitizedCode) {
    newFund.fundCode = ''
    newFund.fundCodeDisplay = ''
    newFund.fundName = ''
    return
  }

  newFund.fundCode = sanitizedCode

  // 先从列表中查找
  const fund = fundsList.value.find(f => f.fundCode === sanitizedCode)
  if (fund) {
    newFund.fundCodeDisplay = fund.fundCodeDisplay
    newFund.fundName = fund.fundName
  } else {
    // 如果列表中找不到，通过接口获取
    const fundDetail = await fetchFundByCode(sanitizedCode)
    if (fundDetail) {
      newFund.fundCodeDisplay = fundDetail.fundCodeDisplay
      newFund.fundName = fundDetail.fundName
    } else {
      newFund.fundCodeDisplay = formatFundCodeDisplay(sanitizedCode)
      newFund.fundName = ''
      ElMessage.warning('未找到该基金信息')
      newFund.fundCode = ''
      newFund.fundCodeDisplay = ''
    }
  }
}

const addFund = () => {
  if (!newFund.fundCode || !newFund.fundName) {
    ElMessage.warning('请选择基金')
    return
  }

  const sanitizedCode = normalizeFundCodeRaw(newFund.fundCode)
  if (!sanitizedCode) {
    ElMessage.warning('基金代码无效')
    return
  }

  // 检查是否已添加该基金
  const exists = step4Holdings.value.some(h => h.fundCode === sanitizedCode)
  if (exists) {
    ElMessage.warning('该基金已添加，请勿重复添加')
    return
  }

  step4Holdings.value.push({
    fundCode: sanitizedCode,
    fundCodeDisplay: newFund.fundCodeDisplay,
    fundName: newFund.fundName,
    weight: newFund.weight || 0,
    remark: newFund.remark
  })
  addFundDialogVisible.value = false
  updateWeightSum()
  ElMessage.success('基金添加成功')
}

const removeHolding = (index: number) => {
  step4Holdings.value.splice(index, 1)
  updateWeightSum()
}

const updateWeightSum = () => {
  // 触发计算属性更新
}

// 保存草稿
const saveDraft = async () => {
  if (!portfolioId.value) {
    ElMessage.warning('请先完成第一步创建组合')
    return
  }

  saving.value = true
  try {
    // 根据当前步骤保存对应数据
    if (currentStep.value >= 2) {
      await handleStep2()
    }
    if (currentStep.value >= 3) {
      await handleStep3()
    }
    if (currentStep.value >= 4) {
      await handleStep4()
    }
    ElMessage.success('草稿已保存')
  } catch (error) {
    ElMessage.error('保存草稿失败')
  } finally {
    saving.value = false
  }
}

// 加载已有数据（编辑模式）
const loadExistingData = async () => {
  // 如果传入了 portfolioId，加载已有数据并跳转到步骤2
  if (portfolioId.value) {
    try {
      // 加载基础信息
      const response = await axios.get(
        getApiUrl(API_CONFIG.ENDPOINTS.STRATEGY_COMBINATION_ALL)
      )
      
      let dataArray: any[] = []
      if (response.data && response.data.code === 200 && response.data.data) {
        if (Array.isArray(response.data.data)) {
          dataArray = response.data.data
        }
      } else if (Array.isArray(response.data)) {
        dataArray = response.data
      }
      
      const portfolio = dataArray.find((p: any) => p.portfolioId === portfolioId.value)
      if (portfolio) {
        step2Form.name = portfolio.portfolioName || ''
        step2Form.riskLevel = portfolio.riskLevel || ''
        step2Form.strategyType = portfolio.portfolioStrategyType || portfolio.strategyType || ''
        step2Form.strategyId = portfolio.strategyRefId || portfolio.strategyId
        currentStep.value = 2
      }
      
      // 加载产品参数
      try {
        const paramsResponse = await axios.get(
          getApiUrl(API_CONFIG.ENDPOINTS.GET_PRODUCT_PARAMS(portfolioId.value))
        )
        if (paramsResponse.data?.code === 200 && paramsResponse.data?.data) {
          const data = paramsResponse.data.data
          step3Form.minInvestAmount = data.minInvestAmount
          step3Form.maxInvestAmount = data.maxInvestAmount
          step3Form.subscriptionFee = data.subscriptionFee
          step3Form.redemptionFee = data.redemptionFee
          step3Form.managementFee = data.managementFee
          step3Form.openDayRule = data.openDayRule || ''
          step3Form.redemptionRule = data.redemptionRule || ''
        }
      } catch (error) {
        console.warn('加载产品参数失败', error)
      }
      
      // 加载持仓
      try {
        const holdingsResponse = await axios.get(
          getApiUrl(API_CONFIG.ENDPOINTS.GET_HOLDINGS(portfolioId.value))
        )
        if (holdingsResponse.data?.code === 200 && holdingsResponse.data?.data) {
          const holdings = holdingsResponse.data.data
          if (Array.isArray(holdings)) {
            step4Holdings.value = holdings.map((h: any) => ({
              fundCode: normalizeFundCodeRaw(h.fundCode || h.fund_code || ''),
              fundCodeDisplay: formatFundCodeDisplay(h.fundCode || h.fund_code || ''),
              fundName: h.fundName || h.fund_name || '',
              weight: convertWeightToPercent(h.weight),
              remark: h.remark || ''
            }))
          }
        }
      } catch (error) {
        console.warn('加载持仓失败', error)
      }
    } catch (error) {
      console.error('加载组合数据失败', error)
    }
  }
}

// 组件挂载
onMounted(() => {
  fetchStrategies()
  loadExistingData()
})
</script>

<style scoped>
.combination-create {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.steps-indicator {
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 8px;
}

.step-content-card {
  border-radius: 8px;
  margin-bottom: 24px;
}

.step-panel {
  padding: 24px;
}

.step-title {
  margin: 0 0 24px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.form-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.strategy-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.strategy-name {
  font-weight: 500;
  color: #303133;
}

.strategy-info {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.holdings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.weight-sum {
  font-size: 16px;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 4px;
}

.weight-valid {
  color: #67c23a;
  background: #f0f9ff;
}

.weight-warning {
  color: #e6a23c;
  background: #fdf6ec;
}

.weight-error {
  color: #f56c6c;
  background: #fef0f0;
}

.empty-holdings {
  margin-top: 24px;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 24px;
  background: white;
  border-radius: 8px;
}

.fund-option-text {
  display: inline-block;
  width: 100%;
  font-size: 14px;
  color: #303133;
}

@media (max-width: 768px) {
  .combination-create {
    padding: 16px;
  }

  .step-panel {
    padding: 16px;
  }

  .holdings-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
