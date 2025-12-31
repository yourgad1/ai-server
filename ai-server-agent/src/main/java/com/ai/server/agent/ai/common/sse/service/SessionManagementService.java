package com.ai.server.agent.ai.common.sse.service;

import com.ai.server.agent.ai.rest.request.RequestAi;
import com.ai.server.agent.ai.common.sse.entity.SessionInfo;
import com.ai.server.agent.ai.common.sse.repository.SessionInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 会话管理服务类，负责处理会话的创建、更新和管理逻辑
 */
@Service
@Slf4j
public class SessionManagementService {

    @Autowired
    private SessionInfoRepository sessionInfoRepository;

    /**
     * 处理会话管理逻辑
     * 如果携带了用户id和会话id就直接处理，如果未携带，需要创建新的会话id
     */
    public void handleSessionManagement(RequestAi requestAi) {
        // 检查并设置userId
        if (requestAi.getUserId() == null || requestAi.getUserId().isEmpty()) {
            // 如果没有获取到用户id，使用test作为临时解决方案
            requestAi.setUserId("test");
            log.warn("请求中未携带userId，使用默认userId: test");
        }

        // 检查并设置sessionId
        if (requestAi.getSessionId() == null || requestAi.getSessionId().isEmpty()) {
            // 生成新的sessionId
            String newSessionId = UUID.randomUUID().toString();
            requestAi.setSessionId(newSessionId);
            log.info("请求中未携带sessionId，生成新sessionId: {}", newSessionId);

            // 从用户消息中生成会话名称，截取前20个字符
            String sessionName = requestAi.getMessage() != null ? 
                (requestAi.getMessage().length() > 20 ? requestAi.getMessage().substring(0, 20) + "..." : requestAi.getMessage()) : 
                "新会话";
            
            // 创建新的会话信息
            SessionInfo sessionInfo = SessionInfo.builder()
                    .sessionId(newSessionId)
                    .userId(requestAi.getUserId())
                    .sessionName(sessionName)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .lastActiveAt(LocalDateTime.now())
                    .expiredAt(LocalDateTime.now().plusYears(1)) // 会话有效期1年
                    .build();

            // 保存会话信息到数据库
            sessionInfoRepository.save(sessionInfo);
        } else {
            // 会话已存在，更新会话信息
            Optional<SessionInfo> existingSession = sessionInfoRepository.findBySessionId(requestAi.getSessionId());
            if (existingSession.isPresent()) {
                SessionInfo sessionInfo = existingSession.get();
                sessionInfo.setIsActive(true);
                sessionInfo.setLastActiveAt(LocalDateTime.now());
                sessionInfo.setExpiredAt(LocalDateTime.now().plusYears(1)); // 延长会话有效期为1年
                sessionInfoRepository.save(sessionInfo);
                log.info("更新现有会话信息，sessionId: {}", requestAi.getSessionId());
            } else {
                // 会话ID不存在，创建新会话
                String sessionId = requestAi.getSessionId();
                
                // 从用户消息中生成会话名称，截取前20个字符
                String sessionName = requestAi.getMessage() != null ? 
                    (requestAi.getMessage().length() > 20 ? requestAi.getMessage().substring(0, 20) + "..." : requestAi.getMessage()) : 
                    "新会话";
                
                SessionInfo sessionInfo = SessionInfo.builder()
                        .sessionId(sessionId)
                        .userId(requestAi.getUserId())
                        .sessionName(sessionName)
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .lastActiveAt(LocalDateTime.now())
                        .expiredAt(LocalDateTime.now().plusYears(1)) // 会话有效期1年
                        .build();
                sessionInfoRepository.save(sessionInfo);
                log.info("会话ID不存在，创建新会话，sessionId: {}", sessionId);
            }
        }
    }
}