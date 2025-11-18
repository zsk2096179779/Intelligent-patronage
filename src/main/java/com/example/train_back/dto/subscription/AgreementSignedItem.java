package com.example.train_back.dto.subscription;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter@Setter
public class AgreementSignedItem {
    private String code;
    private String signNo;
    private LocalDateTime signedAt;
}
