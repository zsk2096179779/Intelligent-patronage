package com.example.train_back.mapper;

import com.example.train_back.entity.Portfolio;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PortfolioMapper {

    /**
     * 根据组合ID查询组合信息
     */
    Portfolio selectById(@Param("id") Integer id);
}
