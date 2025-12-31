package com.ai.server.agent.ai.common.sse.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.time.LocalDateTime;

/**
 * 消息分组实体类，用于将一次问答的多次消息分组返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageGroup {
    /**
     * 消息分组ID
     */
    private String messageGroupId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 分组消息列表
     */
    private List<SseMessage> messages;
    
    /**
     * 分组创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 分组最后更新时间
     */
    private LocalDateTime updatedAt;
}
