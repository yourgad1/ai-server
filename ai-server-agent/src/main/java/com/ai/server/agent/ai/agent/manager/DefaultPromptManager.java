package com.ai.server.agent.ai.agent.manager;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认提示词管理器实现类，使用单例模式管理提示词模板
 */
@Component
public class DefaultPromptManager implements PromptManager {

    // 存储提示词模板的映射，key为提示词类型，value为提示词模板
    private final Map<String, PromptTemplate> promptTemplates = new HashMap<>();

    /**
     * 获取提示词模板
     * @param promptType 提示词类型
     * @return 提示词模板
     */
    @Override
    public PromptTemplate getPromptTemplate(String promptType) {
        PromptTemplate template = promptTemplates.get(promptType);
        if (template == null) {
            throw new IllegalArgumentException("Prompt template not found for type: " + promptType);
        }
        return template;
    }

    /**
     * 创建提示词
     * @param promptType 提示词类型
     * @param variables 模板变量
     * @return 提示词实例
     */
    @Override
    public Prompt createPrompt(String promptType, Map<String, Object> variables) {
        PromptTemplate template = getPromptTemplate(promptType);
        return template.create(variables);
    }

    /**
     * 注册提示词模板
     * @param promptType 提示词类型
     * @param templateContent 模板内容
     */
    @Override
    public void registerPromptTemplate(String promptType, String templateContent) {
        if (promptType == null || templateContent == null) {
            throw new IllegalArgumentException("Prompt type and template content cannot be null");
        }
        promptTemplates.put(promptType, new PromptTemplate(templateContent));
    }

    /**
     * 检查提示词类型是否存在
     * @param promptType 提示词类型
     * @return 是否存在
     */
    @Override
    public boolean containsPromptType(String promptType) {
        return promptTemplates.containsKey(promptType);
    }
}
