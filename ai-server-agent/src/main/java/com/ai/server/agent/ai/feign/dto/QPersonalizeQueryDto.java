package com.ai.server.agent.ai.feign.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QPersonalizeQueryDto {

    private String userId;
}
