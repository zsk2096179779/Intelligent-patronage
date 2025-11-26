package com.fengqi.fund.fundadvisor.mapper.factor;

import com.fengqi.fund.fundadvisor.entity.factor.FactorWeight;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 因子权重Mapper接口
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
@Mapper
public interface FactorWeightMapper {
    
    /**
     * 插入权重配置
     */
    @Insert("INSERT INTO factor_weight (derived_factor_id, base_factor_id, weight, weight_percentage, " +
            "is_enabled, create_time, update_time, creator, updater, remark) " +
            "VALUES (#{derivedFactorId}, #{baseFactorId}, #{weight}, #{weightPercentage}, " +
            "#{isEnabled}, #{createTime}, #{updateTime}, #{creator}, #{updater}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "weightId")
    int insertWeight(FactorWeight factorWeight);
    
    /**
     * 批量插入权重配置
     */
    @Insert({
        "<script>",
        "INSERT INTO factor_weight (derived_factor_id, base_factor_id, weight, weight_percentage, " +
        "is_enabled, create_time, update_time, creator, updater, remark) VALUES ",
        "<foreach collection='weights' item='weight' separator=','>",
        "(#{weight.derivedFactorId}, #{weight.baseFactorId}, #{weight.weight}, #{weight.weightPercentage}, " +
        "#{weight.isEnabled}, #{weight.createTime}, #{weight.updateTime}, #{weight.creator}, #{weight.updater}, #{weight.remark})",
        "</foreach>",
        "</script>"
    })
    int batchInsertWeights(@Param("weights") List<FactorWeight> weights);
    
    /**
     * 更新权重配置
     */
    @Update("UPDATE factor_weight SET weight = #{weight}, weight_percentage = #{weightPercentage}, " +
            "is_enabled = #{isEnabled}, update_time = #{updateTime}, updater = #{updater}, remark = #{remark} " +
            "WHERE weight_id = #{weightId}")
    int updateWeight(FactorWeight factorWeight);
    
    /**
     * 删除权重配置
     */
    @Delete("DELETE FROM factor_weight WHERE weight_id = #{weightId}")
    int deleteWeight(@Param("weightId") Integer weightId);
    
    /**
     * 批量删除权重配置
     */
    @Delete({
        "<script>",
        "DELETE FROM factor_weight WHERE weight_id IN ",
        "<foreach collection='weightIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    int batchDeleteWeights(@Param("weightIds") List<Integer> weightIds);
    
    /**
     * 删除衍生因子的所有权重配置
     */
    @Delete("DELETE FROM factor_weight WHERE derived_factor_id = #{derivedFactorId}")
    int deleteWeightsByDerivedFactor(@Param("derivedFactorId") Integer derivedFactorId);
    
    /**
     * 根据ID获取权重配置
     */
    @Select("SELECT * FROM factor_weight WHERE weight_id = #{weightId}")
    FactorWeight getWeightById(@Param("weightId") Integer weightId);
    
    /**
     * 获取衍生因子的所有权重配置
     */
    @Select("SELECT fw.*, fb.factor_name, fb.factor_code, fb.data_source " +
            "FROM factor_weight fw " +
            "LEFT JOIN factor_base fb ON fw.base_factor_id = fb.base_id " +
            "WHERE fw.derived_factor_id = #{derivedFactorId} " +
            "ORDER BY fw.weight DESC")
    List<FactorWeight> getWeightsByDerivedFactor(@Param("derivedFactorId") Integer derivedFactorId);
    
    /**
     * 获取权重配置详情（包含基础因子信息）
     */
    @Select("SELECT fw.weight_id, fw.derived_factor_id, fw.base_factor_id, fw.weight, fw.weight_percentage, " +
            "fw.is_enabled, fw.create_time, fw.update_time, fw.creator, fw.updater, fw.remark, " +
            "fb.factor_name as base_factor_name, fb.factor_code as base_factor_code, fb.data_source " +
            "FROM factor_weight fw " +
            "LEFT JOIN factor_base fb ON fw.base_factor_id = fb.base_id " +
            "WHERE fw.weight_id = #{weightId}")
    FactorWeight getWeightWithDetail(@Param("weightId") Integer weightId);
    
    /**
     * 获取基础因子的所有权重配置
     */
    @Select("SELECT fw.*, fb.factor_name, fb.factor_code " +
            "FROM factor_weight fw " +
            "LEFT JOIN factor_base fb ON fw.derived_factor_id = fb.base_id " +
            "WHERE fw.base_factor_id = #{baseFactorId} " +
            "ORDER BY fw.update_time DESC")
    List<FactorWeight> getWeightsByBaseFactor(@Param("baseFactorId") Integer baseFactorId);
    
    /**
     * 计算衍生因子的权重总和
     */
    @Select("SELECT COALESCE(SUM(weight), 0) FROM factor_weight " +
            "WHERE derived_factor_id = #{derivedFactorId} AND is_enabled = 1")
    Double getTotalWeightByDerivedFactor(@Param("derivedFactorId") Integer derivedFactorId);
    
    /**
     * 检查权重配置是否存在
     */
    @Select("SELECT COUNT(*) FROM factor_weight " +
            "WHERE derived_factor_id = #{derivedFactorId} AND base_factor_id = #{baseFactorId}")
    int checkWeightExists(@Param("derivedFactorId") Integer derivedFactorId, 
                          @Param("baseFactorId") Integer baseFactorId);
    
    /**
     * 获取所有权重配置（分页）
     */
    @Select("SELECT fw.*, fb.factor_name as base_factor_name, fb.factor_code as base_factor_code, " +
            "df.factor_name as derived_factor_name " +
            "FROM factor_weight fw " +
            "LEFT JOIN factor_base fb ON fw.base_factor_id = fb.base_id " +
            "LEFT JOIN factor_base df ON fw.derived_factor_id = df.base_id " +
            "ORDER BY fw.update_time DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<FactorWeight> getAllWeightsWithPagination(@Param("limit") Integer limit, @Param("offset") Integer offset);
    
    /**
     * 获取权重配置总数
     */
    @Select("SELECT COUNT(*) FROM factor_weight")
    Integer getTotalWeightCount();
}