package com.example.train_back.dto;

import java.time.LocalDateTime;

public class RiskStatusResp {

    private boolean hasAssessment;        // 是否有记录
    private String riskLevel;            // 用户当前风险等级
    private LocalDateTime assessmentDate;
    private LocalDateTime expireDate;
    private boolean expired;             // 是否已过期
    private Integer daysUntilExpire;     // 距离过期还有多少天（已过期为0或负数）

    // ===== getter / setter =====
    public boolean isHasAssessment() {
        return hasAssessment;
    }

    public void setHasAssessment(boolean hasAssessment) {
        this.hasAssessment = hasAssessment;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public LocalDateTime getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDateTime assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public LocalDateTime getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDateTime expireDate) {
        this.expireDate = expireDate;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public Integer getDaysUntilExpire() {
        return daysUntilExpire;
    }

    public void setDaysUntilExpire(Integer daysUntilExpire) {
        this.daysUntilExpire = daysUntilExpire;
    }
}
