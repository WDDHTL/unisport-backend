-- 扩充通知类型，支持微信交换相关通知
ALTER TABLE `notifications`
    MODIFY `type` ENUM('like','comment','follow','system','wechat_exchange_request','wechat_exchange_accept','wechat_exchange_reject') NOT NULL;

-- 扩充关联对象类型，支持微信交换
ALTER TABLE `notifications`
    MODIFY `related_type` ENUM('post','comment','user','match','wechat_exchange') NULL;
