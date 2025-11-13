package com.example.train_back.service.impl;

import com.example.train_back.dto.HoldingDTO;
import com.example.train_back.entity.Fund;
import com.example.train_back.entity.PortfolioHolding;
import com.example.train_back.mapper.PortfolioHoldingMapper;
import com.example.train_back.service.FundService;
import com.example.train_back.service.PortfolioHoldingService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 组合持仓Service实现类
 */
@Service
public class PortfolioHoldingServiceImpl implements PortfolioHoldingService {
    
    @Autowired
    private PortfolioHoldingMapper holdingMapper;
    
    @Autowired
    private FundService fundService;
    
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal SUM_TOLERANCE_DECIMAL = new BigDecimal("0.0001");
    private static final BigDecimal SUM_TOLERANCE_PERCENT = new BigDecimal("0.01");
    
    @Override
    @Transactional
    public boolean saveHoldings(Integer portfolioId, List<HoldingDTO> holdings) {
        // 先删除旧的持仓
        holdingMapper.deleteByPortfolioId(portfolioId);
        
        if (holdings == null || holdings.isEmpty()) {
            return true;
        }
        
        // 转换为实体对象，并验证基金代码是否存在
        List<PortfolioHolding> holdingList = new ArrayList<>();
        for (HoldingDTO dto : holdings) {
            // 验证基金代码是否存在
            if (dto.getFundCode() == null || dto.getFundCode().trim().isEmpty()) {
                throw new IllegalArgumentException("基金代码不能为空");
            }
            
            Fund fund = fundService.getFundByCode(dto.getFundCode().trim());
            if (fund == null) {
                throw new IllegalArgumentException("基金代码不存在：" + dto.getFundCode());
            }
            
            PortfolioHolding holding = new PortfolioHolding();
            holding.setPortfolioId(portfolioId);
            BeanUtils.copyProperties(dto, holding);
            
            // 统一转换权重：兼容 0-1 与 0-100 的传入格式
            BigDecimal normalizedWeight = normalizeWeight(dto.getWeight());
            holding.setWeight(normalizedWeight.setScale(6, RoundingMode.HALF_UP));
            
            // 如果前端没有传 fund_name，从基金表获取
            if (holding.getFundName() == null || holding.getFundName().trim().isEmpty()) {
                holding.setFundName(fund.getFundName());
            }
            
            holdingList.add(holding);
        }
        
        // 批量插入
        int rows = holdingMapper.batchInsert(holdingList);
        return rows > 0;
    }
    
    @Override
    public List<HoldingDTO> getHoldings(Integer portfolioId) {
        List<PortfolioHolding> holdings = holdingMapper.selectByPortfolioId(portfolioId);
        List<HoldingDTO> dtoList = new ArrayList<>();
        
        for (PortfolioHolding holding : holdings) {
            HoldingDTO dto = new HoldingDTO();
            BeanUtils.copyProperties(holding, dto);
            
            // 返回前统一转换为百分比格式，保持对外接口一致（0-100）
            if (holding.getWeight() != null) {
                BigDecimal stored = holding.getWeight();
                BigDecimal percent = stored.compareTo(ONE) <= 0
                        ? stored.multiply(HUNDRED)
                        : stored;
                dto.setWeight(percent.setScale(2, RoundingMode.HALF_UP));
            }
            
            dtoList.add(dto);
        }
        
        return dtoList;
    }
    
    @Override
    public boolean validateWeightSum(List<HoldingDTO> holdings) {
        if (holdings == null || holdings.isEmpty()) {
            return false;
        }
        
        BigDecimal sum = BigDecimal.ZERO;
        boolean isPercentMode = false;
        try {
            for (HoldingDTO holding : holdings) {
                BigDecimal weight = holding.getWeight();
                if (weight == null) {
                    return false;
                }
                BigDecimal value = weight.stripTrailingZeros();
                if (value.compareTo(BigDecimal.ZERO) < 0) {
                    return false;
                }
                if (value.compareTo(ONE) > 0) {
                    isPercentMode = true;
                }
                sum = sum.add(value);
            }
        } catch (Exception ex) {
            return false;
        }
        
        if (isPercentMode) {
            BigDecimal diff = sum.subtract(HUNDRED).abs();
            return diff.compareTo(SUM_TOLERANCE_PERCENT) <= 0;
        } else {
            BigDecimal diff = sum.subtract(ONE).abs();
            return diff.compareTo(SUM_TOLERANCE_DECIMAL) <= 0;
        }
    }

    private BigDecimal normalizeWeight(BigDecimal rawWeight) {
        if (rawWeight == null) {
            throw new IllegalArgumentException("持仓权重不能为空");
        }
        BigDecimal weight = rawWeight.stripTrailingZeros();
        if (weight.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("持仓权重不能为负数");
        }
        BigDecimal normalized = weight;
        if (normalized.compareTo(ONE) > 0) {
            normalized = normalized.divide(HUNDRED, 8, RoundingMode.HALF_UP);
        }
        if (normalized.compareTo(ONE) > 0) {
            throw new IllegalArgumentException("持仓权重不得超过100%");
        }
        return normalized.setScale(8, RoundingMode.HALF_UP);
    }
}

