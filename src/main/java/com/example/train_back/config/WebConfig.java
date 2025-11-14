package com.example.train_back.config;

import com.example.train_back.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类（注册拦截器）
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private AuthInterceptor authInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**") // 拦截所有 /api/** 路径
                .excludePathPatterns(
                        "/api/auth/login",      // 排除登录接口
                        "/api/auth/register",  // 排除注册接口
                        "/api/auth/check-username" // 排除检查用户名接口
                );
    }
}

