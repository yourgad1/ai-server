package com.ai.server.agent.ai.common.sql;

import com.ai.server.agent.ai.common.sql.impl.DataSourceSqlExecutionStrategy;
import com.ai.server.agent.ai.common.sql.impl.FeignSqlExecutionStrategy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * SQL执行策略工厂
 * 根据配置选择合适的SQL执行策略
 */
@Component
public class SqlExecutionStrategyFactory {

    @Autowired
    private DataSourceSqlExecutionStrategy dataSourceSqlExecutionStrategy;

    @Autowired
    private ObjectProvider<FeignSqlExecutionStrategy> feignSqlExecutionStrategyProvider;

    @Value("${agent.config.type:feign}")
    private String configType;

    /**
     * 获取SQL执行策略
     * @param strategyType 策略类型：direct=数据源直接执行，feign=Feign远程执行
     * @return SQL执行策略
     */
    public SqlExecutionStrategy getSqlExecutionStrategy(String... strategyType) {
        String type = strategyType.length > 0 ? strategyType[0] : configType;
        if ("direct".equals(type)) {
            return dataSourceSqlExecutionStrategy;
        } else {
            return feignSqlExecutionStrategyProvider.getIfAvailable();
        }
    }
}
