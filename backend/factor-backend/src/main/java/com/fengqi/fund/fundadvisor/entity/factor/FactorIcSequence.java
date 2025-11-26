package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 因子IC序列明细实体类
 * 对应 factor_ic_sequence 表
 */
@Data
public class FactorIcSequence {
    
    /**
     * 序列ID
     */
    private Integer sequenceId;
    
    /**
     * 关联结果ID
     */
    private Integer resultId;
    
    /**
     * 交易日期
     */
    private LocalDate tradeDate;
    
    /**
     * 当日IC值
     */
    private BigDecimal icValue;
    
    /**
     * Rank IC值（可选）
     */
    private BigDecimal rankIc;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}









