package com.ai.server.agent.ai.agent.dynamic.repository;

import com.ai.server.agent.ai.agent.dynamic.DynamicAgentConfig;

import java.util.List;

/**
 * Agent配置仓库接口，定义Agent配置的访问方法
 */
public interface AgentConfigRepository {
    
    /**
     * 获取所有启用的Agent配置
     * @return 启用的Agent配置列表
     */
    List<DynamicAgentConfig> findAllEnabledAgents();
    
    /**
     * 根据Agent名称获取配置
     * @param agentName Agent名称
     * @return Agent配置
     */
    DynamicAgentConfig findByAgentName(String agentName);
}
