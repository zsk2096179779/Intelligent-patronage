package com.example.backend.mapper;

import com.example.backend.entity.RebalanceConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RebalanceConfigMapper {
    RebalanceConfig findByStrategyId(@Param("strategyId") Long strategyId);

    int upsert(RebalanceConfig config);
}

