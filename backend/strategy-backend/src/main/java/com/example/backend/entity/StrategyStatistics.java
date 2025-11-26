package com.example.backend.entity;

import java.math.BigDecimal;

public class StrategyStatistics {
    private Long strategyId;
    private BigDecimal totalReturn;
    private BigDecimal annualReturn;
    private BigDecimal maxDrawdown;
    private BigDecimal recent30dReturn;
    private BigDecimal recent6mReturn;
    private BigDecimal excessReturn;
    private BigDecimal sharpeRatio;
    private BigDecimal volatility;
    private BigDecimal beta;
    private BigDecimal informationRatio;
    private Integer winRateCount;
    private BigDecimal winRatePercentage;

    public Long getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Long strategyId) {
        this.strategyId = strategyId;
    }

    public BigDecimal getTotalReturn() {
        return totalReturn;
    }

    public void setTotalReturn(BigDecimal totalReturn) {
        this.totalReturn = totalReturn;
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

    public BigDecimal getRecent30dReturn() {
        return recent30dReturn;
    }

    public void setRecent30dReturn(BigDecimal recent30dReturn) {
        this.recent30dReturn = recent30dReturn;
    }

    public BigDecimal getRecent6mReturn() {
        return recent6mReturn;
    }

    public void setRecent6mReturn(BigDecimal recent6mReturn) {
        this.recent6mReturn = recent6mReturn;
    }

    public BigDecimal getExcessReturn() {
        return excessReturn;
    }

    public void setExcessReturn(BigDecimal excessReturn) {
        this.excessReturn = excessReturn;
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

    public BigDecimal getBeta() {
        return beta;
    }

    public void setBeta(BigDecimal beta) {
        this.beta = beta;
    }

    public BigDecimal getInformationRatio() {
        return informationRatio;
    }

    public void setInformationRatio(BigDecimal informationRatio) {
        this.informationRatio = informationRatio;
    }

    public Integer getWinRateCount() {
        return winRateCount;
    }

    public void setWinRateCount(Integer winRateCount) {
        this.winRateCount = winRateCount;
    }

    public BigDecimal getWinRatePercentage() {
        return winRatePercentage;
    }

    public void setWinRatePercentage(BigDecimal winRatePercentage) {
        this.winRatePercentage = winRatePercentage;
    }
}

