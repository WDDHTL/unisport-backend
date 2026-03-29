-- 新增微信交换请求表
DROP TABLE IF EXISTS `wechat_exchange_requests`;
CREATE TABLE `wechat_exchange_requests` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `requester_id` BIGINT NOT NULL COMMENT '发起方用户ID',
    `target_id` BIGINT NOT NULL COMMENT '接收方用户ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/accepted/rejected/cancelled/expired',
    `source` VARCHAR(32) DEFAULT NULL COMMENT '来源标记',
    `requester_wechat_snapshot` VARCHAR(128) NOT NULL COMMENT '发起时的微信号快照（加密）',
    `target_wechat_snapshot` VARCHAR(128) DEFAULT NULL COMMENT '同意时的微信号快照（加密）',
    `respond_message` VARCHAR(255) DEFAULT NULL COMMENT '处理说明/原因',
    `expired_at` DATETIME NOT NULL COMMENT '过期时间',
    `responded_at` DATETIME DEFAULT NULL COMMENT '处理时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_requester_target_status` (`requester_id`, `target_id`, `status`),
    KEY `idx_target_status_created` (`target_id`, `status`, `created_at`),
    KEY `idx_requester_status_created` (`requester_id`, `status`, `created_at`),
    CONSTRAINT `fk_wechat_requester` FOREIGN KEY (`requester_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_wechat_target` FOREIGN KEY (`target_id`) REFERENCES `users`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信交换请求';
