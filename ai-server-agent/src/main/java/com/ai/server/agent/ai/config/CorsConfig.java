package com.ai.server.agent.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // 全局跨域配置
                registry.addMapping("/**")
                        .allowedOriginPatterns("*") // 允许所有域名，生产环境应指定具体域名
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization", "Content-Disposition") // 暴露自定义头信息
                        .allowCredentials(true) // 允许携带凭证（如cookies）
                        .maxAge(3600); // 预检请求缓存时间（秒）
            }
        };
    }
}