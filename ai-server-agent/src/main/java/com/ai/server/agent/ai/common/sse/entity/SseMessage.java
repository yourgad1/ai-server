package com.ai.server.agent.ai.common.sse.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SSE消息实体类，对应sse_message表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseMessage {
    /**
     * 消息唯一标识符，UUID，自动生成
     */
    private String id;
    
    /**
     * 用户唯一标识符，参考UserContextHolder获取
     */
    private String userId;
    
    /**
     * 会话唯一标识符，与session_info表关联
     */
    private String sessionId;
    
    /**
     * 连接唯一标识符，关联SSE连接
     */
    private String connectId;
    
    /**
     * 智能体唯一标识符，与agent_config表关联
     */
    private String agentId;
    
    /**
     * 消息类型：message(普通消息)、log(日志消息)、error(错误消息)、table(表格消息)
     */
    private String messageType;

    
    /**
     * 消息内容
     */
    private String messageContent;
    
    /**
     * 消息发送时间
     */
    private LocalDateTime sendTime;
    
    /**
     * 是否逻辑删除，0=未删除，1=已删除
     */
    private boolean isDeleted = false;
}