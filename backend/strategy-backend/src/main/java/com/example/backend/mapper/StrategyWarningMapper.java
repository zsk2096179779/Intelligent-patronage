package com.example.backend.mapper;

import com.example.backend.entity.StrategyWarning;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StrategyWarningMapper {
    List<StrategyWarning> findByStrategyId(@Param("strategyId") Long strategyId);
    
    int insert(StrategyWarning warning);
    
    int insertBatch(@Param("warnings") List<StrategyWarning> warnings);
    
    int deleteByStrategyId(@Param("strategyId") Long strategyId);
}

