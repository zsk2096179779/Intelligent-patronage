package com.fengqi.fund.fundadvisor.service.factor.dto;

import lombok.Data;
import lombok.Builder;
import java.util.List;

/**
 * 公式验证结果
 * 
 * 存储公式语法和逻辑验证的结果
 * 包含错误信息和警告信息
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Data
@Builder
public class FormulaValidationResult {
    
    /**
     * 验证是否通过
     */
    private Boolean isValid;
    
    /**
     * 错误信息列表
     */
    private List<ValidationMessage> errors;
    
    /**
     * 警告信息列表
     */
    private List<ValidationMessage> warnings;
    
    /**
     * 验证信息列表
     */
    private List<ValidationMessage> infos;
    
    /**
     * 验证耗时（毫秒）
     */
    private Long validationDuration;
    
    /**
     * 验证时间戳
     */
    private java.time.LocalDateTime validationTime;
    
    /**
     * 验证消息
     */
    @Data
    @Builder
    public static class ValidationMessage {
        
        /**
         * 消息类型
         */
        private MessageType type;
        
        /**
         * 消息内容
         */
        private String message;
        
        /**
         * 字段名（如果适用）
         */
        private String fieldName;
        
        /**
         * 行号（如果适用）
         */
        private Integer lineNumber;
        
        /**
         * 列号（如果适用）
         */
        private Integer columnNumber;
        
        /**
         * 建议修复方案
         */
        private String suggestion;
        
        public enum MessageType {
            ERROR("错误"),
            WARNING("警告"),
            INFO("信息");
            
            private final String description;
            
            MessageType(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
}