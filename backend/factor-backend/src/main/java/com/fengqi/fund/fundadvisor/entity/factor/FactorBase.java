package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 基础因子表实体类
 * 
 * 存储系统内置基础因子及固定公式
 * 支持数据来源管理和更新频率配置
 * 
 * @author fund-advisor
 * @since 2025-11-19
 */
@Data
public class FactorBase {
    
    /**
     * 基础因子ID（主键）
     * 自增主键，唯一标识一个基础因子
     */
    private Integer baseId;
    
    /**
     * 基础因子名称
     * 如：市盈率TTM、市净率、换手率
     * 基础因子的完整名称，包含计量单位说明
     */
    private String factorName;
    
    /**
     * 基础因子编码（唯一标识）
     * 如：PE_TTM、PB、TURNOVER_RATE
     * 系统内部使用的唯一标识符，避免中文字符问题
     */
    private String factorCode;
    
    /**
     * 基础因子固定计算公式
     * 如：股价/过去12个月每股收益
     * 详细描述基础因子的计算逻辑和数据处理步骤
     */
    private String factorFormula;
    
    /**
     * 数据来源
     * 如：Wind、Tushare、 Bloomberg、聚源
     * 指定基础因子的数据提供商和获取方式
     */
    private String dataSource;
    
    /**
     * 更新频率
     * 日度/周度，支撑数据更新监控
     * 定义基础因子数据的预期更新周期
     */
    private String updateFrequency;
    
    /**
     * 数据起始日期
     * 记录该因子历史数据的最早可用日期
     */
    private LocalDate dataStartDate;
    
    /**
     * 最新数据日期
     * 记录该因子历史数据的最新可用日期
     * 用于监控数据更新情况和质量检查
     */
    private LocalDate latestDataDate;
    
    /**
     * 因子说明
     * 如：反映公司估值水平
     * 详细说明因子的经济学含义、投资意义和适用场景
     */
    private String dataDesc;
    
    /**
     * 是否有效
     * 1=有效，0=失效
     * 控制基础因子的启用状态，支持因子版本管理
     */
    private Boolean isValid;
    
    /**
     * 创建时间
     * 记录基础因子的创建时间戳
     */
    private LocalDateTime createTime;
}