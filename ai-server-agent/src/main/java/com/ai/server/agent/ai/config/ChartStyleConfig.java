package com.ai.server.agent.ai.config;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.ui.VerticalAlignment;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * 图表样式配置类
 * 将所有图表相关的样式配置集中管理，便于统一维护和修改
 */

/**
 * 图表样式配置类
 * 将所有图表相关的样式配置集中管理，便于统一维护和修改
 */
public class ChartStyleConfig {

    // 字体大小配置
    public static final int TITLE_FONT_SIZE = 18;
    public static final int AXIS_FONT_SIZE = 12;
    public static final int LABEL_FONT_SIZE = 10;
    
    // 图表默认尺寸
    public static final int DEFAULT_WIDTH = 600;
    public static final int DEFAULT_HEIGHT = 400;
    
    // 现代配色方案
    public static final Color[] MODERN_COLORS = {
        new Color(44, 160, 101),  // 绿色 - 主要数据
        new Color(65, 168, 206),  // 蓝色 - 次要数据
        new Color(241, 197, 48),  // 黄色 - 第三数据
        new Color(225, 87, 89),   // 红色 - 警告数据
        new Color(147, 101, 184)  // 紫色 - 其他数据
    };
    
    // 饼图现代柔和配色方案
    public static final Color[] PIE_CHART_COLORS = {
        new Color(52, 152, 219, 230),  // 柔和蓝
        new Color(46, 204, 113, 230),  // 柔和绿
        new Color(155, 89, 182, 230),  // 柔和紫
        new Color(241, 196, 15, 230),  // 柔和黄
        new Color(231, 76, 60, 230),   // 柔和红
        new Color(243, 156, 18, 230)   // 柔和橙
    };
    
    // 网格线颜色
    public static final Color GRID_LINE_COLOR = new Color(220, 220, 220);
    
    // 坐标轴颜色
    public static final Color AXIS_LINE_COLOR = new Color(230, 230, 230);
    
    // 标题颜色
    public static final Color TITLE_COLOR = new Color(51, 51, 51);
    
    // 标签颜色
    public static final Color LABEL_COLOR = new Color(70, 70, 70);
    
    // 柱状图主色 - 使用更暗的纯色，适合企业报表
    public static final Color BAR_CHART_MAIN_COLOR = new Color(31, 73, 125);  // 深蓝色
    
    // 柱状图备用深色
    public static final Color BAR_CHART_ALT_COLOR = new Color(41, 128, 185); // 稍浅的蓝色
    
    // 折线图主色
    public static final Color LINE_CHART_MAIN_COLOR = new Color(142, 68, 173);
    
    // 阴影颜色 - 移除高光后可以设置为透明
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 0);
    
    // 获取支持中文的字体 - 提供多个备选方案确保中文显示
    private static Font getChineseFont(String fontFamily, int style, int size) {
        // 定义常用中文字体列表，按优先级排序
        String[] chineseFontFamilies = {
            "Microsoft YaHei UI", // 微软雅黑UI（Windows系统优先使用）
            "Microsoft YaHei",    // 微软雅黑英文名称
            "SimHei",            // 黑体
            "Microsoft Sans Serif", // Windows默认字体
            "SimSun",            // 宋体
            "KaiTi",             // 楷体
            "FangSong",          // 仿宋
            "Arial Unicode MS"    // 跨平台Unicode字体
        };
        
        // 尝试每个字体，返回第一个可用的字体
        for (String fontName : chineseFontFamilies) {
            try {
                // 直接创建字体而不进行复杂验证，确保字体设置正确应用
                Font font = new Font(fontName, style, size);
                // 即使返回Dialog字体也使用，确保系统使用默认中文字体
                return font;
            } catch (Exception e) {
                // 忽略错误，继续尝试下一个字体
            }
        }
        
        // 最终回退到系统默认字体
        return new Font(Font.DIALOG, style, size);
    }
    
    // 图例项字体 - 确保支持中文
    public static Font getLegendItemFont() {
        return getChineseFont("微软雅黑", Font.PLAIN, LABEL_FONT_SIZE);
    }
    
    // 标题字体 - 确保支持中文
    public static Font getTitleFont() {
        return getChineseFont("微软雅黑", Font.BOLD, TITLE_FONT_SIZE);
    }
    
    // 坐标轴标签字体 - 确保支持中文
    public static Font getAxisLabelFont() {
        // 使用更大的字体大小确保中文清晰显示
        return getChineseFont("Microsoft YaHei", Font.PLAIN, AXIS_FONT_SIZE + 2);
    }
    
    // 坐标轴刻度字体 - 确保支持中文
    public static Font getAxisTickFont() {
        // 确保坐标轴刻度（包括中文标签）正确显示
        return getChineseFont("Microsoft YaHei", Font.PLAIN, AXIS_FONT_SIZE + 1);
    }
    
    // 数据标签字体 - 确保支持中文
    public static Font getDataLabelFont() {
        return getChineseFont("微软雅黑", Font.PLAIN, LABEL_FONT_SIZE + 1);
    }
    
    /**
     * 自定义柱状图样式
     */
    public static void customizeBarChart(JFreeChart chart, boolean showLabels) {
        // 设置图表背景为白色，无边框
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        
        // 设置标题样式
        chart.getTitle().setFont(getTitleFont());
        chart.getTitle().setPaint(TITLE_COLOR);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.CENTER);
        
        // 获取绘图区域
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        
        // 设置网格线 - 企业风格更简洁
        plot.setRangeGridlinePaint(new Color(200, 200, 200)); // 浅灰色网格线
        plot.setRangeGridlineStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        plot.setDomainGridlinesVisible(false);
        
        // 设置分类轴 (X轴) - 企业风格
        CategoryAxis categoryAxis = plot.getDomainAxis();
        categoryAxis.setCategoryMargin(0.2);
        // 确保设置中文标签字体
        Font tickFont = getAxisTickFont();
        Font labelFont = getAxisLabelFont();
        categoryAxis.setTickLabelFont(tickFont);
        categoryAxis.setLabelFont(labelFont);
        categoryAxis.setAxisLinePaint(new Color(150, 150, 150)); // 更深的轴线
        categoryAxis.setTickMarkPaint(new Color(150, 150, 150));
        categoryAxis.setLowerMargin(0.05);
        categoryAxis.setUpperMargin(0.05);
        // 确保标签可见性设置正确
        categoryAxis.setTickLabelsVisible(true);
        
        // 设置数值轴 (Y轴) - 企业风格
        NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
        numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // 确保设置中文标签字体
        numberAxis.setTickLabelFont(getAxisTickFont());
        numberAxis.setLabelFont(getAxisLabelFont());
        numberAxis.setAxisLinePaint(new Color(150, 150, 150)); // 更深的轴线
        numberAxis.setTickMarkPaint(new Color(150, 150, 150));
        // 根据数据范围自动设置合适的刻度单位，更适合企业报表
        numberAxis.setAutoTickUnitSelection(true);
        numberAxis.setAutoRangeIncludesZero(true);
        // 设置更精确的自动范围计算
        numberAxis.setAutoRangeStickyZero(true);
        // 确保标签可见性设置正确
        numberAxis.setTickLabelsVisible(true);
        
        // 设置柱状图渲染器 - 企业报表风格，纯色无高光
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        // 使用不透明的深色，确保纯色显示
        renderer.setSeriesPaint(0, BAR_CHART_MAIN_COLOR); // 深蓝色，无高光
        // 确保不绘制柱子轮廓，避免产生高光效果
        renderer.setDrawBarOutline(false);
        // 移除阴影效果
        renderer.setShadowVisible(false);
        // 设置标准绘制器确保纯色填充，禁用任何内置的渐变效果
        renderer.setBarPainter(new StandardBarPainter());
        // 确保渲染器完全使用纯色填充
        // 已使用 StandardBarPainter 禁用渐变，无需额外设置
        // 调整柱子宽度
        renderer.setMaximumBarWidth(0.6);
        renderer.setMinimumBarLength(1);
        renderer.setItemMargin(0.15);
        // 确保所有渲染属性都设置为禁用高光和渐变
        
        // 显示数值标签
        if (showLabels) {
            renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getInstance()));
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelFont(getDataLabelFont());
            renderer.setDefaultItemLabelPaint(LABEL_COLOR);
            renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
        }
        
        // 设置图例样式
        configureLegend(chart);
    }
    
    /**
     * 自定义折线图样式
     */
    public static void customizeLineChart(JFreeChart chart, boolean showLabels) {
        // 设置图表背景为白色，无边框
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        
        // 设置标题样式
        chart.getTitle().setFont(getTitleFont());
        chart.getTitle().setPaint(TITLE_COLOR);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.CENTER);
        
        // 获取绘图区域
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        
        // 设置网格线
        plot.setRangeGridlinePaint(GRID_LINE_COLOR);
        plot.setRangeGridlineStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        plot.setDomainGridlinePaint(GRID_LINE_COLOR);
        plot.setDomainGridlineStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        
        // 设置分类轴 (X轴)
        CategoryAxis categoryAxis = plot.getDomainAxis();
        categoryAxis.setCategoryMargin(0.25);
        categoryAxis.setTickLabelFont(getAxisTickFont());
        categoryAxis.setLabelFont(getAxisLabelFont());
        categoryAxis.setAxisLinePaint(AXIS_LINE_COLOR);
        categoryAxis.setTickMarkPaint(AXIS_LINE_COLOR);
        categoryAxis.setLowerMargin(0.05);
        categoryAxis.setUpperMargin(0.05);
        
        // 设置数值轴 (Y轴)
        ValueAxis valueAxis = plot.getRangeAxis();
        valueAxis.setTickLabelFont(getAxisTickFont());
        valueAxis.setLabelFont(getAxisLabelFont());
        valueAxis.setAxisLinePaint(AXIS_LINE_COLOR);
        valueAxis.setTickMarkPaint(AXIS_LINE_COLOR);
        
        if (valueAxis instanceof NumberAxis) {
            NumberAxis numberAxis = (NumberAxis) valueAxis;
            numberAxis.setAutoRangeIncludesZero(true);
            numberAxis.setTickUnit(new NumberTickUnit(10));
        }
        
        // 设置折线图渲染器
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, LINE_CHART_MAIN_COLOR);
        renderer.setSeriesStroke(0, new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // 设置数据点样式 - 移除高光效果
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Float(-5f, -5f, 10f, 10f));
        renderer.setSeriesFillPaint(0, LINE_CHART_MAIN_COLOR);
        // 移除白色边框高光
        renderer.setUseOutlinePaint(false);
        
        // 显示数值标签
        if (showLabels) {
            renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getInstance()));
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelFont(getDataLabelFont());
            renderer.setDefaultItemLabelPaint(LABEL_COLOR);
            renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
            renderer.setDefaultNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER));
        }
        
        // 设置图例样式
        configureLegend(chart);
    }
    
    /**
     * 自定义饼图样式
     */
    public static void customizePieChart(JFreeChart chart, boolean showLabels) {
        // 设置图表背景为白色，无边框
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        
        // 设置标题样式
        chart.getTitle().setFont(getTitleFont());
        chart.getTitle().setPaint(TITLE_COLOR);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.CENTER);
        
        // 获取饼图绘图区域
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        
        // 设置饼图参数
        plot.setStartAngle(90);
        plot.setInteriorGap(0.05);
        
        // 应用配色方案
        applyPieChartColors(plot);
        
        // 显示百分比标签
        if (showLabels) {
            NumberFormat format = new DecimalFormat("0.0%");
            plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})", null, format));
            plot.setLabelFont(getDataLabelFont());
            plot.setLabelPaint(LABEL_COLOR);
            plot.setLabelGap(0.03);
            
            // 优化标签显示
            plot.setLabelBackgroundPaint(null);
            plot.setLabelOutlinePaint(null);
            plot.setLabelShadowPaint(null);
            plot.setLabelLinkPaint(GRID_LINE_COLOR);
            plot.setLabelLinkStroke(new BasicStroke(0.8f));
        }
        
        // 移除阴影效果（高光效果）
        plot.setShadowPaint(null);
        plot.setShadowXOffset(0.0);
        plot.setShadowYOffset(0.0);
        
        // 设置图例样式
        configureLegend(chart);
    }
    
    /**
     * 配置图例样式
     */
    private static void configureLegend(JFreeChart chart) {
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(getLegendItemFont());
            chart.getLegend().setBorder(0, 0, 0, 0);
            chart.getLegend().setHorizontalAlignment(HorizontalAlignment.CENTER);
            chart.getLegend().setVerticalAlignment(VerticalAlignment.BOTTOM);
        }
    }
    
    /**
     * 应用饼图颜色方案
     */
    private static void applyPieChartColors(PiePlot plot) {
        if (plot != null && plot.getDataset() != null && plot.getDataset().getItemCount() > 0) {
            int itemCount = plot.getDataset().getItemCount();
            for (int i = 0; i < itemCount; i++) {
                plot.setSectionPaint(i, PIE_CHART_COLORS[i % PIE_CHART_COLORS.length]);
            }
        }
    }
    
    /**
     * 获取默认宽度
     */
    public static int getDefaultWidth() {
        return DEFAULT_WIDTH;
    }
    
    /**
     * 获取默认高度
     */
    public static int getDefaultHeight() {
        return DEFAULT_HEIGHT;
    }
}
