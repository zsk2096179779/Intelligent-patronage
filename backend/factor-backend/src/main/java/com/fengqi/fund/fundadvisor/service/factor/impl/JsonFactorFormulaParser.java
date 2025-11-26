package com.fengqi.fund.fundadvisor.service.factor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * JSON格式因子公式解析器
 * 
 * 解析存储在 factor_base.factor_formula 中的JSON格式公式
 * 支持表达式计算和函数调用
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Slf4j
@Component
public class JsonFactorFormulaParser {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 解析JSON格式的因子公式
     * 
     * @param formulaJson JSON格式的公式字符串
     * @return 解析后的公式信息
     */
    public ParsedFormula parseFormula(String formulaJson) {
        if (formulaJson == null || formulaJson.trim().isEmpty()) {
            return ParsedFormula.invalid("公式为空");
        }
        
        try {
            JsonNode root = objectMapper.readTree(formulaJson);
            
            // 验证必需字段
            if (!root.has("type") || !root.has("version") || !root.has("expression")) {
                return ParsedFormula.invalid("公式缺少必需字段：type, version, expression");
            }
            
            String type = root.get("type").asText();
            String version = root.get("version").asText();
            String expression = root.get("expression").asText();
            
            if (!"formula".equals(type)) {
                return ParsedFormula.invalid("公式类型必须是 'formula'");
            }
            
            // 解析数据字段
            List<String> dataFields = new ArrayList<>();
            if (root.has("dataFields") && root.get("dataFields").isArray()) {
                for (JsonNode field : root.get("dataFields")) {
                    dataFields.add(field.asText());
                }
            }
            
            // 解析描述
            String description = root.has("description") ? root.get("description").asText() : "";
            
            return ParsedFormula.valid(expression, dataFields, description, version);
            
        } catch (Exception e) {
            log.error("解析公式JSON失败: {}", formulaJson, e);
            return ParsedFormula.invalid("公式JSON格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 验证公式格式
     * 
     * @param formulaJson JSON格式的公式字符串
     * @return 验证结果
     */
    public boolean validateFormula(String formulaJson) {
        ParsedFormula parsed = parseFormula(formulaJson);
        return parsed.isValid();
    }
    
    /**
     * 解析后的公式信息
     */
    public static class ParsedFormula {
        private final boolean valid;
        private final String expression;
        private final List<String> dataFields;
        private final String description;
        private final String version;
        private final String errorMessage;
        
        private ParsedFormula(boolean valid, String expression, List<String> dataFields, 
                             String description, String version, String errorMessage) {
            this.valid = valid;
            this.expression = expression;
            this.dataFields = dataFields;
            this.description = description;
            this.version = version;
            this.errorMessage = errorMessage;
        }
        
        public static ParsedFormula valid(String expression, List<String> dataFields, 
                                        String description, String version) {
            return new ParsedFormula(true, expression, dataFields, description, version, null);
        }
        
        public static ParsedFormula invalid(String errorMessage) {
            return new ParsedFormula(false, null, null, null, null, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getExpression() {
            return expression;
        }
        
        public List<String> getDataFields() {
            return dataFields;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getVersion() {
            return version;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}









