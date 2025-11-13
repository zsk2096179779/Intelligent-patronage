package com.example.train_back.entity;

import java.time.LocalDateTime;

/**
 * 策略组合实体类
 */
public class StrategyCombination {
    /**
     * 组合ID（主键，自增）
     */
    private Integer id;
    
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
     * 组合简介
     */
    private String summary;
    
    /**
     * 目标客户
     */
    private String targetInvestor;
    
    /**
     * 相对应的策略ID（外键，关联strategies表）
     */
    private Integer strategyId;
    
    /**
     * 是否上架（0-未上架/未审核，1-已上架/已审核，-1-已拒绝）
     */
    private Integer listed;
    
    /**
     * 状态（draft-草稿，pending_review-待审核，approved-已通过，rejected-已拒绝）
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    public StrategyCombination() {
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @Override
    public String toString() {
        return "StrategyCombination{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", riskLevel='" + riskLevel + '\'' +
                ", strategyType='" + strategyType + '\'' +
                ", strategyId=" + strategyId +
                ", listed=" + listed +
                '}';
    }
}

