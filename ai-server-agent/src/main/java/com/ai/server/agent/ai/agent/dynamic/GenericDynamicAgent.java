package com.ai.server.agent.ai.agent.dynamic;

import com.ai.server.agent.ai.agent.core.Agent;
import com.ai.server.agent.ai.agent.manager.PromptManager;
import com.ai.server.agent.ai.agent.template.BaseAgent;
import com.ai.server.agent.ai.interceptor.UserContextHolder;
import com.ai.server.agent.ai.rest.response.ResponseAi;
import com.ai.server.agent.ai.util.ChatResponseToEntity;
import com.ai.server.agent.ai.util.ThinkContentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

/**
 * 通用动态Agent实现类，支持动态配置和运行时调整
 */
@Slf4j
public class GenericDynamicAgent extends BaseAgent<Object> {
    
    /**
     * Agent名称
     */
    private String agentName;
    
    /**
     * Agent类型
     */
    private String agentType;
    
    /**
     * 工具列表
     */
    private List<Object> tools;
    
    /**
     * 需要动态传入的提示词变量键名列表（var_type为dynamic）
     */
    private Set<String> dynamicPromptVariableKeys = new HashSet<>();
    
    /**
     * 需要运行时传入的提示词变量键名列表（var_type为runtime）
     */
    private Set<String> runtimePromptVariableKeys = new HashSet<>();
    
    /**
     * 聊天记忆配置
     */
    private ChatMemoryConfig chatMemoryConfig;
    
    /**
     * JdbcTemplate，用于更新聊天记录的agent_name
     */
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Spring应用上下文，用于获取ChatMemoryRepository
     */
    private org.springframework.context.ApplicationContext applicationContext;

    /**
     * 构造方法
     * @param chatClient ChatClient实例
     */
    public GenericDynamicAgent(ChatClient chatClient) {
        super(chatClient);
    }
    
    /**
     * 构造方法
     * @param chatClient ChatClient实例
     * @param systemPromptType 系统提示词类型
     */
    public GenericDynamicAgent(ChatClient chatClient, String systemPromptType) {
        super(chatClient, systemPromptType);
    }
    
    /**
     * 构造方法
     * @param chatClient ChatClient实例
     * @param systemPromptType 系统提示词类型
     * @param promptManager 提示词管理器
     */
    public GenericDynamicAgent(ChatClient chatClient, String systemPromptType, PromptManager promptManager) {
        super(chatClient, systemPromptType, promptManager);
    }
    
    @Override
    protected Object doChat(Agent.Request request, String type) {
        String content = null;
        String res = null;
        // 创建final变量保存agentName，确保在更新时不会被意外修改
        final String finalAgentName = this.agentName;
        
        // 检测动态和运行时变量是否被传入
        checkRequiredVariables(request);
        
        // 获取基础ChatClient
        ChatClient chatClient = getChatClient();
        
        // 在调用时构建ChatMemory，确保每次使用最新的记忆配置
        ChatMemory chatMemory = buildChatMemory(false);
        if (chatMemory != null) {
            // 创建带有ChatMemory的ChatClient
            chatClient = createChatClientWithMemory(chatClient, chatMemory);
            log.info("已创建带有ChatMemory的ChatClient");
        }

        
        // 保存原sessionId，用于存入session_id字段
        String sessionId = null;
        // 保存conversationId，用于更新聊天记录
        String conversationId = null;

        // 根据请求类型处理
        if (request instanceof Agent.MultiRequest) {
            Agent.MultiRequest multiRequest = (Agent.MultiRequest) request;
            // 获取会话ID（sessionId），用于存入session_id字段
            sessionId = multiRequest.getSessionId();
            
            // 获取用户ID，确保不同用户之间的聊天记忆隔离
            String userId = UserContextHolder.getInstance().getUserId();
            userId = userId != null ? userId : "test";
            
            // 确定最终会话ID：基于session_id、userId和agent_name生成，确保同一用户同一会话同一智能体共享记忆
            conversationId = sessionId + "_" + userId + "_" + this.agentName;
            // 创建final临时变量，用于lambda表达式
            final String finalConversationId = conversationId;

            // 调试：查询并打印当前会话的记忆记录
            debugSessionMemory(sessionId, this.agentName);
            
            // 处理多请求类型，设置conversation_id关联历史记录
            content = chatClient.prompt()
                    .messages(multiRequest.getMessage())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, finalConversationId))
                    .call()
                    .content();
                
        } else if (request instanceof Agent.ChatRequest) {
            Agent.ChatRequest chatRequest = (Agent.ChatRequest) request;
            // 处理聊天请求，使用Spring AI推荐的方式构建提示词
            ChatClient.ChatClientRequestSpec promptSpec = chatClient.prompt();
            
            // 如果有系统提示词类型，动态创建带有变量填充的系统提示词
            if (getSystemPromptType() != null && getPromptManager() != null) {
                // 1. 从PromptManager获取PromptTemplate
                PromptTemplate promptTemplate = getPromptTemplate(getSystemPromptType());
                // 2. 使用request.getContext()填充模板变量，创建完整的系统提示词字符串
                String systemPrompt = promptTemplate.render(request.getContext());
                // 3. 使用字符串类型的system方法
                promptSpec = promptSpec.system(systemPrompt);
            }
            
            // 获取会话ID（sessionId），用于存入session_id字段
            sessionId = chatRequest.getSessionId();
            
            // 获取用户ID，确保不同用户之间的聊天记忆隔离
            String userId = UserContextHolder.getInstance().getUserId();
            userId = userId != null ? userId : "test";
            
            // 确定最终会话ID：基于session_id、userId和agent_name生成，确保同一用户同一会话同一智能体共享记忆
            conversationId = sessionId + "_" + userId + "_" + this.agentName;
            // 创建final临时变量，用于lambda表达式
            final String finalConversationId = conversationId;

            // 调试：查询并打印当前会话的记忆记录
            debugSessionMemory(sessionId, this.agentName);
            
            // 调用AI模型并获取响应，设置conversation_id关联历史记录
            content = promptSpec.user(chatRequest.getMessage())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, finalConversationId))
                    .call()
                    .content();
        }
        
        // 处理响应内容
        if (content != null) {
            log.info(content);
            res = ThinkContentUtil.removeBeforeThink(content);
        }
       // 聊天完成后，更新本次会话的记录：设置agent_name和session_id
        if (conversationId != null) {
            updateChatMemoryAgentName(finalAgentName, sessionId, conversationId);
        }
        
        return ChatResponseToEntity.getJson(res);
    }
    
    /**
     * 检测动态和运行时变量是否被传入
     * @param request 请求对象
     */
    private void checkRequiredVariables(Agent.Request request) {
        if (request == null) {
            return;
        }
        
        // 获取请求上下文
        Map<String, Object> context = request.getContext();
        Set<String> providedKeys = context == null ? new HashSet<>() : context.keySet();
        
        // 检测dynamic类型变量
        for (String dynamicKey : dynamicPromptVariableKeys) {
            if (!providedKeys.contains(dynamicKey)) {
                log.warn("Agent [{}] - Dynamic variable [{}] is required but not provided", 
                        agentName, dynamicKey);
            }
        }
        
        // 检测runtime类型变量
        for (String runtimeKey : runtimePromptVariableKeys) {
            if (!providedKeys.contains(runtimeKey)) {
                log.warn("Agent [{}] - Runtime variable [{}] is required but not provided", 
                        agentName, runtimeKey);
            }
        }
    }
    
    @Override
    protected void doChatStream(Agent.Request request) throws Exception {
        // 检测动态和运行时变量是否被传入
        checkRequiredVariables(request);
        // 确保工具被注入到ChatClient中
        ChatClient chatClient = getChatClient();
        if (tools != null && !tools.isEmpty()) {
            chatClient = chatClient.mutate().defaultTools(tools.toArray(new Object[0])).build();
        }
        
        // 在调用时构建ChatMemory，确保每次使用最新的记忆配置
        ChatMemory chatMemory = buildChatMemory(true);
        if (chatMemory != null) {
            // 创建带有ChatMemory的ChatClient
            chatClient = createChatClientWithMemory(chatClient, chatMemory);
            log.info("已创建带有ChatMemory的ChatClient（流式）");
        }
        
        // 创建final变量保存agentName，确保在更新时不会被意外修改
        final String finalAgentName = this.agentName;
        
        // 保存原sessionId，用于存入session_id字段
        String sessionId;
        
        // 根据请求类型处理
        if (request instanceof Agent.MultiRequest) {
            Agent.MultiRequest multiRequest = (Agent.MultiRequest) request;
            // 获取会话ID（sessionId），用于存入session_id字段
            sessionId = multiRequest.getSessionId();
            
            // 获取用户ID，确保不同用户之间的聊天记忆隔离
            String userId = UserContextHolder.getInstance().getUserId();
            userId = userId != null ? userId : "test";
            
            // 确定最终会话ID：基于session_id、userId和agent_name生成，确保同一用户同一会话同一智能体共享记忆
            final String conversationId = sessionId + "_" + userId + "_" + this.agentName;

            // 处理多请求类型的流式响应，设置conversation_id关联历史记录
            chatClient.prompt()
                    .messages(multiRequest.getMessage())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .stream()
                    .content()
                    .subscribe(
                            content -> {
                                // 通过SSE发送到前端
                                if (multiRequest.getConnId() != null) {
                                    getSseEmitterManager().sendEvent(multiRequest.getConnId(), ResponseAi.ofMessage(content));
                                } else {
                                    // 如果没有连接ID，打印到控制台
                                    System.out.print(content);
                                }
                            },
                            error -> {
                                log.error("流式聊天发生错误", error);
                            },
                            () -> {
                                log.info("已完成聊天记录的流式响应");
                                // 聊天完成后，更新本次会话的记录：设置agent_name和session_id
                                updateChatMemoryAgentName(finalAgentName, sessionId, conversationId);
                            }
                    );
        } else if (request instanceof Agent.ChatRequest) {
            Agent.ChatRequest chatRequest = (Agent.ChatRequest) request;
            // 处理聊天请求的流式响应，使用Spring AI推荐的方式构建提示词
            ChatClient.ChatClientRequestSpec promptSpec = chatClient.prompt();
            
            // 如果有系统提示词类型，动态创建带有变量填充的系统提示词
            if (getSystemPromptType() != null && getPromptManager() != null) {
                // 1. 从PromptManager获取PromptTemplate
                PromptTemplate promptTemplate = getPromptTemplate(getSystemPromptType());
                // 2. 使用request.getContext()填充模板变量，创建完整的系统提示词字符串
                String systemPrompt = promptTemplate.render(request.getContext());
                // 3. 使用字符串类型的system方法
                promptSpec = promptSpec.system(systemPrompt);
            }
            
            // 获取会话ID（sessionId），用于存入session_id字段
            sessionId = chatRequest.getSessionId();
            
            // 获取用户ID，确保不同用户之间的聊天记忆隔离
            String userId = UserContextHolder.getInstance().getUserId();
            userId = userId != null ? userId : "test";
            
            // 确定最终会话ID：基于session_id、userId和agent_name生成，确保同一用户同一会话同一智能体共享记忆
            final String conversationId = sessionId + "_" + userId + "_" + this.agentName;

            // 调用AI模型并获取流式响应，设置conversation_id关联历史记录
            promptSpec.user(chatRequest.getMessage())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .stream()
                    .content()
                    .subscribe(
                            content -> {
                                // 通过SSE发送到前端
                                if (chatRequest.getConnId() != null) {
                                    getSseEmitterManager().sendEvent(chatRequest.getConnId(), ResponseAi.ofMessage(content));
                                } else {
                                    // 如果没有连接ID，打印到控制台
                                    System.out.print(content);
                                }
                            },
                            error -> {
                                log.error("流式聊天发生错误", error);
                            },
                            () -> {
                                log.info("已完成聊天记录的流式响应");
                                // 流式响应完成后，更新当前会话的记录：设置agent_name和session_id
                                updateChatMemoryAgentName(finalAgentName, sessionId, conversationId);
                            }
                    );
        } else {
            sessionId = null;
        }
    }
    
    /**
     * 创建带有ChatMemory的ChatClient
     * @param chatClient 原始ChatClient
     * @param chatMemory ChatMemory实例
     * @return 带有ChatMemory的ChatClient
     */
    protected ChatClient createChatClientWithMemory(ChatClient chatClient, ChatMemory chatMemory) {
        return chatClient.mutate()
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
    
    /**
     * 创建默认的ChatMemory
     * @param maxMessages 最大消息数
     * @return ChatMemory实例
     */
    protected ChatMemory createDefaultChatMemory(int maxMessages) {
        // 注意：默认创建的ChatMemory没有设置chatMemoryRepository，需要在外部手动设置
        // 或者通过依赖注入获取chatMemoryRepository并设置
        return MessageWindowChatMemory.builder()
                .maxMessages(maxMessages)
                .build();
    }
    
    /**
     * 构建ChatMemory实例
     * @param isStream 是否为流式请求
     * @return 构建好的ChatMemory实例
     */
    private ChatMemory buildChatMemory(boolean isStream) {
        ChatMemory chatMemory = null;
        if (chatMemoryConfig != null && chatMemoryConfig.isEnabled()) {
            String logPrefix = isStream ? "（流式）" : "";
            log.info("正在构建ChatMemory{}{}，配置: memoryType={}, maxMessages={}", 
                    logPrefix, isStream ? "" : "",
                    chatMemoryConfig.getMemoryType(), chatMemoryConfig.getMaxMessages());
            
            // 根据最新配置创建ChatMemory实例
            // 记忆构建使用session_id字段的值，确保构建的记忆是该会话中属于该智能体的记忆
            chatMemory = MessageWindowChatMemory.builder()
                    .chatMemoryRepository(applicationContext.getBean("chatMemoryRepository", org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository.class))
                    .maxMessages(chatMemoryConfig.getMaxMessages())
                    .build();
            
            log.info("ChatMemory{}{}构建完成，maxMessages={}", 
                    logPrefix, isStream ? "" : "", chatMemoryConfig.getMaxMessages());
        } else {
            log.info("未构建ChatMemory{}{}，原因: chatMemoryConfig={}, isEnabled={}", 
                    isStream ? "（流式）" : "", isStream ? "" : "",
                    chatMemoryConfig, chatMemoryConfig != null ? chatMemoryConfig.isEnabled() : false);
        }
        return chatMemory;
    }
    
    /**
     * 更新聊天记录的agent_name和session_id
     * @param agentName 智能体名称
     * @param sessionId 会话ID
     * @param conversationId 对话ID
     */
    private void updateChatMemoryAgentName(String agentName, String sessionId, String conversationId) {
        if (jdbcTemplate != null) {
            String userId = UserContextHolder.getInstance().getUserId();
            userId = userId != null ? userId : "test";
            
            // 更新当前conversation_id下的所有记录：设置agent_name和session_id
            String updateSql = "UPDATE SPRING_AI_CHAT_MEMORY SET agent_name = ?, session_id = ? WHERE conversation_id = ? AND user_id = ? AND agent_name IS NULL;";
            int updatedCount = jdbcTemplate.update(updateSql, agentName, sessionId, conversationId, userId);
            
            log.info("已更新聊天记录的agent_name: {}, session_id: {}, conversation_id: {}, 用户ID: {}, 更新数量: {}", 
                    agentName, sessionId, conversationId, userId, updatedCount);
        }
    }
    
    /**
     * 调试：查询并打印当前会话的记忆记录
     * @param sessionId 会话ID
     * @param agentName 智能体名称
     */
    private void debugSessionMemory(String sessionId, String agentName) {
        if (jdbcTemplate != null && sessionId != null) {
            String userId = UserContextHolder.getInstance().getUserId();
            userId = userId != null ? userId : "test";
            
            // 执行SQL查询，查看当前会话的记忆记录
            String debugSql = "SELECT id, content, type, conversation_id, timestamp FROM SPRING_AI_CHAT_MEMORY WHERE session_id = ? AND agent_name = ? AND user_id = ? ORDER BY timestamp DESC LIMIT ?";
            List<Map<String, Object>> memoryRecords = jdbcTemplate.queryForList(debugSql, sessionId, agentName, userId, chatMemoryConfig.getMaxMessages());
            
            log.info("=== 调试：当前会话记忆记录查询结果 ===");
            log.info("SQL: {}", debugSql);
            log.info("参数: session_id={}, agent_name={}, user_id={}, limit={}", sessionId, agentName, userId, chatMemoryConfig.getMaxMessages());
            log.info("查询到 {} 条记录", memoryRecords.size());
            for (int i = 0; i < memoryRecords.size(); i++) {
                Map<String, Object> record = memoryRecords.get(i);
                log.info("记录[{}]: id={}, type={}, content={}, timestamp={}, conversation_id={}", 
                        i, record.get("id"), record.get("type"), record.get("content"), record.get("timestamp"), record.get("conversation_id"));
            }
            log.info("=================================");
        }
    }
    

    
    // getter和setter方法
    public String getAgentName() {
        return agentName;
    }
    
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
    
    public String getAgentType() {
        return agentType;
    }
    
    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }
    
    public List<Object> getTools() {
        return tools;
    }
    
    public void setTools(List<Object> tools) {
        this.tools = tools;
    }
    
    public Set<String> getDynamicPromptVariableKeys() {
        return dynamicPromptVariableKeys;
    }

    public void setDynamicPromptVariableKeys(Set<String> dynamicPromptVariableKeys) {
        this.dynamicPromptVariableKeys = dynamicPromptVariableKeys;
    }
    
    public void setDynamicPromptVariableKeys(List<String> dynamicPromptVariableKeys) {
        this.dynamicPromptVariableKeys = new HashSet<>(dynamicPromptVariableKeys);
    }

    public Set<String> getRuntimePromptVariableKeys() {
        return runtimePromptVariableKeys;
    }

    public void setRuntimePromptVariableKeys(Set<String> runtimePromptVariableKeys) {
        this.runtimePromptVariableKeys = runtimePromptVariableKeys;
    }

    public void setRuntimePromptVariableKeys(List<String> runtimePromptVariableKeys) {
        this.runtimePromptVariableKeys = new HashSet<>(runtimePromptVariableKeys);
    }
    
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setChatMemoryConfig(ChatMemoryConfig chatMemoryConfig) {
        this.chatMemoryConfig = chatMemoryConfig;
    }
    
    public void setApplicationContext(org.springframework.context.ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
