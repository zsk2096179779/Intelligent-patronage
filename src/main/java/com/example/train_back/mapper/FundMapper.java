package com.example.train_back.mapper;

import com.example.train_back.entity.Fund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 基金Mapper接口
 */
@Mapper
public interface FundMapper {
    
    /**
     * 查询所有基金列表（用于下拉框选择）
     * @return 基金列表
     */
    List<Fund> selectAllFunds();
    
    /**
     * 根据基金代码查询基金信息
     * @param fundCode 基金代码
     * @return 基金信息
     */
    Fund selectByFundCode(@Param("fundCode") String fundCode);
    
    /**
     * 根据基金代码列表批量查询
     * @param fundCodes 基金代码列表
     * @return 基金列表
     */
    List<Fund> selectByFundCodes(@Param("fundCodes") List<String> fundCodes);
    
    /**
     * 搜索基金（根据代码或名称模糊查询）
     * @param keyword 关键词
     * @return 基金列表
     */
    List<Fund> searchFunds(@Param("keyword") String keyword);
}

