-- 会话信息表DDL
CREATE TABLE IF NOT EXISTS `session_info` (
  `session_id` VARCHAR(128) NOT NULL COMMENT '会话唯一标识符，作为主键',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户唯一标识符，参考UserContextHolder获取，代码侧设置默认值test',
  `session_name` VARCHAR(255) NULL COMMENT '会话名称，默认使用第一个用户消息作为名称',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '会话是否活跃，0=不活跃，1=活跃，2=弃用',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会话创建时间',
  `last_active_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '会话最后活跃时间',
  `expired_at` TIMESTAMP NULL COMMENT '会话过期时间',
  PRIMARY KEY (`session_id`),
  INDEX `idx_session_info_user` (`user_id`),
  INDEX `idx_session_info_active` (`is_active`),
  INDEX `idx_session_info_expired` (`expired_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会话信息表，用于存储会话的生命周期和元数据';