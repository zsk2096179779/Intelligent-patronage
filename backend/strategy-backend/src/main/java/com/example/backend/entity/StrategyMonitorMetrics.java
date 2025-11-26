package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class StrategyMonitorMetrics {
    private Long strategyId;
    private String systemStatus;  // 从strategy.status获取
    private Integer riskLevel;  // 从strategy.risk_level获取
    private String rebalancePeriod;  // 从strategy_config.rebalance_period获取
    private LocalDateTime monitorUpdatedAt;  // 从strategy.updated_at获取（对应API的updatedAt）

    public Long getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Long strategyId) {
        this.strategyId = strategyId;
    }

    public String getSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(String systemStatus) {
        this.systemStatus = systemStatus;
    }

    public Integer getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(Integer riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRebalancePeriod() {
        return rebalancePeriod;
    }

    public void setRebalancePeriod(String rebalancePeriod) {
        this.rebalancePeriod = rebalancePeriod;
    }

    @JsonIgnore  // 序列化时忽略此方法，只使用 getUpdatedAt()
    public LocalDateTime getMonitorUpdatedAt() {
        return monitorUpdatedAt;
    }

    public void setMonitorUpdatedAt(LocalDateTime monitorUpdatedAt) {
        this.monitorUpdatedAt = monitorUpdatedAt;
    }

    // 为了兼容前端，提供 updatedAt 的 getter（映射到 monitorUpdatedAt）
    @JsonProperty("updatedAt")  // 明确指定序列化字段名为 updatedAt
    public LocalDateTime getUpdatedAt() {
        return monitorUpdatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.monitorUpdatedAt = updatedAt;
    }
}
