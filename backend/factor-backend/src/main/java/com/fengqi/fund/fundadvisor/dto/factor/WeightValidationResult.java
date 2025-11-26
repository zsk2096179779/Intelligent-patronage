package com.fengqi.fund.fundadvisor.dto.factor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 权重验证结果DTO
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
@Data
@Schema(description = "权重验证结果")
public class WeightValidationResult {
    
    @Schema(description = "是否验证通过", example = "true")
    private Boolean valid;
    
    @Schema(description = "验证消息", example = "权重配置验证通过")
    private String message;
    
    @Schema(description = "权重总和", example = "1.0")
    private Double totalWeight;
    
    @Schema(description = "验证错误列表")
    private List<ValidationError> errors;
    
    @Schema(description = "权重警告列表")
    private List<ValidationWarning> warnings;
    
    @Schema(description = "建议的归一化权重")
    private List<NormalizedWeight> suggestedWeights;
    
    /**
     * 验证错误
     */
    @Data
    @Schema(description = "验证错误")
    public static class ValidationError {
        
        @Schema(description = "错误代码", example = "WEIGHT_EXCEEDS_LIMIT")
        private String code;
        
        @Schema(description = "错误消息", example = "因子ID为1的权重值超过上限")
        private String message;
        
        @Schema(description = "基础因子ID", example = "1")
        private Integer baseFactorId;
        
        @Schema(description = "基础因子名称", example = "市盈率倒数")
        private String baseFactorName;
        
        @Schema(description = "当前权重值", example = "1.5")
        private Double currentValue;
        
        @Schema(description = "允许的最大值", example = "1.0")
        private Double maxValue;
    }
    
    /**
     * 验证警告
     */
    @Data
    @Schema(description = "验证警告")
    public static class ValidationWarning {
        
        @Schema(description = "警告代码", example = "WEIGHT_TOO_SMALL")
        private String code;
        
        @Schema(description = "警告消息", example = "因子ID为2的权重值过小，可能影响因子效果")
        private String message;
        
        @Schema(description = "基础因子ID", example = "2")
        private Integer baseFactorId;
        
        @Schema(description = "基础因子名称", example = "市净率倒数")
        private String baseFactorName;
        
        @Schema(description = "当前权重值", example = "0.01")
        private Double currentValue;
        
        @Schema(description = "建议的最小值", example = "0.05")
        private Double suggestedMinValue;
    }
    
    /**
     * 归一化权重建议
     */
    @Data
    @Schema(description = "归一化权重建议")
    public static class NormalizedWeight {
        
        @Schema(description = "基础因子ID", example = "1")
        private Integer baseFactorId;
        
        @Schema(description = "基础因子名称", example = "市盈率倒数")
        private String baseFactorName;
        
        @Schema(description = "原始权重", example = "1.5")
        private Double originalWeight;
        
        @Schema(description = "建议权重", example = "0.6")
        private Double suggestedWeight;
        
        @Schema(description = "建议权重百分比", example = "60.0")
        private Double suggestedPercentage;
    }
}