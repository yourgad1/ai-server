

package com.ai.server.agent.core.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Column  {

    @JsonProperty("name")
    private String name;
    @JsonProperty("display")
    private String display;
    @JsonProperty("date_pattern")
    private String datePattern;

}
