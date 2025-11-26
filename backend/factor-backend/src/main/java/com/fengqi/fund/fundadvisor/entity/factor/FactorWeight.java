package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 因子权重实体类
 * 
 * 用于管理衍生因子创建时各基础因子的权重配置
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
@Data
public class FactorWeight {
    
    /**
     * 权重配置ID
     */
    private Integer weightId;
    
    /**
     * 衍生因子ID（关联到factor_base表）
     */
    private Integer derivedFactorId;
    
    /**
     * 基础因子ID
     */
    private Integer baseFactorId;
    
    /**
     * 权重值（0-1之间的小数）
     */
    private Double weight;
    
    /**
     * 权重百分比（便于前端显示，0-100）
     */
    private Double weightPercentage;
    
    /**
     * 是否启用（1启用，0禁用）
     */
    private Integer isEnabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建者
     */
    private String creator;
    
    /**
     * 更新者
     */
    private String updater;
    
    /**
     * 备注
     */
    private String remark;
}