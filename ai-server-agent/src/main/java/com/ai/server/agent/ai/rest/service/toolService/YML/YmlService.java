package com.ai.server.agent.ai.rest.service.toolService.YML;

import cn.hutool.core.io.FileUtil;
import com.ai.server.agent.ai.rest.service.toolService.TableToMetricService;
import com.ai.server.agent.ai.common.sse.AiGlobalSseEmitterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


@Service
@Slf4j
public class YmlService {


    @Autowired
    private TableToMetricService tableToMetricService;

    @Value("${metadata-path:/metadata}")
    private String metadataPath;

    @Autowired
    private AiGlobalSseEmitterManager sseEmitterManager ;

    /**
     * 判断是否需要生成metric.yml
     */
    private boolean isGenerateMetricRequired(String fileName, String llmResponse) {
        return isTablesFile(fileName) && llmResponse.contains("需要转换为metric.yml");
    }

    /**
     * 判断是否为tables文件
     */
    private boolean isTablesFile(String fileName) {
        return fileName.toLowerCase().contains("tables") && (fileName.endsWith(".yml") || fileName.endsWith(".yaml"));
    }

    /**
     * 处理tables.yml转换为metric.yml的逻辑
     * @param tableYmlFile
     * @throws IOException
     *
     */
    public void processTablesToMetric(MultipartFile tableYmlFile) throws Exception {
//        try {
//            // 1. 保存tables.yml文件
//            sseEmitterManager.sendEvent(ConnectionIdContext.getConnectionId(),ResponseAi.ofMessage("开始保存tables.yml文件..."));
//            String tablesFilePath = saveTablesFile(tableYmlFile.getBytes());
//            sseEmitterManager.sendEvent(ConnectionIdContext.getConnectionId(),ResponseAi.ofMessage("已保存tables.yml文件到：" + tablesFilePath));
//
//            // 2. 生成metric.yml
//            sseEmitterManager.sendEvent(ConnectionIdContext.getConnectionId(),ResponseAi.ofMessage("开始生成metric.yml文件..."));
//            //从tables.yml文件中读取内容
//            new String(tableYmlFile.getBytes(), StandardCharsets.UTF_8);
//            String tableYmlContent = FileUtil.readString(tablesFilePath, "UTF-8");
//            String metricYmlContent = tableToMetricService.generateMetricsFromTables(tableYmlContent);
//
//            // 3. 保存生成的metric.yml文件
//            String metricFilePath = saveMetricFile(metricYmlContent);
//            sseEmitterManager.sendEvent(ConnectionIdContext.getConnectionId(),ResponseAi.ofMessage("已成功生成并保存metric.yml文件到：" + metricFilePath));
//
//        } catch (Exception e) {
//            log.error("生成metric.yml失败: {}", e.getMessage(), e);
//            sseEmitterManager.sendEvent(ConnectionIdContext.getConnectionId(),ResponseAi.ofMessage("生成metric.yml失败: " + e.getMessage()));
//        }
    }

    /**
     * 保存tables.yml文件
     */
    private String saveTablesFile(byte[] fileContent) {
        String tablesDirPath = getTablesDirPath();
        FileUtil.mkdir(tablesDirPath);
        String filePath = tablesDirPath + File.separator + "tables.yml";
        FileUtil.writeBytes(fileContent, filePath);
        return filePath;
    }

    /**
     * 保存metric.yml文件
     */
    private String saveMetricFile(String fileContent) {
        String metricsDirPath = getMetricsDirPath();
        FileUtil.mkdir(metricsDirPath);
        String filePath = metricsDirPath + File.separator + "metric.yml";
        FileUtil.writeString(fileContent, filePath, "UTF-8");
        return filePath;
    }

    /**
     * 获取tables目录路径
     */
    private String getTablesDirPath() {
        // 获取项目根目录
        String rootPath = System.getProperty("user.dir");
        // 构建完整的tables目录路径
        return rootPath + metadataPath + File.separator + "tables";
    }

    /**
     * 获取metrics目录路径
     */
    private String getMetricsDirPath() {
        // 获取项目根目录
        String rootPath = System.getProperty("user.dir");
        // 构建完整的metrics目录路径
        return rootPath + metadataPath + File.separator + "metrics";
    }
}
