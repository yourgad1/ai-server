-- 为spring_ai_chat_memory表添加触发器，兼容旧版本MySQL
-- 注意：Spring JDBC不支持DELIMITER语法，因此触发器定义使用单行格式
DROP TRIGGER IF EXISTS `before_spring_ai_chat_memory_insert`;
CREATE TRIGGER IF NOT EXISTS `before_spring_ai_chat_memory_insert` BEFORE INSERT ON `SPRING_AI_CHAT_MEMORY` FOR EACH ROW SET NEW.id = IF(NEW.id IS NULL OR NEW.id = '', UUID(), NEW.id);