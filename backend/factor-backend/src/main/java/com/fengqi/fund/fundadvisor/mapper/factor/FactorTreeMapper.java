package com.fengqi.fund.fundadvisor.mapper.factor;

import com.fengqi.fund.fundadvisor.entity.factor.FactorTree;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FactorTreeMapper {
    
    /**
     * 创建因子树
     */
    @Insert("INSERT INTO factor_tree (parent_id, node_name, node_type, factor_id, is_leaf, sort_order, description, scene_id) " +
            "VALUES (NULL, #{nodeName}, 'TREE', NULL, 0, 1, #{description}, #{sceneId})")
    @Options(useGeneratedKeys = true, keyProperty = "treeid")
    int createFactorTree(FactorTree factorTree);
    
    /**
     * 根据场景ID查询所有树
     */
    @Select("SELECT * FROM factor_tree WHERE node_type = 'TREE' AND (#{sceneId} IS NULL OR scene_id = #{sceneId}) " +
            "ORDER BY treeid")
    List<FactorTree> getTreesByScene(@Param("sceneId") String sceneId);
    
    /**
     * 获取树结构
     */
    @Select("SELECT * FROM factor_tree WHERE node_type = 'TREE' AND treeid = #{treeId}")
    FactorTree getTreeById(@Param("treeId") Integer treeId);
    
    /**
     * 根据ID获取任意类型的节点
     */
    @Select("SELECT * FROM factor_tree WHERE treeid = #{treeId}")
    FactorTree getNodeById(@Param("treeId") Integer treeId);
    
    /**
     * 获取子节点
     */
    @Select("SELECT * FROM factor_tree WHERE parent_id = #{parentId} ORDER BY sort_order, treeid")
    List<FactorTree> getChildNodes(@Param("parentId") Integer parentId);
    
    /**
     * 获取完整树结构（递归查询）
     */
    @Select("WITH RECURSIVE tree_cte AS (" +
            "  SELECT * FROM factor_tree WHERE treeid = #{treeId}" +
            "  UNION ALL" +
            "  SELECT f.* FROM factor_tree f" +
            "  INNER JOIN tree_cte t ON f.parent_id = t.treeid" +
            ") SELECT * FROM tree_cte ORDER BY parent_id, sort_order, treeid")
    List<FactorTree> getCompleteTreeStructure(@Param("treeId") Integer treeId);
    
    /**
     * 添加节点
     */
    @Insert("INSERT INTO factor_tree (parent_id, node_name, node_type, factor_id, is_leaf, sort_order, description, scene_id) " +
            "VALUES (#{parentId}, #{nodeName}, #{nodeType}, #{factorId}, #{isLeaf}, #{sortOrder}, #{description}, #{sceneId})")
    @Options(useGeneratedKeys = true, keyProperty = "treeid")
    int addTreeNode(FactorTree node);
    
    /**
     * 更新节点信息
     */
    @Update("UPDATE factor_tree SET node_name = #{nodeName}, description = #{description}, " +
            "sort_order = #{sortOrder} WHERE treeid = #{treeid}")
    int updateTreeNode(FactorTree node);
    
    /**
     * 移动节点
     */
    @Update("UPDATE factor_tree SET parent_id = #{newParentId}, sort_order = #{sortOrder} WHERE treeid = #{treeid}")
    int moveNode(@Param("treeid") Integer treeid, @Param("newParentId") Integer newParentId, 
                 @Param("sortOrder") Integer sortOrder);
    
    /**
     * 删除节点
     */
    @Delete("DELETE FROM factor_tree WHERE treeid = #{treeid}")
    int deleteTreeNode(@Param("treeid") Integer treeid);
    
    /**
     * 检查节点是否为空（无子节点）
     */
    @Select("SELECT COUNT(*) FROM factor_tree WHERE parent_id = #{treeid}")
    int hasChildNodes(@Param("treeid") Integer treeid);
    
    /**
     * 获取最大排序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM factor_tree WHERE parent_id = #{parentId}")
    Integer getMaxSortOrder(@Param("parentId") Integer parentId);
    
    /**
     * 懒加载获取直接子节点
     */
    @Select("SELECT * FROM factor_tree WHERE parent_id = #{parentId} ORDER BY sort_order, treeid LIMIT #{pageSize} OFFSET #{offset}")
    List<FactorTree> getLazyChildNodes(@Param("parentId") Integer parentId, 
                                       @Param("pageSize") Integer pageSize, 
                                       @Param("offset") Integer offset);
    
    /**
     * 获取所有后代节点（递归）
     */
    @Select("WITH RECURSIVE descendant_cte AS (" +
            "  SELECT * FROM factor_tree WHERE treeid = #{nodeId}" +
            "  UNION ALL" +
            "  SELECT f.* FROM factor_tree f" +
            "  INNER JOIN descendant_cte d ON f.parent_id = d.treeid" +
            ") SELECT * FROM descendant_cte ORDER BY parent_id, sort_order, treeid")
    List<FactorTree> getAllDescendants(@Param("nodeId") Integer nodeId);
    
    /**
     * 获取节点的完整路径（从根到当前节点）
     */
    @Select("WITH RECURSIVE path_cte AS (" +
            "  SELECT * FROM factor_tree WHERE parent_id IS NULL" +
            "  UNION ALL" +
            "  SELECT f.* FROM factor_tree f" +
            "  INNER JOIN path_cte p ON f.parent_id = p.treeid" +
            ")" +
            " SELECT p.* FROM factor_tree p" +
            "  INNER JOIN path_cte c ON p.treeid = c.treeid" +
            "  WHERE c.treeid = #{nodeId} OR c.parent_id = #{nodeId}" +
            "  ORDER BY p.sort_order, p.treeid")
    List<FactorTree> getNodePath(@Param("nodeId") Integer nodeId);
    
    /**
     * 获取树根节点的子节点数量（用于分页）
     */
    @Select("SELECT COUNT(*) FROM factor_tree WHERE parent_id = #{parentId}")
    Integer getChildNodeCount(@Param("parentId") Integer parentId);
    
    /**
     * 根据名称搜索树
     */
    @Select("SELECT * FROM factor_tree WHERE node_type = 'TREE' AND " +
            "(node_name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))")
    List<FactorTree> searchTrees(@Param("keyword") String keyword);
    
    /**
     * 搜索因子
     */
    @Select("SELECT * FROM factor_tree WHERE node_type != 'TREE' AND " +
            "(node_name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))")
    List<FactorTree> searchFactors(@Param("keyword") String keyword);
}