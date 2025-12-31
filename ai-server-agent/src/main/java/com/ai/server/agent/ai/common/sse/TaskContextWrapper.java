package com.ai.server.agent.ai.common.sse;

import java.util.concurrent.Callable;

public class TaskContextWrapper {
    /**
     * 包装Runnable任务，保留当前线程的连接ID上下文
     */
    public static Runnable wrapWithConnectionId(Runnable task) {
        String currentConnectionId = ConnectionIdContext.getConnectionId();
        return () -> {
            try {
                // 在线程执行前恢复连接ID
                ConnectionIdContext.setConnectionId(currentConnectionId);
                task.run();
            } finally {
                // 清理上下文
                ConnectionIdContext.clear();
            }
        };
    }
    
    /**
     * 包装Callable任务，保留当前线程的连接ID上下文
     */
    public static <T> Callable<T> wrapWithConnectionId(Callable<T> task) {
        String currentConnectionId = ConnectionIdContext.getConnectionId();
        return () -> {
            try {
                // 在线程执行前恢复连接ID
                ConnectionIdContext.setConnectionId(currentConnectionId);
                return task.call();
            } finally {
                // 清理上下文
                ConnectionIdContext.clear();
            }
        };
    }
}
