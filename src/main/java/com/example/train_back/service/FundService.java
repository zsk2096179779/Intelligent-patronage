package com.example.train_back.service;

import com.example.train_back.dto.FundFilterRequest;
import com.example.train_back.entity.Fund;

import java.util.List;

/**
 * 基金Service接口
 */
public interface FundService {
    
    /**
     * 获取所有基金列表
     * @return 基金列表
     */
    List<Fund> getAllFunds();
    
    /**
     * 根据基金代码查询基金信息
     * @param fundCode 基金代码
     * @return 基金信息
     */
    Fund getFundByCode(String fundCode);
    
    /**
     * 根据基金代码列表批量查询
     * @param fundCodes 基金代码列表
     * @return 基金列表
     */
    List<Fund> getFundsByCodes(List<String> fundCodes);
    
    /**
     * 搜索基金（根据代码或名称模糊查询）
     * @param keyword 关键词
     * @return 基金列表
     */
    List<Fund> searchFunds(String keyword);
    
    /**
     * 验证基金代码是否存在
     * @param fundCode 基金代码
     * @return 是否存在
     */
    boolean validateFundCode(String fundCode);

    /**
     * 按条件筛选基金
     * @param filterRequest 筛选条件
     * @return 基金列表
     */
    List<Fund> filterFunds(FundFilterRequest filterRequest);
}

