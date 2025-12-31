package com.ai.server.agent.ai.agent.dynamic.repository;

import com.ai.server.agent.ai.agent.dynamic.ChatMemoryConfig;
import com.ai.server.agent.ai.agent.dynamic.DynamicAgentConfig;

import java.util.Map;

/**
 * Agent配置映射工具类，用于将数据库查询结果映射为Agent配置对象
 */
public class AgentConfigMappingUtil {

    /**
     * 将数据库查询结果映射为DynamicAgentConfig对象
     * @param map 数据库查询结果
     * @return DynamicAgentConfig对象
     */
    public static DynamicAgentConfig mapToDynamicAgentConfig(Map<String, Object> map) {
        DynamicAgentConfig config = new DynamicAgentConfig();
        config.setAgentName((String) map.get("agent_name"));
        config.setSystemPrompt((String) map.get("system_prompt"));
        config.setAgentType((String) map.get("agent_type"));
        config.setEnabled((Boolean) map.get("enabled"));
        config.setDescription((String) map.get("description"));
        // hasTools字段会在setToolNames方法中自动设置
        return config;
    }

    /**
     * 将数据库查询结果映射为ChatMemoryConfig对象
     * @param map 数据库查询结果
     * @return ChatMemoryConfig对象
     */
    public static ChatMemoryConfig mapToChatMemoryConfig(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        ChatMemoryConfig chatMemoryConfig = new ChatMemoryConfig();
        chatMemoryConfig.setMemoryType((String) map.get("memory_type"));
        chatMemoryConfig.setMaxMessages((Integer) map.get("max_messages"));
        
        // 读取message_expire字段，如果为null则使用默认值
        if (map.get("message_expire") != null) {
            chatMemoryConfig.setMessageExpireTime(((Number) map.get("message_expire")).longValue());
        }
        
        chatMemoryConfig.setEnabled((Boolean) map.get("enabled"));
        return chatMemoryConfig;
    }
}
