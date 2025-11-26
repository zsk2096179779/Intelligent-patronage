<template>
  <div class="main-container">
    <div class="left-panel">
      <div class="factor-tree-title">可选基础因子</div>
      <el-tree
        ref="treeRef"
        :data="factorTree"
        node-key="id"
        :props="treeProps"
        show-checkbox
        default-expand-all
        :check-strictly="true"
        @check="handleCheck"
      >
        <template #default="{ node, data }">
          <span class="custom-tree-node">
            <span>{{ node.label }}</span>
            <span v-if="data.isLeaf" class="node-tag">因子</span>
            <span v-else class="node-tag folder">目录</span>
          </span>
        </template>
      </el-tree>
    </div>

    <div class="right-panel">
      <el-card>
        <template #header>
          <div class="card-header">
              <span class="header-title">{{ isEditMode ? '衍生因子编辑' : '衍生因子创建向导' }}</span>
              <div class="header-actions">
                <el-button @click="onCancel">取消</el-button>
                <el-button type="warning" plain @click="validateData" :loading="validating">数据校验</el-button>
                <el-button type="success" plain @click="previewFormula" :disabled="!weights.length">预览结果</el-button>
              </div>
            </div>
        </template>

        <el-steps :active="step" finish-status="success" align-center class="custom-steps">
          <el-step title="选择成分因子" description="勾选左侧基础因子" />
          <el-step title="参数配置" description="设置权重与属性" />
        </el-steps>

        <div v-if="step === 0" class="step-content">
          <div class="selected-panel">
            <div class="panel-header-row">
              <div class="panel-title">已选成分因子（{{ selectedFactors.length }}）</div>
              <el-button type="primary" size="small" @click="clearSelection" link>清空选择</el-button>
            </div>

            <el-table :data="selectedFactors" border stripe size="small" height="400">
              <el-table-column label="因子名称" min-width="160" prop="label" />
              <el-table-column label="ID" width="80" prop="baseId" align="center" />
              <el-table-column label="描述" min-width="200" show-overflow-tooltip prop="description" />
              <el-table-column fixed="right" label="操作" width="70" align="center">
                <template #default="{ $index }">
                  <el-button type="danger" icon="Delete" circle size="small" @click="removeSelected($index)" />
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="step-btns">
            <el-button @click="onCancel">取消</el-button>
            <el-button type="primary" :disabled="selectedFactors.length === 0" @click="goNextStep">下一步</el-button>
          </div>
        </div>

        <div v-else class="step-content">
          <el-form ref="formRef" :model="formData" :rules="rules" label-width="110px" class="config-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="因子名称" prop="factorName">
                  <el-input v-model="formData.factorName" placeholder="如：估值综合因子" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="因子编码" prop="factorCode">
                  <el-input v-model="formData.factorCode" placeholder="如：VAL_COM" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                 <el-form-item label="挂载目录" prop="targetParentId" required>
                  <el-tree-select
                    v-model="formData.targetParentId"
                    :data="folderTree"
                    :props="treeSelectProps"
                    placeholder="请选择存放因子的文件夹"
                    style="width: 100%"
                    filterable
                    clearable
                    :multiple="false"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="场景ID" prop="sceneId">
                  <el-select
                    v-model="formData.sceneId"
                    placeholder="请选择场景"
                    style="width: 100%"
                  >
                    <el-option label="股票" value="EQUITY" />
                    <el-option label="债券" value="BOND" />
                    <el-option label="基金" value="FUND" />
                    <el-option label="自定义" value="CUSTOM" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="风格标签">
                   <el-select
                      v-model="formData.styleTagCodes"
                      multiple
                      collapse-tags
                      placeholder="请选择"
                      style="width: 100%"
                   >
                    <el-option
                      v-for="tag in availableStyleTags"
                      :key="tag.tagCode"
                      :label="tag.tagName"
                      :value="tag.tagCode"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="因子说明">
              <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="请输入计算逻辑或业务含义" />
            </el-form-item>

            <el-divider content-position="left">权重策略</el-divider>

            <el-form-item label="策略方式">
              <el-radio-group v-model="strategy" @change="handleStrategyChange">
                <el-radio-button label="equal">等权分配</el-radio-button>
                <el-radio-button label="custom">自定义权重</el-radio-button>
                <el-radio-button label="mcap">市值加权(模拟)</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <div class="weight-config-container">
              <div class="weight-header">
                <div class="weight-summary">
                  <span>总权重: </span>
                  <span :class="{'text-success': isWeightValid, 'text-danger': !isWeightValid}">
                    {{ formattedWeightSum }}%
                  </span>
                  <span v-if="!isWeightValid" class="weight-error-msg">
                    (需等于 100%，差值: {{ (weightSum.value - 100).toFixed(1) }}%)
                  </span>
                </div>
                <div class="weight-actions">
                  <el-button 
                    size="small" 
                    type="primary" 
                    plain
                    @click="autoDistributeWeights"
                    v-if="weights.length > 0"
                  >
                    等权分配
                  </el-button>
                </div>
              </div>
              
              <el-alert
                v-if="weights.length === 0"
                type="warning"
                show-icon
                :closable="false"
                class="mb-2"
              >
                请先在第一步选择成分因子
              </el-alert>

              <el-table :data="weights" border size="small" height="240">
                <el-table-column prop="name" label="成分因子" min-width="150" />
                <el-table-column label="权重配置" width="350">
                  <template #default="{ row }">
                    <div class="slider-container">
                      <el-slider
                        v-model="row.weight"
                        :min="0"
                        :max="100"
                        :step="1"
                        :disabled="strategy !== 'custom'"
                        show-input
                        :show-input-controls="false"
                        input-size="small"
                      />
                      <span class="unit">%</span>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-form>

          <div class="feedback-section">
             <el-alert
              v-if="formulaPreviewText"
              type="info"
              show-icon
              :closable="false"
              class="mb-2"
            >
              <template #title>
                <div class="preview-title">公式预览</div>
              </template>
              <div class="formula-text">{{ formulaPreviewText }}</div>
            </el-alert>

            <el-alert
              v-if="lastValidation"
              :type="lastValidation.status === 'ok' ? 'success' : 'warning'"
              show-icon
              :closable="false"
            >
              <template #title>{{ lastValidation.message }}</template>
            </el-alert>
          </div>

          <div class="step-btns">
            <el-button @click="step = 0">上一步</el-button>
            <el-button 
              type="primary" 
              :loading="submitting" 
              :disabled="!isFormValid" 
              @click="onSubmit"
            >
              {{ isEditMode ? '更新衍生因子' : '提交创建' }}
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '@/utils/request';
import { ElMessage, ElTree } from 'element-plus';
import { Delete } from '@element-plus/icons-vue';

// --- 类型定义 ---
interface FactorNode {
  id: number;
  label: string;
  isLeaf: boolean;
  children?: FactorNode[];
  baseId?: number; // 基础因子的真实ID
  nodeName?: string;
  nodeType?: string;
  description?: string;
  [key: string]: unknown;
}

interface WeightItem {
  baseId: number;
  name: string;
  weight: number;
}

interface StyleTag {
  tagCode: string;
  tagName: string;
}

interface DerivedFactorData {
  derivedId: number;
  factorName: string;
  factorCode: string;
  factorDesc: string;
  baseFactors: Array<{
    baseId: number;
    factorName: string;
    factorCode: string;
    weight: number;
  }>;
  styleTagCodes?: string;
  treeNodeId?: number;
  parentId?: number;
}

// --- 配置项 ---
const API_BASE = '/api/factor'; // 使用相对路径，通过 Vite 代理
// 用于左侧树的配置
const treeProps = {
  label: 'label',
  children: 'children',
  isLeaf: 'isLeaf'
};
// 用于TreeSelect的配置（挂载目录选择器）
const treeSelectProps = {
  label: 'label',
  children: 'children',
  value: 'id'
};

// 路由相关
const route = useRoute();
const router = useRouter();

// --- 状态 ---
const treeRef = ref<InstanceType<typeof ElTree>>();
const factorTree = ref<FactorNode[]>([]);
const folderTree = ref<FactorNode[]>([]); // 仅包含目录的树，用于选择挂载点
const selectedFactors = ref<FactorNode[]>([]);
const step = ref(0);
const validating = ref(false);
const submitting = ref(false);
const isEditMode = ref(false);
const derivedId = ref<number | null>(null);

const formData = reactive({
  factorName: '',
  factorCode: '',
  description: '',
  targetParentId: undefined, // 修改为undefined以适应TreeSelect组件
  styleTagCodes: [] as string[],
  sceneId: 'EQUITY' // 场景ID，默认为EQUITY
});

const rules = {
  factorName: [{ required: true, message: '请输入因子名称', trigger: 'blur' }],
  factorCode: [
    { required: true, message: '请输入因子编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '仅支持字母、数字和下划线', trigger: 'blur' }
  ],
  targetParentId: [{ required: true, message: '请选择挂载目录', trigger: 'change' }],
  sceneId: [{ 
    required: (rule: any, value: string, callback: Function) => {
      // 当parentId为0或未指定时，sceneId是必需的
      if ((formData.targetParentId === 0 || formData.targetParentId === undefined) && !value) {
        callback(new Error('添加到根节点时必须指定场景ID'));
      } else {
        callback();
      }
    },
    trigger: 'change'
  }]
};

const availableStyleTags = ref<StyleTag[]>([]);
const weights = ref<WeightItem[]>([]);
const strategy = ref<'equal' | 'custom' | 'mcap'>('equal');
const formulaPreviewText = ref('');
const lastValidation = ref<{ status: 'ok' | 'warn'; message: string } | null>(null);

// --- 计算属性 ---
const weightSum = computed(() => weights.value.reduce((acc, cur) => acc + (cur.weight || 0), 0));
const formattedWeightSum = computed(() => weightSum.value.toFixed(0));
const isWeightValid = computed(() => Math.abs(weightSum.value - 100) < 0.1);
const isFormValid = computed(() => {
  // 基本表单验证
  const hasBasicInfo = formData.factorName && formData.factorCode && formData.targetParentId !== undefined;
  // 权重验证
  const hasWeights = weights.value.length > 0;
  const weightsValid = isWeightValid.value;
  // 根节点场景ID验证
  const sceneIdValid = !(formData.targetParentId === 0 || formData.targetParentId === undefined) || formData.sceneId;
  
  return hasBasicInfo && hasWeights && weightsValid && sceneIdValid;
});

// --- 初始化 ---
onMounted(async () => {
  // 获取路由参数
  const id = route.params.derivedId;
  if (id && !isNaN(Number(id))) {
    derivedId.value = Number(id);
    isEditMode.value = true;
  }
  
  // 加载基础数据
  await Promise.all([fetchFactorTree(), fetchStyleTags()]);
  
  // 如果是编辑模式，加载现有数据
  if (isEditMode.value && derivedId.value) {
    await fetchDerivedFactorData(derivedId.value);
  }
});

// 获取衍生因子详情数据
const fetchDerivedFactorData = async (id: number) => {
  try {
    const res = await request.get(`${API_BASE}/factor-management/derived-factors/${id}`);
    
    if (res.data?.success || res.data?.code === 200) {
      // 适配后端FactorManagementResponse结构，从derivedFactorDataList获取数据
      const factorData = res.data.data?.derivedFactorDataList?.[0];
      
      if (!factorData) {
        ElMessage.warning('未找到衍生因子数据');
        return;
      }
      
      // 填充表单数据
      formData.factorName = factorData.factorName || '';
      formData.factorCode = factorData.factorCode || '';
      formData.description = factorData.factorDesc || '';
      formData.targetParentId = factorData.parentId || undefined;
      formData.styleTagCodes = factorData.styleTagCodes ? factorData.styleTagCodes.split(',') : [];
      formData.sceneId = factorData.sceneId || 'EQUITY'; // 加载场景ID，默认为EQUITY
      
      // 填充权重数据
      if (factorData.baseFactors && Array.isArray(factorData.baseFactors)) {
        weights.value = factorData.baseFactors.map((f: any) => ({
          baseId: f.baseId,
          name: f.factorName || `因子${f.baseId}`,
          weight: (f.weight || 0) * 100 // 转为百分比
        }));
        
        // 更新策略类型
        if (weights.value.length > 0) {
          const allEqual = weights.value.every(w => Math.abs(w.weight - 100 / weights.value.length) < 0.1);
          strategy.value = allEqual ? 'equal' : 'custom';
        }
        
        // 更新步骤为第二步，直接进入编辑模式
        step.value = 1;
        
        // 如果有基础因子信息，自动选择左侧树中的对应节点
        const ids = factorData.baseFactors.map((f: any) => f.baseId);
        // 查找对应节点并选中
        const findAndCheckNodes = (nodes: FactorNode[]) => {
          nodes.forEach(node => {
            if (node.baseId && ids.includes(node.baseId)) {
              treeRef.value?.setChecked(node.id, true, false);
            }
            if (node.children) {
              findAndCheckNodes(node.children);
            }
          });
        };
        findAndCheckNodes(factorTree.value);
      } else {
        weights.value = [];
      }
      
      // 从weightConfigData获取公式预览文本
      if (res.data.data?.weightConfigData?.formulaText) {
        formulaPreviewText.value = res.data.data.weightConfigData.formulaText;
      } else {
        // 如果没有公式文本，尝试生成一个
        generatePreviewFormula();
      }
    } else {
      // 处理后端返回的错误
      const errorMsg = res.data?.message || '获取衍生因子数据失败';
      ElMessage.error(errorMsg);
    }
  } catch (e: any) {
    // 使用更具体的错误信息
    const msg = e.response?.data?.message || e.message || '获取衍生因子数据失败';
    ElMessage.error(`加载失败: ${msg}`);
  }
};

// 生成预览公式
const generatePreviewFormula = () => {
  if (weights.value.length === 0) {
    formulaPreviewText.value = '';
    return;
  }
  
  const parts = weights.value
    .filter(w => w.weight > 0)
    .map(w => `${w.name}×${w.weight.toFixed(1)}%`);
  
  formulaPreviewText.value = `${formData.factorName || 'Factor'} = ${parts.join(' + ')}`;
};

// --- API 方法 ---
// 1. 获取因子树
const fetchFactorTree = async () => {
  try {
    const res = await request.get(`${API_BASE}/factor-trees`);
    if (res.data?.code === 200 || res.data?.code === 0) {
      const trees = Array.isArray(res.data.data) ? res.data.data : [];
      if (trees.length > 0) {
        // 假设取第一棵树，或者根据业务逻辑取特定树
        const treeId = trees[0].id || trees[0].treeid;
        const structRes = await request.get(`${API_BASE}/factor-trees/${treeId}/structure`);

        const rawNodes = structRes.data.data?.nodes || structRes.data.data || [];
        // 统一格式化节点
        factorTree.value = formatTreeNodes(Array.isArray(rawNodes) ? rawNodes : [rawNodes]);
        // 生成仅目录树（用于选择挂载点）
        folderTree.value = filterFolders(factorTree.value);
      }
    }
  } catch (e) {
    console.error('加载因子树失败，尝试加载基础因子列表兜底', e);
    await fetchAvailableBaseFactors();
  }
};

// 2. 格式化树节点（适配后端各种可能的返回结构）
const formatTreeNodes = (nodes: any[]): FactorNode[] => {
  return nodes.map(node => {
    // 确定真实ID：基础因子ID优先，其次为树节点ID
    const baseId = node.factorId || node.baseId || null;
    const isLeaf = node.isLeaf || (node.children && node.children.length === 0) || !node.children;

    return {
      id: node.id || node.treeid, // 树节点ID
      baseId: baseId,             // 关联的基础因子ID
      label: node.nodeName || node.label || node.name || '未命名',
      nodeName: node.nodeName,
      nodeType: node.nodeType || (isLeaf ? 'FACTOR' : 'CATEGORY'),
      description: node.description,
      isLeaf: isLeaf,
      children: node.children ? formatTreeNodes(node.children) : [],
      disabled: !isLeaf // 在左侧树中，非叶子节点（目录）不可作为成分因子被勾选
    };
  });
};

// 3. 提取目录结构（递归过滤）
const filterFolders = (nodes: FactorNode[]): FactorNode[] => {
  return nodes
    .filter(n => !n.isLeaf || n.nodeType === 'CATEGORY') // 更宽松的过滤条件
    .map(n => ({
      ...n,
      disabled: false, // 确保目录节点可以被选择
      children: n.children ? filterFolders(n.children) : []
    }));
};

// 4. 兜底：直接获取基础因子列表
const fetchAvailableBaseFactors = async () => {
  try {
    const res = await request.get(`${API_BASE}/factor-management/derived-factors/available-base-factors`);
    if (res.data?.success) {
      const list = res.data.data?.baseFactorList || res.data.data || [];
      factorTree.value = list.map((f: any) => ({
        id: f.factorId || f.id,
        baseId: f.factorId || f.id,
        label: f.factorName || f.name,
        nodeName: f.factorName,
        isLeaf: true,
        nodeType: 'FACTOR',
        description: f.description
      }));
    }
  } catch (e) {
    ElMessage.error('无法加载基础因子数据');
  }
};

// 5. 获取风格标签
const fetchStyleTags = async () => {
  try {
    const res = await request.get(`${API_BASE}/style-tags`);
    if (res.data?.code === 200 || res.data?.code === 0) {
      const tags = Array.isArray(res.data.data) ? res.data.data : [];
      availableStyleTags.value = tags.map((t: any) => ({
        tagCode: t.tagCode,
        tagName: t.tagName
      }));
    }
  } catch (e) {
    // 容错处理
    console.warn('风格标签加载失败');
  }
};

// --- 交互逻辑 ---

const handleCheck = (_: any, { checkedNodes }: any) => {
  // 只收集叶子节点（基础因子）
  selectedFactors.value = checkedNodes.filter((n: FactorNode) => n.isLeaf && n.baseId);
};

const clearSelection = () => {
  selectedFactors.value = [];
  treeRef.value?.setCheckedKeys([]);
};

const removeSelected = (index: number) => {
  const nodeToRemove = selectedFactors.value[index];
  selectedFactors.value.splice(index, 1);
  // 同步取消树的勾选状态
  treeRef.value?.setChecked(nodeToRemove.id, false, false);
};

const goNextStep = () => {
  step.value = 1;
  // 初始化权重
  if (weights.value.length === 0 || weights.value.length !== selectedFactors.value.length) {
    const defaultWeight = Number((100 / selectedFactors.value.length).toFixed(0));
    // 重新计算确保总和为100
    const remainder = 100 - (defaultWeight * selectedFactors.value.length);

    weights.value = selectedFactors.value.map((f, i) => ({
      baseId: f.baseId!,
      name: f.label || f.nodeName || `因子${f.baseId}`,
      weight: i === selectedFactors.value.length - 1 ? defaultWeight + remainder : defaultWeight
    }));
  }
};

const handleStrategyChange = () => {
  if (strategy.value === 'equal') {
    const count = weights.value.length;
    const avg = Math.floor(100 / count);
    const last = 100 - avg * (count - 1);
    weights.value.forEach((w, i) => w.weight = i === count - 1 ? last : avg);
  } else if (strategy.value === 'mcap') {
    // 模拟市值加权
    let total = 0;
    weights.value.forEach((w, i) => {
      const raw = (weights.value.length - i) * 10;
      total += raw;
      w.weight = raw; // 暂存
    });
    // 归一化
    let currentSum = 0;
    weights.value.forEach((w, i) => {
      if (i === weights.value.length - 1) {
        w.weight = 100 - currentSum;
      } else {
        w.weight = Math.round((w.weight / total) * 100);
        currentSum += w.weight;
      }
    });
  }
};

// 6. 数据校验
const validateData = async () => {
  if (!selectedFactors.value.length) return ElMessage.warning('请先选择因子');
  validating.value = true;
  try {
    // 直接传递baseFactorIds数组作为请求体
    const ids = selectedFactors.value.map(f => f.baseId!);
    
    const res = await request.post(`${API_BASE}/factor-management/validate-data`, ids);
    
    if (res.data?.success || res.data?.code === 200) {
      lastValidation.value = {
        status: res.data.data?.success ? 'ok' : 'warn',
        message: res.data.data?.message || res.data.msg || '数据校验完成'
      };
    }
  } catch (e: any) {
    lastValidation.value = { status: 'warn', message: e.response?.data?.message || '校验服务异常' };
  } finally {
    validating.value = false;
  }
};

// 7. 公式预览
const previewFormula = async () => {
  try {
    // 准备查询参数
    const baseFactorIds = weights.value.map(w => w.baseId);
    // 构建权重映射
    const weightsMap: Record<string, number> = {};
    weights.value.forEach(w => {
      weightsMap[w.baseId] = w.weight / 100; // 转换为小数格式
    });
    
    // 构建查询字符串
    const params = new URLSearchParams();
    // 添加baseFactorIds
    baseFactorIds.forEach(id => {
      params.append('baseFactorIds', id.toString());
    });
    // 添加weights
    Object.entries(weightsMap).forEach(([id, weight]) => {
      params.append(`weights[${id}]`, weight.toString());
    });
    
    // 使用GET请求，通过查询参数传递数据
    const res = await request.get(`${API_BASE}/factor-management/derived-factors/preview?${params.toString()}`);

    if (res.data?.success) {
      // 如果后端返回了具体的计算结果数据
      if (res.data.data?.previewData) {
        formulaPreviewText.value = `示例数据: ${JSON.stringify(res.data.data.previewData).slice(0, 100)}...`;
      } else if (res.data.data?.formulaText) {
        // 使用后端返回的公式文本
        formulaPreviewText.value = res.data.data.formulaText;
      } else {
        // 生成文本公式
        const parts = weights.value
          .filter(w => w.weight > 0)
          .map(w => `${w.name}×${w.weight}%`);
        formulaPreviewText.value = `${formData.factorName || 'Factor'} = ${parts.join(' + ')}`;
      }
    }
  } catch (e: any) {
    const msg = e.response?.data?.message || '预览请求失败';
    ElMessage.error(msg);
  }
};

// 7.5 自动等权分配权重
const autoDistributeWeights = () => {
  if (weights.value.length === 0) return;
  
  // 计算每个因子的等权值
  const equalWeight = 100 / weights.value.length;
  
  // 分配权重
  weights.value.forEach((item) => {
    item.weight = equalWeight;
  });
  
  ElMessage.success('已自动等权分配权重');
  
  // 如果启用了自定义策略，需要更新策略类型
  if (strategy.value === 'equal') {
    strategy.value = 'custom';
  }
};

// 7.6 配置因子权重（独立接口）
const configureFactorWeights = async (derivedId: number) => {
  try {
    // 构建权重字符串: 'id':weight, 'id':weight
    const weightStr = weights.value
      .map(w => `'${w.baseId}': ${w.weight / 100}`) 
      .join(', ');
    
    // 构建权重配置请求体
    const payload = {
      operationType: 'CONFIGURE_FACTOR_WEIGHTS',
      weightConfigInfo: {
        baseFactorWeights: weightStr,
        weightDesc: strategy.value === 'equal' ? '等权' : '自定义权重',
        formulaRemark: '前端合成'
      }
    };
    
    const res = await request.post(`${API_BASE}/factor-management/derived-factors/${derivedId}/weights`, payload);
    
    if (res.data?.success || res.data?.code === 200) {
      ElMessage.success('权重配置成功');
      return true;
    } else {
      ElMessage.error(res.data?.message || '权重配置失败');
      return false;
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '权重配置请求失败');
    return false;
  }
};

// 7.7 验证因子权重（独立接口）
const validateFactorWeights = async (derivedId: number) => {
  try {
    // 构建权重字符串: 'id':weight, 'id':weight
    const weightStr = weights.value
      .map(w => `'${w.baseId}': ${w.weight / 100}`) 
      .join(', ');
    
    // 构建权重验证请求体
    const payload = {
      operationType: 'VALIDATE_FACTOR_WEIGHTS',
      weightConfigInfo: {
        baseFactorWeights: weightStr,
        weightDesc: strategy.value === 'equal' ? '等权' : '自定义权重'
      }
    };
    
    const res = await request.post(`${API_BASE}/factor-management/derived-factors/${derivedId}/weights/validate`, payload);
    
    if (res.data?.success || res.data?.code === 200) {
      ElMessage.success('权重验证通过');
      return true;
    } else {
      ElMessage.error(res.data?.message || '权重验证失败');
      return false;
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '权重验证请求失败');
    return false;
  }
 };

// 8. 提交创建/更新 (完整流程)
const onSubmit = async () => {
  if (!formData.factorName || !formData.factorCode || !formData.targetParentId) {
    return ElMessage.warning('请补全因子名称、编码及挂载目录');
  }
  if (!isWeightValid.value) {
    return ElMessage.error('权重总和必须为 100%');
  }

  submitting.value = true;
  try {
    // 保存原始baseFactorIds
    const originalBaseFactorIds = weights.value.map(w => w.baseId);

    if (isEditMode.value && derivedId.value) {
      // 编辑模式：先更新基本信息，然后单独配置权重
      // 1. 构建更新基本信息的请求体
      const updatePayload = {
        operationType: 'UPDATE_DERIVED_FACTOR',
        derivedFactorInfo: {
          derivedId: derivedId.value,
          factorName: formData.factorName,
          factorCode: formData.factorCode,
          factorDesc: formData.description,
          styleTagCodes: formData.styleTagCodes.join(','),
          baseFactorIds: originalBaseFactorIds,
          createUserId: 1 // 示例用户ID，实际应从 Store/Token 获取
        },
        treeOperationInfo: {
          parentId: formData.targetParentId,
          nodeName: formData.factorName,
          nodeType: 'FACTOR',
          isLeaf: true,
          sceneId: formData.sceneId || 'EQUITY'
        }
      };
      
      // 更新基本信息
      const updateRes = await request.put(`${API_BASE}/factor-management/derived-factors/${derivedId.value}`, updatePayload);
      
      if (!updateRes.data?.success && updateRes.data?.code !== 200) {
        throw new Error(updateRes.data?.message || '更新基本信息失败');
      }
      
      // 2. 使用独立接口配置权重
      await configureFactorWeights(derivedId.value);
      
      // 编辑模式成功处理
      ElMessage.success('衍生因子更新成功！');
      
      // 延迟跳转
      setTimeout(() => {
        router.push('/model2/FactorTreeManagement');
      }, 1000);
      
      return; // 编辑模式完成，提前返回
    } else {
      // 创建模式：使用完整流程创建接口
      // 1. 构建权重字符串
      const weightStr = weights.value
        .map(w => `'${w.baseId}': ${w.weight / 100}`) // 转换为 0.6 这种小数格式
        .join(', ');
      
      // 2. 构建符合 FactorManagementRequest DTO 的请求体
      const createPayload = {
        operationType: 'CREATE_DERIVED_FACTOR',
        derivedFactorInfo: {
          factorName: formData.factorName,
          factorCode: formData.factorCode,
          factorDesc: formData.description,
          calcStrategyId: 1, // 默认策略：归一化处理后的加权组合
          styleTagCodes: formData.styleTagCodes.join(','),
          baseFactorIds: originalBaseFactorIds,
          createUserId: 1 // 示例用户ID，实际应从 Store/Token 获取
        },
        weightConfigInfo: {
          baseFactorWeights: weightStr,
          weightDesc: strategy.value === 'equal' ? '等权' : '自定义权重',
          formulaRemark: '前端合成'
        },
        treeOperationInfo: {
          parentId: formData.targetParentId,
          nodeName: formData.factorName, // 树节点名称默认同因子名
          nodeType: 'FACTOR',
          isLeaf: true,
          sceneId: formData.sceneId || 'EQUITY' // 使用表单中的场景ID或默认值
        }
      };

      // 创建模式使用POST完整流程接口
      const res = await request.post(`${API_BASE}/factor-management/derived-factors`, createPayload);
      
      // 创建模式成功处理
      // 获取创建的衍生因子ID
      const newDerivedId = res.data.data?.derivedFactorDataList?.[0]?.derivedId;
      
      // 构建详细的成功消息
      let successMsg = `衍生因子创建成功！`;
      
      // 如果有权重配置信息，可以显示权重已应用的消息
      if (res.data.data?.weightConfigData) {
        successMsg += ' 权重配置已应用。';
      }
      
      ElMessage.success(successMsg);
      
      // 延迟跳转
      setTimeout(() => {
        router.push('/model2/FactorTreeManagement');
      }, 1000);
    }
  } catch (e: any) {
    const msg = e.response?.data?.message || e.message || '提交发生错误';
    ElMessage.error(msg);
  } finally {
    submitting.value = false;
  }
};

const onCancel = () => {
  // 可以是返回上一页，或者重置当前状态
  if (window.history.length > 1) {
    window.history.back();
  } else {
    step.value = 0;
    selectedFactors.value = [];
    formData.factorName = '';
  }
};
</script>

<style scoped>
.main-container {
  display: flex;
  height: calc(100vh - 84px); /* 减去顶部导航高度 */
  gap: 16px;
  padding: 16px;
  background-color: #f0f2f5;
}

.left-panel {
  width: 320px;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}

.factor-tree-title {
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #ebeef5;
  color: #303133;
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0; /* 防止flex子项溢出 */
}

.el-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border: none;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}

.el-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 20px 40px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.custom-steps {
  margin-bottom: 30px;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

/* 步骤1样式 */
.selected-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.panel-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.panel-title {
  font-weight: bold;
  color: #606266;
}

/* 步骤2样式 */
.config-form {
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
}

.weight-config-container {
  background: #f8f9fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 20px;
  margin-top: 20px;
}

.weight-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: bold;
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.weight-summary {
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 14px;
}

.weight-actions {
  display: flex;
  gap: 8px;
}

.text-success { color: #67c23a; }
.text-danger { color: #f56c6c; }
.weight-error-msg { font-size: 12px; color: #f56c6c; font-weight: normal; margin-left: 8px; }

.slider-container {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-right: 12px;
}

.slider-container .unit {
  min-width: 20px;
  text-align: right;
}

.weight-config-container .el-alert {
  margin-bottom: 15px;
}

.slider-container .el-slider {
  flex: 1;
}

.unit {
  font-size: 12px;
  color: #909399;
  width: 15px;
}

.step-btns {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  text-align: right;
  flex-shrink: 0;
}

/* 树节点样式 */
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.node-tag {
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 2px;
  background: #ecf5ff;
  color: #409eff;
  border: 1px solid #d9ecff;
}

.node-tag.folder {
  background: #f4f4f5;
  color: #909399;
  border-color: #e9e9eb;
}

.preview-title {
  font-weight: bold;
}

.formula-text {
  font-family: monospace;
  word-break: break-all;
  margin-top: 4px;
}
</style>
