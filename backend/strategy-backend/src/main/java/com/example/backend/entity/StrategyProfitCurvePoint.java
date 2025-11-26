package com.example.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StrategyProfitCurvePoint {
    private Long id;
    private Long strategyId;
    private LocalDate pointDate;
    private BigDecimal netValue;

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

    public LocalDate getPointDate() {
        return pointDate;
    }

    public void setPointDate(LocalDate pointDate) {
        this.pointDate = pointDate;
    }

    public BigDecimal getNetValue() {
        return netValue;
    }

    public void setNetValue(BigDecimal netValue) {
        this.netValue = netValue;
    }
}

