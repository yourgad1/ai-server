package com.ai.server.agent.ai.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThinkContentUtil {

    /**
     * 去除内容中包含</think> 之前的内容
     */
    public static String removeBeforeThink(String content){
        if (content.contains("</think>")){
            int start = content.indexOf("</think>");
           return content.substring(start+9);
        }
        return content;
    }

    public static void main(String[] args) {
        String content = "好的，我需要分析用户的问题：“查询2025年9月30号杭州的停电用户数”。首先，用户明确提到“查询”，这通常与数据检索相关。接下来，时间点是2025年9月30日，地点是杭州，而具体的数据是“停电用户数”。根据给定的角色说明，如果问题涉及系统数据查询或指标数据查询，意图类型应归类为metric_query。\n" +
                "\n" +
                "进一步分析，“停电用户数”显然是一个具体的指标，属于电力系统的数据查询范畴。用户并没有提到任何文件操作，比如生成报告或修改文档，所以排除file_processing类别。此外，问题不涉及其他类型的询问，如帮助操作指导或一般性信息，因此也不属于other类别。\n" +
                "\n" +
                "确认所有条件后，可以确定用户的请求完全符合metric_query的标准，即获取特定时间地点的指标数据。因此，正确的意图类型应该是metric_query。\n" +
                "</think>\n" +
                "\n" +
                "metric_query";
        System.out.println(removeBeforeThink(content));
    }
}
