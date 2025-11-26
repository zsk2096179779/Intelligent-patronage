package com.example.backend.controller;

import com.example.backend.dto.RiskExposureMatrix;
import com.example.backend.entity.StrategyFilterRule;
import com.example.backend.entity.StrategyFactor;
import com.example.backend.service.StrategyMonitoringService;
import com.example.backend.service.StrategyWarningService;
import com.example.backend.service.RiskExposureMatrixService;
import com.example.backend.service.StrategyFactorService;
import com.example.backend.service.StrategyFilterRuleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/strategy-monitoring")
public class StrategyMonitoringController {

    private final StrategyMonitoringService monitoringService;
    private final StrategyWarningService warningService;
    private final RiskExposureMatrixService riskExposureMatrixService;
    private final StrategyFactorService factorService;
    private final StrategyFilterRuleService filterRuleService;

    public StrategyMonitoringController(StrategyMonitoringService monitoringService,
                                       StrategyWarningService warningService,
                                       RiskExposureMatrixService riskExposureMatrixService,
                                       StrategyFactorService factorService,
                                       StrategyFilterRuleService filterRuleService) {
        this.monitoringService = monitoringService;
        this.warningService = warningService;
        this.riskExposureMatrixService = riskExposureMatrixService;
        this.factorService = factorService;
        this.filterRuleService = filterRuleService;
    }

    @PostMapping("/Metrics")
    public ResponseEntity<?> metrics(@RequestBody(required = false) StrategyMonitorRequest request,
                                     HttpSession session) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        if (request == null || request.getId() == null) {
            return badRequest("strategyId 不能为空");
        }
        var metrics = monitoringService.getMetrics(request.getId());
        // 从 strategy 和 strategy_config 表读取数据，如果策略不存在则返回空对象
        return ResponseEntity.ok(metrics != null ? metrics : new HashMap<>());
    }

    @PostMapping("/Warnings")
    public ResponseEntity<?> warnings(@RequestBody(required = false) StrategyMonitorRequest request,
                                      HttpSession session) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        if (request == null || request.getId() == null) {
            return badRequest("strategyId 不能为空");
        }
        
        // 每次访问时先获取最新的预警数据
        try {
            warningService.fetchAndSaveWarnings(request.getId());
        } catch (Exception e) {
            // 即使获取失败，也返回已有的预警数据
            // 记录错误但不影响返回结果
        }
        
        // 返回数据库中的预警数据
        return ResponseEntity.ok(monitoringService.getWarnings(request.getId()));
    }

    @PostMapping("/ProfitCurve")
    public ResponseEntity<?> profitCurve(@RequestBody(required = false) StrategyMonitorRequest request,
                                         HttpSession session) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        if (request == null || request.getId() == null) {
            return badRequest("strategyId 不能为空");
        }
        return ResponseEntity.ok(monitoringService.getProfitCurve(request.getId()));
    }

    @PostMapping("/Heatmap")
    public ResponseEntity<?> heatmap(@RequestBody(required = false) StrategyMonitorRequest request,
                                     HttpSession session) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        if (request == null || request.getId() == null) {
            return badRequest("strategyId 不能为空");
        }
        return ResponseEntity.ok(monitoringService.getHeatmap(request.getId()));
    }

    @PostMapping("/RiskExposureMatrix")
    public ResponseEntity<?> riskExposureMatrix(@RequestBody(required = false) StrategyMonitorRequest request,
                                                 HttpSession session) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        if (request == null || request.getId() == null) {
            return badRequest("strategyId 不能为空");
        }
        
        try {
            Long strategyId = request.getId();
            
            // 获取策略因子配置
            List<StrategyFactor> strategyFactors = factorService.getFactorsByStrategyId(strategyId);
            if (strategyFactors == null || strategyFactors.isEmpty()) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", false);
                resp.put("message", "策略未配置因子，无法计算风险暴露矩阵");
                return ResponseEntity.badRequest().body(resp);
            }
            
            // 获取筛选规则（包含topN）
            List<StrategyFilterRule> filterRules = filterRuleService.getFilterRulesByStrategyId(strategyId);
            StrategyFilterRule filterRule = filterRules != null && !filterRules.isEmpty() 
                    ? filterRules.get(0) 
                    : null;
            
            // 计算风险暴露矩阵
            RiskExposureMatrix matrix = riskExposureMatrixService.calculateRiskExposureMatrix(
                    strategyFactors, 
                    filterRule
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", matrix);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", "计算风险暴露矩阵失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    private boolean isSessionValid(HttpSession session) {
        return session != null && session.getAttribute("userId") != null;
    }

    private ResponseEntity<Map<String, Object>> unauthorized() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", "未登录或会话已过期");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
    }

    private ResponseEntity<Map<String, Object>> badRequest(String msg) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", msg);
        return ResponseEntity.badRequest().body(resp);
    }

    public static class StrategyMonitorRequest {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

