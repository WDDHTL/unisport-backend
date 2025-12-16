-- ====================================
-- UniSport 数据库初始化脚本
-- ====================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS unisport DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE unisport;

-- ====================================
-- 1. 用户系统模块
-- ====================================

-- 用户表
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `account` VARCHAR(50) NOT NULL COMMENT '账号（学号/手机号）',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `school` VARCHAR(100) NOT NULL COMMENT '学校名称',
    `department` VARCHAR(100) NOT NULL COMMENT '院系',
    `student_id` VARCHAR(30) COMMENT '学号',
    `bio` VARCHAR(500) COMMENT '个人简介',
    `gender` VARCHAR(10) COMMENT '性别：male-男, female-女, other-其他',
    `status` TINYINT DEFAULT 1 COMMENT '账号状态：1-正常, 0-禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_account` (`account`),
    KEY `idx_school_dept` (`school`, `department`),
    KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户关注关系表
DROP TABLE IF EXISTS `user_follows`;
CREATE TABLE `user_follows` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `follower_id` BIGINT NOT NULL COMMENT '关注者ID',
    `following_id` BIGINT NOT NULL COMMENT '被关注者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follow` (`follower_id`, `following_id`),
    KEY `idx_follower` (`follower_id`),
    KEY `idx_following` (`following_id`),
    FOREIGN KEY (`follower_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`following_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';

-- 教育经历表
DROP TABLE IF EXISTS `user_educations`;
CREATE TABLE `user_educations` (
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
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户教育经历表';

-- ====================================
-- 2. 赛事模块
-- ====================================

-- 运动分类表
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `code` VARCHAR(20) NOT NULL COMMENT '分类代码',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(10) COMMENT '图标emoji',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用, 0-禁用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动分类表';

-- 联赛表
DROP TABLE IF EXISTS `leagues`;
CREATE TABLE `leagues` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '联赛ID',
    `category_id` INT NOT NULL COMMENT '运动分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '联赛名称',
    `year` INT NOT NULL COMMENT '赛季年份',
    `description` TEXT COMMENT '联赛描述',
    `start_date` DATE COMMENT '开始日期',
    `end_date` DATE COMMENT '结束日期',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-进行中, 0-已结束',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_year` (`category_id`, `year`),
    FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联赛表';

-- 队伍表
DROP TABLE IF EXISTS `teams`;
CREATE TABLE `teams` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '队伍ID',
    `category_id` INT NOT NULL COMMENT '运动分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '队伍名称',
    `logo` VARCHAR(500) COMMENT '队徽URL',
    `captain_id` BIGINT COMMENT '队长用户ID',
    `department` VARCHAR(100) COMMENT '所属院系',
    `description` TEXT COMMENT '队伍介绍',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_captain` (`captain_id`),
    FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`),
    FOREIGN KEY (`captain_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队伍表';

-- 队伍成员表
DROP TABLE IF EXISTS `team_members`;
CREATE TABLE `team_members` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `team_id` BIGINT NOT NULL COMMENT '队伍ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `jersey_number` INT COMMENT '球衣号码',
    `position` VARCHAR(20) COMMENT '位置',
    `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_user` (`team_id`, `user_id`),
    KEY `idx_user` (`user_id`),
    FOREIGN KEY (`team_id`) REFERENCES `teams`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队伍成员表';

-- 比赛表
DROP TABLE IF EXISTS `matches`;
CREATE TABLE `matches` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '比赛ID',
    `league_id` BIGINT NOT NULL COMMENT '联赛ID',
    `category_id` INT NOT NULL COMMENT '运动分类ID',
    `team_a_id` BIGINT COMMENT 'A队ID',
    `team_b_id` BIGINT COMMENT 'B队ID',
    `player_a_id` BIGINT COMMENT 'A选手ID',
    `player_b_id` BIGINT COMMENT 'B选手ID',
    `team_a_name` VARCHAR(100) NOT NULL COMMENT 'A方名称',
    `team_b_name` VARCHAR(100) NOT NULL COMMENT 'B方名称',
    `score_a` INT COMMENT 'A方得分',
    `score_b` INT COMMENT 'B方得分',
    `status` VARCHAR(20) DEFAULT 'upcoming' COMMENT '比赛状态：upcoming-未开始, live-进行中, finished-已结束',
    `match_time` DATETIME NOT NULL COMMENT '比赛时间',
    `location` VARCHAR(200) NOT NULL COMMENT '比赛地点',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_league` (`league_id`),
    KEY `idx_category_time` (`category_id`, `match_time`),
    KEY `idx_status` (`status`),
    FOREIGN KEY (`league_id`) REFERENCES `leagues`(`id`),
    FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='比赛表';

-- ====================================
-- 3. 社交模块
-- ====================================

-- 帖子表
DROP TABLE IF EXISTS `posts`;
CREATE TABLE `posts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '发布用户ID',
    `category_id` INT NOT NULL COMMENT '运动分类ID',
    `content` TEXT NOT NULL COMMENT '帖子内容',
    `images` JSON COMMENT '图片URL数组',
    `likes_count` INT DEFAULT 0 COMMENT '点赞数',
    `comments_count` INT DEFAULT 0 COMMENT '评论数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常, 0-删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_category_time` (`category_id`, `created_at` DESC),
    KEY `idx_created` (`created_at` DESC),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- 评论表
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
    `parent_id` BIGINT COMMENT '父评论ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `likes_count` INT DEFAULT 0 COMMENT '点赞数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常, 0-删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_post` (`post_id`, `created_at`),
    KEY `idx_user` (`user_id`),
    KEY `idx_parent` (`parent_id`),
    FOREIGN KEY (`post_id`) REFERENCES `posts`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`parent_id`) REFERENCES `comments`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ====================================
-- 4. 初始化基础数据
-- ====================================

-- 插入运动分类
INSERT INTO `categories` (`code`, `name`, `icon`, `sort_order`) VALUES
('football', '足球', '⚽', 1),
('basketball', '篮球', '🏀', 2),
('badminton', '羽毛球', '🏸', 3),
('pingpong', '乒乓球', '🏓', 4),
('fitness', '健身', '💪', 5);

-- 插入测试用户
INSERT INTO `users` (`account`, `password`, `nickname`, `avatar`, `school`, `department`, `bio`, `gender`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'https://ui-avatars.com/api/?name=Admin&background=0D8ABC&color=fff', '清华大学', '计算机系', '系统管理员', 'male'),
('user001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户', 'https://ui-avatars.com/api/?name=Test&background=FF5733&color=fff', '清华大学', '经管学院', '热爱运动', 'male');

-- 注意：密码为 bcrypt 加密后的 "123456"
