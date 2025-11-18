package com.example.train_back.dto.agreement;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AgreementListItemVO {

    private Integer id;
    private String agreementCode;
    private String agreementType;
    private String title;
    private String version;
    private String effectiveDate;
    private Boolean required;
    private Boolean signed;

    private String contentHash;
}
