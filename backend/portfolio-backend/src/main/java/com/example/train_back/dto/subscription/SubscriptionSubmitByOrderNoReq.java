package com.example.train_back.dto.subscription;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class SubscriptionSubmitByOrderNoReq {

    private Boolean riskMismatchConfirmed;
    private String paymentMethod;
    private String customerRemark;
}
