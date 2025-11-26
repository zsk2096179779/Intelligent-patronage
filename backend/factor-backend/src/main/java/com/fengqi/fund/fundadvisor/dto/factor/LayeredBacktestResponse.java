package com.fengqi.fund.fundadvisor.dto.factor;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分层回测响应数据传输对象
 */
@Data
@Schema(description = "分层回测响应数据")
public class LayeredBacktestResponse {
    
    @Schema(description = "操作类型")
    private String operationType;
    
    @Schema(description = "是否成功")
    private Boolean success;
    
    @Schema(description = "消息")
    private String message;
    
    @Schema(description = "操作时间")
    private LocalDateTime operationTime;
    
    @Schema(description = "任务信息")
    private TaskInfo taskInfo;
    
    @Schema(description = "分位数累计收益率曲线数据")
    private List<QuantileCumulativeReturn> cumulativeReturnCurve;
    
    @Schema(description = "分位数平均年化收益率数据")
    private List<QuantileAnnualizedReturn> annualizedReturnChart;
    
    @Schema(description = "分位数收益分布箱线图数据")
    private List<QuantileReturnDistribution> returnDistributionChart;
    
    /**
     * 任务信息
     */
    @Data
    @Schema(description = "任务信息")
    public static class TaskInfo {
        @Schema(description = "任务ID")
        private Integer taskId;
        
        @Schema(description = "任务名称")
        private String taskName;
        
        @Schema(description = "任务类型")
        private String taskType;
        
        @Schema(description = "任务状态")
        private String taskStatus;
        
        @Schema(description = "进度")
        private Integer progress;
        
        @Schema(description = "创建时间")
        private LocalDateTime createTime;
        
        @Schema(description = "开始时间")
        private LocalDateTime startTime;
        
        @Schema(description = "结束时间")
        private LocalDateTime endTime;
    }
    
    /**
     * 分位数累计收益率曲线数据点
     */
    @Data
    @Schema(description = "分位数累计收益率曲线数据")
    public static class QuantileCumulativeReturn {
        @Schema(description = "分位数（1-5，分别代表5个分位组）")
        private Integer quantile;
        
        @Schema(description = "日期-收益率映射")
        private Map<LocalDate, BigDecimal> returnSeries;
        
        @Schema(description = "总收益率")
        private BigDecimal totalReturn;
        
        @Schema(description = "年化收益率")
        private BigDecimal annualizedReturn;
        
        @Schema(description = "夏普比率")
        private BigDecimal sharpeRatio;
        
        @Schema(description = "最大回撤")
        private BigDecimal maxDrawdown;
    }
    
    /**
     * 分位数平均年化收益率数据
     */
    @Data
    @Schema(description = "分位数平均年化收益率数据")
    public static class QuantileAnnualizedReturn {
        @Schema(description = "分位数（1-5）")
        private Integer quantile;
        
        @Schema(description = "分位数名称（如：Q1-低分位组）")
        private String quantileName;
        
        @Schema(description = "平均年化收益率")
        private BigDecimal avgAnnualizedReturn;
        
        @Schema(description = "年化收益率标准差")
        private BigDecimal annualizedReturnStd;
        
        @Schema(description = "正收益比例")
        private BigDecimal positiveReturnRatio;
        
        @Schema(description = "胜率")
        private BigDecimal winRate;
    }
    
    /**
     * 分位数收益分布箱线图数据
     */
    @Data
    @Schema(description = "分位数收益分布箱线图数据")
    public static class QuantileReturnDistribution {
        @Schema(description = "分位数（1-5）")
        private Integer quantile;
        
        @Schema(description = "分位数名称")
        private String quantileName;
        
        @Schema(description = "持有期（天）")
        private Integer holdingPeriod;
        
        @Schema(description = "收益率数据集")
        private List<BigDecimal> returns;
        
        @Schema(description = "最小值")
        private BigDecimal minValue;
        
        @Schema(description = "第一四分位数(Q1)")
        private BigDecimal q1Return;
        
        @Schema(description = "中位数(Q2)")
        private BigDecimal medianReturn;
        
        @Schema(description = "第三四分位数(Q3)")
        private BigDecimal q3Return;
        
        @Schema(description = "最大值")
        private BigDecimal maxReturn;
        
        @Schema(description = "最小值")
        private BigDecimal minReturn;
        
        @Schema(description = "异常值列表")
        private List<BigDecimal> outliers;
        
        @Schema(description = "平均值")
        private BigDecimal meanReturn;
        
        @Schema(description = "标准差")
        private BigDecimal stdReturn;
        
        @Schema(description = "胜率")
        private BigDecimal winRate;
    }
}