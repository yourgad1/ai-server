package com.ai.server.agent.ai.tools;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 构建包含文本+Base64图片的UserMessage，适配目标JSON请求体
 */
public class AiImageMessageBuilder {

    public static void main(String[] args) throws Exception {
        // 1. 构建图片Media（核心：封装image_url和Base64 URL）
        Media imageMedia = buildImageMedia("base64","image/png");

        // 2. 构建UserMessage（文本 + 图片Media）
        UserMessage userMessage = UserMessage.builder()
                .text("识别图片中的图表数据，生成JSON格式结果") // 文本内容
                .media(imageMedia) // 图片多媒体内容
                .metadata(buildMetadata()) // 可选：添加元数据
                .build();

        // 3. 验证结果（模拟框架序列化后的JSON结构）
        printSerializedMessage(userMessage);
    }

    /**
     * 构建图片Media对象（适配image_url格式）
     *
     * @return Media对象
     */
    private static Media buildImageMedia(String base64Url,String mimeType) throws IOException {

        // 3. 构建image_url的核心参数（url + detail）
        Map<String, Object> imageUrlData = new HashMap<>();
        imageUrlData.put("url", base64Url); // Base64图片URL
        imageUrlData.put("detail", "auto"); // 识别精度（auto/low/high）

        // 4. 构建Media对象（关键：MimeType指定图片类型，data存入image_url参数）
        return Media.builder()
                .mimeType(MimeType.valueOf(mimeType)) // 图片MIME类型
                .data(imageUrlData) // 核心：存入image_url的参数Map
                .name("chart-image") // 可选：自定义Media名称
                .id("media-" + System.currentTimeMillis()) // 可选：自定义Media ID
                .build();
    }



    /**
     * 根据图片格式获取MimeType（匹配Media.Format中的常量）
     */
    private static MimeType getImageMimeType(String format) {
        return switch (format.toLowerCase()) {
            case "png" -> Media.Format.IMAGE_PNG;
            case "jpg", "jpeg" -> Media.Format.IMAGE_JPEG;
            case "gif" -> Media.Format.IMAGE_GIF;
            case "webp" -> Media.Format.IMAGE_WEBP;
            default -> throw new IllegalArgumentException("不支持的图片格式：" + format);
        };
    }

    /**
     * 构建消息元数据（可选）
     */
    private static Map<String, Object> buildMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("requestId", System.currentTimeMillis());
        metadata.put("model", "gpt-4-vision-preview");
        return metadata;
    }

    /**
     * 模拟框架序列化后的JSON结构（验证是否匹配目标格式）
     */
    private static void printSerializedMessage(UserMessage userMessage) {
        // 拼接JSON字符串（模拟Spring AI客户端的序列化逻辑）
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"messages\": [\n");
        json.append("    {\n");
        json.append("      \"role\": \"").append(userMessage.getMessageType().name().toLowerCase()).append("\",\n");
        json.append("      \"content\": [\n");
        // 文本部分
        json.append("        {\"type\": \"text\", \"text\": \"").append(userMessage.getText()).append("\"},\n");
        // 图片部分
        json.append("        {\n");
        json.append("          \"type\": \"image_url\",\n");
        json.append("          \"image_url\": {\n");
        Media media = userMessage.getMedia().get(0);
        Map<String, Object> imageUrl = (Map<String, Object>) media.getData();
        json.append("            \"url\": \"").append(imageUrl.get("url")).append("\",\n");
        json.append("            \"detail\": \"").append(imageUrl.get("detail")).append("\"\n");
        json.append("          }\n");
        json.append("        }\n");
        json.append("      ],\n");
        json.append("      \"metadata\": ").append(userMessage.getMetadata()).append("\n");
        json.append("    }\n");
        json.append("  ]\n");
        json.append("}");

        // 打印结果
        System.out.println("生成的请求体JSON：");
        System.out.println(json);
    }
}