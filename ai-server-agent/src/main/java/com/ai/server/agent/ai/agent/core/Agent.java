package com.ai.server.agent.ai.agent.core;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.Map;

/**
 * name 智能体抽象接口
 * @author zhouyuhui
 * @param <T>
 */
public interface Agent<T> {


     T chat(Request request, String type);

    void chatStream(Request request) throws Exception;

    @Data
    @SuperBuilder
    @AllArgsConstructor
    class Request{
        private String connId;
        private Map<String,Object> context;
        private Boolean isStreaming;
        private String sessionId;
    }
    @Data
    @SuperBuilder
    class ChatRequest extends Request{
        private String message;

    }

    @Data
    @SuperBuilder
    class MultiRequest extends Request{
        private UserMessage message;
    }

}
