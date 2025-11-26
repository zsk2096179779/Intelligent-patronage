package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

/**
 * 因子树场景表实体类
 * 
 * 存储因子树的应用场景信息
 * 支持多场景因子树的隔离管理
 * 
 * @author fund-advisor
 * @since 2025-11-19
 */
@Data
public class FactorTreeScene {
    
    /**
     * 场景ID（主键）
     * 如：EQUITY、FIXED_INCOME、ALTERNATIVE
     * 唯一标识一个应用场景的代码
     */
    private String sceneId;
    
    /**
     * 场景名称
     * 如：权益投资、固收投资、另类投资
     * 用户界面显示的友好名称
     */
    private String sceneName;
    
    /**
     * 场景描述
     * 如：用于权益类FOF产品的因子筛选、回测
     * 详细说明场景的适用范围和业务用途
     */
    private String sceneDesc;
}