package com.ai.server.agent.ai.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * 图表到Word文档的工具类
 * 提供将生成的图表插入到Word文档指定位置的功能
 */
@Component
@Slf4j
public class ChartToWordUtil {

    /**
     * 在Word文档中查找并替换特定占位符为图表
     * @param document 要处理的Word文档
     * @param placeholder 占位符文本（如 "[$CHART1]"）
     * @param chartData 图表的字节数组
     * @param width 图表宽度（单位：像素）
     * @param height 图表高度（单位：像素）
     * @return 是否成功插入图表
     */
    public boolean insertChartAtPlaceholder(XWPFDocument document, String placeholder, 
                                          byte[] chartData, int width, int height) {
        // 参数验证
        if (document == null || placeholder == null || chartData == null || chartData.length == 0) {
            log.error("参数不完整或无效，无法插入图表: document={}, placeholder={}, chartData={}, chartDataLength={}", 
                     document != null, placeholder != null, chartData != null, chartData != null ? chartData.length : 0);
            return false;
        }
        
        // 验证图表数据是否为有效的PNG格式 - 添加完整的边界检查
        if (chartData.length < 8) {
            log.error("图表数据长度不足，不是有效的PNG格式，无法插入");
            return false;
        }
        
        // 安全地检查PNG文件头
        boolean isValidPNG = chartData[0] == (byte)0x89 && 
                           chartData[1] == (byte)0x50 && 
                           chartData[2] == (byte)0x4E && 
                           chartData[3] == (byte)0x47;
        
        if (!isValidPNG) {
            log.error("图表数据不是有效的PNG格式，首字节: {}, {}, {}, {}", 
                     chartData[0], chartData[1], chartData[2], chartData[3]);
            return false;
        }
        
        log.info("图表数据验证通过，PNG格式正确，数据长度: {} 字节", chartData.length);
        
        // 设置默认尺寸
        if (width <= 0) width = 600;
        if (height <= 0) height = 400;
        
        log.info("插入图表参数: 占位符='{}', 宽度={}px, 高度={}px", placeholder, width, height);
        
        // 遍历所有段落
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String paragraphText = paragraph.getText();
            log.debug("检查段落文本: '{}'", paragraphText);
            
            // 如果段落包含占位符
            if (paragraphText != null && paragraphText.contains(placeholder)) {
                log.info("找到包含占位符的段落");
                
                // 直接修改原段落，清空内容后添加新内容
                try {
                    // 清空原段落中的所有运行元素
                    List<XWPFRun> runs = paragraph.getRuns();
                    for (int i = runs.size() - 1; i >= 0; i--) {
                        paragraph.removeRun(i);
                    }
                    log.info("已清空原段落中的所有内容");
                    
                    // 分割文本：占位符前、占位符、占位符后
                    int placeholderIndex = paragraphText.indexOf(placeholder);
                    String textBefore = paragraphText.substring(0, placeholderIndex);
                    String textAfter = paragraphText.substring(placeholderIndex + placeholder.length());
                    
                    // 记录原始样式（如果有）
                    XWPFRun originalRun = runs.isEmpty() ? null : runs.get(0);
                    
                    // 添加占位符前的文本
                    if (!textBefore.isEmpty()) {
                        XWPFRun beforeRun = paragraph.createRun();
                        beforeRun.setText(textBefore);
                        // 尝试复制原始样式
                        if (originalRun != null) {
                            copyRunProperties(originalRun, beforeRun);
                        }
                        log.debug("添加占位符前的文本: '{}'", textBefore);
                    }
                    
                    // 创建用于插入图片的Run
                    XWPFRun chartRun = paragraph.createRun();
                    
                    // 直接使用Units.toEMU转换像素到EMU单位
                    int emuWidth = Units.toEMU(width);
                    int emuHeight = Units.toEMU(height);
                    log.info("转换后的EMU单位: 宽度={}, 高度={}", emuWidth, emuHeight);
                    
                    // 使用try-with-resources确保流被正确关闭
                    try (ByteArrayInputStream imageStream = new ByteArrayInputStream(chartData)) {
                        // 直接使用addPicture方法插入图片
                        chartRun.addPicture(imageStream, XWPFDocument.PICTURE_TYPE_PNG, "chart.png", emuWidth, emuHeight);
                        log.info("图表图片已成功插入到文档中");
                    } catch (IndexOutOfBoundsException e) {
                        log.error("插入图表时发生索引越界异常: {}", e.getMessage());
                        return false;
                    }
                    
                    // 添加占位符后的文本
                    if (!textAfter.isEmpty()) {
                        XWPFRun afterRun = paragraph.createRun();
                        afterRun.setText(textAfter);
                        // 尝试复制原始样式
                        if (originalRun != null) {
                            copyRunProperties(originalRun, afterRun);
                        }
                        log.debug("添加占位符后的文本: '{}'", textAfter);
                    }
                    
                    log.info("成功修改原段落内容，插入图表并保留必要文本");
                    
                    // 只处理第一个找到的占位符
                    return true;
                    
                } catch (Exception e) {
                    log.error("插入图表时出错: {}", e.getMessage(), e);
                    // 更详细地记录异常信息
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    StringBuilder stackTraceStr = new StringBuilder();
                    for (int i = 0; i < Math.min(5, stackTrace.length); i++) {
                        stackTraceStr.append(stackTrace[i]).append("\n");
                    }
                    log.error("异常堆栈前5行: \n{}", stackTraceStr.toString());
                    return false;
                }
            }
        }
        
        log.warn("未在文档中找到占位符: {}", placeholder);
        return false;
    }

    /**
     * 复制Run的属性从源Run到目标Run
     */
    private void copyRunProperties(XWPFRun source, XWPFRun target) {
        if (source == null || target == null) {
            return;
        }
        
        // 复制字体设置
        if (source.getFontSize() != -1) {
            target.setFontSize(source.getFontSize());
        }
        
        if (source.getFontFamily() != null) {
            target.setFontFamily(source.getFontFamily());
        }
        
        if (source.getColor() != null) {
            target.setColor(source.getColor());
        }
        
        // 复制样式设置
        target.setBold(source.isBold());
        target.setItalic(source.isItalic());
        target.setUnderline(source.getUnderline());
        target.setStrikeThrough(source.isStrikeThrough());
    }

    /**
     * 在Word文档中查找并替换多个图表占位符
     * @param document 要处理的Word文档
     * @param chartDataMap 占位符和图表数据的映射
     * @return 成功插入的图表数量
     */
    public int insertMultipleCharts(XWPFDocument document, Map<String, byte[]> chartDataMap) {
        int count = 0;
        
        if (document == null || chartDataMap == null || chartDataMap.isEmpty()) {
            log.error("参数不完整，无法插入图表");
            return 0;
        }
        
        for (Map.Entry<String, byte[]> entry : chartDataMap.entrySet()) {
            String placeholder = entry.getKey();
            byte[] chartData = entry.getValue();
            
            if (insertChartAtPlaceholder(document, placeholder, chartData, 500, 300)) {
                count++;
            }
        }
        
        log.info("成功插入 {} 个图表到文档中", count);
        return count;
    }
    
    /**
     * 在Word文档中查找并替换多个图表占位符（支持自定义尺寸）
     * @param document 要处理的Word文档
     * @param chartDataMap 占位符和图表数据的映射
     * @param width 图表宽度（像素）
     * @param height 图表高度（像素）
     * @return 是否成功插入所有图表
     */
    public boolean insertMultipleCharts(XWPFDocument document, Map<String, byte[]> chartDataMap, int width, int height) {
        if (document == null || chartDataMap == null || chartDataMap.isEmpty()) {
            log.error("参数不完整，无法插入图表");
            return false;
        }
        
        boolean allSuccess = true;
        for (Map.Entry<String, byte[]> entry : chartDataMap.entrySet()) {
            String placeholder = entry.getKey();
            byte[] chartData = entry.getValue();
            
            if (!insertChartAtPlaceholder(document, placeholder, chartData, width, height)) {
                allSuccess = false;
                log.warn("插入图表失败，占位符: {}", placeholder);
            }
        }
        
        log.info("多图表插入完成（自定义尺寸：{}x{}像素），是否全部成功: {}", width, height, allSuccess);
        return allSuccess;
    }

    /**
     * 从文件路径加载Word文档
     * @param filePath 文件路径
     * @return XWPFDocument对象
     */
    public XWPFDocument loadWordDocument(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return new XWPFDocument(fis);
        }
    }

    /**
     * 保存Word文档到指定路径
     * @param document 文档对象
     * @param filePath 保存路径
     */
    public void saveWordDocument(XWPFDocument document, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            document.write(fos);
        }
        log.info("文档已保存到: {}", filePath);
    }

    /**
     * 将Word文档转换为字节数组
     * @param document 文档对象
     * @return 字节数组
     */
    public byte[] documentToByteArray(XWPFDocument document) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        out.flush();
        byte[] data = out.toByteArray();
        out.close();
        return data;
    }


}