package com.ai.server.agent.ai.agent.dynamic;

import com.ai.server.agent.ai.agent.core.Agent;
import com.ai.server.agent.ai.agent.dynamic.repository.AgentConfigRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent配置变更API，用于手动触发Agent配置更新
 */
@RestController
@RequestMapping("/api/agent/config")
@Slf4j
public class AgentConfigController {

    @Autowired
    private DynamicAgentManager dynamicAgentManager;

    @Autowired
    private AgentConfigChangeListener configChangeListener;

    @Autowired
    private AgentConfigRepository agentConfigRepository;

    /**
     * 手动触发指定Agent的配置更新
     * @param agentName Agent名称
     * @return 更新结果
     */
    @PostMapping("/reload/{agentName}")
    public ResponseEntity<AgentReloadResult> reloadAgent(@PathVariable String agentName) {
        log.info("Manual reload agent requested: {}", agentName);
        
        try {
            Agent<?> agent = dynamicAgentManager.safeReloadAgent(agentName);
            if (agent != null) {
                return ResponseEntity.ok(new AgentReloadResult(agentName, true, "Successfully reloaded agent"));
            } else {
                return ResponseEntity.badRequest().body(new AgentReloadResult(agentName, false, "Failed to reload agent, agent not found or disabled"));
            }
        } catch (Exception e) {
            log.error("Error reloading agent: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new AgentReloadResult(agentName, false, "Error reloading agent: " + e.getMessage()));
        }
    }

    /**
     * 手动触发所有Agent的配置更新
     * @return 批量更新结果
     */
    @PostMapping("/reload-all")
    public ResponseEntity<List<AgentReloadResult>> reloadAllAgents() {
        log.info("Manual reload all agents requested");
        
        try {
            List<DynamicAgentConfig> allConfigs = agentConfigRepository.findAllEnabledAgents();
            List<AgentReloadResult> results = new ArrayList<>();
            
            for (DynamicAgentConfig config : allConfigs) {
                String agentName = config.getAgentName();
                try {
                    Agent<?> agent = dynamicAgentManager.safeReloadAgent(agentName);
                    if (agent != null) {
                        results.add(new AgentReloadResult(agentName, true, "Successfully reloaded agent"));
                    } else {
                        results.add(new AgentReloadResult(agentName, false, "Failed to reload agent"));
                    }
                } catch (Exception e) {
                    log.error("Error reloading agent {}: {}", agentName, e.getMessage(), e);
                    results.add(new AgentReloadResult(agentName, false, "Error: " + e.getMessage()));
                }
            }
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error reloading all agents: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 手动触发配置变更检查
     * @return 检查结果
     */
    @PostMapping("/check-changes")
    public ResponseEntity<String> checkConfigChanges() {
        log.info("Manual config change check requested");
        
        try {
            configChangeListener.checkConfigChanges();
            return ResponseEntity.ok("Config change check triggered successfully");
        } catch (Exception e) {
            log.error("Error checking config changes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error checking config changes: " + e.getMessage());
        }
    }

    /**
     * 获取所有Agent配置信息
     * @return Agent配置列表
     */
    @GetMapping("/all")
    public ResponseEntity<List<DynamicAgentConfig>> getAllAgentConfigs() {
        log.info("Get all agent configs requested");
        
        try {
            List<DynamicAgentConfig> configs = agentConfigRepository.findAllEnabledAgents();
            return ResponseEntity.ok(configs);
        } catch (Exception e) {
            log.error("Error getting all agent configs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 获取指定Agent的配置信息
     * @param agentName Agent名称
     * @return Agent配置
     */
    @GetMapping("/{agentName}")
    public ResponseEntity<DynamicAgentConfig> getAgentConfig(@PathVariable String agentName) {
        log.info("Get agent config requested: {}", agentName);
        
        try {
            DynamicAgentConfig config = agentConfigRepository.findByAgentName(agentName);
            if (config != null) {
                return ResponseEntity.ok(config);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting agent config: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * Agent配置更新结果
     */
    @Data
    public static class AgentReloadResult {
        private String agentName;
        private boolean success;
        private String message;

        public AgentReloadResult(String agentName, boolean success, String message) {
            this.agentName = agentName;
            this.success = success;
            this.message = message;
        }
    }
}
