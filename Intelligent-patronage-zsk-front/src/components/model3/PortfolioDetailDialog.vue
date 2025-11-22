<template>
  <el-dialog
    v-model="dialogVisible"
    :title="portfolioDetail?.portfolioName || '组合详情'"
    width="900px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-loading="loading" class="detail-content">
      <div v-if="portfolioDetail" class="detail-wrapper">
        <!-- 基本信息 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <span class="section-title">基本信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="组合ID">{{ portfolioDetail.portfolioId }}</el-descriptions-item>
            <el-descriptions-item label="组合名称">{{ portfolioDetail.portfolioName }}</el-descriptions-item>
            <el-descriptions-item label="策略名称">{{ portfolioDetail.strategyName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="策略类型">
              <el-tag size="small">{{ portfolioDetail.strategyType || '—' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="组合类型">
              <el-tag size="small" type="info">{{ portfolioDetail.portfolioStrategyType || '—' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="风险等级">
              <el-tag :type="getRiskTagType(portfolioDetail.riskLevel)" size="small">
                {{ portfolioDetail.riskLevel || '—' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="策略描述" :span="2">
              {{ portfolioDetail.description || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(portfolioDetail.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="费率">
              {{ formatPercent(portfolioDetail.feeRate) }}
            </el-descriptions-item>
            <el-descriptions-item label="上架状态">
              <el-tag :type="portfolioDetail.listed === 1 ? 'success' : 'info'" size="small">
                {{ portfolioDetail.listed === 1 ? '已上架' : '未上架' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 收益指标 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <span class="section-title">收益指标</span>
            </div>
          </template>
          <el-row :gutter="20" class="metrics-grid">
            <el-col :xs="12" :sm="8" :md="6">
              <div class="metric-item">
                <div class="metric-label">累计收益</div>
                <div class="metric-value highlight-positive">
                  {{ formatPercent(portfolioDetail.returnRate) }}
                </div>
                <div class="metric-desc">策略累计收益率</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6">
              <div class="metric-item">
                <div class="metric-label">年化收益</div>
                <div class="metric-value highlight-positive">
                  {{ formatPercent(portfolioDetail.annualReturn) }}
                </div>
                <div class="metric-desc">年化收益率</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6">
              <div class="metric-item">
                <div class="metric-label">最大回撤</div>
                <div class="metric-value highlight-negative">
                  {{ formatPercent(portfolioDetail.maxDrawdown) }}
                </div>
                <div class="metric-desc">历史最大回撤率</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6">
              <div class="metric-item">
                <div class="metric-label">夏普比率</div>
                <div class="metric-value">
                  {{ formatNumber(portfolioDetail.sharpeRatio) }}
                </div>
                <div class="metric-desc">风险调整后收益</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6">
              <div class="metric-item">
                <div class="metric-label">波动率</div>
                <div class="metric-value">
                  {{ formatPercent(portfolioDetail.volatility) }}
                </div>
                <div class="metric-desc">收益波动程度</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6">
              <div class="metric-item">
                <div class="metric-label">胜率</div>
                <div class="metric-value highlight-positive">
                  {{ formatPercent(portfolioDetail.winRate) }}
                </div>
                <div class="metric-desc">平仓胜率</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 收益指标详细说明 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <span class="section-title">指标说明</span>
            </div>
          </template>
          <div class="indicator-description">
            <div class="desc-item">
              <strong>累计收益：</strong>策略自成立以来的累计收益率，反映策略的整体表现。
            </div>
            <div class="desc-item">
              <strong>年化收益：</strong>将累计收益按年化计算，便于不同期限策略的比较。
            </div>
            <div class="desc-item">
              <strong>最大回撤：</strong>策略净值从最高点到最低点的最大跌幅，反映策略的风险水平。
            </div>
            <div class="desc-item">
              <strong>夏普比率：</strong>衡量每承担一单位风险所获得的超额收益，数值越高越好。
            </div>
            <div class="desc-item">
              <strong>波动率：</strong>收益率的波动程度，反映策略的稳定性。
            </div>
            <div class="desc-item">
              <strong>胜率：</strong>平仓交易中盈利交易的比例，反映策略的盈利概率。
            </div>
          </div>
        </el-card>
      </div>

      <div v-else-if="!loading" class="empty-state">
        <el-empty description="组合信息加载失败" />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getApiUrl, API_CONFIG } from '../../config/api'

interface PortfolioDetail {
  portfolioId: number
  portfolioName: string
  riskLevel: string
  portfolioStrategyType: string
  listed: number
  status: string
  strategyId: number
  strategyName: string
  strategyType: string
  description: string
  createTime: string
  scale: number
  feeRate: number
  returnRate: number
  annualReturn: number
  maxDrawdown: number
  sharpeRatio: number
  volatility: number
  winRate: number
}

interface Props {
  visible: boolean
  portfolioId: number | null
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  portfolioId: null
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const dialogVisible = ref(false)
const loading = ref(false)
const portfolioDetail = ref<PortfolioDetail | null>(null)

// 监听 visible 变化
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
  if (newVal && props.portfolioId) {
    fetchDetail()
  }
})

// 监听 dialogVisible 变化，同步到父组件
watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

// 获取组合详情
const fetchDetail = async () => {
  if (!props.portfolioId) return

  loading.value = true
  portfolioDetail.value = null

  try {
    const response = await axios.get(
      getApiUrl(API_CONFIG.ENDPOINTS.GET_PORTFOLIO_DETAIL(props.portfolioId)),
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

    if (data) {
      // 规范化数据
      portfolioDetail.value = {
        portfolioId: data.portfolioId ?? data.portfolio_id ?? data.id,
        portfolioName: data.portfolioName ?? data.portfolio_name ?? data.name ?? '',
        riskLevel: data.riskLevel ?? data.risk_level ?? '',
        portfolioStrategyType: data.portfolioStrategyType ?? data.portfolio_strategy_type ?? '',
        listed: data.listed ?? 0,
        status: data.status ?? '',
        strategyId: data.strategyId ?? data.strategy_id ?? 0,
        strategyName: data.strategyName ?? data.strategy_name ?? '',
        strategyType: data.strategyType ?? data.strategy_type ?? '',
        description: data.description ?? data.desc ?? '',
        createTime: data.createTime ?? data.create_time ?? data.createdAt ?? data.created_at ?? '',
        scale: data.scale ?? 0,
        feeRate: data.feeRate ?? data.fee_rate ?? 0,
        returnRate: data.returnRate ?? data.return_rate ?? 0,
        annualReturn: data.annualReturn ?? data.annual_return ?? 0,
        maxDrawdown: data.maxDrawdown ?? data.max_drawdown ?? 0,
        sharpeRatio: data.sharpeRatio ?? data.sharpe_ratio ?? 0,
        volatility: data.volatility ?? 0,
        winRate: data.winRate ?? data.win_rate ?? 0
      }
    } else {
      ElMessage.warning('未获取到组合详情数据')
    }
  } catch (error) {
    console.error('获取组合详情失败', error)
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 404) {
        ElMessage.error('组合不存在')
      } else {
        ElMessage.error(error.response?.data?.message || '获取组合详情失败，请稍后重试')
      }
    } else {
      ElMessage.error('获取组合详情失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  portfolioDetail.value = null
}

// 获取风险等级标签类型
const getRiskTagType = (riskLevel: string) => {
  if (!riskLevel) return 'info'
  if (riskLevel.startsWith('R')) {
    const level = parseInt(riskLevel.substring(1))
    if (level <= 2) return 'success'
    if (level === 3) return 'warning'
    return 'danger'
  }
  if (riskLevel.includes('低') || riskLevel === '中低') return 'success'
  if (riskLevel.includes('中') && !riskLevel.includes('高') && !riskLevel.includes('低')) return 'warning'
  if (riskLevel.includes('高') || riskLevel === '中高') return 'danger'
  return 'info'
}

// 格式化百分比
const formatPercent = (value: number | null | undefined) => {
  if (value === undefined || value === null || isNaN(value)) return '—'
  if (Math.abs(value) > 1) {
    return `${Number(value).toFixed(2)}%`
  }
  return `${(Number(value) * 100).toFixed(2)}%`
}

// 格式化数字
const formatNumber = (value: number | null | undefined, decimals: number = 2) => {
  if (value === undefined || value === null || isNaN(value)) return '—'
  return Number(value).toFixed(decimals)
}

// 格式化资产规模
const formatScale = (value: number | undefined) => {
  if (value === undefined || value === null || isNaN(value)) return '—'
  if (value >= 10000) {
    return (Number(value) / 100000000).toFixed(2)
  }
  return Number(value).toFixed(2)
}

// 格式化日期时间
const formatDateTime = (value: string | undefined) => {
  if (!value) return '—'
  try {
    const normalizedValue = value.replace(' ', 'T')
    const date = new Date(normalizedValue)
    if (isNaN(date.getTime())) return value
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  } catch {
    return value
  }
}
</script>

<style scoped>
.detail-content {
  min-height: 400px;
}

.detail-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.metrics-grid {
  margin-top: 16px;
}

.metric-item {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.metric-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.metric-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.metric-value.highlight-positive {
  color: #67c23a;
}

.metric-value.highlight-negative {
  color: #f56c6c;
}

.metric-desc {
  font-size: 12px;
  color: #909399;
}

.indicator-description {
  padding: 16px 0;
}

.desc-item {
  margin-bottom: 12px;
  line-height: 1.6;
  color: #606266;
  font-size: 14px;
}

.desc-item strong {
  color: #303133;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>

