package com.example.backend.entity;

import java.time.LocalDateTime;

public class StrategyConfig {
    private Long id;
    private Long strategyId;
    private String rebalancePeriod;  // 再平衡周期（weekly/monthly/quarterly）
    private Integer maxRebalanceRatio;  // 最大调仓比例（1-100）
    private Boolean isActiveRebalance;  // 是否主动调仓（true=主动，false=被动）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public String getRebalancePeriod() {
        return rebalancePeriod;
    }

    public void setRebalancePeriod(String rebalancePeriod) {
        this.rebalancePeriod = rebalancePeriod;
    }

    public Integer getMaxRebalanceRatio() {
        return maxRebalanceRatio;
    }

    public void setMaxRebalanceRatio(Integer maxRebalanceRatio) {
        this.maxRebalanceRatio = maxRebalanceRatio;
    }

    public Boolean getIsActiveRebalance() {
        return isActiveRebalance;
    }

    public void setIsActiveRebalance(Boolean isActiveRebalance) {
        this.isActiveRebalance = isActiveRebalance;
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
}

