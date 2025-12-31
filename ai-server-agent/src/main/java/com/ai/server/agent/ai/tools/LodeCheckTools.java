package com.ai.server.agent.ai.tools;

import com.ai.server.agent.ai.common.data.DataSqlExec;
import com.ai.server.agent.ai.feign.dto.R;
import com.alibaba.nacos.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LodeCheckTools {

    @Autowired
    private DataSqlExec dataSqlExec;
    private final String dictBaseSql = "select * from ai_transcode_dict where 1 = 1";
    @Tool(description = "查询组织机构编码字典")
    public String getOrgDict(
            @ToolParam(description = "模糊查询编码值(如:需要查询新疆省的编码，则输入'%新疆%',无需包含省市县))")
                          String likeValue){
        String sql = dictBaseSql;
        sql += " and dict_type = '管理单位'";
        //如果likeValue包含省市县，则去掉
        if (likeValue.contains("省") || likeValue.contains("市") || likeValue.contains("县")){
            likeValue = likeValue.replace("省","");
            likeValue = likeValue.replace("市","");
            likeValue = likeValue.replace("县","");
        }
        if (StringUtils.isNotBlank(likeValue)){
            sql += " and dict_value like '"+likeValue+"'";
        }
        log.info("查询org字典sql:{}",sql);
        return getRes(sql);
    }

    @Tool(description = "查询下级单位组织机构编码字典")
    public String getChildOrgDict(
            @ToolParam(description = "父组织机构/管理单位编码")
            String parentCode){
        String sql = dictBaseSql;
        sql += " and dict_type = '管理单位'";
        if (StringUtils.isNotBlank(parentCode)){
            sql += " and parent_code = "+parentCode;
        }
        log.info("查询child_org字典sql:{}",sql);
        return getRes(sql);
    }


    @Tool(description = "查询电压等级(kv)编码字典全量")
    public String getVoltageDict(
            @ToolParam(description = "模糊查询编码值(如:需要查询电压等级为220kv的编码，则输入'%220%")
            String likeValue){
        String sql = dictBaseSql;
        sql += " and dict_type = '电压等级kv'";
        log.info("查询Voltage字典sql:{}",sql);
        return getRes(sql);
    }

    @Tool(description = "查询行业编码字典")
    public String getIndustryDict(
            @ToolParam(description = "模糊查询编码值(如:需要查询行业为陶瓷制造业的编码，则输入'%陶瓷%'))")
            String likeValue){
        String sql = dictBaseSql;
        sql += " and dict_type = '行业编码'";
        if (StringUtils.isNotBlank(likeValue)){
            sql += " and dict_value like '"+likeValue+"'";
        }
        log.info("查询Voltage字典sql:{}",sql);
        return getRes(sql);
    }

    @Tool(description = "查询子行业编码字典")
    public String getChildIndustryDict(
            @ToolParam(description = "父行业编码")
            String parentCode){
        String sql = dictBaseSql;
        sql += " and dict_type = '管理单位'";
        if (StringUtils.isNotBlank(parentCode)){
            sql += " and parent_code = "+parentCode;
        }
        log.info("查询child_org字典sql:{}",sql);
        return getRes(sql);
    }

    /**
     * 获取转码值
     * @param dictType
     * @param dictCode
     * @return
     */
    public String getDictCode(String dictType,String dictCode){
        String sql = "select dict_value from ai_transcode_dict where 1 = 1";
        sql += " and dict_type = '"+dictType+"'";
        sql += " and dict_code = '"+dictCode+"'";
        log.info("查询转码字典sql:{}",sql);
        R<List<Map<String, Object>>> select = dataSqlExec.runSql(sql);
        List<Map<String, Object>> data = select.getData();
        if (data.size() > 0){
            return (String) data.get(0).get("dict_value");
        }
        return "";
    }


    private String getRes(String sql){
        R<List<Map<String, Object>>> select = dataSqlExec.runSql(sql);
        ArrayList<String> list = new ArrayList<>();
        for (Map<String, Object> datum : select.getData()) {
            list.add(String.format("%s=%s",datum.get("dict_value"),datum.get("dict_code")));
        }
        String join = StringUtils.join(list, ",");
        return join;
    }
}
