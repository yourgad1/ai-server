package com.ai.server.agent.ai.strategy;

import com.ai.server.agent.ai.rest.request.RequestAi;

/**
 * 基于意图的聊天请求处理策略接口
 * 根据SystemAgent的意图识别结果选择不同的处理策略
 */
public interface IntentBasedStrategy {
    
    /**
     * 处理聊天请求
     * @param requestAi 请求参数
     * @param intent 识别出的意图
     * @throws Exception 处理异常
     */
    void handleRequest(RequestAi requestAi, String intent);
    
    /**
     * 判断是否支持当前意图
     * @param intent 识别出的意图
     * @param requestAi 请求参数
     * @return 是否支持该意图
     */
    boolean supports(String intent, RequestAi requestAi);
}