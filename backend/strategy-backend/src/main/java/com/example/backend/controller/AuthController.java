package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

/**
 * Strategy Backend 认证接口
 * 注意：主要的认证功能由 portfolio-backend 提供 (/api/auth)
 * 此 Controller 仅用于 strategy-backend 内部认证检查
 */
@RestController
@RequestMapping("/api/strategy-auth")
public class AuthController {

    /**
     * 获取当前登录用户信息（从 Session 中读取）
     * 依赖 Spring Session JDBC 实现跨服务 Session 共享
     */
    @GetMapping("/me")
    public ResponseEntity<?> currentUser(HttpSession session) {
        Object userId = session != null ? session.getAttribute("userId") : null;
        if (userId == null) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", "未登录");
            return ResponseEntity.status(401).body(resp);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("userId", userId);
        resp.put("username", session.getAttribute("username"));
        resp.put("role", session.getAttribute("role"));
        return ResponseEntity.ok(resp);
    }
}