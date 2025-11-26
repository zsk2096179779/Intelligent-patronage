package com.example.train_back.dto;

import java.math.BigDecimal;

/**
 * 更新组合收益指标请求DTO
 * 用于更新组合产品的实际运行数据
 */
public class UpdatePortfolioPerformanceDTO {
    
    /**
     * 策略收益（%）
     */
    private BigDecimal returnRate;
    
    /**
     * 年化收益（%）
     */
    private BigDecimal annualReturn;
    
    /**
     * 最大回撤（%）
     */
    private BigDecimal maxDrawdown;
    
    /**
     * 夏普比率
     */
    private BigDecimal sharpeRatio;
    
    /**
     * 波动率（%）
     */
    private BigDecimal volatility;
    
    /**
     * 胜率（%）
     */
    private BigDecimal winRate;

    public UpdatePortfolioPerformanceDTO() {
    }

    // Getter and Setter methods
    
    public BigDecimal getReturnRate() {
        return returnRate;
    }

    public void setReturnRate(BigDecimal returnRate) {
        this.returnRate = returnRate;
    }

    public BigDecimal getAnnualReturn() {
        return annualReturn;
    }

    public void setAnnualReturn(BigDecimal annualReturn) {
        this.annualReturn = annualReturn;
    }

    public BigDecimal getMaxDrawdown() {
        return maxDrawdown;
    }

    public void setMaxDrawdown(BigDecimal maxDrawdown) {
        this.maxDrawdown = maxDrawdown;
    }

    public BigDecimal getSharpeRatio() {
        return sharpeRatio;
    }

    public void setSharpeRatio(BigDecimal sharpeRatio) {
        this.sharpeRatio = sharpeRatio;
    }

    public BigDecimal getVolatility() {
        return volatility;
    }

    public void setVolatility(BigDecimal volatility) {
        this.volatility = volatility;
    }

    public BigDecimal getWinRate() {
        return winRate;
    }

    public void setWinRate(BigDecimal winRate) {
        this.winRate = winRate;
    }

    @Override
    public String toString() {
        return "UpdatePortfolioPerformanceDTO{" +
                "returnRate=" + returnRate +
                ", annualReturn=" + annualReturn +
                ", maxDrawdown=" + maxDrawdown +
                ", sharpeRatio=" + sharpeRatio +
                ", volatility=" + volatility +
                ", winRate=" + winRate +
                '}';
    }
}

