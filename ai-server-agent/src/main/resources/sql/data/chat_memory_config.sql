-- 为所有智能体添加默认聊天记忆配置
INSERT INTO `chat_memory_config` (`agent_id`, `memory_type`, `max_messages`, `message_expire`, `enabled`)
SELECT
    agent_id,
    'messageWindow',
    10,
    -1,
    1
FROM `agent_config`;