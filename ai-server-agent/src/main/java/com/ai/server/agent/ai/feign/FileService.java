package com.ai.server.agent.ai.feign;

import com.ai.server.agent.ai.rest.entity.FileDTO;
import com.ai.server.agent.ai.rest.entity.UploadResponseVO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author zhouyuhui
 * 1.5 文件服务
 */
@FeignClient("file-service")
@Component
public interface FileService {

    //上传
    @PostMapping(value = "/file/file-info/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Headers("Content-Type: multipart/form-data")
    UploadResponseVO upload(@RequestPart("file") MultipartFile file);

    //预览
    @RequestMapping(value = "/file/file-info/preview", method = RequestMethod.POST)
    ResponseEntity<byte[]> getPreview(@RequestBody FileDTO fileParam);

}