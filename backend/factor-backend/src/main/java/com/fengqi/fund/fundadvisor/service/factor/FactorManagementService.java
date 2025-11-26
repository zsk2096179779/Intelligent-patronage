package com.fengqi.fund.fundadvisor.service.factor;

import com.fengqi.fund.fundadvisor.dto.factor.FactorManagementRequest;
import com.fengqi.fund.fundadvisor.dto.factor.FactorManagementResponse;

import java.util.List;
import java.util.Map;

/**
 * 因子管理综合服务接口
 * 整合基础因子、衍生因子、权重配置、因子树等所有操作
 */
public interface FactorManagementService {

    // ==================== 基础因子操作 ====================



    /**
     * 更新基础因子
     */
    FactorManagementResponse updateBaseFactor(Integer baseId, FactorManagementRequest.BaseFactorInfo baseFactorInfo);

    /**
     * 删除基础因子
     */
    FactorManagementResponse deleteBaseFactor(Integer baseId);

    /**
     * 查询基础因子
     */
    FactorManagementResponse getBaseFactor(Integer baseId);

    /**
     * 查询所有基础因子
     */
    FactorManagementResponse getAllBaseFactors();

    // ==================== 衍生因子操作 ====================

    /**
     * 创建衍生因子（包含权重配置）
     */
    FactorManagementResponse createDerivedFactor(FactorManagementRequest.DerivedFactorInfo derivedFactorInfo);

    /**
     * 更新衍生因子
     */
    FactorManagementResponse updateDerivedFactor(Integer derivedId, FactorManagementRequest.DerivedFactorInfo derivedFactorInfo);

    /**
     * 删除衍生因子
     */
    FactorManagementResponse deleteDerivedFactor(Integer derivedId);

    /**
     * 查询衍生因子
     */
    FactorManagementResponse getDerivedFactor(Integer derivedId);

    /**
     * 查询所有衍生因子
     */
    FactorManagementResponse getAllDerivedFactors();

    // ==================== 权重配置操作 ====================

    /**
     * 配置因子权重
     */
    FactorManagementResponse configureFactorWeights(FactorManagementRequest.WeightConfigInfo weightConfigInfo);

    /**
     * 更新因子权重
     */
    FactorManagementResponse updateFactorWeights(Integer derivedId, FactorManagementRequest.WeightConfigInfo weightConfigInfo);

    /**
     * 验证权重配置
     */
    FactorManagementResponse validateFactorWeights(FactorManagementRequest.WeightConfigInfo weightConfigInfo);

    /**
     * 归一化权重
     */
    Map<Integer, Double> normalizeWeights(Map<Integer, Double> weights);

    // ==================== 因子树操作 ====================

    /**
     * 创建因子树
     */
    FactorManagementResponse createFactorTree(String sceneId, String treeName, String description);

    /**
     * 添加因子树节点
     */
    FactorManagementResponse addFactorTreeNode(FactorManagementRequest.TreeOperationInfo treeInfo);

    /**
     * 更新因子树节点
     */
    FactorManagementResponse updateFactorTreeNode(Integer nodeId, FactorManagementRequest.TreeOperationInfo treeInfo);

    /**
     * 删除因子树节点
     */
    FactorManagementResponse deleteFactorTreeNode(Integer nodeId);

    /**
     * 移动因子树节点
     */
    FactorManagementResponse moveFactorTreeNode(Integer nodeId, Integer newParentId, Integer newSortOrder);

    /**
     * 查询因子树
     */
    FactorManagementResponse getFactorTree(String sceneId);

    /**
     * 查询因子树节点
     */
    FactorManagementResponse getFactorTreeNode(Integer nodeId);

    // ==================== 场景管理操作 ====================

    /**
     * 查询所有场景
     */
    FactorManagementResponse getAllScenes();

    /**
     * 创建场景
     */
    FactorManagementResponse createScene(String sceneId, String sceneName, String sceneDesc, Integer managerUserId);

    // ==================== 综合操作 ====================

    /**
     * 统一的因子管理操作入口
     */
    FactorManagementResponse processFactorManagement(FactorManagementRequest request);



    /**
     * 验证因子数据完整性
     */
    FactorManagementResponse validateFactorData(List<Integer> baseFactorIds);

    /**
     * 搜索因子
     */
    FactorManagementResponse searchFactors(String keyword, String type, String sceneId);

    /**
     * 获取因子统计信息
     */
    FactorManagementResponse getFactorStatistics();

    // ==================== 衍生因子创建流程支持 ====================

    /**
     * 获取可用于创建衍生因子的基础因子列表
     */
    FactorManagementResponse getAvailableBaseFactorsForDerived();

    /**
     * 预览衍生因子计算结果
     */
    FactorManagementResponse previewDerivedFactor(List<Integer> baseFactorIds, Map<Integer, Double> weights, Integer calcStrategyId);

    /**
     * 完整的衍生因子创建流程（包含验证、创建、挂树等）
     */
    FactorManagementResponse createDerivedFactorWithFullFlow(FactorManagementRequest request);

    // ==================== 风格标签相关操作 ====================

    /**
     * 根据风格标签ID查询关联的衍生因子
     */
    FactorManagementResponse getDerivedFactorsByStyleTagId(Integer tagId);

    /**
     * 根据多个风格标签ID查询关联的衍生因子（满足任一标签即可）
     */
    FactorManagementResponse getDerivedFactorsByStyleTagIds(List<Integer> tagIds);

    /**
     * 根据风格标签编码查询关联的衍生因子
     */
    FactorManagementResponse getDerivedFactorsByStyleTagCode(String tagCode);

    /**
     * 根据多个风格标签编码查询关联的衍生因子（满足任一标签即可）
     */
    FactorManagementResponse getDerivedFactorsByStyleTagCodes(List<String> tagCodes);
}