package com.ai.server.agent.ai.config;

import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepositoryDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;

/**
 * ChatMemory配置类，用于配置JdbcChatMemoryRepository
 */
@Configuration
public class ChatMemoryConfig {
    
    @Autowired
    private DataSource dataSource;
    
    /**
     * 创建JdbcChatMemoryRepository实例，用于将聊天记忆存储到MySQL数据库
     * @return JdbcChatMemoryRepository实例
     */
    @Bean
    public JdbcChatMemoryRepository chatMemoryRepository() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .dialect(JdbcChatMemoryRepositoryDialect.from(dataSource))
                .build();
    }
}
