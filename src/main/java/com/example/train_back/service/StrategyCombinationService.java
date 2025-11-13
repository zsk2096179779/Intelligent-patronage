package com.example.train_back.service;

import com.example.train_back.dto.PortfolioDetailDTO;
import com.example.train_back.dto.UpdatePortfolioBasicInfoDTO;
import com.example.train_back.entity.StrategyCombination;

import java.util.List;

/**
 * 策略组合Service接口
 */
public interface StrategyCombinationService {
    
    /**
     * 获取所有策略组合（包含策略信息）
     * @return 所有策略组合详情列表
     */
    List<PortfolioDetailDTO> getAllPortfolioDetails();
    
    /**
     * 审核通过：将组合的 listed 字段从 0 更新为 1
     * @param id 组合ID
     * @return 是否更新成功
     */
    boolean approvePortfolio(Integer id);
    
    /**
     * 审核拒绝：将组合的 listed 字段更新为 -1
     * @param id 组合ID
     * @param reason 拒绝原因（可选，用于记录）
     * @return 是否更新成功
     */
    boolean rejectPortfolio(Integer id, String reason);
    
    /**
     * 创建组合产品
     * @param name 组合名称
     * @param riskLevel 风险等级
     * @param strategyType 策略类型
     * @param strategyRefId 策略引用ID（关联 strategies.strategy_ref_id）
     * @param summary 组合简介
     * @param targetInvestor 目标客户
     * @return 创建的组合ID，失败返回null
     */
    Integer createPortfolio(String name, String riskLevel, String strategyType, Integer strategyRefId, String summary, String targetInvestor);
    
    /**
     * 更新组合基础信息
     * @param portfolioId 组合ID
     * @param basicInfoDTO 基础信息DTO
     * @return 是否成功
     */
    boolean updatePortfolioBasicInfo(Integer portfolioId, UpdatePortfolioBasicInfoDTO basicInfoDTO);
    
    /**
     * 根据ID查询组合
     * @param portfolioId 组合ID
     * @return 组合对象
     */
    StrategyCombination getPortfolioById(Integer portfolioId);
    
    /**
     * 提交审核
     * @param portfolioId 组合ID
     * @return 是否成功
     */
    boolean submitForReview(Integer portfolioId);
}

