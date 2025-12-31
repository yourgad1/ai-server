package com.ai.server.agent.ai.agent.manager;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

/**
 * 提示词管理器接口，用于管理和获取提示词模板
 */
public interface PromptManager {

    /**
     * 获取提示词模板
     * @param promptType 提示词类型
     * @return 提示词模板
     */
    PromptTemplate getPromptTemplate(String promptType);

    /**
     * 创建提示词
     * @param promptType 提示词类型
     * @param variables 模板变量
     * @return 提示词实例
     */
    Prompt createPrompt(String promptType, Map<String, Object> variables);

    /**
     * 注册提示词模板
     * @param promptType 提示词类型
     * @param templateContent 模板内容
     */
    void registerPromptTemplate(String promptType, String templateContent);

    /**
     * 检查提示词类型是否存在
     * @param promptType 提示词类型
     * @return 是否存在
     */
    boolean containsPromptType(String promptType);
}
