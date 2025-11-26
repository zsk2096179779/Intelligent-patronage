package com.example.backend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FactorBase {
    private Integer baseId;  // 基础因子ID
    private String factorName;  // 基础因子名称
    private String factorCode;  // 基础因子编码（唯一标识）
    private String factorFormula;  // 基础因子固定计算公式
    private String dataSource;  // 数据来源
    private String updateFrequency;  // 更新频率（日度/周度）
    private LocalDate dataStartDate;  // 数据起始日期
    private LocalDate latestDataDate;  // 最新数据日期
    private String dataDesc;  // 因子说明
    private Boolean isValid;  // 是否有效（1=有效，0=失效）
    private LocalDateTime createTime;  // 创建时间

    public Integer getBaseId() {
        return baseId;
    }

    public void setBaseId(Integer baseId) {
        this.baseId = baseId;
    }

    public String getFactorName() {
        return factorName;
    }

    public void setFactorName(String factorName) {
        this.factorName = factorName;
    }

    public String getFactorCode() {
        return factorCode;
    }

    public void setFactorCode(String factorCode) {
        this.factorCode = factorCode;
    }

    public String getFactorFormula() {
        return factorFormula;
    }

    public void setFactorFormula(String factorFormula) {
        this.factorFormula = factorFormula;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getUpdateFrequency() {
        return updateFrequency;
    }

    public void setUpdateFrequency(String updateFrequency) {
        this.updateFrequency = updateFrequency;
    }

    public LocalDate getDataStartDate() {
        return dataStartDate;
    }

    public void setDataStartDate(LocalDate dataStartDate) {
        this.dataStartDate = dataStartDate;
    }

    public LocalDate getLatestDataDate() {
        return latestDataDate;
    }

    public void setLatestDataDate(LocalDate latestDataDate) {
        this.latestDataDate = latestDataDate;
    }

    public String getDataDesc() {
        return dataDesc;
    }

    public void setDataDesc(String dataDesc) {
        this.dataDesc = dataDesc;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

