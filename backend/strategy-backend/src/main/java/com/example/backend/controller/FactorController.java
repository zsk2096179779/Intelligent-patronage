package com.example.backend.controller;

import com.example.backend.entity.Factor;
import com.example.backend.entity.FactorBase;
import com.example.backend.service.FactorBaseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/factors")
public class FactorController {

    private final FactorBaseService factorBaseService;

    public FactorController(FactorBaseService factorBaseService) {
        this.factorBaseService = factorBaseService;
    }

    @GetMapping
    public ResponseEntity<?> listFactors(HttpSession session) {
        // 检查会话（如果需要认证）
        if (session == null || session.getAttribute("userId") == null) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", "未登录或会话已过期");
            return ResponseEntity.status(401).body(resp);
        }

        try {
            // 从数据库查询所有有效的因子
            List<FactorBase> factorBases = factorBaseService.getAllValidFactors();
            
            // 转换为前端需要的格式（兼容旧的Factor实体）
            List<Factor> factors = factorBases.stream()
                .map(fb -> {
                    Factor factor = new Factor();
                    factor.setId(fb.getBaseId().longValue());
                    factor.setName(fb.getFactorName());
                    factor.setCode(fb.getFactorCode());
                    factor.setDescription(fb.getDataDesc() != null ? fb.getDataDesc() : "");
                    return factor;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(factors);
        } catch (Exception e) {
            // 如果查询失败，返回模拟数据作为降级方案
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", "获取因子列表失败: " + e.getMessage());
            resp.put("data", getMockFactors());
            return ResponseEntity.ok(resp);
        }
    }

    // 模拟因子数据（降级方案）
    private List<Factor> getMockFactors() {
        List<Factor> factors = new ArrayList<>();
        
        factors.add(createFactor(1L, "市盈率因子", "PE", "基于市盈率的估值因子"));
        factors.add(createFactor(2L, "市净率因子", "PB", "基于市净率的估值因子"));
        factors.add(createFactor(3L, "ROE因子", "ROE", "净资产收益率因子"));
        factors.add(createFactor(4L, "营收增长率因子", "REVENUE_GROWTH", "营业收入增长率因子"));
        factors.add(createFactor(5L, "净利润增长率因子", "PROFIT_GROWTH", "净利润增长率因子"));
        factors.add(createFactor(6L, "波动率因子", "VOLATILITY", "价格波动率因子"));
        factors.add(createFactor(7L, "动量因子", "MOMENTUM", "价格动量因子"));
        factors.add(createFactor(8L, "换手率因子", "TURNOVER", "股票换手率因子"));
        
        return factors;
    }

    private Factor createFactor(Long id, String name, String code, String description) {
        Factor factor = new Factor();
        factor.setId(id);
        factor.setName(name);
        factor.setCode(code);
        factor.setDescription(description);
        return factor;
    }
}

