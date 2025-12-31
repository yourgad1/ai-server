-- Agent工具关联表DDL
CREATE TABLE IF NOT EXISTS `agent_tool_rel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `agent_id` VARCHAR(64) NOT NULL COMMENT '关联的Agent ID',
  `tool_name` VARCHAR(128) NOT NULL COMMENT '工具Bean名称，从Spring上下文中获取',
  `tool_order` INT NOT NULL DEFAULT 0 COMMENT '工具执行顺序',
  PRIMARY KEY (`id`),
  KEY `idx_agent_id` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Agent工具关联表';
