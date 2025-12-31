package com.ai.server.agent.ai.common.startup;

import com.ai.server.agent.ai.common.sql.SqlInitializationService;
import com.ai.server.agent.ai.agent.dynamic.DynamicAgentManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 项目启动初始化器
 * 统一管理项目初始化流程，按照以下顺序执行：
 * 1. 检测数模（有则跳过，没有则初始化）
 * 2. 加载数模
 * 3. 读取智能体相关配置信息
 * 4. 创建智能体
 */
@Component
@Order(1) // 设置最高优先级，确保第一个执行
@Slf4j
public class StartupInitializer implements ApplicationRunner {

    @Autowired
    private SqlInitializationService sqlInitializationService;

    @Autowired
    private DynamicAgentManager dynamicAgentManager;

    /**
     * 初始化标记，确保只执行一次
     */
    private static volatile boolean initialized = false;

    @Override
    public void run(ApplicationArguments args) {
        // 双重检查锁定，确保只执行一次初始化
        if (initialized) {
            log.info("StartupInitializer already initialized, skipping...");
            return;
        }

        synchronized (StartupInitializer.class) {
            if (initialized) {
                log.info("StartupInitializer already initialized, skipping...");
                return;
            }

            log.info("Starting project initialization...");

            try {
                // 1. 检测数模（有则跳过，没有则初始化）
                log.info("Step 1: Checking and initializing database schema...");
                sqlInitializationService.init();

                // 2. 加载数模
                log.info("Step 2: Loading database schema...");
                // 数模加载由SqlInitializationService的init方法自动完成

                // 3. 读取智能体相关配置信息
                log.info("Step 3: Reading agent configuration...");
                // 智能体配置读取由DynamicAgentManager的init方法自动完成

                // 4. 创建智能体
                log.info("Step 4: Creating agents...");
                dynamicAgentManager.init();

                log.info("Project initialization completed successfully");
            } catch (Exception e) {
                log.error("Project initialization failed: {}", e.getMessage(), e);
            } finally {
                initialized = true;
            }
        }
    }
}
