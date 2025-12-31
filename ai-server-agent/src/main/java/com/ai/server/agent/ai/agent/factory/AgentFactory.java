package com.ai.server.agent.ai.agent.factory;

import com.ai.server.agent.ai.agent.builder.AgentConfig;
import com.ai.server.agent.ai.agent.core.Agent;

/**
 * Agent工厂接口，定义Agent创建方法
 */
public interface AgentFactory {

    /**
     * 根据类型创建Agent实例
     * @param <T> Agent类型
     * @param agentType Agent类型
     * @return Agent实例
     */
    <T> Agent<T> createAgent(String agentType);

    /**
     * 根据类型和配置创建Agent实例
     * @param <T> Agent类型
     * @param agentType Agent类型
     * @param config 配置参数
     * @return Agent实例
     */
    <T> Agent<T> createAgent(String agentType, AgentConfig config);
    
    /**
     * 根据Agent名称从数据库动态创建Agent实例
     * @param <T> Agent类型
     * @param agentName Agent名称
     * @return Agent实例
     */
    <T> Agent<T> createAgentByName(String agentName);
}
