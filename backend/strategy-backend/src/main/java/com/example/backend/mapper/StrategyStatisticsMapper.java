package com.example.backend.mapper;

import com.example.backend.entity.StrategyStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StrategyStatisticsMapper {

    StrategyStatistics findByStrategyId(@Param("strategyId") Long strategyId);
}

