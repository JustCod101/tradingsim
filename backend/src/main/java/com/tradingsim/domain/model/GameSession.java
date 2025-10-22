package com.tradingsim.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 游戏会话领域模型
 * 
 * @author TradingSim Team
 */
@Entity
@Table(name = "game_session")
public class GameSession {
    
    @Id
    private String id;
    
    @Column(name = "stock_code", nullable = false)
    private String stockCode;
    
    @Column(name = "timeframe", nullable = false)
    private String timeframe;
    
    @Column(name = "start_time")
    private Instant startTime;
    
    @Column(name = "end_time")
    private Instant endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status;
    
    @Column(name = "current_frame_index")
    private Integer currentFrameIndex;
    
    @Column(name = "total_frames")
    private Integer totalFrames;
    
    @Column(name = "initial_balance", precision = 19, scale = 4)
    private BigDecimal initialBalance;
    
    @Column(name = "current_balance", precision = 19, scale = 4)
    private BigDecimal currentBalance;
    
    @Column(name = "total_pnl", precision = 19, scale = 4)
    private BigDecimal totalPnl;
    
    @Column(name = "max_drawdown", precision = 19, scale = 4)
    private BigDecimal maxDrawdown;
    
    @Column(name = "win_rate", precision = 5, scale = 4)
    private BigDecimal winRate;
    
    @Column(name = "total_trades")
    private Integer totalTrades;
    
    @Column(name = "winning_trades")
    private Integer winningTrades;
    
    @Column(name = "losing_trades")
    private Integer losingTrades;
    
    @Column(name = "score", precision = 19, scale = 4)
    private BigDecimal score;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @OneToMany(mappedBy = "sessionId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GameDecision> decisions = new ArrayList<>();
    
    public GameSession() {
        this.status = SessionStatus.CREATED;
        this.currentFrameIndex = 0;
        this.totalTrades = 0;
        this.winningTrades = 0;
        this.losingTrades = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public GameSession(String id, String stockCode, String timeframe, BigDecimal initialBalance) {
        this();
        this.id = id;
        this.stockCode = stockCode;
        this.timeframe = timeframe;
        this.initialBalance = initialBalance;
        this.currentBalance = initialBalance;
        this.totalPnl = BigDecimal.ZERO;
        this.maxDrawdown = BigDecimal.ZERO;
        this.winRate = BigDecimal.ZERO;
        this.score = BigDecimal.ZERO;
    }
    
    /**
     * 开始游戏会话
     */
    public void start() {
        if (status != SessionStatus.CREATED && status != SessionStatus.PAUSED) {
            throw new IllegalStateException("Cannot start session in status: " + status);
        }
        this.status = SessionStatus.RUNNING;
        if (startTime == null) {
            this.startTime = Instant.now();
        }
        this.updatedAt = Instant.now();
    }
    
    /**
     * 暂停游戏会话
     */
    public void pause() {
        if (status != SessionStatus.RUNNING) {
            throw new IllegalStateException("Cannot pause session in status: " + status);
        }
        this.status = SessionStatus.PAUSED;
        this.updatedAt = Instant.now();
    }
    
    /**
     * 完成游戏会话
     */
    public void complete() {
        if (status != SessionStatus.RUNNING && status != SessionStatus.PAUSED) {
            throw new IllegalStateException("Cannot complete session in status: " + status);
        }
        this.status = SessionStatus.COMPLETED;
        this.endTime = Instant.now();
        this.updatedAt = Instant.now();
        calculateFinalStats();
    }
    
    /**
     * 取消游戏会话
     */
    public void cancel() {
        if (status == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed session");
        }
        this.status = SessionStatus.CANCELLED;
        this.endTime = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    /**
     * 添加决策
     */
    public void addDecision(GameDecision decision) {
        decisions.add(decision);
        if (decision.isTradingDecision()) {
            totalTrades++;
            if (decision.isProfitable()) {
                winningTrades++;
            } else {
                losingTrades++;
            }
            updateBalance(decision.getPnl());
        }
        this.updatedAt = Instant.now();
    }
    
    /**
     * 更新余额
     */
    private void updateBalance(BigDecimal pnl) {
        if (pnl != null) {
            currentBalance = currentBalance.add(pnl);
            totalPnl = totalPnl.add(pnl);
            
            // 计算最大回撤
            BigDecimal drawdown = initialBalance.subtract(currentBalance);
            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
            }
        }
    }
    
    /**
     * 计算最终统计数据
     */
    private void calculateFinalStats() {
        if (totalTrades > 0) {
            winRate = BigDecimal.valueOf(winningTrades)
                    .divide(BigDecimal.valueOf(totalTrades), 4, BigDecimal.ROUND_HALF_UP);
        }
        
        // 计算综合得分（可以根据需要调整算法）
        BigDecimal returnRate = totalPnl.divide(initialBalance, 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal drawdownPenalty = maxDrawdown.divide(initialBalance, 4, BigDecimal.ROUND_HALF_UP);
        score = returnRate.subtract(drawdownPenalty).multiply(winRate);
    }
    
    /**
     * 推进到下一帧
     */
    public void nextFrame() {
        if (status == SessionStatus.RUNNING) {
            currentFrameIndex++;
            this.updatedAt = Instant.now();
        }
    }
    
    /**
     * 判断是否已完成所有帧
     */
    public boolean isCompleted() {
        return totalFrames != null && currentFrameIndex >= totalFrames;
    }
    
    /**
     * 获取进度百分比
     */
    public BigDecimal getProgress() {
        if (totalFrames == null || totalFrames == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(currentFrameIndex)
                .divide(BigDecimal.valueOf(totalFrames), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getStockCode() {
        return stockCode;
    }
    
    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }
    
    public String getTimeframe() {
        return timeframe;
    }
    
    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }
    
    public Instant getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
    
    public SessionStatus getStatus() {
        return status;
    }
    
    public void setStatus(SessionStatus status) {
        this.status = status;
    }
    
    public Integer getCurrentFrameIndex() {
        return currentFrameIndex;
    }
    
    public void setCurrentFrameIndex(Integer currentFrameIndex) {
        this.currentFrameIndex = currentFrameIndex;
    }
    
    public Integer getTotalFrames() {
        return totalFrames;
    }
    
    public void setTotalFrames(Integer totalFrames) {
        this.totalFrames = totalFrames;
    }
    
    public BigDecimal getInitialBalance() {
        return initialBalance;
    }
    
    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
    
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }
    
    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }
    
    public BigDecimal getTotalPnl() {
        return totalPnl;
    }
    
    public void setTotalPnl(BigDecimal totalPnl) {
        this.totalPnl = totalPnl;
    }
    
    public BigDecimal getMaxDrawdown() {
        return maxDrawdown;
    }
    
    public void setMaxDrawdown(BigDecimal maxDrawdown) {
        this.maxDrawdown = maxDrawdown;
    }
    
    public BigDecimal getWinRate() {
        return winRate;
    }
    
    public void setWinRate(BigDecimal winRate) {
        this.winRate = winRate;
    }
    
    public Integer getTotalTrades() {
        return totalTrades;
    }
    
    public void setTotalTrades(Integer totalTrades) {
        this.totalTrades = totalTrades;
    }
    
    public Integer getWinningTrades() {
        return winningTrades;
    }
    
    public void setWinningTrades(Integer winningTrades) {
        this.winningTrades = winningTrades;
    }
    
    public Integer getLosingTrades() {
        return losingTrades;
    }
    
    public void setLosingTrades(Integer losingTrades) {
        this.losingTrades = losingTrades;
    }
    
    public BigDecimal getScore() {
        return score;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<GameDecision> getDecisions() {
        return decisions;
    }
    
    public void setDecisions(List<GameDecision> decisions) {
        this.decisions = decisions;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameSession that = (GameSession) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "GameSession{" +
                "id='" + id + '\'' +
                ", stockCode='" + stockCode + '\'' +
                ", timeframe='" + timeframe + '\'' +
                ", status=" + status +
                ", currentFrameIndex=" + currentFrameIndex +
                ", totalFrames=" + totalFrames +
                ", totalPnl=" + totalPnl +
                ", score=" + score +
                ", createdAt=" + createdAt +
                '}';
    }
}