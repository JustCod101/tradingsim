package com.tradingsim.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件基类
 * 
 * @author TradingSim Team
 */
public abstract class DomainEvent {
    
    private final String eventId;
    private final Instant occurredAt;
    private final String aggregateId;
    private final String eventType;
    
    protected DomainEvent(String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
        this.aggregateId = aggregateId;
        this.eventType = this.getClass().getSimpleName();
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public Instant getOccurredAt() {
        return occurredAt;
    }
    
    public String getAggregateId() {
        return aggregateId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    @Override
    public String toString() {
        return "DomainEvent{" +
                "eventId='" + eventId + '\'' +
                ", occurredAt=" + occurredAt +
                ", aggregateId='" + aggregateId + '\'' +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}