package com.example.backend.service;

import com.example.backend.dto.FundFactorData;
import com.example.backend.entity.StrategyFactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基金因子分析服务
 * 读取CSV文件，计算因子得分，选出topN基金
 */
@Service
public class FundFactorAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(FundFactorAnalysisService.class);
    private static final String CSV_FILE_NAME = "fund_factors_result.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 读取CSV文件并解析为基金因子数据列表
     * @return 基金因子数据列表
     */
    public List<FundFactorData> loadFundFactorsFromCsv() {
        List<FundFactorData> fundList = new ArrayList<>();
        
        try {
            // 获取当前工作目录
            String currentDir = System.getProperty("user.dir");
            Path csvPath = null;
            
            // 策略：从当前目录向上查找，直到找到CSV文件或到达根目录
            Path currentPath = Paths.get(currentDir);
            
            // 最多向上查找5级目录
            for (int i = 0; i < 5; i++) {
                Path testPath = currentPath.resolve(CSV_FILE_NAME);
                if (testPath.toFile().exists()) {
                    csvPath = testPath;
                    logger.info("找到CSV文件: {}", csvPath);
                    break;
                }
                
                // 向上查找父目录
                Path parent = currentPath.getParent();
                if (parent == null) {
                    break;
                }
                currentPath = parent;
            }
            
            if (csvPath == null || !csvPath.toFile().exists()) {
                logger.error("CSV文件不存在，已从以下目录向上查找: {}", currentDir);
                logger.error("请确保文件 {} 存在于项目根目录", CSV_FILE_NAME);
                return fundList;
            }
            
            logger.info("读取CSV文件: {}", csvPath);
            
            try (BufferedReader reader = new BufferedReader(new FileReader(csvPath.toFile()))) {
                String line;
                boolean isFirstLine = true;
                
                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue; // 跳过表头
                    }
                    
                    FundFactorData fund = parseCsvLine(line);
                    if (fund != null) {
                        fundList.add(fund);
                    }
                }
            }
            
            logger.info("成功读取 {} 条基金数据", fundList.size());
            
        } catch (IOException e) {
            logger.error("读取CSV文件失败", e);
        }
        
        return fundList;
    }
    
    /**
     * 解析CSV行数据
     */
    private FundFactorData parseCsvLine(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 11) {
                return null;
            }
            
            FundFactorData fund = new FundFactorData();
            fund.setCode(parts[0].trim());
            fund.setName(parts[1].trim());
            
            // 解析日期
            try {
                fund.setUpdateDate(LocalDate.parse(parts[2].trim(), DATE_FORMATTER));
            } catch (Exception e) {
                logger.debug("日期解析失败: {}", parts[2]);
            }
            
            // 解析数值字段
            fund.setNav(parseBigDecimal(parts[3]));
            fund.setNavAcc(parseBigDecimal(parts[4]));
            fund.setDividendRatio(parseBigDecimal(parts[5]));
            fund.setVolatility20(parseBigDecimal(parts[6]));
            fund.setMomentum20(parseBigDecimal(parts[7]));
            fund.setDrawdown(parseBigDecimal(parts[8]));
            fund.setSharpe20(parseBigDecimal(parts[9]));
            fund.setMeanReversion(parseBigDecimal(parts[10]));
            
            return fund;
            
        } catch (Exception e) {
            logger.debug("解析CSV行失败: {}", line, e);
            return null;
        }
    }
    
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 标准化因子值（Z-score标准化）
     */
    private BigDecimal normalizeValue(BigDecimal value, BigDecimal mean, BigDecimal std) {
        if (value == null || mean == null || std == null || std.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return value.subtract(mean).divide(std, 6, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算统计量（均值和标准差）
     */
    private Map<String, Map<String, BigDecimal>> calculateStatistics(List<FundFactorData> funds, List<StrategyFactor> strategyFactors) {
        Map<String, List<BigDecimal>> factorValues = new HashMap<>();
        
        // 收集每个因子的所有值
        for (FundFactorData fund : funds) {
            for (StrategyFactor factor : strategyFactors) {
                BigDecimal value = fund.getFactorValue(factor.getFactorName());
                if (value != null) {
                    factorValues.computeIfAbsent(factor.getFactorName(), k -> new ArrayList<>()).add(value);
                }
            }
        }
        
        // 计算均值和标准差
        Map<String, Map<String, BigDecimal>> stats = new HashMap<>();
        for (Map.Entry<String, List<BigDecimal>> entry : factorValues.entrySet()) {
            String factorName = entry.getKey();
            List<BigDecimal> values = entry.getValue();
            
            if (values.isEmpty()) {
                continue;
            }
            
            // 计算均值
            BigDecimal sum = values.stream()
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal mean = sum.divide(BigDecimal.valueOf(values.size()), 6, RoundingMode.HALF_UP);
            
            // 计算标准差
            BigDecimal variance = values.stream()
                    .filter(Objects::nonNull)
                    .map(v -> v.subtract(mean).pow(2))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(values.size()), 6, RoundingMode.HALF_UP);
            BigDecimal std = new BigDecimal(Math.sqrt(variance.doubleValue()));
            
            Map<String, BigDecimal> factorStats = new HashMap<>();
            factorStats.put("mean", mean);
            factorStats.put("std", std);
            stats.put(factorName, factorStats);
        }
        
        return stats;
    }
    
    /**
     * 根据策略因子配置计算基金综合得分并选出topN
     * @param strategyFactors 策略因子配置（包含因子名称和权重）
     * @param topN 选出前N只基金
     * @return topN基金列表，按得分降序排列
     */
    public List<FundFactorData> calculateTopNFunds(List<StrategyFactor> strategyFactors, int topN) {
        if (strategyFactors == null || strategyFactors.isEmpty()) {
            logger.warn("策略因子配置为空");
            return new ArrayList<>();
        }
        
        // 加载所有基金数据
        List<FundFactorData> allFunds = loadFundFactorsFromCsv();
        if (allFunds.isEmpty()) {
            logger.warn("CSV文件中没有基金数据");
            return new ArrayList<>();
        }
        
        // 计算统计量（用于标准化）
        Map<String, Map<String, BigDecimal>> statistics = calculateStatistics(allFunds, strategyFactors);
        
        // 对每个基金计算综合得分
        for (FundFactorData fund : allFunds) {
            BigDecimal compositeScore = BigDecimal.ZERO;
            BigDecimal totalWeight = BigDecimal.ZERO;
            
            for (StrategyFactor factor : strategyFactors) {
                BigDecimal weight = factor.getWeight();
                if (weight == null || weight.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                
                BigDecimal factorValue = fund.getFactorValue(factor.getFactorName());
                if (factorValue == null) {
                    continue;
                }
                
                // 标准化因子值
                Map<String, BigDecimal> factorStats = statistics.get(factor.getFactorName());
                if (factorStats != null) {
                    BigDecimal normalizedValue = normalizeValue(
                            factorValue,
                            factorStats.get("mean"),
                            factorStats.get("std")
                    );
                    
                    // 加权累加
                    compositeScore = compositeScore.add(normalizedValue.multiply(weight));
                    totalWeight = totalWeight.add(weight);
                }
            }
            
            // 归一化得分（除以总权重）
            if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
                compositeScore = compositeScore.divide(totalWeight, 6, RoundingMode.HALF_UP);
            }
            
            fund.setCompositeScore(compositeScore);
        }
        
        // 按得分降序排序，选出topN
        return allFunds.stream()
                .filter(f -> f.getCompositeScore() != null)
                .sorted((f1, f2) -> f2.getCompositeScore().compareTo(f1.getCompositeScore()))
                .limit(topN)
                .collect(Collectors.toList());
    }
}

