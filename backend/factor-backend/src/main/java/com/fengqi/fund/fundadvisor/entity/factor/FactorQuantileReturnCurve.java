package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 分位数累计收益率曲线实体类
 */
@Data
public class FactorQuantileReturnCurve {
    
    /**
     * 曲线ID
     */
    private Integer curveId;
    
    /**
     * 关联任务ID
     */
    private Integer taskId;
    
    /**
     * 因子ID
     */
    private Integer factorId;
    
    /**
     * 因子编码
     */
    private String factorCode;
    
    /**
     * 因子名称
     */
    private String factorName;
    
    /**
     * 日期列表（JSON格式）
     */
    private String datesJson;
    
    /**
     * 各分位数累计收益率序列（JSON格式）
     */
    private String quantileReturnsJson;
    
    /**
     * 分位数描述（JSON格式）
     */
    private String quantileDescriptionsJson;
    
    /**
     * 计算日期
     */
    private LocalDate calculationDate;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

