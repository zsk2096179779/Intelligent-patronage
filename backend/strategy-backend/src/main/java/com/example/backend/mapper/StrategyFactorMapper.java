package com.example.backend.mapper;

import com.example.backend.entity.StrategyFactor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StrategyFactorMapper {
    int insert(StrategyFactor factor);
    List<StrategyFactor> findByStrategyId(@Param("strategyId") Long strategyId);
    int update(StrategyFactor factor);
    int deleteByStrategyId(@Param("strategyId") Long strategyId);
    int deleteById(@Param("id") Long id);
}

