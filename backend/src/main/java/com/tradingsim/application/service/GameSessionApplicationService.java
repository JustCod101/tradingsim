package com.tradingsim.application.service;

import com.tradingsim.application.dto.CreateSessionRequest;
import com.tradingsim.application.dto.SessionResponse;
import com.tradingsim.application.dto.SubmitDecisionRequest;
import com.tradingsim.domain.model.SessionStatus;

import java.util.List;
import java.util.Optional;

/**
 * 游戏会话应用服务接口
 * 
 * @author TradingSim Team
 */
public interface GameSessionApplicationService {
    
    /**
     * 创建新的游戏会话
     */
    SessionResponse createSession(CreateSessionRequest request);
    
    /**
     * 开始游戏会话
     */
    SessionResponse startSession(String sessionId);
    
    /**
     * 暂停游戏会话
     */
    SessionResponse pauseSession(String sessionId);
    
    /**
     * 恢复游戏会话
     */
    SessionResponse resumeSession(String sessionId);
    
    /**
     * 完成游戏会话
     */
    SessionResponse completeSession(String sessionId);
    
    /**
     * 取消游戏会话
     */
    SessionResponse cancelSession(String sessionId);
    
    /**
     * 提交决策
     */
    SessionResponse submitDecision(SubmitDecisionRequest request);
    
    /**
     * 推进到下一帧
     */
    SessionResponse nextFrame(String sessionId);
    
    /**
     * 根据ID获取会话
     */
    Optional<SessionResponse> getSessionById(String sessionId);
    
    /**
     * 根据状态获取会话列表
     */
    List<SessionResponse> getSessionsByStatus(SessionStatus status);
    
    /**
     * 根据股票代码获取会话列表
     */
    List<SessionResponse> getSessionsByStockCode(String stockCode);
    
    /**
     * 获取活跃会话列表
     */
    List<SessionResponse> getActiveSessions();
    
    /**
     * 获取排行榜
     */
    List<SessionResponse> getLeaderboard(int limit);
    
    /**
     * 删除会话
     */
    void deleteSession(String sessionId);
    
    /**
     * 清理过期会话
     */
    int cleanupExpiredSessions();
}