package com.ai.server.agent.ai.config;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.ai.tool.resolution.StaticToolCallbackResolver;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;

@EnableConfigurationProperties(OpenAiProperties.class)
@Slf4j
@RequiredArgsConstructor
@Configuration
public class OpenAiChatClientConfig {
    private final OpenAiProperties openAiProperties;


    @Bean
    public WebClient.Builder webClient() {
        return WebClient.builder()
                .filter((request, next) -> {
                    // 记录请求信息
                    log.info("OpenAI WebClient Request:");
                    log.info("URL: {}", request.url());
                    log.info("Method: {}", request.method());
                    log.info("Headers: {}", request.headers());

                    // 执行请求并获取响应
                    return next.exchange(request)
                            .doOnNext(response -> {
                                // 记录响应信息
                                log.info("OpenAI WebClient Response:");
                                log.info("Status Code: {}", response.statusCode());
                                log.info("Headers: {}", response.headers().asHttpHeaders());
                            });
                });
    }

    @Bean
    @Primary
    public OpenAiApi defaultOpenAiApi(WebClient.Builder webClient) {
        OpenAiApi.Builder builder = OpenAiApi.builder().baseUrl(openAiProperties.getBaseUrl())
                .apiKey(openAiProperties.getApiKey())
                .completionsPath(openAiProperties.getCompletionsPath())
                .webClientBuilder(webClient);
        
        if (openAiProperties.getApiKey() == null || "NoopApiKey".equals(openAiProperties.getApiKey())) {
            builder.apiKey(new NoopApiKey());
        }
        return builder.build();
    }


    @Bean
    @Primary
    public ChatClient defaultClient(OpenAiApi openAiApi) {
        OpenAiChatOptions openAiChatOptions = new OpenAiChatOptions();
        openAiChatOptions.setModel(openAiProperties.getChat().getModel());
        openAiChatOptions.setTemperature(openAiProperties.getChat().getTemperature());
        openAiChatOptions.setMaxTokens(openAiProperties.getChat().getMaxTokens());
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        ToolCallbackResolver toolCallbackResolver = new StaticToolCallbackResolver(new ArrayList<>());
        ToolExecutionExceptionProcessor toolExecutionExceptionProcessor = new DefaultToolExecutionExceptionProcessor(false);
        ToolCallingManager toolCallingManager = new DefaultToolCallingManager(
                observationRegistry, toolCallbackResolver, toolExecutionExceptionProcessor);
        RetryTemplate retryTemplate = RetryTemplate.defaultInstance();
        ChatModel openAiChatModel = new OpenAiChatModel(openAiApi, openAiChatOptions, toolCallingManager, retryTemplate, observationRegistry);
        return ChatClient.create(openAiChatModel);
    }


    @Bean("multiOpenAiApi")
    public OpenAiApi MultiOpenAiApi(WebClient.Builder webClient) {
        OpenAiApi.Builder builder = OpenAiApi.builder().baseUrl(openAiProperties.getMulti().getBaseUrl())
                .apiKey(openAiProperties.getMulti().getApiKey())
                .completionsPath(openAiProperties.getMulti().getCompletionsPath())
                .webClientBuilder(webClient);
        
        if (openAiProperties.getMulti().getApiKey() == null || "NoopApiKey".equals(openAiProperties.getMulti().getApiKey())) {
            log.info("multiOpenAiApi apiKey is  NoopApiKey");
            builder.apiKey(new NoopApiKey());
        }
        return builder.build();
    }


    @Bean("multiClient")
    public ChatClient getMultiClient(@Qualifier("multiOpenAiApi") OpenAiApi openAiApi) {
        OpenAiChatOptions openAiChatOptions = new OpenAiChatOptions();
        openAiChatOptions.setModel(openAiProperties.getMulti().getModel());
        openAiChatOptions.setTemperature(openAiProperties.getMulti().getTemperature());
        openAiChatOptions.setMaxTokens(openAiProperties.getMulti().getMaxTokens());
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        ToolCallbackResolver toolCallbackResolver = new StaticToolCallbackResolver(new ArrayList<>());
        ToolExecutionExceptionProcessor toolExecutionExceptionProcessor = new DefaultToolExecutionExceptionProcessor(false);
        ToolCallingManager toolCallingManager = new DefaultToolCallingManager(
                observationRegistry, toolCallbackResolver, toolExecutionExceptionProcessor);
        RetryTemplate retryTemplate = RetryTemplate.defaultInstance();
        ChatModel openAiChatModel = new OpenAiChatModel(openAiApi, openAiChatOptions, toolCallingManager, retryTemplate, observationRegistry);
        return ChatClient.create(openAiChatModel);
    }
    



}
