package com.example.train_back.dto.subscription;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ConfirmRiskMismatchReq {

    private Boolean confirmed;
    private String confirmReason;
}
