package com.fengqi.fund.fundadvisor.mapper.factor;

import com.fengqi.fund.fundadvisor.entity.factor.FactorLayeredResult;
import com.fengqi.fund.fundadvisor.entity.factor.FactorLayeredNavSeries;
import com.fengqi.fund.fundadvisor.entity.factor.FactorLayeredHoldingStats;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 分层回测Mapper接口
 */
@Mapper
public interface FactorLayeredBacktestMapper {
    
    // ==================== 分层回测结果 ====================
    
    /**
     * 插入分层回测结果
     */
    @Insert("INSERT INTO factor_layered_result " +
            "(task_id, factor_id, factor_code, factor_name, quantile, start_date, end_date, " +
            "total_return, annualized_return, sharpe_ratio, max_drawdown, win_rate, " +
            "volatility, calculation_date, create_time) " +
            "VALUES (#{taskId}, #{factorId}, #{factorCode}, #{factorName}, #{quantile}, " +
            "#{startDate}, #{endDate}, #{totalReturn}, #{annualizedReturn}, #{sharpeRatio}, " +
            "#{maxDrawdown}, #{winRate}, #{volatility}, #{calculationDate}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "resultId")
    int insertLayeredResult(FactorLayeredResult result);
    
    /**
     * 批量插入分层回测结果
     */
    @Insert("<script>" +
            "INSERT INTO factor_layered_result " +
            "(task_id, factor_id, factor_code, factor_name, quantile, start_date, end_date, " +
            "total_return, annualized_return, sharpe_ratio, max_drawdown, win_rate, " +
            "volatility, calculation_date, create_time) VALUES " +
            "<foreach collection='results' item='result' separator=','>" +
            "(#{result.taskId}, #{result.factorId}, #{result.factorCode}, #{result.factorName}, " +
            "#{result.quantile}, #{result.startDate}, #{result.endDate}, #{result.totalReturn}, " +
            "#{result.annualizedReturn}, #{result.sharpeRatio}, #{result.maxDrawdown}, " +
            "#{result.winRate}, #{result.volatility}, #{result.calculationDate}, #{result.createTime})" +
            "</foreach>" +
            "</script>")
    int batchInsertLayeredResults(@Param("results") List<FactorLayeredResult> results);
    
    /**
     * 根据任务ID查询分层回测结果
     */
    @Select("SELECT * FROM factor_layered_result WHERE task_id = #{taskId} ORDER BY quantile")
    List<FactorLayeredResult> selectLayeredResultsByTaskId(@Param("taskId") Integer taskId);
    
    /**
     * 根据任务ID和分位数查询结果
     */
    @Select("SELECT * FROM factor_layered_result WHERE task_id = #{taskId} AND quantile = #{quantile}")
    FactorLayeredResult selectLayeredResultByTaskIdAndQuantile(
            @Param("taskId") Integer taskId, 
            @Param("quantile") Integer quantile);
    
    /**
     * 根据因子ID查询历史分层回测结果
     */
    @Select("SELECT * FROM factor_layered_result WHERE factor_id = #{factorId} " +
            "ORDER BY calculation_date DESC LIMIT #{limit}")
    List<FactorLayeredResult> selectLayeredResultsByFactorId(
            @Param("factorId") Integer factorId, 
            @Param("limit") Integer limit);
    
    // ==================== 净值序列 ====================
    
    /**
     * 插入净值序列
     */
    @Insert("INSERT INTO factor_layered_nav_series " +
            "(result_id, trade_date, nav_value, daily_return, create_time) " +
            "VALUES (#{resultId}, #{tradeDate}, #{navValue}, #{dailyReturn}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "seriesId")
    int insertNavSeries(FactorLayeredNavSeries navSeries);
    
    /**
     * 批量插入净值序列
     */
    @Insert("<script>" +
            "INSERT INTO factor_layered_nav_series (result_id, trade_date, nav_value, daily_return, create_time) VALUES " +
            "<foreach collection='navSeries' item='nav' separator=','>" +
            "(#{nav.resultId}, #{nav.tradeDate}, #{nav.navValue}, #{nav.dailyReturn}, #{nav.createTime})" +
            "</foreach>" +
            "</script>")
    int batchInsertNavSeries(@Param("navSeries") List<FactorLayeredNavSeries> navSeries);
    
    /**
     * 根据结果ID查询净值序列
     */
    @Select("SELECT * FROM factor_layered_nav_series WHERE result_id = #{resultId} ORDER BY trade_date")
    List<FactorLayeredNavSeries> selectNavSeriesByResultId(@Param("resultId") Integer resultId);
    
    /**
     * 根据任务ID查询所有分位数的净值序列
     */
    @Select("SELECT navs.*, lr.quantile FROM factor_layered_nav_series navs " +
            "JOIN factor_layered_result lr ON navs.result_id = lr.result_id " +
            "WHERE lr.task_id = #{taskId} " +
            "ORDER BY lr.quantile, navs.trade_date")
    List<FactorLayeredNavSeries> selectNavSeriesByTaskId(@Param("taskId") Integer taskId);
    
    /**
     * 根据任务ID和分位数查询净值序列
     */
    @Select("SELECT navs.* FROM factor_layered_nav_series navs " +
            "JOIN factor_layered_result lr ON navs.result_id = lr.result_id " +
            "WHERE lr.task_id = #{taskId} AND lr.quantile = #{quantile} " +
            "ORDER BY navs.trade_date")
    List<FactorLayeredNavSeries> selectNavSeriesByTaskIdAndQuantile(
            @Param("taskId") Integer taskId, 
            @Param("quantile") Integer quantile);
    
    // ==================== 持有期统计 ====================
    
    /**
     * 插入持有期统计
     */
    @Insert("INSERT INTO factor_layered_holding_stats " +
            "(result_id, holding_period, mean_return, std_return, min_return, " +
            "q1_return, median_return, q3_return, max_return, win_rate, create_time) " +
            "VALUES (#{resultId}, #{holdingDays}, #{meanReturn}, #{stdReturn}, #{minReturn}, " +
            "#{q1Return}, #{medianReturn}, #{q3Return}, #{maxReturn}, #{winRate}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "statsId")
    int insertHoldingStats(FactorLayeredHoldingStats holdingStats);
    
    /**
     * 批量插入持有期统计
     */
    @Insert("<script>" +
            "INSERT INTO factor_layered_holding_stats " +
            "(result_id, holding_period, mean_return, std_return, min_return, " +
            "q1_return, median_return, q3_return, max_return, win_rate, create_time) VALUES " +
            "<foreach collection='stats' item='stat' separator=','>" +
            "(#{stat.resultId}, #{stat.holdingDays}, #{stat.meanReturn}, #{stat.stdReturn}, " +
            "#{stat.minReturn}, #{stat.q1Return}, #{stat.medianReturn}, #{stat.q3Return}, " +
            "#{stat.maxReturn}, #{stat.winRate}, #{stat.createTime})" +
            "</foreach>" +
            "</script>")
    int batchInsertHoldingStats(@Param("stats") List<FactorLayeredHoldingStats> stats);
    
    /**
     * 根据结果ID查询持有期统计
     */
    @Select("SELECT * FROM factor_layered_holding_stats WHERE result_id = #{resultId} ORDER BY holding_period")
    List<FactorLayeredHoldingStats> selectHoldingStatsByResultId(@Param("resultId") Integer resultId);
    
    /**
     * 根据任务ID查询所有分位数的持有期统计
     */
    @Select("SELECT stats.*, lr.quantile FROM factor_layered_holding_stats stats " +
            "JOIN factor_layered_result lr ON stats.result_id = lr.result_id " +
            "WHERE lr.task_id = #{taskId} " +
            "ORDER BY lr.quantile, stats.holding_period")
    List<FactorLayeredHoldingStats> selectHoldingStatsByTaskId(@Param("taskId") Integer taskId);
    
    /**
     * 根据任务ID、分位数和持有期查询统计
     */
    @Select("SELECT stats.* FROM factor_layered_holding_stats stats " +
            "JOIN factor_layered_result lr ON stats.result_id = lr.result_id " +
            "WHERE lr.task_id = #{taskId} AND lr.quantile = #{quantile} AND stats.holding_period = #{holdingDays}")
    FactorLayeredHoldingStats selectHoldingStatsByTaskIdQuantileAndPeriod(
            @Param("taskId") Integer taskId, 
            @Param("quantile") Integer quantile, 
            @Param("holdingDays") Integer holdingDays);
    
    /**
     * 获取指定持有期的所有分位数统计
     */
    @Select("SELECT stats.*, lr.quantile FROM factor_layered_holding_stats stats " +
            "JOIN factor_layered_result lr ON stats.result_id = lr.result_id " +
            "WHERE lr.task_id = #{taskId} AND stats.holding_period = #{holdingDays} " +
            "ORDER BY lr.quantile")
    List<FactorLayeredHoldingStats> selectHoldingStatsByTaskIdAndPeriod(
            @Param("taskId") Integer taskId, 
            @Param("holdingDays") Integer holdingDays);
}