package com.example.train_back.dto;

/**
 * 策略列表DTO（用于下拉框选择）
 */
public class StrategyListDTO {
    /**
     * 策略ID
     */
    private Integer strategyId;
    
    /**
     * 策略名称
     */
    private String strategyName;
    
    /**
     * 策略类型（可选，用于展示）
     */
    private String strategyType;
    
    /**
     * 策略描述（可选）
     */
    private String description;
    
    /**
     * 策略引用ID（用于组合关联的ID）
     */
    private Integer strategyRefId;

    public StrategyListDTO() {
    }

    public StrategyListDTO(Integer strategyId, String strategyName, String strategyType, String description, Integer strategyRefId) {
        this.strategyId = strategyId;
        this.strategyName = strategyName;
        this.strategyType = strategyType;
        this.description = description;
        this.strategyRefId = strategyRefId;
    }

    public Integer getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Integer strategyId) {
        this.strategyId = strategyId;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public String getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(String strategyType) {
        this.strategyType = strategyType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStrategyRefId() {
        return strategyRefId;
    }

    public void setStrategyRefId(Integer strategyRefId) {
        this.strategyRefId = strategyRefId;
    }

    @Override
    public String toString() {
        return "StrategyListDTO{" +
                "strategyId=" + strategyId +
                ", strategyName='" + strategyName + '\'' +
                ", strategyType='" + strategyType + '\'' +
                ", description='" + description + '\'' +
                ", strategyRefId=" + strategyRefId +
                '}';
    }
}

