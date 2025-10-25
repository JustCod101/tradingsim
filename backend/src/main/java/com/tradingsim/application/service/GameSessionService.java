package com.tradingsim.application.service;

import com.tradingsim.domain.model.*;
import com.tradingsim.domain.repository.GameSessionRepository;
import com.tradingsim.domain.repository.GameDecisionRepository;
import com.tradingsim.domain.repository.OhlcvDataRepository;
import com.tradingsim.infrastructure.spi.GameStrategyProvider;
import com.tradingsim.infrastructure.spi.MarketDataProvider;
import com.tradingsim.infrastructure.spi.NotificationProvider;
import com.tradingsim.infrastructure.spi.SpiManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * 游戏会话服务
 * 负责游戏会话的创建、管理和决策处理
 */
@Service
@Transactional
public class GameSessionService {

    private static final Logger logger = LoggerFactory.getLogger(GameSessionService.class);

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameDecisionRepository gameDecisionRepository;

    @Autowired
    private OhlcvDataRepository ohlcvDataRepository;

    @Autowired
    private SpiManager spiManager;

    /**
     * 创建新的游戏会话
     */
    public GameSession createGameSession(String stockCode, String timeframe, BigDecimal initialBalance) {
        logger.info("Creating game session for stock: {}, timeframe: {}, balance: {}", stockCode, timeframe, initialBalance);

        // 验证市场数据可用性
        List<MarketDataProvider> marketDataProviders = spiManager.getEnabledProviders(MarketDataProvider.class);
        if (marketDataProviders.isEmpty()) {
            throw new IllegalStateException("No market data provider available");
        }

        MarketDataProvider marketDataProvider = marketDataProviders.get(0);
        if (!marketDataProvider.getSupportedStockCodes().contains(stockCode)) {
            throw new IllegalArgumentException("Stock code not supported: " + stockCode);
        }

        // 创建游戏会话
        String sessionId = UUID.randomUUID().toString();
        GameSession session = new GameSession(sessionId, stockCode, timeframe, initialBalance);

        // 保存会话
        GameSession savedSession = gameSessionRepository.save(session);

        // 发送通知
        sendNotification("GAME_START", "system", "游戏开始", 
                        String.format("游戏会话已创建，股票代码: %s", stockCode),
                        Map.of("sessionId", sessionId, "stockCode", stockCode, "initialBalance", initialBalance));

        logger.info("Game session created: {}", sessionId);
        return savedSession;
    }

    /**
     * 提交游戏决策
     */
    public GameDecision submitDecision(String sessionId, Integer frameIndex, DecisionType decisionType, 
                                     BigDecimal price, Integer quantity, Long responseTimeMs) {
        logger.info("Submitting decision for session: {}, frame: {}, type: {}", sessionId, frameIndex, decisionType);

        // 获取游戏会话
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + sessionId));

        // 验证会话状态
        if (session.getStatus() != SessionStatus.RUNNING) {
            throw new IllegalStateException("Game session is not active: " + sessionId);
        }

        // 验证帧索引
        if (!frameIndex.equals(session.getCurrentFrameIndex())) {
            throw new IllegalArgumentException("Invalid frame index. Expected: " + session.getCurrentFrameIndex() + ", got: " + frameIndex);
        }

        // 创建决策
        GameDecision decision = new GameDecision(sessionId, frameIndex, decisionType);
        decision.setPrice(price);
        decision.setQuantity(quantity != null ? quantity : 1);
        decision.setResponseTimeMs(responseTimeMs);

        // 获取当前市场数据
        List<MarketDataProvider> marketDataProviders = spiManager.getEnabledProviders(MarketDataProvider.class);
        MarketDataProvider marketDataProvider = marketDataProviders.get(0);
        OhlcvData currentData = marketDataProvider.getLatestOhlcvData(session.getStockCode());

        // 获取前一帧的市场数据来计算盈亏
        OhlcvData previousData = getPreviousMarketData(session.getStockCode(), frameIndex);
        double previousPrice = previousData != null ? previousData.getClosePrice().doubleValue() : currentData.getClosePrice().doubleValue();
        
        // 计算盈亏
        List<GameStrategyProvider> strategyProviders = spiManager.getEnabledProviders(GameStrategyProvider.class);
        GameStrategyProvider strategyProvider = strategyProviders.get(0);
        double pnl = strategyProvider.calculatePnl(decision, currentData.getClosePrice().doubleValue(), previousPrice);
        decision.setPnl(BigDecimal.valueOf(pnl));

        // 保存决策
        GameDecision savedDecision = gameDecisionRepository.save(decision);

        // 发送通知
        sendNotification("DECISION_RESULT", "system", "决策结果", 
                        String.format("决策类型: %s, 盈亏: %.2f", decisionType, pnl),
                        Map.of("sessionId", sessionId, "decisionType", decisionType.toString(), "pnl", pnl));

        logger.info("Decision submitted: {}, PnL: {}", savedDecision.getId(), pnl);
        return savedDecision;
    }

    /**
     * 结束游戏会话
     */
    public GameSession endGameSession(String sessionId) {
        logger.info("Ending game session: {}", sessionId);

        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.RUNNING) {
            throw new IllegalStateException("Game session is not running: " + sessionId);
        }

        // 更新会话状态
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(Instant.now());

        // 计算最终分数
        List<GameDecision> decisions = gameDecisionRepository.findBySessionId(sessionId);
        double finalScore = calculateFinalScore(session, decisions);
        // GameSession没有setFinalScore方法，这里只计算分数用于通知

        // 保存会话
        GameSession savedSession = gameSessionRepository.save(session);

        // 发送通知
        sendNotification("GAME_END", "system", "游戏结束", 
                        String.format("游戏会话已结束，最终分数: %.2f", finalScore),
                        Map.of("sessionId", sessionId, "finalScore", finalScore, "totalPnl", session.getTotalPnl()));

        logger.info("Game session ended: {}, Final score: {}", sessionId, finalScore);
        return savedSession;
    }

    /**
     * 获取游戏会话详情
     */
    @Transactional(readOnly = true)
    public GameSession getGameSession(String sessionId) {
        return gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + sessionId));
    }

    /**
     * 获取用户的游戏会话列表
     */
    @Transactional(readOnly = true)
    public List<GameSession> getUserGameSessions(String userId) {
        // 由于当前GameSession模型没有userId字段，这里返回空列表
        // 实际实现需要根据业务需求调整
        return List.of();
    }

    /**
     * 获取会话的决策列表
     */
    @Transactional(readOnly = true)
    public List<GameDecision> getSessionDecisions(String sessionId) {
        return gameDecisionRepository.findBySessionId(sessionId);
    }

    /**
     * 计算决策盈亏
     */
    private BigDecimal calculatePnl(GameDecision decision, GameSession session, OhlcvData currentMarketData) {

        // 获取前一帧的市场数据
        OhlcvData previousMarketData = getPreviousMarketData(session.getStockCode(), decision.getFrameIndex());
        if (previousMarketData == null) {
            return BigDecimal.ZERO;
        }

        // 使用策略提供者计算盈亏
        List<GameStrategyProvider> strategyProviders = spiManager.getEnabledProviders(GameStrategyProvider.class);
        if (!strategyProviders.isEmpty()) {
            GameStrategyProvider strategyProvider = strategyProviders.get(0);
            double pnl = strategyProvider.calculatePnl(decision, 
                    currentMarketData.getClosePrice().doubleValue(), 
                    previousMarketData.getClosePrice().doubleValue());
            return BigDecimal.valueOf(pnl);
        }

        return BigDecimal.ZERO;
    }

    /**
     * 更新会话状态
     */
    private void updateSessionAfterDecision(GameSession session, GameDecision decision) {
        // 更新总盈亏
        session.setTotalPnl(session.getTotalPnl().add(decision.getPnl()));

        // 更新余额
        if (decision.getDecisionType() == DecisionType.LONG) {
            BigDecimal cost = decision.getPrice().multiply(BigDecimal.valueOf(decision.getQuantity()));
            session.setCurrentBalance(session.getCurrentBalance().subtract(cost));
        } else if (decision.getDecisionType() == DecisionType.SHORT) {
            BigDecimal revenue = decision.getPrice().multiply(BigDecimal.valueOf(decision.getQuantity()));
            session.setCurrentBalance(session.getCurrentBalance().add(revenue));
        }

        // 更新当前帧
        session.setCurrentFrameIndex(session.getCurrentFrameIndex() + 1);

        // 保存会话
        gameSessionRepository.save(session);
    }

    /**
     * 计算决策分数
     */
    private double calculateDecisionScore(GameDecision decision, GameSession session) {
        List<GameStrategyProvider> strategyProviders = spiManager.getEnabledProviders(GameStrategyProvider.class);
        if (!strategyProviders.isEmpty()) {
            GameStrategyProvider strategyProvider = strategyProviders.get(0);
            List<GameDecision> decisions = gameDecisionRepository.findBySessionId(session.getId());
            return strategyProvider.calculateScore(session, decisions);
        }
        return 0.0;
    }

    /**
     * 计算最终分数
     */
    private double calculateFinalScore(GameSession session, List<GameDecision> decisions) {
        List<GameStrategyProvider> strategyProviders = spiManager.getEnabledProviders(GameStrategyProvider.class);
        if (!strategyProviders.isEmpty()) {
            GameStrategyProvider strategyProvider = strategyProviders.get(0);
            return strategyProvider.calculateScore(session, decisions);
        }
        return 0.0;
    }

    /**
     * 获取当前市场数据
     */
    private OhlcvData getCurrentMarketData(String stockCode, Integer frameIndex) {
        // 这里应该根据frameIndex获取对应的市场数据
        // 简化实现：获取最新数据
        return ohlcvDataRepository.findLatestByStockCode(stockCode).orElse(null);
    }

    /**
     * 获取前一帧市场数据
     */
    private OhlcvData getPreviousMarketData(String stockCode, Integer frameIndex) {
        // 这里应该根据frameIndex-1获取对应的市场数据
        // 简化实现：获取最新数据
        return ohlcvDataRepository.findLatestByStockCode(stockCode).orElse(null);
    }

    /**
     * 发送通知
     */
    private void sendNotification(String type, String recipient, String subject, String content, Map<String, Object> parameters) {
        List<NotificationProvider> notificationProviders = spiManager.getEnabledProviders(NotificationProvider.class);
        for (NotificationProvider provider : notificationProviders) {
            try {
                provider.sendNotification(type, recipient, subject, content, parameters);
            } catch (Exception e) {
                logger.warn("Failed to send notification via provider: {}", provider.getProviderName(), e);
            }
        }
    }
}