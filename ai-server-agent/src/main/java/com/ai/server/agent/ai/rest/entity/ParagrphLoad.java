package com.ai.server.agent.ai.rest.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParagrphLoad {
    @JsonProperty
    private String fileName;
    @JsonProperty
    private List<Placeholder> content;
    @JsonProperty
    private String text;


    public static ParagrphLoad jsonToParagrphLoad(String json) {

        return JSON.parseObject(json, ParagrphLoad.class);
    }


    @ToString
    public static class Placeholder {
        @JsonProperty
        private Integer order;
        @JsonProperty
        private String change;
        @JsonProperty
        private Boolean isChange;
        public void setOrder(Integer order) {
            this.order = order;
        }
        public void setChange(String change) {
            this.change = change;
        }
        public void setIsChange(Boolean isChange) {
            this.isChange = isChange;
        }
        public Integer getOrder() {
            return order;
        }
        public String getChange() {
            return change;
        }
        public Boolean getIsChange() {
            return isChange;
        }

        public Placeholder() {
        }
    }
}
