package com.example.train_back.mapper;

import com.example.train_back.entity.PortfolioProductParams;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 组合产品参数Mapper接口
 */
@Mapper
public interface PortfolioProductParamsMapper {
    
    /**
     * 插入或更新产品参数
     * @param params 产品参数
     * @return 影响行数
     */
    int insertOrUpdate(PortfolioProductParams params);
    
    /**
     * 根据组合ID查询产品参数
     * @param portfolioId 组合ID
     * @return 产品参数
     */
    PortfolioProductParams selectByPortfolioId(@Param("portfolioId") Integer portfolioId);
    
    /**
     * 删除产品参数
     * @param portfolioId 组合ID
     * @return 影响行数
     */
    int deleteByPortfolioId(@Param("portfolioId") Integer portfolioId);
}

