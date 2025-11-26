package com.example.backend.mapper;

import com.example.backend.entity.FactorBase;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FactorBaseMapper {
    /**
     * 查询所有有效的因子
     */
    List<FactorBase> findAllValid();
    
    /**
     * 根据ID查询因子
     */
    FactorBase findById(Integer baseId);
    
    /**
     * 根据编码查询因子
     */
    FactorBase findByCode(String factorCode);
}

