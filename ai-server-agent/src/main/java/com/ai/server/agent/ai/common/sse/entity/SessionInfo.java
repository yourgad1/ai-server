package com.ai.server.agent.ai.common.sse.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话信息实体类，对应session_info表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo {
    /**
     * 会话唯一标识符，作为主键
     */
    private String sessionId;
    
    /**
     * 用户唯一标识符，参考UserContextHolder获取
     */
    private String userId;
    
    /**
     * 会话名称
     */
    private String sessionName;
    
    /**
     * 会话是否活跃，0=不活跃，1=活跃
     */
    private Boolean isActive;
    
    /**
     * 会话创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 会话最后活跃时间
     */
    private LocalDateTime lastActiveAt;
    
    /**
     * 会话过期时间
     */
    private LocalDateTime expiredAt;
}