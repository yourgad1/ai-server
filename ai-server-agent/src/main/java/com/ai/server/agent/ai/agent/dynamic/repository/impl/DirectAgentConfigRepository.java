package com.ai.server.agent.ai.agent.dynamic.repository.impl;

import com.ai.server.agent.ai.agent.dynamic.repository.AgentConfigMappingUtil;
import com.ai.server.agent.ai.agent.dynamic.ChatMemoryConfig;
import com.ai.server.agent.ai.agent.dynamic.DynamicAgentConfig;
import com.ai.server.agent.ai.agent.dynamic.repository.AgentConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 直接访问数据库的Agent配置仓库实现
 */
@Slf4j
@Repository
public class DirectAgentConfigRepository implements AgentConfigRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public List<DynamicAgentConfig> findAllEnabledAgents() {
        // 1. 查询所有启用的Agent配置
        String sql = "SELECT * FROM agent_config WHERE enabled = 1";
        List<Map<String, Object>> agentConfigs = jdbcTemplate.queryForList(sql);
        
        List<DynamicAgentConfig> dynamicAgentConfigs = new ArrayList<>();
        
        // 2. 为每个Agent配置查询关联的工具、提示词变量和聊天记忆配置
        for (Map<String, Object> agentConfig : agentConfigs) {
            DynamicAgentConfig dynamicAgentConfig = AgentConfigMappingUtil.mapToDynamicAgentConfig(agentConfig);
            
            // 查询工具列表
            String agentId = (String) agentConfig.get("agent_id");
            List<String> toolNames = findToolNamesByAgentId(agentId);
            dynamicAgentConfig.setToolNames(toolNames);
            
            // 查询提示词变量
            Map<String, Object> promptVariables = findPromptVariablesByAgentId(agentId);
            dynamicAgentConfig.setPromptVariables(promptVariables);
            
            // 查询dynamic类型的提示词变量键名
            List<String> dynamicKeys = findDynamicPromptVariableKeysByAgentId(agentId);
            dynamicAgentConfig.setDynamicPromptVariableKeys(dynamicKeys);
            
            // 查询runtime类型的提示词变量键名
            List<String> runtimeKeys = findRuntimePromptVariableKeysByAgentId(agentId);
            dynamicAgentConfig.setRuntimePromptVariableKeys(runtimeKeys);
            
            // 查询聊天记忆配置
            ChatMemoryConfig chatMemoryConfig = findChatMemoryConfigByAgentId(agentId);
            dynamicAgentConfig.setChatMemoryConfig(chatMemoryConfig);
            
            dynamicAgentConfigs.add(dynamicAgentConfig);
        }
        
        return dynamicAgentConfigs;
    }
    
    @Override
    public DynamicAgentConfig findByAgentName(String agentName) {
        // 查询指定名称的Agent配置
        String sql = "SELECT * FROM agent_config WHERE agent_name = ? AND enabled = 1";
        List<Map<String, Object>> agentConfigs = jdbcTemplate.queryForList(sql, agentName);
        
        if (agentConfigs == null || agentConfigs.isEmpty()) {
            return null;
        }
        
        Map<String, Object> agentConfig = agentConfigs.get(0);
        DynamicAgentConfig dynamicAgentConfig = AgentConfigMappingUtil.mapToDynamicAgentConfig(agentConfig);
        
        // 查询工具列表
        String agentId = (String) agentConfig.get("agent_id");
        List<String> toolNames = findToolNamesByAgentId(agentId);
        dynamicAgentConfig.setToolNames(toolNames);
        
        // 查询提示词变量
        Map<String, Object> promptVariables = findPromptVariablesByAgentId(agentId);
        dynamicAgentConfig.setPromptVariables(promptVariables);
        
        // 查询dynamic类型的提示词变量键名
        List<String> dynamicKeys = findDynamicPromptVariableKeysByAgentId(agentId);
        dynamicAgentConfig.setDynamicPromptVariableKeys(dynamicKeys);
        
        // 查询runtime类型的提示词变量键名
        List<String> runtimeKeys = findRuntimePromptVariableKeysByAgentId(agentId);
        dynamicAgentConfig.setRuntimePromptVariableKeys(runtimeKeys);
        
        // 查询聊天记忆配置
        ChatMemoryConfig chatMemoryConfig = findChatMemoryConfigByAgentId(agentId);
        dynamicAgentConfig.setChatMemoryConfig(chatMemoryConfig);
        
        return dynamicAgentConfig;
    }
    

    
    /**
     * 根据Agent ID查询工具名称列表
     * @param agentId Agent ID
     * @return 工具名称列表
     */
    private List<String> findToolNamesByAgentId(String agentId) {
        String sql = "SELECT tool_name FROM agent_tool_rel WHERE agent_id = ? ORDER BY tool_order";
        return jdbcTemplate.queryForList(sql, String.class, agentId);
    }
    
    /**
     * 根据Agent ID查询提示词变量
     * @param agentId Agent ID
     * @return 提示词变量映射
     */
    private Map<String, Object> findPromptVariablesByAgentId(String agentId) {
        String sql = "SELECT var_key, var_value FROM agent_prompt_var WHERE agent_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, agentId);
        
        Map<String, Object> variables = new HashMap<>();
        for (Map<String, Object> row : rows) {
            variables.put((String) row.get("var_key"), row.get("var_value"));
        }
        
        return variables;
    }
    
    /**
     * 根据Agent ID查询dynamic类型的提示词变量键名
     * @param agentId Agent ID
     * @return dynamic类型的提示词变量键名列表
     */
    private List<String> findDynamicPromptVariableKeysByAgentId(String agentId) {
        String sql = "SELECT var_key FROM agent_prompt_var WHERE agent_id = ? AND var_type = 'dynamic'";
        return jdbcTemplate.queryForList(sql, String.class, agentId);
    }
    
    /**
     * 根据Agent ID查询runtime类型的提示词变量键名
     * @param agentId Agent ID
     * @return runtime类型的提示词变量键名列表
     */
    private List<String> findRuntimePromptVariableKeysByAgentId(String agentId) {
        String sql = "SELECT var_key FROM agent_prompt_var WHERE agent_id = ? AND var_type = 'runtime'";
        return jdbcTemplate.queryForList(sql, String.class, agentId);
    }
    
    /**
     * 根据Agent ID查询聊天记忆配置
     * @param agentId Agent ID
     * @return 聊天记忆配置
     */
    private ChatMemoryConfig findChatMemoryConfigByAgentId(String agentId) {
        String sql = "SELECT * FROM chat_memory_config WHERE agent_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, agentId);
        
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        
        return AgentConfigMappingUtil.mapToChatMemoryConfig(rows.get(0));
    }
}