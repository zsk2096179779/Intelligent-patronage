package com.example.train_back.controller;

import com.example.train_back.dto.LoginRequestDTO;
import com.example.train_back.dto.RegisterRequestDTO;
import com.example.train_back.dto.UserInfoResponseDTO;
import com.example.train_back.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证Controller（登录、注册、登出）
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户登录接口
     * @param loginRequest 登录请求
     * @param session HTTP会话
     * @return 响应结果
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequestDTO loginRequest, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 参数验证
            if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                response.put("code", 400);
                response.put("message", "用户名不能为空");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                response.put("code", 400);
                response.put("message", "密码不能为空");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 执行登录
            UserInfoResponseDTO userInfo = userService.login(loginRequest);
            
            // 将用户信息存储到Session中
            session.setAttribute("userId", userInfo.getId());
            session.setAttribute("username", userInfo.getUsername());
            session.setAttribute("role", userInfo.getRole());

            // 返回成功响应
            response.put("code", 200);
            response.put("message", "登录成功");
            response.put("data", userInfo);
            log.info("登录请求参数: {}", loginRequest);
            log.info("用户名: {}", loginRequest.getUsername());
            log.info("密码: {}", loginRequest.getPassword());



            return ResponseEntity.ok(response);


        } catch (RuntimeException e) {
            response.put("code", 401);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(401).body(response);


        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "登录失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 用户注册接口
     * @param registerRequest 注册请求
     * @return 响应结果
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequestDTO registerRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 参数验证
            if (registerRequest == null || registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                response.put("code", 400);
                response.put("message", "用户名不能为空");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
                response.put("code", 400);
                response.put("message", "密码不能为空");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 密码长度验证（建议至少6位）
            if (registerRequest.getPassword().length() < 6) {
                response.put("code", 400);
                response.put("message", "密码长度至少6位");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 执行注册
            UserInfoResponseDTO userInfo = userService.register(registerRequest);
            
            // 返回成功响应
            response.put("code", 200);
            response.put("message", "注册成功");
            response.put("data", userInfo);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "注册失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取当前登录用户信息接口
     * @param session HTTP会话
     * @return 响应结果
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 从Session中获取用户ID
            Integer userId = (Integer) session.getAttribute("userId");

            if (userId == null) {
                response.put("code", 401);
                response.put("message", "未登录");
                response.put("data", null);
                return ResponseEntity.status(401).body(response);
            }

            // 查询用户信息
            UserInfoResponseDTO userInfo = userService.getUserInfo(userId);

            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", userInfo);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("code", 404);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取用户信息失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取当前登录用户信息接口（别名，兼容策略运营模块）
     * @param session HTTP会话
     * @return 响应结果
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserMe(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 从Session中获取用户信息
            Integer userId = (Integer) session.getAttribute("userId");
            String username = (String) session.getAttribute("username");
            String role = (String) session.getAttribute("role");

            if (userId == null) {
                response.put("success", false);
                response.put("message", "未登录");
                return ResponseEntity.status(401).body(response);
            }

            // 返回成功响应（兼容策略运营模块的响应格式）
            response.put("success", true);
            response.put("userId", userId);
            response.put("username", username);
            response.put("role", role);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取用户信息失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 用户登出接口
     * @param session HTTP会话
     * @return 响应结果
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 使会话失效
            session.invalidate();
            
            response.put("code", 200);
            response.put("message", "登出成功");
            response.put("data", null);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "登出失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 检查用户名是否可用接口
     * @param username 用户名
     * @return 响应结果
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean exists = userService.isUsernameExists(username);
            response.put("code", 200);
            response.put("message", "检查完成");
            response.put("data", Map.of("available", !exists));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "检查失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}

