package com.tradingsim.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tradingsim.domain.model.GameDecision;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易决策响应DTO
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Schema(description = "交易决策响应")
public class DecisionResponse {
    
    @Schema(description = "决策ID", example = "decision_1640995200_abc123")
    private String decisionId;
    
    @Schema(description = "会话ID", example = "session_1640995200_abc123")
    private String sessionId;
    
    @Schema(description = "帧索引", example = "150")
    private int frameIndex;
    
    @Schema(description = "决策类型", example = "BUY")
    private String decisionType;
    
    @Schema(description = "交易价格", example = "150.25")
    private BigDecimal price;
    
    @Schema(description = "交易数量", example = "100")
    private int quantity;
    
    @Schema(description = "盈亏", example = "125.50")
    private BigDecimal pnl;
    
    @Schema(description = "得分", example = "8.5")
    private double score;
    
    @Schema(description = "响应时间(毫秒)", example = "1250")
    private long responseTime;
    
    @Schema(description = "客户端ID", example = "web_client_001")
    private String clientId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;
    
    // 默认构造函数
    public DecisionResponse() {}
    
    /**
     * 从领域模型转换为DTO
     */
    public static DecisionResponse fromDomain(GameDecision decision) {
        DecisionResponse response = new DecisionResponse();
        response.setDecisionId(decision.getDecisionId());
        response.setSessionId(decision.getSessionId());
        response.setFrameIndex(decision.getFrameIndex());
        response.setDecisionType(decision.getDecisionType().name());
        response.setPrice(decision.getPrice());
        response.setQuantity(decision.getQuantity());
        response.setPnl(decision.getPnl());
        response.setScore(decision.getScore());
        response.setResponseTime(decision.getResponseTime());
        response.setClientId(decision.getClientId());
        response.setCreatedAt(decision.getCreatedAt());
        return response;
    }
    
    // Getters and Setters
    public String getDecisionId() {
        return decisionId;
    }
    
    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public int getFrameIndex() {
        return frameIndex;
    }
    
    public void setFrameIndex(int frameIndex) {
        this.frameIndex = frameIndex;
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
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getPnl() {
        return pnl;
    }
    
    public void setPnl(BigDecimal pnl) {
        this.pnl = pnl;
    }
    
    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = score;
    }
    
    public long getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}