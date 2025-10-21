package com.tradingsim.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 游戏会话领域模型
 * 
 * 代表一次完整的交易模拟会话，包含:
 * - 会话基本信息 (ID、状态、时间)
 * - 关联的游戏段信息
 * - 会话统计数据
 * - 决策记录列表
 * 
 * DDD 聚合根，负责维护会话的业务不变性
 */
@Entity
@Table(name = "game_session")
public class GameSession {
    
    @Id
    @Column(name = "session_id", length = 64)
    private String sessionId;
    
    @Column(name = "segment_id", nullable = false)
    private Long segmentId;
    
    @Column(name = "user_id", length = 64)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status;
    
    @Column(name = "current_frame", nullable = false)
    private Integer currentFrame = 0;
    
    @Column(name = "total_frames", nullable = false)
    private Integer totalFrames;
    
    @Column(name = "total_score")
    private Double totalScore = 0.0;
    
    @Column(name = "total_pnl")
    private Double totalPnl = 0.0;
    
    @Column(name = "decision_count", nullable = false)
    private Integer decisionCount = 0;
    
    @Column(name = "timeout_count", nullable = false)
    private Integer timeoutCount = 0;
    
    @Column(name = "avg_response_time_ms")
    private Long avgResponseTimeMs;
    
    @Column(name = "seed_value", nullable = false)
    private Long seedValue;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "started_at")
    private Instant startedAt;
    
    @Column(name = "completed_at")
    private Instant completedAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    // 关联的决策记录 (懒加载)
    @OneToMany(mappedBy = "sessionId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GameDecision> decisions = new ArrayList<>();
    
    // 默认构造函数 (JPA 需要)
    protected GameSession() {}
    
    /**
     * 创建新的游戏会话
     * 
     * @param sessionId 会话ID
     * @param segmentId 游戏段ID
     * @param userId 用户ID
     * @param totalFrames 总帧数
     * @param seedValue 随机种子值
     */
    public GameSession(String sessionId, Long segmentId, String userId, 
                      Integer totalFrames, Long seedValue) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null");
        this.segmentId = Objects.requireNonNull(segmentId, "segmentId cannot be null");
        this.userId = userId;
        this.totalFrames = Objects.requireNonNull(totalFrames, "totalFrames cannot be null");
        this.seedValue = Objects.requireNonNull(seedValue, "seedValue cannot be null");
        this.status = SessionStatus.CREATED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        
        // 业务规则验证
        if (totalFrames <= 0) {
            throw new IllegalArgumentException("totalFrames must be positive");
        }
    }
    
    /**
     * 启动会话
     */
    public void start() {
        if (this.status != SessionStatus.CREATED) {
            throw new IllegalStateException("Session can only be started from CREATED status");
        }
        
        this.status = SessionStatus.RUNNING;
        this.startedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    /**
     * 暂停会话 (等待决策)
     */
    public void pause() {
        if (this.status != SessionStatus.RUNNING) {
            throw new IllegalStateException("Session can only be paused from RUNNING status");
        }
        
        this.status = SessionStatus.PAUSED;
        this.updatedAt = Instant.now();
    }
    
    /**
     * 恢复会话 (从暂停状态)
     */
    public void resume() {
        if (this.status != SessionStatus.PAUSED) {
            throw new IllegalStateException("Session can only be resumed from PAUSED status");
        }
        
        this.status = SessionStatus.RUNNING;
        this.updatedAt = Instant.now();
    }
    
    /**
     * 完成会话
     */
    public void complete() {
        if (this.status != SessionStatus.RUNNING && this.status != SessionStatus.PAUSED) {
            throw new IllegalStateException("Session can only be completed from RUNNING or PAUSED status");
        }
        
        this.status = SessionStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    /**
     * 取消会话
     */
    public void cancel() {
        if (this.status == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed session");
        }
        
        this.status = SessionStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }
    
    /**
     * 推进到下一帧
     */
    public void advanceFrame() {
        if (this.status != SessionStatus.RUNNING) {
            throw new IllegalStateException("Can only advance frame when session is running");
        }
        
        if (this.currentFrame >= this.totalFrames) {
            throw new IllegalStateException("Cannot advance beyond total frames");
        }
        
        this.currentFrame++;
        this.updatedAt = Instant.now();
        
        // 如果到达最后一帧，自动完成会话
        if (this.currentFrame >= this.totalFrames) {
            complete();
        }
    }
    
    /**
     * 添加决策记录
     */
    public void addDecision(GameDecision decision) {
        Objects.requireNonNull(decision, "decision cannot be null");
        
        if (!this.sessionId.equals(decision.getSessionId())) {
            throw new IllegalArgumentException("Decision sessionId does not match");
        }
        
        this.decisions.add(decision);
        this.decisionCount++;
        this.updatedAt = Instant.now();
        
        // 更新平均响应时间
        updateAverageResponseTime(decision.getResponseTimeMs());
    }
    
    /**
     * 记录超时
     */
    public void recordTimeout() {
        this.timeoutCount++;
        this.updatedAt = Instant.now();
    }
    
    /**
     * 更新总分数
     */
    public void updateScore(double scoreIncrement) {
        this.totalScore += scoreIncrement;
        this.updatedAt = Instant.now();
    }
    
    /**
     * 更新总盈亏
     */
    public void updatePnl(double pnlIncrement) {
        this.totalPnl += pnlIncrement;
        this.updatedAt = Instant.now();
    }
    
    /**
     * 更新平均响应时间
     */
    private void updateAverageResponseTime(Long responseTimeMs) {
        if (responseTimeMs == null) return;
        
        if (this.avgResponseTimeMs == null) {
            this.avgResponseTimeMs = responseTimeMs;
        } else {
            // 计算移动平均值
            this.avgResponseTimeMs = (this.avgResponseTimeMs * (this.decisionCount - 1) + responseTimeMs) / this.decisionCount;
        }
    }
    
    /**
     * 检查会话是否已完成
     */
    public boolean isCompleted() {
        return this.status == SessionStatus.COMPLETED;
    }
    
    /**
     * 检查会话是否正在运行
     */
    public boolean isRunning() {
        return this.status == SessionStatus.RUNNING;
    }
    
    /**
     * 检查会话是否已暂停
     */
    public boolean isPaused() {
        return this.status == SessionStatus.PAUSED;
    }
    
    /**
     * 获取会话进度百分比
     */
    public double getProgress() {
        if (totalFrames == 0) return 0.0;
        return (double) currentFrame / totalFrames * 100.0;
    }
    
    /**
     * 获取胜率
     */
    public double getWinRate() {
        if (decisionCount == 0) return 0.0;
        
        long winCount = decisions.stream()
            .filter(d -> d.getPnl() != null && d.getPnl() > 0)
            .count();
        
        return (double) winCount / decisionCount;
    }
    
    // Getters
    public String getSessionId() { return sessionId; }
    public Long getSegmentId() { return segmentId; }
    public String getUserId() { return userId; }
    public SessionStatus getStatus() { return status; }
    public Integer getCurrentFrame() { return currentFrame; }
    public Integer getTotalFrames() { return totalFrames; }
    public Double getTotalScore() { return totalScore; }
    public Double getTotalPnl() { return totalPnl; }
    public Integer getDecisionCount() { return decisionCount; }
    public Integer getTimeoutCount() { return timeoutCount; }
    public Long getAvgResponseTimeMs() { return avgResponseTimeMs; }
    public Long getSeedValue() { return seedValue; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<GameDecision> getDecisions() { return new ArrayList<>(decisions); }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameSession that = (GameSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }
    
    @Override
    public String toString() {
        return "GameSession{" +
                "sessionId='" + sessionId + '\'' +
                ", segmentId=" + segmentId +
                ", status=" + status +
                ", currentFrame=" + currentFrame +
                ", totalFrames=" + totalFrames +
                ", totalScore=" + totalScore +
                ", totalPnl=" + totalPnl +
                '}';
    }
}

/**
 * 会话状态枚举
 */
enum SessionStatus {
    CREATED,    // 已创建，未开始
    RUNNING,    // 运行中
    PAUSED,     // 暂停中 (等待决策)
    COMPLETED,  // 已完成
    CANCELLED   // 已取消
}