package com.ai.server.agent.ai.common.sse.repository;

import com.ai.server.agent.ai.common.sse.entity.SessionInfo;

import java.util.List;
import java.util.Optional;

/**
 * 会话信息仓库接口，定义会话信息的数据库访问方法
 */
public interface SessionInfoRepository {
    
    /**
     * 保存会话信息
     * @param sessionInfo 会话信息实体
     * @return 保存后的会话信息实体
     */
    SessionInfo save(SessionInfo sessionInfo);
    
    /**
     * 根据会话ID查询会话信息
     * @param sessionId 会话ID
     * @return 会话信息实体
     */
    Optional<SessionInfo> findBySessionId(String sessionId);
    
    /**
     * 根据用户ID查询活跃会话列表
     * @param userId 用户ID
     * @return 活跃会话列表
     */
    List<SessionInfo> findActiveSessionsByUserId(String userId);

    
    /**
     * 标记会话为活跃状态
     * @param sessionId 会话ID
     * @return 更新结果
     */
    boolean markSessionActive(String sessionId);
    
    /**
     * 标记会话为不活跃状态
     * @param sessionId 会话ID
     * @return 更新结果
     */
    boolean markSessionInactive(String sessionId);
    
    /**
     * 删除过期会话
     * @return 删除的会话数量
     */
    int deleteExpiredSessions();
    
    /**
     * 删除会话（逻辑删除，将is_active设置为2）
     * @param sessionId 会话ID
     * @return 更新结果
     */
    boolean deleteSession(String sessionId);
}
