package com.ai.server.agent.ai.agent.io;

import org.springframework.core.io.AbstractResource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * 自定义Resource实现，用于包装Base64图片数据
 */
public class Base64ImageResource extends AbstractResource {
    private final byte[] imageData;
    private final String filename;
    private final String description;

    public Base64ImageResource(String base64String, String filename) {
        // 去除Base64字符串中可能存在的"data:image/...;base64,"前缀
        String pureBase64 = base64String.contains(",") 
            ? base64String.substring(base64String.indexOf(",") + 1) 
            : base64String;
        
        this.imageData = Base64.getDecoder().decode(pureBase64);
        this.filename = (filename != null) ? filename : "base64_image.png";
        this.description = "Image resource created from Base64 string";
    }

    public String getFileType(String mimeType){
        return mimeType.split("/")[1];
    }

    public Base64ImageResource(String base64String) {
        this(base64String, null);
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.imageData);
    }

    @Override
    public String getFilename() {
        return this.filename;
    }

    @Override
    public long contentLength() throws IOException {
        return imageData.length;
    }

    @Override
    public boolean exists() {
        return imageData != null && imageData.length > 0;
    }
}