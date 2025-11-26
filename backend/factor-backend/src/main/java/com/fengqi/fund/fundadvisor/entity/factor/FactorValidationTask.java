package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 因子检验任务实体类
 * 对应 factor_validation_task 表
 */
@Data
public class FactorValidationTask {
    
    /**
     * 检验任务ID
     */
    private Integer taskId;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 任务类型（IC_IR=IC/IR计算，LAYERED=分层回测）
     */
    private String taskType;
    
    /**
     * 检验因子ID列表（多个用逗号分隔）
     */
    private String factorIds;
    
    /**
     * 指定基金代码（可选，如果为空则计算所有基金）
     */
    private String fundCode;
    
    /**
     * 回测开始日期
     */
    private LocalDate startDate;
    
    /**
     * 回测结束日期
     */
    private LocalDate endDate;
    
    /**
     * 任务状态（PENDING=待执行，RUNNING=执行中，SUCCESS=成功，FAILED=失败）
     */
    private String taskStatus;
    
    /**
     * 任务进度（0-100）
     */
    private Integer progress;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建人ID
     */
    private Integer createUserId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 开始执行时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束执行时间
     */
    private LocalDateTime endTime;
}









