package com.tradingsim.api.controller;

import com.tradingsim.application.service.GameApplicationService;
import com.tradingsim.application.dto.GameDecisionRequest;
import com.tradingsim.application.dto.GameDecisionResponse;
import com.tradingsim.application.dto.GameSessionResponse;
import com.tradingsim.infrastructure.websocket.message.*;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * 游戏WebSocket控制器
 * 处理实时游戏通信
 * 
 * @author TradingSim Team
 */
@Hidden
@Controller
public class GameWebSocketController {
    
    private static final Logger logger = LoggerFactory.getLogger(GameWebSocketController.class);
    
    @Autowired
    private GameApplicationService gameApplicationService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * 处理游戏决策提交
     */
    @MessageMapping("/game/decision")
    public void submitDecision(GameDecisionRequest request) {
        try {
            logger.info("收到决策提交请求: sessionId={}, decisionType={}", 
                request.getSessionId(), request.getDecisionType());
            
            GameDecisionResponse response = gameApplicationService.submitDecision(
                request.getSessionId(), 
                request
            );
            
            // 发送决策确认消息
            WebSocketMessage<DecisionMessage> message = WebSocketMessage.create(
                WebSocketMessageType.DECISION_RESULT,
                request.getSessionId(),
                DecisionMessage.submitted(
                    request.getSessionId(),
                    0, // frameIndex - 需要从会话中获取
                    com.tradingsim.domain.model.DecisionType.valueOf(request.getDecisionType()),
                    request.getQuantity() != null ? BigDecimal.valueOf(request.getQuantity()) : BigDecimal.ONE,
                    request.getPrice()
                )
            );
            
            messagingTemplate.convertAndSend(
                "/topic/game/decision/" + request.getSessionId(), 
                message
            );
            
            logger.info("决策提交成功: sessionId={}", request.getSessionId());
            
        } catch (Exception e) {
            logger.error("决策提交失败: sessionId={}, error={}", 
                request.getSessionId(), e.getMessage(), e);
            
            // 发送错误消息
            sendError(request.getSessionId(), "决策提交失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理游戏会话状态查询
     */
    @MessageMapping("/game/session/status")
    public void getSessionStatus(String sessionId) {
        try {
            logger.info("收到会话状态查询请求: sessionId={}", sessionId);
            
            GameSessionResponse sessionResponse = gameApplicationService.getSessionById(sessionId);
            if (sessionResponse != null) {
                // 发送会话状态消息
                WebSocketMessage<GameSessionResponse> message = WebSocketMessage.create(
                    WebSocketMessageType.SESSION_STATUS,
                    sessionId,
                    sessionResponse
                );
                
                messagingTemplate.convertAndSend(
                    "/topic/game/session/" + sessionId, 
                    message
                );
                
                logger.info("会话状态查询成功: sessionId={}", sessionId);
            } else {
                sendError(sessionId, "会话不存在: " + sessionId);
            }
            
        } catch (Exception e) {
            logger.error("会话状态查询失败: sessionId={}, error={}", 
                sessionId, e.getMessage(), e);
            sendError(sessionId, "会话状态获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送市场数据更新
     */
    public void sendMarketDataUpdate(String sessionId, Object marketData) {
        messagingTemplate.convertAndSend(
            "/topic/game/market/" + sessionId, 
            marketData
        );
    }
    
    /**
     * 发送游戏状态更新
     */
    public void sendGameStateUpdate(String sessionId, GameSessionResponse session) {
        messagingTemplate.convertAndSend(
            "/topic/game/state/" + sessionId, 
            session
        );
    }
    
    /**
     * 订阅游戏会话数据
     */
    @SubscribeMapping("/game/session/{sessionId}")
    public void subscribeToSession(@DestinationVariable String sessionId) {
        logger.info("客户端订阅游戏会话: sessionId={}", sessionId);
        
        // 发送连接确认消息
        WebSocketMessage<String> connectMessage = WebSocketMessage.create(
            WebSocketMessageType.CONNECT,
            sessionId,
            "已连接到游戏会话: " + sessionId
        );
        
        messagingTemplate.convertAndSend(
            "/topic/game/session/" + sessionId,
            connectMessage
        );
    }
    
    /**
     * 推送市场数据
     */
    public void pushMarketData(String sessionId, GameDataMessage marketData) {
        try {
            WebSocketMessage<GameDataMessage> message = WebSocketMessage.create(
                WebSocketMessageType.MARKET_DATA,
                sessionId,
                marketData
            );
            
            messagingTemplate.convertAndSend(
                "/topic/game/session/" + sessionId,
                message
            );
            
            logger.debug("推送市场数据: sessionId={}, frameIndex={}", 
                sessionId, marketData.getFrameIndex());
                
        } catch (Exception e) {
            logger.error("推送市场数据失败: sessionId={}, error={}", 
                sessionId, e.getMessage(), e);
        }
    }
    
    /**
     * 推送游戏事件
     */
    public void pushGameEvent(String sessionId, GameEventMessage event) {
        try {
            WebSocketMessage<GameEventMessage> message = WebSocketMessage.create(
                WebSocketMessageType.NOTIFICATION,
                sessionId,
                event
            );
            
            messagingTemplate.convertAndSend(
                "/topic/game/session/" + sessionId,
                message
            );
            
            logger.info("推送游戏事件: sessionId={}, eventType={}", 
                sessionId, event.getEventType());
                
        } catch (Exception e) {
            logger.error("推送游戏事件失败: sessionId={}, error={}", 
                sessionId, e.getMessage(), e);
        }
    }
    
    /**
     * 广播会话状态更新
     */
    public void broadcastSessionUpdate(String sessionId, GameSessionMessage sessionUpdate) {
        try {
            WebSocketMessage<GameSessionMessage> message = WebSocketMessage.create(
                WebSocketMessageType.SESSION_STATUS,
                sessionId,
                sessionUpdate
            );
            
            messagingTemplate.convertAndSend(
                "/topic/game/session/" + sessionId,
                message
            );
            
            logger.info("广播会话状态更新: sessionId={}, status={}", 
                sessionId, sessionUpdate.getStatus());
                
        } catch (Exception e) {
            logger.error("广播会话状态更新失败: sessionId={}, error={}", 
                sessionId, e.getMessage(), e);
        }
    }
    
    /**
     * 发送错误消息
     */
    private void sendError(String sessionId, String errorMessage) {
        WebSocketMessage<String> errorMsg = WebSocketMessage.error(sessionId, errorMessage);
        messagingTemplate.convertAndSend(
            "/topic/game/session/" + sessionId, 
            errorMsg
        );
    }
}