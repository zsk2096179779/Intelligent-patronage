<template>
  <div class="detail-container">
    <!-- 头部导航 -->
    <el-header class="detail-header">
      <el-button type="primary" :icon="ArrowLeft" @click="goBack">返回</el-button>
      <span class="header-title">策略详情</span>
    </el-header>

    <!-- 主要内容区 -->
    <el-main class="detail-main" v-loading="loading">
      <div v-if="error" class="error-message">
        <el-alert :title="error" type="error" show-icon />
      </div>

      <div v-if="strategy" class="detail-content">
        <!-- 基本信息卡片 -->
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>基本信息</span>
            </div>
          </template>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="策略名称">
              <span class="strategy-name">{{ strategy.name }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="策略类型">
              {{ strategy.type || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="风险等级">
              <el-tag :type="getRiskTagType(strategy.riskLevel)" size="large">
                {{ getRiskLabel(strategy.riskLevel) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTagType(strategy.status)" size="large">
                {{ getStatusLabel(strategy.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">
              {{ formatDateTime(strategy.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="修改时间" :span="2">
              {{ formatDateTime(strategy.updatedAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="策略描述" :span="2">
              <div class="description-text">
                {{ strategy.description || '暂无描述' }}
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 因子配置卡片 -->
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <el-icon><DataAnalysis /></el-icon>
              <span>因子配置</span>
            </div>
          </template>
          
          <el-table 
            :data="factors" 
            border 
            stripe
            v-if="factors && factors.length > 0"
            style="width: 100%"
          >
            <el-table-column prop="factorName" label="因子名称" min-width="200" />
            <el-table-column label="权重" min-width="120">
              <template #default="{ row }">
                {{ formatWeight(row.weight) }}%
              </template>
            </el-table-column>
            <el-table-column prop="frequency" label="计算频率" min-width="150">
              <template #default="{ row }">
                <el-tag size="small">{{ getFrequencyLabel(row.frequency) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          
          <el-empty v-else description="暂无因子配置" :image-size="100" />
        </el-card>

        <!-- 选基规则卡片 -->
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <el-icon><Setting /></el-icon>
              <span>选基规则</span>
            </div>
          </template>
          
          <div v-if="filterRules && filterRules.length > 0" class="rules-list">
            <div 
              v-for="(rule, index) in filterRules" 
              :key="index" 
              class="rule-item"
            >
              <el-icon class="rule-icon"><Document /></el-icon>
              <div class="rule-content">
                <div class="rule-details">
                  <div v-if="rule.topN" class="rule-detail-item">
                    <span class="rule-label">Top N:</span>
                    <span class="rule-value">选择排名前 {{ rule.topN }} 只基金</span>
                  </div>
                  <div v-if="rule.typeLimit" class="rule-detail-item">
                    <span class="rule-label">类型限制:</span>
                    <span class="rule-value">{{ parseTypeLimit(rule.typeLimit) }}</span>
                  </div>
                  <div v-if="rule.scaleLimit" class="rule-detail-item">
                    <span class="rule-label">规模限制:</span>
                    <span class="rule-value">{{ rule.scaleLimit }}</span>
                  </div>
                  <div v-if="!rule.topN && !rule.typeLimit && !rule.scaleLimit" class="rule-detail-item">
                    <span class="rule-value">暂无选基规则配置</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <el-empty v-else description="暂无选基规则" :image-size="100" />
        </el-card>
      </div>
    </el-main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  ArrowLeft, 
  DataAnalysis, 
  Setting, 
  Document 
} from '@element-plus/icons-vue'
import http from '@/modules/strategy-console/utils/http'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const usernameQuery = computed(() => route.query.username || '~')

// 数据状态
const loading = ref(true)
const error = ref(null)
const strategy = ref(null)
const factors = ref([])
const filterRules = ref([])

// 导航方法
const goBack = () => {
  router.push({
    path: '/strategy/management',
    query: { username: usernameQuery.value }
  })
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  try {
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (e) {
    return dateTime
  }
}

// 格式化权重
const formatWeight = (weight) => {
  if (weight === null || weight === undefined) return '0.00'
  const num = parseFloat(weight)
  return isNaN(num) ? '0.00' : num.toFixed(2)
}

// 获取风险等级标签
const getRiskLabel = (level) => {
  if (level === null || level === undefined) return '-'
  const levelNum = parseInt(level)
  const riskMap = {
    1: '保守型',
    2: '稳健型',
    3: '平衡型',
    4: '进取型',
    5: '激进型'
  }
  return riskMap[levelNum] || `等级${levelNum}`
}

// 获取风险等级标签类型
const getRiskTagType = (level) => {
  if (level === null || level === undefined) return 'info'
  const levelNum = parseInt(level)
  const typeMap = {
    1: 'success',
    2: 'info',
    3: '',
    4: 'warning',
    5: 'danger'
  }
  return typeMap[levelNum] || 'info'
}

// 获取状态标签
const getStatusLabel = (status) => {
  const statusMap = {
    running: '运行中',
    stop: '已停止',
    paused: '审核中'
  }
  return statusMap[status] || status || '-'
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    running: 'success',
    stop: 'info',
    paused: 'warning'
  }
  return typeMap[status] || 'info'
}

// 获取频率标签
const getFrequencyLabel = (frequency) => {
  const freqMap = {
    daily: '每日',
    weekly: '每周',
    monthly: '每月'
  }
  return freqMap[frequency] || frequency || '-'
}

// 解析类型限制（独热编码）
const parseTypeLimit = (typeLimit) => {
  if (!typeLimit) return '-'
  
  // 7种类型的顺序
  const typeOptions = ['股票型', '债券型', '混合型', '指数型', 'QDII', '货币型', '保本型']
  
  // 将独热编码字符串转换为类型名称数组
  const enabledTypes = []
  for (let i = 0; i < typeLimit.length && i < typeOptions.length; i++) {
    if (typeLimit[i] === '1') {
      enabledTypes.push(typeOptions[i])
    }
  }
  
  return enabledTypes.length > 0 ? enabledTypes.join('、') : '无限制'
}

// 加载策略详情数据
const loadData = async () => {
  loading.value = true
  error.value = null

  try {
    const strategyId = route.params.strategyId || route.query.strategyId
    if (!strategyId) {
      throw new Error('策略ID未提供')
    }

    const response = await http.post('/strategy-management/Detail', {
      strategyId: Number(strategyId)
    }, {
      headers: { 'Content-Type': 'application/json' }
    })

    const data = response.data
    
    // 设置策略基本信息
    strategy.value = data.strategy || {}
    
    // 设置因子配置
    factors.value = data.factors || []
    
    // 设置选基规则
    filterRules.value = data.filterRules || []

  } catch (err) {
    console.error('加载策略详情失败:', err)
    error.value = '数据加载失败: ' + (err.response?.data?.message || err.message)
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.detail-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.detail-header {
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 16px;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.detail-main {
  flex: 1;
  padding: 20px;
  overflow: auto;
}

.detail-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card {
  background: #ffffff;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.strategy-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.description-text {
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.rules-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rule-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #f9fafc;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  transition: all 0.3s;
}

.rule-item:hover {
  background: #f0f2f5;
  border-color: #c0c4cc;
}

.rule-icon {
  font-size: 20px;
  color: #409eff;
  margin-top: 2px;
}

.rule-content {
  flex: 1;
}

.rule-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rule-detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rule-label {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  min-width: 80px;
}

.rule-value {
  font-size: 13px;
  color: #303133;
}

.error-message {
  margin-bottom: 20px;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #606266;
}

:deep(.el-descriptions__content) {
  color: #303133;
}
</style>
