package com.example.backend.entity;

import java.math.BigDecimal;

public class StrategyHeatmap {
    private Long id;
    private Long strategyId;
    private String industry;
    private String comparisonDimension;
    private BigDecimal deviationValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Long strategyId) {
        this.strategyId = strategyId;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getComparisonDimension() {
        return comparisonDimension;
    }

    public void setComparisonDimension(String comparisonDimension) {
        this.comparisonDimension = comparisonDimension;
    }

    public BigDecimal getDeviationValue() {
        return deviationValue;
    }

    public void setDeviationValue(BigDecimal deviationValue) {
        this.deviationValue = deviationValue;
    }
}

