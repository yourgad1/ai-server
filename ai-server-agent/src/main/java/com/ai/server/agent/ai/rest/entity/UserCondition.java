package com.ai.server.agent.ai.rest.entity;

import lombok.Data;

@Data
public class UserCondition {
    private String name;
    private String display;
    private String operator;
    private String value;
//    private String type;
}
