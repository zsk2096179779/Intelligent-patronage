package com.example.backend.service;

import com.example.backend.entity.RebalanceConfig;
import com.example.backend.mapper.RebalanceConfigMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RebalanceConfigService {

    private final RebalanceConfigMapper mapper;

    public RebalanceConfigService(RebalanceConfigMapper mapper) {
        this.mapper = mapper;
    }

    public RebalanceConfig getOrCreate(Long strategyId) {
        if (strategyId == null) {
            return defaultConfig(null);
        }
        RebalanceConfig config = mapper.findByStrategyId(strategyId);
        if (config != null) {
            return config;
        }
        RebalanceConfig defaults = defaultConfig(strategyId);
        mapper.upsert(defaults);
        return defaults;
    }

    public void save(RebalanceConfig config) {
        if (config.getStrategyId() == null) {
            throw new IllegalArgumentException("strategyId 不能为空");
        }
        config.setUpdateTime(LocalDateTime.now());
        mapper.upsert(config);
    }

    private RebalanceConfig defaultConfig(Long strategyId) {
        RebalanceConfig config = new RebalanceConfig();
        config.setStrategyId(strategyId);
        config.setActiveRebalancing(Boolean.TRUE);
        config.setTriggerByThreshold(Boolean.TRUE);
        config.setTriggerByPeriodic(Boolean.FALSE);
        config.setFrequency("monthly");
        config.setExecutionTime("09:30");
        config.setMaxAdjustmentRate(BigDecimal.valueOf(20));
        config.setStockDeviation(BigDecimal.valueOf(5));
        config.setBondDeviation(BigDecimal.valueOf(3));
        config.setCommodityDeviation(BigDecimal.valueOf(7));
        config.setCashDeviation(BigDecimal.valueOf(2));
        config.setUpdateTime(LocalDateTime.now());
        return config;
    }
}

