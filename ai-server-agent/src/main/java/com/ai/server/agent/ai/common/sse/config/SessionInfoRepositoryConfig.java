package com.ai.server.agent.ai.common.sse.config;

import com.ai.server.agent.ai.common.sse.repository.SessionInfoRepository;
import com.ai.server.agent.ai.common.sse.repository.impl.DirectSessionInfoRepository;
import com.ai.server.agent.ai.common.sse.repository.impl.FeignSessionInfoRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 会话信息仓库的条件配置类，根据agent.config.type配置选择不同的实现
 */
@Configuration
public class SessionInfoRepositoryConfig {
    
    /**
     * 当agent.config.type为feign时，使用FeignSessionInfoRepository
     * @return FeignSessionInfoRepository实例
     */
    @Bean("feignSessionInfoRepository")
    @ConditionalOnProperty(name = "agent.config.type", havingValue = "feign", matchIfMissing = true)
    public SessionInfoRepository feignSessionInfoRepository() {
        return new FeignSessionInfoRepository();
    }
    
    /**
     * 当agent.config.type为direct时，使用DirectSessionInfoRepository
     * @return DirectSessionInfoRepository实例
     */
    @Bean("directSessionInfoRepository")
    @ConditionalOnProperty(name = "agent.config.type", havingValue = "direct")
    @Primary
    public SessionInfoRepository directSessionInfoRepository() {
        return new DirectSessionInfoRepository();
    }
}