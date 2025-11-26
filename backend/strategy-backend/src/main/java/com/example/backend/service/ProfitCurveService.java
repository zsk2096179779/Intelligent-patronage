package com.example.backend.service;

import com.example.backend.dto.FundFactorData;
import com.example.backend.entity.StrategyFactor;
import com.example.backend.entity.StrategyFilterRule;
import com.example.backend.entity.StrategyProfitCurvePoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收益曲线生成服务
 * 基于策略因子配置和CSV数据生成历史回测曲线
 */
@Service
public class ProfitCurveService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfitCurveService.class);
    
    private final FundFactorAnalysisService factorAnalysisService;
    
    public ProfitCurveService(FundFactorAnalysisService factorAnalysisService) {
        this.factorAnalysisService = factorAnalysisService;
    }
    
    /**
     * 生成策略收益曲线
     * 基于topN基金的累计净值，生成组合净值曲线
     * 
     * @param strategyFactors 策略因子配置
     * @param filterRule 筛选规则（包含topN）
     * @param startDate 起始日期（如果为null，使用90天前）
     * @return 收益曲线数据点列表
     */
    public List<StrategyProfitCurvePoint> generateProfitCurve(
            List<StrategyFactor> strategyFactors,
            StrategyFilterRule filterRule,
            LocalDate startDate) {
        
        if (strategyFactors == null || strategyFactors.isEmpty()) {
            logger.warn("策略因子配置为空，无法生成收益曲线");
            return new ArrayList<>();
        }
        
        int topN = filterRule != null && filterRule.getTopN() != null 
                ? filterRule.getTopN() 
                : 10;
        
        // 获取topN基金
        List<FundFactorData> topNFunds = factorAnalysisService.calculateTopNFunds(strategyFactors, topN);
        
        if (topNFunds.isEmpty()) {
            logger.warn("没有计算出topN基金，无法生成收益曲线");
            return new ArrayList<>();
        }
        
        // 如果没有指定起始日期，使用90天前
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(90);
        }
        
        // 计算策略组合的加权平均累计净值
        BigDecimal totalScore = topNFunds.stream()
                .map(fund -> fund.getCompositeScore() != null && fund.getCompositeScore().compareTo(BigDecimal.ZERO) > 0
                        ? fund.getCompositeScore()
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 如果总分为0，使用等权重
        boolean useEqualWeight = totalScore.compareTo(BigDecimal.ZERO) == 0;
        
        // 计算每个基金的权重
        List<BigDecimal> weights = new ArrayList<>();
        if (useEqualWeight) {
            BigDecimal equalWeight = BigDecimal.ONE.divide(BigDecimal.valueOf(topN), 6, RoundingMode.HALF_UP);
            for (int i = 0; i < topN; i++) {
                weights.add(equalWeight);
            }
        } else {
            for (FundFactorData fund : topNFunds) {
                BigDecimal score = fund.getCompositeScore() != null && fund.getCompositeScore().compareTo(BigDecimal.ZERO) > 0
                        ? fund.getCompositeScore()
                        : BigDecimal.ZERO;
                BigDecimal weight = score.divide(totalScore, 6, RoundingMode.HALF_UP);
                weights.add(weight);
            }
        }
        
        // 计算组合的初始累计净值（加权平均）
        BigDecimal initialNavAcc = BigDecimal.ZERO;
        for (int i = 0; i < topNFunds.size(); i++) {
            FundFactorData fund = topNFunds.get(i);
            BigDecimal weight = weights.get(i);
            if (fund.getNavAcc() != null) {
                initialNavAcc = initialNavAcc.add(fund.getNavAcc().multiply(weight));
            }
        }
        
        // 生成收益曲线数据点
        List<StrategyProfitCurvePoint> curvePoints = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        
        // 生成每日数据点（简化：假设线性增长）
        // 实际应该基于历史净值数据，但CSV只有最新数据，所以使用简化模型
        for (long i = 0; i <= days; i += 1) {  // 每天一个点
            LocalDate currentDate = startDate.plusDays(i);
            
            // 简化模型：假设组合净值从1.0线性增长到当前加权平均累计净值
            // 实际应用中应该使用历史净值数据
            BigDecimal progress = days > 0 
                    ? BigDecimal.valueOf(i).divide(BigDecimal.valueOf(days), 6, RoundingMode.HALF_UP)
                    : BigDecimal.ONE;
            
            // 计算当前日期的净值
            // 从1.0增长到initialNavAcc
            BigDecimal currentNav = BigDecimal.ONE.add(
                    initialNavAcc.subtract(BigDecimal.ONE).multiply(progress)
            );
            
            StrategyProfitCurvePoint point = new StrategyProfitCurvePoint();
            point.setPointDate(currentDate);
            point.setNetValue(currentNav.setScale(4, RoundingMode.HALF_UP));
            
            curvePoints.add(point);
        }
        
        logger.info("生成收益曲线: {} 个数据点，从 {} 到 {}，初始净值: {}，最终净值: {}", 
                curvePoints.size(), startDate, endDate, 
                BigDecimal.ONE, 
                curvePoints.isEmpty() ? BigDecimal.ZERO : curvePoints.get(curvePoints.size() - 1).getNetValue());
        
        return curvePoints;
    }
    
    /**
     * 基于topN基金的当前累计净值，生成简化的收益曲线
     * 使用指数增长模型模拟历史净值变化
     */
    public List<StrategyProfitCurvePoint> generateProfitCurveWithExponentialModel(
            List<StrategyFactor> strategyFactors,
            StrategyFilterRule filterRule,
            LocalDate startDate) {
        
        if (strategyFactors == null || strategyFactors.isEmpty()) {
            return new ArrayList<>();
        }
        
        int topN = filterRule != null && filterRule.getTopN() != null 
                ? filterRule.getTopN() 
                : 10;
        
        List<FundFactorData> topNFunds = factorAnalysisService.calculateTopNFunds(strategyFactors, topN);
        
        if (topNFunds.isEmpty()) {
            return new ArrayList<>();
        }
        
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(90);
        }
        
        // 计算组合的加权平均累计净值
        BigDecimal totalScore = topNFunds.stream()
                .map(fund -> fund.getCompositeScore() != null && fund.getCompositeScore().compareTo(BigDecimal.ZERO) > 0
                        ? fund.getCompositeScore()
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<BigDecimal> weights = new ArrayList<>();
        if (totalScore.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal equalWeight = BigDecimal.ONE.divide(BigDecimal.valueOf(topN), 6, RoundingMode.HALF_UP);
            for (int i = 0; i < topN; i++) {
                weights.add(equalWeight);
            }
        } else {
            for (FundFactorData fund : topNFunds) {
                BigDecimal score = fund.getCompositeScore() != null && fund.getCompositeScore().compareTo(BigDecimal.ZERO) > 0
                        ? fund.getCompositeScore()
                        : BigDecimal.ZERO;
                weights.add(score.divide(totalScore, 6, RoundingMode.HALF_UP));
            }
        }
        
        BigDecimal finalNavAcc = BigDecimal.ZERO;
        for (int i = 0; i < topNFunds.size(); i++) {
            FundFactorData fund = topNFunds.get(i);
            BigDecimal weight = weights.get(i);
            if (fund.getNavAcc() != null) {
                finalNavAcc = finalNavAcc.add(fund.getNavAcc().multiply(weight));
            }
        }
        
        // 使用指数增长模型：nav(t) = 1.0 * (finalNavAcc / 1.0)^(t/T)
        List<StrategyProfitCurvePoint> curvePoints = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        
        if (totalDays <= 0) {
            totalDays = 1;
        }
        
        for (long i = 0; i <= totalDays; i += 1) {
            LocalDate currentDate = startDate.plusDays(i);
            
            // 指数增长模型
            double progress = (double) i / totalDays;
            double exponent = Math.pow(finalNavAcc.doubleValue(), progress);
            BigDecimal currentNav = BigDecimal.valueOf(exponent).setScale(4, RoundingMode.HALF_UP);
            
            StrategyProfitCurvePoint point = new StrategyProfitCurvePoint();
            point.setPointDate(currentDate);
            point.setNetValue(currentNav);
            
            curvePoints.add(point);
        }
        
        return curvePoints;
    }
}

