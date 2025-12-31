package com.ai.server.agent.ai.common.sql.impl;

import com.ai.server.agent.ai.common.sql.SqlExecutionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * 数据源SQL执行策略实现
 * 使用JdbcTemplate直接执行SQL语句和脚本
 */
@Component
@Slf4j
public class DataSourceSqlExecutionStrategy implements SqlExecutionStrategy {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int execute(String sql) {
        log.debug("使用DataSource执行SQL: {}", sql);
        return jdbcTemplate.update(sql);
    }

    @Override
    public boolean executeScript(String sqlScriptPath) {
        log.info("使用DataSource执行SQL脚本: {}", sqlScriptPath);
        boolean isTriggerScript = sqlScriptPath.contains("triggers");
        
        try {
            // 读取SQL脚本文件，指定UTF-8编码避免中文乱码
            ClassPathResource resource = new ClassPathResource(sqlScriptPath);
            if (!resource.exists()) {
                log.error("SQL脚本文件不存在: {}", sqlScriptPath);
                return false;
            }

            // 获取数据库连接并执行脚本
            try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
                // 使用EncodedResource指定UTF-8编码，确保中文内容正确执行
                org.springframework.core.io.support.EncodedResource encodedResource = 
                    new org.springframework.core.io.support.EncodedResource(resource, "UTF-8");
                
                // 使用Spring的ScriptUtils执行脚本，支持正确处理分号和触发器
                ScriptUtils.executeSqlScript(connection, encodedResource);
                
                log.info("SQL脚本执行成功: {}", sqlScriptPath);
                return true;
            }
        } catch (Exception e) {
            // 对于触发器脚本，记录警告而不是错误
            if (isTriggerScript) {
                log.warn("执行触发器脚本异常: {}", sqlScriptPath, e);
            } else {
                log.error("执行SQL脚本失败: {}", sqlScriptPath, e);
            }
            return false;
        }
    }

    @Override
    public int execute(String sql, Object... args) {
        log.debug("使用DataSource执行带参数SQL: {}, 参数: {}", sql, args);
        return jdbcTemplate.update(sql, args);
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql) {
        log.debug("使用DataSource执行查询: {}", sql);
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql, Object... args) {
        log.debug("使用DataSource执行带参数查询: {}, 参数: {}", sql, args);
        return jdbcTemplate.queryForList(sql, args);
    }

    @Override
    public <T> List<T> queryForList(String sql, Class<T> elementType) {
        log.debug("使用DataSource执行指定类型查询: {}, 类型: {}", sql, elementType);
        return jdbcTemplate.queryForList(sql, elementType);
    }

    @Override
    public <T> List<T> queryForList(String sql, Class<T> elementType, Object... args) {
        log.debug("使用DataSource执行带参数指定类型查询: {}, 类型: {}, 参数: {}", sql, elementType, args);
        return jdbcTemplate.queryForList(sql, elementType, args);
    }

    @Override
    public Map<String, Object> queryForMap(String sql) {
        log.debug("使用DataSource执行单条映射查询: {}", sql);
        return jdbcTemplate.queryForMap(sql);
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Object... args) {
        log.debug("使用DataSource执行带参数单条映射查询: {}, 参数: {}", sql, args);
        return jdbcTemplate.queryForMap(sql, args);
    }

    @Override
    public <T> T queryForObject(String sql, Class<T> requiredType) {
        log.debug("使用DataSource执行单值查询: {}, 类型: {}", sql, requiredType);
        return jdbcTemplate.queryForObject(sql, requiredType);
    }

    @Override
    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
        log.debug("使用DataSource执行带参数单值查询: {}, 类型: {}, 参数: {}", sql, requiredType, args);
        return jdbcTemplate.queryForObject(sql, requiredType, args);
    }

    @Override
    public boolean tableExists(String tableName) {
        log.debug("使用DataSource检查表是否存在: {}", tableName);
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查DataSource表是否存在失败: {}", tableName, e);
            return false;
        }
    }
}
