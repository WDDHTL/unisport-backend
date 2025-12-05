package com.unisport.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 
 * 提供JWT Token的生成、解析和验证功能
 * 使用HMAC-SHA256算法进行签名
 *
 * @author UniSport Team
 */
@Slf4j
public class JwtUtil {

    /**
     * 生成JWT Token
     * 
     * 将用户ID和账号信息编码到Token中
     * Token包含：
     * - userId: 用户ID
     * - account: 用户账号
     * - iat: 签发时间
     * - exp: 过期时间
     *
     * @param userId 用户ID
     * @param account 用户账号
     * @param secret JWT密钥
     * @param expiration 过期时间（毫秒）
     * @return JWT Token字符串
     */
    public static String generateToken(Long userId, String account, String secret, Long expiration) {
        try {
            // 当前时间
            Date now = new Date();
            // 过期时间
            Date expirationDate = new Date(now.getTime() + expiration);

            // 构建Token的载荷（Payload）
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("account", account);

            // 生成密钥
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            // 生成JWT Token
            String token = Jwts.builder()
                    .claims(claims)                     // 设置载荷
                    .issuedAt(now)                      // 签发时间
                    .expiration(expirationDate)         // 过期时间
                    .signWith(key)                      // 签名算法和密钥
                    .compact();

            log.debug("JWT Token生成成功，用户ID：{}，账号：{}", userId, account);
            return token;

        } catch (Exception e) {
            log.error("JWT Token生成失败", e);
            throw new RuntimeException("Token生成失败", e);
        }
    }

    /**
     * 解析JWT Token
     * 
     * 解析Token并提取其中的载荷信息
     *
     * @param token JWT Token字符串
     * @param secret JWT密钥
     * @return Claims对象，包含Token中的所有信息
     * @throws RuntimeException Token解析失败或Token已过期
     */
    public static Claims parseToken(String token, String secret) {
        try {
            // 生成密钥
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            // 解析Token
            Claims claims = Jwts.parser()
                    .verifyWith(key)                    // 验证签名
                    .build()
                    .parseSignedClaims(token)           // 解析Token
                    .getPayload();

            log.debug("JWT Token解析成功");
            return claims;

        } catch (Exception e) {
            log.error("JWT Token解析失败：{}", e.getMessage());
            throw new RuntimeException("Token无效或已过期", e);
        }
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token字符串
     * @param secret JWT密钥
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取用户账号
     *
     * @param token JWT Token字符串
     * @param secret JWT密钥
     * @return 用户账号
     */
    public static String getAccountFromToken(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.get("account", String.class);
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token字符串
     * @param secret JWT密钥
     * @return true-有效，false-无效
     */
    public static boolean validateToken(String token, String secret) {
        try {
            parseToken(token, secret);
            return true;
        } catch (Exception e) {
            log.warn("Token验证失败：{}", e.getMessage());
            return false;
        }
    }
}
