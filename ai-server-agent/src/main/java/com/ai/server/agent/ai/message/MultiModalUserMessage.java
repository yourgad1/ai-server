package com.ai.server.agent.ai.message;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义多模态用户消息类，支持特定的多模态请求格式
 * 格式示例：
 * {
 *   "role": "user",
 *   "content": [
 *     {
 *       "type": "text",
 *       "text": "What's in this image?"
 *     },
 *     {
 *       "type": "image_base64",
 *       "image": "image_base64"
 *     }
 *   ]
 * }
 */
public class MultiModalUserMessage {

    private final UserMessage userMessage;

    /**
     * 构造函数，使用文本和媒体内容创建多模态用户消息
     * @param text 文本内容
     * @param media 媒体内容
     */
    public MultiModalUserMessage(String text, Media media) {
        // 使用builder模式创建UserMessage实例
        this.userMessage = UserMessage.builder()
                .text(text)
                .media(media)
                .build();
    }

    /**
     * 生成符合要求的多模态请求格式
     * @return 符合要求的多模态请求格式的Map
     */
    public Map<String, Object> toMultiModalMap() {
        // 创建可变的消息结构
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("role", "user");
        
        // 创建content列表，包含文本和媒体内容
        List<Map<String, Object>> content = new ArrayList<>();
        
        // 添加文本内容
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", userMessage.getText());
        content.add(textContent);
        
        // 添加媒体内容（如果有）
        List<Media> mediaList = userMessage.getMedia();
        if (mediaList != null && !mediaList.isEmpty()) {
            for (Media media : mediaList) {
                String mediaType = getMediaType(media.getMimeType());
                
                // 获取媒体数据并转换为Base64字符串
                String imageBase64 = media.getData() != null ? media.getData().toString() : "";
                
                Map<String, Object> mediaContent = new HashMap<>();
                mediaContent.put("type", mediaType);
                mediaContent.put("image", imageBase64);
                content.add(mediaContent);
            }
        }
        
        // 添加content字段
        messageMap.put("content", content);
        
        return messageMap;
    }

    /**
     * 根据MimeType获取媒体类型字符串
     * @param mimeType MimeType对象
     * @return 媒体类型字符串，如"image_base64"
     */
    private String getMediaType(MimeType mimeType) {
        if (mimeType == null) {
            return "image_base64"; // 默认使用image_base64
        }
        
        String type = mimeType.getType();
        String subtype = mimeType.getSubtype();
        
        // 处理图片类型
        if ("image".equals(type)) {
            return "image_base64";
        }
        
        // 处理其他类型（可以根据需要扩展）
        return type + "_" + subtype;
    }

    /**
     * 获取内部的UserMessage实例
     * @return UserMessage实例
     */
    public UserMessage getUserMessage() {
        return userMessage;
    }
}
