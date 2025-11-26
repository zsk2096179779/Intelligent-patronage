package com.example.backend.mapper;

import com.example.backend.entity.StrategyFilterRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StrategyFilterRuleMapper {
    int insert(StrategyFilterRule rule);
    List<StrategyFilterRule> findByStrategyId(@Param("strategyId") Long strategyId);
    int update(StrategyFilterRule rule);
    int deleteByStrategyId(@Param("strategyId") Long strategyId);
    int deleteById(@Param("id") Long id);
}

