package com.ai.server.agent.ai.rest.entity;

import lombok.Data;

import java.util.List;
@Data
public class DataResult {
    private Integer total;
    private List data;

    private Integer pageNum;


    private Integer pageSize;
}
