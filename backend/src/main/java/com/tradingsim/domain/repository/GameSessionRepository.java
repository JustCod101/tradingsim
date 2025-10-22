package com.tradingsim.domain.repository;

import com.tradingsim.domain.model.GameSession;
import com.tradingsim.domain.model.SessionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 游戏会话仓储接口
 * 
 * @author TradingSim Team
 */
public interface GameSessionRepository {
    
    /**
     * 保存游戏会话
     */
    GameSession save(GameSession session);
    
    /**
     * 根据ID查找游戏会话
     */
    Optional<GameSession> findById(String id);
    
    /**
     * 根据状态查找游戏会话
     */
    List<GameSession> findByStatus(SessionStatus status);
    
    /**
     * 根据股票代码查找游戏会话
     */
    List<GameSession> findByStockCode(String stockCode);
    
    /**
     * 根据创建时间范围查找游戏会话
     */
    List<GameSession> findByCreatedAtBetween(Instant startTime, Instant endTime);
    
    /**
     * 查找活跃的游戏会话（运行中或暂停状态）
     */
    List<GameSession> findActiveSessions();
    
    /**
     * 获取排行榜数据（按得分排序）
     */
    List<GameSession> findTopScoringSessions(int limit);
    
    /**
     * 统计指定状态的会话数量
     */
    long countByStatus(SessionStatus status);
    
    /**
     * 删除游戏会话
     */
    void deleteById(String id);
    
    /**
     * 检查会话是否存在
     */
    boolean existsById(String id);
    
    /**
     * 批量保存游戏会话
     */
    List<GameSession> saveAll(List<GameSession> sessions);
}