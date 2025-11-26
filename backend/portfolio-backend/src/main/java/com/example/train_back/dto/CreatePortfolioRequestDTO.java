package com.example.train_back.dto;

/**
 * 创建组合产品请求DTO
 */
public class CreatePortfolioRequestDTO {
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
     * 策略引用ID（关联 strategies.strategy_ref_id）
     */
    private Integer strategyRefId;
    
    /**
     * 组合简介
     */
    private String summary;
    
    /**
     * 目标客户
     */
    private String targetInvestor;

    public CreatePortfolioRequestDTO() {
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

    public Integer getStrategyRefId() {
        return strategyRefId;
    }

    public void setStrategyRefId(Integer strategyRefId) {
        this.strategyRefId = strategyRefId;
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

    @Override
    public String toString() {
        return "CreatePortfolioRequestDTO{" +
                "name='" + name + '\'' +
                ", riskLevel='" + riskLevel + '\'' +
                ", strategyType='" + strategyType + '\'' +
                ", strategyRefId=" + strategyRefId +
                '}';
    }
}

