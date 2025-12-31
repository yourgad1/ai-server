package com.ai.server.agent.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChartDataTools {

    @Tool(description = "获取当前时间")
    public String getDate(){
        //获取当前北京时间(UTC+8)，并格式化输出xxxx年xx月xx日
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Shanghai"));
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        return now.format(formatter);
    }
    @Tool(description = "各产业售电量情况数据")
    public Map<String, Double> getChartData(@ToolParam(description = "查询日期") String date) {
        HashMap<String, Double> hashMap = new HashMap<>();
        hashMap.put("工业", 1000.0);
        hashMap.put("商业", 500.0);
        hashMap.put("服务", 300.0);
        hashMap.put("其他", 200.0);
        return hashMap;
    }
}
