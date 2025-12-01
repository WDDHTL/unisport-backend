-- ====================================
-- UniSport 数据库迁移脚本
-- 描述：为 users 表添加学号字段
-- 创建时间：2025-12-01
-- 版本：v1.1.0
-- ====================================

USE unisport;

-- 为已有的 users 表添加 student_id 字段
-- 注意：如果表不存在此字段才执行，否则跳过
ALTER TABLE `users` 
ADD COLUMN IF NOT EXISTS `student_id` VARCHAR(30) COMMENT '学号' AFTER `department`;

-- 添加学号字段的索引（可选，用于优化查询性能）
ALTER TABLE `users` 
ADD INDEX IF NOT EXISTS `idx_student_id` (`student_id`);

-- 验证字段是否添加成功
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = 'unisport'
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'student_id';

-- 查看 users 表结构
DESC users;

-- ====================================
-- 注意事项
-- ====================================
-- 1. student_id 字段为 VARCHAR(30)，允许为 NULL
-- 2. 已有用户的 student_id 默认为 NULL，可在编辑资料时补充
-- 3. 索引 idx_student_id 用于优化学号查询性能
-- 4. 执行前请备份数据库
-- ====================================
