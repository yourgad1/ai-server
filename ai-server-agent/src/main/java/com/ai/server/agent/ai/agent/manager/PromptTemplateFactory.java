package com.ai.server.agent.ai.agent.manager;

import org.springframework.ai.chat.prompt.PromptTemplate;

/**
 * PromptTemplate工厂接口，用于创建不同类型的PromptTemplate
 */
public interface PromptTemplateFactory {

    /**
     * 创建PromptTemplate实例
     * @param promptType 提示词类型
     * @param templateContent 模板内容
     * @return PromptTemplate实例
     */
    PromptTemplate createPromptTemplate(String promptType, String templateContent);

    /**
     * 创建带有默认配置的PromptTemplate实例
     * @param promptType 提示词类型
     * @param templateContent 模板内容
     * @return PromptTemplate实例
     */
    PromptTemplate createDefaultPromptTemplate(String promptType, String templateContent);
}
