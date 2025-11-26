package com.example.train_back.service.impl;

import com.example.train_back.dto.LoginRequestDTO;
import com.example.train_back.dto.RegisterRequestDTO;
import com.example.train_back.dto.UserInfoResponseDTO;
import com.example.train_back.entity.User;
import com.example.train_back.mapper.UserMapper;
import com.example.train_back.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户Service实现类
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public UserInfoResponseDTO login(LoginRequestDTO loginRequest) {
        // 根据用户名查询用户
        User user = userMapper.selectByUsername(loginRequest.getUsername());
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }
        
        // 验证密码（明文比较）
        if (!loginRequest.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 转换为响应DTO
        return convertToUserInfoResponse(user);
    }
    
    @Override
    @Transactional
    public UserInfoResponseDTO register(RegisterRequestDTO registerRequest) {
        // 检查用户名是否已存在
        if (isUsernameExists(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在（如果提供了邮箱）
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().isEmpty()) {
            if (isEmailExists(registerRequest.getEmail())) {
                throw new RuntimeException("邮箱已被注册");
            }
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        // 直接存储明文密码（不加密）
        user.setPassword(registerRequest.getPassword());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setRole("USER"); // 默认角色为普通用户
        user.setStatus(1); // 默认状态为正常
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // 插入数据库
        int rows = userMapper.insertUser(user);
        if (rows <= 0) {
            throw new RuntimeException("注册失败");
        }
        
        // 返回用户信息
        return convertToUserInfoResponse(user);
    }
    
    @Override
    public UserInfoResponseDTO getUserInfo(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToUserInfoResponse(user);
    }
    
    @Override
    public boolean isUsernameExists(String username) {
        User user = userMapper.selectByUsername(username);
        return user != null;
    }
    
    @Override
    public boolean isEmailExists(String email) {
        User user = userMapper.selectByEmail(email);
        return user != null;
    }
    
    /**
     * 将User实体转换为UserInfoResponseDTO
     */
    private UserInfoResponseDTO convertToUserInfoResponse(User user) {
        UserInfoResponseDTO dto = new UserInfoResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        return dto;
    }
}

