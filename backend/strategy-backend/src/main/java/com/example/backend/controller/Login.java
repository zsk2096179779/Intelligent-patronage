package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.service.TokenService;
import com.example.backend.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Token 登录接口（已废弃）
 * 注意：此接口仅用于 Token 认证，建议使用 portfolio-backend 的 /api/auth/login 进行 Session 认证
 */
@RestController
public class Login {
    @Autowired
    private UserService userServe;

    @Autowired
    private TokenService tokenService;

    /**
     * Token 登录接口（已废弃，建议使用 Session 认证）
     */
    @PostMapping("/api/test")
    public ResponseEntity<?> CheckUser(@RequestBody User user, HttpSession session) {
        String username = user.getUsername();
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("用户名不能为空");
        }
        username = username.trim();
        user = userServe.getUserByName(username);
        // 检查用户是否存在
        if (user == null) {
            return ResponseEntity.status(404).body("用户不存在");
        }
        // 生成访问令牌
        String token = tokenService.generateToken(user);

        // 返回成功响应
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("username", user.getUsername());
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
}