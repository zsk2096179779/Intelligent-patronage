package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

/**
 * 因子树表实体类
 * 
 * 存储因子和分类的树形层次结构
 * 支持多场景管理和无限层级扩展
 * 
 * @author fund-advisor
 * @since 2025-11-19
 */
@Data
public class FactorTree {
    
    /**
     * 树节点ID（主键）
     * 自增主键，唯一标识一个树节点
     */
    private Integer treeid;
    
    /**
     * 父节点ID
     * 父节点的treeid，根节点时为null
     * 构建树形结构的父子关系
     */
    private Integer parentId;
    
    /**
     * 节点名称
     * 分类节点：如估值类因子、成长类因子
     * 因子节点：如PE因子、PB因子
     */
    private String nodeName;
    
    /**
     * 节点类型
     * TREE=树根节点，CATEGORY=分类节点，FACTOR=因子节点
     * 区分节点的不同用途和展示方式
     */
    private String nodeType;
    
    /**
     * 关联因子ID
     * 当nodeType=FACTOR时，关联到具体因子的ID
     * null时表示该节点为分类节点
     */
    private Integer factorId;
    
    /**
     * 是否叶子节点
     * true=叶子节点（有因子），false=非叶子节点（只有分类）
     * 用于树形结构的快速判断和渲染
     */
    private Boolean isLeaf;
    
    /**
     * 排序号
     * 同级节点下的显示顺序，数值越小越靠前
     * 支持拖拽排序和自定义顺序
     */
    private Integer sortOrder;
    
    /**
     * 节点描述
     * 分类节点：描述该分类的特征和包含的因子类型
     * 因子节点：描述因子的定义和计算方法
     */
    private String description;
    
    /**
     * 场景ID
     * 关联到factor_tree_scene表的scene_id
     * 支持因子树的多场景管理和隔离
     * 如：EQUITY=权益场景，FIXED_INCOME=固收场景
     */
    private String sceneId;
}