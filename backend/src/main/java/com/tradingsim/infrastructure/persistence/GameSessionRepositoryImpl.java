package com.tradingsim.infrastructure.persistence;

import com.tradingsim.domain.model.GameSession;
import com.tradingsim.domain.model.SessionStatus;
import com.tradingsim.domain.repository.GameSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

/**
 * 游戏会话仓储JPA接口
 * 
 * @author TradingSim Team
 */
interface GameSessionJpaRepository extends JpaRepository<GameSession, String> {
    
    @Query("SELECT s FROM GameSession s WHERE s.status = :status ORDER BY s.createdAt DESC")
    List<GameSession> findByStatus(@Param("status") SessionStatus status);
    
    @Query("SELECT s FROM GameSession s WHERE s.stockCode = :stockCode ORDER BY s.createdAt DESC")
    List<GameSession> findByStockCode(@Param("stockCode") String stockCode);
    
    @Query("SELECT s FROM GameSession s WHERE s.createdAt BETWEEN :startTime AND :endTime ORDER BY s.createdAt DESC")
    List<GameSession> findByCreatedAtBetween(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
    
    @Query("SELECT s FROM GameSession s WHERE s.status IN ('RUNNING', 'PAUSED') ORDER BY s.createdAt DESC")
    List<GameSession> findActiveSessions();
    
    @Query("SELECT s FROM GameSession s WHERE s.score IS NOT NULL ORDER BY s.score DESC")
    List<GameSession> findTopScoringSessions(Pageable pageable);
    
    @Query("SELECT COUNT(s) FROM GameSession s WHERE s.status = :status")
    long countByStatus(@Param("status") SessionStatus status);
}

/**
 * 游戏会话仓储实现类
 * 
 * @author TradingSim Team
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
    public Optional<GameSession> findById(String id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public List<GameSession> findByStatus(SessionStatus status) {
        return jpaRepository.findByStatus(status);
    }
    
    @Override
    public List<GameSession> findByStockCode(String stockCode) {
        return jpaRepository.findByStockCode(stockCode);
    }
    
    @Override
    public List<GameSession> findByCreatedAtBetween(Instant startTime, Instant endTime) {
        return jpaRepository.findByCreatedAtBetween(startTime, endTime);
    }
    
    @Override
    public List<GameSession> findActiveSessions() {
        return jpaRepository.findActiveSessions();
    }
    
    @Override
    public List<GameSession> findTopScoringSessions(int limit) {
        return jpaRepository.findTopScoringSessions(PageRequest.of(0, limit));
    }
    
    @Override
    public long countByStatus(SessionStatus status) {
        return jpaRepository.countByStatus(status);
    }
    
    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }
    
    @Override
    public List<GameSession> saveAll(List<GameSession> sessions) {
        return jpaRepository.saveAll(sessions);
    }
}