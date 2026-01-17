package com.unisport.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/17$
 */
@Data
@Component
@ConfigurationProperties(prefix = "post.purge")
public class PostPurgeProperties {
    private int retentionDays = 30;
    private String cron = "0 10 3 * * ?";
    private String zone = "Asia/Shanghai";
}
