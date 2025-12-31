-- Agent配置表DDL
CREATE TABLE IF NOT EXISTS `agent_config` (
  `agent_id` VARCHAR(64) NOT NULL COMMENT 'Agent唯一标识符，UUID，自动生成',
  `agent_name` VARCHAR(128) NOT NULL COMMENT 'Agent名称，业务侧通过该名称访问',
  `system_prompt` TEXT NOT NULL COMMENT '系统提示词',
  `agent_type` VARCHAR(16) NOT NULL COMMENT 'Agent类型，仅支持chat和stream',
  `has_tools` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否使用工具，0=不使用，1=使用',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用，0=禁用，1=启用',
  `description` TEXT COMMENT 'Agent描述信息',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（预留字段，未来可用于扩展）',
  PRIMARY KEY (`agent_id`),
  UNIQUE KEY `uk_agent_name` (`agent_name`),
  CHECK (`agent_type` IN ('chat', 'stream'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Agent配置表';