package com.example.train_back.dto.subscription;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter@Setter
public class SubscriptionCreateResp {

    private String orderNo;
    private Long orderId;

    private Integer portfolioId;
    private String portfolioName;

    private BigDecimal subscriptionAmount;
    private BigDecimal feeAmount;

    private String orderStatus;
    private LocalDateTime createdAt;
}
