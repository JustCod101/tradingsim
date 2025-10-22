package com.tradingsim.infrastructure.websocket.message;

/**
 * WebSocket消息类型枚举
 * 定义所有支持的WebSocket消息类型
 * 
 * @author TradingSim Team
 */
public enum WebSocketMessageType {
    
    // 游戏会话相关
    SESSION_CREATED("session_created"),
    SESSION_STARTED("session_started"),
    SESSION_PAUSED("session_paused"),
    SESSION_RESUMED("session_resumed"),
    SESSION_ENDED("session_ended"),
    SESSION_STATUS("session_status"),
    
    // 游戏数据相关
    FRAME_DATA("frame_data"),
    MARKET_DATA("market_data"),
    KLINE_DATA("kline_data"),
    
    // 决策相关
    DECISION_REQUEST("decision_request"),
    DECISION_RESULT("decision_result"),
    DECISION_TIMEOUT("decision_timeout"),
    
    // 游戏事件
    KEYPOINT_DETECTED("keypoint_detected"),
    PAUSE_REQUESTED("pause_requested"),
    REWARD_CALCULATED("reward_calculated"),
    
    // 系统消息
    ERROR("error"),
    HEARTBEAT("heartbeat"),
    NOTIFICATION("notification"),
    
    // 连接管理
    CONNECT("connect"),
    DISCONNECT("disconnect"),
    SUBSCRIBE("subscribe"),
    UNSUBSCRIBE("unsubscribe");
    
    private final String type;
    
    WebSocketMessageType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return type;
    }
    
    /**
     * 根据字符串获取消息类型
     */
    public static WebSocketMessageType fromString(String type) {
        for (WebSocketMessageType messageType : values()) {
            if (messageType.type.equals(type)) {
                return messageType;
            }
        }
        throw new IllegalArgumentException("Unknown message type: " + type);
    }
}