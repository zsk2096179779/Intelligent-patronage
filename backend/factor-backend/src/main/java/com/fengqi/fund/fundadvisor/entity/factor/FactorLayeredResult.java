package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 分层回测结果实体类
 */
@Data
public class FactorLayeredResult {
    
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
     * 分位数（1-5，分别代表5个分位组）
     */
    private Integer quantile;
    
    /**
     * 回测开始日期
     */
    private LocalDate startDate;
    
    /**
     * 回测结束日期
     */
    private LocalDate endDate;
    
    /**
     * 总收益率
     */
    private BigDecimal totalReturn;
    
    /**
     * 年化收益率
     */
    private BigDecimal annualizedReturn;
    
    /**
     * 夏普比率
     */
    private BigDecimal sharpeRatio;
    
    /**
     * 最大回撤
     */
    private BigDecimal maxDrawdown;
    
    /**
     * 胜率
     */
    private BigDecimal winRate;
    
    /**
     * 波动率
     */
    private BigDecimal volatility;
    
    /**
     * 计算日期（回测结束日期）
     */
    private LocalDate calculationDate;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}