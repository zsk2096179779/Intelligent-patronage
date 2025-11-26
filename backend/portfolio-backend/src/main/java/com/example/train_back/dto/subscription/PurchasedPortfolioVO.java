package com.example.train_back.dto.subscription;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 已购买的组合产品VO（包含组合详情和订阅信息）
 */
@Getter
@Setter
public class PurchasedPortfolioVO {
    
    // ========== 组合基本信息 ==========
    private Integer portfolioId;
    private String portfolioName;
    private String riskLevel;
    private String strategyType;
    private String strategyName;
    
    // ========== 组合收益指标 ==========
    private Double returnRate;        // 策略收益（%）
    private Double annualReturn;      // 年化收益（%）
    private Double maxDrawdown;       // 最大回撤（%）
    private Double sharpeRatio;       // 夏普比率
    private Double volatility;        // 波动率（%）
    private Double winRate;           // 胜率（%）
    
    // ========== 订阅信息 ==========
    private String orderNo;           // 订单号
    private BigDecimal subscriptionAmount;  // 订阅金额
    private LocalDateTime subscribedAt;      // 订阅时间（订单完成时间）
    private LocalDateTime createdAt;        // 订单创建时间
    
    // ========== 组合其他信息 ==========
    private String description;       // 策略描述
    private Double feeRate;          // 费率（%）
    private LocalDateTime portfolioCreateTime;  // 组合创立时间
}

