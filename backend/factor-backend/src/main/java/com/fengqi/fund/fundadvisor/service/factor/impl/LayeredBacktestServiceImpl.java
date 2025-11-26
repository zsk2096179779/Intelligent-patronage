package com.fengqi.fund.fundadvisor.service.factor.impl;

import com.fengqi.fund.fundadvisor.dto.factor.LayeredBacktestResponse;
import com.fengqi.fund.fundadvisor.entity.factor.FactorLayeredResult;
import com.fengqi.fund.fundadvisor.entity.factor.FactorLayeredNavSeries;
import com.fengqi.fund.fundadvisor.entity.factor.FactorLayeredHoldingStats;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorLayeredBacktestMapper;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorValidationMapper;
import com.fengqi.fund.fundadvisor.entity.factor.FactorValidationTask;
import com.fengqi.fund.fundadvisor.service.factor.LayeredBacktestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分层回测服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LayeredBacktestServiceImpl implements LayeredBacktestService {
    
    private final FactorLayeredBacktestMapper layeredBacktestMapper;
    private final FactorValidationMapper factorValidationMapper;
    
    @Override
    public LayeredBacktestResponse getCumulativeReturnCurve(Integer taskId) {
        try {
            log.info("获取分位数累计收益率曲线数据，任务ID: {}", taskId);
            
            // 验证任务是否存在
            FactorValidationTask task = factorValidationMapper.selectTaskById(taskId);
            if (task == null) {
                return createErrorResponse("GET_CUMULATIVE_RETURN_CURVE", "任务不存在");
            }
            
            if (!"LAYERED".equals(task.getTaskType()) && !"SUCCESS".equals(task.getTaskStatus())) {
                return createErrorResponse("GET_CUMULATIVE_RETURN_CURVE", "任务不是分层回测类型或未完成");
            }
            
            // 获取分层回测结果
            List<FactorLayeredResult> results = layeredBacktestMapper.selectLayeredResultsByTaskId(taskId);
            if (results.isEmpty()) {
                return createErrorResponse("GET_CUMULATIVE_RETURN_CURVE", "未找到分层回测结果");
            }
            
            LayeredBacktestResponse response = new LayeredBacktestResponse();
            response.setOperationType("GET_CUMULATIVE_RETURN_CURVE");
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setOperationTime(LocalDateTime.now());
            
            // 设置任务信息
            response.setTaskInfo(buildTaskInfo(task));
            
            // 构建分位数累计收益率曲线数据
            List<LayeredBacktestResponse.QuantileCumulativeReturn> cumulativeReturns = new ArrayList<>();
            
            for (FactorLayeredResult result : results) {
                LayeredBacktestResponse.QuantileCumulativeReturn cumulativeReturn = 
                        new LayeredBacktestResponse.QuantileCumulativeReturn();
                
                cumulativeReturn.setQuantile(result.getQuantile());
                cumulativeReturn.setTotalReturn(result.getTotalReturn());
                cumulativeReturn.setAnnualizedReturn(result.getAnnualizedReturn());
                cumulativeReturn.setSharpeRatio(result.getSharpeRatio());
                cumulativeReturn.setMaxDrawdown(result.getMaxDrawdown());
                
                // 获取净值序列数据
                List<FactorLayeredNavSeries> navSeries = 
                        layeredBacktestMapper.selectNavSeriesByResultId(result.getResultId());
                
                Map<LocalDate, BigDecimal> returnSeries = navSeries.stream()
                        .collect(Collectors.toMap(
                                FactorLayeredNavSeries::getTradeDate,
                                nav -> nav.getNavValue().subtract(BigDecimal.ONE), // 净值-1 = 累计收益率
                                (existing, replacement) -> existing,
                                TreeMap::new
                        ));
                
                cumulativeReturn.setReturnSeries(returnSeries);
                cumulativeReturns.add(cumulativeReturn);
            }
            
            response.setCumulativeReturnCurve(cumulativeReturns);
            
            log.info("成功获取{}个分位数的累计收益率曲线数据", cumulativeReturns.size());
            return response;
            
        } catch (Exception e) {
            log.error("获取分位数累计收益率曲线数据失败", e);
            return createErrorResponse("GET_CUMULATIVE_RETURN_CURVE", "查询失败: " + e.getMessage());
        }
    }
    
    @Override
    public LayeredBacktestResponse getAnnualizedReturnChart(Integer taskId) {
        try {
            log.info("获取分位数平均年化收益率柱状图数据，任务ID: {}", taskId);
            
            // 验证任务
            FactorValidationTask task = factorValidationMapper.selectTaskById(taskId);
            if (task == null) {
                return createErrorResponse("GET_ANNUALIZED_RETURN_CHART", "任务不存在");
            }
            
            // 获取分层回测结果
            List<FactorLayeredResult> results = layeredBacktestMapper.selectLayeredResultsByTaskId(taskId);
            if (results.isEmpty()) {
                return createErrorResponse("GET_ANNUALIZED_RETURN_CHART", "未找到分层回测结果");
            }
            
            LayeredBacktestResponse response = new LayeredBacktestResponse();
            response.setOperationType("GET_ANNUALIZED_RETURN_CHART");
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setOperationTime(LocalDateTime.now());
            
            response.setTaskInfo(buildTaskInfo(task));
            
            // 构建分位数平均年化收益率数据
            List<LayeredBacktestResponse.QuantileAnnualizedReturn> annualizedReturns = new ArrayList<>();
            
            for (FactorLayeredResult result : results) {
                LayeredBacktestResponse.QuantileAnnualizedReturn annualizedReturn = 
                        new LayeredBacktestResponse.QuantileAnnualizedReturn();
                
                annualizedReturn.setQuantile(result.getQuantile());
                annualizedReturn.setQuantileName("Q" + result.getQuantile() + "-" + 
                        getQuantileDescription(result.getQuantile()));
                annualizedReturn.setAvgAnnualizedReturn(result.getAnnualizedReturn());
                
                // 获取该分位数的净值序列，计算年化收益率标准差
                List<FactorLayeredNavSeries> navSeries = 
                        layeredBacktestMapper.selectNavSeriesByResultId(result.getResultId());
                
                BigDecimal annualizedReturnStd = calculateAnnualizedReturnStd(navSeries);
                annualizedReturn.setAnnualizedReturnStd(annualizedReturnStd);
                annualizedReturn.setPositiveReturnRatio(result.getWinRate());
                annualizedReturn.setWinRate(result.getWinRate());
                
                annualizedReturns.add(annualizedReturn);
            }
            
            // 按分位数排序
            annualizedReturns.sort(Comparator.comparing(LayeredBacktestResponse.QuantileAnnualizedReturn::getQuantile));
            
            response.setAnnualizedReturnChart(annualizedReturns);
            
            log.info("成功获取{}个分位数的年化收益率数据", annualizedReturns.size());
            return response;
            
        } catch (Exception e) {
            log.error("获取分位数平均年化收益率柱状图数据失败", e);
            return createErrorResponse("GET_ANNUALIZED_RETURN_CHART", "查询失败: " + e.getMessage());
        }
    }
    
    @Override
    public LayeredBacktestResponse getReturnDistributionChart(Integer taskId, Integer[] holdingPeriods) {
        try {
            log.info("获取分位数收益分布箱线图数据，任务ID: {}, 持有期: {}", taskId, holdingPeriods);
            
            // 验证任务
            FactorValidationTask task = factorValidationMapper.selectTaskById(taskId);
            if (task == null) {
                return createErrorResponse("GET_RETURN_DISTRIBUTION_CHART", "任务不存在");
            }
            
            // 默认持有期
            if (holdingPeriods == null || holdingPeriods.length == 0) {
                holdingPeriods = new Integer[]{5, 10, 20, 60, 120}; // 5天、10天、20天、60天、120天
            }
            
            // 获取所有分位数的持有期统计数据
            List<FactorLayeredHoldingStats> allStats = 
                    layeredBacktestMapper.selectHoldingStatsByTaskId(taskId);
            
            if (allStats.isEmpty()) {
                return createErrorResponse("GET_RETURN_DISTRIBUTION_CHART", "未找到持有期统计数据");
            }
            
            LayeredBacktestResponse response = new LayeredBacktestResponse();
            response.setOperationType("GET_RETURN_DISTRIBUTION_CHART");
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setOperationTime(LocalDateTime.now());
            
            response.setTaskInfo(buildTaskInfo(task));
            
            // 按分位数和持有期分组
            Map<Integer, Map<Integer, FactorLayeredHoldingStats>> statsMap = allStats.stream()
                    .collect(Collectors.groupingBy(
                            stat -> {
                                // 从result_id获取分位数
                                List<FactorLayeredResult> results = 
                                        layeredBacktestMapper.selectLayeredResultsByTaskId(taskId);
                                return results.stream()
                                        .filter(r -> r.getResultId().equals(stat.getResultId()))
                                        .findFirst()
                                        .map(FactorLayeredResult::getQuantile)
                                        .orElse(0);
                            },
                            Collectors.toMap(
                                    FactorLayeredHoldingStats::getHoldingDays,
                                    stat -> stat,
                                    (existing, replacement) -> existing
                            )
                    ));
            
            // 构建收益分布箱线图数据
            List<LayeredBacktestResponse.QuantileReturnDistribution> returnDistributions = new ArrayList<>();
            
            for (Integer quantile : Arrays.asList(1, 2, 3, 4, 5)) {
                Map<Integer, FactorLayeredHoldingStats> quantileStats = statsMap.get(quantile);
                if (quantileStats == null) continue;
                
                for (Integer holdingDays : holdingPeriods) {
                    FactorLayeredHoldingStats stat = quantileStats.get(holdingDays);
                    if (stat == null) continue;
                    
                    LayeredBacktestResponse.QuantileReturnDistribution distribution = 
                            new LayeredBacktestResponse.QuantileReturnDistribution();
                    
                    distribution.setQuantile(quantile);
                    distribution.setQuantileName("Q" + quantile + "-" + getQuantileDescription(quantile));
                    distribution.setHoldingPeriod(holdingDays);
                    
                    // 设置统计值
                    distribution.setMinReturn(stat.getMinReturn());
                    distribution.setQ1Return(stat.getQ1Return());
                    distribution.setMedianReturn(stat.getMedianReturn());
                    distribution.setQ3Return(stat.getQ3Return());
                    distribution.setMaxReturn(stat.getMaxReturn());
                    distribution.setMeanReturn(stat.getMeanReturn());
                    distribution.setStdReturn(stat.getStdReturn());
                    distribution.setWinRate(stat.getWinRate());
                    
                    // TODO: 这里可以后续实现异常值检测
                    distribution.setOutliers(new ArrayList<>());
                    
                    returnDistributions.add(distribution);
                }
            }
            
            response.setReturnDistributionChart(returnDistributions);
            
            log.info("成功获取{}个分位数收益分布数据", returnDistributions.size());
            return response;
            
        } catch (Exception e) {
            log.error("获取分位数收益分布箱线图数据失败", e);
            return createErrorResponse("GET_RETURN_DISTRIBUTION_CHART", "查询失败: " + e.getMessage());
        }
    }
    
    @Override
    public LayeredBacktestResponse getCompleteLayeredBacktestData(Integer taskId) {
        try {
            log.info("获取完整的分层回测数据，任务ID: {}", taskId);
            
            LayeredBacktestResponse response = new LayeredBacktestResponse();
            response.setOperationType("GET_COMPLETE_LAYERED_BACKTEST_DATA");
            response.setOperationTime(LocalDateTime.now());
            
            // 验证任务
            FactorValidationTask task = factorValidationMapper.selectTaskById(taskId);
            if (task == null) {
                return createErrorResponse("GET_COMPLETE_LAYERED_BACKTEST_DATA", "任务不存在");
            }
            
            response.setTaskInfo(buildTaskInfo(task));
            
            // 获取所有三个图表的数据
            LayeredBacktestResponse cumulativeResponse = getCumulativeReturnCurve(taskId);
            LayeredBacktestResponse annualizedResponse = getAnnualizedReturnChart(taskId);
            LayeredBacktestResponse distributionResponse = getReturnDistributionChart(taskId, null);
            
            if (cumulativeResponse.getSuccess() && annualizedResponse.getSuccess() && distributionResponse.getSuccess()) {
                response.setSuccess(true);
                response.setMessage("查询成功");
                response.setCumulativeReturnCurve(cumulativeResponse.getCumulativeReturnCurve());
                response.setAnnualizedReturnChart(annualizedResponse.getAnnualizedReturnChart());
                response.setReturnDistributionChart(distributionResponse.getReturnDistributionChart());
            } else {
                response.setSuccess(false);
                response.setMessage("部分数据获取失败");
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("获取完整的分层回测数据失败", e);
            return createErrorResponse("GET_COMPLETE_LAYERED_BACKTEST_DATA", "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建任务信息
     */
    private LayeredBacktestResponse.TaskInfo buildTaskInfo(FactorValidationTask task) {
        LayeredBacktestResponse.TaskInfo taskInfo = new LayeredBacktestResponse.TaskInfo();
        taskInfo.setTaskId(task.getTaskId());
        taskInfo.setTaskName(task.getTaskName());
        taskInfo.setTaskType(task.getTaskType());
        taskInfo.setTaskStatus(task.getTaskStatus());
        taskInfo.setProgress(task.getProgress());
        taskInfo.setCreateTime(task.getCreateTime());
        taskInfo.setStartTime(task.getStartTime());
        taskInfo.setEndTime(task.getEndTime());
        return taskInfo;
    }
    
    /**
     * 计算年化收益率标准差
     */
    private BigDecimal calculateAnnualizedReturnStd(List<FactorLayeredNavSeries> navSeries) {
        if (navSeries.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        // 计算日收益率
        List<BigDecimal> dailyReturns = new ArrayList<>();
        for (int i = 1; i < navSeries.size(); i++) {
            BigDecimal prevNav = navSeries.get(i - 1).getNavValue();
            BigDecimal currentNav = navSeries.get(i).getNavValue();
            BigDecimal dailyReturn = currentNav.divide(prevNav, 8, RoundingMode.HALF_UP)
                    .subtract(BigDecimal.ONE);
            dailyReturns.add(dailyReturn);
        }
        
        // 计算日收益率标准差
        BigDecimal mean = dailyReturns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyReturns.size()), 8, RoundingMode.HALF_UP);
        
        BigDecimal variance = dailyReturns.stream()
                .map(r -> r.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyReturns.size()), 8, RoundingMode.HALF_UP);
        
        BigDecimal dailyStd = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        
        // 年化标准差（假设252个交易日）
        BigDecimal annualizedStd = dailyStd.multiply(BigDecimal.valueOf(Math.sqrt(252)));
        
        return annualizedStd.setScale(6, RoundingMode.HALF_UP);
    }
    
    /**
     * 获取分位数描述
     */
    private String getQuantileDescription(Integer quantile) {
        switch (quantile) {
            case 1: return "低分位组";
            case 2: return "中低分位组";
            case 3: return "中分位组";
            case 4: return "中高分位组";
            case 5: return "高分位组";
            default: return "未知分位组";
        }
    }
    
    @Override
    public boolean executeLayeredBacktest(Integer taskId) {
        try {
            log.info("开始执行分层回测，任务ID: {}", taskId);
            
            // 获取任务信息
            FactorValidationTask task = factorValidationMapper.selectTaskById(taskId);
            if (task == null) {
                log.error("任务不存在: {}", taskId);
                return false;
            }
            
            if (!"LAYERED".equals(task.getTaskType())) {
                log.error("任务类型不是分层回测: {}", taskId);
                return false;
            }
            
            // 更新任务状态为执行中
            FactorValidationTask runningTask = new FactorValidationTask();
            runningTask.setTaskId(taskId);
            runningTask.setTaskStatus("RUNNING");
            runningTask.setProgress(0);
            runningTask.setStartTime(LocalDateTime.now());
            factorValidationMapper.updateTaskStatus(runningTask);
            
            // 解析因子ID
            String[] factorIdStrs = task.getFactorIds().split(",");
            Integer[] factorIds = Arrays.stream(factorIdStrs)
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toArray(Integer[]::new);
            
            // 为每个因子执行分层回测
            for (Integer factorId : factorIds) {
                try {
                    executeFactorLayeredBacktest(taskId, factorId, task.getStartDate(), task.getEndDate());
                    log.info("因子{}分层回测完成", factorId);
                } catch (Exception e) {
                    log.error("因子{}分层回测失败", factorId, e);
                    // 继续执行其他因子
                }
            }
            
            // 更新任务状态为完成
            FactorValidationTask completedTask = new FactorValidationTask();
            completedTask.setTaskId(taskId);
            completedTask.setTaskStatus("SUCCESS");
            completedTask.setProgress(100);
            completedTask.setEndTime(LocalDateTime.now());
            factorValidationMapper.updateTaskStatus(completedTask);
            
            log.info("分层回测执行完成，任务ID: {}", taskId);
            return true;
            
        } catch (Exception e) {
            log.error("分层回测执行失败，任务ID: {}", taskId, e);
            // 更新任务状态为失败
            FactorValidationTask failedTask = new FactorValidationTask();
            failedTask.setTaskId(taskId);
            failedTask.setTaskStatus("FAILED");
            failedTask.setProgress(0);
            failedTask.setErrorMessage(e.getMessage());
            failedTask.setEndTime(LocalDateTime.now());
            factorValidationMapper.updateTaskStatus(failedTask);
            return false;
        }
    }
    
    /**
     * 为单个因子执行分层回测
     */
    private void executeFactorLayeredBacktest(Integer taskId, Integer factorId, 
                                            LocalDate startDate, LocalDate endDate) {
        // TODO: 这里需要实现实际的分层回测逻辑
        // 1. 获取因子数据
        // 2. 获取股票收益率数据
        // 3. 按因子值分层（通常分为5分位）
        // 4. 构建投资组合并计算净值
        // 5. 计算各项统计指标
        // 6. 计算持有期收益统计
        
        // 这里先创建示例数据
        createSampleLayeredBacktestData(taskId, factorId, startDate, endDate);
    }
    
    /**
     * 创建示例分层回测数据（实际项目中应该替换为真实的计算逻辑）
     */
    private void createSampleLayeredBacktestData(Integer taskId, Integer factorId, 
                                               LocalDate startDate, LocalDate endDate) {
        
        // 计算回测天数
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        int tradingDays = (int) (daysBetween * 252 / 365); // 估算交易日数量
        
        // 为每个分位数创建结果
        for (int quantile = 1; quantile <= 5; quantile++) {
            // 创建分层回测结果记录
            FactorLayeredResult result = new FactorLayeredResult();
            result.setTaskId(taskId);
            result.setFactorId(factorId);
            result.setFactorCode("FACTOR_" + factorId);
            result.setFactorName("测试因子" + factorId);
            result.setQuantile(quantile);
            result.setStartDate(startDate);
            result.setEndDate(endDate);
            result.setCalculationDate(LocalDate.now());
            result.setCreateTime(LocalDateTime.now());
            
            // 模拟不同分位数的收益特征
            // 高分位数因子通常表现更好
            double baseReturn = 0.08 + (quantile - 3) * 0.03; // 基础年化收益
            double volatility = 0.15 + Math.random() * 0.1; // 波动率
            double sharpe = baseReturn / volatility; // 夏普比率
            double maxDrawdown = 0.05 + Math.random() * 0.15; // 最大回撤
            double winRate = 0.45 + quantile * 0.05; // 胜率
            
            result.setTotalReturn(BigDecimal.valueOf(baseReturn * (tradingDays / 252.0)));
            result.setAnnualizedReturn(BigDecimal.valueOf(baseReturn));
            result.setSharpeRatio(BigDecimal.valueOf(sharpe));
            result.setMaxDrawdown(BigDecimal.valueOf(maxDrawdown));
            result.setWinRate(BigDecimal.valueOf(winRate));
            result.setVolatility(BigDecimal.valueOf(volatility));
            
            // 插入结果记录
            layeredBacktestMapper.insertLayeredResult(result);
            
            // 创建净值序列数据
            createSampleNavSeries(result.getResultId(), startDate, endDate, baseReturn, volatility);
            
            // 创建持有期统计数据
            createSampleHoldingStats(result.getResultId());
        }
    }
    
    /**
     * 创建示例净值序列数据
     */
    private void createSampleNavSeries(Integer resultId, LocalDate startDate, LocalDate endDate, 
                                     double annualReturn, double volatility) {
        
        LocalDate currentDate = startDate;
        BigDecimal navValue = BigDecimal.ONE;
        List<FactorLayeredNavSeries> navSeries = new ArrayList<>();
        
        while (!currentDate.isAfter(endDate)) {
            // 只在工作日生成数据（简化处理，跳过周末）
            if (currentDate.getDayOfWeek().getValue() <= 5) {
                FactorLayeredNavSeries nav = new FactorLayeredNavSeries();
                nav.setResultId(resultId);
                nav.setTradeDate(currentDate);
                nav.setCreateTime(LocalDateTime.now());
                
                // 模拟日收益率（正态分布）
                double dailyReturnMean = annualReturn / 252;
                double dailyReturnStd = volatility / Math.sqrt(252);
                double dailyReturn = dailyReturnMean + dailyReturnStd * (Math.random() - 0.5) * 2;
                
                // 计算净值
                BigDecimal dailyReturnMultiplier = BigDecimal.ONE.add(BigDecimal.valueOf(dailyReturn));
                navValue = navValue.multiply(dailyReturnMultiplier);
                
                nav.setNavValue(navValue);
                nav.setDailyReturn(BigDecimal.valueOf(dailyReturn));
                nav.setCumulativeReturn(navValue.subtract(BigDecimal.ONE));
                nav.setStockCount(50 + (int)(Math.random() * 100)); // 随机股票数量
                
                navSeries.add(nav);
            }
            currentDate = currentDate.plusDays(1);
        }
        
        // 批量插入净值序列
        layeredBacktestMapper.batchInsertNavSeries(navSeries);
    }
    
    /**
     * 创建示例持有期统计数据
     */
    private void createSampleHoldingStats(Integer resultId) {
        List<FactorLayeredHoldingStats> stats = new ArrayList<>();
        
        // 常用持有期：5天、10天、20天、60天、120天
        Integer[] holdingPeriods = {5, 10, 20, 60, 120};
        
        for (Integer period : holdingPeriods) {
            FactorLayeredHoldingStats stat = new FactorLayeredHoldingStats();
            stat.setResultId(resultId);
            stat.setHoldingDays(period);
            stat.setCreateTime(LocalDateTime.now());
            
            // 模拟持有期收益统计
            double meanReturn = (Math.random() - 0.3) * 0.1; // 平均收益
            double stdReturn = 0.02 + Math.random() * 0.08; // 收益标准差
            
            stat.setMeanReturn(BigDecimal.valueOf(meanReturn));
            stat.setStdReturn(BigDecimal.valueOf(stdReturn));
            stat.setMinReturn(BigDecimal.valueOf(meanReturn - 3 * stdReturn));
            stat.setQ1Return(BigDecimal.valueOf(meanReturn - 0.674 * stdReturn));
            stat.setMedianReturn(BigDecimal.valueOf(meanReturn));
            stat.setQ3Return(BigDecimal.valueOf(meanReturn + 0.674 * stdReturn));
            stat.setMaxReturn(BigDecimal.valueOf(meanReturn + 3 * stdReturn));
            stat.setWinRate(BigDecimal.valueOf(0.3 + Math.random() * 0.4));
            
            stats.add(stat);
        }
        
        // 批量插入持有期统计
        layeredBacktestMapper.batchInsertHoldingStats(stats);
    }
    
    /**
     * 创建错误响应
     */
    private LayeredBacktestResponse createErrorResponse(String operationType, String message) {
        LayeredBacktestResponse response = new LayeredBacktestResponse();
        response.setOperationType(operationType);
        response.setSuccess(false);
        response.setMessage(message);
        response.setOperationTime(LocalDateTime.now());
        return response;
    }
}