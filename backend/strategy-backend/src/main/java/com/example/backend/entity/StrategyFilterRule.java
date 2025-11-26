package com.example.backend.entity;

import java.time.LocalDateTime;

public class StrategyFilterRule {
    private Long id;
    private Long strategyId;
    private Integer topN;  // 筛选排名前N只
    private String typeLimit;  // 类型限制(7种类型独热编码)
    private Integer scaleLimit;  // 规模限制
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

    public Integer getTopN() {
        return topN;
    }

    public void setTopN(Integer topN) {
        this.topN = topN;
    }

    public String getTypeLimit() {
        return typeLimit;
    }

    public void setTypeLimit(String typeLimit) {
        this.typeLimit = typeLimit;
    }

    public Integer getScaleLimit() {
        return scaleLimit;
    }

    public void setScaleLimit(Integer scaleLimit) {
        this.scaleLimit = scaleLimit;
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

