<template>
  <div class="style-tag-management">
    <!-- 页面标题和统计 -->
    <el-card shadow="never" class="header-card">
      <div class="header-section">
        <div class="title-area">
          <h2>风格标签管理</h2>
          <p class="subtitle">管理衍生因子的风格标签，支持创建、编辑、删除和关联因子查询</p>
        </div>
        <div class="statistics-area">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.totalTags || 0 }}</div>
            <div class="stat-label">标签总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statistics.activeTags || 0 }}</div>
            <div class="stat-label">启用标签</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statistics.associatedFactors || 0 }}</div>
            <div class="stat-label">关联因子</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 搜索和操作栏 -->
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <div class="left-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索标签名称或编码"
            clearable
            style="width: 300px;"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch" style="margin-left: 12px;">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
        <div class="right-actions">
          <el-button type="primary" @click="openCreateDialog">
            <el-icon><Plus /></el-icon>
            创建风格标签
          </el-button>
          <el-button @click="loadAllTags">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 标签列表 -->
    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="tagList"
        border
        stripe
        height="600"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="tagId" label="标签ID" width="80" />
        <el-table-column prop="tagCode" label="标签编码" width="150">
          <template #default="{ row }">
            <el-tag type="info">{{ row.tagCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tagName" label="标签名称" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="viewTagDetail(row)">
              {{ row.tagName }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="factorCount" label="关联因子数" width="120">
          <template #default="{ row }">
            <el-badge :value="row.factorCount || 0" :max="99" class="factor-badge">
              <el-button
                text
                type="primary"
                size="small"
                @click="viewAssociatedFactors(row)"
                :disabled="!row.factorCount"
              >
                查看因子
              </el-button>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group size="small">
              <el-button text type="primary" @click="viewTagDetail(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button text type="primary" @click="editTag(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button text type="danger" @click="deleteTag(row)">
                <el-icon><Delete /></el-icon>
                删除
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
        />
      </div>
    </el-card>

    <!-- 创建/编辑标签对话框 -->
    <el-dialog
      v-model="showFormDialog"
      :title="formMode === 'create' ? '创建风格标签' : '编辑风格标签'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="tagForm" :rules="tagFormRules" ref="tagFormRef" label-width="100px">
        <el-form-item label="标签名称" prop="tagName">
          <el-input
            v-model="tagForm.tagName"
            placeholder="请输入标签名称，例如：价值风格"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="标签编码" prop="tagCode">
          <el-input
            v-model="tagForm.tagCode"
            placeholder="请输入标签编码，例如：VALUE_STYLE"
            maxlength="50"
            show-word-limit
            :disabled="formMode === 'edit'"
          >
            <template #append>
              <el-button @click="generateTagCode" v-if="formMode === 'create'">
                自动生成
              </el-button>
            </template>
          </el-input>
          <div class="form-tip" v-if="formMode === 'create'">
            建议使用大写字母和下划线，例如：VALUE_STYLE
          </div>
          <div class="form-tip" v-else>
            标签编码不可修改
          </div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="tagForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入标签描述，例如：适用于价值投资风格的因子"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showFormDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ formMode === 'create' ? '创建' : '更新' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 标签详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="风格标签详情"
      width="700px"
    >
      <el-descriptions :column="2" border v-if="currentTag">
        <el-descriptions-item label="标签ID">{{ currentTag.tagId }}</el-descriptions-item>
        <el-descriptions-item label="标签编码">
          <el-tag type="info">{{ currentTag.tagCode }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标签名称" :span="2">
          <span style="font-weight: 600; font-size: 16px;">{{ currentTag.tagName }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="关联因子数" :span="2">
          <el-badge :value="currentTag.factorCount || 0" :max="99">
            <el-button
              text
              type="primary"
              size="small"
              @click="viewAssociatedFactors(currentTag)"
              :disabled="!currentTag.factorCount"
            >
              查看关联因子
            </el-button>
          </el-badge>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ currentTag.description || '暂无描述' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ currentTag.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ currentTag.updateTime }}
        </el-descriptions-item>
        <el-descriptions-item label="创建人ID" v-if="currentTag.createUserId">
          {{ currentTag.createUserId }}
        </el-descriptions-item>
        <el-descriptions-item label="更新人ID" v-if="currentTag.updateUserId">
          {{ currentTag.updateUserId }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 关联因子列表对话框 -->
    <el-dialog
      v-model="showFactorsDialog"
      :title="`关联因子列表 - ${currentTag?.tagName || ''}`"
      width="1000px"
      :close-on-click-modal="false"
    >
      <div v-loading="factorsLoading">
        <el-alert
          v-if="associatedFactors.length > 0"
          :title="`共有 ${associatedFactors.length} 个因子使用此标签`"
          type="info"
          :closable="false"
          style="margin-bottom: 20px;"
        />

        <el-table
          v-if="associatedFactors.length > 0"
          :data="associatedFactors"
          border
          max-height="500"
        >
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="factorId" label="因子ID" width="80" />
          <el-table-column prop="factorCode" label="因子编码" width="150" />
          <el-table-column prop="factorName" label="因子名称" min-width="150">
            <template #default="{ row }">
              <el-link type="primary" @click="viewFactorDetail(row)">
                {{ row.factorName }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="factorType" label="因子类型" width="100">
            <template #default="{ row }">
              <el-tag>{{ row.factorType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createTime" label="创建时间" width="160" />
        </el-table>

        <el-empty v-else description="该标签暂未关联任何因子" />
      </div>
      <template #footer>
        <el-button @click="showFactorsDialog = false">关闭</el-button>
        <el-button type="primary" @click="exportAssociatedFactors" :disabled="associatedFactors.length === 0">
          <el-icon><Download /></el-icon>
          导出列表
        </el-button>
      </template>
    </el-dialog>

    <!-- 因子详情对话框 -->
    <el-dialog
      v-model="showFactorDetailDialog"
      title="因子详情"
      width="800px"
    >
      <el-descriptions :column="2" border v-if="currentFactor">
        <el-descriptions-item label="因子ID">{{ currentFactor.factorId }}</el-descriptions-item>
        <el-descriptions-item label="因子编码">{{ currentFactor.factorCode }}</el-descriptions-item>
        <el-descriptions-item label="因子名称" :span="2">
          {{ currentFactor.factorName }}
        </el-descriptions-item>
        <el-descriptions-item label="因子类型">
          <el-tag>{{ currentFactor.factorType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentFactor.status === 'ACTIVE' ? 'success' : 'info'">
            {{ currentFactor.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ currentFactor.description || '暂无描述' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ currentFactor.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ currentFactor.updateTime }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  View,
  Edit,
  Delete,
  Download
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// ========== 类型定义 ==========
interface ApiResponse<T = unknown> {
  code: number
  message?: string
  data?: T
  success?: boolean
}

interface StyleTag {
  tagId: number
  tagName: string
  tagCode: string
  description?: string
  factorCount?: number
  createTime: string
  updateTime: string
  createUserId?: number
  updateUserId?: number
}

interface DerivedFactor {
  factorId: number
  factorCode: string
  factorName: string
  factorType: string
  description?: string
  status: string
  createTime: string
  updateTime: string
}

interface FactorManagementResponse {
  success: boolean
  message: string
  factors?: DerivedFactor[]
  totalCount?: number
}

interface TagForm {
  tagId?: number
  tagName: string
  tagCode: string
  description: string
  createUserId?: number
}

interface Statistics {
  totalTags: number
  activeTags: number
  associatedFactors: number
}

// ========== 状态管理 ==========
const API_BASE = '/api/factor/style-tags'

// 搜索关键词
const searchKeyword = ref<string>('')

// 标签列表
const tagList = ref<StyleTag[]>([])
const loading = ref<boolean>(false)

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 统计信息
const statistics = reactive<Statistics>({
  totalTags: 0,
  activeTags: 0,
  associatedFactors: 0
})

// 对话框
const showFormDialog = ref<boolean>(false)
const showDetailDialog = ref<boolean>(false)
const showFactorsDialog = ref<boolean>(false)
const showFactorDetailDialog = ref<boolean>(false)

// 表单
const tagFormRef = ref<FormInstance>()
const formMode = ref<'create' | 'edit'>('create')
const submitting = ref<boolean>(false)

const tagForm = reactive<TagForm>({
  tagName: '',
  tagCode: '',
  description: '',
  createUserId: 1 // 默认用户ID，实际应从登录信息获取
})

const tagFormRules: FormRules = {
  tagName: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 2, max: 50, message: '标签名称长度应在 2-50 个字符', trigger: 'blur' }
  ],
  tagCode: [
    { required: true, message: '请输入标签编码', trigger: 'blur' },
    { pattern: /^[A-Z0-9_]+$/, message: '标签编码只能包含大写字母、数字和下划线', trigger: 'blur' }
  ]
}

// 当前操作的标签和因子
const currentTag = ref<StyleTag | null>(null)
const currentFactor = ref<DerivedFactor | null>(null)

// 关联因子列表
const associatedFactors = ref<DerivedFactor[]>([])
const factorsLoading = ref<boolean>(false)

// ========== 计算属性 ==========

// ========== API 调用 ==========
// 加载所有标签
async function loadAllTags() {
  loading.value = true
  try {
    console.log('=== 加载所有标签 ===')
    console.log('请求 URL:', `${API_BASE}`)
    console.log('请求方法: GET')

    const response = await request.get<ApiResponse<StyleTag[]>>(`${API_BASE}`)

    console.log('响应状态:', response.status)
    console.log('响应数据:', response.data)

    if (response.data.code === 200 && response.data.data) {
      tagList.value = response.data.data
      pagination.total = response.data.data.length

      // 更新统计
      statistics.totalTags = response.data.data.length
      statistics.activeTags = response.data.data.length
      statistics.associatedFactors = response.data.data.reduce(
        (sum, tag) => sum + (tag.factorCount || 0),
        0
      )
      console.log('加载成功，标签数量:', response.data.data.length)
    } else {
      console.error('加载失败，错误信息:', response.data.message)
      ElMessage.error(response.data.message || '加载标签列表失败')
    }
  } catch (error) {
    console.error('加载标签列表失败:', error)
    ElMessage.error('加载标签列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索标签
async function handleSearch() {
  if (!searchKeyword.value.trim()) {
    await loadAllTags()
    return
  }

  loading.value = true
  try {
    console.log('=== 搜索标签 ===')
    console.log('请求 URL:', `${API_BASE}/search`)
    console.log('请求方法: GET')
    console.log('请求参数:', { keyword: searchKeyword.value.trim() })

    const response = await request.get<ApiResponse<StyleTag[]>>(
      `${API_BASE}/search`,
      { params: { keyword: searchKeyword.value.trim() } }
    )

    console.log('响应状态:', response.status)
    console.log('响应数据:', response.data)

    if (response.data.code === 200 && response.data.data) {
      tagList.value = response.data.data
      pagination.total = response.data.data.length
      pagination.page = 1

      console.log('搜索成功，找到标签数量:', response.data.data.length)
      if (response.data.data.length === 0) {
        ElMessage.info('未找到匹配的标签')
      }
    } else {
      console.error('搜索失败，错误信息:', response.data.message)
      ElMessage.error(response.data.message || '搜索失败')
    }
  } catch (error) {
    console.error('搜索标签失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
function handleReset() {
  searchKeyword.value = ''
  pagination.page = 1
  loadAllTags()
}

// 打开创建对话框
function openCreateDialog() {
  formMode.value = 'create'
  resetForm()
  showFormDialog.value = true
}

// 编辑标签
function editTag(tag: StyleTag) {
  formMode.value = 'edit'
  tagForm.tagId = tag.tagId
  tagForm.tagName = tag.tagName
  tagForm.tagCode = tag.tagCode
  tagForm.description = tag.description || ''
  showFormDialog.value = true
}

// 提交表单
async function handleSubmit() {
  if (!tagFormRef.value) return

  await tagFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (formMode.value === 'create') {
        await createTag()
      } else {
        await updateTag()
      }
    } finally {
      submitting.value = false
    }
  })
}

// 创建标签
async function createTag() {
  try {
    const requestData = {
      tagName: tagForm.tagName,
      tagCode: tagForm.tagCode,
      description: tagForm.description || undefined,
      createUserId: tagForm.createUserId
    }

    console.log('=== 创建标签 ===')
    console.log('请求 URL:', `${API_BASE}`)
    console.log('请求方法: POST')
    console.log('请求数据:', requestData)

    const response = await request.post<ApiResponse<StyleTag>>(`${API_BASE}`, requestData)

    console.log('响应状态:', response.status)
    console.log('响应数据:', response.data)

    if (response.data.code === 200) {
      console.log('创建标签成功')
      ElMessage.success('创建标签成功')
      showFormDialog.value = false
      resetForm()
      await loadAllTags()
    } else {
      console.error('创建标签失败，错误信息:', response.data.message)
      ElMessage.error(response.data.message || '创建标签失败')
    }
  } catch (error) {
    console.error('创建标签失败:', error)
    ElMessage.error('创建标签失败')
  }
}

// 更新标签
async function updateTag() {
  if (!tagForm.tagId) return

  try {
    const requestData = {
      tagName: tagForm.tagName,
      tagCode: tagForm.tagCode,
      description: tagForm.description || undefined
    }

    console.log('=== 更新标签 ===')
    console.log('请求 URL:', `${API_BASE}/${tagForm.tagId}`)
    console.log('请求方法: PUT')
    console.log('请求数据:', requestData)

    const response = await request.put<ApiResponse<StyleTag>>(
      `${API_BASE}/${tagForm.tagId}`,
      requestData
    )

    console.log('响应状态:', response.status)
    console.log('响应数据:', response.data)

    if (response.data.code === 200) {
      console.log('更新标签成功')
      ElMessage.success('更新标签成功')
      showFormDialog.value = false
      resetForm()
      await loadAllTags()
    } else {
      console.error('更新标签失败，错误信息:', response.data.message)
      ElMessage.error(response.data.message || '更新标签失败')
    }
  } catch (error) {
    console.error('更新标签失败:', error)
    ElMessage.error('更新标签失败')
  }
}

// 删除标签
function deleteTag(tag: StyleTag) {
  ElMessageBox.confirm(
    `确定要删除标签"${tag.tagName}"吗？${tag.factorCount ? `该标签关联了 ${tag.factorCount} 个因子。` : ''}`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      console.log('=== 删除标签 ===')
      console.log('请求 URL:', `${API_BASE}/${tag.tagId}`)
      console.log('请求方法: DELETE')
      console.log('标签信息:', { tagId: tag.tagId, tagName: tag.tagName, tagCode: tag.tagCode })

      const response = await request.delete<ApiResponse<boolean>>(
        `${API_BASE}/${tag.tagId}`
      )

      console.log('响应状态:', response.status)
      console.log('响应数据:', response.data)

      if (response.data.code === 200 && response.data.data) {
        console.log('删除标签成功')
        ElMessage.success('删除标签成功')
        await loadAllTags()
      } else {
        console.error('删除标签失败，错误信息:', response.data.message)
        ElMessage.error(response.data.message || '删除标签失败')
      }
    } catch (error) {
      console.error('删除标签失败:', error)
      ElMessage.error('删除标签失败')
    }
  }).catch(() => {
    console.log('用户取消删除操作')
  })
}

// 查看标签详情
async function viewTagDetail(tag: StyleTag) {
  // 根据编码获取最新信息
  try {
    console.log('=== 查看标签详情 ===')
    console.log('请求 URL:', `${API_BASE}/code/${tag.tagCode}`)
    console.log('请求方法: GET')
    console.log('标签编码:', tag.tagCode)

    const response = await axios.get<ApiResponse<StyleTag>>(
      `${API_BASE}/code/${tag.tagCode}`
    )

    console.log('响应状态:', response.status)
    console.log('响应数据:', response.data)

    if (response.data.code === 200 && response.data.data) {
      console.log('获取标签详情成功')
      currentTag.value = response.data.data
      showDetailDialog.value = true
    } else {
      console.error('获取标签详情失败，错误信息:', response.data.message)
      ElMessage.error(response.data.message || '获取标签详情失败')
    }
  } catch (error) {
    console.error('获取标签详情失败:', error)
    ElMessage.error('获取标签详情失败')
  }
}

// 查看关联因子
async function viewAssociatedFactors(tag: StyleTag) {
  currentTag.value = tag
  showFactorsDialog.value = true
  factorsLoading.value = true
  associatedFactors.value = []

  try {
    console.log('=== 查看关联因子 ===')
    console.log('请求 URL:', `${API_BASE}/code/${tag.tagCode}/factors`)
    console.log('请求方法: GET')
    console.log('标签编码:', tag.tagCode)

    const response = await request.get<ApiResponse<FactorManagementResponse>>(
      `${API_BASE}/code/${tag.tagCode}/factors`
    )

    console.log('响应状态:', response.status)
    console.log('响应数据:', response.data)

    if (response.data.code === 200 && response.data.data) {
      const factorResponse = response.data.data
      if (factorResponse.success && factorResponse.factors) {
        console.log('获取关联因子成功，因子数量:', factorResponse.factors.length)
        associatedFactors.value = factorResponse.factors
      } else {
        console.warn('未找到关联因子:', factorResponse.message)
        ElMessage.warning(factorResponse.message || '未找到关联因子')
      }
    } else {
      console.error('获取关联因子失败，错误信息:', response.data.message)
      ElMessage.error(response.data.message || '获取关联因子失败')
    }
  } catch (error) {
    console.error('获取关联因子失败:', error)
    ElMessage.error('获取关联因子失败')
  } finally {
    factorsLoading.value = false
  }
}

// 查看因子详情
function viewFactorDetail(factor: DerivedFactor) {
  currentFactor.value = factor
  showFactorDetailDialog.value = true
}

// 导出关联因子
function exportAssociatedFactors() {
  ElMessage.info('导出功能开发中...')
  // TODO: 实现导出功能
}

// 生成标签编码
function generateTagCode() {
  if (!tagForm.tagName) {
    ElMessage.warning('请先输入标签名称')
    return
  }

  // 简单的拼音转换（实际项目中应使用专业的拼音库）
  const pinyin = tagForm.tagName
    .replace(/[\u4e00-\u9fa5]/g, () => {
      // 这里只是示例，实际应使用拼音转换库
      return 'CODE'
    })
    .toUpperCase()
    .replace(/[^A-Z0-9]/g, '_')

  tagForm.tagCode = `${pinyin}_${Date.now().toString().slice(-6)}`
  ElMessage.success('已自动生成标签编码')
}

// 重置表单
function resetForm() {
  tagForm.tagId = undefined
  tagForm.tagName = ''
  tagForm.tagCode = ''
  tagForm.description = ''
  tagFormRef.value?.clearValidate()
}

// ========== 生命周期 ==========
onMounted(() => {
  loadAllTags()
})
</script>

<style scoped>
.style-tag-management {
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

.toolbar-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-actions,
.right-actions {
  display: flex;
  align-items: center;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.factor-badge {
  margin-right: 8px;
}

.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
</style>
