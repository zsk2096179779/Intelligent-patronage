package com.fengqi.fund.fundadvisor.dto.factor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 因子检验请求DTO
 */
@Data
@Schema(description = "因子检验请求")
public class FactorValidationRequest {
    
    @Schema(description = "任务名称", example = "价值因子IC检验", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String taskName;
    
    @Schema(description = "任务类型", example = "IC_IR", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String taskType = "IC_IR";
    
    @Schema(description = "检验因子名称列表", example = "[\"基金收益率因子\", \"基金波动率因子\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "因子名称列表不能为空")
    @NotNull(message = "因子名称列表不能为空")
    @JsonDeserialize(using = FactorNamesDeserializer.class)
    private List<String> factorNames;
    
    /**
     * 自定义反序列化器，支持字符串格式的 factorNames
     * 例如: "[\"基金收益率因子\"]" 或 ["基金收益率因子"]
     */
    public static class FactorNamesDeserializer extends JsonDeserializer<List<String>> {
        private static final ObjectMapper objectMapper = new ObjectMapper();
        
        @Override
        public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            com.fasterxml.jackson.core.JsonToken currentToken = p.getCurrentToken();
            
            if (currentToken == com.fasterxml.jackson.core.JsonToken.START_ARRAY) {
                // 如果是数组格式，直接解析
                return objectMapper.readValue(p, new TypeReference<List<String>>() {});
            } else if (currentToken == com.fasterxml.jackson.core.JsonToken.VALUE_STRING) {
                // 如果是字符串格式，先解析字符串为数组
                String jsonString = p.getText();
                try {
                    return objectMapper.readValue(jsonString, new TypeReference<List<String>>() {});
                } catch (Exception e) {
                    // 如果解析失败，尝试作为单个字符串处理
                    List<String> result = new ArrayList<>();
                    result.add(jsonString);
                    return result;
                }
            } else {
                throw new IOException("无法解析 factorNames，期望数组或字符串格式，当前类型: " + currentToken);
            }
        }
    }
    
    @Schema(description = "回测开始日期", example = "2020-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "回测开始日期不能为空")
    private LocalDate startDate;
    
    @Schema(description = "回测结束日期", example = "2025-11-20", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "回测结束日期不能为空")
    private LocalDate endDate;
    
    @Schema(description = "创建人ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer createUserId;
    
    @Schema(description = "基准指数代码", example = "000300", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String benchmarkCode;
}

