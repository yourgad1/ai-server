package com.ai.server.agent.ai.rest.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MetricSample {

    private String metricName;
}
