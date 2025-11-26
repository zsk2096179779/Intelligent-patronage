package com.fengqi.fund.fundadvisor.service.factor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 基金因子计算服务接口
 * 基于fund_etf_spot_ths表的数据自动计算因子值
 */
public interface FundFactorCalculationService {
    
    /**
     * 计算基金的基础因子值
     * 基于fund_etf_spot_ths表的数据计算各种因子
     * 
     * @param factorId 因子ID
     * @param fundCode 基金代码（如果为null，则计算所有基金）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 因子值数据列表（基金代码、日期、因子值）
     */
    List<Map<String, Object>> calculateFundFactorValues(Integer factorId, 
                                                         String fundCode,
                                                         LocalDate startDate, 
                                                         LocalDate endDate);
    
    /**
     * 计算基金的收益率数据（下期收益率，用于IC计算）
     */
    List<Map<String, Object>> calculateFundReturnRates(LocalDate startDate, LocalDate endDate);
    
    /**
     * 计算指定基金的收益率数据（下期收益率，用于IC计算）
     */
    List<Map<String, Object>> calculateFundReturnRates(LocalDate startDate, LocalDate endDate, String fundCode);
    
    /**
     * 计算单只基金的因子值（按日期）
     */
    Map<LocalDate, BigDecimal> calculateSingleFundFactorValues(Integer factorId,
                                                                String fundCode,
                                                                LocalDate startDate,
                                                                LocalDate endDate);
}









