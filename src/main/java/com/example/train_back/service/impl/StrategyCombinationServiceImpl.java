package com.example.train_back.service.impl;

import com.example.train_back.dto.ListedPortfolioOrderDataDTO;
import com.example.train_back.dto.PortfolioDetailDTO;
import com.example.train_back.dto.UpdatePortfolioBasicInfoDTO;
import com.example.train_back.dto.UpdatePortfolioPerformanceDTO;
import com.example.train_back.entity.StrategyCombination;
import com.example.train_back.mapper.StrategyCombinationMapper;
import com.example.train_back.service.StrategyCombinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 策略组合Service实现类
 */
@Service
public class StrategyCombinationServiceImpl implements StrategyCombinationService {
    
    @Autowired
    private StrategyCombinationMapper strategyCombinationMapper;
    
    @Override
    public List<PortfolioDetailDTO> getAllPortfolioDetails() {
        List<PortfolioDetailDTO> details = strategyCombinationMapper.selectAllPortfolioDetails();
        // 设置上架状态文本
        details.forEach(this::setListedText);
        return details;
    }
    
    @Override
    public PortfolioDetailDTO getPortfolioDetailById(Integer portfolioId) {
        PortfolioDetailDTO detail = strategyCombinationMapper.selectPortfolioDetailById(portfolioId);
        if (detail != null) {
            // 设置上架状态文本
            setListedText(detail);
        }
        return detail;
    }
    
    /**
     * 设置上架状态文本
     */
    private void setListedText(PortfolioDetailDTO dto) {
        if (dto.getListed() != null) {
            if (dto.getListed() == 1) {
                dto.setListedText("已上架");
            } else if (dto.getListed() == 0) {
                dto.setListedText("未上架");
            } else if (dto.getListed() == -1) {
                dto.setListedText("已拒绝");
            } else {
                dto.setListedText("未知");
            }
        }
    }
    
    @Override
    @Transactional
    public boolean approvePortfolio(Integer id) {
        int rows = strategyCombinationMapper.approvePortfolio(id);
        return rows > 0;
    }
    
    @Override
    @Transactional
    public boolean rejectPortfolio(Integer id, String reason) {
        // 更新 listed 字段为 -签约业务模块设计.md，并记录拒绝原因到 reject_reason 字段
        int rows = strategyCombinationMapper.rejectPortfolio(id, reason);
        return rows > 0;
    }
    
    @Override
    @Transactional
    public Integer createPortfolio(String name, String riskLevel, String strategyType, Integer strategyRefId, String summary, String targetInvestor) {
        // 创建组合对象
        StrategyCombination portfolio = new StrategyCombination();
        portfolio.setName(name);
        portfolio.setRiskLevel(riskLevel);
        portfolio.setStrategyType(strategyType);
        portfolio.setStrategyId(strategyRefId);
        portfolio.setSummary(summary);
        portfolio.setTargetInvestor(targetInvestor);
        portfolio.setListed(0); // 初始状态为未审核
        portfolio.setStatus("draft"); // 初始状态为草稿
        
        // 插入数据库，useGeneratedKeys 会自动设置生成的 ID
        int rows = strategyCombinationMapper.createPortfolio(portfolio);
        if (rows > 0) {
            return portfolio.getId(); // 返回生成的 ID
        }
        return null;
    }
    
    @Override
    @Transactional
    public boolean updatePortfolioBasicInfo(Integer portfolioId, UpdatePortfolioBasicInfoDTO basicInfoDTO) {
        StrategyCombination portfolio = strategyCombinationMapper.selectById(portfolioId);
        if (portfolio == null) {
            return false;
        }
        
        // 更新字段
        portfolio.setName(basicInfoDTO.getName());
        portfolio.setRiskLevel(basicInfoDTO.getRiskLevel());
        portfolio.setStrategyType(basicInfoDTO.getStrategyType());
        portfolio.setSummary(basicInfoDTO.getSummary());
        portfolio.setTargetInvestor(basicInfoDTO.getTargetInvestor());
        portfolio.setStrategyId(basicInfoDTO.getStrategyId());
        
        int rows = strategyCombinationMapper.updatePortfolioBasicInfo(portfolio);
        return rows > 0;
    }
    
    @Override
    public StrategyCombination getPortfolioById(Integer portfolioId) {
        return strategyCombinationMapper.selectById(portfolioId);
    }
    
    @Override
    @Transactional
    public boolean submitForReview(Integer portfolioId) {
        // 检查组合是否存在
        StrategyCombination portfolio = strategyCombinationMapper.selectById(portfolioId);
        if (portfolio == null) {
            return false;
        }
        
        // 只有草稿状态才能提交审核
        if (!"draft".equals(portfolio.getStatus())) {
            return false;
        }
        
        // 更新状态为待审核
        int rows = strategyCombinationMapper.updateStatus(portfolioId, "pending_review");
        return rows > 0;
    }
    
    @Override
    public List<ListedPortfolioOrderDataDTO> getListedPortfolioOrderData() {
        return strategyCombinationMapper.selectListedPortfolioOrderData();
    }
    
    @Override
    @Transactional
    public boolean updatePortfolioPerformance(Integer portfolioId, UpdatePortfolioPerformanceDTO performanceDTO) {
        // 检查组合是否存在
        StrategyCombination portfolio = strategyCombinationMapper.selectById(portfolioId);
        if (portfolio == null) {
            return false;
        }
        
        // 更新收益指标
        int rows = strategyCombinationMapper.updatePortfolioPerformance(
                portfolioId,
                performanceDTO.getReturnRate(),
                performanceDTO.getAnnualReturn(),
                performanceDTO.getMaxDrawdown(),
                performanceDTO.getSharpeRatio(),
                performanceDTO.getVolatility(),
                performanceDTO.getWinRate()
        );
        
        return rows > 0;
    }
}

