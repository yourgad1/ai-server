package com.ai.server.agent.ai.util;

import cn.hutool.core.util.ObjectUtil;
import com.ai.server.agent.ai.config.TranscodingConfig;
import com.ai.server.agent.ai.tools.LodeCheckTools;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
@Component
public class TranscodeUtil {

    @Autowired
    private ObjectProvider<LodeCheckTools> lodeCheckToolsProvider;

    public static List<Map<String,Object>> getTranscoding(List<TranscodingConfig.Transcoding> collection, List<Map<String,Object>> list){
        for (TranscodingConfig.Transcoding transcode : collection) {
            for (Map p : list) {
                if (ObjectUtil.isNotEmpty(p.get(transcode.getHeader()))){
                    String key = (String)p.get(transcode.getHeader());
                    try {
                        String className = "com.sgcc.c2000.metric.ai.enums." + transcode.getTarget();
                        Class<?> enumClass = Class.forName(className);
                        Method getDesc = enumClass.getMethod("getDesc",String.class);
                        String desc = (String)getDesc.invoke(null,key.trim());
                        if (StringUtil.isNotBlank(desc)){
                            p.put(transcode.getHeader(), desc);
                        }
                    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                             IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return list;
    }


    public List<Map<String, Object>> getDiceValue(List<Map<String, Object>> res) {
        for (Map<String, Object> re : res) {
            re.forEach(
                    (key, value) -> {
                        if (key.contains("专变")) {
                            if (value.toString().equals("0")) {
                                value = "专变";
                            } else {
                                value = "专线";
                            }
                            re.put(key, value);
                        }
                        if (key.contains("供电单位")) {
                            String dictCode = lodeCheckToolsProvider.getIfAvailable().getDictCode("管理单位", (String) value);
                            if (StringUtil.isNotBlank(dictCode)) {
                                value = dictCode;
                            }
                            re.put(key, value);
                        }
                        if (key.contains("电压等级")) {
                            String dictCode = lodeCheckToolsProvider.getIfAvailable().getDictCode("电压等级kv", (String) value);
                            if (StringUtil.isNotBlank(dictCode)) {
                                value = dictCode;
                            }
                            re.put(key, value);
                        }
                        if (key.contains("行业")) {
                            String dictCode = lodeCheckToolsProvider.getIfAvailable().getDictCode("行业编码", (String) value);
                            if (StringUtil.isNotBlank(dictCode)) {
                                value = dictCode;
                            }
                            re.put(key, value);
                        }
                    }
            );
        }
        return res;
    }
}
