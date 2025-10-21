package com.tradingsim.domain.repository;

import com.tradingsim.domain.model.GameSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 游戏会话仓储接口
 * 
 * 定义游戏会话的数据访问操作，遵循DDD仓储模式:
 * - 提供聚合根的持久化操作
 * - 支持按各种条件查询会话
 * - 包含统计和分析查询
 * - 支持分页和排序
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
public interface GameSessionRepository {
    
    /**
     * 保存游戏会话
     * 
     * @param session 游戏会话聚合根
     * @return 保存后的会话
     */
    GameSession save(GameSession session);
    
    /**
     * 根据ID查找会话
     * 
     * @param sessionId 会话ID
     * @return 会话实例，如果不存在则返回空
     */
    Optional<GameSession> findById(String sessionId);
    
    /**
     * 根据用户ID查找会话列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 会话分页结果
     */
    Page<GameSession> findByUserId(String userId, Pageable pageable);
    
    /**
     * 根据段ID查找会话列表
     * 
     * @param segmentId 段ID
     * @param pageable 分页参数
     * @return 会话分页结果
     */
    Page<GameSession> findBySegmentId(String segmentId, Pageable pageable);
    
    /**
     * 根据状态查找会话列表
     * 
     * @param status 会话状态
     * @param pageable 分页参数
     * @return 会话分页结果
     */
    Page<GameSession> findByStatus(GameSession.SessionStatus status, Pageable pageable);
    
    /**
     * 查找用户在指定时间范围内的会话
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 会话列表
     */
    List<GameSession> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查找活跃会话 (正在进行或暂停的会话)
     * 
     * @param pageable 分页参数
     * @return 活跃会话分页结果
     */
    Page<GameSession> findActiveSessions(Pageable pageable);
    
    /**
     * 查找超时的会话 (超过指定时间未更新的活跃会话)
     * 
     * @param timeoutThreshold 超时阈值
     * @return 超时会话列表
     */
    List<GameSession> findTimeoutSessions(LocalDateTime timeoutThreshold);
    
    /**
     * 统计用户的会话数量
     * 
     * @param userId 用户ID
     * @return 会话总数
     */
    long countByUserId(String userId);
    
    /**
     * 统计指定状态的会话数量
     * 
     * @param status 会话状态
     * @return 会话数量
     */
    long countByStatus(GameSession.SessionStatus status);
    
    /**
     * 查找用户的最佳得分会话
     * 
     * @param userId 用户ID
     * @param limit 返回数量限制
     * @return 按总得分降序排列的会话列表
     */
    List<GameSession> findTopScoreSessionsByUserId(String userId, int limit);
    
    /**
     * 查找全局排行榜 (按总得分)
     * 
     * @param limit 返回数量限制
     * @return 按总得分降序排列的会话列表
     */
    List<GameSession> findGlobalTopScoreSessions(int limit);
    
    /**
     * 查找指定段的排行榜
     * 
     * @param segmentId 段ID
     * @param limit 返回数量限制
     * @return 按总得分降序排列的会话列表
     */
    List<GameSession> findTopScoreSessionsBySegmentId(String segmentId, int limit);
    
    /**
     * 删除会话
     * 
     * @param sessionId 会话ID
     */
    void deleteById(String sessionId);
    
    /**
     * 批量删除过期会话
     * 
     * @param expiredBefore 过期时间阈值
     * @return 删除的会话数量
     */
    int deleteExpiredSessions(LocalDateTime expiredBefore);
    
    /**
     * 检查会话是否存在
     * 
     * @param sessionId 会话ID
     * @return 是否存在
     */
    boolean existsById(String sessionId);
    
    /**
     * 获取用户会话统计信息
     * 
     * @param userId 用户ID
     * @return 统计信息映射
     */
    UserSessionStats getUserSessionStats(String userId);
    
    /**
     * 用户会话统计信息
     */
    interface UserSessionStats {
        long getTotalSessions();
        long getCompletedSessions();
        double getAverageScore();
        double getBestScore();
        double getWinRate();
        long getTotalDecisions();
        double getAverageResponseTime();
    }
}