package com.tradingsim.domain.model.user;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 用户统计信息实体类
 * 
 * @author TradingSim Team
 */
public class UserStats {
    
    private String userId;
    private int totalSessions;
    private int completedSessions;
    private BigDecimal totalScore;
    private BigDecimal bestScore;
    private BigDecimal averageScore;
    private BigDecimal totalPnl;
    private BigDecimal bestPnl;
    private BigDecimal worstPnl;
    private BigDecimal winRate;
    private int totalTrades;
    private int winningTrades;
    private int losingTrades;
    private long avgResponseTimeMs;
    private int maxConsecutiveWins;
    private int maxConsecutiveLosses;
    private long totalPlayTimeMinutes;
    private Integer rankPosition;
    private BigDecimal rankPercentile;
    private Instant createdAt;
    private Instant updatedAt;
    
    // 构造函数
    public UserStats() {}
    
    public UserStats(String userId) {
        this.userId = userId;
        this.totalSessions = 0;
        this.completedSessions = 0;
        this.totalScore = BigDecimal.ZERO;
        this.bestScore = BigDecimal.ZERO;
        this.averageScore = BigDecimal.ZERO;
        this.totalPnl = BigDecimal.ZERO;
        this.bestPnl = BigDecimal.ZERO;
        this.worstPnl = BigDecimal.ZERO;
        this.winRate = BigDecimal.ZERO;
        this.totalTrades = 0;
        this.winningTrades = 0;
        this.losingTrades = 0;
        this.avgResponseTimeMs = 0;
        this.maxConsecutiveWins = 0;
        this.maxConsecutiveLosses = 0;
        this.totalPlayTimeMinutes = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public int getTotalSessions() {
        return totalSessions;
    }
    
    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }
    
    public int getCompletedSessions() {
        return completedSessions;
    }
    
    public void setCompletedSessions(int completedSessions) {
        this.completedSessions = completedSessions;
    }
    
    public BigDecimal getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }
    
    public BigDecimal getBestScore() {
        return bestScore;
    }
    
    public void setBestScore(BigDecimal bestScore) {
        this.bestScore = bestScore;
    }
    
    public BigDecimal getAverageScore() {
        return averageScore;
    }
    
    public void setAverageScore(BigDecimal averageScore) {
        this.averageScore = averageScore;
    }
    
    public BigDecimal getTotalPnl() {
        return totalPnl;
    }
    
    public void setTotalPnl(BigDecimal totalPnl) {
        this.totalPnl = totalPnl;
    }
    
    public BigDecimal getBestPnl() {
        return bestPnl;
    }
    
    public void setBestPnl(BigDecimal bestPnl) {
        this.bestPnl = bestPnl;
    }
    
    public BigDecimal getWorstPnl() {
        return worstPnl;
    }
    
    public void setWorstPnl(BigDecimal worstPnl) {
        this.worstPnl = worstPnl;
    }
    
    public BigDecimal getWinRate() {
        return winRate;
    }
    
    public void setWinRate(BigDecimal winRate) {
        this.winRate = winRate;
    }
    
    public int getTotalTrades() {
        return totalTrades;
    }
    
    public void setTotalTrades(int totalTrades) {
        this.totalTrades = totalTrades;
    }
    
    public int getWinningTrades() {
        return winningTrades;
    }
    
    public void setWinningTrades(int winningTrades) {
        this.winningTrades = winningTrades;
    }
    
    public int getLosingTrades() {
        return losingTrades;
    }
    
    public void setLosingTrades(int losingTrades) {
        this.losingTrades = losingTrades;
    }
    
    public long getAvgResponseTimeMs() {
        return avgResponseTimeMs;
    }
    
    public void setAvgResponseTimeMs(long avgResponseTimeMs) {
        this.avgResponseTimeMs = avgResponseTimeMs;
    }
    
    public int getMaxConsecutiveWins() {
        return maxConsecutiveWins;
    }
    
    public void setMaxConsecutiveWins(int maxConsecutiveWins) {
        this.maxConsecutiveWins = maxConsecutiveWins;
    }
    
    public int getMaxConsecutiveLosses() {
        return maxConsecutiveLosses;
    }
    
    public void setMaxConsecutiveLosses(int maxConsecutiveLosses) {
        this.maxConsecutiveLosses = maxConsecutiveLosses;
    }
    
    public long getTotalPlayTimeMinutes() {
        return totalPlayTimeMinutes;
    }
    
    public void setTotalPlayTimeMinutes(long totalPlayTimeMinutes) {
        this.totalPlayTimeMinutes = totalPlayTimeMinutes;
    }
    
    public Integer getRankPosition() {
        return rankPosition;
    }
    
    public void setRankPosition(Integer rankPosition) {
        this.rankPosition = rankPosition;
    }
    
    public BigDecimal getRankPercentile() {
        return rankPercentile;
    }
    
    public void setRankPercentile(BigDecimal rankPercentile) {
        this.rankPercentile = rankPercentile;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // 业务方法
    public double getCompletionRate() {
        if (totalSessions == 0) return 0.0;
        return (double) completedSessions / totalSessions;
    }
    
    public boolean hasPlayedGames() {
        return totalSessions > 0;
    }
    
    public boolean isExperiencedPlayer() {
        return completedSessions >= 10;
    }
    
    public boolean isTopPerformer() {
        return rankPercentile != null && rankPercentile.compareTo(new BigDecimal("0.1")) <= 0;
    }
    
    @Override
    public String toString() {
        return "UserStats{" +
                "userId='" + userId + '\'' +
                ", totalSessions=" + totalSessions +
                ", completedSessions=" + completedSessions +
                ", totalScore=" + totalScore +
                ", bestScore=" + bestScore +
                ", winRate=" + winRate +
                ", rankPosition=" + rankPosition +
                '}';
    }
}