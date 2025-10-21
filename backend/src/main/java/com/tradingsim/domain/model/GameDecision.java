package com.tradingsim.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 游戏决策领域模型
 * 
 * 代表用户在关键点做出的交易决策，包含:
 * - 决策基本信息 (ID、类型、价格、数量)
 * - 决策时机信息 (帧索引、时间戳)
 * - 决策结果信息 (盈亏、评分)
 * - 决策性能信息 (响应时间)
 * 
 * DDD 实体，属于 GameSession 聚合
 */
@Entity
@Table(name = "game_decision")
public class GameDecision {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "decision_id")
    private Long decisionId;
    
    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;
    
    @Column(name = "frame_index", nullable = false)
    private Integer frameIndex;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", nullable = false)
    private DecisionType decisionType;
    
    @Column(name = "decision_price", precision = 10, scale = 4)
    private BigDecimal decisionPrice;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;
    
    @Column(name = "pnl", precision = 10, scale = 4)
    private BigDecimal pnl;
    
    @Column(name = "score", precision = 8, scale = 2)
    private BigDecimal score;
    
    @Column(name = "response_time_ms")
    private Long responseTimeMs;
    
    @Column(name = "client_id", length = 64)
    private String clientId;
    
    @Column(name = "decision_timestamp", nullable = false)
    private Instant decisionTimestamp;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    // 默认构造函数 (JPA 需要)
    protected GameDecision() {}
    
    /**
     * 创建新的游戏决策
     * 
     * @param sessionId 会话ID
     * @param frameIndex 帧索引
     * @param decisionType 决策类型
     * @param decisionPrice 决策价格 (SKIP时可为null)
     * @param quantity 数量
     * @param clientId 客户端ID (用于幂等)
     * @param decisionTimestamp 决策时间戳
     */
    public GameDecision(String sessionId, Integer frameIndex, DecisionType decisionType,
                       BigDecimal decisionPrice, Integer quantity, String clientId,
                       Instant decisionTimestamp) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null");
        this.frameIndex = Objects.requireNonNull(frameIndex, "frameIndex cannot be null");
        this.decisionType = Objects.requireNonNull(decisionType, "decisionType cannot be null");
        this.decisionPrice = decisionPrice;
        this.quantity = Objects.requireNonNull(quantity, "quantity cannot be null");
        this.clientId = clientId;
        this.decisionTimestamp = Objects.requireNonNull(decisionTimestamp, "decisionTimestamp cannot be null");
        this.createdAt = Instant.now();
        
        // 业务规则验证
        validateDecision();
    }
    
    /**
     * 验证决策的业务规则
     */
    private void validateDecision() {
        if (frameIndex < 0) {
            throw new IllegalArgumentException("frameIndex must be non-negative");
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        
        // BUY 和 SELL 必须有价格，SKIP 不需要价格
        if ((decisionType == DecisionType.BUY || decisionType == DecisionType.SELL) 
            && decisionPrice == null) {
            throw new IllegalArgumentException("decisionPrice is required for BUY/SELL decisions");
        }
        
        if (decisionType == DecisionType.SKIP && decisionPrice != null) {
            throw new IllegalArgumentException("decisionPrice should be null for SKIP decisions");
        }
        
        if (decisionPrice != null && decisionPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("decisionPrice must be positive");
        }
    }
    
    /**
     * 设置决策结果 (盈亏和评分)
     * 
     * @param pnl 盈亏
     * @param score 评分
     */
    public void setResult(BigDecimal pnl, BigDecimal score) {
        this.pnl = pnl;
        this.score = score;
    }
    
    /**
     * 设置响应时间
     * 
     * @param responseTimeMs 响应时间(毫秒)
     */
    public void setResponseTime(Long responseTimeMs) {
        if (responseTimeMs != null && responseTimeMs < 0) {
            throw new IllegalArgumentException("responseTimeMs must be non-negative");
        }
        this.responseTimeMs = responseTimeMs;
    }
    
    /**
     * 检查是否为买入决策
     */
    public boolean isBuy() {
        return decisionType == DecisionType.BUY;
    }
    
    /**
     * 检查是否为卖出决策
     */
    public boolean isSell() {
        return decisionType == DecisionType.SELL;
    }
    
    /**
     * 检查是否为跳过决策
     */
    public boolean isSkip() {
        return decisionType == DecisionType.SKIP;
    }
    
    /**
     * 检查是否有盈利
     */
    public boolean isProfitable() {
        return pnl != null && pnl.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 检查是否有亏损
     */
    public boolean isLoss() {
        return pnl != null && pnl.compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * 获取决策方向 (1: 买入, -1: 卖出, 0: 跳过)
     */
    public int getDirection() {
        switch (decisionType) {
            case BUY: return 1;
            case SELL: return -1;
            case SKIP: return 0;
            default: return 0;
        }
    }
    
    /**
     * 获取决策金额 (价格 * 数量)
     */
    public BigDecimal getAmount() {
        if (decisionPrice == null) return BigDecimal.ZERO;
        return decisionPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    /**
     * 创建决策的副本 (用于审计)
     */
    public GameDecision copy() {
        GameDecision copy = new GameDecision(sessionId, frameIndex, decisionType, 
                                           decisionPrice, quantity, clientId, decisionTimestamp);
        copy.decisionId = this.decisionId;
        copy.pnl = this.pnl;
        copy.score = this.score;
        copy.responseTimeMs = this.responseTimeMs;
        copy.createdAt = this.createdAt;
        return copy;
    }
    
    // Getters
    public Long getDecisionId() { return decisionId; }
    public String getSessionId() { return sessionId; }
    public Integer getFrameIndex() { return frameIndex; }
    public DecisionType getDecisionType() { return decisionType; }
    public BigDecimal getDecisionPrice() { return decisionPrice; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getPnl() { return pnl; }
    public BigDecimal getScore() { return score; }
    public Long getResponseTimeMs() { return responseTimeMs; }
    public String getClientId() { return clientId; }
    public Instant getDecisionTimestamp() { return decisionTimestamp; }
    public Instant getCreatedAt() { return createdAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameDecision that = (GameDecision) o;
        return Objects.equals(decisionId, that.decisionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(decisionId);
    }
    
    @Override
    public String toString() {
        return "GameDecision{" +
                "decisionId=" + decisionId +
                ", sessionId='" + sessionId + '\'' +
                ", frameIndex=" + frameIndex +
                ", decisionType=" + decisionType +
                ", decisionPrice=" + decisionPrice +
                ", quantity=" + quantity +
                ", pnl=" + pnl +
                ", score=" + score +
                ", responseTimeMs=" + responseTimeMs +
                '}';
    }
}

/**
 * 决策类型枚举
 */
enum DecisionType {
    BUY,    // 买入
    SELL,   // 卖出
    SKIP    // 跳过
}