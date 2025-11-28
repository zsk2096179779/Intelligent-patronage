<template>
  <div class="marketplace">
    <div class="page-header">
      <div>
        <h2 class="page-title">组合产品订购</h2>
        <p class="page-subtitle">
          <span v-if="canPurchase">展示已上架的组合产品，可查看详情并进行签约订购</span>
          <span v-else>展示已上架的组合产品，可查看详情（当前角色仅可查看，无法订购）</span>
        </p>
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
            <el-tag
              v-if="canPurchase"
              :type="getPortfolioPurchaseTagType(item.portfolioId)"
              round
            >
              {{ getPortfolioPurchaseText(item.portfolioId) }}
            </el-tag>
            <el-tag v-else type="info" round>仅查看</el-tag>
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

          <div v-if="canPurchase" class="subscribe-panel">
            <div>
              <h4 class="section-header">签约订购</h4>
              <p class="subscribe-tip">签约前请仔细阅读组合详情并确认风险承受能力。</p>
            </div>
            <el-button
              type="success"
              size="large"
              :loading="precheckLoading || isSelectedComboPurchaseChecking"
              :disabled="hasSubscribedSelectedCombo"
              @click="startSubscribeFlow"
            >
              {{ hasSubscribedSelectedCombo ? '已签约过' : '签约订购' }}
            </el-button>
          </div>
          <div v-else class="subscribe-panel view-only">
            <div>
              <h4 class="section-header">查看模式</h4>
              <p class="subscribe-tip">当前角色仅可查看组合产品信息，无法进行订购操作。</p>
            </div>
          </div>
        </div>
      </template>
    </el-drawer>

    <!-- 协议勾选弹窗 -->
    <el-dialog
      v-model="agreementDialogVisible"
      title="签约前必读协议"
      width="800px"
    >
      <el-table
        v-loading="agreementLoading"
        :data="agreementList"
        size="small"
        border
        @selection-change="onAgreementSelectionChange"
      >
        <el-table-column type="selection" width="50" />

        <el-table-column label="协议名称" min-width="260">
          <template #default="{ row }">
            <el-link type="primary" @click="viewAgreement(row)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="version" label="版本" width="80" />

        <el-table-column label="生效日期" width="120">
          <template #default="{ row }">
            {{ row.effectiveDate ? formatDateTime(row.effectiveDate) : '—' }}
          </template>
        </el-table-column>

        <el-table-column label="失效日期" width="120">
          <template #default="{ row }">
            {{ row.expireDate ? formatDateTime(row.expireDate) : '长期有效' }}
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="agreementDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="agreementSigning"
            @click="confirmAgreementsAndContinue"
          >
            我已阅读并同意以上协议
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 协议详情弹窗 -->
    <el-dialog
      v-model="agreementDetailVisible"
      title="协议详情"
      width="800px"
    >
      <div v-if="agreementDetail" class="agreement-content">
        <h3 class="agreement-title">{{ agreementDetail.title }}</h3>
        <p class="agreement-meta">
          协议编码：{{ agreementDetail.agreementCode || '—' }} ·
          版本：{{ agreementDetail.version || '—' }} ·
          生效日期：{{ agreementDetail.effectiveDate ? formatDateTime(agreementDetail.effectiveDate) : '—' }}
        </p>
        <div class="agreement-body" v-html="agreementDetail.content" />
      </div>
      <div v-else>
        <el-skeleton :rows="8" animated />
      </div>
    </el-dialog>

    <el-dialog
      v-model="subscribeVisible"
      :show-close="true"
      width="480px"
      class="purchase-dialog"
      :align-center="true"
    >
      <!-- 自定义头部 -->
      <template #header>
        <div class="purchase-header-bar">财富账户持仓</div>
      </template>

      <div class="purchase-content">
        <!-- 蓝色卡片头部 -->
        <div class="product-header-card">
          <div class="ph-row">
            <span class="ph-name">{{ selectedCombo?.portfolioName }}</span>
            <span class="ph-tag">{{ selectedCombo?.riskLevel || 'R3' }}</span>
          </div>
          <div class="ph-code">{{ selectedCombo?.portfolioId }}</div>
        </div>

           <!-- 资金账户：显示来自 investor_profile 的银行卡号（已脱敏） -->
          <div class="account-row">
          <div>资金账户</div>
          <div>{{ maskedAccount }}</div>
          </div>


        <!-- 输入区域 -->
        <div class="input-section">
          <div class="input-label">委托金额</div>
          <div class="input-wrapper">
            <span class="currency-symbol">¥</span>
            <el-input-number
              v-model="subscribeForm.amount"
              :min="1"
              :step="1"
              :precision="2"
              :controls="false"
              class="big-amount-input"
              placeholder="请输入划转金额"
            />
          </div>
          <div class="input-helper">1万元起购，每日转入上限10万元</div>
        </div>

        <!-- 风险提示 -->
        <div class="risk-warning-text">
          <el-icon><InfoFilled /></el-icon>
          风险提示：本人对转入该产品的风险已有足够了解，并自愿承担由此引起的投资风险和相关责任
        </div>

        <div class="watermark-text">仅用于展示</div>

        <!-- 协议勾选 -->
        <div class="agreement-checkbox-area">
          <el-checkbox v-model="isAgreementChecked">
            已仔细阅读
            <span v-if="currentAgreementName">并同意 <span class="link-text" @click.stop="viewCurrentAgreement">《{{ currentAgreementName }}》</span></span>
            <span v-else>并同意相关协议</span>
          </el-checkbox>
        </div>
      </div>

      <template #footer>
        <div class="purchase-footer">
          <el-button class="transfer-btn" type="danger" size="large" @click="handleFirstTransfer">
            首次转入
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ========================================================== -->
    <!-- 修改部分 2: 确认弹窗 (模仿图2) -->
    <!-- ========================================================== -->
    <el-dialog
      v-model="confirmDialogVisible"
      title="首次转入确认"
      width="320px"
      center
      class="confirm-dialog"
      :show-close="false"
    >
      <div class="confirm-list">
        <div class="confirm-item">
          <span class="c-label">产品名称：</span>
          <span class="c-value">{{ selectedCombo?.portfolioName }}</span>
        </div>
        <div class="confirm-item">
          <span class="c-label">产品代码：</span>
          <span class="c-value">{{ selectedCombo?.portfolioId }}</span>
        </div>
        <div class="confirm-item">
          <span class="c-label">资金账户：</span>
          <span class="c-value">{{ maskedAccount }}</span>
        </div>
        <div class="confirm-item">
          <span class="c-label">委托金额：</span>
          <span class="c-value font-money">¥ {{ formatMoney(subscribeForm.amount * 10000) }}</span>
        </div>
      </div>
      <p class="confirm-tip">仅提交转入申请，最终以成交为准</p>
      <template #footer>
        <div class="confirm-dialog-btns">
          <el-button @click="confirmDialogVisible = false" class="btn-cancel">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitSubscribeFinal" class="btn-confirm">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import axios from 'axios'
import {
  ElMessage,
  ElMessageBox,
} from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getApiUrl, API_CONFIG } from '../../config/api'
import { useAuthStore } from '@/stores/auth'
import { useRoute, useRouter } from 'vue-router'
import { useAiContextStore } from '@/stores/aiContext'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// ====== 类型定义 ======
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

interface AgreementItem {
  id: number
  agreementCode?: string
  agreementType?: string
  title: string
  version?: string
  isLatest?: number
  status?: number
  effectiveDate?: string
  expireDate?: string
  contentHash?: string
}

interface AgreementDetail extends AgreementItem {
  content: string
}

interface RiskMatchInfo {
  matched: boolean
  canPurchase: boolean
  needConfirm: boolean
  warningMessage?: string
  userRiskLevel?: string
  productRiskLevel?: string
  productRiskLevelNum?: number
}


// ====== 角色判断：当前用户能不能下单 ======
const canPurchase = computed(() => {
  return authStore.userInfo?.role === 'USER'
})

// ====== 组合列表相关 ======
const loading = ref(false)
const tableData = ref<CombinationItem[]>([])
const searchKeyword = ref('')
const detailVisible = ref(false)
const selectedCombo = ref<CombinationItem | null>(null)
const focusPortfolioId = ref<number | null>(null)

const holdings = ref<HoldingItem[]>([])
const holdingsLoading = ref(false)

// ====== 签约订单相关 ======
const precheckLoading = ref(false)
const currentOrderNo = ref<string | null>(null)
const currentRiskMatch = ref<RiskMatchInfo | null>(null)
const needRiskMismatchConfirm = ref(false)

// ====== 协议签署相关 ======
const agreementDialogVisible = ref(false)
const agreementDetailVisible = ref(false)
const selectedAgreementRows = ref<AgreementItem[]>([])

// 这里用小写场景码，和后端 DB 对齐：subscription / risk_mismatch
const agreementScenario = ref<'subscription' | 'risk_mismatch'>('subscription')

const agreementList = ref<AgreementItem[]>([])
const agreementDetail = ref<AgreementDetail | null>(null)
const agreementLoading = ref(false)
const agreementSigning = ref(false)
const selectedAgreementIds = ref<number[]>([])

// ---------------- 新增/修改的变量 ----------------
const subscribeVisible = ref(false) // 购买主界面
const confirmDialogVisible = ref(false) // 确认小弹窗
const isAgreementChecked = ref(false) // 勾选框
const currentAgreementName = ref('') // 显示在勾选框旁的协议名
const submitting = ref(false)

const investorProfile = ref<any>(null)
const investorLoading = ref(false)

// 表单数据
const subscribeForm = reactive({
  customerName: '',
  amount: 10, // 单位：万元
  signedAt: '',
  remark: ''
})

// ====== 组合签约校验：命中后端单个组合接口 ======
type PurchaseStatus = { loading: boolean; purchased?: boolean }
const purchaseStatusMap = reactive<Record<number, PurchaseStatus>>({})

const ensurePurchaseStatus = async (portfolioId: number) => {
  if (!portfolioId || !canPurchase.value) return
  const current = purchaseStatusMap[portfolioId]
  if (current && (current.loading || current.purchased !== undefined)) {
    return
  }

  purchaseStatusMap[portfolioId] = { ...(current || {}), loading: true }
  try {
    const res = await axios.get(
      getApiUrl(API_CONFIG.ENDPOINTS.SUBSCRIPTION_CHECK_PURCHASED(portfolioId))
    )
    if (res.data?.code === 200) {
      purchaseStatusMap[portfolioId] = {
        loading: false,
        purchased: !!res.data?.data?.purchased
      }
    } else {
      purchaseStatusMap[portfolioId] = { loading: false, purchased: false }
      console.warn('检查组合签约状态失败', res.data?.message)
    }
  } catch (error) {
    purchaseStatusMap[portfolioId] = { loading: false, purchased: false }
    console.warn('检查组合签约状态异常', error)
  }
}

const hasSubscribedSelectedCombo = computed(() => {
  if (!selectedCombo.value) return false
  const id = Number(selectedCombo.value.portfolioId)
  if (!id) return false
  return !!purchaseStatusMap[id]?.purchased
})

const isSelectedComboPurchaseChecking = computed(() => {
  if (!selectedCombo.value) return false
  const id = Number(selectedCombo.value.portfolioId)
  if (!id) return false
  return !!purchaseStatusMap[id]?.loading
})

const getPortfolioPurchaseText = (portfolioId: number) => {
  const status = purchaseStatusMap[portfolioId]
  if (!status) return '查询中'
  if (status.loading) return '查询中'
  return status.purchased ? '已签约' : '可订购'
}

const getPortfolioPurchaseTagType = (portfolioId: number) => {
  const status = purchaseStatusMap[portfolioId]
  if (!status || status.loading) return 'info'
  return status.purchased ? 'warning' : 'success'
}

// ====== 错误码处理工具函数 ======
const ERROR_CODE_MAP: Record<number, string> = {
  // 通用错误
  200: '操作成功',
  1001: '参数错误，请检查请求参数',
  1002: '未授权，请登录',
  1003: '当前角色无权限执行此操作',
  1004: '资源不存在',

  // OTC相关
  2001: 'OTC账户未开通，请先开通OTC账户',
  2002: 'OTC账户已开通',
  2003: 'OTC账户开通失败，请联系客服',

  // 风险测评相关
  3001: '风险测评未完成，请先完成风险测评',
  3002: '风险测评已过期，请重新进行测评',
  3003: '风险等级不匹配，请确认是否继续',
  3004: '风险不匹配需要确认后才能提交',

  // 协议相关
  4001: '协议未签署，请先签署相关协议',
  4002: '协议签署失败，请重试或联系客服',

  // 订单相关
  5001: '订单不存在',
  5002: '订单状态错误，不允许当前操作',
  5003: '订单已提交，请勿重复提交',
  5004: '缺少签名，请先签名',
  5005: '支付失败，请检查支付信息',

  // 业务规则相关
  6001: '金额低于最低限额',
  6002: '金额超过最高限额',
  6003: '产品未上架，暂不可购买'
}

const handleApiError = (error: any, defaultMsg = '操作失败，请重试') => {
  const errorCode = error?.response?.data?.code
  let errorMsg = ERROR_CODE_MAP[errorCode] || defaultMsg

  // 如果后端返回了具体错误信息，优先使用
  if (error?.response?.data?.message) {
    errorMsg = error.response.data.message
  }

  ElMessage.error(errorMsg)
  console.error(`[API Error ${errorCode}]:`, error)

  try {
    const aiStore = useAiContextStore()
    aiStore.setLastError(errorCode || 0, errorMsg)
  } catch (e) {
    // 忽略 store 未初始化的边缘情况
  }
}

const fetchInvestorProfile = async () => {
  investorLoading.value = true
  try {
    const res = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.INVESTOR_PROFILE))
    // 兼容后端返回结构：{ code:200, data: {...} } 或 直接返回对象
    if (res.data && res.data.code === 200 && res.data.data) {
      investorProfile.value = res.data.data
    } else if (res.data && res.data.data === undefined && res.data) {
      investorProfile.value = res.data
    } else if (res.data && Array.isArray(res.data)) {
      investorProfile.value = res.data[0] ?? null
    } else {
      investorProfile.value = null
    }
  } catch (e) {
    handleApiError(e, '加载投资者档案失败')
    investorProfile.value = null
  } finally {
    investorLoading.value = false
  }
}

const maskedAccount = computed(() => {
  const candidate =
    investorProfile.value?.bankCardNumber ||
    investorProfile.value?.otcAccountNo ||
    ''

  const s = String(candidate || '')
  if (!s) return '—'
  const len = s.length
  if (len <= 8) {
    return s.substring(0, 3) + '*****' + s.substring(Math.max(0, len - 3))
  }

  const first = s.substring(0, 6)
  const last = s.substring(len - 4)
  return `${first} **** **** ${last}`
})

// ====== 一些派生计算 ======
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

// ====== 拉组合列表 ======
const fetchData = async () => {
  loading.value = true
  try {
    const response = await axios.get(
      getApiUrl(API_CONFIG.ENDPOINTS.STRATEGY_COMBINATION_ALL),
      { headers: { 'Content-Type': 'application/json' } }
    )

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
      portfolioStrategyType:
        item.portfolioStrategyType ??
        item.portfolio_strategy_type ??
        item.strategyType ??
        item.strategy_type ??
        '',
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
    if (canPurchase.value) {
      tableData.value.forEach(item => {
        if (item.portfolioId) {
          ensurePurchaseStatus(Number(item.portfolioId))
        }
      })
    }

    syncFocusFromRoute()
  } catch (error) {
    handleApiError(error, '加载组合列表失败')
  } finally {
    loading.value = false
  }
}

const syncFocusFromRoute = () => {
  const pid = Number(route.query.portfolioId)
  if (!pid) {
    focusPortfolioId.value = null
    return
  }
  focusPortfolioId.value = pid
  const combo = tableData.value.find(item => Number(item.portfolioId) === pid)
  if (combo) {
    openDetail(combo)
  }
}

watch(
  () => route.query.portfolioId,
  () => {
    syncFocusFromRoute()
  }
)

// ====== 查看组合详情 & 成份基金 ======
const openDetail = async (item: CombinationItem) => {
  selectedCombo.value = item
  detailVisible.value = true
  loadHoldings(item.portfolioId)
  ensurePurchaseStatus(item.portfolioId)
  // 获取最新的组合详情（包含完整收益指标）
  await loadPortfolioDetail(item.portfolioId)
}

// 加载组合详情（获取最新收益指标）
const loadPortfolioDetail = async (portfolioId: number) => {
  try {
    const response = await axios.get(
      getApiUrl(API_CONFIG.ENDPOINTS.GET_PORTFOLIO_DETAIL(portfolioId)),
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    let data: any = null

    // 处理不同的响应格式
    if (response.data && response.data.code === 200 && response.data.data) {
      data = response.data.data
    } else if (response.data && !response.data.code) {
      data = response.data
    }

    if (data && selectedCombo.value) {
      // 更新选中组合的完整数据（包括收益指标和资产规模等）
      selectedCombo.value.returnRate = data.returnRate ?? data.return_rate ?? selectedCombo.value.returnRate
      selectedCombo.value.annualReturn = data.annualReturn ?? data.annual_return ?? selectedCombo.value.annualReturn
      selectedCombo.value.maxDrawdown = data.maxDrawdown ?? data.max_drawdown ?? selectedCombo.value.maxDrawdown
      selectedCombo.value.sharpeRatio = data.sharpeRatio ?? data.sharpe_ratio ?? selectedCombo.value.sharpeRatio
      selectedCombo.value.volatility = data.volatility ?? selectedCombo.value.volatility
      selectedCombo.value.winRate = data.winRate ?? data.win_rate ?? selectedCombo.value.winRate
      // 更新资产规模
      if (data.scale !== undefined && data.scale !== null) {
        selectedCombo.value.scale = data.scale
      }
      // 更新其他基本信息
      if (data.description !== undefined) {
        selectedCombo.value.description = data.description ?? data.desc ?? selectedCombo.value.description
      }
    }
  } catch (error) {
    // 静默失败，不影响详情显示
    console.warn('获取组合详情失败，使用列表数据', error)
  }
}

const loadHoldings = async (portfolioId: number) => {
  holdings.value = []
  holdingsLoading.value = true
  try {
    const response = await axios.get(
      getApiUrl(API_CONFIG.ENDPOINTS.GET_HOLDINGS(portfolioId))
    )
    if (response.data && response.data.code === 200 && Array.isArray(response.data.data)) {
      holdings.value = response.data.data.map((item: any) => ({
        fundCode: item.fundCode ?? item.fund_code ?? '',
        fundName: item.fundName ?? item.fund_name ?? '',
        weight: Number(item.weight ?? 0) * (Number(item.weight ?? 0) <= 1 ? 100 : 1),
        remark: item.remark || ''
      }))
    }
  } catch (error) {
    handleApiError(error, '加载基金持仓失败')
  } finally {
    holdingsLoading.value = false
  }
}


//  点击“签约订购”按钮入口
const startSubscribeFlow = async () => {
  if (!selectedCombo.value) return
  if (hasSubscribedSelectedCombo.value) {
    ElMessage.info('您已签约过该组合，无需重复签约')
    return
  }

  precheckLoading.value = true
  try {
    const combo = selectedCombo.value
    // 1）OTC 开通检查
    const otcRes = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.OTC_STATUS))
    const otcRaw = otcRes.data
    const otc = otcRaw.data ?? otcRaw
    if (!otc || !otc.opened) {
      // 错误码 2001
      ElMessage.warning('您尚未开通场外账户，请先完成 OTC 开通')
      await router.push('/otc/open')
      return
    }

    // 2）风险测评状态检查
    const riskRes = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.RISK_STATUS))
    const riskRaw = riskRes.data
    const risk = riskRaw.data ?? riskRaw
    if (!risk || !risk.hasAssessment || risk.expired) {
      // 错误码 3001
      ElMessage.warning('您尚未完成有效的风险测评，请先进行风险测评')
      await router.push('/risk/assessment')
      return
    }

    // 3）风险匹配检查
    const matchRes = await axios.post(
      // 错误码 3002
      getApiUrl(API_CONFIG.ENDPOINTS.RISK_MATCH_CHECK),
      { portfolioId: combo.portfolioId }
    )
    const matchRaw = matchRes.data
    const match: RiskMatchInfo = matchRaw.data ?? matchRaw

    currentRiskMatch.value = match
// 每次重置标记
    needRiskMismatchConfirm.value = false

// 不匹配且不能确认 → 直接拦截
    if (!match || (!match.matched && !match.needConfirm)) {
      // 错误码 3003
      const msg =
        match?.warningMessage ||
        '您的风险等级与该产品不匹配，当前不支持购买。'
      ElMessage.warning(msg)
      return
    }

// 风险不匹配但允许确认 → 标记 + 提示一次
    if (!match.matched && match.needConfirm) {
      needRiskMismatchConfirm.value = true

      await ElMessageBox.confirm(
        match.warningMessage ||
        '您的风险等级与该产品不匹配，如坚持购买需签署不匹配确认协议。',
        '风险提示',
        {
          confirmButtonText: '我已知晓，继续',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).catch(() => {
        // 用户点了取消 → 直接终止整个签约流程
        throw new Error('user-cancel')
      })
    }

    // 4） 创建订单 (获取 orderNo)
    const draftRes = await axios.post(
      getApiUrl(API_CONFIG.ENDPOINTS.SUBSCRIPTION_CREATE),
      {
        portfolioId: combo.portfolioId,
        subscriptionAmount: subscribeForm.amount,
        dividendMode: 'reinvest',
        autoInvestEnabled: false
      }
    )

    if (draftRes.data?.code === 200) {
      currentOrderNo.value = draftRes.data.data.orderNo

      // 5）根据风险匹配情况，先弹哪一类协议
      if (!match.matched && match.needConfirm) {
        // 先签“不匹配确认”类协议
        await openAgreementDialog('risk_mismatch')
      } else {
        // 直接签订购相关协议
        await openAgreementDialog('subscription')
      }

    } else {
      handleApiError({ response: { data: draftRes.data } }, '创建订单失败')
    }
  } catch (e: any) {
    if (e && e.message === 'user-cancel') {
      // 用户手动取消，不提示报错
      return
    }
    console.error('签约前置检查或创建订单失败', e)
    handleApiError(e, '签约前置检查失败，请稍后重试')
  } finally {
    precheckLoading.value = false
  }
}

// 打开购买界面
const openSubscribeDialog = () => {
  subscribeForm.amount = 10 // 重置默认金额
  isAgreementChecked.value = false
  subscribeVisible.value = true
}

// 点击“首次转入”
const handleFirstTransfer = () => {
  if (!isAgreementChecked.value) {
    ElMessage.warning('请先阅读并勾选相关协议')
    return
  }
  if (!subscribeForm.amount || subscribeForm.amount <= 0) {
    ElMessage.warning('请输入有效的转入金额')
    return
  }
  // 打开确认弹窗
  confirmDialogVisible.value = true
}

// 点击“确认” (最终提交)
const submitSubscribeFinal = async () => {
  if (!currentOrderNo.value) return
  submitting.value = true

  try {
    // 1) 如果有协议，批量签署 (隐式签署)
    if (agreementList.value.length > 0) {
      const signRes = await axios.post(getApiUrl(API_CONFIG.ENDPOINTS.AGREEMENT_SIGN_BATCH), {
        scenario: 'subscription',
        relatedOrderNo: currentOrderNo.value,
        agreements: agreementList.value.map(row => ({
          agreementId: row.id,
          agreementCode: row.agreementCode || '',
          agreementVersion: row.version || '',
          contentHash: row.contentHash || '',
          signatureData: 'SIGNED_BY_CLICK'
        }))
      })
      if (signRes.data?.code !== 200) {
        // 错误码 4002
        handleApiError({ response: { data: signRes.data } }, '协议签署失败')
        return
      }
    }

    // 2) 更新订单金额
    const updateRes = await axios.put(getApiUrl(API_CONFIG.ENDPOINTS.SUBSCRIPTION_UPDATE(currentOrderNo.value)), {
      subscriptionAmount: subscribeForm.amount,
      customerRemark: '用户通过UI界面确认购买',
      signedAt: new Date().toISOString().split('T')[0]
    })
    if (updateRes.data?.code !== 200) {
    // 错误码 5002
    handleApiError({ response: { data: updateRes.data } }, '订单更新失败')
    return
    }

    // 3) 提交订单
    const submitRes = await axios.post(getApiUrl(API_CONFIG.ENDPOINTS.SUBSCRIPTION_SUBMIT(currentOrderNo.value)), {})

    if (submitRes.data && submitRes.data.code === 200) {
      ElMessage.success('申购申请提交成功')
      confirmDialogVisible.value = false
      subscribeVisible.value = false // 关闭主弹窗
      if (selectedCombo.value) {
        const id = Number(selectedCombo.value.portfolioId)
        if (id) {
          purchaseStatusMap[id] = { loading: false, purchased: true }
        }
      }
    } else {
      handleApiError({ response: { data: submitRes.data } }, '订单提交失败')
    }
  } catch (e) {
    console.error(e)
    handleApiError(e, '提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 查看协议
const viewCurrentAgreement = async () => {
  if(agreementList.value.length > 0) {
    // 查看第一个协议作为示例
    const row = agreementList.value[0]
    agreementDetailVisible.value = true
    try {
      const res = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.AGREEMENT_DETAIL(row.id)))
        if (res.data?.code !== 200) {
        handleApiError({ response: { data: res.data } }, '加载协议详情失败')
        agreementDetailVisible.value = false
          return
        }
      agreementDetail.value = res.data.data
    } catch(e) {
      handleApiError(e, '加载协议详情失败')
      agreementDetailVisible.value = false
    }
  }
}
// ====== 打开协议列表弹窗并加载数据 ======
const openAgreementDialog = async (
  scenario: 'subscription' | 'risk_mismatch' = 'subscription'
) => {
  if (!currentOrderNo.value) {
    ElMessage.error('订单不存在，请重新发起签约')
    return
  }

  agreementScenario.value = scenario
  agreementDialogVisible.value = true
  agreementLoading.value = true
  selectedAgreementIds.value = []
  agreementList.value = []

  try {
    const res = await axios.get(
      getApiUrl(API_CONFIG.ENDPOINTS.AGREEMENT_LIST),
      { params: { scenario } }
    )

    if (res.data && res.data.code === 200 && Array.isArray(res.data.data)) {
      agreementList.value = res.data.data
      if (!res.data.data.length) {
        // 当前场景未配置协议
        if (scenario === 'risk_mismatch') {
          // 直接告诉后端：用户已确认风险不匹配，然后继续走订购协议
          await confirmRiskMismatchOnBackend()
          agreementDialogVisible.value = false
          await openAgreementDialog('subscription')
        } else {
          ElMessage.info('当前场景未配置需签署的协议，将直接进入签约流程')
          agreementDialogVisible.value = false
          await openSubscribeDialog()
        }
      }
    } else {
      agreementList.value = []
      ElMessage.warning(res.data?.message || '未获取到需签署的协议')
    }
  } catch (e) {
    console.error('加载协议列表失败', e)
    agreementList.value = []
    handleApiError(e, '加载协议列表失败')
  } finally {
    agreementLoading.value = false
  }
}

// table 勾选变化
const onAgreementSelectionChange = (rows: AgreementItem[]) => {
  selectedAgreementRows.value = rows
  selectedAgreementIds.value = rows.map(r => r.id)
}

// 查看协议详情
const viewAgreement = async (row: AgreementItem) => {
  agreementDetailVisible.value = true
  agreementDetail.value = null
  try {
    const res = await axios.get(
      getApiUrl(API_CONFIG.ENDPOINTS.AGREEMENT_DETAIL(row.id))
    )
    if (res.data && res.data.code === 200 && res.data.data) {
      agreementDetail.value = res.data.data
    } else {
      agreementDetail.value = {
        ...row,
        content: res.data?.message || '暂无协议正文'
      }
    }
  } catch (e) {
    console.error('加载协议详情失败', e)
    handleApiError(e, '加载协议详情失败')
    agreementDetailVisible.value = false
  }
}

// 调用后端确认“风险不匹配”这个标志
const confirmRiskMismatchOnBackend = async () => {
  if (!currentOrderNo.value) return
  try {
    const res = await axios.post(
      getApiUrl(
        API_CONFIG.ENDPOINTS.SUBSCRIPTION_CONFIRM_RISK_MISMATCH(currentOrderNo.value)
      ),
      { confirmed: true,
        confirmReason: '用户已在前端确认风险不匹配'}
    )
    if (res.data?.code !== 200) {
    // 错误码 3004
    handleApiError({ response: { data: res.data } }, '确认风险不匹配失败')
    throw new Error('risk-mismatch-confirm-failed')
    }
  } catch (e) {
    console.error('确认风险不匹配失败', e)
    handleApiError(e, '确认风险不匹配失败')
    throw e
  }
}

// 批量签署协议，然后决定下一步
const confirmAgreementsAndContinue = async () => {
  if (!selectedAgreementIds.value.length) {
    ElMessage.warning('请勾选需要签署的协议')
    return
  }
  if (!selectedCombo.value) return
  if (!currentOrderNo.value) {
    ElMessage.error('订单尚未创建，请先填写并保存签约信息')
    return
  }

  agreementSigning.value = true
  try {
    // 1) 先批量签署当前场景的协议
    const signRes = await axios.post(
      getApiUrl(API_CONFIG.ENDPOINTS.AGREEMENT_SIGN_BATCH),
      {
        scenario: agreementScenario.value,          // 'subscription' | 'risk_mismatch'
        relatedOrderNo: currentOrderNo.value,       // ❗不要用 portfolioId，这里约定用订单号
        agreements: selectedAgreementRows.value.map(row => ({
          agreementId: row.id,
          agreementCode: row.agreementCode ?? '',
          agreementVersion: row.version ?? '',
          contentHash: row.contentHash ?? '',
          signatureData: 'SIGNED_BY_CLICK'         // 先用“点击代签”，以后换成画板签名再改
        }))
      }
    )
    if (signRes.data?.code !== 200) {
    // 错误码 4002
    handleApiError({ response: { data: signRes.data } }, '协议签署失败')
    return
    }

        // 2）当前场景：订购协议
    if (agreementScenario.value === 'subscription') {
      // 签完订购协议后，打开"首次转入"对话框让用户填写金额
      ElMessage.success('订购相关协议签署成功，请填写转入金额')
      agreementDialogVisible.value = false
      subscribeVisible.value = true // 打开金额填写对话框
      return
    }

    // 3）当前场景：risk_mismatch，不匹配确认协议签完 → 告诉后端已确认风险不匹配
    if (agreementScenario.value === 'risk_mismatch') {
      const confirmRes = await axios.post(
        getApiUrl(
          API_CONFIG.ENDPOINTS.SUBSCRIPTION_CONFIRM_RISK_MISMATCH(
            currentOrderNo.value
          )
        ),
        {
          confirmed: true,
          confirmReason: '用户已签署风险不匹配确认书'
        }
      )

      if (confirmRes.data?.code !== 200) {
        // 错误码 3004
       handleApiError({ response: { data: confirmRes.data } }, '风险不匹配确认失败')
        return
      }

      ElMessage.success('风险不匹配确认已完成，请继续填写签约信息')
      agreementDialogVisible.value = false
      await openAgreementDialog('subscription')
      return
    }
  } catch (e) {
      console.error('协议签署/确认失败', e)
    handleApiError(e, '协议签署失败')
    } finally {
      agreementSigning.value = false
    }
}

// 工具函数
const formatMoney = (val: number) => {
  if (!val) return '0.00'
  return val.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
const formatPercent = (value?: number) => {
  if (value === undefined || value === null || isNaN(value)) return '—'
  return `${(Math.abs(value) > 1 ? value : value * 100).toFixed(2)}%`
}
const formatDateTime = (value?: string) => { /* ... */ return value || '-' }
const formatScale = (value?: number) => {
  if (value === undefined || value === null || isNaN(value)) return '—'
  // 如果值大于等于10000，假设单位是元，转换为亿元（除以1亿）
  if (value >= 10000) {
    return (Number(value) / 100000000).toFixed(2)
  }
  // 如果值较小，可能已经是亿元单位，直接显示
  return Number(value).toFixed(2)
}

onMounted(() => {
  fetchData()
  fetchInvestorProfile()
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

.subscribe-panel.view-only {
  background: #f5f7fa;
  justify-content: flex-start;
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

.agreement-content {
  max-height: 480px;
  overflow-y: auto;
}
.agreement-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
}
.agreement-meta {
  margin: 0 0 16px;
  font-size: 12px;
  color: #909399;
}
.agreement-body {
  font-size: 14px;
  line-height: 1.6;
}

:deep(.purchase-dialog .el-dialog__header) {
  padding: 16px 0;
  text-align: center;
  border-bottom: 1px solid #eee;
  margin-right: 0;
}
:deep(.purchase-dialog .el-dialog__body) {
  padding: 0; /* 全宽 */
}
:deep(.purchase-dialog .el-dialog__footer) {
  padding: 0;
}

.purchase-header-bar {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.purchase-content {
  padding: 20px;
  background: #fff;
}

/* 蓝色产品卡片头部 */
.product-header-card {
  background: #4a90e2; /* 蓝色背景 */
  /* 或者使用渐变以更接近图片: linear-gradient(180deg, #5b86e5 0%, #36d1dc 100%); */
  padding: 16px;
  border-radius: 8px 8px 0 0; /* 上圆角 */
  color: #fff;
  margin-bottom: 0;
}

.ph-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.ph-name {
  font-size: 18px;
  font-weight: 600;
}
.ph-tag {
  background: rgba(255,255,255,0.2);
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}
.ph-code {
  font-size: 14px;
  opacity: 0.9;
}

/* 账户行 */
.account-row {
  display: flex;
  justify-content: space-between;
  padding: 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
  font-size: 15px;
  color: #666;
}

/* 输入区域 */
.input-section {
  padding: 20px 10px 10px 10px;
}
.input-label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}
.input-wrapper {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 8px;
}
.currency-symbol {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-right: 10px;
}
/* 覆盖 Element Plus 输入框样式，使其看起来像纯文本输入 */
:deep(.big-amount-input .el-input__wrapper) {
  box-shadow: none !important;
  padding: 0;
}
:deep(.big-amount-input .el-input__inner) {
  font-size: 28px;
  font-weight: 400;
  color: #333;
  height: 40px;
  line-height: 40px;
  text-align: left;
}
.input-helper {
  margin-top: 8px;
  font-size: 13px;
  color: #999;
}

/* 风险提示 */
.risk-warning-text {
  margin: 20px 10px;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  display: flex;
  gap: 6px;
}

.watermark-text {
  position: absolute;
  right: 0;
  bottom: 100px;
  font-size: 60px;
  color: rgba(0,0,0,0.03);
  pointer-events: none;
  font-family: cursive;
  transform: rotate(-15deg);
}

/* 协议勾选 */
.agreement-checkbox-area {
  padding: 0 10px 20px 10px;
}
.link-text {
  color: #409eff;
  cursor: pointer;
}

/* 底部按钮 */
.purchase-footer {
  width: 100%;
}
.transfer-btn {
  width: 100%;
  height: 50px;
  font-size: 18px;
  border-radius: 0; /* 直角更像APP底部栏 */
  background-color: #e74c3c;
  border-color: #e74c3c;
}
.transfer-btn:hover {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

/* ======================================================
   Custom Styles for the Confirmation Dialog (Image 2)
   ====================================================== */
.confirm-list {
  padding: 10px 0;
}
.confirm-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 15px;
}
.c-label {
  color: #666;
}
.c-value {
  color: #333;
  font-weight: 500;
  text-align: right;
}
.font-money {
  font-family: sans-serif;
}
.confirm-tip {
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-top: 16px;
}
.confirm-dialog-btns {
  display: flex;
  width: 100%;
  border-top: 1px solid #eee;
}
.confirm-dialog-btns .el-button {
  flex: 1;
  margin: 0;
  border: none;
  height: 48px;
  border-radius: 0;
  background: transparent;
  font-size: 16px;
}
.btn-cancel {
  color: #606266;
  border-right: 1px solid #eee !important;
}
.btn-confirm {
  color: #409eff;
}
:deep(.confirm-dialog .el-dialog__body) {
  padding: 20px 25px 10px 25px;
}
:deep(.confirm-dialog .el-dialog__footer) {
  padding: 0;
}
</style>
