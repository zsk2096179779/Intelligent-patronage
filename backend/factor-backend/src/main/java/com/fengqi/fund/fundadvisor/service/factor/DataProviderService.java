package com.fengqi.fund.fundadvisor.service.factor;

import java.util.List;
import java.util.Map;

/**
 * 数据提供服务接口
 * 
 * 负责从各种数据源获取因子计算所需的数据
 * 支持市场数据、财务数据、宏观数据等
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
public interface DataProviderService {
    
    /**
     * 获取股票基础数据
     * 
     * @param stockCode 股票代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param fields 需要的字段列表
     * @return 数据映射（日期 -> 字段值）
     */
    Map<String, Map<String, Object>> getStockData(String stockCode, String startDate, String endDate, List<String> fields);
    
    /**
     * 获取单日股票数据
     * 
     * @param stockCode 股票代码
     * @param date 日期
     * @param fields 需要的字段列表
     * @return 字段值映射
     */
    Map<String, Object> getSingleDayData(String stockCode, String date, List<String> fields);
    
    /**
     * 获取财务数据
     * 
     * @param stockCode 股票代码
     * @param reportDate 报告期
     * @param fields 需要的字段列表
     * @return 财务数据映射
     */
    Map<String, Object> getFinancialData(String stockCode, String reportDate, List<String> fields);
    
    /**
     * 获取多只股票数据
     * 
     * @param stockCodes 股票代码列表
     * @param date 日期
     * @param fields 需要的字段列表
     * @return 股票数据映射（股票代码 -> 字段值）
     */
    Map<String, Map<String, Object>> getBatchData(List<String> stockCodes, String date, List<String> fields);
    
    /**
     * 检查数据可用性
     * 
     * @param stockCode 股票代码
     * @param date 日期
     * @param fields 需要的字段列表
     * @return 数据可用性报告
     */
    DataAvailabilityReport checkDataAvailability(String stockCode, String date, List<String> fields);
    
    /**
     * 数据可用性报告
     */
    class DataAvailabilityReport {
        private final boolean available;
        private final Map<String, Boolean> fieldAvailability;
        private final String earliestDate;
        private final String latestDate;
        private final String errorMessage;
        
        public DataAvailabilityReport(boolean available, Map<String, Boolean> fieldAvailability, 
                                   String earliestDate, String latestDate, String errorMessage) {
            this.available = available;
            this.fieldAvailability = fieldAvailability;
            this.earliestDate = earliestDate;
            this.latestDate = latestDate;
            this.errorMessage = errorMessage;
        }
        
        public boolean isAvailable() { return available; }
        public Map<String, Boolean> getFieldAvailability() { return fieldAvailability; }
        public String getEarliestDate() { return earliestDate; }
        public String getLatestDate() { return latestDate; }
        public String getErrorMessage() { return errorMessage; }
    }
}