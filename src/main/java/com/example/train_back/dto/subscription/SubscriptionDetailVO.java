package com.example.train_back.dto.subscription;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter@Getter
public class SubscriptionDetailVO {

    private String orderNo;
    private Long orderId;
    private Integer userId;

    private Integer portfolioId;
    private String portfolioName;
    private String portfolioRiskLevel;  // 冗余展示

    private BigDecimal subscriptionAmount;
    private BigDecimal feeAmount;
    private BigDecimal feeRate;
    private BigDecimal actualAmount;

    private String dividendMode;
    private Boolean autoInvestEnabled;
    private String autoInvestPeriod;
    private BigDecimal autoInvestAmount;

    private String userRiskLevel;
    private String productRiskLevel;
    private Boolean riskMatched;

    private List<AgreementSignedItem> agreementsSigned;
    private Boolean allAgreementsSigned;

    private String signatureData;

    private String orderStatus;
    private String paymentStatus;
    private String auditStatus;

    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime paidAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
}
