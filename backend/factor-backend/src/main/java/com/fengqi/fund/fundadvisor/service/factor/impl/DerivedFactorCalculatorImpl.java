package com.fengqi.fund.fundadvisor.service.factor.impl;

import com.fengqi.fund.fundadvisor.entity.factor.DerivedFactor;
import com.fengqi.fund.fundadvisor.mapper.factor.DerivedFactorMapper;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorBaseMapper;
import com.fengqi.fund.fundadvisor.service.factor.DerivedFactorCalculator;
import com.fengqi.fund.fundadvisor.service.factor.FactorCalculationEngine;
import com.fengqi.fund.fundadvisor.service.factor.dto.FactorCalculationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 衍生因子计算器实现类
 * 
 * 负责衍生因子的加权计算
 * 支持多种计算策略和权重配置
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DerivedFactorCalculatorImpl implements DerivedFactorCalculator {
    
    private final DerivedFactorMapper derivedFactorMapper;
    private final FactorBaseMapper factorBaseMapper;
    private final FactorCalculationEngine factorCalculationEngine;
    
    // 计算策略映射
    private final Map<Integer, String> calcStrategies = new HashMap<>();
    
    {
        // 初始化内置计算策略
        calcStrategies.put(1, "归一化处理后的加权组合");
        calcStrategies.put(2, "简单等权平均");
        calcStrategies.put(3, "市值加权");
        calcStrategies.put(4, "风险调整加权");
    }
    
    @Override
    public FactorCalculationResult calculateDerivedFactor(Integer derivedId, String stockCode, String calculateDate) {
        log.debug("开始计算衍生因子，derivedId: {}, stockCode: {}, date: {}", derivedId, stockCode, calculateDate);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 获取衍生因子配置
            DerivedFactorConfig config = getDerivedFactorConfig(derivedId);
            if (!config.isValid()) {
                return createErrorResult(derivedId, stockCode, calculateDate,
                    FactorCalculationResult.CalculationStatus.FAILED, "衍生因子配置无效");
            }
            
            // 计算衍生因子值
            FactorCalculationResult result = calculateWithCustomWeights(
                config.getBaseFactorIds(),
                config.getDefaultWeights(),
                stockCode,
                calculateDate,
                config.getCalcStrategyId()
            );
            
            // 设置衍生因子特定信息
            result.setFactorId(derivedId);
            
            long executionTime = System.currentTimeMillis() - startTime;
            result.setExecutionTime(executionTime);
            
            return result;
            
        } catch (Exception e) {
            log.error("衍生因子计算失败", e);
            return createErrorResult(derivedId, stockCode, calculateDate,
                FactorCalculationResult.CalculationStatus.FAILED, e.getMessage());
        }
    }
    
    @Override
    public Map<String, FactorCalculationResult> batchCalculateDerivedFactor(Integer derivedId, List<String> stockCodes, String calculateDate) {
        log.info("批量计算衍生因子，derivedId: {}, stockCount: {}, date: {}", derivedId, stockCodes.size(), calculateDate);
        
        // 获取衍生因子配置
        DerivedFactorConfig config = getDerivedFactorConfig(derivedId);
        if (!config.isValid()) {
            return stockCodes.stream()
                .collect(Collectors.toMap(
                    stockCode -> stockCode,
                    stockCode -> createErrorResult(derivedId, stockCode, calculateDate,
                        FactorCalculationResult.CalculationStatus.FAILED, "衍生因子配置无效")
                ));
        }
        
        // 并行计算
        List<CompletableFuture<Map.Entry<String, FactorCalculationResult>>> futures = stockCodes.stream()
            .map(stockCode -> CompletableFuture.supplyAsync(() -> {
                FactorCalculationResult result = calculateWithCustomWeights(
                    config.getBaseFactorIds(),
                    config.getDefaultWeights(),
                    stockCode,
                    calculateDate,
                    config.getCalcStrategyId()
                );
                result.setFactorId(derivedId);
                return Map.entry(stockCode, result);
            }))
            .collect(Collectors.toList());
        
        // 等待所有计算完成
        return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    @Override
    public FactorCalculationResult calculateWithCustomWeights(List<Integer> baseFactorIds, 
                                                             Map<Integer, Double> weights,
                                                             String stockCode, 
                                                             String calculateDate,
                                                             Integer calcStrategyId) {
        log.debug("使用自定义权重计算衍生因子，baseFactors: {}, weights: {}, strategy: {}", 
                 baseFactorIds, weights, calcStrategyId);
        
        try {
            // 验证权重配置
            WeightValidationResult validation = validateWeights(baseFactorIds, weights);
            if (!validation.isValid()) {
                return createErrorResult(null, stockCode, calculateDate,
                    FactorCalculationResult.CalculationStatus.INVALID_FORMULA, validation.getErrorMessage());
            }
            
            // 计算各基础因子值
            Map<Integer, FactorCalculationResult> baseFactorResults = new HashMap<>();
            for (Integer baseId : baseFactorIds) {
                FactorCalculationResult result = factorCalculationEngine.calculateBaseFactor(baseId, stockCode, calculateDate);
                baseFactorResults.put(baseId, result);
                
                // 检查基础因子计算是否成功
                if (result.getStatus() != FactorCalculationResult.CalculationStatus.SUCCESS) {
                    return createErrorResult(null, stockCode, calculateDate,
                        FactorCalculationResult.CalculationStatus.INSUFFICIENT_DATA, 
                        "基础因子计算失败: " + result.getErrorMessage());
                }
            }
            
            // 根据计算策略计算衍生因子值
            BigDecimal derivedValue = calculateDerivedValue(baseFactorResults, validation.getNormalizedWeights(), calcStrategyId);
            
            // 构建中间结果
            Map<String, Object> intermediateResults = new HashMap<>();
            intermediateResults.put("baseFactorResults", baseFactorResults);
            intermediateResults.put("normalizedWeights", validation.getNormalizedWeights());
            intermediateResults.put("calculationStrategy", calcStrategies.getOrDefault(calcStrategyId, "未知策略"));
            
            return FactorCalculationResult.builder()
                .factorId(null) // 将在调用方设置
                .stockCode(stockCode)
                .calculateDate(calculateDate)
                .factorValue(derivedValue)
                .status(FactorCalculationResult.CalculationStatus.SUCCESS)
                .calculateTime(LocalDateTime.now())
                .intermediateResults(intermediateResults)
                .build();
                
        } catch (Exception e) {
            log.error("自定义权重计算失败", e);
            return createErrorResult(null, stockCode, calculateDate,
                FactorCalculationResult.CalculationStatus.FAILED, e.getMessage());
        }
    }
    
    @Override
    public FactorCalculationResult calculateWithEqualWeights(List<Integer> baseFactorIds,
                                                           String stockCode,
                                                           String calculateDate) {
        log.debug("使用等权策略计算衍生因子，baseFactors: {}", baseFactorIds);
        
        // 构建等权权重
        double equalWeight = 1.0 / baseFactorIds.size();
        Map<Integer, Double> equalWeights = baseFactorIds.stream()
            .collect(Collectors.toMap(id -> id, id -> equalWeight));
        
        // 使用等权权重计算
        return calculateWithCustomWeights(baseFactorIds, equalWeights, stockCode, calculateDate, 2); // 策略2：简单等权平均
    }
    
    @Override
    public DerivedFactorConfig getDerivedFactorConfig(Integer derivedId) {
        try {
            // TODO: 实现从数据库获取衍生因子配置
            // 这里返回模拟配置
            List<Integer> baseFactorIds = Arrays.asList(1, 2, 3); // 模拟基础因子ID
            Map<Integer, Double> defaultWeights = new HashMap<>();
            defaultWeights.put(1, 0.4);
            defaultWeights.put(2, 0.3);
            defaultWeights.put(3, 0.3);
            
            return new DerivedFactorConfig(
                derivedId,
                "模拟衍生因子",
                "SIMULATED_FACTOR",
                baseFactorIds,
                defaultWeights,
                1, // 归一化处理后的加权组合
                calcStrategies.get(1),
                true
            );
            
        } catch (Exception e) {
            log.error("获取衍生因子配置失败，derivedId: {}", derivedId, e);
            return new DerivedFactorConfig(derivedId, null, null, Collections.emptyList(),
                Collections.emptyMap(), null, null, false);
        }
    }
    
    @Override
    public WeightValidationResult validateWeights(List<Integer> baseFactorIds, Map<Integer, Double> weights) {
        try {
            // 检查权重是否为空
            if (CollectionUtils.isEmpty(weights)) {
                return WeightValidationResult.invalid("权重配置不能为空");
            }
            
            // 检查每个基础因子是否有对应权重
            for (Integer baseId : baseFactorIds) {
                if (!weights.containsKey(baseId)) {
                    return WeightValidationResult.invalid("基础因子 " + baseId + " 缺少权重配置");
                }
            }
            
            // 检查权重是否为非负数
            for (Map.Entry<Integer, Double> entry : weights.entrySet()) {
                if (entry.getValue() == null || entry.getValue() < 0) {
                    return WeightValidationResult.invalid("权重值必须为非负数: " + entry.getKey() + " = " + entry.getValue());
                }
            }
            
            // 计算总权重
            double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();
            
            // 检查总权重是否为0
            if (totalWeight <= 0) {
                return WeightValidationResult.invalid("总权重必须大于0");
            }
            
            // 归一化权重
            Map<Integer, Double> normalizedWeights = new HashMap<>();
            for (Map.Entry<Integer, Double> entry : weights.entrySet()) {
                normalizedWeights.put(entry.getKey(), entry.getValue() / totalWeight);
            }
            
            return WeightValidationResult.valid(normalizedWeights, totalWeight);
            
        } catch (Exception e) {
            log.error("权重验证失败", e);
            return WeightValidationResult.invalid("权重验证异常: " + e.getMessage());
        }
    }
    
    /**
     * 根据计算策略计算衍生因子值
     */
    private BigDecimal calculateDerivedValue(Map<Integer, FactorCalculationResult> baseFactorResults,
                                           Map<Integer, Double> normalizedWeights,
                                           Integer calcStrategyId) {
        switch (calcStrategyId) {
            case 1:
                return calculateNormalizedWeightedSum(baseFactorResults, normalizedWeights);
            case 2:
                return calculateEqualWeightedAverage(baseFactorResults);
            case 3:
                return calculateMarketCapWeighted(baseFactorResults, normalizedWeights);
            case 4:
                return calculateRiskAdjustedWeighted(baseFactorResults, normalizedWeights);
            default:
                return calculateNormalizedWeightedSum(baseFactorResults, normalizedWeights);
        }
    }
    
    /**
     * 归一化处理后的加权组合
     */
    private BigDecimal calculateNormalizedWeightedSum(Map<Integer, FactorCalculationResult> baseFactorResults,
                                                    Map<Integer, Double> normalizedWeights) {
        BigDecimal weightedSum = BigDecimal.ZERO;
        
        for (Map.Entry<Integer, FactorCalculationResult> entry : baseFactorResults.entrySet()) {
            Integer baseId = entry.getKey();
            FactorCalculationResult result = entry.getValue();
            Double weight = normalizedWeights.get(baseId);
            
            if (result.getFactorValue() != null && weight != null) {
                BigDecimal weightedValue = result.getFactorValue().multiply(BigDecimal.valueOf(weight));
                weightedSum = weightedSum.add(weightedValue);
            }
        }
        
        return weightedSum.setScale(8, RoundingMode.HALF_UP);
    }
    
    /**
     * 简单等权平均
     */
    private BigDecimal calculateEqualWeightedAverage(Map<Integer, FactorCalculationResult> baseFactorResults) {
        BigDecimal sum = BigDecimal.ZERO;
        int validCount = 0;
        
        for (FactorCalculationResult result : baseFactorResults.values()) {
            if (result.getFactorValue() != null) {
                sum = sum.add(result.getFactorValue());
                validCount++;
            }
        }
        
        if (validCount == 0) {
            return BigDecimal.ZERO;
        }
        
        return sum.divide(BigDecimal.valueOf(validCount), 8, RoundingMode.HALF_UP);
    }
    
    /**
     * 市值加权
     */
    private BigDecimal calculateMarketCapWeighted(Map<Integer, FactorCalculationResult> baseFactorResults,
                                                   Map<Integer, Double> weights) {
        // TODO: 实现市值加权逻辑
        // 这里简化为归一化加权
        return calculateNormalizedWeightedSum(baseFactorResults, weights);
    }
    
    /**
     * 风险调整加权
     */
    private BigDecimal calculateRiskAdjustedWeighted(Map<Integer, FactorCalculationResult> baseFactorResults,
                                                     Map<Integer, Double> weights) {
        // TODO: 实现风险调整加权逻辑
        // 这里简化为归一化加权
        return calculateNormalizedWeightedSum(baseFactorResults, weights);
    }
    
    /**
     * 创建错误结果
     */
    private FactorCalculationResult createErrorResult(Integer factorId, String stockCode, String calculateDate,
                                                     FactorCalculationResult.CalculationStatus status, String errorMessage) {
        return FactorCalculationResult.builder()
            .factorId(factorId)
            .stockCode(stockCode)
            .calculateDate(calculateDate)
            .factorValue(null)
            .status(status)
            .calculateTime(LocalDateTime.now())
            .errorMessage(errorMessage)
            .build();
    }
}