package com.ai.server.agent.ai.agent.dynamic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Agent任务状态管理器，用于管理Agent的任务执行状态
 * 主要功能：
 * 1. 记录每个Agent的活跃任务数
 * 2. 提供任务状态查询接口
 * 3. 支持安全更新Agent配置
 */
@Slf4j
@Component
public class AgentTaskStatusManager {

    /**
     * 任务计数器，key: agentName, value: 活跃任务数
     * 使用ConcurrentHashMap保证线程安全
     */
    private final Map<String, AtomicInteger> taskCounters = new ConcurrentHashMap<>();

    /**
     * 记录Agent任务开始执行
     * @param agentName Agent名称
     */
    public void incrementTaskCount(String agentName) {
        AtomicInteger counter = taskCounters.computeIfAbsent(agentName, k -> new AtomicInteger(0));
        int count = counter.incrementAndGet();
        log.debug("Agent [{}] task count incremented to: {}", agentName, count);
    }

    /**
     * 记录Agent任务执行完成
     * @param agentName Agent名称
     */
    public void decrementTaskCount(String agentName) {
        AtomicInteger counter = taskCounters.get(agentName);
        if (counter != null) {
            int count = counter.decrementAndGet();
            log.debug("Agent [{}] task count decremented to: {}", agentName, count);
            if (count <= 0) {
                taskCounters.remove(agentName);
                log.debug("Agent [{}] has no active tasks, removed from task counters", agentName);
            }
        }
    }

    /**
     * 检查Agent是否有活跃任务
     * @param agentName Agent名称
     * @return 是否有活跃任务
     */
    public boolean hasActiveTasks(String agentName) {
        AtomicInteger counter = taskCounters.get(agentName);
        return counter != null && counter.get() > 0;
    }

    /**
     * 获取Agent的活跃任务数
     * @param agentName Agent名称
     * @return 活跃任务数
     */
    public int getActiveTaskCount(String agentName) {
        AtomicInteger counter = taskCounters.get(agentName);
        return counter != null ? counter.get() : 0;
    }

    /**
     * 等待Agent的所有活跃任务完成
     * @param agentName Agent名称
     * @param timeoutMs 超时时间（毫秒）
     * @return 是否在超时时间内完成
     */
    public boolean waitForTasksCompletion(String agentName, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        log.info("Waiting for agent [{}] tasks to complete, timeout: {}ms", agentName, timeoutMs);
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (!hasActiveTasks(agentName)) {
                log.info("Agent [{}] has no active tasks, can be safely updated", agentName);
                return true;
            }
            
            try {
                // 每100毫秒检查一次
                Thread.sleep(100);
                log.debug("Agent [{}] still has {} active tasks, waiting...", 
                        agentName, getActiveTaskCount(agentName));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Wait for agent [{}] tasks completion interrupted", agentName);
                return false;
            }
        }
        
        log.warn("Timeout waiting for agent [{}] tasks to complete, still has {} active tasks", 
                agentName, getActiveTaskCount(agentName));
        return false;
    }
}
