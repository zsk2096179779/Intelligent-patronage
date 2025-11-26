package com.example.train_back.dto.subscription;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter@Setter
public class SubscriptionSubmitResp {

    private String orderNo;
    private String orderStatus;
    private String auditStatus;
    private LocalDateTime submittedAt;

    private String nextStep;          // e.g. "AUDIT"
    private String estimatedAuditTime;// e.g. "2个工作日内"
}
