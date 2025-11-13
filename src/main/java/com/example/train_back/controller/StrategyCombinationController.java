package com.example.train_back.controller;

import com.example.train_back.dto.*;
import com.example.train_back.service.PortfolioHoldingService;
import com.example.train_back.service.PortfolioProductParamsService;
import com.example.train_back.service.StrategyCombinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 策略组合Controller（包含创建和审核功能）
 */
@RestController
@RequestMapping("/api/strategy-combination")
public class StrategyCombinationController {
    
    @Autowired
    private StrategyCombinationService strategyCombinationService;
    
    @Autowired
    private PortfolioProductParamsService productParamsService;
    
    @Autowired
    private PortfolioHoldingService holdingService;
    
    /**
     * 创建组合产品接口
     * @param request 创建组合请求
     * @return 响应结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPortfolio(@RequestBody CreatePortfolioRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 参数验证
            if (request == null || request.getName() == null || request.getName().trim().isEmpty()) {
                response.put("code", 400);
                response.put("message", "创建失败：组合名称不能为空");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.getStrategyRefId() == null) {
                response.put("code", 400);
                response.put("message", "创建失败：策略引用ID不能为空");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 创建组合
            Integer portfolioId = strategyCombinationService.createPortfolio(
                    request.getName(),
                    request.getRiskLevel(),
                    request.getStrategyType(),
                    request.getStrategyRefId(),
                    request.getSummary(),
                    request.getTargetInvestor()
            );
            
            if (portfolioId != null) {
                response.put("code", 200);
                response.put("message", "创建成功");
                Map<String, Object> data = new HashMap<>();
                data.put("portfolioId", portfolioId);
                response.put("data", data);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 500);
                response.put("message", "创建失败：数据库插入失败");
                response.put("data", null);
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "创建失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 审核通过接口
     * @param id 组合ID
     * @return 响应结果
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approvePortfolio(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = strategyCombinationService.approvePortfolio(id);
            if (success) {
                response.put("code", 200);
                response.put("message", "审核通过成功");
                response.put("data", null);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "审核失败：组合不存在或状态不正确（可能已经审核过）");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "审核失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 审核拒绝接口
     * @param id 组合ID
     * @param request 拒绝原因
     * @return 响应结果
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectPortfolio(
            @PathVariable Integer id,
            @RequestBody RejectRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String reason = request != null ? request.getReason() : null;
            boolean success = strategyCombinationService.rejectPortfolio(id, reason);
            if (success) {
                response.put("code", 200);
                response.put("message", "审核拒绝成功");
                response.put("data", null);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "审核失败：组合不存在");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "审核失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 更新组合基础信息
     * @param id 组合ID
     * @param request 基础信息请求
     * @return 响应结果
     */
    @PutMapping("/{id}/basic-info")
    public ResponseEntity<Map<String, Object>> updateBasicInfo(
            @PathVariable Integer id,
            @RequestBody UpdatePortfolioBasicInfoDTO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = strategyCombinationService.updatePortfolioBasicInfo(id, request);
            if (success) {
                response.put("code", 200);
                response.put("message", "更新成功");
                response.put("data", null);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "更新失败：组合不存在");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "更新失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 保存产品参数
     * @param id 组合ID
     * @param paramsDTO 产品参数
     * @return 响应结果
     */
    @PostMapping("/{id}/product-params")
    public ResponseEntity<Map<String, Object>> saveProductParams(
            @PathVariable Integer id,
            @RequestBody ProductParamsDTO paramsDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = productParamsService.saveOrUpdateProductParams(id, paramsDTO);
            if (success) {
                response.put("code", 200);
                response.put("message", "保存成功");
                response.put("data", null);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 500);
                response.put("message", "保存失败");
                response.put("data", null);
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "保存失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 查询产品参数
     * @param id 组合ID
     * @return 响应结果
     */
    @GetMapping("/{id}/product-params")
    public ResponseEntity<Map<String, Object>> getProductParams(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProductParamsDTO params = productParamsService.getProductParams(id);
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", params);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 保存持仓
     * @param id 组合ID
     * @param holdings 持仓列表
     * @return 响应结果
     */
    @PostMapping("/{id}/holdings")
    public ResponseEntity<Map<String, Object>> saveHoldings(
            @PathVariable Integer id,
            @RequestBody List<HoldingDTO> holdings) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 验证权重总和
            if (!holdingService.validateWeightSum(holdings)) {
                response.put("code", 400);
                response.put("message", "保存失败：持仓权重总和必须为100%（或 1.0）");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean success = holdingService.saveHoldings(id, holdings);
            if (success) {
                response.put("code", 200);
                response.put("message", "保存成功");
                response.put("data", null);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 500);
                response.put("message", "保存失败");
                response.put("data", null);
                return ResponseEntity.status(500).body(response);
            }
        } catch (IllegalArgumentException e) {
            response.put("code", 400);
            response.put("message", "保存失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "保存失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 查询持仓
     * @param id 组合ID
     * @return 响应结果
     */
    @GetMapping("/{id}/holdings")
    public ResponseEntity<Map<String, Object>> getHoldings(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<HoldingDTO> holdings = holdingService.getHoldings(id);
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", holdings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 提交审核
     * @param id 组合ID
     * @return 响应结果
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<Map<String, Object>> submitForReview(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = strategyCombinationService.submitForReview(id);
            if (success) {
                response.put("code", 200);
                response.put("message", "提交审核成功");
                response.put("data", null);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "提交失败：组合不存在或状态不正确（只能提交草稿状态的组合）");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "提交失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}

