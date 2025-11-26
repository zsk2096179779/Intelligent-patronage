package com.example.backend.controller;

import com.example.backend.entity.RebalanceBacktestResult;
import com.example.backend.service.BacktestEngine;
import com.example.backend.service.RebalanceBacktestService;
import com.example.backend.service.StrategyConfigService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/strategy-rebalance")
public class StrategyRebalanceController {

    private static final Logger log = LoggerFactory.getLogger(StrategyRebalanceController.class);

    private final StrategyConfigService configService;
    private final RebalanceBacktestService backtestService;
    private final BacktestEngine backtestEngine;

    public StrategyRebalanceController(StrategyConfigService configService,
                                       RebalanceBacktestService backtestService,
                                       BacktestEngine backtestEngine) {
        this.configService = configService;
        this.backtestService = backtestService;
        this.backtestEngine = backtestEngine;
    }

    @PostMapping("/Detail")
    public ResponseEntity<?> detail(@RequestBody StrategyIdRequest request, HttpSession session) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        if (request == null || request.getId() == null) {
            return badRequest("strategyId 不能为空");
        }
        com.example.backend.entity.StrategyConfig config = configService.getOrCreate(request.getId());
        return ResponseEntity.ok(config);
    }

    @PostMapping("/Update")
    public ResponseEntity<?> update(@RequestBody com.example.backend.entity.StrategyConfig config, HttpSession session) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        try {
            configService.save(config);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "配置更新成功");
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(resp);
        }
    }

    @PostMapping("/HuiCe")
    public ResponseEntity<?> backtest(@RequestBody BacktestRequest request, HttpSession session) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        if (request.getStrategyId() == null) {
            return badRequest("strategyId 不能为空");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return badRequest("开始日期和结束日期不能为空");
        }
        if (request.getInitialCapital() == null || request.getInitialCapital() <= 0) {
            return badRequest("初始资金必须大于0");
        }
        
        try {
            // 使用回测引擎执行回测
            BacktestEngine.BacktestResult result = backtestEngine.runBacktest(
                    request.getStrategyId(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getInitialCapital(),
                    request.getTransactionFee() != null ? request.getTransactionFee() : 0.15,
                    request.getSlippage() != null ? request.getSlippage() : 0.1
            );
            
            // 保存回测结果到数据库
            RebalanceBacktestResult savedResult = backtestService.recordResult(
                    request.getStrategyId(),
                    result.getCumulativeReturn(),
                    result.getMaxDrawdown(),
                    result.getSharpeRatio(),
                    result.getTrades()
            );
            
            Map<String, Object> resp = new HashMap<>();
            resp.put("cumulativeReturn", savedResult.getCumulativeReturn());
            resp.put("maxDrawdown", savedResult.getMaxDrawdown());
            resp.put("sharpeRatio", savedResult.getSharpeRatio());
            resp.put("trades", savedResult.getTrades());
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            log.error("回测执行失败", ex);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", "回测执行失败: " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()));
            // 即使失败也返回一个默认结果，避免前端显示错误
            resp.put("cumulativeReturn", 0.0);
            resp.put("maxDrawdown", 0.0);
            resp.put("sharpeRatio", 0.0);
            resp.put("trades", 0);
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


    public static class StrategyIdRequest {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    public static class BacktestRequest {
        private Long strategyId;
        private LocalDate startDate;
        private LocalDate endDate;
        private Double initialCapital;
        private Double transactionFee;
        private Double slippage;

        public Long getStrategyId() {
            return strategyId;
        }

        public void setStrategyId(Long strategyId) {
            this.strategyId = strategyId;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public Double getInitialCapital() {
            return initialCapital;
        }

        public void setInitialCapital(Double initialCapital) {
            this.initialCapital = initialCapital;
        }

        public Double getTransactionFee() {
            return transactionFee;
        }

        public void setTransactionFee(Double transactionFee) {
            this.transactionFee = transactionFee;
        }

        public Double getSlippage() {
            return slippage;
        }

        public void setSlippage(Double slippage) {
            this.slippage = slippage;
        }
    }
}

