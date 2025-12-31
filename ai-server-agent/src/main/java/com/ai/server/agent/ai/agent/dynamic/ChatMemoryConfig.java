package com.ai.server.agent.ai.agent.dynamic;

import lombok.Data;

/**
 * 聊天记忆配置类，用于配置ChatMemory的各种属性
 */
@Data
public class ChatMemoryConfig {
    
    /**
     * 聊天记忆类型，例如："messageWindow"、"conversation"等
     */
    private String memoryType;
    
    /**
     * 最大消息数
     */
    private int maxMessages;
    
    /**
     * 消息过期时间（毫秒），默认Long.MAX_VALUE表示不过期
     */
    private long messageExpireTime = Long.MAX_VALUE;
    
    /**
     * 是否启用聊天记忆
     */
    private boolean enabled;
}
