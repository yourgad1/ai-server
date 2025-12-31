package com.ai.server.agent.ai.agent.template;

import com.ai.server.agent.ai.agent.manager.PromptManager;
import com.ai.server.agent.ai.common.sse.AiGlobalSseEmitterManager;
import com.ai.server.agent.ai.agent.core.Agent;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent抽象基类，实现公共逻辑
 * @param <T>
 */
@Slf4j
@Repository
public abstract class BaseAgent<T> implements Agent<T> {

    protected ChatClient chatClient;
    
    @Autowired
    protected AiGlobalSseEmitterManager sseEmitterManager;
    
    /**
     * -- SETTER --
     *  设置默认提示词变量
     *
     * @param defaultPromptVariables 默认提示词变量
     */
    @Setter
    protected Map<String, Object> defaultPromptVariables = new ConcurrentHashMap<>();
    protected String systemPromptType;
    protected PromptManager promptManager;
    /**
     * -- SETTER --
     *  设置ChatMemory
     *
     * @param chatMemory ChatMemory
     */
    @Setter
    protected ChatMemory chatMemory;
    
    /**
     * 工具列表
     */
    @Setter
    protected List<Object> tools = new ArrayList<>();
    
    /**
     * 获取工具列表
     * @return 工具列表
     */
    public List<Object> getTools() {
        return tools;
    }

    /**
     * 构造方法，使用ChatClient创建Agent
     * @param chatClient ChatClient实例
     */
    public BaseAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 构造方法，使用ChatClient和系统提示词类型创建Agent
     * @param chatClient ChatClient实例
     * @param systemPromptType 系统提示词类型
     */
    public BaseAgent(ChatClient chatClient, String systemPromptType) {
        this.chatClient = chatClient;
        this.systemPromptType = systemPromptType;
    }

    /**
     * 构造方法，使用ChatClient、系统提示词类型和PromptManager创建Agent
     * @param chatClient ChatClient实例
     * @param systemPromptType 系统提示词类型
     * @param promptManager 提示词管理器
     */
    public BaseAgent(ChatClient chatClient, String systemPromptType, PromptManager promptManager) {
        this.chatClient = chatClient;
        this.systemPromptType = systemPromptType;
        this.promptManager = promptManager;
    }

    /**
     * 公共chat方法实现，封装请求校验和上下文处理
     * @param request 请求对象
     * @param type 请求类型
     * @return 响应结果
     */
    @Override
    public T chat(Request request, String type) {
        // 公共逻辑：参数校验
        if (request == null) {
            log.warn("Request is null");
            return null;
        }

        // 公共逻辑：上下文处理
        if (request.getContext() == null) {
            request.setContext(new ConcurrentHashMap<>());
        }

        // 合并默认提示词变量到请求上下文，只添加request中没有的键
        for (Map.Entry<String, Object> entry : defaultPromptVariables.entrySet()) {
            if (!request.getContext().containsKey(entry.getKey())) {
                request.getContext().put(entry.getKey(), entry.getValue());
            }
        }

        // 调用具体Agent的doChat方法处理业务逻辑
        return doChat(request, type);
    }

    /**
     * 公共chatStream方法实现，封装请求校验和上下文处理
     * @param request 请求对象
     * @throws Exception 异常信息
     */
    @Override
    public void chatStream(Request request) throws Exception {
        // 公共逻辑：参数校验
        if (request == null) {
            log.warn("Request is null");
            return;
        }

        // 公共逻辑：上下文处理
        if (request.getContext() == null) {
            request.setContext(new ConcurrentHashMap<>());
        }

        // 合并默认提示词变量到请求上下文，只添加request中没有的键
        for (Map.Entry<String, Object> entry : defaultPromptVariables.entrySet()) {
            if (!request.getContext().containsKey(entry.getKey())) {
                request.getContext().put(entry.getKey(), entry.getValue());
            }
        }

        // 调用具体Agent的doChatStream方法处理业务逻辑
        doChatStream(request);
    }

    /**
     * 使用PromptManager创建提示词
     * @param promptType 提示词类型
     * @param variables 模板变量
     * @return 提示词实例
     */
    protected Prompt createPrompt(String promptType, Map<String, Object> variables) {
        if (promptManager == null) {
            throw new IllegalStateException("PromptManager is not initialized");
        }
        return promptManager.createPrompt(promptType, variables);
    }

    /**
     * 获取提示词模板
     * @param promptType 提示词类型
     * @return 提示词模板
     */
    protected PromptTemplate getPromptTemplate(String promptType) {
        if (promptManager == null) {
            throw new IllegalStateException("PromptManager is not initialized");
        }
        return promptManager.getPromptTemplate(promptType);
    }

    /**
     * 抽象方法，由具体Agent实现特定的chat业务逻辑
     * @param request 请求对象
     * @param type 请求类型
     * @return 响应结果
     */
    protected abstract T doChat(Agent.Request request, String type);

    /**
     * 抽象方法，由具体Agent实现特定的chatStream业务逻辑
     * @param request 请求对象
     * @throws Exception 异常信息
     */
    protected abstract void doChatStream(Agent.Request request) throws Exception;

    /**
     * 添加默认提示词变量
     * @param key 键
     * @param value 值
     */
    public void addDefaultPromptVariable(String key, Object value) {
        this.defaultPromptVariables.put(key, value);
    }

    /**
     * 获取ChatClient
     * @return ChatClient
     */
    public ChatClient getChatClient() {
        return chatClient;
    }

    /**
     * 获取系统提示词类型
     * @return 系统提示词类型
     */
    public String getSystemPromptType() {
        return systemPromptType;
    }

    /**
     * 设置系统提示词类型
     * @param systemPromptType 系统提示词类型
     */
    public void setSystemPromptType(String systemPromptType) {
        this.systemPromptType = systemPromptType;
    }

    /**
     * 获取PromptManager
     * @return PromptManager
     */
    public PromptManager getPromptManager() {
        return promptManager;
    }

    /**
     * 设置PromptManager
     * @param promptManager PromptManager
     */
    public void setPromptManager(PromptManager promptManager) {
        this.promptManager = promptManager;
    }
    
    /**
     * 获取默认提示词变量
     * @return 默认提示词变量
     */
    public Map<String, Object> getDefaultPromptVariables() {
        return defaultPromptVariables;
    }
    
    /**
     * 获取ChatMemory
     * @return ChatMemory
     */
    public ChatMemory getChatMemory() {
        return chatMemory;
    }
    
    /**
     * 获取SSE管理器
     * @return SSE管理器
     */
    public AiGlobalSseEmitterManager getSseEmitterManager() {
        return sseEmitterManager;
    }
    
    /**
     * 设置SSE管理器
     * @param sseEmitterManager SSE管理器
     */
    public void setSseEmitterManager(AiGlobalSseEmitterManager sseEmitterManager) {
        this.sseEmitterManager = sseEmitterManager;
    }

}
