package com.fengqi.fund.fundadvisor.service.factor;

import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.entity.factor.StyleTag;

import java.util.List;

/**
 * 风格标签服务接口
 * 
 * 提供风格标签的CRUD操作
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
public interface StyleTagService {
    
    /**
     * 创建风格标签
     * 
     * @param tagName 标签名称
     * @param tagCode 标签编码
     * @param description 标签描述
     * @param createUserId 创建人ID
     * @return 创建结果
     */
    ResultDTO<StyleTag> createStyleTag(String tagName, String tagCode, String description, Integer createUserId);
    
    /**
     * 根据ID查询风格标签
     * 
     * @param tagId 标签ID
     * @return 标签信息
     */
    ResultDTO<StyleTag> getStyleTagById(Integer tagId);
    
    /**
     * 根据编码查询风格标签
     * 
     * @param tagCode 标签编码
     * @return 标签信息
     */
    ResultDTO<StyleTag> getStyleTagByCode(String tagCode);
    
    /**
     * 查询所有有效的风格标签
     * 
     * @return 标签列表
     */
    ResultDTO<List<StyleTag>> getAllStyleTags();
    
    /**
     * 根据ID列表查询风格标签
     * 
     * @param tagIds 标签ID列表
     * @return 标签列表
     */
    ResultDTO<List<StyleTag>> getStyleTagsByIds(List<Integer> tagIds);
    
    /**
     * 更新风格标签
     * 
     * @param tagId 标签ID
     * @param tagName 标签名称
     * @param tagCode 标签编码
     * @param description 标签描述
     * @return 更新结果
     */
    ResultDTO<StyleTag> updateStyleTag(Integer tagId, String tagName, String tagCode, String description);
    
    /**
     * 删除风格标签（软删除）
     * 
     * @param tagId 标签ID
     * @return 删除结果
     */
    ResultDTO<Boolean> deleteStyleTag(Integer tagId);
    
    /**
     * 搜索风格标签
     * 
     * @param keyword 搜索关键词（名称或编码）
     * @return 标签列表
     */
    ResultDTO<List<StyleTag>> searchStyleTags(String keyword);
}


