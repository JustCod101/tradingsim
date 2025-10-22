package com.tradingsim.application.dto;

import com.tradingsim.domain.model.SessionStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 游戏会话响应DTO
 * 
 * @author TradingSim Team
 */
public class SessionResponse {
    
    private String sessionId;
    private String stockCode;
    private String timeframe;
    private Instant startTime;
    private Instant endTime;
    private SessionStatus status;
    private Integer currentFrameIndex;
    private Integer totalFrames;
    private BigDecimal initialBalance;
    private BigDecimal currentBalance;
    private BigDecimal totalPnl;
    private BigDecimal maxDrawdown;
    private BigDecimal winRate;
    private Integer totalTrades;
    private Integer profitableTrades;
    private Integer losingTrades;
    private Integer score;
    private Instant createdAt;
    private Instant updatedAt;
    
    public SessionResponse() {}
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getStockCode() {
        return stockCode;
    }
    
    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }
    
    public String getTimeframe() {
        return timeframe;
    }
    
    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }
    
    public Instant getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
    
    public SessionStatus getStatus() {
        return status;
    }
    
    public void setStatus(SessionStatus status) {
        this.status = status;
    }
    
    public Integer getCurrentFrameIndex() {
        return currentFrameIndex;
    }
    
    public void setCurrentFrameIndex(Integer currentFrameIndex) {
        this.currentFrameIndex = currentFrameIndex;
    }
    
    public Integer getTotalFrames() {
        return totalFrames;
    }
    
    public void setTotalFrames(Integer totalFrames) {
        this.totalFrames = totalFrames;
    }
    
    public BigDecimal getInitialBalance() {
        return initialBalance;
    }
    
    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
    
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }
    
    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }
    
    public BigDecimal getTotalPnl() {
        return totalPnl;
    }
    
    public void setTotalPnl(BigDecimal totalPnl) {
        this.totalPnl = totalPnl;
    }
    
    public BigDecimal getMaxDrawdown() {
        return maxDrawdown;
    }
    
    public void setMaxDrawdown(BigDecimal maxDrawdown) {
        this.maxDrawdown = maxDrawdown;
    }
    
    public BigDecimal getWinRate() {
        return winRate;
    }
    
    public void setWinRate(BigDecimal winRate) {
        this.winRate = winRate;
    }
    
    public Integer getTotalTrades() {
        return totalTrades;
    }
    
    public void setTotalTrades(Integer totalTrades) {
        this.totalTrades = totalTrades;
    }
    
    public Integer getProfitableTrades() {
        return profitableTrades;
    }
    
    public void setProfitableTrades(Integer profitableTrades) {
        this.profitableTrades = profitableTrades;
    }
    
    public Integer getLosingTrades() {
        return losingTrades;
    }
    
    public void setLosingTrades(Integer losingTrades) {
        this.losingTrades = losingTrades;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
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
    
    /**
     * 计算进度百分比
     */
    public Double getProgressPercentage() {
        if (totalFrames == null || totalFrames == 0 || currentFrameIndex == null) {
            return 0.0;
        }
        return (double) currentFrameIndex / totalFrames * 100.0;
    }
    
    /**
     * 判断会话是否已完成
     */
    public boolean isCompleted() {
        return status == SessionStatus.COMPLETED || status == SessionStatus.CANCELLED;
    }
    
    @Override
    public String toString() {
        return "SessionResponse{" +
                "sessionId='" + sessionId + '\'' +
                ", stockCode='" + stockCode + '\'' +
                ", status=" + status +
                ", currentFrameIndex=" + currentFrameIndex +
                ", totalFrames=" + totalFrames +
                ", currentBalance=" + currentBalance +
                ", totalPnl=" + totalPnl +
                '}';
    }
}