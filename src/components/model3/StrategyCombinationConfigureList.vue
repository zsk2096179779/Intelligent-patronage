<template>
  <div class="configure-list">
    <div class="page-header">
      <div>
        <h2 class="page-title">组合配置管理</h2>
        <p class="page-subtitle">对已创建的组合进行详细配置，配置完成后可提交审核</p>
      </div>
      <el-button type="primary" @click="goToCreate">创建新组合</el-button>
    </div>

    <el-card class="list-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>待配置组合列表</span>
          <el-button :icon="Refresh" circle @click="fetchData" :loading="loading" />
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="portfolioId" label="组合ID" width="100" align="center" />
        <el-table-column prop="portfolioName" label="组合名称" min-width="150" />
        <el-table-column prop="riskLevel" label="风险等级" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getRiskTagType(scope.row.riskLevel)">
              {{ scope.row.riskLevel || '—' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="strategyName" label="关联策略" min-width="150" />
        <el-table-column prop="portfolioStrategyType" label="策略类型" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="配置状态" width="120" align="center">
          <template #default="scope">
            <el-tag type="warning">待配置</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="goToConfigure(scope.row.portfolioId)">
              开始配置
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="tableData.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无待配置的组合" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getApiUrl, API_CONFIG } from '../../config/api'

interface Portfolio {
  portfolioId: number
  portfolioName: string
  riskLevel: string
  portfolioStrategyType: string
  strategyId: number
  strategyName: string
  strategyType: string
  strategyRefId: number
  createTime: string
  listed: number // 0-待配置/待审批，1-已通过，-1-已拒绝
  status?: string // draft-未配置, pending_review-待审核
}

const router = useRouter()
const tableData = ref<Portfolio[]>([])
const loading = ref(false)

const getRiskTagType = (riskLevel: string) => {
  const riskMap: Record<string, string> = {
    '低': 'success',
    '中低': 'info',
    '中': 'warning',
    '中高': 'warning',
    '高': 'danger'
  }
  return riskMap[riskLevel] || ''
}

const formatDateTime = (value: string | undefined | null) => {
  if (!value || value === '' || value === null || value === undefined) {
    console.log('formatDateTime: 空值或未定义', value)
    return '—'
  }
  
  try {
    // 处理 YYYY-MM-DD HH:MM:SS 格式（后端返回的格式）
    // 将空格替换为 T，使其符合 ISO 8601 格式，便于 Date 对象解析
    const normalizedValue = String(value).replace(' ', 'T')
    const date = new Date(normalizedValue)
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      // 如果解析失败，尝试直接使用原值
      console.warn('formatDateTime: 日期解析失败', value, normalizedValue)
      return String(value)
    }
    
    // 格式化为 YYYY/MM/DD HH:MM:SS
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    const formatted = `${year}/${month}/${day} ${hours}:${minutes}:${seconds}`
    console.log('formatDateTime: 格式化结果', value, '->', formatted)
    return formatted
  } catch (error) {
    console.error('formatDateTime: 格式化异常', value, error)
    return String(value)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const response = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.STRATEGY_COMBINATION_ALL), {
      headers: {
        'Content-Type': 'application/json'
      }
    })

    let dataArray: any[] = []
    
    if (response.data && response.data.code === 200 && response.data.data) {
      if (Array.isArray(response.data.data)) {
        dataArray = response.data.data
      }
    } else if (Array.isArray(response.data)) {
      dataArray = response.data
    } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
      dataArray = response.data.data
    }

    // 规范化数据，处理不同的字段名
    const normalizedData: Portfolio[] = dataArray.map((item: any) => {
      // 优先使用 created_at（后端新字段）
      const createTime = item.created_at ?? item.createdAt ?? item.createTime ?? item.create_time ?? item.createDate ?? item.create_date ?? null
      
      // 调试日志：查看第一个条目的原始数据和时间字段
      if (dataArray.indexOf(item) === 0) {
        console.log('配置列表 - 原始数据示例:', item)
        console.log('配置列表 - 提取的创建时间:', createTime)
        console.log('配置列表 - 所有时间相关字段:', {
          created_at: item.created_at,
          createdAt: item.createdAt,
          createTime: item.createTime,
          create_time: item.create_time,
          createDate: item.createDate,
          create_date: item.create_date
        })
      }
      
      return {
        portfolioId: item.portfolioId ?? item.portfolio_id ?? item.id,
        portfolioName: item.portfolioName ?? item.portfolio_name ?? item.name ?? '',
        riskLevel: item.riskLevel ?? item.risk_level ?? '',
        portfolioStrategyType: item.portfolioStrategyType ?? item.portfolio_strategy_type ?? item.strategyType ?? item.strategy_type ?? '',
        strategyId: item.strategyId ?? item.strategy_id ?? 0,
        strategyName: item.strategyName ?? item.strategy_name ?? '',
        strategyType: item.strategyType ?? item.strategy_type ?? '',
        strategyRefId: item.strategyRefId ?? item.strategy_ref_id ?? item.strategyId ?? item.strategy_id ?? 0,
        createTime: createTime || '',
        listed: item.listed ?? 0,
        status: item.status ?? item.state ?? ''
      }
    })

    // 只显示未配置的组合（status = 'draft' 表示草稿未提交）
    tableData.value = normalizedData
      .filter(item => {
        const status = (item.status || '').toString().trim().toLowerCase()
        return status === '' || status === 'draft'
      })
      .sort((a, b) => (a.portfolioId || 0) - (b.portfolioId || 0))
  } catch (error) {
    console.error('获取组合列表失败', error)
    if (axios.isAxiosError(error)) {
      ElMessage.error(error.response?.data?.message || '获取组合列表失败，请检查后端接口')
    } else {
      ElMessage.error('获取组合列表失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const goToCreate = () => {
  router.push('/combination/create')
}

const goToConfigure = (portfolioId: number) => {
  router.push(`/combination/configure/${portfolioId}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.configure-list {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.list-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-state {
  padding: 40px 0;
}
</style>

