-- 创建动态Agent相关表结构（更新版）
-- agent_type仅支持chat和stream两种类型
-- 添加has_tools字段判断是否使用工具

-- 1. Agent配置表（更新版）
CREATE TABLE `agent_config` (
  `agent_id` VARCHAR(64) NOT NULL DEFAULT (UUID()) COMMENT 'Agent唯一标识符，UUID，自动生成',
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

-- 为兼容旧版本MySQL，添加触发器自动生成UUID（MySQL 8.0+ 支持DEFAULT (UUID())语法，旧版本需要触发器）
DELIMITER //
CREATE TRIGGER `before_agent_config_insert` BEFORE INSERT ON `agent_config`
FOR EACH ROW
BEGIN
    IF NEW.agent_id IS NULL OR NEW.agent_id = '' THEN
        SET NEW.agent_id = UUID();
    END IF;
END //
DELIMITER ;

-- 2. Agent工具关联表（移除外键约束）
CREATE TABLE `agent_tool_rel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `agent_id` VARCHAR(64) NOT NULL COMMENT '关联的Agent ID',
  `tool_name` VARCHAR(128) NOT NULL COMMENT '工具Bean名称，从Spring上下文中获取',
  `tool_order` INT NOT NULL DEFAULT 0 COMMENT '工具执行顺序',
  PRIMARY KEY (`id`),
  KEY `idx_agent_id` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Agent工具关联表';

-- 3. Agent提示词变量表（移除外键约束）
CREATE TABLE `agent_prompt_var` (
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

-- 4. 聊天记忆配置表（移除外键约束）
CREATE TABLE `chat_memory_config` (
  `agent_id` VARCHAR(64) NOT NULL COMMENT '关联的Agent ID，一对一关系',
  `memory_type` VARCHAR(64) NOT NULL DEFAULT 'messageWindow' COMMENT '记忆类型：messageWindow、conversation等',
  `max_messages` INT NOT NULL DEFAULT 10 COMMENT '最大消息数',
  `message_expire` BIGINT NOT NULL DEFAULT 3600000 COMMENT '消息过期时间(毫秒)',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用，0=禁用，1=启用',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（预留字段，未来可用于扩展）',
  PRIMARY KEY (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='聊天记忆配置表';

-- 5. Spring AI聊天记忆表（合并后的表，Spring AI JdbcChatMemoryRepository使用）
CREATE TABLE `spring_ai_chat_memory` (
  `id` VARCHAR(255) NOT NULL DEFAULT (UUID()) COMMENT '消息唯一标识符，UUID，自动生成',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户唯一标识符，参考UserContextHolder获取，代码侧设置默认值test',
  `session_id` VARCHAR(128) NOT NULL COMMENT '会话唯一标识符，升级为长期会话标识',
  `agent_id` VARCHAR(64) NULL COMMENT '智能体唯一标识符，与agent_config表关联',
  `message_type` VARCHAR(32) NOT NULL DEFAULT 'user' COMMENT '消息类型：user(用户问的)、agent(Agent需要的)、system(系统实际展示的)',
  `chat_type` VARCHAR(32) NOT NULL DEFAULT 'system' COMMENT '聊天类型：system(系统级会话)、custom(自定义聊天)',
  `message` TEXT NOT NULL COMMENT '聊天消息内容，JSON格式',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_spring_ai_chat_memory_user_session` (`user_id`, `session_id`),
  INDEX `idx_spring_ai_chat_memory_session` (`session_id`),
  INDEX `idx_spring_ai_chat_memory_agent` (`agent_id`),
  INDEX `idx_spring_ai_chat_memory_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Spring AI聊天记忆表，用于存储聊天对话历史，按用户-会话维度存储';

-- 为spring_ai_chat_memory表添加触发器，兼容旧版本MySQL
DELIMITER //
CREATE TRIGGER `before_spring_ai_chat_memory_insert` BEFORE INSERT ON `spring_ai_chat_memory`
FOR EACH ROW
BEGIN
    IF NEW.id IS NULL OR NEW.id = '' THEN
        SET NEW.id = UUID();
    END IF;
END //
DELIMITER ;

-- 6. 会话信息表（用于存储会话的生命周期和元数据，移除外键约束）
CREATE TABLE `session_info` (
  `session_id` VARCHAR(128) NOT NULL COMMENT '会话唯一标识符，作为主键',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户唯一标识符，参考UserContextHolder获取，代码侧设置默认值test',
  `session_name` VARCHAR(255) NULL COMMENT '会话名称，默认使用第一个用户消息作为名称',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '会话是否活跃，0=不活跃，1=活跃',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会话创建时间',
  `last_active_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '会话最后活跃时间',
  `expired_at` TIMESTAMP NULL COMMENT '会话过期时间',
  PRIMARY KEY (`session_id`),
  INDEX `idx_session_info_user` (`user_id`),
  INDEX `idx_session_info_active` (`is_active`),
  INDEX `idx_session_info_expired` (`expired_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会话信息表，用于存储会话的生命周期和元数据';

-- 为现有表添加session_name列的ALTER语句
ALTER TABLE `session_info` ADD COLUMN `session_name` VARCHAR(255) NULL COMMENT '会话名称，默认使用第一个用户消息作为名称' AFTER `user_id`;

-- 3. SSE消息记录表
CREATE TABLE `sse_message` (
  `id` VARCHAR(255) NOT NULL DEFAULT (UUID()) COMMENT '消息唯一标识符，UUID，自动生成',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户唯一标识符，参考UserContextHolder获取',
  `session_id` VARCHAR(128) NOT NULL COMMENT '会话唯一标识符，与session_info表关联',
  `connect_id` VARCHAR(255) NOT NULL COMMENT '连接唯一标识符，关联SSE连接',
  `agent_id` VARCHAR(64) NULL COMMENT '智能体唯一标识符，与agent_config表关联',
  `message_type` VARCHAR(32) NOT NULL DEFAULT 'message' COMMENT '消息类型：message(普通消息)、log(日志消息)、error(错误消息)、table(表格消息)',
  `message_group` VARCHAR(128) NOT NULL COMMENT '消息分组ID，用于将一次问答的多次消息分组，使用会话内唯一的UUID',
  `message_content` TEXT NOT NULL COMMENT '消息内容',
  `send_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息发送时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否逻辑删除，0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_sse_message_user_session` (`user_id`, `session_id`),
  INDEX `idx_sse_message_session` (`session_id`),
  INDEX `idx_sse_message_group` (`message_group`),
  INDEX `idx_sse_message_agent` (`agent_id`),
  INDEX `idx_sse_message_send_time` (`send_time`),
  INDEX `idx_sse_message_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='SSE消息记录表，用于存储发送给前端的消息';

-- 为sse_message表添加触发器，兼容旧版本MySQL
DELIMITER //
CREATE TRIGGER `before_sse_message_insert` BEFORE INSERT ON `sse_message`
FOR EACH ROW
BEGIN
    IF NEW.id IS NULL OR NEW.id = '' THEN
        SET NEW.id = UUID();
    END IF;
END //
DELIMITER ;
