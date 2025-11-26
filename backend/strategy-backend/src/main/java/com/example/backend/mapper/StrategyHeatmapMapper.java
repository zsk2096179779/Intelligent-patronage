package com.example.backend.mapper;

import com.example.backend.entity.StrategyHeatmap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StrategyHeatmapMapper {
    List<StrategyHeatmap> findByStrategyId(@Param("strategyId") Long strategyId);
}

