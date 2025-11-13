package com.example.train_back.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 跨域配置
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 配置 CORS 跨域访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的源（前端地址）- Spring Boot 3.x 使用 allowedOriginPatterns
                .allowedOriginPatterns("http://localhost:5174", "http://localhost:3000", "http://127.0.0.1:5173")
                // 允许的 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许发送凭证（如 Cookie）
                .allowCredentials(true)
                // 预检请求的缓存时间（秒）
                .maxAge(3600);
    }
}

