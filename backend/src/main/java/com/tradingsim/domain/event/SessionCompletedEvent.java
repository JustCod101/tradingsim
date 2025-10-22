package com.tradingsim.domain.event;

import java.math.BigDecimal;

/**
 * 会话完成事件
 * 
 * @author TradingSim Team
 */
public class SessionCompletedEvent extends DomainEvent {
    
    private final BigDecimal finalBalance;
    private final BigDecimal totalPnl;
    private final BigDecimal maxDrawdown;
    private final BigDecimal winRate;
    private final Integer totalTrades;
    private final Integer score;
    
    public SessionCompletedEvent(String sessionId, BigDecimal finalBalance, BigDecimal totalPnl, 
                                BigDecimal maxDrawdown, BigDecimal winRate, Integer totalTrades, Integer score) {
        super(sessionId);
        this.finalBalance = finalBalance;
        this.totalPnl = totalPnl;
        this.maxDrawdown = maxDrawdown;
        this.winRate = winRate;
        this.totalTrades = totalTrades;
        this.score = score;
    }
    
    public BigDecimal getFinalBalance() {
        return finalBalance;
    }
    
    public BigDecimal getTotalPnl() {
        return totalPnl;
    }
    
    public BigDecimal getMaxDrawdown() {
        return maxDrawdown;
    }
    
    public BigDecimal getWinRate() {
        return winRate;
    }
    
    public Integer getTotalTrades() {
        return totalTrades;
    }
    
    public Integer getScore() {
        return score;
    }
    
    @Override
    public String toString() {
        return "SessionCompletedEvent{" +
                "sessionId='" + getAggregateId() + '\'' +
                ", finalBalance=" + finalBalance +
                ", totalPnl=" + totalPnl +
                ", maxDrawdown=" + maxDrawdown +
                ", winRate=" + winRate +
                ", totalTrades=" + totalTrades +
                ", score=" + score +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}