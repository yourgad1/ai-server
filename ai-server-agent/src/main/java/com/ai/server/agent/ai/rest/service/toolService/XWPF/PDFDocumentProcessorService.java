package com.ai.server.agent.ai.rest.service.toolService.XWPF;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * PDF文档处理服务
 * 功能：1.区分是普通pdf还是扫描型pdf(图片) 2.如果是普通PDF则提取其内容，文字。如果是扫描型pdf，则将图片提取，调用多模态大模型
 */
@Service
@Slf4j
public class PDFDocumentProcessorService {

    @Autowired
    private ChatClient chatClient;

    /**
     * 处理PDF文档，区分类型并执行相应操作
     * @param multipartFile PDF文档文件
     * @return 处理结果
     */
    public String processPdfDocument(MultipartFile multipartFile) throws Exception {
        log.info("开始处理PDF文档: {}", multipartFile.getOriginalFilename());
        
        // 转换MultipartFile为File
        File file = convertMultipartFileToFile(multipartFile);
        
        // 使用PDFBox打开PDF文档
        try (PDDocument document = PDDocument.load(file)) {
            // 判断是否为扫描型PDF
            boolean isScanned = isScannedPdf(document);
            
            if (isScanned) {
                log.info("该PDF为扫描型PDF，开始提取图片并调用多模态大模型");
                return processScannedPdf(document);
            } else {
                log.info("该PDF为普通PDF，开始提取文本内容");
                return extractTextFromPdf(document);
            }
        } catch (Exception e) {
            log.error("处理PDF文档时发生错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 判断PDF是否为扫描型PDF（图片PDF）
     * @param document PDF文档对象
     * @return true为扫描型PDF，false为普通PDF
     */
    private boolean isScannedPdf(PDDocument document) throws Exception {
        // 使用PDFTextStripper提取文本
        PDFTextStripper textStripper = new PDFTextStripper();
        String text = textStripper.getText(document);
        
        // 清理提取的文本，去除空白字符
        String cleanedText = text.replaceAll("\\s+", "");
        
        // 如果提取的文本长度过短，判断为扫描型PDF
        // 这里可以根据实际情况调整阈值
        return cleanedText.length() < 10;
    }

    /**
     * 从普通PDF中提取文本内容
     * @param document PDF文档对象
     * @return 提取的文本内容
     */
    private String extractTextFromPdf(PDDocument document) throws Exception {
        PDFTextStripper textStripper = new PDFTextStripper();
        return textStripper.getText(document);
    }

    /**
     * 处理扫描型PDF，提取图片并调用多模态大模型
     * @param document PDF文档对象
     * @return 多模态大模型处理结果
     */
    private String processScannedPdf(PDDocument document) throws Exception {
        // 获取PDF的所有页面
        PDPageTree pages = document.getDocumentCatalog().getPages();
        
        // 遍历所有页面，提取图片
        for (PDPage page : pages) {
            // 获取页面的资源
            for (org.apache.pdfbox.cos.COSName name : page.getResources().getXObjectNames()) {
                PDXObject xobject = page.getResources().getXObject(name);
                if (xobject instanceof PDImageXObject) {
                    PDImageXObject image = (PDImageXObject) xobject;
                    
                    // 将图片转换为字节数组
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    javax.imageio.ImageIO.write(image.getImage(), "jpg", baos);
                    byte[] imageBytes = baos.toByteArray();
                    
                    // 构建Media对象
                    Media media = Media.builder()
                            .mimeType(MediaType.IMAGE_JPEG)
                            .data(imageBytes)
                            .build();
                    
                    // 构建UserMessage，包含提示信息和图片
                    UserMessage userMessage = UserMessage.builder()
                            .text("请识别并提取这张图片中的文本内容")
                            .media(media)
                            .build();
                    
                    // 调用多模态大模型
                    log.info("调用多模态大模型处理图片");
                    return chatClient.prompt().messages(userMessage).call().content();
                }
            }
        }
        
        // 如果没有提取到图片，返回空字符串
        return "";
    }

    /**
     * 将Spring MultipartFile转换为Java File对象
     * @param multipartFile Spring MultipartFile对象
     * @return Java File对象
     */
    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
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
     * @param fileName 文件名
     * @return 文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            // 如果没有扩展名，返回默认的pdf
            return "pdf";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}