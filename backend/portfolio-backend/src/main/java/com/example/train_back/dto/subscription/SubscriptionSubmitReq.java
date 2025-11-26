package com.example.train_back.dto.subscription;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter@Setter
public class SubscriptionSubmitReq {

    private Integer portfolioId;
    private BigDecimal subscriptionAmount;
    private String dividendMode;
    private Boolean autoInvestEnabled;
    private String autoInvestPeriod;
    private BigDecimal autoInvestAmount;

    private Boolean riskMismatchConfirmed;
    private String paymentMethod;   // bank_transfer / alipay / wechat / balance
    private String customerRemark;
}
