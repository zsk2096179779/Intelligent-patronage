package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

/**
 * 自定义风格因子混合表实体类
 * 
 * 存储风格因子与基础因子的组合关系和权重配置
 * 支持多个基础因子按权重组合生成风格因子
 * 
 * @author fund-advisor
 * @since 2025-11-19
 */
@Data
public class CustomStyleFactorMix {
    
    /**
     * 混合ID（主键）
     * 自增主键，唯一标识一个风格因子混合配置
     */
    private Integer mixid;
    
    /**
     * 风格因子ID
     * 关联到自定义风格因子表的ID
     * 指定该混合配置所属的风格因子
     */
    private Integer styleFactorId;
    
    /**
     * 基础因子ID
     * 关联到基础因子表或衍生因子表的ID
     * 指定参与混合的具体因子
     */
    private Integer factorId;
    
    /**
     * 权重
     * 基础因子在风格因子组合中的权重比例
     * 通常为0-1之间的小数，权重总和可能为1
     */
    private Double weight;
    
    /**
     * 是否归一化
     * true=已归一化，false=未归一化
     * 标识该权重是否已经过归一化处理
     * 归一化确保不同量级因子的可比性
     */
    private Boolean normalized;
}