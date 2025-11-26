package com.fengqi.fund.fundadvisor.controller.factor;

import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.dto.factor.FactorQueryRequest;
import com.fengqi.fund.fundadvisor.dto.factor.FactorSelectionRequest;
import com.fengqi.fund.fundadvisor.dto.factor.FactorPreviewResponse;
import com.fengqi.fund.fundadvisor.entity.factor.FactorBase;
import com.fengqi.fund.fundadvisor.service.factor.FactorBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基础因子管理控制器
 * 
 * 提供基础因子查询、选择、预览等功能，专为加权合成衍生因子服务
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
@Slf4j
@RestController
@RequestMapping("/api/factor/base")
@Tag(name = "基础因子管理", description = "基础因子的查询、选择、预览等操作")
public class FactorBaseController {
    
    @Autowired
    private FactorBaseService factorBaseService;
    
    @GetMapping("/query")
    @Operation(summary = "查询基础因子", description = "支持分页、搜索、筛选等多种查询方式")
    public ResultDTO<List<FactorBase>> queryFactors(FactorQueryRequest request) {
        log.info("查询基础因子，请求参数: {}", request);
        return factorBaseService.queryFactors(request);
    }
    
    @GetMapping("/ids")
    @Operation(summary = "根据ID列表获取因子", description = "根据多个ID批量获取基础因子")
    public ResultDTO<List<FactorBase>> getFactorsByIds(
            @Parameter(description = "因子ID列表，用逗号分隔")
            @RequestParam String factorIds) {
        List<Integer> idList = java.util.Arrays.stream(factorIds.split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .collect(java.util.stream.Collectors.toList());
        log.info("根据ID列表获取因子，IDs: {}", idList);
        return factorBaseService.getFactorsByIds(idList);
    }
    
    @PostMapping("/selection/validate")
    @Operation(summary = "验证因子选择", description = "验证选中的基础因子是否有效")
    public ResultDTO<Boolean> validateFactorSelection(
            @Parameter(description = "因子ID列表")
            @RequestParam String factorIds) {
        List<Integer> idList = java.util.Arrays.stream(factorIds.split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .collect(java.util.stream.Collectors.toList());
        log.info("验证因子选择，IDs: {}", idList);
        return factorBaseService.validateFactorSelection(idList);
    }
    
    @PostMapping("/selection/preview")
    @Operation(summary = "预览选中因子", description = "预览选中的基础因子数据")
    public ResultDTO<FactorPreviewResponse> previewSelectedFactors(
            @RequestBody FactorSelectionRequest request) {
        log.info("预览选中因子，请求: {}", request);
        return factorBaseService.previewSelectedFactors(request);
    }
}