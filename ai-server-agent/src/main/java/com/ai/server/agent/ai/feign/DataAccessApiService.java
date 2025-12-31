package com.ai.server.agent.ai.feign;

import com.ai.server.agent.ai.feign.dto.ApiMetaDto;
import com.ai.server.agent.ai.feign.dto.ApiReportQPersonalize;
import com.ai.server.agent.ai.feign.dto.QPersonalizeQueryDto;
import com.ai.server.agent.ai.feign.dto.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(value = "data-access-api", path = "/data-access")
public interface DataAccessApiService {

    @PostMapping(value = "/report/list")
    R<List<ApiMetaDto>> getReportList();

    @PostMapping(value = "/report/getPersonalizeList")
    R<List<ApiReportQPersonalize>> getPersonalizeList(QPersonalizeQueryDto queryDto);

    /**
     * 预测接口
     * @param json
     * @return
     */
    @PostMapping(value = "/api/v1/{apiCode}")
    R<ApiReportQPersonalize> getApiData(@PathVariable("apiCode") String code, String json);
}
