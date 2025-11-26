package com.fengqi.fund.fundadvisor.service.factor.impl;

import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.dto.factor.FactorPreviewResponse;
import com.fengqi.fund.fundadvisor.dto.factor.FactorQueryRequest;
import com.fengqi.fund.fundadvisor.dto.factor.FactorSelectionRequest;
import com.fengqi.fund.fundadvisor.entity.factor.FactorBase;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorBaseMapper;
import com.fengqi.fund.fundadvisor.service.factor.FactorBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基础因子服务实现类
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
@Slf4j
@Service
public class FactorBaseServiceImpl implements FactorBaseService {
    
    @Autowired
    private FactorBaseMapper factorBaseMapper;
    
    @Override
    public ResultDTO<List<FactorBase>> queryFactors(FactorQueryRequest request) {
        try {
            // 参数校验
            if (request == null) {
                return ResultDTO.error("请求参数不能为空");
            }
            
            List<FactorBase> factors;
            
            // 根据条件查询
            if (StringUtils.hasText(request.getKeyword())) {
                // 关键词搜索
                log.info("使用关键词搜索: {}", request.getKeyword());
                factors = factorBaseMapper.searchFactors(request.getKeyword());
            } else if (StringUtils.hasText(request.getFactorType())) {
                // 按类型查询
                log.info("按类型查询: {}", request.getFactorType());
                factors = factorBaseMapper.getFactorsByType(request.getFactorType());
            } else if (StringUtils.hasText(request.getDataSource())) {
                // 按数据源查询
                log.info("按数据源查询: {}", request.getDataSource());
                factors = factorBaseMapper.getFactorsByDataSource(request.getDataSource());
            } else if (Boolean.TRUE.equals(request.getPopularOnly())) {
                // 查询热门因子
                Integer limit = request.getPageSize() != null ? request.getPageSize() : 20;
                log.info("查询热门因子，限制: {}", limit);
                factors = factorBaseMapper.getPopularFactors(limit);
            } else {
                // 默认查询所有有效因子
                log.info("默认查询所有有效因子");
                factors = factorBaseMapper.getAllValidFactors();
            }
            
            // 确保factors不为null
            if (factors == null) {
                factors = new ArrayList<>();
            }
            
            log.info("查询完成，返回{}条记录", factors.size());
            return ResultDTO.success(factors);
            
        } catch (Exception e) {
            log.error("查询基础因子失败，错误详情：", e);
            return ResultDTO.error("查询基础因子失败：" + e.getMessage());
        }
    }
    
    @Override
    public ResultDTO<List<FactorBase>> getFactorsByIds(List<Integer> factorIds) {
        try {
            if (factorIds == null || factorIds.isEmpty()) {
                return ResultDTO.error("因子ID列表不能为空");
            }
            
            List<FactorBase> factors = factorBaseMapper.getFactorsByIds(factorIds);
            return ResultDTO.success(factors);
        } catch (Exception e) {
            log.error("根据ID列表获取因子失败", e);
            return ResultDTO.error("获取基础因子失败");
        }
    }
    
    @Override
    public ResultDTO<FactorPreviewResponse> previewSelectedFactors(FactorSelectionRequest request) {
        try {
            if (request.getFactorIds() == null || request.getFactorIds().isEmpty()) {
                return ResultDTO.error("请至少选择一个基础因子");
            }
            
            // 获取选中的因子信息
            List<FactorBase> factors = factorBaseMapper.getFactorsByIds(request.getFactorIds());
            if (factors.isEmpty()) {
                return ResultDTO.error("未找到选中的基础因子");
            }
            
            // 构建预览响应
            FactorPreviewResponse response = new FactorPreviewResponse();
            
            // 转换因子基本信息
            List<FactorPreviewResponse.FactorBasicInfo> factorInfos = factors.stream()
                .map(this::convertToBasicInfo)
                .collect(Collectors.toList());
            response.setFactors(factorInfos);
            
            // 构建日期范围
            FactorPreviewResponse.DateRange dateRange = calculateDateRange(factors);
            response.setDateRange(dateRange);
            
            // 构建数据统计
            FactorPreviewResponse.DataStatistics statistics = calculateStatistics(factors);
            response.setStatistics(statistics);
            
            // 模拟数据预览（实际应用中应该从真实数据源获取）
            List<Map<String, Object>> dataPreview = generateDataPreview(factors, request);
            response.setDataPreview(dataPreview);
            
            return ResultDTO.success(response, "因子预览生成成功");
            
        } catch (Exception e) {
            log.error("预览选中因子失败", e);
            return ResultDTO.error("预览因子数据失败");
        }
    }
    
    @Override
    public ResultDTO<Boolean> validateFactorSelection(List<Integer> factorIds) {
        try {
            if (factorIds == null || factorIds.isEmpty()) {
                return ResultDTO.error("因子ID列表不能为空");
            }
            
            // 检查所有因子是否存在且有效
            List<FactorBase> factors = factorBaseMapper.getFactorsByIds(factorIds);
            if (factors.size() != factorIds.size()) {
                return ResultDTO.error("选中的因子中包含无效因子");
            }
            
            // 检查因子数量限制
            if (factorIds.size() > 20) {
                return ResultDTO.error("单次最多选择20个因子");
            }
            
            return ResultDTO.success(true, "因子选择验证通过");
            
        } catch (Exception e) {
            log.error("验证因子选择失败", e);
            return ResultDTO.error("验证因子选择失败");
        }
    }
    

    
    /**
     * 转换为基本信息的DTO
     */
    private FactorPreviewResponse.FactorBasicInfo convertToBasicInfo(FactorBase factor) {
        FactorPreviewResponse.FactorBasicInfo info = new FactorPreviewResponse.FactorBasicInfo();
        info.setBaseId(factor.getBaseId());
        info.setFactorName(factor.getFactorName());
        info.setFactorCode(factor.getFactorCode());
        info.setDataSource(factor.getDataSource());
        info.setDataStartDate(factor.getDataStartDate());
        info.setLatestDataDate(factor.getLatestDataDate());
        info.setDataDesc(factor.getDataDesc());
        // displayName 和 factorType 需要从 factor_definition 表获取，这里暂时留空
        return info;
    }
    
    /**
     * 计算数据日期范围
     */
    private FactorPreviewResponse.DateRange calculateDateRange(List<FactorBase> factors) {
        FactorPreviewResponse.DateRange dateRange = new FactorPreviewResponse.DateRange();
        
        LocalDate startDate = factors.stream()
            .map(FactorBase::getDataStartDate)
            .filter(Objects::nonNull)
            .min(LocalDate::compareTo)
            .orElse(null);
            
        LocalDate endDate = factors.stream()
            .map(FactorBase::getLatestDataDate)
            .filter(Objects::nonNull)
            .max(LocalDate::compareTo)
            .orElse(null);
            
        dateRange.setStartDate(startDate);
        dateRange.setEndDate(endDate);
        
        if (startDate != null && endDate != null) {
            dateRange.setTotalDays((int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1);
        }
        
        return dateRange;
    }
    
    /**
     * 计算数据统计信息
     */
    private FactorPreviewResponse.DataStatistics calculateStatistics(List<FactorBase> factors) {
        FactorPreviewResponse.DataStatistics statistics = new FactorPreviewResponse.DataStatistics();
        
        statistics.setTotalRecords(factors.size());
        statistics.setValidRecords(factors.size());
        statistics.setMissingRate(0.0);
        
        // 这里可以添加更复杂的统计逻辑
        Map<String, Double> factorStats = new HashMap<>();
        for (FactorBase factor : factors) {
            factorStats.put(factor.getFactorCode(), 100.0); // 示例数据
        }
        statistics.setFactorStats(factorStats);
        
        return statistics;
    }
    
    /**
     * 生成模拟数据预览
     */
    private List<Map<String, Object>> generateDataPreview(List<FactorBase> factors, FactorSelectionRequest request) {
        List<Map<String, Object>> preview = new ArrayList<>();
        
        // 生成最近10天的模拟数据
        LocalDate endDate = LocalDate.now();
        for (int i = 9; i >= 0; i--) {
            Map<String, Object> row = new HashMap<>();
            LocalDate date = endDate.minusDays(i);
            row.put("date", date);
            
            // 为每个因子生成模拟数据
            for (FactorBase factor : factors) {
                double value = 50.0 + Math.random() * 100; // 50-150之间的随机值
                row.put(factor.getFactorCode(), Math.round(value * 100.0) / 100.0);
            }
            
            preview.add(row);
        }
        
        return preview;
    }
    

    
}