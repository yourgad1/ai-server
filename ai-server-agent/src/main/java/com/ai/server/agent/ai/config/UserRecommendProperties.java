package com.ai.server.agent.ai.config;

import com.ai.server.agent.ai.rest.entity.UserHeader;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "user-recommend")
@RefreshScope
@Data
public class UserRecommendProperties {
    private List<UserHeader> header = new ArrayList<>();
}