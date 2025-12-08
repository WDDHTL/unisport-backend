-- ====================================
-- 添加积分榜表
-- ====================================

USE unisport;

-- 积分榜表
DROP TABLE IF EXISTS `standings`;
CREATE TABLE `standings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `league_id` BIGINT NOT NULL COMMENT '联赛ID',
    `team_id` BIGINT COMMENT '队伍ID（团队项目）',
    `user_id` BIGINT COMMENT '用户ID（个人项目）',
    `team_name` VARCHAR(100) NOT NULL COMMENT '队伍/用户名称',
    `rank` INT NOT NULL COMMENT '排名',
    `played` INT DEFAULT 0 COMMENT '已赛场次',
    `won` INT DEFAULT 0 COMMENT '胜场',
    `drawn` INT DEFAULT 0 COMMENT '平局',
    `lost` INT DEFAULT 0 COMMENT '负场',
    `points` INT DEFAULT 0 COMMENT '积分',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_league` (`league_id`),
    KEY `idx_team` (`team_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_league_rank` (`league_id`, `rank`),
    FOREIGN KEY (`league_id`) REFERENCES `leagues`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`team_id`) REFERENCES `teams`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分榜表';

-- 添加示例数据（可选）
-- 注意：需要先确保 leagues 和 teams 表中有相应的数据
