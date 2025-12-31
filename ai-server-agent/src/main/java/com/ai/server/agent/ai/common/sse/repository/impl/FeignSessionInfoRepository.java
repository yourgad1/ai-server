package com.ai.server.agent.ai.common.sse.repository.impl;

import com.ai.server.agent.ai.common.sse.entity.SessionInfo;
import com.ai.server.agent.ai.common.sse.repository.SessionInfoRepository;
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
import java.util.Optional;

/**
 * 使用Feign访问数据库的会话信息仓库实现
 */
@Slf4j
public class FeignSessionInfoRepository implements SessionInfoRepository {
    
    @Autowired
    private DataAccessMysqlClient dataAccessMysqlClient;
    
    @Value("${data-access.app-id:default}")
    private String appId;
    
    @Value("${data-access.data-source:default}")
    private String dataSource;

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
            
            // 构建RemoteSqlDTO
            RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                    .appId(appId)
                    .sql(sql)
                    .params(params)
                    .dataSource(dataSource)
                    .build();
            
            // 执行SQL
            R<Integer> result = dataAccessMysqlClient.update(remoteSqlDTO);
            
            if (result != null && result.getData() != null && result.getData() > 0) {
                log.info("会话信息保存成功，会话ID: {}", sessionInfo.getSessionId());
                return sessionInfo;
            } else {
                log.error("会话信息保存失败，返回结果: {}", result);
                return null;
            }
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
            
            // 映射结果
            if (result != null && result.getData() != null && !result.getData().isEmpty()) {
                return Optional.of(mapToSessionInfo(result.getData().get(0)));
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
            
            return mapToSessionInfoList(result);
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
            
            // 构建参数列表
            List<Object> params = new ArrayList<>();
            params.add(LocalDateTime.now());
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
            
            return result != null && result.getData() != null && result.getData() > 0;
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
            
            // 构建参数列表
            List<Object> params = new ArrayList<>();
            params.add(LocalDateTime.now());
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
            
            return result != null && result.getData() != null && result.getData() > 0;
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
            
            // 构建参数列表
            List<Object> params = new ArrayList<>();
            params.add(LocalDateTime.now());
            
            // 构建RemoteSqlDTO
            RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                    .appId(appId)
                    .sql(sql)
                    .params(params)
                    .dataSource(dataSource)
                    .build();
            
            // 执行删除操作
            R<Integer> result = dataAccessMysqlClient.update(remoteSqlDTO);
            
            return result != null && result.getData() != null ? result.getData() : 0;
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
            
            // 构建参数列表
            List<Object> params = new ArrayList<>();
            params.add(LocalDateTime.now());
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
            
            log.info("会话已删除，会话ID: {}, 更新行数: {}", sessionId, result != null && result.getData() != null ? result.getData() : 0);
            return result != null && result.getData() != null && result.getData() > 0;
        } catch (Exception e) {
            log.error("删除会话异常，会话ID: {}", sessionId, e);
            return false;
        }
    }
    
    /**
     * 将查询结果映射为SessionInfo对象列表
     * @param result 查询结果
     * @return SessionInfo对象列表
     */
    private List<SessionInfo> mapToSessionInfoList(R<List<Map<String, Object>>> result) {
        List<SessionInfo> sessionInfos = new ArrayList<>();
        
        if (result != null && result.getData() != null) {
            for (Map<String, Object> row : result.getData()) {
                SessionInfo sessionInfo = mapToSessionInfo(row);
                if (sessionInfo != null) {
                    sessionInfos.add(sessionInfo);
                }
            }
        }
        
        return sessionInfos;
    }
    
    /**
     * 将单条查询结果映射为SessionInfo对象
     * @param row 单条查询结果
     * @return SessionInfo对象
     */
    private SessionInfo mapToSessionInfo(Map<String, Object> row) {
        try {
            return SessionInfo.builder()
                    .sessionId((String) row.get("session_id"))
                    .userId((String) row.get("user_id"))
                    .sessionName((String) row.get("session_name"))
                    .isActive((Integer) row.get("is_active") == 1)
                    .createdAt((LocalDateTime) row.get("created_at"))
                    .lastActiveAt((LocalDateTime) row.get("last_active_at"))
                    .expiredAt((LocalDateTime) row.get("expired_at"))
                    .build();
        } catch (Exception e) {
            log.error("映射会话信息失败，行数据: {}", row, e);
            return null;
        }
    }
}
