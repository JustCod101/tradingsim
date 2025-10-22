package com.tradingsim.infrastructure.persistence;

import com.tradingsim.domain.model.DecisionType;
import com.tradingsim.domain.model.GameDecision;
import com.tradingsim.domain.repository.GameDecisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 游戏决策仓储JPA接口
 * 
 * @author TradingSim Team
 */
interface GameDecisionJpaRepository extends JpaRepository<GameDecision, Long> {
    
    @Query("SELECT d FROM GameDecision d WHERE d.sessionId = :sessionId ORDER BY d.frameIndex ASC")
    List<GameDecision> findBySessionId(@Param("sessionId") String sessionId);
    
    @Query("SELECT d FROM GameDecision d WHERE d.sessionId = :sessionId AND d.frameIndex = :frameIndex")
    Optional<GameDecision> findBySessionIdAndFrameIndex(@Param("sessionId") String sessionId, 
                                                        @Param("frameIndex") Integer frameIndex);
    
    @Query("SELECT d FROM GameDecision d WHERE d.sessionId = :sessionId AND d.decisionType = :decisionType ORDER BY d.frameIndex ASC")
    List<GameDecision> findBySessionIdAndDecisionType(@Param("sessionId") String sessionId, 
                                                      @Param("decisionType") DecisionType decisionType);
    
    @Query("SELECT d FROM GameDecision d WHERE d.sessionId = :sessionId AND d.decisionType IN ('BUY', 'SELL') ORDER BY d.frameIndex ASC")
    List<GameDecision> findTradingDecisionsBySessionId(@Param("sessionId") String sessionId);
    
    @Query("SELECT d FROM GameDecision d WHERE d.sessionId = :sessionId AND d.pnl > 0 ORDER BY d.frameIndex ASC")
    List<GameDecision> findProfitableDecisionsBySessionId(@Param("sessionId") String sessionId);
    
    @Query("SELECT d FROM GameDecision d WHERE d.sessionId = :sessionId AND d.decisionTime BETWEEN :startTime AND :endTime ORDER BY d.frameIndex ASC")
    List<GameDecision> findBySessionIdAndDecisionTimeBetween(@Param("sessionId") String sessionId,
                                                            @Param("startTime") Instant startTime,
                                                            @Param("endTime") Instant endTime);
    
    @Query("SELECT COUNT(d) FROM GameDecision d WHERE d.sessionId = :sessionId")
    long countBySessionId(@Param("sessionId") String sessionId);
    
    @Query("SELECT COUNT(d) FROM GameDecision d WHERE d.sessionId = :sessionId AND d.decisionType IN ('BUY', 'SELL')")
    long countTradingDecisionsBySessionId(@Param("sessionId") String sessionId);
    
    @Query("SELECT COUNT(d) FROM GameDecision d WHERE d.sessionId = :sessionId AND d.pnl > 0")
    long countProfitableDecisionsBySessionId(@Param("sessionId") String sessionId);
    
    @Query("SELECT d FROM GameDecision d WHERE d.sessionId = :sessionId ORDER BY d.frameIndex DESC")
    Optional<GameDecision> findLastDecisionBySessionId(@Param("sessionId") String sessionId);
    
    @Query("DELETE FROM GameDecision d WHERE d.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") String sessionId);
    
    @Query("DELETE FROM GameDecision d WHERE d.decisionTime < :cutoffTime")
    void deleteByDecisionTimeBefore(@Param("cutoffTime") Instant cutoffTime);
}

/**
 * 游戏决策仓储实现类
 * 
 * @author TradingSim Team
 */
@Repository
public class GameDecisionRepositoryImpl implements GameDecisionRepository {
    
    private final GameDecisionJpaRepository jpaRepository;
    
    @Autowired
    public GameDecisionRepositoryImpl(GameDecisionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public GameDecision save(GameDecision decision) {
        return jpaRepository.save(decision);
    }
    
    @Override
    public Optional<GameDecision> findById(Long id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public List<GameDecision> findBySessionId(String sessionId) {
        return jpaRepository.findBySessionId(sessionId);
    }
    
    @Override
    public Optional<GameDecision> findBySessionIdAndFrameIndex(String sessionId, Integer frameIndex) {
        return jpaRepository.findBySessionIdAndFrameIndex(sessionId, frameIndex);
    }
    
    @Override
    public List<GameDecision> findBySessionIdAndDecisionType(String sessionId, DecisionType decisionType) {
        return jpaRepository.findBySessionIdAndDecisionType(sessionId, decisionType);
    }
    
    @Override
    public List<GameDecision> findTradingDecisionsBySessionId(String sessionId) {
        return jpaRepository.findTradingDecisionsBySessionId(sessionId);
    }
    
    @Override
    public List<GameDecision> findProfitableDecisionsBySessionId(String sessionId) {
        return jpaRepository.findProfitableDecisionsBySessionId(sessionId);
    }
    
    @Override
    public List<GameDecision> findBySessionIdAndDecisionTimeBetween(String sessionId, Instant startTime, Instant endTime) {
        return jpaRepository.findBySessionIdAndDecisionTimeBetween(sessionId, startTime, endTime);
    }
    
    @Override
    public long countBySessionId(String sessionId) {
        return jpaRepository.countBySessionId(sessionId);
    }
    
    @Override
    public long countTradingDecisionsBySessionId(String sessionId) {
        return jpaRepository.countTradingDecisionsBySessionId(sessionId);
    }
    
    @Override
    public long countProfitableDecisionsBySessionId(String sessionId) {
        return jpaRepository.countProfitableDecisionsBySessionId(sessionId);
    }
    
    @Override
    public Optional<GameDecision> findLastDecisionBySessionId(String sessionId) {
        return jpaRepository.findLastDecisionBySessionId(sessionId);
    }
    
    @Override
    public List<GameDecision> saveAll(List<GameDecision> decisions) {
        return jpaRepository.saveAll(decisions);
    }
    
    @Override
    public void deleteBySessionId(String sessionId) {
        jpaRepository.deleteBySessionId(sessionId);
    }
    
    @Override
    public void deleteByDecisionTimeBefore(Instant timestamp) {
        jpaRepository.deleteByDecisionTimeBefore(timestamp);
    }
}