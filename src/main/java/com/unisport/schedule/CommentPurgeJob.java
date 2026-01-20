package com.unisport.schedule;

import com.unisport.properties.CommentPurgeProperties;
import com.unisport.properties.PostPurgeProperties;
import com.unisport.service.CommentPurgeService;
import com.unisport.service.PostPurgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/20$
 */
@Component
@RequiredArgsConstructor
public class CommentPurgeJob {

    private CommentPurgeService commentPurgeService;
    private CommentPurgeProperties props;

    @Scheduled(cron = "${comment.purge.cron}", zone = "${comment.purge.zone}")
    public void run() {
        commentPurgeService.purgeExpiredDeletedComments(props.getRetentionDays());
    }
}
