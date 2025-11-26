package com.example.backend.mapper;

import com.example.backend.entity.StrategyMonitorMetrics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StrategyMonitorMetricsMapper {

    StrategyMonitorMetrics findByStrategyId(@Param("strategyId") Long strategyId);
}

