
package com.ai.server.agent.ai.feign.dto;


import lombok.Data;

@Data
public class R<T> {
    /**
     * 状态码(200代表成功)
     */
    private int code;

    private boolean success;
    private String message;
    private String messageCode;
    private String timestamp;
    private T data;

}
