package com.fengqi.fund.fundadvisor.service.factor.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 因子计算结果
 * 
 * 存储因子计算的详细信息
 * 包含计算结果、数据质量信息、执行统计等
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Data
@Builder
public class FactorCalculationResult {
    
    /**
     * 基础因子ID或衍生因子ID
     */
    private Integer factorId;
    
    /**
     * 股票代码
     */
    private String stockCode;
    
    /**
     * 计算日期
     */
    private String calculateDate;
    
    /**
     * 因子值
     */
    private BigDecimal factorValue;
    
    /**
     * 计算状态
     */
    private CalculationStatus status;
    
    /**
     * 数据来源
     */
    private String dataSource;
    
    /**
     * 使用的公式
     */
    private String formula;
    
    /**
     * 计算时间戳
     */
    private LocalDateTime calculateTime;
    
    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;
    
    /**
     * 数据质量信息
     */
    private DataQualityInfo dataQualityInfo;
    
    /**
     * 中间计算结果
     * 存储计算过程中的中间值
     */
    private Map<String, Object> intermediateResults;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 计算状态枚举
     */
    public enum CalculationStatus {
        SUCCESS("计算成功"),
        FAILED("计算失败"),
        INSUFFICIENT_DATA("数据不足"),
        DIVISION_BY_ZERO("除零错误"),
        INVALID_FORMULA("公式无效"),
        TIMEOUT("计算超时"),
        PERMISSION_DENIED("权限不足");
        
        private final String description;
        
        CalculationStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 数据质量信息
     */
    @Data
    @Builder
    public static class DataQualityInfo {
        
        /**
         * 数据完整性百分比
         */
        private Double completenessRatio;
        
        /**
         * 缺失值数量
         */
        private Integer missingCount;
        
        /**
         * 异常值数量
         */
        private Integer outlierCount;
        
        /**
         * 数据最早日期
         */
        private String earliestDate;
        
        /**
         * 数据最新日期
         */
        private String latestDate;
        
        /**
         * 数据样本量
         */
        private Integer sampleSize;
        
        /**
         * 是否通过质量检查
         */
        private Boolean qualityPassed;
    }
}