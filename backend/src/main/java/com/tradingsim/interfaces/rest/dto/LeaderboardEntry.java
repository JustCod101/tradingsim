package com.tradingsim.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tradingsim.domain.model.GameSession;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 排行榜条目DTO
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Schema(description = "排行榜条目")
public class LeaderboardEntry {
    
    @Schema(description = "排名", example = "1")
    private int rank;
    
    @Schema(description = "会话ID", example = "session_1640995200_abc123")
    private String sessionId;
    
    @Schema(description = "用户ID", example = "user123")
    private String userId;
    
    @Schema(description = "数据段ID", example = "segment_AAPL_1640995200")
    private String segmentId;
    
    @Schema(description = "总分数", example = "95.5")
    private double totalScore;
    
    @Schema(description = "总盈亏", example = "2150.75")
    private BigDecimal totalPnl;
    
    @Schema(description = "胜率", example = "75.0")
    private double winRate;
    
    @Schema(description = "平均响应时间(毫秒)", example = "1150")
    private double averageResponseTime;
    
    @Schema(description = "总决策数", example = "20")
    private int totalDecisions;
    
    @Schema(description = "超时次数", example = "1")
    private int timeoutCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "完成时间", example = "2024-01-01 10:30:00")
    private LocalDateTime completedAt;
    
    // 默认构造函数
    public LeaderboardEntry() {}
    
    /**
     * 从领域模型转换为DTO
     */
    public static LeaderboardEntry fromDomain(GameSession session) {
        LeaderboardEntry entry = new LeaderboardEntry();
        entry.setSessionId(session.getSessionId());
        entry.setUserId(session.getUserId());
        entry.setSegmentId(session.getSegmentId());
        entry.setTotalScore(session.getTotalScore());
        entry.setTotalPnl(session.getTotalPnl());
        entry.setWinRate(session.getWinRate());
        entry.setAverageResponseTime(session.getAverageResponseTime());
        entry.setTotalDecisions(session.getBuyDecisionCount() + 
                               session.getSellDecisionCount() + 
                               session.getSkipDecisionCount());
        entry.setTimeoutCount(session.getTimeoutCount());
        entry.setCompletedAt(session.getUpdatedAt());
        return entry;
    }
    
    // Getters and Setters
    public int getRank() {
        return rank;
    }
    
    public void setRank(int rank) {
        this.rank = rank;
    }
    
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
    
    public double getWinRate() {
        return winRate;
    }
    
    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }
    
    public double getAverageResponseTime() {
        return averageResponseTime;
    }
    
    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }
    
    public int getTotalDecisions() {
        return totalDecisions;
    }
    
    public void setTotalDecisions(int totalDecisions) {
        this.totalDecisions = totalDecisions;
    }
    
    public int getTimeoutCount() {
        return timeoutCount;
    }
    
    public void setTimeoutCount(int timeoutCount) {
        this.timeoutCount = timeoutCount;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}