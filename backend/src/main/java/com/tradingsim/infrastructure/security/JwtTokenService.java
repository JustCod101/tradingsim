package com.tradingsim.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token服务
 * 负责生成、验证和解析JWT Token
 */
@Service
public class JwtTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);
    
    @Value("${tradingsim.security.jwt.secret:tradingsim-secret-key-for-jwt-token-generation-and-validation}")
    private String jwtSecret;
    
    @Value("${tradingsim.security.jwt.expiration:86400}") // 24小时
    private Long jwtExpirationInSeconds;
    
    @Value("${tradingsim.security.jwt.refresh-expiration:604800}") // 7天
    private Long refreshTokenExpirationInSeconds;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    /**
     * 生成访问Token
     */
    public String generateAccessToken(String userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put("type", "access");
        
        return createToken(claims, userId, jwtExpirationInSeconds);
    }
    
    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        
        return createToken(claims, userId, refreshTokenExpirationInSeconds);
    }
    
    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims, String subject, Long expirationInSeconds) {
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationInSeconds, ChronoUnit.SECONDS);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 从Token中获取用户ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("userId") : null;
    }
    
    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("username") : null;
    }
    
    /**
     * 从Token中获取用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }
    
    /**
     * 从Token中获取Token类型
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("type") : null;
    }
    
    /**
     * 获取Token过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null && !isTokenExpired(token);
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证访问Token
     */
    public boolean validateAccessToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        String tokenType = getTokenTypeFromToken(token);
        return "access".equals(tokenType);
    }
    
    /**
     * 验证刷新Token
     */
    public boolean validateRefreshToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        String tokenType = getTokenTypeFromToken(token);
        return "refresh".equals(tokenType);
    }
    
    /**
     * 检查Token是否过期
     */
    private boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }
    
    /**
     * 从Token中解析Claims
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
            return null;
        } catch (UnsupportedJwtException e) {
            logger.warn("JWT token is unsupported: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            logger.warn("JWT token is malformed: {}", e.getMessage());
            return null;
        } catch (SignatureException e) {
            logger.warn("JWT signature validation failed: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            logger.warn("JWT token compact of handler are invalid: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取Token剩余有效时间（秒）
     */
    public Long getTokenRemainingTime(String token) {
        Date expiration = getExpirationDateFromToken(token);
        if (expiration == null) {
            return 0L;
        }
        
        long remainingTime = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        return Math.max(0L, remainingTime);
    }
    
    /**
     * 获取访问Token过期时间配置
     */
    public Long getAccessTokenExpirationInSeconds() {
        return jwtExpirationInSeconds;
    }
    
    /**
     * 获取刷新Token过期时间配置
     */
    public Long getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpirationInSeconds;
    }
}