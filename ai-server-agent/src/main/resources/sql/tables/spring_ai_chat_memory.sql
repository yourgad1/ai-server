-- Spring AI聊天记忆表DDL（兼容Spring AI框架和本工程业务）
-- 严格按照Spring AI标准设计，功能重复字段以Spring AI标准字段为主
CREATE TABLE IF NOT EXISTS `SPRING_AI_CHAT_MEMORY` (
  -- Spring AI标准字段（必须包含，功能重复字段以此为准）
  `id` VARCHAR(255) NOT NULL  COMMENT '消息唯一标识符，UUID，自动生成',
  `content` LONGTEXT NOT NULL COMMENT '聊天消息内容（Spring AI标准字段，替代原有message字段）',
  `type` VARCHAR(32) NOT NULL DEFAULT 'user' COMMENT '消息类型：user, assistant, system（Spring AI标准字段，替代原有message_type字段）',
  `conversation_id` VARCHAR(255) NOT NULL COMMENT '会话唯一标识符（Spring AI标准字段，替代原有session_id字段）',
  `timestamp` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '消息创建时间戳（Spring AI标准字段，替代原有created_at字段）',
  
  -- 本工程业务核心字段（非重复字段，保留）
  `user_id` VARCHAR(64) NOT NULL DEFAULT 'test' COMMENT '用户唯一标识符',
  `agent_name` VARCHAR(64) NULL COMMENT '智能体名称，与agent_config表关联',
  `chat_type` VARCHAR(32) NOT NULL DEFAULT 'system' COMMENT '聊天类型：system(系统级会话)、custom(自定义聊天)',
  `session_id` VARCHAR(64) NULL COMMENT '会话ID，用于区别不同会话的智能体记忆',
  
  -- 主键和索引
  PRIMARY KEY (`id`),
  -- Spring AI必要索引
  INDEX `idx_spring_ai_chat_memory_conversation` (`conversation_id`),
  INDEX `idx_spring_ai_chat_memory_timestamp` (`timestamp`),
  -- 业务索引
  INDEX `idx_spring_ai_chat_memory_user` (`user_id`),
  INDEX `idx_spring_ai_chat_memory_agent` (`agent_name`),
  INDEX `idx_spring_ai_chat_memory_session` (`session_id`),
  INDEX `idx_spring_ai_chat_memory_user_conversation` (`user_id`, `conversation_id`),
  INDEX `idx_spring_ai_chat_memory_agent_conversation` (`agent_name`, `conversation_id`),
  INDEX `idx_spring_ai_chat_memory_agent_session` (`agent_name`, `session_id`),
  INDEX `idx_spring_ai_chat_memory_agent_session_timestamp` (`agent_name`, `session_id`, `timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Spring AI聊天记忆表，兼容Spring AI框架和本工程业务';



