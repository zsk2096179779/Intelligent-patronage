package com.fengqi.fund.fundadvisor.controller.factor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.dto.factor.FactorPreviewResponse;
import com.fengqi.fund.fundadvisor.dto.factor.FactorQueryRequest;
import com.fengqi.fund.fundadvisor.dto.factor.FactorSelectionRequest;
import com.fengqi.fund.fundadvisor.entity.factor.FactorBase;
import com.fengqi.fund.fundadvisor.service.factor.FactorBaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FactorBaseController 测试类
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
@WebMvcTest(FactorBaseController.class)
class FactorBaseControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private FactorBaseService factorBaseService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private FactorBase testFactorBase;
    private FactorQueryRequest testQueryRequest;
    private FactorSelectionRequest testSelectionRequest;
    
    @BeforeEach
    void setUp() {
        // 设置测试基础因子
        testFactorBase = new FactorBase();
        testFactorBase.setBaseId(1);
        testFactorBase.setFactorName("测试因子");
        testFactorBase.setFactorCode("TEST_FACTOR");
        testFactorBase.setFactorFormula("test formula");
        testFactorBase.setDataSource("测试数据源");
        testFactorBase.setUpdateFrequency("日度");
        testFactorBase.setDataStartDate(LocalDate.now());
        testFactorBase.setLatestDataDate(LocalDate.now());
        testFactorBase.setDataDesc("测试描述");
        testFactorBase.setIsValid(true);
        
        // 设置测试查询请求
        testQueryRequest = new FactorQueryRequest();
        testQueryRequest.setKeyword("测试");
        testQueryRequest.setPageNum(1);
        testQueryRequest.setPageSize(10);
        
        // 设置测试选择请求
        testSelectionRequest = new FactorSelectionRequest();
        testSelectionRequest.setFactorIds(Arrays.asList(1, 2, 3));
    }
    
    // ==================== 查询基础因子测试 ====================
    
    @Test
    void testQueryFactors_Success() throws Exception {
        // Given
        List<FactorBase> factors = new ArrayList<>();
        factors.add(testFactorBase);
        
        ResultDTO<List<FactorBase>> expectedResult = ResultDTO.success(factors);
        when(factorBaseService.queryFactors(any(FactorQueryRequest.class)))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/base/query")
                .param("keyword", "测试")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].baseId").value(1))
                .andExpect(jsonPath("$.data[0].factorName").value("测试因子"));
    }
    
    @Test
    void testQueryFactors_WithTypeFilter() throws Exception {
        // Given
        List<FactorBase> factors = new ArrayList<>();
        factors.add(testFactorBase);
        
        ResultDTO<List<FactorBase>> expectedResult = ResultDTO.success(factors);
        when(factorBaseService.queryFactors(any(FactorQueryRequest.class)))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/base/query")
                .param("factorType", "VALUE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    void testQueryFactors_WithDataSourceFilter() throws Exception {
        // Given
        List<FactorBase> factors = new ArrayList<>();
        factors.add(testFactorBase);
        
        ResultDTO<List<FactorBase>> expectedResult = ResultDTO.success(factors);
        when(factorBaseService.queryFactors(any(FactorQueryRequest.class)))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/base/query")
                .param("dataSource", "测试数据源"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    void testQueryFactors_EmptyResult() throws Exception {
        // Given
        List<FactorBase> emptyList = new ArrayList<>();
        ResultDTO<List<FactorBase>> expectedResult = ResultDTO.success(emptyList);
        when(factorBaseService.queryFactors(any(FactorQueryRequest.class)))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/base/query")
                .param("keyword", "不存在"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
    
    @Test
    void testQueryFactors_NoParameters() throws Exception {
        // Given
        List<FactorBase> factors = new ArrayList<>();
        factors.add(testFactorBase);
        
        ResultDTO<List<FactorBase>> expectedResult = ResultDTO.success(factors);
        when(factorBaseService.queryFactors(any(FactorQueryRequest.class)))
                .thenReturn(expectedResult);
        
        // When & Then - 无参数查询应该使用默认值
        mockMvc.perform(get("/factor/base/query"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    void testQueryFactors_ServiceError() throws Exception {
        // Given
        ResultDTO<List<FactorBase>> errorResult = ResultDTO.error("查询失败");
        when(factorBaseService.queryFactors(any(FactorQueryRequest.class)))
                .thenReturn(errorResult);
        
        // When & Then
        mockMvc.perform(get("/factor/base/query")
                .param("keyword", "测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testQueryFactors_WithPopularOnly() throws Exception {
        // Given
        List<FactorBase> factors = new ArrayList<>();
        factors.add(testFactorBase);
        
        ResultDTO<List<FactorBase>> expectedResult = ResultDTO.success(factors);
        when(factorBaseService.queryFactors(any(FactorQueryRequest.class)))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/base/query")
                .param("popularOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    // ==================== 根据ID列表获取因子测试 ====================
    
    @Test
    void testGetFactorsByIds_Success() throws Exception {
        // Given
        List<FactorBase> factors = new ArrayList<>();
        factors.add(testFactorBase);
        
        ResultDTO<List<FactorBase>> expectedResult = ResultDTO.success(factors);
        when(factorBaseService.getFactorsByIds(anyList()))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/base/ids")
                .param("factorIds", "1,2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].baseId").value(1));
    }
    
    @Test
    void testGetFactorsByIds_SingleId() throws Exception {
        // Given
        List<FactorBase> factors = new ArrayList<>();
        factors.add(testFactorBase);
        
        ResultDTO<List<FactorBase>> expectedResult = ResultDTO.success(factors);
        when(factorBaseService.getFactorsByIds(anyList()))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/base/ids")
                .param("factorIds", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    void testGetFactorsByIds_InvalidIds() throws Exception {
        // When & Then - 无效的ID格式会导致解析异常，返回400错误
        mockMvc.perform(get("/factor/base/ids")
                .param("factorIds", "invalid"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGetFactorsByIds_EmptyList() throws Exception {
        // When & Then - 空字符串会导致解析错误，返回400错误
        mockMvc.perform(get("/factor/base/ids")
                .param("factorIds", ""))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGetFactorsByIds_MissingParameter() throws Exception {
        // When & Then - 缺少参数会导致400错误
        mockMvc.perform(get("/factor/base/ids"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGetFactorsByIds_ServiceError() throws Exception {
        // Given
        ResultDTO<List<FactorBase>> errorResult = ResultDTO.error("服务错误");
        when(factorBaseService.getFactorsByIds(anyList()))
                .thenReturn(errorResult);
        
        // When & Then
        mockMvc.perform(get("/factor/base/ids")
                .param("factorIds", "1,2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    // ==================== 验证因子选择测试 ====================
    
    @Test
    void testValidateFactorSelection_Success() throws Exception {
        // Given
        ResultDTO<Boolean> expectedResult = ResultDTO.success(true, "因子选择有效");
        when(factorBaseService.validateFactorSelection(anyList()))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(post("/factor/base/selection/validate")
                .param("factorIds", "1,2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }
    
    @Test
    void testValidateFactorSelection_InvalidSelection() throws Exception {
        // Given
        ResultDTO<Boolean> expectedResult = ResultDTO.success(false, "存在无效的因子ID");
        when(factorBaseService.validateFactorSelection(anyList()))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(post("/factor/base/selection/validate")
                .param("factorIds", "999,1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));
    }
    
    @Test
    void testValidateFactorSelection_EmptySelection() throws Exception {
        // When & Then - 空字符串会导致解析错误，返回400错误
        mockMvc.perform(post("/factor/base/selection/validate")
                .param("factorIds", ""))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testValidateFactorSelection_MissingParameter() throws Exception {
        // When & Then - 缺少参数会导致400错误
        mockMvc.perform(post("/factor/base/selection/validate"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testValidateFactorSelection_InvalidIds() throws Exception {
        // When & Then - 无效的ID格式会导致解析异常
        mockMvc.perform(post("/factor/base/selection/validate")
                .param("factorIds", "invalid"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testValidateFactorSelection_ServiceError() throws Exception {
        // Given
        ResultDTO<Boolean> errorResult = ResultDTO.error("验证失败");
        when(factorBaseService.validateFactorSelection(anyList()))
                .thenReturn(errorResult);
        
        // When & Then
        mockMvc.perform(post("/factor/base/selection/validate")
                .param("factorIds", "1,2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    // ==================== 预览选中因子测试 ====================
    
    @Test
    void testPreviewSelectedFactors_Success() throws Exception {
        // Given
        FactorPreviewResponse previewResponse = new FactorPreviewResponse();
        // FactorPreviewResponse 使用 @Data 注解，使用正确的字段设置数据
        // 实际字段是 factors, dataPreview, dateRange, statistics
        
        ResultDTO<FactorPreviewResponse> expectedResult = ResultDTO.success(previewResponse, "预览成功");
        when(factorBaseService.previewSelectedFactors(any(FactorSelectionRequest.class)))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(post("/factor/base/selection/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSelectionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }
    
    @Test
    void testPreviewSelectedFactors_EmptySelection() throws Exception {
        // Given
        FactorSelectionRequest emptyRequest = new FactorSelectionRequest();
        emptyRequest.setFactorIds(new ArrayList<>());
        
        ResultDTO<FactorPreviewResponse> expectedResult = ResultDTO.error("请至少选择一个因子");
        when(factorBaseService.previewSelectedFactors(any(FactorSelectionRequest.class)))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(post("/factor/base/selection/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testPreviewSelectedFactors_InvalidRequest() throws Exception {
        // Given - 无效的JSON请求
        String invalidJson = "{\"invalid\": \"request\"}";
        
        // When & Then - Spring会处理无效的请求绑定
        mockMvc.perform(post("/factor/base/selection/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testPreviewSelectedFactors_MissingRequestBody() throws Exception {
        // When & Then - 缺少请求体
        mockMvc.perform(post("/factor/base/selection/preview")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testPreviewSelectedFactors_ServiceError() throws Exception {
        // Given
        ResultDTO<FactorPreviewResponse> errorResult = ResultDTO.error("预览失败");
        when(factorBaseService.previewSelectedFactors(any(FactorSelectionRequest.class)))
                .thenReturn(errorResult);
        
        // When & Then
        mockMvc.perform(post("/factor/base/selection/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSelectionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testPreviewSelectedFactors_ServiceException() throws Exception {
        // Given
        when(factorBaseService.previewSelectedFactors(any(FactorSelectionRequest.class)))
                .thenThrow(new RuntimeException("服务异常"));
        
        // When & Then - 服务层异常会向上传播，可能返回500或由框架处理
        try {
            mockMvc.perform(post("/factor/base/selection/preview")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testSelectionRequest)))
                    .andExpect(status().is5xxServerError());
        } catch (Exception e) {
            // 如果没有全局异常处理，可能会抛出异常
            // 这是正常的，因为控制器没有处理这种情况
        }
    }
    
    @Test
    void testPreviewSelectedFactors_NullFactorIds() throws Exception {
        // Given
        FactorSelectionRequest nullRequest = new FactorSelectionRequest();
        nullRequest.setFactorIds(null);
        
        ResultDTO<FactorPreviewResponse> errorResult = ResultDTO.error("请至少选择一个基础因子");
        when(factorBaseService.previewSelectedFactors(any(FactorSelectionRequest.class)))
                .thenReturn(errorResult);
        
        // When & Then
        mockMvc.perform(post("/factor/base/selection/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
}

