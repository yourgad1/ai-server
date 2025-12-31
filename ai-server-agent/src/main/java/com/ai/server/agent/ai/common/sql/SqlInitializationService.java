package com.ai.server.agent.ai.common.sql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;


/**
 * SQL初始化服务
 * 在项目启动时执行SQL脚本，支持根据配置选择数据源直接执行或Feign远程执行
 * 按表维度进行初始化，逐个表检查并初始化
 */
@Service
@Slf4j
public class SqlInitializationService {

    @Autowired
    private SqlExecutionStrategyFactory sqlExecutionStrategyFactory;

    @Value("${sql.init.enabled:true}")
    private boolean enabled;

    @Value("${sql.init.tables-dir:sql/tables}")
    private String tablesDir;

    private static final List<String> TABLES_TO_INIT = Arrays.asList(
            "agent_config",
            "agent_prompt_var",
            "agent_tool_rel",
            "chat_memory_config",
            "SPRING_AI_CHAT_MEMORY",
            "session_info",
            "sse_message"
    );

    // 添加一个标志，确保SQL初始化只执行一次
    private static volatile boolean initialized = false;

    /**
     * 初始化SQL表结构和数据
     */
    public void init() {
        if (!enabled) {
            log.info("SQL初始化已禁用");
            return;
        }

        // 双重检查锁定，确保SQL初始化只执行一次
        if (initialized) {
            log.info("SQL初始化已执行，跳过重复执行");
            return;
        }
        synchronized (SqlInitializationService.class) {
            if (initialized) {
                log.info("SQL初始化已执行，跳过重复执行");
                return;
            }

            log.info("开始执行SQL初始化");
            SqlExecutionStrategy strategy = sqlExecutionStrategyFactory.getSqlExecutionStrategy();

            try {
                // 遍历每个表，逐个检查并初始化
                for (String tableName : TABLES_TO_INIT) {
                    initTable(tableName, strategy);
                }

                // 执行触发器脚本（作为可选操作，失败不影响整体初始化）
                try {
                    initTriggers(strategy);
                } catch (Exception e) {
                    log.warn("触发器初始化异常，将跳过触发器初始化", e);
                }

                log.info("SQL初始化执行完成");
                initialized = true;
            } catch (Exception e) {
                log.error("SQL初始化执行异常", e);
            }
        }
    }
    
    /**
     * 初始化触发器
     * @param strategy SQL执行策略
     */
    private void initTriggers(SqlExecutionStrategy strategy) throws Exception {
        log.info("开始初始化触发器");
        
        try {
            // 直接执行触发器脚本，使用相对路径
            String triggerScriptPath = "sql/triggers/spring_ai_chat_memory_triggers.sql";
            log.info("执行触发器脚本: {}", triggerScriptPath);
            
            boolean scriptResult = strategy.executeScript(triggerScriptPath);
            if (scriptResult) {
                log.info("触发器脚本执行成功");
            } else {
                log.warn("触发器脚本执行失败");
            }
        } catch (Exception e) {
            log.warn("执行触发器脚本异常: {}", e.getMessage());
            // 不抛出异常，触发器初始化失败不影响整体启动
        }
        
        log.info("触发器初始化完成");
    }

    /**
     * 初始化单个表
     * @param tableName 表名
     * @param strategy SQL执行策略
     * @throws Exception 初始化异常
     */
    private void initTable(String tableName, SqlExecutionStrategy strategy) throws Exception {
        log.info("开始初始化表: {}", tableName);
        
        // 执行DDL脚本（创建表，如果不存在）
        // 脚本文件名使用小写，表名使用大写
        String scriptFileName = tableName.toLowerCase();
        String tableScriptPath = tablesDir + "/" + scriptFileName + ".sql";
        log.info("执行表 {} 的SQL脚本: {}", tableName, tableScriptPath);
        boolean scriptResult = strategy.executeScript(tableScriptPath);
        
        if (scriptResult) {
            log.info("表 {} 的SQL脚本执行成功", tableName);
        } else {
            log.error("表 {} 的SQL脚本执行失败", tableName);
            return;
        }
        
        // 验证表是否真的存在
        boolean tableExists = strategy.tableExists(tableName);
        if (!tableExists) {
            log.warn("表 {} 创建脚本执行成功，但实际表不存在，跳过后续操作", tableName);
            return;
        }
        
        // 插入初始数据
        insertTableData(tableName, strategy);
        
        log.info("表 {} 初始化完成", tableName);
    }



    /**
     * 插入表的初始数据
     * @param tableName 表名
     * @param strategy SQL执行策略
     */
    private void insertTableData(String tableName, SqlExecutionStrategy strategy) {
        try {
            // 检查表中是否已有数据
            String countSql = "SELECT COUNT(*) FROM " + tableName;
            Integer count = strategy.queryForObject(countSql, Integer.class);
            
            if (count != null && count > 0) {
                log.info("表 {} 已有数据，跳过初始数据插入", tableName);
                return;
            }
            
            // 检查数据脚本文件是否存在，脚本文件名使用小写
            String scriptFileName = tableName.toLowerCase();
            String dataScriptPath = "sql/data/" + scriptFileName + ".sql";
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource resource = resolver.getResource("classpath:" + dataScriptPath);
            
            if (!resource.exists()) {
                log.info("表 {} 初始数据脚本不存在，跳过数据插入", tableName);
                return;
            }
            
            // 插入初始数据
            log.info("表 {} 无数据，开始插入初始数据", tableName);
            
            // 从文件中执行插入语句
            boolean scriptResult = strategy.executeScript(dataScriptPath);
            if (scriptResult) {
                log.info("表 {} 初始数据插入成功", tableName);
            } else {
                log.warn("表 {} 初始数据插入脚本执行失败，跳过数据插入", tableName);
            }
        } catch (Exception e) {
            log.error("插入表 {} 初始数据失败", tableName, e);
        }
    }


}
