package com.example.backend.mapper;

import com.example.backend.entity.RebalanceBacktestResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RebalanceBacktestMapper {

    int insert(RebalanceBacktestResult result);

    RebalanceBacktestResult findLatestByStrategyId(@Param("strategyId") Long strategyId);
}

