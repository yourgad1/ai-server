package com.ai.server.agent.ai.rest.controller;

import com.ai.server.agent.ai.rest.request.RequestAi;
import com.ai.server.agent.ai.common.sse.AiGlobalSseEmitterManager;
import com.ai.server.agent.ai.common.sse.service.SessionManagementService;
import com.ai.server.agent.ai.strategy.context.IntentBasedRequestContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

@RestController
@RequestMapping("/c2000/ai")
@Slf4j
public class WebAiController {

    @Autowired
    private IntentBasedRequestContext intentBasedRequestContext;

    @Autowired
    @Qualifier("chatTaskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    private AiGlobalSseEmitterManager sseEmitterManager;
    @Autowired
    private SessionManagementService sessionManagementService;


    /**
     * 融合聊天接口，支持直接文件上传(yml生成)和常规文本查询
     * 同时支持JSON请求体和multipart/form-data文件上传两种方式
     * 使用基于意图的策略模式，根据SystemAgent的意图识别结果选择合适的处理策略
     *
     * @return SseEmitter 用于服务器推送事件
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestPart RequestAi requestAi,
                           HttpServletResponse response) {
        // 设置响应头禁用缓冲和优化SSE连接
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Transfer-Encoding", "chunked");
        
        // 会话管理逻辑
        sessionManagementService.handleSessionManagement(requestAi);
        
        // 生成连接ID
        String connId = UUID.randomUUID().toString();
        requestAi.setConnId(connId);
        
        // 创建SSE发射器，超时时间为5分钟，传入正确的会话ID
        SseEmitter emitter = sseEmitterManager.createEmitterByConnId(connId, requestAi.getSessionId(), 5 * 60 * 1000);

        // 立即刷新响应头
        try {
            response.flushBuffer();
        } catch (Exception e) {
            log.warn("刷新响应头失败: {}", e.getMessage());
        }
        taskExecutor.execute(() -> {
            try {
                intentBasedRequestContext.handleRequest(requestAi);
            } catch (Exception e) {

                log.error("任务执行失败，connectionId: {}, 错误信息: {}", connId, e.getMessage());
                log.error(e.getStackTrace().toString());
            }
            // 移除finally块中的completeConnection调用，让SSE连接自然结束
            // 或者在AI模型响应完成后由SSE管理器自动处理连接关闭
        });
        return emitter;
    }
}
