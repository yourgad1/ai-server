package com.ai.server.agent.ai.util;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatResponseToEntity {


    public static  <T> T JsonToentity(Class<T> type,String json) {
        String json1 = getJson(json);
        log.info("解析后的json:{}",json1);
        T t = JSON.parseObject(json1, type);
        return  t;

    }

    /**
     * 判断格式
     * 1. json字符串
     * 2. json模板 ：```json {}```
     *
     * 如果是模板，则去掉模板，返回json字符串
     * @param json
     * @return
     */
    public static String getJson(String json) {
        if(json.contains("```json")) {
            //找到位置
            int start = json.indexOf("```json");
            int end = json.lastIndexOf("```");
            if(start != -1 && end != -1) {
                return json.substring(start+8, end);
            }
        }
        return json;
    }

    /**
     * markdown 解析
     */
    public static String getMarkdown(String json) {
        if(json.contains("```markdown")) {
            //找到位置
            int start = json.indexOf("```markdown");
            int end = json.lastIndexOf("```");
            if(start != -1 && end != -1) {
                if (end<start+12){
                    //没有结束符号
                    return json.substring(start+12)+"...问题过多，超过展示限制！";
                }else {
                    return json.substring(start+12, end);
                }
            }
        }
        return json;
    }

    /**
     * 去除（...）
     * @param args
     */
    public static String remove(String args) {
        if(args.contains("(")) {
            //找到位置
            int start = args.indexOf("(");
            return  args.substring(0,start);
        }
        return args;
    }

    public static void main(String[] args) {
        String s ="专变/专线用户类型(0专变/1专线)";
        System.out.println(remove(s));
    }


}
