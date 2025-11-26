package com.fengqi.fund.fundadvisor.controller.factor;

import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.dto.factor.FactorManagementRequest;
import com.fengqi.fund.fundadvisor.dto.factor.FactorManagementResponse;
import com.fengqi.fund.fundadvisor.entity.factor.FactorTree;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorTreeMapper;
import com.fengqi.fund.fundadvisor.service.factor.FactorManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 衍生因子管理控制器
 * 管理衍生因子的创建、更新、删除、查询以及权重配置等操作
 * 基础因子为固定导入，衍生因子通过选择基础因子并分配权重合成
 */
@Slf4j
@RestController
@RequestMapping("/api/factor/factor-management")
@RequiredArgsConstructor
@Tag(name = "衍生因子管理", description = "衍生因子管理相关接口")
@Validated
public class FactorManagementController {

    private final FactorManagementService factorManagementService;
    private final FactorTreeMapper factorTreeMapper;

    // ==================== 通用方法 ====================
    
    /**
     * 通用响应处理方法
     * @param operationName 操作名称
     * @param action 具体操作的回调函数
     * @return ResponseEntity
     */
    private ResponseEntity<ResultDTO<FactorManagementResponse>> handleResponse(String operationName, Supplier<FactorManagementResponse> action) {
        try {
            FactorManagementResponse response = action.get();
            if (response.getSuccess()) {
                return ResponseEntity.ok(ResultDTO.success(response, response.getMessage()));
            } else {
                // 返回400错误时包含更详细的信息，包括操作名称和时间戳
                String detailedErrorMessage = String.format("%s - 错误时间: %s, %s", 
                        operationName, LocalDateTime.now(), response.getMessage());
                return ResponseEntity.badRequest()
                        .body(ResultDTO.error(detailedErrorMessage));
            }
        } catch (Exception e) {
            log.error(operationName + "失败", e);
            // 500错误时也添加更详细的异常信息，包括异常类型和堆栈摘要
            String detailedErrorMessage = String.format("%s失败: 异常类型: %s, 错误信息: %s, 堆栈摘要: %s", 
                    operationName, e.getClass().getName(), e.getMessage(), getStackTraceSummary(e));
            return ResponseEntity.internalServerError()
                    .body(ResultDTO.error(detailedErrorMessage));
        }
    }
    
    /**
     * 获取异常堆栈的简短摘要，用于调试
     */
    private String getStackTraceSummary(Exception e) {
        if (e.getStackTrace().length > 0) {
            StackTraceElement element = e.getStackTrace()[0];
            return String.format("%s.%s:%d", 
                    element.getClassName(), 
                    element.getMethodName(), 
                    element.getLineNumber());
        }
        return "未知位置";
    }
    
    /**
     * 验证操作类型并返回具体的错误消息
     * @param request 请求对象
     * @param expectedType 期望的操作类型
     * @return 如果验证通过返回null，否则返回错误消息
     */
    private String validateOperationType(FactorManagementRequest request, String expectedType) {
        if (request.getOperationType() == null) {
            return "操作类型不能为空，期望类型: " + expectedType;
        }
        if (!expectedType.equals(request.getOperationType())) {
            return "操作类型不匹配，期望: " + expectedType + "，实际: " + request.getOperationType();
        }
        return null;
    }
    
    /**F
     * 检查对象是否为null
     * @param obj 要检查的对象
     * @param fieldName 字段名称
     * @return 如果为null返回错误消息，否则返回null
     */
    private String checkNotNull(Object obj, String fieldName) {
        if (obj == null) {
            return fieldName + "不能为空";
        }
        return null;
    }
    
    // ==================== 衍生因子管理 ====================

    @Operation(summary = "创建衍生因子", description = "通过选择基础因子并配置权重创建新的衍生因子")
    @PostMapping("/derived-factors/basic")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> createDerivedFactor(
            @Valid @RequestBody FactorManagementRequest request) {
        log.info("[CREATE_DERIVED_FACTOR] 收到创建请求, operationType={}, baseFactorIds={}, weightConfigInfo是否存在={}",
                request.getOperationType(),
                request.getDerivedFactorInfo() != null ? request.getDerivedFactorInfo().getBaseFactorIds() : "null",
                request.getWeightConfigInfo() != null);

        String operationTypeError = validateOperationType(request, "CREATE_DERIVED_FACTOR");
        if (operationTypeError != null) {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(operationTypeError));
        }
        
        String derivedFactorInfoError = checkNotNull(request.getDerivedFactorInfo(), "衍生因子信息");
            if (derivedFactorInfoError != null) {
                String detailedError = String.format("[CREATE_DERIVED_FACTOR] 请求验证失败: %s, 请求内容: %s", 
                        derivedFactorInfoError, request);
                return ResponseEntity.badRequest()
                        .body(ResultDTO.error(detailedError));
            }
        
        // 自动设置为归一化处理后的加权组合策略（使用默认ID 1）
        if (request.getDerivedFactorInfo().getCalcStrategyId() == null) {
            request.getDerivedFactorInfo().setCalcStrategyId(1);
        }

        FactorManagementRequest.DerivedFactorInfo derivedFactorInfo = request.getDerivedFactorInfo();
        boolean hasCustomWeights = request.getWeightConfigInfo() != null
                && request.getWeightConfigInfo().getBaseFactorWeights() != null
                && !request.getWeightConfigInfo().getBaseFactorWeights().trim().isEmpty();

        return handleResponse("创建衍生因子",
            () -> {
                List<Integer> originalBaseFactorIds = derivedFactorInfo.getBaseFactorIds();
                if (hasCustomWeights) {
                    // 防止服务层自动写入等权
                    log.info("[CREATE_DERIVED_FACTOR] 检测到自定义权重，临时跳过默认等权配置, baseFactorIds={}", originalBaseFactorIds);
                    derivedFactorInfo.setBaseFactorIds(Collections.emptyList());
                }

                FactorManagementResponse createResponse = factorManagementService.createDerivedFactor(derivedFactorInfo);
                if (!createResponse.getSuccess()) {
                    log.warn("[CREATE_DERIVED_FACTOR] 创建衍生因子失败: {}", createResponse.getMessage());
                    // 恢复原始 baseFactorIds，避免影响后续请求
                    derivedFactorInfo.setBaseFactorIds(originalBaseFactorIds);
                    return createResponse;
                }

                Integer derivedId = createResponse.getDerivedFactorDataList() != null && 
                        !createResponse.getDerivedFactorDataList().isEmpty() 
                        ? createResponse.getDerivedFactorDataList().get(0).getDerivedId() 
                        : null;
                
                if (hasCustomWeights) {
                    FactorManagementRequest.WeightConfigInfo weightConfigInfo = request.getWeightConfigInfo();
                    // 自动设置衍生因子ID
                    weightConfigInfo.setDerivedId(derivedId);
                    log.info("[CREATE_DERIVED_FACTOR] 应用自定义权重, derivedId={}, weights={}", derivedId, weightConfigInfo.getBaseFactorWeights());

                    FactorManagementResponse weightResponse = factorManagementService.configureFactorWeights(weightConfigInfo);
                    if (!weightResponse.getSuccess()) {
                        log.error("[CREATE_DERIVED_FACTOR] 自定义权重配置失败: {}", weightResponse.getMessage());
                        throw new IllegalStateException("自定义权重配置失败: " + weightResponse.getMessage());
                    }

                    createResponse.setWeightConfigData(weightResponse.getWeightConfigData());
                    // 重新查询最新的因子及权重信息
                    FactorManagementResponse refreshed = factorManagementService.getDerivedFactor(derivedId);
                    if (refreshed.getSuccess() && refreshed.getDerivedFactorDataList() != null 
                            && !refreshed.getDerivedFactorDataList().isEmpty()) {
                        createResponse.setDerivedFactorDataList(refreshed.getDerivedFactorDataList());
                    } else {
                        log.warn("[CREATE_DERIVED_FACTOR] 权重已配置，但刷新衍生因子信息失败: {}", refreshed.getMessage());
                    }
                    // 恢复原始 baseFactorIds，便于后续逻辑复用 request 对象
                    derivedFactorInfo.setBaseFactorIds(originalBaseFactorIds);
                }
                
                // 如果提供了树节点操作信息，则添加到因子树
                if (request.getTreeOperationInfo() != null) {
                    log.info("[CREATE_DERIVED_FACTOR] 检测到树节点配置信息，准备添加到因子树");
                    
                    // 验证树节点信息
                    validateTreeOperationInfo(request.getTreeOperationInfo());
                    
                    // 自动设置 factorId（关联到新创建的衍生因子）
                    request.getTreeOperationInfo().setFactorId(derivedId);
                    request.getTreeOperationInfo().setIsLeaf(true); // 衍生因子是叶子节点
                    // nodeId 会在添加节点时自动生成，不需要设置
                    
                    // 优先使用衍生因子的名称作为节点名称，避免重复名称问题
                    String factorName = createResponse.getDerivedFactorDataList() != null && 
                            !createResponse.getDerivedFactorDataList().isEmpty() 
                            ? createResponse.getDerivedFactorDataList().get(0).getFactorName() 
                            : null;
                    String requestedNodeName = request.getTreeOperationInfo().getNodeName();
                    
                    // 如果用户指定了节点名称，先尝试使用，如果失败则使用因子名称
                    if (requestedNodeName != null && !requestedNodeName.trim().isEmpty()) {
                        // 先尝试使用用户指定的名称
                        request.getTreeOperationInfo().setNodeName(requestedNodeName);
                    } else {
                        // 如果没有指定，直接使用因子名称
                        request.getTreeOperationInfo().setNodeName(factorName);
                    }
                    
                    // 如果没有指定节点类型，默认为 FACTOR
                    if (request.getTreeOperationInfo().getNodeType() == null || 
                        request.getTreeOperationInfo().getNodeType().trim().isEmpty()) {
                        request.getTreeOperationInfo().setNodeType("FACTOR");
                    }
                    
                    // 添加到因子树
                    FactorManagementResponse treeResponse = factorManagementService.addFactorTreeNode(request.getTreeOperationInfo());
                    if (!treeResponse.getSuccess()) {
                        // 如果失败且是因为重复名称，尝试使用因子名称
                        if (treeResponse.getMessage() != null && 
                            treeResponse.getMessage().contains("已存在同名节点") &&
                            requestedNodeName != null && !requestedNodeName.trim().isEmpty() &&
                            !factorName.equals(requestedNodeName)) {
                            log.warn("[CREATE_DERIVED_FACTOR] 节点名称'{}'重复，尝试使用因子名称: {}", requestedNodeName, factorName);
                            request.getTreeOperationInfo().setNodeName(factorName);
                            treeResponse = factorManagementService.addFactorTreeNode(request.getTreeOperationInfo());
                            
                            if (!treeResponse.getSuccess()) {
                                log.error("[CREATE_DERIVED_FACTOR] 使用因子名称'{}'添加节点也失败: {}", factorName, treeResponse.getMessage());
                                // 如果因子名称也重复，尝试添加序号后缀
                                String nodeNameWithSuffix = factorName + "_" + derivedId;
                                log.warn("[CREATE_DERIVED_FACTOR] 尝试使用带ID后缀的节点名称: {}", nodeNameWithSuffix);
                                request.getTreeOperationInfo().setNodeName(nodeNameWithSuffix);
                                treeResponse = factorManagementService.addFactorTreeNode(request.getTreeOperationInfo());
                            }
                        }
                        
                        if (!treeResponse.getSuccess()) {
                            log.error("[CREATE_DERIVED_FACTOR] 树节点添加失败，最终错误: {}", treeResponse.getMessage());
                            log.warn("[CREATE_DERIVED_FACTOR] 因子已创建成功(derivedId: {})，但添加到树节点失败。请手动将因子添加到树中。", derivedId);
                            // 不抛出异常，只记录日志，因为因子已经创建成功
                        } else {
                            log.info("[CREATE_DERIVED_FACTOR] 成功添加到树节点，节点名称: {}", request.getTreeOperationInfo().getNodeName());
                            createResponse.setTreeData(treeResponse.getTreeData());
                        }
                    } else {
                        log.info("[CREATE_DERIVED_FACTOR] 因子添加到树节点成功，节点名称: {}", request.getTreeOperationInfo().getNodeName());
                        // 设置树节点数据到响应中
                        createResponse.setTreeData(treeResponse.getTreeData());
                    }
                }

                return createResponse;
            });
    }
    
    @Operation(summary = "创建衍生因子完整流程", description = "支持完整流程创建衍生因子的接口")
     @PostMapping("/derived-factors")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> createDerivedFactorWithFullFlow(
            @Valid @RequestBody FactorManagementRequest request) {
        log.info("[CREATE_DERIVED_FACTOR_WITH_FULL_FLOW] 收到完整流程创建请求, 基础因子={}, 权重配置={}, 树节点信息={}",
                request.getDerivedFactorInfo() != null ? request.getDerivedFactorInfo().getBaseFactorIds() : "null",
                request.getWeightConfigInfo() != null ? request.getWeightConfigInfo().getBaseFactorWeights() : "null",
                request.getTreeOperationInfo() != null ? request.getTreeOperationInfo().getParentId() : "null");
        // 设置操作类型为完整流程创建
        request.setOperationType("CREATE_DERIVED_FACTOR_WITH_FULL_FLOW");
        
        // 验证必填参数
        String derivedFactorInfoError = checkNotNull(request.getDerivedFactorInfo(), "衍生因子信息");
        if (derivedFactorInfoError != null) {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(derivedFactorInfoError));
        }
        
        try {
            // 验证衍生因子基本信息
            validateDerivedFactorInfo(request.getDerivedFactorInfo());
            
            // 验证树节点信息（如果提供）
            if (request.getTreeOperationInfo() != null) {
                validateTreeOperationInfo(request.getTreeOperationInfo());
            }
            
            // 调用服务层处理完整流程
            return handleResponse("创建衍生因子完整流程", 
                () -> factorManagementService.processFactorManagement(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(e.getMessage()));
        }
    }
    
    // 验证衍生因子信息
    private void validateDerivedFactorInfo(FactorManagementRequest.DerivedFactorInfo derivedFactorInfo) {
        if (derivedFactorInfo == null) {
            throw new IllegalArgumentException("衍生因子信息对象不能为空");
        }
        if (derivedFactorInfo.getFactorName() == null || derivedFactorInfo.getFactorName().trim().isEmpty()) {
            throw new IllegalArgumentException("因子名称不能为空，请提供有效的因子名称");
        }
        if (derivedFactorInfo.getFactorCode() == null || derivedFactorInfo.getFactorCode().trim().isEmpty()) {
            throw new IllegalArgumentException("因子编码不能为空，请提供有效的因子编码");
        }
        // 验证因子编码格式（假设需要符合特定格式）
        if (!derivedFactorInfo.getFactorCode().matches("^[A-Za-z0-9_]+$")) {
            throw new IllegalArgumentException("因子编码格式无效，只能包含字母、数字和下划线");
        }
        // 如果calcStrategyId为null，设置默认值为1（归一化处理后的加权组合）
        if (derivedFactorInfo.getCalcStrategyId() == null) {
            derivedFactorInfo.setCalcStrategyId(1);
        }
    }
    
    /**
     * 验证树节点操作信息
     * 确保父节点存在，防止向不存在的树节点添加因子
     */
    private void validateTreeOperationInfo(FactorManagementRequest.TreeOperationInfo treeOperationInfo) {
        if (treeOperationInfo == null) {
            return; // 如果树节点信息为空，跳过验证
        }
        
        Integer parentId = treeOperationInfo.getParentId();
        
        // 如果指定了parentId且不为0，验证父节点是否存在
        if (parentId != null && parentId != 0) {
            FactorTree parentNode = factorTreeMapper.getNodeById(parentId);
            if (parentNode == null) {
                throw new IllegalArgumentException("指定的父节点不存在，无法添加因子到树节点。parentId: " + parentId);
            }
            log.info("树节点验证通过，parentId: {}, 节点名称: {}, 节点类型: {}", 
                    parentId, parentNode.getNodeName(), parentNode.getNodeType());
        } else {
            // 如果没有指定parentId或parentId为0，表示要添加到根节点，需要验证场景ID
            String sceneId = treeOperationInfo.getSceneId();
            if (sceneId == null || sceneId.trim().isEmpty()) {
                throw new IllegalArgumentException("添加到根节点时必须指定场景ID");
            }
            log.info("将添加到场景根节点，sceneId: {}", sceneId);
        }
    }

    @Operation(summary = "更新衍生因子", description = "更新指定的衍生因子信息")
    @PutMapping("/derived-factors/{derivedId}")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> updateDerivedFactor(
            @Parameter(description = "衍生因子ID") @PathVariable @NotNull Integer derivedId,
            @Valid @RequestBody FactorManagementRequest request) {
        String operationTypeError = validateOperationType(request, "UPDATE_DERIVED_FACTOR");
        if (operationTypeError != null) {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(operationTypeError));
        }
        
        String derivedFactorInfoError = checkNotNull(request.getDerivedFactorInfo(), "衍生因子信息");
        if (derivedFactorInfoError != null) {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(derivedFactorInfoError));
        }
        
        // 自动设置为归一化处理后的加权组合策略（使用默认ID 1）
        if (request.getDerivedFactorInfo().getCalcStrategyId() == null) {
            request.getDerivedFactorInfo().setCalcStrategyId(1);
        }

        return handleResponse("更新衍生因子", 
            () -> factorManagementService.updateDerivedFactor(derivedId, request.getDerivedFactorInfo()));
    }

    @Operation(summary = "删除衍生因子", description = "删除指定的衍生因子")
    @DeleteMapping("/derived-factors/{derivedId}")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> deleteDerivedFactor(
            @Parameter(description = "衍生因子ID") @PathVariable @NotNull Integer derivedId) {
        return handleResponse("删除衍生因子", 
            () -> factorManagementService.deleteDerivedFactor(derivedId));
    }

    @Operation(summary = "查询衍生因子", description = "根据ID查询衍生因子详情")
    @GetMapping("/derived-factors/{derivedId}")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> getDerivedFactor(
            @Parameter(description = "衍生因子ID") @PathVariable @NotNull Integer derivedId) {
        return handleResponse("查询衍生因子", 
            () -> factorManagementService.getDerivedFactor(derivedId));
    }

    @Operation(summary = "查询所有衍生因子", description = "获取所有有效的衍生因子列表")
    @GetMapping("/derived-factors")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> getAllDerivedFactors() {
        return handleResponse("查询所有衍生因子", 
            () -> factorManagementService.getAllDerivedFactors());
    }

    // ==================== 权重配置管理 ====================

    @Operation(summary = "配置因子权重", description = "为衍生因子配置基础因子的权重")
    @PostMapping("/derived-factors/{derivedId}/weights")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> configureFactorWeights(
            @Parameter(description = "衍生因子ID") @PathVariable @NotNull Integer derivedId,
            @Valid @RequestBody FactorManagementRequest request) {
        log.info("[CONFIGURE_FACTOR_WEIGHTS] Controller收到请求, 路径derivedId={}, weights={}",
                derivedId,
                request.getWeightConfigInfo() != null ? request.getWeightConfigInfo().getBaseFactorWeights() : "null");

        String operationTypeError = validateOperationType(request, "CONFIGURE_FACTOR_WEIGHTS");
        if (operationTypeError != null) {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(operationTypeError));
        }
        
        String weightConfigError = checkNotNull(request.getWeightConfigInfo(), "权重配置信息");
        if (weightConfigError != null) {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(weightConfigError));
        }
        
        // 设置衍生因子ID
        request.getWeightConfigInfo().setDerivedId(derivedId);
        
        // 验证权重配置
        if (request.getWeightConfigInfo().getBaseFactorWeights() == null || request.getWeightConfigInfo().getBaseFactorWeights().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error("基础因子权重配置不能为空"));
        }
        
        return handleResponse("配置因子权重", 
            () -> factorManagementService.configureFactorWeights(request.getWeightConfigInfo()));
    }

    @Operation(summary = "验证因子权重", description = "验证衍生因子的权重配置是否有效")
    @PostMapping("/derived-factors/{derivedId}/weights/validate")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> validateFactorWeights(
            @Parameter(description = "衍生因子ID") @PathVariable @NotNull Integer derivedId,
            @Valid @RequestBody FactorManagementRequest request) {
        log.info("[VALIDATE_FACTOR_WEIGHTS] Controller收到请求, 路径derivedId={}, weights={}",
                derivedId,
                request.getWeightConfigInfo() != null ? request.getWeightConfigInfo().getBaseFactorWeights() : "null");

        String operationTypeError = validateOperationType(request, "VALIDATE_FACTOR_WEIGHTS");
        if (operationTypeError != null) {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(operationTypeError));
        }
        
        String weightConfigError = checkNotNull(request.getWeightConfigInfo(), "权重配置信息");
        if (weightConfigError != null) {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(weightConfigError));
        }
        
        // 设置衍生因子ID
        request.getWeightConfigInfo().setDerivedId(derivedId);
        
        // 验证权重配置
        if (request.getWeightConfigInfo().getBaseFactorWeights() == null || request.getWeightConfigInfo().getBaseFactorWeights().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error("基础因子权重配置不能为空"));
        }
        
        return handleResponse("验证因子权重", 
            () -> factorManagementService.validateFactorWeights(request.getWeightConfigInfo()));
    }



    // ==================== 统一操作入口 ====================

    // 移除了统一操作入口，改用RESTful风格的独立端点

    // ==================== 衍生因子创建流程 ====================

    @Operation(summary = "获取可用基础因子", description = "获取可用于创建衍生因子的基础因子列表")
    @GetMapping("/derived-factors/available-base-factors")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> getAvailableBaseFactorsForDerived() {
        return handleResponse("获取可用基础因子", 
            () -> factorManagementService.getAvailableBaseFactorsForDerived());
    }

    @Operation(summary = "预览衍生因子", description = "预览衍生因子的计算结果（使用归一化处理后的加权组合策略）")
    @PostMapping("/derived-factors/preview")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> previewDerivedFactor(
            @Parameter(description = "基础因子ID列表") @RequestParam @NotNull List<Integer> baseFactorIds,
            @Parameter(description = "权重配置") @RequestParam @NotNull Map<Integer, Double> weights) {
        // 使用默认的归一化处理后的加权组合策略（ID=1）
        Integer defaultCalcStrategyId = 1;
        return handleResponse("预览衍生因子", 
            () -> factorManagementService.previewDerivedFactor(baseFactorIds, weights, defaultCalcStrategyId));
    }

    // 注：批量创建功能待service层实现后添加

    // ==================== 工具方法 ====================

    @Operation(summary = "搜索因子", description = "根据关键词搜索因子")
    @GetMapping("/search")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> searchFactors(
            @Parameter(description = "搜索关键词") @RequestParam @NotNull String keyword,
            @Parameter(description = "因子类型") @RequestParam(required = false) String type,
            @Parameter(description = "场景ID") @RequestParam(required = false) String sceneId) {
        return handleResponse("搜索因子", 
            () -> factorManagementService.searchFactors(keyword, type, sceneId));
    }

    @Operation(summary = "获取因子统计信息", description = "获取因子管理的统计信息")
    @GetMapping("/statistics")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> getFactorStatistics() {
        return handleResponse("获取因子统计信息", 
            () -> factorManagementService.getFactorStatistics());
    }

    @Operation(summary = "验证因子数据", description = "验证基础因子数据的完整性")
    @PostMapping("/validate-data")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> validateFactorData(
            @Parameter(description = "基础因子ID列表") @RequestBody @NotNull List<Integer> baseFactorIds) {
        return handleResponse("验证因子数据", 
            () -> factorManagementService.validateFactorData(baseFactorIds));
    }

    // ==================== 风格标签相关查询 ====================

    @Operation(summary = "根据风格标签编码查询衍生因子", description = "根据风格标签编码查询关联的所有衍生因子")
    @GetMapping("/derived-factors/by-style-tag-code/{tagCode}")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> getDerivedFactorsByStyleTagCode(
            @Parameter(description = "风格标签编码") @PathVariable @NotNull String tagCode) {
        return handleResponse("根据风格标签编码查询衍生因子", 
            () -> factorManagementService.getDerivedFactorsByStyleTagCode(tagCode));
    }

    @Operation(summary = "根据多个风格标签编码查询衍生因子", description = "查询满足任一风格标签的所有衍生因子")
    @GetMapping("/derived-factors/by-style-tag-codes")
    public ResponseEntity<ResultDTO<FactorManagementResponse>> getDerivedFactorsByStyleTagCodes(
            @Parameter(description = "风格标签编码列表，用逗号分隔") @RequestParam @NotNull String tagCodes) {
        List<String> tagCodeList = Arrays.stream(tagCodes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
        return handleResponse("根据风格标签编码列表查询衍生因子", 
            () -> factorManagementService.getDerivedFactorsByStyleTagCodes(tagCodeList));
    }

    @Operation(summary = "根据风格标签ID查询衍生因子（已废弃，请使用by-style-tag-code）", description = "查询指定风格标签关联的所有衍生因子")
    @GetMapping("/derived-factors/by-style-tag/{tagId}")
    @Deprecated
    public ResponseEntity<ResultDTO<FactorManagementResponse>> getDerivedFactorsByStyleTag(
            @Parameter(description = "风格标签ID") @PathVariable @NotNull Integer tagId) {
        return handleResponse("根据风格标签查询衍生因子", 
            () -> factorManagementService.getDerivedFactorsByStyleTagId(tagId));
    }


}