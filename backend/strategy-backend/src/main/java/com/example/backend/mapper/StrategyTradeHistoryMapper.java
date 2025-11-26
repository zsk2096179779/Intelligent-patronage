package com.example.backend.mapper;

import com.example.backend.entity.StrategyTradeHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StrategyTradeHistoryMapper {
    List<StrategyTradeHistory> findByStrategyId(@Param("strategyId") Long strategyId);
}

