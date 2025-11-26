package com.fengqi.fund.fundadvisor.service.factor.impl;

import com.fengqi.fund.fundadvisor.dto.factor.FactorIcVisualizationResponse;
import com.fengqi.fund.fundadvisor.dto.factor.FactorValidationRequest;
import com.fengqi.fund.fundadvisor.dto.factor.FactorValidationResponse;
import com.fengqi.fund.fundadvisor.entity.factor.FactorBase;
import com.fengqi.fund.fundadvisor.entity.factor.FactorDerived;
import com.fengqi.fund.fundadvisor.entity.factor.FactorIcIrResult;
import com.fengqi.fund.fundadvisor.entity.factor.FactorIcSequence;
import com.fengqi.fund.fundadvisor.entity.factor.FactorQuantileReturnCurve;
import com.fengqi.fund.fundadvisor.entity.factor.FactorValidationTask;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorManagementMapper;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorValidationMapper;
import com.fengqi.fund.fundadvisor.mapper.factor.FundFactorMapper;
import com.fengqi.fund.fundadvisor.service.factor.FactorValidationService;
import com.fengqi.fund.fundadvisor.service.factor.FundFactorCalculationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 因子检验服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FactorValidationServiceImpl implements FactorValidationService {
    
    private final FactorValidationMapper factorValidationMapper;
    private final FactorManagementMapper factorManagementMapper;
    private final FundFactorCalculationService fundFactorCalculationService;
    private final FundFactorMapper fundFactorMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    @Transactional
    public FactorValidationResponse createValidationTask(FactorValidationRequest request) {
        try {
            // 验证因子名称列表
            if (request.getFactorNames() == null || request.getFactorNames().isEmpty()) {
                return createErrorResponse("CREATE_VALIDATION_TASK", "因子名称列表不能为空");
            }
            
            // 根据名称查询因子ID
            List<Integer> factorIds = resolveFactorIdsByName(request.getFactorNames());
            if (factorIds.isEmpty()) {
                return createErrorResponse("CREATE_VALIDATION_TASK", 
                        "无法找到以下因子: " + String.join(", ", request.getFactorNames()));
            }
            
            // 检查是否有部分因子未找到
            if (factorIds.size() < request.getFactorNames().size()) {
                List<String> foundNames = new ArrayList<>();
                for (String factorName : request.getFactorNames()) {
                    FactorDerived derivedFactor = factorManagementMapper.selectDerivedFactorByName(factorName);
                    FactorBase baseFactor = factorManagementMapper.selectBaseFactorByName(factorName);
                    if (derivedFactor != null || baseFactor != null) {
                        foundNames.add(factorName);
                    }
                }
                List<String> notFoundNames = new ArrayList<>(request.getFactorNames());
                notFoundNames.removeAll(foundNames);
                log.warn("部分因子未找到: {}", notFoundNames);
            }
            
            // 检查因子状态（内部验证）
            String statusCheckError = validateFactorStatus(factorIds);
            if (statusCheckError != null) {
                return createErrorResponse("CREATE_VALIDATION_TASK", statusCheckError);
            }
            
            // 创建任务
            FactorValidationTask task = new FactorValidationTask();
            
            // 构建任务名称
            String taskName = request.getTaskName() != null ? request.getTaskName() : 
                    "因子检验任务_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            
            task.setTaskName(taskName);
            task.setTaskType(request.getTaskType() != null ? request.getTaskType() : "IC_IR");
            task.setFactorIds(factorIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));
            task.setStartDate(request.getStartDate());
            task.setEndDate(request.getEndDate());
            task.setTaskStatus("PENDING");
            task.setProgress(0);
            task.setCreateUserId(request.getCreateUserId());
            task.setCreateTime(LocalDateTime.now());
            
            factorValidationMapper.insertTask(task);
            
            // 异步执行计算
            executeIcIrCalculation(task.getTaskId());
            
            FactorValidationResponse response = new FactorValidationResponse();
            response.setOperationType("CREATE_VALIDATION_TASK");
            response.setSuccess(true);
            response.setMessage("任务创建成功，正在后台执行计算");
            response.setOperationTime(LocalDateTime.now());
            
            FactorValidationResponse.TaskInfo taskInfo = new FactorValidationResponse.TaskInfo();
            taskInfo.setTaskId(task.getTaskId());
            taskInfo.setTaskName(task.getTaskName());
            taskInfo.setTaskType(task.getTaskType());
            taskInfo.setTaskStatus(task.getTaskStatus());
            taskInfo.setProgress(task.getProgress());
            taskInfo.setCreateTime(task.getCreateTime());
            response.setTaskInfo(taskInfo);
            
            return response;
        } catch (Exception e) {
            log.error("创建检验任务失败", e);
            return createErrorResponse("CREATE_VALIDATION_TASK", "创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据因子名称解析因子ID（支持基础因子和衍生因子）
     * 
     * @param factorNames 因子名称列表
     * @return 因子ID列表
     */
    private List<Integer> resolveFactorIdsByName(List<String> factorNames) {
        List<Integer> factorIds = new ArrayList<>();
        
        for (String factorName : factorNames) {
            // 先查询衍生因子
            FactorDerived derivedFactor = factorManagementMapper.selectDerivedFactorByName(factorName);
            if (derivedFactor != null && derivedFactor.getIsValid()) {
                factorIds.add(derivedFactor.getDerivedId());
                log.debug("根据名称找到衍生因子: {} -> ID: {}", factorName, derivedFactor.getDerivedId());
                continue;
            }
            
            // 再查询基础因子
            FactorBase baseFactor = factorManagementMapper.selectBaseFactorByName(factorName);
            if (baseFactor != null && baseFactor.getIsValid()) {
                factorIds.add(baseFactor.getBaseId());
                log.debug("根据名称找到基础因子: {} -> ID: {}", factorName, baseFactor.getBaseId());
                continue;
            }
            
            log.warn("未找到因子: {}", factorName);
        }
        
        return factorIds;
    }
    
    @Override
    @Async
    @Transactional
    public void executeIcIrCalculation(Integer taskId) {
        try {
            log.info("开始执行IC/IR计算，任务ID: {}", taskId);
            
            // 更新任务状态为执行中
            FactorValidationTask task = factorValidationMapper.selectTaskById(taskId);
            if (task == null) {
                log.error("任务不存在，taskId: {}", taskId);
                return;
            }
            
            task.setTaskStatus("RUNNING");
            task.setProgress(10);
            task.setStartTime(LocalDateTime.now());
            factorValidationMapper.updateTaskStatus(task);
            
            // 解析因子ID列表
            List<Integer> factorIds = Arrays.stream(task.getFactorIds().split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            
            // 解析基金名称（从任务名称中提取）
            String fundName = null;
            String fundCode = null;
            String taskName = task.getTaskName();
            log.info("任务名称: {}", taskName);
            
            if (taskName != null && taskName.contains("[") && taskName.endsWith("]")) {
                fundName = taskName.substring(taskName.indexOf("[") + 1, taskName.length() - 1);
                log.info("解析出的基金名称: {}", fundName);
                
                fundCode = fundFactorMapper.selectFundCodeByName(fundName);
                log.info("从任务名称解析到基金名称: {}, 基金代码: {}", fundName, fundCode);
            } else {
                log.warn("任务名称格式不正确，无法解析基金名称。任务名称: {}", taskName);
            }
            
            List<FactorValidationResponse.IcIrResult> results = new ArrayList<>();
            
            // 对每个因子进行计算
            int totalFactors = factorIds.size();
            for (int i = 0; i < totalFactors; i++) {
                Integer factorId = factorIds.get(i);
                log.info("计算因子IC/IR，因子ID: {}, 进度: {}/{}", factorId, i + 1, totalFactors);
                
                // 更新进度
                int progress = 10 + (int) ((i + 1.0) / totalFactors * 80);
                task.setProgress(progress);
                factorValidationMapper.updateTaskStatus(task);
                
                // 计算IC/IR
                FactorValidationResponse.IcIrResult result = calculateIcIr(factorId, task.getStartDate(), task.getEndDate(), fundCode);
                if (result != null) {
                    // 保存结果到数据库
                    FactorIcIrResult icIrResult = new FactorIcIrResult();
                    icIrResult.setTaskId(taskId);
                    icIrResult.setFactorId(factorId);
                    icIrResult.setFactorCode(result.getFactorCode());
                    icIrResult.setFactorName(result.getFactorName());
                    icIrResult.setIcMean(result.getIcMean());
                    icIrResult.setIcStd(result.getIcStd());
                    icIrResult.setIrValue(result.getIrValue());
                    icIrResult.setIcPositiveRatio(result.getIcPositiveRatio());
                    icIrResult.setCalculationDate(task.getEndDate());
                    icIrResult.setCreateTime(LocalDateTime.now());
                    
                    // 序列化IC序列
                    if (result.getIcSequence() != null) {
                        try {
                            String icSequenceJson = objectMapper.writeValueAsString(result.getIcSequence());
                            icIrResult.setIcSequence(icSequenceJson);
                        } catch (Exception e) {
                            log.warn("序列化IC序列失败", e);
                        }
                    }
                    
                    factorValidationMapper.insertIcIrResult(icIrResult);
                    
                    // 保存IC序列明细
                    if (result.getIcSequence() != null && !result.getIcSequence().isEmpty()) {
                        List<FactorIcSequence> sequences = new ArrayList<>();
                        for (Map.Entry<LocalDate, BigDecimal> entry : result.getIcSequence().entrySet()) {
                            FactorIcSequence seq = new FactorIcSequence();
                            seq.setResultId(icIrResult.getResultId());
                            seq.setTradeDate(entry.getKey());
                            seq.setIcValue(entry.getValue());
                            seq.setCreateTime(LocalDateTime.now());
                            sequences.add(seq);
                        }
                        if (!sequences.isEmpty()) {
                            factorValidationMapper.batchInsertIcSequence(sequences);
                        }
                    }
                    
                    results.add(result);
                }
            }
            
            // 更新任务状态为成功
            task.setTaskStatus("SUCCESS");
            task.setProgress(100);
            task.setEndTime(LocalDateTime.now());
            factorValidationMapper.updateTaskStatus(task);
            
            log.info("IC/IR计算完成，任务ID: {}, 成功计算{}个因子", taskId, results.size());
            
        } catch (Exception e) {
            log.error("执行IC/IR计算失败，任务ID: {}", taskId, e);
            
            // 更新任务状态为失败
            try {
                FactorValidationTask task = factorValidationMapper.selectTaskById(taskId);
                if (task != null) {
                    task.setTaskStatus("FAILED");
                    task.setErrorMessage(e.getMessage());
                    task.setEndTime(LocalDateTime.now());
                    factorValidationMapper.updateTaskStatus(task);
                }
            } catch (Exception ex) {
                log.error("更新任务状态失败", ex);
            }
        }
    }
    
    /**
     * 计算单个因子的IC/IR
     * 基于fund_etf_spot_ths表的数据自动计算因子值，然后计算IC/IR
     * 支持基础因子和衍生因子
     */
    private FactorValidationResponse.IcIrResult calculateIcIr(Integer factorId, LocalDate startDate, LocalDate endDate, String fundCode) {
        try {
            // 获取因子信息（先查衍生因子，再查基础因子）
            FactorDerived derivedFactor = factorManagementMapper.selectDerivedFactorById(factorId);
            FactorBase baseFactor = null;
            String factorCode;
            String factorName;
            boolean isDerived = true;
            
            if (derivedFactor != null && derivedFactor.getIsValid()) {
                factorCode = derivedFactor.getFactorCode();
                factorName = derivedFactor.getFactorName();
            } else {
                // 查询基础因子
                baseFactor = factorManagementMapper.selectBaseFactorById(factorId);
                if (baseFactor == null || !baseFactor.getIsValid()) {
                    log.warn("因子不存在或已失效，factorId: {}", factorId);
                    return null;
                }
                factorCode = baseFactor.getFactorCode();
                factorName = baseFactor.getFactorName();
                isDerived = false;
            }
            
            log.info("开始计算IC/IR，因子ID: {}, 因子类型: {}, 因子编码: {}, 因子名称: {}, 指定基金: {}, 日期范围: {} 到 {}", 
                    factorId, isDerived ? "衍生因子" : "基础因子", factorCode, factorName, fundCode, startDate, endDate);
            
            // 1. 自动计算因子值（基于fund_etf_spot_ths表）
            List<Map<String, Object>> factorValues = fundFactorCalculationService.calculateFundFactorValues(
                    factorId, fundCode, startDate, endDate);
            
            if (factorValues.isEmpty()) {
                log.warn("无法计算因子值，factorId: {}", factorId);
                return null;
            }
            
            log.info("计算出{}条因子值数据", factorValues.size());
            
            // 2. 获取基金收益率数据（下期收益率，用于IC计算）
            log.info("开始获取收益率数据，开始日期: {}, 结束日期: {}, 基金代码: {}", startDate, endDate, fundCode);
            List<Map<String, Object>> returnRates = fundFactorCalculationService.calculateFundReturnRates(startDate, endDate, fundCode);
            
            if (returnRates.isEmpty()) {
                log.warn("无法获取收益率数据");
                return null;
            }
            
            log.info("获取到{}条收益率数据", returnRates.size());
            
            // 3. 按日期分组，计算每日IC值
            // IC = corr(因子值, 下期收益率)
            Map<LocalDate, BigDecimal> icSequence = calculateDailyIc(factorValues, returnRates);
            
            if (icSequence.isEmpty()) {
                log.warn("无法计算IC序列");
                return null;
            }
            
            log.info("计算出{}个交易日的IC值", icSequence.size());
            
            // 4. 计算IC统计量
            List<BigDecimal> icValues = new ArrayList<>(icSequence.values());
            BigDecimal icMean = calculateMean(icValues);
            BigDecimal icStd = calculateStd(icValues, icMean);
            BigDecimal irValue = icStd.compareTo(BigDecimal.ZERO) > 0 
                    ? icMean.divide(icStd, 6, RoundingMode.HALF_UP) 
                    : BigDecimal.ZERO;
            
            // 计算IC正相关比例
            long positiveCount = icValues.stream()
                    .filter(v -> v.compareTo(BigDecimal.ZERO) > 0)
                    .count();
            BigDecimal icPositiveRatio = icValues.isEmpty() ? BigDecimal.ZERO :
                    BigDecimal.valueOf(positiveCount)
                            .divide(BigDecimal.valueOf(icValues.size()), 4, RoundingMode.HALF_UP);
            
            FactorValidationResponse.IcIrResult result = new FactorValidationResponse.IcIrResult();
            result.setFactorId(factorId);
            result.setFactorCode(factorCode);
            result.setFactorName(factorName);
            result.setIcMean(icMean);
            result.setIcStd(icStd);
            result.setIrValue(irValue);
            result.setIcPositiveRatio(icPositiveRatio);
            result.setIcSequence(icSequence);
            result.setCalculationDate(endDate);
            
            log.info("IC/IR计算完成，因子: {}, IC均值: {}, IC标准差: {}, IR值: {}", 
                    factorName, icMean, icStd, irValue);
            
            return result;
        } catch (Exception e) {
            log.error("计算IC/IR失败，factorId: {}", factorId, e);
            return null;
        }
    }
    
    /**
     * 计算每日IC值
     * IC = corr(因子值, 下期收益率)
     * 对每个交易日，计算所有基金的因子值与下期收益率的相关系数
     */
    private Map<LocalDate, BigDecimal> calculateDailyIc(List<Map<String, Object>> factorValues,
                                                          List<Map<String, Object>> returnRates) {
        Map<LocalDate, BigDecimal> icSequence = new LinkedHashMap<>();
        
        // 构建收益率映射：基金代码 -> 日期 -> 收益率
        Map<String, Map<LocalDate, BigDecimal>> returnMap = new HashMap<>();
        for (Map<String, Object> rate : returnRates) {
            String fundCode = (String) rate.get("code");
            
            // 修复类型转换：数据库返回的是 java.sql.Date，需要转换为 LocalDate
            Object factorDateObj = rate.get("factor_date");
            LocalDate factorDate = null;
            if (factorDateObj instanceof java.sql.Date) {
                factorDate = ((java.sql.Date) factorDateObj).toLocalDate();
            } else if (factorDateObj instanceof LocalDate) {
                factorDate = (LocalDate) factorDateObj;
            }
            
            BigDecimal nextReturn = rate.get("next_return_rate") != null ?
                    BigDecimal.valueOf(((Number) rate.get("next_return_rate")).doubleValue()) : null;
            
            if (fundCode != null && factorDate != null && nextReturn != null) {
                returnMap.computeIfAbsent(fundCode, k -> new HashMap<>()).put(factorDate, nextReturn);
            }
        }
        
        // 按日期分组因子值
        Map<LocalDate, List<Map<String, Object>>> factorValuesByDate = factorValues.stream()
                .collect(Collectors.groupingBy(data -> {
                    Object tradeDateObj = data.get("tradeDate");
                    if (tradeDateObj instanceof java.sql.Date) {
                        return ((java.sql.Date) tradeDateObj).toLocalDate();
                    } else if (tradeDateObj instanceof LocalDate) {
                        return (LocalDate) tradeDateObj;
                    }
                    return null;
                }));
        
        // 对每个交易日计算IC
        for (Map.Entry<LocalDate, List<Map<String, Object>>> entry : factorValuesByDate.entrySet()) {
            LocalDate tradeDate = entry.getKey();
            List<Map<String, Object>> dayFactorValues = entry.getValue();
            
            // 收集该日期的因子值和对应的下期收益率
            List<Double> factorValueList = new ArrayList<>();
            List<Double> returnList = new ArrayList<>();
            
            for (Map<String, Object> factorData : dayFactorValues) {
                String fundCode = (String) factorData.get("fundCode");
                Object factorValueObj = factorData.get("factorValue");
                BigDecimal factorValue = null;
                
                if (factorValueObj instanceof BigDecimal) {
                    factorValue = (BigDecimal) factorValueObj;
                } else if (factorValueObj instanceof Number) {
                    factorValue = BigDecimal.valueOf(((Number) factorValueObj).doubleValue());
                }
                
                if (fundCode != null && factorValue != null) {
                    Map<LocalDate, BigDecimal> fundReturns = returnMap.get(fundCode);
                    if (fundReturns != null) {
                        BigDecimal nextReturn = fundReturns.get(tradeDate);
                        if (nextReturn != null) {
                            factorValueList.add(factorValue.doubleValue());
                            returnList.add(nextReturn.doubleValue());
                        }
                    }
                }
            }
            
            // 计算相关系数（IC值）
            if (factorValueList.size() >= 2) {
                BigDecimal icValue = calculateCorrelation(factorValueList, returnList);
                if (icValue != null) {
                    icSequence.put(tradeDate, icValue);
                }
            }
        }
        
        return icSequence;
    }
    
    /**
     * 计算两个序列的相关系数（皮尔逊相关系数）
     */
    private BigDecimal calculateCorrelation(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.size() < 2) {
            return null;
        }
        
        int n = x.size();
        
        // 计算均值
        double meanX = x.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double meanY = y.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        // 计算协方差和方差
        double covariance = 0.0;
        double varianceX = 0.0;
        double varianceY = 0.0;
        
        for (int i = 0; i < n; i++) {
            double diffX = x.get(i) - meanX;
            double diffY = y.get(i) - meanY;
            covariance += diffX * diffY;
            varianceX += diffX * diffX;
            varianceY += diffY * diffY;
        }
        
        covariance /= n;
        varianceX /= n;
        varianceY /= n;
        
        // 计算相关系数
        double stdX = Math.sqrt(varianceX);
        double stdY = Math.sqrt(varianceY);
        
        if (stdX == 0 || stdY == 0) {
            return BigDecimal.ZERO;
        }
        
        double correlation = covariance / (stdX * stdY);
        
        // 处理NaN和无穷大
        if (Double.isNaN(correlation) || Double.isInfinite(correlation)) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(correlation).setScale(6, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算均值
     */
    private BigDecimal calculateMean(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 6, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算标准差
     */
    private BigDecimal calculateStd(List<BigDecimal> values, BigDecimal mean) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal variance = values.stream()
                .map(v -> v.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 6, RoundingMode.HALF_UP);
        return new BigDecimal(Math.sqrt(variance.doubleValue()))
                .setScale(6, RoundingMode.HALF_UP);
    }
    
    @Override
    public FactorValidationResponse getTaskStatus(Integer taskId) {
        try {
            FactorValidationTask task = factorValidationMapper.selectTaskById(taskId);
            if (task == null) {
                return createErrorResponse("GET_TASK_STATUS", "任务不存在");
            }
            
            FactorValidationResponse response = new FactorValidationResponse();
            response.setOperationType("GET_TASK_STATUS");
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setOperationTime(LocalDateTime.now());
            
            FactorValidationResponse.TaskInfo taskInfo = new FactorValidationResponse.TaskInfo();
            taskInfo.setTaskId(task.getTaskId());
            taskInfo.setTaskName(task.getTaskName());
            taskInfo.setTaskType(task.getTaskType());
            taskInfo.setTaskStatus(task.getTaskStatus());
            taskInfo.setProgress(task.getProgress());
            taskInfo.setCreateTime(task.getCreateTime());
            taskInfo.setStartTime(task.getStartTime());
            taskInfo.setEndTime(task.getEndTime());
            response.setTaskInfo(taskInfo);
            
            return response;
        } catch (Exception e) {
            log.error("查询任务状态失败", e);
            return createErrorResponse("GET_TASK_STATUS", "查询失败: " + e.getMessage());
        }
    }
    
    @Override
    public FactorValidationResponse getTaskResults(Integer taskId) {
        try {
            FactorValidationTask task = factorValidationMapper.selectTaskById(taskId);
            if (task == null) {
                return createErrorResponse("GET_TASK_RESULTS", "任务不存在");
            }
            
            List<FactorIcIrResult> dbResults = factorValidationMapper.selectIcIrResultsByTaskId(taskId);
            
            FactorValidationResponse response = new FactorValidationResponse();
            response.setOperationType("GET_TASK_RESULTS");
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setOperationTime(LocalDateTime.now());
            
            FactorValidationResponse.TaskInfo taskInfo = new FactorValidationResponse.TaskInfo();
            taskInfo.setTaskId(task.getTaskId());
            taskInfo.setTaskName(task.getTaskName());
            taskInfo.setTaskType(task.getTaskType());
            taskInfo.setTaskStatus(task.getTaskStatus());
            taskInfo.setProgress(task.getProgress());
            taskInfo.setCreateTime(task.getCreateTime());
            taskInfo.setStartTime(task.getStartTime());
            taskInfo.setEndTime(task.getEndTime());
            response.setTaskInfo(taskInfo);
            
            // 转换结果
            List<FactorValidationResponse.IcIrResult> results = new ArrayList<>();
            for (FactorIcIrResult dbResult : dbResults) {
                FactorValidationResponse.IcIrResult result = new FactorValidationResponse.IcIrResult();
                result.setResultId(dbResult.getResultId());
                result.setTaskId(dbResult.getTaskId());
                result.setFactorId(dbResult.getFactorId());
                result.setFactorCode(dbResult.getFactorCode());
                result.setFactorName(dbResult.getFactorName());
                result.setIcMean(dbResult.getIcMean());
                result.setIcStd(dbResult.getIcStd());
                result.setIrValue(dbResult.getIrValue());
                result.setIcPositiveRatio(dbResult.getIcPositiveRatio());
                result.setCalculationDate(dbResult.getCalculationDate());
                
                // 反序列化IC序列
                if (dbResult.getIcSequence() != null && !dbResult.getIcSequence().isEmpty()) {
                    try {
                        Map<LocalDate, BigDecimal> icSequence = objectMapper.readValue(
                                dbResult.getIcSequence(),
                                new TypeReference<Map<LocalDate, BigDecimal>>() {});
                        result.setIcSequence(icSequence);
                    } catch (Exception e) {
                        log.warn("反序列化IC序列失败", e);
                    }
                }
                
                results.add(result);
            }
            response.setIcIrResults(results);
            
            return response;
        } catch (Exception e) {
            log.error("查询任务结果失败", e);
            return createErrorResponse("GET_TASK_RESULTS", "查询失败: " + e.getMessage());
        }
    }
    
    @Override
    public FactorValidationResponse getAllTasks() {
        try {
            List<FactorValidationTask> tasks = factorValidationMapper.selectAllTasks();
            
            FactorValidationResponse response = new FactorValidationResponse();
            response.setOperationType("GET_ALL_TASKS");
            response.setSuccess(true);
            response.setMessage("查询成功，共" + tasks.size() + "个任务");
            response.setOperationTime(LocalDateTime.now());
            
            // 注意：这里只返回任务列表，不包含详细结果
            // 如果需要详细结果，可以调用 getTaskResults
            // 可以将任务列表转换为TaskInfo列表，但当前DTO结构不支持，暂时只返回成功状态
            
            return response;
        } catch (Exception e) {
            log.error("查询所有任务失败", e);
            return createErrorResponse("GET_ALL_TASKS", "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证因子状态（私有方法，仅在创建任务时内部使用）
     * 支持基础因子和衍生因子
     * 
     * @param factorIds 因子ID列表
     * @return 如果验证通过返回null，否则返回错误消息
     */
    private String validateFactorStatus(List<Integer> factorIds) {
        try {
            List<String> invalidFactors = new ArrayList<>();
            List<String> missingFactors = new ArrayList<>();
            
            for (Integer factorId : factorIds) {
                // 先查询衍生因子
                FactorDerived derivedFactor = factorManagementMapper.selectDerivedFactorById(factorId);
                if (derivedFactor != null) {
                    if (!derivedFactor.getIsValid()) {
                        invalidFactors.add(derivedFactor.getFactorName() + " (ID: " + factorId + ")");
                    }
                    continue;
                }
                
                // 再查询基础因子
                FactorBase baseFactor = factorManagementMapper.selectBaseFactorById(factorId);
                if (baseFactor != null) {
                    if (!baseFactor.getIsValid()) {
                        invalidFactors.add(baseFactor.getFactorName() + " (ID: " + factorId + ")");
                    }
                    continue;
                }
                
                // 都找不到
                missingFactors.add("因子ID: " + factorId);
            }
            
            if (!missingFactors.isEmpty() || !invalidFactors.isEmpty()) {
                StringBuilder message = new StringBuilder();
                if (!missingFactors.isEmpty()) {
                    message.append("以下因子不存在: ").append(String.join(", ", missingFactors));
                }
                if (!invalidFactors.isEmpty()) {
                    if (message.length() > 0) {
                        message.append("; ");
                    }
                    message.append("以下因子已失效: ").append(String.join(", ", invalidFactors));
                }
                return message.toString();
            }
            
            return null; // 验证通过
        } catch (Exception e) {
            log.error("验证因子状态失败", e);
            return "验证因子状态失败: " + e.getMessage();
        }
    }
    
    @Override
    public FactorValidationResponse getFactorHistoryResults(Integer factorId, Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 10; // 默认返回最近10条
            }
            
            List<FactorIcIrResult> dbResults = factorValidationMapper.selectIcIrResultsByFactorId(factorId, limit);
            
            FactorValidationResponse response = new FactorValidationResponse();
            response.setOperationType("GET_FACTOR_HISTORY_RESULTS");
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setOperationTime(LocalDateTime.now());
            
            List<FactorValidationResponse.IcIrResult> results = new ArrayList<>();
            for (FactorIcIrResult dbResult : dbResults) {
                FactorValidationResponse.IcIrResult result = new FactorValidationResponse.IcIrResult();
                result.setResultId(dbResult.getResultId());
                result.setTaskId(dbResult.getTaskId());
                result.setFactorId(dbResult.getFactorId());
                result.setFactorCode(dbResult.getFactorCode());
                result.setFactorName(dbResult.getFactorName());
                result.setIcMean(dbResult.getIcMean());
                result.setIcStd(dbResult.getIcStd());
                result.setIrValue(dbResult.getIrValue());
                result.setIcPositiveRatio(dbResult.getIcPositiveRatio());
                result.setCalculationDate(dbResult.getCalculationDate());
                
                // 反序列化IC序列
                if (dbResult.getIcSequence() != null && !dbResult.getIcSequence().isEmpty()) {
                    try {
                        Map<LocalDate, BigDecimal> icSequence = objectMapper.readValue(
                                dbResult.getIcSequence(),
                                new TypeReference<Map<LocalDate, BigDecimal>>() {});
                        result.setIcSequence(icSequence);
                    } catch (Exception e) {
                        log.warn("反序列化IC序列失败", e);
                    }
                }
                
                results.add(result);
            }
            response.setIcIrResults(results);
            
            return response;
        } catch (Exception e) {
            log.error("查询因子历史结果失败", e);
            return createErrorResponse("GET_FACTOR_HISTORY_RESULTS", "查询失败: " + e.getMessage());
        }
    }
    
    private FactorValidationResponse createErrorResponse(String operationType, String message) {
        FactorValidationResponse response = new FactorValidationResponse();
        response.setOperationType(operationType);
        response.setSuccess(false);
        response.setMessage(message);
        response.setOperationTime(LocalDateTime.now());
        return response;
    }
    
    @Override
    public FactorIcVisualizationResponse getQuantileCumulativeReturn(Integer taskId, Integer factorId) {
        try {
            // 获取任务信息
            FactorValidationTask task = factorValidationMapper.selectTaskById(taskId);
            if (task == null) {
                return createErrorVisualizationResponse("任务不存在");
            }
            
            // 确定因子ID
            if (factorId == null) {
                List<Integer> factorIds = Arrays.stream(task.getFactorIds().split(","))
                        .map(String::trim)
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                if (factorIds.isEmpty()) {
                    return createErrorVisualizationResponse("任务中没有因子");
                }
                factorId = factorIds.get(0);
            }
            
            // 先检查数据库是否有缓存的结果
            FactorQuantileReturnCurve cachedCurve = factorValidationMapper.selectQuantileReturnCurve(taskId, factorId);
            if (cachedCurve != null) {
                log.info("从缓存加载分位数累计收益率曲线，taskId: {}, factorId: {}", taskId, factorId);
                return buildResponseFromCache(cachedCurve);
            }
            
            // 获取因子信息
            FactorDerived derivedFactor = factorManagementMapper.selectDerivedFactorById(factorId);
            FactorBase baseFactor = null;
            String factorName;
            if (derivedFactor != null && derivedFactor.getIsValid()) {
                factorName = derivedFactor.getFactorName();
            } else {
                baseFactor = factorManagementMapper.selectBaseFactorById(factorId);
                if (baseFactor == null || !baseFactor.getIsValid()) {
                    return createErrorVisualizationResponse("因子不存在或已失效");
                }
                factorName = baseFactor.getFactorName();
            }
            
            // 解析基金代码（从任务名称中提取）
            String fundCode = null;
            String taskName = task.getTaskName();
            if (taskName != null && taskName.contains("[") && taskName.endsWith("]")) {
                String fundName = taskName.substring(taskName.indexOf("[") + 1, taskName.length() - 1);
                fundCode = fundFactorMapper.selectFundCodeByName(fundName);
            }
            
            // 计算因子值和收益率
            List<Map<String, Object>> factorValues = fundFactorCalculationService.calculateFundFactorValues(
                    factorId, fundCode, task.getStartDate(), task.getEndDate());
            List<Map<String, Object>> returnRates = fundFactorCalculationService.calculateFundReturnRates(
                    task.getStartDate(), task.getEndDate(), fundCode);
            
            if (factorValues.isEmpty() || returnRates.isEmpty()) {
                return createErrorVisualizationResponse("无法获取因子值或收益率数据");
            }
            
            // 按日期分组，计算每个日期的分位数累计收益率
            Map<LocalDate, List<Map<String, Object>>> factorValuesByDate = factorValues.stream()
                    .collect(Collectors.groupingBy(data -> {
                        Object tradeDateObj = data.get("tradeDate");
                        if (tradeDateObj instanceof java.sql.Date) {
                            return ((java.sql.Date) tradeDateObj).toLocalDate();
                        } else if (tradeDateObj instanceof LocalDate) {
                            return (LocalDate) tradeDateObj;
                        }
                        return null;
                    }));
            
            // 构建收益率映射
            Map<String, Map<LocalDate, BigDecimal>> returnMap = new HashMap<>();
            for (Map<String, Object> rate : returnRates) {
                String code = (String) rate.get("code");
                Object dateObj = rate.get("factor_date");
                LocalDate date = null;
                if (dateObj instanceof java.sql.Date) {
                    date = ((java.sql.Date) dateObj).toLocalDate();
                } else if (dateObj instanceof LocalDate) {
                    date = (LocalDate) dateObj;
                }
                BigDecimal nextReturn = rate.get("next_return_rate") != null ?
                        BigDecimal.valueOf(((Number) rate.get("next_return_rate")).doubleValue()) : null;
                // 如果收益率是百分比形式（绝对值大于1），转换为小数形式（除以100）
                // 例如：1.0 (1%) -> 0.01
                // 正常的日收益率应该在 -1 到 1 之间（-100% 到 100%），如果绝对值大于1，很可能是百分比数值形式
                if (nextReturn != null && nextReturn.abs().compareTo(BigDecimal.ONE) > 0) {
                    nextReturn = nextReturn.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                    log.debug("收益率从百分比形式转换为小数形式: {} -> {}", 
                            rate.get("next_return_rate"), nextReturn);
                }
                if (code != null && date != null && nextReturn != null) {
                    returnMap.computeIfAbsent(code, k -> new HashMap<>()).put(date, nextReturn);
                }
            }
            
            // 按日期排序
            List<LocalDate> sortedDates = new ArrayList<>(factorValuesByDate.keySet());
            sortedDates.sort(LocalDate::compareTo);
            
            // 计算每个分位数的累计收益率
            Map<Integer, List<BigDecimal>> quantileReturns = new HashMap<>();
            Map<Integer, BigDecimal> quantileCumulativeReturns = new HashMap<>();
            for (int q = 1; q <= 5; q++) {
                quantileReturns.put(q, new ArrayList<>());
                quantileCumulativeReturns.put(q, BigDecimal.ONE);
            }
            
            // 为每个日期记录累计收益率（即使某天没有数据，也使用前一天的累计收益率）
            for (LocalDate date : sortedDates) {
                List<Map<String, Object>> dayFactorValues = factorValuesByDate.get(date);
                
                // 如果当天有数据，计算并更新累计收益率
                if (dayFactorValues != null && !dayFactorValues.isEmpty()) {
                    // 计算该日期的分位数分组
                    List<FactorValueWithReturn> factorValueList = new ArrayList<>();
                    for (Map<String, Object> factorData : dayFactorValues) {
                        String code = (String) factorData.get("fundCode");
                        Object factorValueObj = factorData.get("factorValue");
                        BigDecimal factorValue = null;
                        if (factorValueObj instanceof BigDecimal) {
                            factorValue = (BigDecimal) factorValueObj;
                        } else if (factorValueObj instanceof Number) {
                            factorValue = BigDecimal.valueOf(((Number) factorValueObj).doubleValue());
                        }
                        
                        if (code != null && factorValue != null) {
                            Map<LocalDate, BigDecimal> fundReturns = returnMap.get(code);
                            if (fundReturns != null) {
                                BigDecimal nextReturn = fundReturns.get(date);
                                if (nextReturn != null) {
                                    factorValueList.add(new FactorValueWithReturn(factorValue, nextReturn));
                                }
                            }
                        }
                    }
                    
                    if (factorValueList.size() >= 5) {
                        // 按因子值排序并分组
                        factorValueList.sort((a, b) -> a.factorValue.compareTo(b.factorValue));
                        int groupSize = factorValueList.size() / 5;
                        
                        // 计算每个分位数的平均收益率（剔除异常值）
                        for (int q = 1; q <= 5; q++) {
                            int startIdx = (q - 1) * groupSize;
                            int endIdx = (q == 5) ? factorValueList.size() : q * groupSize;
                            
                            // 提取该分位数的所有收益率
                            List<BigDecimal> quantileReturnList = new ArrayList<>();
                            for (int i = startIdx; i < endIdx; i++) {
                                quantileReturnList.add(factorValueList.get(i).nextReturn);
                            }
                            
                            // 使用3倍标准差原则剔除异常值
                            List<BigDecimal> filteredReturns = removeOutliers(quantileReturnList);
                            
                            // 计算剔除异常值后的平均收益率
                            BigDecimal avgReturn = BigDecimal.ZERO;
                            if (!filteredReturns.isEmpty()) {
                                avgReturn = calculateMean(filteredReturns);
                                
                                // 合理性检查：日收益率应该在合理范围内（-50% 到 50%）
                                // 如果超出范围，可能是数据错误，使用0
                                if (avgReturn.compareTo(BigDecimal.valueOf(-0.5)) < 0 || 
                                    avgReturn.compareTo(BigDecimal.valueOf(0.5)) > 0) {
                                    log.warn("分位数{}的平均收益率异常: {}，超出合理范围[-0.5, 0.5]，使用0", q, avgReturn);
                                    avgReturn = BigDecimal.ZERO;
                                }
                            }
                            
                            // 更新累计收益率
                            // 累计收益率 = 前一日累计收益率 * (1 + 日收益率)
                            BigDecimal currentCumulative = quantileCumulativeReturns.get(q);
                            BigDecimal newCumulative = currentCumulative.multiply(BigDecimal.ONE.add(avgReturn));
                            
                            // 边界检查：累计收益率不应该小于0.0001（避免变成0）
                            if (newCumulative.compareTo(BigDecimal.valueOf(0.0001)) < 0) {
                                log.warn("分位数{}的累计收益率异常小: {}，设置为0.0001", q, newCumulative);
                                newCumulative = BigDecimal.valueOf(0.0001);
                            }
                            
                            // 限制小数位数为4位
                            newCumulative = newCumulative.setScale(4, RoundingMode.HALF_UP);
                            quantileCumulativeReturns.put(q, newCumulative);
                        }
                    }
                }
                
                // 为每个分位数添加当天的累计收益率（即使没有数据，也使用前一天的累计收益率）
                for (int q = 1; q <= 5; q++) {
                    quantileReturns.get(q).add(quantileCumulativeReturns.get(q));
                }
            }
            
            // 构建响应
            FactorIcVisualizationResponse response = new FactorIcVisualizationResponse();
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setTaskId(taskId);
            response.setFactorId(factorId);
            response.setFactorName(factorName);
            
            FactorIcVisualizationResponse.QuantileCumulativeReturnData data = 
                    new FactorIcVisualizationResponse.QuantileCumulativeReturnData();
            data.setDates(sortedDates);
            data.setQuantileReturns(quantileReturns);
            
            Map<Integer, String> quantileDescriptions = new HashMap<>();
            quantileDescriptions.put(1, "低分位组");
            quantileDescriptions.put(2, "中低分位组");
            quantileDescriptions.put(3, "中分位组");
            quantileDescriptions.put(4, "中高分位组");
            quantileDescriptions.put(5, "高分位组");
            data.setQuantileDescriptions(quantileDescriptions);
            
            response.setCumulativeReturnData(data);
            
            // 保存计算结果到数据库
            try {
                saveQuantileReturnCurveToDatabase(taskId, factorId, factorName, sortedDates, 
                        quantileReturns, quantileDescriptions, task.getEndDate());
            } catch (Exception e) {
                log.warn("保存分位数累计收益率曲线到数据库失败，taskId: {}, factorId: {}", taskId, factorId, e);
                // 保存失败不影响返回结果
            }
            
            return response;
        } catch (Exception e) {
            log.error("获取分位数累计收益率曲线失败，taskId: {}, factorId: {}", taskId, factorId, e);
            return createErrorVisualizationResponse("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 从缓存构建响应
     */
    private FactorIcVisualizationResponse buildResponseFromCache(FactorQuantileReturnCurve cachedCurve) {
        try {
            FactorIcVisualizationResponse response = new FactorIcVisualizationResponse();
            response.setSuccess(true);
            response.setMessage("查询成功（来自缓存）");
            response.setTaskId(cachedCurve.getTaskId());
            response.setFactorId(cachedCurve.getFactorId());
            response.setFactorName(cachedCurve.getFactorName());
            
            // 解析日期列表
            List<LocalDate> dates = objectMapper.readValue(
                    cachedCurve.getDatesJson(),
                    new TypeReference<List<LocalDate>>() {});
            
            // 解析分位数收益率
            Map<Integer, List<BigDecimal>> quantileReturns = objectMapper.readValue(
                    cachedCurve.getQuantileReturnsJson(),
                    new TypeReference<Map<Integer, List<BigDecimal>>>() {});
            
            // 解析分位数描述
            Map<Integer, String> quantileDescriptions = null;
            if (cachedCurve.getQuantileDescriptionsJson() != null) {
                quantileDescriptions = objectMapper.readValue(
                        cachedCurve.getQuantileDescriptionsJson(),
                        new TypeReference<Map<Integer, String>>() {});
            } else {
                // 如果没有描述，使用默认值
                quantileDescriptions = new HashMap<>();
                quantileDescriptions.put(1, "低分位组");
                quantileDescriptions.put(2, "中低分位组");
                quantileDescriptions.put(3, "中分位组");
                quantileDescriptions.put(4, "中高分位组");
                quantileDescriptions.put(5, "高分位组");
            }
            
            FactorIcVisualizationResponse.QuantileCumulativeReturnData data = 
                    new FactorIcVisualizationResponse.QuantileCumulativeReturnData();
            data.setDates(dates);
            data.setQuantileReturns(quantileReturns);
            data.setQuantileDescriptions(quantileDescriptions);
            
            response.setCumulativeReturnData(data);
            
            return response;
        } catch (Exception e) {
            log.error("从缓存构建响应失败", e);
            return createErrorVisualizationResponse("从缓存加载失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存分位数累计收益率曲线到数据库
     */
    private void saveQuantileReturnCurveToDatabase(Integer taskId, Integer factorId, String factorName,
                                                    List<LocalDate> dates,
                                                    Map<Integer, List<BigDecimal>> quantileReturns,
                                                    Map<Integer, String> quantileDescriptions,
                                                    LocalDate calculationDate) {
        try {
            FactorQuantileReturnCurve curve = new FactorQuantileReturnCurve();
            curve.setTaskId(taskId);
            curve.setFactorId(factorId);
            curve.setFactorName(factorName);
            
            // 序列化日期列表
            String datesJson = objectMapper.writeValueAsString(dates);
            curve.setDatesJson(datesJson);
            
            // 序列化分位数收益率
            String quantileReturnsJson = objectMapper.writeValueAsString(quantileReturns);
            curve.setQuantileReturnsJson(quantileReturnsJson);
            
            // 序列化分位数描述
            if (quantileDescriptions != null) {
                String quantileDescriptionsJson = objectMapper.writeValueAsString(quantileDescriptions);
                curve.setQuantileDescriptionsJson(quantileDescriptionsJson);
            }
            
            curve.setCalculationDate(calculationDate);
            curve.setCreateTime(LocalDateTime.now());
            
            factorValidationMapper.insertOrUpdateQuantileReturnCurve(curve);
            log.info("成功保存分位数累计收益率曲线到数据库，taskId: {}, factorId: {}, curveId: {}", 
                    taskId, factorId, curve.getCurveId());
        } catch (JsonProcessingException e) {
            log.error("序列化分位数累计收益率曲线数据失败", e);
            throw new RuntimeException("保存数据失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("保存分位数累计收益率曲线到数据库失败", e);
            throw new RuntimeException("保存数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public FactorIcVisualizationResponse getQuantileAnnualizedReturn(Integer taskId, Integer factorId) {
        try {
            // 获取任务信息
            FactorValidationTask task = factorValidationMapper.selectTaskById(taskId);
            if (task == null) {
                return createErrorVisualizationResponse("任务不存在");
            }
            
            // 确定因子ID
            if (factorId == null) {
                List<Integer> factorIds = Arrays.stream(task.getFactorIds().split(","))
                        .map(String::trim)
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                if (factorIds.isEmpty()) {
                    return createErrorVisualizationResponse("任务中没有因子");
                }
                factorId = factorIds.get(0);
            }
            
            // 获取因子信息
            FactorDerived derivedFactor = factorManagementMapper.selectDerivedFactorById(factorId);
            FactorBase baseFactor = null;
            String factorName;
            if (derivedFactor != null && derivedFactor.getIsValid()) {
                factorName = derivedFactor.getFactorName();
            } else {
                baseFactor = factorManagementMapper.selectBaseFactorById(factorId);
                if (baseFactor == null || !baseFactor.getIsValid()) {
                    return createErrorVisualizationResponse("因子不存在或已失效");
                }
                factorName = baseFactor.getFactorName();
            }
            
            // 解析基金代码
            String fundCode = null;
            String taskName = task.getTaskName();
            if (taskName != null && taskName.contains("[") && taskName.endsWith("]")) {
                String fundName = taskName.substring(taskName.indexOf("[") + 1, taskName.length() - 1);
                fundCode = fundFactorMapper.selectFundCodeByName(fundName);
            }
            
            // 计算因子值和收益率
            List<Map<String, Object>> factorValues = fundFactorCalculationService.calculateFundFactorValues(
                    factorId, fundCode, task.getStartDate(), task.getEndDate());
            List<Map<String, Object>> returnRates = fundFactorCalculationService.calculateFundReturnRates(
                    task.getStartDate(), task.getEndDate(), fundCode);
            
            if (factorValues.isEmpty() || returnRates.isEmpty()) {
                return createErrorVisualizationResponse("无法获取因子值或收益率数据");
            }
            
            // 构建收益率映射
            Map<String, Map<LocalDate, BigDecimal>> returnMap = new HashMap<>();
            for (Map<String, Object> rate : returnRates) {
                String code = (String) rate.get("code");
                Object dateObj = rate.get("factor_date");
                LocalDate date = null;
                if (dateObj instanceof java.sql.Date) {
                    date = ((java.sql.Date) dateObj).toLocalDate();
                } else if (dateObj instanceof LocalDate) {
                    date = (LocalDate) dateObj;
                }
                BigDecimal nextReturn = rate.get("next_return_rate") != null ?
                        BigDecimal.valueOf(((Number) rate.get("next_return_rate")).doubleValue()) : null;
                // 如果收益率是百分比形式（绝对值大于1），转换为小数形式（除以100）
                // 例如：1.0 (1%) -> 0.01
                // 正常的日收益率应该在 -1 到 1 之间（-100% 到 100%），如果绝对值大于1，很可能是百分比数值形式
                if (nextReturn != null && nextReturn.abs().compareTo(BigDecimal.ONE) > 0) {
                    nextReturn = nextReturn.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                    log.debug("收益率从百分比形式转换为小数形式: {} -> {}", 
                            rate.get("next_return_rate"), nextReturn);
                }
                if (code != null && date != null && nextReturn != null) {
                    returnMap.computeIfAbsent(code, k -> new HashMap<>()).put(date, nextReturn);
                }
            }
            
            // 按日期分组
            Map<LocalDate, List<Map<String, Object>>> factorValuesByDate = factorValues.stream()
                    .collect(Collectors.groupingBy(data -> {
                        Object tradeDateObj = data.get("tradeDate");
                        if (tradeDateObj instanceof java.sql.Date) {
                            return ((java.sql.Date) tradeDateObj).toLocalDate();
                        } else if (tradeDateObj instanceof LocalDate) {
                            return (LocalDate) tradeDateObj;
                        }
                        return null;
                    }));
            
            // 计算每个分位数的平均年化收益率
            Map<Integer, List<BigDecimal>> quantileDailyReturns = new HashMap<>();
            for (int q = 1; q <= 5; q++) {
                quantileDailyReturns.put(q, new ArrayList<>());
            }
            
            for (Map.Entry<LocalDate, List<Map<String, Object>>> entry : factorValuesByDate.entrySet()) {
                LocalDate date = entry.getKey();
                List<Map<String, Object>> dayFactorValues = entry.getValue();
                
                List<FactorValueWithReturn> factorValueList = new ArrayList<>();
                for (Map<String, Object> factorData : dayFactorValues) {
                    String code = (String) factorData.get("fundCode");
                    Object factorValueObj = factorData.get("factorValue");
                    BigDecimal factorValue = null;
                    if (factorValueObj instanceof BigDecimal) {
                        factorValue = (BigDecimal) factorValueObj;
                    } else if (factorValueObj instanceof Number) {
                        factorValue = BigDecimal.valueOf(((Number) factorValueObj).doubleValue());
                    }
                    
                    if (code != null && factorValue != null) {
                        Map<LocalDate, BigDecimal> fundReturns = returnMap.get(code);
                        if (fundReturns != null) {
                            BigDecimal nextReturn = fundReturns.get(date);
                            if (nextReturn != null) {
                                factorValueList.add(new FactorValueWithReturn(factorValue, nextReturn));
                            }
                        }
                    }
                }
                
                if (factorValueList.size() < 5) {
                    continue;
                }
                
                // 按因子值排序并分组
                factorValueList.sort((a, b) -> a.factorValue.compareTo(b.factorValue));
                int groupSize = factorValueList.size() / 5;
                
                for (int q = 1; q <= 5; q++) {
                    int startIdx = (q - 1) * groupSize;
                    int endIdx = (q == 5) ? factorValueList.size() : q * groupSize;
                    
                    // 提取该分位数的所有收益率
                    List<BigDecimal> quantileReturns = new ArrayList<>();
                    for (int i = startIdx; i < endIdx; i++) {
                        quantileReturns.add(factorValueList.get(i).nextReturn);
                    }
                    
                    // 使用3倍标准差原则剔除异常值
                    List<BigDecimal> filteredReturns = removeOutliers(quantileReturns);
                    
                    // 计算剔除异常值后的平均收益率
                    if (!filteredReturns.isEmpty()) {
                        BigDecimal avgReturn = calculateMean(filteredReturns);
                        quantileDailyReturns.get(q).add(avgReturn);
                    }
                }
            }
            
            // 计算年化收益率（假设252个交易日）
            List<Integer> quantiles = Arrays.asList(1, 2, 3, 4, 5);
            List<BigDecimal> annualizedReturns = new ArrayList<>();
            List<BigDecimal> annualizedReturnStds = new ArrayList<>();
            List<String> quantileDescriptions = Arrays.asList("低分位组", "中低分位组", "中分位组", "中高分位组", "高分位组");
            
            for (int q = 1; q <= 5; q++) {
                List<BigDecimal> dailyReturns = quantileDailyReturns.get(q);
                if (dailyReturns.isEmpty()) {
                    annualizedReturns.add(BigDecimal.ZERO);
                    annualizedReturnStds.add(BigDecimal.ZERO);
                    continue;
                }
                
                // 计算平均日收益率
                BigDecimal meanDailyReturn = calculateMean(dailyReturns);
                // 年化收益率 = (1 + 平均日收益率)^252 - 1
                BigDecimal annualizedReturn = BigDecimal.ONE.add(meanDailyReturn)
                        .pow(252)
                        .subtract(BigDecimal.ONE)
                        .multiply(BigDecimal.valueOf(100)) // 转换为百分比
                        .setScale(4, RoundingMode.HALF_UP);
                
                // 计算年化收益率标准差
                BigDecimal dailyStd = calculateStd(dailyReturns, meanDailyReturn);
                BigDecimal annualizedStd = dailyStd.multiply(BigDecimal.valueOf(Math.sqrt(252)))
                        .multiply(BigDecimal.valueOf(100)) // 转换为百分比
                        .setScale(4, RoundingMode.HALF_UP);
                
                annualizedReturns.add(annualizedReturn);
                annualizedReturnStds.add(annualizedStd);
            }
            
            // 构建响应
            FactorIcVisualizationResponse response = new FactorIcVisualizationResponse();
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setTaskId(taskId);
            response.setFactorId(factorId);
            response.setFactorName(factorName);
            
            FactorIcVisualizationResponse.QuantileAnnualizedReturnData data = 
                    new FactorIcVisualizationResponse.QuantileAnnualizedReturnData();
            data.setQuantiles(quantiles);
            data.setAnnualizedReturns(annualizedReturns);
            data.setAnnualizedReturnStds(annualizedReturnStds);
            data.setQuantileDescriptions(quantileDescriptions);
            
            response.setAnnualizedReturnData(data);
            
            return response;
        } catch (Exception e) {
            log.error("获取分位数平均年化收益率失败，taskId: {}, factorId: {}", taskId, factorId, e);
            return createErrorVisualizationResponse("查询失败: " + e.getMessage());
        }
    }
    
    @Override
    public FactorIcVisualizationResponse getIcTearSheetData(Integer taskId, Integer factorId) {
        try {
            // 获取任务信息
            FactorValidationTask task = factorValidationMapper.selectTaskById(taskId);
            if (task == null) {
                return createErrorVisualizationResponse("任务不存在");
            }
            
            // 确定因子ID
            Integer finalFactorId = factorId;
            if (finalFactorId == null) {
                List<Integer> factorIds = Arrays.stream(task.getFactorIds().split(","))
                        .map(String::trim)
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                if (factorIds.isEmpty()) {
                    return createErrorVisualizationResponse("任务中没有因子");
                }
                finalFactorId = factorIds.get(0);
            }
            
            // 获取IC/IR结果
            List<FactorIcIrResult> results = factorValidationMapper.selectIcIrResultsByTaskId(taskId);
            final Integer targetFactorId = finalFactorId;
            FactorIcIrResult result = results.stream()
                    .filter(r -> r.getFactorId().equals(targetFactorId))
                    .findFirst()
                    .orElse(null);
            
            if (result == null) {
                return createErrorVisualizationResponse("未找到该因子的IC/IR结果");
            }
            
            // 获取IC序列
            List<FactorIcSequence> sequences = factorValidationMapper.selectIcSequenceByResultId(result.getResultId());
            if (sequences.isEmpty()) {
                return createErrorVisualizationResponse("未找到IC序列数据");
            }
            
            // 按日期排序
            sequences.sort((a, b) -> a.getTradeDate().compareTo(b.getTradeDate()));
            
            // 提取IC值
            List<LocalDate> dates = sequences.stream()
                    .map(FactorIcSequence::getTradeDate)
                    .collect(Collectors.toList());
            List<BigDecimal> icValues = sequences.stream()
                    .map(FactorIcSequence::getIcValue)
                    .collect(Collectors.toList());
            
            // 计算滚动统计（30日滚动均值）
            int windowSize = 30;
            List<BigDecimal> rollingMean = new ArrayList<>();
            for (int i = 0; i < icValues.size(); i++) {
                int startIdx = Math.max(0, i - windowSize + 1);
                List<BigDecimal> windowValues = icValues.subList(startIdx, i + 1);
                BigDecimal mean = calculateMean(windowValues);
                rollingMean.add(mean);
            }
            
            // 计算IC分布直方图数据（10个区间）
            int binCount = 10;
            BigDecimal minIc = icValues.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal maxIc = icValues.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal binWidth = maxIc.subtract(minIc).divide(BigDecimal.valueOf(binCount), 6, RoundingMode.HALF_UP);
            
            List<BigDecimal> bins = new ArrayList<>();
            List<Integer> frequencies = new ArrayList<>();
            for (int i = 0; i <= binCount; i++) {
                bins.add(minIc.add(binWidth.multiply(BigDecimal.valueOf(i))));
            }
            for (int i = 0; i < binCount; i++) {
                final int binIndex = i;
                long count = icValues.stream()
                        .filter(v -> {
                            BigDecimal lower = bins.get(binIndex);
                            BigDecimal upper = bins.get(binIndex + 1);
                            return (v.compareTo(lower) >= 0 && (binIndex == binCount - 1 || v.compareTo(upper) < 0));
                        })
                        .count();
                frequencies.add((int) count);
            }
            
            // 计算IC统计量
            List<BigDecimal> sortedIcValues = new ArrayList<>(icValues);
            sortedIcValues.sort(BigDecimal::compareTo);
            
            BigDecimal icMedian = sortedIcValues.isEmpty() ? BigDecimal.ZERO :
                    sortedIcValues.size() % 2 == 0 ?
                            sortedIcValues.get(sortedIcValues.size() / 2 - 1)
                                    .add(sortedIcValues.get(sortedIcValues.size() / 2))
                                    .divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP) :
                            sortedIcValues.get(sortedIcValues.size() / 2);
            
            // 计算偏度和峰度（简化版）
            BigDecimal icSkewness = calculateSkewness(icValues, result.getIcMean(), result.getIcStd());
            BigDecimal icKurtosis = calculateKurtosis(icValues, result.getIcMean(), result.getIcStd());
            
            // 构建响应
            FactorIcVisualizationResponse response = new FactorIcVisualizationResponse();
            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setTaskId(taskId);
            response.setFactorId(targetFactorId);
            response.setFactorName(result.getFactorName());
            
            // IC序列数据
            FactorIcVisualizationResponse.IcSequenceData icSequenceData = 
                    new FactorIcVisualizationResponse.IcSequenceData();
            icSequenceData.setDates(dates);
            icSequenceData.setIcValues(icValues);
            icSequenceData.setRollingMean(rollingMean);
            
            // IC分布数据
            FactorIcVisualizationResponse.IcDistributionData icDistributionData = 
                    new FactorIcVisualizationResponse.IcDistributionData();
            icDistributionData.setBins(bins);
            icDistributionData.setFrequencies(frequencies);
            icDistributionData.setMean(result.getIcMean());
            icDistributionData.setStd(result.getIcStd());
            
            // IC统计汇总表
            FactorIcVisualizationResponse.IcStatisticsTable icStatisticsTable = 
                    new FactorIcVisualizationResponse.IcStatisticsTable();
            icStatisticsTable.setIcMean(result.getIcMean());
            icStatisticsTable.setIcStd(result.getIcStd());
            icStatisticsTable.setIrValue(result.getIrValue());
            icStatisticsTable.setIcPositiveRatio(result.getIcPositiveRatio());
            icStatisticsTable.setIcMin(minIc);
            icStatisticsTable.setIcMax(maxIc);
            icStatisticsTable.setIcMedian(icMedian);
            icStatisticsTable.setIcSkewness(icSkewness);
            icStatisticsTable.setIcKurtosis(icKurtosis);
            icStatisticsTable.setSampleCount(icValues.size());
            
            // IC滚动统计
            FactorIcVisualizationResponse.IcRollingStatistics icRollingStatistics = 
                    new FactorIcVisualizationResponse.IcRollingStatistics();
            icRollingStatistics.setDates(dates);
            icRollingStatistics.setRollingMean(rollingMean);
            icRollingStatistics.setRollingStd(calculateRollingStd(icValues, windowSize));
            icRollingStatistics.setWindowSize(windowSize);
            
            // 组装TearSheet数据
            FactorIcVisualizationResponse.IcTearSheetData icTearSheetData = 
                    new FactorIcVisualizationResponse.IcTearSheetData();
            icTearSheetData.setIcSequenceData(icSequenceData);
            icTearSheetData.setIcDistributionData(icDistributionData);
            icTearSheetData.setIcStatisticsTable(icStatisticsTable);
            icTearSheetData.setIcRollingStatistics(icRollingStatistics);
            
            response.setIcTearSheetData(icTearSheetData);
            
            return response;
        } catch (Exception e) {
            log.error("获取IC检验统计图表数据失败，taskId: {}, factorId: {}", taskId, factorId, e);
            return createErrorVisualizationResponse("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建错误响应
     */
    private FactorIcVisualizationResponse createErrorVisualizationResponse(String message) {
        FactorIcVisualizationResponse response = new FactorIcVisualizationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
    
    /**
     * 辅助类：因子值和收益率
     */
    private static class FactorValueWithReturn {
        BigDecimal factorValue;
        BigDecimal nextReturn;
        
        FactorValueWithReturn(BigDecimal factorValue, BigDecimal nextReturn) {
            this.factorValue = factorValue;
            this.nextReturn = nextReturn;
        }
    }
    
    /**
     * 计算偏度
     */
    private BigDecimal calculateSkewness(List<BigDecimal> values, BigDecimal mean, BigDecimal std) {
        if (values.isEmpty() || std.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            BigDecimal diff = value.subtract(mean);
            sum = sum.add(diff.pow(3));
        }
        
        BigDecimal n = BigDecimal.valueOf(values.size());
        BigDecimal skewness = sum.divide(n, 6, RoundingMode.HALF_UP)
                .divide(std.pow(3), 6, RoundingMode.HALF_UP);
        
        return skewness.setScale(6, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算峰度
     */
    private BigDecimal calculateKurtosis(List<BigDecimal> values, BigDecimal mean, BigDecimal std) {
        if (values.isEmpty() || std.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            BigDecimal diff = value.subtract(mean);
            sum = sum.add(diff.pow(4));
        }
        
        BigDecimal n = BigDecimal.valueOf(values.size());
        BigDecimal kurtosis = sum.divide(n, 6, RoundingMode.HALF_UP)
                .divide(std.pow(4), 6, RoundingMode.HALF_UP)
                .subtract(BigDecimal.valueOf(3)); // 减去3得到超额峰度
        
        return kurtosis.setScale(6, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算滚动标准差
     */
    private List<BigDecimal> calculateRollingStd(List<BigDecimal> values, int windowSize) {
        List<BigDecimal> rollingStd = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            int startIdx = Math.max(0, i - windowSize + 1);
            List<BigDecimal> windowValues = values.subList(startIdx, i + 1);
            BigDecimal mean = calculateMean(windowValues);
            BigDecimal std = calculateStd(windowValues, mean);
            rollingStd.add(std);
        }
        return rollingStd;
    }
    
    /**
     * 使用3倍标准差原则剔除异常值
     * 保留在 [均值 - 3*标准差, 均值 + 3*标准差] 范围内的值
     * 
     * @param values 原始数据列表
     * @return 剔除异常值后的数据列表
     */
    private List<BigDecimal> removeOutliers(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 如果数据点少于3个，无法计算标准差，返回原数据
        if (values.size() < 3) {
            return new ArrayList<>(values);
        }
        
        // 计算均值和标准差
        BigDecimal mean = calculateMean(values);
        BigDecimal std = calculateStd(values, mean);
        
        // 如果标准差为0，说明所有值相同，返回原数据
        if (std.compareTo(BigDecimal.ZERO) == 0) {
            return new ArrayList<>(values);
        }
        
        // 计算上下限：均值 ± 3倍标准差
        BigDecimal lowerBound = mean.subtract(std.multiply(BigDecimal.valueOf(3)));
        BigDecimal upperBound = mean.add(std.multiply(BigDecimal.valueOf(3)));
        
        // 筛选出在范围内的值
        List<BigDecimal> filteredValues = new ArrayList<>();
        int outlierCount = 0;
        for (BigDecimal value : values) {
            if (value.compareTo(lowerBound) >= 0 && value.compareTo(upperBound) <= 0) {
                filteredValues.add(value);
            } else {
                outlierCount++;
            }
        }
        
        // 如果剔除后数据为空，返回原数据（避免全部被剔除）
        if (filteredValues.isEmpty()) {
            log.warn("3倍标准差剔除后数据为空，返回原数据。均值: {}, 标准差: {}, 下限: {}, 上限: {}", 
                    mean, std, lowerBound, upperBound);
            return new ArrayList<>(values);
        }
        
        // 记录剔除的异常值数量（仅在剔除数量较多时记录）
        if (outlierCount > 0 && outlierCount * 2 > values.size()) {
            log.warn("3倍标准差剔除异常值: 原始数据{}个，剔除{}个异常值，保留{}个。均值: {}, 标准差: {}", 
                    values.size(), outlierCount, filteredValues.size(), mean, std);
        } else if (outlierCount > 0) {
            log.debug("3倍标准差剔除异常值: 原始数据{}个，剔除{}个异常值，保留{}个", 
                    values.size(), outlierCount, filteredValues.size());
        }
        
        return filteredValues;
    }
}

