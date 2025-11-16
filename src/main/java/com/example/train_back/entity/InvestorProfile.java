package com.example.train_back.entity;

import java.time.LocalDateTime;

public class InvestorProfile {

    private Long id;
    private Integer userId;
    private String otcAccountNo;
    private Integer otcStatus;           // 0-未开通，1-已开通
    private LocalDateTime otcOpenDate;

    private String realName;
    private String idCardNo;
    private String mobile;
    private String bankCardNo;
    private String bankName;

    private String riskLevel;
    private Integer riskScore;
    private LocalDateTime riskAssessmentDate;
    private LocalDateTime riskExpireDate;

    private String annualIncomeRange;
    private String totalAssetRange;
    private String investmentExperience;

    private Integer status;              // 0-禁用，1-正常
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getOtcAccountNo() {
        return otcAccountNo;
    }

    public void setOtcAccountNo(String otcAccountNo) {
        this.otcAccountNo = otcAccountNo;
    }

    public Integer getOtcStatus() {
        return otcStatus;
    }

    public void setOtcStatus(Integer otcStatus) {
        this.otcStatus = otcStatus;
    }

    public LocalDateTime getOtcOpenDate() {
        return otcOpenDate;
    }

    public void setOtcOpenDate(LocalDateTime otcOpenDate) {
        this.otcOpenDate = otcOpenDate;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getBankCardNo() {
        return bankCardNo;
    }

    public void setBankCardNo(String bankCardNo) {
        this.bankCardNo = bankCardNo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
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

    public String getAnnualIncomeRange() {
        return annualIncomeRange;
    }

    public void setAnnualIncomeRange(String annualIncomeRange) {
        this.annualIncomeRange = annualIncomeRange;
    }

    public String getTotalAssetRange() {
        return totalAssetRange;
    }

    public void setTotalAssetRange(String totalAssetRange) {
        this.totalAssetRange = totalAssetRange;
    }

    public String getInvestmentExperience() {
        return investmentExperience;
    }

    public void setInvestmentExperience(String investmentExperience) {
        this.investmentExperience = investmentExperience;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
