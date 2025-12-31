package com.ai.server.agent.ai.common.data;

import com.ai.server.agent.ai.feign.DataAccessMysqlClient;
import com.ai.server.agent.ai.feign.dto.R;
import com.ai.server.agent.core.jdbc.RemoteSqlDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
@Component
public class DataSqlExec {

    @Autowired
    private DataAccessMysqlClient dataAccessMysqlClient;
    public R<List<Map<String, Object>>> runSql(String sql){
        RemoteSqlDTO remoteSqlDTO = new RemoteSqlDTO();
        remoteSqlDTO.setSql(sql);
        // 执行SQL查询
        R<List<Map<String, Object>>> t = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        return t;
    }
}
