package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

/**
 * 分类因子表实体类
 * 
 * 存储因子分类信息，支持按业务场景对因子进行分组管理
 * 使用JSON格式存储因子列表，便于灵活扩展
 * 
 * @author fund-advisor
 * @since 2025-11-19
 */
@Data
public class CategoryFactor {
    
    /**
     * 分类ID（主键）
     * 自增主键，唯一标识一个因子分类
     */
    private Integer categoryId;
    
    /**
     * 分类名称
     * 如：估值类因子、成长类因子、质量类因子
     * 用于界面的分类展示和搜索
     */
    private String name;
    
    /**
     * 分类描述
     * 详细说明该分类的特征、适用场景和包含的因子类型
     */
    private String description;
    
    /**
     * 因子列表（JSON格式）
     * 存储该分类下包含的因子ID列表
     * 支持动态扩展，便于因子分类的灵活管理
     * 示例：[1,2,3,4] 或 {"factors":[1,2,3],"count":3}
     */
    private String factorList;
}