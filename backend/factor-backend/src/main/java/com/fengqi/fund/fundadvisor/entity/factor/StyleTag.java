package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 风格标签表实体类
 * 
 * 存储风格分类标签的定义和描述信息
 * 用于对衍生因子进行分类管理
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Data
public class StyleTag {
    
    /**
     * 风格标签ID（主键）
     * 自增主键，唯一标识一个风格标签
     */
    private Integer tagId;
    
    /**
     * 风格标签名称
     * 如：价值、成长、质量、低波动、动量
     * 标签的唯一名称，用于显示和识别
     */
    private String tagName;
    
    /**
     * 风格标签编码（唯一标识）
     * 如：VALUE、GROWTH、QUALITY、LOW_VOLATILITY、MOMENTUM
     * 系统内部使用的唯一标识符，避免中文字符问题
     */
    private String tagCode;
    
    /**
     * 标签描述
     * 详细说明该风格标签的定义、特征和投资意义
     * 如：价值风格标签，强调低估值、高分红的投资策略
     */
    private String description;
    
    /**
     * 创建人ID
     * 记录风格标签的创建者信息，预留权限关联
     */
    private Integer createUserId;
    
    /**
     * 创建时间
     * 记录风格标签的创建时间戳
     */
    private LocalDateTime createTime;
    
    /**
     * 是否有效
     * 1=有效，0=失效
     * 控制风格标签的启用状态和生命周期管理
     */
    private Integer isValid;
}


