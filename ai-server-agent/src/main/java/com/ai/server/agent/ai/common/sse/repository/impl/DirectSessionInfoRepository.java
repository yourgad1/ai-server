package com.ai.server.agent.ai.common.sse.repository.impl;

import com.ai.server.agent.ai.common.sse.entity.SessionInfo;
import com.ai.server.agent.ai.common.sse.repository.SessionInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 直接访问数据库的会话信息仓库实现
 */
@Slf4j
public class DirectSessionInfoRepository implements SessionInfoRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public SessionInfo save(SessionInfo sessionInfo) {
        try {
            // 构建插入或更新SQL
            String sql;
            List<Object> params = new ArrayList<>();
            
            // 查询会话是否存在
            Optional<SessionInfo> existingSession = findBySessionId(sessionInfo.getSessionId());
            if (existingSession.isPresent()) {
                // 更新会话
                sql = "UPDATE session_info SET user_id = ?, session_name = ?, is_active = ?, last_active_at = ?, expired_at = ? " +
                      "WHERE session_id = ?";
                params.add(sessionInfo.getUserId());
                params.add(sessionInfo.getSessionName());
                params.add(sessionInfo.getIsActive() ? 1 : 0);
                params.add(sessionInfo.getLastActiveAt());
                params.add(sessionInfo.getExpiredAt());
                params.add(sessionInfo.getSessionId());
            } else {
                // 插入新会话
                sql = "INSERT INTO session_info (session_id, user_id, session_name, is_active, created_at, last_active_at, expired_at) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
                params.add(sessionInfo.getSessionId());
                params.add(sessionInfo.getUserId());
                params.add(sessionInfo.getSessionName());
                params.add(sessionInfo.getIsActive() ? 1 : 0);
                params.add(LocalDateTime.now());
                params.add(LocalDateTime.now());
                params.add(sessionInfo.getExpiredAt());
            }
            
            // 执行SQL
            jdbcTemplate.update(sql, params.toArray());
            
            log.info("会话信息保存成功，会话ID: {}", sessionInfo.getSessionId());
            return sessionInfo;
        } catch (Exception e) {
            log.error("会话信息保存异常", e);
            return null;
        }
    }

    @Override
    public Optional<SessionInfo> findBySessionId(String sessionId) {
        try {
            // 构建查询SQL
            String sql = "SELECT session_id, user_id, session_name, is_active, created_at, last_active_at, expired_at " +
                        "FROM session_info " +
                        "WHERE session_id = ?";
            
            // 执行查询操作
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sessionId);
            
            // 映射结果
            if (!rows.isEmpty()) {
                return Optional.of(mapToSessionInfo(rows.get(0)));
            }
            
            return Optional.empty();
        } catch (Exception e) {
            log.error("根据会话ID查询会话信息异常", e);
            return Optional.empty();
        }
    }

    @Override
    public List<SessionInfo> findActiveSessionsByUserId(String userId) {
        try {
            // 构建查询SQL
            String sql = "SELECT session_id, user_id, session_name, is_active, created_at, last_active_at, expired_at " +
                        "FROM session_info " +
                        "WHERE user_id = ? AND is_active = 1 " +
                        "ORDER BY last_active_at DESC";
            
            // 执行查询操作
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId);
            
            // 映射结果
            List<SessionInfo> sessionInfos = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                sessionInfos.add(mapToSessionInfo(row));
            }
            
            return sessionInfos;
        } catch (Exception e) {
            log.error("根据用户ID查询活跃会话异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean markSessionActive(String sessionId) {
        try {
            // 构建更新SQL
            String sql = "UPDATE session_info SET is_active = 1, last_active_at = ? WHERE session_id = ?";
            
            // 执行更新操作
            int rowsAffected = jdbcTemplate.update(sql, LocalDateTime.now(), sessionId);
            
            return rowsAffected > 0;
        } catch (Exception e) {
            log.error("标记会话为活跃状态异常", e);
            return false;
        }
    }

    @Override
    public boolean markSessionInactive(String sessionId) {
        try {
            // 构建更新SQL
            String sql = "UPDATE session_info SET is_active = 0, last_active_at = ? WHERE session_id = ?";
            
            // 执行更新操作
            int rowsAffected = jdbcTemplate.update(sql, LocalDateTime.now(), sessionId);
            
            return rowsAffected > 0;
        } catch (Exception e) {
            log.error("标记会话为不活跃状态异常", e);
            return false;
        }
    }

    @Override
    public int deleteExpiredSessions() {
        try {
            // 构建删除SQL
            String sql = "DELETE FROM session_info WHERE expired_at IS NOT NULL AND expired_at < ?";
            
            // 执行删除操作
            int rowsAffected = jdbcTemplate.update(sql, LocalDateTime.now());
            
            return rowsAffected;
        } catch (Exception e) {
            log.error("删除过期会话异常", e);
            return 0;
        }
    }
    
    @Override
    public boolean deleteSession(String sessionId) {
        try {
            // 构建更新SQL，将is_active设置为2表示删除
            String sql = "UPDATE session_info SET is_active = 2, last_active_at = ? WHERE session_id = ?";
            
            // 执行更新操作
            int rowsAffected = jdbcTemplate.update(sql, LocalDateTime.now(), sessionId);
            
            log.info("会话已删除，会话ID: {}, 更新行数: {}", sessionId, rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            log.error("删除会话异常，会话ID: {}", sessionId, e);
            return false;
        }
    }
    
    /**
     * 将单条查询结果映射为SessionInfo对象
     * @param row 单条查询结果
     * @return SessionInfo对象
     */
    private SessionInfo mapToSessionInfo(Map<String, Object> row) {
        try {
            // 处理is_active字段，兼容Boolean和Integer类型
            Object isActiveObj = row.get("is_active");
            boolean isActive;
            if (isActiveObj instanceof Boolean) {
                isActive = (Boolean) isActiveObj;
            } else if (isActiveObj instanceof Integer) {
                isActive = ((Integer) isActiveObj) == 1;
            } else {
                // 处理其他可能的类型
                isActive = Boolean.parseBoolean(String.valueOf(isActiveObj));
            }
            
            // 处理日期时间字段，兼容java.sql.Timestamp和LocalDateTime类型
            LocalDateTime createdAt = null;
            LocalDateTime lastActiveAt = null;
            LocalDateTime expiredAt = null;
            
            // 处理created_at字段
            Object createdAtObj = row.get("created_at");
            if (createdAtObj != null) {
                if (createdAtObj instanceof java.sql.Timestamp) {
                    createdAt = ((java.sql.Timestamp) createdAtObj).toLocalDateTime();
                } else if (createdAtObj instanceof java.util.Date) {
                    createdAt = ((java.util.Date) createdAtObj).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                } else if (createdAtObj instanceof LocalDateTime) {
                    createdAt = (LocalDateTime) createdAtObj;
                }
            }
            
            // 处理last_active_at字段
            Object lastActiveAtObj = row.get("last_active_at");
            if (lastActiveAtObj != null) {
                if (lastActiveAtObj instanceof java.sql.Timestamp) {
                    lastActiveAt = ((java.sql.Timestamp) lastActiveAtObj).toLocalDateTime();
                } else if (lastActiveAtObj instanceof java.util.Date) {
                    lastActiveAt = ((java.util.Date) lastActiveAtObj).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                } else if (lastActiveAtObj instanceof LocalDateTime) {
                    lastActiveAt = (LocalDateTime) lastActiveAtObj;
                }
            }
            
            // 处理expired_at字段
            Object expiredAtObj = row.get("expired_at");
            if (expiredAtObj != null) {
                if (expiredAtObj instanceof java.sql.Timestamp) {
                    expiredAt = ((java.sql.Timestamp) expiredAtObj).toLocalDateTime();
                } else if (expiredAtObj instanceof java.util.Date) {
                    expiredAt = ((java.util.Date) expiredAtObj).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                } else if (expiredAtObj instanceof LocalDateTime) {
                    expiredAt = (LocalDateTime) expiredAtObj;
                }
            }
            
            return SessionInfo.builder()
                    .sessionId((String) row.get("session_id"))
                    .userId((String) row.get("user_id"))
                    .sessionName((String) row.get("session_name"))
                    .isActive(isActive)
                    .createdAt(createdAt)
                    .lastActiveAt(lastActiveAt)
                    .expiredAt(expiredAt)
                    .build();
        } catch (Exception e) {
            log.error("映射会话信息失败，行数据: {}", row, e);
            return null;
        }
    }
}