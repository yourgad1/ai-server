package com.ai.server.agent.ai.feign.mysql;

import com.ai.server.agent.ai.feign.dto.R;
import com.ai.server.agent.ai.feign.DataAccessMysqlClient;
import com.ai.server.agent.core.jdbc.RemoteSqlDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Service
public class DataAccessMysqlDataClient implements DataAccessMysqlClient {
    @Override
    public R<List<Map<String, Object>>> queryForList(RemoteSqlDTO remoteSqlDTO) {
        R r = new R();
        Map p = new HashMap();
        p.put("用户户号","6500000068202");
        p.put("企业名称","中粮糖业控股股份有限公司奇台糖业分公司");
        p.put("运行容量",8000);
        ArrayList<Object> list = new ArrayList<>();
        list.add(p);
        list.add(p);
        r.setData(list);
        r.setCode(200);
        r.setSuccess(true);
        r.setMessage("success");
        return r;
    }
    
    @Override
    public R<Integer> update(RemoteSqlDTO remoteSqlDTO) {
        R r = new R();
        r.setData(1); // 模拟成功更新1条记录
        r.setCode(200);
        r.setSuccess(true);
        r.setMessage("success");
        return r;
    }
}
