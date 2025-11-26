package com.example.backend.service;

import com.example.backend.dto.FundFactorData;
import com.example.backend.dto.RiskExposureMatrix;
import com.example.backend.entity.StrategyFactor;
import com.example.backend.entity.StrategyFilterRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 风险暴露矩阵计算服务
 * 计算仓位、收益、回撤与基准的差值
 */
@Service
public class RiskExposureMatrixService {
    
    private static final Logger logger = LoggerFactory.getLogger(RiskExposureMatrixService.class);
    
    private final FundFactorAnalysisService factorAnalysisService;
    
    public RiskExposureMatrixService(FundFactorAnalysisService factorAnalysisService) {
        this.factorAnalysisService = factorAnalysisService;
    }
    
    /**
     * 计算风险暴露矩阵
     * @param strategyFactors 策略因子配置
     * @param filterRule 筛选规则（包含topN）
     * @return 风险暴露矩阵
     */
    public RiskExposureMatrix calculateRiskExposureMatrix(
            List<StrategyFactor> strategyFactors,
            StrategyFilterRule filterRule) {
        
        if (strategyFactors == null || strategyFactors.isEmpty()) {
            logger.warn("策略因子配置为空");
            return createEmptyMatrix();
        }
        
        int topN = filterRule != null && filterRule.getTopN() != null 
                ? filterRule.getTopN() 
                : 10; // 默认10只
        
        // 计算topN基金
        List<FundFactorData> topNFunds = factorAnalysisService.calculateTopNFunds(strategyFactors, topN);
        
        if (topNFunds.isEmpty()) {
            logger.warn("没有计算出topN基金");
            return createEmptyMatrix();
        }
        
        // 计算基准值（所有基金的平均值）
        Map<String, BigDecimal> baseline = calculateBaseline();
        
        // 构建基金信息列表（横轴）
        List<RiskExposureMatrix.FundInfo> fundInfos = topNFunds.stream()
                .map(fund -> new RiskExposureMatrix.FundInfo(
                        fund.getCode(),
                        fund.getName(),
                        fund.getCompositeScore()
                ))
                .collect(Collectors.toList());
        
        // 计算基准仓位：等权重分配给topN基金，每个基金 1/topN
        BigDecimal baselinePosition = BigDecimal.ONE.divide(BigDecimal.valueOf(topN), 6, RoundingMode.HALF_UP);
        baseline.put("position", baselinePosition);
        
        // 计算各项指标（纵轴）
        Map<String, List<BigDecimal>> metrics = new HashMap<>();
        
        // 1. 策略仓位（按因子值加权分配）
        // 计算所有topN基金的综合得分总和
        BigDecimal totalScore = topNFunds.stream()
                .map(fund -> fund.getCompositeScore() != null ? fund.getCompositeScore() : BigDecimal.ZERO)
                .filter(score -> score.compareTo(BigDecimal.ZERO) > 0)  // 只考虑正分
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 如果总分为0或负数，使用等权重
        List<BigDecimal> strategyPositions;
        if (totalScore.compareTo(BigDecimal.ZERO) > 0) {
            // 按综合得分加权分配仓位
            strategyPositions = topNFunds.stream()
                    .map(fund -> {
                        BigDecimal score = fund.getCompositeScore() != null && fund.getCompositeScore().compareTo(BigDecimal.ZERO) > 0
                                ? fund.getCompositeScore()
                                : BigDecimal.ZERO;
                        return totalScore.compareTo(BigDecimal.ZERO) > 0
                                ? score.divide(totalScore, 6, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;
                    })
                    .collect(Collectors.toList());
        } else {
            // 如果所有得分都是0或负数，使用等权重
            strategyPositions = topNFunds.stream()
                    .map(f -> baselinePosition)
                    .collect(Collectors.toList());
        }
        
        // 2. 仓位差值（策略仓位 - 基准仓位）
        List<BigDecimal> positionDiffs = strategyPositions.stream()
                .map(position -> position.subtract(baselinePosition))
                .collect(Collectors.toList());
        metrics.put("position", positionDiffs);
        
        // 3. 收益（使用累计净值增长率，与基准的差值）
        BigDecimal baselineReturn = baseline.getOrDefault("return", BigDecimal.ZERO);
        List<BigDecimal> returns = topNFunds.stream()
                .map(fund -> {
                    // 使用累计净值作为收益指标
                    BigDecimal fundReturn = fund.getNavAcc() != null 
                            ? fund.getNavAcc().subtract(BigDecimal.ONE) 
                            : BigDecimal.ZERO;
                    return fundReturn.subtract(baselineReturn);
                })
                .collect(Collectors.toList());
        metrics.put("return", returns);
        
        // 4. 回撤（与基准的差值）
        BigDecimal baselineDrawdown = baseline.getOrDefault("drawdown", BigDecimal.ZERO);
        List<BigDecimal> drawdowns = topNFunds.stream()
                .map(fund -> {
                    BigDecimal fundDrawdown = fund.getDrawdown() != null 
                            ? fund.getDrawdown() 
                            : BigDecimal.ZERO;
                    return fundDrawdown.subtract(baselineDrawdown);
                })
                .collect(Collectors.toList());
        metrics.put("drawdown", drawdowns);
        
        return new RiskExposureMatrix(fundInfos, metrics, baseline);
    }
    
    /**
     * 计算基准值（所有基金的平均值）
     * 注意：仓位基准值在调用处根据topN计算，这里不计算
     */
    private Map<String, BigDecimal> calculateBaseline() {
        // 加载所有基金数据
        List<FundFactorData> allFunds = factorAnalysisService.loadFundFactorsFromCsv();
        
        if (allFunds.isEmpty()) {
            Map<String, BigDecimal> baseline = new HashMap<>();
            baseline.put("return", BigDecimal.ZERO);
            baseline.put("drawdown", BigDecimal.ZERO);
            return baseline;
        }
        
        int totalFunds = allFunds.size();
        
        // 计算平均收益（累计净值增长率）
        BigDecimal avgReturn = allFunds.stream()
                .filter(f -> f.getNavAcc() != null)
                .map(f -> f.getNavAcc().subtract(BigDecimal.ONE))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(totalFunds), 6, RoundingMode.HALF_UP);
        
        // 计算平均回撤
        BigDecimal avgDrawdown = allFunds.stream()
                .filter(f -> f.getDrawdown() != null)
                .map(FundFactorData::getDrawdown)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(totalFunds), 6, RoundingMode.HALF_UP);
        
        Map<String, BigDecimal> baseline = new HashMap<>();
        baseline.put("return", avgReturn);
        baseline.put("drawdown", avgDrawdown);
        
        return baseline;
    }
    
    /**
     * 创建空的风险暴露矩阵
     */
    private RiskExposureMatrix createEmptyMatrix() {
        RiskExposureMatrix matrix = new RiskExposureMatrix();
        matrix.setFunds(new ArrayList<>());
        matrix.setMetrics(new HashMap<>());
        matrix.setBaseline(new HashMap<>());
        return matrix;
    }
}

