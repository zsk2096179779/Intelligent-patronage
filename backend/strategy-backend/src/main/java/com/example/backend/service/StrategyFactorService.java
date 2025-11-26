package com.example.backend.service;

import com.example.backend.entity.StrategyFactor;
import com.example.backend.mapper.StrategyFactorMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StrategyFactorService {

    private final StrategyFactorMapper factorMapper;

    public StrategyFactorService(StrategyFactorMapper factorMapper) {
        this.factorMapper = factorMapper;
    }

    @Transactional
    public void saveFactors(Long strategyId, List<StrategyFactor> factors) {
        // 先删除该策略的所有因子
        factorMapper.deleteByStrategyId(strategyId);
        
        // 插入新因子
        if (factors != null && !factors.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (StrategyFactor factor : factors) {
                factor.setStrategyId(strategyId);
                factor.setCreatedAt(now);
                factor.setUpdatedAt(now);
                factorMapper.insert(factor);
            }
        }
    }

    public List<StrategyFactor> getFactorsByStrategyId(Long strategyId) {
        return factorMapper.findByStrategyId(strategyId);
    }

    @Transactional
    public void deleteByStrategyId(Long strategyId) {
        factorMapper.deleteByStrategyId(strategyId);
    }
}

