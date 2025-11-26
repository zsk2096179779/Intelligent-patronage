package com.example.backend.service;

import com.example.backend.entity.FactorBase;
import com.example.backend.mapper.FactorBaseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FactorBaseService {

    private final FactorBaseMapper factorBaseMapper;

    public FactorBaseService(FactorBaseMapper factorBaseMapper) {
        this.factorBaseMapper = factorBaseMapper;
    }

    /**
     * 获取所有有效的因子
     */
    public List<FactorBase> getAllValidFactors() {
        return factorBaseMapper.findAllValid();
    }

    /**
     * 根据ID获取因子
     */
    public FactorBase getById(Integer baseId) {
        return factorBaseMapper.findById(baseId);
    }

    /**
     * 根据编码获取因子
     */
    public FactorBase getByCode(String factorCode) {
        return factorBaseMapper.findByCode(factorCode);
    }
}

