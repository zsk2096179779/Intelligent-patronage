<template>
  <div class="factor-validation-management">
    <!-- 页面标题和统计 -->
    <el-card shadow="never" class="header-card">
      <div class="header-section">
        <div class="title-area">
          <h2>因子检验管理</h2>
          <p class="subtitle">创建因子检验任务，计算IC/IR指标，评估因子有效性</p>
        </div>
        <div class="statistics-area">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.totalTasks || 0 }}</div>
            <div class="stat-label">检验任务总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statistics.runningTasks || 0 }}</div>
            <div class="stat-label">运行中</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statistics.completedTasks || 0 }}</div>
            <div class="stat-label">已完成</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statistics.failedTasks || 0 }}</div>
            <div class="stat-label">失败</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 操作栏 -->
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <div class="left-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索任务ID或因子名称"
            clearable
            style="width: 300px;"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select
            v-model="statusFilter"
            placeholder="任务状态"
            clearable
            style="width: 150px; margin-left: 12px;"
          >
            <el-option label="全部状态" value="" />
            <el-option label="待执行" value="PENDING" />
            <el-option label="运行中" value="RUNNING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </div>
        <div class="right-actions">
          <el-button type="primary" @click="showCreateTaskDialog = true">
            <el-icon><Plus /></el-icon>
            创建检验任务
          </el-button>
          <el-button @click="loadTasksFromFactorHistory">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 任务列表 -->
    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="filteredTaskList"
        border
        stripe
        height="600"
        :empty-text="loading ? '加载中...' : '暂无数据'"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="taskId" label="任务ID" width="80" />
        <el-table-column prop="taskName" label="任务名称" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="viewTaskDetail(row)">
              {{ row.taskName || `任务-${row.taskId}` }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="factorCount" label="因子数量" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.factorCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dateRange" label="检验期间" min-width="200">
          <template #default="{ row }">
            {{ row.startDate }} ~ {{ row.endDate }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="150">
          <template #default="{ row }">
            <el-progress
              :percentage="row.progress || 0"
              :status="getProgressStatus(row.status)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button-group size="small">
              <el-button text type="info" @click="viewTaskDetail(row)">
                <el-icon><Document /></el-icon>
                详情
              </el-button>
              <el-button
                text
                type="danger"
                :disabled="row.status !== 'COMPLETED'"
                @click="viewIcVisualization(row)"
              >
                IC可视化
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

    <!-- 创建检验任务对话框 -->
    <el-dialog
      v-model="showCreateTaskDialog"
      title="创建检验任务"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form :model="taskForm" :rules="taskFormRules" ref="taskFormRef" label-width="120px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input
            v-model="taskForm.taskName"
            placeholder="请输入任务名称（可选）"
          />
        </el-form-item>

        <el-form-item label="选择因子" prop="factorIds">
          <div style="width: 100%;">
            <el-select
              v-model="taskForm.factorIds"
              multiple
              filterable
              placeholder="请选择要检验的因子"
              style="width: 100%;"
            >
              <el-option
                v-for="factor in availableFactors"
                :key="factor.factorId"
                :label="`${factor.factorName} (${factor.factorCode})`"
                :value="factor.factorId"
              />
            </el-select>
            <div class="form-item-tip">
              已选择 {{ taskForm.factorIds.length }} 个因子
              <el-button text type="primary" size="small" @click="showCheckFactorDialog = true">
                检查因子状态
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="检验期间" prop="dateRange">
          <el-date-picker
            v-model="taskForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="检验类型">
          <el-checkbox-group v-model="taskForm.validationTypes">
            <el-checkbox label="IC" value="IC">信息系数 (IC)</el-checkbox>
            <el-checkbox label="IR" value="IR">信息比率 (IR)</el-checkbox>
            <el-checkbox label="RANK_IC" value="RANK_IC">排序IC</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="计算频率">
          <el-radio-group v-model="taskForm.frequency">
            <el-radio label="DAILY">日度</el-radio>
            <el-radio label="WEEKLY">周度</el-radio>
            <el-radio label="MONTHLY">月度</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="taskForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateTaskDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTask" :loading="creating">
          创建并执行
        </el-button>
      </template>
    </el-dialog>

    <!-- 任务状态对话框 -->
    <el-dialog
      v-model="showStatusDialog"
      title="任务状态"
      width="600px"
    >
      <div v-loading="statusLoading">
        <el-descriptions :column="2" border v-if="currentTaskStatus">
          <el-descriptions-item label="任务ID">{{ currentTaskStatus.taskId }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentTaskStatus.status)">
              {{ getStatusLabel(currentTaskStatus.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="进度" :span="2">
            <el-progress
              :percentage="currentTaskStatus.progress || 0"
              :status="getProgressStatus(currentTaskStatus.status)"
            />
          </el-descriptions-item>
          <el-descriptions-item label="因子数量">
            {{ currentTaskStatus.factorCount }}
          </el-descriptions-item>
          <el-descriptions-item label="已完成">
            {{ currentTaskStatus.completedCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">
            {{ currentTaskStatus.startTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间">
            {{ currentTaskStatus.endTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="消息" :span="2">
            {{ currentTaskStatus.message || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showStatusDialog = false">关闭</el-button>
        <el-button
          v-if="currentTaskStatus && currentTaskStatus.status === 'RUNNING'"
          type="primary"
          @click="refreshTaskStatus"
        >
          刷新状态
        </el-button>
      </template>
    </el-dialog>

    <!-- 任务结果对话框 -->
    <el-dialog
      v-model="showResultsDialog"
      title="检验结果"
      width="1000px"
      :close-on-click-modal="false"
    >
      <div v-loading="resultsLoading">
        <!-- 汇总信息 -->
        <el-alert
          v-if="currentTaskResults"
          :title="`检验完成，共 ${currentTaskResults.factorCount} 个因子`"
          type="success"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div>检验期间: {{ currentTaskResults.startDate }} ~ {{ currentTaskResults.endDate }}</div>
            <div v-if="currentTaskResults.avgIC !== undefined">
              平均IC: {{ currentTaskResults.avgIC?.toFixed(4) }}
            </div>
            <div v-if="currentTaskResults.avgIR !== undefined">
              平均IR: {{ currentTaskResults.avgIR?.toFixed(4) }}
            </div>
          </template>
        </el-alert>

        <div
          v-if="currentTaskResults && currentTaskResults.results && currentTaskResults.results.length > 0"
          class="chart-container"
        >
          <h4>IC / IR 可视化</h4>
          <div ref="taskResultsChartRef" class="chart-canvas"></div>
        </div>

        <!-- 结果表格 -->
        <el-table
          v-if="currentTaskResults && currentTaskResults.results"
          :data="currentTaskResults.results"
          border
          max-height="500"
        >
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="factorId" label="因子ID" width="80" />
          <el-table-column prop="factorName" label="因子名称" min-width="150" />
          <el-table-column prop="ic" label="IC值" width="100" sortable>
            <template #default="{ row }">
              <span :class="getICClass(row.ic)">
                {{ row.ic?.toFixed(4) || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="ir" label="IR值" width="100" sortable>
            <template #default="{ row }">
              <span :class="getIRClass(row.ir)">
                {{ row.ir?.toFixed(4) || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="rankIC" label="排序IC" width="100" sortable>
            <template #default="{ row }">
              {{ row.rankIC?.toFixed(4) || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="winRate" label="胜率" width="100">
            <template #default="{ row }">
              {{ row.winRate ? `${(row.winRate * 100).toFixed(2)}%` : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="评级" width="100">
            <template #default="{ row }">
              <el-tag :type="getRatingTagType(row.rating)">
                {{ getRatingLabel(row.rating) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button text type="primary" size="small" @click="viewFactorHistory(row)">
                历史记录
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-else description="暂无结果数据" />
      </div>
      <template #footer>
        <el-button @click="showResultsDialog = false">关闭</el-button>
        <el-button type="primary" @click="exportResults">
          <el-icon><Download /></el-icon>
          导出结果
        </el-button>
      </template>
    </el-dialog>

    <!-- 所有因子历史记录展示区域 -->
    <el-card shadow="never" class="history-card">
      <template #header>
        <div class="card-header">
          <span>因子历史检验记录概览</span>
        </div>
      </template>
      <div v-if="allFactorsHistory.length > 0" class="history-overview">
        <el-tabs v-model="activeHistoryTab" type="border-card" closable>
          <el-tab-pane
            v-for="item in allFactorsHistory"
            :key="item.factorId"
            :label="`${item.factorName} (${item.history.length}条)`"
            :name="item.factorId.toString()"
          >
            <el-table
              :data="item.history"
              border
              max-height="400"
            >
              <el-table-column type="index" label="序号" width="60" />
              <el-table-column prop="taskId" label="任务ID" width="80" />
              <el-table-column prop="dateRange" label="检验期间" min-width="200">
                <template #default="{ row }">
                  {{ row.calculationDate || (row.startDate ? `${row.startDate} ~ ${row.endDate}` : '-') }}
                </template>
              </el-table-column>
              <el-table-column prop="ic" label="IC值" width="100">
                <template #default="{ row }">
                  <span :class="getICClass(row.ic)">
                    {{ row.ic?.toFixed(4) || '-' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="ir" label="IR值" width="100">
                <template #default="{ row }">
                  <span :class="getIRClass(row.ir)">
                    {{ row.ir?.toFixed(4) || '-' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="检验时间" width="160" />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
      <el-empty v-else description="暂无因子历史记录概览" />
    </el-card>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="任务详情"
      width="700px"
    >
      <el-descriptions :column="2" border v-if="currentTask">
        <el-descriptions-item label="任务ID">{{ currentTask.taskId }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">
          {{ currentTask.taskName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentTask.status)">
            {{ getStatusLabel(currentTask.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="因子数量">
          {{ currentTask.factorCount }}
        </el-descriptions-item>
        <el-descriptions-item label="检验期间" :span="2">
          {{ currentTask.startDate }} ~ {{ currentTask.endDate }}
        </el-descriptions-item>
        <el-descriptions-item label="检验类型" :span="2">
          {{ currentTask.validationTypes?.join(', ') || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="计算频率">
          {{ getFrequencyLabel(currentTask.frequency || 'DAILY') }}
        </el-descriptions-item>
        <el-descriptions-item label="进度">
          {{ currentTask.progress || 0 }}%
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ currentTask.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="完成时间">
          {{ currentTask.endTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentTask.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 任务结果显示 -->
      <div v-if="currentTask.status === 'COMPLETED'" class="mt-4">
        <el-divider>IC/IR计算结果</el-divider>

        <div v-loading="resultsLoading" class="task-results-container">
          <div v-if="currentTaskResults" class="mb-4">
            <div class="stats-row">
              <el-statistic
                v-if="currentTaskResults.avgIC !== undefined"
                title="平均IC"
                :value="currentTaskResults.avgIC"
                :precision="4"
                :value-style="{ color: getICColor(currentTaskResults.avgIC) }"
              />
              <el-statistic
                v-if="currentTaskResults.avgIR !== undefined"
                title="平均IR"
                :value="currentTaskResults.avgIR"
                :precision="4"
                :value-style="{ color: getICColor(currentTaskResults.avgIR) }"
              />
            </div>
          </div>

          <!-- 因子结果表格 -->
          <el-table
            v-if="currentTaskResults && currentTaskResults.results && currentTaskResults.results.length > 0"
            :data="currentTaskResults.results"
            border
            style="width: 100%"
          >
            <el-table-column prop="factorId" label="因子ID" width="80" />
            <el-table-column prop="factorName" label="因子名称" min-width="150" />
            <el-table-column prop="ic" label="IC值" width="100">
              <template #default="{ row }">
                <span :class="getICClass(row.ic)">
                  {{ row.ic?.toFixed(4) || '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="ir" label="IR值" width="100">
              <template #default="{ row }">
                <span :class="getICClass(row.ir)">
                  {{ row.ir?.toFixed(4) || '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="icStd" label="IC标准差" width="120">
              <template #default="{ row }">
                {{ row.icStd?.toFixed(4) || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="icPositiveRatio" label="IC正比率" width="120">
              <template #default="{ row }">
                {{ row.icPositiveRatio !== undefined ? (row.icPositiveRatio * 100).toFixed(2) + '%' : '-' }}
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-else-if="!resultsLoading" description="暂无IC/IR计算结果" />
        </div>
      </div>
    </el-dialog>

    <!-- 检查因子状态对话框 -->
    <el-dialog
      v-model="showCheckFactorDialog"
      title="检查因子状态"
      width="600px"
    >
      <div v-loading="checkingFactors">
        <el-table
          v-if="factorStatusList.length > 0"
          :data="factorStatusList"
          border
          max-height="400"
        >
          <el-table-column prop="factorId" label="因子ID" width="80" />
          <el-table-column prop="factorName" label="因子名称" min-width="150" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isValid ? 'success' : 'danger'">
                {{ row.isValid ? '有效' : '无效' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="说明" min-width="150" />
        </el-table>
        <el-empty v-else description="暂无数据" />
      </div>
      <template #footer>
        <el-button @click="showCheckFactorDialog = false">关闭</el-button>
        <el-button type="primary" @click="handleCheckFactorStatus">
          重新检查
        </el-button>
      </template>
    </el-dialog>

    <!-- 因子历史记录对话框 -->
    <el-dialog
      v-model="showHistoryDialog"
      title="因子历史检验记录"
      width="900px"
    >
      <div v-loading="historyLoading">
        <el-table
          v-if="factorHistory.length > 0"
          :data="factorHistory"
          border
          max-height="500"
        >
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="taskId" label="任务ID" width="80" />
          <el-table-column prop="dateRange" label="检验期间" min-width="200">
            <template #default="{ row }">
              {{ row.startDate }} ~ {{ row.endDate }}
            </template>
          </el-table-column>
          <el-table-column prop="ic" label="IC值" width="100">
            <template #default="{ row }">
              <span :class="getICClass(row.ic)">
                {{ row.ic?.toFixed(4) || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="ir" label="IR值" width="100">
            <template #default="{ row }">
              <span :class="getIRClass(row.ir)">
                {{ row.ir?.toFixed(4) || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="检验时间" width="160" />
        </el-table>
        <el-empty v-else description="暂无历史记录" />
      </div>
    </el-dialog>

    <!-- 分层回测对话框 -->
    <el-dialog
      v-model="showLayeredBacktestDialog"
      title="分层回测结果"
      width="1200px"
      :close-on-click-modal="false"
    >
      <div v-loading="layeredBacktestLoading">
        <div v-if="layeredBacktestData">
          <el-descriptions :column="2" border style="margin-bottom: 20px;">
            <el-descriptions-item label="任务ID">{{ layeredBacktestData.taskId }}</el-descriptions-item>
            <el-descriptions-item label="因子">{{ layeredBacktestData.factorName || '-' }}</el-descriptions-item>
          </el-descriptions>

          <div v-if="layeredBacktestData.cumulativeReturns" class="chart-container">
            <h4>分位数累计收益率曲线</h4>
            <div ref="layeredChartRef" class="chart-canvas"></div>
          </div>
          <el-empty v-else description="暂无曲线数据" />
        </div>
        <el-empty v-else description="暂无分层回测数据" />
      </div>
      <template #footer>
        <el-button @click="showLayeredBacktestDialog = false">关闭</el-button>
        <el-button
          type="primary"
          @click="executeLayeredBacktest"
          :loading="layeredBacktestExecuting"
        >
          执行分层回测
        </el-button>
      </template>
    </el-dialog>

    <!-- IC检验可视化对话框 -->
    <el-dialog
      v-model="showIcVisualizationDialog"
      title="IC检验可视化"
      width="1200px"
      :close-on-click-modal="false"
    >
      <div v-loading="icVisualizationLoading">
        <div v-if="icVisualizationData">
          <el-descriptions :column="2" border style="margin-bottom: 20px;">
            <el-descriptions-item label="任务ID">{{ icVisualizationData.taskId }}</el-descriptions-item>
            <el-descriptions-item label="因子">{{ icVisualizationData.factorName || '-' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 因子选择器 -->
          <el-select
            v-model="selectedFactorId"
            placeholder="选择因子"
            style="width: 200px; margin-bottom: 20px;"
            @change="loadIcVisualizationData"
          >
            <el-option
              v-for="factor in currentTaskResults?.results || []"
              :key="factor.factorId"
              :label="factor.factorName"
              :value="factor.factorId"
            />
          </el-select>

          <!-- IC检验可视化图表（占位，实际项目中使用图表库如ECharts） -->
          <div v-if="icVisualizationData.icTearSheetData?.icSequenceData" class="chart-container">
            <h4>IC 序列</h4>
            <div ref="icSequenceChartRef" class="chart-canvas"></div>
          </div>
          <div v-else class="chart-container">
            <el-empty description="暂无IC序列数据" />
          </div>

          <div v-if="icVisualizationData.quantileAnnualizedReturns" class="chart-container">
            <h4>分位数平均年化收益率</h4>
            <div ref="quantileAnnualizedChartRef" class="chart-canvas"></div>
          </div>
          <div v-else class="chart-container">
            <el-empty description="暂无年化收益率数据" />
          </div>
        </div>
        <el-empty v-else description="暂无IC可视化数据" />
      </div>
      <template #footer>
        <el-button @click="showIcVisualizationDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  View,
  DataAnalysis,
  Document,
  Download
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import * as echarts from 'echarts'

// ========== 类型定义 ==========
interface ApiResponse<T = unknown> {
  code: number
  message?: string
  data?: T
  success?: boolean
}

interface ValidationTask {
  taskId: number
  taskName?: string
  factorIds: number[]
  factorCount: number
  completedCount?: number
  startDate: string
  endDate: string
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED'
  progress?: number
  validationTypes?: string[]
  frequency?: string
  remark?: string
  createTime: string
  startTime?: string
  endTime?: string
  message?: string
}

interface ValidationResult {
  factorId: number
  factorName: string
  ic?: number
  ir?: number
  rankIC?: number
  winRate?: number
  rating?: 'EXCELLENT' | 'GOOD' | 'FAIR' | 'POOR'
}

interface TaskResults {
  taskId: number
  factorCount: number
  startDate: string
  endDate: string
  avgIC?: number
  avgIR?: number
  results: ValidationResult[]
}

interface FactorStatus {
  factorId: number
  factorName: string
  isValid: boolean
  message: string
}

interface FactorOption {
  factorId: number
  factorCode: string
  factorName: string
}

interface Statistics {
  totalTasks: number
  runningTasks: number
  completedTasks: number
  failedTasks: number
}

interface FactorTreeScene {
  sceneId: string;
  sceneName: string;
  sceneDesc?: string;
}

interface FactorTree {
  treeId?: number;  // 驼峰命名（前端使用）
  treeid?: number;  // 后端返回的字段名
  id?: number;      // 兼容其他可能的字段名
  treeName?: string;
  nodeName?: string;  // 后端可能使用 nodeName
  description?: string;
  sceneId: string;
  parentId?: number;
  nodeType?: string;
  factorId?: number;
  factorCode?: string;
  children?: FactorTree[];
}

interface FactorBaseItem {
  baseId: number;
  factorName?: string;
  factorCode?: string;
  isValid?: boolean | number;
}

// 后端FactorValidationResponse对应的接口定义
interface FactorValidationResponse {
  success: boolean
  message?: string
  taskId?: number
  factorCount?: number
  completedCount?: number
  progress?: number
  status?: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED'
  startTime?: string
  endTime?: string
  taskInfo?: ValidationTask
  tasks?: ValidationTask[]
  results?: ValidationResult[]
  avgIC?: number
  avgIR?: number
  // 其他可能的字段
}

// 分层回测响应接口定义
interface LayeredBacktestResponse {
  success: boolean
  message?: string
  taskId?: number
  factorId?: number
  factorName?: string
  // 分位数累计收益率曲线数据
  cumulativeReturns?: {
    dates: string[]
    quantiles: {
      name: string
      values: number[]
    }[]
  }
  // 其他分层回测相关数据
}

// IC检验可视化响应接口定义（匹配后端FactorIcVisualizationResponse）
interface FactorIcVisualizationResponse {
  success: boolean
  message?: string
  taskId?: number
  factorId?: number
  factorName?: string
  // IC Tear Sheet数据（Alphalens风格）
  icTearSheetData?: {
    icSequenceData?: {
      dates: string[]
      icValues: number[]
      rankIcValues?: number[]
      rollingMean?: number[]
    }
    icDistributionData?: {
      bins: number[]
      frequencies: number[]
      mean?: number
      std?: number
    }
    icStatisticsTable?: {
      mean: number
      std: number
      tStat: number
      pValue: number
      winRate: number
      rankMean?: number
      rankStd?: number
      rankTStat?: number
      rankPValue?: number
      rankWinRate?: number
    }
    icRollingStatistics?: {
      rollingMean: number[]
      rollingStd: number[]
      windowSize: number
    }
  }
  // 分位数累计收益率数据
  quantileCumulativeReturns?: {
    dates: string[]
    quantiles: {
      name: string
      values: number[]
    }[]
  }
  // 分位数平均年化收益率数据
  quantileAnnualizedReturns?: {
    quantiles: string[]
    returns: number[]
  }
  // 其他IC可视化相关数据
}

// 状态管理 ==========
const API_BASE = '/api/factor/validation'
const FACTOR_BASE_API = '/api/factor/base'

// 搜索和筛选
const searchKeyword = ref<string>('')
const statusFilter = ref<string>('')
// 激活的历史记录标签页
const activeHistoryTab = ref<string>('')

// 任务列表
const taskList = ref<ValidationTask[]>([])
const loading = ref<boolean>(false)

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 统计信息
const statistics = reactive<Statistics>({
  totalTasks: 0,
  runningTasks: 0,
  completedTasks: 0,
  failedTasks: 0
})

// 对话框
const showCreateTaskDialog = ref<boolean>(false)
const showStatusDialog = ref<boolean>(false)
const showResultsDialog = ref<boolean>(false)
const showDetailDialog = ref<boolean>(false)
const showCheckFactorDialog = ref<boolean>(false)
const showHistoryDialog = ref<boolean>(false)

// 表单
const taskFormRef = ref<FormInstance>()
const taskForm = reactive({
  taskName: '',
  factorIds: [] as number[],
  dateRange: [] as string[],
  validationTypes: ['IC', 'IR'],
  frequency: 'DAILY',
  remark: ''
})

const taskFormRules: FormRules = {
  factorIds: [{ required: true, message: '请选择至少一个因子', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择检验期间', trigger: 'change' }]
}

// 可用因子列表
const availableFactors = ref<FactorOption[]>([])

// 当前操作的任务
const currentTask = ref<ValidationTask | null>(null)
const currentTaskStatus = ref<ValidationTask | null>(null)
const currentTaskResults = ref<TaskResults | null>(null)

// 加载状态
const creating = ref<boolean>(false)
const statusLoading = ref<boolean>(false)
const resultsLoading = ref<boolean>(false)
const checkingFactors = ref<boolean>(false)
const historyLoading = ref<boolean>(false)

// 因子状态列表
const factorStatusList = ref<FactorStatus[]>([])

// 因子历史记录
const factorHistory = ref<ValidationResult[]>([])
// 所有因子的历史记录（用于页面加载时展示）
const allFactorsHistory = ref<{factorId: number, factorName: string, history: ValidationResult[]}[]>([])

// 分层回测相关状态
const showLayeredBacktestDialog = ref<boolean>(false)
const layeredBacktestLoading = ref<boolean>(false)
const layeredBacktestExecuting = ref<boolean>(false)
const layeredBacktestData = ref<LayeredBacktestResponse | null>(null)

// IC检验可视化相关状态
const showIcVisualizationDialog = ref<boolean>(false)
const icVisualizationLoading = ref<boolean>(false)
const icVisualizationData = ref<FactorIcVisualizationResponse | null>(null)
const selectedFactorId = ref<number | null>(null)

// 图表引用
const taskResultsChartRef = ref<HTMLDivElement | null>(null)
const icSequenceChartRef = ref<HTMLDivElement | null>(null)
const quantileAnnualizedChartRef = ref<HTMLDivElement | null>(null)
const layeredChartRef = ref<HTMLDivElement | null>(null)

let taskResultsChart: echarts.ECharts | null = null
let icSequenceChart: echarts.ECharts | null = null
let quantileAnnualizedChart: echarts.ECharts | null = null
let layeredChart: echarts.ECharts | null = null

// 自动刷新定时器
let autoRefreshTimer: ReturnType<typeof setInterval> | null = null

// ========== 图表渲染辅助 ==========
function disposeTaskResultsChart() {
  if (taskResultsChart) {
    taskResultsChart.dispose()
    taskResultsChart = null
  }
}

function disposeIcCharts() {
  if (icSequenceChart) {
    icSequenceChart.dispose()
    icSequenceChart = null
  }
  if (quantileAnnualizedChart) {
    quantileAnnualizedChart.dispose()
    quantileAnnualizedChart = null
  }
}

function disposeLayeredChart() {
  if (layeredChart) {
    layeredChart.dispose()
    layeredChart = null
  }
}

function handleWindowResize() {
  taskResultsChart?.resize()
  icSequenceChart?.resize()
  quantileAnnualizedChart?.resize()
  layeredChart?.resize()
}

async function renderTaskResultsChart() {
  if (!taskResultsChartRef.value || !currentTaskResults.value || !currentTaskResults.value.results?.length) {
    disposeTaskResultsChart()
    return
  }
  if (!taskResultsChart) {
    taskResultsChart = echarts.init(taskResultsChartRef.value)
  }
  const categories = currentTaskResults.value.results.map(result => result.factorName)
  const icValues = currentTaskResults.value.results.map(result => result.ic ?? 0)
  const irValues = currentTaskResults.value.results.map(result => result.ir ?? 0)
  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['IC', 'IR'] },
    grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
    xAxis: { type: 'category', data: categories, axisLabel: { interval: 0, rotate: categories.length > 6 ? 30 : 0 } },
    yAxis: { type: 'value', name: 'Value' },
    series: [
      { name: 'IC', type: 'bar', data: icValues, itemStyle: { color: '#67c23a' } },
      { name: 'IR', type: 'bar', data: irValues, itemStyle: { color: '#409eff' } }
    ]
  }
  taskResultsChart.setOption(option)
}

async function renderIcSequenceChart() {
  const sequenceData = icVisualizationData.value?.icTearSheetData?.icSequenceData
  if (!icSequenceChartRef.value || !sequenceData || !sequenceData.dates?.length) {
    if (icSequenceChart) {
      icSequenceChart.clear()
    }
    return
  }
  if (!icSequenceChart) {
    icSequenceChart = echarts.init(icSequenceChartRef.value)
  }
  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['IC', 'Rank IC'] },
    grid: { left: '3%', right: '4%', bottom: '8%', containLabel: true },
    xAxis: { type: 'category', data: sequenceData.dates },
    yAxis: { type: 'value', name: 'IC' },
    series: [
      { name: 'IC', type: 'line', data: sequenceData.icValues ?? [], smooth: true, showSymbol: false },
      { name: 'Rank IC', type: 'line', data: sequenceData.rankIcValues ?? [], smooth: true, showSymbol: false }
    ]
  }
  icSequenceChart.setOption(option)
}

async function renderQuantileAnnualizedChart() {
  const annualizedData = icVisualizationData.value?.quantileAnnualizedReturns
  if (!quantileAnnualizedChartRef.value || !annualizedData || !annualizedData.quantiles?.length) {
    if (quantileAnnualizedChart) {
      quantileAnnualizedChart.clear()
    }
    return
  }
  if (!quantileAnnualizedChart) {
    quantileAnnualizedChart = echarts.init(quantileAnnualizedChartRef.value)
  }
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: (params: unknown) => {
        const payload = params as { name?: string; value?: number }
        const label = payload.name ?? ''
        const rawValue = typeof payload.value === 'number' ? payload.value : Number(payload.value ?? 0)
        return `${label}: ${(rawValue * 100).toFixed(2)}%`
      }
    },
    xAxis: { type: 'category', data: annualizedData.quantiles },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => `${(value * 100).toFixed(1)}%`
      }
    },
    series: [
      {
        name: '年化收益率',
        type: 'bar',
        data: annualizedData.returns ?? [],
        itemStyle: { color: '#e6a23c' }
      }
    ]
  }
  quantileAnnualizedChart.setOption(option)
}

async function renderLayeredChart() {
  const cumulativeReturns = layeredBacktestData.value?.cumulativeReturns
  if (!layeredChartRef.value || !cumulativeReturns || !cumulativeReturns.dates?.length) {
    if (layeredChart) {
      layeredChart.clear()
    }
    return
  }
  if (!layeredChart) {
    layeredChart = echarts.init(layeredChartRef.value)
  }
  const series: echarts.SeriesOption[] = (cumulativeReturns.quantiles || []).map(quantile => ({
    name: quantile.name,
    type: 'line',
    smooth: true,
    data: quantile.values
  }))
  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'axis' },
    legend: { type: 'scroll' },
    grid: { left: '3%', right: '4%', bottom: '8%', containLabel: true },
    xAxis: { type: 'category', data: cumulativeReturns.dates },
    yAxis: { type: 'value', name: '累计收益率' },
    series
  }
  layeredChart.setOption(option)
}

watch(showResultsDialog, async (visible) => {
  if (visible && currentTaskResults.value?.results?.length) {
    await nextTick()
    renderTaskResultsChart()
  } else if (!visible) {
    disposeTaskResultsChart()
  }
})

watch(() => currentTaskResults.value?.results, async (results) => {
  if (results && showResultsDialog.value) {
    await nextTick()
    renderTaskResultsChart()
  }
})

watch(showIcVisualizationDialog, async (visible) => {
  if (visible && icVisualizationData.value) {
    await nextTick()
    renderIcSequenceChart()
    renderQuantileAnnualizedChart()
  } else if (!visible) {
    disposeIcCharts()
  }
})

watch(icVisualizationData, async (data) => {
  if (data && showIcVisualizationDialog.value) {
    await nextTick()
    renderIcSequenceChart()
    renderQuantileAnnualizedChart()
  }
})

watch(showLayeredBacktestDialog, async (visible) => {
  if (visible && layeredBacktestData.value?.cumulativeReturns) {
    await nextTick()
    renderLayeredChart()
  } else if (!visible) {
    disposeLayeredChart()
  }
})

watch(layeredBacktestData, async (data) => {
  if (data?.cumulativeReturns && showLayeredBacktestDialog.value) {
    await nextTick()
    renderLayeredChart()
  }
})

// ========== 计算属性 ==========
const filteredTaskList = computed(() => {
  let list = taskList.value

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(task =>
      task.taskId.toString().includes(keyword) ||
      task.taskName?.toLowerCase().includes(keyword)
    )
  }

  // 状态筛选
  if (statusFilter.value) {
    list = list.filter(task => task.status === statusFilter.value)
  }

  return list
})

// ========== API调用 - 分层回测和IC可视化 ==========
// 加载分层回测数据
async function loadLayeredBacktestData() {
  if (!currentTask.value) return

  layeredBacktestLoading.value = true
  try {
    // 获取分层回测数据 - 使用正确的后端接口路径
    const response = await request.get<ApiResponse<LayeredBacktestResponse>>(
      `${API_BASE}/tasks/${currentTask.value.taskId}/layered/cumulative-return`,
      {
        params: { factorId: selectedFactorId.value || undefined },
        responseType: 'json' // 明确指定期望JSON响应
      }
    )

    // 检查响应是否有效
    if (typeof response.data === 'object' && response.data !== null) {
      if (response.data.success && response.data.data) {
        layeredBacktestData.value = response.data.data
        await nextTick()
        renderLayeredChart()
      } else {
        ElMessage.error(response.data.message || '获取分层回测数据失败：后端处理错误')
        layeredBacktestData.value = null
      }
    } else {
      ElMessage.error('获取分层回测数据失败：收到非预期的响应格式')
      console.error('获取分层回测数据失败：响应数据格式错误', response.data)
      layeredBacktestData.value = null
    }
  } catch (error: unknown) {
    console.error('获取分层回测数据失败:', error)

    const responsePayload = getAxiosErrorResponse(error)
    if (responsePayload?.data && isHtmlPayload(responsePayload.data)) {
      ElMessage.error('获取分层回测数据失败：API路径可能错误或后端未正确配置')
      console.error('API返回HTML而非JSON，可能是路径错误或后端配置问题')
    } else if (responsePayload?.data) {
      const errorData = responsePayload.data
      // 特别处理"未找到分层回测结果"的情况，提供更友好的提示
      if (errorData.message && errorData.message.includes('未找到分层回测结果')) {
        ElMessage.warning(`任务ID ${currentTask.value?.taskId} 暂无分层回测数据`)
      } else {
        const errorMessage = extractResponseMessage(errorData)
        ElMessage.error(errorMessage || '获取分层回测数据失败：网络错误或服务器无响应')
      }
    } else {
      ElMessage.error('获取分层回测数据失败：网络错误或服务器无响应')
    }

    layeredBacktestData.value = null
  } finally {
    layeredBacktestLoading.value = false
  }
}

// 查看分层回测
async function viewLayeredBacktest(task: ValidationTask) {
  currentTask.value = task
  showLayeredBacktestDialog.value = true
  // 添加友好提示，告知用户正在获取数据
  ElMessage({ message: '正在获取分层回测数据，请稍候...', type: 'info' })
  await loadLayeredBacktestData()
}


// 执行分层回测
async function executeLayeredBacktest() {
  if (!currentTask.value) return

  layeredBacktestExecuting.value = true
  try {
    const response = await request.post<ApiResponse<string>>(
      `${API_BASE}/tasks/${currentTask.value.taskId}/layered/execute`,
      {}, // 空请求体
      { responseType: 'json' } // 明确指定期望JSON响应
    )

    // 检查响应是否有效
    if (typeof response.data === 'object' && response.data !== null) {
      if (response.data.success) {
        ElMessage.success(response.data.message || '分层回测执行成功')
        await loadLayeredBacktestData()
      } else {
        ElMessage.error(response.data.message || '分层回测执行失败：后端处理错误')
      }
      } else {
        ElMessage.error('分层回测执行失败：收到非预期的响应格式')
        console.error('分层回测执行失败：响应数据格式错误', response.data)
      }
  } catch (error: unknown) {
    console.error('执行分层回测失败:', error)

    const responsePayload = getAxiosErrorResponse(error)
    if (responsePayload?.data && isHtmlPayload(responsePayload.data)) {
      ElMessage.error('分层回测执行失败：API路径可能错误或后端未正确配置')
      console.error('API返回HTML而非JSON，可能是路径错误或后端配置问题')
    } else if (responsePayload?.data) {
      const errorMessage = extractResponseMessage(responsePayload.data)
      ElMessage.error(errorMessage || '分层回测执行失败：网络错误或服务器无响应')
    } else {
      ElMessage.error('分层回测执行失败：网络错误或服务器无响应')
    }
  } finally {
      layeredBacktestExecuting.value = false
    }
}

// 查看IC检验可视化
async function viewIcVisualization(task: ValidationTask) {
  // 检查任务状态
  if (task.status !== 'COMPLETED') {
    ElMessage.warning('该任务尚未完成，无法查看IC可视化数据。请等待任务完成后重试。')
    return
  }

  currentTask.value = task
  showIcVisualizationDialog.value = true

  // 先加载任务结果，确保有IC/IR数据
  if (!currentTaskResults.value || currentTaskResults.value.taskId !== task.taskId) {
    resultsLoading.value = true
    try {
      const response = await request.get<ApiResponse<FactorValidationResponse>>(
        `${API_BASE}/tasks/${task.taskId}/results`,
        { responseType: 'json' }
      )

      if (response.data.success && response.data.data) {
        const data = response.data.data
        currentTaskResults.value = {
          taskId: task.taskId,
          factorCount: data.factorCount || 0,
          startDate: task.startDate,
          endDate: task.endDate,
          avgIC: data.avgIC,
          avgIR: data.avgIR,
          results: data.results || []
        }
      }
    } catch (error) {
      console.error('加载任务结果失败:', error)
      ElMessage.error('加载任务结果失败，无法获取IC可视化数据')
      showIcVisualizationDialog.value = false
      return
    } finally {
      resultsLoading.value = false
    }
  }

  // 从任务结果中获取因子ID
  if (currentTaskResults.value && currentTaskResults.value.results.length > 0) {
    // 使用第一个有结果的因子
    selectedFactorId.value = currentTaskResults.value.results[0].factorId
  } else if (task.factorIds && task.factorIds.length > 0) {
    // 如果没有结果但有因子ID，使用第一个因子ID
    selectedFactorId.value = task.factorIds[0]
  } else {
    ElMessage.warning('该任务没有可用的因子数据')
    showIcVisualizationDialog.value = false
    return
  }

  await loadIcVisualizationData()
}

// 注意：loadIcVisualizationData函数在文件后续位置已定义，此处不再重复定义



// ========== 工具函数 ==========
function getStatusTagType(status: string): 'success' | 'info' | 'warning' | 'danger' | undefined {
  const typeMap: Record<string, 'success' | 'info' | 'warning' | 'danger'> = {
    PENDING: 'info',
    RUNNING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger'
  }
  return typeMap[status] || undefined;
}

function getStatusLabel(status: string): string {
  const labelMap: Record<string, string> = {
    PENDING: '待执行',
    RUNNING: '运行中',
    COMPLETED: '已完成',
    FAILED: '失败'
  }
  return labelMap[status] || status;
}

function getProgressStatus(status: string): '' | 'success' | 'exception' {
  if (status === 'COMPLETED') return 'success';
  if (status === 'FAILED') return 'exception';
  return '';
}

function getFrequencyLabel(frequency: string): string {
  const labelMap: Record<string, string> = {
    DAILY: '日度',
    WEEKLY: '周度',
    MONTHLY: '月度'
  }
  return labelMap[frequency] || frequency
}

function getICClass(ic: number | undefined): string {
  if (ic === undefined) return ''
  if (Math.abs(ic) >= 0.05) return 'ic-excellent'
  if (Math.abs(ic) >= 0.03) return 'ic-good'
  if (Math.abs(ic) >= 0.01) return 'ic-fair'
  return 'ic-poor'
}

// 获取IC值对应的颜色值（用于统计组件）
function getICColor(ic: number | undefined): string {
  if (ic === undefined) return '#909399'
  if (Math.abs(ic) >= 0.05) return '#67c23a'
  if (Math.abs(ic) >= 0.03) return '#409eff'
  if (Math.abs(ic) >= 0.01) return '#e6a23c'
  return '#f56c6c'
}

function getIRClass(ir: number | undefined): string {
  if (ir === undefined) return ''
  if (Math.abs(ir) >= 1.0) return 'ir-excellent'
  if (Math.abs(ir) >= 0.5) return 'ir-good'
  if (Math.abs(ir) >= 0.2) return 'ir-fair'
  return 'ir-poor'
}

function getRatingTagType(rating: string | undefined): '' | 'success' | 'warning' | 'danger' {
  const typeMap: Record<string, '' | 'success' | 'warning' | 'danger'> = {
    EXCELLENT: 'success',
    GOOD: '',
    FAIR: 'warning',
    POOR: 'danger'
  }
  return typeMap[rating || ''] || ''
}

function getRatingLabel(rating: string | undefined): string {
  const labelMap: Record<string, string> = {
    EXCELLENT: '优秀',
    GOOD: '良好',
    FAIR: '一般',
    POOR: '较差'
  }
  return labelMap[rating || ''] || '-'
}

function normalizeFactorIdList(ids: unknown): number[] {
  if (Array.isArray(ids)) {
    return (ids as Array<string | number>)
      .map(value => Number(value))
      .filter(value => !Number.isNaN(value))
  }
  if (typeof ids === 'string') {
    return ids.split(',')
      .map(part => Number(part.trim()))
      .filter(value => !Number.isNaN(value))
  }
  return []
}

function unwrapResultList<T>(payload: unknown): T[] {
  if (Array.isArray(payload)) {
    return payload as T[]
  }
  if (payload && typeof payload === 'object' && 'data' in payload) {
    const nested = (payload as { data?: unknown }).data
    return Array.isArray(nested) ? nested as T[] : []
  }
  return []
}

function getAxiosErrorResponse(error: unknown): { data?: unknown } | null {
  if (error && typeof error === 'object' && 'response' in error) {
    const candidate = (error as { response?: { data?: unknown } }).response
    return candidate ?? null
  }
  return null
}

function isHtmlPayload(payload: unknown): payload is string {
  return typeof payload === 'string' && payload.startsWith('<!DOCTYPE html>')
}

function extractResponseMessage(payload: unknown): string | undefined {
  if (payload && typeof payload === 'object' && 'message' in payload) {
    return (payload as { message?: string }).message
  }
  return undefined
}

function extractErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message
  }
  if (typeof error === 'string') {
    return error
  }
  return ''
}

// ========== API 调用 ==========
// 直接从因子历史API加载数据并转换为任务列表
async function loadTasksFromFactorHistory() {
  loading.value = true
  try {
    console.log('直接从因子历史API加载任务数据...')

    // 创建任务列表集合
    const tasksMap = new Map<number, ValidationTask>();

    // 直接查询几个特定的因子ID的历史数据（根据日志显示的ID）
    const factorIds = [1003, 1004, 1005, 1006, 1007, 1008];

    // 并发请求所有因子的历史数据
  const promises = factorIds.map(factorId =>
    request.get<ApiResponse<{ icIrResults: any[] }>>(
      `${API_BASE}/factors/${factorId}/history`,
      { responseType: 'json' }
    ).catch(error => {
        console.warn(`查询因子 ${factorId} 历史结果失败:`, error);
        return null;
      })
    );

    const results = await Promise.all(promises);

    // 处理所有响应，将历史记录转换为任务
    results.forEach((response, index) => {
      if (response && response.data && response.data.success && response.data.data) {
        const factorId = factorIds[index];
        const icIrResults = response.data.data.icIrResults || [];

        // 按taskId分组创建任务
        icIrResults.forEach(item => {
          if (item.taskId) {
            if (!tasksMap.has(item.taskId)) {
              // 创建新任务
              tasksMap.set(item.taskId, {
                taskId: item.taskId,
                taskName: `因子检验任务 ${item.taskId}`,
                factorIds: [factorId],
                factorCount: 1,
                startDate: item.calculationDate,
                endDate: item.calculationDate,
                status: 'COMPLETED',
                progress: 100, // 已完成的任务进度为100%
                createTime: item.calculationDate,
                updateTime: item.calculationDate,
                endTime: item.calculationDate,
                remark: `因子 ${item.factorName || factorId} 的检验任务`
              });
            } else {
              // 更新现有任务的因子信息
              const existingTask = tasksMap.get(item.taskId)!;
              if (!existingTask.factorIds.includes(factorId)) {
                existingTask.factorIds.push(factorId);
                existingTask.factorCount++;
              }
            }
          }
        });
      }
    });

    // 转换为数组并设置到任务列表
    taskList.value = Array.from(tasksMap.values());
    pagination.total = taskList.value.length;

    // 更新统计信息
    statistics.totalTasks = taskList.value.length;
    statistics.runningTasks = 0; // 所有从历史数据创建的任务都视为已完成
    statistics.completedTasks = taskList.value.length;
    statistics.failedTasks = 0;

    console.log('成功从因子历史API加载任务列表，共', taskList.value.length, '个任务');
    // 输出任务列表数据结构用于调试
    console.log('转换后的任务列表数据:', taskList.value);
    // 完成加载，设置loading为false
    loading.value = false;
  } catch (error) {
    console.error('从因子历史API加载任务失败:', error);
    ElMessage.error('加载任务失败，请稍后重试');

    // 清空列表
    taskList.value = [];
    pagination.total = 0;
    statistics.totalTasks = 0;
    statistics.runningTasks = 0;
    // 错误情况下也要设置loading为false
    loading.value = false;
  }
}

// 加载任务列表（保留原始函数，但会被新函数替代）
async function loadTaskList() {
  loading.value = true
  try {
    // 调整为正确的响应类型，匹配后端ResultDTO<FactorValidationResponse>格式
    const response = await request.get<ApiResponse<FactorValidationResponse>>(
      `${API_BASE}/tasks`,
      { responseType: 'json' } // 明确指定期望JSON响应
    )

    // 检查响应是否有效
    if (typeof response.data === 'object' && response.data !== null) {
      if (response.data.success && response.data.data) {
        // 后端返回成功，提取数据
        const responseData = response.data.data
        const rawTasks = (responseData?.tasks || []) as ValidationTask[]
        const normalizedTasks: ValidationTask[] = rawTasks.map(task => {
          const normalizedFactorIds = normalizeFactorIdList(task.factorIds)
          return {
            ...task,
            factorIds: normalizedFactorIds,
            factorCount: task.factorCount ?? normalizedFactorIds.length
          }
        })

        taskList.value = normalizedTasks
        pagination.total = normalizedTasks.length

        // 更新统计
        statistics.totalTasks = normalizedTasks.length
        statistics.runningTasks = normalizedTasks.filter(t => t.status === 'RUNNING').length
        statistics.completedTasks = normalizedTasks.filter(t => t.status === 'COMPLETED').length
        statistics.failedTasks = normalizedTasks.filter(t => t.status === 'FAILED').length

        console.log('成功加载任务列表，共', taskList.value.length, '个任务')
      } else {
        // 后端返回失败，尝试从因子历史API加载
        console.log('任务列表API返回失败，尝试从因子历史API加载');
        await loadTasksFromFactorHistory();
        return;
      }
    } else {
      // 响应不是预期格式，尝试从因子历史API加载
      console.log('任务列表API响应格式错误，尝试从因子历史API加载');
      await loadTasksFromFactorHistory();
      return;
    }
  } catch (error: unknown) {
    console.error('加载任务列表失败:', error)

    // 发生错误时，尝试从因子历史API加载
    console.log('加载任务列表时发生错误，尝试从因子历史API加载');
    await loadTasksFromFactorHistory();
  } finally {
    loading.value = false
  }
}

// 创建检验任务
async function handleCreateTask() {
  if (!taskFormRef.value) return

  await taskFormRef.value.validate(async (valid) => {
    if (!valid) return

    creating.value = true
    try {
      const [startDate, endDate] = taskForm.dateRange

      // 根据后端接口，调整请求参数
      // 后端期望 factorNames（因子名称列表），需要确保使用正确的因子名称
      const factorNames = taskForm.factorIds.map(id => {
        const factor = availableFactors.value.find(f => f.factorId === id);
        // 优先使用 factorName，如果没有则使用 factorCode
        return factor ? (factor.factorName || factor.factorCode) : '';
      }).filter(Boolean);

      if (factorNames.length === 0) {
        ElMessage.error('请选择至少一个有效的因子');
        return;
      }

      const response = await request.post<ApiResponse<FactorValidationResponse>>(
        `${API_BASE}/tasks`,
        {
          taskName: taskForm.taskName || undefined,
          factorNames: factorNames,
          startDate,
          endDate,
          taskType: 'IC_IR', // 后端默认值
          // 注意：后端接口不支持 validationTypes 和 frequency 参数
          // 这些参数可能需要通过其他方式配置或使用默认值
        },
        { responseType: 'json' } // 明确指定期望JSON响应
      )

      // 检查响应是否有效
      if (typeof response.data === 'object' && response.data !== null) {
        if (response.data.success) {
          ElMessage.success(response.data.message || '检验任务创建成功，正在执行...')
          showCreateTaskDialog.value = false
          resetTaskForm()
          await loadTaskList()
        } else {
          ElMessage.error(response.data.message || '创建任务失败：后端处理错误')
        }
      } else {
        ElMessage.error('创建任务失败：收到非预期的响应格式')
        console.error('创建任务失败：响应数据格式错误', response.data)
      }
    } catch (error: unknown) {
      console.error('创建检验任务失败:', error)

      const responsePayload = getAxiosErrorResponse(error)
      if (responsePayload?.data && isHtmlPayload(responsePayload.data)) {
        ElMessage.error('创建任务失败：API路径可能错误或后端未正确配置')
        console.error('API返回HTML而非JSON，可能是路径错误或后端配置问题')
      } else if (responsePayload?.data) {
        const errorMessage = extractResponseMessage(responsePayload.data)
        ElMessage.error(errorMessage || '创建任务失败：网络错误或服务器无响应')
      } else {
        ElMessage.error('创建任务失败：网络错误或服务器无响应')
      }
    } finally {
      creating.value = false
    }
  })
}

// 查看任务状态
async function viewTaskStatus(task: ValidationTask) {
  currentTask.value = task
  showStatusDialog.value = true
  await refreshTaskStatus()
}

// 刷新任务状态
async function refreshTaskStatus() {
  if (!currentTask.value) return

  statusLoading.value = true
  try {
    // 调整为正确的响应类型
    const response = await request.get<ApiResponse<FactorValidationResponse>>(
      `${API_BASE}/tasks/${currentTask.value.taskId}/status`,
      { responseType: 'json' } // 明确指定期望JSON响应
    )

    // 检查响应是否有效
    if (typeof response.data === 'object' && response.data !== null) {
      if (response.data.success && response.data.data) {
        currentTaskStatus.value = response.data.data as unknown as ValidationTask

        // 同时更新列表中的任务
        const index = taskList.value.findIndex(t => t.taskId === currentTask.value!.taskId)
        if (index > -1) {
          taskList.value[index] = { ...taskList.value[index], ...(response.data.data as unknown as ValidationTask) }
        }
      } else {
        ElMessage.error(response.data.message || '获取任务状态失败：后端处理错误')
      }
    } else {
      ElMessage.error('获取任务状态失败：收到非预期的响应格式')
      console.error('获取任务状态失败：响应数据格式错误', response.data)
      currentTaskStatus.value = null
    }
  } catch (error: unknown) {
    console.error('获取任务状态失败:', error)

    const responsePayload = getAxiosErrorResponse(error)
    if (responsePayload?.data && isHtmlPayload(responsePayload.data)) {
      ElMessage.error('获取任务状态失败：API路径可能错误或后端未正确配置')
      console.error('API返回HTML而非JSON，可能是路径错误或后端配置问题')
    } else if (responsePayload?.data) {
      const errorMessage = extractResponseMessage(responsePayload.data)
      ElMessage.error(errorMessage || '获取任务状态失败：网络错误或服务器无响应')
    } else {
      ElMessage.error('获取任务状态失败：网络错误或服务器无响应')
    }

    currentTaskStatus.value = null
  } finally {
    statusLoading.value = false
  }
}



// 加载IC检验可视化数据
async function loadIcVisualizationData() {
  if (!currentTask.value) return

  icVisualizationLoading.value = true
  try {
    // 获取IC Tear Sheet数据，这是一个综合数据接口
    const response = await request.get<ApiResponse<FactorIcVisualizationResponse>>(
      `${API_BASE}/tasks/${currentTask.value.taskId}/ic/tear-sheet`,
      {
        params: { factorId: selectedFactorId.value || undefined },
        responseType: 'json' // 明确指定期望JSON响应
      }
    )

    // 检查响应是否有效
    if (typeof response.data === 'object' && response.data !== null) {
      if (response.data.success && response.data.data) {
        icVisualizationData.value = response.data.data
        console.log('IC可视化数据加载成功:', response.data.data)
        await nextTick()
        renderIcSequenceChart()
        renderQuantileAnnualizedChart()
      } else {
        // 后端返回错误，通常是"未找到该因子的IC/IR结果"
        const errorMsg = response.data.message || '获取IC可视化数据失败：后端处理错误'
        ElMessage.error(errorMsg)
        console.warn('IC可视化数据获取失败:', errorMsg)

        // 如果是因为没有IC/IR结果，提示用户
        if (errorMsg.includes('未找到') || errorMsg.includes('IC/IR结果')) {
          ElMessage.warning('该因子尚未完成IC/IR计算，请等待任务完成后重试')
        }
      }
    } else {
      ElMessage.error('获取IC可视化数据失败：收到非预期的响应格式')
      console.error('获取IC可视化数据失败：响应数据格式错误', response.data)
    }
  } catch (error: unknown) {
    console.error('获取IC可视化数据失败:', error)

    const responsePayload = getAxiosErrorResponse(error)
    if (responsePayload?.data && isHtmlPayload(responsePayload.data)) {
      ElMessage.error('获取IC可视化数据失败：API路径可能错误或后端未正确配置')
      console.error('API返回HTML而非JSON，可能是路径错误或后端配置问题')
    } else if (responsePayload?.data) {
      const errorMessage = extractResponseMessage(responsePayload.data) || '获取IC可视化数据失败'
      ElMessage.error(errorMessage)
      if (errorMessage.includes('未找到') || errorMessage.includes('IC/IR结果')) {
        ElMessage.warning('该因子尚未完成IC/IR计算，请等待任务完成后重试')
      }
    } else {
      ElMessage.error('获取IC可视化数据失败：网络错误或服务器无响应')
    }
  } finally {
    icVisualizationLoading.value = false
  }
}

// 查看任务结果
async function viewTaskResults(task: ValidationTask) {
  currentTask.value = task
  showResultsDialog.value = true
  resultsLoading.value = true

  try {
    // 调整为正确的响应类型，匹配后端ResultDTO<FactorValidationResponse>格式
    const response = await request.get<ApiResponse<FactorValidationResponse>>(
      `${API_BASE}/tasks/${task.taskId}/results`,
      { responseType: 'json' } // 明确指定期望JSON响应
    )

    // 检查响应是否有效
    if (typeof response.data === 'object' && response.data !== null) {
      if (response.data.success && response.data.data) {
        // 从data中提取结果数据并构造TaskResults对象
        const data = response.data.data
        currentTaskResults.value = {
          taskId: task.taskId,
          factorCount: data.factorCount || 0,
          startDate: task.startDate,
          endDate: task.endDate,
          avgIC: data.avgIC,
          avgIR: data.avgIR,
          results: data.results || []
        }
        await nextTick()
        renderTaskResultsChart()

        // 设置默认选中的因子（用于IC可视化）
        if (currentTaskResults.value.results.length > 0) {
          selectedFactorId.value = currentTaskResults.value.results[0].factorId
        }
      } else {
        ElMessage.error(response.data.message || '获取结果失败：后端处理错误')
        showResultsDialog.value = false
      }
    } else {
      ElMessage.error('获取结果失败：收到非预期的响应格式')
      console.error('获取结果失败：响应数据格式错误', response.data)
      showResultsDialog.value = false
      currentTaskResults.value = null
    }
  } catch (error: unknown) {
    console.error('获取任务结果失败:', error)

    const responsePayload = getAxiosErrorResponse(error)
    if (responsePayload?.data && isHtmlPayload(responsePayload.data)) {
      ElMessage.error('获取结果失败：API路径可能错误或后端未正确配置')
      console.error('API返回HTML而非JSON，可能是路径错误或后端配置问题')
    } else if (responsePayload?.data) {
      const errorMessage = extractResponseMessage(responsePayload.data)
      ElMessage.error(errorMessage || '获取结果失败：网络错误或服务器无响应')
    } else {
      ElMessage.error('获取结果失败：网络错误或服务器无响应')
    }
    showResultsDialog.value = false
    currentTaskResults.value = null
  } finally {
    resultsLoading.value = false
  }
}

// 查看任务详情
async function viewTaskDetail(task: ValidationTask) {
  currentTask.value = task
  showDetailDialog.value = true
  // 加载任务结果数据
  await loadTaskResults(task)
}

// 加载任务结果数据
async function loadTaskResults(task: ValidationTask) {
  if (!task || !task.taskId) return

  resultsLoading.value = true
  try {
    // 调用新的任务结果查询API
    const response = await request.get<ApiResponse<FactorValidationResponse>>(
      `${API_BASE}/tasks/${task.taskId}/results`,
      {
        responseType: 'json' // 明确指定期望JSON响应
      }
    )

    // 检查响应是否有效
    if (typeof response.data === 'object' && response.data !== null) {
      if (response.data.success && response.data.data) {
        // 从data中提取结果数据并构造TaskResults对象
        const responseData = response.data.data

        currentTaskResults.value = {
          taskId: task.taskId,
          taskName: task.taskName,
          factorCount: responseData.icIrResults?.length || 0,
          startDate: task.startDate,
          endDate: task.endDate,
          results: (responseData.icIrResults || []).map((result: any) => ({
            resultId: result.resultId,
            factorId: result.factorId,
            factorCode: result.factorCode,
            factorName: result.factorName,
            ic: result.icMean,
            ir: result.irValue,
            icStd: result.icStd,
            icPositiveRatio: result.icPositiveRatio,
            calculationDate: result.calculationDate
          })),
          // 计算平均IC和IR
          avgIC: responseData.icIrResults && responseData.icIrResults.length > 0
            ? responseData.icIrResults.reduce((sum: number, r: any) => sum + (r.icMean || 0), 0) / responseData.icIrResults.length
            : undefined,
          avgIR: responseData.icIrResults && responseData.icIrResults.length > 0
            ? responseData.icIrResults.reduce((sum: number, r: any) => sum + (r.irValue || 0), 0) / responseData.icIrResults.length
            : undefined
        }

        console.log('成功加载任务结果数据:', currentTaskResults.value)
      } else {
        ElMessage.error(response.data.message || '获取任务结果失败：后端处理错误')
        currentTaskResults.value = null
      }
    } else {
      ElMessage.error('获取任务结果失败：收到非预期的响应格式')
      console.error('获取任务结果失败：响应数据格式错误', response.data)
      currentTaskResults.value = null
    }
  } catch (error: unknown) {
    console.error('获取任务结果失败:', error)

    const responsePayload = getAxiosErrorResponse(error)
    if (responsePayload?.data && isHtmlPayload(responsePayload.data)) {
      ElMessage.error('获取任务结果失败：API路径可能错误或后端未正确配置')
    } else if (responsePayload?.data) {
      const errorMessage = extractResponseMessage(responsePayload.data)
      ElMessage.error(errorMessage || '获取任务结果失败：网络错误或服务器无响应')
    } else {
      ElMessage.error('获取任务结果失败：网络错误或服务器无响应')
    }

    currentTaskResults.value = null
  } finally {
    resultsLoading.value = false
  }
}

// 检查因子状态（由于后端没有直接的检查接口，我们使用因子是否存在来模拟）
async function handleCheckFactorStatus() {
  if (taskForm.factorIds.length === 0) {
    ElMessage.warning('请先选择因子')
    return
  }

  checkingFactors.value = true
  try {
    // 模拟检查因子状态
    const statusList: FactorStatus[] = [];

    for (const factorId of taskForm.factorIds) {
      const factor = availableFactors.value.find(f => f.factorId === factorId);
      if (factor) {
        statusList.push({
          factorId: factor.factorId,
          factorName: factor.factorName,
          isValid: true,
          message: '因子状态正常'
        });
      } else {
        statusList.push({
          factorId: factorId,
          factorName: `未知因子(${factorId})`,
          isValid: false,
          message: '因子不存在'
        });
      }
    }

    factorStatusList.value = statusList;

    const invalidCount = statusList.filter(f => !f.isValid).length;
    if (invalidCount > 0) {
      ElMessage.warning(`有 ${invalidCount} 个因子状态异常`)
    } else {
      ElMessage.success('所有因子状态正常')
    }
  } catch (error) {
    console.error('检查因子状态失败:', error);
    ElMessage.error('检查因子状态失败');
  } finally {
    checkingFactors.value = false
  }
}

// 查看因子历史
async function viewFactorHistory(result: ValidationResult) {
  currentTask.value = { factorCount: 1 } as ValidationTask
  showHistoryDialog.value = true
  historyLoading.value = true

  try {
    const response = await request.get<ApiResponse<FactorValidationResponse>>(
      `${API_BASE}/factors/${result.factorId}/history`,
      {
        params: { limit: 10 },
        responseType: 'json' // 明确指定期望JSON响应
      }
    )

    // 检查响应是否有效
    if (typeof response.data === 'object' && response.data !== null) {
      if (response.data.success && response.data.data) {
          // 从data中提取历史记录结果，使用正确的字段名icIrResults
          const rawResults = response.data.data.icIrResults || [];

          // 转换字段映射，确保与表格期望的格式一致
          factorHistory.value = rawResults.map(item => ({
            taskId: item.taskId,
            // 将calculationDate用作日期范围的显示
            dateRange: item.calculationDate,
            startDate: item.calculationDate,
            endDate: item.calculationDate,
            // 映射icMean到ic字段
            ic: item.icMean,
            // 映射irValue到ir字段
            ir: item.irValue,
            // 保留创建时间
            createTime: item.calculationDate,
            // 保留原始数据，便于后续扩展
            ...item
          }));

          console.log(`成功加载因子 ${result.factorName} 的历史记录，共 ${factorHistory.value.length} 条`)
      } else {
        ElMessage.error(response.data.message || '获取因子历史记录失败：后端处理错误')
        factorHistory.value = []
      }
    } else {
      ElMessage.error('获取因子历史记录失败：收到非预期的响应格式')
      console.error('获取因子历史记录失败：响应数据格式错误', response.data)
      factorHistory.value = []
    }
  } catch (error: unknown) {
    console.error('获取因子历史记录失败:', error)

    const responsePayload = getAxiosErrorResponse(error)
    if (responsePayload?.data && isHtmlPayload(responsePayload.data)) {
      ElMessage.error('获取因子历史记录失败：API路径可能错误或后端未正确配置')
      console.error('API返回HTML而非JSON，可能是路径错误或后端配置问题')
    } else if (responsePayload?.data) {
      const errorMessage = extractResponseMessage(responsePayload.data)
      ElMessage.error(errorMessage || '获取因子历史记录失败：网络错误或服务器无响应')
    } else {
      ElMessage.error('获取因子历史记录失败：网络错误或服务器无响应')
    }
    factorHistory.value = []
  } finally {
    historyLoading.value = false
  }
}

// 根据IC值获取评级
// 导出结果
async function exportResults() {
  if (!currentTaskResults.value || !currentTask.value) {
    ElMessage.warning('没有可导出的结果数据');
    return;
  }

  try {
    // 添加BOM以支持Excel正确识别UTF-8编码
    const BOM = '\uFEFF';

    // 创建CSV内容
    let csvContent = BOM + '因子ID,因子名称,IC值,IR值,排序IC,胜率,评级\n';

    currentTaskResults.value.results.forEach(result => {
      // 对包含逗号或换行符的字段进行适当处理
      const factorName = result.factorName ? `"${result.factorName.replace(/"/g, '""')}"` : '-';
      csvContent += `${result.factorId},${factorName},${result.ic?.toFixed(4) || '-'},${result.ir?.toFixed(4) || '-'},${result.rankIC?.toFixed(4) || '-'},${result.winRate ? `${(result.winRate * 100).toFixed(2)}%` : '-'},${getRatingLabel(result.rating)}\n`;
    });

    // 创建下载链接
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);

    // 生成文件名，避免特殊字符
    const taskName = currentTask.value.taskName ? currentTask.value.taskName.replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, '_') : '未命名';
    const fileName = `因子检验结果_${taskName}_${currentTask.value.taskId}_${new Date().toISOString().split('T')[0]}.csv`;

    link.setAttribute('href', url);
    link.setAttribute('download', fileName);
    link.style.visibility = 'hidden';

    // 处理IE浏览器兼容性
    const enhancedNavigator = navigator as Navigator & { msSaveBlob?: (blob: Blob, name?: string) => void }
    if (typeof enhancedNavigator.msSaveBlob === 'function') {
      enhancedNavigator.msSaveBlob(blob, fileName);
    } else {
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }

    ElMessage.success('结果导出成功');
    console.log(`已导出因子检验结果文件: ${fileName}`);
  } catch (error) {
    console.error('导出结果失败:', error);
    ElMessage.error('导出结果失败，请重试');
  }
}

// 重置表单
function resetTaskForm() {
  taskForm.taskName = ''
  taskForm.factorIds = []
  taskForm.dateRange = []
  taskForm.validationTypes = ['IC', 'IR']
  taskForm.frequency = 'DAILY'
  taskForm.remark = ''
  taskFormRef.value?.clearValidate()
}

// 批量查询因子历史结果
async function loadFactorsHistory() {
  if (availableFactors.value.length === 0) {
    console.log('没有可用因子，跳过历史结果查询');
    return;
  }

  console.log(`开始查询 ${availableFactors.value.length} 个因子的历史结果`);

  // 限制并发查询数量，避免请求过多
  const concurrencyLimit = 5;
  const factorGroups = [];

  // 将因子分成多个组
  for (let i = 0; i < availableFactors.value.length; i += concurrencyLimit) {
    factorGroups.push(availableFactors.value.slice(i, i + concurrencyLimit));
  }

  // 按组进行并发查询
  for (const group of factorGroups) {
    const promises = group.map(factor =>
      request.get<ApiResponse<FactorValidationResponse>>(
        `${API_BASE}/factors/${factor.factorId}/history`,
        {
          params: { limit: 10 },
          responseType: 'json'
        }
      ).catch(error => {
        console.warn(`查询因子 ${factor.factorId} ${factor.factorName} 历史结果失败:`, error);
        return null; // 返回null，避免整个Promise.all失败
      })
    );

    const results = await Promise.all(promises);

    // 处理每个成功的响应
    results.forEach((response, index) => {
      if (response && response.data && response.data.success && response.data.data) {
        const factor = group[index];
        // 从icIrResults获取数据，这是API实际返回的字段名
        const rawResults = response.data.data.icIrResults || [];
        console.log(`成功获取因子 ${factor.factorId} ${factor.factorName} 的历史结果，共 ${rawResults.length} 条记录`);

        // 转换字段映射，确保与表格期望的格式一致
        const historyResults = rawResults.map(item => ({
          taskId: item.taskId,
          // 将calculationDate用作日期范围的显示
          dateRange: item.calculationDate,
          startDate: item.calculationDate,
          endDate: item.calculationDate,
          // 映射icMean到ic字段
          ic: item.icMean,
          // 映射irValue到ir字段
          ir: item.irValue,
          // 保留创建时间
          createTime: item.calculationDate,
          // 保留原始数据，便于后续扩展
          ...item
        }));

        // 存储因子历史结果到响应式变量
        allFactorsHistory.value.push({
          factorId: factor.factorId,
          factorName: factor.factorName,
          history: historyResults
        });
      }
    });
  }

  console.log('所有因子历史结果查询完成');
}

// 加载可用因子（从后端API获取）
async function loadAvailableFactors() {
  // 优先尝试通过基础因子接口获取有效因子列表
  try {
    const baseResponse = await request.get<ApiResponse<FactorBaseItem[]>>(
      `${FACTOR_BASE_API}/query`,
      {
        params: {
          popularOnly: false,
          pageSize: 200
        },
        responseType: 'json'
      }
    )

    if (typeof baseResponse.data === 'string' && (baseResponse.data as string).startsWith('<!DOCTYPE html>')) {
      console.error('基础因子接口返回HTML，可能是网关或路径配置异常');
      throw new Error('基础因子接口返回HTML');
    }

    const basePayload = baseResponse.data
    const baseSuccess = basePayload && ((basePayload.code === 200) || basePayload.success)
    const baseList = Array.isArray(basePayload?.data) ? basePayload.data as FactorBaseItem[] : []

    if (baseSuccess && baseList.length > 0) {
      const mappedFactors = baseList
        .filter(item => item && (item.isValid === undefined || item.isValid === null || item.isValid === true || item.isValid === 1))
        .map(item => ({
          factorId: item.baseId,
          factorCode: item.factorCode || `BASE_FACTOR_${item.baseId}`,
          factorName: item.factorName || item.factorCode || `基础因子-${item.baseId}`
        }))
        .filter(item => item.factorId !== undefined && item.factorName)

      if (mappedFactors.length > 0) {
        availableFactors.value = mappedFactors
        console.log('通过基础因子接口加载可用因子，共', mappedFactors.length, '个因子')

        // 获取因子后查询历史结果
        await loadFactorsHistory();
        return
      }
      console.warn('基础因子接口返回为空列表，准备回退到因子树接口')
    } else if (basePayload?.message) {
      console.warn('基础因子接口返回失败：', basePayload.message)
    }
  } catch (error: unknown) {
    console.error('通过基础因子接口加载因子失败:', error)
  }

  // 基础因子接口不可用时，回退到因子树接口
  try {
    // 首先获取所有场景
    const scenesResponse = await request.get<ApiResponse<FactorTreeScene[]>>(
      '/api/factor/factor-trees/scenes',
      { responseType: 'json' }
    )

    // 检查响应是否为HTML
    if (typeof scenesResponse.data === 'string' && (scenesResponse.data as string).startsWith('<!DOCTYPE html>')) {
      console.error('API返回HTML而非JSON，可能是路径错误或后端配置问题');
      throw new Error('API返回HTML内容');
    }

    if (scenesResponse.data.success && scenesResponse.data.data) {
      // 选择第一个场景或默认场景
      const scenesData = unwrapResultList<FactorTreeScene>(scenesResponse.data.data)
      const defaultSceneId = scenesData[0]?.sceneId || '';

      // 获取该场景下的所有因子树
      const treesResponse = await request.get<ApiResponse<FactorTree[]>>(
        `/api/factor/factor-trees?sceneId=${defaultSceneId}`,
        { responseType: 'json' }
      )

      // 检查响应是否为HTML
      if (typeof treesResponse.data === 'string' && (treesResponse.data as string).startsWith('<!DOCTYPE html>')) {
        console.error('API返回HTML而非JSON，可能是路径错误或后端配置问题');
        throw new Error('API返回HTML内容');
      }

      if (treesResponse.data.success && treesResponse.data.data) {
        // 对于每个因子树，获取其结构并提取因子节点
        const factorsSet = new Set<FactorOption>();
        let hasValidData = false;

        // 处理返回的数据结构：可能是数组，也可能是包装在 data 中的数组
        const treesData = unwrapResultList<FactorTree>(treesResponse.data.data)

        for (const tree of treesData) {
          // 兼容 treeid 和 treeId 两种字段名
          const treeId = tree.treeid || tree.treeId || tree.id;

          // 如果 treeId 不存在，跳过这个树
          if (!treeId) {
            console.warn('跳过无效的因子树（缺少 treeId）:', tree);
            continue;
          }

          const structureResponse = await request.get<ApiResponse<FactorTree>>(
            `/api/factor/factor-trees/${treeId}/structure`,
            { responseType: 'json' }
          )

          // 检查响应是否为HTML
          if (typeof structureResponse.data === 'string' && (structureResponse.data as string).startsWith('<!DOCTYPE html>')) {
            console.error('API返回HTML而非JSON，可能是路径错误或后端配置问题');
            continue; // 跳过这个树，但继续尝试其他树
          }

          if (structureResponse.data.success && structureResponse.data.data) {
            // 递归提取因子节点
            extractFactorNodes(structureResponse.data.data, factorsSet);
            hasValidData = true;
          }
        }

        if (hasValidData && factorsSet.size > 0) {
          availableFactors.value = Array.from(factorsSet);
          console.log('成功加载可用因子，共', availableFactors.value.length, '个因子')

          // 获取因子后查询历史结果
          await loadFactorsHistory();
        } else {
          // 如果没有有效的因子数据，清空列表
          availableFactors.value = []
          console.warn('未找到可用的因子数据')
        }
      } else {
        availableFactors.value = []
        console.warn('未找到可用的因子树数据')
      }
    } else {
      availableFactors.value = []
      console.warn('未找到可用的场景数据')
    }
  } catch (error: unknown) {
    console.error('加载可用因子失败:', error);
    const message = extractErrorMessage(error) || '加载可用因子失败：网络错误或服务器无响应'
    ElMessage.error(message)
    availableFactors.value = []
  }
}

// 递归提取因子节点
function extractFactorNodes(nodeData: FactorTree | null | undefined, factorsSet: Set<FactorOption>) {
  if (!nodeData) return;

  // 检查是否是因子节点
  if (nodeData.nodeType === 'FACTOR' && nodeData.factorId) {
    factorsSet.add({
      factorId: nodeData.factorId,
      factorCode: nodeData.factorCode || `F${nodeData.factorId}`,
      factorName: nodeData.nodeName || nodeData.factorCode || `因子-${nodeData.factorId}`
    });
  }

  // 递归处理子节点
  if (nodeData.children && Array.isArray(nodeData.children)) {
    nodeData.children.forEach((child) => extractFactorNodes(child, factorsSet));
  }
}

// ========== 生命周期 ==========
onMounted(async () => {
  console.log('因子检验管理页面：开始加载数据');
  window.addEventListener('resize', handleWindowResize);

  // 加载真实数据
  try {
    // 优先从因子历史API直接加载任务数据
    await loadTasksFromFactorHistory();
    // 然后加载可用因子（用于其他功能）
    await loadAvailableFactors();
    console.log('因子检验管理页面：数据加载完成');
  } catch (error) {
    console.error('加载数据时出错:', error);
    ElMessage.error('页面数据加载失败，请刷新页面重试');
  }

  // 每30秒自动刷新运行中的任务（仅在页面可见且有运行中任务时）
  autoRefreshTimer = setInterval(() => {
    // 检查页面是否可见
    if (document.hidden) {
      return; // 页面不可见时不刷新
    }

    // 检查是否有运行中的任务
    const hasRunningTasks = taskList.value.some(t => t.status === 'RUNNING')
    if (hasRunningTasks) {
      console.log('自动刷新任务列表（检测到运行中的任务）')
      loadTaskList()
    }
  }, 30000) // 30秒刷新一次

  console.log('因子检验管理页面已加载');
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
    console.log('已清理自动刷新定时器')
  }
  window.removeEventListener('resize', handleWindowResize)
  disposeTaskResultsChart()
  disposeIcCharts()
  disposeLayeredChart()
})
</script>

<style scoped>
.factor-validation-management {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.history-card {
  margin-top: 20px;
}

.history-overview {
  margin-top: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.form-item-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

/* IC/IR 值颜色 */
.ic-excellent,
.ir-excellent {
  color: #67c23a;
  font-weight: 600;
}

.ic-good,
.ir-good {
  color: #409eff;
  font-weight: 500;
}

.ic-fair,
.ir-fair {
  color: #e6a23c;
}

.ic-poor,
.ir-poor {
  color: #f56c6c;
}

/* 图表容器样式 */
.chart-container {
  margin-bottom: 20px;
}

.chart-container h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.chart-placeholder {
  width: 100%;
  height: 400px;
  background-color: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}

.chart-canvas {
  width: 100%;
  height: 360px;
}
.task-results-container {
        padding: 10px 0;
      }

      .stats-row {
        display: flex;
        gap: 40px;
        margin-bottom: 20px;
      }

      .mt-4 {
        margin-top: 16px;
      }

      .positive-ic {
        color: #67c23a;
      }

      .negative-ic {
        color: #f56c6c;
      }

      .neutral-ic {
        color: #909399;
      }
    </style>
