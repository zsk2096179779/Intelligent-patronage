package com.example.train_back.mapper;

import com.example.train_back.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User selectByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户对象
     */
    User selectByEmail(@Param("email") String email);
    
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    User selectById(@Param("id") Integer id);
    
    /**
     * 插入新用户
     * @param user 用户对象（插入后会自动设置生成的ID）
     * @return 插入的行数
     */
    int insertUser(User user);
}

