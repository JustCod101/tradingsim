package com.tradingsim.application.dto;

import com.tradingsim.domain.model.DecisionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * 提交决策请求DTO
 * 
 * @author TradingSim Team
 */
public class SubmitDecisionRequest {
    
    @NotNull(message = "会话ID不能为空")
    private String sessionId;
    
    @NotNull(message = "帧索引不能为空")
    @PositiveOrZero(message = "帧索引不能为负数")
    private Integer frameIndex;
    
    @NotNull(message = "决策类型不能为空")
    private DecisionType decisionType;
    
    private BigDecimal price;
    
    @PositiveOrZero(message = "数量不能为负数")
    private Integer quantity;
    
    @PositiveOrZero(message = "响应时间不能为负数")
    private Long responseTimeMs;
    
    public SubmitDecisionRequest() {}
    
    public SubmitDecisionRequest(String sessionId, Integer frameIndex, DecisionType decisionType, 
                                BigDecimal price, Integer quantity, Long responseTimeMs) {
        this.sessionId = sessionId;
        this.frameIndex = frameIndex;
        this.decisionType = decisionType;
        this.price = price;
        this.quantity = quantity;
        this.responseTimeMs = responseTimeMs;
    }
    
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
    
    public DecisionType getDecisionType() {
        return decisionType;
    }
    
    public void setDecisionType(DecisionType decisionType) {
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
    
    @Override
    public String toString() {
        return "SubmitDecisionRequest{" +
                "sessionId='" + sessionId + '\'' +
                ", frameIndex=" + frameIndex +
                ", decisionType=" + decisionType +
                ", price=" + price +
                ", quantity=" + quantity +
                ", responseTimeMs=" + responseTimeMs +
                '}';
    }
}