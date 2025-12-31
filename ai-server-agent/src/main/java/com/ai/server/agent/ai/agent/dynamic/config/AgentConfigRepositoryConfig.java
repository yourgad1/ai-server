package com.ai.server.agent.ai.agent.dynamic.config;

import com.ai.server.agent.ai.agent.dynamic.repository.AgentConfigRepository;
import com.ai.server.agent.ai.agent.dynamic.repository.impl.DirectAgentConfigRepository;
import com.ai.server.agent.ai.agent.dynamic.repository.impl.FeignAgentConfigRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Agent配置仓库的条件配置类，根据agent.config.type配置选择不同的实现
 */
@Configuration
public class AgentConfigRepositoryConfig {
    
    /**
     * 当agent.config.type为feign时，使用FeignAgentConfigRepository
     * @return FeignAgentConfigRepository实例
     */
    @Bean("feignAgentConfigRepository")
    @ConditionalOnProperty(name = "agent.config.type", havingValue = "feign", matchIfMissing = true)
    public AgentConfigRepository feignAgentConfigRepository() {
        return new FeignAgentConfigRepository();
    }
    
    /**
     * 当agent.config.type为direct时，使用DirectAgentConfigRepository
     * @return DirectAgentConfigRepository实例
     */
    @Bean("directAgentConfigRepository")
    @ConditionalOnProperty(name = "agent.config.type", havingValue = "direct")
    @Primary
    public AgentConfigRepository directAgentConfigRepository() {
        return new DirectAgentConfigRepository();
    }
}