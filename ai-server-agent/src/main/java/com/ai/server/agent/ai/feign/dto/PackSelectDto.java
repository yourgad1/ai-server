package com.ai.server.agent.ai.feign.dto;

import lombok.Data;

import java.util.List;

@Data
public class PackSelectDto {

    /**
     * 期望日期
     */
    private String expectDate;

    /**
     * 接口包列表
     */
    private List<ApiMetaDto> packList;
}
