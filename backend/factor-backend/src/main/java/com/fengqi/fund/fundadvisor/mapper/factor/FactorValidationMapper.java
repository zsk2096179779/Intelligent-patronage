package com.fengqi.fund.fundadvisor.mapper.factor;

import com.fengqi.fund.fundadvisor.entity.factor.FactorIcIrResult;
import com.fengqi.fund.fundadvisor.entity.factor.FactorIcSequence;
import com.fengqi.fund.fundadvisor.entity.factor.FactorQuantileReturnCurve;
import com.fengqi.fund.fundadvisor.entity.factor.FactorValidationTask;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 因子检验Mapper接口
 */
@Mapper
public interface FactorValidationMapper {
    
    // ==================== 任务管理 ====================
    
    /**
     * 插入检验任务
     */
    @Insert("INSERT INTO factor_validation_task " +
            "(task_name, task_type, factor_ids, start_date, end_date, task_status, progress, " +
            "create_user_id, create_time) " +
            "VALUES (#{taskName}, #{taskType}, #{factorIds}, #{startDate}, #{endDate}, " +
            "#{taskStatus}, #{progress}, #{createUserId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "taskId")
    int insertTask(FactorValidationTask task);
    
    /**
     * 根据ID查询任务
     */
    @Select("SELECT * FROM factor_validation_task WHERE task_id = #{taskId}")
    FactorValidationTask selectTaskById(@Param("taskId") Integer taskId);
    
    /**
     * 更新任务状态
     */
    @Update("UPDATE factor_validation_task SET " +
            "task_status = #{taskStatus}, " +
            "progress = #{progress}, " +
            "error_message = #{errorMessage}, " +
            "start_time = #{startTime}, " +
            "end_time = #{endTime} " +
            "WHERE task_id = #{taskId}")
    int updateTaskStatus(FactorValidationTask task);
    
    /**
     * 查询所有任务
     */
    @Select("SELECT * FROM factor_validation_task ORDER BY create_time DESC")
    List<FactorValidationTask> selectAllTasks();
    
    // ==================== IC/IR结果 ====================
    
    /**
     * 插入IC/IR结果
     */
    @Insert("INSERT INTO factor_ic_ir_result " +
            "(task_id, factor_id, factor_code, factor_name, ic_mean, ic_std, ir_value, " +
            "ic_positive_ratio, ic_sequence, calculation_date, create_time) " +
            "VALUES (#{taskId}, #{factorId}, #{factorCode}, #{factorName}, #{icMean}, " +
            "#{icStd}, #{irValue}, #{icPositiveRatio}, #{icSequence}, #{calculationDate}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "resultId")
    int insertIcIrResult(FactorIcIrResult result);
    
    /**
     * 根据任务ID查询IC/IR结果
     */
    @Select("SELECT * FROM factor_ic_ir_result WHERE task_id = #{taskId} ORDER BY factor_id")
    List<FactorIcIrResult> selectIcIrResultsByTaskId(@Param("taskId") Integer taskId);
    
    /**
     * 根据因子ID查询IC/IR结果
     */
    @Select("SELECT * FROM factor_ic_ir_result WHERE factor_id = #{factorId} " +
            "ORDER BY calculation_date DESC LIMIT #{limit}")
    List<FactorIcIrResult> selectIcIrResultsByFactorId(@Param("factorId") Integer factorId, 
                                                         @Param("limit") Integer limit);
    
    // ==================== IC序列明细 ====================
    
    /**
     * 插入IC序列明细
     */
    @Insert("INSERT INTO factor_ic_sequence " +
            "(result_id, trade_date, ic_value, rank_ic, create_time) " +
            "VALUES (#{resultId}, #{tradeDate}, #{icValue}, #{rankIc}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "sequenceId")
    int insertIcSequence(FactorIcSequence sequence);
    
    /**
     * 批量插入IC序列明细
     */
    @Insert("<script>" +
            "INSERT INTO factor_ic_sequence (result_id, trade_date, ic_value, rank_ic, create_time) VALUES " +
            "<foreach collection='sequences' item='seq' separator=','>" +
            "(#{seq.resultId}, #{seq.tradeDate}, #{seq.icValue}, #{seq.rankIc}, #{seq.createTime})" +
            "</foreach>" +
            "</script>")
    int batchInsertIcSequence(@Param("sequences") List<FactorIcSequence> sequences);
    
    /**
     * 根据结果ID查询IC序列
     */
    @Select("SELECT * FROM factor_ic_sequence WHERE result_id = #{resultId} ORDER BY trade_date")
    List<FactorIcSequence> selectIcSequenceByResultId(@Param("resultId") Integer resultId);
    
    // ==================== 分位数累计收益率曲线 ====================
    
    /**
     * 插入分位数累计收益率曲线数据
     */
    @Insert("INSERT INTO factor_quantile_return_curve " +
            "(task_id, factor_id, factor_code, factor_name, dates_json, quantile_returns_json, " +
            "quantile_descriptions_json, calculation_date, create_time) " +
            "VALUES (#{taskId}, #{factorId}, #{factorCode}, #{factorName}, #{datesJson}, " +
            "#{quantileReturnsJson}, #{quantileDescriptionsJson}, #{calculationDate}, #{createTime}) " +
            "ON DUPLICATE KEY UPDATE " +
            "dates_json = VALUES(dates_json), " +
            "quantile_returns_json = VALUES(quantile_returns_json), " +
            "quantile_descriptions_json = VALUES(quantile_descriptions_json), " +
            "update_time = CURRENT_TIMESTAMP")
    @Options(useGeneratedKeys = true, keyProperty = "curveId")
    int insertOrUpdateQuantileReturnCurve(FactorQuantileReturnCurve curve);
    
    /**
     * 根据任务ID和因子ID查询分位数累计收益率曲线
     */
    @Select("SELECT * FROM factor_quantile_return_curve " +
            "WHERE task_id = #{taskId} AND factor_id = #{factorId} " +
            "ORDER BY create_time DESC LIMIT 1")
    FactorQuantileReturnCurve selectQuantileReturnCurve(@Param("taskId") Integer taskId, 
                                                         @Param("factorId") Integer factorId);
}







