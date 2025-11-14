package com.example.train_back.controller;

import com.example.train_back.dto.PortfolioDetailDTO;
import com.example.train_back.service.StrategyCombinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 组合Controller（前端接口：/combos）
 */
@RestController
@RequestMapping("/")
public class ComboController {
    
    @Autowired
    private StrategyCombinationService strategyCombinationService;
    
    /**
     * 获取所有策略组合（前端接口：/combos）
     * @return 所有策略组合详情列表
     */
    @GetMapping("/combos")
    public ResponseEntity<Map<String, Object>> getCombos() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<PortfolioDetailDTO> portfolioDetails = strategyCombinationService.getAllPortfolioDetails();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", portfolioDetails);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}

