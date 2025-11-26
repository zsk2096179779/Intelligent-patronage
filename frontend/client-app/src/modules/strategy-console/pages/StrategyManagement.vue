<template>
  <el-container style="height: 100vh;">
    <el-header style="background: #f5f7fa; border-bottom: 1px solid #e4e7ed; display: flex; align-items: center; padding: 0 20px;">
      <span style="font-size: 18px; font-weight: bold;">策略管理</span>
    </el-header>
    
    <el-main style="background: #f5f7fa; padding: 20px; overflow: auto;">
      <div style="background: #fff; border-radius: 4px; box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); margin-bottom: 20px;">
        <div style="padding: 20px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h3 style="font-size: 16px; font-weight: 500; margin: 0;">策略列表</h3>
            <div style="display: flex;">
              <el-input 
                v-model="searchQuery"
                placeholder="搜索策略名称" 
                clearable
                style="margin-right: 15px; width: 240px;"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-select v-model="statusFilter" placeholder="全部状态" style="width: 120px;">
              <el-option label="全部状态" value="all" />
              <el-option label="运行中" value="running" />
              <el-option label="已停止" value="stop" />
              <el-option label="审核中" value="paused" />
              </el-select>
            </div>
          </div>
          
          <el-table :data="pagedStrategies" v-loading="loading" style="width: 100%">
            <el-table-column prop="name" label="策略名称" min-width="180" />
            <el-table-column prop="type" label="策略类型" min-width="120" />
            <el-table-column label="状态" min-width="100">
              <template #default="{ row }">
                <el-tag 
                  :type="statusTypeMap[row.status]"
                  :effect="row.status === 'running' ? 'light' : 'plain'"
                  style="display: flex; align-items: center;"
                >
                  <span :class="['status-dot', statusDotClassMap[row.status]]"></span>
                  {{ statusLabelMap[row.status] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" min-width="180" />
            <el-table-column label="风险等级" min-width="120">
              <template #default="{ row }">
                <span>{{ getRiskLabel(row.riskLevel) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="150">
              <template #default="{ row }">
                <el-button type="text" size="small" @click="viewDetail(row)">详情</el-button>
                <!-- 审核中的策略不显示启动/停止按钮 -->
                <el-button 
                  v-if="row.status === 'running'" 
                  type="text" 
                  size="small"
                  style="color: #f56c6c;"
                  @click="stopStrategy(row)"
                >
                  停止
                </el-button>
                <el-button 
                  v-else-if="row.status === 'stop'" 
                  type="text" 
                  size="small"
                  style="color: #67c23a;"
                  @click="startStrategy(row)"
                >
                  启动
                </el-button>
                <el-button type="text" size="small" style="color:#f56c6c;" @click="showDeleteConfirm(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 20px;">
            <div style="font-size: 12px; color: #909399;">
              共 {{ filteredStrategies.length }} 条记录，每页显示 {{ pageSize }} 条
            </div>
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 30, 40]"
              :total="filteredStrategies.length"
              layout="prev, pager, next"
              background
              small
            />
          </div>
        </div>
      </div>
    </el-main>
    <el-dialog
      v-model="deleteDialogVisible"
      title="删除策略"
      width="30%"
      :before-close="handleDialogClose"
    >
      <span>确定要删除策略 <strong>{{ deletingStrategyName }}</strong> 吗？此操作不可恢复。</span>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmDelete" style="background-color: #f56c6c; border:none;">
            确定删除
          </el-button>
        </div>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import http from '@/modules/strategy-console/utils/http'
import { ElMessage,ElMessageBox } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
// 加载状态
const loading = ref(false)
// 当前登录用户
const currentUsername = ref(null)

const usernameQueryValue = computed(() => {
  if (route.query.username) {
    return route.query.username
  }
  if (currentUsername.value) {
    return currentUsername.value
  }
  return '~'
})

// 搜索过滤条件
const searchQuery = ref('')
const statusFilter = ref('all')

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)

// 状态映射
const statusLabelMap = {
  running: '运行中',
  stop: '已停止',
  paused: '审核中'
}

const statusTypeMap = {
  running: 'success',
  stop: 'info',
  paused: 'warning'
}

const statusDotClassMap = {
  running: 'status-dot-success',
  stop: 'status-dot-info',
  paused: 'status-dot-warning'
}

// 策略数据
const strategies = ref([])

// 计算属性 - 过滤后的策略
const filteredStrategies = computed(() => {
  return strategies.value.filter(strategy => {
    const nameMatch = searchQuery.value 
      ? strategy.name.toLowerCase().includes(searchQuery.value.toLowerCase())
      : true
      
    const statusMatch = statusFilter.value === 'all' 
      ? true 
      : strategy.status === statusFilter.value
      
    return nameMatch && statusMatch
  })
})

// 计算属性 - 分页数据
const pagedStrategies = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStrategies.value.slice(start, end)
})

// 获取策略数据
const fetchStrategies = async () => {
  try {
    loading.value = true
    // 先获取当前登录用户
    try {
      const me = await http.get('/auth/me')
      if (me.status === 200 && me.data?.success) {
        currentUsername.value = me.data.username || null
      } else {
        currentUsername.value = null
      }
    } catch (_) {
      currentUsername.value = null
    }

    // 未登录或未知用户，展示空列表
    if (!currentUsername.value) {
      strategies.value = []
      return
    }

    // 根据当前用户获取策略列表（兼容后端参数 name/username）
    const response = await http.post('/strategy-management', {
      id:1,
      username: currentUsername.value
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    })

    const list = Array.isArray(response.data)
      ? response.data
      : (Array.isArray(response.data?.data) ? response.data.data : [])

    strategies.value = list.map(strategy => {
      return {
        id: strategy.id,
        name: strategy.name,
        type: strategy.type || '未知类型',
        status: String(strategy.status || 'stop').toLowerCase(),
        createdAt: formatDateTime(strategy.createdAt || strategy.createTime),
        riskLevel: strategy.riskLevel || strategy.risk_level || null
      }
    })
  } catch (error) {
    console.error('获取策略失败:', error)
    // 遇到错误展示为空列表
    strategies.value = []
  } finally {
    loading.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  const options = { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit', 
    minute: '2-digit'
  }
  return new Date(dateTime).toLocaleString('zh-CN', options).replace(/\//g, '-')
}

// 类型现在直接是字符串，不需要转换

// 将风险等级编码转为可读名称
const getRiskLabel = (level) => {
  if (level === null || level === undefined || level === '' || level === 'null' || level === 'undefined') {
    return '-'
  }
  
  // 处理数字或字符串形式的数字（包括小数格式如 1.00）
  let levelNum
  if (typeof level === 'string') {
    // 尝试解析字符串，去除空格，支持小数格式（如 "1.00" -> 1）
    const trimmed = level.trim()
    // 先尝试解析为浮点数，然后转为整数（处理 1.00 这种情况）
    const parsed = parseFloat(trimmed)
    levelNum = isNaN(parsed) ? null : Math.floor(parsed)
  } else if (typeof level === 'number') {
    // 如果是数字（包括小数），转为整数
    levelNum = Math.floor(level)
  } else {
    // 如果既不是字符串也不是数字，尝试转换为字符串再解析
    const str = String(level)
    const parsed = parseFloat(str)
    levelNum = isNaN(parsed) ? null : Math.floor(parsed)
  }
  
  if (levelNum === null || isNaN(levelNum)) {
    // 如果无法解析为数字，直接返回原值（可能是已经转换好的文本）
    return String(level)
  }
  
  const levels = {
    1: '保守型',
    2: '稳健型',
    3: '平衡型',
    4: '进取型',
    5: '激进型'
  }
  
  return levels[levelNum] || String(level)
}

// 策略操作函数
const viewDetail = (strategy) => {
  router.push({
    name: 'StrategyDetail',
    params: { strategyId: String(strategy.id) },
    query: { username: usernameQueryValue.value }
  })
}

// 停止策略
const stopStrategy = async (strategy) => {
  try {
    loading.value = true
    // 发送停止请求到后端（通过/api代理）
    const response = await http.post('/strategy-management/stop', 
      { strategyId: strategy.id }, 
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )
    
    if (response.data.success) {
      // 请求成功后更新前端状态
      strategy.status = 'stop'
      ElMessage.success('策略已停止')
    } else {
      ElMessage.error(`停止策略失败: ${response.data.message || '未知错误'}`)
    }
  } catch (error) {
    console.error('停止策略失败:', error)
    ElMessage.error('停止策略失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 启动策略
const startStrategy = async (strategy) => {
  try {
    loading.value = true
    // 发送启动请求到后端（通过/api代理）
    const response = await http.post('/strategy-management/start', 
      { strategyId: strategy.id }, 
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )
    
    if (response.data.success) {
      // 请求成功后更新前端状态
      strategy.status = 'running'
      ElMessage.success('策略已启动')
    } else {
      ElMessage.error(`启动策略失败: ${response.data.message || '未知错误'}`)
    }
  } catch (error) {
    console.error('启动策略失败:', error)
    ElMessage.error('启动策略失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const deleteDialogVisible = ref(false)
const deletingStrategy = ref(null) // 待删除的策略
const deletingStrategyName = computed(() => deletingStrategy.value?.name || '')
// 删除策略
const showDeleteConfirm = (strategy) => {
  deletingStrategy.value = strategy
  deleteDialogVisible.value = true
}

// 实际执行删除
const confirmDelete = async () => {
  if (!deletingStrategy.value) return
  
  try {
    loading.value = true
    const response = await http.post('/strategy-management/delete', 
      { strategyId: deletingStrategy.value.id }, 
      { headers: { 'Content-Type': 'application/json' } }
    )
    
    if (response.data.success) {
      // 从列表中移除已删除的策略
      const index = strategies.value.findIndex(s => s.id === deletingStrategy.value.id)
      if (index !== -1) strategies.value.splice(index, 1)
      ElMessage.success('策略已删除')
    } else {
      ElMessage.error(`删除策略失败: ${response.data.message || '未知错误'}`)
    }
  } catch (error) {
    console.error('删除策略失败:', error)
    ElMessage.error('删除策略失败: ' + error.message)
  } finally {
    loading.value = false
    deleteDialogVisible.value = false
    deletingStrategy.value = null
  }
}

// 关闭对话框时的提示
const handleDialogClose = (done) => {
  ElMessageBox.confirm('确认取消删除策略吗？')
    .then(() => done())
    .catch(() => {/* 取消关闭操作 */})
}

// 组件挂载时获取数据
onMounted(() => {
  fetchStrategies()
})
</script>

<style scoped>
.el-header {
  height: 60px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}

.status-dot-success {
  background-color: #67c23a;
}

.status-dot-info {
  background-color: #909399;
}

.status-dot-warning {
  background-color: #e6a23c;
}
</style>