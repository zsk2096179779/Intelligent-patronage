package com.fengqi.fund.fundadvisor.mapper.factor;

import com.fengqi.fund.fundadvisor.entity.factor.FactorTreeScene;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FactorTreeSceneMapper {
    
    /**
     * 获取所有场景
     */
    @Select("SELECT * FROM factor_tree_scene ORDER BY scene_id")
    List<FactorTreeScene> getAllScenes();
    
    /**
     * 根据场景ID获取场景
     */
    @Select("SELECT * FROM factor_tree_scene WHERE scene_id = #{sceneId}")
    FactorTreeScene getSceneById(@Param("sceneId") String sceneId);
    
    /**
     * 创建场景
     */
    @Insert("INSERT INTO factor_tree_scene (scene_id, scene_name, scene_desc) " +
            "VALUES (#{sceneId}, #{sceneName}, #{sceneDesc})")
    int createScene(FactorTreeScene scene);
    
    /**
     * 更新场景
     */
    @Update("UPDATE factor_tree_scene SET scene_name = #{sceneName}, scene_desc = #{sceneDesc} " +
            "WHERE scene_id = #{sceneId}")
    int updateScene(FactorTreeScene scene);
    
    /**
     * 删除场景
     */
    @Delete("DELETE FROM factor_tree_scene WHERE scene_id = #{sceneId}")
    int deleteScene(@Param("sceneId") String sceneId);
}