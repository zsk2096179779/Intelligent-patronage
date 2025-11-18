package com.example.train_back.dto.subscription;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter@Setter
public class SubscriptionUpdateReq {

    private String dividendMode;
    private Boolean autoInvestEnabled;
    private String autoInvestPeriod;
    private BigDecimal autoInvestAmount;
    private String customerRemark;
}
