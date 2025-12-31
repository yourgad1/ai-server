

package com.ai.server.agent.core.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class Metric implements Cloneable {


    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("type")
    private MetricType type;
    @JsonProperty("expression")
    private String expression;

    @JsonProperty("data_model")
    private String dataModel;
    @JsonProperty("dimensions")
    private List<Dimension> dimensions = Lists.newArrayList();

    @JsonProperty("base_metrics")
    @Builder.Default
    private List<String> baseMetrics = Lists.newArrayList();


    @Override
    public Metric clone() {
        try {
            Metric clone = (Metric) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
