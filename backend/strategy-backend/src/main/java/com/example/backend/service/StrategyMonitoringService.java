package com.example.backend.service;

import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StrategyMonitoringService {

    private final StrategyMonitorMetricsMapper metricsMapper;
    private final StrategyWarningMapper warningMapper;
    private final StrategyProfitCurveMapper profitCurveMapper;
    private final StrategyHeatmapMapper heatmapMapper;
    private final ProfitCurveService profitCurveService;
    private final StrategyFactorService factorService;
    private final StrategyFilterRuleService filterRuleService;

    public StrategyMonitoringService(StrategyMonitorMetricsMapper metricsMapper,
                                     StrategyWarningMapper warningMapper,
                                     StrategyProfitCurveMapper profitCurveMapper,
                                     StrategyHeatmapMapper heatmapMapper,
                                     ProfitCurveService profitCurveService,
                                     StrategyFactorService factorService,
                                     StrategyFilterRuleService filterRuleService) {
        this.metricsMapper = metricsMapper;
        this.warningMapper = warningMapper;
        this.profitCurveMapper = profitCurveMapper;
        this.heatmapMapper = heatmapMapper;
        this.profitCurveService = profitCurveService;
        this.factorService = factorService;
        this.filterRuleService = filterRuleService;
    }

    /**
     * 获取策略监控指标
     * 如果 strategy_metrics 表中没有数据，返回 null（由 Controller 处理）
     */
    public StrategyMonitorMetrics getMetrics(Long strategyId) {
        StrategyMonitorMetrics metrics = metricsMapper.findByStrategyId(strategyId);
        // 如果 strategy_metrics 表中没有数据，返回 null
        // Controller 会将其转换为空 HashMap
        return metrics;
    }

    /**
     * 获取策略预警列表
     * 如果没有预警数据，返回空列表
     */
    public List<StrategyWarning> getWarnings(Long strategyId) {
        List<StrategyWarning> warnings = warningMapper.findByStrategyId(strategyId);
        return warnings != null ? warnings : new java.util.ArrayList<>();
    }

    /**
     * 获取策略收益曲线数据
     * 优先从数据库读取，如果没有数据，则基于策略因子配置和CSV数据生成
     * 按日期升序排列，如果没有数据，返回空列表
     */
    public List<StrategyProfitCurvePoint> getProfitCurve(Long strategyId) {
        // 先尝试从数据库读取
        List<StrategyProfitCurvePoint> curve = profitCurveMapper.findByStrategyId(strategyId);
        
        // 如果数据库中有数据，直接返回
        if (curve != null && !curve.isEmpty()) {
            return curve;
        }
        
        // 如果数据库中没有数据，基于策略因子配置生成
        try {
            // 获取策略因子配置
            List<StrategyFactor> strategyFactors = factorService.getFactorsByStrategyId(strategyId);
            if (strategyFactors == null || strategyFactors.isEmpty()) {
                return new java.util.ArrayList<>();
            }
            
            // 获取筛选规则
            List<StrategyFilterRule> filterRules = filterRuleService.getFilterRulesByStrategyId(strategyId);
            StrategyFilterRule filterRule = filterRules != null && !filterRules.isEmpty() 
                    ? filterRules.get(0) 
                    : null;
            
            // 生成收益曲线（使用90天前作为起始日期）
            LocalDate startDate = LocalDate.now().minusDays(90);
            List<StrategyProfitCurvePoint> generatedCurve = profitCurveService.generateProfitCurveWithExponentialModel(
                    strategyFactors, 
                    filterRule, 
                    startDate
            );
            
            return generatedCurve != null ? generatedCurve : new java.util.ArrayList<>();
            
        } catch (Exception e) {
            // 如果生成失败，返回空列表
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 获取策略持仓偏离度热力图数据
     * 如果没有数据或表不存在，返回空列表
     */
    public List<StrategyHeatmap> getHeatmap(Long strategyId) {
        try {
            List<StrategyHeatmap> heatmap = heatmapMapper.findByStrategyId(strategyId);
            return heatmap != null ? heatmap : new java.util.ArrayList<>();
        } catch (Exception e) {
            // 如果表不存在或其他数据库错误，返回空列表
            // 这样前端可以继续使用风险暴露矩阵数据
            return new java.util.ArrayList<>();
        }
    }
}

