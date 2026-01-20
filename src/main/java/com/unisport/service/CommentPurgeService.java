package com.unisport.service;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/17$
 */
public interface CommentPurgeService {

    void purgeExpiredDeletedComments(int retentionDays);
}
