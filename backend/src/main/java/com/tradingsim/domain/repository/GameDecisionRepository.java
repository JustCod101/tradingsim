package com.tradingsim.domain.repository;

import com.tradingsim.domain.model.GameDecision;
import com.tradingsim.domain.model.DecisionType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 游戏决策仓储接口
 * 
 * @author TradingSim Team
 */
public interface GameDecisionRepository {
    
    /**
     * 保存游戏决策
     */
    GameDecision save(GameDecision decision);
    
    /**
     * 根据ID查找游戏决策
     */
    Optional<GameDecision> findById(Long id);
    
    /**
     * 根据会话ID查找所有决策
     */
    List<GameDecision> findBySessionId(String sessionId);
    
    /**
     * 根据会话ID和帧索引查找决策
     */
    Optional<GameDecision> findBySessionIdAndFrameIndex(String sessionId, Integer frameIndex);
    
    /**
     * 根据会话ID和决策类型查找决策
     */
    List<GameDecision> findBySessionIdAndDecisionType(String sessionId, DecisionType decisionType);
    
    /**
     * 根据会话ID查找交易决策（买入和卖出）
     */
    List<GameDecision> findTradingDecisionsBySessionId(String sessionId);
    
    /**
     * 根据会话ID查找盈利决策
     */
    List<GameDecision> findProfitableDecisionsBySessionId(String sessionId);
    
    /**
     * 根据会话ID和时间范围查找决策
     */
    List<GameDecision> findBySessionIdAndDecisionTimeBetween(
            String sessionId, Instant startTime, Instant endTime);
    
    /**
     * 统计会话的决策数量
     */
    long countBySessionId(String sessionId);
    
    /**
     * 统计会话的交易决策数量
     */
    long countTradingDecisionsBySessionId(String sessionId);
    
    /**
     * 统计会话的盈利决策数量
     */
    long countProfitableDecisionsBySessionId(String sessionId);
    
    /**
     * 根据会话ID查找最后一个决策
     */
    Optional<GameDecision> findLastDecisionBySessionId(String sessionId);
    
    /**
     * 批量保存游戏决策
     */
    List<GameDecision> saveAll(List<GameDecision> decisions);
    
    /**
     * 根据会话ID删除所有决策
     */
    void deleteBySessionId(String sessionId);
    
    /**
     * 删除指定时间之前的决策
     */
    void deleteByDecisionTimeBefore(Instant timestamp);
}