package com.fengqi.fund.fundadvisor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * 跨域全局配置类
 * 
 * @author fengqi
 * @since 2025-11-21
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许所有路径
                .allowedOriginPatterns(
                    "http://localhost:5173",      // 前端开发端口 - client-app
                    "http://localhost:5174",      // 前端开发端口 - 备用
                    "http://localhost:8081",      // strategy-console 开发端口
                    "http://127.0.0.1:5173",
                    "http://127.0.0.1:5174"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // 允许的HTTP方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true) // 允许发送Cookie
                .maxAge(3600) // 预检请求的缓存时间（秒）
                .exposedHeaders("Content-Disposition", "Authorization"); // 暴露的响应头
    }

    /**
     * 自定义 CORS 配置源
     * 提供更细粒度的 CORS 配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:8081",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:5174"
        ));

        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));

        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Content-Disposition", "Authorization", "X-Total-Count"
        ));

        // 是否允许发送Cookie
        configuration.setAllowCredentials(true);

        // 预检请求缓存时间
        configuration.setMaxAge(3600L);

        // 将配置应用到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}