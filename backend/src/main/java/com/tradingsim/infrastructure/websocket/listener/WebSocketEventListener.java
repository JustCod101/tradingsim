package com.tradingsim.infrastructure.websocket.listener;

import com.tradingsim.infrastructure.websocket.message.GameEventMessage;
import com.tradingsim.infrastructure.websocket.message.WebSocketMessage;
import com.tradingsim.infrastructure.websocket.message.WebSocketMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

/**
 * WebSocket事件监听器
 * 处理WebSocket连接、断开、订阅和取消订阅事件
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    /**
     * 处理WebSocket连接事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        logger.info("WebSocket连接建立: sessionId={}", sessionId);
        
        // 发送连接确认消息
        WebSocketMessage<GameEventMessage> message = WebSocketMessage.create(
            WebSocketMessageType.CONNECT,
            sessionId,
            GameEventMessage.connected(sessionId)
        );
        
        messagingTemplate.convertAndSend("/topic/system", message);
    }

    /**
     * 处理WebSocket断开事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        logger.info("WebSocket连接断开: sessionId={}", sessionId);
        
        // 发送断开连接消息
        WebSocketMessage<GameEventMessage> message = WebSocketMessage.create(
            WebSocketMessageType.DISCONNECT,
            sessionId,
            GameEventMessage.disconnected(sessionId)
        );
        
        messagingTemplate.convertAndSend("/topic/system", message);
    }

    /**
     * 处理订阅事件
     */
    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        logger.info("客户端订阅: sessionId={}, destination={}", sessionId, destination);
        
        // 如果是订阅游戏会话，发送订阅确认
        if (destination != null && destination.startsWith("/topic/game/session/")) {
            String gameSessionId = destination.substring("/topic/game/session/".length());
            
            WebSocketMessage<GameEventMessage> message = WebSocketMessage.create(
                WebSocketMessageType.SUBSCRIBE,
                gameSessionId,
                GameEventMessage.info(gameSessionId, "订阅成功", "已订阅游戏会话: " + gameSessionId)
            );
            
            messagingTemplate.convertAndSend(destination, message);
        }
    }

    /**
     * 处理取消订阅事件
     */
    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String subscriptionId = headerAccessor.getSubscriptionId();
        
        logger.info("客户端取消订阅: sessionId={}, subscriptionId={}", sessionId, subscriptionId);
        
        // 发送取消订阅确认
        WebSocketMessage<GameEventMessage> message = WebSocketMessage.create(
            WebSocketMessageType.UNSUBSCRIBE,
            sessionId,
            GameEventMessage.info(sessionId, "取消订阅", "已取消订阅: " + subscriptionId)
        );
        
        messagingTemplate.convertAndSend("/topic/system", message);
    }
}