package com.fengqi.fund.fundadvisor.mapper.factor;

import com.fengqi.fund.fundadvisor.entity.factor.*;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 因子管理综合Mapper接口
 * 整合基础因子、衍生因子、权重配置、因子树等操作
 */
@Repository
@Mapper
public interface FactorManagementMapper {

    // ==================== 基础因子操作 ====================

    /**
     * 插入基础因子
     */
    @Insert("INSERT INTO factor_base (factor_name, factor_code, factor_formula, data_source, " +
            "update_frequency, data_start_date, latest_data_date, data_desc, is_valid, create_time) " +
            "VALUES (#{factorName}, #{factorCode}, #{factorFormula}, #{dataSource}, " +
            "#{updateFrequency}, #{dataStartDate}, #{latestDataDate}, #{dataDesc}, #{isValid}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "baseId")
    int insertBaseFactor(FactorBase factorBase);

    /**
     * 更新基础因子
     */
    @Update("UPDATE factor_base SET factor_name = #{factorName}, factor_formula = #{factorFormula}, " +
            "data_source = #{dataSource}, update_frequency = #{updateFrequency}, " +
            "data_start_date = #{dataStartDate}, data_desc = #{dataDesc} WHERE base_id = #{baseId}")
    int updateBaseFactor(FactorBase factorBase);

    /**
     * 删除基础因子
     */
    @Delete("DELETE FROM factor_base WHERE base_id = #{baseId}")
    int deleteBaseFactor(@Param("baseId") Integer baseId);

    /**
     * 查询基础因子
     */
    @Select("SELECT * FROM factor_base WHERE base_id = #{baseId}")
    FactorBase selectBaseFactorById(@Param("baseId") Integer baseId);

    /**
     * 查询所有有效的基础因子
     */
    @Select("SELECT * FROM factor_base WHERE is_valid = 1 ORDER BY create_time DESC")
    List<FactorBase> selectAllValidBaseFactors();

    /**
     * 根据编码查询基础因子
     */
    @Select("SELECT * FROM factor_base WHERE factor_code = #{factorCode}")
    FactorBase selectBaseFactorByCode(@Param("factorCode") String factorCode);
    
    /**
     * 根据名称查询基础因子
     */
    @Select("SELECT * FROM factor_base WHERE factor_name = #{factorName} AND is_valid = 1")
    FactorBase selectBaseFactorByName(@Param("factorName") String factorName);

    // ==================== 衍生因子操作 ====================

    /**
     * 插入衍生因子
     */
    @Insert("INSERT INTO factor_derived (factor_name, factor_code, factor_desc, calc_strategy_id, " +
            "style_tag_ids, tree_node_id, create_user_id, is_valid, create_time) " +
            "VALUES (#{factorName}, #{factorCode}, #{factorDesc}, #{calcStrategyId}, " +
            "#{styleTagIds}, #{treeNodeId}, #{createUserId}, #{isValid}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "derivedId")
    int insertDerivedFactor(FactorDerived factorDerived);

    /**
     * 更新衍生因子
     */
    @Update("UPDATE factor_derived SET factor_name = #{factorName}, factor_desc = #{factorDesc}, " +
            "calc_strategy_id = #{calcStrategyId}, style_tag_ids = #{styleTagIds}, " +
            "tree_node_id = #{treeNodeId} WHERE derived_id = #{derivedId}")
    int updateDerivedFactor(FactorDerived factorDerived);

    /**
     * 删除衍生因子
     */
    @Delete("DELETE FROM factor_derived WHERE derived_id = #{derivedId}")
    int deleteDerivedFactor(@Param("derivedId") Integer derivedId);

    /**
     * 查询衍生因子
     */
    @Select("SELECT * FROM factor_derived WHERE derived_id = #{derivedId}")
    FactorDerived selectDerivedFactorById(@Param("derivedId") Integer derivedId);

    /**
     * 查询所有有效的衍生因子
     */
    @Select("SELECT fd.*, cs.strategy_name FROM factor_derived fd " +
            "LEFT JOIN calc_strategies cs ON fd.calc_strategy_id = cs.strategy_id " +
            "WHERE fd.is_valid = 1 ORDER BY fd.create_time DESC")
    List<Map<String, Object>> selectAllValidDerivedFactors();

    /**
     * 根据编码查询衍生因子
     */
    @Select("SELECT * FROM factor_derived WHERE factor_code = #{factorCode}")
    FactorDerived selectDerivedFactorByCode(@Param("factorCode") String factorCode);
    
    /**
     * 根据名称查询衍生因子
     */
    @Select("SELECT * FROM factor_derived WHERE factor_name = #{factorName} AND is_valid = 1")
    FactorDerived selectDerivedFactorByName(@Param("factorName") String factorName);

    // ==================== 衍生因子关联操作 ====================

    /**
     * 批量插入衍生因子关联（基础因子权重）
     */
    @Insert({
        "<script>",
        "INSERT INTO derived_factors (derived_id, base_id, weight, weight_desc, calc_strategy_id, formula_remark) VALUES ",
        "<foreach collection='derivedFactors' item='item' separator=','>",
        "(#{item.derivedId}, #{item.baseId}, #{item.weight}, #{item.weightDesc}, #{item.calcStrategyId}, #{item.formulaRemark})",
        "</foreach>",
        "</script>"
    })
    int batchInsertDerivedFactors(@Param("derivedFactors") List<DerivedFactor> derivedFactors);

    /**
     * 删除衍生因子的所有关联
     */
    @Delete("DELETE FROM derived_factors WHERE derived_id = #{derivedId}")
    int deleteDerivedFactorRelations(@Param("derivedId") Integer derivedId);

    /**
     * 查询衍生因子的基础因子关联
     */
    @Select("SELECT df.*, fb.factor_name, fb.factor_code FROM derived_factors df " +
            "LEFT JOIN factor_base fb ON df.base_id = fb.base_id " +
            "WHERE df.derived_id = #{derivedId}")
    List<Map<String, Object>> selectDerivedFactorRelations(@Param("derivedId") Integer derivedId);

    // ==================== 计算策略操作 ====================

    /**
     * 查询所有有效的计算策略
     */
    @Select("SELECT * FROM calc_strategies WHERE is_valid = 1 ORDER BY strategy_id")
    List<CalcStrategy> selectAllValidCalcStrategies();

    /**
     * 查询计算策略
     */
    @Select("SELECT * FROM calc_strategies WHERE strategy_id = #{strategyId}")
    CalcStrategy selectCalcStrategyById(@Param("strategyId") Integer strategyId);

    // ==================== 因子树操作 ====================

    /**
     * 插入因子树节点
     */
    @Insert("INSERT INTO factor_tree (parent_id, node_name, node_type, factor_id, is_leaf, " +
            "sort_order, description, scene_id) " +
            "VALUES (#{parentId}, #{nodeName}, #{nodeType}, #{factorId}, #{isLeaf}, " +
            "#{sortOrder}, #{description}, #{sceneId})")
    @Options(useGeneratedKeys = true, keyProperty = "treeid")
    int insertFactorTreeNode(FactorTree factorTree);

    /**
     * 更新因子树节点
     */
    @Update("UPDATE factor_tree SET parent_id = #{parentId}, node_name = #{nodeName}, " +
            "node_type = #{nodeType}, factor_id = #{factorId}, is_leaf = #{isLeaf}, " +
            "sort_order = #{sortOrder}, description = #{description} WHERE treeid = #{treeid}")
    int updateFactorTreeNode(FactorTree factorTree);

    /**
     * 删除因子树节点
     */
    @Delete("DELETE FROM factor_tree WHERE treeid = #{treeId}")
    int deleteFactorTreeNode(@Param("treeId") Integer treeId);

    /**
     * 查询因子树节点
     */
    @Select("SELECT ft.*, fts.scene_name FROM factor_tree ft " +
            "LEFT JOIN factor_tree_scene fts ON ft.scene_id = fts.scene_id " +
            "WHERE ft.treeid = #{treeId}")
    Map<String, Object> selectFactorTreeNodeById(@Param("treeId") Integer treeId);

    /**
     * 查询因子树结构
     */
    @Select("SELECT ft.*, fts.scene_name FROM factor_tree ft " +
            "LEFT JOIN factor_tree_scene fts ON ft.scene_id = fts.scene_id " +
            "WHERE ft.scene_id = #{sceneId} ORDER BY ft.parent_id, ft.sort_order")
    List<Map<String, Object>> selectFactorTreeByScene(@Param("sceneId") String sceneId);

    /**
     * 查询因子树的所有子节点
     */
    @Select("SELECT * FROM factor_tree WHERE parent_id = #{parentId} ORDER BY sort_order")
    List<FactorTree> selectFactorTreeChildren(@Param("parentId") Integer parentId);

    // ==================== 场景管理操作 ====================

    /**
     * 查询所有有效的场景
     */
    @Select("SELECT * FROM factor_tree_scene WHERE is_valid = 1 ORDER BY create_time")
    List<FactorTreeScene> selectAllValidScenes();

    /**
     * 查询场景
     */
    @Select("SELECT * FROM factor_tree_scene WHERE scene_id = #{sceneId}")
    FactorTreeScene selectSceneById(@Param("sceneId") String sceneId);

    // ==================== 综合查询操作 ====================

    /**
     * 查询因子及其树节点关联信息
     */
    @Select("SELECT ft.*, fd.factor_name as derived_factor_name, fd.factor_code as derived_factor_code, " +
            "fb.factor_name as base_factor_name, fb.factor_code as base_factor_code " +
            "FROM factor_tree ft " +
            "LEFT JOIN factor_derived fd ON ft.factor_id = fd.derived_id AND ft.node_type = 'DERIVED_FACTOR' " +
            "LEFT JOIN factor_base fb ON ft.factor_id = fb.base_id AND ft.node_type = 'BASE_FACTOR' " +
            "WHERE ft.scene_id = #{sceneId}")
    List<Map<String, Object>> selectFactorTreeWithFactors(@Param("sceneId") String sceneId);

    /**
     * 查询可用于创建衍生因子的基础因子
     */
    @Select("SELECT fb.* FROM factor_base fb " +
            "WHERE fb.is_valid = 1 AND fb.latest_data_date IS NOT NULL " +
            "ORDER BY fb.create_time DESC")
    List<FactorBase> selectAvailableBaseFactorsForDerived();

    /**
     * 检查因子编码是否存在
     */
    @Select("SELECT COUNT(*) FROM factor_base WHERE factor_code = #{factorCode} " +
            "UNION ALL " +
            "SELECT COUNT(*) FROM factor_derived WHERE factor_code = #{factorCode}")
    List<Integer> checkFactorCodeExists(@Param("factorCode") String factorCode);

    /**
     * 验证基础因子数据完整性
     */
    @Select("SELECT base_id, factor_name, factor_code, " +
            "CASE WHEN latest_data_date IS NULL THEN 'FAILED' ELSE 'PASSED' END as data_status " +
            "FROM factor_base WHERE base_id IN " +
            "<foreach collection='baseIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>")
    List<Map<String, Object>> validateBaseFactorData(@Param("baseIds") List<Integer> baseIds);

    // ==================== 风格标签相关查询 ====================

    /**
     * 根据风格标签ID查询关联的衍生因子
     * 使用 FIND_IN_SET 函数匹配逗号分隔的 style_tag_ids 字段
     */
    @Select("SELECT * FROM factor_derived " +
            "WHERE is_valid = 1 AND FIND_IN_SET(#{tagId}, style_tag_ids) > 0 " +
            "ORDER BY create_time DESC")
    List<FactorDerived> selectDerivedFactorsByStyleTagId(@Param("tagId") Integer tagId);

    /**
     * 根据多个风格标签ID查询关联的衍生因子（满足任一标签即可）
     */
    @Select("<script>" +
            "SELECT * FROM factor_derived " +
            "WHERE is_valid = 1 AND (" +
            "<foreach collection='tagIds' item='tagId' separator=' OR '>" +
            "FIND_IN_SET(#{tagId}, style_tag_ids) > 0" +
            "</foreach>" +
            ") ORDER BY create_time DESC" +
            "</script>")
    List<FactorDerived> selectDerivedFactorsByStyleTagIds(@Param("tagIds") List<Integer> tagIds);

    /**
     * 根据风格标签编码查询关联的衍生因子
     * 先通过编码查询标签ID，然后查询关联的因子
     */
    @Select("SELECT fd.* FROM factor_derived fd " +
            "INNER JOIN style_tag st ON FIND_IN_SET(st.tag_id, fd.style_tag_ids) > 0 " +
            "WHERE fd.is_valid = 1 AND st.tag_code = #{tagCode} AND st.is_valid = 1 " +
            "ORDER BY fd.create_time DESC")
    List<FactorDerived> selectDerivedFactorsByStyleTagCode(@Param("tagCode") String tagCode);

    /**
     * 根据多个风格标签编码查询关联的衍生因子（满足任一标签即可）
     */
    @Select("<script>" +
            "SELECT DISTINCT fd.* FROM factor_derived fd " +
            "INNER JOIN style_tag st ON FIND_IN_SET(st.tag_id, fd.style_tag_ids) > 0 " +
            "WHERE fd.is_valid = 1 AND st.is_valid = 1 AND st.tag_code IN " +
            "<foreach collection='tagCodes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            " ORDER BY fd.create_time DESC" +
            "</script>")
    List<FactorDerived> selectDerivedFactorsByStyleTagCodes(@Param("tagCodes") List<String> tagCodes);
}