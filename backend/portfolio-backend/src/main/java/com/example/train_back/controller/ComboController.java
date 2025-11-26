package com.example.train_back.controller;

import com.example.train_back.dto.PortfolioDetailDTO;
import com.example.train_back.service.StrategyCombinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    
    /**
     * 根据组合ID查询组合详情（包含年化收益、累计收益等指标）
     * 接口地址：GET /combos/{id}
     * 
     * @param id 组合ID
     * @return 组合详情，包含收益指标
     */
    @GetMapping("/combos/{id}")
    public ResponseEntity<Map<String, Object>> getComboDetail(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            PortfolioDetailDTO portfolioDetail = strategyCombinationService.getPortfolioDetailById(id);
            if (portfolioDetail != null) {
                response.put("code", 200);
                response.put("message", "查询成功");
                response.put("data", portfolioDetail);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 404);
                response.put("message", "组合不存在");
                response.put("data", null);
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}

