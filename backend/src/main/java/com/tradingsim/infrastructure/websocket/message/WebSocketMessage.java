package com.tradingsim.infrastructure.websocket.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

/**
 * WebSocket消息基础类
 * 定义所有WebSocket消息的通用结构
 * 
 * @author TradingSim Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage<T> {
    
    private String id;
    private String type;
    private String sessionId;
    private Long sequenceNumber;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;
    
    private T data;
    
    // 默认构造函数
    public WebSocketMessage() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
    }
    
    // 构造函数
    public WebSocketMessage(WebSocketMessageType type, String sessionId, T data) {
        this();
        this.type = type.getType();
        this.sessionId = sessionId;
        this.data = data;
    }
    
    public WebSocketMessage(String type, String sessionId, T data) {
        this();
        this.type = type;
        this.sessionId = sessionId;
        this.data = data;
    }
    
    // 静态工厂方法
    public static <T> WebSocketMessage<T> create(WebSocketMessageType type, String sessionId, T data) {
        return new WebSocketMessage<>(type, sessionId, data);
    }
    
    public static WebSocketMessage<String> error(String sessionId, String errorMessage) {
        return new WebSocketMessage<>(WebSocketMessageType.ERROR, sessionId, errorMessage);
    }
    
    public static <T> WebSocketMessage<T> notification(String sessionId, T notification) {
        return new WebSocketMessage<>(WebSocketMessageType.NOTIFICATION, sessionId, notification);
    }
    
    public static WebSocketMessage<String> heartbeat() {
        return new WebSocketMessage<>(WebSocketMessageType.HEARTBEAT, null, "ping");
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Long getSequenceNumber() {
        return sequenceNumber;
    }
    
    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "WebSocketMessage{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                ", timestamp=" + timestamp +
                ", data=" + data +
                '}';
    }
}