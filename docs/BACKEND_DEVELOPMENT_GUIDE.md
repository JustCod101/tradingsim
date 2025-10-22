# 后端开发手册

## 目录
- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [开发环境搭建](#开发环境搭建)
- [核心模块详解](#核心模块详解)
- [API开发指南](#api开发指南)
- [WebSocket开发指南](#websocket开发指南)
- [数据库设计](#数据库设计)
- [缓存策略](#缓存策略)
- [测试指南](#测试指南)
- [部署指南](#部署指南)
- [性能优化](#性能优化)
- [监控与日志](#监控与日志)
- [常见问题](#常见问题)

## 项目概述

交易模拟系统后端是一个基于Spring Boot的微服务架构应用，提供实时交易模拟、游戏会话管理、市场数据处理等核心功能。

### 主要功能
- 游戏会话管理
- 实时市场数据推送
- 交易决策处理
- 用户评分系统
- WebSocket实时通信
- 数据持久化

## 技术栈

### 核心框架
- **Spring Boot 3.x** - 主框架
- **Spring WebFlux** - 响应式Web框架
- **Spring Data JPA** - 数据访问层
- **Spring WebSocket** - WebSocket支持
- **Spring Security** - 安全框架

### 数据库
- **TimescaleDB** - 时序数据存储
- **PostgreSQL** - 关系型数据存储
- **Redis** - 缓存和会话存储

### 消息队列
- **Apache Kafka** - 消息流处理

### 监控工具
- **Micrometer** - 指标收集
- **Prometheus** - 监控数据存储
- **Grafana** - 监控面板

## 项目结构

```
backend/
├── src/main/java/com/tradingsim/
│   ├── api/                    # REST API控制器
│   │   ├── controller/         # 控制器
│   │   ├── dto/               # 数据传输对象
│   │   └── validation/        # 参数验证
│   ├── domain/                # 领域模型
│   │   ├── model/             # 实体类
│   │   ├── repository/        # 仓储接口
│   │   └── service/           # 领域服务
│   ├── infra/                 # 基础设施层
│   │   ├── config/            # 配置类
│   │   ├── persistence/       # 数据持久化
│   │   ├── cache/             # 缓存实现
│   │   ├── ws/                # WebSocket实现
│   │   └── external/          # 外部服务集成
│   ├── application/           # 应用服务层
│   │   ├── service/           # 应用服务
│   │   ├── usecase/           # 用例实现
│   │   └── event/             # 事件处理
│   └── spi/                   # 服务提供接口
│       ├── scoring/           # 评分规则SPI
│       ├── detection/         # 检测器SPI
│       └── market/            # 市场数据SPI
├── src/main/resources/
│   ├── application.yml        # 应用配置
│   ├── application-dev.yml    # 开发环境配置
│   ├── application-prod.yml   # 生产环境配置
│   └── db/migration/          # 数据库迁移脚本
└── src/test/                  # 测试代码
```

## 开发环境搭建

### 前置要求
- JDK 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 14+
- Redis 6+

### 环境配置

1. **克隆项目**
```bash
git clone <repository-url>
cd tradingsim/backend
```

2. **启动依赖服务**
```bash
# 启动数据库和缓存
docker-compose up -d postgres redis timescaledb
```

3. **配置数据库**
```bash
# 执行数据库迁移脚本
psql -h localhost -U tradingsim -d tradingsim_db -f scripts/sql/01_timescale_schema.sql
psql -h localhost -U tradingsim -d tradingsim_db -f scripts/sql/02_business_schema.sql
```

4. **配置应用**
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tradingsim_db
    username: tradingsim
    password: your_password
  redis:
    host: localhost
    port: 6379
```

5. **启动应用**
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

## 核心模块详解

### 1. 领域模型 (Domain Model)

#### GameSession - 游戏会话
```java
@Entity
@Table(name = "game_session")
public class GameSession {
    @Id
    private String sessionId;
    private Long segmentId;
    private String userId;
    private SessionStatus status;
    private Integer currentFrame;
    private Integer totalFrames;
    // ... 其他字段
}
```

#### GameDecision - 游戏决策
```java
@Entity
@Table(name = "game_decision")
public class GameDecision {
    @Id
    @GeneratedValue
    private Long decisionId;
    private String sessionId;
    private Integer frameIndex;
    private DecisionType decisionType;
    private BigDecimal decisionPrice;
    // ... 其他字段
}
```

#### OhlcvData - 市场数据
```java
@Entity
@Table(name = "ohlcv_data")
public class OhlcvData {
    @EmbeddedId
    private OhlcvId id;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;
}
```

### 2. 应用服务 (Application Service)

#### GameSessionService
```java
@Service
@Transactional
public class GameSessionService {
    
    public GameSession createSession(CreateSessionRequest request) {
        // 创建游戏会话逻辑
    }
    
    public void startSession(String sessionId) {
        // 启动会话逻辑
    }
    
    public void processDecision(String sessionId, DecisionRequest decision) {
        // 处理决策逻辑
    }
}
```

#### MarketDataService
```java
@Service
public class MarketDataService {
    
    public List<OhlcvData> getMarketData(String symbol, Instant start, Instant end) {
        // 获取市场数据
    }
    
    public void streamMarketData(String sessionId, Consumer<OhlcvData> callback) {
        // 流式推送市场数据
    }
}
```

### 3. SPI扩展机制

#### 评分规则SPI
```java
public interface ScoringRule {
    String getName();
    BigDecimal calculateScore(GameDecision decision, MarketContext context);
    boolean isApplicable(GameDecision decision);
}

@Component
public class ProfitBasedScoringRule implements ScoringRule {
    @Override
    public BigDecimal calculateScore(GameDecision decision, MarketContext context) {
        // 基于盈利的评分逻辑
    }
}
```

#### 检测器SPI
```java
public interface PatternDetector {
    String getPatternName();
    boolean detect(List<OhlcvData> data);
    double getConfidence();
}

@Component
public class TrendDetector implements PatternDetector {
    @Override
    public boolean detect(List<OhlcvData> data) {
        // 趋势检测逻辑
    }
}
```

## API开发指南

### REST API设计原则

1. **RESTful设计**
   - 使用标准HTTP方法 (GET, POST, PUT, DELETE)
   - 资源导向的URL设计
   - 统一的响应格式

2. **API版本控制**
```java
@RestController
@RequestMapping("/api/v1/sessions")
public class GameSessionController {
    // API实现
}
```

3. **统一响应格式**
```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String timestamp;
}
```

### 参数验证
```java
@PostMapping
public ResponseEntity<ApiResponse<GameSession>> createSession(
    @Valid @RequestBody CreateSessionRequest request) {
    // 实现逻辑
}

public class CreateSessionRequest {
    @NotNull
    @Size(min = 1, max = 64)
    private String userId;
    
    @NotNull
    @Positive
    private Long segmentId;
}
```

### 异常处理
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(ValidationException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(e.getMessage()));
    }
    
    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSessionNotFound(SessionNotFoundException e) {
        return ResponseEntity.notFound()
            .build();
    }
}
```

## WebSocket开发指南

### WebSocket配置
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new GameWebSocketHandler(), "/ws/game")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
```

### 消息类型定义
```java
// 会话状态消息
public class GameSessionMessage {
    private String type = "session_update";
    private String sessionId;
    private String status;
    private Integer currentFrame;
    private Double totalScore;
    private Instant timestamp;
}

// OHLCV数据消息
public class OhlcvMessage {
    private String type = "ohlcv_data";
    private String symbol;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;
    private Instant timestamp;
}
```

### WebSocket处理器
```java
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 连接建立处理
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 消息处理
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 连接关闭处理
    }
}
```

### 消息广播
```java
@Service
public class WebSocketBroadcastService {
    
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    
    public void broadcastToSession(String sessionId, Object message) {
        sessions.stream()
            .filter(session -> sessionId.equals(getSessionId(session)))
            .forEach(session -> sendMessage(session, message));
    }
    
    public void broadcastToAll(Object message) {
        sessions.forEach(session -> sendMessage(session, message));
    }
}
```

## 数据库设计

### 表结构设计

#### 游戏会话表
```sql
CREATE TABLE game_session (
    session_id VARCHAR(64) PRIMARY KEY,
    segment_id BIGINT NOT NULL,
    user_id VARCHAR(64),
    status VARCHAR(20) NOT NULL,
    current_frame INTEGER NOT NULL DEFAULT 0,
    total_frames INTEGER NOT NULL,
    total_score DECIMAL(10,2) DEFAULT 0,
    total_pnl DECIMAL(10,4) DEFAULT 0,
    decision_count INTEGER NOT NULL DEFAULT 0,
    timeout_count INTEGER NOT NULL DEFAULT 0,
    avg_response_time_ms BIGINT,
    seed_value BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

#### 决策记录表
```sql
CREATE TABLE game_decision (
    decision_id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    frame_index INTEGER NOT NULL,
    decision_type VARCHAR(10) NOT NULL,
    decision_price DECIMAL(10,4),
    quantity INTEGER NOT NULL DEFAULT 1,
    pnl DECIMAL(10,4),
    score DECIMAL(8,2),
    response_time_ms BIGINT,
    client_id VARCHAR(64),
    decision_timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (session_id) REFERENCES game_session(session_id)
);
```

#### 时序数据表 (TimescaleDB)
```sql
CREATE TABLE ohlcv_data (
    symbol VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    open DECIMAL(10,4) NOT NULL,
    high DECIMAL(10,4) NOT NULL,
    low DECIMAL(10,4) NOT NULL,
    close DECIMAL(10,4) NOT NULL,
    volume BIGINT NOT NULL,
    PRIMARY KEY (symbol, timestamp)
);

-- 创建时序表
SELECT create_hypertable('ohlcv_data', 'timestamp');
```

### 索引优化
```sql
-- 会话查询索引
CREATE INDEX idx_game_session_user_status ON game_session(user_id, status);
CREATE INDEX idx_game_session_created_at ON game_session(created_at);

-- 决策查询索引
CREATE INDEX idx_game_decision_session_frame ON game_decision(session_id, frame_index);
CREATE INDEX idx_game_decision_timestamp ON game_decision(decision_timestamp);

-- 时序数据索引
CREATE INDEX idx_ohlcv_symbol_time ON ohlcv_data(symbol, timestamp DESC);
```

## 缓存策略

### Redis配置
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

### 缓存使用
```java
@Service
public class MarketDataService {
    
    @Cacheable(value = "market-data", key = "#symbol + ':' + #date")
    public List<OhlcvData> getDailyData(String symbol, LocalDate date) {
        // 从数据库获取数据
    }
    
    @CacheEvict(value = "market-data", allEntries = true)
    public void clearCache() {
        // 清除缓存
    }
}
```

### 会话状态缓存
```java
@Component
public class SessionCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void cacheSession(GameSession session) {
        String key = "session:" + session.getSessionId();
        redisTemplate.opsForValue().set(key, session, Duration.ofHours(1));
    }
    
    public GameSession getSession(String sessionId) {
        String key = "session:" + sessionId;
        return (GameSession) redisTemplate.opsForValue().get(key);
    }
}
```

## 测试指南

### 单元测试
```java
@ExtendWith(MockitoExtension.class)
class GameSessionServiceTest {
    
    @Mock
    private GameSessionRepository sessionRepository;
    
    @InjectMocks
    private GameSessionService sessionService;
    
    @Test
    void shouldCreateSession() {
        // Given
        CreateSessionRequest request = new CreateSessionRequest();
        request.setUserId("user123");
        request.setSegmentId(1L);
        
        // When
        GameSession session = sessionService.createSession(request);
        
        // Then
        assertThat(session.getUserId()).isEqualTo("user123");
        assertThat(session.getStatus()).isEqualTo(SessionStatus.CREATED);
    }
}
```

### 集成测试
```java
@SpringBootTest
@Testcontainers
class GameSessionIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateAndRetrieveSession() {
        // 集成测试逻辑
    }
}
```

### WebSocket测试
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Test
    void shouldReceiveSessionUpdates() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient());
        StompSession session = stompClient.connect("ws://localhost:" + port + "/ws", 
            new StompSessionHandlerAdapter() {}).get();
        
        // WebSocket测试逻辑
    }
}
```

## 部署指南

### Docker化部署
```dockerfile
FROM openjdk:17-jre-slim

WORKDIR /app
COPY target/tradingsim-backend.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose
```yaml
version: '3.8'
services:
  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=postgres
      - REDIS_HOST=redis
    depends_on:
      - postgres
      - redis
  
  postgres:
    image: timescale/timescaledb:latest-pg14
    environment:
      POSTGRES_DB: tradingsim_db
      POSTGRES_USER: tradingsim
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
  
  redis:
    image: redis:6-alpine
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

### 生产环境配置
```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    password: ${REDIS_PASSWORD}
    lettuce:
      pool:
        max-active: 20
        max-idle: 10

logging:
  level:
    com.tradingsim: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 性能优化

### 数据库优化
1. **连接池配置**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

2. **查询优化**
```java
// 使用批量查询
@Query("SELECT s FROM GameSession s WHERE s.userId = :userId AND s.status IN :statuses")
List<GameSession> findByUserIdAndStatuses(@Param("userId") String userId, 
                                         @Param("statuses") List<SessionStatus> statuses);

// 使用分页查询
Page<GameDecision> findBySessionIdOrderByFrameIndex(String sessionId, Pageable pageable);
```

### 缓存优化
```java
// 多级缓存
@Cacheable(value = "market-data", key = "#symbol + ':' + #timeframe")
public List<OhlcvData> getMarketData(String symbol, String timeframe) {
    return marketDataRepository.findBySymbolAndTimeframe(symbol, timeframe);
}

// 缓存预热
@EventListener(ApplicationReadyEvent.class)
public void warmUpCache() {
    // 预加载热点数据
}
```

### 异步处理
```java
@Async
@EventListener
public void handleDecisionEvent(DecisionProcessedEvent event) {
    // 异步处理决策事件
}

@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

## 监控与日志

### 指标监控
```java
@Component
public class GameMetrics {
    
    private final Counter sessionCreatedCounter;
    private final Timer decisionProcessingTimer;
    private final Gauge activeSessionsGauge;
    
    public GameMetrics(MeterRegistry meterRegistry) {
        this.sessionCreatedCounter = Counter.builder("game.sessions.created")
            .description("Number of game sessions created")
            .register(meterRegistry);
        
        this.decisionProcessingTimer = Timer.builder("game.decision.processing.time")
            .description("Time taken to process decisions")
            .register(meterRegistry);
    }
    
    public void recordSessionCreated() {
        sessionCreatedCounter.increment();
    }
    
    public void recordDecisionProcessingTime(Duration duration) {
        decisionProcessingTimer.record(duration);
    }
}
```

### 日志配置
```yaml
logging:
  level:
    com.tradingsim: INFO
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId}] %logger{36} - %msg%n"
  file:
    name: logs/tradingsim-backend.log
    max-size: 100MB
    max-history: 30
```

### 健康检查
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "Available")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("database", "Unavailable")
                .withException(e)
                .build();
        }
        return Health.down().build();
    }
}
```

## 常见问题

### Q1: 如何处理高并发场景下的会话管理？
A: 使用Redis分布式锁和乐观锁机制：
```java
@Transactional
public void processDecision(String sessionId, DecisionRequest request) {
    String lockKey = "session:lock:" + sessionId;
    Boolean acquired = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, "locked", Duration.ofSeconds(10));
    
    if (!acquired) {
        throw new ConcurrentModificationException("Session is being processed");
    }
    
    try {
        // 处理决策逻辑
    } finally {
        redisTemplate.delete(lockKey);
    }
}
```

### Q2: 如何优化大量历史数据的查询性能？
A: 使用TimescaleDB的时间分区和数据压缩：
```sql
-- 设置数据保留策略
SELECT add_retention_policy('ohlcv_data', INTERVAL '1 year');

-- 启用数据压缩
ALTER TABLE ohlcv_data SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'symbol'
);

-- 设置压缩策略
SELECT add_compression_policy('ohlcv_data', INTERVAL '7 days');
```

### Q3: 如何处理WebSocket连接断开重连？
A: 实现心跳机制和自动重连：
```java
@Scheduled(fixedRate = 30000)
public void sendHeartbeat() {
    sessions.forEach(session -> {
        if (session.isOpen()) {
            sendMessage(session, new HeartbeatMessage());
        } else {
            sessions.remove(session);
        }
    });
}
```

### Q4: 如何进行数据库迁移？
A: 使用Flyway进行版本化迁移：
```sql
-- V1__Initial_schema.sql
CREATE TABLE game_session (...);

-- V2__Add_scoring_fields.sql
ALTER TABLE game_session ADD COLUMN total_score DECIMAL(10,2);
```

---

## 参考资料
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [TimescaleDB文档](https://docs.timescale.com/)
- [Redis文档](https://redis.io/documentation)
- [项目API文档](./openapi.yaml)
- [WebSocket协议文档](./websocket-protocol.md)