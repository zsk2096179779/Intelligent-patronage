<template>
  <div class="strategy-create-container">
    <el-header class="header">
      <h1 class="header-title">策略创建</h1>
    </el-header>

    <el-card class="creation-panel">
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="120px"
        class="strategy-form"
      >
        <!-- 策略名称 -->
        <el-form-item label="策略名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入策略名称（2-20个字符）"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <!-- 策略类型 -->
        <el-form-item label="策略类型" prop="type">
          <el-select 
            v-model="form.type" 
            placeholder="请选择策略类型"
            style="width: 100%"
          >
            <el-option label="基金策略" value="基金策略" />
            <el-option label="ETF策略" value="ETF策略" />
            <el-option label="混合型" value="混合型" />
            <el-option label="大类资产配置" value="大类资产配置" />
            <el-option label="FOF组合" value="FOF组合" />
            <el-option label="基金指数组合" value="基金指数组合" />
            <el-option label="择时策略" value="择时策略" />
          </el-select>
        </el-form-item>

        <!-- 风险等级 -->
        <el-form-item label="风险等级" prop="riskLevel">
          <el-radio-group v-model="form.riskLevel" class="radio-group">
            <el-radio :label="1">保守型</el-radio>
            <el-radio :label="2">稳健型</el-radio>
            <el-radio :label="3">平衡型</el-radio>
            <el-radio :label="4">进取型</el-radio>
            <el-radio :label="5">激进型</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 描述 -->
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4"
            placeholder="请输入策略描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 因子配置 -->
        <el-form-item label="因子配置" prop="factors">
          <div class="factor-config-section">
            <div class="factor-header">
              <span class="factor-tip">从因子表中选择因子并设置权重（权重总和应为100%）</span>
              <el-button 
                type="primary" 
                size="small" 
                @click="loadFactors"
                :loading="loadingFactors"
              >
                刷新因子列表
              </el-button>
            </div>
            
            <div v-if="availableFactors.length === 0" class="empty-factors">
              <el-empty description="暂无可用因子" :image-size="80" />
            </div>
            
            <el-table 
              v-else
              :data="factorTableData" 
              border 
              class="factor-table"
              style="margin-top: 15px"
              @selection-change="handleFactorSelectionChange"
              ref="factorTableRef"
            >
              <el-table-column 
                type="selection" 
                width="55" 
                :selectable="checkSelectable"
                :reserve-selection="false"
              />
              <el-table-column prop="name" label="因子名称" min-width="150" />
              <el-table-column prop="code" label="因子代码" min-width="120" />
              <el-table-column prop="description" label="因子描述" min-width="200" show-overflow-tooltip />
              <el-table-column label="权重(%)" min-width="150">
                <template #default="{ row }">
                  <el-input-number 
                    v-model="row.weight" 
                    :min="0" 
                    :max="100" 
                    :precision="2"
                    :step="1"
                    controls-position="right"
                    style="width: 100%"
                    :disabled="!row.selected"
                    @change="validateWeights"
                  />
                </template>
              </el-table-column>
            </el-table>
            
            <div class="weight-summary">
              <span>已选因子数量: {{ selectedFactorsCount }}</span>
              <span :class="{ 'weight-error': totalWeight !== 100 }">
                权重总和: {{ totalWeight.toFixed(2) }}%
              </span>
            </div>
          </div>
        </el-form-item>

        <!-- 选基规则 -->
        <el-form-item label="选基规则">
          <div class="fund-selection-rules">
            <el-form-item label="Top N" prop="topN" style="margin-bottom: 20px;">
              <el-input-number 
                v-model="form.topN" 
                :min="1" 
                :max="100"
                placeholder="选择前N只基金"
                style="width: 100%"
              />
              <span class="rule-tip">选择排名前N的基金</span>
            </el-form-item>

            <el-form-item label="类型限制" prop="typeLimit" style="margin-bottom: 20px;">
              <el-select 
                v-model="form.typeLimit" 
                multiple
                placeholder="请选择基金类型"
                style="width: 100%"
                collapse-tags
                collapse-tags-tooltip
              >
                <el-option 
                  v-for="fundType in fundTypeOptions" 
                  :key="fundType.value"
                  :label="fundType.label" 
                  :value="fundType.value" 
                />
              </el-select>
              <span class="rule-tip">可选择多个基金类型</span>
            </el-form-item>

            <el-form-item label="规模限制" prop="scaleLimit">
              <el-input-number 
                v-model="form.scaleLimit" 
                :min="0"
                :precision="2"
                placeholder="最小规模（亿元）"
                style="width: 100%"
              />
              <span class="rule-tip">基金最小规模限制（单位：亿元）</span>
            </el-form-item>
          </div>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            创建策略
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import http from '@/modules/strategy-console/utils/http'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const factorTableRef = ref(null)
const submitting = ref(false)
const loadingFactors = ref(false)

// 基金类型选项
const fundTypeOptions = [
  { label: '股票型', value: '股票型' },
  { label: '债券型', value: '债券型' },
  { label: '混合型', value: '混合型' },
  { label: '指数型', value: '指数型' },
  { label: 'QDII', value: 'QDII' },
  { label: '货币型', value: '货币型' },
  { label: '保本型', value: '保本型' }
]

// 表单数据
const form = reactive({
  name: '',
  type: null,
  riskLevel: 3,
  description: '',
  factors: [],
  topN: 10,
  typeLimit: [],
  scaleLimit: 0
})

// 可用因子列表（从后端获取）
const availableFactors = ref([])

// 因子表格数据（包含选中状态和权重）
const factorTableData = ref([])

// 更新因子表格数据
const updateFactorTableData = () => {
  factorTableData.value = availableFactors.value.map(factor => {
    const selected = form.factors.find(f => f.factorId === factor.id)
    return {
      ...factor,
      selected: !!selected,
      weight: selected ? selected.weight : 0
    }
  })
}

// 监听可用因子列表变化
watch(availableFactors, () => {
  updateFactorTableData()
}, { immediate: true })

// 已选因子数量
const selectedFactorsCount = computed(() => {
  return form.factors.filter(f => f.weight > 0).length
})

// 权重总和
const totalWeight = computed(() => {
  return form.factors.reduce((sum, f) => sum + (f.weight || 0), 0)
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入策略名称', trigger: 'blur' },
    { min: 2, max: 20, message: '策略名称长度为2-20个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择策略类型', trigger: 'change' }
  ],
  riskLevel: [
    { required: true, message: '请选择风险等级', trigger: 'change' }
  ],
  description: [
    { max: 500, message: '描述不能超过500个字符', trigger: 'blur' }
  ],
  factors: [
    { 
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少选择一个因子'))
        } else if (totalWeight.value !== 100) {
          callback(new Error('因子权重总和必须为100%'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  topN: [
    { required: true, message: '请输入Top N值', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: 'Top N值应在1-100之间', trigger: 'blur' }
  ]
}

// 检查因子是否可选
const checkSelectable = (row) => {
  return true
}

// 加载因子列表
const loadFactors = async () => {
  loadingFactors.value = true
  try {
    // 尝试从后端获取因子列表
    // 如果API不存在，使用模拟数据
    try {
      const response = await http.get('/factors')
      if (response.data && Array.isArray(response.data)) {
        availableFactors.value = response.data
      } else if (response.data?.data && Array.isArray(response.data.data)) {
        availableFactors.value = response.data.data
      } else {
        // 如果API返回格式不符合预期，使用模拟数据
        availableFactors.value = getMockFactors()
      }
    } catch (error) {
      console.warn('获取因子列表失败，使用模拟数据:', error)
      // 使用模拟数据
      availableFactors.value = getMockFactors()
    }
    updateFactorTableData()
  } catch (error) {
    console.error('加载因子列表失败:', error)
    ElMessage.error('加载因子列表失败: ' + (error.message || '未知错误'))
    availableFactors.value = getMockFactors()
    updateFactorTableData()
  } finally {
    loadingFactors.value = false
  }
}

// 模拟因子数据（如果后端API不存在）
const getMockFactors = () => {
  return [
    { id: 1, name: '市盈率因子', code: 'PE', description: '基于市盈率的估值因子' },
    { id: 2, name: '市净率因子', code: 'PB', description: '基于市净率的估值因子' },
    { id: 3, name: 'ROE因子', code: 'ROE', description: '净资产收益率因子' },
    { id: 4, name: '营收增长率因子', code: 'REVENUE_GROWTH', description: '营业收入增长率因子' },
    { id: 5, name: '净利润增长率因子', code: 'PROFIT_GROWTH', description: '净利润增长率因子' },
    { id: 6, name: '波动率因子', code: 'VOLATILITY', description: '价格波动率因子' },
    { id: 7, name: '动量因子', code: 'MOMENTUM', description: '价格动量因子' },
    { id: 8, name: '换手率因子', code: 'TURNOVER', description: '股票换手率因子' }
  ]
}

// 验证权重
const validateWeights = () => {
  // 更新form.factors数组中已选因子的权重
  factorTableData.value.forEach(row => {
    if (row.selected) {
      const factor = form.factors.find(f => f.factorId === row.id)
      if (factor) {
        factor.weight = row.weight || 0
      } else {
        form.factors.push({
          factorId: row.id,
          factorName: row.name,
          factorCode: row.code,
          weight: row.weight || 0
        })
      }
    }
  })
  
  // 移除未选中的因子
  form.factors = form.factors.filter(f => {
    const row = factorTableData.value.find(r => r.id === f.factorId)
    return row && row.selected
  })
  
  // 触发验证
  if (formRef.value) {
    formRef.value.validateField('factors')
  }
}

// 处理因子选择变化
const handleFactorSelectionChange = (selection) => {
  // 更新选中状态和权重
  const selectedIds = new Set(selection.map(s => s.id))
  factorTableData.value.forEach(row => {
    const wasSelected = row.selected
    row.selected = selectedIds.has(row.id)
    if (!row.selected && wasSelected) {
      // 取消选择时重置权重
      row.weight = 0
      // 从form.factors中移除
      const index = form.factors.findIndex(f => f.factorId === row.id)
      if (index > -1) {
        form.factors.splice(index, 1)
      }
    } else if (row.selected && !wasSelected) {
      // 新选择时添加到form.factors，初始权重为0
      if (!form.factors.find(f => f.factorId === row.id)) {
        form.factors.push({
          factorId: row.id,
          factorName: row.name,
          factorCode: row.code,
          weight: 0
        })
      }
    }
  })
  validateWeights()
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 验证因子配置
    if (form.factors.length === 0) {
      ElMessage.warning('请至少选择一个因子并设置权重')
      return
    }
    
    if (Math.abs(totalWeight.value - 100) > 0.01) {
      ElMessage.warning('因子权重总和必须为100%')
      return
    }
    
    submitting.value = true
    const loading = ElLoading.service({ fullscreen: true, text: '正在创建策略...' })
    
    try {
      // 准备提交数据 - 匹配新数据库结构
      const factors = form.factors.map(f => ({
        factorName: f.factorName || f.name,
        weight: f.weight || 0,
        frequency: f.frequency || 'daily'
      }))
      
      // 构建选基规则 - 新结构：单个对象包含所有字段
      const filterRules = {}
      if (form.topN) {
        filterRules.topN = form.topN
      }
      if (form.typeLimit && form.typeLimit.length > 0) {
        // 将类型数组转换为独热编码字符串（7种类型）
        // 例如：['股票型', '混合型'] -> "1100000"
        const typeOptions = ['股票型', '债券型', '混合型', '指数型', 'QDII', '货币型', '保本型']
        const typeLimitArray = Array.isArray(form.typeLimit) ? form.typeLimit : [form.typeLimit]
        const oneHotEncoding = typeOptions.map(type => typeLimitArray.includes(type) ? '1' : '0').join('')
        filterRules.typeLimit = oneHotEncoding
      }
      if (form.scaleLimit) {
        filterRules.scaleLimit = form.scaleLimit
      }
      
      const requestData = {
        name: form.name,
        type: form.type,
        riskLevel: form.riskLevel,
        description: form.description,
        factors: factors,
        filterRules: filterRules
      }
      
      const response = await http.post('/strategy-management/new', requestData, {
        headers: { 'Content-Type': 'application/json' }
      })
      
      if (response.data.success) {
        ElMessage.success(`策略 "${form.name}" 创建成功！`)
        resetForm()
        // 可选：跳转到策略管理页面
        router.push({
          path: '/strategy/management',
          query: { username: route.query.username || '~' }
        })
      } else {
        ElMessage.error(`创建失败: ${response.data.message || '未知错误'}`)
      }
    } finally {
      loading.close()
      submitting.value = false
    }
  } catch (error) {
    if (error !== false) { // 表单验证失败会返回false
      console.error('创建策略失败:', error)
      
      // 如果是 401 错误，响应拦截器已经处理了重定向，这里只显示提示
      if (error.response?.status === 401) {
        ElMessage.error('未授权或令牌失效，正在跳转到登录页...')
      } else {
        ElMessage.error('创建失败: ' + (error.response?.data?.message || error.message || '未知错误'))
      }
    }
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.name = ''
  form.type = null
  form.riskLevel = 3
  form.description = ''
  form.factors = []
  form.topN = 10
  form.typeLimit = []
  form.scaleLimit = 0
  // 重置因子表格
  updateFactorTableData()
}

// 组件挂载时加载因子列表
onMounted(() => {
  loadFactors()
})
</script>

<style scoped>
.strategy-create-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.header-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.creation-panel {
  flex: 1;
  border-radius: 8px;
  overflow: auto;
}

.strategy-form {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.factor-config-section {
  width: 100%;
}

.factor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.factor-tip {
  font-size: 14px;
  color: #606266;
}

.empty-factors {
  padding: 40px 0;
  text-align: center;
}

.factor-table {
  margin-top: 15px;
}

.weight-summary {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
}

.weight-error {
  color: #f56c6c;
  font-weight: 500;
}

.fund-selection-rules {
  background: #f9fafb;
  padding: 20px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.rule-tip {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}
</style>
