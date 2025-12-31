package com.ai.server.agent.ai.rest.entity;

import lombok.Data;

@Data
public class UploadResponseVO {

    private Boolean success;

    private String code;

    private String message;

    private String data;
}