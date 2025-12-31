package com.ai.server.agent.ai.common.sse.config;

import com.ai.server.agent.ai.common.sse.repository.SseMessageRepository;
import com.ai.server.agent.ai.common.sse.repository.impl.DirectSseMessageRepository;
import com.ai.server.agent.ai.common.sse.repository.impl.FeignSseMessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * SSE消息仓库的条件配置类，根据agent.config.type配置选择不同的实现
 */
@Configuration
public class SseMessageRepositoryConfig {
    
    /**
     * 当agent.config.type为feign时，使用FeignSseMessageRepository
     * @return FeignSseMessageRepository实例
     */
    @Bean("feignSseMessageRepository")
    @ConditionalOnProperty(name = "agent.config.type", havingValue = "feign", matchIfMissing = true)
    public SseMessageRepository feignSseMessageRepository() {
        return new FeignSseMessageRepository();
    }
    
    /**
     * 当agent.config.type为direct时，使用DirectSseMessageRepository
     * @return DirectSseMessageRepository实例
     */
    @Bean("directSseMessageRepository")
    @ConditionalOnProperty(name = "agent.config.type", havingValue = "direct")
    @Primary
    public SseMessageRepository directSseMessageRepository() {
        return new DirectSseMessageRepository();
    }
}