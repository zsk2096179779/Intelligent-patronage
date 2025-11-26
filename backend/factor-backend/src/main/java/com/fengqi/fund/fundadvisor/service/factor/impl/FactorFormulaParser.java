package com.fengqi.fund.fundadvisor.service.factor.impl;

import com.fengqi.fund.fundadvisor.service.factor.dto.FactorCalculationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 因子公式解析器
 * 
 * 负责解析标准格式的因子计算公式
 * 支持语法验证和上下文提取
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Slf4j
@Component
public class FactorFormulaParser {
    
    // 标准公式格式正则表达式
    private static final Pattern FORMULA_PATTERN = 
        Pattern.compile("^([^:]+):([^:]*):([^:]*):([^:]*):([^:]*)$");
    
    // 支持的聚合方式
    private static final Set<String> SUPPORTED_AGGREGATIONS = Set.of(
        "LAST", "MEAN", "SUM", "MEDIAN", "STDDEV", "MAX", "MIN"
    );
    
    // 支持的数据字段
    private static final Set<String> SUPPORTED_FIELDS = Set.of(
        // 价格数据
        "CLOSE_PRICE", "OPEN_PRICE", "HIGH_PRICE", "LOW_PRICE",
        // 财务数据
        "EPS_TTM", "BVPS", "RPS", "DPS",
        // 财务报表数据
        "REVENUE_CURRENT", "REVENUE_LAG1", "NET_PROFIT_CURRENT", "NET_PROFIT_LAG1",
        // 时间序列数据
        "CLOSE_PRICE_20D", "CLOSE_PRICE_60D", "CLOSE_PRICE_250D",
        "DAILY_RETURN_20D", "PRICE_SERIES_20D"
    );
    
    /**
     * 解析因子公式
     * 
     * @param formula 标准格式的因子公式
     * @return 解析后的计算上下文
     */
    public FactorCalculationContext parseFormula(String formula) {
        log.debug("开始解析因子公式: {}", formula);
        
        try {
            // 预处理公式：去除多余空格
            String cleanedFormula = formula.trim().replaceAll("\\s+", " ");
            
            // 匹配标准格式
            Matcher matcher = FORMULA_PATTERN.matcher(cleanedFormula);
            if (!matcher.matches()) {
                return FactorCalculationContext.builder()
                    .isValid(false)
                    .errorMessage("公式格式不正确，应为: FACTOR_NAME:表达式:数据字段:过滤条件:聚合方式")
                    .build();
            }
            
            // 提取各部分
            String factorName = matcher.group(1).trim();
            String expression = matcher.group(2).trim();
            String dataFieldsStr = matcher.group(3).trim();
            String filterCondition = matcher.group(4).trim();
            String aggregationMethod = matcher.group(5).trim();
            
            // 验证各部分
            ValidationResult validation = validateFormulaParts(
                factorName, expression, dataFieldsStr, filterCondition, aggregationMethod
            );
            
            if (!validation.isValid()) {
                return FactorCalculationContext.builder()
                    .isValid(false)
                    .errorMessage(validation.getErrorMessage())
                    .build();
            }
            
            // 解析数据字段
            List<String> dataFields = parseDataFields(dataFieldsStr);
            
            // 构建计算上下文
            FactorCalculationContext context = FactorCalculationContext.builder()
                .factorName(factorName)
                .expression(expression)
                .dataFields(dataFields)
                .filterCondition(filterCondition.isEmpty() ? null : filterCondition)
                .aggregationMethod(aggregationMethod.isEmpty() ? "LAST" : aggregationMethod)
                .dataSourceMapping(buildDataSourceMapping(dataFields))
                .isValid(true)
                .build();
            
            log.debug("公式解析成功: {}", context.getFactorName());
            return context;
            
        } catch (Exception e) {
            log.error("公式解析失败: {}", formula, e);
            return FactorCalculationContext.builder()
                .isValid(false)
                .errorMessage("公式解析异常: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * 验证公式各部分
     */
    private ValidationResult validateFormulaParts(String factorName, String expression, 
                                                 String dataFieldsStr, String filterCondition, 
                                                 String aggregationMethod) {
        
        // 验证因子名称
        if (factorName.isEmpty() || !factorName.matches("^[A-Za-z][A-Za-z0-9_]*$")) {
            return ValidationResult.invalid("因子名称格式不正确，应以字母开头，只能包含字母、数字和下划线");
        }
        
        // 验证表达式
        if (expression.isEmpty()) {
            return ValidationResult.invalid("计算表达式不能为空");
        }
        
        // 基本表达式语法检查
        if (!validateExpressionSyntax(expression)) {
            return ValidationResult.invalid("计算表达式语法错误");
        }
        
        // 验证数据字段
        if (!dataFieldsStr.isEmpty()) {
            List<String> dataFields = parseDataFields(dataFieldsStr);
            for (String field : dataFields) {
                if (!SUPPORTED_FIELDS.contains(field)) {
                    return ValidationResult.invalid("不支持的数据字段: " + field);
                }
            }
        }
        
        // 验证聚合方式
        if (!aggregationMethod.isEmpty() && !SUPPORTED_AGGREGATIONS.contains(aggregationMethod)) {
            return ValidationResult.invalid("不支持的聚合方式: " + aggregationMethod);
        }
        
        // 验证过滤条件语法
        if (!filterCondition.isEmpty() && !validateFilterSyntax(filterCondition)) {
            return ValidationResult.invalid("过滤条件语法错误");
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * 验证表达式语法
     */
    private boolean validateExpressionSyntax(String expression) {
        try {
            // 简单的语法检查：括号匹配
            int balance = 0;
            for (char c : expression.toCharArray()) {
                if (c == '(') balance++;
                else if (c == ')') {
                    balance--;
                    if (balance < 0) return false;
                }
            }
            return balance == 0;
            
            // TODO: 更详细的语法验证可以使用ANTLR等解析器
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证过滤条件语法
     */
    private boolean validateFilterSyntax(String filterCondition) {
        try {
            // 简单验证：检查基本运算符和字段名
            return filterCondition.matches("^[A-Za-z_][A-Za-z0-9_]*\\s*(=|!=|>|<|>=|<=|IS|IS NOT)\\s*.+$");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 解析数据字段列表
     */
    private List<String> parseDataFields(String dataFieldsStr) {
        if (dataFieldsStr.isEmpty()) {
            return Collections.emptyList();
        }
        
        return Arrays.stream(dataFieldsStr.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }
    
    /**
     * 构建数据源映射
     */
    private Map<String, String> buildDataSourceMapping(List<String> dataFields) {
        Map<String, String> mapping = new HashMap<>();
        
        for (String field : dataFields) {
            String dataSource = inferDataSource(field);
            mapping.put(field, dataSource);
        }
        
        return mapping;
    }
    
    /**
     * 推断数据源
     */
    private String inferDataSource(String field) {
        if (field.startsWith("CLOSE_PRICE") || field.startsWith("OPEN_PRICE") || 
            field.startsWith("HIGH_PRICE") || field.startsWith("LOW_PRICE") ||
            field.contains("RETURN") || field.contains("PRICE_SERIES")) {
            return "market_data";
        } else if (field.contains("REVENUE") || field.contains("PROFIT") || 
                   field.contains("ASSETS") || field.contains("EQUITY")) {
            return "financial_statement";
        } else if (field.endsWith("_TTM") || field.endsWith("BVPS") || 
                   field.endsWith("RPS") || field.endsWith("DPS")) {
            return "financial_ratio";
        }
        return "unknown";
    }
    
    /**
     * 验证结果
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}