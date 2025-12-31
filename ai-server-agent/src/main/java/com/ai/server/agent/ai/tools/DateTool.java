package com.ai.server.agent.ai.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DateTool {
    @Tool(description = "获取当前时间")
    public String getDate(@ToolParam(description = "日期格式") String format){
        //获取当前北京时间(UTC+8)，并格式化输出xxxx年xx月xx日
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Shanghai"));
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
        return now.format(formatter);
    }





}
