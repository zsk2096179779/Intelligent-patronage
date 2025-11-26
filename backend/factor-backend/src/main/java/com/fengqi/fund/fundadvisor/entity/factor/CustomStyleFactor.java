package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 自定义风格因子表实体类
 * 
 * 存储用户创建的风格因子定义和配置信息
 * 支持多种计算方法和更新频率配置
 * 
 * @author fund-advisor
 * @since 2025-11-19
 */
@Data
public class CustomStyleFactor {
    
    /**
     * 风格因子ID（主键）
     * 自增主键，唯一标识一个自定义风格因子
     */
    private Integer styleid;
    
    /**
     * 风格因子名称
     * 如：value_factor、growth_factor、momentum_factor
     * 系统内部使用的唯一标识名称
     */
    private String name;
    
    /**
     * 显示名称
     * 如：价值因子、成长因子、动量因子
     * 用户界面显示的友好名称，支持中文
     */
    private String displayName;
    
    /**
     * 描述信息
     * 详细说明风格因子的定义、计算逻辑和投资意义
     */
    private String description;
    
    /**
     * 计算方法
     * 如：PE和PB的加权平均、近期收益率的标准差
     * 描述风格因子的具体计算公式和算法
     */
    private String calcMethod;
    
    /**
     * 创建时间
     * 记录风格因子的创建时间戳
     */
    private LocalDateTime createTime;
    
    /**
     * 更新频率
     * 如：daily（日度）、weekly（周度）、monthly（月度）
     * 定义风格因子的数据更新周期
     */
    private String updateFrequency;
    
    /**
     * 是否启用
     * true=启用，false=禁用
     * 控制风格因子的使用状态
     */
    private Boolean enabled;
    
    /**
     * 风格标签
     * 如：价值、成长、质量、低波动
     * 用于风格分类和筛选的标签标识
     */
    private String styleTag;
}