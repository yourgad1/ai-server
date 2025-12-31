package com.ai.server.agent.core.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReportMatchDto {

    /**
     * 期望日期
     */
    private String expectDate;

    /**
     * 接口包列表
     */
    private List<Long> reportIds;

    private String questionId;
}
