package com.example.train_back.service.impl;

import com.example.train_back.dto.StrategyListDTO;
import com.example.train_back.mapper.StrategyMapper;
import com.example.train_back.service.StrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 策略Service实现类
 */
@Service
public class StrategyServiceImpl implements StrategyService {
    
    @Autowired
    private StrategyMapper strategyMapper;
    
    @Override
    public List<StrategyListDTO> getAllStrategies() {
        return strategyMapper.selectAllStrategies();
    }
}

