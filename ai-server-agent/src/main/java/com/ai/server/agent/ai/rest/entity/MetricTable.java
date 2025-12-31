package com.ai.server.agent.ai.rest.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MetricTable {

    private String tableName;
    private List<String> columnNames;
}
