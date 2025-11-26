package com.example.train_back.dto;

import java.math.BigDecimal;

/**
 * 持仓DTO
 */
public class HoldingDTO {
    /**
     * 持仓ID（更新时需要）
     */
    private Integer id;
    
    /**
     * 基金/证券代码
     */
    private String fundCode;
    
    /**
     * 基金/证券名称
     */
    private String fundName;
    
    /**
     * 目标权重（%）
     */
    private BigDecimal weight;
    
    /**
     * 备注
     */
    private String remark;

    public HoldingDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

