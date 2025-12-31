package com.ai.server.agent.ai.agent.dynamic;

import com.ai.server.agent.ai.agent.dynamic.repository.AgentConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent配置变更监听器，用于检测配置变更并触发安全更新
 * 使用定时轮询机制，定期检查数据库中的配置是否发生变化
 */
@Component
@Slf4j
public class AgentConfigChangeListener {

    @Autowired
    private DynamicAgentManager dynamicAgentManager;

    @Autowired
    private AgentConfigRepository agentConfigRepository;

    /**
     * 本地配置版本缓存，key: agentName, value: 最后更新时间
     */
    private final Map<String, Date> localConfigVersions = new ConcurrentHashMap<>();

    /**
     * 定时检查配置变更，每30秒执行一次
     * 1. 获取所有启用的Agent配置
     * 2. 对比本地缓存的配置版本
     * 3. 如果配置变更，触发安全更新
     */
    @Scheduled(fixedDelay = 30000)
    public void checkConfigChanges() {
        log.info("Checking for agent config changes...");

        try {
            // 获取所有启用的Agent配置
            List<DynamicAgentConfig> latestConfigs = agentConfigRepository.findAllEnabledAgents();

            for (DynamicAgentConfig config : latestConfigs) {
                String agentName = config.getAgentName();
                Date lastUpdateTime = getLastUpdateTime(config);
                Date localVersion = localConfigVersions.get(agentName);
                // 检查配置是否变更
                if (isConfigChanged(localVersion, lastUpdateTime)) {
                    log.info("Agent config changed: {}, updating...", agentName);
                    // 触发安全更新
                    dynamicAgentManager.safeReloadAgent(agentName);
                    // 更新本地版本缓存
                    localConfigVersions.put(agentName, lastUpdateTime);
                    log.info("Agent config updated successfully: {}", agentName);
                }
            }

            // 处理已删除的Agent配置
            cleanupDeletedAgents(latestConfigs);

        } catch (Exception e) {
            log.error("Error checking for agent config changes: {}", e.getMessage(), e);
        }
    }

    /**
     * 从配置中获取最后更新时间
     * 由于DynamicAgentConfig没有直接包含updateTime字段，需要从数据库重新查询
     * @param config Agent配置
     * @return 最后更新时间
     */
    private Date getLastUpdateTime(DynamicAgentConfig config) {
        // 这里需要从数据库查询配置的最后更新时间
        // 由于DynamicAgentConfig没有包含updateTime字段，我们使用当前时间作为临时解决方案
        // 实际实现中应该修改DynamicAgentConfig类，添加updateTime字段
        return new Date();
    }

    /**
     * 检查配置是否发生变更
     * @param localVersion 本地缓存的版本时间
     * @param dbVersion 数据库中的版本时间
     * @return 是否发生变更
     */
    private boolean isConfigChanged(Date localVersion, Date dbVersion) {
        if (localVersion == null) {
            // 新配置，需要加载
            return true;
        }
        if (dbVersion == null) {
            // 数据库中没有配置，不需要更新
            return false;
        }
        // 比较更新时间，数据库版本更新则需要更新
        return dbVersion.after(localVersion);
    }

    /**
     * 清理已删除的Agent配置
     * @param latestConfigs 最新的Agent配置列表
     */
    private void cleanupDeletedAgents(List<DynamicAgentConfig> latestConfigs) {
        // 获取最新的Agent名称集合
        Map<String, Boolean> latestAgentNames = new ConcurrentHashMap<>();
        for (DynamicAgentConfig config : latestConfigs) {
            latestAgentNames.put(config.getAgentName(), true);
        }

        // 清理本地缓存中不存在的Agent配置
        localConfigVersions.keySet().removeIf(agentName -> !latestAgentNames.containsKey(agentName));
    }

    /**
     * 手动触发配置检查
     */
    public void triggerConfigCheck() {
        log.info("Manual config check triggered");
        checkConfigChanges();
    }

    /**
     * 获取本地配置版本缓存
     * @return 配置版本缓存
     */
    public Map<String, Date> getLocalConfigVersions() {
        return localConfigVersions;
    }
}
