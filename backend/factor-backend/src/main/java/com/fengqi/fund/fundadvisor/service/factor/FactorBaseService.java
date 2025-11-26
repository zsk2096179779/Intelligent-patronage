package com.fengqi.fund.fundadvisor.service.factor;

import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.dto.factor.FactorQueryRequest;
import com.fengqi.fund.fundadvisor.dto.factor.FactorSelectionRequest;
import com.fengqi.fund.fundadvisor.dto.factor.FactorPreviewResponse;
import com.fengqi.fund.fundadvisor.entity.factor.FactorBase;

import java.util.List;

/**
 * 基础因子服务接口
 * 
 * 提供基础因子查询、选择、预览等功能，专为加权合成衍生因子服务
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
public interface FactorBaseService {
    
    /**
     * 查询基础因子（支持分页、搜索、筛选）
     */
    ResultDTO<List<FactorBase>> queryFactors(FactorQueryRequest request);
    
    /**
     * 根据多个ID获取因子
     */
    ResultDTO<List<FactorBase>> getFactorsByIds(List<Integer> factorIds);
    
    /**
     * 预览选中的因子数据
     */
    ResultDTO<FactorPreviewResponse> previewSelectedFactors(FactorSelectionRequest request);
    
    /**
     * 验证选中的因子是否有效
     */
    ResultDTO<Boolean> validateFactorSelection(List<Integer> factorIds);
}