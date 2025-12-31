package com.ai.server.agent.ai.common.sql;

import java.util.List;
import java.util.Map;

/**
 * SQL执行策略接口
 * 定义SQL执行的方法，支持不同的数据访问方式
 */
public interface SqlExecutionStrategy {
    
    /**
     * 执行DDL或DML语句
     * @param sql SQL语句
     * @return 影响的行数
     */
    int execute(String sql);
    
    /**
     * 执行带参数的DDL或DML语句
     * @param sql SQL语句
     * @param args 参数列表
     * @return 影响的行数
     */
    int execute(String sql, Object... args);
    
    /**
     * 执行SQL脚本文件
     * @param sqlScriptPath SQL脚本文件路径（相对于classpath）
     * @return 执行结果
     */
    boolean executeScript(String sqlScriptPath);
    
    /**
     * 查询返回映射列表
     * @param sql SQL语句
     * @return 映射列表
     */
    List<Map<String, Object>> queryForList(String sql);
    
    /**
     * 执行带参数的查询，返回映射列表
     * @param sql SQL语句
     * @param args 参数列表
     * @return 映射列表
     */
    List<Map<String, Object>> queryForList(String sql, Object... args);
    
    /**
     * 执行查询，返回指定类型的列表
     * @param sql SQL语句
     * @param elementType 元素类型
     * @param <T> 泛型类型
     * @return 指定类型的列表
     */
    <T> List<T> queryForList(String sql, Class<T> elementType);
    
    /**
     * 执行带参数的查询，返回指定类型的列表
     * @param sql SQL语句
     * @param elementType 元素类型
     * @param args 参数列表
     * @param <T> 泛型类型
     * @return 指定类型的列表
     */
    <T> List<T> queryForList(String sql, Class<T> elementType, Object... args);
    
    /**
     * 执行查询，返回单个映射
     * @param sql SQL语句
     * @return 映射
     */
    Map<String, Object> queryForMap(String sql);
    
    /**
     * 执行带参数的查询，返回单个映射
     * @param sql SQL语句
     * @param args 参数列表
     * @return 映射
     */
    Map<String, Object> queryForMap(String sql, Object... args);
    
    /**
     * 执行查询，返回单个值
     * @param sql SQL语句
     * @param requiredType 返回值类型
     * @param <T> 泛型类型
     * @return 单个值
     */
    <T> T queryForObject(String sql, Class<T> requiredType);
    
    /**
     * 执行带参数的查询，返回单个值
     * @param sql SQL语句
     * @param requiredType 返回值类型
     * @param args 参数列表
     * @param <T> 泛型类型
     * @return 单个值
     */
    <T> T queryForObject(String sql, Class<T> requiredType, Object... args);
    
    /**
     * 检查指定表是否存在
     * @param tableName 表名
     * @return true=存在，false=不存在
     */
    boolean tableExists(String tableName);
}
