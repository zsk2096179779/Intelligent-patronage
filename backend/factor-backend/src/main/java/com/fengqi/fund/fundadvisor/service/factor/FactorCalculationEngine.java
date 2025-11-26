package com.fengqi.fund.fundadvisor.service.factor;

import com.fengqi.fund.fundadvisor.service.factor.dto.FactorCalculationContext;
import com.fengqi.fund.fundadvisor.service.factor.dto.FactorCalculationResult;
import com.fengqi.fund.fundadvisor.service.factor.dto.FormulaValidationResult;

import java.util.List;
import java.util.Map;

/**
 * 因子计算引擎接口
 * 
 * 提供基础因子和衍生因子的计算功能
 * 支持标准公式解析和执行
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
public interface FactorCalculationEngine {
    
    /**
     * 解析因子公式
     * 
     * @param formula 标准格式的因子公式
     * @return 解析后的计算上下文
     */
    FactorCalculationContext parseFormula(String formula);
    
    /**
     * 计算基础因子值
     * 
     * @param baseId 基础因子ID
     * @param stockCode 股票代码
     * @param calculateDate 计算日期
     * @return 因子计算结果
     */
    FactorCalculationResult calculateBaseFactor(Integer baseId, String stockCode, String calculateDate);
    
    /**
     * 批量计算基础因子值
     * 
     * @param baseId 基础因子ID
     * @param stockCodes 股票代码列表
     * @param calculateDate 计算日期
     * @return 批量因子计算结果
     */
    Map<String, FactorCalculationResult> batchCalculateBaseFactor(Integer baseId, List<String> stockCodes, String calculateDate);
    
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
     * 验证公式语法
     * 
     * @param formula 因子公式
     * @return 验证结果
     */
    FormulaValidationResult validateFormula(String formula);
    
    /**
     * 预览因子计算结果
     * 
     * @param formula 因子公式
     * @param stockCodes 预览股票代码列表
     * @param calculateDate 计算日期
     * @return 预览计算结果
     */
    Map<String, FactorCalculationResult> previewCalculation(String formula, List<String> stockCodes, String calculateDate);
}