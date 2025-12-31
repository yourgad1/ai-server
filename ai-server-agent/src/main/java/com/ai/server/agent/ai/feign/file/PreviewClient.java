package com.ai.server.agent.ai.feign.file;

import com.ai.server.agent.ai.feign.PreviewService;
import com.ai.server.agent.ai.rest.entity.ZnydFile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public class PreviewClient implements PreviewService {
    @Override
    public ResponseEntity<byte[]> getFileUrl(String id) {
        return (ResponseEntity<byte[]>) ResponseEntity.ok();
    }

    @Override
    public ZnydFile upload(MultipartFile file) {
        return null;
    }
}
