package com.example.train_back.dto;

/**
 * 基金筛选请求 DTO
 */
public class FundFilterRequest {

    private String keyword;
    private String fundType;
    private String category;
    private String operationCycle;
    private Double minFundSize;
    private Double maxFundSize;
    private Double minFeeRate;
    private Double maxFeeRate;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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

    public Double getMinFundSize() {
        return minFundSize;
    }

    public void setMinFundSize(Double minFundSize) {
        this.minFundSize = minFundSize;
    }

    public Double getMaxFundSize() {
        return maxFundSize;
    }

    public void setMaxFundSize(Double maxFundSize) {
        this.maxFundSize = maxFundSize;
    }

    public Double getMinFeeRate() {
        return minFeeRate;
    }

    public void setMinFeeRate(Double minFeeRate) {
        this.minFeeRate = minFeeRate;
    }

    public Double getMaxFeeRate() {
        return maxFeeRate;
    }

    public void setMaxFeeRate(Double maxFeeRate) {
        this.maxFeeRate = maxFeeRate;
    }

    /**
     * 是否包含除关键字之外的高级筛选项
     */
    public boolean hasAdvancedFilters() {
        return hasText(fundType)
                || hasText(category)
                || hasText(operationCycle)
                || minFundSize != null
                || maxFundSize != null
                || minFeeRate != null
                || maxFeeRate != null;
    }

    /**
     * 是否包含任意筛选条件（包含关键字）
     */
    public boolean hasAnyFilter() {
        return hasText(keyword) || hasAdvancedFilters();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

