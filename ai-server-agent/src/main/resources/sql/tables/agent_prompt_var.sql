-- Agent提示词变量表DDL
CREATE TABLE IF NOT EXISTS `agent_prompt_var` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `agent_id` VARCHAR(64) NOT NULL COMMENT '关联的Agent ID',
  `var_key` VARCHAR(128) NOT NULL COMMENT '变量键名',
  `var_value` TEXT COMMENT '变量值，JSON格式存储',
  `var_type` VARCHAR(32) NOT NULL DEFAULT 'static' COMMENT '变量类型：static(静态)、dynamic(动态)、runtime(运行时)',
  `var_source` VARCHAR(64) NOT NULL DEFAULT 'database' COMMENT '变量来源：database、business(业务侧传入)、system(系统提供)',
  `description` VARCHAR(256) COMMENT '变量描述',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（预留字段，未来可用于扩展）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_id_var_key` (`agent_id`, `var_key`),
  KEY `idx_agent_id` (`agent_id`),
  CHECK ((`var_type` = 'static' AND `var_value` IS NOT NULL) OR `var_type` != 'static')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Agent提示词变量表';