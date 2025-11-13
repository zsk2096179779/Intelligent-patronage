package com.example.train_back.service;

import com.example.train_back.dto.HoldingDTO;

import java.util.List;

/**
 * 组合持仓Service接口
 */
public interface PortfolioHoldingService {
    
    /**
     * 保存持仓（先删除旧的，再插入新的）
     * @param portfolioId 组合ID
     * @param holdings 持仓列表
     * @return 是否成功
     */
    boolean saveHoldings(Integer portfolioId, List<HoldingDTO> holdings);
    
    /**
     * 查询持仓列表
     * @param portfolioId 组合ID
     * @return 持仓列表
     */
    List<HoldingDTO> getHoldings(Integer portfolioId);
    
    /**
     * 验证权重总和是否为100%
     * @param holdings 持仓列表
     * @return 是否有效
     */
    boolean validateWeightSum(List<HoldingDTO> holdings);
}

