<template>
  <div class="marketplace">
    <div class="page-header">
      <div>
        <h2 class="page-title">组合产品订购</h2>
        <p class="page-subtitle">展示已上架的组合产品，可查看详情并进行签约订购</p>
      </div>
      <el-input
        v-model="searchKeyword"
        class="search-input"
        placeholder="搜索组合名称/策略类型"
        clearable
        :prefix-icon="Search"
      />
    </div>

    <el-row :gutter="20" class="summary-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">上架组合数</div>
          <div class="summary-value">{{ listedCount }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">平均年化收益</div>
          <div class="summary-value">{{ formatPercent(avgAnnualReturn) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">平均最大回撤</div>
          <div class="summary-value">{{ formatPercent(avgMaxDrawdown) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">平均夏普比率</div>
          <div class="summary-value">{{ avgSharpeRatio.toFixed(2) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && filteredList.length === 0" description="暂无可订购的组合" />

    <el-row :gutter="20" class="cards-row" v-loading="loading">
      <el-col
        v-for="item in filteredList"
        :key="item.portfolioId"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
      >
        <el-card shadow="hover" class="combo-card" @click="openDetail(item)">
          <div class="card-header">
            <h3 class="combo-name">{{ item.portfolioName }}</h3>
            <el-tag type="success" round>可订购</el-tag>
          </div>
          <div class="card-section">
            <div class="section-title">基础信息</div>
            <ul class="info-list">
              <li>
                <span class="label">策略类型</span>
                <span class="value">{{ item.portfolioStrategyType || '—' }}</span>
              </li>
              <li>
                <span class="label">风险等级</span>
                <span class="value">{{ item.riskLevel || '—' }}</span>
              </li>
              <li>
                <span class="label">策略名称</span>
                <span class="value">{{ item.strategyName || '—' }}</span>
              </li>
            </ul>
          </div>
          <div class="card-section">
            <div class="section-title">收益指标</div>
            <ul class="info-list">
              <li>
                <span class="label">年化收益</span>
                <span class="value highlight">{{ formatPercent(item.annualReturn) }}</span>
              </li>
              <li>
                <span class="label">最大回撤</span>
                <span class="value">{{ formatPercent(item.maxDrawdown) }}</span>
              </li>
              <li>
                <span class="label">夏普比率</span>
                <span class="value">{{ (item.sharpeRatio ?? 0).toFixed(2) }}</span>
              </li>
            </ul>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-drawer
      v-model="detailVisible"
      :title="selectedCombo?.portfolioName || '组合详情'"
      size="40%"
      :with-header="true"
    >
      <template #default>
        <div v-if="selectedCombo" class="detail-content">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="组合ID">{{ selectedCombo.portfolioId }}</el-descriptions-item>
            <el-descriptions-item label="策略名称">{{ selectedCombo.strategyName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="策略类型">{{ selectedCombo.portfolioStrategyType || '—' }}</el-descriptions-item>
            <el-descriptions-item label="风险等级">{{ selectedCombo.riskLevel || '—' }}</el-descriptions-item>
            <el-descriptions-item label="策略描述" :span="2">{{ selectedCombo.description || '—' }}</el-descriptions-item>
            <el-descriptions-item label="创立时间">{{ formatDateTime(selectedCombo.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="资产规模">{{ formatScale(selectedCombo.scale) }} 亿元</el-descriptions-item>
          </el-descriptions>

          <el-divider />

          <h4 class="section-header">收益情况</h4>
          <el-row :gutter="16" class="metrics-row">
            <el-col :span="8">
              <div class="metric-card">
                <div class="metric-label">策略收益</div>
                <div class="metric-value highlight">{{ formatPercent(selectedCombo.returnRate) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-card">
                <div class="metric-label">年化收益</div>
                <div class="metric-value">{{ formatPercent(selectedCombo.annualReturn) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-card">
                <div class="metric-label">最大回撤</div>
                <div class="metric-value">{{ formatPercent(selectedCombo.maxDrawdown) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-card">
                <div class="metric-label">夏普比率</div>
                <div class="metric-value">{{ (selectedCombo.sharpeRatio ?? 0).toFixed(2) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-card">
                <div class="metric-label">波动率</div>
                <div class="metric-value">{{ formatPercent(selectedCombo.volatility) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-card">
                <div class="metric-label">胜率</div>
                <div class="metric-value">{{ formatPercent(selectedCombo.winRate) }}</div>
              </div>
            </el-col>
          </el-row>

          <el-divider />

          <h4 class="section-header">成份基金列表</h4>
          <el-table :data="holdings" border stripe v-loading="holdingsLoading" style="margin-bottom: 16px;">
            <el-table-column prop="fundCode" label="基金代码" width="140" />
            <el-table-column prop="fundName" label="基金名称" min-width="160" />
            <el-table-column prop="weight" label="权重(%)" width="120" align="right">
              <template #default="scope">{{ scope.row.weight.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" />
          </el-table>

          <el-empty
            v-if="!holdingsLoading && holdings.length === 0"
            description="暂无基金持仓数据"
            :image-size="80"
          />

          <div class="subscribe-panel">
            <div>
              <h4 class="section-header">签约订购</h4>
              <p class="subscribe-tip">签约前请仔细阅读组合详情并确认风险承受能力。</p>
            </div>
            <el-button type="success" size="large" @click="openSubscribeDialog">签约订购</el-button>
          </div>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="subscribeVisible" title="签约订购" width="500px">
      <el-form :model="subscribeForm" ref="subscribeFormRef" :rules="subscribeRules" label-width="100px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="subscribeForm.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="投资金额(万元)" prop="amount">
          <el-input-number
            v-model="subscribeForm.amount"
            :min="1"
            :precision="2"
            :step="1"
            class="full-width"
          />
        </el-form-item>
        <el-form-item label="签约日期" prop="signedAt">
          <el-date-picker
            v-model="subscribeForm.signedAt"
            type="date"
            placeholder="请选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="subscribeForm.remark" type="textarea" rows="3" placeholder="可填写附加要求" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="subscribeVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitSubscribe">确认签约</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getApiUrl, API_CONFIG } from '../../config/api'

interface CombinationItem {
  portfolioId: number
  portfolioName: string
  riskLevel: string
  portfolioStrategyType: string
  listed: number
  status?: string
  strategyId: number
  strategyName: string
  strategyType: string
  description: string
  strategyRefId: number
  createTime?: string
  scale?: number
  feeRate?: number
  returnRate?: number
  annualReturn?: number
  volatility?: number
  sharpeRatio?: number
  maxDrawdown?: number
  winRate?: number
  funds?: string
}

interface HoldingItem {
  fundCode: string
  fundName: string
  weight: number
  remark?: string
}

const loading = ref(false)
const tableData = ref<CombinationItem[]>([])
const searchKeyword = ref('')
const detailVisible = ref(false)
const selectedCombo = ref<CombinationItem | null>(null)
const holdings = ref<HoldingItem[]>([])
const holdingsLoading = ref(false)

const subscribeVisible = ref(false)
const subscribeFormRef = ref<FormInstance>()
const subscribeForm = reactive({
  customerName: '',
  amount: 10,
  signedAt: '',
  remark: ''
})
const submitting = ref(false)

const subscribeRules: FormRules<typeof subscribeForm> = {
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入签约金额', trigger: 'blur' }],
  signedAt: [{ required: true, message: '请选择签约日期', trigger: 'change' }]
}

const listedCombinations = computed(() =>
  tableData.value.filter(item => {
    const status = (item.status || '').toLowerCase()
    return item.listed === 1 || status === 'approved'
  })
)

const listedCount = computed(() => listedCombinations.value.length)

const avgAnnualReturn = computed(() => {
  if (listedCombinations.value.length === 0) return 0
  const sum = listedCombinations.value.reduce((acc, cur) => acc + (cur.annualReturn || 0), 0)
  return sum / listedCombinations.value.length
})

const avgMaxDrawdown = computed(() => {
  if (listedCombinations.value.length === 0) return 0
  const sum = listedCombinations.value.reduce((acc, cur) => acc + (cur.maxDrawdown || 0), 0)
  return sum / listedCombinations.value.length
})

const avgSharpeRatio = computed(() => {
  if (listedCombinations.value.length === 0) return 0
  const sum = listedCombinations.value.reduce((acc, cur) => acc + (cur.sharpeRatio || 0), 0)
  return sum / listedCombinations.value.length
})

const filteredList = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return listedCombinations.value
  }
  return listedCombinations.value.filter(item => {
    const name = item.portfolioName?.toLowerCase() || ''
    const strategyType = item.portfolioStrategyType?.toLowerCase() || ''
    return name.includes(keyword) || strategyType.includes(keyword)
  })
})

const fetchData = async () => {
  loading.value = true
  try {
    const response = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.STRATEGY_COMBINATION_ALL), {
      headers: { 'Content-Type': 'application/json' }
    })

    let dataArray: any[] = []
    if (response.data && response.data.code === 200 && response.data.data) {
      if (Array.isArray(response.data.data)) {
        dataArray = response.data.data
      }
    } else if (Array.isArray(response.data)) {
      dataArray = response.data
    }

    tableData.value = dataArray.map((item: any) => ({
      portfolioId: item.portfolioId ?? item.portfolio_id ?? item.id,
      portfolioName: item.portfolioName ?? item.portfolio_name ?? item.name ?? '',
      riskLevel: item.riskLevel ?? item.risk_level ?? '',
      portfolioStrategyType: item.portfolioStrategyType ?? item.portfolio_strategy_type ?? item.strategyType ?? item.strategy_type ?? '',
      listed: Number(item.listed ?? 0),
      status: item.status ?? item.state ?? '',
      strategyId: item.strategyId ?? item.strategy_id ?? 0,
      strategyName: item.strategyName ?? item.strategy_name ?? '',
      strategyType: item.strategyType ?? item.strategy_type ?? '',
      description: item.description ?? item.desc ?? '',
      strategyRefId: item.strategyRefId ?? item.strategy_ref_id ?? 0,
      createTime: item.created_at ?? item.createTime ?? item.create_time ?? '',
      scale: Number(item.scale ?? 0),
      feeRate: Number(item.feeRate ?? item.fee_rate ?? 0),
      returnRate: Number(item.returnRate ?? item.return_rate ?? 0),
      annualReturn: Number(item.annualReturn ?? item.annual_return ?? 0),
      volatility: Number(item.volatility ?? 0),
      sharpeRatio: Number(item.sharpeRatio ?? item.sharpe_ratio ?? 0),
      maxDrawdown: Number(item.maxDrawdown ?? item.max_drawdown ?? 0),
      winRate: Number(item.winRate ?? item.win_rate ?? 0),
      funds: item.funds ?? ''
    }))
  } catch (error) {
    console.error('获取组合产品失败', error)
    if (axios.isAxiosError(error)) {
      ElMessage.error(error.response?.data?.message || '获取组合产品失败，请稍后重试')
    } else {
      ElMessage.error('获取组合产品失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const openDetail = (item: CombinationItem) => {
  selectedCombo.value = item
  detailVisible.value = true
  loadHoldings(item.portfolioId)
}

const loadHoldings = async (portfolioId: number) => {
  holdings.value = []
  holdingsLoading.value = true
  try {
    const response = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.GET_HOLDINGS(portfolioId)))
    if (response.data && response.data.code === 200 && Array.isArray(response.data.data)) {
      holdings.value = response.data.data.map((item: any) => ({
        fundCode: item.fundCode ?? item.fund_code ?? '',
        fundName: item.fundName ?? item.fund_name ?? '',
        weight: Number(item.weight ?? 0) * (Number(item.weight ?? 0) <= 1 ? 100 : 1),
        remark: item.remark || ''
      }))
    }
  } catch (error) {
    console.warn('加载基金持仓失败', error)
  } finally {
    holdingsLoading.value = false
  }
}

const openSubscribeDialog = () => {
  if (!selectedCombo.value) return
  subscribeForm.customerName = ''
  subscribeForm.amount = 10
  subscribeForm.signedAt = ''
  subscribeForm.remark = ''
  subscribeVisible.value = true
}

const submitSubscribe = async () => {
  if (!subscribeFormRef.value) return
  try {
    await subscribeFormRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  setTimeout(() => {
    submitting.value = false
    subscribeVisible.value = false
    ElMessage.success('签约申请已提交，工作人员将尽快与您联系')
  }, 800)
}

const formatPercent = (value?: number) => {
  if (value === undefined || value === null || isNaN(value)) return '—'
  if (Math.abs(value) > 1) {
    return `${value.toFixed(2)}%`
  }
  return `${(value * 100).toFixed(2)}%`
}

const formatScale = (value?: number) => {
  if (!value || isNaN(value)) return '—'
  return (value / 10000).toFixed(2)
}

const formatDateTime = (value?: string) => {
  if (!value) return '—'
  try {
    const normalizedValue = value.replace(' ', 'T')
    const date = new Date(normalizedValue)
    if (isNaN(date.getTime())) {
      return value
    }
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}/${month}/${day}`
  } catch {
    return value
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.marketplace {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  gap: 16px;
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
}

.search-input {
  max-width: 260px;
}

.summary-row {
  margin-bottom: 20px;
}

.summary-card {
  border-radius: 12px;
  background: linear-gradient(135deg, #1f3c88, #3b5998);
  color: #fff;
}

.summary-label {
  font-size: 14px;
  opacity: 0.8;
}

.summary-value {
  margin-top: 8px;
  font-size: 26px;
  font-weight: 600;
}

.cards-row {
  margin-top: 8px;
}

.combo-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.combo-card:hover {
  transform: translateY(-4px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.combo-name {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
}

.card-section {
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 6px;
}

.info-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.info-list li {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-bottom: 4px;
}

.label {
  color: #909399;
}

.value {
  color: #303133;
}

.value.highlight {
  color: #1f3c88;
  font-weight: 600;
}

.detail-content {
  padding-right: 8px;
}

.section-header {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.metrics-row .metric-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.metric-label {
  color: #909399;
  margin-bottom: 6px;
}

.metric-value {
  font-size: 18px;
  color: #303133;
}

.metric-value.highlight {
  color: #1f3c88;
  font-weight: 600;
}

.subscribe-panel {
  margin-top: 24px;
  padding: 16px;
  background: #f0f5ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.subscribe-tip {
  margin: 4px 0 0;
  color: #606266;
  font-size: 13px;
}

.full-width {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
