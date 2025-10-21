package com.tradingsim.infra.persistence;

import com.tradingsim.domain.model.GameSession;
import com.tradingsim.domain.repository.GameSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 游戏会话仓储实现
 * 
 * 基于Spring Data JPA实现的GameSession仓储
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Repository
public class GameSessionRepositoryImpl implements GameSessionRepository {
    
    private final GameSessionJpaRepository jpaRepository;
    
    @Autowired
    public GameSessionRepositoryImpl(GameSessionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public GameSession save(GameSession session) {
        return jpaRepository.save(session);
    }
    
    @Override
    public Optional<GameSession> findById(String sessionId) {
        return jpaRepository.findById(sessionId);
    }
    
    @Override
    public List<GameSession> findByUserId(String userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @Override
    public List<GameSession> findBySegmentId(String segmentId) {
        return jpaRepository.findBySegmentIdOrderByCreatedAtDesc(segmentId);
    }
    
    @Override
    public List<GameSession> findByStatus(GameSession.SessionStatus status) {
        return jpaRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    @Override
    public List<GameSession> findActiveSessionsOlderThan(LocalDateTime threshold) {
        return jpaRepository.findActiveSessionsOlderThan(threshold);
    }
    
    @Override
    public List<GameSession> findTimeoutSessions(LocalDateTime threshold) {
        return jpaRepository.findTimeoutSessions(threshold);
    }
    
    @Override
    public long countByUserId(String userId) {
        return jpaRepository.countByUserId(userId);
    }
    
    @Override
    public long countBySegmentId(String segmentId) {
        return jpaRepository.countBySegmentId(segmentId);
    }
    
    @Override
    public List<GameSession> findTopScoringByUser(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "totalScore"));
        return jpaRepository.findByUserIdOrderByTotalScoreDesc(userId, pageable);
    }
    
    @Override
    public List<GameSession> findTopScoringGlobal(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "totalScore"));
        return jpaRepository.findAllByOrderByTotalScoreDesc(pageable);
    }
    
    @Override
    public List<GameSession> findTopScoringBySegment(String segmentId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "totalScore"));
        return jpaRepository.findBySegmentIdOrderByTotalScoreDesc(segmentId, pageable);
    }
    
    @Override
    public void deleteById(String sessionId) {
        jpaRepository.deleteById(sessionId);
    }
    
    @Override
    public void deleteByUserId(String userId) {
        jpaRepository.deleteByUserId(userId);
    }
    
    @Override
    public UserSessionStats getUserSessionStats(String userId) {
        return jpaRepository.getUserSessionStats(userId);
    }
}

/**
 * Spring Data JPA Repository接口
 */
interface GameSessionJpaRepository extends JpaRepository<GameSession, String> {
    
    List<GameSession> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<GameSession> findBySegmentIdOrderByCreatedAtDesc(String segmentId);
    
    List<GameSession> findByStatusOrderByCreatedAtDesc(GameSession.SessionStatus status);
    
    @Query("SELECT s FROM GameSession s WHERE s.status IN ('ACTIVE', 'PAUSED') AND s.updatedAt < :threshold")
    List<GameSession> findActiveSessionsOlderThan(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT s FROM GameSession s WHERE s.status = 'ACTIVE' AND " +
           "s.updatedAt < :threshold AND s.timeoutCount > 0")
    List<GameSession> findTimeoutSessions(@Param("threshold") LocalDateTime threshold);
    
    long countByUserId(String userId);
    
    long countBySegmentId(String segmentId);
    
    List<GameSession> findByUserIdOrderByTotalScoreDesc(String userId, Pageable pageable);
    
    List<GameSession> findAllByOrderByTotalScoreDesc(Pageable pageable);
    
    List<GameSession> findBySegmentIdOrderByTotalScoreDesc(String segmentId, Pageable pageable);
    
    void deleteByUserId(String userId);
    
    @Query("SELECT new com.tradingsim.infra.persistence.UserSessionStatsImpl(" +
           "COUNT(s), " +
           "AVG(s.totalScore), " +
           "MAX(s.totalScore), " +
           "AVG(s.totalPnl), " +
           "SUM(CASE WHEN s.totalPnl > 0 THEN 1 ELSE 0 END) * 100.0 / COUNT(s), " +
           "AVG(s.averageResponseTime), " +
           "SUM(s.buyDecisionCount), " +
           "SUM(s.sellDecisionCount), " +
           "SUM(s.skipDecisionCount), " +
           "SUM(s.timeoutCount)" +
           ") FROM GameSession s WHERE s.userId = :userId AND s.status = 'COMPLETED'")
    UserSessionStats getUserSessionStats(@Param("userId") String userId);
}

/**
 * UserSessionStats实现类
 */
class UserSessionStatsImpl implements GameSessionRepository.UserSessionStats {
    
    private final long totalSessions;
    private final double averageScore;
    private final double bestScore;
    private final double averagePnl;
    private final double winRate;
    private final double averageResponseTime;
    private final long totalBuyDecisions;
    private final long totalSellDecisions;
    private final long totalSkipDecisions;
    private final long totalTimeouts;
    
    public UserSessionStatsImpl(long totalSessions, double averageScore, double bestScore,
                               double averagePnl, double winRate, double averageResponseTime,
                               long totalBuyDecisions, long totalSellDecisions,
                               long totalSkipDecisions, long totalTimeouts) {
        this.totalSessions = totalSessions;
        this.averageScore = averageScore;
        this.bestScore = bestScore;
        this.averagePnl = averagePnl;
        this.winRate = winRate;
        this.averageResponseTime = averageResponseTime;
        this.totalBuyDecisions = totalBuyDecisions;
        this.totalSellDecisions = totalSellDecisions;
        this.totalSkipDecisions = totalSkipDecisions;
        this.totalTimeouts = totalTimeouts;
    }
    
    @Override
    public long getTotalSessions() {
        return totalSessions;
    }
    
    @Override
    public double getAverageScore() {
        return averageScore;
    }
    
    @Override
    public double getBestScore() {
        return bestScore;
    }
    
    @Override
    public double getAveragePnl() {
        return averagePnl;
    }
    
    @Override
    public double getWinRate() {
        return winRate;
    }
    
    @Override
    public double getAverageResponseTime() {
        return averageResponseTime;
    }
    
    @Override
    public long getTotalBuyDecisions() {
        return totalBuyDecisions;
    }
    
    @Override
    public long getTotalSellDecisions() {
        return totalSellDecisions;
    }
    
    @Override
    public long getTotalSkipDecisions() {
        return totalSkipDecisions;
    }
    
    @Override
    public long getTotalTimeouts() {
        return totalTimeouts;
    }
}