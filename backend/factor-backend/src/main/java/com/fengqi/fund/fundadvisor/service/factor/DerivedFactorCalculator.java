package com.fengqi.fund.fundadvisor.service.factor;

import com.fengqi.fund.fundadvisor.service.factor.dto.FactorCalculationResult;
import java.util.List;
import java.util.Map;

/**
 * 衍生因子计算器接口
 * 
 * 专门负责衍生因子的加权计算
 * 支持多种计算策略和权重配置
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
public interface DerivedFactorCalculator {
    
    /**
     * 计算衍生因子值
     * 
     * @param derivedId 衍生因子ID
     * @param stockCode 股票代码
     * @param calculateDate 计算日期
     * @return 衍生因子计算结果
     */
    FactorCalculationResult calculateDerivedFactor(Integer derivedId, String stockCode, String calculateDate);
    
    /**
     * 批量计算衍生因子值
     * 
     * @param derivedId 衍生因子ID
     * @param stockCodes 股票代码列表
     * @param calculateDate 计算日期
     * @return 批量计算结果
     */
    Map<String, FactorCalculationResult> batchCalculateDerivedFactor(Integer derivedId, List<String> stockCodes, String calculateDate);
    
    /**
     * 使用自定义权重计算衍生因子
     * 
     * @param baseFactorIds 基础因子ID列表
     * @param weights 权重映射（基础因子ID -> 权重）
     * @param stockCode 股票代码
     * @param calculateDate 计算日期
     * @param calcStrategyId 计算策略ID
     * @return 计算结果
     */
    FactorCalculationResult calculateWithCustomWeights(List<Integer> baseFactorIds, 
                                                       Map<Integer, Double> weights,
                                                       String stockCode, 
                                                       String calculateDate,
                                                       Integer calcStrategyId);
    
    /**
     * 使用等权策略计算衍生因子
     * 
     * @param baseFactorIds 基础因子ID列表
     * @param stockCode 股票代码
     * @param calculateDate 计算日期
     * @return 计算结果
     */
    FactorCalculationResult calculateWithEqualWeights(List<Integer> baseFactorIds,
                                                     String stockCode,
                                                     String calculateDate);
    
    /**
     * 获取衍生因子配置
     * 
     * @param derivedId 衍生因子ID
     * @return 衍生因子配置信息
     */
    DerivedFactorConfig getDerivedFactorConfig(Integer derivedId);
    
    /**
     * 验证权重配置
     * 
     * @param baseFactorIds 基础因子ID列表
     * @param weights 权重映射
     * @return 验证结果
     */
    WeightValidationResult validateWeights(List<Integer> baseFactorIds, Map<Integer, Double> weights);
    
    /**
     * 衍生因子配置
     */
    class DerivedFactorConfig {
        private final Integer derivedId;
        private final String factorName;
        private final String factorCode;
        private final List<Integer> baseFactorIds;
        private final Map<Integer, Double> defaultWeights;
        private final Integer calcStrategyId;
        private final String calcStrategyName;
        private final boolean isValid;
        
        public DerivedFactorConfig(Integer derivedId, String factorName, String factorCode,
                                 List<Integer> baseFactorIds, Map<Integer, Double> defaultWeights,
                                 Integer calcStrategyId, String calcStrategyName, boolean isValid) {
            this.derivedId = derivedId;
            this.factorName = factorName;
            this.factorCode = factorCode;
            this.baseFactorIds = baseFactorIds;
            this.defaultWeights = defaultWeights;
            this.calcStrategyId = calcStrategyId;
            this.calcStrategyName = calcStrategyName;
            this.isValid = isValid;
        }
        
        public Integer getDerivedId() { return derivedId; }
        public String getFactorName() { return factorName; }
        public String getFactorCode() { return factorCode; }
        public List<Integer> getBaseFactorIds() { return baseFactorIds; }
        public Map<Integer, Double> getDefaultWeights() { return defaultWeights; }
        public Integer getCalcStrategyId() { return calcStrategyId; }
        public String getCalcStrategyName() { return calcStrategyName; }
        public boolean isValid() { return isValid; }
    }
    
    /**
     * 权重验证结果
     */
    class WeightValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final Map<Integer, Double> normalizedWeights;
        private final double totalWeight;
        
        public WeightValidationResult(boolean valid, String errorMessage, 
                                     Map<Integer, Double> normalizedWeights, double totalWeight) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.normalizedWeights = normalizedWeights;
            this.totalWeight = totalWeight;
        }
        
        public static WeightValidationResult valid(Map<Integer, Double> normalizedWeights, double totalWeight) {
            return new WeightValidationResult(true, null, normalizedWeights, totalWeight);
        }
        
        public static WeightValidationResult invalid(String errorMessage) {
            return new WeightValidationResult(false, errorMessage, null, 0.0);
        }
        
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public Map<Integer, Double> getNormalizedWeights() { return normalizedWeights; }
        public double getTotalWeight() { return totalWeight; }
    }
}