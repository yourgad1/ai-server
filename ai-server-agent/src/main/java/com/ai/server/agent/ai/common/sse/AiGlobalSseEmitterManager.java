package com.ai.server.agent.ai.common.sse;

import com.ai.server.agent.ai.common.sse.entity.SessionInfo;
import com.ai.server.agent.ai.common.sse.entity.SseMessage;
import com.ai.server.agent.ai.common.sse.repository.SessionInfoRepository;
import com.ai.server.agent.ai.common.sse.repository.SseMessageRepository;
import com.ai.server.agent.ai.rest.response.ResponseAi;
import com.ai.server.agent.ai.interceptor.UserContextHolder;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.Optional;

@Component
@Slf4j
public class AiGlobalSseEmitterManager {
    // 存储所有活动的SSE连接
    private final ConcurrentMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    // 存储连接状态（true表示活跃，false表示已关闭/超时/错误）
    private final ConcurrentMap<String, AtomicBoolean> connectionStates = new ConcurrentHashMap<>();
    
    @Autowired
    private SseMessageRepository sseMessageRepository;
    
    @Autowired
    private SessionInfoRepository sessionInfoRepository;
    
    // 用户上下文持有者
    private final UserContextHolder userContextHolder = UserContextHolder.getInstance();
    
    // 用于跟踪用户ID警告是否已经记录，避免重复警告
    private final AtomicBoolean userIdWarningLogged = new AtomicBoolean(false);
    
    // 存储会话ID与连接ID的映射关系
    private final ConcurrentMap<String, String> sessionIdToConnectId = new ConcurrentHashMap<>();
    
    // 存储连接ID与会话ID的映射关系
    private final ConcurrentMap<String, String> connectIdToSessionId = new ConcurrentHashMap<>();

    /**
     * 创建发射器并绑定到当前线程，自定义超时时间
     */
    public SseEmitter createEmitterByConnId(String connId, String sessionId, long timeoutMs) {
        // 创建带自定义超时的发射器
        SseEmitter emitter = new SseEmitter(timeoutMs);
        // 添加监听器处理连接完成、错误和超时
        setupEmitterListeners(connId, emitter);
        // 初始化连接状态为活跃
        connectionStates.put(connId, new AtomicBoolean(true));
        // 注册发射器
        emitters.put(connId, emitter);

        // 设置响应头禁用缓冲和增强实时推送配置
        disableCacheInResponse();

        // 获取用户ID，只在第一次获取不到时记录警告
        String userId = getUserId();

        // 创建或更新会话信息
        createOrUpdateSession(sessionId, userId, connId);

        // 发送一个初始事件，确认连接建立并验证实时性
        try {
            // 先等待一段时间让handler有机会初始化
            Thread.sleep(50);
            // 强制等待handler初始化，传入正确的connectionId
            waitForHandlerInitialization(emitter, connId);
            SseEmitter.SseEventBuilder connected = SseEmitter.event().name("connected");
            // 尝试直接通过Emitter发送原始SSE格式的数据
            try {
                emitter.send(connected);
                log.info("已发送原始格式的初始连接事件，连接ID: {}", connId);
            } catch (IllegalStateException e) {
                // 如果直接发送原始数据失败，尝试使用标准builder
                log.warn("原始初始事件发送失败，尝试标准事件，连接ID: {}", connId, e);
                try {
                    SseEmitter.SseEventBuilder initEvent = SseEmitter.event()
                            .id(String.valueOf(connId))
                            .name("connected")
                            .data(ResponseAi.ofMessage("Connection established"))
                            .reconnectTime(1000);
                    emitter.send(initEvent);
                    log.info("已发送标准格式的初始连接事件，连接ID: {}", connId);
                } catch (Exception ex) {
                    log.warn("标准初始事件发送也失败，将继续运行，连接ID: {}", connId, ex);
                }
            }
            // 立即执行强制刷新所有缓冲区
            forceFlushAllBuffers(connId);
        } catch (Exception e) {
            log.warn("发送初始连接事件失败，连接ID: {}", connId, e);
        }
        log.info("创建新的SSE连接并绑定连接ID: {}，会话ID: {}", connId, sessionId);
        return emitter;
    }
    
    /**
     * 创建发射器并绑定到当前线程，自定义超时时间
     */
    public SseEmitter createEmitterByConnId(String connId, long timeoutMs) {
        return createEmitterByConnId(connId, connId, timeoutMs);
    }
    
    /**
     * 创建或更新会话信息
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param connectId 连接ID
     */
    private void createOrUpdateSession(String sessionId, String userId, String connectId) {
        try {
            // 查询会话是否存在
            Optional<SessionInfo> existingSession = sessionInfoRepository.findBySessionId(sessionId);
            SessionInfo sessionInfo;
            
            if (existingSession.isPresent()) {
                // 更新现有会话
                sessionInfo = existingSession.get();
                sessionInfo.setIsActive(true);
                sessionInfo.setLastActiveAt(LocalDateTime.now());
            } else {
                // 创建新会话
                sessionInfo = SessionInfo.builder()
                        .sessionId(sessionId)
                        .userId(userId)
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .lastActiveAt(LocalDateTime.now())
                        .build();
            }
            
            // 保存会话信息到数据库
            sessionInfoRepository.save(sessionInfo);
            
            // 更新映射关系
            sessionIdToConnectId.put(sessionId, connectId);
            connectIdToSessionId.put(connectId, sessionId);
        } catch (Exception e) {
            log.error("创建或更新会话失败，会话ID: {}, 连接ID: {}", sessionId, connectId, e);
        }
    }

    /**
     * 获取用户ID，只在第一次获取不到时记录警告
     * @return 用户ID
     */
    private String getUserId() {
        String userId = userContextHolder.getUserId();
        if (userId == null || userId.isEmpty()) {
            // 只在第一次获取不到用户ID时记录警告，避免重复日志
            if (userIdWarningLogged.compareAndSet(false, true)) {
                log.warn("无法获取用户ID，使用默认值'test'。请检查用户上下文设置。");
            }
            userId = "test";
        }
        return userId;
    }
    
    /**
     * 禁用响应缓冲和增强实时推送配置
     */
    private void disableCacheInResponse() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletResponse response = requestAttributes.getResponse();
                if (response != null) {
                    // 禁用所有缓存
                    response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate, private");
                    response.setHeader("Pragma", "no-cache");
                    response.setHeader("Expires", "0");

                    // 优化连接和实时推送
                    response.setHeader("Connection", "keep-alive");
                    response.setHeader("X-Accel-Buffering", "no"); // 禁用Nginx缓冲
                    response.setHeader("X-Content-Type-Options", "nosniff");
                    response.setHeader("Transfer-Encoding", "chunked");
                    response.setHeader("Access-Control-Allow-Origin", "*"); // 允许跨域

                    // 设置合理的缓冲区大小，平衡实时性和性能
                    response.setBufferSize(4096); // 优化：增加缓冲区大小到4096，减少刷新次数

                    // 立即刷新响应头和缓冲区
                    response.flushBuffer();
                    log.debug("已设置响应头并刷新缓冲区");
                }
            }
        } catch (Exception e) {
            log.debug("无法设置响应头: {}", e.getMessage()); // 优化：将警告改为调试日志
        }
    }

    /**
     * 设置发射器监听器
     */
    private void setupEmitterListeners(String connectionId, SseEmitter emitter) {
        // 连接完成
        emitter.onCompletion(() -> {
            markConnectionInactive(connectionId);
            log.info("SSE连接完成，连接ID: {}", connectionId);
        });

        // 连接错误
        emitter.onError(e -> {
            log.error("SSE连接错误，连接ID: {}", connectionId, e);
            markConnectionInactive(connectionId);
        });

        // 连接超时
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时，连接ID: {}", connectionId);
            markConnectionInactive(connectionId);
        });
    }


    /**
     * 使用反射检查SseEmitter的handler是否已初始化
     * 这是一个更直接的handler状态检查方法，不会触发实际的发送操作
     */
    private boolean isHandlerInitialized(SseEmitter emitter) {
        if (emitter == null) {
            return false;
        }

        try {
            // 使用反射获取父类ResponseBodyEmitter中的handler字段
            java.lang.reflect.Field handlerField = emitter.getClass().getSuperclass().getDeclaredField("handler");
            handlerField.setAccessible(true);
            Object handler = handlerField.get(emitter);
            boolean initialized = handler != null;

            // 记录handler状态日志（调试级别）
            if (log.isDebugEnabled()) {
                log.debug("SSE连接handler状态检查: {}", initialized ? "已初始化" : "未初始化");
            }

            return initialized;
        } catch (Exception e) {
            // 反射访问失败时，不再尝试发送测试消息，直接返回true
            // 优化：减少不必要的发送操作，提高性能和稳定性
            log.debug("反射检查handler失败，错误: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 等待handler初始化完成
     * 即使不能直接初始化handler，我们可以等待框架自动初始化
     */
    private void waitForHandlerInitialization(SseEmitter emitter, String connectionId) {
        if (emitter == null) {
            return;
        }

        int maxRetries = 20; // 优化：减少重试次数到20次
        int retryCount = 0;
        int delayMs = 50; // 优化：增加每次尝试间隔到50ms，总超时时间保持1000ms
        boolean hasAttemptedSend = false;

        while (retryCount < maxRetries) {
            try {
                if (isHandlerInitialized(emitter)) {
                    log.debug("handler 已初始化，准备发送消息，连接ID: {}", connectionId);
                    // 初始化成功后立即执行一次刷新，使用正确的连接ID
                    forceFlushAllBuffers(connectionId);
                    return;
                }

                // 优化：只在重试次数过半时尝试发送测试数据，减少不必要的资源消耗
                if (retryCount == maxRetries / 2 && !hasAttemptedSend) {
                    // 尝试通过发送极小的测试数据来触发handler初始化
                    try {
                        // 尝试发送原始SSE格式的测试事件，更简单更直接
                        SseEmitter.SseEventBuilder init = SseEmitter.event().name("_init");
                        emitter.send(init);
                        log.debug("尝试触发handler初始化，连接ID: {}", connectionId);

                        // 发送后立即刷新，使用正确的连接ID
                        forceFlushAllBuffers(connectionId);
                        hasAttemptedSend = true;
                    } catch (Exception e) {
                        // 忽略发送测试消息的异常，继续等待
                        log.debug("测试消息发送异常: {}, 连接ID: {}", e.getMessage(), connectionId);
                    }
                }

                Thread.sleep(delayMs);
                retryCount++;
            } catch (Exception e) {
                log.debug("等待handler初始化时发生异常: {}, 连接ID: {}", e.getMessage(), connectionId);
                break;
            }
        }

        // 优化：只在必要时记录警告日志，减少日志噪音
        if (!isHandlerInitialized(emitter)) {
            log.warn("等待handler初始化超时，将尝试强制发送，连接ID: {}", connectionId);
        }
    }


    /**
     * 发送SSE事件
     */
    public void sendEvent(String connectionId, Object message) {
        sendEvent(connectionId, "message", message);
    }

    /**
     * 发送带类型的SSE事件，并确保实时推送
     */
    public void sendEvent(String connectionId, String eventType, Object message) {
        SseEmitter emitter = emitters.get(connectionId);
        boolean initialized = isHandlerInitialized(emitter);
        // 记录handler状态，但不再因为handler未初始化而提前退出
        if (!initialized) {
            log.warn("handler 未初始化，尝试强制发送，连接ID: {}", connectionId);
            // 在handler未初始化情况下，强制尝试等待和刷新，传入正确的connectionId
            waitForHandlerInitialization(emitter, connectionId);
        }

        if (emitter != null && isConnectionActive(connectionId)) {
            try {
                // 创建标准化的SSE事件
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .id(connectionId)
                        .name(eventType)
                        .data(message)
                        .reconnectTime(1000); // 重连时间

                // 发送事件 - 即使handler未初始化也尝试发送
                try {
                    emitter.send(event);
                    // 发送成功后立即执行强制刷新
                    forceFlushAllBuffers(connectionId);

                    // 保存SSE消息到数据库（在发送成功后执行，确保不会影响SSE流）
                    saveSseMessageToDatabase(connectionId, eventType, message);

                } catch (IllegalStateException e) {
                    // 如果因为handler未初始化导致异常，记录并尝试等待后重试
                    log.warn("发送事件时遇到IllegalStateException(可能是handler未初始化)，连接ID: {}", connectionId, e);

                    // 尝试等待一小段时间后重试
                    try {
                        Thread.sleep(50);
                        emitter.send(event);
                        log.info("重试后成功发送SSE事件，连接ID: {}, 事件类型: {}", connectionId, eventType);

                        // 发送成功后立即执行强制刷新
                        forceFlushAllBuffers(connectionId);
                        
                        // 保存SSE消息到数据库（在发送成功后执行，确保不会影响SSE流）
                        saveSseMessageToDatabase(connectionId, eventType, message);
                    } catch (Exception retryEx) {
                        log.error("重试发送SSE事件失败，连接ID: {}", connectionId, retryEx);
                        
                        // 即使发送失败，也尝试保存消息到数据库
                        try {
                            saveSseMessageToDatabase(connectionId, eventType, message);
                        } catch (Exception dbEx) {
                            log.error("保存SSE消息到数据库失败，连接ID: {}", connectionId, dbEx);
                        }
                    }
                }

                // 短时间暂停，确保数据有时间发送
                Thread.sleep(20); // 增加暂停时间确保数据传输

                // 再次刷新以确保数据完全发送
                try {
                    forceFlushAllBuffers(connectionId);
                } catch (Exception e) {
                    log.debug("二次刷新失败: {}", e.getMessage());
                }

            } catch (Exception e) {
                log.error("发送SSE事件失败，连接ID: {}", connectionId, e);
            }
        } else {
            log.warn("无法发送SSE事件：连接不存在或已关闭，连接ID: {}", connectionId);
            // 即使连接关闭，也保存消息到数据库
            saveSseMessageToDatabase(connectionId, eventType, message);
        }
    }
    
    /**
     * 保存SSE消息到数据库
     */
    private void saveSseMessageToDatabase(String connectionId, String eventType, Object message) {
        try {
            // 获取用户ID，只在第一次获取不到时记录警告
            String userId = getUserId();
            
            // 从映射中获取会话ID
            String sessionId = connectIdToSessionId.get(connectionId);
            // 如果会话ID为空，使用连接ID作为默认值
            if (sessionId == null || sessionId.isEmpty()) {
                log.warn("无法获取会话ID，使用连接ID作为默认值。连接ID: {}", connectionId);
                sessionId = connectionId;
            }
            
            // 生成或获取消息分组ID，使用连接ID作为分组ID，确保一次问答的所有消息属于同一个分组
            // 按照最新逻辑，使用连接ID代替消息分组字段
            String messageGroup = connectionId;
            
            // 将消息转换为字符串，处理可能的序列化异常
            String messageContent;
            try {
                if (message instanceof ResponseAi) {
                    // 处理ResponseAi类型，确保data字段能正确序列化
                    ResponseAi responseAi = (ResponseAi) message;
                    // 创建一个副本，只保留可序列化的字段
                    ResponseAi serializableResponseAi = ResponseAi.ofMessage(responseAi.getAnswer());
                    messageContent = serializableResponseAi.toString();
                } else {
                    // 其他类型直接转换
                    messageContent = message.toString();
                }
            } catch (Exception e) {
                // 序列化失败时，使用简单的消息表示
                log.warn("消息序列化失败，使用简单表示，连接ID: {}", connectionId, e);
                messageContent = String.format("[%s] %s", eventType, message.getClass().getSimpleName());
            }
            
            // 构建SSE消息，包含connectId字段和messageGroup字段
            SseMessage sseMessage = SseMessage.builder()
                    .userId(userId)
                    .sessionId(sessionId) // 使用正确的会话ID
                    .connectId(connectionId) // 保存连接ID
                    .messageType(eventType)
                    .messageContent(messageContent) // 使用处理后的消息内容
                    .build();
            
            // 保存到数据库
            sseMessageRepository.save(sseMessage);
        } catch (Exception e) {
            log.error("保存SSE消息到数据库失败，连接ID: {}", connectionId, e);
        }
    }

    /**
     * 强制刷新所有可能的缓冲区，确保消息立即推送
     * 这是一个综合刷新方法，尝试通过多种方式刷新不同层次的缓冲区
     */
    private void forceFlushAllBuffers(String connectionId) {
        log.debug("开始强制刷新所有缓冲区，连接ID: {}", connectionId);

        // 尝试直接从请求上下文刷新响应（主要刷新方式）
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletResponse response = requestAttributes.getResponse();
                if (response != null) {
                    // 强制刷新响应缓冲区（只保留必要的刷新操作）
                    response.flushBuffer();
                    log.debug("成功直接刷新响应缓冲区，连接ID: {}", connectionId);
                }
            }
        } catch (Exception e) {
            log.debug("直接刷新响应缓冲区失败，连接ID: {}", connectionId, e);
        }

        log.debug("强制刷新所有缓冲区完成，连接ID: {}", connectionId);
    }

    /**
     * 发送错误事件
     */
    public void sendErrorEvent(String connectionId, Object errorMessage) {
        sendEvent(connectionId, "error", errorMessage);
    }

    /**
     * 处理Flux数据流
     */
    public <T> void processFlux(String connectionId, Publisher<T> fluxPublisher, Function<T, String> converter) {
        if (fluxPublisher == null) {
            log.warn("Flux发布者为空，连接ID: {}", connectionId);
            return;
        }
        SseEmitter emitter = emitters.get(connectionId);
        if (emitter == null || !isConnectionActive(connectionId)) {
            log.warn("无法处理Flux：连接不存在或已关闭，连接ID: {}", connectionId);
            return;
        }
        AtomicBoolean condition= new AtomicBoolean(false);
        Flux.from(fluxPublisher)
                .doOnError(error -> {
                    log.error("Flux处理错误，连接ID: {}", connectionId, error);
                })
                .doFinally(signal -> {
                    log.info("Flux处理完成，信号: {}, 连接ID: {}", signal, connectionId);
                    synchronized (fluxPublisher) {
                        condition.set(true);
                        fluxPublisher.notifyAll();
                    }

                })
                .subscribe(
                        data -> {
                            try {
                                String message = converter.apply(data);
                                log.info("回答：{}",message);
                                sendEvent(connectionId, ResponseAi.ofMessage(message));
                            } catch (Exception e) {
                                log.error("转换和发送数据失败，连接ID: {}", connectionId, e);
                            }
                        },
                        error -> {
                            log.error("Flux订阅错误，连接ID: {}", connectionId, error);
                            sendErrorEvent(connectionId, ResponseAi.ofError("订阅错误: " + error.getMessage()));
                        }
                );

        synchronized (fluxPublisher) {
            try {
                while (!condition.get())
                    fluxPublisher.wait(5 * 60 * 1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 处理字符串类型的Flux数据流
     */
    public void processStringFlux(String connectionId, Publisher<String> fluxPublisher) {
        processFlux(connectionId, fluxPublisher, s -> s);
    }


    /**
     * 完成连接，并清理相关映射关系
     */
    public void completeConnection(String connId) {
        SseEmitter emitter = emitters.get(connId);
        // 使用我们自己维护的连接状态来判断是否需要完成连接
        if (emitter != null) {
            emitter.complete();
            
            // 调用markConnectionInactive来清理所有相关映射关系，确保连接ID失效
            markConnectionInactive(connId);
        }
    }


    /**
     * 检查连接是否激活
     */
    public boolean isConnectionActive(String connectionId) {
        AtomicBoolean state = connectionStates.get(connectionId);
        return state != null && state.get();
    }

    /**
     * 标记连接为非激活状态，并清理相关映射关系，确保连接ID在会话结束后失效
     */
    public boolean markConnectionInactive(String connectionId) {
        AtomicBoolean state = connectionStates.get(connectionId);
        boolean result = state != null && state.compareAndSet(true, false);
        
        if (result) {
            // 清理所有与连接ID相关的映射关系，确保连接ID失效
            // 1. 清理emitters映射
            emitters.remove(connectionId);
            
            // 2. 清理connectionStates映射
            connectionStates.remove(connectionId);
            
            // 3. 获取并清理sessionIdToConnectId映射
            String sessionId = connectIdToSessionId.get(connectionId);
            if (sessionId != null) {
                sessionIdToConnectId.remove(sessionId);
            }
            
            // 4. 清理connectIdToSessionId映射
            connectIdToSessionId.remove(connectionId);
            
            log.info("已清理连接ID相关映射，连接ID: {}, 会话ID: {}", connectionId, sessionId);
        }
        
        return result;
    }

}