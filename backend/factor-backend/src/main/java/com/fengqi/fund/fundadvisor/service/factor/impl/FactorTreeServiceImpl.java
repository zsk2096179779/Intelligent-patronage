package com.fengqi.fund.fundadvisor.service.factor.impl;

import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.entity.factor.FactorTree;
import com.fengqi.fund.fundadvisor.entity.factor.FactorTreeScene;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorTreeMapper;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorTreeSceneMapper;
import com.fengqi.fund.fundadvisor.service.factor.FactorTreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class FactorTreeServiceImpl implements FactorTreeService {
    
    @Autowired
    private FactorTreeMapper factorTreeMapper;
    
    @Autowired
    private FactorTreeSceneMapper factorTreeSceneMapper;
    
    @Override
    @Transactional
    public ResultDTO<FactorTree> createFactorTree(String treeName, String description, String sceneId) {
        try {
            // 验证参数
            if (treeName == null || treeName.trim().isEmpty()) {
                return ResultDTO.error("树名称不能为空");
            }
            
            // 检查场景是否存在（如果指定了场景ID）
            if (sceneId != null && !sceneId.trim().isEmpty()) {
                FactorTreeScene scene = factorTreeSceneMapper.getSceneById(sceneId);
                if (scene == null) {
                    return ResultDTO.error("指定场景不存在");
                }
            }
            
            // 创建因子树根节点
            FactorTree factorTree = new FactorTree();
            factorTree.setNodeName(treeName);
            factorTree.setDescription(description);
            factorTree.setSceneId(sceneId);
            
            int result = factorTreeMapper.createFactorTree(factorTree);
            if (result > 0) {
                log.info("成功创建因子树: {}", treeName);
                return ResultDTO.success(factorTree, "因子树创建成功");
            } else {
                return ResultDTO.error("因子树创建失败");
            }
            
        } catch (Exception e) {
            log.error("创建因子树失败", e);
            return ResultDTO.error("系统异常，创建失败");
        }
    }
    
    @Override
    public ResultDTO<List<FactorTree>> getAllTrees(String sceneId) {
        try {
            List<FactorTree> trees = factorTreeMapper.getTreesByScene(sceneId);
            return ResultDTO.success(trees);
        } catch (Exception e) {
            log.error("获取因子树列表失败", e);
            return ResultDTO.error("获取因子树列表失败");
        }
    }
    
    @Override
    public ResultDTO<Map<String, Object>> getTreeStructure(Integer treeId) {
        try {
            FactorTree tree = factorTreeMapper.getTreeById(treeId);
            if (tree == null) {
                return ResultDTO.error("因子树不存在");
            }
            
            List<FactorTree> allNodes = factorTreeMapper.getCompleteTreeStructure(treeId);
            List<Map<String, Object>> treeData = buildTreeData(allNodes, null);
            
            Map<String, Object> result = new HashMap<>();
            result.put("tree", tree);
            result.put("nodes", treeData);
            
            return ResultDTO.success(result);
        } catch (Exception e) {
            log.error("获取树结构失败", e);
            return ResultDTO.error("获取树结构失败");
        }
    }
    
    @Override
    @Transactional
    public ResultDTO<FactorTree> addTreeNode(Integer treeId, Integer parentId, String nodeName, 
                                             String nodeType, Integer factorId, String description) {
        try {
            // 验证参数
            if (nodeName == null || nodeName.trim().isEmpty()) {
                return ResultDTO.error("节点名称不能为空");
            }
            
            if (nodeType == null || nodeType.trim().isEmpty()) {
                return ResultDTO.error("节点类型不能为空");
            }
            
            // 验证节点类型
            if (!Arrays.asList("TREE", "CATEGORY", "FACTOR").contains(nodeType)) {
                return ResultDTO.error("无效的节点类型，必须是TREE、CATEGORY或FACTOR");
            }
            
            // 验证树是否存在（如果指定了treeId）并获取树信息
            FactorTree tree = null;
            if (treeId != null) {
                tree = factorTreeMapper.getTreeById(treeId);
                if (tree == null) {
                    return ResultDTO.error("指定的因子树不存在，treeId: " + treeId);
                }
            }
            
            // 验证父子关系
            ResultDTO<Boolean> parentValidation = validateParentChildRelation(treeId, parentId, nodeType);
            if (!parentValidation.isSuccess()) {
                return ResultDTO.error(parentValidation.getMessage());
            }
            
            // 检查同层级下是否已存在同名节点
            ResultDTO<Boolean> duplicateCheck = checkDuplicateNode(parentId, nodeName);
            if (!duplicateCheck.isSuccess()) {
                return ResultDTO.error(duplicateCheck.getMessage());
            }
            
            // 获取最大排序号
            Integer maxSortOrder = factorTreeMapper.getMaxSortOrder(parentId);
            Integer sortOrder = maxSortOrder != null ? maxSortOrder + 1 : 1;
            
            // 确定节点场景
            String sceneId = null;
            if (tree != null) {
                sceneId = tree.getSceneId();
            }
            
            FactorTree node = new FactorTree();
            node.setParentId(parentId);
            node.setNodeName(nodeName);
            node.setNodeType(nodeType);
            node.setFactorId(factorId);
            node.setDescription(description);
            node.setSortOrder(sortOrder);
            node.setSceneId(sceneId);
            node.setIsLeaf(factorId != null); // 如果关联了因子，则为叶子节点
            
            int result = factorTreeMapper.addTreeNode(node);
            if (result > 0) {
                // 树结构一致性检查和更新
                updateTreeConsistency(parentId);
                
                log.info("成功添加节点: {} 到树: {}", nodeName, treeId);
                return ResultDTO.success(node, "节点添加成功");
            } else {
                return ResultDTO.error("节点添加失败");
            }
            
        } catch (Exception e) {
            log.error("添加节点失败", e);
            return ResultDTO.error("系统异常，添加失败");
        }
    }
    
    @Override
    @Transactional
    public ResultDTO<FactorTree> updateTreeNode(Integer nodeId, String nodeName, String description, Integer sortOrder) {
        try {
            FactorTree node = new FactorTree();
            node.setTreeid(nodeId);
            node.setNodeName(nodeName);
            node.setDescription(description);
            node.setSortOrder(sortOrder);
            
            int result = factorTreeMapper.updateTreeNode(node);
            if (result > 0) {
                log.info("成功更新节点: {}", nodeId);
                return ResultDTO.success(node, "节点更新成功");
            } else {
                return ResultDTO.error("节点更新失败");
            }
            
        } catch (Exception e) {
            log.error("更新节点失败", e);
            return ResultDTO.error("系统异常，更新失败");
        }
    }
    
    @Override
    @Transactional
    public ResultDTO<Boolean> moveTreeNode(Integer nodeId, Integer newParentId, Integer sortOrder) {
        try {
            // 验证节点是否存在
            FactorTree node = factorTreeMapper.getNodeById(nodeId);
            if (node == null) {
                return ResultDTO.error("要移动的节点不存在");
            }
            
            // 验证新父节点是否存在
            if (newParentId != null) {
                FactorTree newParent = factorTreeMapper.getNodeById(newParentId);
                if (newParent == null) {
                    return ResultDTO.error("新的父节点不存在");
                }
            }
            
            // 检查是否会形成循环引用（移动节点时需要检查）
            if (newParentId != null && wouldCreateCycle(newParentId, nodeId)) {
                return ResultDTO.error("不能将节点移动到自己的子节点下，会形成循环引用");
            }
            
            int result = factorTreeMapper.moveNode(nodeId, newParentId, sortOrder);
            if (result > 0) {
                log.info("成功移动节点: {} 到父节点: {}", nodeId, newParentId);
                return ResultDTO.success(true, "节点移动成功");
            } else {
                return ResultDTO.error("节点移动失败");
            }
            
        } catch (Exception e) {
            log.error("移动节点失败", e);
            return ResultDTO.error("系统异常，移动失败");
        }
    }
    
    @Override
    @Transactional
    public ResultDTO<Boolean> deleteEmptyNode(Integer nodeId) {
        try {
            // 检查是否有子节点
            int childCount = factorTreeMapper.hasChildNodes(nodeId);
            if (childCount > 0) {
                return ResultDTO.error("节点下有子节点，无法删除");
            }
            
            int result = factorTreeMapper.deleteTreeNode(nodeId);
            if (result > 0) {
                log.info("成功删除空节点: {}", nodeId);
                return ResultDTO.success(true, "节点删除成功");
            } else {
                return ResultDTO.error("节点删除失败");
            }
            
        } catch (Exception e) {
            log.error("删除节点失败", e);
            return ResultDTO.error("系统异常，删除失败");
        }
    }
    
    @Override
    public ResultDTO<List<FactorTree>> searchTrees(String keyword) {
        try {
            List<FactorTree> trees = factorTreeMapper.searchTrees(keyword);
            return ResultDTO.success(trees);
        } catch (Exception e) {
            log.error("搜索因子树失败", e);
            return ResultDTO.error("搜索失败");
        }
    }
    
    @Override
    public ResultDTO<List<FactorTree>> searchFactors(String keyword) {
        try {
            List<FactorTree> factors = factorTreeMapper.searchFactors(keyword);
            return ResultDTO.success(factors);
        } catch (Exception e) {
            log.error("搜索因子失败", e);
            return ResultDTO.error("搜索失败");
        }
    }
    
    @Override
    public ResultDTO<List<FactorTreeScene>> getAllScenes() {
        try {
            List<FactorTreeScene> scenes = factorTreeSceneMapper.getAllScenes();
            return ResultDTO.success(scenes);
        } catch (Exception e) {
            log.error("获取场景列表失败", e);
            return ResultDTO.error("获取场景列表失败");
        }
    }
    
    @Override
    @Transactional
    public ResultDTO<FactorTreeScene> createScene(String sceneId, String sceneName, String sceneDesc) {
        try {
            // 检查场景ID是否已存在
            FactorTreeScene existingScene = factorTreeSceneMapper.getSceneById(sceneId);
            if (existingScene != null) {
                return ResultDTO.error("场景ID已存在");
            }
            
            FactorTreeScene scene = new FactorTreeScene();
            scene.setSceneId(sceneId);
            scene.setSceneName(sceneName);
            scene.setSceneDesc(sceneDesc);
            
            int result = factorTreeSceneMapper.createScene(scene);
            if (result > 0) {
                log.info("成功创建场景: {}", sceneName);
                return ResultDTO.success(scene, "场景创建成功");
            } else {
                return ResultDTO.error("场景创建失败");
            }
            
        } catch (Exception e) {
            log.error("创建场景失败", e);
            return ResultDTO.error("系统异常，创建失败");
        }
    }
    
    @Override
    public List<Map<String, Object>> buildTreeData(List<FactorTree> nodes, Integer parentId) {
        List<Map<String, Object>> treeData = new ArrayList<>();
        
        for (FactorTree node : nodes) {
            if (Objects.equals(node.getParentId(), parentId)) {
                Map<String, Object> nodeData = new HashMap<>();
                nodeData.put("id", node.getTreeid());
                nodeData.put("name", node.getNodeName());
                nodeData.put("type", node.getNodeType());
                nodeData.put("factorId", node.getFactorId());
                nodeData.put("isLeaf", node.getIsLeaf());
                nodeData.put("description", node.getDescription());
                nodeData.put("sortOrder", node.getSortOrder());
                nodeData.put("sceneId", node.getSceneId());
                
                // 递归构建子节点
                List<Map<String, Object>> children = buildTreeData(nodes, node.getTreeid());
                if (!children.isEmpty()) {
                    nodeData.put("children", children);
                }
                
                treeData.add(nodeData);
            }
        }
        
        return treeData;
    }
    
    @Override
    public ResultDTO<List<FactorTree>> getLazyChildNodes(Integer parentId) {
        try {
            List<FactorTree> children = factorTreeMapper.getChildNodes(parentId);
            return ResultDTO.success(children);
        } catch (Exception e) {
            log.error("获取懒加载子节点失败", e);
            return ResultDTO.error("获取子节点失败");
        }
    }
    
    @Override
    public ResultDTO<List<FactorTree>> getAllDescendants(Integer nodeId) {
        try {
            List<FactorTree> descendants = factorTreeMapper.getAllDescendants(nodeId);
            return ResultDTO.success(descendants);
        } catch (Exception e) {
            log.error("获取所有后代节点失败", e);
            return ResultDTO.error("获取后代节点失败");
        }
    }
    
    @Override
    public ResultDTO<List<Map<String, Object>>> getNodePath(Integer nodeId) {
        try {
            List<FactorTree> pathNodes = factorTreeMapper.getNodePath(nodeId);
            List<Map<String, Object>> path = new ArrayList<>();
            
            for (FactorTree node : pathNodes) {
                Map<String, Object> nodeData = new HashMap<>();
                nodeData.put("id", node.getTreeid());
                nodeData.put("name", node.getNodeName());
                nodeData.put("type", node.getNodeType());
                nodeData.put("factorId", node.getFactorId());
                nodeData.put("isLeaf", node.getIsLeaf());
                nodeData.put("description", node.getDescription());
                nodeData.put("sortOrder", node.getSortOrder());
                nodeData.put("sceneId", node.getSceneId());
                path.add(nodeData);
            }
            
            return ResultDTO.success(path);
        } catch (Exception e) {
            log.error("获取节点路径失败", e);
            return ResultDTO.error("获取节点路径失败");
        }
    }
    
    @Override
    public ResultDTO<Map<String, Object>> getTreeWithPagination(Integer treeId, Integer pageSize, Integer cursor) {
        try {
            FactorTree tree = factorTreeMapper.getTreeById(treeId);
            if (tree == null) {
                return ResultDTO.error("因子树不存在");
            }
            
            // 获取根节点的直接子节点（分页）
            Integer offset = cursor != null ? cursor : 0;
            Integer actualPageSize = pageSize != null ? pageSize : 20;
            List<FactorTree> rootChildren = factorTreeMapper.getLazyChildNodes(treeId, actualPageSize, offset);
            
            // 获取总数
            Integer totalCount = factorTreeMapper.getChildNodeCount(treeId);
            Integer nextCursor = offset + rootChildren.size() < totalCount ? offset + rootChildren.size() : null;
            
            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("tree", tree);
            result.put("children", buildTreeData(rootChildren, treeId));
            result.put("pagination", Map.of(
                "pageSize", actualPageSize,
                "totalCount", totalCount,
                "hasMore", offset + rootChildren.size() < totalCount,
                "nextCursor", nextCursor
            ));
            
            return ResultDTO.success(result);
        } catch (Exception e) {
            log.error("获取分页树结构失败", e);
            return ResultDTO.error("获取树结构失败");
        }
    }
    
    /**
     * 验证父子关系
     */
    private ResultDTO<Boolean> validateParentChildRelation(Integer treeId, Integer parentId, String nodeType) {
        try {
            // 如果提供了treeId，验证树节点是否为TREE类型
            if (treeId != null) {
                FactorTree tree = factorTreeMapper.getTreeById(treeId);
                if (tree != null && !"TREE".equals(tree.getNodeType())) {
                    return ResultDTO.error("指定的treeId必须是一个有效的因子树节点");
                }
            }
            
            // 如果是添加到根节点下（parentId为null，表示添加到树的根节点下）
            if (parentId == null) {
                // 不能将TREE节点添加到根节点下（因为树根节点本身就是TREE类型）
                if ("TREE".equals(nodeType)) {
                    return ResultDTO.error("树节点只能添加到顶级");
                }
                // 不返回，继续执行其他验证（比如场景检查）
            } else {
                // 检查父节点是否存在
                FactorTree parentNode = factorTreeMapper.getNodeById(parentId);
                if (parentNode == null) {
                    return ResultDTO.error("父节点不存在");
                }
                
                // 不能将树节点添加到非根节点下
                if ("TREE".equals(nodeType) && !"TREE".equals(parentNode.getNodeType())) {
                    return ResultDTO.error("树节点只能添加到顶级");
                }
                
                // 因子节点必须有父节点
                if ("FACTOR".equals(nodeType) && parentId == null) {
                    return ResultDTO.error("因子节点必须有父节点");
                }
            }
            
            // 添加新节点时不需要检查循环引用，因为新节点还没有treeid
            // 循环引用检查只在移动现有节点时才需要
            // 这里我们暂时移除循环引用检查，因为当前是添加操作
            
            // 检查层级深度（防止层级过深）
            int depth = getNodeDepth(parentId);
            if (depth >= 10) {
                return ResultDTO.error("树结构层级过深，最多支持10层");
            }
            
            return ResultDTO.success(true);
            
        } catch (Exception e) {
            log.error("验证父子关系失败", e);
            return ResultDTO.error("验证父子关系失败");
        }
    }
    
    /**
     * 检查是否会形成循环引用
     */
    private boolean wouldCreateCycle(Integer parentId, Integer excludeNodeId) {
        if (excludeNodeId == null) {
            return false;
        }
        
        // 递归检查父节点的所有祖先节点
        Integer currentId = parentId;
        while (currentId != null) {
            if (currentId.equals(excludeNodeId)) {
                return true;
            }
            FactorTree current = factorTreeMapper.getNodeById(currentId);
            if (current == null) {
                break;
            }
            currentId = current.getParentId();
        }
        return false;
    }
    
    /**
     * 获取节点深度
     */
    private int getNodeDepth(Integer nodeId) {
        int depth = 0;
        Integer currentId = nodeId;
        
        while (currentId != null) {
            FactorTree node = factorTreeMapper.getNodeById(currentId);
            if (node == null) {
                break;
            }
            currentId = node.getParentId();
            depth++;
        }
        
        return depth;
    }
    
    /**
     * 检查同层级下是否已存在同名节点
     */
    private ResultDTO<Boolean> checkDuplicateNode(Integer parentId, String nodeName) {
        try {
            List<FactorTree> siblings = factorTreeMapper.getChildNodes(parentId);
            for (FactorTree sibling : siblings) {
                if (nodeName.trim().equals(sibling.getNodeName().trim())) {
                    return ResultDTO.error("同层级下已存在同名节点: " + nodeName);
                }
            }
            return ResultDTO.success(true);
        } catch (Exception e) {
            log.error("检查重复节点失败", e);
            return ResultDTO.error("检查重复节点失败");
        }
    }
    
    /**
     * 更新树结构一致性
     */
    private void updateTreeConsistency(Integer parentId) {
        try {
            if (parentId != null) {
                // 更新父节点的叶子状态
                List<FactorTree> children = factorTreeMapper.getChildNodes(parentId);
                FactorTree parent = factorTreeMapper.getNodeById(parentId);
                
                if (parent != null) {
                    // 如果父节点有子节点，则不是叶子节点
                    boolean hasChildren = !children.isEmpty();
                    boolean hasFactorChildren = children.stream()
                        .anyMatch(child -> "FACTOR".equals(child.getNodeType()));
                    
                    // 更新父节点的叶子状态
                    if (hasFactorChildren) {
                        parent.setIsLeaf(true);
                    } else if (hasChildren) {
                        parent.setIsLeaf(false);
                    }
                    
                    factorTreeMapper.updateTreeNode(parent);
                    
                    // 递归更新上级节点
                    updateTreeConsistency(parent.getParentId());
                }
            }
        } catch (Exception e) {
            log.error("更新树结构一致性失败", e);
        }
    }
}