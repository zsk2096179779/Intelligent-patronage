package com.example.train_back.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 组合产品参数实体类
 */
public class PortfolioProductParams {
    /**
     * 主键ID
     */
    private Integer id;
    
    /**
     * 组合ID（外键，关联portfolios表）
     */
    private Integer portfolioId;
    
    /**
     * 最低投资额
     */
    private BigDecimal minInvestAmount;
    
    /**
     * 最高投资额/规模上限
     */
    private BigDecimal maxInvestAmount;
    
    /**
     * 申购费率（%）
     */
    private BigDecimal subscriptionFee;
    
    /**
     * 赎回费率（%）
     */
    private BigDecimal redemptionFee;
    
    /**
     * 管理费率（%）
     */
    private BigDecimal managementFee;
    
    /**
     * 开放日规则
     */
    private String openDayRule;
    
    /**
     * 赎回规则
     */
    private String redemptionRule;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    public PortfolioProductParams() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Integer portfolioId) {
        this.portfolioId = portfolioId;
    }

    public BigDecimal getMinInvestAmount() {
        return minInvestAmount;
    }

    public void setMinInvestAmount(BigDecimal minInvestAmount) {
        this.minInvestAmount = minInvestAmount;
    }

    public BigDecimal getMaxInvestAmount() {
        return maxInvestAmount;
    }

    public void setMaxInvestAmount(BigDecimal maxInvestAmount) {
        this.maxInvestAmount = maxInvestAmount;
    }

    public BigDecimal getSubscriptionFee() {
        return subscriptionFee;
    }

    public void setSubscriptionFee(BigDecimal subscriptionFee) {
        this.subscriptionFee = subscriptionFee;
    }

    public BigDecimal getRedemptionFee() {
        return redemptionFee;
    }

    public void setRedemptionFee(BigDecimal redemptionFee) {
        this.redemptionFee = redemptionFee;
    }

    public BigDecimal getManagementFee() {
        return managementFee;
    }

    public void setManagementFee(BigDecimal managementFee) {
        this.managementFee = managementFee;
    }

    public String getOpenDayRule() {
        return openDayRule;
    }

    public void setOpenDayRule(String openDayRule) {
        this.openDayRule = openDayRule;
    }

    public String getRedemptionRule() {
        return redemptionRule;
    }

    public void setRedemptionRule(String redemptionRule) {
        this.redemptionRule = redemptionRule;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "PortfolioProductParams{" +
                "id=" + id +
                ", portfolioId=" + portfolioId +
                ", minInvestAmount=" + minInvestAmount +
                ", maxInvestAmount=" + maxInvestAmount +
                ", subscriptionFee=" + subscriptionFee +
                ", redemptionFee=" + redemptionFee +
                ", managementFee=" + managementFee +
                ", openDayRule='" + openDayRule + '\'' +
                ", redemptionRule='" + redemptionRule + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

