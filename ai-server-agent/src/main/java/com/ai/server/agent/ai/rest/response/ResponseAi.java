package com.ai.server.agent.ai.rest.response;

import cn.hutool.json.JSONUtil;
import com.ai.server.agent.ai.rest.entity.DataResult;
import com.ai.server.agent.ai.constant.ResponseEventConstant;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ResponseAi {

    private String answer;
    private String event;//agent_message、agent_table、agent_log、agent_data、agent_error
    //存储定制场景智能体返回的系统实体对象
    private Object data;

    public static ResponseAi ofUserMessage(String msg) {
        return new ResponseAi(msg, ResponseEventConstant.USER_MESSAGE, null);
    }

    public static ResponseAi ofMessage(String msg) {
        return new ResponseAi(msg, ResponseEventConstant.AGENT_MESSAGE, null);
    }

    public static ResponseAi ofTable(String connId, List data) {
        return new ResponseAi(connId, ResponseEventConstant.AGENT_TABLE, data);
    }

    public static ResponseAi ofTableLimit(String connId, List data) {
        DataResult dataResult = new DataResult();
        if (data.size() > 10){
            dataResult.setData(data.subList(0, 10));
        }else {
            dataResult.setData(data);
        }
        dataResult.setTotal(data.size());
        dataResult.setPageNum(1);
        dataResult.setPageSize(10);
        return new ResponseAi(connId, ResponseEventConstant.AGENT_TABLE_LIMIT,dataResult);
    }

    public static ResponseAi ofTableLimit(String connId, DataResult data) {
        return new ResponseAi(connId, ResponseEventConstant.AGENT_TABLE_LIMIT, data);
    }

    public static ResponseAi ofData(Object data) {
        return new ResponseAi(null, ResponseEventConstant.AGENT_DATA, data);
    }

    public static ResponseAi ofLog(String msg) {
        return new ResponseAi(msg, ResponseEventConstant.AGENT_LOG, null);
    }

    public static ResponseAi ofError(String msg) {
        return new ResponseAi(msg, ResponseEventConstant.AGENT_ERROR, null);
    }

    @Override
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }
}
