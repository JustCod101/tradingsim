package com.tradingsim.application.dto;

import java.math.BigDecimal;

/**
 * 游戏决策请求DTO
 * 
 * @author TradingSim Team
 */
public class GameDecisionRequest {
    
    private String sessionId;
    private String decisionType;
    private BigDecimal price;
    private Integer quantity;
    private Long responseTimeMs;

    public GameDecisionRequest() {}

    public GameDecisionRequest(String sessionId, String decisionType, BigDecimal price, Integer quantity, Long responseTimeMs) {
        this.sessionId = sessionId;
        this.decisionType = decisionType;
        this.price = price;
        this.quantity = quantity;
        this.responseTimeMs = responseTimeMs;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(String decisionType) {
        this.decisionType = decisionType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
}