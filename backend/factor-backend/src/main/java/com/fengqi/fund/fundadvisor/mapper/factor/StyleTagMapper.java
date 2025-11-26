package com.fengqi.fund.fundadvisor.mapper.factor;

import com.fengqi.fund.fundadvisor.entity.factor.StyleTag;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 风格标签 Mapper 接口
 * 
 * 提供风格标签的数据库操作
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Mapper
public interface StyleTagMapper {
    
    /**
     * 创建风格标签
     */
    @Insert("INSERT INTO style_tag (tag_name, tag_code, description, create_user_id, is_valid, create_time) " +
            "VALUES (#{tagName}, #{tagCode}, #{description}, #{createUserId}, #{isValid}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "tagId")
    int insertStyleTag(StyleTag styleTag);
    
    /**
     * 根据ID查询风格标签
     */
    @Select("SELECT * FROM style_tag WHERE tag_id = #{tagId} AND is_valid = 1")
    StyleTag selectStyleTagById(@Param("tagId") Integer tagId);
    
    /**
     * 根据编码查询风格标签
     */
    @Select("SELECT * FROM style_tag WHERE tag_code = #{tagCode} AND is_valid = 1")
    StyleTag selectStyleTagByCode(@Param("tagCode") String tagCode);
    
    /**
     * 根据名称查询风格标签
     */
    @Select("SELECT * FROM style_tag WHERE tag_name = #{tagName} AND is_valid = 1")
    StyleTag selectStyleTagByName(@Param("tagName") String tagName);
    
    /**
     * 查询所有有效的风格标签
     */
    @Select("SELECT * FROM style_tag WHERE is_valid = 1 ORDER BY create_time DESC")
    List<StyleTag> selectAllValidStyleTags();
    
    /**
     * 根据ID列表查询风格标签
     */
    @Select("<script>" +
            "SELECT * FROM style_tag WHERE tag_id IN " +
            "<foreach collection='tagIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND is_valid = 1 ORDER BY create_time DESC" +
            "</script>")
    List<StyleTag> selectStyleTagsByIds(@Param("tagIds") List<Integer> tagIds);
    
    /**
     * 更新风格标签
     */
    @Update("UPDATE style_tag SET tag_name = #{tagName}, tag_code = #{tagCode}, " +
            "description = #{description} WHERE tag_id = #{tagId} AND is_valid = 1")
    int updateStyleTag(StyleTag styleTag);
    
    /**
     * 删除风格标签（软删除）
     */
    @Update("UPDATE style_tag SET is_valid = 0 WHERE tag_id = #{tagId}")
    int deleteStyleTag(@Param("tagId") Integer tagId);
    
    /**
     * 检查标签编码是否存在（排除指定ID）
     */
    @Select("SELECT COUNT(*) FROM style_tag WHERE tag_code = #{tagCode} AND is_valid = 1 " +
            "AND (#{excludeTagId} IS NULL OR tag_id != #{excludeTagId})")
    int countByTagCode(@Param("tagCode") String tagCode, @Param("excludeTagId") Integer excludeTagId);
    
    /**
     * 检查标签名称是否存在（排除指定ID）
     */
    @Select("SELECT COUNT(*) FROM style_tag WHERE tag_name = #{tagName} AND is_valid = 1 " +
            "AND (#{excludeTagId} IS NULL OR tag_id != #{excludeTagId})")
    int countByTagName(@Param("tagName") String tagName, @Param("excludeTagId") Integer excludeTagId);
    
    /**
     * 搜索风格标签（根据名称或编码模糊查询）
     */
    @Select("SELECT * FROM style_tag WHERE is_valid = 1 " +
            "AND (tag_name LIKE CONCAT('%', #{keyword}, '%') OR tag_code LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY create_time DESC")
    List<StyleTag> searchStyleTags(@Param("keyword") String keyword);
}


