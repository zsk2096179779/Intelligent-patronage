package com.fengqi.fund.fundadvisor.service.factor.impl;

import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.entity.factor.StyleTag;
import com.fengqi.fund.fundadvisor.mapper.factor.StyleTagMapper;
import com.fengqi.fund.fundadvisor.service.factor.StyleTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 风格标签服务实现类
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StyleTagServiceImpl implements StyleTagService {
    
    private final StyleTagMapper styleTagMapper;
    
    @Override
    @Transactional
    public ResultDTO<StyleTag> createStyleTag(String tagName, String tagCode, String description, Integer createUserId) {
        try {
            // 1. 参数验证
            if (tagName == null || tagName.trim().isEmpty()) {
                return ResultDTO.error("风格标签名称不能为空");
            }
            
            if (tagCode == null || tagCode.trim().isEmpty()) {
                return ResultDTO.error("风格标签编码不能为空");
            }
            
            // 2. 验证标签名称唯一性
            StyleTag existingByName = styleTagMapper.selectStyleTagByName(tagName.trim());
            if (existingByName != null) {
                return ResultDTO.error("风格标签名称已存在: " + tagName);
            }
            
            // 3. 验证标签编码唯一性
            StyleTag existingByCode = styleTagMapper.selectStyleTagByCode(tagCode.trim());
            if (existingByCode != null) {
                return ResultDTO.error("风格标签编码已存在: " + tagCode);
            }
            
            // 4. 创建风格标签
            StyleTag styleTag = new StyleTag();
            styleTag.setTagName(tagName.trim());
            styleTag.setTagCode(tagCode.trim());
            styleTag.setDescription(description != null ? description.trim() : null);
            styleTag.setCreateUserId(createUserId);
            styleTag.setIsValid(1); // 默认为有效
            
            int result = styleTagMapper.insertStyleTag(styleTag);
            if (result > 0) {
                log.info("成功创建风格标签，tagId: {}, tagName: {}, tagCode: {}", 
                        styleTag.getTagId(), tagName, tagCode);
                return ResultDTO.success(styleTag, "风格标签创建成功");
            } else {
                return ResultDTO.error("风格标签创建失败");
            }
            
        } catch (Exception e) {
            log.error("创建风格标签失败", e);
            return ResultDTO.error("系统异常，创建失败: " + e.getMessage());
        }
    }
    
    @Override
    public ResultDTO<StyleTag> getStyleTagById(Integer tagId) {
        try {
            if (tagId == null) {
                return ResultDTO.error("风格标签ID不能为空");
            }
            
            StyleTag styleTag = styleTagMapper.selectStyleTagById(tagId);
            if (styleTag != null) {
                return ResultDTO.success(styleTag, "查询成功");
            } else {
                return ResultDTO.error("风格标签不存在");
            }
        } catch (Exception e) {
            log.error("查询风格标签失败", e);
            return ResultDTO.error("查询失败: " + e.getMessage());
        }
    }
    
    @Override
    public ResultDTO<StyleTag> getStyleTagByCode(String tagCode) {
        try {
            if (tagCode == null || tagCode.trim().isEmpty()) {
                return ResultDTO.error("风格标签编码不能为空");
            }
            
            StyleTag styleTag = styleTagMapper.selectStyleTagByCode(tagCode.trim());
            if (styleTag != null) {
                return ResultDTO.success(styleTag, "查询成功");
            } else {
                return ResultDTO.error("风格标签不存在");
            }
        } catch (Exception e) {
            log.error("根据编码查询风格标签失败", e);
            return ResultDTO.error("查询失败: " + e.getMessage());
        }
    }
    
    @Override
    public ResultDTO<List<StyleTag>> getAllStyleTags() {
        try {
            List<StyleTag> styleTags = styleTagMapper.selectAllValidStyleTags();
            return ResultDTO.success(styleTags, "查询成功");
        } catch (Exception e) {
            log.error("查询所有风格标签失败", e);
            return ResultDTO.error("查询失败: " + e.getMessage());
        }
    }
    
    @Override
    public ResultDTO<List<StyleTag>> getStyleTagsByIds(List<Integer> tagIds) {
        try {
            if (tagIds == null || tagIds.isEmpty()) {
                return ResultDTO.error("标签ID列表不能为空");
            }
            
            List<StyleTag> styleTags = styleTagMapper.selectStyleTagsByIds(tagIds);
            return ResultDTO.success(styleTags, "查询成功");
        } catch (Exception e) {
            log.error("根据ID列表查询风格标签失败", e);
            return ResultDTO.error("查询失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ResultDTO<StyleTag> updateStyleTag(Integer tagId, String tagName, String tagCode, String description) {
        try {
            // 1. 参数验证
            if (tagId == null) {
                return ResultDTO.error("风格标签ID不能为空");
            }
            
            // 2. 检查标签是否存在
            StyleTag existing = styleTagMapper.selectStyleTagById(tagId);
            if (existing == null) {
                return ResultDTO.error("风格标签不存在");
            }
            
            // 3. 验证标签名称唯一性（排除当前标签）
            if (tagName != null && !tagName.trim().isEmpty()) {
                int nameCount = styleTagMapper.countByTagName(tagName.trim(), tagId);
                if (nameCount > 0) {
                    return ResultDTO.error("风格标签名称已存在: " + tagName);
                }
            }
            
            // 4. 验证标签编码唯一性（排除当前标签）
            if (tagCode != null && !tagCode.trim().isEmpty()) {
                int codeCount = styleTagMapper.countByTagCode(tagCode.trim(), tagId);
                if (codeCount > 0) {
                    return ResultDTO.error("风格标签编码已存在: " + tagCode);
                }
            }
            
            // 5. 更新标签信息
            if (tagName != null && !tagName.trim().isEmpty()) {
                existing.setTagName(tagName.trim());
            }
            if (tagCode != null && !tagCode.trim().isEmpty()) {
                existing.setTagCode(tagCode.trim());
            }
            if (description != null) {
                existing.setDescription(description.trim());
            }
            
            int result = styleTagMapper.updateStyleTag(existing);
            if (result > 0) {
                log.info("成功更新风格标签，tagId: {}", tagId);
                // 重新查询最新的标签信息
                StyleTag updated = styleTagMapper.selectStyleTagById(tagId);
                return ResultDTO.success(updated, "风格标签更新成功");
            } else {
                return ResultDTO.error("风格标签更新失败");
            }
            
        } catch (Exception e) {
            log.error("更新风格标签失败", e);
            return ResultDTO.error("更新失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ResultDTO<Boolean> deleteStyleTag(Integer tagId) {
        try {
            if (tagId == null) {
                return ResultDTO.error("风格标签ID不能为空");
            }
            
            // 检查标签是否存在
            StyleTag existing = styleTagMapper.selectStyleTagById(tagId);
            if (existing == null) {
                return ResultDTO.error("风格标签不存在");
            }
            
            // 执行软删除
            int result = styleTagMapper.deleteStyleTag(tagId);
            if (result > 0) {
                log.info("成功删除风格标签，tagId: {}", tagId);
                return ResultDTO.success(true, "风格标签删除成功");
            } else {
                return ResultDTO.error("风格标签删除失败");
            }
            
        } catch (Exception e) {
            log.error("删除风格标签失败", e);
            return ResultDTO.error("删除失败: " + e.getMessage());
        }
    }
    
    @Override
    public ResultDTO<List<StyleTag>> searchStyleTags(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                // 如果关键词为空，返回所有标签
                return getAllStyleTags();
            }
            
            List<StyleTag> styleTags = styleTagMapper.searchStyleTags(keyword.trim());
            return ResultDTO.success(styleTags, "搜索成功");
        } catch (Exception e) {
            log.error("搜索风格标签失败", e);
            return ResultDTO.error("搜索失败: " + e.getMessage());
        }
    }
}


