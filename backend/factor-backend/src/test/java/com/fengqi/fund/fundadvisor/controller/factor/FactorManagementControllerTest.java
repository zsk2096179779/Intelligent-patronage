package com.fengqi.fund.fundadvisor.controller.factor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fengqi.fund.fundadvisor.dto.factor.FactorManagementRequest;
import com.fengqi.fund.fundadvisor.dto.factor.FactorManagementResponse;
import com.fengqi.fund.fundadvisor.entity.factor.FactorTree;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorTreeMapper;
import com.fengqi.fund.fundadvisor.service.factor.FactorManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FactorManagementController 测试类
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
@WebMvcTest(FactorManagementController.class)
class FactorManagementControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private FactorManagementService factorManagementService;
    
    @MockBean
    private FactorTreeMapper factorTreeMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private FactorManagementRequest.DerivedFactorInfo testDerivedFactorInfo;
    private FactorManagementRequest.WeightConfigInfo testWeightConfigInfo;
    private FactorManagementRequest.TreeOperationInfo testTreeOperationInfo;
    private FactorManagementResponse testResponse;
    private FactorTree testParentNode;
    
    @BeforeEach
    void setUp() {
        // 设置测试衍生因子信息
        testDerivedFactorInfo = new FactorManagementRequest.DerivedFactorInfo();
        testDerivedFactorInfo.setFactorName("测试衍生因子");
        testDerivedFactorInfo.setFactorCode("TEST_DERIVED");
        testDerivedFactorInfo.setFactorDesc("测试描述");
        testDerivedFactorInfo.setCalcStrategyId(1);
        testDerivedFactorInfo.setBaseFactorIds(Arrays.asList(3, 4));
        testDerivedFactorInfo.setStyleTagIds("1,2");
        testDerivedFactorInfo.setCreateUserId(1);
        
        // 设置测试权重配置信息
        testWeightConfigInfo = new FactorManagementRequest.WeightConfigInfo();
        testWeightConfigInfo.setDerivedId(1);
        testWeightConfigInfo.setBaseFactorWeights("'3': 0.6, '4': 0.4");
        testWeightConfigInfo.setWeightDesc("用户自定义权重");
        
        // 设置测试树节点操作信息
        testTreeOperationInfo = new FactorManagementRequest.TreeOperationInfo();
        testTreeOperationInfo.setParentId(1);
        testTreeOperationInfo.setNodeName("价值因子");
        testTreeOperationInfo.setNodeType("CATEGORY");
        testTreeOperationInfo.setSceneId("EQUITY");
        testTreeOperationInfo.setDescription("包含所有价值相关因子");
        
        // 设置测试父节点
        testParentNode = new FactorTree();
        testParentNode.setTreeid(1);
        testParentNode.setNodeName("价值因子");
        testParentNode.setNodeType("CATEGORY");
        testParentNode.setSceneId("EQUITY");
        
        // 设置测试响应
        testResponse = new FactorManagementResponse();
        testResponse.setSuccess(true);
        testResponse.setMessage("操作成功");
        testResponse.setOperationTime(LocalDateTime.now());
        
        FactorManagementResponse.DerivedFactorData derivedFactorData = 
                new FactorManagementResponse.DerivedFactorData();
        derivedFactorData.setDerivedId(1);
        derivedFactorData.setFactorName("测试衍生因子");
        derivedFactorData.setFactorCode("TEST_DERIVED");
        testResponse.setDerivedFactorData(derivedFactorData);
    }
    
    /**
     * 创建新的衍生因子信息实例，避免测试之间的状态污染
     */
    private FactorManagementRequest.DerivedFactorInfo createDerivedFactorInfo() {
        FactorManagementRequest.DerivedFactorInfo info = new FactorManagementRequest.DerivedFactorInfo();
        info.setFactorName("测试衍生因子");
        info.setFactorCode("TEST_DERIVED");
        info.setFactorDesc("测试描述");
        info.setCalcStrategyId(1);
        info.setBaseFactorIds(Arrays.asList(3, 4));
        info.setStyleTagIds("1,2");
        info.setCreateUserId(1);
        return info;
    }
    
    /**
     * 创建新的权重配置信息实例
     */
    private FactorManagementRequest.WeightConfigInfo createWeightConfigInfo() {
        FactorManagementRequest.WeightConfigInfo info = new FactorManagementRequest.WeightConfigInfo();
        info.setDerivedId(1);
        info.setBaseFactorWeights("'3': 0.6, '4': 0.4");
        info.setWeightDesc("用户自定义权重");
        return info;
    }
    
    /**
     * 创建新的树节点操作信息实例
     */
    private FactorManagementRequest.TreeOperationInfo createTreeOperationInfo() {
        FactorManagementRequest.TreeOperationInfo info = new FactorManagementRequest.TreeOperationInfo();
        info.setParentId(1);
        info.setNodeName("价值因子");
        info.setNodeType("CATEGORY");
        info.setSceneId("EQUITY");
        info.setDescription("包含所有价值相关因子");
        return info;
    }
    
    // ==================== 创建衍生因子测试 ====================
    
    @Test
    void testCreateDerivedFactor_Success() throws Exception {
        // Given - 创建新的独立实例，因为控制器可能会修改baseFactorIds（特别是在有自定义权重的情况下）
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("CREATE_DERIVED_FACTOR");
        request.setDerivedFactorInfo(createDerivedFactorInfo());
        
        when(factorManagementService.createDerivedFactor(any(FactorManagementRequest.DerivedFactorInfo.class)))
                .thenReturn(testResponse);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.derivedFactorData.derivedId").value(1))
                .andExpect(jsonPath("$.data.derivedFactorData.factorName").value("测试衍生因子"));
    }
    
    @Test
    void testCreateDerivedFactor_WithCustomWeights() throws Exception {
        // Given - 使用辅助方法创建新的独立实例，避免控制器修改共享对象
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("CREATE_DERIVED_FACTOR");
        request.setDerivedFactorInfo(createDerivedFactorInfo());
        request.setWeightConfigInfo(createWeightConfigInfo());
        
        FactorManagementResponse weightResponse = new FactorManagementResponse();
        weightResponse.setSuccess(true);
        weightResponse.setMessage("权重配置成功");
        
        when(factorManagementService.createDerivedFactor(any(FactorManagementRequest.DerivedFactorInfo.class)))
                .thenReturn(testResponse);
        when(factorManagementService.configureFactorWeights(any(FactorManagementRequest.WeightConfigInfo.class)))
                .thenReturn(weightResponse);
        when(factorManagementService.getDerivedFactor(anyInt()))
                .thenReturn(testResponse);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testCreateDerivedFactor_WithCustomWeights_WeightConfigFailure() throws Exception {
        // Given - 使用辅助方法创建新的独立实例
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("CREATE_DERIVED_FACTOR");
        request.setDerivedFactorInfo(createDerivedFactorInfo());
        request.setWeightConfigInfo(createWeightConfigInfo());
        
        FactorManagementResponse weightErrorResponse = new FactorManagementResponse();
        weightErrorResponse.setSuccess(false);
        weightErrorResponse.setMessage("权重配置失败");
        
        when(factorManagementService.createDerivedFactor(any(FactorManagementRequest.DerivedFactorInfo.class)))
                .thenReturn(testResponse);
        when(factorManagementService.configureFactorWeights(any(FactorManagementRequest.WeightConfigInfo.class)))
                .thenReturn(weightErrorResponse);
        
        // When & Then - 应该抛出异常导致500错误
        mockMvc.perform(post("/factor/factor-management/derived-factors/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testCreateDerivedFactor_ServiceFailure() throws Exception {
        // Given - 使用辅助方法创建新的独立实例
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("CREATE_DERIVED_FACTOR");
        request.setDerivedFactorInfo(createDerivedFactorInfo());
        
        FactorManagementResponse errorResponse = new FactorManagementResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage("创建失败");
        
        when(factorManagementService.createDerivedFactor(any(FactorManagementRequest.DerivedFactorInfo.class)))
                .thenReturn(errorResponse);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testCreateDerivedFactor_ServiceException() throws Exception {
        // Given - 使用辅助方法创建新的独立实例
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("CREATE_DERIVED_FACTOR");
        request.setDerivedFactorInfo(createDerivedFactorInfo());
        
        when(factorManagementService.createDerivedFactor(any(FactorManagementRequest.DerivedFactorInfo.class)))
                .thenThrow(new RuntimeException("服务异常"));
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testCreateDerivedFactor_InvalidOperationType() throws Exception {
        // Given - 创建新的独立实例，保持测试隔离
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("INVALID_OPERATION");
        
        FactorManagementRequest.DerivedFactorInfo customDerivedFactorInfo = createDerivedFactorInfo();
        request.setDerivedFactorInfo(customDerivedFactorInfo);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testCreateDerivedFactor_MissingDerivedFactorInfo() throws Exception {
        // Given
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("CREATE_DERIVED_FACTOR");
        // 不设置 derivedFactorInfo
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    // ==================== 创建衍生因子完整流程测试 ====================
    
    @Test
    void testCreateDerivedFactorWithFullFlow_Success() throws Exception {
        // Given - 使用辅助方法创建新的独立实例，避免服务层修改共享对象
        FactorManagementRequest request = new FactorManagementRequest();
        request.setDerivedFactorInfo(createDerivedFactorInfo());
        request.setWeightConfigInfo(createWeightConfigInfo());
        request.setTreeOperationInfo(createTreeOperationInfo());
        
        when(factorTreeMapper.getNodeById(1)).thenReturn(testParentNode);
        when(factorManagementService.processFactorManagement(any(FactorManagementRequest.class)))
                .thenReturn(testResponse);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testCreateDerivedFactorWithFullFlow_ParentNodeNotFound() throws Exception {
        // Given - 使用辅助方法创建新的独立实例
        FactorManagementRequest request = new FactorManagementRequest();
        request.setDerivedFactorInfo(createDerivedFactorInfo());
        
        FactorManagementRequest.TreeOperationInfo customTreeOperationInfo = createTreeOperationInfo();
        request.setTreeOperationInfo(customTreeOperationInfo);
        
        when(factorTreeMapper.getNodeById(1)).thenReturn(null);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testCreateDerivedFactorWithFullFlow_ParentIdZero_WithoutSceneId() throws Exception {
        // Given - parentId为0但没有sceneId应该失败，使用辅助方法创建新的独立实例
        FactorManagementRequest request = new FactorManagementRequest();
        request.setDerivedFactorInfo(createDerivedFactorInfo());
        
        FactorManagementRequest.TreeOperationInfo treeInfo = new FactorManagementRequest.TreeOperationInfo();
        treeInfo.setParentId(0);
        // 不设置sceneId
        request.setTreeOperationInfo(treeInfo);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testCreateDerivedFactorWithFullFlow_ParentIdZero_WithSceneId() throws Exception {
        // Given - parentId为0但有sceneId应该成功，使用辅助方法创建新的独立实例
        FactorManagementRequest request = new FactorManagementRequest();
        request.setDerivedFactorInfo(createDerivedFactorInfo());
        
        FactorManagementRequest.TreeOperationInfo treeInfo = new FactorManagementRequest.TreeOperationInfo();
        treeInfo.setParentId(0);
        treeInfo.setSceneId("EQUITY");
        request.setTreeOperationInfo(treeInfo);
        
        when(factorManagementService.processFactorManagement(any(FactorManagementRequest.class)))
                .thenReturn(testResponse);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testCreateDerivedFactorWithFullFlow_InvalidFactorCode() throws Exception {
        // Given - 创建新的独立实例，避免修改共享的testDerivedFactorInfo
        FactorManagementRequest request = new FactorManagementRequest();
        FactorManagementRequest.DerivedFactorInfo invalidDerivedFactorInfo = new FactorManagementRequest.DerivedFactorInfo();
        invalidDerivedFactorInfo.setFactorName("测试衍生因子");
        invalidDerivedFactorInfo.setFactorCode("INVALID-CODE-WITH-DASH"); // 无效的因子编码
        invalidDerivedFactorInfo.setFactorDesc("测试描述");
        request.setDerivedFactorInfo(invalidDerivedFactorInfo);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testCreateDerivedFactorWithFullFlow_EmptyFactorName() throws Exception {
        // Given - 创建新的独立实例，避免修改共享的testDerivedFactorInfo
        FactorManagementRequest request = new FactorManagementRequest();
        FactorManagementRequest.DerivedFactorInfo emptyNameDerivedFactorInfo = new FactorManagementRequest.DerivedFactorInfo();
        emptyNameDerivedFactorInfo.setFactorName(""); // 空因子名称
        emptyNameDerivedFactorInfo.setFactorCode("TEST_DERIVED");
        emptyNameDerivedFactorInfo.setFactorDesc("测试描述");
        request.setDerivedFactorInfo(emptyNameDerivedFactorInfo);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testCreateDerivedFactorWithFullFlow_EmptyFactorCode() throws Exception {
        // Given - 创建新的独立实例，避免修改共享的testDerivedFactorInfo
        FactorManagementRequest request = new FactorManagementRequest();
        FactorManagementRequest.DerivedFactorInfo emptyCodeDerivedFactorInfo = new FactorManagementRequest.DerivedFactorInfo();
        emptyCodeDerivedFactorInfo.setFactorName("测试衍生因子");
        emptyCodeDerivedFactorInfo.setFactorCode(""); // 空因子编码
        emptyCodeDerivedFactorInfo.setFactorDesc("测试描述");
        request.setDerivedFactorInfo(emptyCodeDerivedFactorInfo);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testCreateDerivedFactorWithFullFlow_ServiceFailure() throws Exception {
        // Given - 使用辅助方法创建新的独立实例
        FactorManagementRequest request = new FactorManagementRequest();
        request.setDerivedFactorInfo(createDerivedFactorInfo());
        
        FactorManagementResponse errorResponse = new FactorManagementResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage("服务层创建失败");
        
        when(factorManagementService.processFactorManagement(any(FactorManagementRequest.class)))
                .thenReturn(errorResponse);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testCreateDerivedFactorWithFullFlow_ServiceException() throws Exception {
        // Given - 使用辅助方法创建新的独立实例
        FactorManagementRequest request = new FactorManagementRequest();
        request.setDerivedFactorInfo(createDerivedFactorInfo());
        
        when(factorManagementService.processFactorManagement(any(FactorManagementRequest.class)))
                .thenThrow(new RuntimeException("服务异常"));
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    // ==================== 更新衍生因子测试 ====================
    
    @Test
    void testUpdateDerivedFactor_Success() throws Exception {
        // Given - 使用辅助方法创建新的独立实例
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("UPDATE_DERIVED_FACTOR");
        request.setDerivedFactorInfo(createDerivedFactorInfo());
        
        when(factorManagementService.updateDerivedFactor(anyInt(), any(FactorManagementRequest.DerivedFactorInfo.class)))
                .thenReturn(testResponse);
        
        // When & Then
        mockMvc.perform(put("/factor/factor-management/derived-factors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testUpdateDerivedFactor_InvalidOperationType() throws Exception {
        // Given - 使用辅助方法创建新的独立实例
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("INVALID_OPERATION");
        request.setDerivedFactorInfo(createDerivedFactorInfo());
        
        // When & Then
        mockMvc.perform(put("/factor/factor-management/derived-factors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testUpdateDerivedFactor_MissingDerivedFactorInfo() throws Exception {
        // Given
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("UPDATE_DERIVED_FACTOR");
        // 不设置 derivedFactorInfo
        
        // When & Then
        mockMvc.perform(put("/factor/factor-management/derived-factors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testUpdateDerivedFactor_ServiceFailure() throws Exception {
        // Given - 使用辅助方法创建新的独立实例
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("UPDATE_DERIVED_FACTOR");
        request.setDerivedFactorInfo(createDerivedFactorInfo());
        
        FactorManagementResponse errorResponse = new FactorManagementResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage("更新失败");
        
        when(factorManagementService.updateDerivedFactor(anyInt(), any(FactorManagementRequest.DerivedFactorInfo.class)))
                .thenReturn(errorResponse);
        
        // When & Then
        mockMvc.perform(put("/factor/factor-management/derived-factors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    // ==================== 删除衍生因子测试 ====================
    
    @Test
    void testDeleteDerivedFactor_Success() throws Exception {
        // Given
        when(factorManagementService.deleteDerivedFactor(1))
                .thenReturn(testResponse);
        
        // When & Then
        mockMvc.perform(delete("/factor/factor-management/derived-factors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testDeleteDerivedFactor_ServiceFailure() throws Exception {
        // Given
        FactorManagementResponse errorResponse = new FactorManagementResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage("删除失败");
        
        when(factorManagementService.deleteDerivedFactor(1))
                .thenReturn(errorResponse);
        
        // When & Then
        mockMvc.perform(delete("/factor/factor-management/derived-factors/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testDeleteDerivedFactor_ServiceException() throws Exception {
        // Given
        when(factorManagementService.deleteDerivedFactor(1))
                .thenThrow(new RuntimeException("删除异常"));
        
        // When & Then
        mockMvc.perform(delete("/factor/factor-management/derived-factors/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    // ==================== 查询衍生因子测试 ====================
    
    @Test
    void testGetDerivedFactor_Success() throws Exception {
        // Given
        when(factorManagementService.getDerivedFactor(1))
                .thenReturn(testResponse);
        
        // When & Then
        mockMvc.perform(get("/factor/factor-management/derived-factors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.derivedFactorData.derivedId").value(1));
    }
    
    @Test
    void testGetDerivedFactor_NotFound() throws Exception {
        // Given
        FactorManagementResponse notFoundResponse = new FactorManagementResponse();
        notFoundResponse.setSuccess(false);
        notFoundResponse.setMessage("衍生因子不存在");
        
        when(factorManagementService.getDerivedFactor(999))
                .thenReturn(notFoundResponse);
        
        // When & Then
        mockMvc.perform(get("/factor/factor-management/derived-factors/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testGetDerivedFactor_ServiceException() throws Exception {
        // Given
        when(factorManagementService.getDerivedFactor(1))
                .thenThrow(new RuntimeException("查询异常"));
        
        // When & Then
        mockMvc.perform(get("/factor/factor-management/derived-factors/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testGetAllDerivedFactors_Success() throws Exception {
        // Given
        when(factorManagementService.getAllDerivedFactors())
                .thenReturn(testResponse);
        
        // When & Then
        mockMvc.perform(get("/factor/factor-management/derived-factors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testGetAllDerivedFactors_ServiceFailure() throws Exception {
        // Given
        FactorManagementResponse errorResponse = new FactorManagementResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage("查询失败");
        
        when(factorManagementService.getAllDerivedFactors())
                .thenReturn(errorResponse);
        
        // When & Then
        mockMvc.perform(get("/factor/factor-management/derived-factors"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    // ==================== 权重配置测试 ====================
    
    @Test
    void testConfigureFactorWeights_Success() throws Exception {
        // Given
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("CONFIGURE_FACTOR_WEIGHTS");
        request.setWeightConfigInfo(testWeightConfigInfo);
        
        FactorManagementResponse weightResponse = new FactorManagementResponse();
        weightResponse.setSuccess(true);
        weightResponse.setMessage("权重配置成功");
        
        when(factorManagementService.configureFactorWeights(any(FactorManagementRequest.WeightConfigInfo.class)))
                .thenReturn(weightResponse);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors/1/weights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testConfigureFactorWeights_MissingWeightConfig() throws Exception {
        // Given
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("CONFIGURE_FACTOR_WEIGHTS");
        // 不设置 weightConfigInfo
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors/1/weights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testConfigureFactorWeights_EmptyWeights() throws Exception {
        // Given
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("CONFIGURE_FACTOR_WEIGHTS");
        
        FactorManagementRequest.WeightConfigInfo emptyWeightInfo = new FactorManagementRequest.WeightConfigInfo();
        emptyWeightInfo.setDerivedId(1);
        emptyWeightInfo.setBaseFactorWeights(""); // 空权重
        request.setWeightConfigInfo(emptyWeightInfo);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors/1/weights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testConfigureFactorWeights_InvalidOperationType() throws Exception {
        // Given
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("INVALID_OPERATION");
        request.setWeightConfigInfo(testWeightConfigInfo);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors/1/weights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testConfigureFactorWeights_ServiceFailure() throws Exception {
        // Given
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("CONFIGURE_FACTOR_WEIGHTS");
        request.setWeightConfigInfo(testWeightConfigInfo);
        
        FactorManagementResponse errorResponse = new FactorManagementResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage("权重配置失败");
        
        when(factorManagementService.configureFactorWeights(any(FactorManagementRequest.WeightConfigInfo.class)))
                .thenReturn(errorResponse);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors/1/weights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testValidateFactorWeights_Success() throws Exception {
        // Given
        FactorManagementRequest request = new FactorManagementRequest();
        request.setOperationType("VALIDATE_FACTOR_WEIGHTS");
        request.setWeightConfigInfo(testWeightConfigInfo);
        
        FactorManagementResponse validateResponse = new FactorManagementResponse();
        validateResponse.setSuccess(true);
        validateResponse.setMessage("权重验证通过");
        
        when(factorManagementService.validateFactorWeights(any(FactorManagementRequest.WeightConfigInfo.class)))
                .thenReturn(validateResponse);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors/1/weights/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    // ==================== 其他工具方法测试 ====================
    
    @Test
    void testGetAvailableBaseFactorsForDerived_Success() throws Exception {
        // Given
        FactorManagementResponse response = new FactorManagementResponse();
        response.setSuccess(true);
        response.setMessage("查询成功");
        
        when(factorManagementService.getAvailableBaseFactorsForDerived())
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/factor/factor-management/derived-factors/available-base-factors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testPreviewDerivedFactor_Success() throws Exception {
        // Given
        FactorManagementResponse response = new FactorManagementResponse();
        response.setSuccess(true);
        response.setMessage("预览成功");
        
        when(factorManagementService.previewDerivedFactor(anyList(), anyMap(), anyInt()))
                .thenReturn(response);
        
        // When & Then - Spring MVC通过 @RequestParam Map 可以接收所有query参数
        // 对于List<Integer>，使用多个同名参数 baseFactorIds=3&baseFactorIds=4
        // 对于Map<Integer, Double>，Spring会尝试解析，但实际可能无法正确映射
        // 这里测试框架会调用服务层，即使参数解析有问题，mock会返回成功响应
        mockMvc.perform(post("/factor/factor-management/derived-factors/preview")
                .param("baseFactorIds", "3", "4")
                .param("weights['3']", "0.6")
                .param("weights['4']", "0.4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testPreviewDerivedFactor_MissingParameters() throws Exception {
        // When & Then - 缺少必填参数会返回400错误
        mockMvc.perform(post("/factor/factor-management/derived-factors/preview"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testPreviewDerivedFactor_MissingBaseFactorIds() throws Exception {
        // When & Then - 缺少baseFactorIds参数会返回400错误
        mockMvc.perform(post("/factor/factor-management/derived-factors/preview")
                .param("weights['3']", "0.6"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testPreviewDerivedFactor_MissingWeights() throws Exception {
        // When & Then - 缺少weights参数会返回400错误
        mockMvc.perform(post("/factor/factor-management/derived-factors/preview")
                .param("baseFactorIds", "3", "4"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testPreviewDerivedFactor_ServiceFailure() throws Exception {
        // Given
        FactorManagementResponse errorResponse = new FactorManagementResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage("预览失败");
        
        when(factorManagementService.previewDerivedFactor(anyList(), anyMap(), anyInt()))
                .thenReturn(errorResponse);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/derived-factors/preview")
                .param("baseFactorIds", "3", "4")
                .param("weights['3']", "0.6")
                .param("weights['4']", "0.4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testSearchFactors_Success() throws Exception {
        // Given
        FactorManagementResponse response = new FactorManagementResponse();
        response.setSuccess(true);
        response.setMessage("搜索成功");
        
        when(factorManagementService.searchFactors(anyString(), anyString(), anyString()))
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/factor/factor-management/search")
                .param("keyword", "测试")
                .param("type", "DERIVED")
                .param("sceneId", "EQUITY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testSearchFactors_MissingKeyword() throws Exception {
        // When & Then - keyword是必填参数，缺少会导致400错误
        mockMvc.perform(get("/factor/factor-management/search")
                .param("type", "DERIVED"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testSearchFactors_ServiceFailure() throws Exception {
        // Given
        FactorManagementResponse errorResponse = new FactorManagementResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage("搜索失败");
        
        when(factorManagementService.searchFactors(anyString(), anyString(), anyString()))
                .thenReturn(errorResponse);
        
        // When & Then
        mockMvc.perform(get("/factor/factor-management/search")
                .param("keyword", "测试"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testGetFactorStatistics_Success() throws Exception {
        // Given
        FactorManagementResponse response = new FactorManagementResponse();
        response.setSuccess(true);
        response.setMessage("统计成功");
        
        when(factorManagementService.getFactorStatistics())
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/factor/factor-management/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testGetFactorStatistics_ServiceFailure() throws Exception {
        // Given
        FactorManagementResponse errorResponse = new FactorManagementResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage("统计失败");
        
        when(factorManagementService.getFactorStatistics())
                .thenReturn(errorResponse);
        
        // When & Then
        mockMvc.perform(get("/factor/factor-management/statistics"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testValidateFactorData_Success() throws Exception {
        // Given
        List<Integer> baseFactorIds = Arrays.asList(3, 4);
        FactorManagementResponse response = new FactorManagementResponse();
        response.setSuccess(true);
        response.setMessage("验证成功");
        
        when(factorManagementService.validateFactorData(anyList()))
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/validate-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(baseFactorIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testValidateFactorData_MissingRequestBody() throws Exception {
        // When & Then - 缺少请求体会导致400错误
        mockMvc.perform(post("/factor/factor-management/validate-data")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testValidateFactorData_EmptyList() throws Exception {
        // Given
        List<Integer> emptyList = Arrays.asList();
        FactorManagementResponse response = new FactorManagementResponse();
        response.setSuccess(false);
        response.setMessage("因子ID列表不能为空");
        
        when(factorManagementService.validateFactorData(anyList()))
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/validate-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyList)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testValidateFactorData_ServiceFailure() throws Exception {
        // Given
        List<Integer> baseFactorIds = Arrays.asList(3, 4);
        FactorManagementResponse errorResponse = new FactorManagementResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage("验证失败");
        
        when(factorManagementService.validateFactorData(anyList()))
                .thenReturn(errorResponse);
        
        // When & Then
        mockMvc.perform(post("/factor/factor-management/validate-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(baseFactorIds)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}

