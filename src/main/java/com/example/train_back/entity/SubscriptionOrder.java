package com.example.train_back.entity;



import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter@Getter
public class SubscriptionOrder {

    private Long id;
    private String orderNo;
    private Integer userId;

    private Integer portfolioId;
    private String portfolioName;

    private BigDecimal subscriptionAmount;
    private BigDecimal actualAmount;
    private BigDecimal feeAmount;

    private String dividendMode;
    private Integer autoInvestEnabled;   // 0/1
    private String autoInvestPeriod;
    private BigDecimal autoInvestAmount;

    private String userRiskLevel;
    private String productRiskLevel;
    private Integer riskMatched;               // 0/1
    private Integer riskMismatchConfirmed;     // 0/1

    private String agreementsSigned;           // JSON 字符串
    private Integer allAgreementsSigned;       // 0/1

    private String signatureData;
    private String signatureIp;
    private LocalDateTime signedAt;

    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paidAt;
    private String paymentChannelNo;

    private String orderStatus;    // draft/pending/approved/rejected/paid/completed/cancelled
    private String auditStatus;    // pending/approved/rejected
    private Integer auditorId;
    private String auditRemark;
    private LocalDateTime auditedAt;

    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    private String subscribeChannel;
    private String customerRemark;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
