package com.example.backend.mapper;

import com.example.backend.entity.StrategyProfitCurvePoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StrategyProfitCurveMapper {
    List<StrategyProfitCurvePoint> findByStrategyId(@Param("strategyId") Long strategyId);
}

