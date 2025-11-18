package com.example.train_back.dto.agreement;

import java.util.List;

public class AgreementSignBatchResultVO {

    public static class SignRecord {
        private String signNo;
        private String agreementCode;
        private String agreementVersion;
        private String signedAt; // 用字符串返回，前端展示方便

        public String getSignNo() {
            return signNo;
        }

        public void setSignNo(String signNo) {
            this.signNo = signNo;
        }

        public String getAgreementCode() {
            return agreementCode;
        }

        public void setAgreementCode(String agreementCode) {
            this.agreementCode = agreementCode;
        }

        public String getAgreementVersion() {
            return agreementVersion;
        }

        public void setAgreementVersion(String agreementVersion) {
            this.agreementVersion = agreementVersion;
        }

        public String getSignedAt() {
            return signedAt;
        }

        public void setSignedAt(String signedAt) {
            this.signedAt = signedAt;
        }

        // getter / setter
    }

    private Integer signedCount;
    private List<SignRecord> signRecords;

    public Integer getSignedCount() {
        return signedCount;
    }

    public void setSignedCount(Integer signedCount) {
        this.signedCount = signedCount;
    }

    public List<SignRecord> getSignRecords() {
        return signRecords;
    }

    public void setSignRecords(List<SignRecord> signRecords) {
        this.signRecords = signRecords;
    }

    // getter / setter
}
