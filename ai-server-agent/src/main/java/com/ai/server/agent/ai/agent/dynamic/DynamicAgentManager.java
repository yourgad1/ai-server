package com.ai.server.agent.ai.agent.dynamic;

import com.ai.server.agent.ai.agent.builder.AgentBuilder;
import com.ai.server.agent.ai.agent.core.Agent;
import com.ai.server.agent.ai.agent.dynamic.repository.AgentConfigRepository;
import com.ai.server.agent.ai.agent.manager.PromptManager;
import com.ai.server.agent.ai.common.sse.AiGlobalSseEmitterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 动态Agent管理器，负责Agent的创建、管理和执行
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON) // 明确指定单例模式
@Primary // 确保只有一个实例被注入
@Slf4j
public class DynamicAgentManager {
    
    /**
     * Agent实例映射，key为agentName
     */
    private final Map<String, Agent<?>> agentMap = new ConcurrentHashMap<>();
    
    /**
     * Agent配置映射，key为agentName
     */
    private final Map<String, DynamicAgentConfig> agentConfigMap = new ConcurrentHashMap<>();
    
    /**
     * 读写锁，保护Agent实例的并发访问
     */
    private final ReentrantReadWriteLock agentLock = new ReentrantReadWriteLock();
    
    @Autowired
    private ChatClient chatClient;
    
    @Autowired
    private PromptManager promptManager;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private JdbcChatMemoryRepository chatMemoryRepository;
    
    @Autowired
    private AgentConfigRepository agentConfigRepository;
    
    @Autowired
    private AgentTaskStatusManager taskStatusManager;
    
    @Autowired
    private AiGlobalSseEmitterManager sseEmitterManager;
    
    /**
     * 初始化标记，确保只执行一次初始化
     * 使用静态变量，确保所有实例共享同一个初始化状态
     */
    private static volatile boolean initialized = false;

    /**
     * 初始化方法，从配置中心加载Agent配置并创建Agent实例
     */
    public void init() {
        // 双重检查锁定，确保只执行一次初始化
        if (initialized) {
            log.info("DynamicAgentManager already initialized, skipping...");
            return;
        }
        
        // 使用类锁，确保所有实例共享同一个锁
        synchronized (DynamicAgentManager.class) {
            if (initialized) {
                log.info("DynamicAgentManager already initialized, skipping...");
                return;
            }
            
            log.info("Initializing DynamicAgentManager...");
            
            try {
                // 从配置中心加载Agent配置
                List<DynamicAgentConfig> configs = loadAgentConfigs();
                
                // 1. 先注册所有提示词模板到PromptManager
                for (DynamicAgentConfig config : configs) {
                    if (config.isEnabled() && config.getSystemPrompt() != null) {
                        // 使用agentName作为promptType，方便后续获取
                        promptManager.registerPromptTemplate(config.getAgentName(), config.getSystemPrompt());
                        log.info("Registered prompt template for agent: {}", config.getAgentName());
                    }
                }
                
                // 2. 为每个配置创建Agent实例
                for (DynamicAgentConfig config : configs) {
                    if (config.isEnabled()) {
                        try {
                            Agent<?> agent = createAgent(config);
                            agentMap.put(config.getAgentName(), agent);
                            agentConfigMap.put(config.getAgentName(), config);
                            log.info("Created agent: {}", config.getAgentName());
                        } catch (Exception e) {
                            log.error("Failed to create agent: {}", config.getAgentName(), e);
                        }
                    }
                }
                
                log.info("DynamicAgentManager initialized with {} agents", agentMap.size());
            } catch (Exception e) {
                log.error("Failed to initialize DynamicAgentManager: {}", e.getMessage(), e);
            } finally {
                // 无论是否出现异常，都标记为已初始化，避免重复初始化
                initialized = true;
            }
        }
    }


    
    /**
     * 从数据库加载Agent配置
     * @return Agent配置列表
     */
    private List<DynamicAgentConfig> loadAgentConfigs() {
        return agentConfigRepository.findAllEnabledAgents();
    }
    
    /**
     * 创建Agent实例
     * @param config Agent配置
     * @return Agent实例
     */
    private Agent<?> createAgent(DynamicAgentConfig config) {
        log.info("Creating agent: {} with config: {}", config.getAgentName(), config);
        
        // 创建AgentBuilder
        AgentBuilder<GenericDynamicAgent> builder = AgentBuilder.create();
        
        // 设置Agent类
        builder.withAgentClass(GenericDynamicAgent.class);
        
        // 设置ChatClient
        builder.withChatClient(chatClient);
        
        // 设置Agent名称和类型
        builder.withAgentName(config.getAgentName());
        builder.withAgentType(config.getAgentType());
        
        // 使用PromptManager获取提示词模板并创建提示词
        String systemPrompt = null;
        if (promptManager.containsPromptType(config.getAgentName())) {
            // 从PromptManager获取提示词模板
            PromptTemplate promptTemplate = promptManager.getPromptTemplate(config.getAgentName());
            
            // 创建提示词变量映射
            Map<String, Object> promptVariables = new HashMap<>();
            if (config.getPromptVariables() != null) {
                promptVariables.putAll(config.getPromptVariables());
            }
            
            // 1. 自动添加常见变量，不依赖模板内容检测
            // 添加当前日期到变量映射，适用于所有模板
            String currentDate = new java.util.Date().toString();
            promptVariables.put("current_date", currentDate);
            promptVariables.put("current_data", currentDate);
            
            // 添加agent信息到变量映射，适用于所有模板
            promptVariables.put("agent_name", config.getAgentName());
            promptVariables.put("agent_type", config.getAgentType());
            
            // 2. 添加try-catch处理，确保即使模板中有未替换的变量，也能成功创建提示词
            try {
                // 创建提示词
                Prompt prompt = promptTemplate.create(promptVariables);
                systemPrompt = prompt.getInstructions().toString();
                log.info("Created prompt for agent: {} using template", config.getAgentName());
            } catch (IllegalStateException e) {
                // 处理未替换的变量
                if (e.getMessage().startsWith("Not all variables were replaced in the template")) {
                    log.warn("Some variables were not replaced in the template for agent: {}. Error: {}", config.getAgentName(), e.getMessage());
                    
                    // 直接使用配置中的system_prompt作为fallback
                    systemPrompt = config.getSystemPrompt();
                    log.info("Created prompt for agent: {} using direct system prompt as fallback", config.getAgentName());
                } else {
                    // 其他错误，重新抛出
                    throw e;
                }
            }
        } else if (config.getSystemPrompt() != null) {
            // 兼容处理：如果PromptManager中没有注册模板，直接使用配置中的提示词
            systemPrompt = config.getSystemPrompt();
            log.info("Created prompt for agent: {} using direct system prompt", config.getAgentName());
        }
        
        // 设置系统提示词
        if (systemPrompt != null) {
            builder.withSystemPrompt(systemPrompt);
        }
        
        // 设置默认提示词变量
        if (config.getPromptVariables() != null && !config.getPromptVariables().isEmpty()) {
            builder.withDefaultPromptVariables(config.getPromptVariables());
        }
        
        // 加载工具
        List<Object> tools = loadTools(config.getToolNames());
        // 对于simpleChatClient，检查是否已经包含dateTool，如果没有则添加
        if ("simpleChatClient".equals(config.getAgentName())) {
            // 检查配置中是否已经包含dateTool
            boolean hasDateTool = false;
            if (config.getToolNames() != null) {
                for (String toolName : config.getToolNames()) {
                    if ("dateTool".equals(toolName)) {
                        hasDateTool = true;
                        break;
                    }
                }
            }
            // 如果配置中没有dateTool，则添加
            if (!hasDateTool && applicationContext.containsBean("dateTool")) {
                // 使用延迟加载机制创建dateTool代理
                Object dateToolProxy = createToolProxy("dateTool");
                tools.add(dateToolProxy);
            }
        }
        builder.withTools(tools);
        
        // 设置聊天记忆配置，让Agent在调用时构建ChatMemory
        if (config.getChatMemoryConfig() != null) {
            // 在Agent实例创建后设置chatMemoryConfig
            // 这里先跳过，在下面的代码中设置
        }
        
        // 注入SSE管理器
        builder.withSseEmitterManager(sseEmitterManager);
        
        // 注入JdbcTemplate，用于更新聊天记录的agent_id
        JdbcTemplate jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
        builder.addDependency("jdbcTemplate", jdbcTemplate);
        
        // 使用AgentBuilder构建Agent实例
        GenericDynamicAgent agent = builder.build(promptManager);
        
        // 设置动态和运行时变量键名
        agent.setDynamicPromptVariableKeys(config.getDynamicPromptVariableKeys());
        agent.setRuntimePromptVariableKeys(config.getRuntimePromptVariableKeys());
        
        // 设置聊天记忆配置，让Agent在调用时构建ChatMemory
        agent.setChatMemoryConfig(config.getChatMemoryConfig());
        
        // 设置Spring应用上下文，用于获取ChatMemoryRepository
        agent.setApplicationContext(applicationContext);
        
        return agent;
    }
    
    /**
     * 配置ChatMemory
     * @param agent Agent实例
     * @param config Agent配置
     */
    private void configureChatMemory(GenericDynamicAgent agent, DynamicAgentConfig config) {
        ChatMemoryConfig chatMemoryConfig = config.getChatMemoryConfig();
        if (chatMemoryConfig != null && chatMemoryConfig.isEnabled()) {
            ChatMemory chatMemory;
            
            // 根据配置创建ChatMemory实例，使用JdbcChatMemoryRepository存储到MySQL
            if ("messageWindow".equals(chatMemoryConfig.getMemoryType())) {
                chatMemory = MessageWindowChatMemory.builder()
                        .chatMemoryRepository(chatMemoryRepository)
                        .maxMessages(chatMemoryConfig.getMaxMessages())
                        .build();
            } else {
                // 默认使用MessageWindowChatMemory，使用JdbcChatMemoryRepository存储到MySQL
                chatMemory = MessageWindowChatMemory.builder()
                        .chatMemoryRepository(chatMemoryRepository)
                        .maxMessages(chatMemoryConfig.getMaxMessages())
                        .build();
            }
            
            // 设置ChatMemory
            agent.setChatMemory(chatMemory);
        }
    }
    
    /**
     * 根据工具名称列表加载工具实例，实现延迟加载避免循环依赖
     * @param toolNames 工具名称列表
     * @return 工具实例列表
     */
    private List<Object> loadTools(List<String> toolNames) {
        List<Object> tools = new ArrayList<>();
        
        if (toolNames != null && !toolNames.isEmpty()) {
            for (String toolName : toolNames) {
                try {
                    // 延迟加载：检查工具是否存在，但不直接获取实例
                    // 而是在工具实际使用时通过代理动态获取
                    if (applicationContext.containsBean(toolName)) {
                        // 创建工具代理，在实际调用时才获取真实实例
                        Object toolProxy = createToolProxy(toolName);
                        // 现在createToolProxy总是返回代理对象，不会返回null
                        tools.add(toolProxy);
                        log.info("Added proxy for tool: {} to agent", toolName);
                    } else {
                        log.warn("Tool not found in context: {}", toolName);
                    }
                } catch (Exception e) {
                    log.error("Failed to process tool: {}", toolName, e);
                }
            }
        }
        
        return tools;
    }
    
    /**
     * 创建工具代理，实现延迟加载
     * @param toolName 工具名称
     * @return 工具代理对象
     */
    private Object createToolProxy(String toolName) {
        // 获取工具的所有接口
        Class<?> toolClass = applicationContext.getType(toolName);
        if (toolClass == null) {
            throw new IllegalArgumentException("Tool class not found: " + toolName);
        }
        
        Class<?>[] interfaces = toolClass.getInterfaces();
        if (interfaces.length == 0) {
            // 如果工具类没有实现任何接口，直接尝试获取实例
            // 简化实现，避免复杂的CGLIB代理可能带来的问题
            log.info("Tool {} has no interfaces, trying to get direct instance with retry mechanism", toolName);
            return getRealToolInstance(toolName);
        } else {
            // 如果工具类实现了接口，返回基于接口的代理
            return createJdkProxy(toolName, interfaces);
        }
    }
    
    /**
     * 创建JDK动态代理
     * @param toolName 工具名称
     * @param interfaces 接口数组
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    private <T> T createJdkProxy(String toolName, Class<?>... interfaces) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(
            getClass().getClassLoader(),
            interfaces,
            (proxy, method, args) -> {
                // 当代理方法被调用时，才从ApplicationContext获取真实工具实例
                Object realTool = getRealToolInstance(toolName);
                if (realTool == null) {
                    log.error("Failed to get real tool instance for {} when invoking method {}", toolName, method.getName());
                    throw new IllegalStateException("Tool instance not available: " + toolName);
                }
                // 调用真实工具的方法
                return method.invoke(realTool, args);
            }
        );
    }
    
    /**
     * 获取真实工具实例，带重试机制
     * @param toolName 工具名称
     * @return 工具实例，如果获取失败返回null
     */
    private Object getRealToolInstance(String toolName) {
        Object realTool = null;
        int retryCount = 3;
        long retryDelay = 100;
        
        for (int i = 0; i < retryCount; i++) {
            try {
                if (applicationContext.containsBean(toolName)) {
                    realTool = applicationContext.getBean(toolName);
                    if (realTool != null) {
                        log.info("Successfully got real tool instance for: {}", toolName);
                        break;
                    }
                }
                log.warn("Tool {} not available, retrying in {}ms... (Attempt {}/{})", toolName, retryDelay, i+1, retryCount);
                Thread.sleep(retryDelay);
                retryDelay *= 2; // 指数退避
            } catch (BeanCurrentlyInCreationException e) {
                log.warn("Tool {} is still in creation, retrying in {}ms... (Attempt {}/{})", toolName, retryDelay, i+1, retryCount);
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                retryDelay *= 2; // 指数退避
            } catch (Exception e) {
                log.error("Error getting real tool instance for {}: {}, Attempt {}/{}", toolName, e.getMessage(), i+1, retryCount);
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                retryDelay *= 2; // 指数退避
            }
        }
        
        if (realTool == null) {
            log.error("Failed to get real tool instance for {} after {} attempts", toolName, retryCount);
        }
        
        return realTool;
    }
    
    /**
     * 根据Agent名称获取Agent实例
     * @param agentName Agent名称
     * @param <T> Agent类型
     * @return Agent实例
     */
    @SuppressWarnings("unchecked")
    public <T> Agent<T> getAgent(String agentName) {
        agentLock.readLock().lock();
        try {
            return (Agent<T>) agentMap.get(agentName);
        } finally {
            agentLock.readLock().unlock();
        }
    }
    
    /**
     * 根据Agent名称获取Agent配置
     * @param agentName Agent名称
     * @return Agent配置
     */
    public DynamicAgentConfig getAgentConfig(String agentName) {
        return agentConfigMap.get(agentName);
    }
    
    /**
     * 获取所有Agent名称
     * @return Agent名称列表
     */
    public Set<String> getAllAgentNames() {
        return agentMap.keySet();
    }
    
    /**
     * 安全重新加载Agent配置并更新Agent实例
     * 1. 等待正在执行的任务完成
     * 2. 更新PromptManager中的提示词模板
     * 3. 原子替换Agent实例
     * 4. 更新配置缓存
     * @param agentName Agent名称
     * @return 更新后的Agent实例
     */
    public Agent<?> safeReloadAgent(String agentName) {
        log.info("Safe reloading agent: {}", agentName);
        
        try {
            // 1. 从数据库重新加载该Agent的配置
            DynamicAgentConfig config = agentConfigRepository.findByAgentName(agentName);
            if (config == null || !config.isEnabled()) {
                log.warn("Agent not found or disabled: {}", agentName);
                return null;
            }
            
            // 2. 更新PromptManager中的提示词模板
            if (config.getSystemPrompt() != null) {
                promptManager.registerPromptTemplate(agentName, config.getSystemPrompt());
                log.info("Updated prompt template for agent: {}", agentName);
            }
            
            // 3. 创建新的Agent实例
            Agent<?> newAgent = createAgent(config);
            
            // 4. 等待活跃任务完成（最多等待30秒）
            boolean tasksCompleted = taskStatusManager.waitForTasksCompletion(agentName, 30000);
            if (!tasksCompleted) {
                log.warn("Timeout waiting for tasks to complete, forcing reload: {}", agentName);
            }
            
            // 5. 原子替换Agent实例
            agentLock.writeLock().lock();
            try {
                Agent<?> oldAgent = agentMap.put(agentName, newAgent);
                agentConfigMap.put(agentName, config);
                log.info("Successfully reloaded agent: {}", agentName);
                return newAgent;
            } finally {
                agentLock.writeLock().unlock();
            }
        } catch (Exception e) {
            log.error("Failed to reload agent: {}", agentName, e);
            return getAgent(agentName); // 返回旧实例
        }
    }
    
    /**
     * 重新加载Agent配置并更新Agent实例
     * @param agentName Agent名称
     * @return 更新后的Agent实例
     */
    public synchronized Agent<?> reloadAgent(String agentName) {
        log.info("Reloading agent: {}", agentName);
        
        // 从配置中心重新加载该Agent的配置
        DynamicAgentConfig config = loadAgentConfig(agentName);
        if (config != null && config.isEnabled()) {
            try {
                Agent<?> agent = createAgent(config);
                agentMap.put(agentName, agent);
                agentConfigMap.put(agentName, config);
                log.info("Reloaded agent: {}", agentName);
                return agent;
            } catch (Exception e) {
                log.error("Failed to reload agent: {}", agentName, e);
                return agentMap.get(agentName); // 返回旧实例
            }
        }
        
        return null;
    }
    
    /**
     * 从数据库加载单个Agent配置
     * @param agentName Agent名称
     * @return Agent配置
     */
    private DynamicAgentConfig loadAgentConfig(String agentName) {
        // 从数据库重新加载最新配置
        return agentConfigRepository.findByAgentName(agentName);
    }
    
    /**
     * 注册新的Agent配置
     * @param config Agent配置
     * @return 新创建的Agent实例
     */
    public synchronized Agent<?> registerAgent(DynamicAgentConfig config) {
        log.info("Registering new agent: {}", config.getAgentName());
        
        if (agentMap.containsKey(config.getAgentName())) {
            log.warn("Agent already exists: {}", config.getAgentName());
            return agentMap.get(config.getAgentName());
        }
        
        try {
            Agent<?> agent = createAgent(config);
            agentMap.put(config.getAgentName(), agent);
            agentConfigMap.put(config.getAgentName(), config);
            log.info("Registered new agent: {}", config.getAgentName());
            return agent;
        } catch (Exception e) {
            log.error("Failed to register agent: {}", config.getAgentName(), e);
            return null;
        }
    }
    
    /**
     * 注销Agent
     * @param agentName Agent名称
     * @return 是否注销成功
     */
    public synchronized boolean unregisterAgent(String agentName) {
        log.info("Unregistering agent: {}", agentName);
        
        // 等待所有任务完成
        taskStatusManager.waitForTasksCompletion(agentName, 30000);
        
        agentLock.writeLock().lock();
        try {
            Agent<?> removedAgent = agentMap.remove(agentName);
            DynamicAgentConfig removedConfig = agentConfigMap.remove(agentName);
            
            if (removedAgent != null && removedConfig != null) {
                log.info("Unregistered agent: {}", agentName);
                return true;
            }
            
            log.warn("Agent not found: {}", agentName);
            return false;
        } finally {
            agentLock.writeLock().unlock();
        }
    }
}