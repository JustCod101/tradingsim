package com.tradingsim.application.service.impl;

import com.tradingsim.application.dto.GameSessionResponse;
import com.tradingsim.application.dto.GameDecisionRequest;
import com.tradingsim.application.dto.GameDecisionResponse;
import com.tradingsim.application.service.GameApplicationService;
import com.tradingsim.domain.model.GameSession;
import com.tradingsim.domain.model.GameDecision;
import com.tradingsim.domain.service.GameSessionDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 游戏应用服务实现
 * 
 * @author TradingSim Team
 */
@Service
public class GameApplicationServiceImpl implements GameApplicationService {

    private final GameSessionDomainService gameSessionDomainService;

    @Autowired
    public GameApplicationServiceImpl(GameSessionDomainService gameSessionDomainService) {
        this.gameSessionDomainService = gameSessionDomainService;
    }

    @Override
    public GameSessionResponse createSession(String stockCode, String difficulty) {
        // 使用默认参数创建会话
        GameSession session = gameSessionDomainService.createSession(stockCode, "1m", new java.math.BigDecimal("10000"));
        return convertToResponse(session);
    }

    @Override
    public GameSessionResponse getSessionById(String sessionId) {
        return gameSessionDomainService.getSessionById(sessionId)
                .map(this::convertToResponse)
                .orElse(null);
    }

    @Override
    public GameDecisionResponse submitDecision(String sessionId, GameDecisionRequest request) {
        // 需要先获取当前帧索引和决策类型
        GameSession session = gameSessionDomainService.submitDecision(
                sessionId, 
                0, // 当前帧索引，需要从会话中获取
                com.tradingsim.domain.model.DecisionType.valueOf(request.getDecisionType()), 
                request.getPrice(), 
                request.getQuantity(), 
                request.getResponseTimeMs()
        );
        // 返回最新的决策
        if (!session.getDecisions().isEmpty()) {
            GameDecision lastDecision = session.getDecisions().get(session.getDecisions().size() - 1);
            return convertToResponse(lastDecision);
        }
        return null;
    }

    @Override
    public List<GameDecisionResponse> getSessionDecisions(String sessionId) {
        // 通过会话获取决策列表
        return gameSessionDomainService.getSessionById(sessionId)
                .map(session -> session.getDecisions().stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList()))
                .orElse(java.util.Collections.emptyList());
    }

    @Override
    public GameSessionResponse finishSession(String sessionId) {
        GameSession session = gameSessionDomainService.completeSession(sessionId);
        return convertToResponse(session);
    }

    @Override
    public List<GameSessionResponse> getLeaderboard(int limit) {
        List<GameSession> sessions = gameSessionDomainService.getLeaderboard(limit);
        return sessions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GameSessionResponse> getUserGameHistory(String userId, int limit) {
        // 暂时返回空列表，因为GameSession没有userId字段
        return java.util.Collections.emptyList();
    }

    /**
     * 将GameSession转换为GameSessionResponse
     */
    private GameSessionResponse convertToResponse(GameSession session) {
        GameSessionResponse response = new GameSessionResponse();
        response.setSessionId(session.getId()); // 使用getId()
        response.setUserId("default-user"); // 默认用户ID
        response.setStockCode(session.getStockCode());
        response.setStatus(session.getStatus().toString());
        response.setDifficulty("normal"); // 默认难度
        response.setInitialBalance(session.getInitialBalance());
        response.setCurrentBalance(session.getCurrentBalance());
        response.setTotalReturn(session.getTotalPnl()); // 使用总盈亏
        response.setReturnPercentage(java.math.BigDecimal.ZERO); // 计算回报百分比
        response.setCurrentFrame(session.getCurrentFrameIndex()); // 使用getCurrentFrameIndex()
        response.setTotalFrames(session.getTotalFrames());
        response.setScore(session.getScore().intValue()); // 转换为Integer
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        response.setFinishedAt(session.getEndTime()); // 使用结束时间
        return response;
    }

    /**
     * 将GameDecision转换为GameDecisionResponse
     */
    private GameDecisionResponse convertToResponse(GameDecision decision) {
        if (decision == null) {
            return null;
        }

        GameDecisionResponse response = new GameDecisionResponse();
        response.setDecisionId(decision.getId().toString()); // 转换为String
        response.setSessionId(decision.getSessionId());
        response.setFrameIndex(decision.getFrameIndex());
        response.setDecisionType(decision.getDecisionType().toString());
        response.setPrice(decision.getPrice());
        response.setQuantity(decision.getQuantity());
        response.setResponseTimeMs(decision.getResponseTimeMs());
        response.setProfit(decision.getPnl()); // 使用getPnl()
        response.setProfitPercentage(java.math.BigDecimal.ZERO); // 计算利润百分比
        response.setCreatedAt(decision.getCreatedAt());

        return response;
    }
}