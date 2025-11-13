package com.example.train_back.entity;

import java.time.LocalDate;

/**
 * 基金实体类
 */
public class Fund {
    /**
     * 基金代码（主键）
     */
    private String fundCode;
    
    /**
     * 基金名称
     */
    private String fundName;
    
    /**
     * 基金描述
     */
    private String fundDescription;
    
    /**
     * 基金经理ID
     */
    private Integer managerId;
    
    /**
     * 基金公司ID
     */
    private Integer companyId;
    
    /**
     * 基金类型
     */
    private String fundType;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 运作周期
     */
    private String operationCycle;
    
    /**
     * 基金规模
     */
    private Double fundSize;
    
    /**
     * 成立日期
     */
    private LocalDate inceptionDate;
    
    /**
     * 费率
     */
    private Double feeRate;
    
    /**
     * 股票资产
     */
    private Double stockAsset;
    
    /**
     * 现金资产
     */
    private Double cashAsset;
    
    /**
     * 债券资产
     */
    private Double bondAsset;
    
    /**
     * 存款资产
     */
    private Double depositAsset;
    
    /**
     * 比例
     */
    private Double proportion;

    public Fund() {
    }

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public String getFundDescription() {
        return fundDescription;
    }

    public void setFundDescription(String fundDescription) {
        this.fundDescription = fundDescription;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getFundType() {
        return fundType;
    }

    public void setFundType(String fundType) {
        this.fundType = fundType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOperationCycle() {
        return operationCycle;
    }

    public void setOperationCycle(String operationCycle) {
        this.operationCycle = operationCycle;
    }

    public Double getFundSize() {
        return fundSize;
    }

    public void setFundSize(Double fundSize) {
        this.fundSize = fundSize;
    }

    public LocalDate getInceptionDate() {
        return inceptionDate;
    }

    public void setInceptionDate(LocalDate inceptionDate) {
        this.inceptionDate = inceptionDate;
    }

    public Double getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(Double feeRate) {
        this.feeRate = feeRate;
    }

    public Double getStockAsset() {
        return stockAsset;
    }

    public void setStockAsset(Double stockAsset) {
        this.stockAsset = stockAsset;
    }

    public Double getCashAsset() {
        return cashAsset;
    }

    public void setCashAsset(Double cashAsset) {
        this.cashAsset = cashAsset;
    }

    public Double getBondAsset() {
        return bondAsset;
    }

    public void setBondAsset(Double bondAsset) {
        this.bondAsset = bondAsset;
    }

    public Double getDepositAsset() {
        return depositAsset;
    }

    public void setDepositAsset(Double depositAsset) {
        this.depositAsset = depositAsset;
    }

    public Double getProportion() {
        return proportion;
    }

    public void setProportion(Double proportion) {
        this.proportion = proportion;
    }
}

