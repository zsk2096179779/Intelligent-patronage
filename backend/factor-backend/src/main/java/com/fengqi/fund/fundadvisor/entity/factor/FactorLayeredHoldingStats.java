package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分层回测持有期收益统计实体类
 */
@Data
public class FactorLayeredHoldingStats {
    
    /**
     * 统计ID
     */
    private Integer statsId;
    
    /**
     * 关联结果ID
     */
    private Integer resultId;
    
    /**
     * 持有期天数
     */
    private Integer holdingDays;
    
    /**
     * 平均收益率
     */
    private BigDecimal meanReturn;
    
    /**
     * 收益率标准差
     */
    private BigDecimal stdReturn;
    
    /**
     * 最小收益率
     */
    private BigDecimal minReturn;
    
    /**
     * 第一四分位数
     */
    private BigDecimal q1Return;
    
    /**
     * 中位数收益率
     */
    private BigDecimal medianReturn;
    
    /**
     * 第三四分位数
     */
    private BigDecimal q3Return;
    
    /**
     * 最大收益率
     */
    private BigDecimal maxReturn;
    
    /**
     * 胜率（正收益比例）
     */
    private BigDecimal winRate;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}