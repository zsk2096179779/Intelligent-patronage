package com.fengqi.fund.fundadvisor.dto.factor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 因子查询请求DTO
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
@Data
@Schema(description = "因子查询请求")
public class FactorQueryRequest {
    
    @Schema(description = "搜索关键词", example = "市盈率")
    private String keyword;
    
    @Schema(description = "因子类型", example = "估值")
    private String factorType;
    
    @Schema(description = "数据源", example = "Wind")
    private String dataSource;
    
    @Schema(description = "是否只查询热门因子", example = "false")
    private Boolean popularOnly;
    
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "页面大小", example = "20")
    private Integer pageSize = 20;
    
    @Schema(description = "是否有效", example = "true")
    private Boolean isValid = true;
}