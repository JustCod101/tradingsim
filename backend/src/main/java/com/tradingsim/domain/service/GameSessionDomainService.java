package com.tradingsim.domain.service;

import com.tradingsim.domain.model.GameSession;
import com.tradingsim.domain.model.GameDecision;
import com.tradingsim.domain.model.SessionStatus;
import com.tradingsim.domain.model.DecisionType;
import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.domain.repository.GameSessionRepository;
import com.tradingsim.domain.repository.GameDecisionRepository;
import com.tradingsim.domain.repository.OhlcvDataRepository;
import com.tradingsim.infrastructure.spi.GameStrategyProvider;
import com.tradingsim.infrastructure.spi.MarketDataProvider;
import com.tradingsim.infrastructure.spi.SpiManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 游戏会话领域服务
 * 
 * @author TradingSim Team
 */
@Service
public class GameSessionDomainService {
    
    private final GameSessionRepository sessionRepository;
    private final GameDecisionRepository decisionRepository;
    private final OhlcvDataRepository ohlcvDataRepository;
    private final SpiManager spiManager;
    
    public GameSessionDomainService(GameSessionRepository sessionRepository,
                                   GameDecisionRepository decisionRepository,
                                   OhlcvDataRepository ohlcvDataRepository,
                                   SpiManager spiManager) {
        this.sessionRepository = sessionRepository;
        this.decisionRepository = decisionRepository;
        this.ohlcvDataRepository = ohlcvDataRepository;
        this.spiManager = spiManager;
    }
    
    /**
     * 创建新的游戏会话
     */
    public GameSession createSession(String stockCode, String timeframe, BigDecimal initialBalance) {
        String sessionId = generateSessionId();
        GameSession session = new GameSession(sessionId, stockCode, timeframe, initialBalance);
        session.setCreatedAt(Instant.now());
        session.setUpdatedAt(Instant.now());
        return sessionRepository.save(session);
    }
    
    /**
     * 开始游戏会话
     */
    public GameSession startSession(String sessionId) {
        GameSession session = getSessionByIdOrThrow(sessionId);
        session.start();
        return sessionRepository.save(session);
    }
    
    /**
     * 暂停游戏会话
     */
    public GameSession pauseSession(String sessionId) {
        GameSession session = getSessionByIdOrThrow(sessionId);
        session.pause();
        return sessionRepository.save(session);
    }
    
    /**
     * 恢复游戏会话
     */
    public GameSession resumeSession(String sessionId) {
        GameSession session = getSessionByIdOrThrow(sessionId);
        if (session.getStatus() != SessionStatus.PAUSED) {
            throw new IllegalStateException("Cannot resume session in status: " + session.getStatus());
        }
        session.start(); // 重用start方法
        return sessionRepository.save(session);
    }
    
    /**
     * 完成游戏会话
     */
    public GameSession completeSession(String sessionId) {
        GameSession session = getSessionByIdOrThrow(sessionId);
        session.complete();
        return sessionRepository.save(session);
    }
    
    /**
     * 取消游戏会话
     */
    public GameSession cancelSession(String sessionId) {
        GameSession session = getSessionByIdOrThrow(sessionId);
        session.cancel();
        return sessionRepository.save(session);
    }
    
    /**
     * 提交决策
     */
    public GameSession submitDecision(String sessionId, Integer frameIndex, DecisionType decisionType, 
                                    BigDecimal price, Integer quantity, Long responseTimeMs) {
        GameSession session = getSessionByIdOrThrow(sessionId);
        
        if (session.getStatus() != SessionStatus.RUNNING) {
            throw new IllegalStateException("Cannot submit decision for session in status: " + session.getStatus());
        }
        
        // 验证决策的帧索引
        if (!frameIndex.equals(session.getCurrentFrameIndex())) {
            throw new IllegalArgumentException("Decision frame index does not match current session frame");
        }
        
        // 创建决策
        GameDecision decision = new GameDecision(sessionId, frameIndex, decisionType);
        decision.setPrice(price);
        decision.setQuantity(quantity != null ? quantity : 1);
        decision.setResponseTimeMs(responseTimeMs);
        
        // 计算PnL
        calculateAndSetPnl(decision, session);
        
        // 保存决策
        decisionRepository.save(decision);
        
        // 更新会话
        session.addDecision(decision);
        session.nextFrame();
        
        // 检查是否完成所有帧
        if (session.isCompleted()) {
            session.complete();
        }
        
        return sessionRepository.save(session);
    }
    
    /**
     * 推进到下一帧
     */
    public GameSession nextFrame(String sessionId) {
        GameSession session = getSessionByIdOrThrow(sessionId);
        
        if (session.getStatus() != SessionStatus.RUNNING) {
            throw new IllegalStateException("Cannot advance frame for session in status: " + session.getStatus());
        }
        
        session.nextFrame();
        
        // 检查是否完成所有帧
        if (session.isCompleted()) {
            session.complete();
        }
        
        return sessionRepository.save(session);
    }
    
    /**
     * 计算会话统计数据
     */
    public void calculateSessionStats(String sessionId) {
        GameSession session = getSessionByIdOrThrow(sessionId);
        List<GameDecision> decisions = decisionRepository.findBySessionId(sessionId);
        
        // 重新计算统计数据
        int totalTrades = 0;
        int winningTrades = 0;
        BigDecimal totalPnl = BigDecimal.ZERO;
        BigDecimal maxBalance = session.getInitialBalance();
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        
        for (GameDecision decision : decisions) {
            if (decision.isTradingDecision()) {
                totalTrades++;
                if (decision.isProfitable()) {
                    winningTrades++;
                }
                if (decision.getPnl() != null) {
                    totalPnl = totalPnl.add(decision.getPnl());
                }
            }
        }
        
        // 计算最大回撤
        BigDecimal currentBalance = session.getInitialBalance();
        for (GameDecision decision : decisions) {
            if (decision.getPnl() != null) {
                currentBalance = currentBalance.add(decision.getPnl());
                if (currentBalance.compareTo(maxBalance) > 0) {
                    maxBalance = currentBalance;
                }
                BigDecimal drawdown = maxBalance.subtract(currentBalance);
                if (drawdown.compareTo(maxDrawdown) > 0) {
                    maxDrawdown = drawdown;
                }
            }
        }
        
        // 更新会话统计数据
        session.setTotalTrades(totalTrades);
        session.setWinningTrades(winningTrades);
        session.setLosingTrades(totalTrades - winningTrades);
        session.setTotalPnl(totalPnl);
        session.setCurrentBalance(session.getInitialBalance().add(totalPnl));
        session.setMaxDrawdown(maxDrawdown);
        
        if (totalTrades > 0) {
            BigDecimal winRate = BigDecimal.valueOf(winningTrades)
                    .divide(BigDecimal.valueOf(totalTrades), 4, BigDecimal.ROUND_HALF_UP);
            session.setWinRate(winRate);
        }
        
        sessionRepository.save(session);
    }
    
    /**
     * 获取会话排行榜
     */
    public List<GameSession> getLeaderboard(int limit) {
        return sessionRepository.findTopScoringSessions(limit);
    }
    
    /**
     * 检查会话是否可以操作
     */
    public boolean canOperateSession(String sessionId) {
        Optional<GameSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return false;
        }
        
        SessionStatus status = sessionOpt.get().getStatus();
        return status == SessionStatus.RUNNING || status == SessionStatus.PAUSED;
    }
    
    /**
     * 清理过期会话
     */
    public void cleanupExpiredSessions(Instant cutoffTime) {
        List<GameSession> expiredSessions = sessionRepository.findByCreatedAtBetween(
                Instant.EPOCH, cutoffTime);
        
        for (GameSession session : expiredSessions) {
            if (session.getStatus() == SessionStatus.CREATED || 
                session.getStatus() == SessionStatus.CANCELLED) {
                sessionRepository.deleteById(session.getId());
            }
        }
    }
    
    /**
     * 根据ID获取会话（如果不存在则抛出异常）
     */
    public Optional<GameSession> getSessionById(String sessionId) {
        return sessionRepository.findById(sessionId);
    }
    
    public List<GameSession> getSessionsByStatus(SessionStatus status) {
        return sessionRepository.findByStatus(status);
    }
    
    public List<GameSession> getSessionsByStockCode(String stockCode) {
        return sessionRepository.findByStockCode(stockCode);
    }
    
    public List<GameSession> getActiveSessions() {
        return sessionRepository.findActiveSessions();
    }
    
    public void deleteSession(String sessionId) {
        sessionRepository.deleteById(sessionId);
    }
    
    public int cleanupExpiredSessions() {
        Instant cutoffTime = Instant.now().minusSeconds(24 * 60 * 60); // 24小时前
        Instant endTime = Instant.now();
        List<GameSession> expiredSessions = sessionRepository.findByCreatedAtBetween(Instant.EPOCH, cutoffTime);
        for (GameSession session : expiredSessions) {
            sessionRepository.deleteById(session.getId());
        }
        return expiredSessions.size();
    }
    
    private GameSession getSessionByIdOrThrow(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }
    
    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 计算并设置决策的PnL
     */
    private void calculateAndSetPnl(GameDecision decision, GameSession session) {
        try {
            // 获取当前市场数据
            List<MarketDataProvider> marketDataProviders = spiManager.getEnabledProviders(MarketDataProvider.class);
            if (marketDataProviders.isEmpty()) {
                decision.setPnl(BigDecimal.ZERO);
                return;
            }
            
            MarketDataProvider marketDataProvider = marketDataProviders.get(0);
            OhlcvData currentData = marketDataProvider.getLatestOhlcvData(session.getStockCode());
            
            // 获取前一帧的市场数据
            OhlcvData previousData = getPreviousMarketData(session.getStockCode(), decision.getFrameIndex());
            double previousPrice = previousData != null ? 
                previousData.getClosePrice().doubleValue() : 
                currentData.getClosePrice().doubleValue();
            
            // 使用策略提供者计算PnL
            List<GameStrategyProvider> strategyProviders = spiManager.getEnabledProviders(GameStrategyProvider.class);
            if (!strategyProviders.isEmpty()) {
                GameStrategyProvider strategyProvider = strategyProviders.get(0);
                double pnl = strategyProvider.calculatePnl(decision, 
                    currentData.getClosePrice().doubleValue(), 
                    previousPrice);
                decision.setPnl(BigDecimal.valueOf(pnl));
            } else {
                decision.setPnl(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            // 如果计算失败，设置为0
            decision.setPnl(BigDecimal.ZERO);
        }
    }
    
    /**
     * 获取前一帧的市场数据
     */
    private OhlcvData getPreviousMarketData(String stockCode, Integer frameIndex) {
        if (frameIndex <= 0) {
            return null;
        }
        
        try {
            // 这里简化处理，实际应该根据时间序列获取前一帧数据
            // 暂时返回null，让调用方使用当前价格
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}