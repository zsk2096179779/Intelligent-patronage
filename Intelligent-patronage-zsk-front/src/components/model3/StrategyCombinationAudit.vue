<template>
  <div class="strategy-audit-page">
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
        <el-radio-group v-model="filterType" size="default">
          <el-radio-button label="all">已审核策略</el-radio-button>
          <el-radio-button label="pending">待审批</el-radio-button>
        </el-radio-group>
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
        
        <el-table-column prop="scale" label="资产规模(亿元)" width="140" align="right">
          <template #default="scope">
            {{ formatScale(scope.row.scale) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="annualReturn" label="年化收益(%)" width="130" align="right">
          <template #default="scope">
            <span :class="getReturnClass(scope.row.annualReturn)">
              {{ formatPercent(scope.row.annualReturn) }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="returnRate" label="策略收益(%)" width="130" align="right">
          <template #default="scope">
            <span :class="getReturnClass(scope.row.returnRate)">
              {{ formatPercent(scope.row.returnRate) }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="sharpeRatio" label="夏普比率" width="110" align="right">
          <template #default="scope">
            {{ formatNumber(scope.row.sharpeRatio, 2) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="maxDrawdown" label="最大回撤(%)" width="130" align="right">
          <template #default="scope">
            <span class="down-color">
              {{ formatPercent(scope.row.maxDrawdown) }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="listed" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag 
              :type="scope.row.listed === 1 ? 'success' : scope.row.listed === -1 ? 'danger' : (scope.row.status === 'pending_review' ? 'warning' : 'info')" 
              size="small"
            >
              {{ getStatusText(scope.row.listed, scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <!-- 待审核状态（status = 'pending_review'）：显示通过和拒绝按钮 -->
            <template v-if="scope.row.status === 'pending_review'">
              <el-button 
                type="success" 
                size="small" 
                @click="handleApprove(scope.row)" 
                :icon="Check"
              >
                通过
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="handleReject(scope.row)" 
                :icon="Close"
              >
                拒绝
              </el-button>
            </template>
            <!-- 已通过状态：显示已通过 -->
            <span v-if="scope.row.listed === 1" class="approved-text">已通过</span>
            <!-- 已拒绝状态：显示拒绝 -->
            <span v-if="scope.row.listed === -1" class="rejected-text">已拒绝</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="策略组合详情" width="900px" :close-on-click-modal="false">
      <div v-if="currentDetail" class="detail-content">
        <el-tabs>
          <el-tab-pane label="基本信息">
            <el-descriptions :column="2" border style="margin-top: 16px;">
              <el-descriptions-item label="组合ID">{{ currentDetail.portfolioId }}</el-descriptions-item>
              <el-descriptions-item label="组合名称">{{ currentDetail.portfolioName }}</el-descriptions-item>
              <el-descriptions-item label="策略名称">{{ currentDetail.strategyName }}</el-descriptions-item>
              <el-descriptions-item label="策略ID">{{ currentDetail.strategyId }}</el-descriptions-item>
              <el-descriptions-item label="风险等级">
                <el-tag :type="getRiskTagType(currentDetail.riskLevel)">
                  {{ currentDetail.riskLevel }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="组合类型">
                <el-tag :type="getPortfolioTypeTag(currentDetail.portfolioStrategyType)">
                  {{ currentDetail.portfolioStrategyType }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="策略类型">
                <el-tag :type="getStrategyTypeTag(currentDetail.strategyType)">
                  {{ currentDetail.strategyType }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="上架状态">
                <el-tag 
                  :type="currentDetail.listed === 1 ? 'success' : currentDetail.listed === -1 ? 'danger' : (currentDetail.status === 'pending_review' ? 'warning' : 'info')"
                >
                  {{ getStatusText(currentDetail.listed, currentDetail.status) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="策略信息">
            <el-descriptions :column="2" border style="margin-top: 16px;">
              <el-descriptions-item label="策略ID">{{ currentDetail.strategyId }}</el-descriptions-item>
              <el-descriptions-item label="策略名称">{{ currentDetail.strategyName }}</el-descriptions-item>
              <el-descriptions-item label="策略描述" :span="2">
                {{ currentDetail.description || '—' }}
              </el-descriptions-item>
              <el-descriptions-item label="策略参考ID">{{ currentDetail.strategyRefId }}</el-descriptions-item>
              <el-descriptions-item label="创立时间">
                {{ formatDateTime(currentDetail.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="资产规模">
                {{ formatScale(currentDetail.scale) }} 亿元
              </el-descriptions-item>
              <el-descriptions-item label="费率">
                {{ formatPercent(currentDetail.feeRate) }}
              </el-descriptions-item>
              <el-descriptions-item label="成份基金" :span="2">
                <div v-if="currentDetail.funds" class="funds-list">
                  <el-tag v-for="(fund, index) in parseFunds(currentDetail.funds)" :key="index" style="margin-right: 8px; margin-bottom: 4px;">
                    {{ fund }}
                  </el-tag>
                </div>
                <span v-else>—</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="收益指标">
            <el-descriptions :column="2" border style="margin-top: 16px;">
              <el-descriptions-item label="策略收益(%)">
                <span :class="getReturnClass(currentDetail.returnRate)">
                  {{ formatPercent(currentDetail.returnRate) }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="年化收益(%)">
                <span :class="getReturnClass(currentDetail.annualReturn)">
                  {{ formatPercent(currentDetail.annualReturn) }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="波动率">
                {{ formatNumber(currentDetail.volatility, 2) }}
              </el-descriptions-item>
              <el-descriptions-item label="夏普比率">
                {{ formatNumber(currentDetail.sharpeRatio, 2) }}
              </el-descriptions-item>
              <el-descriptions-item label="最大回撤(%)">
                <span class="down-color">
                  {{ formatPercent(currentDetail.maxDrawdown) }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="平仓胜率(%)">
                {{ formatPercent(currentDetail.winRate) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝审核" width="500px" @close="handleRejectDialogClose">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="rejectLoading">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close, Refresh } from '@element-plus/icons-vue'
import { getApiUrl, API_CONFIG } from '../../config/api'

// 策略组合数据类型（根据实际后端返回的数据结构）
interface StrategyCombination {
  portfolioId: number
  portfolioName: string
  riskLevel: string
  portfolioStrategyType: string
  listed: number // 0-未审核，1-已通过，-1-已拒绝
  status?: string // draft-未配置, pending_review-待审核, approved-已通过, rejected-已拒绝
  strategyId: number
  strategyName: string
  strategyType: string
  description: string
  strategyRefId: number
  createTime: string
  scale: number // 资产规模（元）
  funds: string // 成份基金
  feeRate: number // 费率（小数形式，如 0.01 表示 1%）
  returnRate: number // 策略收益（小数形式，如 0.15 表示 15%）
  annualReturn: number // 年化收益（小数形式，如 0.13 表示 13%）
  volatility: number // 波动率
  sharpeRatio: number // 夏普比率
  maxDrawdown: number // 最大回撤（小数形式，如 0.08 表示 8%）
  winRate: number // 平仓胜率（小数形式，如 0.65 表示 65%）
}

// 响应式数据
const tableData = ref<StrategyCombination[]>([])
const loading = ref(false)
const filterType = ref<'all' | 'pending'>('all')
const detailDialogVisible = ref(false)
const currentDetail = ref<StrategyCombination | null>(null)
const rejectDialogVisible = ref(false)
const rejectForm = ref({ reason: '' })
const rejectLoading = ref(false)
const currentRejectRow = ref<StrategyCombination | null>(null)

// 筛选后的数据
const filteredData = computed(() => {
  if (filterType.value === 'pending') {
    // 待审批：只显示 status = 'pending_review' 的组合（待审核）
    return tableData.value.filter(item => {
      const status = item.status || ''
      return status === 'pending_review'
    })
  } else {
    // 已审批策略：显示已通过的（listed = 1）和已拒绝的（listed = -1）
    return tableData.value.filter(item => item.listed === 1 || item.listed === -1)
  }
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

// 格式化数字
const formatNumber = (value: number | undefined, decimals: number = 2) => {
  if (value === undefined || value === null || isNaN(value)) return '—'
  return Number(value).toFixed(decimals)
}

// 格式化资产规模（从元转换为亿元）
const formatScale = (value: number | undefined) => {
  if (value === undefined || value === null || isNaN(value)) return '—'
  // 如果数值很大（以元为单位），转换为亿元
  if (value >= 10000) {
    return (Number(value) / 100000000).toFixed(2)
  }
  // 如果数值较小（可能已经是亿元），直接显示
  return Number(value).toFixed(2)
}

// 格式化百分比（后端返回的是小数形式，如 0.15 表示 15%）
const formatPercent = (value: number | undefined) => {
  if (value === undefined || value === null || isNaN(value)) return '—'
  // 如果值大于1，说明已经是百分比形式，直接显示
  if (Math.abs(value) > 1) {
    return `${Number(value).toFixed(2)}%`
  }
  // 如果值小于等于1，说明是小数形式，转换为百分比
  return `${(Number(value) * 100).toFixed(2)}%`
}

// 格式化日期时间
const formatDateTime = (value: string | undefined) => {
  if (!value) return '—'
  try {
    // 处理 YYYY-MM-DD HH:MM:SS 格式（后端返回的格式）
    // 将空格替换为 T，使其符合 ISO 8601 格式，便于 Date 对象解析
    const normalizedValue = value.replace(' ', 'T')
    const date = new Date(normalizedValue)
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      // 如果解析失败，尝试直接使用原值
      return value
    }
    
    // 格式化为 YYYY/MM/DD HH:MM:SS
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    return `${year}/${month}/${day} ${hours}:${minutes}:${seconds}`
  } catch {
    return value
  }
}

// 获取收益率样式类
const getReturnClass = (value: number | undefined) => {
  if (value === undefined || value === null || isNaN(value)) return ''
  return Number(value) >= 0 ? 'up-color' : 'down-color'
}

// 获取状态文本
const getStatusText = (listed: number, status?: string) => {
  if (listed === 1) return '已通过'
  if (listed === -1) return '已拒绝'
  if (status === 'pending_review') return '待审核'
  return '未审核'
}

// 判断组合是否已配置（已配置的组合包括：待审核、已通过、已拒绝）
const isConfigured = (item: StrategyCombination) => {
  // 已配置的组合：status = 'pending_review' 或 listed = 1 或 listed = -1
  const status = item.status || ''
  return status === 'pending_review' || item.listed === 1 || item.listed === -1
}

// 只统计已配置的组合
const configuredData = computed(() => {
  return tableData.value.filter(isConfigured)
})

const totalCount = computed(() => configuredData.value.length)
const pendingCount = computed(() => {
  return configuredData.value.filter(item => {
    const status = item.status || ''
    return status === 'pending_review'
  }).length
})
const approvedCount = computed(() => configuredData.value.filter(item => item.listed === 1).length)
const rejectedCount = computed(() => configuredData.value.filter(item => item.listed === -1).length)

const summaryStats = computed(() => [
  { key: 'total', label: '组合总数', value: totalCount.value },
  { key: 'pending', label: '待审批', value: pendingCount.value, subLabel: pendingCount.value ? '请及时处理' : '暂无待办' },
  { key: 'approved', label: '已通过', value: approvedCount.value },
  { key: 'rejected', label: '已拒绝', value: rejectedCount.value }
])

// 解析成份基金
const parseFunds = (funds: string | undefined): string[] => {
  if (!funds) return []
  try {
    const parsed = JSON.parse(funds)
    if (Array.isArray(parsed)) {
      return parsed
    }
    return [funds]
  } catch {
    // 如果不是JSON，按逗号分割
    return funds.split(',').map(f => f.trim()).filter(f => f)
  }
}

// 获取所有策略组合列表
const fetchData = async () => {
  loading.value = true
  try {
    const response = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.STRATEGY_COMBINATION_ALL), {
      headers: {
        'Content-Type': 'application/json'
      }
    })

    let dataArray: any[] = []
    
    // 情况1: 统一响应格式 { code: 200, message: "...", data: [...] }
    if (response.data && response.data.code === 200 && response.data.data) {
      if (Array.isArray(response.data.data)) {
        dataArray = response.data.data
      }
    }
    // 情况2: 直接返回数组
    else if (Array.isArray(response.data)) {
      dataArray = response.data
    }
    // 情况3: 数据在 response.data.data 中（即使 code 不是 200）
    else if (response.data && response.data.data && Array.isArray(response.data.data)) {
      dataArray = response.data.data
    }

    // 规范化数据，处理不同的字段名（特别是 created_at）
    const normalizedData: StrategyCombination[] = dataArray.map((item: any) => ({
      portfolioId: item.portfolioId ?? item.portfolio_id ?? item.id,
      portfolioName: item.portfolioName ?? item.portfolio_name ?? item.name ?? '',
      riskLevel: item.riskLevel ?? item.risk_level ?? '',
      portfolioStrategyType: item.portfolioStrategyType ?? item.portfolio_strategy_type ?? item.strategyType ?? item.strategy_type ?? '',
      listed: item.listed ?? 0,
      status: item.status ?? item.state ?? '',
      strategyId: item.strategyId ?? item.strategy_id ?? 0,
      strategyName: item.strategyName ?? item.strategy_name ?? '',
      strategyType: item.strategyType ?? item.strategy_type ?? '',
      description: item.description ?? item.desc ?? '',
      strategyRefId: item.strategyRefId ?? item.strategy_ref_id ?? item.strategyId ?? item.strategy_id ?? 0,
      createTime: item.createTime ?? item.create_time ?? item.createdAt ?? item.created_at ?? item.createDate ?? item.create_date ?? '',
      scale: item.scale ?? 0,
      funds: item.funds ?? '',
      feeRate: item.feeRate ?? item.fee_rate ?? 0,
      returnRate: item.returnRate ?? item.return_rate ?? 0,
      annualReturn: item.annualReturn ?? item.annual_return ?? 0,
      volatility: item.volatility ?? 0,
      sharpeRatio: item.sharpeRatio ?? item.sharpe_ratio ?? 0,
      maxDrawdown: item.maxDrawdown ?? item.max_drawdown ?? 0,
      winRate: item.winRate ?? item.win_rate ?? 0
    }))

    if (normalizedData.length > 0) {
      // 按组合ID升序排序
      normalizedData.sort((a, b) => {
        return (a.portfolioId || 0) - (b.portfolioId || 0)
      })
      tableData.value = normalizedData
    } else {
      ElMessage.warning('暂无策略组合数据')
      tableData.value = []
    }
  } catch (error) {
    console.error('获取策略组合列表失败', error)
    if (axios.isAxiosError(error)) {
      // CORS 错误
      if (error.code === 'ERR_NETWORK' || error.message.includes('CORS')) {
        ElMessage.error('CORS 跨域错误：请检查后端 CORS 配置，或使用 Vite 代理')
      }
      // 连接被拒绝
      else if (error.code === 'ERR_CONNECTION_REFUSED' || error.message.includes('ERR_CONNECTION_REFUSED')) {
        ElMessage.error('无法连接到后端服务器，请确保后端服务已启动在 http://localhost:8080')
      }
      // 403 Forbidden
      else if (error.response?.status === 403) {
        ElMessage.error('访问被拒绝 (403)：请检查接口权限或路径是否正确')
        console.error('403 错误详情:', error.response)
      }
      // 其他 HTTP 错误
      else if (error.response) {
        ElMessage.error(`请求失败 (${error.response.status}): ${error.response.data?.message || error.message}`)
      }
      // 其他网络错误
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
const showDetail = (row: StrategyCombination) => {
  currentDetail.value = { ...row }
  detailDialogVisible.value = true
}

// 审核通过
const handleApprove = async (row: StrategyCombination) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过策略组合"${row.portfolioName}"的审核吗？通过后该组合将上架。`,
      '确认审核',
      {
        confirmButtonText: '确定通过',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    try {
      const response = await axios.post(
        getApiUrl(API_CONFIG.ENDPOINTS.STRATEGY_COMBINATION_APPROVE(row.portfolioId))
      )
      
      if (response.data.code === 200) {
        ElMessage.success('审核通过成功')
        await fetchData()
      } else {
        ElMessage.error(response.data.message || '审核失败')
      }
    } catch (error) {
      console.error('审核通过失败', error)
      if (axios.isAxiosError(error)) {
        if (error.response?.status === 404) {
          ElMessage.warning('审核接口尚未实现，请联系后端开发人员')
        } else {
          ElMessage.error(error.response?.data?.message || '审核失败，请稍后重试')
        }
      } else {
        ElMessage.error('审核失败，请稍后重试')
      }
    } finally {
      loading.value = false
    }
  } catch {
    // 用户取消
  }
}

// 审核拒绝
const handleReject = (row: StrategyCombination) => {
  currentRejectRow.value = row
  rejectForm.value.reason = ''
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectForm.value.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  if (!currentRejectRow.value) return

  rejectLoading.value = true
  try {
    const response = await axios.post(
      getApiUrl(API_CONFIG.ENDPOINTS.STRATEGY_COMBINATION_REJECT(currentRejectRow.value.portfolioId)),
      { reason: rejectForm.value.reason }
    )
    
    if (response.data.code === 200) {
      ElMessage.success('审核拒绝成功')
      rejectDialogVisible.value = false
      await fetchData()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    console.error('审核拒绝失败', error)
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 404) {
        ElMessage.warning('审核接口尚未实现，请联系后端开发人员')
      } else {
        ElMessage.error(error.response?.data?.message || '操作失败，请稍后重试')
      }
    } else {
      ElMessage.error('操作失败，请稍后重试')
    }
  } finally {
    rejectLoading.value = false
  }
}

// 关闭拒绝对话框
const handleRejectDialogClose = () => {
  rejectForm.value.reason = ''
  currentRejectRow.value = null
}

// 组件挂载时获取数据
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.strategy-audit-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.filter-section {
  display: flex;
  align-items: center;
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

.up-color {
  color: #67c23a;
  font-weight: 600;
}

.down-color {
  color: #f56c6c;
  font-weight: 600;
}

.approved-text {
  color: #67c23a;
  font-size: 14px;
  font-weight: 500;
}

.rejected-text {
  color: #f56c6c;
  font-size: 14px;
  font-weight: 500;
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
}

.summary-sub {
  font-size: 12px;
  opacity: 0.7;
}

.funds-list {
  max-height: 150px;
  overflow-y: auto;
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
