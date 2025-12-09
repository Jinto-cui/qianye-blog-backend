package com.qianye.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author: Jinto Cui
 * @desc: Web相关配置“：
 *      1.增加跨域配置
 * @date: 2025/12/9 23:00
 * @version: v1.0
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/rest/v1/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*") // 允许所有头部，确保requestId能放入到响应头
                .allowCredentials(true)
                .maxAge(3600);
    }
}