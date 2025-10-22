package com.tradingsim.infrastructure.websocket.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * 游戏事件WebSocket消息
 * 用于在WebSocket中传输游戏事件和通知
 * 
 * @author TradingSim Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameEventMessage {
    
    private String sessionId;
    private String eventType;
    private String title;
    private String message;
    private String level; // INFO, WARNING, ERROR, SUCCESS
    private Map<String, Object> data;
    private Boolean autoClose;
    private Integer duration; // 自动关闭时间（秒）
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;
    
    // 默认构造函数
    public GameEventMessage() {
        this.timestamp = Instant.now();
    }
    
    // 构造函数
    public GameEventMessage(String sessionId, String eventType, String message) {
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.message = message;
        this.timestamp = Instant.now();
    }
    
    // 创建信息事件
    public static GameEventMessage info(String sessionId, String title, String message) {
        GameEventMessage event = new GameEventMessage(sessionId, "INFO", message);
        event.title = title;
        event.level = "INFO";
        event.autoClose = true;
        event.duration = 5;
        return event;
    }
    
    // 创建警告事件
    public static GameEventMessage warning(String sessionId, String title, String message) {
        GameEventMessage event = new GameEventMessage(sessionId, "WARNING", message);
        event.title = title;
        event.level = "WARNING";
        event.autoClose = true;
        event.duration = 8;
        return event;
    }
    
    // 创建错误事件
    public static GameEventMessage error(String sessionId, String title, String message) {
        GameEventMessage event = new GameEventMessage(sessionId, "ERROR", message);
        event.title = title;
        event.level = "ERROR";
        event.autoClose = false;
        return event;
    }
    
    // 创建成功事件
    public static GameEventMessage success(String sessionId, String title, String message) {
        GameEventMessage event = new GameEventMessage(sessionId, "SUCCESS", message);
        event.title = title;
        event.level = "SUCCESS";
        event.autoClose = true;
        event.duration = 3;
        return event;
    }
    
    // 创建游戏开始事件
    public static GameEventMessage gameStarted(String sessionId, String stockCode) {
        GameEventMessage event = success(sessionId, "游戏开始", 
            String.format("交易模拟游戏已开始，股票代码：%s", stockCode));
        event.eventType = "GAME_STARTED";
        return event;
    }
    
    // 创建游戏结束事件
    public static GameEventMessage gameEnded(String sessionId, String result) {
        GameEventMessage event = info(sessionId, "游戏结束", result);
        event.eventType = "GAME_ENDED";
        event.duration = 10;
        return event;
    }
    
    // 创建交易执行事件
    public static GameEventMessage tradeExecuted(String sessionId, String tradeInfo) {
        GameEventMessage event = success(sessionId, "交易执行", tradeInfo);
        event.eventType = "TRADE_EXECUTED";
        return event;
    }
    
    // 创建连接事件
    public static GameEventMessage connected(String sessionId) {
        GameEventMessage event = success(sessionId, "连接成功", "WebSocket连接已建立");
        event.eventType = "CONNECTED";
        return event;
    }
    
    // 创建断开连接事件
    public static GameEventMessage disconnected(String sessionId) {
        GameEventMessage event = warning(sessionId, "连接断开", "WebSocket连接已断开");
        event.eventType = "DISCONNECTED";
        return event;
    }
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getLevel() {
        return level;
    }
    
    public void setLevel(String level) {
        this.level = level;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public Boolean getAutoClose() {
        return autoClose;
    }
    
    public void setAutoClose(Boolean autoClose) {
        this.autoClose = autoClose;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}