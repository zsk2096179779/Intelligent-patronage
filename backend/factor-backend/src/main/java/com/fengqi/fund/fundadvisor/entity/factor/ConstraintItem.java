package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

/**
 * 约束项表实体类
 * 
 * 存储因子筛选和约束的具体规则项
 * 支持多种约束条件的组合配置
 * 
 * @author fund-advisor
 * @since 2025-11-19
 */
@Data
public class ConstraintItem {
    
    /**
     * 约束项ID（主键）
     * 自增主键，唯一标识一个约束项
     */
    private Integer itemid;
    
    /**
     * 约束组ID
     * 关联到约束组表，用于约束项的分组管理
     * 同一组的约束项按照指定关系组合执行
     */
    private Integer groupId;
    
    /**
     * 约束类型
     * 如：RANGE（范围）、COMPARE（比较）、EXCLUDE（排除）
     * 定义约束项的基本分类和验证方式
     */
    private String type;
    
    /**
     * 参数名称
     * 如：pe_ratio、market_cap、turnover_rate
     * 指定需要约束的因子参数或指标
     */
    private String paramName;
    
    /**
     * 操作符
     * 如：>、<、>=、<=、=、!=、BETWEEN、IN、NOT_IN
     * 定义参数值与阈值之间的比较关系
     */
    private String operator;
    
    /**
     * 阈值
     * 约束的具体数值边界
     * 可以是单个值或范围值，根据操作符类型而定
     */
    private Double threshold;
    
    /**
     * 条件关系
     * 如：AND、OR、NOT
     * 定义该约束项与组内其他约束项的逻辑关系
     * 用于构建复杂的约束条件组合
     */
    private String conditionRelation;
}