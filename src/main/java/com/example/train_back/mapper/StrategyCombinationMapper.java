package com.example.train_back.mapper;

import com.example.train_back.dto.PortfolioDetailDTO;
import com.example.train_back.entity.StrategyCombination;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 策略组合Mapper接口
 */
@Mapper
public interface StrategyCombinationMapper {
    
    /**
     * 查询所有策略组合（包含策略信息）
     * @return 所有策略组合详情列表
     */
    List<PortfolioDetailDTO> selectAllPortfolioDetails();
    
    /**
     * 根据组合ID查询组合详情（包含策略信息）
     * @param portfolioId 组合ID
     * @return 组合详情
     */
    PortfolioDetailDTO selectPortfolioDetailById(@Param("portfolioId") Integer portfolioId);
    
    /**
     * 审核通过：将 listed 字段从 0 更新为 签约业务模块设计.md
     * @param id 组合ID
     * @return 更新的行数
     */
    int approvePortfolio(@Param("id") Integer id);
    
    /**
     * 审核拒绝：将 listed 字段更新为 -签约业务模块设计.md，并记录拒绝原因
     * @param id 组合ID
     * @param reason 拒绝原因
     * @return 更新的行数
     */
    int rejectPortfolio(@Param("id") Integer id, @Param("reason") String reason);
    
    /**
     * 创建组合产品
     * @param portfolio 组合对象（插入后会自动设置生成的ID）
     * @return 插入的行数
     */
    int createPortfolio(StrategyCombination portfolio);
    
    /**
     * 更新组合基础信息
     * @param portfolio 组合对象
     * @return 更新的行数
     */
    int updatePortfolioBasicInfo(StrategyCombination portfolio);
    
    /**
     * 根据ID查询组合
     * @param id 组合ID
     * @return 组合对象
     */
    StrategyCombination selectById(@Param("id") Integer id);

    StrategyCombination selectByIdForDeal(@Param("id") Integer id);
    /**
     * 更新组合状态
     * @param id 组合ID
     * @param status 状态
     * @return 更新的行数
     */
    int updateStatus(@Param("id") Integer id, @Param("status") String status);
}

