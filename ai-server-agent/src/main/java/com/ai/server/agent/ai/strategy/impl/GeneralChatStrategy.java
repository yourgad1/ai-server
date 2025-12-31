package com.ai.server.agent.ai.strategy.impl;

import com.ai.server.agent.ai.rest.request.RequestAi;
import com.ai.server.agent.ai.rest.response.ResponseAi;
import com.ai.server.agent.ai.agent.core.Agent;
import com.ai.server.agent.ai.agent.factory.AgentFactory;
import com.ai.server.agent.ai.common.sse.AiGlobalSseEmitterManager;
import com.ai.server.agent.ai.constant.IntentConstant;
import com.ai.server.agent.ai.strategy.IntentBasedStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 通用聊天策略实现
 * 处理通用聊天意图的请求，不涉及指标查询或文件处理
 */
@Component
@Slf4j
public class GeneralChatStrategy implements IntentBasedStrategy {

    @Autowired
    private AgentFactory agentFactory;
    @Autowired
    private AiGlobalSseEmitterManager sseEmitterManager;


    @Override
    public void handleRequest(RequestAi requestAi, String intent) {
        try {
            sseEmitterManager.sendEvent(requestAi.getConnId(), ResponseAi.ofLog("执行通用任务\n"));
            // 进行通用聊天
            Agent<Object> simpleChatClient = agentFactory.createAgentByName("simpleChatClient");
            
            // 创建上下文
            java.util.Map<String, Object> context = new java.util.HashMap<>();
            
            Agent.Request request = Agent.ChatRequest.builder()
                    .connId(requestAi.getConnId())
                    .message(requestAi.getMessage())
                    .context(context)
                    .sessionId(requestAi.getSessionId())
                    .build();
            simpleChatClient.chatStream(request);
        } catch (Exception e) {
            log.error("通用聊天处理失败", e);
            try {
                sseEmitterManager.sendEvent(requestAi.getConnId(), ResponseAi.ofLog("聊天处理失败：" + e.getMessage()));
            } catch (Exception ex) {
                log.error("发送错误信息失败", ex);
            }
        }
    }

    @Override
    public boolean supports(String intent, RequestAi requestAi) {
        // 当没有明确意图时，作为默认策略
        return intent == null ||
                intent.contains(IntentConstant.OTHER
                );
    }
}