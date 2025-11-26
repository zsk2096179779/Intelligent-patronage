package com.example.backend.mapper;

import com.example.backend.entity.StrategyHolding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StrategyHoldingMapper {
    List<StrategyHolding> findByStrategyId(@Param("strategyId") Long strategyId);
}

