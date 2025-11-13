package com.example.train_back.service;

import com.example.train_back.dto.ProductParamsDTO;

/**
 * 组合产品参数Service接口
 */
public interface PortfolioProductParamsService {
    
    /**
     * 保存或更新产品参数
     * @param portfolioId 组合ID
     * @param paramsDTO 产品参数DTO
     * @return 是否成功
     */
    boolean saveOrUpdateProductParams(Integer portfolioId, ProductParamsDTO paramsDTO);
    
    /**
     * 查询产品参数
     * @param portfolioId 组合ID
     * @return 产品参数DTO
     */
    ProductParamsDTO getProductParams(Integer portfolioId);
}

