package com.ai.server.agent.ai.common.sse;

public class ConnectionIdContext {
    // 线程本地变量存储连接ID
    private static final ThreadLocal<String> CONNECTION_ID_HOLDER = new ThreadLocal<>();
    
    /**
     * 设置当前线程的连接ID
     */
    public static void setConnectionId(String connectionId) {
        CONNECTION_ID_HOLDER.set(connectionId);
    }
    
    /**
     * 获取当前线程的连接ID
     */
    public static String getConnectionId() {
        return CONNECTION_ID_HOLDER.get();
    }
    
    /**
     * 清除当前线程的连接ID，避免内存泄漏
     */
    public static void clear() {
        CONNECTION_ID_HOLDER.remove();
    }
}