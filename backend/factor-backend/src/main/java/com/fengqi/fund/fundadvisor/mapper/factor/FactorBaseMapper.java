package com.fengqi.fund.fundadvisor.mapper.factor;

import com.fengqi.fund.fundadvisor.entity.factor.FactorBase;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 基础因子数据访问层
 * 
 * 提供基础因子的查询、搜索、分页等功能
 * 支持因子多选和预览功能
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
@Mapper
public interface FactorBaseMapper {
    
    /**
     * 获取所有有效的基础因子
     */
    @Select("SELECT * FROM factor_base WHERE is_valid = 1 ORDER BY base_id")
    List<FactorBase> getAllValidFactors();
    
    /**
     * 分页查询基础因子
     */
    @Select("SELECT * FROM factor_base WHERE is_valid = 1 " +
            "ORDER BY base_id LIMIT #{pageSize} OFFSET #{offset}")
    List<FactorBase> getFactorsWithPagination(@Param("pageSize") Integer pageSize, 
                                           @Param("offset") Integer offset);
    
    /**
     * 根据因子类型查询基础因子
     */
    @Select("SELECT * FROM factor_base " +
            "WHERE is_valid = 1 " +
            "ORDER BY base_id")
    List<FactorBase> getFactorsByType(@Param("factorType") String factorType);
    
    /**
     * 根据ID查询基础因子
     */
    @Select("SELECT * FROM factor_base WHERE base_id = #{baseId} AND is_valid = 1")
    FactorBase getFactorById(@Param("baseId") Integer baseId);
    
    /**
     * 根据因子编码查询基础因子
     */
    @Select("SELECT * FROM factor_base WHERE factor_code = #{factorCode} AND is_valid = 1")
    FactorBase getFactorByCode(@Param("factorCode") String factorCode);
    
    /**
     * 根据多个ID查询基础因子
     */
    @Select("<script>" +
            "SELECT * FROM factor_base WHERE base_id IN " +
            "<foreach collection='factorIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND is_valid = 1 ORDER BY base_id" +
            "</script>")
    List<FactorBase> getFactorsByIds(@Param("factorIds") List<Integer> factorIds);
    
    /**
     * 搜索基础因子
     */
    @Select("SELECT * FROM factor_base " +
            "WHERE is_valid = 1 AND " +
            "(factor_name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "factor_code LIKE CONCAT('%', #{keyword}, '%') OR " +
            "data_source LIKE CONCAT('%', #{keyword}, '%') OR " +
            "data_desc LIKE CONCAT('%', #{keyword}, '%') OR " +
            "factor_formula LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY base_id")
    List<FactorBase> searchFactors(@Param("keyword") String keyword);
    
    /**
     * 获取基础因子总数（用于分页）
     */
    @Select("SELECT COUNT(*) FROM factor_base WHERE is_valid = 1")
    Integer getTotalFactorCount();
    
    /**
     * 获取按类型分组的因子统计
     */
    @Select("SELECT 'ALL' as factor_type, COUNT(*) as count " +
            "FROM factor_base " +
            "WHERE is_valid = 1 " +
            "GROUP BY factor_type ORDER BY count DESC")
    List<Object> getFactorTypeStatistics();
    
    /**
     * 获取常用因子（可配置，比如使用频率高的前20个）
     */
    @Select("SELECT f.* FROM factor_base f " +
            "WHERE f.is_valid = 1 " +
            "ORDER BY f.latest_data_date DESC, f.base_id " +
            "LIMIT #{limit}")
    List<FactorBase> getPopularFactors(@Param("limit") Integer limit);
    
    /**
     * 获取指定数据源的因子
     */
    @Select("SELECT * FROM factor_base " +
            "WHERE is_valid = 1 AND (#{dataSource} IS NULL OR data_source = #{dataSource}) " +
            "ORDER BY base_id")
    List<FactorBase> getFactorsByDataSource(@Param("dataSource") String dataSource);
}