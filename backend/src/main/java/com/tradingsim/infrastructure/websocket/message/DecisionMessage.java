package com.tradingsim.infrastructure.websocket.message;

import com.tradingsim.domain.model.DecisionType;
import com.tradingsim.domain.model.GameDecision;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 决策WebSocket消息
 * 用于在WebSocket中传输用户决策和决策结果
 * 
 * @author TradingSim Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecisionMessage {
    
    private String sessionId;
    private Integer frameIndex;
    private String decisionType;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal executedPrice;
    private BigDecimal executedQuantity;
    private String status;
    private String reason;
    private BigDecimal pnl;
    private BigDecimal commission;
    private Long responseTimeMs;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant executedAt;
    
    // 默认构造函数
    public DecisionMessage() {}
    
    // 构造函数
    public DecisionMessage(String sessionId, Integer frameIndex, DecisionType decisionType) {
        this.sessionId = sessionId;
        this.frameIndex = frameIndex;
        this.decisionType = decisionType.name();
        this.timestamp = Instant.now();
    }
    
    // 从GameDecision创建消息
    public static DecisionMessage fromGameDecision(GameDecision decision) {
        DecisionMessage message = new DecisionMessage();
        message.sessionId = decision.getSessionId();
        message.frameIndex = decision.getFrameIndex();
        message.decisionType = decision.getDecisionType().name();
        message.quantity = decision.getQuantity() != null ? BigDecimal.valueOf(decision.getQuantity()) : null;
        message.price = decision.getPrice();
        message.pnl = decision.getPnl();
        message.responseTimeMs = decision.getResponseTimeMs();
        message.timestamp = decision.getCreatedAt();
        message.executedAt = decision.getDecisionTime();
        message.status = "EXECUTED"; // 默认状态
        return message;
    }
    
    // 创建决策提交消息
    public static DecisionMessage submitted(String sessionId, Integer frameIndex, DecisionType decisionType, 
                                          BigDecimal quantity, BigDecimal price) {
        DecisionMessage message = new DecisionMessage(sessionId, frameIndex, decisionType);
        message.quantity = quantity;
        message.price = price;
        message.status = "SUBMITTED";
        return message;
    }
    
    // 创建决策执行消息
    public static DecisionMessage executed(GameDecision decision) {
        DecisionMessage message = fromGameDecision(decision);
        message.status = "EXECUTED";
        message.executedAt = Instant.now();
        return message;
    }
    
    // 创建决策拒绝消息
    public static DecisionMessage rejected(String sessionId, Integer frameIndex, DecisionType decisionType, 
                                         String reason) {
        DecisionMessage message = new DecisionMessage(sessionId, frameIndex, decisionType);
        message.status = "REJECTED";
        message.reason = reason;
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
    
    public String getDecisionType() {
        return decisionType;
    }
    
    public void setDecisionType(String decisionType) {
        this.decisionType = decisionType;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getExecutedPrice() {
        return executedPrice;
    }
    
    public void setExecutedPrice(BigDecimal executedPrice) {
        this.executedPrice = executedPrice;
    }
    
    public BigDecimal getExecutedQuantity() {
        return executedQuantity;
    }
    
    public void setExecutedQuantity(BigDecimal executedQuantity) {
        this.executedQuantity = executedQuantity;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public BigDecimal getPnl() {
        return pnl;
    }
    
    public void setPnl(BigDecimal pnl) {
        this.pnl = pnl;
    }
    
    public BigDecimal getCommission() {
        return commission;
    }
    
    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }
    
    public Long getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public Instant getExecutedAt() {
        return executedAt;
    }
    
    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
    }
}