package com.example.backend.controller;

import com.example.backend.service.StrategyWarningService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/strategy-warning")
public class StrategyWarningController {

    private final StrategyWarningService warningService;

    public StrategyWarningController(StrategyWarningService warningService) {
        this.warningService = warningService;
    }

    /**
     * 手动触发获取预警数据
     * @param request 包含 strategyId（可选，如果为null则保存为通用预警）
     * @param session HTTP会话
     * @return 保存的预警数量
     */
    @PostMapping("/fetch")
    public ResponseEntity<?> fetchWarnings(@RequestBody(required = false) WarningFetchRequest request,
                                           HttpSession session) {
        if (!isSessionValid(session)) {
            return unauthorized();
        }
        
        try {
            Long strategyId = (request != null && request.getStrategyId() != null) 
                ? request.getStrategyId() 
                : null;
            
            int count = warningService.fetchAndSaveWarnings(strategyId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "成功获取并保存预警数据");
            response.put("count", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取预警数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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

    public static class WarningFetchRequest {
        private Long strategyId;

        public Long getStrategyId() {
            return strategyId;
        }

        public void setStrategyId(Long strategyId) {
            this.strategyId = strategyId;
        }
    }
}

