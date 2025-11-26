package com.example.backend.mapper;

import com.example.backend.entity.StrategyReturnPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StrategyReturnChartMapper {
    List<StrategyReturnPoint> findByStrategyId(@Param("strategyId") Long strategyId);
}

