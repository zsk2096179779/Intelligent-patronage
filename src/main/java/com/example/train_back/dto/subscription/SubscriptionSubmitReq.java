package com.example.train_back.dto.subscription;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class SubscriptionSubmitReq {

    private Boolean riskMismatchConfirmed;
    private String paymentMethod;   // bank_transfer / alipay / wechat / balance
    private String customerRemark;
}
