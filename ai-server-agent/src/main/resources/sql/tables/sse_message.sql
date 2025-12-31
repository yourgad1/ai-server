-- SSE消息记录表DDL
CREATE TABLE IF NOT EXISTS `sse_message` (
  `id` VARCHAR(255) NOT NULL COMMENT '消息唯一标识符，UUID，自动生成',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户唯一标识符，参考UserContextHolder获取',
  `session_id` VARCHAR(128) NOT NULL COMMENT '会话唯一标识符，与session_info表关联',
  `connect_id` VARCHAR(255) NOT NULL COMMENT '连接唯一标识符，关联SSE连接',
  `agent_id` VARCHAR(64) NULL COMMENT '智能体唯一标识符，与agent_config表关联',
  `message_type` VARCHAR(32) NOT NULL DEFAULT 'message' COMMENT '消息类型：message(普通消息)、log(日志消息)、error(错误消息)、table(表格消息)',
  `message_content` TEXT NOT NULL COMMENT '消息内容',
  `send_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '消息发送时间，精确到毫秒',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否逻辑删除，0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_sse_message_user_session` (`user_id`, `session_id`),
  INDEX `idx_sse_message_session` (`session_id`),
  INDEX `idx_sse_message_agent` (`agent_id`),
  INDEX `idx_sse_message_send_time` (`send_time`),
  INDEX `idx_sse_message_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='SSE消息记录表，用于存储发送给前端的消息';