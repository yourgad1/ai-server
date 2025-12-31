package com.ai.server.agent.ai.rest.entity;

import cn.hutool.json.JSONUtil;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MetricInfo {
    @JsonProperty("metricName")
    private String metricName;
    @JsonProperty("dataDate")
    private List<String> dataDate;
    @JsonProperty("mgtOrgCode")
    private List<String> mgtOrgCode;


    public static void main(String[] args) {
        String msg="{\"metricName\":\"重要用户数\",\"dataDate\":[\"'2025-09-19'\"],\"mgtOrgCode\":[\"'1101'\"]}";
        System.out.println(JSONUtil.toBean(msg, MetricInfo.class));
    }
}
