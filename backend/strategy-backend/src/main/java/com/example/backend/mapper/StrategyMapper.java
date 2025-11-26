package com.example.backend.mapper;

import com.example.backend.entity.Strategy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StrategyMapper {
    int insert(Strategy strategy);

    List<Strategy> findAll();
    
    List<Strategy> findByOwner(@Param("owner") Long owner);

    Strategy findById(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("owner") Long owner);

    int update(Strategy strategy);

    int deleteById(@Param("id") Long id, @Param("owner") Long owner);
}

