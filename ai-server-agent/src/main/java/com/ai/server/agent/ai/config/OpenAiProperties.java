package com.ai.server.agent.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.ai.openai")
@RefreshScope
public class OpenAiProperties {
    private String baseUrl;
    private String apiKey;
    private String completionsPath;

    private ChatOptions chat = new ChatOptions();
    private MultiOptions multi = new MultiOptions();

    @Data
    public static class ChatOptions {
        private String model;
        private Integer maxTokens;
        private Double temperature;
        private Double topP;
        private Double presencePenalty;
    }

    @Data
    public static class MultiOptions {
        private String baseUrl;
        private String apiKey;
        private String completionsPath;
        private String model;
        private Integer maxTokens;
        private Double temperature;
        private Double topP;
        private Double presencePenalty;
    }

}
