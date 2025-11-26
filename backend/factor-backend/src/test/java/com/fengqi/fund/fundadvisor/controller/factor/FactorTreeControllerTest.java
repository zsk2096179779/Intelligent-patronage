package com.fengqi.fund.fundadvisor.controller.factor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.dto.factor.CreateTreeRequest;
import com.fengqi.fund.fundadvisor.entity.factor.FactorTree;
import com.fengqi.fund.fundadvisor.service.factor.FactorTreeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FactorTreeController.class)
class FactorTreeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private FactorTreeService factorTreeService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private FactorTree testTree;
    
    @BeforeEach
    void setUp() {
        testTree = new FactorTree();
        testTree.setTreeid(1);
        testTree.setNodeName("测试因子树");
        testTree.setDescription("测试描述");
        testTree.setNodeType("TREE");
    }
    
    @Test
    void testCreateTree_Success() throws Exception {
        // Given - setUp()已经在@BeforeEach中自动调用，不需要手动调用
        CreateTreeRequest request = new CreateTreeRequest();
        request.setTreeName("测试因子树");
        request.setDescription("测试描述");
        request.setSceneId("EQUITY");
        
        ResultDTO<FactorTree> expectedResult = ResultDTO.success(testTree, "因子树创建成功");
        when(factorTreeService.createFactorTree(anyString(), anyString(), anyString()))
                .thenReturn(expectedResult);
        
        // When & Then - 使用正确的路径 /factor/factor-trees
        mockMvc.perform(post("/factor/factor-trees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("因子树创建成功"))
                .andExpect(jsonPath("$.data.treeid").value(1))
                .andExpect(jsonPath("$.data.nodeName").value("测试因子树"));
    }
    
    @Test
    void testCreateTree_ValidationError() throws Exception {
        // Given
        CreateTreeRequest request = new CreateTreeRequest();
        request.setTreeName(""); // 空名称
        
        ResultDTO<FactorTree> expectedResult = ResultDTO.error("树名称不能为空");
        when(factorTreeService.createFactorTree(anyString(), anyString(), anyString()))
                .thenReturn(expectedResult);
        
        // When & Then - 使用正确的路径 /factor/factor-trees
        mockMvc.perform(post("/factor/factor-trees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("树名称不能为空"));
    }
    
    @Test
    void testGetAllTrees_Success() throws Exception {
        // Given
        List<FactorTree> trees = new ArrayList<>();
        trees.add(testTree);
        
        ResultDTO<List<FactorTree>> expectedResult = ResultDTO.success(trees);
        when(factorTreeService.getAllTrees(anyString())).thenReturn(expectedResult);
        
        // When & Then - 使用正确的路径 /factor/factor-trees
        mockMvc.perform(get("/factor/factor-trees")
                .param("sceneId", "EQUITY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].treeid").value(1))
                .andExpect(jsonPath("$.data[0].nodeName").value("测试因子树"));
    }
    
    @Test
    void testGetTreeStructure_Success() throws Exception {
        // Given
        ResultDTO expectedResult = ResultDTO.success(testTree);
        when(factorTreeService.getTreeStructure(anyInt())).thenReturn(expectedResult);
        
        // When & Then - 使用正确的路径 /factor/factor-trees/{treeId}/structure
        mockMvc.perform(get("/factor/factor-trees/1/structure"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.treeid").value(1));
    }
    
    @Test
    void testSearchTrees_Success() throws Exception {
        // Given
        List<FactorTree> trees = new ArrayList<>();
        trees.add(testTree);
        
        ResultDTO<List<FactorTree>> expectedResult = ResultDTO.success(trees);
        when(factorTreeService.searchTrees(anyString())).thenReturn(expectedResult);
        
        // When & Then - 根据实际控制器路径调整，应该使用/search/trees
        mockMvc.perform(get("/factor/factor-trees/search/trees")
                .param("keyword", "测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].treeid").value(1));
    }
    
    @Test
    void testSearchFactors_Success() throws Exception {
        // Given
        List<FactorTree> factors = new ArrayList<>();
        factors.add(testTree);
        
        ResultDTO<List<FactorTree>> expectedResult = ResultDTO.success(factors);
        when(factorTreeService.searchFactors(anyString())).thenReturn(expectedResult);
        
        // When & Then - 根据实际控制器路径调整
        mockMvc.perform(get("/factor/factor-trees/search/factors")
                .param("keyword", "测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].treeid").value(1));
    }
    
    @Test
    void testGetAllScenes_Success() throws Exception {
        // Given
        ResultDTO expectedResult = ResultDTO.success(new ArrayList<>());
        when(factorTreeService.getAllScenes()).thenReturn(expectedResult);
        
        // When & Then - 根据实际控制器路径调整
        mockMvc.perform(get("/factor/factor-trees/scenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    void testCreateScene_Success() throws Exception {
        // Given
        ResultDTO expectedResult = ResultDTO.success(null, "场景创建成功");
        when(factorTreeService.createScene(anyString(), anyString(), anyString()))
                .thenReturn(expectedResult);
        
        // When & Then - 根据实际控制器路径调整
        mockMvc.perform(post("/factor/factor-trees/scenes")
                .param("sceneId", "TEST")
                .param("sceneName", "测试场景")
                .param("sceneDesc", "测试描述"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("场景创建成功"));
    }
}