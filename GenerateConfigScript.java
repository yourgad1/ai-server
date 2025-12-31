import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 生成数据库配置脚本的工具类
 * 用于查询数据库中的配置数据，并生成更新后的插入脚本
 */
public class GenerateConfigScript {
    // 数据库连接信息
    private static final String URL = "jdbc:mysql://8.136.11.45:3306/ai_server";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "1qaz@1QAZ";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            System.out.println("-- 智能体动态配置SQL脚本（根据实际数据库数据生成）");
            System.out.println("-- 生成时间：" + new Date());
            System.out.println();
            System.out.println("-- 1. 清空现有数据（可选，根据实际情况决定是否执行）");
            System.out.println("-- TRUNCATE TABLE agent_tool_rel;");
            System.out.println("-- TRUNCATE TABLE agent_prompt_var;");
            System.out.println("-- TRUNCATE TABLE chat_memory_config;");
            System.out.println("-- TRUNCATE TABLE agent_config;");
            System.out.println();

            // 查询所有agent_config记录
            List<Map<String, Object>> agentConfigs = queryAgentConfigs(conn);

            // 2. 生成agent_config插入脚本
            System.out.println("-- 2. 插入Agent配置记录");
            for (Map<String, Object> agentConfig : agentConfigs) {
                generateAgentConfigInsert(agentConfig);
            }
            System.out.println();

            // 3. 生成agent_prompt_var插入脚本
            System.out.println("-- 3. 插入提示词变量记录");
            for (Map<String, Object> agentConfig : agentConfigs) {
                String agentId = (String) agentConfig.get("agent_id");
                String agentName = (String) agentConfig.get("agent_name");
                List<Map<String, Object>> promptVars = queryPromptVars(conn, agentId);
                for (Map<String, Object> var : promptVars) {
                    generatePromptVarInsert(agentName, var);
                }
            }
            System.out.println();

            // 4. 生成agent_tool_rel插入脚本
            System.out.println("-- 4. 插入工具关联记录");
            for (Map<String, Object> agentConfig : agentConfigs) {
                String agentId = (String) agentConfig.get("agent_id");
                String agentName = (String) agentConfig.get("agent_name");
                List<Map<String, Object>> tools = queryAgentTools(conn, agentId);
                for (Map<String, Object> tool : tools) {
                    generateToolRelInsert(agentName, tool);
                }
            }
            System.out.println();

            // 5. 生成chat_memory_config插入脚本
            System.out.println("-- 5. 插入聊天记忆配置记录");
            for (Map<String, Object> agentConfig : agentConfigs) {
                String agentId = (String) agentConfig.get("agent_id");
                String agentName = (String) agentConfig.get("agent_name");
                List<Map<String, Object>> memoryConfigs = queryChatMemoryConfigs(conn, agentId);
                for (Map<String, Object> memoryConfig : memoryConfigs) {
                    generateChatMemoryConfigInsert(agentName, memoryConfig);
                }
            }
            System.out.println();

            // 6. 生成查看结果的SQL
            System.out.println("-- 6. 查看插入结果");
            System.out.println("SELECT ");
            System.out.println("    ac.agent_id, ");
            System.out.println("    ac.agent_name, ");
            System.out.println("    ac.system_prompt, ");
            System.out.println("    ac.agent_type, ");
            System.out.println("    ac.has_tools, ");
            System.out.println("    ac.enabled, ");
            System.out.println("    ac.description, ");
            System.out.println("    COUNT(atr.tool_name) as tool_count, ");
            System.out.println("    COUNT(apv.var_key) as var_count, ");
            System.out.println("    cmc.memory_type, ");
            System.out.println("    cmc.max_messages");
            System.out.println("FROM `agent_config` ac");
            System.out.println("LEFT JOIN `agent_tool_rel` atr ON ac.agent_id = atr.agent_id");
            System.out.println("LEFT JOIN `agent_prompt_var` apv ON ac.agent_id = apv.agent_id");
            System.out.println("LEFT JOIN `chat_memory_config` cmc ON ac.agent_id = cmc.agent_id");
            System.out.println("GROUP BY ac.agent_id;");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询所有agent_config记录
     */
    private static List<Map<String, Object>> queryAgentConfigs(Connection conn) throws SQLException {
        String sql = "SELECT * FROM agent_config";
        return executeQuery(conn, sql);
    }

    /**
     * 查询指定agent_id的提示词变量
     */
    private static List<Map<String, Object>> queryPromptVars(Connection conn, String agentId) throws SQLException {
        String sql = "SELECT * FROM agent_prompt_var WHERE agent_id = ?";
        return executeQuery(conn, sql, agentId);
    }

    /**
     * 查询指定agent_id的工具关联
     */
    private static List<Map<String, Object>> queryAgentTools(Connection conn, String agentId) throws SQLException {
        String sql = "SELECT * FROM agent_tool_rel WHERE agent_id = ? ORDER BY tool_order";
        return executeQuery(conn, sql, agentId);
    }

    /**
     * 查询指定agent_id的聊天记忆配置
     */
    private static List<Map<String, Object>> queryChatMemoryConfigs(Connection conn, String agentId) throws SQLException {
        String sql = "SELECT * FROM chat_memory_config WHERE agent_id = ?";
        return executeQuery(conn, sql, agentId);
    }

    /**
     * 执行查询并返回结果列表
     */
    private static List<Map<String, Object>> executeQuery(Connection conn, String sql, Object... params) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    result.add(row);
                }
            }
        }
        return result;
    }

    /**
     * 生成agent_config插入语句
     */
    private static void generateAgentConfigInsert(Map<String, Object> agentConfig) {
        String agentName = (String) agentConfig.get("agent_name");
        String systemPrompt = (String) agentConfig.get("system_prompt");
        String agentType = (String) agentConfig.get("agent_type");
        Boolean hasTools = (Boolean) agentConfig.get("has_tools");
        Boolean enabled = (Boolean) agentConfig.get("enabled");
        String description = (String) agentConfig.get("description");

        // 转义单引号
        systemPrompt = escapeSingleQuotes(systemPrompt);
        description = escapeSingleQuotes(description);

        System.out.printf("-- %s%n", agentName);
        System.out.println("INSERT INTO `agent_config` (`agent_name`, `system_prompt`, `agent_type`, `has_tools`, `enabled`, `description`) ");
        System.out.printf("VALUES ('%s', '%s', '%s', %d, %d, '%s');%n", 
                agentName, systemPrompt, agentType, hasTools ? 1 : 0, enabled ? 1 : 0, description);
        System.out.println();
    }

    /**
     * 生成agent_prompt_var插入语句
     */
    private static void generatePromptVarInsert(String agentName, Map<String, Object> var) {
        String varKey = (String) var.get("var_key");
        String varValue = var.get("var_value") != null ? (String) var.get("var_value") : "";
        String varType = (String) var.get("var_type");
        String varSource = (String) var.get("var_source");
        String description = var.get("description") != null ? (String) var.get("description") : "";

        // 转义单引号
        varValue = escapeSingleQuotes(varValue);
        description = escapeSingleQuotes(description);

        System.out.printf("-- %s - %s%n", agentName, varKey);
        System.out.println("INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`) ");
        System.out.printf("SELECT agent_id, '%s', '%s', '%s', '%s', '%s' FROM `agent_config` WHERE `agent_name` = '%s';%n", 
                varKey, varValue, varType, varSource, description, agentName);
        System.out.println();
    }

    /**
     * 生成agent_tool_rel插入语句
     */
    private static void generateToolRelInsert(String agentName, Map<String, Object> tool) {
        String toolName = (String) tool.get("tool_name");
        Integer toolOrder = (Integer) tool.get("tool_order");

        System.out.printf("-- %s - %s%n", agentName, toolName);
        System.out.println("INSERT INTO `agent_tool_rel` (`agent_id`, `tool_name`, `tool_order`) ");
        System.out.printf("SELECT agent_id, '%s', %d FROM `agent_config` WHERE `agent_name` = '%s';%n", 
                toolName, toolOrder, agentName);
        System.out.println();
    }

    /**
     * 生成chat_memory_config插入语句
     */
    private static void generateChatMemoryConfigInsert(String agentName, Map<String, Object> memoryConfig) {
        String memoryType = (String) memoryConfig.get("memory_type");
        Integer maxMessages = (Integer) memoryConfig.get("max_messages");
        Long messageExpire = (Long) memoryConfig.get("message_expire");
        Boolean enabled = (Boolean) memoryConfig.get("enabled");

        System.out.printf("-- %s - 聊天记忆配置%n", agentName);
        System.out.println("INSERT INTO `chat_memory_config` (`agent_id`, `memory_type`, `max_messages`, `message_expire`, `enabled`) ");
        System.out.printf("SELECT agent_id, '%s', %d, %d, %d FROM `agent_config` WHERE `agent_name` = '%s';%n", 
                memoryType, maxMessages, messageExpire, enabled ? 1 : 0, agentName);
        System.out.println();
    }

    /**
     * 转义单引号
     */
    private static String escapeSingleQuotes(String str) {
        return str != null ? str.replace("'", "''") : "";
    }
}
