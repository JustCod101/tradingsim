package com.tradingsim.application.dto.user;

import java.math.BigDecimal;

/**
 * 用户排名响应DTO
 */
public class UserRanking {
    private String userId;
    private String username;
    private String avatarUrl;
    private BigDecimal totalScore;
    private BigDecimal totalPnl;
    private BigDecimal winRate;
    private Integer rankPosition;

    public UserRanking() {}

    public UserRanking(String userId, String username, String avatarUrl, 
                      BigDecimal totalScore, BigDecimal totalPnl, BigDecimal winRate, Integer rankPosition) {
        this.userId = userId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.totalScore = totalScore;
        this.totalPnl = totalPnl;
        this.winRate = winRate;
        this.rankPosition = rankPosition;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public BigDecimal getTotalPnl() {
        return totalPnl;
    }

    public void setTotalPnl(BigDecimal totalPnl) {
        this.totalPnl = totalPnl;
    }

    public BigDecimal getWinRate() {
        return winRate;
    }

    public void setWinRate(BigDecimal winRate) {
        this.winRate = winRate;
    }

    public Integer getRankPosition() {
        return rankPosition;
    }

    public void setRankPosition(Integer rankPosition) {
        this.rankPosition = rankPosition;
    }
}