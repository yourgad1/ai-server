package com.ai.server.agent.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: zhouyuhui
 * @Description: 转码配置，需要在enums包下定义枚举类，枚举类需要提供getDesc方法,在yml中配置transcoding
 *
 */
@Component
@ConfigurationProperties(prefix = "transcoding")
@RefreshScope
@Data
public class TranscodingConfig {
    private String parentOrg;
    private List<Transcoding> trance;

    @Data
    public static class Transcoding {
        private String header;
        private String target;
    }

}
