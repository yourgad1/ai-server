import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class GetJsonResult {
    public static void main(String[] args) {
        List<Map<String, Object>> metricResult = getMetricResult("[select * from 重要用户数 where 日期=2022 and 组织机构='安徽省']", "[预测负荷]");
        JSONObject resultJson = new JSONObject();
        resultJson.put("answer", metricResult);
        resultJson.put("event", "agent_message");

        System.out.println(resultJson.toString());
    }
    public static List<Map<String, Object>> getMetricResult(String metricSql, String metricName) {
        List<Map<String, Object>> results = Lists.newArrayList();
        Map<String, Object> row = Maps.newHashMap();
        if("预测负荷".equals(metricName)){
            row.put("组织机构", "安徽");
            row.put("日期", "2025-07-18");
            row.put("预测负荷", "70000000");
            results.add(row);
        }else{
            row.put("组织机构", "安徽");
            row.put("日期", "2025-07-18");
            row.put("重要用户数", "999");
            results.add(row);
        }
        return results;
    }
}
