package com.fengqi.fund.fundadvisor.dto.factor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 因子检验响应DTO
 */
@Data
@Schema(description = "因子检验响应")
public class FactorValidationResponse {
    
    @Schema(description = "操作类型", example = "CREATE_VALIDATION_TASK")
    private String operationType;
    
    @Schema(description = "操作是否成功", example = "true")
    private Boolean success;
    
    @Schema(description = "响应消息", example = "任务创建成功")
    private String message;
    
    @Schema(description = "任务信息")
    private TaskInfo taskInfo;
    
    @Schema(description = "IC/IR结果列表")
    private List<IcIrResult> icIrResults;
    
    @Schema(description = "操作时间")
    private LocalDateTime operationTime;
    
    // 便利方法：获取任务ID
    public Integer getTaskId() {
        if (taskInfo != null) {
            return taskInfo.getTaskId();
        }
        return null;
    }
    
    /**
     * 任务信息
     */
    @Data
    @Schema(description = "任务信息")
    public static class TaskInfo {
        @Schema(description = "任务ID", example = "1")
        private Integer taskId;
        
        @Schema(description = "任务名称", example = "价值因子IC检验")
        private String taskName;
        
        @Schema(description = "任务类型", example = "IC_IR")
        private String taskType;
        
        @Schema(description = "任务状态", example = "SUCCESS")
        private String taskStatus;
        
        @Schema(description = "任务进度", example = "100")
        private Integer progress;
        
        @Schema(description = "创建时间")
        private LocalDateTime createTime;
        
        @Schema(description = "开始执行时间")
        private LocalDateTime startTime;
        
        @Schema(description = "结束执行时间")
        private LocalDateTime endTime;
    }
    
    /**
     * IC/IR结果
     */
    @Data
    @Schema(description = "IC/IR结果")
    public static class IcIrResult {
        @Schema(description = "结果ID", example = "1")
        private Integer resultId;
        
        @Schema(description = "任务ID", example = "1")
        private Integer taskId;
        
        @Schema(description = "因子ID", example = "1")
        private Integer factorId;
        
        @Schema(description = "因子编码", example = "VAL_COM")
        private String factorCode;
        
        @Schema(description = "因子名称", example = "估值综合因子")
        private String factorName;
        
        @Schema(description = "IC均值", example = "0.123456")
        private BigDecimal icMean;
        
        @Schema(description = "IC标准差", example = "0.045678")
        private BigDecimal icStd;
        
        @Schema(description = "IR值", example = "2.701234")
        private BigDecimal irValue;
        
        @Schema(description = "IC正相关比例", example = "0.65")
        private BigDecimal icPositiveRatio;
        
        @Schema(description = "IC序列（日期->IC值）")
        private Map<LocalDate, BigDecimal> icSequence;
        
        @Schema(description = "计算日期")
        private LocalDate calculationDate;
    }
}








