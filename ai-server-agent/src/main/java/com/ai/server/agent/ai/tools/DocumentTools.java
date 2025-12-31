package com.ai.server.agent.ai.tools;


import com.ai.server.agent.ai.rest.entity.ParagrphLoad;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DocumentTools {

    @Tool(description = "获取当前时间")
    public String getDate(){
        //获取当前北京时间(UTC+8)，并格式化输出xxxx年xx月xx日
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Shanghai"));
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        return now.format(formatter);
    }

    @Tool(description = "获取当前地区")
    public String getRegion(){
        return "江苏省南京市";
    }

    @Tool(description = "获取当前供电单位(供电公司)")
    public String getPowerCompany(){
        return "江苏电网";
    }


    @Tool(description = "计算[$]占位符的数量")
    public Integer countPlaceholders(@ToolParam(description = "文章内容") String content){
        //获取[$]的个数
        int count = 0;
        for (int i = 0; i < content.length() - 2; i++) {
            // 检查是否存在[$]格式的占位符
            if (content.charAt(i) == '[' && content.charAt(i+1) == '$' && content.charAt(i+2) == ']') {
                count++;
                i += 2; // 跳过已处理的占位符
            }
        }
        return count;
    }

    @Tool(description = "文档生成预览")
    public String documentGeneratePreview(@ToolParam(description = "原始文档内容（带[$]占位符）") String doc,
                                          @ToolParam(description = "替换次序和替换文字，{\"order\":\"占位符次序\",\"change\":\"替换占位符的文字\",\"isChange\":\"true/false 是否有变更内容\"}") List<ParagrphLoad.Placeholder> placeholders){
        // 定义占位符常量，方便统一修改
        final String PLACEHOLDER = "[$]";
        final int PLACEHOLDER_LENGTH = PLACEHOLDER.length();
        
        // 参数验证
        if (doc == null) {
            log.warn("文档内容为null，返回空字符串");
            return "";
        }
        
        if (placeholders == null || placeholders.isEmpty()) {
            log.warn("替换占位符列表为空，返回原始文档");
            return doc;
        }
        
        // 记录起始日志
        int originalPlaceholderCount = countPlaceholders(doc);
        log.info("开始文档生成预览，原始文档长度: {}, 占位符数量: {}", 
                 doc.length(), originalPlaceholderCount);
        
        // 创建可变字符串以进行替换操作
        StringBuilder result = new StringBuilder(doc);
        // 跟踪已处理的占位符数量
        int processedCount = 0;
        // 最大处理次数，防止无限循环（设置为占位符数量的2倍作为安全边界）
        int maxIterations = Math.max(originalPlaceholderCount * 2, 100); // 至少100次，防止占位符计数错误
        int iterationCount = 0;
        
        try {
            // 循环处理所有占位符
            while (processedCount < originalPlaceholderCount && iterationCount < maxIterations) {
                iterationCount++;
                int placeholderIndex = result.indexOf(PLACEHOLDER);
                
                // 如果找不到更多占位符，退出循环
                if (placeholderIndex == -1) {
                    log.debug("未找到更多占位符，提前退出循环");
                    break;
                }
                
                // 获取当前应处理的占位符索引
                int currentOrder = processedCount + 1;
                Optional<ParagrphLoad.Placeholder> optionalPlaceholder = 
                    placeholders.stream()
                        .filter(p -> p != null && p.getOrder() == currentOrder)
                        .findFirst();
                
                if (optionalPlaceholder.isPresent()) {
                    ParagrphLoad.Placeholder placeholder = optionalPlaceholder.get();
                    if (placeholder.getIsChange() && placeholder.getChange() != null) {
                        // 替换占位符为实际内容
                        String replacement = placeholder.getChange();
                        result.replace(placeholderIndex, placeholderIndex + PLACEHOLDER_LENGTH, replacement);
                        log.debug("替换占位符 #{}: 内容='{}', 长度变化: +{}", 
                                  currentOrder, 
                                  replacement.length() > 20 ? replacement.substring(0, 20) + "..." : replacement, 
                                  replacement.length() - PLACEHOLDER_LENGTH);
                    } else {
                        // 移动到下一个占位符，不做替换
                        log.debug("保留占位符 #{} (isChange=false)", currentOrder);
                        // 跳过当前占位符，避免死循环
                        placeholderIndex = result.indexOf(PLACEHOLDER, placeholderIndex + PLACEHOLDER_LENGTH);
                    }
                } else {
                    log.warn("找不到order={}的占位符配置，跳过当前占位符", currentOrder);
                    // 跳过当前占位符，避免死循环
                    placeholderIndex = result.indexOf(PLACEHOLDER, placeholderIndex + PLACEHOLDER_LENGTH);
                }
                
                processedCount++;
            }
        } catch (Exception e) {
            log.error("文档生成预览过程中发生异常: {}", e.getMessage(), e);
            // 发生异常时返回原始文档，确保不会返回部分处理的结果
            return doc;
        }
        
        // 安全检查：如果处理次数异常多，可能存在问题
        if (iterationCount >= maxIterations) {
            log.warn("达到最大迭代次数，可能存在问题。处理了 {} 个占位符，原始文档中有 {} 个占位符", 
                     processedCount, originalPlaceholderCount);
        }
        
        String finalResult = result.toString();
        int remainingPlaceholders = countPlaceholders(finalResult);
        log.info("文档生成预览完成，处理后的文档长度: {}, 剩余占位符数量: {}, 成功替换: {}", 
                 finalResult.length(), 
                 remainingPlaceholders, 
                 originalPlaceholderCount - remainingPlaceholders);
        
        return finalResult;
    }

}