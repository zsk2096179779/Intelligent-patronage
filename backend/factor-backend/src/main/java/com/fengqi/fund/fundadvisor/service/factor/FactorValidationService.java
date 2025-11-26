package com.fengqi.fund.fundadvisor.service.factor;

import com.fengqi.fund.fundadvisor.dto.factor.FactorIcVisualizationResponse;
import com.fengqi.fund.fundadvisor.dto.factor.FactorValidationRequest;
import com.fengqi.fund.fundadvisor.dto.factor.FactorValidationResponse;

/**
 * 因子检验服务接口
 */
public interface FactorValidationService {
    
    /**
     * 创建检验任务
     */
    FactorValidationResponse createValidationTask(FactorValidationRequest request);
    
    /**
     * 执行IC/IR计算（异步）
     */
    void executeIcIrCalculation(Integer taskId);
    
    /**
     * 查询任务状态
     */
    FactorValidationResponse getTaskStatus(Integer taskId);
    
    /**
     * 查询任务结果
     */
    FactorValidationResponse getTaskResults(Integer taskId);
    
    /**
     * 查询所有任务
     */
    FactorValidationResponse getAllTasks();
    
    /**
     * 根据因子ID查询历史IC/IR结果
     */
    FactorValidationResponse getFactorHistoryResults(Integer factorId, Integer limit);
    
    /**
     * 获取分位数累计收益率曲线数据
     * 
     * @param taskId 任务ID
     * @param factorId 因子ID（可选，如果不指定则使用任务中的第一个因子）
     * @return 分位数累计收益率曲线数据
     */
    FactorIcVisualizationResponse getQuantileCumulativeReturn(Integer taskId, Integer factorId);
    
    /**
     * 获取分位数平均年化收益率柱状图数据
     * 
     * @param taskId 任务ID
     * @param factorId 因子ID（可选，如果不指定则使用任务中的第一个因子）
     * @return 分位数平均年化收益率数据
     */
    FactorIcVisualizationResponse getQuantileAnnualizedReturn(Integer taskId, Integer factorId);
    
    /**
     * 获取IC检验统计图表数据（Alphalens风格）
     * 
     * @param taskId 任务ID
     * @param factorId 因子ID（可选，如果不指定则使用任务中的第一个因子）
     * @return IC检验统计图表数据
     */
    FactorIcVisualizationResponse getIcTearSheetData(Integer taskId, Integer factorId);
}

