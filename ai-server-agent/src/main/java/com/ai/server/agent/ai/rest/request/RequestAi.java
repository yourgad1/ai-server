package com.ai.server.agent.ai.rest.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RequestAi {

    @JsonProperty
    private String message;

    //区分不同用户问题，不同用户的动态看板
    @JsonProperty
    private String userId;
    
    //会话ID，用于标识同一用户的连续对话
    @JsonProperty
    private String sessionId;

    //识别功能类别，铭牌提取、填充审核
    private String queryType;
    //base64编码后的图片，用于铭牌提取
    private String media;
    //image/jpeg、image/png
    private String mimeType;
    //连接id
    private String connId;
}
