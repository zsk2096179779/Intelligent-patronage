package com.example.train_back.dto.subscription;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class SubscriptionOrderListItemVO {

    private String orderNo;
    private Integer portfolioId;
    private String portfolioName;

    private BigDecimal subscriptionAmount;

    private String orderStatus;
    private String paymentStatus;
    private String auditStatus;

    private String createdAt;
    private String submittedAt;
    private String paidAt;
    private String completedAt;
}
