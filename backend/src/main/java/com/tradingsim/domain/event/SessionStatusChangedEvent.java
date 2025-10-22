package com.tradingsim.domain.event;

import com.tradingsim.domain.model.SessionStatus;

/**
 * 会话状态变更事件
 * 
 * @author TradingSim Team
 */
public class SessionStatusChangedEvent extends DomainEvent {
    
    private final SessionStatus oldStatus;
    private final SessionStatus newStatus;
    
    public SessionStatusChangedEvent(String sessionId, SessionStatus oldStatus, SessionStatus newStatus) {
        super(sessionId);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
    
    public SessionStatus getOldStatus() {
        return oldStatus;
    }
    
    public SessionStatus getNewStatus() {
        return newStatus;
    }
    
    @Override
    public String toString() {
        return "SessionStatusChangedEvent{" +
                "sessionId='" + getAggregateId() + '\'' +
                ", oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}