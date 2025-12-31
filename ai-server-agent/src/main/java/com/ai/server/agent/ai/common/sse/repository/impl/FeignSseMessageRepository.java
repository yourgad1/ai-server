package com.ai.server.agent.ai.common.sse.repository.impl;

import com.ai.server.agent.ai.common.sse.entity.SseMessage;
import com.ai.server.agent.ai.common.sse.repository.SseMessageRepository;
import com.ai.server.agent.ai.feign.DataAccessMysqlClient;
import com.ai.server.agent.ai.feign.dto.R;
import com.ai.server.agent.core.jdbc.RemoteSqlDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 使用Feign访问数据库的SSE消息仓库实现
 */
@Slf4j
public class FeignSseMessageRepository implements SseMessageRepository {
    
    @Autowired
    private DataAccessMysqlClient dataAccessMysqlClient;
    
    @Value("${data-access.app-id:default}")
    private String appId;
    
    @Value("${data-access.data-source:default}")
    private String dataSource;
    
    @Override
    public SseMessage save(SseMessage sseMessage) {
        try {
            // 生成UUID作为消息ID
            if (sseMessage.getId() == null || sseMessage.getId().isEmpty()) {
                sseMessage.setId(UUID.randomUUID().toString());
            }
            
            // 设置发送时间
            if (sseMessage.getSendTime() == null) {
                sseMessage.setSendTime(LocalDateTime.now());
            }
            
            // 构建插入SQL
            String sql = "INSERT INTO sse_message (id, user_id, session_id, connect_id, agent_id, message_type, message_content, send_time, is_deleted) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            // 构建参数列表
            List<Object> params = new ArrayList<>();
            params.add(sseMessage.getId());
            params.add(sseMessage.getUserId());
            params.add(sseMessage.getSessionId());
            params.add(sseMessage.getConnectId());
            params.add(sseMessage.getAgentId());
            params.add(sseMessage.getMessageType());
            params.add(sseMessage.getMessageContent());
            params.add(sseMessage.getSendTime());
            params.add(sseMessage.isDeleted());
            
            // 构建RemoteSqlDTO
            RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                    .appId(appId)
                    .sql(sql)
                    .params(params)
                    .dataSource(dataSource)
                    .build();
            
            // 执行插入操作
            R<Integer> result = dataAccessMysqlClient.update(remoteSqlDTO);
            
            if (result != null && result.getData() != null && result.getData() > 0) {
                log.info("SSE消息保存成功，消息ID: {}", sseMessage.getId());
                return sseMessage;
            } else {
                log.error("SSE消息保存失败，返回结果: {}", result);
                return null;
            }
        } catch (Exception e) {
            log.error("SSE消息保存异常", e);
            return null;
        }
    }
    
    @Override
    public List<SseMessage> findByUserIdAndSessionId(String userId, String sessionId) {
        try {
            // 构建查询SQL
            String sql = "SELECT id, user_id, session_id, connect_id, agent_id, message_type, message_content, send_time, is_deleted " +
                        "FROM sse_message " +
                        "WHERE user_id = ? AND session_id = ? AND is_deleted = 0 " +
                        "ORDER BY send_time ASC";
            
            // 构建参数列表
            List<Object> params = new ArrayList<>();
            params.add(userId);
            params.add(sessionId);
            
            // 构建RemoteSqlDTO
            RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                    .appId(appId)
                    .sql(sql)
                    .params(params)
                    .dataSource(dataSource)
                    .build();
            
            // 执行查询操作
            R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
            
            return mapToSseMessages(result);
        } catch (Exception e) {
            log.error("根据用户ID和会话ID查询SSE消息异常", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<SseMessage> findBySessionId(String sessionId) {
        try {
            // 构建查询SQL
            String sql = "SELECT id, user_id, session_id, connect_id, agent_id, message_type, message_content, send_time, is_deleted " +
                        "FROM sse_message " +
                        "WHERE session_id = ? AND is_deleted = 0 " +
                        "ORDER BY send_time ASC";
            
            // 构建参数列表
            List<Object> params = new ArrayList<>();
            params.add(sessionId);
            
            // 构建RemoteSqlDTO
            RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                    .appId(appId)
                    .sql(sql)
                    .params(params)
                    .dataSource(dataSource)
                    .build();
            
            // 执行查询操作
            R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
            
            return mapToSseMessages(result);
        } catch (Exception e) {
            log.error("根据会话ID查询SSE消息异常", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<SseMessage> findByAgentId(String agentId) {
        try {
            // 构建查询SQL
            String sql = "SELECT id, user_id, session_id, connect_id, agent_id, message_type, message_content, send_time, is_deleted " +
                        "FROM sse_message " +
                        "WHERE agent_id = ? AND is_deleted = 0 " +
                        "ORDER BY send_time ASC";
            
            // 构建参数列表
            List<Object> params = new ArrayList<>();
            params.add(agentId);
            
            // 构建RemoteSqlDTO
            RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                    .appId(appId)
                    .sql(sql)
                    .params(params)
                    .dataSource(dataSource)
                    .build();
            
            // 执行查询操作
            R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
            
            return mapToSseMessages(result);
        } catch (Exception e) {
            log.error("根据智能体ID查询SSE消息异常", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<SseMessage> findByUserId(String userId) {
        try {
            // 构建查询SQL
            String sql = "SELECT id, user_id, session_id, connect_id, agent_id, message_type, message_content, send_time, is_deleted " +
                        "FROM sse_message " +
                        "WHERE user_id = ? AND is_deleted = 0 " +
                        "ORDER BY send_time ASC";
            
            // 构建参数列表
            List<Object> params = new ArrayList<>();
            params.add(userId);
            
            // 构建RemoteSqlDTO
            RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                    .appId(appId)
                    .sql(sql)
                    .params(params)
                    .dataSource(dataSource)
                    .build();
            
            // 执行查询操作
            R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
            
            return mapToSseMessages(result);
        } catch (Exception e) {
            log.error("根据用户ID查询SSE消息异常", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public int deleteBySessionId(String sessionId) {
        try {
            // 构建更新SQL，实现逻辑删除
            String sql = "UPDATE sse_message SET is_deleted = 1 WHERE session_id = ?";
            
            // 构建参数列表
            List<Object> params = new ArrayList<>();
            params.add(sessionId);
            
            // 构建RemoteSqlDTO
            RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                    .appId(appId)
                    .sql(sql)
                    .params(params)
                    .dataSource(dataSource)
                    .build();
            
            // 执行更新操作
            R<Integer> result = dataAccessMysqlClient.update(remoteSqlDTO);
            
            if (result != null && result.getData() != null) {
                return result.getData();
            } else {
                log.error("逻辑删除会话消息失败，返回结果: {}", result);
                return 0;
            }
        } catch (Exception e) {
            log.error("逻辑删除会话消息异常", e);
            return 0;
        }
    }
    
    @Override
    public int deleteByUserId(String userId) {
        try {
            // 构建更新SQL，实现逻辑删除
            String sql = "UPDATE sse_message SET is_deleted = 1 WHERE user_id = ?";
            
            // 构建参数列表
            List<Object> params = new ArrayList<>();
            params.add(userId);
            
            // 构建RemoteSqlDTO
            RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                    .appId(appId)
                    .sql(sql)
                    .params(params)
                    .dataSource(dataSource)
                    .build();
            
            // 执行更新操作
            R<Integer> result = dataAccessMysqlClient.update(remoteSqlDTO);
            
            if (result != null && result.getData() != null) {
                return result.getData();
            } else {
                log.error("逻辑删除用户消息失败，返回结果: {}", result);
                return 0;
            }
        } catch (Exception e) {
            log.error("逻辑删除用户消息异常", e);
            return 0;
        }
    }
    
    /**
     * 将查询结果映射为SseMessage对象列表
     * @param result 查询结果
     * @return SseMessage对象列表
     */
    private List<SseMessage> mapToSseMessages(R<List<Map<String, Object>>> result) {
        List<SseMessage> sseMessages = new ArrayList<>();
        
        if (result != null && result.getData() != null) {
            for (Map<String, Object> row : result.getData()) {
                SseMessage sseMessage = mapToSseMessage(row);
                if (sseMessage != null) {
                    sseMessages.add(sseMessage);
                }
            }
        }
        
        return sseMessages;
    }
    
    /**
     * 将单条查询结果映射为SseMessage对象
     * @param row 单条查询结果
     * @return SseMessage对象
     */
    private SseMessage mapToSseMessage(Map<String, Object> row) {
        try {
            // 检查is_deleted字段是否存在（兼容旧版本表结构）
            boolean isDeleted = false;
            if (row.containsKey("is_deleted")) {
                Object isDeletedObj = row.get("is_deleted");
                if (isDeletedObj != null) {
                    if (isDeletedObj instanceof Boolean) {
                        isDeleted = (Boolean) isDeletedObj;
                    } else if (isDeletedObj instanceof Number) {
                        isDeleted = ((Number) isDeletedObj).intValue() == 1;
                    } else {
                        isDeleted = Boolean.parseBoolean(isDeletedObj.toString());
                    }
                }
            }
            
            return SseMessage.builder()
                    .id((String) row.get("id"))
                    .userId((String) row.get("user_id"))
                    .sessionId((String) row.get("session_id"))
                    .connectId((String) row.get("connect_id"))
                    .agentId((String) row.get("agent_id"))
                    .messageType((String) row.get("message_type"))
                    .messageContent((String) row.get("message_content"))
                    .sendTime((LocalDateTime) row.get("send_time"))
                    .isDeleted(isDeleted)
                    .build();
        } catch (Exception e) {
            log.error("映射SSE消息失败，行数据: {}", row, e);
            return null;
        }
    }
}