package com.ai.server.agent.ai.rest.service.toolService.XWPF;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ai.server.agent.ai.util.ChartToWordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 图表文档服务
 * 整合图表生成和Word文档操作，提供完整的数据可视化和文档生成功能
 */
@Service
@Slf4j
public class ChartDocumentService {

    private final ChartGeneratorService chartGeneratorService;
    private final ChartToWordUtil chartToWordUtil;
    public ChartDocumentService(ChartGeneratorService chartGeneratorService,
                              ChartToWordUtil chartToWordUtil) {
        this.chartGeneratorService = chartGeneratorService;
        this.chartToWordUtil = chartToWordUtil;
    }

    private final int CHART_WIDTH = 300;
    private final int CHART_HEIGHT = 200;


    public boolean insertChart(String agentJson,XWPFDocument document){
        JSONObject jsonObject = JSON.parseObject(agentJson);
        String chartType = jsonObject.getString("chartType");
        JSONObject chartData = jsonObject.getJSONObject("chartData");
        switch (chartType){
            case "bar":
                return insertBarChart(document, "[BAR_CHART]", jsonObject.getString("chartName"), chartData.toJavaObject(Map.class));
            case "line":
                return insertLineChart(document, "[LINE_CHART]", jsonObject.getString("chartName"), chartData.toJavaObject(Map.class));
            case "pie":
                return insertPieChart(document, "[PIE_CHART]", jsonObject.getString("chartName"), chartData.toJavaObject(Map.class));
            default:
                log.error("未知的图表类型: {}", chartType);
                return false;
        }
    }

    /**
     * 在Word文档中插入柱状图
     * @param document 要处理的Word文档
     * @param placeholder 占位符文本（如 "[BAR_CHART]"）
     * @param title 图表标题
     * @param chartData 图表数据
     * @return 是否成功插入图表
     */
    public boolean insertBarChart(XWPFDocument document, String placeholder, String title, 
                                Map<String, Double> chartData) {
        try {
            // 生成柱状图
            byte[] chartImage = chartGeneratorService.generateDefaultBarChart(title, chartData);
            if (chartImage == null) {
                log.error("生成柱状图失败");
                return false;
            }

            // 插入图表到文档
            return chartToWordUtil.insertChartAtPlaceholder(document, placeholder, chartImage, CHART_WIDTH, CHART_HEIGHT);
        } catch (Exception e) {
            log.error("插入柱状图到文档时出错: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 在Word文档中插入折线图
     * @param document 要处理的Word文档
     * @param placeholder 占位符文本（如 "[LINE_CHART]"）
     * @param title 图表标题
     * @param chartData 图表数据
     * @return 是否成功插入图表
     */
    public boolean insertLineChart(XWPFDocument document, String placeholder, String title, 
                                 Map<String, Double> chartData) {
        try {
            // 生成折线图
            byte[] chartImage = chartGeneratorService.generateDefaultLineChart(title, chartData);
            if (chartImage == null) {
                log.error("生成折线图失败");
                return false;
            }

            // 插入图表到文档
            return chartToWordUtil.insertChartAtPlaceholder(document, placeholder, chartImage, CHART_WIDTH, CHART_HEIGHT);
        } catch (Exception e) {
            log.error("插入折线图到文档时出错: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 在Word文档中插入饼图
     * @param document 要处理的Word文档
     * @param placeholder 占位符文本（如 "[PIE_CHART]"）
     * @param title 图表标题
     * @param chartData 图表数据
     * @return 是否成功插入图表
     */
    public boolean insertPieChart(XWPFDocument document, String placeholder, String title, 
                                Map<String, Double> chartData) {
        try {
            // 生成饼图
            byte[] chartImage = chartGeneratorService.generateDefaultPieChart(title, chartData);
            if (chartImage == null) {
                log.error("生成饼图失败");
                return false;
            }

            // 插入图表到文档
            return chartToWordUtil.insertChartAtPlaceholder(document, placeholder, chartImage, CHART_WIDTH, CHART_HEIGHT);
        } catch (Exception e) {
            log.error("插入饼图到文档时出错: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 处理上传的Word文档并插入多个图表
     * @param multipartFile 上传的Word文档
     * @param chartDataMap 图表数据映射，键为占位符，值为图表数据
     * @param chartTypes 图表类型映射，键为占位符，值为图表类型（bar 柱状图, line 折线图, pie 饼图）
     * @return 处理后的Word文档字节数组
     */
    public byte[] processDocumentWithCharts(MultipartFile multipartFile, 
                                          Map<String, Map<String, Double>> chartDataMap,
                                          Map<String, String> chartTypes,
                                          Map<String, String> chartTitles) throws Exception {
        try {
            // 将MultipartFile转换为File
            File file = WordDocumentProcessorService.convertMultipartFileToFile(multipartFile);
            
            // 加载Word文档
            XWPFDocument document = new XWPFDocument(new FileInputStream(file));
            
            // 插入多个图表
            for (Map.Entry<String, Map<String, Double>> entry : chartDataMap.entrySet()) {
                String placeholder = entry.getKey();
                Map<String, Double> chartData = entry.getValue();
                String chartType = chartTypes.getOrDefault(placeholder, "bar"); // 默认柱状图
                String chartTitle = chartTitles.getOrDefault(placeholder, "图表");
                
                boolean success = false;
                switch (chartType.toLowerCase()) {
                    case "line":
                        success = insertLineChart(document, placeholder, chartTitle, chartData);
                        break;
                    case "pie":
                        success = insertPieChart(document, placeholder, chartTitle, chartData);
                        break;
                    case "bar":
                    default:
                        success = insertBarChart(document, placeholder, chartTitle, chartData);
                        break;
                }
                
                if (!success) {
                    log.warn("插入图表失败: {}, 占位符: {}", chartType, placeholder);
                }
            }
            
            // 将文档转换为字节数组
            return chartToWordUtil.documentToByteArray(document);
        } catch (Exception e) {
            log.error("处理文档并插入图表时出错: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 示例方法：生成示例数据
     * 实际应用中，这些数据应该从数据库查询获取
     */
    public Map<String, Double> generateSampleData() {
        Map<String, Double> data = new HashMap<>();
        data.put("一月", 1500.0);
        data.put("二月", 2300.0);
        data.put("三月", 3200.0);
        data.put("四月", 2800.0);
        data.put("五月", 4100.0);
        data.put("六月", 3500.0);
        return data;
    }

    /**
     * 示例方法：生成饼图示例数据
     */
    public Map<String, Double> generateSamplePieData() {
        Map<String, Double> data = new HashMap<>();
        data.put("A产品", 35.5);
        data.put("B产品", 25.3);
        data.put("C产品", 20.2);
        data.put("D产品", 19.0);
        return data;
    }
}