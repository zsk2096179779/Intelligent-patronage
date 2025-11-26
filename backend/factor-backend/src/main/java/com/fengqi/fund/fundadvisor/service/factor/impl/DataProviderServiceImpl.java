package com.fengqi.fund.fundadvisor.service.factor.impl;

import com.fengqi.fund.fundadvisor.service.factor.DataProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 数据提供服务实现类
 * 
 * 模拟数据获取服务
 * 实际项目中应该连接真实的数据源（Wind、Tushare等）
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Slf4j
@Service
public class DataProviderServiceImpl implements DataProviderService {
    
    // 模拟数据存储
    private final Map<String, Map<String, Object>> mockDataStore = new HashMap<>();
    
    public DataProviderServiceImpl() {
        initializeMockData();
    }
    
    @Override
    public Map<String, Map<String, Object>> getStockData(String stockCode, String startDate, String endDate, List<String> fields) {
        log.debug("获取股票数据，stockCode: {}, startDate: {}, endDate: {}, fields: {}", 
                 stockCode, startDate, endDate, fields);
        
        Map<String, Map<String, Object>> result = new HashMap<>();
        
        // 生成时间序列数据（模拟）
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            Map<String, Object> dailyData = generateRandomData(fields);
            result.put(date.toString(), dailyData);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getSingleDayData(String stockCode, String date, List<String> fields) {
        log.debug("获取单日股票数据，stockCode: {}, date: {}, fields: {}", stockCode, date, fields);
        
        return generateRandomData(fields);
    }
    
    @Override
    public Map<String, Object> getFinancialData(String stockCode, String reportDate, List<String> fields) {
        log.debug("获取财务数据，stockCode: {}, reportDate: {}, fields: {}", stockCode, reportDate, fields);
        
        return generateRandomFinancialData(fields);
    }
    
    @Override
    public Map<String, Map<String, Object>> getBatchData(List<String> stockCodes, String date, List<String> fields) {
        log.debug("获取批量股票数据，stockCount: {}, date: {}, fields: {}", stockCodes.size(), date, fields);
        
        Map<String, Map<String, Object>> result = new HashMap<>();
        
        for (String stockCode : stockCodes) {
            Map<String, Object> stockData = generateRandomData(fields);
            result.put(stockCode, stockData);
        }
        
        return result;
    }
    
    @Override
    public DataAvailabilityReport checkDataAvailability(String stockCode, String date, List<String> fields) {
        log.debug("检查数据可用性，stockCode: {}, date: {}, fields: {}", stockCode, date, fields);
        
        Map<String, Boolean> fieldAvailability = new HashMap<>();
        boolean allAvailable = true;
        
        for (String field : fields) {
            boolean available = isFieldAvailable(field);
            fieldAvailability.put(field, available);
            if (!available) {
                allAvailable = false;
            }
        }
        
        return new DataAvailabilityReport(
            allAvailable,
            fieldAvailability,
            "2020-01-01", // 模拟最早日期
            LocalDate.now().toString(), // 模拟最新日期
            allAvailable ? null : "部分数据不可用"
        );
    }
    
    /**
     * 生成随机数据
     */
    private Map<String, Object> generateRandomData(List<String> fields) {
        Map<String, Object> data = new HashMap<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        for (String field : fields) {
            data.put(field, generateFieldValue(field, random));
        }
        
        return data;
    }
    
    /**
     * 生成随机财务数据
     */
    private Map<String, Object> generateRandomFinancialData(List<String> fields) {
        Map<String, Object> data = new HashMap<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        for (String field : fields) {
            data.put(field, generateFinancialFieldValue(field, random));
        }
        
        return data;
    }
    
    /**
     * 根据字段名生成对应的随机值
     */
    private Object generateFieldValue(String field, ThreadLocalRandom random) {
        switch (field) {
            // 价格数据
            case "CLOSE_PRICE":
                return 10.0 + random.nextDouble() * 90.0; // 10-100
            case "OPEN_PRICE":
                return 9.5 + random.nextDouble() * 90.0; // 9.5-99.5
            case "HIGH_PRICE":
                return 11.0 + random.nextDouble() * 95.0; // 11-106
            case "LOW_PRICE":
                return 9.0 + random.nextDouble() * 85.0; // 9-94
                
            // 历史价格数据
            case "CLOSE_PRICE_20D":
                return 8.0 + random.nextDouble() * 80.0; // 8-88
            case "CLOSE_PRICE_60D":
                return 6.0 + random.nextDouble() * 70.0; // 6-76
            case "CLOSE_PRICE_250D":
                return 5.0 + random.nextDouble() * 60.0; // 5-65
                
            // 财务数据
            case "EPS_TTM":
                return 0.5 + random.nextDouble() * 5.0; // 0.5-5.5
            case "BVPS":
                return 5.0 + random.nextDouble() * 25.0; // 5-30
            case "RPS":
                return 10.0 + random.nextDouble() * 100.0; // 10-110
            case "DPS":
                return 0.1 + random.nextDouble() * 2.0; // 0.1-2.1
                
            // 财务报表数据
            case "REVENUE_CURRENT":
                return 1000000000.0 + random.nextDouble() * 9000000000.0; // 10亿-100亿
            case "REVENUE_LAG1":
                return 800000000.0 + random.nextDouble() * 7200000000.0; // 8亿-80亿
            case "NET_PROFIT_CURRENT":
                return 50000000.0 + random.nextDouble() * 450000000.0; // 5000万-5亿
            case "NET_PROFIT_LAG1":
                return 40000000.0 + random.nextDouble() * 360000000.0; // 4000万-4亿
            case "NET_ASSETS":
                return 2000000000.0 + random.nextDouble() * 18000000000.0; // 20亿-200亿
            case "TOTAL_ASSETS":
                return 5000000000.0 + random.nextDouble() * 45000000000.0; // 50亿-500亿
                
            // 计算后的数据
            case "ROE":
                return 0.05 + random.nextDouble() * 0.20; // 5%-25%
            case "ROA":
                return 0.02 + random.nextDouble() * 0.10; // 2%-12%
            case "GROSS_MARGIN":
                return 0.10 + random.nextDouble() * 0.40; // 10%-50%
            case "ROE_RANK":
                return random.nextInt(1000) + 1; // 1-1000
            case "ROA_RANK":
                return random.nextInt(1000) + 1; // 1-1000
            case "GROSS_MARGIN_RANK":
                return random.nextInt(1000) + 1; // 1-1000
                
            // 收益率数据
            case "DAILY_RETURN_20D":
                return generateRandomReturns(20, random);
            case "PRICE_SERIES_20D":
                return generateRandomPriceSeries(20, random);
                
            default:
                return random.nextDouble() * 100.0; // 默认值
        }
    }
    
    /**
     * 生成财务字段值
     */
    private Object generateFinancialFieldValue(String field, ThreadLocalRandom random) {
        switch (field) {
            case "TOTAL_REVENUE":
                return 1000000000.0 + random.nextDouble() * 9000000000.0; // 10亿-100亿
            case "NET_PROFIT":
                return 50000000.0 + random.nextDouble() * 450000000.0; // 5000万-5亿
            case "TOTAL_ASSETS":
                return 2000000000.0 + random.nextDouble() * 18000000000.0; // 20亿-200亿
            case "SHAREHOLDER_EQUITY":
                return 1000000000.0 + random.nextDouble() * 9000000000.0; // 10亿-100亿
            default:
                return random.nextDouble() * 1000000000.0; // 默认值
        }
    }
    
    /**
     * 生成随机收益率序列
     */
    private List<Double> generateRandomReturns(int days, ThreadLocalRandom random) {
        List<Double> returns = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            returns.add((random.nextDouble() - 0.5) * 0.1); // -5% 到 +5%
        }
        return returns;
    }
    
    /**
     * 生成随机价格序列
     */
    private List<Double> generateRandomPriceSeries(int days, ThreadLocalRandom random) {
        List<Double> prices = new ArrayList<>();
        double basePrice = 50.0 + random.nextDouble() * 50.0; // 基础价格 50-100
        
        for (int i = 0; i < days; i++) {
            double change = (random.nextDouble() - 0.5) * 0.1; // -5% 到 +5%
            basePrice = basePrice * (1 + change);
            prices.add(basePrice);
        }
        
        return prices;
    }
    
    /**
     * 检查字段是否可用
     */
    private boolean isFieldAvailable(String field) {
        // 模拟所有字段都可用
        return true;
    }
    
    /**
     * 初始化模拟数据
     */
    private void initializeMockData() {
        log.info("初始化模拟数据存储");
        // 可以在这里预加载一些常用的模拟数据
    }
}