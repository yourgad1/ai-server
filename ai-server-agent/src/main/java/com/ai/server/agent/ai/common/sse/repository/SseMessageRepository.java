package com.ai.server.agent.ai.common.sse.repository;

import com.ai.server.agent.ai.common.sse.entity.SseMessage;

import java.util.List;

/**
 * SSE消息仓库接口，定义SSE消息的数据库访问方法
 */
public interface SseMessageRepository {
    
    /**
     * 保存SSE消息
     * @param sseMessage SSE消息实体
     * @return 保存后的SSE消息实体
     */
    SseMessage save(SseMessage sseMessage);
    
    /**
     * 根据用户ID和会话ID查询SSE消息
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return SSE消息列表
     */
    List<SseMessage> findByUserIdAndSessionId(String userId, String sessionId);
    
    /**
     * 根据会话ID查询SSE消息
     * @param sessionId 会话ID
     * @return SSE消息列表
     */
    List<SseMessage> findBySessionId(String sessionId);
    
    /**
     * 根据用户ID查询SSE消息
     * @param userId 用户ID
     * @return SSE消息列表
     */
    List<SseMessage> findByUserId(String userId);
    
    /**
     * 根据智能体ID查询SSE消息
     * @param agentId 智能体ID
     * @return SSE消息列表
     */
    List<SseMessage> findByAgentId(String agentId);
    
    /**
     * 根据会话ID逻辑删除SSE消息
     * @param sessionId 会话ID
     * @return 删除的消息数量
     */
    int deleteBySessionId(String sessionId);
    
    /**
     * 根据用户ID逻辑删除SSE消息
     * @param userId 用户ID
     * @return 删除的消息数量
     */
    int deleteByUserId(String userId);
}