package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 衍生因子表实体类
 * 
 * 对应数据库表 derived_factors
 * 存储衍生因子与基础因子的关联关系和权重配置
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Data
public class DerivedFactor {
    
    /**
     * 主键ID
     */
    private Integer factorid;
    
    /**
     * 衍生因子ID
     */
    private Integer derivedId;
    
    /**
     * 基础因子ID
     */
    private Integer baseId;
    
    /**
     * 权重
     */
    private Double weight;
    
    /**
     * 权重说明
     * 如：用户自定义权重、系统默认等权
     */
    private String weightDesc;
    
    /**
     * 基础数据校验状态
     * UNCHECKED=未校验，PASSED=通过，FAILED=失败
     */
    private String baseDataCheckStatus;
    
    /**
     * 基础数据校验时间
     */
    private LocalDateTime checkTime;
    
    /**
     * 计算策略ID
     * 关联calc_strategies表，如加权求和、等权平均
     */
    private Integer calcStrategyId;
    
    /**
     * 公式组件备注
     * 如：基础因子PB需剔除负值后参与计算
     */
    private String formulaRemark;
    
    /**
     * 记录更新时间
     */
    private LocalDateTime updateTime;
}