package com.fengqi.fund.fundadvisor.mapper.factor;

import com.fengqi.fund.fundadvisor.entity.factor.DerivedFactor;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 衍生因子数据访问层
 * 
 * 提供衍生因子的增删改查功能
 * 支持权重配置和基础因子关联
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Mapper
public interface DerivedFactorMapper {
    
    /**
     * 获取所有有效的衍生因子
     */
    @Select("SELECT * FROM factor_derived WHERE is_valid = 1 ORDER BY derived_id")
    List<DerivedFactor> getAllValidDerivedFactors();
    
    /**
     * 根据ID查询衍生因子
     */
    @Select("SELECT * FROM factor_derived WHERE derived_id = #{derivedId} AND is_valid = 1")
    DerivedFactor getDerivedFactorById(@Param("derivedId") Integer derivedId);
    
    /**
     * 根据因子编码查询衍生因子
     */
    @Select("SELECT * FROM factor_derived WHERE factor_code = #{factorCode} AND is_valid = 1")
    DerivedFactor getDerivedFactorByCode(@Param("factorCode") String factorCode);
    
    /**
     * 根据多个ID查询衍生因子
     */
    @Select("<script>" +
            "SELECT * FROM factor_derived WHERE derived_id IN " +
            "<foreach collection='derivedIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND is_valid = 1 ORDER BY derived_id" +
            "</script>")
    List<DerivedFactor> getDerivedFactorsByIds(@Param("derivedIds") List<Integer> derivedIds);
    
    /**
     * 根据风格标签ID查询衍生因子
     */
    @Select("SELECT * FROM factor_derived " +
            "WHERE style_tag_ids LIKE CONCAT('%', #{tagId}, '%') " +
            "AND is_valid = 1 ORDER BY derived_id")
    List<DerivedFactor> getDerivedFactorsByStyleTagId(@Param("tagId") Integer tagId);
    
    /**
     * 根据风格标签编码查询衍生因子
     */
    @Select("SELECT fd.* FROM factor_derived fd " +
            "JOIN style_tag st ON FIND_IN_SET(st.tag_id, fd.style_tag_ids) > 0 " +
            "WHERE st.tag_code = #{tagCode} " +
            "AND fd.is_valid = 1 AND st.is_valid = 1 " +
            "ORDER BY fd.derived_id")
    List<DerivedFactor> getDerivedFactorsByStyleTagCode(@Param("tagCode") String tagCode);
    
    /**
     * 根据多个风格标签编码查询衍生因子
     */
    @Select("<script>" +
            "SELECT DISTINCT fd.* FROM factor_derived fd " +
            "JOIN style_tag st ON FIND_IN_SET(st.tag_id, fd.style_tag_ids) > 0 " +
            "WHERE st.tag_code IN " +
            "<foreach collection='tagCodes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            "AND fd.is_valid = 1 AND st.is_valid = 1 " +
            "ORDER BY fd.derived_id" +
            "</script>")
    List<DerivedFactor> getDerivedFactorsByStyleTagCodes(@Param("tagCodes") List<String> tagCodes);
    
    /**
     * 搜索衍生因子
     */
    @Select("SELECT * FROM factor_derived " +
            "WHERE is_valid = 1 AND " +
            "(factor_name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "factor_code LIKE CONCAT('%', #{keyword}, '%') OR " +
            "factor_desc LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY derived_id")
    List<DerivedFactor> searchDerivedFactors(@Param("keyword") String keyword);
    
    /**
     * 获取衍生因子总数
     */
    @Select("SELECT COUNT(*) FROM factor_derived WHERE is_valid = 1")
    Integer getTotalDerivedFactorCount();
    
    /**
     * 获取指定树节点下的衍生因子
     */
    @Select("SELECT * FROM factor_derived " +
            "WHERE tree_node_id = #{treeNodeId} " +
            "AND is_valid = 1 ORDER BY derived_id")
    List<DerivedFactor> getDerivedFactorsByTreeNodeId(@Param("treeNodeId") Integer treeNodeId);
    
    /**
     * 获取指定场景下的衍生因子
     */
    @Select("SELECT fd.* FROM factor_derived fd " +
            "JOIN factor_tree ft ON fd.tree_node_id = ft.treeid " +
            "WHERE ft.scene_id = #{sceneId} " +
            "AND fd.is_valid = 1 AND ft.is_leaf = 1 " +
            "ORDER BY ft.sort_order, fd.derived_id")
    List<DerivedFactor> getDerivedFactorsByScene(@Param("sceneId") String sceneId);
    
    /**
     * 创建衍生因子
     */
    @Insert("INSERT INTO factor_derived (factor_name, factor_code, factor_desc, calc_strategy_id, " +
            "style_tag_ids, tree_node_id, create_user_id, is_valid) " +
            "VALUES (#{factorName}, #{factorCode}, #{factorDesc}, #{calcStrategyId}, " +
            "#{styleTagIds}, #{treeNodeId}, #{createUserId}, #{isValid})")
    @Options(useGeneratedKeys = true, keyProperty = "derivedId")
    int createDerivedFactor(DerivedFactor derivedFactor);
    
    /**
     * 更新衍生因子
     */
    @Update("UPDATE factor_derived SET " +
            "factor_name = #{factorName}, " +
            "factor_code = #{factorCode}, " +
            "factor_desc = #{factorDesc}, " +
            "calc_strategy_id = #{calcStrategyId}, " +
            "style_tag_ids = #{styleTagIds}, " +
            "tree_node_id = #{treeNodeId} " +
            "WHERE derived_id = #{derivedId}")
    int updateDerivedFactor(DerivedFactor derivedFactor);
    
    /**
     * 删除衍生因子（逻辑删除）
     */
    @Update("UPDATE factor_derived SET is_valid = 0 WHERE derived_id = #{derivedId}")
    int deleteDerivedFactor(@Param("derivedId") Integer derivedId);
    
    /**
     * 批量删除衍生因子
     */
    @Update("<script>" +
            "UPDATE factor_derived SET is_valid = 0 WHERE derived_id IN " +
            "<foreach collection='derivedIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDeleteDerivedFactors(@Param("derivedIds") List<Integer> derivedIds);
    
    /**
     * 获取常用衍生因子
     */
    @Select("SELECT fd.* FROM factor_derived fd " +
            "LEFT JOIN factor_ic_ir_result ic ON fd.derived_id = ic.factor_id " +
            "WHERE fd.is_valid = 1 " +
            "ORDER BY ic.ic_mean DESC, fd.derived_id " +
            "LIMIT #{limit}")
    List<DerivedFactor> getPopularDerivedFactors(@Param("limit") Integer limit);
    
    /**
     * 获取最近创建的衍生因子
     */
    @Select("SELECT * FROM factor_derived " +
            "WHERE is_valid = 1 " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<DerivedFactor> getRecentDerivedFactors(@Param("limit") Integer limit);
    
    /**
     * 检查因子编码是否存在
     */
    @Select("SELECT COUNT(*) FROM factor_derived " +
            "WHERE factor_code = #{factorCode} " +
            "AND is_valid = 1 " +
            "AND derived_id != #{excludeId}")
    Integer countByFactorCodeExcludeId(@Param("factorCode") String factorCode, @Param("excludeId") Integer excludeId);
}