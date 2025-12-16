-- ====================================
-- UniSport 数据库迁移脚本
-- 描述：新增用户教育经历表 user_educations
-- 创建时间：2025-12-15
-- 版本：v1.1.0
-- ====================================

USE unisport;

CREATE TABLE IF NOT EXISTS `user_educations` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '教育经历ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `school_id` BIGINT COMMENT '学校ID',
    `school` VARCHAR(100) COMMENT '学校名称',
    `department_id` BIGINT COMMENT '学院ID',
    `department` VARCHAR(100) COMMENT '学院名称',
    `student_id` VARCHAR(30) COMMENT '学号',
    `start_date` VARCHAR(7) COMMENT '开始时间(YYYY-MM)',
    `end_date` VARCHAR(7) DEFAULT NULL COMMENT '结束时间(YYYY-MM)，null表示至今',
    `is_primary` TINYINT DEFAULT 0 COMMENT '是否主要教育经历：1-是 0-否',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '验证状态：pending/verified/failed',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_primary_created` (`is_primary`, `created_at`),
    KEY `idx_school_department` (`school_id`, `department_id`),
    CONSTRAINT `fk_education_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户教育经历表';

ALTER TABLE `user_educations`
    ADD INDEX IF NOT EXISTS `idx_user` (`user_id`),
    ADD INDEX IF NOT EXISTS `idx_primary_created` (`is_primary`, `created_at`),
    ADD INDEX IF NOT EXISTS `idx_school_department` (`school_id`, `department_id`);
