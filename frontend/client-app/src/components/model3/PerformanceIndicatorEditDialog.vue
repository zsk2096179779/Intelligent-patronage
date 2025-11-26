<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`设置性能指标 - ${portfolioName}`"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="140px"
      label-position="left"
    >
      <el-form-item label="策略收益(%)" prop="returnRate">
        <el-input-number
          v-model="formData.returnRate"
          :precision="2"
          :step="0.1"
          :min="-100"
          :max="1000"
          style="width: 100%"
          placeholder="请输入策略收益率"
        />
        <div class="form-tip">策略收益率，单位：百分比</div>
      </el-form-item>

      <el-form-item label="年化收益(%)" prop="annualReturn">
        <el-input-number
          v-model="formData.annualReturn"
          :precision="2"
          :step="0.1"
          :min="-100"
          :max="1000"
          style="width: 100%"
          placeholder="请输入年化收益率"
        />
        <div class="form-tip">年化收益率，单位：百分比</div>
      </el-form-item>

      <el-form-item label="最大回撤(%)" prop="maxDrawdown">
        <el-input-number
          v-model="formData.maxDrawdown"
          :precision="2"
          :step="0.1"
          :min="-100"
          :max="0"
          style="width: 100%"
          placeholder="请输入最大回撤率"
        />
        <div class="form-tip">最大回撤率，通常为负值，单位：百分比</div>
      </el-form-item>

      <el-form-item label="夏普比率" prop="sharpeRatio">
        <el-input-number
          v-model="formData.sharpeRatio"
          :precision="2"
          :step="0.1"
          :min="-10"
          :max="10"
          style="width: 100%"
          placeholder="请输入夏普比率"
        />
        <div class="form-tip">夏普比率，衡量风险调整后的收益</div>
      </el-form-item>

      <el-form-item label="波动率(%)" prop="volatility">
        <el-input-number
          v-model="formData.volatility"
          :precision="2"
          :step="0.1"
          :min="0"
          :max="100"
          style="width: 100%"
          placeholder="请输入波动率"
        />
        <div class="form-tip">波动率，单位：百分比</div>
      </el-form-item>

      <el-form-item label="胜率(%)" prop="winRate">
        <el-input-number
          v-model="formData.winRate"
          :precision="2"
          :step="0.1"
          :min="0"
          :max="100"
          style="width: 100%"
          placeholder="请输入胜率"
        />
        <div class="form-tip">平仓胜率，单位：百分比</div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          保存
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, reactive } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import axios from 'axios'
import { getApiUrl, API_CONFIG } from '../../config/api'

interface PerformanceData {
  returnRate: number | null
  annualReturn: number | null
  maxDrawdown: number | null
  sharpeRatio: number | null
  volatility: number | null
  winRate: number | null
}

interface Props {
  visible: boolean
  portfolioId: number | null
  portfolioName: string
  initialData?: PerformanceData | null
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  portfolioId: null,
  portfolioName: '',
  initialData: null
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const dialogVisible = ref(false)
const submitting = ref(false)

const formData = reactive<PerformanceData>({
  returnRate: null,
  annualReturn: null,
  maxDrawdown: null,
  sharpeRatio: null,
  volatility: null,
  winRate: null
})

// 表单验证规则
const rules: FormRules = {
  returnRate: [
    { required: true, message: '请输入策略收益率', trigger: 'blur' }
  ],
  annualReturn: [
    { required: true, message: '请输入年化收益率', trigger: 'blur' }
  ],
  maxDrawdown: [
    { required: true, message: '请输入最大回撤率', trigger: 'blur' }
  ],
  sharpeRatio: [
    { required: true, message: '请输入夏普比率', trigger: 'blur' }
  ],
  volatility: [
    { required: true, message: '请输入波动率', trigger: 'blur' }
  ],
  winRate: [
    { required: true, message: '请输入胜率', trigger: 'blur' }
  ]
}

// 监听 visible 变化
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
  if (newVal && props.initialData) {
    // 初始化表单数据
    Object.assign(formData, {
      returnRate: props.initialData.returnRate ?? null,
      annualReturn: props.initialData.annualReturn ?? null,
      maxDrawdown: props.initialData.maxDrawdown ?? null,
      sharpeRatio: props.initialData.sharpeRatio ?? null,
      volatility: props.initialData.volatility ?? null,
      winRate: props.initialData.winRate ?? null
    })
  }
})

// 监听 dialogVisible 变化，同步到父组件
watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  // 重置表单
  if (formRef.value) {
    formRef.value.resetFields()
  }
  // 清空表单数据
  Object.assign(formData, {
    returnRate: null,
    annualReturn: null,
    maxDrawdown: null,
    sharpeRatio: null,
    volatility: null,
    winRate: null
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请填写完整的性能指标信息')
    return
  }

  if (!props.portfolioId) {
    ElMessage.error('组合ID不能为空')
    return
  }

  submitting.value = true
  try {
    const response = await axios.put(
      getApiUrl(API_CONFIG.ENDPOINTS.UPDATE_PORTFOLIO_PERFORMANCE(props.portfolioId)),
      {
        returnRate: formData.returnRate,
        annualReturn: formData.annualReturn,
        maxDrawdown: formData.maxDrawdown,
        sharpeRatio: formData.sharpeRatio,
        volatility: formData.volatility,
        winRate: formData.winRate
      },
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    if (response.data && response.data.code === 200) {
      ElMessage.success('性能指标更新成功')
      emit('success')
      handleClose()
    } else {
      ElMessage.error(response.data?.message || '更新失败')
    }
  } catch (error) {
    console.error('更新性能指标失败', error)
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 404) {
        ElMessage.error('接口不存在，请检查后端接口路径')
      } else if (error.response?.status === 403) {
        ElMessage.error('权限不足，无法更新性能指标')
      } else {
        ElMessage.error(error.response?.data?.message || '更新失败，请稍后重试')
      }
    } else {
      ElMessage.error('更新失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-input-number) {
  width: 100%;
}
</style>

