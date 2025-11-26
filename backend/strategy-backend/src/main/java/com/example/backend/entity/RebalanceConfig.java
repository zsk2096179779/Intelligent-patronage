package com.example.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RebalanceConfig {
    private Long strategyId;
    private Boolean activeRebalancing;
    private Boolean triggerByThreshold;
    private Boolean triggerByPeriodic;
    private String frequency;
    private String executionTime;
    private BigDecimal maxAdjustmentRate;
    private BigDecimal stockDeviation;
    private BigDecimal bondDeviation;
    private BigDecimal commodityDeviation;
    private BigDecimal cashDeviation;
    private LocalDateTime updateTime;

    public Long getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Long strategyId) {
        this.strategyId = strategyId;
    }

    public Boolean getActiveRebalancing() {
        return activeRebalancing;
    }

    public void setActiveRebalancing(Boolean activeRebalancing) {
        this.activeRebalancing = activeRebalancing;
    }

    public Boolean getTriggerByThreshold() {
        return triggerByThreshold;
    }

    public void setTriggerByThreshold(Boolean triggerByThreshold) {
        this.triggerByThreshold = triggerByThreshold;
    }

    public Boolean getTriggerByPeriodic() {
        return triggerByPeriodic;
    }

    public void setTriggerByPeriodic(Boolean triggerByPeriodic) {
        this.triggerByPeriodic = triggerByPeriodic;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    public BigDecimal getMaxAdjustmentRate() {
        return maxAdjustmentRate;
    }

    public void setMaxAdjustmentRate(BigDecimal maxAdjustmentRate) {
        this.maxAdjustmentRate = maxAdjustmentRate;
    }

    public BigDecimal getStockDeviation() {
        return stockDeviation;
    }

    public void setStockDeviation(BigDecimal stockDeviation) {
        this.stockDeviation = stockDeviation;
    }

    public BigDecimal getBondDeviation() {
        return bondDeviation;
    }

    public void setBondDeviation(BigDecimal bondDeviation) {
        this.bondDeviation = bondDeviation;
    }

    public BigDecimal getCommodityDeviation() {
        return commodityDeviation;
    }

    public void setCommodityDeviation(BigDecimal commodityDeviation) {
        this.commodityDeviation = commodityDeviation;
    }

    public BigDecimal getCashDeviation() {
        return cashDeviation;
    }

    public void setCashDeviation(BigDecimal cashDeviation) {
        this.cashDeviation = cashDeviation;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}

