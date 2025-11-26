package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

/**
 * 基金因子映射表实体类
 * 
 * 存储基金与因子的关联关系
 * 支持JSON格式的因子列表存储
 * 
 * @author fund-advisor
 * @since 2025-11-19
 */
@Data
public class FundFactorMapping {
    
    /**
     * 映射关系ID（主键）
     * 自增主键，唯一标识一个基金因子映射关系
     */
    private Integer id;
    
    /**
     * 基金ID
     * 对应基金表中的基金代码或内部ID
     * 唯一标识一个基金产品
     */
    private String fundId;
    
    /**
     * 因子名称列表（JSON格式）
     * 存储该基金关联的所有因子名称或ID
     * 支持动态扩展，便于因子关联管理
     * 示例：["pe_factor","pb_factor","momentum_factor"]
     */
    private String factorNames;
}