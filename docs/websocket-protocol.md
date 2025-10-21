# WebSocket 协议规范

## 概述

TradingSim 使用 STOMP over WebSocket 协议进行实时通信，支持K线数据推送、关键点暂停、决策提交和奖励通知。

## 连接信息

- **WebSocket URL**: `ws://localhost:8080/ws` (可配置)
- **协议**: STOMP 1.2
- **消息格式**: JSON
- **心跳间隔**: 30秒 (可配置)

## 连接流程

### 1. 建立连接

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    // 订阅主题
    subscribeToSession(sessionId);
});
```

### 2. 订阅主题

```javascript
// 订阅会话帧数据
stompClient.subscribe('/topic/session.' + sessionId + '.frames', function(message) {
    const frameData = JSON.parse(message.body);
    handleFrameMessage(frameData);
});

// 订阅会话状态变化
stompClient.subscribe('/topic/session.' + sessionId + '.status', function(message) {
    const statusData = JSON.parse(message.body);
    handleStatusChange(statusData);
});
```

## 主题定义

### 订阅主题 (客户端接收)

#### `/topic/session.{sessionId}.frames`
接收K线帧数据和游戏事件

**消息类型**:
- `KLINE` - K线数据推送
- `PAUSE` - 关键点暂停
- `REWARD` - 评分奖励
- `END` - 会话结束

#### `/topic/session.{sessionId}.status`
接收会话状态变化通知

### 发送目标 (客户端发送)

#### `/app/session.{sessionId}.decision`
提交交易决策 (幂等操作)

## 消息格式

### 1. KLINE 消息 (服务端 → 客户端)

```json
{
  "type": "KLINE",
  "sessionId": "session-abc123",
  "frameIndex": 25,
  "timestamp": "2024-01-01T10:05:00Z",
  "data": {
    "open": 150.25,
    "high": 150.80,
    "low": 150.10,
    "close": 150.65,
    "volume": 5420,
    "isKeypoint": false
  },
  "metadata": {
    "totalFrames": 120,
    "remainingFrames": 95,
    "intervalMs": 250
  }
}
```

**字段说明**:
- `frameIndex`: 当前帧索引 (从0开始)
- `isKeypoint`: 是否为关键点
- `intervalMs`: 推送间隔 (可配置)

### 2. PAUSE 消息 (服务端 → 客户端)

```json
{
  "type": "PAUSE",
  "sessionId": "session-abc123",
  "frameIndex": 30,
  "timestamp": "2024-01-01T10:05:30Z",
  "data": {
    "reason": "KEYPOINT_DETECTED",
    "currentPrice": 150.75,
    "timeoutSec": 10,
    "detectorType": "LocalExtremaDetector"
  },
  "metadata": {
    "keypointIndex": 2,
    "totalKeypoints": 5,
    "detectionConfidence": 0.85
  }
}
```

**字段说明**:
- `reason`: 暂停原因 (KEYPOINT_DETECTED, MANUAL_PAUSE)
- `timeoutSec`: 决策超时时间 (可配置)
- `detectorType`: 检测器类型
- `detectionConfidence`: 检测置信度

### 3. REWARD 消息 (服务端 → 客户端)

```json
{
  "type": "REWARD",
  "sessionId": "session-abc123",
  "frameIndex": 35,
  "timestamp": "2024-01-01T10:05:35Z",
  "data": {
    "decisionId": 12345,
    "windowSize": 5,
    "pnl": 1.25,
    "totalScore": 87.5,
    "scoreBreakdown": {
      "pnlScore": 62.5,
      "riskPenalty": -12.0,
      "feePenalty": -0.75,
      "timeBonusScore": 5.0
    },
    "ruleVersion": "v1.0"
  },
  "metadata": {
    "maxDrawdown": 0.02,
    "volatility": 0.025,
    "responseTimeMs": 2500
  }
}
```

**字段说明**:
- `windowSize`: 评分窗口大小 (可配置)
- `scoreBreakdown`: 评分明细
- `ruleVersion`: 评分规则版本
- `responseTimeMs`: 决策响应时间

### 4. END 消息 (服务端 → 客户端)

```json
{
  "type": "END",
  "sessionId": "session-abc123",
  "frameIndex": 120,
  "timestamp": "2024-01-01T10:15:00Z",
  "data": {
    "reason": "COMPLETED",
    "finalStats": {
      "totalDecisions": 8,
      "totalScore": 245.8,
      "totalPnl": 12.35,
      "avgResponseTime": 2800,
      "timeoutCount": 1,
      "winRate": 0.75
    }
  },
  "metadata": {
    "duration": 600000,
    "completedAt": "2024-01-01T10:15:00Z"
  }
}
```

**字段说明**:
- `reason`: 结束原因 (COMPLETED, CANCELLED, ERROR)
- `finalStats`: 最终统计数据
- `duration`: 会话持续时间(毫秒)

### 5. STATUS 消息 (服务端 → 客户端)

```json
{
  "type": "STATUS",
  "sessionId": "session-abc123",
  "timestamp": "2024-01-01T10:05:00Z",
  "data": {
    "oldStatus": "CREATED",
    "newStatus": "RUNNING",
    "currentFrame": 0,
    "totalFrames": 120
  }
}
```

### 6. DECISION 消息 (客户端 → 服务端)

```json
{
  "frameIndex": 30,
  "decisionType": "BUY",
  "price": 150.75,
  "quantity": 1,
  "timestampMs": 1704110730000,
  "clientId": "client-uuid-123"
}
```

**字段说明**:
- `decisionType`: BUY, SELL, SKIP
- `price`: 决策价格 (SKIP时可为null)
- `quantity`: 数量 (可配置，默认1)
- `timestampMs`: 客户端时间戳
- `clientId`: 客户端唯一标识 (用于幂等)

## 错误处理

### 错误消息格式

```json
{
  "type": "ERROR",
  "sessionId": "session-abc123",
  "timestamp": "2024-01-01T10:05:00Z",
  "error": {
    "code": "DECISION_TIMEOUT",
    "message": "决策超时，自动跳过",
    "details": {
      "frameIndex": 30,
      "timeoutSec": 10
    }
  }
}
```

### 常见错误代码

| 错误代码 | 描述 | 处理建议 |
|---------|------|---------|
| `DECISION_TIMEOUT` | 决策超时 | 自动跳过，继续游戏 |
| `INVALID_FRAME` | 无效帧索引 | 重新同步状态 |
| `DUPLICATE_DECISION` | 重复决策 | 忽略，幂等处理 |
| `SESSION_ENDED` | 会话已结束 | 停止发送决策 |
| `INVALID_PRICE` | 无效价格 | 重新提交决策 |
| `CONNECTION_LOST` | 连接丢失 | 触发重连机制 |

## 断线重连机制

### 1. 检测断线

```javascript
stompClient.onWebSocketClose = function() {
    console.log('WebSocket connection closed');
    startReconnection();
};

stompClient.onWebSocketError = function(error) {
    console.error('WebSocket error:', error);
    startReconnection();
};
```

### 2. 重连流程

```javascript
function startReconnection() {
    let reconnectAttempts = 0;
    const maxAttempts = 5;  // 可配置
    const baseDelay = 1000; // 可配置: 基础延迟(毫秒)
    
    function attemptReconnect() {
        if (reconnectAttempts >= maxAttempts) {
            console.error('Max reconnection attempts reached');
            return;
        }
        
        const delay = baseDelay * Math.pow(2, reconnectAttempts); // 指数退避
        setTimeout(() => {
            reconnectAttempts++;
            console.log(`Reconnection attempt ${reconnectAttempts}`);
            
            // 重新建立连接
            connect().then(() => {
                // 获取会话快照
                return fetchSessionSnapshot(sessionId);
            }).then((snapshot) => {
                // 恢复状态
                restoreSessionState(snapshot);
                reconnectAttempts = 0;
            }).catch(() => {
                attemptReconnect();
            });
        }, delay);
    }
    
    attemptReconnect();
}
```

### 3. 状态同步

```javascript
function fetchSessionSnapshot(sessionId) {
    return fetch(`/api/v1/sessions/${sessionId}/replay?fromFrame=${lastFrameIndex}`)
        .then(response => response.json())
        .then(data => {
            // 同步错过的帧数据
            data.frames.forEach(frame => {
                handleFrameMessage(frame);
            });
            return data;
        });
}
```

## 性能优化

### 1. 消息压缩 (可配置)

```javascript
// 启用消息压缩
stompClient.configure({
    debug: false,
    heartbeatIncoming: 30000,  // 可配置
    heartbeatOutgoing: 30000,  // 可配置
    reconnectDelay: 5000       // 可配置
});
```

### 2. 批量处理

```javascript
// 批量处理帧数据以提高性能
let frameBuffer = [];
const batchSize = 5;  // 可配置

function handleFrameMessage(frameData) {
    frameBuffer.push(frameData);
    
    if (frameBuffer.length >= batchSize) {
        processBatchFrames(frameBuffer);
        frameBuffer = [];
    }
}
```

### 3. 内存管理

```javascript
// 限制历史数据缓存大小
const maxHistorySize = 1000;  // 可配置

function addToHistory(frameData) {
    frameHistory.push(frameData);
    
    if (frameHistory.length > maxHistorySize) {
        frameHistory.shift(); // 移除最旧的数据
    }
}
```

## 监控指标

### WebSocket 连接指标

- `ws_active_connections` - 活跃连接数
- `ws_connection_duration_seconds` - 连接持续时间
- `ws_message_send_rate` - 消息发送速率
- `ws_message_receive_rate` - 消息接收速率
- `ws_reconnection_count` - 重连次数

### 消息处理指标

- `ws_frame_push_latency_ms` - 帧推送延迟
- `ws_decision_response_time_ms` - 决策响应时间
- `ws_message_queue_size` - 消息队列大小
- `ws_error_count` - 错误计数

## 安全考虑

### 1. 消息验证

- 所有消息都包含会话ID验证
- 客户端时间戳用于防重放攻击
- 决策消息包含客户端ID确保幂等性

### 2. 速率限制 (可配置)

- 每个连接最大消息发送速率: 10/秒
- 决策提交速率限制: 1/秒
- 连接数限制: 1000个并发连接

### 3. 数据保护

- 严禁向客户端发送未来窗口数据
- 所有价格数据仅包含当前和历史信息
- 关键点检测结果不提前暴露

## 示例代码

### 完整的客户端实现

```javascript
class TradingSimWebSocket {
    constructor(sessionId, config = {}) {
        this.sessionId = sessionId;
        this.config = {
            url: config.url || 'ws://localhost:8080/ws',
            reconnectAttempts: config.reconnectAttempts || 5,
            heartbeatInterval: config.heartbeatInterval || 30000,
            ...config
        };
        this.stompClient = null;
        this.isConnected = false;
        this.lastFrameIndex = -1;
    }
    
    connect() {
        return new Promise((resolve, reject) => {
            const socket = new SockJS(this.config.url);
            this.stompClient = Stomp.over(socket);
            
            this.stompClient.connect({}, 
                (frame) => {
                    this.isConnected = true;
                    this.subscribe();
                    resolve(frame);
                },
                (error) => {
                    this.isConnected = false;
                    reject(error);
                }
            );
        });
    }
    
    subscribe() {
        // 订阅帧数据
        this.stompClient.subscribe(
            `/topic/session.${this.sessionId}.frames`,
            (message) => this.handleMessage(JSON.parse(message.body))
        );
        
        // 订阅状态变化
        this.stompClient.subscribe(
            `/topic/session.${this.sessionId}.status`,
            (message) => this.handleStatusChange(JSON.parse(message.body))
        );
    }
    
    sendDecision(decision) {
        if (!this.isConnected) {
            throw new Error('WebSocket not connected');
        }
        
        this.stompClient.send(
            `/app/session.${this.sessionId}.decision`,
            {},
            JSON.stringify(decision)
        );
    }
    
    handleMessage(message) {
        this.lastFrameIndex = message.frameIndex;
        
        switch (message.type) {
            case 'KLINE':
                this.onKlineData(message);
                break;
            case 'PAUSE':
                this.onPause(message);
                break;
            case 'REWARD':
                this.onReward(message);
                break;
            case 'END':
                this.onSessionEnd(message);
                break;
            case 'ERROR':
                this.onError(message);
                break;
        }
    }
    
    // 事件处理器 - 由用户实现
    onKlineData(message) { /* 用户实现 */ }
    onPause(message) { /* 用户实现 */ }
    onReward(message) { /* 用户实现 */ }
    onSessionEnd(message) { /* 用户实现 */ }
    onError(message) { /* 用户实现 */ }
    onStatusChange(message) { /* 用户实现 */ }
}
```

---

**注意**: 所有时间戳均使用 UTC 时区，客户端需要根据本地时区进行转换。消息中标注"可配置"的参数可通过系统配置进行调整。