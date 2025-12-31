package com.ai.server.agent.ai.common.sse.repository.impl;

import com.ai.server.agent.ai.common.sse.entity.SseMessage;
import com.ai.server.agent.ai.common.sse.repository.SseMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * 直接访问数据库的SSE消息仓库实现
 */
@Slf4j
public class DirectSseMessageRepository implements SseMessageRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public SseMessage save(SseMessage sseMessage) {
        try {
            // 设置发送时间
            sseMessage.setSendTime(LocalDateTime.now());
            
            // 生成UUID作为消息ID
            if (sseMessage.getId() == null || sseMessage.getId().isEmpty()) {
                sseMessage.setId(java.util.UUID.randomUUID().toString());
            }
            
            // 插入SSE消息到数据库
            String sql = "INSERT INTO sse_message (id, user_id, session_id, connect_id, agent_id, message_type, message_content, send_time, is_deleted) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            jdbcTemplate.update(sql, 
                    sseMessage.getId(),
                    sseMessage.getUserId(),
                    sseMessage.getSessionId(),
                    sseMessage.getConnectId(),
                    sseMessage.getAgentId(),
                    sseMessage.getMessageType(),
                    sseMessage.getMessageContent(),
                    sseMessage.getSendTime(),
                    sseMessage.isDeleted());
            
            return sseMessage;
        } catch (Exception e) {
            log.error("保存SSE消息到数据库失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<SseMessage> findByUserIdAndSessionId(String userId, String sessionId) {
        try {
            String sql = "SELECT * FROM sse_message WHERE user_id = ? AND session_id = ? AND is_deleted = 0 ORDER BY send_time ASC";
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapToSseMessage(rs), userId, sessionId);
        } catch (Exception e) {
            log.error("查询用户会话消息失败: {}, {}", userId, sessionId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<SseMessage> findBySessionId(String sessionId) {
        try {
            String sql = "SELECT * FROM sse_message WHERE session_id = ? AND is_deleted = 0 ORDER BY send_time ASC";
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapToSseMessage(rs), sessionId);
        } catch (Exception e) {
            log.error("查询会话消息失败: {}", sessionId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<SseMessage> findByUserId(String userId) {
        try {
            String sql = "SELECT * FROM sse_message WHERE user_id = ? AND is_deleted = 0 ORDER BY send_time ASC";
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapToSseMessage(rs), userId);
        } catch (Exception e) {
            log.error("查询用户消息失败: {}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<SseMessage> findByAgentId(String agentId) {
        try {
            String sql = "SELECT * FROM sse_message WHERE agent_id = ? AND is_deleted = 0 ORDER BY send_time ASC";
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapToSseMessage(rs), agentId);
        } catch (Exception e) {
            log.error("查询智能体消息失败: {}", agentId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public int deleteBySessionId(String sessionId) {
        try {
            String sql = "UPDATE sse_message SET is_deleted = 1 WHERE session_id = ?";
            return jdbcTemplate.update(sql, sessionId);
        } catch (Exception e) {
            log.error("逻辑删除会话消息失败: {}", sessionId, e);
            return 0;
        }
    }

    @Override
    public int deleteByUserId(String userId) {
        try {
            String sql = "UPDATE sse_message SET is_deleted = 1 WHERE user_id = ?";
            return jdbcTemplate.update(sql, userId);
        } catch (Exception e) {
            log.error("逻辑删除用户消息失败: {}", userId, e);
            return 0;
        }
    }

    /**
     * 将数据库查询结果映射为SseMessage对象
     * @param rs 数据库查询结果集
     * @return SseMessage对象
     */
    private SseMessage mapToSseMessage(java.sql.ResultSet rs) throws java.sql.SQLException {
        SseMessage message = SseMessage.builder()
                .id(rs.getString("id"))
                .userId(rs.getString("user_id"))
                .sessionId(rs.getString("session_id"))
                .connectId(rs.getString("connect_id"))
                .messageType(rs.getString("message_type"))
                .messageContent(rs.getString("message_content"))
                .sendTime(rs.getObject("send_time", LocalDateTime.class))
                .build();
        
        // 检查agent_id字段是否存在（兼容旧版本表结构）
        try {
            String agentId = rs.getString("agent_id");
            message.setAgentId(agentId);
        } catch (java.sql.SQLException e) {
            // 如果agent_id字段不存在，忽略该异常
            log.debug("表中不存在agent_id字段，忽略该字段映射");
        }
        
        // 检查is_deleted字段是否存在（兼容旧版本表结构）
        try {
            boolean isDeleted = rs.getBoolean("is_deleted");
            // 使用builder模式重新构建对象，添加isDeleted字段
            message = SseMessage.builder()
                    .id(message.getId())
                    .userId(message.getUserId())
                    .sessionId(message.getSessionId())
                    .connectId(message.getConnectId())
                    .agentId(message.getAgentId())
                    .messageType(message.getMessageType())
                    .messageContent(message.getMessageContent())
                    .sendTime(message.getSendTime())
                    .isDeleted(isDeleted)
                    .build();
        } catch (java.sql.SQLException e) {
            // 如果is_deleted字段不存在，使用默认值false
            log.debug("表中不存在is_deleted字段，使用默认值false");
            // 使用builder模式重新构建对象，设置isDeleted为默认值false
            message = SseMessage.builder()
                    .id(message.getId())
                    .userId(message.getUserId())
                    .sessionId(message.getSessionId())
                    .connectId(message.getConnectId())
                    .agentId(message.getAgentId())
                    .messageType(message.getMessageType())
                    .messageContent(message.getMessageContent())
                    .sendTime(message.getSendTime())
                    .isDeleted(false)
                    .build();
        }
        
        return message;
    }
}
