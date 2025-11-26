package com.example.train_back.dto;

import java.math.BigDecimal;

/**
 * 产品参数DTO
 */
public class ProductParamsDTO {
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

    public ProductParamsDTO() {
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
}

