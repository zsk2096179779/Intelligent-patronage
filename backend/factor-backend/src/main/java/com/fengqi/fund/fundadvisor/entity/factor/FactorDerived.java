package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 衍生因子基本信息表实体类
 * 
 * 对应数据库表 factor_derived
 * 存储用户创建的衍生因子核心信息
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Data
public class FactorDerived {
    
    /**
     * 衍生因子ID（主键）
     */
    private Integer derivedId;
    
    /**
     * 衍生因子名称
     * 用户自定义，如：估值综合因子
     */
    private String factorName;
    
    /**
     * 衍生因子编码（唯一标识）
     * 如：VAL_COM、GRO_COMP
     */
    private String factorCode;
    
    /**
     * 衍生因子描述
     * 用户自定义，如：PE与PB加权组合
     */
    private String factorDesc;
    
    /**
     * 计算策略ID
     * 关联calc_strategies表，如加权求和
     */
    private Integer calcStrategyId;
    
    /**
     * 关联风格标签ID
     * 多个用逗号分隔，如：价值型=1
     */
    private String styleTagIds;
    
    /**
     * 所属因子树节点ID
     * 关联factor_tree表，支撑自动挂树
     */
    private Integer treeNodeId;
    
    /**
     * 创建人ID
     */
    private Integer createUserId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 是否有效
     * 1=有效，0=失效
     */
    private Boolean isValid;
}