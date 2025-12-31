package com.ai.server.agent.core.jdbc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RemoteSqlDTO {
    private String appId;
    private String sql;
    private List<Object> params;
    private String dataSource;
}