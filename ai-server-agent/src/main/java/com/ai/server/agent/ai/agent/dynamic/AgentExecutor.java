package com.ai.server.agent.ai.agent.dynamic;

import com.ai.server.agent.ai.agent.core.Agent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Agent执行器，提供统一的任务执行接口
 */
@Component
@Slf4j
public class AgentExecutor {
    
    @Autowired
    private DynamicAgentManager agentManager;
    
    @Autowired
    private AgentTaskStatusManager taskStatusManager;
    
    /**
     * 执行Agent任务
     * @param agentName Agent名称
     * @param request Agent请求
     * @param type 请求类型
     * @param <T> 返回结果类型
     * @return 执行结果
     */
    @SuppressWarnings("unchecked")
    public <T> T execute(String agentName, Agent.Request request, String type) {
        log.info("Executing agent task: agentName={}, type={}", agentName, type);
        
        // 记录任务开始
        taskStatusManager.incrementTaskCount(agentName);
        
        // 获取Agent实例
        Agent<T> agent = agentManager.getAgent(agentName);
        if (agent == null) {
            log.error("Agent not found: {}", agentName);
            taskStatusManager.decrementTaskCount(agentName);
            throw new IllegalArgumentException("Agent not found: " + agentName);
        }
        
        try {
            // 执行任务
            return agent.chat(request, type);
        } catch (Exception e) {
            log.error("Failed to execute agent task: agentName={}, type={}", agentName, type, e);
            throw new RuntimeException("Failed to execute agent task: " + e.getMessage(), e);
        } finally {
            // 记录任务完成
            taskStatusManager.decrementTaskCount(agentName);
        }
    }
    
    /**
     * 执行Agent聊天任务
     * @param agentName Agent名称
     * @param message 聊天消息
     * @param context 上下文
     * @param <T> 返回结果类型
     * @return 执行结果
     */
    public <T> T executeChat(String agentName, String message, Map<String, Object> context) {
        // 创建聊天请求
        Agent.ChatRequest request = Agent.ChatRequest.builder()
                .message(message)
                .context(context)
                .build();
        
        return execute(agentName, request, "chat");
    }
    
    /**
     * 执行Agent聊天任务（无上下文）
     * @param agentName Agent名称
     * @param message 聊天消息
     * @param <T> 返回结果类型
     * @return 执行结果
     */
    public <T> T executeChat(String agentName, String message) {
        return executeChat(agentName, message, null);
    }
    
    /**
     * 执行Agent多请求任务
     * @param agentName Agent名称
     * @param userMessage 用户消息对象
     * @param context 上下文
     * @param <T> 返回结果类型
     * @return 执行结果
     */
    public <T> T executeMulti(String agentName, org.springframework.ai.chat.messages.UserMessage userMessage, Map<String, Object> context) {
        // 创建多请求
        Agent.MultiRequest request = Agent.MultiRequest.builder()
                .message(userMessage)
                .context(context)
                .build();
        
        return execute(agentName, request, "multi");
    }
    
    /**
     * 执行Agent多请求任务（无上下文）
     * @param agentName Agent名称
     * @param userMessage 用户消息对象
     * @param <T> 返回结果类型
     * @return 执行结果
     */
    public <T> T executeMulti(String agentName, org.springframework.ai.chat.messages.UserMessage userMessage) {
        return executeMulti(agentName, userMessage, null);
    }
    
    /**
     * 执行Agent流式聊天任务
     * @param agentName Agent名称
     * @param request Agent请求
     * @throws Exception 执行异常
     */
    public void executeStream(String agentName, Agent.Request request) throws Exception {
        log.info("Executing agent stream task: agentName={}", agentName);
        
        // 记录任务开始
        taskStatusManager.incrementTaskCount(agentName);
        
        // 获取Agent实例
        Agent<?> agent = agentManager.getAgent(agentName);
        if (agent == null) {
            log.error("Agent not found: {}", agentName);
            taskStatusManager.decrementTaskCount(agentName);
            throw new IllegalArgumentException("Agent not found: " + agentName);
        }
        
        try {
            // 执行流式任务
            agent.chatStream(request);
        } catch (Exception e) {
            log.error("Failed to execute agent stream task: agentName={}", agentName, e);
            throw e;
        } finally {
            // 记录任务完成
            taskStatusManager.decrementTaskCount(agentName);
        }
    }
    
    /**
     * 执行Agent流式聊天任务
     * @param agentName Agent名称
     * @param message 聊天消息
     * @param context 上下文
     * @throws Exception 执行异常
     */
    public void executeChatStream(String agentName, String message, Map<String, Object> context) throws Exception {
        // 创建聊天请求
        Agent.ChatRequest request = Agent.ChatRequest.builder()
                .message(message)
                .context(context)
                .isStreaming(true)
                .build();
        
        executeStream(agentName, request);
    }
    
    /**
     * 执行Agent流式聊天任务（无上下文）
     * @param agentName Agent名称
     * @param message 聊天消息
     * @throws Exception 执行异常
     */
    public void executeChatStream(String agentName, String message) throws Exception {
        executeChatStream(agentName, message, null);
    }
    
    /**
     * 检查Agent是否存在
     * @param agentName Agent名称
     * @return 是否存在
     */
    public boolean exists(String agentName) {
        return agentManager.getAgent(agentName) != null;
    }
    
    /**
     * 获取所有可用Agent名称
     * @return Agent名称集合
     */
    public java.util.Set<String> getAllAgentNames() {
        return agentManager.getAllAgentNames();
    }
}
