package com.example.backend.service;

import com.example.backend.entity.StrategyFilterRule;
import com.example.backend.mapper.StrategyFilterRuleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StrategyFilterRuleService {

    private final StrategyFilterRuleMapper ruleMapper;

    public StrategyFilterRuleService(StrategyFilterRuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
    }

    @Transactional
    public void saveFilterRules(Long strategyId, List<StrategyFilterRule> rules) {
        // 先删除该策略的所有选基规则
        ruleMapper.deleteByStrategyId(strategyId);
        
        // 插入新规则
        if (rules != null && !rules.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (StrategyFilterRule rule : rules) {
                rule.setStrategyId(strategyId);
                rule.setCreatedAt(now);
                rule.setUpdatedAt(now);
                ruleMapper.insert(rule);
            }
        }
    }

    public List<StrategyFilterRule> getFilterRulesByStrategyId(Long strategyId) {
        return ruleMapper.findByStrategyId(strategyId);
    }

    @Transactional
    public void deleteByStrategyId(Long strategyId) {
        ruleMapper.deleteByStrategyId(strategyId);
    }
}

