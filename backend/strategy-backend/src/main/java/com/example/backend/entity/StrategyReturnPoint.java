package com.example.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StrategyReturnPoint {
    private Long id;
    private Long strategyId;
    private LocalDate date;
    private BigDecimal strategyReturn;
    private BigDecimal benchmarkReturn;

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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getStrategyReturn() {
        return strategyReturn;
    }

    public void setStrategyReturn(BigDecimal strategyReturn) {
        this.strategyReturn = strategyReturn;
    }

    public BigDecimal getBenchmarkReturn() {
        return benchmarkReturn;
    }

    public void setBenchmarkReturn(BigDecimal benchmarkReturn) {
        this.benchmarkReturn = benchmarkReturn;
    }
}

