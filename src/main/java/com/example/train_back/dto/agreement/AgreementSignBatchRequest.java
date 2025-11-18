package com.example.train_back.dto.agreement;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class AgreementSignBatchRequest {

    @Setter @Getter
    public static class AgreementItem {
        private Integer agreementId;
        private String agreementCode;
        private String agreementVersion;
        private String contentHash;
        private String signatureData;
    }

    private List<AgreementItem> agreements;
    private String scenario;
    private String relatedOrderNo;

}
