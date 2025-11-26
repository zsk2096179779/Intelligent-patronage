package com.example.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 风险暴露矩阵数据
 * 纵轴：仓位、收益、回撤与基准的差值
 * 横轴：top N 基金
 */
public class RiskExposureMatrix {
    
    /**
     * 基金列表（横轴）
     */
    private List<FundInfo> funds;
    
    /**
     * 风险指标数据（纵轴）
     * key: 指标名称 (position/return/drawdown)
     * value: 每个基金对应的值
     */
    private Map<String, List<BigDecimal>> metrics;
    
    /**
     * 基准值（用于计算差值）
     */
    private Map<String, BigDecimal> baseline;
    
    public RiskExposureMatrix() {
    }
    
    public RiskExposureMatrix(List<FundInfo> funds, Map<String, List<BigDecimal>> metrics, Map<String, BigDecimal> baseline) {
        this.funds = funds;
        this.metrics = metrics;
        this.baseline = baseline;
    }
    
    public List<FundInfo> getFunds() {
        return funds;
    }
    
    public void setFunds(List<FundInfo> funds) {
        this.funds = funds;
    }
    
    public Map<String, List<BigDecimal>> getMetrics() {
        return metrics;
    }
    
    public void setMetrics(Map<String, List<BigDecimal>> metrics) {
        this.metrics = metrics;
    }
    
    public Map<String, BigDecimal> getBaseline() {
        return baseline;
    }
    
    public void setBaseline(Map<String, BigDecimal> baseline) {
        this.baseline = baseline;
    }
    
    /**
     * 基金信息
     */
    public static class FundInfo {
        private String code;
        private String name;
        private BigDecimal score;  // 综合得分
        
        public FundInfo() {
        }
        
        public FundInfo(String code, String name, BigDecimal score) {
            this.code = code;
            this.name = name;
            this.score = score;
        }
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public BigDecimal getScore() {
            return score;
        }
        
        public void setScore(BigDecimal score) {
            this.score = score;
        }
    }
}

