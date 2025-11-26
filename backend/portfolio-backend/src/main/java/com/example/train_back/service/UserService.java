package com.example.train_back.service;

import com.example.train_back.dto.LoginRequestDTO;
import com.example.train_back.dto.RegisterRequestDTO;
import com.example.train_back.dto.UserInfoResponseDTO;

/**
 * 用户Service接口
 */
public interface UserService {
    
    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 用户信息（不包含密码）
     */
    UserInfoResponseDTO login(LoginRequestDTO loginRequest);
    
    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 用户信息（不包含密码）
     */
    UserInfoResponseDTO register(RegisterRequestDTO registerRequest);
    
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息（不包含密码）
     */
    UserInfoResponseDTO getUserInfo(Integer userId);
    
    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return true-已存在，false-不存在
     */
    boolean isUsernameExists(String username);
    
    /**
     * 检查邮箱是否已存在
     * @param email 邮箱
     * @return true-已存在，false-不存在
     */
    boolean isEmailExists(String email);
}

