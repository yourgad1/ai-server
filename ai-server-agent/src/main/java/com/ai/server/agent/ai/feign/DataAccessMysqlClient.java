package com.ai.server.agent.ai.feign;
import com.ai.server.agent.ai.feign.dto.R;
import com.ai.server.agent.core.jdbc.RemoteSqlDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.List;
import java.util.Map;

@FeignClient(value = "data-access-server-mysql", path = "/fg/ms01-00-204/mysql/v1")
public interface DataAccessMysqlClient {

    @PostMapping(value = "/queryForList")
    R<List<Map<String, Object>>> queryForList(@RequestBody RemoteSqlDTO remoteSqlDTO);
    
    @PostMapping(value = "/update")
    R<Integer> update(@RequestBody RemoteSqlDTO remoteSqlDTO);
}
