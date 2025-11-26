package com.fengqi.fund.fundadvisor.dto.factor;

import lombok.Data;

import java.util.List;

/**
 * 因子选择请求DTO
 * 
 * 用于多选基础因子作为衍生因子输入的请求参数
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
@Data
public class FactorSelectionRequest {
    
    /**
     * 选中的基础因子ID列表
     */
    private List<Integer> factorIds;
    
    /**
     * 衍生因子名称
     */
    private String derivedFactorName;
    
    /**
     * 衍生因子描述
     */
    private String description;
    
    /**
     * 计算策略（等权、加权等）
     */
    private String calcStrategy;
    
    /**
     * 是否预览数据
     */
    private Boolean previewData;
    
    /**
     * 预览数据的时间范围
     */
    private String previewPeriod;
}