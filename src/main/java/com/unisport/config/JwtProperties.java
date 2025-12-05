package com.unisport.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性类
 * 
 * 从application.properties中读取JWT相关配置
 * 配置项：
 * - jwt.secret: JWT签名密钥
 * - jwt.expiration: Token过期时间（毫秒）
 *
 * @author UniSport Team
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT签名密钥
     * 用于生成和验证JWT Token的密钥，必须保密
     */
    private String secret;

    /**
     * Token过期时间（毫秒）
     * 默认7天：604800000毫秒
     */
    private Long expiration;
}
