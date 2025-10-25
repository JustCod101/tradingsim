package com.tradingsim.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 游戏决策领域模型
 * 
 * @author TradingSim Team
 */
@Entity
@Table(name = "game_decision")
public class GameDecision {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", nullable = false)
    private String sessionId;
    
    @Column(name = "frame_index", nullable = false)
    private Integer frameIndex;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", nullable = false)
    private DecisionType decisionType;
    
    @Column(name = "price", precision = 19, scale = 4)
    private BigDecimal price;
    
    @Column(name = "quantity")
    private Integer quantity;
    
    @Column(name = "pnl", precision = 19, scale = 4)
    private BigDecimal pnl;
    
    @Column(name = "cumulative_pnl", precision = 19, scale = 4)
    private BigDecimal cumulativePnl;
    
    @Column(name = "decision_time", nullable = false)
    private Instant decisionTime;
    
    @Column(name = "response_time_ms")
    private Long responseTimeMs;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    public GameDecision() {
        this.createdAt = Instant.now();
    }
    
    public GameDecision(String sessionId, Integer frameIndex, DecisionType decisionType) {
        this.sessionId = sessionId;
        this.frameIndex = frameIndex;
        this.decisionType = decisionType;
        this.decisionTime = Instant.now();
        this.createdAt = Instant.now();
    }
    
    /**
     * 判断是否为交易决策（买入或卖出）
     */
    public boolean isTradingDecision() {
        return decisionType == DecisionType.LONG || decisionType == DecisionType.SHORT;
    }
    
    /**
     * 判断是否为盈利决策
     */
    public boolean isProfitable() {
        return pnl != null && pnl.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 计算决策响应时间（毫秒）
     */
    public void calculateResponseTime(Instant frameStartTime) {
        if (frameStartTime != null && decisionTime != null) {
            this.responseTimeMs = decisionTime.toEpochMilli() - frameStartTime.toEpochMilli();
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public BigDecimal getPnl() {
        return pnl;
    }
    
    public void setPnl(BigDecimal pnl) {
        this.pnl = pnl;
    }
    
    public BigDecimal getCumulativePnl() {
        return cumulativePnl;
    }
    
    public void setCumulativePnl(BigDecimal cumulativePnl) {
        this.cumulativePnl = cumulativePnl;
    }
    
    public Instant getDecisionTime() {
        return decisionTime;
    }
    
    public void setDecisionTime(Instant decisionTime) {
        this.decisionTime = decisionTime;
    }
    
    public Long getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameDecision that = (GameDecision) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "GameDecision{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", frameIndex=" + frameIndex +
                ", decisionType=" + decisionType +
                ", price=" + price +
                ", quantity=" + quantity +
                ", pnl=" + pnl +
                ", cumulativePnl=" + cumulativePnl +
                ", decisionTime=" + decisionTime +
                ", responseTimeMs=" + responseTimeMs +
                ", createdAt=" + createdAt +
                '}';
    }
}