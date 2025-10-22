package com.tradingsim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 用于检查应用和依赖服务的健康状态
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 基本健康检查
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "tradingsim-backend");
        return ResponseEntity.ok(health);
    }

    /**
     * 详细健康检查，包括数据库和Redis连接
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "tradingsim-backend");
        health.put("timestamp", System.currentTimeMillis());

        // 检查数据库连接
        try (Connection connection = dataSource.getConnection()) {
            health.put("database", Map.of(
                "status", "UP",
                "url", connection.getMetaData().getURL()
            ));
        } catch (Exception e) {
            health.put("database", Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
        }

        // 检查Redis连接
        try {
            redisTemplate.opsForValue().set("health:check", "ok");
            String value = (String) redisTemplate.opsForValue().get("health:check");
            health.put("redis", Map.of(
                "status", "UP",
                "test", value
            ));
        } catch (Exception e) {
            health.put("redis", Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
        }

        // 确定整体状态
        boolean allUp = health.values().stream()
            .filter(v -> v instanceof Map)
            .map(v -> (Map<?, ?>) v)
            .allMatch(m -> "UP".equals(m.get("status")));

        health.put("status", allUp ? "UP" : "DOWN");

        return ResponseEntity.ok(health);
    }
}