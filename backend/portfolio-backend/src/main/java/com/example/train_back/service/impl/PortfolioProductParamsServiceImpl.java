package com.example.train_back.service.impl;

import com.example.train_back.dto.ProductParamsDTO;
import com.example.train_back.entity.PortfolioProductParams;
import com.example.train_back.mapper.PortfolioProductParamsMapper;
import com.example.train_back.service.PortfolioProductParamsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 组合产品参数Service实现类
 */
@Service
public class PortfolioProductParamsServiceImpl implements PortfolioProductParamsService {
    
    @Autowired
    private PortfolioProductParamsMapper productParamsMapper;
    
    @Override
    @Transactional
    public boolean saveOrUpdateProductParams(Integer portfolioId, ProductParamsDTO paramsDTO) {
        PortfolioProductParams params = new PortfolioProductParams();
        params.setPortfolioId(portfolioId);
        BeanUtils.copyProperties(paramsDTO, params);
        
        int rows = productParamsMapper.insertOrUpdate(params);
        return rows > 0;
    }
    
    @Override
    public ProductParamsDTO getProductParams(Integer portfolioId) {
        PortfolioProductParams params = productParamsMapper.selectByPortfolioId(portfolioId);
        if (params == null) {
            return null;
        }
        
        ProductParamsDTO dto = new ProductParamsDTO();
        BeanUtils.copyProperties(params, dto);
        return dto;
    }
}

