package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 因子IC/IR计算结果实体类
 * 对应 factor_ic_ir_result 表
 */
@Data
public class FactorIcIrResult {
    
    /**
     * 结果ID
     */
    private Integer resultId;
    
    /**
     * 关联任务ID
     */
    private Integer taskId;
    
    /**
     * 因子ID
     */
    private Integer factorId;
    
    /**
     * 因子编码
     */
    private String factorCode;
    
    /**
     * 因子名称
     */
    private String factorName;
    
    /**
     * IC均值
     */
    private BigDecimal icMean;
    
    /**
     * IC标准差
     */
    private BigDecimal icStd;
    
    /**
     * IR值（IC均值 / IC标准差）
     */
    private BigDecimal irValue;
    
    /**
     * IC正相关比例
     */
    private BigDecimal icPositiveRatio;
    
    /**
     * IC序列（JSON格式）
     */
    private String icSequence;
    
    /**
     * 计算日期
     */
    private LocalDate calculationDate;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}









