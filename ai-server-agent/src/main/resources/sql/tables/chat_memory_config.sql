-- 聊天记忆配置表DDL
CREATE TABLE IF NOT EXISTS `chat_memory_config` (
  `agent_id` VARCHAR(64) NOT NULL COMMENT '关联的Agent ID，一对一关系',
  `memory_type` VARCHAR(64) NOT NULL DEFAULT 'messageWindow' COMMENT '记忆类型：messageWindow、conversation等',
  `max_messages` INT NOT NULL DEFAULT 10 COMMENT '最大消息数',
  `message_expire` BIGINT NOT NULL DEFAULT -1 COMMENT '消息过期时间(毫秒)，-1表示永不过期',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用，0=禁用，1=启用',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（预留字段，未来可用于扩展）',
  PRIMARY KEY (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='聊天记忆配置表';