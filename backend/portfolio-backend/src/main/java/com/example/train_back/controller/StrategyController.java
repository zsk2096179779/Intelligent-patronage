package com.example.train_back.controller;

import com.example.train_back.dto.StrategyListDTO;
import com.example.train_back.service.StrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 策略Controller
 */
@RestController
@RequestMapping("/api/strategies")
public class StrategyController {
    
    @Autowired
    private StrategyService strategyService;
    
    /**
     * 获取策略列表接口（用于前端下拉框选择）
     * @return 策略列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStrategies() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<StrategyListDTO> strategies = strategyService.getAllStrategies();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", strategies);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}

