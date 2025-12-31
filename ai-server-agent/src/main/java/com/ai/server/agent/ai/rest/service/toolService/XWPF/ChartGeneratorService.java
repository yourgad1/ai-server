package com.ai.server.agent.ai.rest.service.toolService.XWPF;

import com.ai.server.agent.ai.config.ChartStyleConfig;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 图表生成服务
 * 提供各种类型图表的生成功能，支持柱状图、折线图、饼图等
 */
@Service
@Slf4j
public class ChartGeneratorService {

    private static volatile boolean fontInitialized = false;
    private static final Object fontLock = new Object();

    // 修改构造函数，延迟字体初始化
    public ChartGeneratorService() {
        initializeFontsLazily();
    }

    private static void initializeFontsLazily() {
        if (!fontInitialized) {
            synchronized (fontLock) {
                if (!fontInitialized) {
                    try {
                        // 设置无头模式
                        System.setProperty("java.awt.headless", "true");

                        // 使用安全的字体检测方式
                        safeFontDetection();

                    } catch (Throwable e) {
                        // 捕获所有异常，包括Error
                        System.err.println("字体初始化失败，但不影响服务功能: " + e.getMessage());
                    } finally {
                        fontInitialized = true;
                    }
                }
            }
        }
    }

    private static void safeFontDetection() {
        try {
            // 使用反射方式检测字体，避免直接调用可能失败的方法
            Class<?> geClass = Class.forName("java.awt.GraphicsEnvironment");
            java.lang.reflect.Method getLocalMethod = geClass.getMethod("getLocalGraphicsEnvironment");
            Object ge = getLocalMethod.invoke(null);

            java.lang.reflect.Method getFontsMethod = geClass.getMethod("getAvailableFontFamilyNames");
            String[] fontNames = (String[]) getFontsMethod.invoke(ge);

            System.out.println("安全检测到字体数量: " + fontNames.length);

        } catch (Exception e) {
            System.out.println("安全字体检测完成（可能无字体）");
        }
    }

    // 样式配置已移至ChartStyleConfig类
    
    /**
     * 用于测试的main方法
     */
    public static void main(String[] args) {
        log.info("==== 开始图表生成测试 ====");
        
        // 获取当前时间戳，用于文件名
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        log.info("当前时间戳: " + timestamp);
        
        // 创建测试数据集
        log.info("创建测试数据集...");
        Map<String, Double> testData = new HashMap<>();
        testData.put("产品A", 35.5);
        testData.put("产品B", 42.3);
        testData.put("产品C", 28.7);
        testData.put("产品D", 51.2);
        testData.put("产品E", 38.9);
        log.info("测试数据集创建完成: " + testData);
        
        // 获取明确的输出路径，确保路径存在
        String outputDirPath = System.getProperty("user.dir");
        File outputDir = new File(outputDirPath);
        log.info("使用工作目录作为输出目录: " + outputDir.getAbsolutePath());
        log.info("输出目录是否存在: " + outputDir.exists());
        
        // 创建服务实例并生成图表
        log.info("初始化图表生成服务...");
        ChartGeneratorService service = new ChartGeneratorService();
        
        try {
            // 直接使用当前目录下的文件名，不使用路径分隔符
            String testChartPath = "test_chart_" + timestamp + ".png";
            log.info("开始生成测试图表...");
            
            // 生成图表
            byte[] chartData = service.generateDefaultBarChart("测试销售数据", testData);
            
            // 详细的图表数据信息
            if (chartData == null) {
                log.info("警告: 图表数据为null");
            } else {
                log.info("图表数据生成成功，数据长度: " + chartData.length + " 字节");
            }
            
            // 保存图表到明确路径
            log.info("准备保存图表到: " + new File(testChartPath).getAbsolutePath());
            boolean success = saveChartToFile(chartData, testChartPath);
            
            // 验证保存结果
            log.info("图表保存状态: " + success);
            if (success) {
                File savedFile = new File(testChartPath);
                log.info("图表已成功保存至: " + savedFile.getAbsolutePath());
                log.info("文件大小: " + savedFile.length() + " 字节");
                log.info("文件可读: " + savedFile.canRead());
            }
            
            log.info("\n==== 图表生成测试完成 ====");
        } catch (Exception e) {
            System.err.println("\n图表生成过程中出现错误: " + e.getMessage());
            System.err.println("错误类型: " + e.getClass().getName());
            System.err.println("\n详细错误信息:");
            e.printStackTrace();
            
            // 检查JFreeChart是否正确加载
            try {
                Class<?> jFreeChartClass = Class.forName("org.jfree.chart.JFreeChart");
                log.info("JFreeChart类加载成功: " + jFreeChartClass.getSimpleName());
            } catch (ClassNotFoundException ex) {
                System.err.println("JFreeChart类未找到，可能是依赖问题");
            }
            
            // 检查文件系统权限
            File testFile = new File("permission_test.txt");
            try {
                boolean canWrite = testFile.createNewFile();
                log.info("是否有权限写入文件: " + canWrite);
                if (canWrite) {
                    testFile.delete();
                }
            } catch (IOException ioEx) {
                System.err.println("文件系统权限检查失败: " + ioEx.getMessage());
            }
        }
    }
    
    /**
     * 保存图表数据到文件的辅助方法
     * @param chartData 图表的字节数据
     * @param filePath 文件路径
     * @return 是否保存成功
     */
    private static boolean saveChartToFile(byte[] chartData, String filePath) {
        if (chartData == null || chartData.length == 0) {
            System.err.println("图表数据为空，无法保存");
            return false;
        }
        
        try {
            File file = new File(filePath);
            // 确保父目录存在
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(chartData);
                fos.flush();
            }
            
            // 验证文件是否创建且大小合理
            return file.exists() && file.length() > 0;
        } catch (Exception e) {
            System.err.println("保存文件失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 生成柱状图
     * @param title 图表标题
     * @param categoryAxisLabel X轴标签
     * @param valueAxisLabel Y轴标签
     * @param data 数据集，格式为Map<String, Double>，key为分类名称，value为数值
     * @param width 图表宽度
     * @param height 图表高度
     * @return 图表的字节数组
     */
    public byte[] generateBarChart(String title, String categoryAxisLabel, String valueAxisLabel, 
                                  Map<String, Double> data, int width, int height) {
        return generateBarChart(title, categoryAxisLabel, valueAxisLabel, data, width, height, true);
    }
    
    /**
     * 生成柱状图（带标签控制）
     * @param title 图表标题
     * @param categoryAxisLabel X轴标签
     * @param valueAxisLabel Y轴标签
     * @param data 数据集
     * @param width 图表宽度
     * @param height 图表高度
     * @param showLabels 是否显示数值标签
     * @return 图表的字节数组
     */
    public byte[] generateBarChart(String title, String categoryAxisLabel, String valueAxisLabel, 
                                  Map<String, Double> data, int width, int height, boolean showLabels) {
        try {
            // 创建数据集
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                dataset.setValue(entry.getValue(), "数据", entry.getKey());
            }

            // 创建柱状图
            JFreeChart chart = ChartFactory.createBarChart(
                    title,
                    categoryAxisLabel,
                    valueAxisLabel,
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,  // 是否显示图例
                    true,  // 是否显示工具提示
                    false  // 是否生成URL链接
            );

            // 美化图表
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setRangeGridlinePaint(Color.GRAY);
            plot.setRangeGridlineStroke(new BasicStroke(0.5f));
            
            // 自定义图表样式 - 调用新的配置类方法
            ChartStyleConfig.customizeBarChart(chart, showLabels);
            
            // 将图表转换为字节数组
            return chartToByteArray(chart, width, height);
        } catch (Exception e) {
            log.error("生成柱状图失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 生成折线图
     * @param title 图表标题
     * @param categoryAxisLabel X轴标签
     * @param valueAxisLabel Y轴标签
     * @param data 数据集，格式为Map<String, Double>，key为分类名称，value为数值
     * @param width 图表宽度
     * @param height 图表高度
     * @return 图表的字节数组
     */
    public byte[] generateLineChart(String title, String categoryAxisLabel, String valueAxisLabel, 
                                  Map<String, Double> data, int width, int height) {
        return generateLineChart(title, categoryAxisLabel, valueAxisLabel, data, width, height, true);
    }
    
    /**
     * 生成折线图（带标签控制）
     * @param title 图表标题
     * @param categoryAxisLabel X轴标签
     * @param valueAxisLabel Y轴标签
     * @param data 数据集
     * @param width 图表宽度
     * @param height 图表高度
     * @param showLabels 是否显示数值标签
     * @return 图表的字节数组
     */
    public byte[] generateLineChart(String title, String categoryAxisLabel, String valueAxisLabel, 
                                  Map<String, Double> data, int width, int height, boolean showLabels) {
        try {
            // 创建数据集
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                dataset.setValue(entry.getValue(), "数据", entry.getKey());
            }

            // 创建折线图
            JFreeChart chart = ChartFactory.createLineChart(
                    title,
                    categoryAxisLabel,
                    valueAxisLabel,
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            // 美化图表
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setRangeGridlinePaint(Color.GRAY);
            plot.setRangeGridlineStroke(new BasicStroke(0.5f));
            
            // 自定义图表样式 - 调用新的配置类方法
            ChartStyleConfig.customizeLineChart(chart, showLabels);

            // 将图表转换为字节数组
            return chartToByteArray(chart, width, height);
        } catch (Exception e) {
            log.error("生成折线图失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 生成饼图
     * @param title 图表标题
     * @param data 数据集，格式为Map<String, Double>，key为分类名称，value为数值
     * @param width 图表宽度
     * @param height 图表高度
     * @return 图表的字节数组
     */
    public byte[] generatePieChart(String title, Map<String, Double> data, int width, int height) {
        return generatePieChart(title, data, width, height, true);
    }
    
    /**
     * 生成饼图（带标签控制）
     * @param title 图表标题
     * @param data 数据集
     * @param width 图表宽度
     * @param height 图表高度
     * @param showLabels 是否显示百分比标签
     * @return 图表的字节数组
     */
    public byte[] generatePieChart(String title, Map<String, Double> data, int width, int height, boolean showLabels) {
        try {
            // 创建数据集
            DefaultPieDataset dataset = new DefaultPieDataset();
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }

            // 创建饼图
            JFreeChart chart = ChartFactory.createPieChart(
                    title,
                    dataset,
                    true,
                    true,
                    false
            );
            
            // 自定义图表样式 - 调用新的配置类方法
            ChartStyleConfig.customizePieChart(chart, showLabels);

            // 将图表转换为字节数组
            return chartToByteArray(chart, width, height);
        } catch (Exception e) {
            log.error("生成饼图失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将图表转换为字节数组
     */
    private byte[] chartToByteArray(JFreeChart chart, int width, int height) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ChartUtils.writeChartAsPNG(baos, chart, width, height);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("将图表转换为字节数组时出错", e);
            return null;
        }
    }

    /**
     * 生成默认柱状图
     * @param title 图表标题
     * @param data 数据集，键为类别名称，值为数值
     * @return 图表的字节数组
     */
    public byte[] generateDefaultBarChart(String title, Map<String, Double> data) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            // 填充数据集
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                dataset.addValue(entry.getValue(), "数值", entry.getKey());
            }
            
            log.info("创建柱状图，标题: " + title + "，数据项: " + data.size());
            
            // 创建图表 - 使用简洁的配置
            JFreeChart chart = ChartFactory.createBarChart(
                    title,         // 标题
                    "类别",         // 类别轴标签
                    "数值",         // 数值轴标签
                    dataset,        // 数据集
                    PlotOrientation.VERTICAL, // 图表方向
                    true,           // 是否显示图例
                    true,           // 是否显示工具提示
                    false           // 是否显示URL链接
            );
            
            // 设置图表背景为白色
            chart.setBackgroundPaint(Color.WHITE);
            
            // 应用自定义样式配置
            ChartStyleConfig.customizeBarChart(chart, true);
            
            // 将图表转换为字节数组
            byte[] chartData = chartToByteArray(chart, 800, 600);
            log.info("柱状图数据生成成功，大小: " + chartData.length + " 字节");
            return chartData;
        } catch (Exception e) {
            System.err.println("生成柱状图失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("生成柱状图失败", e);
        }
    }
    
    /**
     * 生成默认折线图
     * @param title 图表标题
     * @param data 数据集，键为类别名称，值为数值
     * @return 图表的字节数组
     */
    public byte[] generateDefaultLineChart(String title, Map<String, Double> data) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            // 填充数据集
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                dataset.addValue(entry.getValue(), "数值", entry.getKey());
            }
            
            log.info("创建折线图，标题: " + title + "，数据项: " + data.size());
            
            // 创建图表 - 使用简洁的配置
            JFreeChart chart = ChartFactory.createLineChart(
                    title,         // 标题
                    "类别",         // 类别轴标签
                    "数值",         // 数值轴标签
                    dataset,        // 数据集
                    PlotOrientation.VERTICAL, // 图表方向
                    true,           // 是否显示图例
                    true,           // 是否显示工具提示
                    false           // 是否显示URL链接
            );
            
            // 设置图表背景为白色
            chart.setBackgroundPaint(Color.WHITE);
            
            // 应用自定义样式配置
            ChartStyleConfig.customizeLineChart(chart, true);
            
            // 将图表转换为字节数组
            byte[] chartData = chartToByteArray(chart, 800, 600);
            log.info("折线图数据生成成功，大小: " + chartData.length + " 字节");
            return chartData;
        } catch (Exception e) {
            System.err.println("生成折线图失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("生成折线图失败", e);
        }
    }
    
    /**
     * 生成默认饼图
     * @param title 图表标题
     * @param data 数据集，键为类别名称，值为数值
     * @return 图表的字节数组
     */
    public byte[] generateDefaultPieChart(String title, Map<String, Double> data) {
        try {
            DefaultPieDataset dataset = new DefaultPieDataset();
            
            // 填充数据集
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }
            
            log.info("创建饼图，标题: " + title + "，数据项: " + data.size());
            
            // 创建图表 - 使用简洁的配置
            JFreeChart chart = ChartFactory.createPieChart(
                    title,         // 标题
                    dataset,       // 数据集
                    true,          // 是否显示图例
                    true,          // 是否显示工具提示
                    false          // 是否显示URL链接
            );
            
            // 设置图表背景为白色
            chart.setBackgroundPaint(Color.WHITE);
            
            // 设置饼图的标签格式
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setLabelGenerator(null); // 简化，不显示标签以避免字体问题
            
            // 应用自定义样式配置
            ChartStyleConfig.customizePieChart(chart, true);
            
            // 将图表转换为字节数组
            byte[] chartData = chartToByteArray(chart, 800, 600);
            log.info("饼图数据生成成功，大小: " + chartData.length + " 字节");
            return chartData;
        } catch (Exception e) {
            System.err.println("生成饼图失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("生成饼图失败", e);
        }
    }
    
    private static File ensureDirectoryExists(String dirPath) {
        // 简化版本，直接返回目录对象
        File directory = new File(dirPath);
        log.info("[DEBUG] 目录路径: " + directory.getAbsolutePath());
        
        // 如果目录不存在，尝试创建
        if (!directory.exists()) {
            log.info("[DEBUG] 目录不存在，尝试创建");
            boolean created = directory.mkdirs();
            log.info("[DEBUG] 目录创建结果: " + created);
        }
        
        return directory.exists() && directory.isDirectory() ? directory : null;
    }
    
}