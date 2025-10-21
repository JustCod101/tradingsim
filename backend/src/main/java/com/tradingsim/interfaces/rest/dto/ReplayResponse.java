package com.tradingsim.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 游戏重放响应DTO
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Schema(description = "游戏重放响应")
public class ReplayResponse {
    
    @Schema(description = "重放ID", example = "replay_1640995200123")
    private String replayId;
    
    @Schema(description = "会话ID", example = "session_1640995200_abc123")
    private String sessionId;
    
    @Schema(description = "重放速度倍数", example = "2.0")
    private double speed;
    
    @Schema(description = "重放状态", example = "STARTED")
    private String status;
    
    @Schema(description = "WebSocket连接URL", example = "ws://localhost:8080/ws/replay/replay_1640995200123")
    private String websocketUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间", example = "2024-01-01 10:00:00")
    private LocalDateTime startedAt;
    
    // 默认构造函数
    public ReplayResponse() {
        this.startedAt = LocalDateTime.now();
    }
    
    // 构造函数
    public ReplayResponse(String replayId, String sessionId, double speed, String status) {
        this();
        this.replayId = replayId;
        this.sessionId = sessionId;
        this.speed = speed;
        this.status = status;
        this.websocketUrl = generateWebSocketUrl(replayId);
    }
    
    /**
     * 生成WebSocket连接URL
     */
    private String generateWebSocketUrl(String replayId) {
        return String.format("ws://localhost:8080/ws/replay/%s", replayId);
    }
    
    // Getters and Setters
    public String getReplayId() {
        return replayId;
    }
    
    public void setReplayId(String replayId) {
        this.replayId = replayId;
        this.websocketUrl = generateWebSocketUrl(replayId);
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getWebsocketUrl() {
        return websocketUrl;
    }
    
    public void setWebsocketUrl(String websocketUrl) {
        this.websocketUrl = websocketUrl;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
}