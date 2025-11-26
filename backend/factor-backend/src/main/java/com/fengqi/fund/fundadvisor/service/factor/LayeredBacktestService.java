package com.fengqi.fund.fundadvisor.service.factor;

import com.fengqi.fund.fundadvisor.dto.factor.LayeredBacktestResponse;

/**
 * 分层回测服务接口
 */
public interface LayeredBacktestService {
    
    /**
     * 获取分位数累计收益率曲线数据
     * 
     * @param taskId 任务ID
     * @return 分位数累计收益率曲线数据
     */
    LayeredBacktestResponse getCumulativeReturnCurve(Integer taskId);
    
    /**
     * 获取分位数平均年化收益率柱状图数据
     * 
     * @param taskId 任务ID
     * @return 分位数平均年化收益率数据
     */
    LayeredBacktestResponse getAnnualizedReturnChart(Integer taskId);
    
    /**
     * 获取分位数收益分布箱线图数据
     * 
     * @param taskId 任务ID
     * @param holdingPeriods 持有期列表（天数），可选参数，默认返回常用持有期
     * @return 分位数收益分布箱线图数据
     */
    LayeredBacktestResponse getReturnDistributionChart(Integer taskId, Integer[] holdingPeriods);
    
    /**
     * 获取完整的分层回测数据
     * 包含所有三个图表的数据
     * 
     * @param taskId 任务ID
     * @return 完整的分层回测数据
     */
    LayeredBacktestResponse getCompleteLayeredBacktestData(Integer taskId);
    
    /**
     * 执行分层回测计算
     * 这是核心计算逻辑，将因子值分层并计算各分位组的收益指标
     * 
     * @param taskId 任务ID
     * @return 是否执行成功
     */
    boolean executeLayeredBacktest(Integer taskId);
}