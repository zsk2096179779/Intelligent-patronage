<template>
  <div class="factor-base-management">
    <!-- 页面标题和统计 -->
    <el-card shadow="never" class="header-card">
      <div class="header-section">
        <div class="title-area">
          <h2>基础因子管理</h2>
          <p class="subtitle">查询、选择和预览基础因子，为创建衍生因子做准备</p>
        </div>
        <div class="statistics-area">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.totalFactors || 0 }}</div>
            <div class="stat-label">基础因子总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statistics.selectedCount || 0 }}</div>
            <div class="stat-label">已选择</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statistics.validatedCount || 0 }}</div>
            <div class="stat-label">已验证</div>
          </div>
        </div>
      </div>
    </el-card>
    <!-- 操作按钮栏 -->
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <div class="left-info">
          <el-text v-if="selectedFactors.length > 0" type="primary" style="font-size: 16px;">
            已选择 <el-text type="primary" style="font-weight: 600;">{{ selectedFactors.length }}</el-text> 个因子
          </el-text>
        </div>
        <div class="right-actions">
          <el-button
            :disabled="selectedFactors.length === 0"
            @click="handleValidateSelection"
          >
            <el-icon><Select /></el-icon>
            验证选择
          </el-button>
          <el-button
            type="primary"
            :disabled="selectedFactors.length === 0"
            @click="handlePreviewSelection"
          >
            <el-icon><View /></el-icon>
            预览选中因子
          </el-button>
          <el-button
            type="success"
            :disabled="selectedFactors.length === 0"
            @click="handleCreateDerived"
          >
            <el-icon><Plus /></el-icon>
            创建衍生因子
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 因子列表 -->
    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="factorList"
        border
        stripe
        height="600"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" :selectable="checkSelectable" />
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="baseId" label="因子ID" width="80" />
        <el-table-column prop="factorCode" label="因子编码" min-width="120" />
        <el-table-column prop="factorName" label="因子名称" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="viewFactorDetail(row)">
              {{ row.factorName }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200">
          <template #default="{ row }">
            <span show-overflow-tooltip>{{ row.description || '暂无描述' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' || row.status === '1' ? 'success' : 'info'" size="small">
              {{ (row.status === 'ACTIVE' || row.status === '1') ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button-group size="small">
              <el-button text type="primary" @click="viewFactorDetail(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button text type="primary" @click="quickSelect(row)">
                <el-icon><Plus /></el-icon>
                选择
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 因子详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="因子详情"
      width="800px"
    >
      <el-descriptions :column="2" border v-if="currentFactor">
        <el-descriptions-item label="因子ID">{{ currentFactor.baseId }}</el-descriptions-item>
        <el-descriptions-item label="因子编码">{{ currentFactor.factorCode }}</el-descriptions-item>
        <el-descriptions-item label="因子名称">{{ currentFactor.factorName }}</el-descriptions-item>
        <el-descriptions-item label="因子类型">
          <el-tag :type="getFactorTypeTagType(currentFactor.factorType)">
            {{ getFactorTypeLabel(currentFactor.factorType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="数据频率">
          {{ getFrequencyLabel(currentFactor.frequency) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentFactor.status === 'ACTIVE' ? 'success' : 'info'">
            {{ currentFactor.status === 'ACTIVE' ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ currentFactor.description || '暂无描述' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" v-if="currentFactor.createTime">
          {{ currentFactor.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间" v-if="currentFactor.updateTime">
          {{ currentFactor.updateTime }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="showPreviewDialog"
      title="预览选中因子"
      width="900px"
      :close-on-click-modal="false"
    >
      <div v-loading="previewLoading">
        <el-alert
          v-if="previewData"
          :title="`已选择 ${previewData.totalCount} 个因子`"
          type="success"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div>覆盖率: {{ previewData.coverageRate }}%</div>
            <div v-if="previewData.validFactorCount !== undefined">
              有效因子: {{ previewData.validFactorCount }} / {{ previewData.totalCount }}
            </div>
          </template>
        </el-alert>

        <el-table
          v-if="previewData && previewData.factors"
          :data="previewData.factors"
          border
          max-height="400"
        >
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="baseId" label="因子ID" width="80" />
          <el-table-column prop="factorCode" label="因子编码" width="120" />
          <el-table-column prop="factorName" label="因子名称" min-width="150" />
          <el-table-column prop="factorType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getFactorTypeTagType(row.factorType)" size="small">
                {{ getFactorTypeLabel(row.factorType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="frequency" label="频率" width="80">
            <template #default="{ row }">
              {{ getFrequencyLabel(row.frequency) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button text type="danger" size="small" @click="removeFromPreview(row)">
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-else description="暂无数据" />
      </div>
      <template #footer>
        <el-button @click="showPreviewDialog = false">关闭</el-button>
        <el-button type="primary" @click="handleConfirmPreview">
          确认并创建衍生因子
        </el-button>
      </template>
    </el-dialog>

    <!-- 根据ID批量获取对话框 -->
    <el-dialog
      v-model="showGetByIdsDialog"
      title="根据ID获取因子"
      width="500px"
    >
      <el-form :model="getByIdsForm" label-width="100px">
        <el-form-item label="因子ID列表">
          <el-input
            v-model="getByIdsForm.factorIds"
            type="textarea"
            :rows="4"
            placeholder="请输入因子ID，用逗号分隔，例如：1,2,3,4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showGetByIdsDialog = false">取消</el-button>
        <el-button type="primary" @click="handleGetByIds">获取</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  Refresh,
  Select,
  View,
  Plus
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// ========== 类型定义 ==========
interface ApiResponse<T = unknown> {
  code: number
  message?: string
  data?: T
}

interface FactorBase {
  baseId: number
  factorCode: string
  factorName: string
  factorType: 'FUNDAMENTAL' | 'TECHNICAL' | 'SENTIMENT' | 'RISK'
  frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY'
  description?: string
  status: 'ACTIVE' | 'INACTIVE'
  createTime?: string
  updateTime?: string
}

interface QueryForm {
  keyword: string
  factorType: string
  frequency: string
  status: string
  page: number
  pageSize: number
}

interface FactorPreviewResponse {
  totalCount: number
  validFactorCount?: number
  coverageRate: number
  factors: FactorBase[]
}

interface Statistics {
  totalFactors: number
  selectedCount: number
  validatedCount: number
}

// ========== 状态管理 ==========
const router = useRouter()
const API_BASE = '/api/factor/base'

// 查询表单
const queryForm = reactive<QueryForm>({
  keyword: '',
  factorType: '',
  frequency: '',
  status: '',
  page: 1,
  pageSize: 20
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 因子列表
const factorList = ref<FactorBase[]>([])
const selectedFactors = ref<FactorBase[]>([])
const loading = ref<boolean>(false)

// 统计信息
const statistics = reactive<Statistics>({
  totalFactors: 0,
  selectedCount: 0,
  validatedCount: 0
})

// 对话框
const showDetailDialog = ref<boolean>(false)
const showPreviewDialog = ref<boolean>(false)
const showGetByIdsDialog = ref<boolean>(false)

// 当前查看的因子
const currentFactor = ref<FactorBase | null>(null)

// 预览数据
const previewData = ref<FactorPreviewResponse | null>(null)
const previewLoading = ref<boolean>(false)

// 根据ID获取表单
const getByIdsForm = reactive({
  factorIds: ''
})

// ========== 工具函数 ==========
// 获取因子类型标签类型
function getFactorTypeTagType(type: string): 'success' | 'info' | 'warning' | 'danger' {
  const typeMap: Record<string, 'success' | 'info' | 'warning' | 'danger'> = {
    FUNDAMENTAL: 'success',
    TECHNICAL: 'warning',
    SENTIMENT: 'info',
    RISK: 'danger'
  }
  return typeMap[type] || 'info'
}

// 获取因子类型标签
function getFactorTypeLabel(type: string): string {
  const labelMap: Record<string, string> = {
    FUNDAMENTAL: '基本面',
    TECHNICAL: '技术面',
    SENTIMENT: '情绪面',
    RISK: '风险'
  }
  return labelMap[type] || type
}

// 获取频率标签
function getFrequencyLabel(frequency: string): string {
  const labelMap: Record<string, string> = {
    DAILY: '日度',
    WEEKLY: '周度',
    MONTHLY: '月度',
    QUARTERLY: '季度',
    YEARLY: '年度'
  }
  return labelMap[frequency] || frequency
}

// 检查是否可选择
function checkSelectable(): boolean {
  // 允许选择所有因子，不限制状态
  return true
}

// ========== API 调用 ==========
// 查询因子
async function handleQuery() {
  loading.value = true
  try {
    const params = {
      keyword: queryForm.keyword || undefined,
      factorType: queryForm.factorType || undefined,
      frequency: queryForm.frequency || undefined,
      status: queryForm.status || undefined,
      page: pagination.page,
      pageSize: pagination.pageSize
    }

    const response = await request.get<ApiResponse<FactorBase[]>>(`${API_BASE}/query`, { params })

    if (response.data.code === 200 && response.data.data) {
      factorList.value = response.data.data
      // 假设后端返回了总数，如果没有则使用当前数据长度
      statistics.totalFactors = response.data.data.length
      pagination.total = response.data.data.length
    } else {
      ElMessage.error(response.data.message || '查询失败')
    }
  } catch (error) {
    console.error('查询因子失败:', error)
    ElMessage.error('查询因子失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
function handleReset() {
  queryForm.keyword = ''
  queryForm.factorType = ''
  queryForm.frequency = ''
  queryForm.status = ''
  pagination.page = 1
  handleQuery()
}

// 根据ID列表获取因子
async function handleGetByIds() {
  if (!getByIdsForm.factorIds.trim()) {
    ElMessage.warning('请输入因子ID')
    return
  }

  try {
    const response = await request.get<ApiResponse<FactorBase[]>>(
      `${API_BASE}/ids`,
      { params: { factorIds: getByIdsForm.factorIds } }
    )

    if (response.data.code === 200 && response.data.data) {
      ElMessage.success(`成功获取 ${response.data.data.length} 个因子`)
      factorList.value = response.data.data
      showGetByIdsDialog.value = false
      getByIdsForm.factorIds = ''
    } else {
      ElMessage.error(response.data.message || '获取失败')
    }
  } catch (error) {
    console.error('根据ID获取因子失败:', error)
    ElMessage.error('获取因子失败')
  }
}

// 验证因子选择
async function handleValidateSelection() {
  if (selectedFactors.value.length === 0) {
    ElMessage.warning('请先选择因子')
    return
  }

  try {
    const factorIds = selectedFactors.value.map(f => f.baseId).join(',')
    const response = await request.post<ApiResponse<boolean>>(
      `${API_BASE}/selection/validate`,
      null,
      { params: { factorIds } }
    )

    if (response.data.code === 200) {
      if (response.data.data) {
        ElMessage.success('因子选择验证通过')
        statistics.validatedCount = selectedFactors.value.length
      } else {
        ElMessage.warning('因子选择验证失败，请检查选中的因子')
      }
    } else {
      ElMessage.error(response.data.message || '验证失败')
    }
  } catch (error) {
    console.error('验证因子选择失败:', error)
    ElMessage.error('验证失败')
  }
}

// 预览选中因子
async function handlePreviewSelection() {
  if (selectedFactors.value.length === 0) {
    ElMessage.warning('请先选择因子')
    return
  }

  previewLoading.value = true
  showPreviewDialog.value = true

  try {
    const factorIds = selectedFactors.value.map(f => f.baseId)
    const response = await request.post<ApiResponse<FactorPreviewResponse>>(
      `${API_BASE}/selection/preview`,
      {
        factorIds,
        previewData: true,
        previewPeriod: 'LATEST'
      }
    )

    if (response.data.code === 200 && response.data.data) {
      previewData.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '预览失败')
      showPreviewDialog.value = false
    }
  } catch (error) {
    console.error('预览选中因子失败:', error)
    ElMessage.error('预览失败')
    showPreviewDialog.value = false
  } finally {
    previewLoading.value = false
  }
}

// 从预览中移除
function removeFromPreview(row: FactorBase) {
  if (previewData.value && previewData.value.factors) {
    const index = previewData.value.factors.findIndex(f => f.baseId === row.baseId)
      if (index > -1) {
        previewData.value.factors.splice(index, 1)
        previewData.value.totalCount--

        // 同时从选中列表移除
        const selectedIndex = selectedFactors.value.findIndex(f => f.baseId === row.baseId)
      if (selectedIndex > -1) {
        selectedFactors.value.splice(selectedIndex, 1)
        statistics.selectedCount = selectedFactors.value.length
      }
    }
  }
}

// 确认预览并创建衍生因子
function handleConfirmPreview() {
  if (!previewData.value || previewData.value.totalCount === 0) {
    ElMessage.warning('没有选中的因子')
    return
  }

  // 跳转到创建衍生因子页面，传递选中的因子ID
  const factorIds = previewData.value.factors.map(f => f.baseId).join(',')
  router.push({
    name: 'CreateDerivedFactorView',
    query: { factorIds }
  })
}

// 创建衍生因子
function handleCreateDerived() {
  if (selectedFactors.value.length === 0) {
    ElMessage.warning('请先选择因子')
    return
  }

  const factorIds = selectedFactors.value.map(f => f.baseId).join(',')
  router.push({
    name: 'CreateDerivedFactorView',
    query: { factorIds }
  })
}

// 查看因子详情
function viewFactorDetail(row: FactorBase) {
  currentFactor.value = row
  showDetailDialog.value = true
}

// 快速选择
function quickSelect(row: FactorBase) {
  const index = selectedFactors.value.findIndex(f => f.baseId === row.baseId)
  if (index === -1) {
    selectedFactors.value.push(row)
    ElMessage.success(`已添加"${row.factorName}"到选择列表`)
  } else {
    ElMessage.info(`"${row.factorName}"已在选择列表中`)
  }
  statistics.selectedCount = selectedFactors.value.length
}

// 处理选择变化
function handleSelectionChange(selection: FactorBase[]) {
  selectedFactors.value = selection
  statistics.selectedCount = selection.length
}

// ========== 生命周期 ==========
onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.factor-base-management {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-area h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.statistics-area {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.query-card {
  margin-bottom: 20px;
  padding: 15px 0;
}

.query-card .el-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.query-card .el-form-item {
  margin-bottom: 10px;
}

.toolbar-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-info {
  font-size: 14px;
}

.right-actions {
  display: flex;
  gap: 12px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
