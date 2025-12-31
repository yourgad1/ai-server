package com.ai.server.agent.ai.feign.mock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ai.server.agent.ai.feign.MetricClient;
import com.ai.server.agent.core.metadata.Dimension;
import com.ai.server.agent.core.metadata.Metric;
import com.ai.server.agent.core.metadata.MetricType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@ConditionalOnExpression("'${c2000.client.type}'.equals('mock')")
@Service
public class MockMetricClient implements MetricClient {
    @Override
    public List<Metric> getMetricSimpleMeta() {
        List<Metric> metrics = Lists.newArrayList();
        metrics.add(Metric.builder().name("重要用户数").description("统计各区域的重要用户的个数").type(MetricType.BASIC).build());
        metrics.add(Metric.builder().name("预测负荷").description("汇总各区域的预测负荷").type(MetricType.BASIC).build());
        return metrics;
    }

    @Override
    public Metric getMetricDetailMeta(String metricName) {
        List<Dimension> dimensions = Lists.newArrayList();
        dimensions.add(Dimension.builder().name("日期").build());
        dimensions.add(Dimension.builder().name("组织机构").build());
        return Metric.builder().name("重要用户数").description("统计电网重要用户的个数")
                .type(MetricType.BASIC).dimensions(dimensions).build();
    }

    @Override
    public List<Map<String, Object>> getMetricResult(String metricSql, String metricName) {
        List<Map<String, Object>> results = Lists.newArrayList();
        Map<String, Object> row = Maps.newTreeMap();
        if ("预测负荷".equals(metricName)) {
            row.put("组织机构", "安徽");
            row.put("日期", "2025-07-18");
            row.put("预测负荷", "70000000");
            results.add(row);
        } else {
            row.put("组织机构", "安徽");
            row.put("日期", "2025-07-18");
            row.put("重要用户数", "999");
            results.add(row);
            Map<String, Object> row2 = Maps.newTreeMap();
            row2.put("组织机构", "江苏");
            row2.put("日期", "2025-07-18");
            row2.put("重要用户数", "999");
            results.add(row2);
        }
        return results;
    }
}
