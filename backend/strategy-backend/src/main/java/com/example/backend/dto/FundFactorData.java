package com.example.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 基金因子数据（从CSV读取）
 */
public class FundFactorData {
    private String code;
    private String name;
    private LocalDate updateDate;
    private BigDecimal nav;  // 单位净值
    private BigDecimal navAcc;  // 累计净值
    private BigDecimal dividendRatio;  // 分红比率
    private BigDecimal volatility20;  // 20日波动率
    private BigDecimal momentum20;  // 20日动量
    private BigDecimal drawdown;  // 最大回撤
    private BigDecimal sharpe20;  // 20日夏普比率
    private BigDecimal meanReversion;  // 均值回归
    
    // 计算后的综合得分
    private BigDecimal compositeScore;
    
    public FundFactorData() {
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public LocalDate getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(LocalDate updateDate) {
        this.updateDate = updateDate;
    }
    
    public BigDecimal getNav() {
        return nav;
    }
    
    public void setNav(BigDecimal nav) {
        this.nav = nav;
    }
    
    public BigDecimal getNavAcc() {
        return navAcc;
    }
    
    public void setNavAcc(BigDecimal navAcc) {
        this.navAcc = navAcc;
    }
    
    public BigDecimal getDividendRatio() {
        return dividendRatio;
    }
    
    public void setDividendRatio(BigDecimal dividendRatio) {
        this.dividendRatio = dividendRatio;
    }
    
    public BigDecimal getVolatility20() {
        return volatility20;
    }
    
    public void setVolatility20(BigDecimal volatility20) {
        this.volatility20 = volatility20;
    }
    
    public BigDecimal getMomentum20() {
        return momentum20;
    }
    
    public void setMomentum20(BigDecimal momentum20) {
        this.momentum20 = momentum20;
    }
    
    public BigDecimal getDrawdown() {
        return drawdown;
    }
    
    public void setDrawdown(BigDecimal drawdown) {
        this.drawdown = drawdown;
    }
    
    public BigDecimal getSharpe20() {
        return sharpe20;
    }
    
    public void setSharpe20(BigDecimal sharpe20) {
        this.sharpe20 = sharpe20;
    }
    
    public BigDecimal getMeanReversion() {
        return meanReversion;
    }
    
    public void setMeanReversion(BigDecimal meanReversion) {
        this.meanReversion = meanReversion;
    }
    
    public BigDecimal getCompositeScore() {
        return compositeScore;
    }
    
    public void setCompositeScore(BigDecimal compositeScore) {
        this.compositeScore = compositeScore;
    }
    
    /**
     * 根据因子名称获取对应的因子值
     */
    public BigDecimal getFactorValue(String factorName) {
        if (factorName == null) {
            return null;
        }
        switch (factorName.toLowerCase()) {
            case "dividend_ratio":
            case "dividendratio":
                return dividendRatio;
            case "volatility_20":
            case "volatility20":
                return volatility20;
            case "momentum_20":
            case "momentum20":
                return momentum20;
            case "drawdown":
                return drawdown;
            case "sharpe_20":
            case "sharpe20":
                return sharpe20;
            case "mean_reversion":
            case "meanreversion":
                return meanReversion;
            default:
                return null;
        }
    }
}

