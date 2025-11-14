package com.example.train_back.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器（用于权限控制）
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许 OPTIONS 预检请求通过（CORS 预检）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        String requestPath = request.getRequestURI();
        
        // 排除的路径（这些路径不需要登录）
        String[] excludedPaths = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/check-username"
        };
        
        // 检查是否是排除的路径
        for (String excludedPath : excludedPaths) {
            if (requestPath.equals(excludedPath)) {
                return true; // 直接放行，不需要登录
            }
        }
        
        // 获取Session
        HttpSession session = request.getSession(false);
        
        // 检查是否已登录
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录，请先登录\",\"data\":null}");
            return false;
        }
        
        // 获取用户角色
        String role = (String) session.getAttribute("role");
        
        // 检查角色权限（根据接口路径判断需要的角色）
        
        // 审核相关接口需要审核人员角色
        if (requestPath.contains("/approve") || requestPath.contains("/reject")) {
            if (!"AUDITOR".equals(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足，需要审核人员权限\",\"data\":null}");
                return false;
            }
        }
        
        // 创建组合产品接口需要工作人员或审核人员角色
        if (requestPath.contains("/strategy-combination") && "POST".equals(request.getMethod())) {
            if (!"STAFF".equals(role) && !"AUDITOR".equals(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足，需要工作人员或审核人员权限\",\"data\":null}");
                return false;
            }
        }
        
        // 其他接口允许所有已登录用户访问
        return true;
    }
}

