package com.ai.server.agent.ai.agent.builder;

import com.ai.server.agent.ai.agent.template.BaseAgent;
import lombok.Builder;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.Map;

/**
 * Agent配置类，用于配置Agent的各种属性
 */
@Data
@Builder
public class AgentConfig {

    /**
     * ChatClient实例
     */
    private ChatClient chatClient;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 默认提示词变量，用于填充提示词模板
     */
    private Map<String, Object> defaultPromptVariables;

    /**
     * Agent类
     */
    private Class<? extends BaseAgent<?>> agentClass;
    
    /**
     * ChatMemory实例，用于管理对话上下文
     */
    private ChatMemory chatMemory;
    
    /**
     * 最大消息数，用于默认ChatMemory配置
     */
    @Builder.Default
    private int maxMessages = 10;
}
