package com.ai.server.agent.ai.feign;

import com.ai.server.agent.ai.rest.entity.ZnydFile;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 1.9新oss
 * @author zhouyuhui
 */
@FeignClient("znyd-resource-service-v2")
@Component
public interface PreviewService {

    @RequestMapping(value = "/oss/db/preview/{id}", method = RequestMethod.GET)
    ResponseEntity<byte[]> getFileUrl(@PathVariable("id") String id);

    @PostMapping(value = "/oss/dp/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Headers("Content-Type: multipart/form-data")
    ZnydFile upload(@RequestPart("file") MultipartFile file);

}