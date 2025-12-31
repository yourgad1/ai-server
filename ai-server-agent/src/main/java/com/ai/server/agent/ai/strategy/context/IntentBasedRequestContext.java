package com.ai.server.agent.ai.strategy.context;


import com.ai.server.agent.ai.rest.request.RequestAi;
import com.ai.server.agent.ai.rest.response.ResponseAi;
import com.ai.server.agent.ai.util.ThinkContentUtil;
import com.ai.server.agent.ai.agent.constant.AgentType;
import com.ai.server.agent.ai.agent.core.Agent;
import com.ai.server.agent.ai.agent.factory.AgentFactory;

import com.ai.server.agent.ai.common.sse.AiGlobalSseEmitterManager;
import com.ai.server.agent.ai.constant.AgentTypeConstant;
import com.ai.server.agent.ai.interceptor.UserContextHolder;
import com.ai.server.agent.ai.strategy.IntentBasedStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


/**
 * 基于意图的聊天请求处理策略上下文
 * 根据SystemAgent的意图识别结果选择合适的策略处理请求
 */
@Component
@Slf4j
public class IntentBasedRequestContext {

    @Autowired
    private List<IntentBasedStrategy> strategies;

    @Autowired
    private AgentFactory agentFactory;
    @Autowired
    private AiGlobalSseEmitterManager sseEmitterManager;

    /**
     * 处理聊天请求
     *
     * @param requestAi 请求参数
     * @throws Exception 处理异常
     */
    public void handleRequest(RequestAi requestAi) {
        // 记录用户消息到数据库，但不发送到前端
        String connId = requestAi.getConnId();
        String message = requestAi.getMessage();
        String sessionId = requestAi.getSessionId();
        
        // 确保会话ID与会话ID的映射关系存在
        try {
            // 调用SSE管理器的createOrUpdateSession方法更新映射关系
            // 使用反射调用，因为该方法是private的
            java.lang.reflect.Method sessionMethod = AiGlobalSseEmitterManager.class.getDeclaredMethod("createOrUpdateSession", String.class, String.class, String.class);
            sessionMethod.setAccessible(true);
            // 调用userContextHolder的getUserId方法获取用户ID
            String userId = UserContextHolder.getInstance().getUserId();
            userId = userId != null ? userId : "test";
            sessionMethod.invoke(sseEmitterManager, sessionId, userId, connId);
        } catch (Exception e) {
            log.warn("更新会话信息失败：{}", e.getMessage());
        }
        
        // 创建ResponseAi对象用于数据库记录
        ResponseAi userMessage = ResponseAi.ofUserMessage(message);
        
        // 调用SSE管理器的saveSseMessageToDatabase方法记录消息到数据库
        // 使用反射调用，因为该方法是private的
        try {
            java.lang.reflect.Method saveMethod = AiGlobalSseEmitterManager.class.getDeclaredMethod("saveSseMessageToDatabase", String.class, String.class, Object.class);
            saveMethod.setAccessible(true);
            saveMethod.invoke(sseEmitterManager, connId, "user", userMessage);
        } catch (Exception e) {
            log.warn("记录用户消息到数据库失败：{}", e.getMessage());
        }
        
        sseEmitterManager.sendEvent(requestAi.getConnId(), ResponseAi.ofLog("意图识别："));
        // 使用SystemAgent进行意图识别
        String intent = determineIntent(requestAi);
        // 根据意图选择合适的策略
        IntentBasedStrategy strategy = selectStrategy(intent, requestAi);
        if (strategy != null) {
            log.info("选择策略：{}", strategy.getClass().getSimpleName());
            strategy.handleRequest(requestAi, intent);
        } else {
            log.warn("未找到合适的策略处理意图：{}", intent);
            sseEmitterManager.sendEvent(requestAi.getConnId(), ResponseAi.ofError("未找到合适的处理策略"));
        }

    }

    /**
     * 使用SystemAgent进行意图识别
     */
    private String determineIntent(RequestAi requestAi) {
        // 构建意图识别请求
        Map<String, Object> context = new HashMap<>();
        
        Agent.ChatRequest intentRequest = Agent.ChatRequest.builder()
                .message(requestAi.getMessage())
                .context(context)
                .sessionId(requestAi.getSessionId())
                .build();
        // 调用SystemAgent的determineIntent客户端进行意图识别
        // 通过AgentFactory获取SystemAgent实例
        Agent<String> systemAgent = agentFactory.createAgent(AgentType.SYSTEM);
        String intent = systemAgent.chat(intentRequest, AgentTypeConstant.SYS_DETERMIN_INTENT);
        log.info("意图识别结果：{}", intent);
        String s = ThinkContentUtil.removeBeforeThink(intent);
        return s.trim();
    }

    /**
     * 根据意图选择合适的策略
     */
    private IntentBasedStrategy selectStrategy(String intent, RequestAi requestAi) {
        for (IntentBasedStrategy strategy : strategies) {
            if (strategy.supports(intent, requestAi)) {
                return strategy;
            }
        }
        return null;
    }
}