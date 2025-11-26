package com.example.backend.interceptor;

import com.example.backend.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Set<String> EXCLUDED_PATHS = Set.of("/api/test");

    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestPath = request.getRequestURI();
        if (EXCLUDED_PATHS.contains(requestPath)) {
            return true;
        }

        // 优先检查 Session（支持跨服务 Session 共享）
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userId = session.getAttribute("userId");
            Object username = session.getAttribute("username");
            Object role = session.getAttribute("role");

            // 如果 Session 中有用户信息，则认证通过
            if (userId != null && username != null && role != null) {
                return true;
            }
        }

        // 如果 Session 认证失败，再尝试 Token 认证（向后兼容）
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            unauthorized(response);
            return false;
        }

        String token = authHeader.substring(7);
        var tokenInfoOpt = tokenService.validate(token);
        if (tokenInfoOpt.isEmpty()) {
            unauthorized(response);
            return false;
        }

        // Token 认证成功，创建 Session 并存储用户信息
        TokenService.TokenInfo info = tokenInfoOpt.get();
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("userId", info.userId());
        newSession.setAttribute("username", info.username());
        newSession.setAttribute("role", info.role());
        return true;
    }

    private void unauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未授权或令牌失效\",\"data\":null}");
    }
}

