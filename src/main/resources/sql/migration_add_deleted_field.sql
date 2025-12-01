-- ====================================
-- 添加逻辑删除字段迁移脚本
-- ====================================

USE unisport;

-- 检查并添加 users 表的 deleted 字段
ALTER TABLE `users` 
ADD COLUMN IF NOT EXISTS `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除';

-- 为 deleted 字段创建索引以提升查询性能
ALTER TABLE `users` 
ADD INDEX IF NOT EXISTS `idx_deleted` (`deleted`);

-- 更新说明
-- 执行方式：mysql -u root -p unisport < migration_add_deleted_field.sql
