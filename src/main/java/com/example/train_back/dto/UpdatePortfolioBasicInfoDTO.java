package com.example.train_back.dto;

/**
 * 更新组合基础信息请求DTO
 */
public class UpdatePortfolioBasicInfoDTO {
    /**
     * 组合名称
     */
    private String name;
    
    /**
     * 风险等级
     */
    private String riskLevel;
    
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
     * 策略ID（关联的策略ID）
     */
    private Integer strategyId;

    public UpdatePortfolioBasicInfoDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
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

    public Integer getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Integer strategyId) {
        this.strategyId = strategyId;
    }
}

