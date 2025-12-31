package com.ai.server.agent.ai.interceptor;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.shaded.javax.annotation.Nullable;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

public class UserInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(UserInterceptor.class);
    
    public static final String X_CLIENT_TOKEN_USER = "x-client-token-user";
    
    public static final String X_CLIENT_TOKEN = "x-client-token";
    
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        log.info("拦截器开始执行");
        checkToken(request.getHeader("x-client-token"));
        String userInfoString = defaultIfBlank(request.getHeader("x-client-token-user"), "{}");
        UserContextHolder.getInstance().setContextStr(request.getHeader("x-client-token-user"));
        UserContextHolder.getInstance().setContext((Map)new ObjectMapper().readValue(userInfoString, Map.class));
        return true;
    }
    
    private void checkToken(String token) {
        log.debug("校验token:{}", token);
    }
    
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
        @Nullable Exception ex)
        throws Exception {
        UserContextHolder.getInstance().clear();
    }
    public static String defaultIfBlank(String str, String defaultStr) {
        return StringUtils.isBlank(str) ? defaultStr : str;
    }
}