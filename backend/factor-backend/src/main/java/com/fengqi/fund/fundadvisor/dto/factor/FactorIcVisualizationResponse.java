package com.fengqi.fund.fundadvisor.dto.factor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 因子IC检验可视化数据响应DTO
 * 用于返回分位数累计收益率曲线、年化收益率柱状图和IC统计图表数据
 */
@Data
@Schema(description = "因子IC检验可视化数据响应")
public class FactorIcVisualizationResponse {
    
    @Schema(description = "操作是否成功", example = "true")
    private Boolean success;
    
    @Schema(description = "响应消息", example = "查询成功")
    private String message;
    
    @Schema(description = "任务ID", example = "1")
    private Integer taskId;
    
    @Schema(description = "因子ID", example = "1")
    private Integer factorId;
    
    @Schema(description = "因子名称", example = "估值综合因子")
    private String factorName;
    
    @Schema(description = "分位数累计收益率曲线数据")
    private QuantileCumulativeReturnData cumulativeReturnData;
    
    @Schema(description = "分位数平均年化收益率柱状图数据")
    private QuantileAnnualizedReturnData annualizedReturnData;
    
    @Schema(description = "IC检验统计图表数据（Alphalens风格）")
    private IcTearSheetData icTearSheetData;
    
    /**
     * 分位数累计收益率曲线数据
     */
    @Data
    @Schema(description = "分位数累计收益率曲线数据")
    public static class QuantileCumulativeReturnData {
        @Schema(description = "日期列表（X轴）")
        private List<LocalDate> dates;
        
        @Schema(description = "各分位数的累计收益率序列（分位数 -> 日期 -> 累计收益率）")
        private Map<Integer, List<BigDecimal>> quantileReturns;
        
        @Schema(description = "分位数描述（1=低分位组, 5=高分位组）")
        private Map<Integer, String> quantileDescriptions;
    }
    
    /**
     * 分位数平均年化收益率柱状图数据
     */
    @Data
    @Schema(description = "分位数平均年化收益率柱状图数据")
    public static class QuantileAnnualizedReturnData {
        @Schema(description = "分位数列表（1-5）")
        private List<Integer> quantiles;
        
        @Schema(description = "各分位数的平均年化收益率（%）")
        private List<BigDecimal> annualizedReturns;
        
        @Schema(description = "各分位数的年化收益率标准差（%）")
        private List<BigDecimal> annualizedReturnStds;
        
        @Schema(description = "分位数描述")
        private List<String> quantileDescriptions;
    }
    
    /**
     * IC检验统计图表数据（Alphalens风格）
     */
    @Data
    @Schema(description = "IC检验统计图表数据（Alphalens风格）")
    public static class IcTearSheetData {
        @Schema(description = "IC序列数据（用于绘制IC走势图）")
        private IcSequenceData icSequenceData;
        
        @Schema(description = "IC分布直方图数据")
        private IcDistributionData icDistributionData;
        
        @Schema(description = "IC统计汇总表")
        private IcStatisticsTable icStatisticsTable;
        
        @Schema(description = "IC滚动统计（如30日滚动均值）")
        private IcRollingStatistics icRollingStatistics;
    }
    
    /**
     * IC序列数据
     */
    @Data
    @Schema(description = "IC序列数据")
    public static class IcSequenceData {
        @Schema(description = "日期列表")
        private List<LocalDate> dates;
        
        @Schema(description = "IC值列表")
        private List<BigDecimal> icValues;
        
        @Schema(description = "Rank IC值列表（可选）")
        private List<BigDecimal> rankIcValues;
        
        @Schema(description = "IC滚动均值（如30日滚动均值）")
        private List<BigDecimal> rollingMean;
    }
    
    /**
     * IC分布直方图数据
     */
    @Data
    @Schema(description = "IC分布直方图数据")
    public static class IcDistributionData {
        @Schema(description = "区间边界（用于直方图）")
        private List<BigDecimal> bins;
        
        @Schema(description = "每个区间的频数")
        private List<Integer> frequencies;
        
        @Schema(description = "IC均值")
        private BigDecimal mean;
        
        @Schema(description = "IC标准差")
        private BigDecimal std;
    }
    
    /**
     * IC统计汇总表
     */
    @Data
    @Schema(description = "IC统计汇总表")
    public static class IcStatisticsTable {
        @Schema(description = "IC均值")
        private BigDecimal icMean;
        
        @Schema(description = "IC标准差")
        private BigDecimal icStd;
        
        @Schema(description = "IR值（IC均值/IC标准差）")
        private BigDecimal irValue;
        
        @Schema(description = "IC正相关比例（IC>0的比例）")
        private BigDecimal icPositiveRatio;
        
        @Schema(description = "IC最小值")
        private BigDecimal icMin;
        
        @Schema(description = "IC最大值")
        private BigDecimal icMax;
        
        @Schema(description = "IC中位数")
        private BigDecimal icMedian;
        
        @Schema(description = "IC偏度")
        private BigDecimal icSkewness;
        
        @Schema(description = "IC峰度")
        private BigDecimal icKurtosis;
        
        @Schema(description = "样本数量（交易日数）")
        private Integer sampleCount;
    }
    
    /**
     * IC滚动统计
     */
    @Data
    @Schema(description = "IC滚动统计")
    public static class IcRollingStatistics {
        @Schema(description = "日期列表")
        private List<LocalDate> dates;
        
        @Schema(description = "滚动均值（如30日滚动均值）")
        private List<BigDecimal> rollingMean;
        
        @Schema(description = "滚动标准差（如30日滚动标准差）")
        private List<BigDecimal> rollingStd;
        
        @Schema(description = "滚动窗口大小（天数）")
        private Integer windowSize;
    }
}

