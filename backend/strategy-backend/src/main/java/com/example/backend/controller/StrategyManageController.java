package com.example.backend.controller;

import com.example.backend.entity.Strategy;
import com.example.backend.service.StrategyDetailService;
import com.example.backend.service.StrategyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/strategy-management")
public class StrategyManageController {

    private final StrategyService strategyService;
    private final StrategyDetailService detailService;

    public StrategyManageController(StrategyService strategyService,
                                    StrategyDetailService detailService) {
        this.strategyService = strategyService;
        this.detailService = detailService;
    }

    @PostMapping
    public ResponseEntity<?> listStrategies(HttpSession session) {
        if (session == null || session.getAttribute("userId") == null) {
            return unauthorized();
        }
        // 只返回当前用户拥有的策略
        Long userId = getUserIdFromSession(session);
        if (userId == null) {
            return unauthorized();
        }
        List<Strategy> strategies = strategyService.listByOwner(userId);
        return ResponseEntity.ok(strategies);
    }

    @PostMapping("/new")
    public ResponseEntity<?> createStrategy(@RequestBody Map<String, Object> payload, HttpSession session) {
        if (session == null || session.getAttribute("userId") == null) {
            return unauthorized();
        }

        Map<String, Object> response = new HashMap<>();

        String strategyName = payload != null ? (String) payload.get("name") : null;
        String type = extractTypeString(payload);
        String description = payload != null ? (String) payload.get("description") : null;
        Integer riskLevel = extractRiskLevel(payload);
        
        // 从session获取当前用户ID作为策略拥有者
        Long owner = getUserIdFromSession(session);
        if (owner == null) {
            response.put("success", false);
            response.put("message", "无法获取用户信息");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Strategy strategy = strategyService.createStrategy(strategyName, type, description, riskLevel, owner, payload);
            response.put("success", true);
            response.put("strategyId", strategy.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            response.put("success", false);
            response.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception ex) {
            response.put("success", false);
            response.put("message", "创建策略失败：" + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/start")
    public ResponseEntity<?> startStrategy(@RequestBody StrategyActionRequest request, HttpSession session) {
        return changeStatus(request, session, "running", "策略已启动");
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopStrategy(@RequestBody StrategyActionRequest request, HttpSession session) {
        return changeStatus(request, session, "stop", "策略已停止");
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteStrategy(@RequestBody StrategyActionRequest request, HttpSession session) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        Map<String, Object> resp = new HashMap<>();
        if (request.getStrategyId() == null) {
            resp.put("success", false);
            resp.put("message", "strategyId 不能为空");
            return ResponseEntity.badRequest().body(resp);
        }
        
        // 获取当前用户ID
        Long userId = getUserIdFromSession(session);
        if (userId == null) {
            resp.put("success", false);
            resp.put("message", "无法获取用户信息");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }
        
        // 验证是否为策略拥有者
        if (!strategyService.isOwner(request.getStrategyId(), userId)) {
            resp.put("success", false);
            resp.put("message", "无权限操作：只有策略拥有者才能删除策略");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        
        boolean deleted = strategyService.deleteById(request.getStrategyId(), userId);
        resp.put("success", deleted);
        resp.put("message", deleted ? "策略已删除" : "策略不存在或删除失败");
        return deleted ? ResponseEntity.ok(resp) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
    }

    @PostMapping("/Detail")
    public ResponseEntity<?> detail(@RequestBody StrategyActionRequest request, HttpSession session) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        if (request.getStrategyId() == null) {
            return badRequest("strategyId 不能为空");
        }
        Optional<Strategy> optional = strategyService.findById(request.getStrategyId());
        if (optional.isEmpty()) {
            return notFound();
        }
        Strategy strategy = optional.get();
        Map<String, Object> detail = new HashMap<>();
        detail.put("strategy", strategy);
        
        // 获取因子配置
        var factors = strategyService.getFactorsByStrategyId(request.getStrategyId());
        detail.put("factors", factors != null ? factors : List.of());
        
        // 获取选基规则
        var filterRules = strategyService.getFilterRulesByStrategyId(request.getStrategyId());
        detail.put("filterRules", filterRules != null ? filterRules : List.of());
        
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/Chart")
    public ResponseEntity<?> chart(@RequestBody StrategyActionRequest request, HttpSession session) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        if (request.getStrategyId() == null) {
            return badRequest("strategyId 不能为空");
        }
        var points = detailService.getReturnPoints(request.getStrategyId());
        Map<String, Object> resp = new HashMap<>();
        resp.put("dataPoints", points);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/Holding")
    public ResponseEntity<?> holdings(@RequestBody StrategyActionRequest request, HttpSession session) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        if (request.getStrategyId() == null) {
            return badRequest("strategyId 不能为空");
        }
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/TradeHistory")
    public ResponseEntity<?> tradeHistory(@RequestBody StrategyActionRequest request, HttpSession session) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        if (request.getStrategyId() == null) {
            return badRequest("strategyId 不能为空");
        }
        return ResponseEntity.ok(List.of());
    }

    private String extractTypeString(Map<String, Object> payload) {
        if (CollectionUtils.isEmpty(payload)) {
            return null;
        }
        Object typeObj = payload.get("type");
        if (typeObj == null) {
            return null;
        }
        if (typeObj instanceof String str && !str.isBlank()) {
            return str;
        }
        // 兼容旧数据：如果是数字，转换为字符串
        if (typeObj instanceof Number number) {
            int typeNum = number.intValue();
            // 可以根据需要映射数字到类型字符串
            return switch (typeNum) {
                case 1 -> "基金策略";
                case 2 -> "ETF策略";
                case 3 -> "混合型";
                default -> String.valueOf(typeNum);
            };
        }
        return typeObj.toString();
    }

    private Integer extractRiskLevel(Map<String, Object> payload) {
        if (CollectionUtils.isEmpty(payload)) {
            return null;
        }
        Object riskLevelObj = payload.get("riskLevel");
        if (riskLevelObj == null) {
            return null;
        }
        if (riskLevelObj instanceof Number number) {
            return number.intValue();
        }
        if (riskLevelObj instanceof String str && !str.isBlank()) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private boolean isSessionValid(HttpSession session) {
        return session != null && session.getAttribute("userId") != null;
    }
    
    /**
     * 从session中安全地获取用户ID（Long类型）
     */
    private Long getUserIdFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return null;
        }
        // 处理Long和Integer类型
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        return null;
    }

    private ResponseEntity<Map<String, Object>> changeStatus(StrategyActionRequest request,
                                                             HttpSession session,
                                                             String status,
                                                             String successMsg) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        Map<String, Object> resp = new HashMap<>();
        if (request.getStrategyId() == null) {
            resp.put("success", false);
            resp.put("message", "strategyId 不能为空");
            return ResponseEntity.badRequest().body(resp);
        }
        
        // 获取当前用户ID
        Long userId = getUserIdFromSession(session);
        if (userId == null) {
            resp.put("success", false);
            resp.put("message", "无法获取用户信息");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }
        
        // 验证是否为策略拥有者
        if (!strategyService.isOwner(request.getStrategyId(), userId)) {
            resp.put("success", false);
            resp.put("message", "无权限操作：只有策略拥有者才能执行此操作");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        
        // 检查策略状态：审核中的策略不能启动或停止
        Optional<Strategy> strategyOpt = strategyService.findById(request.getStrategyId());
        if (strategyOpt.isPresent()) {
            Strategy strategy = strategyOpt.get();
            if ("paused".equals(strategy.getStatus())) {
                resp.put("success", false);
                resp.put("message", "审核中的策略无法启动或停止，请等待审核完成");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }
        }
        
        boolean updated = strategyService.updateStatus(request.getStrategyId(), status, userId);
        resp.put("success", updated);
        resp.put("message", updated ? successMsg : "策略不存在或状态未更新");
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
        return ResponseEntity.ok(resp);
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", message);
        return ResponseEntity.badRequest().body(resp);
    }
    private ResponseEntity<Map<String, Object>> unauthorized() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", "未登录或会话已过期");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
    }

    private ResponseEntity<Map<String, Object>> notFound() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", "策略不存在");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
    }

    public static class StrategyActionRequest {
        private Long strategyId;

        public Long getStrategyId() {
            return strategyId;
        }

        public void setStrategyId(Long strategyId) {
            this.strategyId = strategyId;
        }
    }
}

