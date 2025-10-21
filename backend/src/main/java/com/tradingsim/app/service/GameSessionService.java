package com.tradingsim.app.service;

import com.tradingsim.domain.model.GameDecision;
import com.tradingsim.domain.model.GameSession;
import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.domain.repository.GameSessionRepository;
import com.tradingsim.domain.repository.OhlcvDataRepository;
import com.tradingsim.spi.Detector;
import com.tradingsim.spi.Scoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 游戏会话应用服务
 * 
 * 负责游戏会话的业务逻辑协调:
 * - 会话生命周期管理
 * - 关键点检测和评分
 * - 决策处理和验证
 * - 缓存和性能优化
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Service
@Transactional
public class GameSessionService {
    
    private static final Logger logger = LoggerFactory.getLogger(GameSessionService.class);
    
    private final GameSessionRepository sessionRepository;
    private final OhlcvDataRepository ohlcvRepository;
    private final List<Detector> detectors;
    private final List<Scoring> scoringRules;
    
    @Autowired
    public GameSessionService(GameSessionRepository sessionRepository,
                             OhlcvDataRepository ohlcvRepository,
                             List<Detector> detectors,
                             List<Scoring> scoringRules) {
        this.sessionRepository = sessionRepository;
        this.ohlcvRepository = ohlcvRepository;
        this.detectors = detectors;
        this.scoringRules = scoringRules;
        
        logger.info("GameSessionService initialized with {} detectors and {} scoring rules", 
                   detectors.size(), scoringRules.size());
    }
    
    /**
     * 创建新的游戏会话
     */
    public GameSession createSession(String segmentId, String userId, String stockCode, 
                                   LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Creating new session for user {} with segment {}", userId, segmentId);
        
        // 获取OHLCV数据
        List<OhlcvData> ohlcvData = ohlcvRepository.findByStockCodeAndTimestampBetween(
            stockCode, startTime, endTime);
        
        if (ohlcvData.isEmpty()) {
            throw new IllegalArgumentException("No OHLCV data found for the specified time range");
        }
        
        // 生成会话ID和种子
        String sessionId = generateSessionId();
        long seedValue = generateSeedValue();
        
        // 检测关键点
        List<Detector.KeypointDetection> keypoints = detectKeypoints(ohlcvData, seedValue);
        
        // 创建会话
        GameSession session = new GameSession(
            sessionId, segmentId, userId, ohlcvData.size(), 
            keypoints.size(), seedValue
        );
        
        // 保存会话
        GameSession savedSession = sessionRepository.save(session);
        
        logger.info("Created session {} with {} frames and {} keypoints", 
                   sessionId, ohlcvData.size(), keypoints.size());
        
        return savedSession;
    }
    
    /**
     * 获取会话信息
     */
    @Cacheable(value = "sessions", key = "#sessionId")
    public Optional<GameSession> getSession(String sessionId) {
        return sessionRepository.findById(sessionId);
    }
    
    /**
     * 开始会话
     */
    @CacheEvict(value = "sessions", key = "#sessionId")
    public GameSession startSession(String sessionId) {
        GameSession session = getSessionOrThrow(sessionId);
        session.start();
        return sessionRepository.save(session);
    }
    
    /**
     * 暂停会话
     */
    @CacheEvict(value = "sessions", key = "#sessionId")
    public GameSession pauseSession(String sessionId) {
        GameSession session = getSessionOrThrow(sessionId);
        session.pause();
        return sessionRepository.save(session);
    }
    
    /**
     * 恢复会话
     */
    @CacheEvict(value = "sessions", key = "#sessionId")
    public GameSession resumeSession(String sessionId) {
        GameSession session = getSessionOrThrow(sessionId);
        session.resume();
        return sessionRepository.save(session);
    }
    
    /**
     * 完成会话
     */
    @CacheEvict(value = "sessions", key = "#sessionId")
    public GameSession completeSession(String sessionId) {
        GameSession session = getSessionOrThrow(sessionId);
        session.complete();
        return sessionRepository.save(session);
    }
    
    /**
     * 取消会话
     */
    @CacheEvict(value = "sessions", key = "#sessionId")
    public GameSession cancelSession(String sessionId) {
        GameSession session = getSessionOrThrow(sessionId);
        session.cancel();
        return sessionRepository.save(session);
    }
    
    /**
     * 提交决策
     */
    @CacheEvict(value = "sessions", key = "#sessionId")
    public GameSession submitDecision(String sessionId, GameDecision.DecisionType decisionType, 
                                    BigDecimal price, BigDecimal quantity, String clientId) {
        GameSession session = getSessionOrThrow(sessionId);
        
        // 验证会话状态
        if (!session.canAcceptDecision()) {
            throw new IllegalStateException("Session is not in a state to accept decisions");
        }
        
        // 创建决策
        String decisionId = generateDecisionId();
        GameDecision decision = new GameDecision(
            decisionId, sessionId, session.getCurrentFrame(), 
            decisionType, price, quantity, clientId
        );
        
        // 异步计算评分
        CompletableFuture.supplyAsync(() -> calculateDecisionScore(decision, session))
            .thenAccept(score -> updateDecisionScore(sessionId, decisionId, score));
        
        // 添加决策到会话
        session.addDecision(decision);
        
        // 推进到下一帧
        session.advanceFrame();
        
        // 检查是否完成
        if (session.getCurrentFrame() >= session.getTotalFrames()) {
            session.complete();
        }
        
        return sessionRepository.save(session);
    }
    
    /**
     * 记录超时
     */
    @CacheEvict(value = "sessions", key = "#sessionId")
    public GameSession recordTimeout(String sessionId) {
        GameSession session = getSessionOrThrow(sessionId);
        session.recordTimeout();
        session.advanceFrame();
        
        // 检查是否完成
        if (session.getCurrentFrame() >= session.getTotalFrames()) {
            session.complete();
        }
        
        return sessionRepository.save(session);
    }
    
    /**
     * 获取用户会话列表
     */
    public Page<GameSession> getUserSessions(String userId, Pageable pageable) {
        return sessionRepository.findByUserId(userId, pageable);
    }
    
    /**
     * 获取段会话列表
     */
    public Page<GameSession> getSegmentSessions(String segmentId, Pageable pageable) {
        return sessionRepository.findBySegmentId(segmentId, pageable);
    }
    
    /**
     * 获取排行榜
     */
    @Cacheable(value = "leaderboard", key = "'global:' + #limit")
    public List<GameSession> getGlobalLeaderboard(int limit) {
        return sessionRepository.findGlobalTopScoreSessions(limit);
    }
    
    /**
     * 获取段排行榜
     */
    @Cacheable(value = "leaderboard", key = "'segment:' + #segmentId + ':' + #limit")
    public List<GameSession> getSegmentLeaderboard(String segmentId, int limit) {
        return sessionRepository.findTopScoreSessionsBySegmentId(segmentId, limit);
    }
    
    /**
     * 获取用户统计
     */
    @Cacheable(value = "userStats", key = "#userId")
    public GameSessionRepository.UserSessionStats getUserStats(String userId) {
        return sessionRepository.getUserSessionStats(userId);
    }
    
    /**
     * 清理超时会话
     */
    public int cleanupTimeoutSessions(int timeoutMinutes) {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<GameSession> timeoutSessions = sessionRepository.findTimeoutSessions(timeoutThreshold);
        
        int cleanedCount = 0;
        for (GameSession session : timeoutSessions) {
            try {
                session.cancel();
                sessionRepository.save(session);
                cleanedCount++;
            } catch (Exception e) {
                logger.warn("Failed to cleanup timeout session {}: {}", session.getId(), e.getMessage());
            }
        }
        
        logger.info("Cleaned up {} timeout sessions", cleanedCount);
        return cleanedCount;
    }
    
    /**
     * 检测关键点
     */
    private List<Detector.KeypointDetection> detectKeypoints(List<OhlcvData> ohlcvData, long seedValue) {
        List<Detector.KeypointDetection> allKeypoints = new ArrayList<>();
        
        // 使用所有可用的检测器
        for (Detector detector : detectors) {
            try {
                List<Detector.KeypointDetection> keypoints = detector.detectKeypoints(
                    ohlcvData, 3, 10, seedValue);
                allKeypoints.addAll(keypoints);
                
                logger.debug("Detector {} found {} keypoints", 
                           detector.getName(), keypoints.size());
            } catch (Exception e) {
                logger.warn("Detector {} failed: {}", detector.getName(), e.getMessage());
            }
        }
        
        // 按置信度排序并去重
        allKeypoints.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));
        
        // 移除重复的帧索引 (保留置信度最高的)
        Set<Integer> usedFrames = new HashSet<>();
        List<Detector.KeypointDetection> uniqueKeypoints = new ArrayList<>();
        
        for (Detector.KeypointDetection keypoint : allKeypoints) {
            if (!usedFrames.contains(keypoint.getFrameIndex())) {
                uniqueKeypoints.add(keypoint);
                usedFrames.add(keypoint.getFrameIndex());
            }
        }
        
        return uniqueKeypoints;
    }
    
    /**
     * 计算决策评分
     */
    private double calculateDecisionScore(GameDecision decision, GameSession session) {
        // 这里需要获取历史和未来数据进行评分
        // 为了简化，返回一个模拟分数
        // 实际实现中需要调用评分规则
        
        double totalScore = 0.0;
        int ruleCount = 0;
        
        for (Scoring scoring : scoringRules) {
            try {
                if (scoring.getSupportedDecisionTypes().contains(decision.getType())) {
                    // 这里需要实际的历史和未来数据
                    // Scoring.ScoringResult result = scoring.calculateScore(decision, historicalData, futureData);
                    // totalScore += result.getTotalScore();
                    // ruleCount++;
                    
                    // 临时模拟评分
                    totalScore += ThreadLocalRandom.current().nextDouble(-0.1, 0.1);
                    ruleCount++;
                }
            } catch (Exception e) {
                logger.warn("Scoring rule {} failed: {}", scoring.getName(), e.getMessage());
            }
        }
        
        return ruleCount > 0 ? totalScore / ruleCount : 0.0;
    }
    
    /**
     * 更新决策评分
     */
    private void updateDecisionScore(String sessionId, String decisionId, double score) {
        try {
            Optional<GameSession> sessionOpt = sessionRepository.findById(sessionId);
            if (sessionOpt.isPresent()) {
                GameSession session = sessionOpt.get();
                session.getDecisions().stream()
                    .filter(d -> d.getId().equals(decisionId))
                    .findFirst()
                    .ifPresent(decision -> {
                        decision.setScore(score);
                        session.updateTotalScore(score);
                        sessionRepository.save(session);
                    });
            }
        } catch (Exception e) {
            logger.error("Failed to update decision score for session {}, decision {}: {}", 
                        sessionId, decisionId, e.getMessage());
        }
    }
    
    /**
     * 获取会话或抛出异常
     */
    private GameSession getSessionOrThrow(String sessionId) {
        return sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }
    
    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + 
               ThreadLocalRandom.current().nextInt(1000, 9999);
    }
    
    /**
     * 生成决策ID
     */
    private String generateDecisionId() {
        return "decision_" + System.currentTimeMillis() + "_" + 
               ThreadLocalRandom.current().nextInt(1000, 9999);
    }
    
    /**
     * 生成种子值
     */
    private long generateSeedValue() {
        return ThreadLocalRandom.current().nextLong();
    }
}