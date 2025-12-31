package com.ai.server.agent.ai.config;

import com.ai.server.agent.ai.interceptor.UserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public  void configureAsyncSupport(AsyncSupportConfigurer configurer) {

        configurer.setDefaultTimeout(350000);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 可添加多个
        registry.addInterceptor(new UserInterceptor()).addPathPatterns("/**");
    }
}
