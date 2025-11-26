package com.fengqi.fund.fundadvisor.service.factor;

import com.fengqi.fund.fundadvisor.service.factor.dto.FactorCalculationContext;
import com.fengqi.fund.fundadvisor.service.factor.dto.FactorCalculationResult;
import com.fengqi.fund.fundadvisor.service.factor.dto.FormulaValidationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 因子计算引擎测试类
 * 
 * 测试因子计算引擎的各项功能
 * 包括公式解析、计算、验证等
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@SpringBootTest
@ActiveProfiles("test")
class FactorCalculationEngineTest {
    
    @Autowired
    private FactorCalculationEngine factorCalculationEngine;
    
    @Test
    void testParseValidFormula() {
        // 测试解析有效公式
        String formula = "PE_TTM: (CLOSE_PRICE / EPS_TTM) : CLOSE_PRICE, EPS_TTM : EPS_TTM > 0 : LAST";
        
        FactorCalculationContext context = factorCalculationEngine.parseFormula(formula);
        
        assertNotNull(context);
        assertTrue(context.getIsValid());
        assertEquals("PE_TTM", context.getFactorName());
        assertEquals("(CLOSE_PRICE / EPS_TTM)", context.getExpression());
        assertEquals("LAST", context.getAggregationMethod());
        assertEquals(2, context.getDataFields().size());
        assertTrue(context.getDataFields().contains("CLOSE_PRICE"));
        assertTrue(context.getDataFields().contains("EPS_TTM"));
    }
    
    @Test
    void testParseInvalidFormula() {
        // 测试解析无效公式
        String invalidFormula = "INVALID_FORMULA";
        
        FactorCalculationContext context = factorCalculationEngine.parseFormula(invalidFormula);
        
        assertNotNull(context);
        assertFalse(context.getIsValid());
        assertNotNull(context.getErrorMessage());
    }
    
    @Test
    void testValidateValidFormula() {
        // 测试验证有效公式
        String formula = "PB: (CLOSE_PRICE / BVPS) : CLOSE_PRICE, BVPS : BVPS > 0 : LAST";
        
        FormulaValidationResult result = factorCalculationEngine.validateFormula(formula);
        
        assertNotNull(result);
        assertTrue(result.getIsValid());
        assertTrue(result.getErrors().isEmpty());
    }
    
    @Test
    void testValidateInvalidFormula() {
        // 测试验证无效公式
        String invalidFormula = "INVALID: INVALID_EXPRESSION : INVALID_FIELD : INVALID_CONDITION";
        
        FormulaValidationResult result = factorCalculationEngine.validateFormula(invalidFormula);
        
        assertNotNull(result);
        assertFalse(result.getIsValid());
        assertFalse(result.getErrors().isEmpty());
    }
    
    @Test
    void testCalculateBaseFactor() {
        // 测试计算基础因子
        Integer baseId = 1; // 假设存在ID为1的基础因子
        String stockCode = "000001";
        String calculateDate = "2024-01-15";
        
        FactorCalculationResult result = factorCalculationEngine.calculateBaseFactor(baseId, stockCode, calculateDate);
        
        assertNotNull(result);
        assertEquals(baseId, result.getFactorId());
        assertEquals(stockCode, result.getStockCode());
        assertEquals(calculateDate, result.getCalculateDate());
        assertNotNull(result.getFactorValue());
        assertNotNull(result.getStatus());
    }
    
    @Test
    void testBatchCalculateBaseFactor() {
        // 测试批量计算基础因子
        Integer baseId = 1;
        List<String> stockCodes = Arrays.asList("000001", "000002", "000858");
        String calculateDate = "2024-01-15";
        
        Map<String, FactorCalculationResult> results = factorCalculationEngine.batchCalculateBaseFactor(
            baseId, stockCodes, calculateDate);
        
        assertNotNull(results);
        assertEquals(stockCodes.size(), results.size());
        
        for (String stockCode : stockCodes) {
            assertTrue(results.containsKey(stockCode));
            FactorCalculationResult result = results.get(stockCode);
            assertNotNull(result);
            assertEquals(baseId, result.getFactorId());
            assertEquals(stockCode, result.getStockCode());
            assertEquals(calculateDate, result.getCalculateDate());
        }
    }
    
    @Test
    void testPreviewCalculation() {
        // 测试预览计算
        String formula = "PE_TTM: (CLOSE_PRICE / EPS_TTM) : CLOSE_PRICE, EPS_TTM : EPS_TTM > 0 : LAST";
        List<String> stockCodes = Arrays.asList("000001", "000002");
        String calculateDate = "2024-01-15";
        
        Map<String, FactorCalculationResult> results = factorCalculationEngine.previewCalculation(
            formula, stockCodes, calculateDate);
        
        assertNotNull(results);
        assertEquals(stockCodes.size(), results.size());
        
        for (String stockCode : stockCodes) {
            assertTrue(results.containsKey(stockCode));
            FactorCalculationResult result = results.get(stockCode);
            assertNotNull(result);
            assertEquals(stockCode, result.getStockCode());
            assertEquals(calculateDate, result.getCalculateDate());
            assertEquals(formula, result.getFormula());
        }
    }
    
    @Test
    void testCalculateDerivedFactor() {
        // 测试计算衍生因子
        Integer derivedId = 1; // 假设存在ID为1的衍生因子
        String stockCode = "000001";
        String calculateDate = "2024-01-15";
        
        FactorCalculationResult result = factorCalculationEngine.calculateDerivedFactor(derivedId, stockCode, calculateDate);
        
        assertNotNull(result);
        assertEquals(derivedId, result.getFactorId());
        assertEquals(stockCode, result.getStockCode());
        assertEquals(calculateDate, result.getCalculateDate());
        assertNotNull(result.getStatus());
    }
}