<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的签约订单</h2>
        <p class="page-subtitle">查看当前登录用户的签约订单列表</p>
      </div>
      <el-select v-model="status" placeholder="订单状态" style="width: 180px" @change="loadData">
        <el-option label="全部" value="all" />
        <el-option label="草稿" value="draft" />
        <el-option label="待处理" value="processing" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
    </div>

    <el-table :data="orders" border stripe v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="portfolioName" label="组合名称" min-width="200" />
      <el-table-column prop="subscriptionAmount" label="签约金额" width="120" align="right">
        <template #default="{ row }">
          {{ row.subscriptionAmount?.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="feeAmount" label="手续费" width="100" align="right">
        <template #default="{ row }">
          {{ row.feeAmount?.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="riskMatched" label="风险匹配" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.riskMatched" type="success" size="small">匹配</el-tag>
          <el-tag v-else type="warning" size="small">不匹配</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="orderStatus" label="订单状态" width="120" />
      <el-table-column prop="paymentStatus" label="支付状态" width="120" />
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
          <el-button
            v-if="row.orderStatus === 'draft' || row.orderStatus === 'pending'"
            type="danger"
            link
            size="small"
            @click="cancelOrder(row)"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :current-page="page"
        :page-size="pageSize"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="620px">
      <div v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号" :span="2">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="组合名称" :span="2">{{ detail.portfolioName }}</el-descriptions-item>
          <el-descriptions-item label="签约金额">{{ detail.subscriptionAmount }}</el-descriptions-item>
          <el-descriptions-item label="手续费">{{ detail.feeAmount }}</el-descriptions-item>
          <el-descriptions-item label="用户风险等级">{{ detail.userRiskLevel }}</el-descriptions-item>
          <el-descriptions-item label="产品风险等级">{{ detail.productRiskLevel }}</el-descriptions-item>
          <el-descriptions-item label="风险匹配">{{ detail.riskMatched ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ detail.orderStatus }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">{{ detail.paymentStatus }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDateTime(detail.submittedAt) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatDateTime(detail.completedAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else>
        <el-skeleton :rows="8" animated />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApiUrl, API_CONFIG } from '@/config/api'

const loading = ref(false)
const status = ref('all')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orders = ref<any[]>([])

const detailVisible = ref(false)
const detail = ref<any | null>(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.SUBSCRIPTION_MY_ORDERS), {
      params: {
        status: status.value,
        page: page.value,
        pageSize: pageSize.value
      }
    })

    if (res.data && res.data.code === 200 && res.data.data) {
      orders.value = res.data.data.items || []
      total.value = res.data.data.total || 0
    } else {
      orders.value = []
      total.value = 0
      ElMessage.warning(res.data?.message || '加载订单列表失败')
    }
  } catch (e) {
    console.error('加载订单列表失败', e)
    ElMessage.error('加载订单列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (p: number) => {
  page.value = p
  loadData()
}

const viewDetail = async (row: any) => {
  detailVisible.value = true
  detail.value = null
  try {
    const res = await axios.get(
      getApiUrl(API_CONFIG.ENDPOINTS.SUBSCRIPTION_DETAIL(row.orderNo))
    )
    if (res.data && res.data.code === 200 && res.data.data) {
      detail.value = res.data.data
    } else {
      ElMessage.warning(res.data?.message || '获取订单详情失败')
    }
  } catch (e) {
    console.error('获取订单详情失败', e)
    ElMessage.error('获取订单详情失败，请稍后重试')
  }
}

const cancelOrder = async (row: any) => {
  await ElMessageBox.confirm(
    `确定要取消订单【${row.orderNo}】吗？`,
    '提示',
    { type: 'warning' }
  ).catch(() => {
    throw new Error('user-cancel')
  })

  try {
    const res = await axios.post(
      getApiUrl(API_CONFIG.ENDPOINTS.SUBSCRIPTION_CANCEL(row.orderNo)),
      { reason: '用户手动取消' }
    )
    if (res.data && res.data.code === 200) {
      ElMessage.success('订单已取消')
      loadData()
    } else {
      ElMessage.error(res.data?.message || '取消订单失败')
    }
  } catch (e: any) {
    if (e && e.message === 'user-cancel') return
    console.error('取消订单失败', e)
    ElMessage.error('取消订单失败，请稍后重试')
  }
}

const formatDateTime = (value?: string) => {
  if (!value) return '—'
  const v = value.replace(' ', 'T')
  const d = new Date(v)
  if (isNaN(d.getTime())) return value
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${hh}:${mm}`
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-title {
  margin: 0 0 4px;
}
.page-subtitle {
  margin: 0;
  color: #909399;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
