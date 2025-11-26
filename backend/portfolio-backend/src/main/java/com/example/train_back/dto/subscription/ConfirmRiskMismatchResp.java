package com.example.train_back.dto.subscription;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter@Setter
public class ConfirmRiskMismatchResp {

    private String orderNo;
    private Boolean riskMismatchConfirmed;
    private LocalDateTime confirmedAt;

    private Boolean requiresAdditionalAgreement;
    private String additionalAgreementCode;
    private String additionalAgreementTitle;
}
