package com.fengqi.fund.fundadvisor.dto.factor;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 因子预览响应DTO
 * 
 * 用于展示选中因子的数据预览信息
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
@Data
public class FactorPreviewResponse {
    
    /**
     * 选中的因子基本信息
     */
    private List<FactorBasicInfo> factors;
    
    /**
     * 数据预览表格
     */
    private List<Map<String, Object>> dataPreview;
    
    /**
     * 预览数据的日期范围
     */
    private DateRange dateRange;
    
    /**
     * 数据统计信息
     */
    private DataStatistics statistics;
    
    @Data
    public static class FactorBasicInfo {
        private Integer baseId;
        private String factorName;
        private String factorCode;
        private String displayName;
        private String factorType;
        private String dataSource;
        private LocalDate dataStartDate;
        private LocalDate latestDataDate;
        private String dataDesc;
    }
    
    @Data
    public static class DateRange {
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalDays;
    }
    
    @Data
    public static class DataStatistics {
        private Integer totalRecords;
        private Integer validRecords;
        private Double missingRate;
        private Map<String, Double> factorStats;
    }
}