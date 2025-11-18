package com.example.train_back.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 策略组合实体类
 */
@Setter
@Getter
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
     * 是否上架（0-未上架/未审核，签约业务模块设计.md-已上架/已审核，-签约业务模块设计.md-已拒绝）
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

    private BigDecimal feeRate;

    public StrategyCombination() {
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

