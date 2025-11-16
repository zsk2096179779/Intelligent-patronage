package com.example.train_back.dto;

import java.time.LocalDateTime;

public class InvestorProfileResp {

    // 基本信息（脱敏展示就交给前端）
    private String realName;
    private String mobile;
    private String bankName;
    private String bankCardNo;

    // OTC 信息
    private Integer otcStatus;       // 0-未开通，1-已开通
    private String otcAccountNo;
    private LocalDateTime otcOpenDate;

    // 风险测评信息
    private String riskLevel;
    private Integer riskScore;
    private LocalDateTime riskAssessmentDate;
    private LocalDateTime riskExpireDate;
    private Boolean riskExpired;     // 是否已过期

    // ====== getter / setter（IDEA 自动生成即可） ======
    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankCardNo() {
        return bankCardNo;
    }

    public void setBankCardNo(String bankCardNo) {
        this.bankCardNo = bankCardNo;
    }

    public Integer getOtcStatus() {
        return otcStatus;
    }

    public void setOtcStatus(Integer otcStatus) {
        this.otcStatus = otcStatus;
    }

    public String getOtcAccountNo() {
        return otcAccountNo;
    }

    public void setOtcAccountNo(String otcAccountNo) {
        this.otcAccountNo = otcAccountNo;
    }

    public LocalDateTime getOtcOpenDate() {
        return otcOpenDate;
    }

    public void setOtcOpenDate(LocalDateTime otcOpenDate) {
        this.otcOpenDate = otcOpenDate;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public LocalDateTime getRiskAssessmentDate() {
        return riskAssessmentDate;
    }

    public void setRiskAssessmentDate(LocalDateTime riskAssessmentDate) {
        this.riskAssessmentDate = riskAssessmentDate;
    }

    public LocalDateTime getRiskExpireDate() {
        return riskExpireDate;
    }

    public void setRiskExpireDate(LocalDateTime riskExpireDate) {
        this.riskExpireDate = riskExpireDate;
    }

    public Boolean getRiskExpired() {
        return riskExpired;
    }

    public void setRiskExpired(Boolean riskExpired) {
        this.riskExpired = riskExpired;
    }
}
