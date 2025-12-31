package com.ai.server.agent.ai.feign;

import com.ai.server.agent.core.metadata.Metric;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


@FeignClient(value = "metric-engine-server-query")
@Component
public interface MetricClient {

    /**
     * 获取当前系统的所有指标（需包含指标名称和指标定义）
     */
    @RequestMapping(value = "/api/metric/v1/getMetricSimpleMeta", method = RequestMethod.POST)
    List<Metric> getMetricSimpleMeta();

    /**
     * 根据指标名称，获取指标详情（需包含直接的子指标名称、指标类型和维度名称）
     */
    @RequestMapping(value = "/api/metric/v1/getMetricDetailMeta", method = RequestMethod.POST)
    Metric getMetricDetailMeta(@RequestParam String metricName);

    /**
     * 根据指标sql和指标名称，查询指标数据
     */
    @RequestMapping(value = "/api/metric/v1/getMetricResult", method = RequestMethod.POST)
    List<Map<String, Object>> getMetricResult(@RequestParam String metricSql, @RequestParam String metricName);

}
