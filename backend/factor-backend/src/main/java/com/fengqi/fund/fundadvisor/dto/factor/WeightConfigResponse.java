package com.fengqi.fund.fundadvisor.dto.factor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权重配置响应DTO
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
@Data
@Schema(description = "权重配置响应")
public class WeightConfigResponse {
    
    @Schema(description = "权重配置ID", example = "1")
    private Integer weightId;
    
    @Schema(description = "衍生因子ID", example = "1")
    private Integer derivedFactorId;
    
    @Schema(description = "衍生因子名称", example = "价值成长复合因子")
    private String derivedFactorName;
    
    @Schema(description = "因子权重详情列表")
    private List<FactorWeightDetail> factorWeights;
    
    @Schema(description = "权重总和", example = "1.0")
    private Double totalWeight;
    
    @Schema(description = "是否已归一化", example = "true")
    private Boolean isNormalized;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @Schema(description = "创建者", example = "admin")
    private String creator;
    
    @Schema(description = "备注", example = "价值风格因子权重配置")
    private String remark;
    
    /**
     * 因子权重详情
     */
    @Data
    @Schema(description = "因子权重详情")
    public static class FactorWeightDetail {
        
        @Schema(description = "权重ID", example = "1")
        private Integer weightId;
        
        @Schema(description = "基础因子ID", example = "1")
        private Integer baseFactorId;
        
        @Schema(description = "基础因子名称", example = "市盈率倒数")
        private String baseFactorName;
        
        @Schema(description = "基础因子代码", example = "PE_INV")
        private String baseFactorCode;
        
        @Schema(description = "数据源", example = "Wind")
        private String dataSource;
        
        @Schema(description = "权重值", example = "0.3")
        private Double weight;
        
        @Schema(description = "权重百分比", example = "30.0")
        private Double weightPercentage;
        
        @Schema(description = "是否启用", example = "true")
        private Boolean enabled;
    }
}