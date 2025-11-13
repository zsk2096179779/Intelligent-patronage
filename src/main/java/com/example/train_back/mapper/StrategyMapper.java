package com.example.train_back.mapper;

import com.example.train_back.dto.StrategyListDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 策略Mapper接口
 */
@Mapper
public interface StrategyMapper {
    
    /**
     * 查询所有策略列表（用于下拉框选择）
     * @return 策略列表
     */
    List<StrategyListDTO> selectAllStrategies();
}

