package com.fengqi.fund.fundadvisor.entity.factor;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 因子脚本表实体类
 * 
 * 存储自定义Python脚本及脚本生成的因子公式
 * 支持脚本状态监控和执行日志管理
 * 
 * @author fund-advisor
 * @since 2025-11-19
 */
@Data
public class FactorScript {
    
    /**
     * 脚本ID（主键）
     * 自增主键，唯一标识一个因子脚本
     */
    private Integer scriptId;
    
    /**
     * 脚本名称
     * 如：自定义动量因子脚本、价值因子计算脚本
     * 脚本的易读名称，便于用户识别和管理
     */
    private String scriptName;
    
    /**
     * 脚本存储路径
     * 如：/scripts/momentum.py
     * 脚本文件在服务器上的绝对存储路径
     */
    private String scriptFilePath;
    
    /**
     * 脚本公式描述
     * 文字说明计算逻辑，如：动量=12月收益率-1月收益率
     * 详细描述脚本的计算原理和输出结果说明
     */
    private String scriptFormulaDesc;
    
    /**
     * 脚本状态
     * INIT=初始，RUNNING=运行中，SUCCESS=成功，FAILED=失败
     * 用于脚本的执行状态跟踪和监控
     */
    private String scriptStatus;
    
    /**
     * 上次运行时间
     * 记录脚本最后一次执行的时间戳
     */
    private LocalDateTime lastRunTime;
    
    /**
     * 上次运行日志
     * 记录脚本最后一次执行的详细日志信息
     * 包含执行结果、错误信息、性能数据等
     */
    private String lastRunLog;
    
    /**
     * 输出衍生因子ID
     * 多个用逗号分隔，关联factor_derived表
     * 记录脚本成功执行后生成的衍生因子列表
     */
    private String outputDerivedIds;
    
    /**
     * 上传人ID
     * 预留权限关联，记录脚本上传者信息
     */
    private Integer createUserId;
    
    /**
     * 上传时间
     * 记录脚本文件上传的时间戳
     */
    private LocalDateTime createTime;
}