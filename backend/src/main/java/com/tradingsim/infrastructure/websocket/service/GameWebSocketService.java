package com.tradingsim.infrastructure.websocket.service;

import com.tradingsim.api.controller.GameWebSocketController;
import com.tradingsim.domain.model.GameSession;
import com.tradingsim.domain.model.GameDecision;
import com.tradingsim.infrastructure.websocket.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 游戏WebSocket服务
 * 负责处理游戏相关的实时数据推送和状态广播
 */
@Service
public class GameWebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(GameWebSocketService.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private GameWebSocketController webSocketController;

    // 活跃会话管理
    private final Map<String, GameSession> activeSessions = new ConcurrentHashMap<>();
    
    // 心跳检测调度器
    private final ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(2);

    /**
     * 启动会话监控
     */
    public void startSessionMonitoring(String sessionId) {
        logger.info("启动会话监控: sessionId={}", sessionId);
        
        // 启动心跳检测
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            sendHeartbeat(sessionId);
        }, 30, 30, TimeUnit.SECONDS);
        
        // 发送会话启动事件
        GameEventMessage startEvent = GameEventMessage.gameStarted(sessionId, "UNKNOWN");
        webSocketController.pushGameEvent(sessionId, startEvent);
    }

    /**
     * 停止会话监控
     */
    public void stopSessionMonitoring(String sessionId) {
        logger.info("停止会话监控: sessionId={}", sessionId);
        
        activeSessions.remove(sessionId);
        
        // 发送会话结束事件
        GameEventMessage endEvent = GameEventMessage.gameEnded(sessionId, "会话已结束");
        webSocketController.pushGameEvent(sessionId, endEvent);
    }

    /**
     * 推送市场数据帧
     */
    public void pushMarketFrame(String sessionId, int frameIndex, 
                               String stockCode, BigDecimal price, 
                               BigDecimal open, BigDecimal high, 
                               BigDecimal low, BigDecimal close, 
                               Long volume) {
        try {
            GameDataMessage marketData = GameDataMessage.marketData(
                sessionId, frameIndex, stockCode, open, high, low, close, volume
            );
            
            webSocketController.pushMarketData(sessionId, marketData);
            
            logger.debug("推送市场数据帧: sessionId={}, frameIndex={}, price={}", 
                sessionId, frameIndex, price);
                
        } catch (Exception e) {
            logger.error("推送市场数据帧失败: sessionId={}, frameIndex={}, error={}", 
                sessionId, frameIndex, e.getMessage(), e);
        }
    }

    /**
     * 推送账户状态更新
     */
    public void pushAccountUpdate(String sessionId, int frameIndex,
                                 BigDecimal currentBalance, BigDecimal totalPnl,
                                 BigDecimal position, BigDecimal unrealizedPnl,
                                 BigDecimal realizedPnl, int totalTrades) {
        try {
            GameDataMessage accountData = GameDataMessage.accountStatus(
                sessionId, frameIndex, currentBalance, totalPnl, 
                position.intValue(), unrealizedPnl, realizedPnl
            );
            
            webSocketController.pushMarketData(sessionId, accountData);
            
            logger.debug("推送账户状态: sessionId={}, frameIndex={}, balance={}, pnl={}", 
                sessionId, frameIndex, currentBalance, totalPnl);
                
        } catch (Exception e) {
            logger.error("推送账户状态失败: sessionId={}, frameIndex={}, error={}", 
                sessionId, frameIndex, e.getMessage(), e);
        }
    }

    /**
     * 推送决策结果
     */
    public void pushDecisionResult(String sessionId, GameDecision decision) {
        try {
            DecisionMessage decisionMessage = DecisionMessage.fromGameDecision(decision);
            
            WebSocketMessage<DecisionMessage> message = WebSocketMessage.create(
                WebSocketMessageType.DECISION_RESULT,
                sessionId,
                decisionMessage
            );
            
            messagingTemplate.convertAndSend(
                "/topic/game/session/" + sessionId,
                message
            );
            
            logger.info("推送决策结果: sessionId={}, decisionType={}, pnl={}", 
                sessionId, decision.getDecisionType(), decision.getPnl());
                
        } catch (Exception e) {
            logger.error("推送决策结果失败: sessionId={}, error={}", 
                sessionId, e.getMessage(), e);
        }
    }

    /**
     * 推送游戏状态更新
     */
    public void pushGameStatusUpdate(String sessionId, GameSession session) {
        try {
            GameSessionMessage sessionMessage = GameSessionMessage.fromGameSession(session);
            webSocketController.broadcastSessionUpdate(sessionId, sessionMessage);
            
            // 更新活跃会话缓存
            activeSessions.put(sessionId, session);
            
            logger.info("推送游戏状态更新: sessionId={}, status={}, currentFrame={}", 
                sessionId, session.getStatus(), session.getCurrentFrameIndex());
                
        } catch (Exception e) {
            logger.error("推送游戏状态更新失败: sessionId={}, error={}", 
                sessionId, e.getMessage(), e);
        }
    }

    /**
     * 发送游戏通知
     */
    public void sendGameNotification(String sessionId, String level, 
                                   String title, String message) {
        try {
            GameEventMessage notification;
            
            switch (level.toUpperCase()) {
                case "ERROR":
                    notification = GameEventMessage.error(sessionId, title, message);
                    break;
                case "WARNING":
                    notification = GameEventMessage.warning(sessionId, title, message);
                    break;
                case "SUCCESS":
                    notification = GameEventMessage.success(sessionId, title, message);
                    break;
                default:
                    notification = GameEventMessage.info(sessionId, title, message);
                    break;
            }
            
            webSocketController.pushGameEvent(sessionId, notification);
            
            logger.info("发送游戏通知: sessionId={}, level={}, title={}", 
                sessionId, level, title);
                
        } catch (Exception e) {
            logger.error("发送游戏通知失败: sessionId={}, error={}", 
                sessionId, e.getMessage(), e);
        }
    }

    /**
     * 发送心跳消息
     */
    private void sendHeartbeat(String sessionId) {
        try {
            WebSocketMessage<String> heartbeat = WebSocketMessage.heartbeat();
            heartbeat.setSessionId(sessionId);
            
            messagingTemplate.convertAndSend(
                "/topic/game/session/" + sessionId,
                heartbeat
            );
            
            logger.debug("发送心跳: sessionId={}", sessionId);
            
        } catch (Exception e) {
            logger.warn("发送心跳失败: sessionId={}, error={}", 
                sessionId, e.getMessage());
        }
    }

    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * 检查会话是否活跃
     */
    public boolean isSessionActive(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    /**
     * 关闭服务
     */
    public void shutdown() {
        logger.info("关闭GameWebSocketService");
        heartbeatScheduler.shutdown();
        try {
            if (!heartbeatScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                heartbeatScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            heartbeatScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}