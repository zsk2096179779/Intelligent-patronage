package com.fengqi.fund.fundadvisor.dto.factor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * 权重配置请求DTO
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
@Data
@Schema(description = "权重配置请求")
public class WeightConfigRequest {
    
    @NotNull(message = "衍生因子ID不能为空")
    @Positive(message = "衍生因子ID必须为正数")
    @Schema(description = "衍生因子ID", required = true, example = "1")
    private Integer derivedFactorId;
    
    @NotNull(message = "因子权重列表不能为空")
    @Schema(description = "因子权重配置列表", required = true)
    private List<FactorWeightItem> factorWeights;
    
    @Schema(description = "是否自动归一化权重", example = "true")
    private Boolean autoNormalize = true;
    
    @Schema(description = "权重总和验证阈值", example = "1.0")
    private Double weightSumThreshold = 1.0;
    
    @Schema(description = "备注", example = "价值风格因子权重配置")
    private String remark;
    
    /**
     * 因子权重项
     */
    @Data
    @Schema(description = "因子权重项")
    public static class FactorWeightItem {
        
        @NotNull(message = "基础因子ID不能为空")
        @Positive(message = "基础因子ID必须为正数")
        @Schema(description = "基础因子ID", required = true, example = "1")
        private Integer baseFactorId;
        
        @NotNull(message = "权重值不能为空")
        @DecimalMin(value = "0.0", message = "权重值不能小于0")
        @DecimalMax(value = "1.0", message = "权重值不能大于1")
        @Schema(description = "权重值(0-1)", required = true, example = "0.3")
        private Double weight;
        
        @Schema(description = "是否启用", example = "true")
        private Boolean enabled = true;
    }
}