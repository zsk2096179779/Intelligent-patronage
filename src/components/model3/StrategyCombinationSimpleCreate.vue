<template>
  <div class="simple-create">
    <div class="page-header">
      <div>
        <h2 class="page-title">创建策略组合</h2>
        <p class="page-subtitle">创建基础组合信息，后续可在配置界面进行详细配置</p>
      </div>
    </div>

    <el-card class="form-card" shadow="never">
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createFormRules"
        label-width="120px"
        label-position="right"
      >
        <el-form-item label="组合名称" prop="name">
          <el-input
            v-model="createForm.name"
            placeholder="请输入组合名称"
            maxlength="50"
            show-word-limit
            style="max-width: 500px;"
          />
        </el-form-item>

        <el-form-item label="风险等级" prop="riskLevel">
          <el-select
            v-model="createForm.riskLevel"
            placeholder="请选择风险等级"
            clearable
            filterable
            style="max-width: 500px;"
          >
            <el-option
              v-for="item in riskLevelOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="关联策略" prop="strategyRefId">
          <el-select
            v-model="createForm.strategyRefId"
            placeholder="请选择策略"
            clearable
            filterable
            :loading="strategiesLoading"
            style="max-width: 500px;"
          >
            <el-option
              v-for="strategy in strategiesList"
              :key="strategy.strategyRefId"
              :label="formatStrategyLabel(strategy)"
              :value="strategy.strategyRefId"
            >
              <div class="strategy-option">
                <div class="strategy-name">{{ strategy.strategyName }}</div>
                <div class="strategy-info">
                  <span>RefID: {{ strategy.strategyRefId }}</span>
                  <span v-if="strategy.strategyType">类型: {{ strategy.strategyType }}</span>
                </div>
              </div>
            </el-option>
          </el-select>
          <div class="form-tip">从已存在的策略中选择</div>
        </el-form-item>

        <el-form-item label="策略类型">
          <el-input
            :model-value="selectedStrategy?.strategyType || '—'"
            placeholder="自动根据策略带入"
            disabled
            style="max-width: 500px;"
          />
        </el-form-item>

        <el-form-item label="组合简介">
          <el-input
            v-model="createForm.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入组合简介（可选）"
            maxlength="200"
            show-word-limit
            style="max-width: 500px;"
          />
        </el-form-item>

        <el-form-item label="目标客户">
          <el-input
            v-model="createForm.targetInvestor"
            placeholder="请输入目标客户（可选）"
            maxlength="50"
            style="max-width: 500px;"
          />
        </el-form-item>
      </el-form>

      <div class="form-actions">
        <el-button @click="resetForm">重置</el-button>
        <el-button type="primary" @click="submitForm" :loading="creating">
          创建组合
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getApiUrl, API_CONFIG } from '../../config/api'

interface Strategy {
  strategyId?: number
  strategyRefId: number
  strategyName: string
  strategyType?: string
  description?: string
}

const router = useRouter()
const riskLevelOptions = ['低', '中低', '中', '中高', '高']

const createFormRef = ref<FormInstance>()
const strategiesList = ref<Strategy[]>([])
const strategiesLoading = ref(false)
const creating = ref(false)

const createForm = reactive({
  name: '',
  riskLevel: '',
  strategyType: '',
  strategyRefId: undefined as number | undefined,
  summary: '',
  targetInvestor: ''
})

const validateStrategyRefId = (_rule: any, value: number | undefined, callback: (error?: Error) => void) => {
  if (value === undefined || value === null) {
    callback(new Error('请选择策略'))
    return
  }
  callback()
}

const createFormRules: FormRules = {
  name: [
    { required: true, message: '请输入组合名称', trigger: 'blur' },
    { min: 2, max: 50, message: '组合名称长度需在 2-50 个字符之间', trigger: 'blur' }
  ],
  strategyRefId: [{ validator: validateStrategyRefId, trigger: 'change' }]
}

const selectedStrategy = computed(() =>
  strategiesList.value.find(strategy => strategy.strategyRefId === createForm.strategyRefId)
)

watch(selectedStrategy, (strategy) => {
  createForm.strategyType = strategy?.strategyType || ''
})

const formatStrategyLabel = (strategy: Strategy) => {
  const pieces = [`${strategy.strategyName}`]
  pieces.push(`RefID: ${strategy.strategyRefId}`)
  if (strategy.strategyType) {
    pieces.push(`类型: ${strategy.strategyType}`)
  }
  return pieces.join(' | ')
}

const normalizeStrategy = (item: any): Strategy | null => {
  const strategyRefId = Number(
    item.strategyRefId ??
      item.strategy_ref_id ??
      item.strategyId ??
      item.strategy_id ??
      item.id
  )

  if (!strategyRefId || Number.isNaN(strategyRefId)) {
    return null
  }

  const strategyIdValue = item.strategyId ?? item.strategy_id
  const strategyName =
    item.strategyName ?? item.strategy_name ?? item.name ?? `策略${strategyRefId}`
  const strategyType =
    item.strategyType ?? item.strategy_type ?? item.type ?? ''
  const description = item.description ?? item.remark ?? ''
  return {
    strategyId: strategyIdValue ? Number(strategyIdValue) : undefined,
    strategyRefId,
    strategyName,
    strategyType,
    description
  }
}

const fetchStrategies = async () => {
  strategiesLoading.value = true
  try {
    const response = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.STRATEGIES_LIST))

    let strategies: Strategy[] = []
    if (response.data && response.data.code === 200 && response.data.data) {
      if (Array.isArray(response.data.data)) {
        strategies = response.data.data
          .map(normalizeStrategy)
          .filter((item): item is Strategy => item !== null)
      }
    } else if (Array.isArray(response.data)) {
      strategies = response.data
        .map(normalizeStrategy)
        .filter((item): item is Strategy => item !== null)
    }

    if (!Array.isArray(strategies) || strategies.length === 0) {
      ElMessage.warning('后端未返回策略列表，请确认接口已实现并返回数据')
    } else {
      strategiesList.value = strategies.sort((a, b) => a.strategyRefId - b.strategyRefId)
    }
  } catch (error) {
    console.error('获取策略列表失败', error)
    if (axios.isAxiosError(error)) {
      ElMessage.error(error.response?.data?.message || '获取策略列表失败，请检查后端接口')
    } else {
      ElMessage.error('获取策略列表失败，请稍后重试')
    }
  } finally {
    strategiesLoading.value = false
  }
}

const resetForm = () => {
  createForm.name = ''
  createForm.riskLevel = ''
  createForm.strategyType = ''
  createForm.strategyRefId = undefined
  createForm.summary = ''
  createForm.targetInvestor = ''
  createFormRef.value?.clearValidate()
}

const submitForm = async () => {
  if (!createFormRef.value) return
  try {
    await createFormRef.value.validate()
  } catch {
    return
  }

  creating.value = true
  try {
    const payload = {
      name: createForm.name.trim(),
      riskLevel: createForm.riskLevel || undefined,
      strategyType: selectedStrategy.value?.strategyType || undefined,
      strategyRefId: createForm.strategyRefId,
      summary: createForm.summary || undefined,
      targetInvestor: createForm.targetInvestor || undefined
    }

    const response = await axios.post(
      getApiUrl(API_CONFIG.ENDPOINTS.STRATEGY_COMBINATION_CREATE),
      payload
    )

    const success =
      (response.data && response.data.code === 200) ||
      response.status === 200 ||
      response.status === 201

    if (success) {
      ElMessage.success('组合创建成功，请前往配置界面进行详细配置')
      resetForm()
      // 跳转到配置列表
      router.push('/combination/configure')
    } else {
      ElMessage.error(response.data?.message || '创建失败')
    }
  } catch (error) {
    console.error('创建策略组合失败', error)
    if (axios.isAxiosError(error)) {
      ElMessage.error(error.response?.data?.message || '创建失败，请稍后重试')
    } else {
      ElMessage.error('创建失败，请稍后重试')
    }
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  fetchStrategies()
})
</script>

<style scoped>
.simple-create {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
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

.form-card {
  border-radius: 8px;
  padding: 24px;
}

.form-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.strategy-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.strategy-name {
  font-weight: 500;
  color: #303133;
}

.strategy-info {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e0e0e0;
}
</style>

