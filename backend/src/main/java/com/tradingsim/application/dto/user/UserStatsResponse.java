package com.tradingsim.application.dto.user;

import com.tradingsim.domain.model.user.UserStats;

import java.math.BigDecimal;

/**
 * 用户统计信息响应DTO
 * 
 * @author TradingSim Team
 */
public class UserStatsResponse {
    
    private String userId;
    private int totalSessions;
    private int completedSessions;
    private double completionRate;
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
    
    // 构造函数
    public UserStatsResponse() {}
    
    public UserStatsResponse(UserStats stats) {
        this.userId = stats.getUserId();
        this.totalSessions = stats.getTotalSessions();
        this.completedSessions = stats.getCompletedSessions();
        this.completionRate = stats.getCompletionRate();
        this.totalScore = stats.getTotalScore();
        this.bestScore = stats.getBestScore();
        this.averageScore = stats.getAverageScore();
        this.totalPnl = stats.getTotalPnl();
        this.bestPnl = stats.getBestPnl();
        this.worstPnl = stats.getWorstPnl();
        this.winRate = stats.getWinRate();
        this.totalTrades = stats.getTotalTrades();
        this.winningTrades = stats.getWinningTrades();
        this.losingTrades = stats.getLosingTrades();
        this.avgResponseTimeMs = stats.getAvgResponseTimeMs();
        this.maxConsecutiveWins = stats.getMaxConsecutiveWins();
        this.maxConsecutiveLosses = stats.getMaxConsecutiveLosses();
        this.totalPlayTimeMinutes = stats.getTotalPlayTimeMinutes();
        this.rankPosition = stats.getRankPosition();
        this.rankPercentile = stats.getRankPercentile();
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
    
    public double getCompletionRate() {
        return completionRate;
    }
    
    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
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
}