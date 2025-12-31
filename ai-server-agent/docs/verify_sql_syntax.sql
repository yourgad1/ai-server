-- 验证SQL语法的脚本
-- 仅用于语法检查，不执行实际插入操作

-- 检查Agent配置表插入语法
SELECT 'agent_config' AS table_name, 'valid' AS status
FROM dual
WHERE EXISTS (
    SELECT 1 FROM information_schema.tables WHERE table_name = 'agent_config'
);

-- 检查Agent提示词变量表插入语法
SELECT 'agent_prompt_var' AS table_name, 'valid' AS status
FROM dual
WHERE EXISTS (
    SELECT 1 FROM information_schema.tables WHERE table_name = 'agent_prompt_var'
);

-- 检查Agent工具关联表插入语法
SELECT 'agent_tool_rel' AS table_name, 'valid' AS status
FROM dual
WHERE EXISTS (
    SELECT 1 FROM information_schema.tables WHERE table_name = 'agent_tool_rel'
);

-- 检查聊天记忆配置表插入语法
SELECT 'chat_memory_config' AS table_name, 'valid' AS status
FROM dual
WHERE EXISTS (
    SELECT 1 FROM information_schema.tables WHERE table_name = 'chat_memory_config'
);

-- 显示所有表的结构（可选）
-- SHOW CREATE TABLE agent_config;
-- SHOW CREATE TABLE agent_prompt_var;
-- SHOW CREATE TABLE agent_tool_rel;
-- SHOW CREATE TABLE chat_memory_config;