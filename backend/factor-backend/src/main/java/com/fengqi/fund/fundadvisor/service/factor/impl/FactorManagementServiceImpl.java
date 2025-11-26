package com.fengqi.fund.fundadvisor.service.factor.impl;

import com.fengqi.fund.fundadvisor.dto.factor.FactorManagementRequest;
import com.fengqi.fund.fundadvisor.dto.factor.FactorManagementResponse;
import com.fengqi.fund.fundadvisor.entity.factor.*;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorManagementMapper;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorTreeMapper;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorTreeSceneMapper;
import com.fengqi.fund.fundadvisor.mapper.factor.StyleTagMapper;
import com.fengqi.fund.fundadvisor.service.factor.FactorManagementService;
import com.fengqi.fund.fundadvisor.service.factor.FactorTreeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 因子管理综合服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FactorManagementServiceImpl implements FactorManagementService {

    private final FactorManagementMapper factorManagementMapper;
    private final FactorTreeMapper factorTreeMapper;
    private final FactorTreeService factorTreeService;
    private final FactorTreeSceneMapper factorTreeSceneMapper;
    private final StyleTagMapper styleTagMapper;

    // ==================== 基础因子操作 ====================



    @Override
    @Transactional
    public FactorManagementResponse updateBaseFactor(Integer baseId, FactorManagementRequest.BaseFactorInfo baseFactorInfo) {
        try {
            FactorBase existingFactor = factorManagementMapper.selectBaseFactorById(baseId);
            if (existingFactor == null) {
                return createErrorResponse("UPDATE_BASE_FACTOR", "基础因子不存在");
            }

            // 检查编码冲突
            if (!existingFactor.getFactorCode().equals(baseFactorInfo.getFactorCode()) 
                && isFactorCodeExists(baseFactorInfo.getFactorCode())) {
                return createErrorResponse("UPDATE_BASE_FACTOR", "因子编码已存在");
            }

            existingFactor.setFactorName(baseFactorInfo.getFactorName());
            existingFactor.setFactorCode(baseFactorInfo.getFactorCode());
            existingFactor.setFactorFormula(baseFactorInfo.getFactorFormula());
            existingFactor.setDataSource(baseFactorInfo.getDataSource());
            existingFactor.setUpdateFrequency(baseFactorInfo.getUpdateFrequency());
            existingFactor.setDataStartDate(baseFactorInfo.getDataStartDate() != null ? LocalDate.parse(baseFactorInfo.getDataStartDate()) : null);
            existingFactor.setDataDesc(baseFactorInfo.getDataDesc());

            int result = factorManagementMapper.updateBaseFactor(existingFactor);
            if (result > 0) {
                FactorManagementResponse response = new FactorManagementResponse();
                response.setOperationType("UPDATE_BASE_FACTOR");
                response.setSuccess(true);
                response.setMessage("基础因子更新成功");
                response.setOperationTime(LocalDateTime.now());
                
                FactorManagementResponse.BaseFactorData baseFactorData = convertToBaseFactorData(existingFactor);
                response.setBaseFactorData(baseFactorData);
                
                return response;
            } else {
                return createErrorResponse("UPDATE_BASE_FACTOR", "基础因子更新失败");
            }
        } catch (Exception e) {
            log.error("更新基础因子失败", e);
            return createErrorResponse("UPDATE_BASE_FACTOR", "更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public FactorManagementResponse deleteBaseFactor(Integer baseId) {
        try {
            // 检查是否被衍生因子使用
            List<Map<String, Object>> relations = factorManagementMapper.selectDerivedFactorRelations(baseId);
            if (!relations.isEmpty()) {
                return createErrorResponse("DELETE_BASE_FACTOR", "该基础因子被衍生因子使用，无法删除");
            }

            int result = factorManagementMapper.deleteBaseFactor(baseId);
            if (result > 0) {
                FactorManagementResponse response = new FactorManagementResponse();
                response.setOperationType("DELETE_BASE_FACTOR");
                response.setSuccess(true);
                response.setMessage("基础因子删除成功");
                response.setOperationTime(LocalDateTime.now());
                return response;
            } else {
                return createErrorResponse("DELETE_BASE_FACTOR", "基础因子删除失败");
            }
        } catch (Exception e) {
            log.error("删除基础因子失败", e);
            return createErrorResponse("DELETE_BASE_FACTOR", "删除失败: " + e.getMessage());
        }
    }

    @Override
    public FactorManagementResponse getBaseFactor(Integer baseId) {
        try {
            FactorBase factorBase = factorManagementMapper.selectBaseFactorById(baseId);
            if (factorBase != null) {
                FactorManagementResponse response = new FactorManagementResponse();
                response.setOperationType("GET_BASE_FACTOR");
                response.setSuccess(true);
                response.setMessage("查询成功");
                response.setOperationTime(LocalDateTime.now());
                
                FactorManagementResponse.BaseFactorData baseFactorData = convertToBaseFactorData(factorBase);
                response.setBaseFactorData(baseFactorData);
                
                return response;
            } else {
                return createErrorResponse("GET_BASE_FACTOR", "基础因子不存在");
            }
        } catch (Exception e) {
            log.error("查询基础因子失败", e);
            return createErrorResponse("GET_BASE_FACTOR", "查询失败: " + e.getMessage());
        }
    }

    @Override
    public FactorManagementResponse getAllBaseFactors() {
        try {
            List<FactorBase> baseFactors = factorManagementMapper.selectAllValidBaseFactors();
            FactorManagementResponse response = new FactorManagementResponse();
            response.setOperationType("GET_ALL_BASE_FACTORS");
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setOperationTime(LocalDateTime.now());
            
            // 转换为响应数据
            List<FactorManagementResponse.BaseFactorData> baseFactorDataList = baseFactors.stream()
                    .map(this::convertToBaseFactorData)
                    .collect(Collectors.toList());
            
            // 这里可以设置列表数据，需要根据实际需求调整
            return response;
        } catch (Exception e) {
            log.error("查询所有基础因子失败", e);
            return createErrorResponse("GET_ALL_BASE_FACTORS", "查询失败: " + e.getMessage());
        }
    }

    // ==================== 衍生因子操作 ====================

    @Override
    @Transactional
    public FactorManagementResponse createDerivedFactor(FactorManagementRequest.DerivedFactorInfo derivedFactorInfo) {
        try {
            // 验证编码是否已存在
            if (isFactorCodeExists(derivedFactorInfo.getFactorCode())) {
                return createErrorResponse("CREATE_DERIVED_FACTOR", "因子编码已存在: " + derivedFactorInfo.getFactorCode() + ", 请使用其他编码");
            }

            // 不再强制要求基础因子（可在后续步骤中配置）

            // 计算策略现在是可选的，不再强制要求

            // 处理风格标签编码转换为ID
            String styleTagIds = null;
            if (derivedFactorInfo.getStyleTagCodes() != null && !derivedFactorInfo.getStyleTagCodes().trim().isEmpty()) {
                // 将 styleTagCodes 转换为 styleTagIds
                styleTagIds = convertStyleTagCodesToIds(derivedFactorInfo.getStyleTagCodes());
                if (styleTagIds == null) {
                    return createErrorResponse("CREATE_DERIVED_FACTOR", "风格标签编码转换失败，请检查编码是否正确");
                }
            }

            FactorDerived factorDerived = new FactorDerived();
            factorDerived.setFactorName(derivedFactorInfo.getFactorName());
            factorDerived.setFactorCode(derivedFactorInfo.getFactorCode());
            factorDerived.setFactorDesc(derivedFactorInfo.getFactorDesc());
            factorDerived.setCalcStrategyId(derivedFactorInfo.getCalcStrategyId());
            factorDerived.setStyleTagIds(styleTagIds);
            factorDerived.setTreeNodeId(derivedFactorInfo.getTreeNodeId());
            factorDerived.setCreateUserId(derivedFactorInfo.getCreateUserId());
            factorDerived.setIsValid(true);

            // 创建衍生因子
            int result = factorManagementMapper.insertDerivedFactor(factorDerived);
            if (result > 0) {
                // 只有当提供了基础因子时才创建默认权重配置
                if (derivedFactorInfo.getBaseFactorIds() != null && !derivedFactorInfo.getBaseFactorIds().isEmpty()) {
                    List<DerivedFactor> derivedFactors = createEqualWeightRelations(factorDerived.getDerivedId(), 
                            derivedFactorInfo.getBaseFactorIds(), null); // 使用null作为默认策略
                    
                    if (!derivedFactors.isEmpty()) {
                        factorManagementMapper.batchInsertDerivedFactors(derivedFactors);
                    }
                }

                FactorManagementResponse response = new FactorManagementResponse();
                response.setOperationType("CREATE_DERIVED_FACTOR");
                response.setSuccess(true);
                response.setMessage("衍生因子创建成功");
                response.setOperationTime(LocalDateTime.now());
                
                FactorManagementResponse.DerivedFactorData derivedFactorData = 
                        convertToDerivedFactorData(factorDerived, 
                                derivedFactorInfo.getBaseFactorIds() != null ? derivedFactorInfo.getBaseFactorIds() : Collections.emptyList());
                response.setDerivedFactorDataList(Collections.singletonList(derivedFactorData));
                
                return response;
            } else {
                return createErrorResponse("CREATE_DERIVED_FACTOR", "衍生因子创建失败");
            }
        } catch (Exception e) {
            log.error("创建衍生因子失败", e);
            return createErrorResponse("CREATE_DERIVED_FACTOR", "创建失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public FactorManagementResponse updateDerivedFactor(Integer derivedId, FactorManagementRequest.DerivedFactorInfo derivedFactorInfo) {
        try {
            FactorDerived existingDerived = factorManagementMapper.selectDerivedFactorById(derivedId);
            if (existingDerived == null) {
                return createErrorResponse("UPDATE_DERIVED_FACTOR", "衍生因子不存在");
            }

            // 检查编码冲突
            if (!existingDerived.getFactorCode().equals(derivedFactorInfo.getFactorCode()) 
                && isFactorCodeExists(derivedFactorInfo.getFactorCode())) {
                return createErrorResponse("UPDATE_DERIVED_FACTOR", "因子编码已存在");
            }

            // 处理风格标签编码转换为ID
            if (derivedFactorInfo.getStyleTagCodes() != null && !derivedFactorInfo.getStyleTagCodes().trim().isEmpty()) {
                // 将 styleTagCodes 转换为 styleTagIds
                String styleTagIds = convertStyleTagCodesToIds(derivedFactorInfo.getStyleTagCodes());
                if (styleTagIds == null) {
                    return createErrorResponse("UPDATE_DERIVED_FACTOR", "风格标签编码转换失败，请检查编码是否正确");
                }
                existingDerived.setStyleTagIds(styleTagIds);
            }
            // 如果没有传 styleTagCodes，保持原有值不变

            existingDerived.setFactorName(derivedFactorInfo.getFactorName());
            existingDerived.setFactorCode(derivedFactorInfo.getFactorCode());
            existingDerived.setFactorDesc(derivedFactorInfo.getFactorDesc());
            existingDerived.setCalcStrategyId(derivedFactorInfo.getCalcStrategyId());
            existingDerived.setTreeNodeId(derivedFactorInfo.getTreeNodeId());

            int result = factorManagementMapper.updateDerivedFactor(existingDerived);
            if (result > 0) {
                FactorManagementResponse response = new FactorManagementResponse();
                response.setOperationType("UPDATE_DERIVED_FACTOR");
                response.setSuccess(true);
                response.setMessage("衍生因子更新成功");
                response.setOperationTime(LocalDateTime.now());
                
                // 获取关联的基础因子
                List<Map<String, Object>> relations = factorManagementMapper.selectDerivedFactorRelations(derivedId);
                List<Integer> baseFactorIds = relations.stream()
                        .map(relation -> (Integer) relation.get("base_id"))
                        .collect(Collectors.toList());
                
                FactorManagementResponse.DerivedFactorData derivedFactorData = 
                        convertToDerivedFactorData(existingDerived, baseFactorIds);
                response.setDerivedFactorDataList(Collections.singletonList(derivedFactorData));
                
                return response;
            } else {
                return createErrorResponse("UPDATE_DERIVED_FACTOR", "衍生因子更新失败");
            }
        } catch (Exception e) {
            log.error("更新衍生因子失败", e);
            return createErrorResponse("UPDATE_DERIVED_FACTOR", "更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public FactorManagementResponse deleteDerivedFactor(Integer derivedId) {
        try {
            // 删除关联关系
            factorManagementMapper.deleteDerivedFactorRelations(derivedId);
            
            // 删除衍生因子
            int result = factorManagementMapper.deleteDerivedFactor(derivedId);
            if (result > 0) {
                FactorManagementResponse response = new FactorManagementResponse();
                response.setOperationType("DELETE_DERIVED_FACTOR");
                response.setSuccess(true);
                response.setMessage("衍生因子删除成功");
                response.setOperationTime(LocalDateTime.now());
                return response;
            } else {
                return createErrorResponse("DELETE_DERIVED_FACTOR", "衍生因子删除失败");
            }
        } catch (Exception e) {
            log.error("删除衍生因子失败", e);
            return createErrorResponse("DELETE_DERIVED_FACTOR", "删除失败: " + e.getMessage());
        }
    }

    @Override
    public FactorManagementResponse getDerivedFactor(Integer derivedId) {
        try {
            FactorDerived factorDerived = factorManagementMapper.selectDerivedFactorById(derivedId);
            if (factorDerived != null) {
                FactorManagementResponse response = new FactorManagementResponse();
                response.setOperationType("GET_DERIVED_FACTOR");
                response.setSuccess(true);
                response.setMessage("查询成功");
                response.setOperationTime(LocalDateTime.now());
                
                // 获取关联的基础因子
                List<Map<String, Object>> relations = factorManagementMapper.selectDerivedFactorRelations(derivedId);
                List<Integer> baseFactorIds = relations.stream()
                        .map(relation -> (Integer) relation.get("base_id"))
                        .collect(Collectors.toList());
                
                FactorManagementResponse.DerivedFactorData derivedFactorData = 
                        convertToDerivedFactorData(factorDerived, baseFactorIds);
                response.setDerivedFactorDataList(Collections.singletonList(derivedFactorData));
                
                return response;
            } else {
                return createErrorResponse("GET_DERIVED_FACTOR", "衍生因子不存在");
            }
        } catch (Exception e) {
            log.error("查询衍生因子失败", e);
            return createErrorResponse("GET_DERIVED_FACTOR", "查询失败: " + e.getMessage());
        }
    }

    @Override
    public FactorManagementResponse getAllDerivedFactors() {
        try {
            List<Map<String, Object>> derivedFactors = factorManagementMapper.selectAllValidDerivedFactors();
            FactorManagementResponse response = new FactorManagementResponse();
            response.setOperationType("GET_ALL_DERIVED_FACTORS");
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setOperationTime(LocalDateTime.now());
            return response;
        } catch (Exception e) {
            log.error("查询所有衍生因子失败", e);
            return createErrorResponse("GET_ALL_DERIVED_FACTORS", "查询失败: " + e.getMessage());
        }
    }

    // ==================== 权重配置操作 ====================

    @Override
    @Transactional
    public FactorManagementResponse configureFactorWeights(FactorManagementRequest.WeightConfigInfo weightConfigInfo) {
        try {
            // 调试日志：记录接收到的权重配置信息
            log.info("开始配置因子权重，derivedId: {}, 权重字符串: {}, 权重描述: {}", 
                    weightConfigInfo.getDerivedId(), 
                    weightConfigInfo.getBaseFactorWeights(),
                    weightConfigInfo.getWeightDesc());
            
            // 验证权重
            FactorManagementResponse validationResponse = validateFactorWeights(weightConfigInfo);
            if (!validationResponse.getSuccess()) {
                log.warn("权重验证失败: {}", validationResponse.getMessage());
                return validationResponse;
            }
            log.info("权重验证通过");

            // 解析权重字符串为Map
            Map<Integer, Double> weightsMap = parseWeightsString(weightConfigInfo.getBaseFactorWeights());
            log.info("权重字符串解析结果: {}", weightsMap);
            
            if (weightsMap == null || weightsMap.isEmpty()) {
                log.error("权重配置为空或格式错误");
                return createErrorResponse("CONFIGURE_FACTOR_WEIGHTS", "权重配置为空或格式错误");
            }

            // 归一化权重
            Map<Integer, Double> normalizedWeights = normalizeWeights(weightsMap);
            log.info("权重归一化结果: {}", normalizedWeights);

            // 删除原有配置
            log.info("删除derivedId={}的原有权重配置", weightConfigInfo.getDerivedId());
            factorManagementMapper.deleteDerivedFactorRelations(weightConfigInfo.getDerivedId());

            // 创建新的权重配置
            List<DerivedFactor> derivedFactors = new ArrayList<>();
            for (Map.Entry<Integer, Double> entry : normalizedWeights.entrySet()) {
                log.info("配置基础因子ID: {}, 权重值: {}", entry.getKey(), entry.getValue());
                DerivedFactor derivedFactor = new DerivedFactor();
                derivedFactor.setDerivedId(weightConfigInfo.getDerivedId());
                derivedFactor.setBaseId(entry.getKey());
                derivedFactor.setWeight(entry.getValue());
                derivedFactor.setWeightDesc(weightConfigInfo.getWeightDesc());
                derivedFactor.setBaseDataCheckStatus("PASSED");
                derivedFactor.setCheckTime(LocalDateTime.now());
                derivedFactor.setFormulaRemark(weightConfigInfo.getFormulaRemark());
                derivedFactors.add(derivedFactor);
            }

            log.info("准备批量插入 {} 条权重配置记录", derivedFactors.size());
            int result = factorManagementMapper.batchInsertDerivedFactors(derivedFactors);
            log.info("权重配置批量插入结果: 成功插入 {} 条记录", result);
            
            if (result > 0) {
                FactorManagementResponse response = new FactorManagementResponse();
                response.setOperationType("CONFIGURE_FACTOR_WEIGHTS");
                response.setSuccess(true);
                response.setMessage("权重配置成功");
                response.setOperationTime(LocalDateTime.now());
                
                FactorManagementResponse.WeightConfigData weightConfigData = new FactorManagementResponse.WeightConfigData();
                weightConfigData.setDerivedId(weightConfigInfo.getDerivedId());
                weightConfigData.setBaseFactorWeights(normalizedWeights);
                weightConfigData.setTotalWeight(normalizedWeights.values().stream().mapToDouble(Double::doubleValue).sum());
                weightConfigData.setWeightDesc(weightConfigInfo.getWeightDesc());
                weightConfigData.setFormulaRemark(weightConfigInfo.getFormulaRemark());
                weightConfigData.setBaseDataCheckStatus("PASSED");
                weightConfigData.setCheckTime(LocalDateTime.now());
                
                response.setWeightConfigData(weightConfigData);
                
                return response;
            } else {
                return createErrorResponse("CONFIGURE_FACTOR_WEIGHTS", "权重配置失败");
            }
        } catch (Exception e) {
            log.error("配置权重失败", e);
            return createErrorResponse("CONFIGURE_FACTOR_WEIGHTS", "配置失败: " + e.getMessage());
        }
    }

    @Override
    public FactorManagementResponse validateFactorWeights(FactorManagementRequest.WeightConfigInfo weightConfigInfo) {
        try {
            List<String> errorMessages = new ArrayList<>();
            List<String> warningMessages = new ArrayList<>();

            // 解析权重字符串
            Map<Integer, Double> weightsMap = parseWeightsString(weightConfigInfo.getBaseFactorWeights());
            if (weightsMap == null || weightsMap.isEmpty()) {
                errorMessages.add("权重配置为空或格式错误，请提供有效的JSON格式权重配置");
                
                FactorManagementResponse response = new FactorManagementResponse();
                response.setOperationType("VALIDATE_FACTOR_WEIGHTS");
                response.setSuccess(false);
                response.setMessage("验证失败");
                response.setOperationTime(LocalDateTime.now());
                
                FactorManagementResponse.ValidationResult validationResult = new FactorManagementResponse.ValidationResult();
                validationResult.setValid(false);
                validationResult.setErrorMessages(errorMessages);
                validationResult.setWarningMessages(warningMessages);
                
                response.setValidationResult(validationResult);
                
                return response;
            }

            // 验证权重值范围
            for (Map.Entry<Integer, Double> entry : weightsMap.entrySet()) {
                if (entry.getValue() < 0 || entry.getValue() > 1) {
                    errorMessages.add("基础因子ID " + entry.getKey() + " 的权重值必须在0-1之间");
                }
            }

            // 验证权重总和
            double totalWeight = weightsMap.values().stream()
                    .mapToDouble(Double::doubleValue).sum();
            if (Math.abs(totalWeight - 1.0) > 0.0001) {
                warningMessages.add("权重总和为 " + totalWeight + "，将自动归一化为1.0");
            }

            FactorManagementResponse response = new FactorManagementResponse();
            response.setOperationType("VALIDATE_FACTOR_WEIGHTS");
            response.setSuccess(errorMessages.isEmpty());
            response.setMessage(errorMessages.isEmpty() ? "验证通过" : "验证失败");
            response.setOperationTime(LocalDateTime.now());
            
            FactorManagementResponse.ValidationResult validationResult = new FactorManagementResponse.ValidationResult();
            validationResult.setValid(errorMessages.isEmpty());
            validationResult.setErrorMessages(errorMessages);
            validationResult.setWarningMessages(warningMessages);
            
            response.setValidationResult(validationResult);
            
            return response;
        } catch (Exception e) {
            log.error("验证权重失败", e);
            return createErrorResponse("VALIDATE_FACTOR_WEIGHTS", "验证失败: " + e.getMessage());
        }
    }

    @Override
    public Map<Integer, Double> normalizeWeights(Map<Integer, Double> weights) {
        double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalWeight == 0) {
            return weights;
        }

        Map<Integer, Double> normalizedWeights = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : weights.entrySet()) {
            normalizedWeights.put(entry.getKey(), entry.getValue() / totalWeight);
        }
        return normalizedWeights;
    }

    // ==================== 因子树操作 ====================

    @Override
    @Transactional
    public FactorManagementResponse createFactorTree(String sceneId, String treeName, String description) {
        try {
            // 检查场景是否存在
            FactorTreeScene scene = factorManagementMapper.selectSceneById(sceneId);
            if (scene == null) {
                return createErrorResponse("CREATE_FACTOR_TREE", "场景不存在");
            }

            FactorTree rootNode = new FactorTree();
            rootNode.setParentId(0);
            rootNode.setNodeName(treeName);
            rootNode.setNodeType("TREE_ROOT");
            rootNode.setFactorId(null);
            rootNode.setIsLeaf(false);
            rootNode.setSortOrder(0);
            rootNode.setDescription(description);
            rootNode.setSceneId(sceneId);

            int result = factorManagementMapper.insertFactorTreeNode(rootNode);
            if (result > 0) {
                FactorManagementResponse response = new FactorManagementResponse();
                response.setOperationType("CREATE_FACTOR_TREE");
                response.setSuccess(true);
                response.setMessage("因子树创建成功");
                response.setOperationTime(LocalDateTime.now());
                
                FactorManagementResponse.TreeData treeData = convertToTreeData(rootNode, scene.getSceneName());
                response.setTreeData(treeData);
                
                return response;
            } else {
                return createErrorResponse("CREATE_FACTOR_TREE", "因子树创建失败");
            }
        } catch (Exception e) {
            log.error("创建因子树失败", e);
            return createErrorResponse("CREATE_FACTOR_TREE", "创建失败: " + e.getMessage());
        }
    }

    // ==================== 统一操作入口 ====================

    @Override
    @Transactional
    public FactorManagementResponse processFactorManagement(FactorManagementRequest request) {
        String operationType = request.getOperationType();
        
        switch (operationType) {
            case "GET_ALL_BASE_FACTORS":
                return getAllBaseFactors();
            case "CREATE_DERIVED_FACTOR":
                return createDerivedFactor(request.getDerivedFactorInfo());
            case "CONFIGURE_FACTOR_WEIGHTS":
                return configureFactorWeights(request.getWeightConfigInfo());
            case "CREATE_FACTOR_TREE":
                return createFactorTree(
                    request.getTreeOperationInfo().getSceneId(),
                    request.getTreeOperationInfo().getNodeName(),
                    request.getTreeOperationInfo().getDescription()
                );
            case "CREATE_DERIVED_FACTOR_WITH_FULL_FLOW":
                return createDerivedFactorWithFullFlow(request);
            default:
                return createErrorResponse("PROCESS_FACTOR_MANAGEMENT", "不支持的操作类型: " + operationType);
        }
    }

    // ==================== 辅助方法 ====================

    private FactorManagementResponse createErrorResponse(String operationType, String message) {
        FactorManagementResponse response = new FactorManagementResponse();
        response.setOperationType(operationType);
        response.setSuccess(false);
        // 添加更详细的错误信息，包含操作类型以便调试
        response.setMessage("错误类型: " + operationType + ", 详细原因: " + message);
        response.setOperationTime(LocalDateTime.now());
        return response;
    }
    
    /**
     * 解析权重字符串为Map
     */
    private Map<Integer, Double> parseWeightsString(String weightsString) {
        if (weightsString == null || weightsString.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        
        try {
            // 预处理：将单引号替换为双引号
            String normalizedWeightsString = weightsString.replace("'", "\"");
            
            // 添加大括号，确保是有效的JSON对象格式
            if (!normalizedWeightsString.startsWith("{") && !normalizedWeightsString.endsWith("}")) {
                normalizedWeightsString = "{" + normalizedWeightsString + "}";
            }
            
            ObjectMapper objectMapper = new ObjectMapper();
            // 尝试将字符串解析为Map<String, Double>，然后转换为Map<Integer, Double>
            Map<String, Double> stringKeyMap = objectMapper.readValue(normalizedWeightsString, Map.class);
            Map<Integer, Double> result = new HashMap<>();
            
            for (Map.Entry<String, Double> entry : stringKeyMap.entrySet()) {
                try {
                    Integer key = Integer.parseInt(entry.getKey());
                    result.put(key, entry.getValue());
                } catch (NumberFormatException e) {
                    // 忽略非数字键
                    log.warn("权重配置中包含非数字键: {}", entry.getKey());
                }
            }
            
            return result;
        } catch (JsonProcessingException e) {
            log.error("解析权重配置失败: {}", e.getMessage());
            // 尝试手动解析格式如 "'3': 0.6, '4': 0.4" 的字符串
            return parseWeightsManually(weightsString);
        }
    }
    
    /**
     * 手动解析权重字符串，处理可能不符合标准JSON格式的情况
     */
    private Map<Integer, Double> parseWeightsManually(String weightsString) {
        Map<Integer, Double> result = new HashMap<>();
        
        try {
            // 移除可能的大括号
            String trimmed = weightsString.trim();
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            }
            
            // 按逗号分割键值对
            String[] pairs = trimmed.split(",");
            for (String pair : pairs) {
                // 分割键和值
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String keyStr = keyValue[0].trim().replaceAll("['\"]", ""); // 移除引号
                    String valueStr = keyValue[1].trim().replaceAll("['\"]", ""); // 移除引号
                    
                    try {
                        Integer key = Integer.parseInt(keyStr);
                        Double value = Double.parseDouble(valueStr);
                        result.put(key, value);
                    } catch (NumberFormatException e) {
                        log.warn("权重配置解析失败，键: {}, 值: {}", keyStr, valueStr);
                    }
                }
            }
        } catch (Exception e) {
            log.error("手动解析权重配置失败: {}", e.getMessage());
        }
        
        return result;
    }

    private boolean isFactorCodeExists(String factorCode) {
        List<Integer> counts = factorManagementMapper.checkFactorCodeExists(factorCode);
        return counts.stream().anyMatch(count -> count > 0);
    }

    private FactorManagementResponse.BaseFactorData convertToBaseFactorData(FactorBase factorBase) {
        FactorManagementResponse.BaseFactorData data = new FactorManagementResponse.BaseFactorData();
        data.setBaseId(factorBase.getBaseId());
        data.setFactorName(factorBase.getFactorName());
        data.setFactorCode(factorBase.getFactorCode());
        data.setFactorFormula(factorBase.getFactorFormula());
        data.setDataSource(factorBase.getDataSource());
        data.setUpdateFrequency(factorBase.getUpdateFrequency());
        // LocalDate转换为String
        data.setDataStartDate(factorBase.getDataStartDate() != null ? factorBase.getDataStartDate().toString() : null);
        data.setLatestDataDate(factorBase.getLatestDataDate() != null ? factorBase.getLatestDataDate().toString() : null);
        data.setDataDesc(factorBase.getDataDesc());
        // Boolean转换为Integer
        data.setIsValid(factorBase.getIsValid() != null ? (factorBase.getIsValid() ? 1 : 0) : null);
        data.setCreateTime(factorBase.getCreateTime());
        return data;
    }

    private FactorManagementResponse.DerivedFactorData convertToDerivedFactorData(FactorDerived factorDerived, List<Integer> baseFactorIds) {
        FactorManagementResponse.DerivedFactorData data = new FactorManagementResponse.DerivedFactorData();
        data.setDerivedId(factorDerived.getDerivedId());
        data.setFactorName(factorDerived.getFactorName());
        data.setFactorCode(factorDerived.getFactorCode());
        data.setFactorDesc(factorDerived.getFactorDesc());
        data.setCalcStrategyId(factorDerived.getCalcStrategyId());
        data.setStyleTagIds(factorDerived.getStyleTagIds());
        data.setTreeNodeId(factorDerived.getTreeNodeId());
        data.setCreateUserId(factorDerived.getCreateUserId());
        data.setIsValid(factorDerived.getIsValid() != null ? (factorDerived.getIsValid() ? 1 : 0) : null);
        data.setCreateTime(factorDerived.getCreateTime());

        // 获取计算策略名称
        if (factorDerived.getCalcStrategyId() != null) {
            CalcStrategy strategy = factorManagementMapper.selectCalcStrategyById(factorDerived.getCalcStrategyId());
            if (strategy != null) {
                data.setCalcStrategyName(strategy.getStrategyName());
            }
        }

        // 获取风格标签详情
        if (factorDerived.getStyleTagIds() != null && !factorDerived.getStyleTagIds().trim().isEmpty()) {
            try {
                // 解析风格标签ID字符串（如 "1,2,3"）
                List<Integer> tagIds = Arrays.stream(factorDerived.getStyleTagIds().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                
                if (!tagIds.isEmpty()) {
                    // 查询风格标签详情
                    List<StyleTag> styleTags = styleTagMapper.selectStyleTagsByIds(tagIds);
                    
                    // 转换为 StyleTagInfo 列表
                    List<FactorManagementResponse.StyleTagInfo> styleTagInfos = new ArrayList<>();
                    List<String> styleTagNames = new ArrayList<>();
                    
                    for (StyleTag styleTag : styleTags) {
                        FactorManagementResponse.StyleTagInfo tagInfo = new FactorManagementResponse.StyleTagInfo();
                        tagInfo.setTagId(styleTag.getTagId());
                        tagInfo.setTagName(styleTag.getTagName());
                        tagInfo.setTagCode(styleTag.getTagCode());
                        tagInfo.setDescription(styleTag.getDescription());
                        styleTagInfos.add(tagInfo);
                        styleTagNames.add(styleTag.getTagName());
                    }
                    
                    data.setStyleTags(styleTagInfos);
                    data.setStyleTagNames(styleTagNames);
                }
            } catch (Exception e) {
                log.warn("解析风格标签ID失败: {}", factorDerived.getStyleTagIds(), e);
            }
        }

        // 获取基础因子信息及权重
        List<FactorManagementResponse.BaseFactorWithWeight> baseFactors = new ArrayList<>();
        // 先查询所有关联的权重信息
        Map<Integer, Double> weightsMap = new HashMap<>();
        List<Map<String, Object>> relations = factorManagementMapper.selectDerivedFactorRelations(factorDerived.getDerivedId());
        for (Map<String, Object> relation : relations) {
            Integer baseId = (Integer) relation.get("base_id");
            Double weight = (Double) relation.get("weight");
            String weightDesc = (String) relation.get("weight_desc");
            if (baseId != null && weight != null) {
                weightsMap.put(baseId, weight);
            }
        }
        
        // 构建基础因子列表并设置权重
        if (baseFactorIds != null && !baseFactorIds.isEmpty()) {
            for (Integer baseId : baseFactorIds) {
                FactorBase baseFactor = factorManagementMapper.selectBaseFactorById(baseId);
                if (baseFactor != null) {
                    FactorManagementResponse.BaseFactorWithWeight baseFactorWithWeight = 
                            new FactorManagementResponse.BaseFactorWithWeight();
                    baseFactorWithWeight.setBaseId(baseFactor.getBaseId());
                    baseFactorWithWeight.setFactorName(baseFactor.getFactorName());
                    baseFactorWithWeight.setFactorCode(baseFactor.getFactorCode());
                    // 设置权重信息
                    baseFactorWithWeight.setWeight(weightsMap.get(baseId));
                    // 从relations中查找对应的weightDesc
                    for (Map<String, Object> relation : relations) {
                        if (baseId.equals(relation.get("base_id"))) {
                            baseFactorWithWeight.setWeightDesc((String) relation.get("weight_desc"));
                            break;
                        }
                    }
                    baseFactors.add(baseFactorWithWeight);
                }
            }
        }
        data.setBaseFactors(baseFactors);

        return data;
    }

    private FactorManagementResponse.TreeData convertToTreeData(FactorTree factorTree, String sceneName) {
        FactorManagementResponse.TreeData data = new FactorManagementResponse.TreeData();
        data.setNodeId(factorTree.getTreeid());
        data.setParentId(factorTree.getParentId());
        data.setNodeName(factorTree.getNodeName());
        data.setNodeType(factorTree.getNodeType());
        data.setFactorId(factorTree.getFactorId());
        data.setIsLeaf(factorTree.getIsLeaf());
        data.setSortOrder(factorTree.getSortOrder());
        data.setDescription(factorTree.getDescription());
        data.setSceneId(factorTree.getSceneId());
        data.setSceneName(sceneName);
        return data;
    }

    private List<DerivedFactor> createEqualWeightRelations(Integer derivedId, List<Integer> baseFactorIds, Integer calcStrategyId) {
        List<DerivedFactor> derivedFactors = new ArrayList<>();
        double equalWeight = 1.0 / baseFactorIds.size();

        for (Integer baseId : baseFactorIds) {
            DerivedFactor derivedFactor = new DerivedFactor();
            derivedFactor.setDerivedId(derivedId);
            derivedFactor.setBaseId(baseId);
            derivedFactor.setWeight(equalWeight);
            derivedFactor.setWeightDesc("系统默认等权");
            derivedFactor.setBaseDataCheckStatus("UNCHECKED");
            derivedFactor.setCalcStrategyId(calcStrategyId);
            derivedFactors.add(derivedFactor);
        }

        return derivedFactors;
    }

    // ==================== 未实现的方法（可以根据需要补充） ====================

    @Override
    public FactorManagementResponse updateFactorWeights(Integer derivedId, FactorManagementRequest.WeightConfigInfo weightConfigInfo) {
        return createErrorResponse("UPDATE_FACTOR_WEIGHTS", "功能待实现");
    }

    @Override
    @Transactional
    public FactorManagementResponse addFactorTreeNode(FactorManagementRequest.TreeOperationInfo treeInfo) {
        try {
            log.info("开始添加因子树节点，factorId: {}, parentId: {}, sceneId: {}, nodeName: {}", 
                    treeInfo.getFactorId(), treeInfo.getParentId(), treeInfo.getSceneId(), treeInfo.getNodeName());
            
            // 1. 确定 treeId 和 parentId
            Integer treeId = null;
            Integer parentId = treeInfo.getParentId();
            String nodeName = treeInfo.getNodeName();
            String nodeType = treeInfo.getNodeType() != null ? treeInfo.getNodeType() : "FACTOR";
            Integer factorId = treeInfo.getFactorId();
            String description = treeInfo.getDescription();
            
            // 如果 parentId 为 0 或 null，表示添加到根节点，需要通过 sceneId 找到对应的树
            if (parentId == null || parentId == 0) {
                if (treeInfo.getSceneId() == null || treeInfo.getSceneId().trim().isEmpty()) {
                    return createErrorResponse("ADD_FACTOR_TREE_NODE", "添加到根节点时必须指定场景ID");
                }
                
                // 通过 sceneId 查找对应的树（TREE 类型的节点）
                List<FactorTree> trees = factorTreeMapper.getTreesByScene(treeInfo.getSceneId());
                if (trees == null || trees.isEmpty()) {
                    return createErrorResponse("ADD_FACTOR_TREE_NODE", "场景ID对应的因子树不存在: " + treeInfo.getSceneId());
                }
                
                // 取第一个树（如果有多个，默认取第一个）
                FactorTree tree = trees.get(0);
                treeId = tree.getTreeid();
                parentId = tree.getTreeid(); // 添加到树的根节点下
                
                log.info("通过 sceneId 找到树，treeId: {}, parentId: {}", treeId, parentId);
            } else {
                // 如果指定了 parentId，验证父节点是否存在并获取 treeId
                FactorTree parentNode = factorTreeMapper.getNodeById(parentId);
                if (parentNode == null) {
                    return createErrorResponse("ADD_FACTOR_TREE_NODE", "父节点不存在，parentId: " + parentId);
                }
                
                // 如果是 TREE 类型节点，treeId 就是该节点本身
                if ("TREE".equals(parentNode.getNodeType())) {
                    treeId = parentNode.getTreeid();
                } else {
                    // 否则向上查找 TREE 类型节点
                    FactorTree current = parentNode;
                    while (current != null && current.getParentId() != null) {
                        current = factorTreeMapper.getNodeById(current.getParentId());
                        if (current != null && "TREE".equals(current.getNodeType())) {
                            treeId = current.getTreeid();
                            break;
                        }
                    }
                    if (treeId == null) {
                        return createErrorResponse("ADD_FACTOR_TREE_NODE", "无法找到对应的因子树");
                    }
                }
                
                log.info("通过 parentId 确定 treeId: {}", treeId);
            }
            
            // 如果没有指定节点名称，使用衍生因子的名称
            if (nodeName == null || nodeName.trim().isEmpty()) {
                if (factorId != null) {
                    FactorDerived factor = factorManagementMapper.selectDerivedFactorById(factorId);
                    if (factor != null) {
                        nodeName = factor.getFactorName();
                    } else {
                        nodeName = "未命名节点";
                    }
                } else {
                    nodeName = "未命名节点";
                }
            }
            
            // 2. 调用 FactorTreeService 添加节点
            com.fengqi.fund.fundadvisor.dto.ResultDTO<FactorTree> result = 
                    factorTreeService.addTreeNode(treeId, parentId, nodeName, nodeType, factorId, description);
            
            if (!result.isSuccess()) {
                log.error("添加树节点失败: {}", result.getMessage());
                return createErrorResponse("ADD_FACTOR_TREE_NODE", result.getMessage());
            }
            
            FactorTree addedNode = result.getData();
            log.info("成功添加树节点，nodeId: {}, nodeName: {}", addedNode.getTreeid(), addedNode.getNodeName());
            
            // 3. 更新衍生因子的 treeNodeId
            if (factorId != null && addedNode.getTreeid() != null) {
                FactorDerived factor = factorManagementMapper.selectDerivedFactorById(factorId);
                if (factor != null) {
                    factor.setTreeNodeId(addedNode.getTreeid());
                    factorManagementMapper.updateDerivedFactor(factor);
                    log.info("已更新衍生因子的 treeNodeId，derivedId: {}, treeNodeId: {}", factorId, addedNode.getTreeid());
                }
            }
            
            // 4. 构建响应
            FactorManagementResponse response = new FactorManagementResponse();
            response.setOperationType("ADD_FACTOR_TREE_NODE");
            response.setSuccess(true);
            response.setMessage("节点添加成功");
            response.setOperationTime(LocalDateTime.now());
            
            // 获取场景名称
            String sceneName = null;
            if (addedNode.getSceneId() != null) {
                FactorTreeScene scene = factorTreeSceneMapper.getSceneById(addedNode.getSceneId());
                if (scene != null) {
                    sceneName = scene.getSceneName();
                }
            }
            
            // 转换为 TreeData
            FactorManagementResponse.TreeData treeData = convertToTreeData(addedNode, sceneName);
            response.setTreeData(treeData);
            
            log.info("节点添加成功并返回响应");
            return response;
            
        } catch (Exception e) {
            log.error("添加因子树节点失败", e);
            return createErrorResponse("ADD_FACTOR_TREE_NODE", "添加失败: " + e.getMessage());
        }
    }

    @Override
    public FactorManagementResponse updateFactorTreeNode(Integer nodeId, FactorManagementRequest.TreeOperationInfo treeInfo) {
        return createErrorResponse("UPDATE_FACTOR_TREE_NODE", "功能待实现");
    }

    @Override
    public FactorManagementResponse deleteFactorTreeNode(Integer nodeId) {
        return createErrorResponse("DELETE_FACTOR_TREE_NODE", "功能待实现");
    }

    @Override
    public FactorManagementResponse moveFactorTreeNode(Integer nodeId, Integer newParentId, Integer newSortOrder) {
        return createErrorResponse("MOVE_FACTOR_TREE_NODE", "功能待实现");
    }

    @Override
    public FactorManagementResponse getFactorTree(String sceneId) {
        return createErrorResponse("GET_FACTOR_TREE", "功能待实现");
    }

    @Override
    public FactorManagementResponse getFactorTreeNode(Integer nodeId) {
        return createErrorResponse("GET_FACTOR_TREE_NODE", "功能待实现");
    }

    @Override
    public FactorManagementResponse getAllScenes() {
        return createErrorResponse("GET_ALL_SCENES", "功能待实现");
    }

    @Override
    public FactorManagementResponse createScene(String sceneId, String sceneName, String sceneDesc, Integer managerUserId) {
        return createErrorResponse("CREATE_SCENE", "功能待实现");
    }



    @Override
    public FactorManagementResponse validateFactorData(List<Integer> baseFactorIds) {
        return createErrorResponse("VALIDATE_FACTOR_DATA", "功能待实现");
    }

    @Override
    public FactorManagementResponse searchFactors(String keyword, String type, String sceneId) {
        return createErrorResponse("SEARCH_FACTORS", "功能待实现");
    }

    @Override
    public FactorManagementResponse getFactorStatistics() {
        return createErrorResponse("GET_FACTOR_STATISTICS", "功能待实现");
    }

    @Override
    public FactorManagementResponse getAvailableBaseFactorsForDerived() {
        return createErrorResponse("GET_AVAILABLE_BASE_FACTORS_FOR_DERIVED", "功能待实现");
    }

    @Override
    public FactorManagementResponse previewDerivedFactor(List<Integer> baseFactorIds, Map<Integer, Double> weights, Integer calcStrategyId) {
        return createErrorResponse("PREVIEW_DERIVED_FACTOR", "功能待实现");
    }

    @Override
    @Transactional
    public FactorManagementResponse createDerivedFactorWithFullFlow(FactorManagementRequest request) {
        try {
            // 调试日志：记录开始创建衍生因子
            log.info("开始创建衍生因子完整流程，因子代码: {}", request.getDerivedFactorInfo() != null ? request.getDerivedFactorInfo().getFactorCode() : "未知");
            
            // 1. 验证请求参数
            if (request.getDerivedFactorInfo() == null) {
                log.warn("衍生因子信息为空");
                return createErrorResponse("CREATE_DERIVED_FACTOR_WITH_FULL_FLOW", "衍生因子信息不能为空");
            }
            log.info("请求参数验证通过，基础因子ID列表: {}", request.getDerivedFactorInfo().getBaseFactorIds());
            
            // 2. 创建衍生因子
            log.info("开始创建衍生因子对象");
            FactorManagementResponse derivedFactorResponse = createDerivedFactor(request.getDerivedFactorInfo());
            if (!derivedFactorResponse.getSuccess()) {
                log.warn("衍生因子创建失败: {}", derivedFactorResponse.getMessage());
                return derivedFactorResponse;
            }
            log.info("衍生因子创建成功");
            
            // 获取创建的衍生因子ID
            Integer derivedId = derivedFactorResponse.getDerivedFactorDataList() != null && 
                    !derivedFactorResponse.getDerivedFactorDataList().isEmpty() 
                    ? derivedFactorResponse.getDerivedFactorDataList().get(0).getDerivedId() 
                    : null;
            log.info("获取到新创建的derivedId: {}", derivedId);
            
            // 3. 如果提供了权重配置，则配置权重
            if (request.getWeightConfigInfo() != null) {
                log.info("检测到权重配置信息，准备应用权重");
                // 自动设置正确的derivedId（忽略用户请求中的值）
                if (request.getWeightConfigInfo().getDerivedId() != null) {
                    log.debug("忽略请求中的权重配置derivedId: {}，将使用新创建的衍生因子ID: {}", 
                            request.getWeightConfigInfo().getDerivedId(), derivedId);
                }
                request.getWeightConfigInfo().setDerivedId(derivedId);
                
                log.info("调用configureFactorWeights方法应用权重配置，权重描述: {}", request.getWeightConfigInfo().getWeightDesc());
                FactorManagementResponse weightResponse = configureFactorWeights(request.getWeightConfigInfo());
                if (!weightResponse.getSuccess()) {
                    log.error("权重配置失败: {}", weightResponse.getMessage());
                    throw new RuntimeException("权重配置失败: " + weightResponse.getMessage());
                }
                log.info("权重配置成功");
            } else {
                log.info("未提供权重配置，使用默认配置");
            }
            
            // 4. 如果提供了树节点操作，则添加到因子树（需要先验证树节点是否存在）
            FactorManagementResponse.TreeData treeData = null;
            if (request.getTreeOperationInfo() != null) {
                log.info("检测到树节点配置信息，准备添加到因子树");
                
                // 验证树节点是否存在
                Integer parentId = request.getTreeOperationInfo().getParentId();
                if (parentId != null && parentId != 0) {
                    // 验证父节点是否存在
                    FactorTree parentNode = factorTreeMapper.getNodeById(parentId);
                    if (parentNode == null) {
                        log.error("父节点不存在，parentId: {}", parentId);
                        throw new IllegalArgumentException("指定的父节点不存在，无法添加因子到树节点。parentId: " + parentId);
                    }
                    log.info("父节点验证通过，parentId: {}, 节点名称: {}", parentId, parentNode.getNodeName());
                } else {
                    // 如果没有指定parentId或parentId为0，表示添加到根节点，需要验证场景是否存在
                    String sceneId = request.getTreeOperationInfo().getSceneId();
                    if (sceneId == null || sceneId.trim().isEmpty()) {
                        log.error("未指定场景ID，无法添加到根节点");
                        throw new IllegalArgumentException("添加到根节点时必须指定场景ID");
                    }
                    log.info("将添加到场景根节点，sceneId: {}", sceneId);
                }
                
                // 自动设置 factorId（关联到新创建的衍生因子）
                request.getTreeOperationInfo().setFactorId(derivedId);
                request.getTreeOperationInfo().setIsLeaf(true); // 衍生因子是叶子节点
                // nodeId 会在添加节点时自动生成，不需要设置
                
                // 如果没有指定节点名称，使用衍生因子的名称
                if (request.getTreeOperationInfo().getNodeName() == null || 
                    request.getTreeOperationInfo().getNodeName().trim().isEmpty()) {
                    if (derivedFactorResponse.getDerivedFactorDataList() != null && 
                        !derivedFactorResponse.getDerivedFactorDataList().isEmpty()) {
                        request.getTreeOperationInfo().setNodeName(
                            derivedFactorResponse.getDerivedFactorDataList().get(0).getFactorName());
                    }
                }
                
                // 如果没有指定节点类型，默认为 FACTOR
                if (request.getTreeOperationInfo().getNodeType() == null || 
                    request.getTreeOperationInfo().getNodeType().trim().isEmpty()) {
                    request.getTreeOperationInfo().setNodeType("FACTOR");
                }
                
                log.info("设置树节点参数完成，场景ID: {}, 父节点ID: {}, 节点名称: {}", 
                        request.getTreeOperationInfo().getSceneId(), 
                        request.getTreeOperationInfo().getParentId(),
                        request.getTreeOperationInfo().getNodeName());
                
                FactorManagementResponse treeResponse = addFactorTreeNode(request.getTreeOperationInfo());
                if (!treeResponse.getSuccess()) {
                    log.error("树节点添加失败: {}", treeResponse.getMessage());
                    throw new RuntimeException("树节点添加失败: " + treeResponse.getMessage());
                }
                log.info("因子添加到树节点成功");
                
                // 保存树数据以便后续添加到最终响应中
                treeData = treeResponse.getTreeData();
            }
            
            // 5. 返回成功响应 - 关键修复：重新获取包含最新权重信息的衍生因子数据
            log.info("准备返回响应，重新查询最新的衍生因子数据");
            FactorManagementResponse response = new FactorManagementResponse();
            response.setOperationType("CREATE_DERIVED_FACTOR_WITH_FULL_FLOW");
            response.setSuccess(true);
            response.setMessage("衍生因子完整流程创建成功");
            response.setOperationTime(LocalDateTime.now());
            
            // 重新查询并设置最新的衍生因子数据，确保包含正确的权重信息
            log.info("调用getDerivedFactor查询最新数据，derivedId: {}", derivedId);
            FactorManagementResponse latestDerivedResponse = getDerivedFactor(derivedId);
            if (latestDerivedResponse.getSuccess() && latestDerivedResponse.getDerivedFactorDataList() != null 
                    && !latestDerivedResponse.getDerivedFactorDataList().isEmpty()) {
                log.info("成功获取到最新的衍生因子数据，包含权重配置信息");
                // 添加日志记录获取到的权重信息
                FactorManagementResponse.DerivedFactorData latestData = latestDerivedResponse.getDerivedFactorDataList().get(0);
                if (latestData.getBaseFactors() != null) {
                    for (FactorManagementResponse.BaseFactorWithWeight baseFactor : latestData.getBaseFactors()) {
                        log.info("获取到基础因子ID: {}, 权重: {}, 权重描述: {}", 
                                baseFactor.getBaseId(), baseFactor.getWeight(), baseFactor.getWeightDesc());
                    }
                }
                response.setDerivedFactorDataList(latestDerivedResponse.getDerivedFactorDataList());
            } else {
                log.warn("获取最新衍生因子数据失败，使用原始创建的数据");
                // 降级方案：使用原始数据
                response.setDerivedFactorDataList(derivedFactorResponse.getDerivedFactorDataList());
            }
            
            // 设置权重配置数据（如果有）
            if (request.getWeightConfigInfo() != null) {
                // 重新查询权重配置数据
                FactorManagementResponse weightResponse = getDerivedFactor(derivedId);
                if (weightResponse.getSuccess() && weightResponse.getDerivedFactorDataList() != null &&
                    !weightResponse.getDerivedFactorDataList().isEmpty() &&
                    weightResponse.getDerivedFactorDataList().get(0).getBaseFactors() != null &&
                    !weightResponse.getDerivedFactorDataList().get(0).getBaseFactors().isEmpty()) {
                    // 构建权重配置数据
                    FactorManagementResponse.WeightConfigData weightConfigData = 
                            new FactorManagementResponse.WeightConfigData();
                    weightConfigData.setDerivedId(derivedId);
                    
                    Map<Integer, Double> weights = new HashMap<>();
                    String weightDesc = null;
                    for (FactorManagementResponse.BaseFactorWithWeight baseFactor : 
                            weightResponse.getDerivedFactorDataList().get(0).getBaseFactors()) {
                        weights.put(baseFactor.getBaseId(), baseFactor.getWeight());
                        if (weightDesc == null) {
                            weightDesc = baseFactor.getWeightDesc();
                        }
                    }
                    weightConfigData.setBaseFactorWeights(weights);
                    
                    // 计算总权重
                    double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();
                    weightConfigData.setTotalWeight(totalWeight);
                    weightConfigData.setWeightDesc(weightDesc != null ? weightDesc : "用户自定义权重");
                    weightConfigData.setFormulaRemark(request.getWeightConfigInfo().getFormulaRemark());
                    
                    response.setWeightConfigData(weightConfigData);
                    log.info("已将权重配置数据添加到响应中，总权重: {}", totalWeight);
                }
            }
            
            // 设置树节点数据（如果有）
            if (treeData != null) {
                response.setTreeData(treeData);
                log.info("已将树节点数据添加到响应中，nodeId: {}, nodeName: {}", 
                        treeData.getNodeId(), treeData.getNodeName());
            }
            
            log.info("衍生因子完整流程创建完成，返回响应");
            return response;
        } catch (Exception e) {
            log.error("创建衍生因子完整流程失败", e);
            return createErrorResponse("CREATE_DERIVED_FACTOR_WITH_FULL_FLOW", "创建失败: " + e.getMessage());
        }
    }

    // ==================== 风格标签相关操作 ====================

    @Override
    public FactorManagementResponse getDerivedFactorsByStyleTagId(Integer tagId) {
        try {
            if (tagId == null) {
                return createErrorResponse("GET_DERIVED_FACTORS_BY_STYLE_TAG", "风格标签ID不能为空");
            }

            List<FactorDerived> derivedFactors = factorManagementMapper.selectDerivedFactorsByStyleTagId(tagId);
            
            FactorManagementResponse response = new FactorManagementResponse();
            response.setOperationType("GET_DERIVED_FACTORS_BY_STYLE_TAG");
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setOperationTime(LocalDateTime.now());

            // 转换为响应数据
            List<FactorManagementResponse.DerivedFactorData> derivedFactorDataList = new ArrayList<>();
            for (FactorDerived factorDerived : derivedFactors) {
                // 获取关联的基础因子ID
                List<Map<String, Object>> relations = factorManagementMapper.selectDerivedFactorRelations(factorDerived.getDerivedId());
                List<Integer> baseFactorIds = relations.stream()
                        .map(relation -> (Integer) relation.get("base_id"))
                        .collect(Collectors.toList());
                
                FactorManagementResponse.DerivedFactorData derivedFactorData = 
                        convertToDerivedFactorData(factorDerived, baseFactorIds);
                derivedFactorDataList.add(derivedFactorData);
            }

            // 设置到响应中
            response.setDerivedFactorDataList(derivedFactorDataList);

            return response;
        } catch (Exception e) {
            log.error("根据风格标签查询衍生因子失败", e);
            return createErrorResponse("GET_DERIVED_FACTORS_BY_STYLE_TAG", "查询失败: " + e.getMessage());
        }
    }

    @Override
    public FactorManagementResponse getDerivedFactorsByStyleTagIds(List<Integer> tagIds) {
        try {
            if (tagIds == null || tagIds.isEmpty()) {
                return createErrorResponse("GET_DERIVED_FACTORS_BY_STYLE_TAGS", "风格标签ID列表不能为空");
            }

            List<FactorDerived> derivedFactors = factorManagementMapper.selectDerivedFactorsByStyleTagIds(tagIds);
            
            FactorManagementResponse response = new FactorManagementResponse();
            response.setOperationType("GET_DERIVED_FACTORS_BY_STYLE_TAGS");
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setOperationTime(LocalDateTime.now());

            // 转换为响应数据
            List<FactorManagementResponse.DerivedFactorData> derivedFactorDataList = new ArrayList<>();
            for (FactorDerived factorDerived : derivedFactors) {
                // 获取关联的基础因子ID
                List<Map<String, Object>> relations = factorManagementMapper.selectDerivedFactorRelations(factorDerived.getDerivedId());
                List<Integer> baseFactorIds = relations.stream()
                        .map(relation -> (Integer) relation.get("base_id"))
                        .collect(Collectors.toList());
                
                FactorManagementResponse.DerivedFactorData derivedFactorData = 
                        convertToDerivedFactorData(factorDerived, baseFactorIds);
                derivedFactorDataList.add(derivedFactorData);
            }

            // 设置到响应中
            response.setDerivedFactorDataList(derivedFactorDataList);

            return response;
        } catch (Exception e) {
            log.error("根据风格标签列表查询衍生因子失败", e);
            return createErrorResponse("GET_DERIVED_FACTORS_BY_STYLE_TAGS", "查询失败: " + e.getMessage());
        }
    }

    @Override
    public FactorManagementResponse getDerivedFactorsByStyleTagCode(String tagCode) {
        try {
            if (tagCode == null || tagCode.trim().isEmpty()) {
                return createErrorResponse("GET_DERIVED_FACTORS_BY_STYLE_TAG_CODE", "风格标签编码不能为空");
            }

            // 先验证风格标签是否存在
            StyleTag styleTag = styleTagMapper.selectStyleTagByCode(tagCode.trim());
            if (styleTag == null) {
                return createErrorResponse("GET_DERIVED_FACTORS_BY_STYLE_TAG_CODE", "风格标签编码不存在: " + tagCode);
            }

            List<FactorDerived> allDerivedFactors = factorManagementMapper.selectDerivedFactorsByStyleTagCode(tagCode.trim());
            
            // 过滤：只返回只包含该标签的因子（styleTagIds 只包含该标签ID，不包含其他标签）
            Integer tagId = styleTag.getTagId();
            List<FactorDerived> derivedFactors = allDerivedFactors.stream()
                    .filter(factor -> {
                        String styleTagIds = factor.getStyleTagIds();
                        if (styleTagIds == null || styleTagIds.trim().isEmpty()) {
                            return false;
                        }
                        // 检查 styleTagIds 是否只包含该标签ID
                        // 可能的格式： "3", "3,", ",3", ",3,"
                        String trimmed = styleTagIds.trim();
                        // 移除首尾逗号
                        if (trimmed.startsWith(",")) {
                            trimmed = trimmed.substring(1);
                        }
                        if (trimmed.endsWith(",")) {
                            trimmed = trimmed.substring(0, trimmed.length() - 1);
                        }
                        // 检查是否只包含该标签ID（没有逗号，或者逗号分隔后只有一个值且等于tagId）
                        String[] ids = trimmed.split(",");
                        return ids.length == 1 && String.valueOf(tagId).equals(ids[0].trim());
                    })
                    .collect(Collectors.toList());
            
            FactorManagementResponse response = new FactorManagementResponse();
            response.setOperationType("GET_DERIVED_FACTORS_BY_STYLE_TAG_CODE");
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setOperationTime(LocalDateTime.now());

            // 转换为响应数据
            List<FactorManagementResponse.DerivedFactorData> derivedFactorDataList = new ArrayList<>();
            for (FactorDerived factorDerived : derivedFactors) {
                // 获取关联的基础因子ID
                List<Map<String, Object>> relations = factorManagementMapper.selectDerivedFactorRelations(factorDerived.getDerivedId());
                List<Integer> baseFactorIds = relations.stream()
                        .map(relation -> (Integer) relation.get("base_id"))
                        .collect(Collectors.toList());
                
                FactorManagementResponse.DerivedFactorData derivedFactorData = 
                        convertToDerivedFactorData(factorDerived, baseFactorIds);
                derivedFactorDataList.add(derivedFactorData);
            }

            // 设置到响应中
            response.setDerivedFactorDataList(derivedFactorDataList);

            return response;
        } catch (Exception e) {
            log.error("根据风格标签编码查询衍生因子失败", e);
            return createErrorResponse("GET_DERIVED_FACTORS_BY_STYLE_TAG_CODE", "查询失败: " + e.getMessage());
        }
    }

    @Override
    public FactorManagementResponse getDerivedFactorsByStyleTagCodes(List<String> tagCodes) {
        try {
            if (tagCodes == null || tagCodes.isEmpty()) {
                return createErrorResponse("GET_DERIVED_FACTORS_BY_STYLE_TAG_CODES", "风格标签编码列表不能为空");
            }

            // 验证所有编码是否存在
            List<String> validCodes = new ArrayList<>();
            for (String code : tagCodes) {
                if (code != null && !code.trim().isEmpty()) {
                    StyleTag styleTag = styleTagMapper.selectStyleTagByCode(code.trim());
                    if (styleTag == null) {
                        log.warn("风格标签编码不存在: {}", code);
                        continue; // 跳过不存在的编码
                    }
                    validCodes.add(code.trim());
                }
            }

            if (validCodes.isEmpty()) {
                return createErrorResponse("GET_DERIVED_FACTORS_BY_STYLE_TAG_CODES", "没有有效的风格标签编码");
            }

            List<FactorDerived> derivedFactors = factorManagementMapper.selectDerivedFactorsByStyleTagCodes(validCodes);
            
            FactorManagementResponse response = new FactorManagementResponse();
            response.setOperationType("GET_DERIVED_FACTORS_BY_STYLE_TAG_CODES");
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setOperationTime(LocalDateTime.now());

            // 转换为响应数据
            List<FactorManagementResponse.DerivedFactorData> derivedFactorDataList = new ArrayList<>();
            for (FactorDerived factorDerived : derivedFactors) {
                // 获取关联的基础因子ID
                List<Map<String, Object>> relations = factorManagementMapper.selectDerivedFactorRelations(factorDerived.getDerivedId());
                List<Integer> baseFactorIds = relations.stream()
                        .map(relation -> (Integer) relation.get("base_id"))
                        .collect(Collectors.toList());
                
                FactorManagementResponse.DerivedFactorData derivedFactorData = 
                        convertToDerivedFactorData(factorDerived, baseFactorIds);
                derivedFactorDataList.add(derivedFactorData);
            }

            // 设置到响应中
            response.setDerivedFactorDataList(derivedFactorDataList);

            return response;
        } catch (Exception e) {
            log.error("根据风格标签编码列表查询衍生因子失败", e);
            return createErrorResponse("GET_DERIVED_FACTORS_BY_STYLE_TAG_CODES", "查询失败: " + e.getMessage());
        }
    }

    // ==================== 风格标签编码转换工具方法 ====================

    /**
     * 将风格标签编码字符串转换为ID字符串
     * @param styleTagCodes 风格标签编码，多个用逗号分隔，如 "001,002,003"
     * @return 风格标签ID字符串，如 "1,2,3"，如果编码不存在则返回null
     */
    private String convertStyleTagCodesToIds(String styleTagCodes) {
        if (styleTagCodes == null || styleTagCodes.trim().isEmpty()) {
            return null;
        }

        try {
            // 解析编码字符串
            List<String> codes = Arrays.stream(styleTagCodes.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (codes.isEmpty()) {
                return null;
            }

            // 查询每个编码对应的标签
            List<Integer> tagIds = new ArrayList<>();
            for (String code : codes) {
                StyleTag styleTag = styleTagMapper.selectStyleTagByCode(code);
                if (styleTag == null) {
                    log.warn("风格标签编码不存在: {}", code);
                    return null; // 如果任何一个编码不存在，返回null
                }
                tagIds.add(styleTag.getTagId());
            }

            // 转换为ID字符串
            return tagIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            log.error("转换风格标签编码失败: {}", styleTagCodes, e);
            return null;
        }
    }
}