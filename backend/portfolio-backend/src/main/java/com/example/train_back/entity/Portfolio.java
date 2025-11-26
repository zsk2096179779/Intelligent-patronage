package com.example.train_back.entity;

import java.math.BigDecimal;

public class Portfolio {

    private Integer id;
    private String name;
    private String riskLevel;     // 风险等级：低/中低/中/中高/高 或 “中低风险”
    private String strategyType;
    private Integer strategyId;
    private Integer listed;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Integer strategyId) {
        this.strategyId = strategyId;
    }

    public Integer getListed() {
        return listed;
    }

    public void setListed(Integer listed) {
        this.listed = listed;
    }


}
