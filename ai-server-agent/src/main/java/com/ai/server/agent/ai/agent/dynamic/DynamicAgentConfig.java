package com.ai.server.agent.ai.agent.dynamic;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 动态Agent配置类，用于定义Agent的动态配置
 */
@Data
public class DynamicAgentConfig {
    
    /**
     * Agent唯一名称，用于标识和访问Agent
     */
    private String agentName;
    
    /**
     * 系统提示词
     */
    private String systemPrompt;
    
    /**
     * 默认提示词变量，用于填充提示词模板
     */
    private Map<String, Object> promptVariables;
    
    /**
     * 需要动态传入的提示词变量键名列表（var_type为dynamic）
     */
    private List<String> dynamicPromptVariableKeys;
    
    /**
     * 需要运行时传入的提示词变量键名列表（var_type为runtime）
     */
    private List<String> runtimePromptVariableKeys;
    
    /**
     * 工具名称列表，用于动态加载工具
     */
    private List<String> toolNames;
    
    /**
     * 聊天记忆配置
     */
    private ChatMemoryConfig chatMemoryConfig;
    
    /**
     * Agent类型，仅支持chat和stream
     */
    private String agentType;
    
    /**
     * 是否启用该Agent
     */
    private boolean enabled;
    
    /**
     * 描述信息
     */
    private String description;
    
    /**
     * 是否关联工具
     */
    private boolean hasTools;
    
    /**
     * 设置工具名称列表，并自动计算hasTools字段
     * @param toolNames 工具名称列表
     */
    public void setToolNames(List<String> toolNames) {
        this.toolNames = toolNames;
        this.hasTools = toolNames != null && !toolNames.isEmpty();
    }
    
    /**
     * 验证agentType是否合法
     * @return 是否合法
     */
    public boolean isValidAgentType() {
        return "chat".equals(agentType) || "stream".equals(agentType);
    }
}
