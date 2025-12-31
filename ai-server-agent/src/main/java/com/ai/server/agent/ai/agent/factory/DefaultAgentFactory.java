package com.ai.server.agent.ai.agent.factory;

import com.ai.server.agent.ai.agent.builder.AgentConfig;
import com.ai.server.agent.ai.agent.core.Agent;
import com.ai.server.agent.ai.agent.dynamic.DynamicAgentManager;
import com.ai.server.agent.ai.agent.manager.PromptManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 默认Agent工厂实现类，用于创建不同类型的Agent
 */
@Component
@Slf4j
public class DefaultAgentFactory implements AgentFactory {

    private final DynamicAgentManager dynamicAgentManager;
    private final PromptManager promptManager;

    @Autowired
    public DefaultAgentFactory(DynamicAgentManager dynamicAgentManager, 
                              PromptManager promptManager) {
        this.dynamicAgentManager = dynamicAgentManager;
        this.promptManager = promptManager;
    }

    /**
     * 根据类型创建Agent实例
     * @param agentType Agent类型
     * @return Agent实例
     */
    @Override
    public <T> Agent<T> createAgent(String agentType) {
        // 使用默认配置创建Agent
        return createAgent(agentType, null);
    }

    /**
     * 根据类型和配置创建Agent实例
     * @param agentType Agent类型
     * @param config 配置参数
     * @return Agent实例
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Agent<T> createAgent(String agentType, AgentConfig config) {
        // 从动态Agent管理器获取Agent
        // 使用agentType作为agentName查找动态配置
        Agent<T> dynamicAgent = (Agent<T>) dynamicAgentManager.getAgent(agentType);
        if (dynamicAgent != null) {
            log.info("Using dynamically created agent for type: {}", agentType);
            return dynamicAgent;
        }
        
        // 如果动态Agent不存在，抛出异常
        log.error("No agent found for type: {}", agentType);
        throw new IllegalArgumentException("Unknown agent type or no dynamic configuration found: " + agentType);
    }
    
    /**
     * 根据Agent名称从数据库动态创建Agent实例
     * @param agentName Agent名称
     * @return Agent实例
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Agent<T> createAgentByName(String agentName) {
        // 从动态Agent管理器获取Agent
        Agent<T> agent = (Agent<T>) dynamicAgentManager.getAgent(agentName);
        if (agent != null) {
            return agent;
        }
        
        // 如果不存在，抛出异常
        log.error("No agent found for name: {}", agentName);
        throw new IllegalArgumentException("Unknown agent name or no dynamic configuration found: " + agentName);
    }
}
