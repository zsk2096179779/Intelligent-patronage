<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">已购组合产品</h2>
        <p class="page-subtitle">仅展示当前登录用户已成功签约的组合产品</p>
      </div>
    </div>

    <el-row :gutter="20" class="summary-row">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">已购组合数</div>
          <div class="summary-value">{{ portfolios.length }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">累计认购金额</div>
          <div class="summary-value">{{ formatAmount(totalSubscribedAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">最近认购日期</div>
          <div class="summary-value small">
            {{ latestSubscribedAt ? formatDateTime(latestSubscribedAt) : '—' }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card">
      <el-table :data="portfolios" border stripe v-loading="loading" height="520px">
        <el-table-column prop="portfolioName" label="组合名称" min-width="200" />
        <el-table-column prop="riskLevel" label="风险等级" width="120">
          <template #default="{ row }">
            <el-tag size="small" round>{{ row.riskLevel || '—' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subscriptionAmount" label="认购金额(万元)" width="180" align="right">
          <template #default="{ row }">
            {{ formatAmount(row.subscriptionAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="subscribedAt" label="认购日期" width="200">
          <template #default="{ row }">
            {{ formatDateTime(row.subscribedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetailDialog(row)">
              查看详情
            </el-button>
            <el-button type="primary" link @click="openAppendDialog(row)">
              追加购入
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-empty
      v-if="!loading && portfolios.length === 0"
      description="暂无已购组合"
      style="margin-top: 40px"
    />

    <!-- 组合详情对话框 -->
    <PortfolioDetailDialog
      :visible="detailDialogVisible"
      :portfolio-id="currentPortfolioId"
      @update:visible="detailDialogVisible = $event"
    />

    <el-dialog
      v-model="appendDialogVisible"
      title="追加购入"
      width="420px"
      :close-on-click-modal="false"
    >
      <div class="append-summary">
        <div>
          <div class="append-label">组合名称</div>
          <div class="append-value">{{ appendForm.portfolioName }}</div>
        </div>
        <div>
          <div class="append-label">风险等级</div>
          <el-tag size="small">{{ appendForm.riskLevel || '—' }}</el-tag>
        </div>
      </div>

      <el-form
        ref="appendFormRef"
        :model="appendForm"
        :rules="appendRules"
        label-width="110px"
        status-icon
      >
        <el-form-item label="认购金额(万元)" prop="amount">
          <el-input-number
            v-model="appendForm.amount"
            :min="1"
            :precision="2"
            :step="1"
            class="full-width-input"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="appendDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="appendSubmitting" @click="submitAppendPurchase">
          确认追加
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getApiUrl, API_CONFIG } from '@/config/api'
import { useAuthStore } from '@/stores/auth'
import PortfolioDetailDialog from '@/components/model3/PortfolioDetailDialog.vue'

interface PurchasedPortfolio {
  portfolioId: number
  portfolioName: string
  riskLevel?: string
  subscriptionAmount: number
  subscribedAt?: string
}

const authStore = useAuthStore()
const isUser = computed(() => authStore.userInfo?.role === 'USER')

const loading = ref(false)
const portfolios = ref<PurchasedPortfolio[]>([])

const detailDialogVisible = ref(false)
const currentPortfolioId = ref<number | null>(null)
const appendDialogVisible = ref(false)
const appendSubmitting = ref(false)
const appendFormRef = ref()
const appendForm = reactive({
  portfolioId: 0,
  portfolioName: '',
  riskLevel: '',
  amount: 10,
  paymentMethod: 'bank_transfer',
  dividendMode: 'reinvest',
  autoInvestEnabled: false,
  autoInvestPeriod: '',
  autoInvestAmount: null as number | null,
  customerRemark: ''
})

const appendRules = {
  amount: [
    { required: true, message: '请输入追加金额', trigger: 'blur' },
    {
      validator: (_: any, value: number, callback: (err?: Error) => void) => {
        if (!value || value <= 0) {
          callback(new Error('金额需大于0'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const totalSubscribedAmount = computed(() =>
  portfolios.value.reduce((sum, item) => sum + (item.subscriptionAmount || 0), 0)
)

const latestSubscribedAt = computed(() => {
  const timestamps = portfolios.value
    .map(item => (item.subscribedAt ? new Date(item.subscribedAt).getTime() : 0))
    .filter(ts => ts > 0)
  if (!timestamps.length) return null
  return new Date(Math.max(...timestamps)).toISOString()
})

const loadPurchasedPortfolios = async () => {
  if (!isUser.value) {
    portfolios.value = []
    return
  }

  loading.value = true
  try {
    const res = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.SUBSCRIPTION_PURCHASED_PORTFOLIOS))
    if (res.data?.code === 200 && Array.isArray(res.data?.data)) {
      portfolios.value = res.data.data.map((item: any) => ({
        portfolioId: Number(item.portfolioId ?? item.portfolio_id ?? 0),
        portfolioName: item.portfolioName ?? item.portfolio_name ?? '—',
        riskLevel: item.riskLevel ?? item.risk_level ?? '—',
        subscriptionAmount: Number(item.subscriptionAmount ?? item.amount ?? 0),
        subscribedAt: item.subscribedAt ?? item.createdAt ?? item.portfolioCreateTime ?? ''
      }))
    } else {
      portfolios.value = []
      ElMessage.warning(res.data?.message || '未查询到已购组合')
    }
  } catch (error) {
    portfolios.value = []
    ElMessage.error('加载已购组合失败，请稍后重试')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const formatAmount = (amount: number) =>
  amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

const formatDateTime = (value?: string) => {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${hh}:${mm}`
}

onMounted(() => {
  loadPurchasedPortfolios()
})

const openDetailDialog = (row: PurchasedPortfolio) => {
  currentPortfolioId.value = row.portfolioId
  detailDialogVisible.value = true
}

const openAppendDialog = (row: PurchasedPortfolio) => {
  appendForm.portfolioId = row.portfolioId
  appendForm.portfolioName = row.portfolioName
  appendForm.riskLevel = row.riskLevel || ''
  appendForm.amount = row.subscriptionAmount || 10
  appendDialogVisible.value = true
}

const submitAppendPurchase = () => {
  if (!appendFormRef.value) return
  appendFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    appendSubmitting.value = true
    try {
      const res = await axios.post(
        getApiUrl(API_CONFIG.ENDPOINTS.SUBSCRIPTION_DIRECT_SUBMIT),
        {
          portfolioId: appendForm.portfolioId,
          subscriptionAmount: appendForm.amount,
          orderType: 'append',
          paymentMethod: appendForm.paymentMethod,
          dividendMode: appendForm.dividendMode,
          autoInvestEnabled: appendForm.autoInvestEnabled,
          autoInvestPeriod: appendForm.autoInvestPeriod,
          autoInvestAmount: appendForm.autoInvestAmount,
          riskMismatchConfirmed: true,
          customerRemark: appendForm.customerRemark
        }
      )
      if (res.data?.code === 200) {
        ElMessage.success('追加购入成功')
        appendDialogVisible.value = false
        loadPurchasedPortfolios()
      } else {
        ElMessage.error(res.data?.message || '追加购入失败')
      }
    } catch (error) {
      console.error(error)
      ElMessage.error('追加购入失败，请稍后重试')
    } finally {
      appendSubmitting.value = false
    }
  })
}
</script>

<style scoped>
.full-width-input {
  width: 100%;
}

.append-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
}
.append-label {
  font-size: 13px;
  color: #909399;
}
.append-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.page {
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

.content-card {
  border-radius: 16px;
  padding: 8px;
}

.summary-row {
  margin-bottom: 24px;
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
  font-size: 28px;
  font-weight: 600;
}

.summary-value.small {
  font-size: 18px;
}
</style>

