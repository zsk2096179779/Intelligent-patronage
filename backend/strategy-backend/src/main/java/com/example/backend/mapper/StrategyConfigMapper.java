package com.example.backend.mapper;

import com.example.backend.entity.StrategyConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StrategyConfigMapper {
    int insert(StrategyConfig config);
    StrategyConfig findByStrategyId(@Param("strategyId") Long strategyId);
    int update(StrategyConfig config);
    int deleteByStrategyId(@Param("strategyId") Long strategyId);
}

