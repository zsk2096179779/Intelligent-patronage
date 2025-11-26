package com.example.train_back.dto;

public class OtcOpenReq {

    /**
     * 客户姓名
     */
    private String realName;

    /**
     * 证件号码
     */
    private String idCardNo;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    /**
     * 开户行名称
     */
    private String bankName;

    /**
     * 短信验证码（先占位，后续你可以接短信服务）
     */
    private String smsCode;

    // ===== getter / setter =====

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

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }
}
