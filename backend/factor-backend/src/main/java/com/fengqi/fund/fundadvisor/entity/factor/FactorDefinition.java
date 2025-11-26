package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

/**
 * 因子定义表实体类
 * 
 * 存储因子的基本定义和配置信息
 * 支持多种因子类型和数据格式管理
 * 
 * @author fund-advisor
 * @since 2025-11-19
 */
@Data
public class FactorDefinition {
    
    /**
     * 定义ID（主键）
     * 自增主键，唯一标识一个因子定义
     */
    private Integer definitionid;
    
    /**
     * 因子名称
     * 系统内部使用的唯一因子标识符
     * 如：pe_ratio_ttm、pb_ratio
     */
    private String name;
    
    /**
     * 显示名称
     * 用户界面显示的友好名称，支持中文
     * 如：市盈率TTM、市净率
     */
    private String displayName;
    
    /**
     * 因子类型
     * 如：VALUE（价值）、GROWTH（成长）、QUALITY（质量）
     * 定义因子的投资风格分类
     */
    private String factorType;
    
    /**
     * 数据类型
     * 如：NUMERIC（数值）、RATIO（比率）、PERCENTAGE（百分比）
     * 定义因子的数据格式和存储类型
     */
    private String dataType;
    
    /**
     * 计算方法
     * 描述因子的计算逻辑和算法
     * 如：市值加权、等权平均、时间加权
     */
    private String calcMethod;
    
    /**
     * 更新频率
     * 如：DAILY（日度）、WEEKLY（周度）、MONTHLY（月度）
     * 定义因子数据的更新周期
     */
    private String updateFrequency;
    
    /**
     * 是否启用
     * true=启用，false=禁用
     * 控制因子的使用状态，支持因子版本管理
     */
    private Boolean enabled;
}