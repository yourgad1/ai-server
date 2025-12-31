package com.ai.server.agent.ai.rest.controller;

import com.ai.server.agent.ai.common.sse.entity.SessionInfo;
import com.ai.server.agent.ai.common.sse.entity.SseMessage;
import com.ai.server.agent.ai.common.sse.service.SessionService;
import com.ai.server.agent.ai.rest.response.ResponseAi;
import com.ai.server.agent.ai.interceptor.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话控制器，提供会话和消息查询的API接口
 */
@RestController
@RequestMapping("/c2000/ai/session")
@Slf4j
public class SessionController {
    
    @Autowired
    private SessionService sessionService;
    
    // 用户上下文持有者
    private final UserContextHolder userContextHolder = UserContextHolder.getInstance();
    
    /**
     * 查询当前用户的所有会话
     * @return 会话列表
     */
    @GetMapping("/list")
    public ResponseAi getSessions() {
        try {
            // 获取当前用户ID
            String userId = userContextHolder.getUserId();
            if (userId == null || userId.isEmpty()) {
                log.warn("无法获取用户ID，使用默认值'test'。请检查用户上下文设置。");
                userId = "test";
            }
            
            List<SessionInfo> sessions = sessionService.getSessionsByUserId(userId);
            return ResponseAi.ofData(sessions);
        } catch (Exception e) {
            log.error("查询用户会话失败", e);
            return ResponseAi.ofError("查询用户会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询指定会话的所有消息
     * @param sessionId 会话ID
     * @param groupByConnectId 是否按connect_id分组返回，默认为true
     * @return 消息列表或按connect_id分组的消息
     */
    @GetMapping("/messages/{sessionId}")
    public ResponseAi getSessionMessages(@PathVariable String sessionId, 
                                         @RequestParam(required = false, defaultValue = "true") boolean groupByConnectId) {
        try {
            List<SseMessage> messages = sessionService.getMessagesBySessionId(sessionId);
            
            if (groupByConnectId) {
                // 按connect_id分组，每组内按send_time排序
                java.util.LinkedHashMap<String, List<SseMessage>> connectIdMap = new java.util.LinkedHashMap<>();
                
                for (SseMessage message : messages) {
                    String connectId = message.getConnectId();
                    if (!connectIdMap.containsKey(connectId)) {
                        connectIdMap.put(connectId, new java.util.ArrayList<>());
                    }
                    connectIdMap.get(connectId).add(message);
                }
                
                // 对每组内的消息按send_time排序
                for (List<SseMessage> connectMessages : connectIdMap.values()) {
                    connectMessages.sort(java.util.Comparator.comparing(SseMessage::getSendTime));
                }
                
                return ResponseAi.ofData(connectIdMap);
            } else {
                // 直接返回所有消息列表，已按send_time排序
                return ResponseAi.ofData(messages);
            }
        } catch (Exception e) {
            log.error("查询会话消息失败，会话ID: {}", sessionId, e);
            return ResponseAi.ofError("查询会话消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据会话ID获取会话信息
     * @param sessionId 会话ID
     * @return 会话信息
     */
    @GetMapping("/{sessionId}")
    public ResponseAi getSession(@PathVariable String sessionId) {
        try {
            SessionInfo sessionInfo = sessionService.getSessionById(sessionId);
            return ResponseAi.ofData(sessionInfo);
        } catch (Exception e) {
            log.error("查询会话信息失败，会话ID: {}", sessionId, e);
            return ResponseAi.ofError("查询会话信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建新会话
     * @param sessionName 会话名称（可选）
     * @return 新创建的会话信息
     */
    @PostMapping("/create")
    public ResponseAi createSession(@RequestParam(required = false, defaultValue = "新会话") String sessionName) {
        try {
            // 获取当前用户ID
            String userId = userContextHolder.getUserId();
            if (userId == null || userId.isEmpty()) {
                log.warn("无法获取用户ID，使用默认值'test'。请检查用户上下文设置。");
                userId = "test";
            }
            
            SessionInfo sessionInfo = sessionService.createSession(userId, sessionName);
            if (sessionInfo != null) {
                return ResponseAi.ofData(sessionInfo);
            } else {
                return ResponseAi.ofError("创建新会话失败");
            }
        } catch (Exception e) {
            log.error("创建新会话失败", e);
            return ResponseAi.ofError("创建新会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除会话
     * @param sessionId 会话ID
     * @return 删除结果
     */
    @DeleteMapping("/{sessionId}")
    public ResponseAi deleteSession(@PathVariable String sessionId) {
        try {
            boolean result = sessionService.deleteSession(sessionId);
            if (result) {
                return ResponseAi.ofData("会话删除成功");
            } else {
                return ResponseAi.ofError("会话删除失败");
            }
        } catch (Exception e) {
            log.error("删除会话失败，会话ID: {}", sessionId, e);
            return ResponseAi.ofError("删除会话失败: " + e.getMessage());
        }
    }
}
