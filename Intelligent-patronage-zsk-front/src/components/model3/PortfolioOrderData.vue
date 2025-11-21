<template>
  <div class="portfolio-order-data-page">
    <!-- 数据概览 -->
    <el-row :gutter="16" class="summary-row">
      <el-col :xs="12" :sm="6" v-for="item in summaryStats" :key="item.key">
        <div class="summary-card">
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value">{{ item.value }}</div>
          <div v-if="item.subLabel" class="summary-sub">{{ item.subLabel }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 筛选栏 -->
    <div class="filter-section">
      <div class="filter-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索组合名称、策略名称"
          clearable
          style="width: 300px"
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <span class="data-count">共 {{ filteredData.length }} 条数据</span>
      </div>
      <div class="filter-right">
        <el-button type="primary" @click="fetchData" :icon="Refresh" :loading="loading">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="hover">
      <el-table 
        :data="filteredData" 
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        :empty-text="loading ? '加载中...' : '暂无数据'"
      >
        <el-table-column prop="portfolioId" label="组合ID" width="100" align="center" />
        
        <el-table-column prop="portfolioName" label="组合名称" width="180" show-overflow-tooltip>
          <template #default="scope">
            <el-link type="primary" @click="showDetail(scope.row)" :underline="false">
              {{ scope.row.portfolioName }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column prop="strategyName" label="策略名称" width="180" show-overflow-tooltip />
        
        <el-table-column prop="strategyType" label="策略类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getStrategyTypeTag(scope.row.strategyType)" size="small">
              {{ scope.row.strategyType }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="portfolioStrategyType" label="组合类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getPortfolioTypeTag(scope.row.portfolioStrategyType)" size="small">
              {{ scope.row.portfolioStrategyType }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="riskLevel" label="风险等级" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getRiskTagType(scope.row.riskLevel)" size="small">
              {{ scope.row.riskLevel }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="totalOrderCount" label="订单总数" width="110" align="center">
          <template #default="scope">
            <span class="order-count">{{ scope.row.totalOrderCount || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="completedOrderCount" label="已完成订单" width="120" align="center">
          <template #default="scope">
            <span class="completed-count">{{ scope.row.completedOrderCount || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="totalUserCount" label="订购用户数" width="120" align="center">
          <template #default="scope">
            <span class="user-count">{{ scope.row.totalUserCount || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="totalSubscriptionAmount" label="总签约金额(元)" width="150" align="right">
          <template #default="scope">
            <span class="amount-text">{{ formatAmount(scope.row.totalSubscriptionAmount) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="completedSubscriptionAmount" label="已完成金额(元)" width="150" align="right">
          <template #default="scope">
            <span class="amount-text">{{ formatAmount(scope.row.completedSubscriptionAmount) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="avgSubscriptionAmount" label="平均签约金额(元)" width="160" align="right">
          <template #default="scope">
            <span class="amount-text">{{ formatAmount(scope.row.avgSubscriptionAmount) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="showDetail(scope.row)" 
              :icon="View"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="组合订购数据详情" width="900px" :close-on-click-modal="false">
      <div v-if="currentDetail" class="detail-content">
        <el-tabs>
          <el-tab-pane label="基本信息">
            <el-descriptions :column="2" border style="margin-top: 16px;">
              <el-descriptions-item label="组合ID">{{ currentDetail.portfolioId }}</el-descriptions-item>
              <el-descriptions-item label="组合名称">{{ currentDetail.portfolioName }}</el-descriptions-item>
              <el-descriptions-item label="策略名称">{{ currentDetail.strategyName }}</el-descriptions-item>
              <el-descriptions-item label="策略类型">
                <el-tag :type="getStrategyTypeTag(currentDetail.strategyType)" size="small">
                  {{ currentDetail.strategyType }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="组合类型">
                <el-tag :type="getPortfolioTypeTag(currentDetail.portfolioStrategyType)" size="small">
                  {{ currentDetail.portfolioStrategyType }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="风险等级">
                <el-tag :type="getRiskTagType(currentDetail.riskLevel)">
                  {{ currentDetail.riskLevel }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="组合简介" :span="2">
                {{ currentDetail.summary || '—' }}
              </el-descriptions-item>
              <el-descriptions-item label="目标客户" :span="2">
                {{ currentDetail.targetInvestor || '—' }}
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDateTime(currentDetail.portfolioCreatedAt) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="订购统计">
            <el-descriptions :column="2" border style="margin-top: 16px;">
              <el-descriptions-item label="订单总数">
                <span class="order-count-large">{{ currentDetail.totalOrderCount || 0 }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="已完成订单">
                <span class="completed-count-large">{{ currentDetail.completedOrderCount || 0 }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="订购用户数">
                <span class="user-count-large">{{ currentDetail.totalUserCount || 0 }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="平均签约金额">
                <span class="amount-large">{{ formatAmount(currentDetail.avgSubscriptionAmount) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="总签约金额(元)" :span="2">
                <span class="amount-large">{{ formatAmount(currentDetail.totalSubscriptionAmount) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="已完成金额(元)" :span="2">
                <span class="amount-large">{{ formatAmount(currentDetail.completedSubscriptionAmount) }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Refresh, View, Search } from '@element-plus/icons-vue'
import { getApiUrl, API_CONFIG } from '../../config/api'

// 组合订购数据类型
interface PortfolioOrderData {
  portfolioId: number
  portfolioName: string
  riskLevel: string
  portfolioStrategyType: string
  strategyName: string
  strategyType: string
  summary: string
  targetInvestor: string
  portfolioCreatedAt: string
  totalOrderCount: number
  completedOrderCount: number
  totalSubscriptionAmount: number | null
  completedSubscriptionAmount: number | null
  totalUserCount: number
  avgSubscriptionAmount: number
}

// 响应式数据
const tableData = ref<PortfolioOrderData[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const detailDialogVisible = ref(false)
const currentDetail = ref<PortfolioOrderData | null>(null)

// 筛选后的数据
const filteredData = computed(() => {
  if (!searchKeyword.value.trim()) {
    return tableData.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return tableData.value.filter(item => 
    item.portfolioName?.toLowerCase().includes(keyword) ||
    item.strategyName?.toLowerCase().includes(keyword)
  )
})

// 获取风险等级标签类型
const getRiskTagType = (riskLevel: string) => {
  if (!riskLevel) return 'info'
  // R1-R5 格式
  if (riskLevel.startsWith('R')) {
    const level = parseInt(riskLevel.substring(1))
    if (level <= 2) return 'success'
    if (level === 3) return 'warning'
    return 'danger'
  }
  // 中文风险等级
  if (riskLevel.includes('低') || riskLevel === '中低') return 'success'
  if (riskLevel.includes('中') && !riskLevel.includes('高') && !riskLevel.includes('低')) return 'warning'
  if (riskLevel.includes('高') || riskLevel === '中高') return 'danger'
  return 'info'
}

// 获取策略类型标签
const getStrategyTypeTag = (strategyType: string | undefined) => {
  if (!strategyType) return 'info'
  if (strategyType.includes('成长')) return 'success'
  if (strategyType.includes('稳健') || strategyType.includes('保守')) return 'info'
  if (strategyType.includes('激进')) return 'danger'
  if (strategyType.includes('定制')) return 'warning'
  return 'info'
}

// 获取组合类型标签
const getPortfolioTypeTag = (portfolioType: string | undefined) => {
  if (!portfolioType) return 'info'
  if (portfolioType === 'FOF组合') return 'success'
  return 'info'
}

// 格式化金额
const formatAmount = (value: number | null | undefined) => {
  if (value === undefined || value === null || isNaN(value)) return '—'
  return Number(value).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 格式化日期时间
const formatDateTime = (value: string | undefined) => {
  if (!value) return '—'
  try {
    // 处理 YYYY-MM-DD 或 YYYY-MM-DD HH:MM:SS 格式
    const normalizedValue = value.replace(' ', 'T')
    const date = new Date(normalizedValue)
    
    if (isNaN(date.getTime())) {
      return value
    }
    
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    // 如果有时间部分，显示完整时间，否则只显示日期
    if (value.includes(' ') || value.includes('T')) {
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    }
    return `${year}-${month}-${day}`
  } catch {
    return value
  }
}

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑已在 computed 中实现
}

// 统计数据
const totalPortfolios = computed(() => tableData.value.length)
const totalOrders = computed(() => 
  tableData.value.reduce((sum, item) => sum + (item.totalOrderCount || 0), 0)
)
const totalUsers = computed(() => 
  tableData.value.reduce((sum, item) => sum + (item.totalUserCount || 0), 0)
)
const totalAmount = computed(() => 
  tableData.value.reduce((sum, item) => sum + (item.totalSubscriptionAmount || 0), 0)
)

const summaryStats = computed(() => [
  { 
    key: 'portfolios', 
    label: '已上架组合数', 
    value: totalPortfolios.value 
  },
  { 
    key: 'orders', 
    label: '订单总数', 
    value: totalOrders.value 
  },
  { 
    key: 'users', 
    label: '订购用户数', 
    value: totalUsers.value 
  },
  { 
    key: 'amount', 
    label: '总签约金额(元)', 
    value: formatAmount(totalAmount.value),
    subLabel: '累计签约金额'
  }
])

// 获取组合订购数据
const fetchData = async () => {
  loading.value = true
  try {
    const response = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.PORTFOLIO_ORDER_DATA), {
      headers: {
        'Content-Type': 'application/json'
      }
    })

    let dataArray: any[] = []
    
    // 统一响应格式 { code: 200, message: "...", data: [...] }
    if (response.data && response.data.code === 200 && response.data.data) {
      if (Array.isArray(response.data.data)) {
        dataArray = response.data.data
      }
    }
    // 直接返回数组
    else if (Array.isArray(response.data)) {
      dataArray = response.data
    }
    // 数据在 response.data.data 中
    else if (response.data && response.data.data && Array.isArray(response.data.data)) {
      dataArray = response.data.data
    }

    // 规范化数据
    const normalizedData: PortfolioOrderData[] = dataArray.map((item: any) => ({
      portfolioId: item.portfolioId ?? item.portfolio_id ?? item.id,
      portfolioName: item.portfolioName ?? item.portfolio_name ?? item.name ?? '',
      riskLevel: item.riskLevel ?? item.risk_level ?? '',
      portfolioStrategyType: item.portfolioStrategyType ?? item.portfolio_strategy_type ?? item.strategyType ?? item.strategy_type ?? '',
      strategyName: item.strategyName ?? item.strategy_name ?? '',
      strategyType: item.strategyType ?? item.strategy_type ?? '',
      summary: item.summary ?? item.desc ?? '',
      targetInvestor: item.targetInvestor ?? item.target_investor ?? '',
      portfolioCreatedAt: item.portfolioCreatedAt ?? item.portfolio_created_at ?? item.createdAt ?? item.created_at ?? '',
      totalOrderCount: item.totalOrderCount ?? item.total_order_count ?? 0,
      completedOrderCount: item.completedOrderCount ?? item.completed_order_count ?? 0,
      totalSubscriptionAmount: item.totalSubscriptionAmount ?? item.total_subscription_amount ?? null,
      completedSubscriptionAmount: item.completedSubscriptionAmount ?? item.completed_subscription_amount ?? null,
      totalUserCount: item.totalUserCount ?? item.total_user_count ?? 0,
      avgSubscriptionAmount: item.avgSubscriptionAmount ?? item.avg_subscription_amount ?? 0
    }))

    if (normalizedData.length > 0) {
      // 按组合ID升序排序
      normalizedData.sort((a, b) => {
        return (a.portfolioId || 0) - (b.portfolioId || 0)
      })
      tableData.value = normalizedData
    } else {
      ElMessage.warning('暂无订购数据')
      tableData.value = []
    }
  } catch (error) {
    console.error('获取组合订购数据失败', error)
    if (axios.isAxiosError(error)) {
      if (error.code === 'ERR_NETWORK' || error.message.includes('CORS')) {
        ElMessage.error('CORS 跨域错误：请检查后端 CORS 配置，或使用 Vite 代理')
      }
      else if (error.code === 'ERR_CONNECTION_REFUSED' || error.message.includes('ERR_CONNECTION_REFUSED')) {
        ElMessage.error('无法连接到后端服务器，请确保后端服务已启动')
      }
      else if (error.response?.status === 403) {
        ElMessage.error('访问被拒绝 (403)：请检查接口权限或路径是否正确')
      }
      else if (error.response) {
        ElMessage.error(`请求失败 (${error.response.status}): ${error.response.data?.message || error.message}`)
      }
      else {
        ElMessage.error(error.message || '网络请求失败，请稍后重试')
      }
    } else {
      ElMessage.error('获取数据失败，请稍后重试')
    }
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 显示详情
const showDetail = (row: PortfolioOrderData) => {
  currentDetail.value = { ...row }
  detailDialogVisible.value = true
}

// 组件挂载时获取数据
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.portfolio-order-data-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.filter-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.data-count {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.filter-right {
  display: flex;
  gap: 12px;
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #303133;
  font-weight: 600;
}

:deep(.el-table td) {
  color: #606266;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.order-count {
  color: #409EFF;
  font-weight: 600;
  font-size: 14px;
}

.completed-count {
  color: #67c23a;
  font-weight: 600;
  font-size: 14px;
}

.user-count {
  color: #E6A23C;
  font-weight: 600;
  font-size: 14px;
}

.amount-text {
  color: #303133;
  font-weight: 500;
  font-size: 14px;
}

.detail-content {
  padding: 16px 0;
}

.summary-row {
  margin-bottom: 20px;
}

.summary-row .el-col {
  margin-bottom: 16px;
}

.summary-card {
  padding: 18px 20px;
  border-radius: 14px;
  background: linear-gradient(135deg, #0f172a 0%, #1d3a6b 100%);
  border: 1px solid rgba(255, 255, 255, 0.06);
  min-height: 110px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #e0e7ff;
  box-shadow: 0 18px 45px -35px rgba(15, 23, 42, 0.7);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.summary-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 22px 50px -32px rgba(15, 23, 42, 0.85);
}

.summary-label {
  font-size: 14px;
  opacity: 0.75;
  letter-spacing: 0.4px;
}

.summary-value {
  font-size: 26px;
  font-weight: 700;
  color: #ffffff;
  line-height: 1;
  margin-top: 8px;
}

.summary-sub {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 4px;
}

.order-count-large {
  color: #409EFF;
  font-weight: 700;
  font-size: 20px;
}

.completed-count-large {
  color: #67c23a;
  font-weight: 700;
  font-size: 20px;
}

.user-count-large {
  color: #E6A23C;
  font-weight: 700;
  font-size: 20px;
}

.amount-large {
  color: #303133;
  font-weight: 700;
  font-size: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>

