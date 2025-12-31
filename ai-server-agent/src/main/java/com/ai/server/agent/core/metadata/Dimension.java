

package com.ai.server.agent.core.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;


@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dimension {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("ref")
    private String ref;

    @JsonProperty("links")
    @Builder.Default
    private List<String> links = Collections.emptyList();


}
