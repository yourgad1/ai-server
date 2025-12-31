package com.ai.server.agent.ai.rest.service.toolService.XWPF;


import com.ai.server.agent.ai.rest.entity.ParagrphLoad;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Word文档处理服务
 * 提供Word文档解析（提取样式和内容）和生成（根据模板还原样式）功能
 * 使用基于行的文档编号方案
 */
@Service
@Slf4j
public class WordDocumentProcessorService {

    /**
     * 解析Word文档，提取内容
     * @param multipartFile Word文档文件
     */
    public String parseWordDocument(MultipartFile multipartFile) throws IOException, InvalidFormatException {
        File file = convertMultipartFileToFile(multipartFile);
        if (file.getName().toLowerCase().endsWith(".docx") || file.getName().toLowerCase().endsWith(".doc")) {
            XWPFDocument document = new XWPFDocument(new FileInputStream(file));
            return parseDocxDocument(document);
        } else {
            throw new IllegalArgumentException("不支持的文件格式，仅支持.doc和.docx格式");
        }
    }

    /**
     * 解析word格式文档
     * 基于段落的文方案实现，提取内容
     */
    public String parseDocxDocument(XWPFDocument document) throws IOException, InvalidFormatException {
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        StringBuffer stringBuffer = new StringBuffer();
        for (XWPFParagraph paragraph : paragraphs) {
            String paragraphText = paragraph.getParagraphText();
            log.info("段落内容: {}", paragraphText);
            if (StringUtil.isNotBlank(paragraphText)) {
                stringBuffer.append(paragraphText);
            }
            
        }

       return stringBuffer.toString();
    }

    /**
     * 处理文档中的占位符并更新内容
     */
    public XWPFDocument processAndUpdateDocument(MultipartFile multipartFile, ParagrphLoad doc) throws Exception {
        File file = convertMultipartFileToFile(multipartFile);
        XWPFDocument document = new XWPFDocument(new FileInputStream(file));
        return getNewDocument(document, doc);
    }
    /**
     * 变更文档内容并返回更新后的文档
     * 基于word run的方式实现
     */
    public XWPFDocument getNewDocument(XWPFDocument document,ParagrphLoad doc){
        log.info("开始处理文档中的占位符");
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        List<ParagrphLoad.Placeholder> content = doc.getContent();
        AtomicInteger startIndex = new AtomicInteger(1);
        for (XWPFParagraph paragraph : paragraphs) {
            String paragraphText = paragraph.getParagraphText();
            AtomicReference<Integer> placeholders = new AtomicReference<>(countPlaceholders(paragraphText));
            // 判断是否包含占位符
            if (paragraphText.contains("[$]")) { 
                List<XWPFRun> runs = paragraph.getRuns();
                for (int i = 0; i < runs.size(); i++) {
                    XWPFRun run = runs.get(i);
                    AtomicReference<String> text = new AtomicReference<>(run.text());
                    if (text.get().contains("[$]")) {
                        //循环检测text中的占位符
                        while (text.get().contains("[$]")){
                            if (placeholders.get() == 0){
                                //占位符数量为0，则结束循环
                                break;
                            }
                            int i1 = text.get().indexOf("[$]");
                            int i2 = i1 + 3;
                            //替换占位符,获取order=startIndex的placeholder,如果isChange为true则将占位符替换为content中的内容
                            int finalStartIndex = startIndex.get();
                            content.stream().filter(placeholder -> placeholder.getOrder() == finalStartIndex).findFirst().ifPresent(placeholder -> {
                                if (placeholder.getIsChange()) {
                                    //替换占位符，并将startIndex加1
                                    //将i1到i2之间的内容替换为content中的内容
                                    text.set(text.get().substring(0, i1) + placeholder.getChange() + text.get().substring(i2));
                                    //清空run，并设置新的文本
                                    run.setText(text.get(), 0);
                                    startIndex.getAndIncrement();
                                    placeholders.getAndSet(placeholders.get() - 1);
                                } else {
                                    // 如果isChange为false，则保留原占位符，仅将startIndex加1
                                    startIndex.getAndIncrement();
                                    placeholders.getAndSet(placeholders.get() - 1);
                                }
                            });
                        }
                    }
                }
            }
        }
        log.info("文档处理完成");
        //saveDocumentAsFile(document, doc.getFileName());
        return document;
    }

    /**
     * 统计占位符的数量
     */
    public Integer countPlaceholders(String content){
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



    /**
     * 将更新后的XWPFDocument保存为文件
     */
    public void saveDocumentAsFile(XWPFDocument document, String fileName) {
        File outputFile = null;
        try {
            // 创建临时文件
            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "updated_document.docx";
            }

            // 确保文件名以.docx结尾
            if (!fileName.toLowerCase().endsWith(".docx")) {
                fileName += ".docx";
            }

            //使用filename为文件名
            //指定路径为A:\file
            outputFile = new File("A:\\file", fileName);
            outputFile.deleteOnExit(); // 程序退出时删除临时文件

            // 保存文档到文件
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                document.write(fos);
                fos.flush();
            }
            //关闭文档流
            document.close();
            log.info("Document saved to temporary file: {}", outputFile.getAbsolutePath());

        } catch (Exception e) {
            log.error("Error saving document to file: {}", e.getMessage());
            // 清理临时文件
            if (outputFile != null && outputFile.exists()) {
                outputFile.delete();
            }

        }
    }


    /**
     * 将Spring MultipartFile转换为Java File对象
     */
    /**
     * 将Spring MultipartFile转换为Java File对象
     */
    public static File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        // 创建临时文件
        File tempFile = File.createTempFile(
                "temp_" + System.currentTimeMillis(),
                "." + getFileExtension(multipartFile.getOriginalFilename())
        );

        // 将MultipartFile的内容写入临时文件
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }

        // 设置临时文件在JVM退出时自动删除
        tempFile.deleteOnExit();

        return tempFile;
    }
    /**
     * 获取文件名的扩展名
     */
    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            // 如果没有扩展名，返回默认的docx
            return "docx";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }





}
