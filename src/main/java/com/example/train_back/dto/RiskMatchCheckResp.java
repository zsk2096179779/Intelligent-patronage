package com.example.train_back.dto;

public class RiskMatchCheckResp {

    private boolean matched;           // 是否匹配
    private String userRiskLevel;      // 用户风险等级（C1~C5）
    private String productRiskLevel;   // 产品风险等级（低/中低/中/中高/高）
    private Integer productRiskLevelNum; // 产品风险等级数值 1~5
    private boolean canPurchase;       // 是否可以直接购买
    private String warningMessage;     // 提示文案（不匹配时）
    private boolean needConfirm;       // 是否需要“不匹配确认”

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public String getUserRiskLevel() {
        return userRiskLevel;
    }

    public void setUserRiskLevel(String userRiskLevel) {
        this.userRiskLevel = userRiskLevel;
    }

    public String getProductRiskLevel() {
        return productRiskLevel;
    }

    public void setProductRiskLevel(String productRiskLevel) {
        this.productRiskLevel = productRiskLevel;
    }

    public Integer getProductRiskLevelNum() {
        return productRiskLevelNum;
    }

    public void setProductRiskLevelNum(Integer productRiskLevelNum) {
        this.productRiskLevelNum = productRiskLevelNum;
    }

    public boolean isCanPurchase() {
        return canPurchase;
    }

    public void setCanPurchase(boolean canPurchase) {
        this.canPurchase = canPurchase;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public boolean isNeedConfirm() {
        return needConfirm;
    }

    public void setNeedConfirm(boolean needConfirm) {
        this.needConfirm = needConfirm;
    }
}
