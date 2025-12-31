package com.ai.server.agent.ai.common.sse.service.impl;

import com.ai.server.agent.ai.common.sse.entity.SessionInfo;
import com.ai.server.agent.ai.common.sse.entity.SseMessage;
import com.ai.server.agent.ai.common.sse.repository.SessionInfoRepository;
import com.ai.server.agent.ai.common.sse.repository.SseMessageRepository;
import com.ai.server.agent.ai.common.sse.service.SessionService;
import com.ai.server.agent.ai.common.sse.entity.MessageGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 会话服务实现类，处理会话和消息的查询逻辑
 */
@Service
@Slf4j
public class SessionServiceImpl implements SessionService {
    
    @Autowired
    private SessionInfoRepository sessionInfoRepository;
    
    @Autowired
    private SseMessageRepository sseMessageRepository;

    @Override
    public List<SessionInfo> getSessionsByUserId(String userId) {
        try {
            return sessionInfoRepository.findActiveSessionsByUserId(userId);
        } catch (Exception e) {
            log.error("查询用户会话失败，用户ID: {}", userId, e);
            return List.of();
        }
    }

    @Override
    public List<SseMessage> getMessagesBySessionId(String sessionId) {
        try {
            return sseMessageRepository.findBySessionId(sessionId);
        } catch (Exception e) {
            log.error("查询会话消息失败，会话ID: {}", sessionId, e);
            return List.of();
        }
    }

    @Override
    public List<SseMessage> getMessagesByUserId(String userId) {
        try {
            return sseMessageRepository.findByUserId(userId);
        } catch (Exception e) {
            log.error("查询用户消息失败，用户ID: {}", userId, e);
            return List.of();
        }
    }



    @Override
    public List<MessageGroup> getGroupedMessagesBySessionId(String sessionId) {
        try {
            // 获取会话的所有消息
            List<SseMessage> messages = sseMessageRepository.findBySessionId(sessionId);
            
            // 按messageGroup分组
            return groupMessages(messages);
        } catch (Exception e) {
            log.error("查询会话分组消息失败，会话ID: {}", sessionId, e);
            return List.of();
        }
    }

    @Override
    public List<MessageGroup> getGroupedMessagesByUserId(String userId) {
        try {
            // 获取用户的所有消息
            List<SseMessage> messages = sseMessageRepository.findByUserId(userId);
            
            // 按messageGroup分组
            return groupMessages(messages);
        } catch (Exception e) {
            log.error("查询用户分组消息失败，用户ID: {}", userId, e);
            return List.of();
        }
    }

    @Override
    public SessionInfo getSessionById(String sessionId) {
        try {
            Optional<SessionInfo> sessionInfo = sessionInfoRepository.findBySessionId(sessionId);
            return sessionInfo.orElse(null);
        } catch (Exception e) {
            log.error("查询会话信息失败，会话ID: {}", sessionId, e);
            return null;
        }
    }

    @Override
    public SessionInfo createSession(String userId, String sessionName) {
        try {
            // 生成新的sessionId
            String newSessionId = java.util.UUID.randomUUID().toString();
            
            // 设置会话名称，如果没有提供则使用默认名称
            String finalSessionName = sessionName;
            if (finalSessionName == null || finalSessionName.isEmpty()) {
                finalSessionName = "新会话";
            }
            
            // 创建新的会话信息
            SessionInfo sessionInfo = SessionInfo.builder()
                    .sessionId(newSessionId)
                    .userId(userId)
                    .sessionName(finalSessionName)
                    .isActive(true)
                    .createdAt(java.time.LocalDateTime.now())
                    .lastActiveAt(java.time.LocalDateTime.now())
                    .expiredAt(java.time.LocalDateTime.now().plusYears(1)) // 会话有效期1年
                    .build();
            
            // 保存会话信息到数据库
            return sessionInfoRepository.save(sessionInfo);
        } catch (Exception e) {
            log.error("创建新会话失败，用户ID: {}", userId, e);
            return null;
        }
    }
    
    @Override
    public boolean deleteSession(String sessionId) {
        try {
            return sessionInfoRepository.deleteSession(sessionId);
        } catch (Exception e) {
            log.error("删除会话失败，会话ID: {}", sessionId, e);
            return false;
        }
    }
    
    /**
     * 将消息按messageGroup分组
     * @param messages 消息列表
     * @return 分组后的消息列表
     */
    private List<MessageGroup> groupMessages(List<SseMessage> messages) {
        // 使用LinkedHashMap保存分组顺序
        java.util.LinkedHashMap<String, MessageGroup> groupMap = new java.util.LinkedHashMap<>();
        
        // 遍历所有消息，按connectId分组（不再使用messageGroup）
        for (SseMessage message : messages) {
            String connectId = message.getConnectId();
            
            // 如果分组不存在，创建新分组
            if (!groupMap.containsKey(connectId)) {
                MessageGroup messageGroup = MessageGroup.builder()
                        .messageGroupId(connectId) // 使用connectId作为分组ID
                        .sessionId(message.getSessionId())
                        .userId(message.getUserId())
                        .messages(new java.util.ArrayList<>())
                        .createdAt(message.getSendTime())
                        .updatedAt(message.getSendTime())
                        .build();
                groupMap.put(connectId, messageGroup);
            }
            
            // 将消息添加到对应分组
            MessageGroup group = groupMap.get(connectId);
            group.getMessages().add(message);
            
            // 更新分组的最后更新时间
            if (message.getSendTime().isAfter(group.getUpdatedAt())) {
                group.setUpdatedAt(message.getSendTime());
            }
        }
        
        // 转换为列表并返回
        return new java.util.ArrayList<>(groupMap.values());
    }
}
