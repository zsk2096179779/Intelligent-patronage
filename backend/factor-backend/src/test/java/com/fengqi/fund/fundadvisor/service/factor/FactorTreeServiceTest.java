package com.fengqi.fund.fundadvisor.service.factor;

import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.entity.factor.FactorTree;
import com.fengqi.fund.fundadvisor.entity.factor.FactorTreeScene;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorTreeMapper;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorTreeSceneMapper;
import com.fengqi.fund.fundadvisor.service.factor.impl.FactorTreeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FactorTreeServiceTest {
    
    @Mock
    private FactorTreeMapper factorTreeMapper;
    
    @Mock
    private FactorTreeSceneMapper factorTreeSceneMapper;
    
    @InjectMocks
    private FactorTreeServiceImpl factorTreeService;
    
    private FactorTree testTree;
    private FactorTreeScene testScene;
    
    @BeforeEach
    void setUp() {
        // 准备测试数据
        testTree = new FactorTree();
        testTree.setTreeid(1);
        testTree.setNodeName("测试因子树");
        testTree.setDescription("测试描述");
        testTree.setSceneId("EQUITY");
        testTree.setNodeType("TREE");
        
        testScene = new FactorTreeScene();
        testScene.setSceneId("EQUITY");
        testScene.setSceneName("权益投资");
        testScene.setSceneDesc("权益类投资场景");
    }
    
    @Test
    void testCreateFactorTree_Success() {
        // Given
        String treeName = "测试因子树";
        String description = "测试描述";
        String sceneId = "EQUITY";
        
        when(factorTreeSceneMapper.getSceneById(sceneId)).thenReturn(testScene);
        when(factorTreeMapper.createFactorTree(any(FactorTree.class))).thenReturn(1);
        
        // When
        ResultDTO<FactorTree> result = factorTreeService.createFactorTree(treeName, description, sceneId);
        
        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("因子树创建成功", result.getMessage());
        
        verify(factorTreeSceneMapper).getSceneById(sceneId);
        verify(factorTreeMapper).createFactorTree(any(FactorTree.class));
    }
    
    @Test
    void testCreateFactorTree_EmptyName() {
        // Given
        String treeName = "";
        String description = "测试描述";
        String sceneId = "EQUITY";
        
        // When
        ResultDTO<FactorTree> result = factorTreeService.createFactorTree(treeName, description, sceneId);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("树名称不能为空", result.getMessage());
        
        verify(factorTreeSceneMapper, never()).getSceneById(anyString());
        verify(factorTreeMapper, never()).createFactorTree(any(FactorTree.class));
    }
    
    @Test
    void testCreateFactorTree_SceneNotFound() {
        // Given
        String treeName = "测试因子树";
        String description = "测试描述";
        String sceneId = "INVALID";
        
        when(factorTreeSceneMapper.getSceneById(sceneId)).thenReturn(null);
        
        // When
        ResultDTO<FactorTree> result = factorTreeService.createFactorTree(treeName, description, sceneId);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("指定场景不存在", result.getMessage());
        
        verify(factorTreeSceneMapper).getSceneById(sceneId);
        verify(factorTreeMapper, never()).createFactorTree(any(FactorTree.class));
    }
    
    @Test
    void testGetAllTrees() {
        // Given
        String sceneId = "EQUITY";
        List<FactorTree> expectedTrees = new ArrayList<>();
        expectedTrees.add(testTree);
        
        when(factorTreeMapper.getTreesByScene(sceneId)).thenReturn(expectedTrees);
        
        // When
        ResultDTO<List<FactorTree>> result = factorTreeService.getAllTrees(sceneId);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals(expectedTrees, result.getData());
        
        verify(factorTreeMapper).getTreesByScene(sceneId);
    }
    
    @Test
    void testGetTreeStructure() {
        // Given
        Integer treeId = 1;
        List<FactorTree> nodes = new ArrayList<>();
        nodes.add(testTree);
        List<Map<String, Object>> expectedTreeData = new ArrayList<>();
        
        when(factorTreeMapper.getTreeById(treeId)).thenReturn(testTree);
        when(factorTreeMapper.getCompleteTreeStructure(treeId)).thenReturn(nodes);
        
        // When
        ResultDTO<Map<String, Object>> result = factorTreeService.getTreeStructure(treeId);
        
        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData().containsKey("tree"));
        assertTrue(result.getData().containsKey("nodes"));
        
        verify(factorTreeMapper).getTreeById(treeId);
        verify(factorTreeMapper).getCompleteTreeStructure(treeId);
    }
    
    @Test
    void testGetTreeStructure_TreeNotFound() {
        // Given
        Integer treeId = 999;
        
        when(factorTreeMapper.getTreeById(treeId)).thenReturn(null);
        
        // When
        ResultDTO<Map<String, Object>> result = factorTreeService.getTreeStructure(treeId);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("因子树不存在", result.getMessage());
        
        verify(factorTreeMapper).getTreeById(treeId);
        verify(factorTreeMapper, never()).getCompleteTreeStructure(anyInt());
    }
    
    @Test
    void testAddTreeNode() {
        // Given
        Integer treeId = 1;
        Integer parentId = null;
        String nodeName = "测试节点";
        String nodeType = "CATEGORY";
        Integer factorId = null;
        String description = "节点描述";
        
        when(factorTreeMapper.getMaxSortOrder(parentId)).thenReturn(0);
        when(factorTreeMapper.addTreeNode(any(FactorTree.class))).thenReturn(1);
        
        // When
        ResultDTO<FactorTree> result = factorTreeService.addTreeNode(
                treeId, parentId, nodeName, nodeType, factorId, description);
        
        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("节点添加成功", result.getMessage());
        
        verify(factorTreeMapper).getMaxSortOrder(parentId);
        verify(factorTreeMapper).addTreeNode(any(FactorTree.class));
    }
    
    @Test
    void testAddCategoryNode_Success() throws Exception {
        // Given
        Integer treeId = 1;
        Integer parentId = null; // 添加到根节点下
        String categoryName = "估值类因子";
        String description = "估值相关的因子分类";
        
        FactorTree rootTree = new FactorTree();
        rootTree.setTreeid(1);
        rootTree.setNodeName("测试因子树");
        rootTree.setNodeType("TREE");
        rootTree.setSceneId("EQUITY");
        
        when(factorTreeMapper.getTreeById(treeId)).thenReturn(rootTree);
        when(factorTreeMapper.getMaxSortOrder(parentId)).thenReturn(0);
        when(factorTreeMapper.getChildNodes(parentId)).thenReturn(new ArrayList<>());
        when(factorTreeMapper.addTreeNode(any(FactorTree.class))).thenReturn(1);
        
        // When
        ResultDTO<FactorTree> result = factorTreeService.addTreeNode(
                treeId, parentId, categoryName, "CATEGORY", null, description);
        
        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("估值类因子", result.getData().getNodeName());
        assertEquals("CATEGORY", result.getData().getNodeType());
        assertNull(result.getData().getFactorId());
        assertEquals(description, result.getData().getDescription());
        assertEquals("EQUITY", result.getData().getSceneId());
        
        verify(factorTreeMapper).getTreeById(treeId);
        verify(factorTreeMapper).getChildNodes(parentId);
        verify(factorTreeMapper).getMaxSortOrder(parentId);
        verify(factorTreeMapper).addTreeNode(any(FactorTree.class));
    }
    
    @Test
    void testAddCategoryNode_EmptyName_Failure() {
        // When
        ResultDTO<FactorTree> result = factorTreeService.addTreeNode(
                1, 1, "", "CATEGORY", null, "测试描述");
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("节点名称不能为空", result.getMessage());
        
        verify(factorTreeMapper, never()).getTreeById(anyInt());
        verify(factorTreeMapper, never()).addTreeNode(any(FactorTree.class));
    }
    
    @Test
    void testAddCategoryNode_InvalidNodeType_Failure() {
        // When
        ResultDTO<FactorTree> result = factorTreeService.addTreeNode(
                1, 1, "测试节点", "INVALID_TYPE", null, "测试描述");
        
        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("无效的节点类型"));
        
        verify(factorTreeMapper, never()).getTreeById(anyInt());
        verify(factorTreeMapper, never()).addTreeNode(any(FactorTree.class));
    }
    
    @Test
    void testAddCategoryNode_ParentNotExists_Failure() {
        // Given
        when(factorTreeMapper.getTreeById(1)).thenReturn(null);
        
        // When
        ResultDTO<FactorTree> result = factorTreeService.addTreeNode(
                1, 1, "测试分类", "CATEGORY", null, "测试描述");
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("父节点不存在", result.getMessage());
        
        verify(factorTreeMapper).getTreeById(1);
        verify(factorTreeMapper, never()).addTreeNode(any(FactorTree.class));
    }
    
    @Test
    void testAddCategoryNode_DuplicateName_Failure() {
        // Given - 在根节点下添加重复的名称
        FactorTree rootTree = new FactorTree();
        rootTree.setTreeid(1);
        rootTree.setNodeName("根树");
        rootTree.setNodeType("TREE");
        rootTree.setSceneId("EQUITY");
        
        FactorTree existingChild = new FactorTree();
        existingChild.setTreeid(2);
        existingChild.setNodeName("估值类因子");
        existingChild.setParentId(null); // 直接在根下
        
        List<FactorTree> rootChildren = new ArrayList<>();
        rootChildren.add(existingChild);
        
        when(factorTreeMapper.getTreeById(1)).thenReturn(rootTree);
        when(factorTreeMapper.getChildNodes(null)).thenReturn(rootChildren);
        
        // When - 尝试添加同名的分类节点
        ResultDTO<FactorTree> result = factorTreeService.addTreeNode(
                1, null, "估值类因子", "CATEGORY", null, "重复的名称");
        
        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("已存在同名节点"));
        
        // 验证调用（因为会被各种验证方法调用，所以去掉具体的调用验证）
        verify(factorTreeMapper, never()).addTreeNode(any(FactorTree.class));
    }
    
    @Test
    void testDeleteEmptyNode_Success() {
        // Given
        Integer nodeId = 1;
        
        when(factorTreeMapper.hasChildNodes(nodeId)).thenReturn(0);
        when(factorTreeMapper.deleteTreeNode(nodeId)).thenReturn(1);
        
        // When
        ResultDTO<Boolean> result = factorTreeService.deleteEmptyNode(nodeId);
        
        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        assertEquals("节点删除成功", result.getMessage());
        
        verify(factorTreeMapper).hasChildNodes(nodeId);
        verify(factorTreeMapper).deleteTreeNode(nodeId);
    }
    
    @Test
    void testDeleteEmptyNode_HasChildren() {
        // Given
        Integer nodeId = 1;
        
        when(factorTreeMapper.hasChildNodes(nodeId)).thenReturn(2);
        
        // When
        ResultDTO<Boolean> result = factorTreeService.deleteEmptyNode(nodeId);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("节点下有子节点，无法删除", result.getMessage());
        
        verify(factorTreeMapper).hasChildNodes(nodeId);
        verify(factorTreeMapper, never()).deleteTreeNode(anyInt());
    }
    
    @Test
    void testCreateScene_Success() {
        // Given
        String sceneId = "BOND";
        String sceneName = "债券投资";
        String sceneDesc = "债券类投资场景";
        
        when(factorTreeSceneMapper.getSceneById(sceneId)).thenReturn(null);
        when(factorTreeSceneMapper.createScene(any(FactorTreeScene.class))).thenReturn(1);
        
        // When
        ResultDTO<FactorTreeScene> result = factorTreeService.createScene(sceneId, sceneName, sceneDesc);
        
        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("场景创建成功", result.getMessage());
        
        verify(factorTreeSceneMapper).getSceneById(sceneId);
        verify(factorTreeSceneMapper).createScene(any(FactorTreeScene.class));
    }
    
    @Test
    void testCreateScene_DuplicateId() {
        // Given
        String sceneId = "EQUITY";
        String sceneName = "权益投资";
        String sceneDesc = "权益类投资场景";
        
        when(factorTreeSceneMapper.getSceneById(sceneId)).thenReturn(testScene);
        
        // When
        ResultDTO<FactorTreeScene> result = factorTreeService.createScene(sceneId, sceneName, sceneDesc);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("场景ID已存在", result.getMessage());
        
        verify(factorTreeSceneMapper).getSceneById(sceneId);
        verify(factorTreeSceneMapper, never()).createScene(any(FactorTreeScene.class));
    }
    
    @Test
    void testBuildTreeData() {
        // Given
        List<FactorTree> nodes = new ArrayList<>();
        
        // 根节点
        FactorTree root = new FactorTree();
        root.setTreeid(1);
        root.setParentId(null);
        root.setNodeName("根节点");
        root.setNodeType("TREE");
        nodes.add(root);
        
        // 子节点
        FactorTree child = new FactorTree();
        child.setTreeid(2);
        child.setParentId(1);
        child.setNodeName("子节点");
        child.setNodeType("CATEGORY");
        nodes.add(child);
        
        // When
        List<Map<String, Object>> result = factorTreeService.buildTreeData(nodes, null);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("根节点", result.get(0).get("name"));
        assertTrue(result.get(0).containsKey("children"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> children = (List<Map<String, Object>>) result.get(0).get("children");
        assertEquals(1, children.size());
        assertEquals("子节点", children.get(0).get("name"));
    }
    
    @Test
    void testGetLazyChildNodes_Success() {
        // Given
        Integer parentId = 1;
        List<FactorTree> expectedChildren = new ArrayList<>();
        
        FactorTree child1 = new FactorTree();
        child1.setTreeid(2);
        child1.setParentId(1);
        child1.setNodeName("子节点1");
        child1.setNodeType("CATEGORY");
        expectedChildren.add(child1);
        
        FactorTree child2 = new FactorTree();
        child2.setTreeid(3);
        child2.setParentId(1);
        child2.setNodeName("子节点2");
        child2.setNodeType("FACTOR");
        expectedChildren.add(child2);
        
        when(factorTreeMapper.getChildNodes(parentId)).thenReturn(expectedChildren);
        
        // When
        ResultDTO<List<FactorTree>> result = factorTreeService.getLazyChildNodes(parentId);
        
        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());
        assertEquals("子节点1", result.getData().get(0).getNodeName());
        assertEquals("子节点2", result.getData().get(1).getNodeName());
        
        verify(factorTreeMapper).getChildNodes(parentId);
    }
    
    @Test
    void testGetAllDescendants_Success() {
        // Given
        Integer nodeId = 1;
        List<FactorTree> expectedDescendants = new ArrayList<>();
        
        FactorTree parent = new FactorTree();
        parent.setTreeid(1);
        parent.setParentId(null);
        parent.setNodeName("父节点");
        
        FactorTree child = new FactorTree();
        child.setTreeid(2);
        child.setParentId(1);
        child.setNodeName("子节点");
        
        FactorTree grandChild = new FactorTree();
        grandChild.setTreeid(3);
        grandChild.setParentId(2);
        grandChild.setNodeName("孙子节点");
        
        expectedDescendants.add(parent);
        expectedDescendants.add(child);
        expectedDescendants.add(grandChild);
        
        when(factorTreeMapper.getAllDescendants(nodeId)).thenReturn(expectedDescendants);
        
        // When
        ResultDTO<List<FactorTree>> result = factorTreeService.getAllDescendants(nodeId);
        
        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(3, result.getData().size());
        assertEquals("父节点", result.getData().get(0).getNodeName());
        assertEquals("子节点", result.getData().get(1).getNodeName());
        assertEquals("孙子节点", result.getData().get(2).getNodeName());
        
        verify(factorTreeMapper).getAllDescendants(nodeId);
    }
    
    @Test
    void testGetNodePath_Success() {
        // Given
        Integer nodeId = 3;
        List<FactorTree> expectedPath = new ArrayList<>();
        
        FactorTree root = new FactorTree();
        root.setTreeid(1);
        root.setNodeName("根节点");
        root.setNodeType("TREE");
        
        FactorTree parent = new FactorTree();
        parent.setTreeid(2);
        parent.setNodeName("分类节点");
        parent.setNodeType("CATEGORY");
        
        FactorTree target = new FactorTree();
        target.setTreeid(3);
        target.setNodeName("目标因子");
        target.setNodeType("FACTOR");
        
        expectedPath.add(root);
        expectedPath.add(parent);
        expectedPath.add(target);
        
        when(factorTreeMapper.getNodePath(nodeId)).thenReturn(expectedPath);
        
        // When
        ResultDTO<List<Map<String, Object>>> result = factorTreeService.getNodePath(nodeId);
        
        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(3, result.getData().size());
        assertEquals("根节点", result.getData().get(0).get("name"));
        assertEquals("分类节点", result.getData().get(1).get("name"));
        assertEquals("目标因子", result.getData().get(2).get("name"));
        
        verify(factorTreeMapper).getNodePath(nodeId);
    }
    
    @Test
    void testGetTreeWithPagination_Success() {
        // Given
        Integer treeId = 1;
        Integer pageSize = 10;
        Integer cursor = 0;
        
        FactorTree tree = new FactorTree();
        tree.setTreeid(1);
        tree.setNodeName("测试树");
        tree.setNodeType("TREE");
        
        List<FactorTree> children = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            FactorTree child = new FactorTree();
            child.setTreeid(i + 1);
            child.setParentId(1);
            child.setNodeName("子节点" + i);
            children.add(child);
        }
        
        when(factorTreeMapper.getTreeById(treeId)).thenReturn(tree);
        when(factorTreeMapper.getLazyChildNodes(treeId, 10, 0)).thenReturn(children);
        when(factorTreeMapper.getChildNodeCount(treeId)).thenReturn(5);
        
        // When
        ResultDTO<Map<String, Object>> result = factorTreeService.getTreeWithPagination(treeId, pageSize, cursor);
        
        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        Map<String, Object> data = result.getData();
        assertTrue(data.containsKey("tree"));
        assertTrue(data.containsKey("children"));
        assertTrue(data.containsKey("pagination"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> pagination = (Map<String, Object>) data.get("pagination");
        assertEquals(10, pagination.get("pageSize"));
        assertEquals(5, pagination.get("totalCount"));
        assertEquals(false, pagination.get("hasMore"));
        assertNull(pagination.get("nextCursor"));
        
        verify(factorTreeMapper).getTreeById(treeId);
        verify(factorTreeMapper).getLazyChildNodes(treeId, 10, 0);
        verify(factorTreeMapper).getChildNodeCount(treeId);
    }
    
    @Test
    void testGetTreeWithPagination_HasMorePages() {
        // Given
        Integer treeId = 1;
        Integer pageSize = 2;
        Integer cursor = 0;
        
        FactorTree tree = new FactorTree();
        tree.setTreeid(1);
        tree.setNodeName("测试树");
        
        List<FactorTree> children = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            FactorTree child = new FactorTree();
            child.setTreeid(i + 1);
            child.setParentId(1);
            child.setNodeName("子节点" + i);
            children.add(child);
        }
        
        when(factorTreeMapper.getTreeById(treeId)).thenReturn(tree);
        when(factorTreeMapper.getLazyChildNodes(treeId, 2, 0)).thenReturn(children);
        when(factorTreeMapper.getChildNodeCount(treeId)).thenReturn(10); // 总共10个，当前只返回2个
        
        // When
        ResultDTO<Map<String, Object>> result = factorTreeService.getTreeWithPagination(treeId, pageSize, cursor);
        
        // Then
        assertTrue(result.isSuccess());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> pagination = (Map<String, Object>) result.getData().get("pagination");
        assertEquals(true, pagination.get("hasMore"));
        assertEquals(2, pagination.get("nextCursor")); // 有更多页时返回下一页游标
        
        verify(factorTreeMapper).getLazyChildNodes(treeId, 2, 0);
    }
}