package com.example.train_back.dto.subscription;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter@Setter
public class SubscriptionCreateReq {

    private Integer portfolioId;
    private BigDecimal subscriptionAmount;
    private String dividendMode;      // cash / reinvest
    private Boolean autoInvestEnabled;
    private String autoInvestPeriod;  // weekly / monthly
    private BigDecimal autoInvestAmount;
    private String fundSource;        // bank_card / balance（当前不落库，可选）
}
