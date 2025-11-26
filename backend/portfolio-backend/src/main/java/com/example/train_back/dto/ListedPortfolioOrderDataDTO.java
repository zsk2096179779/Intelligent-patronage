package com.example.train_back.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 已上架组合产品的用户订购数据DTO
 */
public class ListedPortfolioOrderDataDTO {
    
    // ========== 组合基本信息 ==========
    
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
     * 策略名称
     */
    private String strategyName;
    
    /**
     * 策略类型
     */
    private String strategyType;
    
    /**
     * 组合简介
     */
    private String summary;
    
    /**
     * 目标客户
     */
    private String targetInvestor;
    
    /**
     * 组合创建时间
     */
    private LocalDateTime portfolioCreatedAt;
    
    // ========== 订购统计数据 ==========
    
    /**
     * 订单总数
     */
    private Long totalOrderCount;
    
    /**
     * 已完成订单数
     */
    private Long completedOrderCount;
    
    /**
     * 总订购金额
     */
    private BigDecimal totalSubscriptionAmount;
    
    /**
     * 已完成订单总金额
     */
    private BigDecimal completedSubscriptionAmount;
    
    /**
     * 订购用户数（去重后的用户数量）
     */
    private Long totalUserCount;
    
    /**
     * 平均订购金额
     */
    private BigDecimal avgSubscriptionAmount;

    public ListedPortfolioOrderDataDTO() {
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTargetInvestor() {
        return targetInvestor;
    }

    public void setTargetInvestor(String targetInvestor) {
        this.targetInvestor = targetInvestor;
    }

    public LocalDateTime getPortfolioCreatedAt() {
        return portfolioCreatedAt;
    }

    public void setPortfolioCreatedAt(LocalDateTime portfolioCreatedAt) {
        this.portfolioCreatedAt = portfolioCreatedAt;
    }

    public Long getTotalOrderCount() {
        return totalOrderCount;
    }

    public void setTotalOrderCount(Long totalOrderCount) {
        this.totalOrderCount = totalOrderCount;
    }

    public Long getCompletedOrderCount() {
        return completedOrderCount;
    }

    public void setCompletedOrderCount(Long completedOrderCount) {
        this.completedOrderCount = completedOrderCount;
    }

    public BigDecimal getTotalSubscriptionAmount() {
        return totalSubscriptionAmount;
    }

    public void setTotalSubscriptionAmount(BigDecimal totalSubscriptionAmount) {
        this.totalSubscriptionAmount = totalSubscriptionAmount;
    }

    public BigDecimal getCompletedSubscriptionAmount() {
        return completedSubscriptionAmount;
    }

    public void setCompletedSubscriptionAmount(BigDecimal completedSubscriptionAmount) {
        this.completedSubscriptionAmount = completedSubscriptionAmount;
    }

    public Long getTotalUserCount() {
        return totalUserCount;
    }

    public void setTotalUserCount(Long totalUserCount) {
        this.totalUserCount = totalUserCount;
    }

    public BigDecimal getAvgSubscriptionAmount() {
        return avgSubscriptionAmount;
    }

    public void setAvgSubscriptionAmount(BigDecimal avgSubscriptionAmount) {
        this.avgSubscriptionAmount = avgSubscriptionAmount;
    }

    @Override
    public String toString() {
        return "ListedPortfolioOrderDataDTO{" +
                "portfolioId=" + portfolioId +
                ", portfolioName='" + portfolioName + '\'' +
                ", riskLevel='" + riskLevel + '\'' +
                ", portfolioStrategyType='" + portfolioStrategyType + '\'' +
                ", strategyName='" + strategyName + '\'' +
                ", strategyType='" + strategyType + '\'' +
                ", totalOrderCount=" + totalOrderCount +
                ", completedOrderCount=" + completedOrderCount +
                ", totalSubscriptionAmount=" + totalSubscriptionAmount +
                ", completedSubscriptionAmount=" + completedSubscriptionAmount +
                ", totalUserCount=" + totalUserCount +
                ", avgSubscriptionAmount=" + avgSubscriptionAmount +
                '}';
    }
}

