package com.ai.server.agent.ai.common.data;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.ai.server.agent.ai.rest.entity.DataResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * 存储用户推荐的历史数据，用于分页和下载
 */
@Component
public class DataStorageManager {


    //记录连接id数据对应的时间
    private static long DEFAULT_EXPIRE_TIME = 5 * 60 * 1000; // 5分钟

    //记录sql对应的数据时间
    private static long DEFAULT_SQL_EXPIRE_TIME = 6 * 60 * 1000; // 6分钟

    /**
     * 设置默认过期时间（仅用于测试）
     */
    public static void setDefaultExpireTime(long expireTime) {
        DEFAULT_EXPIRE_TIME = expireTime;
    }

    /**
     * 重置默认过期时间为5分钟
     */
    public static void resetDefaultExpireTime() {
        DEFAULT_EXPIRE_TIME = 5 * 60 * 1000;
    }

    /**
     * 设置SQL映射默认过期时间（仅用于测试）
     */
    public static void setDefaultSqlExpireTime(long expireTime) {
        DEFAULT_SQL_EXPIRE_TIME = expireTime;
    }

    /**
     * 重置SQL映射默认过期时间为6分钟
     */
    public static void resetDefaultSqlExpireTime() {
        DEFAULT_SQL_EXPIRE_TIME = 6 * 60 * 1000;
    }

    private final TimedCache<String, List<Map<String, Object>>> dataCache;
    private final TimedCache<String, Object> sqlDataMapCache;

    public DataStorageManager() {
        // 创建定时缓存，默认过期时间5分钟
        dataCache = CacheUtil.newTimedCache(DEFAULT_EXPIRE_TIME);
        // 每秒钟检查一次过期数据
        dataCache.schedulePrune(1000);

        // 创建SQL数据映射缓存，默认过期时间6分钟
        sqlDataMapCache = CacheUtil.newTimedCache(DEFAULT_SQL_EXPIRE_TIME);
        // 每秒钟检查一次过期数据
        sqlDataMapCache.schedulePrune(1000);
    }

    // 存入数据方法
    public void storeData(String connId, List<Map<String, Object>> data) {
        if (connId == null || data == null) {
            throw new IllegalArgumentException("connId and data cannot be null");
        }
        dataCache.put(connId, data);
    }

    /**
     * 存入数据方法（指定过期时间）
     */
    public void storeData(String connId, List<Map<String, Object>> data, long expireTime) {
        dataCache.put(connId, data, expireTime);
    }

    // 读取数据方法（支持分页）
    public DataResult readData(String connId, int page, int pageSize) {
        List<Map<String, Object>> data = dataCache.get(connId);
        DataResult dataResult = new DataResult();
        dataResult.setPageNum(page);
        dataResult.setPageSize(pageSize);
        if (data == null) {
            dataResult.setTotal(0);
            return dataResult;
        }

        int total = data.size();

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        if (fromIndex >= total) {
            dataResult.setTotal(total);
            return dataResult;
        }

        List<Map<String, Object>> pageData = data.subList(fromIndex, toIndex);
        dataResult.setTotal(total);
        dataResult.setData(pageData);
        return dataResult;
    }

    // 将数据转化为Excel的方法
    public byte[] convertToExcel(String connId) throws IOException {
        List<Map<String, Object>> data = dataCache.get(connId);

        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("No valid data found for connection ID: " + connId);
        }

        // 创建Excel工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        Set<String> headers = data.get(0).keySet();
        int colIndex = 0;
        for (String header : headers) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(header);
            // 设置表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        int rowIndex = 1;
        for (Map<String, Object> rowData : data) {
            Row row = sheet.createRow(rowIndex++);
            colIndex = 0;
            for (String header : headers) {
                Cell cell = row.createCell(colIndex++);
                Object value = rowData.get(header);
                if (value != null) {
                    cell.setCellValue(value.toString());
                }
            }
        }

        // 自动调整列宽
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // 输出到字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * 手动清理过期数据
     */
    public void cleanExpiredData() {
        dataCache.prune();
        sqlDataMapCache.prune();
    }

    // SQL数据映射相关方法

    /**
     * 存储SQL和数据映射
     * @param sql SQL语句作为key
     * @param data 映射的数据对象
     */
    public void storeSqlDataMap(String sql, Object data) {
        if (sql == null || data == null) {
            throw new IllegalArgumentException("sql and data cannot be null");
        }
        sqlDataMapCache.put(sql, data);
    }
    /**
     * 存储SQL和数据映射
     * @param sql SQL语句作为key
     * @param data 映射的数据对象
     */
    public void storeSqlDataMapTime(String sql, Object data) {
        if (sql == null || data == null) {
            throw new IllegalArgumentException("sql and data cannot be null");
        }
        sqlDataMapCache.put(sql, data,DEFAULT_SQL_EXPIRE_TIME);
    }

    /**
     * 存储SQL和数据映射（指定过期时间）
     * @param sql SQL语句作为key
     * @param data 映射的数据对象
     * @param expireTime 过期时间（毫秒）
     */
    public void storeSqlDataMap(String sql, Object data, long expireTime) {
        if (sql == null || data == null) {
            throw new IllegalArgumentException("sql and data cannot be null");
        }
        sqlDataMapCache.put(sql, data, expireTime);
    }

    /**
     * 获取SQL映射的数据
     * @param sql SQL语句作为key
     * @return 映射的数据对象，若不存在或已过期则返回null
     */
    public Object getSqlDataMap(String sql) {
        return sqlDataMapCache.get(sql);
    }

    /**
     * 移除指定的SQL数据映射
     * @param sql SQL语句作为key
     */
    public void removeSqlDataMap(String sql) {
        sqlDataMapCache.remove(sql);
    }

    /**
     * 清空所有SQL数据映射
     */
    public void clearSqlDataMap() {
        sqlDataMapCache.clear();
    }

    /**
     * 获取当前SQL数据映射缓存的大小
     * @return 缓存中的映射数量
     */
    public int getSqlDataMapSize() {
        return sqlDataMapCache.size();
    }
}