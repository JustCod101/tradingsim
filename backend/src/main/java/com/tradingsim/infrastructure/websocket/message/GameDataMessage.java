package com.tradingsim.infrastructure.websocket.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * 游戏数据WebSocket消息
 * 用于在WebSocket中传输市场数据和游戏状态信息
 * 
 * @author TradingSim Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameDataMessage {
    
    private String sessionId;
    private Integer frameIndex;
    private String stockCode;
    private BigDecimal price;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;
    private BigDecimal currentBalance;
    private BigDecimal totalPnl;
    private Integer position;
    private BigDecimal unrealizedPnl;
    private BigDecimal realizedPnl;
    private Integer totalTrades;
    private Map<String, Object> indicators;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;
    
    // 默认构造函数
    public GameDataMessage() {}
    
    // 构造函数
    public GameDataMessage(String sessionId, Integer frameIndex) {
        this.sessionId = sessionId;
        this.frameIndex = frameIndex;
        this.timestamp = Instant.now();
    }
    
    // 创建市场数据消息
    public static GameDataMessage marketData(String sessionId, Integer frameIndex, String stockCode, 
                                           BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Long volume) {
        GameDataMessage message = new GameDataMessage(sessionId, frameIndex);
        message.stockCode = stockCode;
        message.open = open;
        message.high = high;
        message.low = low;
        message.close = close;
        message.price = close;
        message.volume = volume;
        return message;
    }
    
    // 创建账户状态消息
    public static GameDataMessage accountStatus(String sessionId, Integer frameIndex, 
                                              BigDecimal currentBalance, BigDecimal totalPnl, 
                                              Integer position, BigDecimal unrealizedPnl, BigDecimal realizedPnl) {
        GameDataMessage message = new GameDataMessage(sessionId, frameIndex);
        message.currentBalance = currentBalance;
        message.totalPnl = totalPnl;
        message.position = position;
        message.unrealizedPnl = unrealizedPnl;
        message.realizedPnl = realizedPnl;
        return message;
    }
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Integer getFrameIndex() {
        return frameIndex;
    }
    
    public void setFrameIndex(Integer frameIndex) {
        this.frameIndex = frameIndex;
    }
    
    public String getStockCode() {
        return stockCode;
    }
    
    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getOpen() {
        return open;
    }
    
    public void setOpen(BigDecimal open) {
        this.open = open;
    }
    
    public BigDecimal getHigh() {
        return high;
    }
    
    public void setHigh(BigDecimal high) {
        this.high = high;
    }
    
    public BigDecimal getLow() {
        return low;
    }
    
    public void setLow(BigDecimal low) {
        this.low = low;
    }
    
    public BigDecimal getClose() {
        return close;
    }
    
    public void setClose(BigDecimal close) {
        this.close = close;
    }
    
    public Long getVolume() {
        return volume;
    }
    
    public void setVolume(Long volume) {
        this.volume = volume;
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
    
    public Integer getPosition() {
        return position;
    }
    
    public void setPosition(Integer position) {
        this.position = position;
    }
    
    public BigDecimal getUnrealizedPnl() {
        return unrealizedPnl;
    }
    
    public void setUnrealizedPnl(BigDecimal unrealizedPnl) {
        this.unrealizedPnl = unrealizedPnl;
    }
    
    public BigDecimal getRealizedPnl() {
        return realizedPnl;
    }
    
    public void setRealizedPnl(BigDecimal realizedPnl) {
        this.realizedPnl = realizedPnl;
    }
    
    public Integer getTotalTrades() {
        return totalTrades;
    }
    
    public void setTotalTrades(Integer totalTrades) {
        this.totalTrades = totalTrades;
    }
    
    public Map<String, Object> getIndicators() {
        return indicators;
    }
    
    public void setIndicators(Map<String, Object> indicators) {
        this.indicators = indicators;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}