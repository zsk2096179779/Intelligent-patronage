package com.fengqi.fund.fundadvisor.service.factor;

import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.entity.factor.FactorTree;
import com.fengqi.fund.fundadvisor.entity.factor.FactorTreeScene;

import java.util.List;
import java.util.Map;

public interface FactorTreeService {
    
    /**
     * 创建因子树
     */
    ResultDTO<FactorTree> createFactorTree(String treeName, String description, String sceneId);
    
    /**
     * 获取所有因子树
     */
    ResultDTO<List<FactorTree>> getAllTrees(String sceneId);
    
    /**
     * 获取树结构
     */
    ResultDTO<Map<String, Object>> getTreeStructure(Integer treeId);
    
    /**
     * 添加节点
     */
    ResultDTO<FactorTree> addTreeNode(Integer treeId, Integer parentId, String nodeName, 
                                     String nodeType, Integer factorId, String description);
    
    /**
     * 更新节点
     */
    ResultDTO<FactorTree> updateTreeNode(Integer nodeId, String nodeName, String description, Integer sortOrder);
    
    /**
     * 移动节点
     */
    ResultDTO<Boolean> moveTreeNode(Integer nodeId, Integer newParentId, Integer sortOrder);
    
    /**
     * 删除空节点
     */
    ResultDTO<Boolean> deleteEmptyNode(Integer nodeId);
    
    /**
     * 搜索因子树
     */
    ResultDTO<List<FactorTree>> searchTrees(String keyword);
    
    /**
     * 搜索因子
     */
    ResultDTO<List<FactorTree>> searchFactors(String keyword);
    
    /**
     * 获取所有场景
     */
    ResultDTO<List<FactorTreeScene>> getAllScenes();
    
    /**
     * 创建场景
     */
    ResultDTO<FactorTreeScene> createScene(String sceneId, String sceneName, String sceneDesc);
    
    /**
     * 构建树形结构数据
     */
    List<Map<String, Object>> buildTreeData(List<FactorTree> nodes, Integer parentId);
    
    /**
     * 懒加载获取子节点
     */
    ResultDTO<List<FactorTree>> getLazyChildNodes(Integer parentId);
    
    /**
     * 递归获取节点及其所有子节点（包含孙子节点）
     */
    ResultDTO<List<FactorTree>> getAllDescendants(Integer nodeId);
    
    /**
     * 获取节点的完整路径
     */
    ResultDTO<List<Map<String, Object>>> getNodePath(Integer nodeId);
    
    /**
     * 分页获取树的根节点（支持懒加载）
     */
    ResultDTO<Map<String, Object>> getTreeWithPagination(Integer treeId, Integer pageSize, Integer cursor);
}