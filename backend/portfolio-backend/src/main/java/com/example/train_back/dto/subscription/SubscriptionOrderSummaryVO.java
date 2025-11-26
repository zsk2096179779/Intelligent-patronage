package com.example.train_back.dto.subscription;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter@Setter
public class SubscriptionOrderSummaryVO {

    private String orderNo;
    private Integer portfolioId;
    private String portfolioName;

    private BigDecimal subscriptionAmount;

    private String orderStatus;
    private String paymentStatus;
    private String auditStatus;

    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime paidAt;
    private LocalDateTime completedAt;
}
