package com.tradingsim.domain.event;

import java.math.BigDecimal;

/**
 * 会话创建事件
 * 
 * @author TradingSim Team
 */
public class SessionCreatedEvent extends DomainEvent {
    
    private final String stockCode;
    private final String timeframe;
    private final BigDecimal initialBalance;
    
    public SessionCreatedEvent(String sessionId, String stockCode, String timeframe, BigDecimal initialBalance) {
        super(sessionId);
        this.stockCode = stockCode;
        this.timeframe = timeframe;
        this.initialBalance = initialBalance;
    }
    
    public String getStockCode() {
        return stockCode;
    }
    
    public String getTimeframe() {
        return timeframe;
    }
    
    public BigDecimal getInitialBalance() {
        return initialBalance;
    }
    
    @Override
    public String toString() {
        return "SessionCreatedEvent{" +
                "sessionId='" + getAggregateId() + '\'' +
                ", stockCode='" + stockCode + '\'' +
                ", timeframe='" + timeframe + '\'' +
                ", initialBalance=" + initialBalance +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}