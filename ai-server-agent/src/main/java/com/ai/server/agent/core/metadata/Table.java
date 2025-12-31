

package com.ai.server.agent.core.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Table implements DataModel {

    @JsonProperty("name")
    private String name;

    @JsonProperty("display")
    private String display;

    @JsonProperty("description")
    private String description;

    @JsonProperty("schema_name")
    private String schemaName;

    @JsonProperty("columns")
    private List<Column> columns;

    @Override
    public String[] getQualifiedName() {
        return new String[]{schemaName, name};
    }

}
