package com.ai.server.agent.ai.common.sql.impl;

import com.ai.server.agent.ai.feign.DataAccessMysqlClient;
import com.ai.server.agent.ai.feign.dto.R;
import com.ai.server.agent.ai.common.sql.SqlExecutionStrategy;
import com.ai.server.agent.core.jdbc.RemoteSqlDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Feign SQL执行策略实现
 * 使用Feign客户端远程执行SQL语句和脚本
 */
@Component
@Slf4j
public class FeignSqlExecutionStrategy implements SqlExecutionStrategy {

    @Autowired
    private DataAccessMysqlClient dataAccessMysqlClient;

    @Value("${data-access.app-id:default}")
    private String appId;

    @Value("${data-access.data-source:default}")
    private String dataSource;

    @Override
    public int execute(String sql) {
        log.debug("使用Feign执行SQL: {}", sql);
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .dataSource(dataSource)
                .build();
        R<Integer> result = dataAccessMysqlClient.update(remoteSqlDTO);
        if (result != null && result.isSuccess() && result.getData() != null) {
            return result.getData();
        } else {
            log.error("Feign执行SQL失败: {}", result != null ? result.getMessage() : "未知错误");
            return 0;
        }
    }

    @Override
    public boolean executeScript(String sqlScriptPath) {
        log.info("使用Feign执行SQL脚本: {}", sqlScriptPath);
        
        boolean isTriggerScript = sqlScriptPath.contains("triggers");
        
        try {
            // 读取SQL脚本文件
            ClassPathResource resource = new ClassPathResource(sqlScriptPath);
            if (!resource.exists()) {
                log.error("SQL脚本文件不存在: {}", sqlScriptPath);
                return false;
            }

            // 读取并执行脚本内容
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                String script = FileCopyUtils.copyToString(reader);
                String[] statements = script.split(";");
                boolean hasFailed = false;
                
                for (String sql : statements) {
                    sql = sql.trim();
                    if (sql.isEmpty() || sql.startsWith("--") || sql.startsWith("/*")) {
                        continue;
                    }
                    
                    log.debug("执行SQL语句: {}", sql);
                    RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                            .appId(appId)
                            .sql(sql)
                            .dataSource(dataSource)
                            .build();
                    
                    R<Integer> result = dataAccessMysqlClient.update(remoteSqlDTO);
                    if (result == null || !result.isSuccess()) {
                        String errorMsg = result != null ? result.getMessage() : "未知错误";
                        if (isTriggerScript) {
                            log.warn("Feign执行触发器SQL语句失败: {}", errorMsg);
                            hasFailed = true;
                        } else {
                            log.error("Feign执行SQL语句失败: {}", errorMsg);
                            return false;
                        }
                    }
                }
                
                if (hasFailed) {
                    log.warn("触发器脚本执行完成，但部分语句失败: {}", sqlScriptPath);
                    return false;
                }
                
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
        log.debug("使用Feign执行带参数SQL: {}, 参数: {}", sql, args);
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .params(List.of(args))
                .dataSource(dataSource)
                .build();
        R<Integer> result = dataAccessMysqlClient.update(remoteSqlDTO);
        if (result != null && result.isSuccess() && result.getData() != null) {
            return result.getData();
        } else {
            log.error("Feign执行SQL失败: {}", result != null ? result.getMessage() : "未知错误");
            return 0;
        }
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql) {
        log.debug("使用Feign执行查询: {}", sql);
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .dataSource(dataSource)
                .build();
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result != null && result.isSuccess() && result.getData() != null) {
            return result.getData();
        } else {
            log.error("Feign执行查询失败: {}", result != null ? result.getMessage() : "未知错误");
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql, Object... args) {
        log.debug("使用Feign执行带参数查询: {}, 参数: {}", sql, args);
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .params(List.of(args))
                .dataSource(dataSource)
                .build();
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result != null && result.isSuccess() && result.getData() != null) {
            return result.getData();
        } else {
            log.error("Feign执行查询失败: {}", result != null ? result.getMessage() : "未知错误");
            return new ArrayList<>();
        }
    }

    @Override
    public <T> List<T> queryForList(String sql, Class<T> elementType) {
        log.debug("使用Feign执行指定类型查询: {}, 类型: {}", sql, elementType);
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .dataSource(dataSource)
                .build();
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result != null && result.isSuccess() && result.getData() != null) {
            List<T> list = new ArrayList<>();
            for (Map<String, Object> map : result.getData()) {
                // 简单类型转换，仅支持String类型
                if (String.class.equals(elementType)) {
                    for (Object value : map.values()) {
                        list.add((T) String.valueOf(value));
                        break; // 只取第一个字段
                    }
                }
            }
            return list;
        } else {
            log.error("Feign执行指定类型查询失败: {}", result != null ? result.getMessage() : "未知错误");
            return new ArrayList<>();
        }
    }

    @Override
    public <T> List<T> queryForList(String sql, Class<T> elementType, Object... args) {
        log.debug("使用Feign执行带参数指定类型查询: {}, 类型: {}, 参数: {}", sql, elementType, args);
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .params(List.of(args))
                .dataSource(dataSource)
                .build();
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result != null && result.isSuccess() && result.getData() != null) {
            List<T> list = new ArrayList<>();
            for (Map<String, Object> map : result.getData()) {
                // 简单类型转换，仅支持String类型
                if (String.class.equals(elementType)) {
                    for (Object value : map.values()) {
                        list.add((T) String.valueOf(value));
                        break; // 只取第一个字段
                    }
                }
            }
            return list;
        } else {
            log.error("Feign执行带参数指定类型查询失败: {}", result != null ? result.getMessage() : "未知错误");
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> queryForMap(String sql) {
        log.debug("使用Feign执行单条映射查询: {}", sql);
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .dataSource(dataSource)
                .build();
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result != null && result.isSuccess() && result.getData() != null && !result.getData().isEmpty()) {
            return result.getData().get(0);
        } else {
            log.error("Feign执行单条映射查询失败: {}", result != null ? result.getMessage() : "未知错误");
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Object... args) {
        log.debug("使用Feign执行带参数单条映射查询: {}, 参数: {}", sql, args);
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .params(List.of(args))
                .dataSource(dataSource)
                .build();
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result != null && result.isSuccess() && result.getData() != null && !result.getData().isEmpty()) {
            return result.getData().get(0);
        } else {
            log.error("Feign执行带参数单条映射查询失败: {}", result != null ? result.getMessage() : "未知错误");
            return new HashMap<>();
        }
    }

    @Override
    public <T> T queryForObject(String sql, Class<T> requiredType) {
        log.debug("使用Feign执行单值查询: {}, 类型: {}", sql, requiredType);
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .dataSource(dataSource)
                .build();
        
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result == null || !result.isSuccess() || result.getData() == null || result.getData().isEmpty()) {
            log.error("Feign执行单值查询失败: {}", result != null ? result.getMessage() : "未知错误");
            return null;
        }
        
        Map<String, Object> map = result.getData().get(0);
        if (map.isEmpty()) {
            return null;
        }
        
        // 获取第一个值
        Object value = map.values().iterator().next();
        if (value == null) {
            return null;
        }
        
        return convertValue(value, requiredType);
    }
    
    /**
     * 将值转换为指定类型
     * @param value 要转换的值
     * @param requiredType 目标类型
     * @param <T> 泛型类型
     * @return 转换后的值
     */
    @SuppressWarnings("unchecked")
    private <T> T convertValue(Object value, Class<T> requiredType) {
        // 如果类型已经匹配，直接返回
        if (requiredType.isInstance(value)) {
            return (T) value;
        }
        
        // 处理包装类型和原始类型
        Class<T> unwrappedType = (Class<T>) (requiredType.isPrimitive() ? 
                org.springframework.util.ClassUtils.resolvePrimitiveIfNecessary(requiredType) : requiredType);
        
        try {
            // 字符串类型转换
            if (String.class.equals(unwrappedType)) {
                return (T) String.valueOf(value);
            }
            
            // 数字类型转换
            if (Number.class.isAssignableFrom(unwrappedType)) {
                Number number = (value instanceof Number) ? (Number) value : 
                        Double.valueOf(String.valueOf(value));
                
                if (Integer.class.equals(unwrappedType) || int.class.equals(requiredType)) {
                    return (T) Integer.valueOf(number.intValue());
                } else if (Long.class.equals(unwrappedType) || long.class.equals(requiredType)) {
                    return (T) Long.valueOf(number.longValue());
                } else if (Double.class.equals(unwrappedType) || double.class.equals(requiredType)) {
                    return (T) Double.valueOf(number.doubleValue());
                } else if (Float.class.equals(unwrappedType) || float.class.equals(requiredType)) {
                    return (T) Float.valueOf(number.floatValue());
                } else if (Short.class.equals(unwrappedType) || short.class.equals(requiredType)) {
                    return (T) Short.valueOf(number.shortValue());
                } else if (Byte.class.equals(unwrappedType) || byte.class.equals(requiredType)) {
                    return (T) Byte.valueOf(number.byteValue());
                }
            }
            
            // 布尔类型转换
            if (Boolean.class.equals(unwrappedType) || boolean.class.equals(requiredType)) {
                if (value instanceof Boolean) {
                    return (T) value;
                } else {
                    String strValue = String.valueOf(value).toLowerCase();
                    return (T) Boolean.valueOf("true".equals(strValue) || "1".equals(strValue));
                }
            }
        } catch (Exception e) {
            log.error("类型转换失败: 值={}, 目标类型={}, 错误={}", value, requiredType, e.getMessage());
        }
        
        // 如果无法转换，返回null
        return null;
    }

    @Override
    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
        log.debug("使用Feign执行带参数单值查询: {}, 类型: {}, 参数: {}", sql, requiredType, args);
        RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                .appId(appId)
                .sql(sql)
                .params(List.of(args))
                .dataSource(dataSource)
                .build();
        
        R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
        if (result == null || !result.isSuccess() || result.getData() == null || result.getData().isEmpty()) {
            log.error("Feign执行带参数单值查询失败: {}", result != null ? result.getMessage() : "未知错误");
            return null;
        }
        
        Map<String, Object> map = result.getData().get(0);
        if (map.isEmpty()) {
            return null;
        }
        
        // 获取第一个值
        Object value = map.values().iterator().next();
        if (value == null) {
            return null;
        }
        
        return convertValue(value, requiredType);
    }

    @Override
    public boolean tableExists(String tableName) {
        log.debug("使用Feign检查表是否存在: {}", tableName);
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
            RemoteSqlDTO remoteSqlDTO = RemoteSqlDTO.builder()
                    .appId(appId)
                    .sql(sql)
                    .params(List.of(tableName))
                    .dataSource(dataSource)
                    .build();
            R<List<Map<String, Object>>> result = dataAccessMysqlClient.queryForList(remoteSqlDTO);
            if (result != null && result.isSuccess() && result.getData() != null && !result.getData().isEmpty()) {
                Map<String, Object> map = result.getData().get(0);
                for (Object value : map.values()) {
                    if (value != null) {
                        Integer count = Integer.valueOf(String.valueOf(value));
                        return count > 0;
                    }
                }
            } else {
                log.error("Feign检查表是否存在失败: {}", result != null ? result.getMessage() : "未知错误");
            }
        } catch (Exception e) {
            log.error("Feign检查表是否存在异常: {}", tableName, e);
        }
        return false;
    }
}
