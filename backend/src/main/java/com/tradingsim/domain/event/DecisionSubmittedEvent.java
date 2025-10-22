package com.tradingsim.domain.event;

import com.tradingsim.domain.model.DecisionType;

import java.math.BigDecimal;

/**
 * 决策提交事件
 * 
 * @author TradingSim Team
 */
public class DecisionSubmittedEvent extends DomainEvent {
    
    private final Integer frameIndex;
    private final DecisionType decisionType;
    private final BigDecimal price;
    private final Integer quantity;
    private final Long responseTimeMs;
    
    public DecisionSubmittedEvent(String sessionId, Integer frameIndex, DecisionType decisionType, 
                                 BigDecimal price, Integer quantity, Long responseTimeMs) {
        super(sessionId);
        this.frameIndex = frameIndex;
        this.decisionType = decisionType;
        this.price = price;
        this.quantity = quantity;
        this.responseTimeMs = responseTimeMs;
    }
    
    public Integer getFrameIndex() {
        return frameIndex;
    }
    
    public DecisionType getDecisionType() {
        return decisionType;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public Long getResponseTimeMs() {
        return responseTimeMs;
    }
    
    @Override
    public String toString() {
        return "DecisionSubmittedEvent{" +
                "sessionId='" + getAggregateId() + '\'' +
                ", frameIndex=" + frameIndex +
                ", decisionType=" + decisionType +
                ", price=" + price +
                ", quantity=" + quantity +
                ", responseTimeMs=" + responseTimeMs +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}