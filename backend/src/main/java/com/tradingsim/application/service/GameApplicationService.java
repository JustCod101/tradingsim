package com.tradingsim.application.service;

import com.tradingsim.application.dto.GameSessionResponse;
import com.tradingsim.application.dto.GameDecisionRequest;
import com.tradingsim.application.dto.GameDecisionResponse;

import java.util.List;

/**
 * 游戏应用服务接口
 * 
 * @author TradingSim Team
 */
public interface GameApplicationService {
    
    /**
     * 创建新的游戏会话
     */
    GameSessionResponse createSession(String stockCode, String difficulty);
    
    /**
     * 根据ID获取游戏会话
     */
    GameSessionResponse getSessionById(String sessionId);
    
    /**
     * 提交交易决策
     */
    GameDecisionResponse submitDecision(String sessionId, GameDecisionRequest request);
    
    /**
     * 获取会话的所有决策
     */
    List<GameDecisionResponse> getSessionDecisions(String sessionId);
    
    /**
     * 结束游戏会话
     */
    GameSessionResponse finishSession(String sessionId);
    
    /**
     * 获取排行榜
     */
    List<GameSessionResponse> getLeaderboard(int limit);
    
    /**
     * 获取用户游戏历史
     */
    List<GameSessionResponse> getUserGameHistory(String userId, int limit);
}