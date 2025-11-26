package com.fengqi.fund.fundadvisor.controller.factor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.dto.factor.StyleTagRequest;
import com.fengqi.fund.fundadvisor.dto.factor.StyleTagUpdateRequest;
import com.fengqi.fund.fundadvisor.entity.factor.StyleTag;
import com.fengqi.fund.fundadvisor.service.factor.StyleTagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StyleTagController 测试类
 * 
 * 测试风格标签的CRUD操作
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@WebMvcTest(StyleTagController.class)
class StyleTagControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private StyleTagService styleTagService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private StyleTag testStyleTag;
    private StyleTagRequest testCreateRequest;
    private StyleTagUpdateRequest testUpdateRequest;
    
    @BeforeEach
    void setUp() {
        // 设置测试风格标签
        testStyleTag = new StyleTag();
        testStyleTag.setTagId(1);
        testStyleTag.setTagName("价值");
        testStyleTag.setTagCode("VALUE");
        testStyleTag.setDescription("价值风格标签，强调低估值、高分红的投资策略");
        testStyleTag.setCreateUserId(1);
        testStyleTag.setCreateTime(LocalDateTime.now());
        testStyleTag.setIsValid(1);
        
        // 设置测试创建请求
        testCreateRequest = new StyleTagRequest();
        testCreateRequest.setTagName("价值");
        testCreateRequest.setTagCode("VALUE");
        testCreateRequest.setDescription("价值风格标签，强调低估值、高分红的投资策略");
        testCreateRequest.setCreateUserId(1);
        
        // 设置测试更新请求
        testUpdateRequest = new StyleTagUpdateRequest();
        testUpdateRequest.setTagName("价值型");
        testUpdateRequest.setTagCode("VALUE_TYPE");
        testUpdateRequest.setDescription("更新后的描述");
    }
    
    // ==================== 创建风格标签测试 ====================
    
    @Test
    void testCreateStyleTag_Success() throws Exception {
        // Given
        ResultDTO<StyleTag> expectedResult = ResultDTO.success(testStyleTag, "风格标签创建成功");
        when(styleTagService.createStyleTag(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(post("/factor/style-tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tagId").value(1))
                .andExpect(jsonPath("$.data.tagName").value("价值"))
                .andExpect(jsonPath("$.data.tagCode").value("VALUE"));
    }
    
    @Test
    void testCreateStyleTag_ValidationError() throws Exception {
        // Given - 缺少必填字段
        StyleTagRequest invalidRequest = new StyleTagRequest();
        invalidRequest.setTagName(""); // 空名称
        
        // When & Then - 验证失败，返回400错误
        mockMvc.perform(post("/factor/style-tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testCreateStyleTag_DuplicateName() throws Exception {
        // Given
        ResultDTO<StyleTag> errorResult = ResultDTO.error("风格标签名称已存在: 价值");
        when(styleTagService.createStyleTag(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(errorResult);
        
        // When & Then
        mockMvc.perform(post("/factor/style-tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("风格标签名称已存在: 价值"));
    }
    
    @Test
    void testCreateStyleTag_DuplicateCode() throws Exception {
        // Given
        ResultDTO<StyleTag> errorResult = ResultDTO.error("风格标签编码已存在: VALUE");
        when(styleTagService.createStyleTag(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(errorResult);
        
        // When & Then
        mockMvc.perform(post("/factor/style-tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
    
    // ==================== 根据编码查询风格标签测试 ====================
    
    @Test
    void testGetStyleTagByCode_Success() throws Exception {
        // Given
        ResultDTO<StyleTag> expectedResult = ResultDTO.success(testStyleTag, "查询成功");
        when(styleTagService.getStyleTagByCode("VALUE"))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/style-tags/code/VALUE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tagId").value(1))
                .andExpect(jsonPath("$.data.tagName").value("价值"))
                .andExpect(jsonPath("$.data.tagCode").value("VALUE"));
    }
    
    @Test
    void testGetStyleTagByCode_WithNumericCode() throws Exception {
        // Given - 测试数字编码的情况（如 "001"）
        StyleTag tagWithNumericCode = new StyleTag();
        tagWithNumericCode.setTagId(1);
        tagWithNumericCode.setTagName("价值");
        tagWithNumericCode.setTagCode("001");
        
        ResultDTO<StyleTag> expectedResult = ResultDTO.success(tagWithNumericCode, "查询成功");
        when(styleTagService.getStyleTagByCode("001"))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/style-tags/code/001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tagCode").value("001"));
    }
    
    @Test
    void testGetStyleTagByCode_NotFound() throws Exception {
        // Given
        ResultDTO<StyleTag> errorResult = ResultDTO.error("风格标签不存在");
        when(styleTagService.getStyleTagByCode("NOT_EXIST"))
                .thenReturn(errorResult);
        
        // When & Then
        mockMvc.perform(get("/factor/style-tags/code/NOT_EXIST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("风格标签不存在"));
    }
    
    // ==================== 查询所有风格标签测试 ====================
    
    @Test
    void testGetAllStyleTags_Success() throws Exception {
        // Given
        List<StyleTag> tags = new ArrayList<>();
        tags.add(testStyleTag);
        
        StyleTag tag2 = new StyleTag();
        tag2.setTagId(2);
        tag2.setTagName("成长");
        tag2.setTagCode("GROWTH");
        tags.add(tag2);
        
        ResultDTO<List<StyleTag>> expectedResult = ResultDTO.success(tags, "查询成功");
        when(styleTagService.getAllStyleTags())
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/style-tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].tagName").value("价值"))
                .andExpect(jsonPath("$.data[1].tagName").value("成长"));
    }
    
    @Test
    void testGetAllStyleTags_EmptyList() throws Exception {
        // Given
        List<StyleTag> emptyList = new ArrayList<>();
        ResultDTO<List<StyleTag>> expectedResult = ResultDTO.success(emptyList, "查询成功");
        when(styleTagService.getAllStyleTags())
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/style-tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
    
    // ==================== 根据ID列表查询风格标签测试 ====================
    
    @Test
    void testGetStyleTagsByIds_Success() throws Exception {
        // Given
        List<StyleTag> tags = Arrays.asList(testStyleTag);
        ResultDTO<List<StyleTag>> expectedResult = ResultDTO.success(tags, "查询成功");
        when(styleTagService.getStyleTagsByIds(anyList()))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/style-tags/ids")
                .param("tagIds", "1,2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    void testGetStyleTagsByIds_InvalidIds() throws Exception {
        // When & Then - 无效的ID格式会导致解析异常
        mockMvc.perform(get("/factor/style-tags/ids")
                .param("tagIds", "invalid"))
                .andExpect(status().isBadRequest());
    }
    
    // ==================== 更新风格标签测试 ====================
    
    @Test
    void testUpdateStyleTag_Success() throws Exception {
        // Given
        StyleTag updatedTag = new StyleTag();
        updatedTag.setTagId(1);
        updatedTag.setTagName("价值型");
        updatedTag.setTagCode("VALUE_TYPE");
        updatedTag.setDescription("更新后的描述");
        
        ResultDTO<StyleTag> expectedResult = ResultDTO.success(updatedTag, "风格标签更新成功");
        when(styleTagService.updateStyleTag(anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(put("/factor/style-tags/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tagName").value("价值型"));
    }
    
    @Test
    void testUpdateStyleTag_NotFound() throws Exception {
        // Given
        ResultDTO<StyleTag> errorResult = ResultDTO.error("风格标签不存在");
        when(styleTagService.updateStyleTag(anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(errorResult);
        
        // When & Then
        mockMvc.perform(put("/factor/style-tags/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
    
    @Test
    void testUpdateStyleTag_DuplicateName() throws Exception {
        // Given
        ResultDTO<StyleTag> errorResult = ResultDTO.error("风格标签名称已存在: 价值型");
        when(styleTagService.updateStyleTag(anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(errorResult);
        
        // When & Then
        mockMvc.perform(put("/factor/style-tags/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
    
    // ==================== 删除风格标签测试 ====================
    
    @Test
    void testDeleteStyleTag_Success() throws Exception {
        // Given
        ResultDTO<Boolean> expectedResult = ResultDTO.success(true, "风格标签删除成功");
        when(styleTagService.deleteStyleTag(1))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(delete("/factor/style-tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }
    
    @Test
    void testDeleteStyleTag_NotFound() throws Exception {
        // Given
        ResultDTO<Boolean> errorResult = ResultDTO.error("风格标签不存在");
        when(styleTagService.deleteStyleTag(999))
                .thenReturn(errorResult);
        
        // When & Then
        mockMvc.perform(delete("/factor/style-tags/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
    
    // ==================== 搜索风格标签测试 ====================
    
    @Test
    void testSearchStyleTags_Success() throws Exception {
        // Given
        List<StyleTag> tags = Arrays.asList(testStyleTag);
        ResultDTO<List<StyleTag>> expectedResult = ResultDTO.success(tags, "搜索成功");
        when(styleTagService.searchStyleTags("价值"))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/style-tags/search")
                .param("keyword", "价值"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].tagName").value("价值"));
    }
    
    @Test
    void testSearchStyleTags_EmptyKeyword() throws Exception {
        // Given - 空关键词应该返回所有标签
        List<StyleTag> tags = Arrays.asList(testStyleTag);
        ResultDTO<List<StyleTag>> expectedResult = ResultDTO.success(tags, "查询成功");
        when(styleTagService.searchStyleTags(null))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/style-tags/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    void testSearchStyleTags_NoResults() throws Exception {
        // Given
        List<StyleTag> emptyList = new ArrayList<>();
        ResultDTO<List<StyleTag>> expectedResult = ResultDTO.success(emptyList, "搜索成功");
        when(styleTagService.searchStyleTags("不存在"))
                .thenReturn(expectedResult);
        
        // When & Then
        mockMvc.perform(get("/factor/style-tags/search")
                .param("keyword", "不存在"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}

