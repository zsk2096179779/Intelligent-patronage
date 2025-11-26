<template>
  <div class="factor-tree-management">
    <el-card shadow="never" class="header-card">
      <div class="header-section">
        <div class="title-area">
          <h2>因子树管理</h2>
          <p class="subtitle">管理因子树的层级结构，支持节点的创建、编辑和移动</p>
        </div>
        <div class="statistics-area">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.totalTrees || 0 }}</div>
            <div class="stat-label">因子树总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statistics.totalNodes || 0 }}</div>
            <div class="stat-label">节点总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statistics.totalScenes || 0 }}</div>
            <div class="stat-label">场景数量</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <div class="left-actions">
          <el-select
            v-model="selectedSceneId"
            placeholder="选择场景"
            clearable
            style="width: 200px;"
            @change="handleSceneChange"
          >
            <el-option label="全部场景" value="" />
            <el-option
              v-for="scene in sceneList"
              :key="scene.sceneId"
              :label="scene.sceneName"
              :value="scene.sceneId"
            />
          </el-select>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索因子树或因子"
            clearable
            style="width: 300px; margin-left: 12px;"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button-group style="margin-left: 12px;">
            <el-button @click="searchType = 'tree'" :type="searchType === 'tree' ? 'primary' : ''">搜索因子树</el-button>
            <el-button @click="searchType = 'factor'" :type="searchType === 'factor' ? 'primary' : ''">搜索因子</el-button>
          </el-button-group>
          <el-button type="primary" @click="handleSearch" style="margin-left: 12px;">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </div>
        <div class="right-actions">
          <el-button @click="showSceneDialog = true">
            <el-icon><FolderAdd /></el-icon>
            新建场景
          </el-button>
          <el-button type="primary" @click="openCreateTreeDialog">
            <el-icon><Plus /></el-icon>
            创建因子树
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never" class="tree-list-card">
          <template #header>
            <div class="card-header">
              <span>因子树列表</span>
              <el-button text @click="loadTreeList">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <el-scrollbar height="700px">
            <div v-loading="treeListLoading">
              <div
                v-for="tree in treeList"
                :key="tree.id"
                class="tree-item"
                :class="{ active: selectedTreeId === tree.id }"
                @click="selectTree(tree)"
              >
                <div class="tree-item-content">
                  <el-icon class="tree-icon"><FolderOpened /></el-icon>
                  <div class="tree-info">
                    <div class="tree-name">{{ tree.nodeName || tree.treeName || '未命名因子树' }}</div>
                    <div class="tree-desc">{{ tree.description || '暂无描述' }}</div>
                  </div>
                </div>
                <div class="tree-item-actions">
                  <el-button text size="small" @click.stop="addNodeToTree(tree)">
                    <el-icon><Plus /></el-icon>
                  </el-button>
                </div>
              </div>
              <el-empty v-if="treeList.length === 0" description="暂无因子树" />
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never" class="tree-structure-card">
          <template #header>
            <div class="card-header">
              <span>树形结构 {{ selectedTreeName ? `- ${selectedTreeName}` : '' }}</span>
              <div>
                <el-button text @click="expandAll">
                  <el-icon><Expand /></el-icon>
                  全部展开
                </el-button>
                <el-button text @click="collapseAll">
                  <el-icon><Fold /></el-icon>
                  全部收起
                </el-button>
              </div>
            </div>
          </template>
          <el-scrollbar height="700px">
            <div v-loading="treeStructureLoading">
              <el-tree
                v-if="treeData.length > 0"
                ref="treeRef"
                :data="treeData"
                :props="treeProps"
                :load="lazyLoadNodes"
                node-key="id"
                :expand-on-click-node="false"
                :default-expand-all="true"
                lazy
              >
                <template #default="{ data }">
                  <div class="tree-node">
                    <div class="node-content">
                      <el-icon v-if="data.nodeType === 'TREE'" class="node-icon tree-type">
                        <FolderOpened />
                      </el-icon>
                      <el-icon v-else-if="data.nodeType === 'CATEGORY'" class="node-icon group-type">
                        <Folder />
                      </el-icon>
                      <el-icon v-else class="node-icon factor-type">
                        <Document />
                      </el-icon>
                      <span class="node-label">{{ data.nodeName }}</span>
                      <el-tag v-if="data.nodeType" size="small" style="margin-left: 8px;">
                        {{ data.nodeType }}
                      </el-tag>
                    </div>
                    <div class="node-actions">
                      <el-button-group size="small">
                        <el-button text type="danger" @click="deleteNode(data)">
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </el-button-group>
                    </div>
                  </div>
                </template>
              </el-tree>
              <el-empty v-else description="请选择一个因子树" />
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="showCreateTreeDialog"
      title="创建因子树"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="treeForm" :rules="treeFormRules" ref="treeFormRef" label-width="100px">
        <el-form-item label="因子树名称" prop="treeName">
          <el-input v-model="treeForm.treeName" placeholder="请输入因子树名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="treeForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="所属场景" prop="sceneId">
          <el-select v-model="treeForm.sceneId" placeholder="选择场景" style="width: 100%;">
            <el-option
              v-for="scene in sceneList"
              :key="scene.sceneId"
              :label="scene.sceneName"
              :value="scene.sceneId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateTreeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTree">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showAddNodeDialog"
      :title="nodeDialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="nodeForm" :rules="nodeFormRules" ref="nodeFormRef" label-width="100px">
        <el-form-item label="父节点">
          <el-input :value="nodeForm.parentName" disabled />
        </el-form-item>
        <el-form-item label="节点名称" prop="nodeName">
          <el-input v-model="nodeForm.nodeName" placeholder="请输入节点名称" />
        </el-form-item>
        <el-form-item label="节点类型" prop="nodeType">
          <el-select v-model="nodeForm.nodeType" placeholder="选择节点类型" style="width: 100%;">
            <el-option label="分组" value="CATEGORY" />
            <el-option label="因子" value="FACTOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="因子ID" prop="factorId" v-if="nodeForm.nodeType === 'FACTOR'">
          <el-input v-model="nodeForm.factorId" placeholder="请输入基础因子ID (数字)" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="nodeForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddNodeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddNode">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showEditNodeDialog"
      title="编辑节点"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editNodeForm" :rules="editNodeFormRules" ref="editNodeFormRef" label-width="100px">
        <el-form-item label="节点名称" prop="nodeName">
          <el-input v-model="editNodeForm.nodeName" placeholder="请输入节点名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="editNodeForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="editNodeForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditNodeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateNode">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showMoveNodeDialog"
      title="移动节点"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="moveNodeForm" ref="moveNodeFormRef" label-width="100px">
        <el-form-item label="当前节点">
          <el-input :value="moveNodeForm.nodeName" disabled />
        </el-form-item>
        <el-form-item label="目标父节点ID" prop="newParentId">
           <el-input v-model.number="moveNodeForm.newParentId" placeholder="请输入目标父节点ID" />
           <div class="form-tip">请输入目标分组节点的ID</div>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="moveNodeForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMoveNodeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleMoveNode">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showSceneDialog"
      title="新建场景"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="sceneForm" :rules="sceneFormRules" ref="sceneFormRef" label-width="100px">
        <el-form-item label="场景ID" prop="sceneId">
          <el-input v-model="sceneForm.sceneId" placeholder="请输入场景ID（唯一标识）" />
        </el-form-item>
        <el-form-item label="场景名称" prop="sceneName">
          <el-input v-model="sceneForm.sceneName" placeholder="请输入场景名称" />
        </el-form-item>
        <el-form-item label="场景描述">
          <el-input
            v-model="sceneForm.sceneDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入场景描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSceneDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateScene">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showPathDialog"
      title="节点路径"
      width="600px"
    >
      <el-steps direction="vertical" :active="nodePath.length">
        <el-step
          v-for="(item, index) in nodePath"
          :key="index"
          :title="item.nodeName || '未命名'"
          :description="`类型: ${item.nodeType} | ID: ${item.id}`"
        />
      </el-steps>
    </el-dialog>

    <el-dialog
      v-model="showSearchDialog"
      :title="`搜索结果 - ${searchType === 'tree' ? '因子树' : '因子'}`"
      width="800px"
    >
      <el-table :data="searchResults" border max-height="500">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nodeName" label="名称" min-width="150" />
        <el-table-column prop="nodeType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.nodeType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button text type="primary" @click="selectSearchResult(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 全局基础样式，确保所有文字继承更大的基础字体 */
.factor-tree-management {
  font-size: 18px; /* 大幅增大基础字体大小 */
  line-height: 1.6; /* 调整行高以提高可读性 */
}

/* 标题样式 */
.header-card h2 {
  font-size: 32px; /* 大幅增大标题字体 */
  margin-bottom: 12px;
  font-weight: bold;
}

.header-card .subtitle {
  font-size: 24px; /* 大幅增大副标题字体 */
  color: #606266;
}

/* 统计数据样式 */
.stat-item .stat-value {
  font-size: 36px; /* 大幅增大统计数值字体 */
  font-weight: bold;
  color: #409eff;
}

.stat-item .stat-label {
  font-size: 20px; /* 大幅增大统计标签字体 */
  color: #606266;
  margin-top: 8px;
}

/* 树形结构相关样式 */
.tree-name {
  font-size: 24px; /* 大幅增大树名称字体 */
  font-weight: 600;
  margin-bottom: 4px;
}

.tree-desc {
  font-size: 18px; /* 大幅增大树描述字体 */
  color: #606266;
}

.node-label {
  font-size: 20px; /* 大幅增大节点标签字体 */
  font-weight: 500;
}

/* 表格样式 */
.el-table {
  font-size: 20px; /* 大幅增大表格字体 */
}

.el-table .cell {
  font-size: 20px; /* 大幅增大表格单元格字体 */
  padding: 12px 0;
}

.el-table th {
  font-size: 22px; /* 增大表格表头字体 */
  font-weight: bold;
  height: 60px;
}

/* 表单相关样式 */
.el-form-item__label {
  font-size: 20px; /* 大幅增大表单标签字体 */
  font-weight: 500;
}

.el-input__wrapper {
  font-size: 20px; /* 大幅增大输入框字体 */
  height: 48px;
}

.el-input__inner {
  font-size: 20px; /* 确保输入框内部文字大小一致 */
  height: 48px;
  line-height: 48px;
}

/* 按钮样式 */
.el-button {
  font-size: 20px; /* 大幅增大按钮字体 */
  padding: 12px 24px; /* 大幅增大按钮内边距 */
  height: auto;
}

.el-button--small {
  font-size: 18px; /* 确保小按钮字体也足够大 */
  padding: 8px 16px;
}

/* 对话框样式 */
.el-dialog__title {
  font-size: 28px; /* 大幅增大对话框标题字体 */
  font-weight: bold;
}

/* 卡片样式 */
.card-header span {
  font-size: 24px; /* 大幅增大卡片标题字体 */
  font-weight: 600;
}

/* 标签样式 */
.el-tag {
  font-size: 18px; /* 增大标签字体 */
  padding: 6px 12px;
  margin: 4px;
}

/* 步骤条样式 */
.el-step__title {
  font-size: 20px; /* 增大步骤条标题字体 */
  font-weight: 500;
}

.el-step__description {
  font-size: 18px; /* 增大步骤条描述字体 */
}

/* 空状态样式 */
.el-empty__description {
  font-size: 20px; /* 增大空状态描述字体 */
}

/* 下拉选择框样式 */
.el-select {
  font-size: 20px; /* 增大下拉选择框字体 */
}

.el-select .el-input__inner {
  font-size: 20px; /* 确保下拉选择框输入框文字大小一致 */
}

/* 确保placeholder也有足够大的字体 */
:deep(.el-input__placeholder) {
  font-size: 20px !important;
}

/* 图标按钮样式调整 */
.el-button .el-icon {
  font-size: 20px;
}

/* 确保滚动条区域内的字体大小一致 */
.el-scrollbar__view {
  font-size: inherit;
}
</style>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElTree } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  Search,
  Plus,
  Refresh,
  FolderAdd,
  FolderOpened,
  Folder,
  Document,
  Delete,
  Expand,
  Fold
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// ========== 类型定义 ==========
interface ApiResponse<T = unknown> {
  code: number
  message?: string
  data?: T
}

interface FactorTreeScene {
  id?: number
  sceneId: string
  sceneName: string
  sceneDesc?: string
}

// 对应后端 FactorTree 实体
interface FactorTreeNode {
  id: number
  nodeName: string // 后端使用 nodeName
  treeName?: string // 根节点可能有 treeName
  nodeType: 'TREE' | 'CATEGORY' | 'FACTOR' // 后端要求TREE、CATEGORY或FACTOR
  description?: string
  parentId?: number
  treeId?: number
  factorId?: number | null
  sortOrder?: number
  children?: FactorTreeNode[]
  hasChildren?: boolean
  isLeaf?: boolean
}

interface TreeStatistics {
  totalTrees: number
  totalNodes: number
  totalScenes: number
}

interface CreateTreeForm {
  treeName: string
  description: string
  sceneId: string
}

interface NodeForm {
  parentId: number | null
  parentName: string
  nodeName: string
  nodeType: 'CATEGORY' | 'FACTOR' | ''
  factorId: string // 输入时为字符串
  description: string
  treeId?: number
}

interface EditNodeForm {
  nodeId: number | null
  nodeName: string
  description: string
  sortOrder: number
}

interface MoveNodeForm {
  nodeId: number | null
  nodeName: string
  newParentId: number | null
  sortOrder: number
}

interface SceneForm {
  sceneId: string
  sceneName: string
  sceneDesc: string
}

// ========== 状态管理 ==========
// 基础路径使用相对路径，通过 Vite 代理
const API_BASE = '/api/factor/factor-trees'

// 场景和因子树列表
const sceneList = ref<FactorTreeScene[]>([])
const treeList = ref<FactorTreeNode[]>([])
const selectedSceneId = ref<string>('')
const selectedTreeId = ref<number | null>(null)
const selectedTreeName = ref<string>('')

// 树形结构
const treeData = ref<FactorTreeNode[]>([])
const treeRef = ref<InstanceType<typeof ElTree>>()
// 修正：el-tree 的 props 映射
const treeProps = {
  label: 'nodeName',
  children: 'children',
  isLeaf: 'isLeaf'
}

// 搜索
const searchKeyword = ref<string>('')
const searchType = ref<'tree' | 'factor'>('tree')
const searchResults = ref<FactorTreeNode[]>([])
const showSearchDialog = ref<boolean>(false)

// 节点路径
const nodePath = ref<FactorTreeNode[]>([])
const showPathDialog = ref<boolean>(false)

// 加载状态
const treeListLoading = ref<boolean>(false)
const treeStructureLoading = ref<boolean>(false)

// 统计信息
const statistics = reactive<TreeStatistics>({
  totalTrees: 0,
  totalNodes: 0,
  totalScenes: 0
})

// 对话框
const showCreateTreeDialog = ref<boolean>(false)
const showAddNodeDialog = ref<boolean>(false)
const showEditNodeDialog = ref<boolean>(false)
const showMoveNodeDialog = ref<boolean>(false)
const showSceneDialog = ref<boolean>(false)

// 表单
const treeFormRef = ref<FormInstance>()
const nodeFormRef = ref<FormInstance>()
const editNodeFormRef = ref<FormInstance>()
const moveNodeFormRef = ref<FormInstance>()
const sceneFormRef = ref<FormInstance>()

const treeForm = reactive<CreateTreeForm>({
  treeName: '',
  description: '',
  sceneId: ''
})

const nodeForm = reactive<NodeForm>({
  parentId: null,
  parentName: '',
  nodeName: '',
  nodeType: '',
  factorId: '',
  description: '',
  treeId: undefined
})

const editNodeForm = reactive<EditNodeForm>({
  nodeId: null,
  nodeName: '',
  description: '',
  sortOrder: 0
})

const moveNodeForm = reactive<MoveNodeForm>({
  nodeId: null,
  nodeName: '',
  newParentId: null,
  sortOrder: 0
})

const sceneForm = reactive<SceneForm>({
  sceneId: '',
  sceneName: '',
  sceneDesc: ''
})

// 表单验证规则
const treeFormRules: FormRules = {
  treeName: [{ required: true, message: '请输入因子树名称', trigger: 'blur' }],
  sceneId: [{ required: true, message: '请选择场景', trigger: 'change' }]
}

const nodeFormRules: FormRules = {
  nodeName: [{ required: true, message: '请输入节点名称', trigger: 'blur' }],
  nodeType: [{ required: true, message: '请选择节点类型', trigger: 'change' }],
  factorId: [{ required: true, message: '请输入因子ID', trigger: 'blur' }]
}

const editNodeFormRules: FormRules = {
  nodeName: [{ required: true, message: '请输入节点名称', trigger: 'blur' }]
}

const sceneFormRules: FormRules = {
  sceneId: [{ required: true, message: '请输入场景ID', trigger: 'blur' }],
  sceneName: [{ required: true, message: '请输入场景名称', trigger: 'blur' }]
}

// ========== 计算属性 ==========
const nodeDialogTitle = computed(() => {
  return nodeForm.parentId ? '添加子节点' : '添加节点'
})

// ========== API 调用 ==========
// 获取所有场景
async function loadScenes() {
  try {
    const response = await request.get<ApiResponse<FactorTreeScene[]>>(`${API_BASE}/scenes`)
    if ((response.data.code === 200 || response.data.code === 0) && response.data.data) {
      sceneList.value = response.data.data
      statistics.totalScenes = response.data.data.length
    }
  } catch (error) {
    console.error('加载场景失败:', error)
    ElMessage.error('加载场景失败')
  }
}

// 创建场景 (后端使用 @RequestParam)
async function handleCreateScene() {
  if (!sceneFormRef.value) return

  await sceneFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      // POST 请求，参数通过 params 传递以匹配 @RequestParam
      const response = await request.post<ApiResponse<FactorTreeScene>>(
        `${API_BASE}/scenes`,
        null,
        {
          params: {
            sceneId: sceneForm.sceneId,
            sceneName: sceneForm.sceneName,
            sceneDesc: sceneForm.sceneDesc || undefined
          }
        }
      )

      if (response.data.code === 200 || response.data.code === 0) {
        ElMessage.success('场景创建成功')
        showSceneDialog.value = false
        resetSceneForm()
        await loadScenes()
      } else {
        ElMessage.error(response.data.message || '创建场景失败')
      }
    } catch (error) {
      console.error('创建场景失败:', error)
      ElMessage.error('创建场景失败')
    }
  })
}

// 获取因子树列表
async function loadTreeList() {
  treeListLoading.value = true
  try {
    const params: { sceneId?: string } = {}
    if (selectedSceneId.value) {
      params.sceneId = selectedSceneId.value
    }

    const response = await request.get<ApiResponse<FactorTreeNode[]>>(`${API_BASE}`, { params })
    if ((response.data.code === 200 || response.data.code === 0) && response.data.data) {
      // 映射数据，确保 nodeName 存在，并将后端的treeid映射到前端的id字段
      treeList.value = response.data.data.map(item => ({
        ...item,
        // 处理API返回的字段映射，兼容treeid到id的转换
        id: 'treeid' in item ? Number(item.treeid) : item.id,
        nodeName: item.nodeName || item.treeName || '未命名因子树'
      }))
      statistics.totalTrees = response.data.data.length
    }
  } catch (error) {
    console.error('加载因子树列表失败:', error)
    ElMessage.error('加载因子树列表失败')
  } finally {
    treeListLoading.value = false
  }
}

// 创建因子树
async function handleCreateTree() {
  if (!treeFormRef.value) return

  await treeFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      const response = await request.post<ApiResponse<FactorTreeNode>>(`${API_BASE}`, {
        treeName: treeForm.treeName,
        description: treeForm.description,
        sceneId: treeForm.sceneId
      })

      if (response.data.code === 200 || response.data.code === 0) {
        ElMessage.success('因子树创建成功')
        showCreateTreeDialog.value = false
        resetTreeForm()
        await loadTreeList()
      } else {
        ElMessage.error(response.data.message || '创建因子树失败')
      }
    } catch (error) {
      console.error('创建因子树失败:', error)
      ElMessage.error('创建因子树失败')
    }
  })
}

// 选择因子树
async function selectTree(tree: FactorTreeNode) {
  // 验证tree.id的有效性
  const treeId = tree.id || (tree as unknown as Record<string, unknown>).treeid || undefined
  if (!treeId || typeof treeId !== 'number' || isNaN(treeId)) {
    console.warn('选择的因子树ID无效，无法加载树结构:', tree.id)
    ElMessage.warning('选择的因子树ID无效，无法加载树结构')
    return
  }

  selectedTreeId.value = treeId
  selectedTreeName.value = tree.nodeName || tree.treeName || '未命名因子树'
  await loadTreeStructure(treeId)
}

// 获取树结构 (后端返回 Map<String, Object>)
async function loadTreeStructure(treeId: number | undefined) {
  // 检查treeId是否有效
  if (!treeId || typeof treeId !== 'number' || isNaN(treeId)) {
    console.warn('无效的treeId，无法加载树结构:', treeId)
    treeData.value = []
    return
  }

  treeStructureLoading.value = true
  try {
    const response = await request.get<ApiResponse<{ tree?: FactorTreeNode; root?: FactorTreeNode; }>>(`${API_BASE}/${treeId}/structure`)
    if ((response.data.code === 200 || response.data.code === 0) && response.data.data) {
      const data = response.data.data

      // 兼容后端返回 Map 结构，查找根节点并确保类型安全
      // 定义更准确的数据类型，兼容后端返回的结构
      interface RootData {
        tree?: FactorTreeNode;
        root?: FactorTreeNode;
        nodes?: FactorTreeNode[];
        id?: number;
        treeid?: number | string;
        name?: string;
        nodeName?: string;
        treeName?: string;
        type?: string;
        nodeType?: 'TREE' | 'CATEGORY' | 'FACTOR';
      }

      // 优先使用tree对象，然后是data本身
      let rootNodeData: RootData | null = null
      
      // 使用Record类型安全访问可能不存在的nodes属性
      const dataWithNodes = data as Record<string, unknown>

      if (data.tree) {
        rootNodeData = data.tree
      } else if (dataWithNodes.nodes && Array.isArray(dataWithNodes.nodes) && dataWithNodes.nodes.length > 0) {
        rootNodeData = dataWithNodes.nodes[0] as FactorTreeNode
      } else {
        rootNodeData = data
      }

      // 确保rootNodeData是一个对象
      if (typeof rootNodeData !== 'object' || rootNodeData === null) {
        throw new Error('无效的树结构数据')
      }

      // 确保必要的属性存在并类型正确
      const rootNode: FactorTreeNode = {
        id: Number(rootNodeData.id || rootNodeData.treeid) || 0, // 确保id是number类型，兼容treeid
        nodeName: rootNodeData.nodeName || rootNodeData.name || rootNodeData.treeName || '未命名因子树', // 兼容name字段
        nodeType: ((rootNodeData.nodeType || rootNodeData.type) as 'TREE' | 'CATEGORY' | 'FACTOR') || 'TREE', // 兼容type字段
        ...rootNodeData
      }

      // 递归处理函数：修正字段映射，标记叶子节点
      const processNode = (node: FactorTreeNode): FactorTreeNode => {
        // 确保节点有有效的ID
        const nodeId = node.id || (node as unknown as Record<string, unknown>).treeid || 0
        const isLeaf = node.isLeaf || node.nodeType === 'FACTOR'

        return {
          ...node,
          // 确保必要字段存在
          id: Number(nodeId), // 确保 id 是数字类型，支持treeid字段
          nodeName: String(node.nodeName || node.treeName || (node as unknown as Record<string, unknown>).name || '未命名'), // 支持name字段并确保是字符串类型
          nodeType: String(node.nodeType || (node as unknown as Record<string, unknown>).type || 'TREE') as 'TREE' | 'CATEGORY' | 'FACTOR', // 支持type字段
          isLeaf,
          children: node.children ? node.children.map(processNode) : []
        }
      }

      treeData.value = [processNode(rootNode)]

      // 计算节点总数
      const countNodes = (nodes: FactorTreeNode[]): number => {
        return nodes.reduce((count, node) => {
          return count + 1 + (node.children ? countNodes(node.children) : 0)
        }, 0)
      }
      statistics.totalNodes = countNodes(treeData.value)
    }
  } catch (error) {
    console.error('加载树结构失败:', error)
    ElMessage.error('加载树结构失败')
  } finally {
    treeStructureLoading.value = false
  }
}

// 懒加载子节点
async function lazyLoadNodes(node: unknown, resolve: (data: FactorTreeNode[]) => void) {
  const nodeData = (node as { data: FactorTreeNode }).data

  if (nodeData.children && nodeData.children.length > 0) {
    resolve(nodeData.children)
    return
  }

  // 处理节点ID，兼容后端的treeid和前端的id字段
  const nodeId = nodeData.id || (nodeData as unknown as Record<string, unknown>).treeid || undefined

  // 验证nodeId是否有效
  if (!nodeId || typeof nodeId !== 'number' || isNaN(nodeId)) {
    console.warn('无效的节点ID，无法加载子节点:', nodeId)
    resolve([])
    return
  }

  try {
    const response = await request.get<ApiResponse<FactorTreeNode[]>>(
      `${API_BASE}/nodes/${nodeId}/children`,
      {
        params: { pageSize: 50, offset: 0 }
      }
    )

    if ((response.data.code === 200 || response.data.code === 0) && response.data.data) {
      // 映射子节点数据
      const children = response.data.data.map(child => ({
        ...child,
        nodeName: child.nodeName || '未命名', // 确保 nodeName
        isLeaf: child.nodeType === 'FACTOR'
      }))
      resolve(children)
    } else {
      resolve([])
    }
  } catch (error) {
    console.error('加载子节点失败:', error)
    resolve([])
  }
}

// 添加节点准备
function addNodeToTree(tree: FactorTreeNode) {
  nodeForm.parentId = tree.id
  nodeForm.parentName = tree.nodeName
  nodeForm.treeId = tree.id
  showAddNodeDialog.value = true
}



// 处理添加节点 (AddNodeRequest)
async function handleAddNode() {
  if (!nodeFormRef.value) return

  await nodeFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      const treeId = nodeForm.treeId || selectedTreeId.value
      if (!treeId) {
        ElMessage.error('请先选择因子树')
        return
      }

      const response = await request.post<ApiResponse<FactorTreeNode>>(
        `${API_BASE}/${treeId}/nodes`,
        {
          parentId: nodeForm.parentId,
          nodeName: nodeForm.nodeName,
          nodeType: nodeForm.nodeType,
          // 将字符串ID转换为数字
          factorId: nodeForm.nodeType === 'FACTOR' ? Number(nodeForm.factorId) : null,
          description: nodeForm.description || undefined
        }
      )

      if (response.data.code === 200 || response.data.code === 0) {
        ElMessage.success('节点添加成功')
        showAddNodeDialog.value = false
        resetNodeForm()
        if (selectedTreeId.value) {
          await loadTreeStructure(selectedTreeId.value)
        }
      } else {
        ElMessage.error(response.data.message || '添加节点失败')
      }
    } catch (error) {
      console.error('添加节点失败:', error)
      ElMessage.error('添加节点失败')
    }
  })
}

// 编辑节点准备已移除，直接在handleUpdateNode中处理

// 处理更新节点 (UpdateNodeRequest)
async function handleUpdateNode() {
  if (!editNodeFormRef.value || !editNodeForm.nodeId) {
    ElMessage.error('节点ID无效，无法更新')
    return
  }

  // 验证nodeId是否有效
  const nodeId = Number(editNodeForm.nodeId)
  if (typeof nodeId !== 'number' || isNaN(nodeId)) {
    console.warn('无效的节点ID，无法更新节点:', editNodeForm.nodeId)
    ElMessage.error('节点ID无效，无法更新')
    return
  }

  await editNodeFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      const response = await request.put<ApiResponse<FactorTreeNode>>(
        `${API_BASE}/nodes/${nodeId}`,
        {
          nodeName: editNodeForm.nodeName,
          description: editNodeForm.description || undefined,
          sortOrder: editNodeForm.sortOrder
        }
      )

      if (response.data.code === 200 || response.data.code === 0) {
        ElMessage.success('节点更新成功')
        showEditNodeDialog.value = false
        if (selectedTreeId.value) {
          await loadTreeStructure(selectedTreeId.value)
        }
      } else {
        ElMessage.error(response.data.message || '更新节点失败')
      }
    } catch (error) {
      console.error('更新节点失败:', error)
      ElMessage.error('更新节点失败')
    }
  })
}

// 移动节点准备已移除，直接在handleMoveNode中处理

// 处理移动节点 (MoveNodeRequest)
async function handleMoveNode() {
  if (!moveNodeForm.nodeId || !moveNodeForm.newParentId) {
    ElMessage.error('请输入目标父节点ID和要移动的节点ID')
    return
  }

  // 验证nodeId和newParentId是否有效
  const nodeId = Number(moveNodeForm.nodeId)
  const newParentId = Number(moveNodeForm.newParentId)

  if (typeof nodeId !== 'number' || isNaN(nodeId) || typeof newParentId !== 'number' || isNaN(newParentId)) {
    console.warn('无效的节点ID或父节点ID，无法移动节点:', { nodeId: moveNodeForm.nodeId, newParentId: moveNodeForm.newParentId })
    ElMessage.error('节点ID或父节点ID无效，无法移动')
    return
  }

  try {
    const response = await request.put<ApiResponse<boolean>>(`${API_BASE}/nodes/move`, {
      nodeId,
      newParentId,
      sortOrder: moveNodeForm.sortOrder
    })

    if (response.data.code === 200 || response.data.code === 0) {
      ElMessage.success('节点移动成功')
      showMoveNodeDialog.value = false
      // 刷新树结构
      if (selectedTreeId.value) {
        await loadTreeStructure(selectedTreeId.value)
      }
    } else {
      ElMessage.error(response.data.message || '移动节点失败')
    }
  } catch (error) {
    console.error('移动节点失败:', error)
    ElMessage.error('移动节点失败')
  }
}

// 删除节点
function deleteNode(node: FactorTreeNode) {
  ElMessageBox.confirm(
    `确定要删除节点"${node.nodeName}"吗？只能删除空节点（无子节点）。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 处理节点ID，兼容后端的treeid和前端的id字段
      const nodeId = node.id || (node as unknown as Record<string, unknown>).treeid || undefined

      // 验证nodeId是否有效
      if (!nodeId || typeof nodeId !== 'number' || isNaN(nodeId)) {
        console.warn('无效的节点ID，无法删除节点:', nodeId)
        ElMessage.error('节点ID无效，无法删除')
        return
      }

      const response = await request.delete<ApiResponse<boolean>>(`${API_BASE}/nodes/${nodeId}`)

      if (response.data.code === 200 || response.data.code === 0) {
        ElMessage.success('节点删除成功')
        if (selectedTreeId.value) {
          await loadTreeStructure(selectedTreeId.value)
        }
      } else {
        ElMessage.error(response.data.message || '删除节点失败')
      }
    } catch (error) {
      console.error('删除节点失败:', error)
      ElMessage.error('删除节点失败，该节点可能有子节点')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 查看节点路径功能暂未使用

// 搜索
async function handleSearch() {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  try {
    const endpoint = searchType.value === 'tree' ? '/search/trees' : '/search/factors'
    const response = await request.get<ApiResponse<FactorTreeNode[]>>(
      `${API_BASE}${endpoint}`,
      { params: { keyword: searchKeyword.value } }
    )

    if ((response.data.code === 200 || response.data.code === 0) && response.data.data) {
      // 映射结果
      searchResults.value = (response.data.data || []).map(item => ({
        ...item,
        nodeName: String(item.nodeName || item.treeName || '未命名')
      }))
      showSearchDialog.value = true

      if (response.data.data.length === 0) {
        ElMessage.info('未找到匹配的结果')
      }
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
  }
}

// 选择搜索结果
async function selectSearchResult(result: FactorTreeNode) {
  if (result.nodeType === 'TREE') {
    await selectTree(result)
  } else if (result.treeId) {
    selectedTreeId.value = result.treeId
    await loadTreeStructure(result.treeId)
  }
  showSearchDialog.value = false
}

// 树操作
function expandAll() {
  const nodes = treeRef.value?.store.nodesMap
  if (nodes) {
    Object.values(nodes).forEach((node: { expanded: boolean }) => {
      node.expanded = true
    })
  }
}

function collapseAll() {
  const nodes = treeRef.value?.store.nodesMap
  if (nodes) {
    Object.values(nodes).forEach((node: { expanded: boolean }) => {
      node.expanded = false
    })
  }
}

// 场景切换
function handleSceneChange() {
  loadTreeList()
}

// 打开创建因子树对话框
function openCreateTreeDialog() {
  showCreateTreeDialog.value = true
}

// 重置表单
function resetTreeForm() {
  treeForm.treeName = ''
  treeForm.description = ''
  treeForm.sceneId = ''
  treeFormRef.value?.clearValidate()
}

function resetNodeForm() {
  nodeForm.parentId = null
  nodeForm.parentName = ''
  nodeForm.nodeName = ''
  nodeForm.nodeType = ''
  nodeForm.factorId = ''
  nodeForm.description = ''
  nodeForm.treeId = undefined
  nodeFormRef.value?.clearValidate()
}

function resetSceneForm() {
  sceneForm.sceneId = ''
  sceneForm.sceneName = ''
  sceneForm.sceneDesc = ''
  sceneFormRef.value?.clearValidate()
}

// 初始化
onMounted(async () => {
  await Promise.all([loadScenes(), loadTreeList()])
})
</script>

<style scoped>
.factor-tree-management {
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

.tree-list-card,
.tree-structure-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.tree-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  margin-bottom: 8px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.tree-item:hover {
  background-color: #f5f7fa;
  border-color: #409eff;
}

.tree-item.active {
  background-color: #ecf5ff;
  border-color: #409eff;
}

.tree-item-content {
  display: flex;
  align-items: center;
  flex: 1;
}

.tree-icon {
  font-size: 20px;
  color: #409eff;
  margin-right: 12px;
}

.tree-info {
  flex: 1;
}

.tree-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.tree-desc {
  font-size: 12px;
  color: #909399;
}

.tree-item-actions {
  opacity: 0;
  transition: opacity 0.3s;
}

.tree-item:hover .tree-item-actions {
  opacity: 1;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 8px;
}

.node-content {
  display: flex;
  align-items: center;
  flex: 1;
}

.node-icon {
  margin-right: 8px;
}

.node-icon.tree-type {
  color: #409eff;
}

.node-icon.group-type {
  color: #67c23a;
}

.node-icon.factor-type {
  color: #e6a23c;
}

.node-label {
  font-size: 14px;
}

.node-actions {
  opacity: 0;
  transition: opacity 0.3s;
}

.tree-node:hover .node-actions {
  opacity: 1;
}

.form-tip {
    font-size: 12px;
    color: #999;
    margin-top: 5px;
}
</style>
