package com.example.train_back.controller;

import com.example.train_back.dto.FundFilterRequest;
import com.example.train_back.entity.Fund;
import com.example.train_back.service.FundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基金Controller
 */
@RestController
@RequestMapping("/api/funds")
public class FundController {
    
    @Autowired
    private FundService fundService;
    
    /**
     * 获取所有基金列表（用于下拉框选择）
     * @return 基金列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getFunds(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fundType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String operationCycle,
            @RequestParam(required = false) Double minFundSize,
            @RequestParam(required = false) Double maxFundSize,
            @RequestParam(required = false) Double minFeeRate,
            @RequestParam(required = false) Double maxFeeRate) {
        Map<String, Object> response = new HashMap<>();
        try {
            FundFilterRequest filterRequest = new FundFilterRequest();
            filterRequest.setKeyword(normalize(keyword));
            filterRequest.setFundType(normalize(fundType));
            filterRequest.setCategory(normalize(category));
            filterRequest.setOperationCycle(normalize(operationCycle));
            filterRequest.setMinFundSize(minFundSize);
            filterRequest.setMaxFundSize(maxFundSize);
            filterRequest.setMinFeeRate(minFeeRate);
            filterRequest.setMaxFeeRate(maxFeeRate);
            
            List<Fund> funds;
            if (filterRequest.hasAdvancedFilters()) {
                // 包含高级筛选条件（可携带关键字）
                funds = fundService.filterFunds(filterRequest);
            } else if (filterRequest.getKeyword() != null) {
                funds = fundService.searchFunds(filterRequest.getKeyword());
            } else {
                funds = fundService.getAllFunds();
            }
            
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", funds);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    /**
     * 根据基金代码查询基金信息
     * @param fundCode 基金代码
     * @return 基金信息
     */
    @GetMapping("/{fundCode}")
    public ResponseEntity<Map<String, Object>> getFundByCode(@PathVariable String fundCode) {
        Map<String, Object> response = new HashMap<>();
        try {
            Fund fund = fundService.getFundByCode(fundCode);
            if (fund != null) {
                response.put("code", 200);
                response.put("message", "查询成功");
                response.put("data", fund);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 404);
                response.put("message", "基金不存在");
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

