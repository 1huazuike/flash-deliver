package com.huizuike.flashdeliverjava.utils;

import com.huizuike.flashdeliverjava.common.constant.RedisKeyConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration:1800}")
    private Long accessExpiration;  // 30 分钟

    @Value("${jwt.refresh-expiration:604800}")
    private Long refreshExpiration;  // 7 天

    private SecretKey signingKey;
    private final StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== 生成 Token ====================

    /**
     * 生成 Access Token（短期）
     */
    public String generateAccessToken(Long userId, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration * 1000))
                .signWith(signingKey)
                .compact();
    }

    /**
     * 生成 Refresh Token（长期，带唯一ID用于撤销）
     */
    public String generateRefreshToken(Long userId) {
        String tokenId = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .claim("tokenId", tokenId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration * 1000))
                .signWith(signingKey)
                .compact();

        // 存储 Refresh Token 信息到 Redis（用于验证和撤销）
        String key = RedisKeyConstants.getRefreshTokenKey(tokenId);
        stringRedisTemplate.opsForValue().set(
                key,
                String.valueOf(userId),
                refreshExpiration,
                TimeUnit.SECONDS
        );

        return token;
    }

    // ==================== 解析 Token ====================

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ==================== 验证 Token ====================

    /**
     * 验证 Access Token
     */
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            String type = claims.get("type", String.class);
            return "access".equals(type) && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证 Refresh Token（包括黑名单检查）
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            String type = claims.get("type", String.class);
            if (!"refresh".equals(type)) {
                return false;
            }

            // 检查是否过期
            if (claims.getExpiration().before(new Date())) {
                return false;
            }

            // 检查黑名单
            String tokenId = claims.get("tokenId", String.class);
            String blacklistKey = RedisKeyConstants.getRefreshTokenBlacklistKey(tokenId);
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(blacklistKey))) {
                return false;
            }

            // 检查 Redis 中是否存在
            String key = RedisKeyConstants.getRefreshTokenKey(tokenId);
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 刷新 Access Token
     */
    public String refreshAccessToken(String refreshToken) {
        if (!validateRefreshToken(refreshToken)) {
            return null;
        }

        Claims claims = parseToken(refreshToken);
        Long userId = Long.valueOf(claims.getSubject());
        String role = claims.get("role", String.class);
        return generateAccessToken(userId, role);
    }

    // ==================== 撤销 Refresh Token ====================

    /**
     * 撤销 Refresh Token（退出登录时调用）
     */
    public void revokeRefreshToken(String refreshToken) {
        try {
            Claims claims = parseToken(refreshToken);
            String tokenId = claims.get("tokenId", String.class);
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();

            if (ttl > 0) {
                // 加入黑名单，过期时间与 Refresh Token 剩余时间一致
                String blacklistKey = RedisKeyConstants.getRefreshTokenBlacklistKey(tokenId);
                stringRedisTemplate.opsForValue().set(
                        blacklistKey,
                        "revoked",
                        ttl,
                        TimeUnit.MILLISECONDS
                );
            }

            // 删除 Redis 中的 Refresh Token 记录
            String key = RedisKeyConstants.getRefreshTokenKey(tokenId);
            stringRedisTemplate.delete(key);

            log.info("Refresh Token 已撤销");
        } catch (Exception e) {
            log.error("撤销 Refresh Token 失败: {}", e.getMessage());
        }
    }

    // ==================== 信息提取 ====================

    public Long getUserIdFromToken(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }

    public Long getAccessExpiration() {
        return accessExpiration;
    }

    public Long getRefreshExpiration() {
        return refreshExpiration;
    }

    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}