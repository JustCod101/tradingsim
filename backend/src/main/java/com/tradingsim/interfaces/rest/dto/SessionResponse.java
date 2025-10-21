package com.tradingsim.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tradingsim.domain.model.GameSession;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 游戏会话响应DTO
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Schema(description = "游戏会话响应")
public class SessionResponse {
    
    @Schema(description = "会话ID", example = "session_1640995200_abc123")
    private String sessionId;
    
    @Schema(description = "用户ID", example = "user123")
    private String userId;
    
    @Schema(description = "数据段ID", example = "segment_AAPL_1640995200")
    private String segmentId;
    
    @Schema(description = "会话状态", example = "ACTIVE")
    private String status;
    
    @Schema(description = "当前帧索引", example = "150")
    private int currentFrame;
    
    @Schema(description = "总帧数", example = "300")
    private int totalFrames;
    
    @Schema(description = "总分数", example = "85.5")
    private double totalScore;
    
    @Schema(description = "总盈亏", example = "1250.75")
    private BigDecimal totalPnl;
    
    @Schema(description = "买入决策数", example = "5")
    private int buyDecisionCount;
    
    @Schema(description = "卖出决策数", example = "3")
    private int sellDecisionCount;
    
    @Schema(description = "跳过决策数", example = "12")
    private int skipDecisionCount;
    
    @Schema(description = "超时次数", example = "2")
    private int timeoutCount;
    
    @Schema(description = "平均响应时间(毫秒)", example = "1250")
    private double averageResponseTime;
    
    @Schema(description = "随机种子", example = "1640995200123")
    private long seedValue;
    
    @Schema(description = "进度百分比", example = "50.0")
    private double progress;
    
    @Schema(description = "胜率", example = "60.0")
    private double winRate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间", example = "2024-01-01 10:30:00")
    private LocalDateTime updatedAt;
    
    // 默认构造函数
    public SessionResponse() {}
    
    /**
     * 从领域模型转换为DTO
     */
    public static SessionResponse fromDomain(GameSession session) {
        SessionResponse response = new SessionResponse();
        response.setSessionId(session.getSessionId());
        response.setUserId(session.getUserId());
        response.setSegmentId(session.getSegmentId());
        response.setStatus(session.getStatus().name());
        response.setCurrentFrame(session.getCurrentFrame());
        response.setTotalFrames(session.getTotalFrames());
        response.setTotalScore(session.getTotalScore());
        response.setTotalPnl(session.getTotalPnl());
        response.setBuyDecisionCount(session.getBuyDecisionCount());
        response.setSellDecisionCount(session.getSellDecisionCount());
        response.setSkipDecisionCount(session.getSkipDecisionCount());
        response.setTimeoutCount(session.getTimeoutCount());
        response.setAverageResponseTime(session.getAverageResponseTime());
        response.setSeedValue(session.getSeedValue());
        response.setProgress(session.getProgress());
        response.setWinRate(session.getWinRate());
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        return response;
    }
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getSegmentId() {
        return segmentId;
    }
    
    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getCurrentFrame() {
        return currentFrame;
    }
    
    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }
    
    public int getTotalFrames() {
        return totalFrames;
    }
    
    public void setTotalFrames(int totalFrames) {
        this.totalFrames = totalFrames;
    }
    
    public double getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }
    
    public BigDecimal getTotalPnl() {
        return totalPnl;
    }
    
    public void setTotalPnl(BigDecimal totalPnl) {
        this.totalPnl = totalPnl;
    }
    
    public int getBuyDecisionCount() {
        return buyDecisionCount;
    }
    
    public void setBuyDecisionCount(int buyDecisionCount) {
        this.buyDecisionCount = buyDecisionCount;
    }
    
    public int getSellDecisionCount() {
        return sellDecisionCount;
    }
    
    public void setSellDecisionCount(int sellDecisionCount) {
        this.sellDecisionCount = sellDecisionCount;
    }
    
    public int getSkipDecisionCount() {
        return skipDecisionCount;
    }
    
    public void setSkipDecisionCount(int skipDecisionCount) {
        this.skipDecisionCount = skipDecisionCount;
    }
    
    public int getTimeoutCount() {
        return timeoutCount;
    }
    
    public void setTimeoutCount(int timeoutCount) {
        this.timeoutCount = timeoutCount;
    }
    
    public double getAverageResponseTime() {
        return averageResponseTime;
    }
    
    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }
    
    public long getSeedValue() {
        return seedValue;
    }
    
    public void setSeedValue(long seedValue) {
        this.seedValue = seedValue;
    }
    
    public double getProgress() {
        return progress;
    }
    
    public void setProgress(double progress) {
        this.progress = progress;
    }
    
    public double getWinRate() {
        return winRate;
    }
    
    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}