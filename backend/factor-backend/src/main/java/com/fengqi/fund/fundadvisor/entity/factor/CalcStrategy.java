package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

/**
 * 计算策略表实体类
 * 
 * 存储衍生因子公式的聚合策略（如加权、等权等）
 * 支持系统内置策略和用户自定义策略扩展
 * 
 * @author fund-advisor
 * @since 2025-11-19
 */
@Data
public class CalcStrategy {
    
    /**
     * 计算策略ID（主键）
     * 自增主键，唯一标识一个计算策略
     */
    private Integer strategyId;
    
    /**
     * 策略名称
     * 如：加权求和、等权平均、市值加权
     * 用于界面显示和策略识别
     */
    private String strategyName;
    
    /**
     * 策略说明
     * 如：加权求和=Σ(基础因子值×权重)
     * 详细描述策略的计算逻辑和使用场景
     */
    private String strategyDesc;
    
    /**
     * 策略标识
     * 如：WEIGHTED_SUM，用于代码调用
     * 唯一的代码标识，避免中文字符带来的编码问题
     */
    private String strategyCode;
    
    /**
     * 是否自定义策略
     * 0=系统内置，1=用户拓展
     * 用于区分系统预定义策略和用户自定义策略
     */
    private Boolean isCustom;
    
    /**
     * 是否有效
     * 1=有效，0=失效
     * 用于策略的启用/禁用控制
     */
    private Boolean isValid;
}