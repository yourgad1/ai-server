/*
package com.sgcc.c2000.metric.ai.strategy.impl;

import cn.hutool.core.util.ObjectUtil;
import com.sgcc.c2000.metric.ai.agent.Agent;
import com.sgcc.c2000.metric.ai.agent.impl.SystemAgent;
import com.sgcc.c2000.metric.ai.constant.AgentTypeConstant;
import com.sgcc.c2000.metric.ai.constant.IntentConstant;
import com.sgcc.c2000.metric.ai.rest.request.RequestAi;
import com.sgcc.c2000.metric.ai.rest.response.ResponseAi;
import com.sgcc.c2000.metric.ai.rest.service.toolService.YML.YmlService;
import com.sgcc.c2000.metric.ai.strategy.IntentBasedStrategy;
import com.sgcc.c2000.metric.ai.common.sse.ConnectionIdContext;
import com.sgcc.c2000.metric.ai.common.sse.AiGlobalSseEmitterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

*/
/**
 * 文件处理策略实现
 * 处理文件处理意图的请求
 *//*

@Component
@Slf4j
public class FileProcessStrategy implements IntentBasedStrategy {
    
    @Autowired
    private SystemAgent systemAgent;

    @Autowired
    private YmlService ymlService;
    @Autowired
    private AiGlobalSseEmitterManager sseEmitterManager ;
    
    @Override
    public void handleRequest( RequestAi requestAi, MultipartFile file, String intent)  {
        try {
            sseEmitterManager.sendEvent(ConnectionIdContext.getConnectionId(),"检测到文件处理意图，开始处理文件\n");
            
            if (file == null || file.isEmpty()) {
                sseEmitterManager.sendEvent(ConnectionIdContext.getConnectionId(),ResponseAi.ofMessage("但未检测到文件,请上传文件"));
                return;
            }
            try {
                // 调用SystemAgent处理
                processFileWithAgent(requestAi, file);
            } catch (Exception e) {
                log.error("文件处理失败", e);
                sseEmitterManager.sendEvent(ConnectionIdContext.getConnectionId(),ResponseAi.ofMessage("文件处理失败：" + e.getMessage()));
            }
            
        } catch (Exception e) {
            log.error("文件处理失败", e);
            sseEmitterManager.sendEvent(ConnectionIdContext.getConnectionId(),ResponseAi.ofMessage("文件处理失败：" + e.getMessage()));
        }
    }
    
    @Override
    public boolean supports(String intent, RequestAi requestAi, MultipartFile file) {
        log.info("文件处理策略判断模式：{}", intent);
        return (intent != null && intent.contains(IntentConstant.FILE_PROCESS) && ObjectUtil.isNotNull(file));
    }

    */
/**
     * 使用SystemAgent处理文件
     *//*

    private void processFileWithAgent(RequestAi requestAi, MultipartFile tempFile) throws Exception {
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("instruction", requestAi.getMessage());
            params.put("fileName", tempFile.getName());
            
            // 调用SystemAgent处理
            Agent.ChatRequest chatRequest = Agent.ChatRequest.builder()
                    .message(requestAi.getMessage())
                    .context(params)
                    .build();
            
            // 使用文件意图智能体判断文件意图
            String result = systemAgent.chat(chatRequest, AgentTypeConstant.SYS_FILE);
            
            if (result.contains("YML_TABLE")){
                ymlService.processTablesToMetric(tempFile);
            }else if (result.contains("DOCX_TEMP")){

            }else if (result.contains("FILE_OTHER")){

            }

            
        } catch (Exception e) {
            log.error("文件处理失败", e);
            throw e;
        }
    }
}*/
