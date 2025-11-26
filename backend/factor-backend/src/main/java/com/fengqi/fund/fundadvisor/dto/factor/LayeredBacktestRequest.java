package com.fengqi.fund.fundadvisor.dto.factor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 分层回测创建请求DTO
 */
@Data
@Schema(description = "分层回测创建请求")
public class LayeredBacktestRequest {
    
    @NotBlank(message = "任务名称不能为空")
    @Schema(description = "任务名称", example = "价值因子分层回测")
    private String taskName;
    
    @NotEmpty(message = "因子名称列表不能为空")
    @Schema(description = "因子名称列表", example = "[\"市盈率因子\", \"市净率因子\", \"动量因子\"]")
    private List<String> factorNames;
    
    @NotNull(message = "开始日期不能为空")
    @PastOrPresent(message = "开始日期不能晚于今天")
    @Schema(description = "回测开始日期", example = "2023-01-01")
    private LocalDate startDate;
    
    @NotNull(message = "结束日期不能为空")
    @PastOrPresent(message = "结束日期不能晚于今天")
    @Schema(description = "回测结束日期", example = "2024-01-01")
    private LocalDate endDate;
    
    @Schema(description = "分位数数量", example = "5", defaultValue = "5")
    private Integer quantileCount = 5;
    
    @Schema(description = "再平衡频率", example = "MONTHLY", defaultValue = "MONTHLY")
    private String rebalanceFrequency = "MONTHLY";
    
    @Schema(description = "是否等权重分配", example = "true", defaultValue = "true")
    private Boolean equalWeighted = true;
    
    @Schema(description = "指定基金代码（可选，如果为空则计算所有基金）", example = "159001")
    private String fundCode;
    
    @Schema(description = "基准指数代码", example = "000300")
    private String benchmarkCode;
    
    @Schema(description = "最小股票数量", example = "30", defaultValue = "30")
    private Integer minStockCount = 30;
    
    @Schema(description = "最大股票数量", example = "100", defaultValue = "100")
    private Integer maxStockCount = 100;
    
    @Schema(description = "是否剔除ST股票", example = "true", defaultValue = "true")
    private Boolean excludeStStocks = true;
    
    @Schema(description = "是否剔除停牌股票", example = "true", defaultValue = "true")
    private Boolean excludeSuspendedStocks = true;
    
    @Schema(description = "是否剔除上市不足30天股票", example = "true", defaultValue = "true")
    private Boolean excludeNewStocks = true;
    
    @Schema(description = "备注")
    private String remarks;
}