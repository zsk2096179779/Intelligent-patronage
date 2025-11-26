package com.fengqi.fund.fundadvisor.service.factor.dto;

import lombok.Data;
import lombok.Builder;
import java.util.List;
import java.util.Map;

/**
 * 因子计算上下文
 * 
 * 存储公式解析后的计算信息
 * 包含表达式、数据字段、过滤条件等
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Data
@Builder
public class FactorCalculationContext {
    
    /**
     * 因子名称标识符
     */
    private String factorName;
    
    /**
     * 计算表达式
     */
    private String expression;
    
    /**
     * 所需的数据字段列表
     */
    private List<String> dataFields;
    
    /**
     * 数据过滤条件
     */
    private String filterCondition;
    
    /**
     * 聚合方式
     */
    private String aggregationMethod;
    
    /**
     * 数据表映射
     * 字段名到数据源的映射关系
     */
    private Map<String, String> dataSourceMapping;
    
    /**
     * 计算参数
     * 额外的计算参数配置
     */
    private Map<String, Object> calculationParams;
    
    /**
     * 时间窗口参数
     * 用于时间序列计算
     */
    private TimeWindow timeWindow;
    
    /**
     * 是否有效
     */
    private Boolean isValid;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 时间窗口配置
     */
    @Data
    @Builder
    public static class TimeWindow {
        /**
         * 窗口大小（天数）
         */
        private Integer windowSize;
        
        /**
         * 时间偏移（天数）
         */
        private Integer offset;
        
        /**
         * 是否包含当日
         */
        private Boolean includeCurrent;
    }
}