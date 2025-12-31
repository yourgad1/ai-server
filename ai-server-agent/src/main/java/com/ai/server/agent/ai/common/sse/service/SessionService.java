package com.ai.server.agent.ai.common.sse.service;

import com.ai.server.agent.ai.common.sse.entity.SessionInfo;
import com.ai.server.agent.ai.common.sse.entity.SseMessage;
import com.ai.server.agent.ai.common.sse.entity.MessageGroup;

import java.util.List;

/**
 * 会话服务接口，用于处理会话和消息的查询逻辑
 */
public interface SessionService {
    
    /**
     * 查询用户的所有会话
     * @param userId 用户ID
     * @return 会话列表
     */
    List<SessionInfo> getSessionsByUserId(String userId);
    
    /**
     * 查询会话的所有消息
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<SseMessage> getMessagesBySessionId(String sessionId);
    
    /**
     * 查询会话的所有消息，按分组返回
     * @param sessionId 会话ID
     * @return 分组消息列表
     */
    List<MessageGroup> getGroupedMessagesBySessionId(String sessionId);
    
    /**
     * 查询用户的所有消息
     * @param userId 用户ID
     * @return 消息列表
     */
    List<SseMessage> getMessagesByUserId(String userId);
    
    /**
     * 查询用户的所有消息，按分组返回
     * @param userId 用户ID
     * @return 分组消息列表
     */
    List<MessageGroup> getGroupedMessagesByUserId(String userId);
    
    /**
     * 根据会话ID获取会话信息
     * @param sessionId 会话ID
     * @return 会话信息
     */
    SessionInfo getSessionById(String sessionId);
    
    /**
     * 创建新会话
     * @param userId 用户ID
     * @param sessionName 会话名称（可选）
     * @return 新创建的会话信息
     */
    SessionInfo createSession(String userId, String sessionName);
    
    /**
     * 删除会话（逻辑删除，将is_active设置为2）
     * @param sessionId 会话ID
     * @return 删除是否成功
     */
    boolean deleteSession(String sessionId);
}
