package com.fengqi.fund.fundadvisor.controller.factor;

import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.dto.factor.FactorManagementResponse;
import com.fengqi.fund.fundadvisor.dto.factor.StyleTagRequest;
import com.fengqi.fund.fundadvisor.dto.factor.StyleTagUpdateRequest;
import com.fengqi.fund.fundadvisor.entity.factor.StyleTag;
import com.fengqi.fund.fundadvisor.service.factor.FactorManagementService;
import com.fengqi.fund.fundadvisor.service.factor.StyleTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 风格标签管理控制器
 * 
 * 提供风格标签的CRUD操作
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Slf4j
@RestController
@RequestMapping("/api/factor/style-tags")
@Tag(name = "风格标签管理", description = "风格标签的创建、查询、更新、删除等操作")
public class StyleTagController {
    
    @Autowired
    private StyleTagService styleTagService;
    
    @Autowired
    private FactorManagementService factorManagementService;
    
    @PostMapping
    @Operation(summary = "创建风格标签", description = "创建新的风格标签，需要提供标签名称、编码和描述")
    public ResultDTO<StyleTag> createStyleTag(@Valid @RequestBody StyleTagRequest request) {
        log.info("创建风格标签请求，tagName: {}, tagCode: {}", request.getTagName(), request.getTagCode());
        return styleTagService.createStyleTag(
                request.getTagName(),
                request.getTagCode(),
                request.getDescription(),
                request.getCreateUserId()
        );
    }
    
    @GetMapping("/code/{tagCode}")
    @Operation(summary = "根据编码查询风格标签", description = "根据标签编码查询风格标签详情")
    public ResultDTO<StyleTag> getStyleTagByCode(
            @Parameter(description = "风格标签编码") 
            @PathVariable String tagCode) {
        try {
            log.info("根据编码查询风格标签，tagCode: {}", tagCode);
            if (tagCode == null || tagCode.trim().isEmpty()) {
                return ResultDTO.error("风格标签编码不能为空");
            }
            return styleTagService.getStyleTagByCode(tagCode.trim());
        } catch (Exception e) {
            log.error("根据编码查询风格标签异常，tagCode: {}", tagCode, e);
            return ResultDTO.error("查询失败: " + e.getMessage());
        }
    }
    
    @GetMapping
    @Operation(summary = "查询所有风格标签", description = "获取所有有效的风格标签列表")
    public ResultDTO<List<StyleTag>> getAllStyleTags() {
        log.info("查询所有风格标签");
        return styleTagService.getAllStyleTags();
    }
    
    @GetMapping("/ids")
    @Operation(summary = "根据ID列表查询风格标签", description = "根据多个ID批量查询风格标签")
    public ResultDTO<List<StyleTag>> getStyleTagsByIds(
            @Parameter(description = "标签ID列表，用逗号分隔") 
            @RequestParam String tagIds) {
        List<Integer> idList = java.util.Arrays.stream(tagIds.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(java.util.stream.Collectors.toList());
        log.info("根据ID列表查询风格标签，IDs: {}", idList);
        return styleTagService.getStyleTagsByIds(idList);
    }
    
    @PutMapping("/{tagId}")
    @Operation(summary = "更新风格标签", description = "更新风格标签的名称、编码或描述")
    public ResultDTO<StyleTag> updateStyleTag(
            @Parameter(description = "风格标签ID") 
            @PathVariable Integer tagId,
            @Valid @RequestBody StyleTagUpdateRequest request) {
        log.info("更新风格标签请求，tagId: {}, tagName: {}, tagCode: {}", 
                tagId, request.getTagName(), request.getTagCode());
        return styleTagService.updateStyleTag(
                tagId,
                request.getTagName(),
                request.getTagCode(),
                request.getDescription()
        );
    }
    
    @DeleteMapping("/{tagId}")
    @Operation(summary = "删除风格标签", description = "删除指定的风格标签（软删除）")
    public ResultDTO<Boolean> deleteStyleTag(
            @Parameter(description = "风格标签ID") 
            @PathVariable Integer tagId) {
        log.info("删除风格标签请求，tagId: {}", tagId);
        return styleTagService.deleteStyleTag(tagId);
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索风格标签", description = "根据关键词搜索风格标签（支持名称和编码模糊查询）")
    public ResultDTO<List<StyleTag>> searchStyleTags(
            @Parameter(description = "搜索关键词（名称或编码）") 
            @RequestParam(required = false) String keyword) {
        log.info("搜索风格标签，keyword: {}", keyword);
        return styleTagService.searchStyleTags(keyword);
    }
    
    @GetMapping("/code/{tagCode}/factors")
    @Operation(summary = "查询风格标签关联的衍生因子", description = "根据风格标签编码查询所有关联的衍生因子")
    public ResultDTO<FactorManagementResponse> getFactorsByStyleTagCode(
            @Parameter(description = "风格标签编码") 
            @PathVariable String tagCode) {
        log.info("查询风格标签关联的衍生因子，tagCode: {}", tagCode);
        try {
            if (tagCode == null || tagCode.trim().isEmpty()) {
                return ResultDTO.error("风格标签编码不能为空");
            }
            FactorManagementResponse response = factorManagementService.getDerivedFactorsByStyleTagCode(tagCode.trim());
            if (response.getSuccess()) {
                return ResultDTO.success(response, "查询成功");
            } else {
                return ResultDTO.error(response.getMessage());
            }
        } catch (Exception e) {
            log.error("查询风格标签关联的衍生因子异常，tagCode: {}", tagCode, e);
            return ResultDTO.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{tagId}/factors")
    @Operation(summary = "查询风格标签关联的衍生因子（已废弃，请使用code/{tagCode}/factors）", description = "根据风格标签ID查询所有关联的衍生因子")
    @Deprecated
    public ResultDTO<FactorManagementResponse> getFactorsByStyleTag(
            @Parameter(description = "风格标签ID") 
            @PathVariable Integer tagId) {
        log.info("查询风格标签关联的衍生因子，tagId: {}", tagId);
        FactorManagementResponse response = factorManagementService.getDerivedFactorsByStyleTagId(tagId);
        if (response.getSuccess()) {
            return ResultDTO.success(response, "查询成功");
        } else {
            return ResultDTO.error(response.getMessage());
        }
    }
}


