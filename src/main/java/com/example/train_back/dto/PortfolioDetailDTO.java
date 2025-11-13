package com.example.train_back.dto;

import java.time.LocalDateTime;

/**
 * 组合详情DTO（包含策略信息）
 */
public class PortfolioDetailDTO {
    /**
     * 组合ID（主键）
     */
    private Integer portfolioId;
    
    /**
     * 组合名称
     */
    private String portfolioName;
    
    /**
     * 风险等级
     */
    private String riskLevel;
    
    /**
     * 组合策略类型
     */
    private String portfolioStrategyType;
    
    /**
     * 是否上架（0-未上架/未审核，1-已上架/已审核，-1-已拒绝）
     */
    private Integer listed;
    
    /**
     * 审核状态（draft/pending_review/approved/rejected）
     */
    private String status;
    
    /**
     * 审核拒绝原因
     */
    private String rejectReason;
    
    // ========== 策略信息 ==========
    
    /**
     * 策略ID
     */
    private Integer strategyId;
    
    /**
     * 策略名称
     */
    private String strategyName;
    
    /**
     * 策略类型
     */
    private String strategyType;
    
    /**
     * 策略描述
     */
    private String description;
    
    /**
     * 对应策略的ID（外键）
     */
    private Integer strategyRefId;
    
    /**
     * 创立时间
     */
    private LocalDateTime createTime;
    
    /**
     * 资产规模（亿元）
     */
    private Double scale;
    
    /**
     * 成份基金（JSON或列表）
     */
    private String funds;
    
    /**
     * 费率（%）
     */
    private Double feeRate;
    
    /**
     * 策略收益（%）
     */
    private Double returnRate;
    
    /**
     * 策略年化收益（%）
     */
    private Double annualReturn;
    
    /**
     * 波动率
     */
    private Double volatility;
    
    /**
     * 夏普比率
     */
    private Double sharpeRatio;
    
    /**
     * 最大回撤（%）
     */
    private Double maxDrawdown;
    
    /**
     * 平仓胜率（%）
     */
    private Double winRate;

    public PortfolioDetailDTO() {
    }

    // Getter and Setter methods
    public Integer getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Integer portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getPortfolioStrategyType() {
        return portfolioStrategyType;
    }

    public void setPortfolioStrategyType(String portfolioStrategyType) {
        this.portfolioStrategyType = portfolioStrategyType;
    }

    public Integer getListed() {
        return listed;
    }

    public void setListed(Integer listed) {
        this.listed = listed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Integer getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Integer strategyId) {
        this.strategyId = strategyId;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public String getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(String strategyType) {
        this.strategyType = strategyType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStrategyRefId() {
        return strategyRefId;
    }

    public void setStrategyRefId(Integer strategyRefId) {
        this.strategyRefId = strategyRefId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public String getFunds() {
        return funds;
    }

    public void setFunds(String funds) {
        this.funds = funds;
    }

    public Double getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(Double feeRate) {
        this.feeRate = feeRate;
    }

    public Double getReturnRate() {
        return returnRate;
    }

    public void setReturnRate(Double returnRate) {
        this.returnRate = returnRate;
    }

    public Double getAnnualReturn() {
        return annualReturn;
    }

    public void setAnnualReturn(Double annualReturn) {
        this.annualReturn = annualReturn;
    }

    public Double getVolatility() {
        return volatility;
    }

    public void setVolatility(Double volatility) {
        this.volatility = volatility;
    }

    public Double getSharpeRatio() {
        return sharpeRatio;
    }

    public void setSharpeRatio(Double sharpeRatio) {
        this.sharpeRatio = sharpeRatio;
    }

    public Double getMaxDrawdown() {
        return maxDrawdown;
    }

    public void setMaxDrawdown(Double maxDrawdown) {
        this.maxDrawdown = maxDrawdown;
    }

    public Double getWinRate() {
        return winRate;
    }

    public void setWinRate(Double winRate) {
        this.winRate = winRate;
    }

    @Override
    public String toString() {
        return "PortfolioDetailDTO{" +
                "portfolioId=" + portfolioId +
                ", portfolioName='" + portfolioName + '\'' +
                ", riskLevel='" + riskLevel + '\'' +
                ", portfolioStrategyType='" + portfolioStrategyType + '\'' +
                ", listed=" + listed +
                ", status='" + status + '\'' +
                ", rejectReason='" + rejectReason + '\'' +
                ", strategyId=" + strategyId +
                ", strategyName='" + strategyName + '\'' +
                ", strategyType='" + strategyType + '\'' +
                ", description='" + description + '\'' +
                ", strategyRefId=" + strategyRefId +
                ", createTime=" + createTime +
                ", scale=" + scale +
                ", funds='" + funds + '\'' +
                ", feeRate=" + feeRate +
                ", returnRate=" + returnRate +
                ", annualReturn=" + annualReturn +
                ", volatility=" + volatility +
                ", sharpeRatio=" + sharpeRatio +
                ", maxDrawdown=" + maxDrawdown +
                ", winRate=" + winRate +
                '}';
    }
}

