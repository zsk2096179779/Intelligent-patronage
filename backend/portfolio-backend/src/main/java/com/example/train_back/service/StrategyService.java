package com.example.train_back.service;

import com.example.train_back.dto.StrategyListDTO;

import java.util.List;

/**
 * 策略Service接口
 */
public interface StrategyService {
    
    /**
     * 获取所有策略列表（用于下拉框选择）
     * @return 策略列表
     */
    List<StrategyListDTO> getAllStrategies();
}

