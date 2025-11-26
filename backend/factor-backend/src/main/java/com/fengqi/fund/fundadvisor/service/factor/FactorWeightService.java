package com.fengqi.fund.fundadvisor.service.factor;

import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.dto.factor.WeightConfigRequest;
import com.fengqi.fund.fundadvisor.dto.factor.WeightConfigResponse;
import com.fengqi.fund.fundadvisor.dto.factor.WeightValidationResult;

import java.util.List;
import java.util.Map;

/**
 * 因子权重服务接口
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
public interface FactorWeightService {
    
    /**
     * 配置因子权重
     * 
     * @param request 权重配置请求
     * @return 配置结果
     */
    ResultDTO<WeightConfigResponse> configureWeights(WeightConfigRequest request);
    
    /**
     * 获取权重配置
     * 
     * @param derivedFactorId 衍生因子ID
     * @return 权重配置详情
     */
    ResultDTO<WeightConfigResponse> getWeightConfig(Integer derivedFactorId);
    
    /**
     * 验证权重配置
     * 
     * @param request 权重配置请求
     * @return 验证结果
     */
    ResultDTO<WeightValidationResult> validateWeights(WeightConfigRequest request);
    
    /**
     * 归一化权重
     * 
     * @param request 权重配置请求
     * @return 归一化后的权重配置
     */
    ResultDTO<WeightConfigResponse> normalizeWeights(WeightConfigRequest request);
    
    /**
     * 更新单个因子权重
     * 
     * @param weightId 权重ID
     * @param weight 新权重值
     * @return 更新结果
     */
    ResultDTO<Boolean> updateSingleWeight(Integer weightId, Double weight);
    
    /**
     * 启用/禁用因子权重
     * 
     * @param weightId 权重ID
     * @param enabled 是否启用
     * @return 更新结果
     */
    ResultDTO<Boolean> toggleWeightEnabled(Integer weightId, Boolean enabled);
    
    /**
     * 删除权重配置
     * 
     * @param weightId 权重ID
     * @return 删除结果
     */
    ResultDTO<Boolean> deleteWeight(Integer weightId);
    
    /**
     * 批量更新权重
     * 
     * @param derivedFactorId 衍生因子ID
     * @param weightItems 权重项列表
     * @return 更新结果
     */
    ResultDTO<WeightConfigResponse> batchUpdateWeights(Integer derivedFactorId, 
                                                        List<WeightConfigRequest.FactorWeightItem> weightItems);
    
    /**
     * 重置为等权重
     * 
     * @param derivedFactorId 衍生因子ID
     * @return 重置结果
     */
    ResultDTO<WeightConfigResponse> resetToEqualWeights(Integer derivedFactorId);
    
    /**
     * 获取权重统计信息
     * 
     * @param derivedFactorId 衍生因子ID
     * @return 统计信息
     */
    ResultDTO<Map<String, Object>> getWeightStatistics(Integer derivedFactorId);
    
    /**
     * 复制权重配置
     * 
     * @param sourceDerivedFactorId 源衍生因子ID
     * @param targetDerivedFactorId 目标衍生因子ID
     * @return 复制结果
     */
    ResultDTO<WeightConfigResponse> copyWeightConfig(Integer sourceDerivedFactorId, Integer targetDerivedFactorId);
}