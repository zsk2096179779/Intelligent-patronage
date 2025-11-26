package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 分层回测净值序列实体类
 */
@Data
public class FactorLayeredNavSeries {
    
    /**
     * 序列ID
     */
    private Integer seriesId;
    
    /**
     * 关联结果ID
     */
    private Integer resultId;
    
    /**
     * 交易日期
     */
    private LocalDate tradeDate;
    
    /**
     * 净值
     */
    private BigDecimal navValue;
    
    /**
     * 日收益率
     */
    private BigDecimal dailyReturn;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 累计收益率
     */
    private BigDecimal cumulativeReturn;
    
    /**
     * 股票数量
     */
    private Integer stockCount;
    
    /**
     * 权重总和
     */
    private BigDecimal weightSum;
    
    /**
     * 换手率
     */
    private BigDecimal turnoverRate;
}