package com.ai.server.agent.ai.agent.manager;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

/**
 * 默认PromptTemplate工厂实现类，用于创建不同类型的PromptTemplate
 */
@Component
public class DefaultPromptTemplateFactory implements PromptTemplateFactory {

    /**
     * 创建PromptTemplate实例
     * @param promptType 提示词类型
     * @param templateContent 模板内容
     * @return PromptTemplate实例
     */
    @Override
    public PromptTemplate createPromptTemplate(String promptType, String templateContent) {
        return new PromptTemplate(templateContent);
    }

    /**
     * 创建带有默认配置的PromptTemplate实例
     * @param promptType 提示词类型
     * @param templateContent 模板内容
     * @return PromptTemplate实例
     */
    @Override
    public PromptTemplate createDefaultPromptTemplate(String promptType, String templateContent) {
        // 目前默认配置与普通配置相同，后续可以根据需要扩展
        return createPromptTemplate(promptType, templateContent);
    }
}
