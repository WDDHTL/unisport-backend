package com.unisport.schedule;

import com.unisport.properties.PostPurgeProperties;
import com.unisport.service.PostPurgeService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * <p>
 * $ 定时物理清理 post
 * </p>
 *
 * @author 86139$
 * @since 2026/1/17$
 */
@Component
@RequiredArgsConstructor
public class PostPurgeJob {

    private PostPurgeService postPurgeService;
    private PostPurgeProperties props;

    @Scheduled(cron = "${post.purge.cron}", zone = "${post.purge.zone}")
    public void run() {
        postPurgeService.purgeExpiredDeletedPosts(props.getRetentionDays());
    }
}