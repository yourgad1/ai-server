package com.ai.server.agent.ai.agent.builder;

import cn.hutool.core.util.StrUtil;
import com.ai.server.agent.ai.agent.manager.PromptManager;
import com.ai.server.agent.ai.agent.template.BaseAgent;
import com.ai.server.agent.ai.common.sse.AiGlobalSseEmitterManager;
import com.ai.server.agent.ai.agent.dynamic.DynamicAgentConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent建造者类，用于灵活构建Agent实例
 * @param <T> Agent类型
 */
@Slf4j
public class AgentBuilder<T extends BaseAgent<?>> {

    private ChatClient chatClient;
    private String systemPrompt;
    private Map<String, Object> defaultPromptVariables = new HashMap<>();
    private List<Object> tools = new ArrayList<>();
    private Map<String, Object> dependencies = new HashMap<>();
    private Class<T> agentClass;
    private ChatMemory chatMemory;
    private int maxMessages = 10;
    private String agentName;
    private String agentType;
    private AiGlobalSseEmitterManager sseEmitterManager;

    /**
     * 私有构造方法，通过静态方法创建实例
     */
    private AgentBuilder() {
    }

    /**
     * 创建AgentBuilder实例
     * @param <T> Agent类型
     * @return AgentBuilder实例
     */
    public static <T extends BaseAgent<?>> AgentBuilder<T> create() {
        return new AgentBuilder<>();
    }

    /**
     * 从DynamicAgentConfig创建AgentBuilder实例
     * @param config 动态Agent配置
     * @param <T> Agent类型
     * @return AgentBuilder实例
     */
    public static <T extends BaseAgent<?>> AgentBuilder<T> fromConfig(DynamicAgentConfig config, Class<T> agentClass) {
        AgentBuilder<T> builder = new AgentBuilder<>();
        builder.agentClass = agentClass;
        builder.systemPrompt = config.getSystemPrompt();
        builder.defaultPromptVariables = config.getPromptVariables() != null ? config.getPromptVariables() : new HashMap<>();
        builder.agentName = config.getAgentName();
        builder.agentType = config.getAgentType();
        
        // 配置ChatMemory
        if (config.getChatMemoryConfig() != null && config.getChatMemoryConfig().isEnabled()) {
            builder.maxMessages = config.getChatMemoryConfig().getMaxMessages();
        }
        
        return builder;
    }

    /**
     * 设置Agent类型
     * @param agentClass Agent类
     * @return AgentBuilder实例
     */
    public AgentBuilder<T> withAgentClass(Class<T> agentClass) {
        this.agentClass = agentClass;
        return this;
    }

    /**
     * 设置ChatClient
     * @param chatClient ChatClient实例
     * @return AgentBuilder实例
     */
    public AgentBuilder<T> withChatClient(ChatClient chatClient) {
        this.chatClient = chatClient;
        return this;
    }

    /**
     * 设置系统提示词
     * @param systemPrompt 系统提示词
     * @return AgentBuilder实例
     */
    public AgentBuilder<T> withSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
        return this;
    }

    /**
     * 添加默认提示词变量
     * @param key 键
     * @param value 值
     * @return AgentBuilder实例
     */
    public AgentBuilder<T> addDefaultPromptVariable(String key, Object value) {
        this.defaultPromptVariables.put(key, value);
        return this;
    }

    /**
     * 设置默认提示词变量
     * @param defaultPromptVariables 默认提示词变量
     * @return AgentBuilder实例
     */
    public AgentBuilder<T> withDefaultPromptVariables(Map<String, Object> defaultPromptVariables) {
        this.defaultPromptVariables = defaultPromptVariables;
        return this;
    }

    /**
     * 设置ChatMemory
     * @param chatMemory ChatMemory实例
     * @return AgentBuilder实例
     */
    public AgentBuilder<T> withChatMemory(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
        return this;
    }

    /**
     * 设置最大消息数
     * @param maxMessages 最大消息数
     * @return AgentBuilder实例
     */
    public AgentBuilder<T> withMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
        return this;
    }

    /**
     * 添加工具
     * @param tool 工具实例
     * @return AgentBuilder实例
     */
    public AgentBuilder<T> addTool(Object tool) {
        this.tools.add(tool);
        return this;
    }

    /**
     * 设置工具列表
     * @param tools 工具列表
     * @return AgentBuilder实例
     */
    public AgentBuilder<T> withTools(List<Object> tools) {
        this.tools = tools;
        return this;
    }

    /**
     * 添加依赖服务
     * @param name 依赖名称
     * @param dependency 依赖实例
     * @return AgentBuilder实例
     */
    public AgentBuilder<T> addDependency(String name, Object dependency) {
        this.dependencies.put(name, dependency);
        return this;
    }

    /**
     * 设置依赖服务列表
     * @param dependencies 依赖服务列表
     * @return AgentBuilder实例
     */
    public AgentBuilder<T> withDependencies(Map<String, Object> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    /**
     * 设置Agent名称
     * @param agentName Agent名称
     * @return AgentBuilder实例
     */
    public AgentBuilder<T> withAgentName(String agentName) {
        this.agentName = agentName;
        return this;
    }

    /**
     * 设置Agent类型
     * @param agentType Agent类型
     * @return AgentBuilder实例
     */
    public AgentBuilder<T> withAgentType(String agentType) {
        this.agentType = agentType;
        return this;
    }
    
    /**
     * 设置SSE管理器
     * @param sseEmitterManager SSE管理器实例
     * @return AgentBuilder实例
     */
    public AgentBuilder<T> withSseEmitterManager(AiGlobalSseEmitterManager sseEmitterManager) {
        this.sseEmitterManager = sseEmitterManager;
        return this;
    }

    /**
     * 构建Agent实例
     * @param promptManager PromptManager实例
     * @return Agent实例
     */
    public T build(PromptManager promptManager) {
        try {
            T agent;
            
            // 根据构造方法参数创建Agent实例
            if (StrUtil.isNotBlank(agentName)) {
                // 使用带有系统提示词类型和PromptManager的构造方法
                try {
                    // 优先尝试三个参数的构造方法（ChatClient, String, PromptManager）
                    // 传递agentName作为systemPromptType，用于从PromptManager获取模板
                    agent = agentClass.getConstructor(ChatClient.class, String.class, PromptManager.class)
                            .newInstance(chatClient, agentName, promptManager);
                } catch (NoSuchMethodException e) {
                    // 如果没有三个参数的构造方法，尝试两个参数的构造方法
                    agent = agentClass.getConstructor(ChatClient.class, String.class)
                            .newInstance(chatClient, agentName);
                }
            } else {
                // 使用只有ChatClient的构造方法
                agent = agentClass.getConstructor(ChatClient.class).newInstance(chatClient);
            }

            // 设置默认提示词变量
            if (!defaultPromptVariables.isEmpty()) {
                agent.setDefaultPromptVariables(defaultPromptVariables);
            }

            // 设置ChatMemory
            // 注意：默认情况下不创建ChatMemory，必须从外部提供带有chatMemoryRepository的ChatMemory
            // 这样可以确保所有ChatMemory实例都使用JdbcChatMemoryRepository存储到数据库
            if (chatMemory != null) {
                agent.setChatMemory(chatMemory);
            }

            // 注入AiGlobalSseEmitterManager
            if (this.sseEmitterManager != null) {
                // 尝试通过setSseEmitterManager方法注入
                String setMethodName = "setSseEmitterManager";
                try {
                    Method setMethod = agent.getClass().getMethod(setMethodName, this.sseEmitterManager.getClass());
                    setMethod.invoke(agent, this.sseEmitterManager);
                    log.info("Successfully injected AiGlobalSseEmitterManager into agent: {}", agent.getClass().getSimpleName());
                } catch (NoSuchMethodException e) {
                    // 如果没有setSseEmitterManager方法，尝试直接设置字段
                    try {
                        Field sseEmitterManagerField = agent.getClass().getDeclaredField("sseEmitterManager");
                        sseEmitterManagerField.setAccessible(true);
                        sseEmitterManagerField.set(agent, this.sseEmitterManager);
                        log.info("Successfully injected AiGlobalSseEmitterManager via field into agent: {}", agent.getClass().getSimpleName());
                    } catch (NoSuchFieldException fieldEx) {
                        // 如果没有sseEmitterManager字段，跳过注入
                        log.info("Agent does not support AiGlobalSseEmitterManager injection: {}", fieldEx.getMessage());
                    }
                } catch (Exception e) {
                    log.error("Failed to inject AiGlobalSseEmitterManager into agent: {}", agent.getClass().getSimpleName(), e);
                }
            }

            // 通过反射注入工具和依赖服务
            injectToolsAndDependencies(agent);

            // 设置Agent名称和类型（如果Agent支持）
            setAgentNameAndType(agent);

            return agent;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build agent: " + e.getMessage(), e);
        }
    }

    /**
     * 通过反射注入工具和依赖服务
     * @param agent Agent实例
     */
    private void injectToolsAndDependencies(T agent) throws Exception {
        // 注入工具
        if (!tools.isEmpty()) {
            try {
                // 尝试传统的setTools方法注入
                Method setToolsMethod = agent.getClass().getMethod("setTools", List.class);
                setToolsMethod.invoke(agent, tools);
            } catch (NoSuchMethodException e) {
                // 如果Agent没有setTools方法，检查是否是ChatClient类型
                // 针对ChatClient及其实现类，使用tools方法注入
                try {
                    // 检查agent是否有client字段，可能是使用ChatClient的代理类
                    Field clientField = agent.getClass().getDeclaredField("client");
                    clientField.setAccessible(true);
                    Object client = clientField.get(agent);
                    
                    // 如果是ChatClient类型，直接在ChatClient上添加工具
                    if (client instanceof ChatClient) {
                        ChatClient chatClient = (ChatClient) client;
                        // 使用mutate()方法创建新的ChatClient构建器并添加工具
                        ChatClient.Builder builder = chatClient.mutate();
                        // 调用defaultTools方法添加工具数组
                        builder = builder.defaultTools(tools.toArray(new Object[0]));
                        // 构建新的ChatClient并设置回agent
                        ChatClient updatedChatClient = builder.build();
                        clientField.set(agent, updatedChatClient);
                        log.info("Successfully injected tools into ChatClient for agent: {}", agent.getClass().getSimpleName());
                    } else {
                        log.info("Agent does not support tools injection: {}", e.getMessage());
                    }
                } catch (NoSuchFieldException fieldEx) {
                    // 如果没有client字段，跳过工具注入
                    log.info("Agent does not support tools injection: {}", fieldEx.getMessage());
                }
            }
        }

        // 注入依赖服务
        for (Map.Entry<String, Object> entry : dependencies.entrySet()) {
            String dependencyName = entry.getKey();
            Object dependency = entry.getValue();
            
            // 尝试查找set方法
            String methodName = "set" + StrUtil.upperFirst(dependencyName);
            try {
                Method setMethod = agent.getClass().getMethod(methodName, dependency.getClass());
                setMethod.invoke(agent, dependency);
            } catch (NoSuchMethodException e) {
                // 如果Agent没有对应的set方法，跳过该依赖注入
                // 可以根据需要记录日志或抛出异常
            }
        }
    }

    /**
     * 设置Agent名称和类型（如果Agent支持）
     * @param agent Agent实例
     */
    private void setAgentNameAndType(T agent) throws Exception {
        // 设置Agent名称
        if (agentName != null) {
            try {
                Method setAgentNameMethod = agent.getClass().getMethod("setAgentName", String.class);
                setAgentNameMethod.invoke(agent, agentName);
            } catch (NoSuchMethodException e) {
                // 如果Agent没有setAgentName方法，跳过名称设置
            }
        }

        // 设置Agent类型
        if (agentType != null) {
            try {
                Method setAgentTypeMethod = agent.getClass().getMethod("setAgentType", String.class);
                setAgentTypeMethod.invoke(agent, agentType);
            } catch (NoSuchMethodException e) {
                // 如果Agent没有setAgentType方法，跳过类型设置
            }
        }
    }

    /**
     * 构建AgentConfig实例
     * @return AgentConfig实例
     */
    public AgentConfig buildAgentConfig() {
        return AgentConfig.builder()
                .chatClient(chatClient)
                .systemPrompt(systemPrompt)
                .defaultPromptVariables(defaultPromptVariables)
                .agentClass(agentClass)
                .chatMemory(chatMemory)
                .maxMessages(maxMessages)
                .build();
    }

    /**
     * 构建带有默认系统提示词、工具和ChatMemory的ChatClient
     * @return ChatClient实例
     */
    public ChatClient buildChatClient() {
        ChatClient.Builder builder;
        if (StrUtil.isBlank(systemPrompt)) {
            builder = chatClient.mutate();
        } else {
            builder = chatClient.mutate().defaultSystem(systemPrompt);
        }
        
        // 如果有工具，添加到ChatClient
        if (!tools.isEmpty()) {
            builder = builder.defaultTools(tools.toArray(new Object[0]));
        }
        
        // 如果提供了ChatMemory，添加到ChatClient
        if (chatMemory != null) {
            builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build());
        }
        
        return builder.build();
    }
}
