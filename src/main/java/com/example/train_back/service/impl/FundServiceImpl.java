package com.example.train_back.service.impl;

import com.example.train_back.entity.Fund;
import com.example.train_back.mapper.FundMapper;
import com.example.train_back.service.FundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 基金Service实现类
 */
@Service
public class FundServiceImpl implements FundService {
    
    @Autowired
    private FundMapper fundMapper;
    
    @Override
    public List<Fund> getAllFunds() {
        return fundMapper.selectAllFunds();
    }
    
    @Override
    public Fund getFundByCode(String fundCode) {
        return fundMapper.selectByFundCode(fundCode);
    }
    
    @Override
    public List<Fund> getFundsByCodes(List<String> fundCodes) {
        if (fundCodes == null || fundCodes.isEmpty()) {
            return List.of();
        }
        return fundMapper.selectByFundCodes(fundCodes);
    }
    
    @Override
    public List<Fund> searchFunds(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllFunds();
        }
        return fundMapper.searchFunds(keyword.trim());
    }
    
    @Override
    public boolean validateFundCode(String fundCode) {
        if (fundCode == null || fundCode.trim().isEmpty()) {
            return false;
        }
        Fund fund = fundMapper.selectByFundCode(fundCode.trim());
        return fund != null;
    }
}

