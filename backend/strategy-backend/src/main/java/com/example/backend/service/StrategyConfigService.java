package com.example.backend.service;

import com.example.backend.entity.StrategyConfig;
import com.example.backend.mapper.StrategyConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class StrategyConfigService {

    private final StrategyConfigMapper configMapper;

    public StrategyConfigService(StrategyConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    @Transactional
    public void save(StrategyConfig config) {
        if (config.getStrategyId() == null) {
            throw new IllegalArgumentException("策略ID不能为空");
        }
        
        LocalDateTime now = LocalDateTime.now();
        StrategyConfig existing = configMapper.findByStrategyId(config.getStrategyId());
        
        if (existing != null) {
            // 更新现有配置
            config.setId(existing.getId());
            config.setUpdatedAt(now);
            configMapper.update(config);
        } else {
            // 插入新配置
            if (config.getRebalancePeriod() == null) {
                config.setRebalancePeriod("monthly");
            }
            if (config.getMaxRebalanceRatio() == null) {
                config.setMaxRebalanceRatio(100);
            }
            if (config.getIsActiveRebalance() == null) {
                config.setIsActiveRebalance(true);
            }
            config.setCreatedAt(now);
            config.setUpdatedAt(now);
            configMapper.insert(config);
        }
    }

    public StrategyConfig getOrCreate(Long strategyId) {
        StrategyConfig config = configMapper.findByStrategyId(strategyId);
        if (config == null) {
            // 创建默认配置
            LocalDateTime now = LocalDateTime.now();
            config = new StrategyConfig();
            config.setStrategyId(strategyId);
            config.setRebalancePeriod("monthly");
            config.setMaxRebalanceRatio(100);
            config.setIsActiveRebalance(true);
            config.setCreatedAt(now);
            config.setUpdatedAt(now);
            configMapper.insert(config);
        }
        return config;
    }

    public StrategyConfig getByStrategyId(Long strategyId) {
        return configMapper.findByStrategyId(strategyId);
    }

    @Transactional
    public void deleteByStrategyId(Long strategyId) {
        configMapper.deleteByStrategyId(strategyId);
    }
}

