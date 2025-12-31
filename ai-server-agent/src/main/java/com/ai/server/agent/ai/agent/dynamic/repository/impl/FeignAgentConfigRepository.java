package com.ai.server.agent.ai.agent.dynamic.repository.impl;

import com.ai.server.agent.ai.agent.dynamic.repository.AgentConfigMappingUtil;
import com.ai.server.agent.ai.feign.DataAccessMysqlClient;
import com.ai.server.agent.ai.feign.dto.R;
import com.ai.server.agent.ai.agent.dynamic.ChatMemoryConfig;
import com.ai.server.agent.ai.agent.dynamic.DynamicAgentConfig;
import com.ai.server.agent.ai.agent.dynamic.repository.AgentConfigRepository;
import com.ai.server.agent.core.jdbc.RemoteSqlDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用Feign访问数据库的Agent配置仓库实现
 */
@Slf4j
@Repository
public class FeignAgentConfigRepository implements AgentConfigRepository {
    
    @Autowired
    private DataAccessMysqlClient dataAccessMysqlClient;
    
    @Value("${data-access.app-id:default}")
    private String appId;
    
    @Value("${data-access.data-source:default}")
    private String dataSource;
    
    @Override
    public List<DynamicAgentConfig> findAllEnabledAgents() {
        // 1. 查询所有启用的Agent配置
        String sql = "SELECT * FROM agent_config WHERE enabled = 1";
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .dataSource(dataSource)
                .build();
        
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result == null || result.getData() == null) {
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> agentConfigs = result.getData();
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
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .params(List.of(agentName))
                .dataSource(dataSource)
                .build();
        
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result == null || result.getData() == null || result.getData().isEmpty()) {
            return null;
        }
        
        Map<String, Object> agentConfig = result.getData().get(0);
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
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .params(List.of(agentId))
                .dataSource(dataSource)
                .build();
        
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result == null || result.getData() == null) {
            return new ArrayList<>();
        }
        
        List<String> toolNames = new ArrayList<>();
        for (Map<String, Object> toolMap : result.getData()) {
            toolNames.add((String) toolMap.get("tool_name"));
        }
        return toolNames;
    }
    
    /**
     * 根据Agent ID查询提示词变量，只处理var_type为static的字段
     * @param agentId Agent ID
     * @return 提示词变量映射
     */
    private Map<String, Object> findPromptVariablesByAgentId(String agentId) {
        String sql = "SELECT var_key, var_value FROM agent_prompt_var WHERE agent_id = ? AND var_type = 'static'";
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .params(List.of(agentId))
                .dataSource(dataSource)
                .build();
        
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result == null || result.getData() == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> promptVariables = new HashMap<>();
        for (Map<String, Object> varMap : result.getData()) {
            promptVariables.put((String) varMap.get("var_key"), varMap.get("var_value"));
        }
        return promptVariables;
    }
    
    /**
     * 根据Agent ID查询dynamic类型的提示词变量键名
     * @param agentId Agent ID
     * @return dynamic类型的提示词变量键名列表
     */
    private List<String> findDynamicPromptVariableKeysByAgentId(String agentId) {
        String sql = "SELECT var_key FROM agent_prompt_var WHERE agent_id = ? AND var_type = 'dynamic'";
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .params(List.of(agentId))
                .dataSource(dataSource)
                .build();
        
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result == null || result.getData() == null) {
            return new ArrayList<>();
        }
        
        List<String> keys = new ArrayList<>();
        for (Map<String, Object> varMap : result.getData()) {
            keys.add((String) varMap.get("var_key"));
        }
        return keys;
    }
    
    /**
     * 根据Agent ID查询runtime类型的提示词变量键名
     * @param agentId Agent ID
     * @return runtime类型的提示词变量键名列表
     */
    private List<String> findRuntimePromptVariableKeysByAgentId(String agentId) {
        String sql = "SELECT var_key FROM agent_prompt_var WHERE agent_id = ? AND var_type = 'runtime'";
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .params(List.of(agentId))
                .dataSource(dataSource)
                .build();
        
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result == null || result.getData() == null) {
            return new ArrayList<>();
        }
        
        List<String> keys = new ArrayList<>();
        for (Map<String, Object> varMap : result.getData()) {
            keys.add((String) varMap.get("var_key"));
        }
        return keys;
    }
    
    /**
     * 根据Agent ID查询聊天记忆配置
     * @param agentId Agent ID
     * @return 聊天记忆配置
     */
    private ChatMemoryConfig findChatMemoryConfigByAgentId(String agentId) {
        String sql = "SELECT * FROM chat_memory_config WHERE agent_id = ?";
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .params(List.of(agentId))
                .dataSource(dataSource)
                .build();
        
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result == null || result.getData() == null || result.getData().isEmpty()) {
            return null;
        }
        
        Map<String, Object> memoryConfig = result.getData().get(0);
        return AgentConfigMappingUtil.mapToChatMemoryConfig(memoryConfig);
    }
}
