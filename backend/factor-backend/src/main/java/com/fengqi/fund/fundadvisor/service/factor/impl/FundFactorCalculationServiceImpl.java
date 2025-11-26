package com.fengqi.fund.fundadvisor.service.factor.impl;

import com.fengqi.fund.fundadvisor.entity.factor.FactorBase;
import com.fengqi.fund.fundadvisor.entity.factor.FactorDerived;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorManagementMapper;
import com.fengqi.fund.fundadvisor.mapper.factor.FundFactorMapper;
import com.fengqi.fund.fundadvisor.service.factor.FundFactorCalculationService;
import com.fengqi.fund.fundadvisor.service.factor.impl.JsonFactorFormulaParser.ParsedFormula;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基金因子计算服务实现类
 * 基于fund_etf_spot_ths表的数据自动计算因子值
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FundFactorCalculationServiceImpl implements FundFactorCalculationService {

    private final FundFactorMapper fundFactorMapper;
    private final FactorManagementMapper factorManagementMapper;
    private final JsonFactorFormulaParser formulaParser;

    @Override
    public List<Map<String, Object>> calculateFundFactorValues(Integer factorId,
                                                                 String fundCode,
                                                                 LocalDate startDate,
                                                                 LocalDate endDate) {
        try {
            // 判断是基础因子还是衍生因子
            FactorBase baseFactor = factorManagementMapper.selectBaseFactorById(factorId);
            FactorDerived derivedFactor = null;

            if (baseFactor == null) {
                derivedFactor = factorManagementMapper.selectDerivedFactorById(factorId);
                if (derivedFactor == null) {
                    log.warn("因子不存在，factorId: {}", factorId);
                    return Collections.emptyList();
                }
            }

            // 优先使用公式计算（如果是基础因子且有公式）
            String factorFormula = baseFactor != null ? baseFactor.getFactorFormula() : null;
            log.info("因子公式检查 - factorId: {}, baseFactor存在: {}, factorFormula: {}, factorFormula是否为空: {}", 
                    factorId, baseFactor != null, factorFormula, factorFormula == null || factorFormula.trim().isEmpty());
            
            if (baseFactor != null && factorFormula != null && !factorFormula.trim().isEmpty()) {
                log.info("使用公式计算基础因子，factorId: {}, formula: {}", factorId, factorFormula);

                if (fundCode != null && !fundCode.trim().isEmpty()) {
                    return calculateByFormula(baseFactor, fundCode, startDate, endDate);
                } else {
                    // 计算所有基金
                    List<Map<String, Object>> allFundData = fundFactorMapper.selectAllFundNavData(startDate, endDate);
                    Map<String, List<Map<String, Object>>> fundDataByCode = allFundData.stream()
                            .collect(Collectors.groupingBy(data -> (String) data.get("code")));

                    List<Map<String, Object>> factorValues = new ArrayList<>();
                    for (Map.Entry<String, List<Map<String, Object>>> entry : fundDataByCode.entrySet()) {
                        List<Map<String, Object>> singleFundValues = calculateByFormula(
                                baseFactor, entry.getKey(), startDate, endDate);
                        factorValues.addAll(singleFundValues);
                    }
                    return factorValues;
                }
            }

            // 回退到基于factor_code的计算方式
            String factorCode = baseFactor != null ? baseFactor.getFactorCode() : derivedFactor.getFactorCode();
            log.info("使用factor_code计算 - factorCode: {}, fundCode: {}", factorCode, fundCode);

            // 根据因子编码计算因子值
            List<Map<String, Object>> factorValues = new ArrayList<>();

            if (fundCode != null && !fundCode.trim().isEmpty()) {
                // 计算单只基金
                factorValues = calculateSingleFundFactorValuesByCode(factorCode, fundCode, startDate, endDate);
            } else {
                // 计算所有基金
                List<Map<String, Object>> allFundData = fundFactorMapper.selectAllFundNavData(startDate, endDate);
                Map<String, List<Map<String, Object>>> fundDataByCode = allFundData.stream()
                        .collect(Collectors.groupingBy(data -> (String) data.get("code")));

                for (Map.Entry<String, List<Map<String, Object>>> entry : fundDataByCode.entrySet()) {
                    List<Map<String, Object>> singleFundValues = calculateSingleFundFactorValuesByCode(
                            factorCode, entry.getKey(), startDate, endDate);
                    factorValues.addAll(singleFundValues);
                }
            }

            return factorValues;
        } catch (Exception e) {
            log.error("计算基金因子值失败，factorId: {}", factorId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据因子编码计算单只基金的因子值
     */
    private List<Map<String, Object>> calculateSingleFundFactorValuesByCode(String factorCode,
                                                                              String fundCode,
                                                                              LocalDate startDate,
                                                                              LocalDate endDate) {
        log.info("开始计算单只基金因子值 - factorCode: {}, fundCode: {}, startDate: {}, endDate: {}", 
                factorCode, fundCode, startDate, endDate);
        
        List<Map<String, Object>> navData = fundFactorMapper.selectFundNavData(fundCode, startDate, endDate);
        log.info("查询到{}条nav数据", navData.size());
        
        if (!navData.isEmpty()) {
            log.info("第一条nav数据: {}", navData.get(0));
        }
        
        List<Map<String, Object>> factorValues = new ArrayList<>();

        // 根据因子编码选择不同的计算方法
        switch (factorCode.toUpperCase()) {
            case "NAV_RETURN":
            case "RETURN_RATE":
            case "FUND_RETURN":
                // 收益率因子：使用change_rate
                for (Map<String, Object> data : navData) {
                    if (data.get("change_rate") != null) {
                        Map<String, Object> factorValue = new HashMap<>();
                        factorValue.put("fundCode", fundCode);
                        factorValue.put("tradeDate", data.get("query_date"));
                        factorValue.put("factorValue", data.get("change_rate"));
                        factorValues.add(factorValue);
                    }
                }
                break;

            case "NAV_VOLATILITY":
            case "VOLATILITY":
                // 波动率因子：计算收益率的标准差
                factorValues = calculateVolatilityFactor(fundCode, navData);
                break;

            case "NAV_MAX_DRAWDOWN":
            case "MAX_DRAWDOWN":
                // 最大回撤因子
                factorValues = calculateMaxDrawdownFactor(fundCode, navData);
                break;

            case "NAV_SHARPE":
            case "SHARPE_RATIO":
                // 夏普比率因子
                factorValues = calculateSharpeRatioFactor(fundCode, navData);
                break;

            case "NAV_MOMENTUM":
            case "MOMENTUM":
                // 动量因子：N日收益率
                factorValues = calculateMomentumFactor(fundCode, navData, 20); // 默认20日动量
                break;

            case "NAV_MEAN_REVERSION":
            case "MEAN_REVERSION":
                // 均值回归因子：当前净值与均值的偏离度
                factorValues = calculateMeanReversionFactor(fundCode, navData);
                break;

            default:
                // 默认使用收益率
                log.warn("未知的因子编码: {}，使用默认收益率计算", factorCode);
                for (Map<String, Object> data : navData) {
                    if (data.get("change_rate") != null) {
                        Map<String, Object> factorValue = new HashMap<>();
                        factorValue.put("fundCode", fundCode);
                        factorValue.put("tradeDate", data.get("query_date"));
                        factorValue.put("factorValue", data.get("change_rate"));
                        factorValues.add(factorValue);
                    }
                }
                break;
        }

        log.info("计算出{}条因子值数据，fundCode: {}", factorValues.size(), fundCode);
        
        if (!factorValues.isEmpty()) {
            log.info("第一条因子值: {}", factorValues.get(0));
        }

        return factorValues;
    }

    /**
     * 计算波动率因子（收益率的标准差）
     */
    private List<Map<String, Object>> calculateVolatilityFactor(String fundCode,
                                                                 List<Map<String, Object>> navData) {
        List<Map<String, Object>> factorValues = new ArrayList<>();
        List<Double> returns = new ArrayList<>();

        // 收集收益率
        for (Map<String, Object> data : navData) {
            if (data.get("change_rate") != null) {
                returns.add(((Number) data.get("change_rate")).doubleValue());
            }
        }

        if (returns.size() < 2) {
            return factorValues;
        }

        // 计算标准差（波动率）
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream()
                .mapToDouble(r -> Math.pow(r - mean, 2))
                .average()
                .orElse(0.0);
        double volatility = Math.sqrt(variance);

        // 为每个日期分配相同的波动率值（滚动窗口计算更准确，这里简化处理）
        for (Map<String, Object> data : navData) {
            if (data.get("query_date") != null) {
                Map<String, Object> factorValue = new HashMap<>();
                factorValue.put("fundCode", fundCode);
                factorValue.put("tradeDate", data.get("query_date"));
                factorValue.put("factorValue", BigDecimal.valueOf(volatility).setScale(6, RoundingMode.HALF_UP));
                factorValues.add(factorValue);
            }
        }

        log.info("计算出{}条因子值数据，fundCode: {}", factorValues.size(), fundCode);
        
        if (!factorValues.isEmpty()) {
            log.info("第一条因子值: {}", factorValues.get(0));
        }

        return factorValues;
    }

    /**
     * 计算最大回撤因子
     */
    private List<Map<String, Object>> calculateMaxDrawdownFactor(String fundCode,
                                                                   List<Map<String, Object>> navData) {
        List<Map<String, Object>> factorValues = new ArrayList<>();

        if (navData.isEmpty()) {
            return factorValues;
        }

        double maxNav = Double.MIN_VALUE;
        double maxDrawdown = 0.0;

        for (Map<String, Object> data : navData) {
            if (data.get("unit_nav") != null) {
                double nav = ((Number) data.get("unit_nav")).doubleValue();
                if (nav > maxNav) {
                    maxNav = nav;
                }
                double drawdown = (maxNav - nav) / maxNav;
                if (drawdown > maxDrawdown) {
                    maxDrawdown = drawdown;
                }
            }
        }

        // 为每个日期分配相同的最大回撤值
        for (Map<String, Object> data : navData) {
            if (data.get("query_date") != null) {
                Map<String, Object> factorValue = new HashMap<>();
                factorValue.put("fundCode", fundCode);
                factorValue.put("tradeDate", data.get("query_date"));
                factorValue.put("factorValue", BigDecimal.valueOf(maxDrawdown).setScale(6, RoundingMode.HALF_UP));
                factorValues.add(factorValue);
            }
        }

        log.info("计算出{}条因子值数据，fundCode: {}", factorValues.size(), fundCode);
        
        if (!factorValues.isEmpty()) {
            log.info("第一条因子值: {}", factorValues.get(0));
        }

        return factorValues;
    }

    /**
     * 计算夏普比率因子
     */
    private List<Map<String, Object>> calculateSharpeRatioFactor(String fundCode,
                                                                   List<Map<String, Object>> navData) {
        List<Map<String, Object>> factorValues = new ArrayList<>();
        List<Double> returns = new ArrayList<>();

        for (Map<String, Object> data : navData) {
            if (data.get("change_rate") != null) {
                returns.add(((Number) data.get("change_rate")).doubleValue());
            }
        }

        if (returns.size() < 2) {
            return factorValues;
        }

        // 计算均值和标准差
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream()
                .mapToDouble(r -> Math.pow(r - mean, 2))
                .average()
                .orElse(0.0);
        double std = Math.sqrt(variance);

        // 夏普比率 = 均值 / 标准差（简化版，不考虑无风险利率）
        double sharpeRatio = std > 0 ? mean / std : 0.0;

        for (Map<String, Object> data : navData) {
            if (data.get("query_date") != null) {
                Map<String, Object> factorValue = new HashMap<>();
                factorValue.put("fundCode", fundCode);
                factorValue.put("tradeDate", data.get("query_date"));
                factorValue.put("factorValue", BigDecimal.valueOf(sharpeRatio).setScale(6, RoundingMode.HALF_UP));
                factorValues.add(factorValue);
            }
        }

        log.info("计算出{}条因子值数据，fundCode: {}", factorValues.size(), fundCode);
        
        if (!factorValues.isEmpty()) {
            log.info("第一条因子值: {}", factorValues.get(0));
        }

        return factorValues;
    }

    /**
     * 计算动量因子（N日收益率）
     */
    private List<Map<String, Object>> calculateMomentumFactor(String fundCode,
                                                                List<Map<String, Object>> navData,
                                                                int period) {
        List<Map<String, Object>> factorValues = new ArrayList<>();

        for (int i = period; i < navData.size(); i++) {
            Map<String, Object> currentData = navData.get(i);
            Map<String, Object> pastData = navData.get(i - period);

            if (currentData.get("unit_nav") != null && pastData.get("unit_nav") != null) {
                double currentNav = ((Number) currentData.get("unit_nav")).doubleValue();
                double pastNav = ((Number) pastData.get("unit_nav")).doubleValue();

                if (pastNav > 0) {
                    double momentum = (currentNav - pastNav) / pastNav;

                    Map<String, Object> factorValue = new HashMap<>();
                    factorValue.put("fundCode", fundCode);
                    factorValue.put("tradeDate", currentData.get("query_date"));
                    factorValue.put("factorValue", BigDecimal.valueOf(momentum).setScale(6, RoundingMode.HALF_UP));
                    factorValues.add(factorValue);
                }
            }
        }

        log.info("计算出{}条因子值数据，fundCode: {}", factorValues.size(), fundCode);
        
        if (!factorValues.isEmpty()) {
            log.info("第一条因子值: {}", factorValues.get(0));
        }

        return factorValues;
    }

    /**
     * 计算均值回归因子
     */
    private List<Map<String, Object>> calculateMeanReversionFactor(String fundCode,
                                                                     List<Map<String, Object>> navData) {
        List<Map<String, Object>> factorValues = new ArrayList<>();

        if (navData.isEmpty()) {
            return factorValues;
        }

        // 计算净值均值
        double sum = navData.stream()
                .filter(data -> data.get("unit_nav") != null)
                .mapToDouble(data -> ((Number) data.get("unit_nav")).doubleValue())
                .sum();
        double mean = sum / navData.size();

        // 计算每个日期与均值的偏离度
        for (Map<String, Object> data : navData) {
            if (data.get("unit_nav") != null && mean > 0) {
                double nav = ((Number) data.get("unit_nav")).doubleValue();
                double deviation = (nav - mean) / mean;

                Map<String, Object> factorValue = new HashMap<>();
                factorValue.put("fundCode", fundCode);
                factorValue.put("tradeDate", data.get("query_date"));
                factorValue.put("factorValue", BigDecimal.valueOf(deviation).setScale(6, RoundingMode.HALF_UP));
                factorValues.add(factorValue);
            }
        }

        log.info("计算出{}条因子值数据，fundCode: {}", factorValues.size(), fundCode);
        
        if (!factorValues.isEmpty()) {
            log.info("第一条因子值: {}", factorValues.get(0));
        }

        return factorValues;
    }

    @Override
    public List<Map<String, Object>> calculateFundReturnRates(LocalDate startDate, LocalDate endDate) {
        // 从fund_etf_spot_ths表获取下期收益率
        return fundFactorMapper.selectFundNextReturnRates(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> calculateFundReturnRates(LocalDate startDate, LocalDate endDate, String fundCode) {
        // 从fund_etf_spot_ths表获取下期收益率
        if (fundCode != null && !fundCode.trim().isEmpty()) {
            // 获取指定基金的下期收益率
            log.info("查询指定基金 {} 的收益率数据，日期范围: {} 到 {}", fundCode, startDate, endDate);
            
            // 添加测试验证
            Integer paramCount = fundFactorMapper.testFundCodeCount(fundCode);
            Integer specificCount = fundFactorMapper.testSpecificFundCount();
            log.info("参数验证 - 传入基金代码 {} 的记录数: {}", fundCode, paramCount);
            log.info("参数验证 - 固定基金代码 159001 的记录数: {}", specificCount);
            
            List<Map<String, Object>> result;
            try {
                // 先使用最简单的查询测试
                List<Map<String, Object>> simpleResult = fundFactorMapper.selectSimpleFundData(fundCode, startDate, endDate);
                log.info("简单查询结果返回 {} 条记录", simpleResult.size());
                
                if (!simpleResult.isEmpty()) {
                    log.info("简单查询第一条记录: {}", simpleResult.get(0));
                }
                
                // 再使用 V2 方法（更严格的查询）
                result = fundFactorMapper.selectFundNextReturnRatesByCodeV2(startDate, endDate, fundCode);
                log.info("V2 查询结果返回 {} 条记录", result.size());
                
                // 如果结果数量不对，使用原方法对比
                if (result.size() > 1000) {
                    log.warn("V2 查询结果异常，使用原方法对比");
                    List<Map<String, Object>> originalResult = fundFactorMapper.selectFundNextReturnRatesByCode(startDate, endDate, fundCode);
                    log.info("原方法查询结果返回 {} 条记录", originalResult.size());
                    
                    if (originalResult.size() < 1000 && !originalResult.isEmpty()) {
                        log.info("使用原方法结果，第一条记录: {}", originalResult.get(0));
                        result = originalResult;
                    }
                }
            } catch (Exception e) {
                log.warn("查询失败，使用原方法: {}", e.getMessage());
                result = fundFactorMapper.selectFundNextReturnRatesByCode(startDate, endDate, fundCode);
                log.info("原方法查询结果返回 {} 条记录", result.size());
            }
            
            if (!result.isEmpty()) {
                log.info("最终返回第一条记录示例: {}", result.get(0));
            }
            return result;
        } else {
            // 获取所有基金的下期收益率
            log.info("查询所有基金的收益率数据，日期范围: {} 到 {}", startDate, endDate);
            List<Map<String, Object>> result = fundFactorMapper.selectFundNextReturnRates(startDate, endDate);
            log.info("查询结果返回 {} 条记录", result.size());
            return result;
        }
    }

    @Override
    public Map<LocalDate, BigDecimal> calculateSingleFundFactorValues(Integer factorId,
                                                                        String fundCode,
                                                                        LocalDate startDate,
                                                                        LocalDate endDate) {
        List<Map<String, Object>> factorValues = calculateFundFactorValues(factorId, fundCode, startDate, endDate);

        Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();
        for (Map<String, Object> value : factorValues) {
            LocalDate date = (LocalDate) value.get("tradeDate");
            BigDecimal factorValue = (BigDecimal) value.get("factorValue");
            if (date != null && factorValue != null) {
                result.put(date, factorValue);
            }
        }

        return result;
    }

    /**
     * 根据公式计算因子值
     */
    private List<Map<String, Object>> calculateByFormula(FactorBase baseFactor,
                                                          String fundCode,
                                                          LocalDate startDate,
                                                          LocalDate endDate) {
        // 解析公式
        ParsedFormula parsed = formulaParser.parseFormula(baseFactor.getFactorFormula());
        if (!parsed.isValid()) {
            log.warn("公式解析失败，factorId: {}, error: {}，回退到factor_code方式",
                    baseFactor.getBaseId(), parsed.getErrorMessage());
            return calculateSingleFundFactorValuesByCode(baseFactor.getFactorCode(), fundCode, startDate, endDate);
        }

        String expression = parsed.getExpression();
        List<Map<String, Object>> navData = fundFactorMapper.selectFundNavData(fundCode, startDate, endDate);
        List<Map<String, Object>> factorValues = new ArrayList<>();

        // 根据表达式类型计算
        if (expression.equals("change_rate")) {
            // 直接使用change_rate
            for (Map<String, Object> data : navData) {
                if (data.get("change_rate") != null) {
                    Map<String, Object> factorValue = new HashMap<>();
                    factorValue.put("fundCode", fundCode);
                    factorValue.put("tradeDate", data.get("query_date"));
                    factorValue.put("factorValue", data.get("change_rate"));
                    factorValues.add(factorValue);
                }
            }
        } else if (expression.startsWith("STD(")) {
            // 标准差函数：STD(字段, 周期)
            factorValues = calculateStdByFormula(expression, fundCode, navData);
        } else if (expression.startsWith("MEAN(")) {
            // 均值函数：MEAN(字段, 周期)
            factorValues = calculateMeanByFormula(expression, fundCode, navData);
        } else if (expression.startsWith("MOMENTUM(") || expression.startsWith("RETURN(")) {
            // 动量/收益率函数：MOMENTUM(字段, 周期) 或 RETURN(字段, 周期)
            factorValues = calculateMomentumByFormula(expression, fundCode, navData);
        } else if (expression.equals("MAX_DRAWDOWN(unit_nav)") || expression.startsWith("MAX_DRAWDOWN(")) {
            // 最大回撤函数
            factorValues = calculateMaxDrawdownFactor(fundCode, navData);
        } else if (expression.contains("/") && expression.contains("STD")) {
            // 夏普比率：MEAN(...) / STD(...)
            factorValues = calculateSharpeByFormula(expression, fundCode, navData);
        } else if (expression.contains("-") && expression.contains("/")) {
            // 简单数学表达式，如：(unit_nav - prev_unit_nav) / prev_unit_nav
            factorValues = calculateSimpleExpression(expression, fundCode, navData);
        } else {
            log.warn("不支持的表达式格式: {}，回退到factor_code方式", expression);
            return calculateSingleFundFactorValuesByCode(baseFactor.getFactorCode(), fundCode, startDate, endDate);
        }

        log.info("计算出{}条因子值数据，fundCode: {}", factorValues.size(), fundCode);
        
        if (!factorValues.isEmpty()) {
            log.info("第一条因子值: {}", factorValues.get(0));
        }

        return factorValues;
    }

    /**
     * 解析函数参数：STD(change_rate, 20) -> ["change_rate", "20"]
     */
    private String[] parseFunctionParams(String expression) {
        int start = expression.indexOf('(');
        int end = expression.lastIndexOf(')');
        if (start < 0 || end < 0) {
            return new String[0];
        }
        String params = expression.substring(start + 1, end);
        return params.split(",");
    }

    /**
     * 根据公式计算标准差
     */
    private List<Map<String, Object>> calculateStdByFormula(String expression,
                                                              String fundCode,
                                                              List<Map<String, Object>> navData) {
        String[] params = parseFunctionParams(expression);
        if (params.length < 2) {
            return Collections.emptyList();
        }

        String field = params[0].trim();
        int period = Integer.parseInt(params[1].trim());

        List<Map<String, Object>> factorValues = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        for (Map<String, Object> data : navData) {
            Object fieldValue = data.get(field);
            if (fieldValue != null) {
                values.add(((Number) fieldValue).doubleValue());

                if (values.size() >= period) {
                    // 计算最近period个值的标准差
                    List<Double> window = values.subList(values.size() - period, values.size());
                    double mean = window.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    double variance = window.stream()
                            .mapToDouble(v -> Math.pow(v - mean, 2))
                            .average()
                            .orElse(0.0);
                    double std = Math.sqrt(variance);

                    Map<String, Object> factorValue = new HashMap<>();
                    factorValue.put("fundCode", fundCode);
                    factorValue.put("tradeDate", data.get("query_date"));
                    factorValue.put("factorValue", BigDecimal.valueOf(std).setScale(6, RoundingMode.HALF_UP));
                    factorValues.add(factorValue);
                }
            }
        }

        log.info("计算出{}条因子值数据，fundCode: {}", factorValues.size(), fundCode);
        
        if (!factorValues.isEmpty()) {
            log.info("第一条因子值: {}", factorValues.get(0));
        }

        return factorValues;
    }

    /**
     * 根据公式计算均值
     */
    private List<Map<String, Object>> calculateMeanByFormula(String expression,
                                                              String fundCode,
                                                              List<Map<String, Object>> navData) {
        String[] params = parseFunctionParams(expression);
        if (params.length < 2) {
            return Collections.emptyList();
        }

        String field = params[0].trim();
        int period = Integer.parseInt(params[1].trim());

        List<Map<String, Object>> factorValues = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        for (Map<String, Object> data : navData) {
            Object fieldValue = data.get(field);
            if (fieldValue != null) {
                values.add(((Number) fieldValue).doubleValue());

                if (values.size() >= period) {
                    List<Double> window = values.subList(values.size() - period, values.size());
                    double mean = window.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

                    Map<String, Object> factorValue = new HashMap<>();
                    factorValue.put("fundCode", fundCode);
                    factorValue.put("tradeDate", data.get("query_date"));
                    factorValue.put("factorValue", BigDecimal.valueOf(mean).setScale(6, RoundingMode.HALF_UP));
                    factorValues.add(factorValue);
                }
            }
        }

        log.info("计算出{}条因子值数据，fundCode: {}", factorValues.size(), fundCode);
        
        if (!factorValues.isEmpty()) {
            log.info("第一条因子值: {}", factorValues.get(0));
        }

        return factorValues;
    }

    /**
     * 根据公式计算动量/收益率
     */
    private List<Map<String, Object>> calculateMomentumByFormula(String expression,
                                                                   String fundCode,
                                                                   List<Map<String, Object>> navData) {
        String[] params = parseFunctionParams(expression);
        if (params.length < 2) {
            return Collections.emptyList();
        }

        String field = params[0].trim();
        int period = Integer.parseInt(params[1].trim());

        List<Map<String, Object>> factorValues = new ArrayList<>();

        for (int i = period; i < navData.size(); i++) {
            Map<String, Object> currentData = navData.get(i);
            Map<String, Object> pastData = navData.get(i - period);

            Object currentValue = currentData.get(field);
            Object pastValue = pastData.get(field);

            if (currentValue != null && pastValue != null) {
                double current = ((Number) currentValue).doubleValue();
                double past = ((Number) pastValue).doubleValue();

                if (past > 0) {
                    double momentum = (current - past) / past;

                    Map<String, Object> factorValue = new HashMap<>();
                    factorValue.put("fundCode", fundCode);
                    factorValue.put("tradeDate", currentData.get("query_date"));
                    factorValue.put("factorValue", BigDecimal.valueOf(momentum).setScale(6, RoundingMode.HALF_UP));
                    factorValues.add(factorValue);
                }
            }
        }

        log.info("计算出{}条因子值数据，fundCode: {}", factorValues.size(), fundCode);
        
        if (!factorValues.isEmpty()) {
            log.info("第一条因子值: {}", factorValues.get(0));
        }

        return factorValues;
    }

    /**
     * 根据公式计算夏普比率
     */
    private List<Map<String, Object>> calculateSharpeByFormula(String expression,
                                                                String fundCode,
                                                                List<Map<String, Object>> navData) {
        // 简化处理：假设表达式为 MEAN(change_rate, 20) / STD(change_rate, 20)
        // 实际应该解析更复杂的表达式，这里先简化
        List<Double> returns = new ArrayList<>();
        for (Map<String, Object> data : navData) {
            if (data.get("change_rate") != null) {
                returns.add(((Number) data.get("change_rate")).doubleValue());
            }
        }

        if (returns.size() < 2) {
            return Collections.emptyList();
        }

        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream()
                .mapToDouble(r -> Math.pow(r - mean, 2))
                .average()
                .orElse(0.0);
        double std = Math.sqrt(variance);
        double sharpeRatio = std > 0 ? mean / std : 0.0;

        List<Map<String, Object>> factorValues = new ArrayList<>();
        for (Map<String, Object> data : navData) {
            if (data.get("query_date") != null) {
                Map<String, Object> factorValue = new HashMap<>();
                factorValue.put("fundCode", fundCode);
                factorValue.put("tradeDate", data.get("query_date"));
                factorValue.put("factorValue", BigDecimal.valueOf(sharpeRatio).setScale(6, RoundingMode.HALF_UP));
                factorValues.add(factorValue);
            }
        }

        log.info("计算出{}条因子值数据，fundCode: {}", factorValues.size(), fundCode);
        
        if (!factorValues.isEmpty()) {
            log.info("第一条因子值: {}", factorValues.get(0));
        }

        return factorValues;
    }

    /**
     * 计算简单数学表达式，如：(unit_nav - prev_unit_nav) / prev_unit_nav
     */
    private List<Map<String, Object>> calculateSimpleExpression(String expression,
                                                                 String fundCode,
                                                                 List<Map<String, Object>> navData) {
        List<Map<String, Object>> factorValues = new ArrayList<>();

        // 简化处理：只支持 (unit_nav - prev_unit_nav) / prev_unit_nav 这种格式
        if (expression.contains("unit_nav") && expression.contains("prev_unit_nav")) {
            for (Map<String, Object> data : navData) {
                Object unitNav = data.get("unit_nav");
                Object prevUnitNav = data.get("prev_unit_nav");

                if (unitNav != null && prevUnitNav != null) {
                    double nav = ((Number) unitNav).doubleValue();
                    double prevNav = ((Number) prevUnitNav).doubleValue();

                    if (prevNav > 0) {
                        double result = (nav - prevNav) / prevNav;

                        Map<String, Object> factorValue = new HashMap<>();
                        factorValue.put("fundCode", fundCode);
                        factorValue.put("tradeDate", data.get("query_date"));
                        factorValue.put("factorValue", BigDecimal.valueOf(result).setScale(6, RoundingMode.HALF_UP));
                        factorValues.add(factorValue);
                    }
                }
            }
        }

        log.info("计算出{}条因子值数据，fundCode: {}", factorValues.size(), fundCode);
        
        if (!factorValues.isEmpty()) {
            log.info("第一条因子值: {}", factorValues.get(0));
        }

        return factorValues;
    }
}

