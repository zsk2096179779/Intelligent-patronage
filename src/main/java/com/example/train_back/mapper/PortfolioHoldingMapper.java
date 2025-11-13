package com.example.train_back.mapper;

import com.example.train_back.entity.PortfolioHolding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 组合持仓Mapper接口
 */
@Mapper
public interface PortfolioHoldingMapper {
    
    /**
     * 批量插入持仓
     * @param holdings 持仓列表
     * @return 影响行数
     */
    int batchInsert(@Param("holdings") List<PortfolioHolding> holdings);
    
    /**
     * 根据组合ID查询持仓列表
     * @param portfolioId 组合ID
     * @return 持仓列表
     */
    List<PortfolioHolding> selectByPortfolioId(@Param("portfolioId") Integer portfolioId);
    
    /**
     * 删除组合的所有持仓
     * @param portfolioId 组合ID
     * @return 影响行数
     */
    int deleteByPortfolioId(@Param("portfolioId") Integer portfolioId);
    
    /**
     * 根据持仓ID删除
     * @param id 持仓ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);
}

