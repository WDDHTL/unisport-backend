-- ====================================
-- 为 posts 表添加 school_id 字段
-- ====================================

USE unisport;

-- 添加 school_id 字段
ALTER TABLE `posts` 
ADD COLUMN `school_id` BIGINT NULL COMMENT '学校ID' AFTER `category_id`;

-- 添加外键约束
ALTER TABLE `posts`
ADD CONSTRAINT `fk_posts_school` 
FOREIGN KEY (`school_id`) REFERENCES `schools`(`id`) ON DELETE CASCADE;

-- 添加索引以提升查询性能
ALTER TABLE `posts` 
ADD INDEX `idx_school_category` (`school_id`, `category_id`);

-- 更新说明
-- 执行方式：mysql -u root -p unisport < migration_add_school_id_to_posts.sql
