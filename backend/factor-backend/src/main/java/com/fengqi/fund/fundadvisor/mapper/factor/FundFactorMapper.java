package com.fengqi.fund.fundadvisor.mapper.factor;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 基金因子数据Mapper接口
 * 用于从fund_etf_spot_ths表读取数据并计算因子值
 */
@Mapper
public interface FundFactorMapper {
    
    /**
     * 查询基金在指定日期范围内的净值数据
     */
    @Select("SELECT code, query_date, unit_nav, acc_nav, prev_unit_nav, prev_acc_nav, " +
            "change_value, change_rate, latest_unit_nav, latest_acc_nav " +
            "FROM fund_etf_spot_ths " +
            "WHERE query_date BETWEEN #{startDate} AND #{endDate} " +
            "AND code = #{fundCode} " +
            "ORDER BY query_date ASC")
    List<Map<String, Object>> selectFundNavData(@Param("fundCode") String fundCode,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
    
    /**
     * 查询所有基金在指定日期范围内的净值数据
     */
    @Select("SELECT code, query_date, unit_nav, acc_nav, prev_unit_nav, prev_acc_nav, " +
            "change_value, change_rate, latest_unit_nav, latest_acc_nav " +
            "FROM fund_etf_spot_ths " +
            "WHERE query_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY code, query_date ASC")
    List<Map<String, Object>> selectAllFundNavData(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
    
    /**
     * 查询指定日期的所有基金净值数据
     */
    @Select("SELECT code, query_date, unit_nav, acc_nav, prev_unit_nav, prev_acc_nav, " +
            "change_value, change_rate, latest_unit_nav, latest_acc_nav " +
            "FROM fund_etf_spot_ths " +
            "WHERE query_date = #{tradeDate} " +
            "ORDER BY code")
    List<Map<String, Object>> selectFundNavDataByDate(@Param("tradeDate") LocalDate tradeDate);
    
    /**
     * 查询基金的下期收益率（用于IC计算）
     * 下期收益率 = (下一天的change_rate)
     */
    @Select("SELECT t1.code, t1.query_date as factor_date, t2.change_rate as next_return_rate " +
            "FROM fund_etf_spot_ths t1 " +
            "LEFT JOIN fund_etf_spot_ths t2 ON t1.code = t2.code " +
            "AND t2.query_date = DATE_ADD(t1.query_date, INTERVAL 1 DAY) " +
            "WHERE t1.query_date BETWEEN #{startDate} AND #{endDate} " +
            "AND t2.change_rate IS NOT NULL " +
            "ORDER BY t1.code, t1.query_date")
    List<Map<String, Object>> selectFundNextReturnRates(@Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);
    
    @Select("SELECT t1.code, t1.query_date as factor_date, t2.change_rate as next_return_rate " +
            "FROM fund_etf_spot_ths t1 " +
            "LEFT JOIN fund_etf_spot_ths t2 ON t1.code = t2.code " +
            "AND t2.query_date = DATE_ADD(t1.query_date, INTERVAL 1 DAY) " +
            "WHERE t1.query_date BETWEEN #{startDate} AND #{endDate} " +
            "AND t1.code = CAST(#{fundCode} AS CHAR) " +
            "AND t2.change_rate IS NOT NULL " +
            "ORDER BY t1.query_date")
    List<Map<String, Object>> selectFundNextReturnRatesByCode(@Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate,
                                                              @Param("fundCode") String fundCode);
    
    /**
     * 根据基金名称查询基金代码
     */
    @Select("SELECT DISTINCT code FROM fund_etf_spot_ths WHERE name = #{fundName} LIMIT 1")
    String selectFundCodeByName(@Param("fundName") String fundName);
    
    @Select("SELECT DISTINCT code, name FROM fund_etf_spot_ths WHERE name LIKE CONCAT('%', #{fundName}, '%') LIMIT 10")
    List<Map<String, Object>> selectSimilarFundsByName(@Param("fundName") String fundName);
    
    // 添加测试方法来验证参数绑定
    @Select("SELECT COUNT(*) as count FROM fund_etf_spot_ths WHERE code = #{fundCode}")
    Integer testFundCodeCount(@Param("fundCode") String fundCode);
    
    @Select("SELECT COUNT(*) as count FROM fund_etf_spot_ths WHERE code = '159001'")
    Integer testSpecificFundCount();
    
    // 使用更明确的查询方式
    @Select("SELECT t1.code, t1.query_date as factor_date, t2.change_rate as next_return_rate " +
            "FROM fund_etf_spot_ths t1 " +
            "LEFT JOIN fund_etf_spot_ths t2 ON t1.code = t2.code " +
            "AND t2.query_date = DATE_ADD(t1.query_date, INTERVAL 1 DAY) " +
            "WHERE t1.query_date BETWEEN #{startDate} AND #{endDate} " +
            "AND t1.code = #{fundCode} " +
            "AND t1.name = (SELECT name FROM fund_etf_spot_ths WHERE code = #{fundCode} LIMIT 1) " +
            "AND t2.change_rate IS NOT NULL " +
            "ORDER BY t1.query_date")
    List<Map<String, Object>> selectFundNextReturnRatesByCodeV2(@Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate,
                                                              @Param("fundCode") String fundCode);
    
    // 添加最简单的测试查询
    @Select("SELECT code, query_date, change_rate " +
            "FROM fund_etf_spot_ths " +
            "WHERE code = #{fundCode} " +
            "AND query_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY query_date")
    List<Map<String, Object>> selectSimpleFundData(@Param("fundCode") String fundCode,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
}









