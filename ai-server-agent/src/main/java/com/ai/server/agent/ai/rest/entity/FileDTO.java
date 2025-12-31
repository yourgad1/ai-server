package com.ai.server.agent.ai.rest.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FileDTO {

    @Schema(name = "附件id")
    private String id;

}