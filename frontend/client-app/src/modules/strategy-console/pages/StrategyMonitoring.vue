<template>
  <el-container class="monitor-container">
    <!-- 头部区域 -->
    <el-header class="monitor-header">
      <div class="header-left">
        <div class="status-indicator" :class="systemStatusClass"></div>
        <span class="text-lg font-bold">策略监控中心</span>
      </div>
      
      <div class="header-controls">
        <el-select 
          v-model="selectedStrategy" 
          placeholder="选择策略" 
          size="medium" 
          @change="handleStrategySelect"
          style="width: 200px;"
          clearable
          value-key="id"
        >
          <el-option
            v-for="strategy in strategies"
            :key="strategy.id"
            :label="strategy.name"
            :value="strategy.id">
          </el-option>
        </el-select>
        
        <el-button type="primary" size="medium" @click="refreshData" :icon="Refresh" :loading="loading">
          刷新数据
        </el-button>
      </div>
    </el-header>
    
    <!-- 主内容区域 -->
    <el-main class="monitor-content">
      <!-- 关键指标卡片区域 -->
      <div class="metrics-grid">
        <!-- 运行状态 -->
        <el-card class="metric-card">
          <div class="metric-title">
            <span>运行状态</span>
            <el-tag size="small" :type="getStatusTagType(monitorData.systemStatus)">
              {{ getStatusText(monitorData.systemStatus) }}
            </el-tag>
          </div>
          <div class="status-card">
            <div class="status-indicator-large" :class="systemStatusClass"></div>
            <div class="metric-value">{{ getStatusText(monitorData.systemStatus) }}</div>
          </div>
        </el-card>

        <!-- 最近更新时间 -->
        <el-card class="metric-card">
          <div class="metric-title">
            <span>最近更新时间</span>
            <el-icon><Clock /></el-icon>
          </div>
          <div class="metric-value-large">{{ formatDateTime(monitorData.updatedAt) }}</div>
        </el-card>

        <!-- 再平衡周期 -->
        <el-card class="metric-card">
          <div class="metric-title">
            <span>再平衡周期</span>
            <el-icon><Timer /></el-icon>
          </div>
          <div class="metric-value-large">{{ getRebalancePeriodText(monitorData.rebalancePeriod) }}</div>
        </el-card>

        <!-- 风险等级 -->
        <el-card class="metric-card">
          <div class="metric-title">
            <span>风险等级</span>
            <el-icon><Warning /></el-icon>
          </div>
          <div class="metric-value-large">{{ getRiskLevelText(monitorData.riskLevel) }}</div>
        </el-card>
      </div>
      
      <!-- 图表和预警区域 -->
      <div class="content-grid">
        <!-- 预警中心 -->
        <el-card class="warning-card">
          <div class="card-title">
            <el-icon><Bell /></el-icon>
            <span>预警中心</span>
          </div>
          <div class="warning-container">
            <div v-if="warnings.length === 0" class="empty-warnings">
              <el-empty description="暂无预警信息" :image-size="80" />
            </div>
            <ul v-else class="warning-list">
              <li v-for="(warning, index) in warnings" :key="index" 
                  class="warning-item" :class="'warning-level-' + warning.level">
                <div class="warning-icon">
                  <el-icon><WarningFilled /></el-icon>
                </div>
                <div class="warning-content">
                  <div class="warning-title">
                    {{ warning.title }}
                    <el-tag size="small" :type="getWarningType(warning.level)">
                      {{ getWarningLevelText(warning.level) }}
                    </el-tag>
                  </div>
                  <div class="warning-time">{{ warning.time }} | {{ warning.description }}</div>
                </div>
              </li>
            </ul>
          </div>
          <div class="timestamp">最后更新: {{ updateTime }}</div>
        </el-card>
        
        <!-- 多维风险暴露矩阵 -->
        <el-card class="chart-card">
          <div class="card-title">
            <el-icon><DataAnalysis /></el-icon>
            <span>多维风险暴露矩阵</span>
          </div>
          <div class="chart-container" ref="heatmapChart" v-loading="loading"></div>
          <div class="heatmap-legend">
            <div class="legend-item">
              <div class="legend-color" style="background: #ffffff"></div>
              <span>无偏离</span>
            </div>
            <div class="legend-item">
              <div class="legend-color" style="background: #e6f7ff"></div>
              <span>轻微偏离</span>
            </div>
            <div class="legend-item">
              <div class="legend-color" style="background: #91d5ff"></div>
              <span>一般偏离</span>
            </div>
            <div class="legend-item">
              <div class="legend-color" style="background: #4096ff"></div>
              <span>较大偏离</span>
            </div>
            <div class="legend-item">
              <div class="legend-color" style="background: #0958d9"></div>
              <span>严重偏离</span>
            </div>
          </div>
        </el-card>
        
        <!-- 历史回测曲线 -->
        <el-card class="chart-card">
          <div class="card-title">
            <el-icon><TrendCharts /></el-icon>
            <span>历史回测曲线</span>
          </div>
          <div class="chart-container" ref="profitChart" v-loading="loading"></div>
        </el-card>
        
        <!-- 风险指标 -->
        <el-card class="chart-card">
          <div class="card-title">
            <el-icon><PieChart /></el-icon>
            <span>风险指标</span>
          </div>
          <div class="chart-container" ref="riskChart" v-loading="loading"></div>
        </el-card>
      </div>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { 
  Refresh, Clock, Timer, Warning, Bell, WarningFilled, 
  DataAnalysis, TrendCharts, PieChart 
} from '@element-plus/icons-vue'
import http from '@/modules/strategy-console/utils/http'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(true)
const error = ref(null)
const selectedStrategy = ref(null)
const strategies = ref([])

// 监控数据
const monitorData = ref({
  systemStatus: '',
  updatedAt: null,
  rebalancePeriod: '',
  riskLevel: null
})

const warnings = ref([])
const updateTime = ref('')

// 图表引用
const heatmapChart = ref(null)
const profitChart = ref(null)
const riskChart = ref(null)

// 图表实例
const heatmapInstance = ref(null)
const profitInstance = ref(null)
const riskInstance = ref(null)

// 计算属性
const systemStatusClass = computed(() => {
  const status = monitorData.value.systemStatus?.toLowerCase()
  if (status === 'running' || status === 'active') return 'running'
  if (status === 'stopped' || status === 'stop') return 'stopped'
  if (status === 'error') return 'error'
  return 'stopped'
})

// 方法
function getCurrentTime() {
  const now = new Date()
  return now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

function formatDateTime(dateTime) {
  if (!dateTime) return '-'
  try {
    // 处理 LocalDateTime 格式 (yyyy-MM-ddTHH:mm:ss 或 yyyy-MM-dd HH:mm:ss)
    let date
    if (typeof dateTime === 'string') {
      // 处理可能的格式: "2024-01-01T10:00:00" 或 "2024-01-01 10:00:00"
      const normalized = dateTime.replace(' ', 'T')
      date = new Date(normalized)
    } else {
      date = new Date(dateTime)
    }
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      return '-'
    }
    
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    })
  } catch (e) {
    console.error('日期格式化错误:', e, dateTime)
    return '-'
  }
}

function getStatusText(status) {
  if (!status || status === '') return '未知状态'
  try {
    const s = String(status).toLowerCase()
    switch (s) {
      case 'running':
      case 'active':
        return '运行中'
      case 'stopped':
      case 'stop':
        return '已停止'
      case 'paused':
        return '审核中'
      case 'error':
        return '错误'
      default:
        return '未知状态'
    }
  } catch (e) {
    console.error('状态解析错误:', e, status)
    return '未知状态'
  }
}

function getStatusTagType(status) {
  if (!status) return 'info'
  const s = status.toLowerCase()
  switch (s) {
    case 'running':
    case 'active':
      return 'success'
    case 'stopped':
    case 'stop':
      return 'info'
    case 'error':
      return 'danger'
    default:
      return 'info'
  }
}

function getRiskLevelText(level) {
  if (level === null || level === undefined || level === '') return '-'
  try {
    const levelNum = typeof level === 'number' ? level : parseInt(level)
    if (isNaN(levelNum) || levelNum < 1 || levelNum > 5) {
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
  } catch (e) {
    console.error('风险等级解析错误:', e, level)
    return String(level)
  }
}

function getRebalancePeriodText(period) {
  if (!period || period === '') return '未设置'
  try {
    const periodMap = {
      'weekly': '每周',
      'monthly': '每月',
      'quarterly': '每季度'
    }
    return periodMap[String(period).toLowerCase()] || String(period)
  } catch (e) {
    console.error('再平衡周期解析错误:', e, period)
    return String(period) || '未设置'
  }
}

async function loadStrategyList() {
  try {
    const response = await http.post('/strategy-management', {
      id: 1,
      username: route.query.username || '~'
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    
    strategies.value = (Array.isArray(response.data) ? response.data : [])
      .map(item => ({
        id: Number(item.id),  // 确保ID是数字类型
        name: item.name || '未命名策略'
      }))

    if (strategies.value.length > 0) {
      const routeStrategyId = route.query.strategyId
      const fallbackId = strategies.value[0].id
      const initialId = routeStrategyId && strategies.value.some(s => s.id == Number(routeStrategyId))
        ? Number(routeStrategyId)
        : fallbackId
      selectedStrategy.value = initialId
      updateRouteParam(initialId)
      loadMonitorData()
    } else {
      error.value = '没有可用的策略数据'
    }
  } catch (err) {
    console.error('加载策略列表失败:', err)
    error.value = '无法加载策略列表: ' + (err.response?.data?.message || err.message)
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

function updateRouteParam(strategyId = selectedStrategy.value) {
  router.replace({
    query: { ...route.query, strategyId }
  })
}

function handleStrategySelect(strategyId) {
  selectedStrategy.value = strategyId
  updateRouteParam(strategyId)
  loadMonitorData()
}

async function loadMonitorData() {
  if (!selectedStrategy.value) {
    ElMessage.warning('请先选择策略')
    return
  }
  
  loading.value = true
  error.value = null
  
  // 缓存数据，便于异步请求完成后按需渲染
  let fallbackHeatmapData = []
  let shouldRenderHeatmap = true

  try {
    const metricsPromise = http.post('/strategy-monitoring/Metrics', {
      id: Number(selectedStrategy.value)
    }).then(response => {
      const metrics = response.data || {}
      console.log('监控指标原始数据:', JSON.stringify(metrics, null, 2))
      monitorData.value = {
        systemStatus: (metrics.systemStatus || metrics.status || 'stop').toString(),
        updatedAt: metrics.updatedAt || metrics.monitorUpdatedAt || null,
        rebalancePeriod: (metrics.rebalancePeriod || 'monthly').toString(),
        riskLevel: (metrics.riskLevel !== null && metrics.riskLevel !== undefined) ? Number(metrics.riskLevel) : null
      }
      initRiskChart(metrics)
    }).catch(err => {
      console.warn('监控指标请求失败:', err)
    })

    const warningsPromise = http.post('/strategy-monitoring/Warnings', {
      id: Number(selectedStrategy.value)
    }).then(response => {
      warnings.value = (response.data || []).map(event => ({
        title: event.title || '预警信息',
        time: event.eventTime ? new Date(event.eventTime).toLocaleTimeString('zh-CN', { hour12: false }) : '-',
        description: event.description || '',
        level: (event.riskLevel || 'low').toLowerCase(),
        resolved: event.resolved || false
      }))
    }).catch(err => {
      console.warn('预警数据请求失败:', err)
    })

    const profitPromise = http.post('/strategy-monitoring/ProfitCurve', {
      id: Number(selectedStrategy.value)
    }).then(async response => {
      await nextTick()
      initProfitChart(response.data || [])
    }).catch(err => {
      console.warn('回测曲线请求失败:', err)
    })

    const heatmapPromise = http.post('/strategy-monitoring/Heatmap', {
      id: Number(selectedStrategy.value)
    }).then(async response => {
      fallbackHeatmapData = response.data || []
      if (shouldRenderHeatmap) {
        await nextTick()
        initHeatmapChart(fallbackHeatmapData)
      }
    }).catch(err => {
      console.warn('热力图数据请求失败:', err)
    })

    const riskMatrixPromise = http.post('/strategy-monitoring/RiskExposureMatrix', {
      id: Number(selectedStrategy.value)
    }).then(async response => {
      if (response.data && response.data.success && response.data.data) {
        shouldRenderHeatmap = false
        await nextTick()
        initRiskExposureMatrixChart(response.data.data)
      } else {
        console.warn('风险暴露矩阵数据为空，回退至热力图')
        if (fallbackHeatmapData.length > 0) {
          await nextTick()
          initHeatmapChart(fallbackHeatmapData)
        }
      }
    }).catch(async err => {
      console.warn('风险暴露矩阵请求失败:', err)
      if (fallbackHeatmapData.length > 0) {
        await nextTick()
        initHeatmapChart(fallbackHeatmapData)
      }
    })

    // 等待所有请求完成（不论成功或失败）
    await Promise.allSettled([
      metricsPromise,
      warningsPromise,
      profitPromise,
      heatmapPromise,
      riskMatrixPromise
    ])

    updateTime.value = getCurrentTime()
  } catch (err) {
    console.error('加载监控数据失败:', err)
    error.value = '加载监控数据失败: ' + (err.response?.data?.message || err.message)
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

function refreshData() {
  if (!selectedStrategy.value) {
    ElMessage.warning('请先选择策略')
    return
  }
  loadMonitorData()
}

// 图表方法
async function initCharts() {
  await nextTick()
  
  if (heatmapInstance.value) heatmapInstance.value.dispose()
  if (profitInstance.value) profitInstance.value.dispose()
  if (riskInstance.value) riskInstance.value.dispose()
  
  heatmapInstance.value = heatmapChart.value ? echarts.init(heatmapChart.value) : null
  profitInstance.value = profitChart.value ? echarts.init(profitChart.value) : null
  riskInstance.value = riskChart.value ? echarts.init(riskChart.value) : null
  
  window.addEventListener('resize', handleResize)
}

function handleResize() {
  if (heatmapInstance.value) heatmapInstance.value.resize()
  if (profitInstance.value) profitInstance.value.resize()
  if (riskInstance.value) riskInstance.value.resize()
}

// 初始化风险暴露矩阵图表
function initRiskExposureMatrixChart(matrixData) {
  if (!matrixData || !heatmapInstance.value) {
    return
  }
  
  const funds = matrixData.funds || []
  const metrics = matrixData.metrics || {}
  const baseline = matrixData.baseline || {}
  
  if (funds.length === 0) {
    return
  }
  
  // 纵轴：仓位、收益、回撤
  const dimensions = ['仓位', '收益', '回撤']
  const dimensionKeys = ['position', 'return', 'drawdown']
  
  // 横轴：基金名称（截断显示）
  const fundNames = funds.map(f => {
    const name = f.name || f.code || '未知'
    return name.length > 8 ? name.substring(0, 8) + '...' : name
  })
  
  // 构建热力图数据
  const data = []
  for (let i = 0; i < dimensions.length; i++) {
    const metricKey = dimensionKeys[i]
    const metricValues = metrics[metricKey] || []
    
    for (let j = 0; j < fundNames.length; j++) {
      const value = metricValues[j] || 0
      data.push([j, i, parseFloat(value)])
    }
  }
  
  // 计算数值范围
  const allValues = Object.values(metrics).flat().map(v => parseFloat(v) || 0)
  const minValue = Math.min(...allValues, 0)
  const maxValue = Math.max(...allValues, 0)
  
  const option = {
    tooltip: {
      position: 'top',
      formatter: function(params) {
        const fundName = fundNames[params.value[0]]
        const dimension = dimensions[params.value[1]]
        const value = params.value[2]
        const baselineValue = baseline[dimensionKeys[params.value[1]]] || 0
        return `${fundName}<br>${dimension}差值: ${value.toFixed(4)}<br>基准值: ${baselineValue.toFixed(4)}`
      }
    },
    grid: {
      top: '10%',
      left: '15%',
      right: '10%',
      bottom: '20%'
    },
    xAxis: {
      type: 'category',
      data: fundNames,
      splitArea: { show: true },
      axisLabel: {
        interval: 0,
        rotate: 45,
        fontSize: 10
      }
    },
    yAxis: {
      type: 'category',
      data: dimensions,
      splitArea: { show: true }
    },
    visualMap: {
      min: minValue,
      max: maxValue,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '5%',
      inRange: {
        color: ['#ffffff', '#e6f7ff', '#91d5ff', '#4096ff', '#0958d9']
      },
      text: ['高', '低'],
      textStyle: {
        color: '#333'
      }
    },
    series: [{
      name: '风险暴露',
      type: 'heatmap',
      data: data,
      label: {
        show: true,
        formatter: function(params) {
          const value = params.value[2]
          return value.toFixed(2)
        },
        color: '#333',
        fontSize: 10
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  
  heatmapInstance.value.setOption(option)
}

// 初始化原始热力图（备用）
function initHeatmapChart(heatmapData) {
  if (!heatmapData || heatmapData.length === 0 || !heatmapInstance.value) {
    return
  }
  
  const industries = [...new Set(heatmapData.map(item => item.industry))]
  const dimensions = [...new Set(heatmapData.map(item => item.comparisonDimension))]
  
  const data = []
  for (let i = 0; i < industries.length; i++) {
    for (let j = 0; j < dimensions.length; j++) {
      const item = heatmapData.find(
        d => d.industry === industries[i] && d.comparisonDimension === dimensions[j]
      )
      if (item) {
        data.push([i, j, parseFloat(item.deviationValue)])
      }
    }
  }
  
  const option = {
    tooltip: {
      position: 'top',
      formatter: function(params) {
        return `${dimensions[params.value[1]]}在${industries[params.value[0]]}的<br>偏离度: ${params.value[2]}%`
      }
    },
    grid: {
      top: '10%',
      left: '10%',
      right: '10%',
      bottom: '15%'
    },
    xAxis: {
      type: 'category',
      data: industries,
      splitArea: { show: true },
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'category',
      data: dimensions,
      splitArea: { show: true }
    },
    visualMap: {
      min: Math.min(...heatmapData.map(item => item.deviationValue)) - 1,
      max: Math.max(...heatmapData.map(item => item.deviationValue)) + 1,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '5%',
      inRange: {
        color: ['#ffffff', '#e6f7ff', '#91d5ff', '#4096ff', '#0958d9']
      }
    },
    series: [{
      name: '偏离程度',
      type: 'heatmap',
      data: data,
      label: {
        show: true,
        formatter: '{c}%',
        color: '#333'
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  
  heatmapInstance.value.setOption(option)
}

function initProfitChart(profitData) {
  if (!profitData || profitData.length === 0 || !profitInstance.value) {
    return
  }
  
  const dates = profitData.map(item => {
    const date = new Date(item.pointDate)
    return `${date.getMonth() + 1}/${date.getDate()}`
  })
  
  const values = profitData.map(item => parseFloat(item.netValue || item.value || 0))
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br>净值: {c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: {
        lineStyle: { color: '#909399' }
      }
    },
    yAxis: {
      type: 'value',
      min: values.length > 0 ? Math.min(...values) * 0.95 : 0,
      axisLine: {
        show: true,
        lineStyle: { color: '#909399' }
      },
      splitLine: {
        lineStyle: { type: 'dashed' }
      }
    },
    series: [{
      name: '预计净值',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: values,
      lineStyle: {
        width: 3,
        color: '#1e9fff'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(30, 159, 255, 0.4)' },
          { offset: 1, color: 'rgba(30, 159, 255, 0.05)' }
        ])
      },
      markPoint: {
        data: [
          { type: 'max', name: '最大值' },
          { type: 'min', name: '最小值' }
        ]
      }
    }]
  }
  
  profitInstance.value.setOption(option)
}

function initRiskChart(metrics) {
  if (!riskInstance.value) return

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}% ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      data: ['市场风险', '信用风险', '流动性风险', '操作风险', '模型风险'],
      textStyle: { color: '#606266' }
    },
    series: [{
      name: '风险分布',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: '16',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        { value: 35, name: '市场风险', itemStyle: { color: '#36cfc9' } },
        { value: 25, name: '信用风险', itemStyle: { color: '#69c0ff' } },
        { value: 18, name: '流动性风险', itemStyle: { color: '#1e9fff' } },
        { value: 12, name: '操作风险', itemStyle: { color: '#597ef7' } },
        { value: 10, name: '模型风险', itemStyle: { color: '#0958d9' } }
      ]
    }]
  }

  riskInstance.value.setOption(option)
}

function getWarningType(level) {
  switch(level) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    case 'low': return 'info'
    default: return 'info'
  }
}

function getWarningLevelText(level) {
  switch(level) {
    case 'high': return '高'
    case 'medium': return '中'
    case 'low': return '低'
    default: return '未知'
  }
}

// 生命周期钩子
onMounted(() => {
  watch(() => route.query.strategyId, (newStrategyId) => {
    if (newStrategyId && strategies.value.some(s => s.id == Number(newStrategyId))) {
      selectedStrategy.value = Number(newStrategyId)
      loadMonitorData()
    }
  })
  
  initCharts()
  loadStrategyList()
  
  // 定时更新时间
  setInterval(() => {
    updateTime.value = getCurrentTime()
  }, 1000)
})

onBeforeUnmount(() => {
  if (heatmapInstance.value) heatmapInstance.value.dispose()
  if (profitInstance.value) profitInstance.value.dispose()
  if (riskInstance.value) riskInstance.value.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.monitor-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
}

.monitor-header {
  background-color: #ffffff;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #909399;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.status-indicator.running {
  background-color: #1e9fff;
  animation: pulse 1.5s infinite;
}

.status-indicator.stopped {
  background-color: #909399;
}

.status-indicator.error {
  background-color: #f56c6c;
  animation: blink 1s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.95); opacity: 0.8; }
  50% { transform: scale(1.05); opacity: 1; }
  100% { transform: scale(0.95); opacity: 0.8; }
}

@keyframes blink {
  0% { opacity: 0.4; }
  50% { opacity: 1; }
  100% { opacity: 0.4; }
}

.text-lg {
  font-size: 18px;
}

.font-bold {
  font-weight: 600;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-controls .el-select {
  min-width: 200px;
}

.monitor-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.metric-card {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  transition: all 0.3s;
  border-left: 4px solid #1e9fff;
}

.metric-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.metric-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-card {
  text-align: center;
}

.status-indicator-large {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin: 0 auto 12px;
  background-color: #909399;
}

.status-indicator-large.running {
  background-color: #1e9fff;
  animation: pulse 1.5s infinite;
}

.status-indicator-large.stopped {
  background-color: #909399;
}

.status-indicator-large.error {
  background-color: #f56c6c;
  animation: blink 1s infinite;
}

.metric-value {
  font-size: 24px;
  font-weight: 600;
  color: #1e2a3a;
}

.metric-value-large {
  font-size: 32px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 12px 0;
}

.metric-subtext {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-auto-rows: minmax(300px, auto);
  gap: 20px;
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-card, .warning-card {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-container {
  flex: 1;
  min-height: 250px;
}

.warning-container {
  background-color: #fdf6ec;
  border-radius: 6px;
  padding: 12px;
  height: 220px;
  overflow-y: auto;
  position: relative;
  border: 1px solid #faecd8;
}

.empty-warnings {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.warning-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.warning-item {
  padding: 12px 16px;
  background-color: #ffffff;
  margin-bottom: 10px;
  border-radius: 6px;
  display: flex;
  align-items: flex-start;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  border-left: 4px solid transparent;
}

.warning-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.warning-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #fef6e9;
  color: #e6a23c;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.warning-level-high .warning-icon {
  background-color: #fef0f0;
  color: #f56c6c;
}

.warning-level-medium .warning-icon {
  background-color: #fef6e9;
  color: #e6a23c;
}

.warning-level-low .warning-icon {
  background-color: #f0f9eb;
  color: #67c23a;
}

.warning-content {
  flex: 1;
}

.warning-title {
  font-weight: 600;
  font-size: 14px;
  color: #1e2a3a;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.warning-time {
  font-size: 12px;
  color: #909399;
}

.timestamp {
  text-align: right;
  color: #909399;
  font-size: 12px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #ebeef5;
}

.warning-level-high {
  border-left-color: #f56c6c;
}

.warning-level-medium {
  border-left-color: #e6a23c;
}

.warning-level-low {
  border-left-color: #67c23a;
}

.heatmap-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #606266;
}

.legend-color {
  width: 20px;
  height: 4px;
  border-radius: 2px;
}
</style>
