package com.ai.server.agent.ai.rest.service.toolService;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.ai.server.agent.core.metadata.Dimension;
import com.ai.server.agent.core.metadata.Metric;
import com.ai.server.agent.core.metadata.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;


@Service
@Slf4j
public class TableToMetricService {

    private final ObjectMapper mapper;

    public TableToMetricService() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        this.mapper = new ObjectMapper(yamlFactory);
    }

    /**
     * 从表格配置生成指标配置
     * @param tablesYml 表格配置的 YAML 字符串
     * @return 指标配置的 YAML 字符串
     */
    public String generateMetricsFromTables(String tablesYml) {
        try {
            // 解析 tables.yml
            Map<String, Object> tablesData = mapper.readValue(tablesYml, Map.class);
            List<Map<String, Object>> tables = (List<Map<String, Object>>) tablesData.get("tables");
            log.info("解析 tables.yml 成功: {}", tables);
            // 生成 metrics
            List<Metric> metrics = generateMetrics(tables);

            // 转换为 metric.yml 格式
            return convertToMetricYml(metrics);
        } catch (IOException e) {
            log.error("YAML处理失败: {}", e.getMessage(), e);
            throw new RuntimeException("YAML处理失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("生成指标失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成指标失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从表数据生成指标
     */
    private List<Metric> generateMetrics(List<Map<String, Object>> tables) {
        List<Metric> metrics = new ArrayList<>();

        for (Map<String, Object> table : tables) {
            //data_model
            String tableName = (String) table.get("name");
            List<Map<String, Object>> columns = (List<Map<String, Object>>) table.get("columns");
            List<Dimension> dimensions = new ArrayList<>();
            // 为每个数值类型的列或指标值列生成基本指标
            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("name");
                String columnDisplay = (String) column.get("display");
                String columnType = (String) column.get("type");
                if (columnType.equals("date")){
                    Dimension dimension = Dimension.builder().name(columnDisplay).ref(columnName).build();
                    dimensions.add(dimension);
                    continue;
                }
                if ( columnName.contains("org")){
                    dimensions.add(Dimension.builder().name(columnDisplay).ref(columnName).build());
                    continue;
                }
                String expression = "sum(" + columnName + ")";
                Metric metric = Metric.builder().name(columnDisplay).dataModel(tableName).type(MetricType.BASIC).dimensions(dimensions)
                        .expression(expression).build();
                metrics.add(metric);
            }
        }
        return metrics;
    }


    /**
     * 转换为 metric.yml 格式
     */
    private String convertToMetricYml(List<Metric> metrics) throws IOException {
        Map<String, Object> metricData = new LinkedHashMap<>();
        metricData.put("metrics", metrics);
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        yamlFactory.enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR);
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        try (StringWriter writer = new StringWriter()) {
            mapper.writeValue(writer, metricData);
            return writer.toString();
        }
    }
}
