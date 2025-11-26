package com.example.train_back.dto.subscription;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter@Setter
public class SubscriptionSignatureResp {
    private String orderNo;
    // 如果你把图片存 OSS/CDN，可以返回 URL；现在可以先返回原始 DataURL
    private String signatureUrl;
    private LocalDateTime signedAt;
}
