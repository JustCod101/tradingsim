package com.tradingsim.infrastructure.websocket.message;

import com.tradingsim.domain.model.GameSession;
import com.tradingsim.domain.model.SessionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 游戏会话WebSocket消息
 * 用于在WebSocket中传输游戏会话状态信息
 * 
 * @author TradingSim Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameSessionMessage {
    
    private String sessionId;
    private String stockCode;
    private String timeframe;
    private String status;
    private Integer currentFrameIndex;
    private Integer totalFrames;
    private BigDecimal initialBalance;
    private BigDecimal currentBalance;
    private BigDecimal totalPnl;
    private BigDecimal maxDrawdown;
    private BigDecimal winRate;
    private Integer totalTrades;
    private Integer winningTrades;
    private Integer losingTrades;
    private BigDecimal score;
    private SessionStatus oldStatus;
    private SessionStatus newStatus;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant startTime;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant endTime;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
    
    // 默认构造函数
    public GameSessionMessage() {}
    
    // 从GameSession创建消息
    public static GameSessionMessage fromGameSession(GameSession session) {
        GameSessionMessage message = new GameSessionMessage();
        message.sessionId = session.getId();
        message.stockCode = session.getStockCode();
        message.timeframe = session.getTimeframe();
        message.status = session.getStatus().name();
        message.currentFrameIndex = session.getCurrentFrameIndex();
        message.totalFrames = session.getTotalFrames();
        message.initialBalance = session.getInitialBalance();
        message.currentBalance = session.getCurrentBalance();
        message.totalPnl = session.getTotalPnl();
        message.maxDrawdown = session.getMaxDrawdown();
        message.winRate = session.getWinRate();
        message.totalTrades = session.getTotalTrades();
        message.winningTrades = session.getWinningTrades();
        message.losingTrades = session.getLosingTrades();
        message.score = session.getScore();
        message.startTime = session.getStartTime();
        message.endTime = session.getEndTime();
        message.createdAt = session.getCreatedAt();
        message.updatedAt = session.getUpdatedAt();
        return message;
    }
    
    // 创建状态变更消息
    public static GameSessionMessage statusChange(GameSession session, SessionStatus oldStatus, SessionStatus newStatus) {
        GameSessionMessage message = fromGameSession(session);
        message.oldStatus = oldStatus;
        message.newStatus = newStatus;
        return message;
    }
    
    // Getters and Setters
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
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
    
    public Integer getWinningTrades() {
        return winningTrades;
    }
    
    public void setWinningTrades(Integer winningTrades) {
        this.winningTrades = winningTrades;
    }
    
    public Integer getLosingTrades() {
        return losingTrades;
    }
    
    public void setLosingTrades(Integer losingTrades) {
        this.losingTrades = losingTrades;
    }
    
    public BigDecimal getScore() {
        return score;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
    public SessionStatus getOldStatus() {
        return oldStatus;
    }
    
    public void setOldStatus(SessionStatus oldStatus) {
        this.oldStatus = oldStatus;
    }
    
    public SessionStatus getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(SessionStatus newStatus) {
        this.newStatus = newStatus;
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
}