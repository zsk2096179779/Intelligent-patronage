package com.example.train_back.dto.subscription;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class SubscriptionSignatureReq {
    // 前端传 data:image/png;base64,... 的整串
    private String signatureData;
}
