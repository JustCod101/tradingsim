# TradingSim API 开发指南

## 📋 目录

- [API设计原则](#api设计原则)
- [RESTful API规范](#restful-api规范)
- [WebSocket协议设计](#websocket协议设计)
- [数据模型设计](#数据模型设计)
- [错误处理](#错误处理)
- [安全实践](#安全实践)
- [性能优化](#性能优化)
- [测试策略](#测试策略)
- [文档规范](#文档规范)
- [最佳实践](#最佳实践)

---

## API设计原则

### 1. 契约优先设计 (Contract-First)
```yaml
设计流程:
  1. 定义API规范 (OpenAPI)
  2. 生成代码骨架
  3. 实现业务逻辑
  4. 编写测试用例
  5. 部署和监控

优势:
  - 前后端并行开发
  - 接口一致性保证
  - 自动化测试覆盖
  - 文档自动生成
```

### 2. 领域驱动设计 (DDD)
```yaml
核心概念:
  - 聚合根 (Aggregate Root)
  - 值对象 (Value Object)
  - 领域服务 (Domain Service)
  - 仓储模式 (Repository Pattern)

API映射:
  - 聚合根 → REST资源
  - 领域服务 → API操作
  - 值对象 → DTO
  - 事件 → WebSocket消息
```

### 3. CQRS模式
```yaml
命令端 (Command):
  - 处理写操作
  - 确保数据一致性
  - 返回操作结果

查询端 (Query):
  - 处理读操作
  - 优化查询性能
  - 支持复杂查询
```

---

## RESTful API规范

### 1. URL设计规范

#### 资源命名
```yaml
规则:
  - 使用名词，避免动词
  - 使用复数形式
  - 使用小写字母
  - 使用连字符分隔单词

示例:
  ✅ /api/v1/users
  ✅ /api/v1/game-sessions
  ✅ /api/v1/trading-decisions
  
  ❌ /api/v1/getUsers
  ❌ /api/v1/User
  ❌ /api/v1/game_sessions
```

#### 层级关系
```yaml
嵌套资源:
  - 最多3层嵌套
  - 表达清晰的从属关系
  - 避免过深的嵌套

示例:
  ✅ /api/v1/users/{userId}/game-sessions
  ✅ /api/v1/game-sessions/{sessionId}/decisions
  
  ❌ /api/v1/users/{userId}/game-sessions/{sessionId}/decisions/{decisionId}/details
```

### 2. HTTP方法使用

#### 标准方法
```yaml
GET:
  用途: 获取资源
  特点: 幂等、安全、可缓存
  示例:
    - GET /api/v1/users/{id}
    - GET /api/v1/game-sessions?status=active

POST:
  用途: 创建资源
  特点: 非幂等、不安全
  示例:
    - POST /api/v1/users
    - POST /api/v1/game-sessions

PUT:
  用途: 完整更新资源
  特点: 幂等、不安全
  示例:
    - PUT /api/v1/users/{id}
    - PUT /api/v1/game-sessions/{id}

PATCH:
  用途: 部分更新资源
  特点: 非幂等、不安全
  示例:
    - PATCH /api/v1/users/{id}
    - PATCH /api/v1/game-sessions/{id}/status

DELETE:
  用途: 删除资源
  特点: 幂等、不安全
  示例:
    - DELETE /api/v1/users/{id}
    - DELETE /api/v1/game-sessions/{id}
```

### 3. 状态码规范

#### 成功响应
```yaml
200 OK:
  用途: 请求成功
  场景: GET、PUT、PATCH成功

201 Created:
  用途: 资源创建成功
  场景: POST成功
  要求: 返回Location头

204 No Content:
  用途: 操作成功但无返回内容
  场景: DELETE成功、PATCH成功
```

#### 客户端错误
```yaml
400 Bad Request:
  用途: 请求参数错误
  场景: 参数验证失败

401 Unauthorized:
  用途: 未认证
  场景: 缺少或无效的认证信息

403 Forbidden:
  用途: 无权限
  场景: 认证成功但权限不足

404 Not Found:
  用途: 资源不存在
  场景: 请求的资源不存在

409 Conflict:
  用途: 资源冲突
  场景: 重复创建、并发修改

422 Unprocessable Entity:
  用途: 语义错误
  场景: 业务规则验证失败
```

#### 服务器错误
```yaml
500 Internal Server Error:
  用途: 服务器内部错误
  场景: 未预期的系统错误

502 Bad Gateway:
  用途: 网关错误
  场景: 上游服务不可用

503 Service Unavailable:
  用途: 服务不可用
  场景: 系统维护、过载
```

### 4. 请求/响应格式

#### 请求格式
```json
// POST /api/v1/game-sessions
{
  "segmentId": "AAPL_2023_Q1",
  "difficulty": "MEDIUM",
  "settings": {
    "initialCapital": 100000,
    "maxPositions": 5,
    "riskLevel": "MODERATE"
  }
}
```

#### 响应格式
```json
// 成功响应
{
  "success": true,
  "data": {
    "id": "session_123",
    "segmentId": "AAPL_2023_Q1",
    "status": "CREATED",
    "createdAt": "2024-01-15T10:30:00Z",
    "settings": {
      "initialCapital": 100000,
      "maxPositions": 5,
      "riskLevel": "MODERATE"
    }
  },
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req_456"
  }
}

// 错误响应
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request parameters",
    "details": [
      {
        "field": "initialCapital",
        "message": "Must be greater than 0"
      }
    ]
  },
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req_456"
  }
}
```

### 5. 分页和过滤

#### 分页参数
```yaml
查询参数:
  - page: 页码 (从1开始)
  - size: 每页大小 (默认20，最大100)
  - sort: 排序字段和方向

示例:
  GET /api/v1/game-sessions?page=1&size=20&sort=createdAt,desc
```

#### 分页响应
```json
{
  "success": true,
  "data": [
    // 数据项
  ],
  "pagination": {
    "page": 1,
    "size": 20,
    "total": 150,
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

#### 过滤参数
```yaml
基础过滤:
  - status: 状态过滤
  - createdAfter: 创建时间过滤
  - createdBefore: 创建时间过滤

示例:
  GET /api/v1/game-sessions?status=ACTIVE&createdAfter=2024-01-01
```

---

## WebSocket协议设计

### 1. 连接管理

#### 连接建立
```javascript
// 客户端连接
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
  'Authorization': 'Bearer ' + token
}, function(frame) {
  console.log('Connected: ' + frame);
});
```

#### 主题订阅
```javascript
// 订阅游戏会话数据
stompClient.subscribe('/topic/session.' + sessionId + '.frames', function(message) {
  const frame = JSON.parse(message.body);
  handleKlineFrame(frame);
});

// 订阅游戏事件
stompClient.subscribe('/topic/session.' + sessionId + '.events', function(message) {
  const event = JSON.parse(message.body);
  handleGameEvent(event);
});
```

### 2. 消息格式

#### 消息结构
```json
{
  "type": "KLINE_FRAME",
  "sessionId": "session_123",
  "timestamp": "2024-01-15T10:30:00Z",
  "sequenceNumber": 1001,
  "data": {
    // 具体数据内容
  }
}
```

#### K线数据消息
```json
{
  "type": "KLINE_FRAME",
  "sessionId": "session_123",
  "timestamp": "2024-01-15T10:30:00Z",
  "sequenceNumber": 1001,
  "data": {
    "symbol": "AAPL",
    "timestamp": "2023-03-15T09:30:00Z",
    "open": 150.25,
    "high": 151.80,
    "low": 149.90,
    "close": 151.45,
    "volume": 1250000,
    "isKeypoint": false
  }
}
```

#### 关键点暂停消息
```json
{
  "type": "KEYPOINT_PAUSE",
  "sessionId": "session_123",
  "timestamp": "2024-01-15T10:30:00Z",
  "sequenceNumber": 1002,
  "data": {
    "keypointId": "kp_789",
    "reason": "SIGNIFICANT_PRICE_MOVEMENT",
    "timeoutSeconds": 30,
    "currentPrice": 151.45,
    "priceChange": 2.15,
    "priceChangePercent": 1.44
  }
}
```

#### 决策消息
```json
{
  "type": "TRADING_DECISION",
  "sessionId": "session_123",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "action": "BUY",
    "quantity": 100,
    "price": 151.45,
    "reason": "Technical breakout pattern"
  }
}
```

### 3. 错误处理

#### 连接错误
```javascript
stompClient.connect({}, function(frame) {
  // 连接成功
}, function(error) {
  console.error('Connection error:', error);
  // 实现重连逻辑
  setTimeout(reconnect, 5000);
});
```

#### 消息错误
```json
{
  "type": "ERROR",
  "sessionId": "session_123",
  "timestamp": "2024-01-15T10:30:00Z",
  "error": {
    "code": "INVALID_DECISION",
    "message": "Decision timeout exceeded",
    "details": {
      "timeoutSeconds": 30,
      "receivedAt": "2024-01-15T10:30:35Z"
    }
  }
}
```

---

## 数据模型设计

### 1. 实体设计原则

#### 聚合根设计
```java
@Entity
@Table(name = "game_sessions")
public class GameSession {
    @Id
    private String id;
    
    @Embedded
    private UserId userId;
    
    @Embedded
    private SegmentId segmentId;
    
    @Enumerated(EnumType.STRING)
    private GameStatus status;
    
    @Embedded
    private GameSettings settings;
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<TradingDecision> decisions;
    
    // 领域方法
    public void startGame() {
        if (this.status != GameStatus.CREATED) {
            throw new IllegalStateException("Game already started");
        }
        this.status = GameStatus.ACTIVE;
        // 发布领域事件
        DomainEvents.publish(new GameStartedEvent(this.id));
    }
}
```

#### 值对象设计
```java
@Embeddable
public class Money {
    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount;
    
    @Column(name = "currency", length = 3)
    private String currency;
    
    // 不可变对象
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

### 2. DTO设计

#### 请求DTO
```java
@Data
@Valid
public class CreateGameSessionRequest {
    @NotBlank(message = "Segment ID is required")
    private String segmentId;
    
    @NotNull(message = "Difficulty is required")
    private GameDifficulty difficulty;
    
    @Valid
    @NotNull(message = "Settings are required")
    private GameSettingsDto settings;
}

@Data
@Valid
public class GameSettingsDto {
    @DecimalMin(value = "1000.0", message = "Initial capital must be at least 1000")
    private BigDecimal initialCapital;
    
    @Min(value = 1, message = "Max positions must be at least 1")
    @Max(value = 10, message = "Max positions cannot exceed 10")
    private Integer maxPositions;
    
    @NotNull(message = "Risk level is required")
    private RiskLevel riskLevel;
}
```

#### 响应DTO
```java
@Data
@Builder
public class GameSessionResponse {
    private String id;
    private String segmentId;
    private GameStatus status;
    private GameSettingsDto settings;
    private PortfolioSummaryDto portfolio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@Builder
public class PortfolioSummaryDto {
    private BigDecimal totalValue;
    private BigDecimal cashBalance;
    private BigDecimal unrealizedPnl;
    private BigDecimal realizedPnl;
    private List<PositionDto> positions;
}
```

### 3. 数据验证

#### 参数验证
```java
@RestController
@RequestMapping("/api/v1/game-sessions")
@Validated
public class GameSessionController {
    
    @PostMapping
    public ResponseEntity<ApiResponse<GameSessionResponse>> createSession(
            @Valid @RequestBody CreateGameSessionRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        // 业务逻辑
    }
    
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<GameSessionResponse>> getSession(
            @PathVariable @Pattern(regexp = "^session_[a-zA-Z0-9]+$") String sessionId,
            @AuthenticationPrincipal UserPrincipal user) {
        // 业务逻辑
    }
}
```

#### 自定义验证器
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SegmentIdValidator.class)
public @interface ValidSegmentId {
    String message() default "Invalid segment ID format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class SegmentIdValidator implements ConstraintValidator<ValidSegmentId, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        return value.matches("^[A-Z]+_\\d{4}_Q[1-4]$");
    }
}
```

---

## 错误处理

### 1. 异常层次结构

#### 业务异常
```java
// 基础业务异常
public abstract class BusinessException extends RuntimeException {
    private final String errorCode;
    private final Object[] args;
    
    protected BusinessException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }
}

// 具体业务异常
public class GameSessionNotFoundException extends BusinessException {
    public GameSessionNotFoundException(String sessionId) {
        super("GAME_SESSION_NOT_FOUND", "Game session not found: " + sessionId, sessionId);
    }
}

public class InvalidTradingDecisionException extends BusinessException {
    public InvalidTradingDecisionException(String reason) {
        super("INVALID_TRADING_DECISION", "Invalid trading decision: " + reason, reason);
    }
}
```

#### 系统异常
```java
public class SystemException extends RuntimeException {
    private final String errorCode;
    
    public SystemException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}

public class ExternalServiceException extends SystemException {
    public ExternalServiceException(String service, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR", "External service error: " + service, cause);
    }
}
```

### 2. 全局异常处理

#### 异常处理器
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        log.warn("Business exception: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .build();
                
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .error(error)
                .meta(createMeta(request))
                .build();
                
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ErrorDetail> details = fieldErrors.stream()
                .map(error -> ErrorDetail.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());
                
        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .details(details)
                .build();
                
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .error(error)
                .meta(createMeta(request))
                .build();
                
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        log.error("Unexpected error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .build();
                
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .error(error)
                .meta(createMeta(request))
                .build();
                
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

### 3. 错误码规范

#### 错误码分类
```yaml
业务错误 (4000-4999):
  4001: USER_NOT_FOUND
  4002: GAME_SESSION_NOT_FOUND
  4003: INVALID_TRADING_DECISION
  4004: INSUFFICIENT_BALANCE
  4005: GAME_ALREADY_ENDED

验证错误 (5000-5999):
  5001: VALIDATION_ERROR
  5002: INVALID_PARAMETER
  5003: MISSING_REQUIRED_FIELD
  5004: INVALID_FORMAT

系统错误 (6000-6999):
  6001: DATABASE_ERROR
  6002: EXTERNAL_SERVICE_ERROR
  6003: CACHE_ERROR
  6004: MESSAGE_QUEUE_ERROR

安全错误 (7000-7999):
  7001: AUTHENTICATION_FAILED
  7002: AUTHORIZATION_FAILED
  7003: TOKEN_EXPIRED
  7004: RATE_LIMIT_EXCEEDED
```

---

## 安全实践

### 1. 认证授权

#### JWT Token实现
```java
@Service
public class JwtTokenService {
    
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    
    public String generateToken(UserPrincipal user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofHours(24));
        
        return JWT.create()
                .withIssuer("tradingsim")
                .withSubject(user.getUserId())
                .withAudience("tradingsim-api")
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiry))
                .withClaim("username", user.getUsername())
                .withClaim("roles", user.getRoles())
                .sign(Algorithm.RSA256(publicKey, privateKey));
    }
    
    public DecodedJWT verifyToken(String token) {
        return JWT.require(Algorithm.RSA256(publicKey, privateKey))
                .withIssuer("tradingsim")
                .withAudience("tradingsim-api")
                .build()
                .verify(token);
    }
}
```

#### 权限控制
```java
@RestController
@RequestMapping("/api/v1/game-sessions")
public class GameSessionController {
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<GameSessionResponse>> createSession(
            @Valid @RequestBody CreateGameSessionRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        // 业务逻辑
    }
    
    @GetMapping("/{sessionId}")
    @PreAuthorize("hasRole('USER') and @gameSessionService.isOwner(#sessionId, authentication.name)")
    public ResponseEntity<ApiResponse<GameSessionResponse>> getSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserPrincipal user) {
        // 业务逻辑
    }
}
```

### 2. 输入验证

#### 参数清理
```java
@Component
public class InputSanitizer {
    
    private final Policy policy;
    
    public InputSanitizer() {
        this.policy = new PolicyFactory().toPolicy();
    }
    
    public String sanitizeHtml(String input) {
        if (input == null) return null;
        return policy.sanitize(input);
    }
    
    public String sanitizeSql(String input) {
        if (input == null) return null;
        return input.replaceAll("[';\"\\\\]", "");
    }
}
```

#### SQL注入防护
```java
@Repository
public class GameSessionRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // 使用参数化查询
    public List<GameSession> findByUserAndStatus(String userId, GameStatus status) {
        String jpql = "SELECT gs FROM GameSession gs WHERE gs.userId = :userId AND gs.status = :status";
        return entityManager.createQuery(jpql, GameSession.class)
                .setParameter("userId", userId)
                .setParameter("status", status)
                .getResultList();
    }
    
    // 使用Criteria API
    public List<GameSession> findByComplexCriteria(GameSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameSession> query = cb.createQuery(GameSession.class);
        Root<GameSession> root = query.from(GameSession.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (criteria.getUserId() != null) {
            predicates.add(cb.equal(root.get("userId"), criteria.getUserId()));
        }
        
        if (criteria.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        
        return entityManager.createQuery(query).getResultList();
    }
}
```

### 3. 访问控制

#### 限流实现
```java
@Component
public class RateLimitingFilter implements Filter {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitProperties properties;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientId = getClientId(httpRequest);
        String key = "rate_limit:" + clientId;
        
        String currentCount = redisTemplate.opsForValue().get(key);
        int count = currentCount != null ? Integer.parseInt(currentCount) : 0;
        
        if (count >= properties.getMaxRequests()) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.getWriter().write("Rate limit exceeded");
            return;
        }
        
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofMinutes(properties.getWindowMinutes()));
        
        chain.doFilter(request, response);
    }
}
```

---

## 性能优化

### 1. 数据库优化

#### 查询优化
```java
@Repository
public class OptimizedGameSessionRepository {
    
    // 使用索引提示
    @Query(value = "SELECT * FROM game_sessions USE INDEX (idx_user_status) " +
                   "WHERE user_id = ?1 AND status = ?2", nativeQuery = true)
    List<GameSession> findByUserAndStatusOptimized(String userId, String status);
    
    // 批量查询
    @Query("SELECT gs FROM GameSession gs WHERE gs.id IN :ids")
    List<GameSession> findByIds(@Param("ids") List<String> ids);
    
    // 投影查询
    @Query("SELECT new com.tradingsim.dto.GameSessionSummary(gs.id, gs.status, gs.createdAt) " +
           "FROM GameSession gs WHERE gs.userId = :userId")
    List<GameSessionSummary> findSummariesByUser(@Param("userId") String userId);
    
    // 分页查询
    @Query("SELECT gs FROM GameSession gs WHERE gs.userId = :userId ORDER BY gs.createdAt DESC")
    Page<GameSession> findByUserPaged(@Param("userId") String userId, Pageable pageable);
}
```

#### 缓存策略
```java
@Service
@CacheConfig(cacheNames = "gameSession")
public class GameSessionService {
    
    @Cacheable(key = "#sessionId")
    public GameSessionResponse getSession(String sessionId) {
        // 查询数据库
    }
    
    @CacheEvict(key = "#sessionId")
    public void updateSession(String sessionId, UpdateGameSessionRequest request) {
        // 更新数据库
    }
    
    @Caching(evict = {
        @CacheEvict(key = "#sessionId"),
        @CacheEvict(cacheNames = "userSessions", key = "#userId")
    })
    public void deleteSession(String sessionId, String userId) {
        // 删除数据库记录
    }
}
```

### 2. 异步处理

#### 异步方法
```java
@Service
public class AsyncGameService {
    
    @Async("gameTaskExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3)
    public CompletableFuture<Void> processGameResult(String sessionId) {
        try {
            // 处理游戏结果
            calculateFinalScore(sessionId);
            updateLeaderboard(sessionId);
            sendNotification(sessionId);
            
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to process game result for session: {}", sessionId, e);
            throw e;
        }
    }
    
    @Recover
    public CompletableFuture<Void> recoverProcessGameResult(Exception ex, String sessionId) {
        log.error("Failed to process game result after retries: {}", sessionId, ex);
        // 发送到死信队列或记录失败日志
        return CompletableFuture.completedFuture(null);
    }
}
```

#### 线程池配置
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean("gameTaskExecutor")
    public TaskExecutor gameTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("game-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

### 3. 连接池优化

#### 数据库连接池
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
      validation-timeout: 5000
      leak-detection-threshold: 60000
```

#### Redis连接池
```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 2000ms
```

---

## 测试策略

### 1. 单元测试

#### Controller测试
```java
@WebMvcTest(GameSessionController.class)
class GameSessionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private GameSessionService gameSessionService;
    
    @Test
    void createSession_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        CreateGameSessionRequest request = CreateGameSessionRequest.builder()
                .segmentId("AAPL_2023_Q1")
                .difficulty(GameDifficulty.MEDIUM)
                .settings(GameSettingsDto.builder()
                        .initialCapital(new BigDecimal("100000"))
                        .maxPositions(5)
                        .riskLevel(RiskLevel.MODERATE)
                        .build())
                .build();
                
        GameSessionResponse response = GameSessionResponse.builder()
                .id("session_123")
                .segmentId("AAPL_2023_Q1")
                .status(GameStatus.CREATED)
                .build();
                
        when(gameSessionService.createSession(any(), any())).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/v1/game-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("session_123"))
                .andExpect(jsonPath("$.data.status").value("CREATED"));
    }
}
```

#### Service测试
```java
@ExtendWith(MockitoExtension.class)
class GameSessionServiceTest {
    
    @Mock
    private GameSessionRepository repository;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private GameSessionService gameSessionService;
    
    @Test
    void createSession_ValidRequest_ReturnsSession() {
        // Given
        String userId = "user_123";
        CreateGameSessionRequest request = createValidRequest();
        GameSession savedSession = createGameSession();
        
        when(userService.findById(userId)).thenReturn(createUser());
        when(repository.save(any(GameSession.class))).thenReturn(savedSession);
        
        // When
        GameSessionResponse response = gameSessionService.createSession(request, userId);
        
        // Then
        assertThat(response.getId()).isEqualTo(savedSession.getId());
        assertThat(response.getStatus()).isEqualTo(GameStatus.CREATED);
        verify(repository).save(any(GameSession.class));
    }
}
```

### 2. 集成测试

#### API集成测试
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class GameSessionIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private GameSessionRepository repository;
    
    @Test
    void createAndGetSession_FullFlow_Success() {
        // Given
        CreateGameSessionRequest request = createValidRequest();
        String token = generateValidToken();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<CreateGameSessionRequest> entity = new HttpEntity<>(request, headers);
        
        // When - Create session
        ResponseEntity<ApiResponse<GameSessionResponse>> createResponse = 
                restTemplate.exchange("/api/v1/game-sessions", HttpMethod.POST, entity, 
                        new ParameterizedTypeReference<ApiResponse<GameSessionResponse>>() {});
        
        // Then - Verify creation
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody().isSuccess()).isTrue();
        
        String sessionId = createResponse.getBody().getData().getId();
        
        // When - Get session
        ResponseEntity<ApiResponse<GameSessionResponse>> getResponse = 
                restTemplate.exchange("/api/v1/game-sessions/" + sessionId, HttpMethod.GET, 
                        new HttpEntity<>(headers), 
                        new ParameterizedTypeReference<ApiResponse<GameSessionResponse>>() {});
        
        // Then - Verify retrieval
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getData().getId()).isEqualTo(sessionId);
    }
}
```

### 3. WebSocket测试

#### WebSocket集成测试
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    private StompSession stompSession;
    private final BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);
    
    @BeforeEach
    void setup() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        
        StompSessionHandler sessionHandler = new TestStompSessionHandler();
        stompSession = stompClient.connect("ws://localhost:" + port + "/ws", sessionHandler)
                .get(5, TimeUnit.SECONDS);
    }
    
    @Test
    void subscribeToGameSession_ReceivesKlineFrames() throws Exception {
        // Given
        String sessionId = "session_123";
        
        // When
        stompSession.subscribe("/topic/session." + sessionId + ".frames", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }
            
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.offer((String) payload);
            }
        });
        
        // Trigger frame sending
        stompSession.send("/app/session." + sessionId + ".start", "");
        
        // Then
        String receivedFrame = blockingQueue.poll(10, TimeUnit.SECONDS);
        assertThat(receivedFrame).isNotNull();
        
        KlineFrame frame = objectMapper.readValue(receivedFrame, KlineFrame.class);
        assertThat(frame.getType()).isEqualTo("KLINE_FRAME");
        assertThat(frame.getSessionId()).isEqualTo(sessionId);
    }
}
```

---

## 文档规范

### 1. OpenAPI规范

#### API文档结构
```yaml
openapi: 3.0.3
info:
  title: TradingSim API
  description: 交易模拟系统API文档
  version: 1.0.0
  contact:
    name: TradingSim Team
    email: dev@tradingsim.com
  license:
    name: MIT
    url: https://opensource.org/licenses/MIT

servers:
  - url: http://localhost:8080
    description: 开发环境
  - url: https://api.tradingsim.com
    description: 生产环境

paths:
  /api/v1/game-sessions:
    post:
      summary: 创建游戏会话
      description: 创建一个新的交易模拟游戏会话
      operationId: createGameSession
      tags:
        - Game Sessions
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateGameSessionRequest'
            examples:
              basic:
                summary: 基础游戏会话
                value:
                  segmentId: "AAPL_2023_Q1"
                  difficulty: "MEDIUM"
                  settings:
                    initialCapital: 100000
                    maxPositions: 5
                    riskLevel: "MODERATE"
      responses:
        '201':
          description: 游戏会话创建成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/GameSessionResponse'
        '400':
          description: 请求参数错误
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
```

#### 数据模型定义
```yaml
components:
  schemas:
    CreateGameSessionRequest:
      type: object
      required:
        - segmentId
        - difficulty
        - settings
      properties:
        segmentId:
          type: string
          pattern: '^[A-Z]+_\d{4}_Q[1-4]$'
          description: 数据段ID
          example: "AAPL_2023_Q1"
        difficulty:
          $ref: '#/components/schemas/GameDifficulty'
        settings:
          $ref: '#/components/schemas/GameSettings'
    
    GameSessionResponse:
      type: object
      properties:
        id:
          type: string
          description: 游戏会话ID
          example: "session_123"
        segmentId:
          type: string
          description: 数据段ID
          example: "AAPL_2023_Q1"
        status:
          $ref: '#/components/schemas/GameStatus'
        settings:
          $ref: '#/components/schemas/GameSettings'
        portfolio:
          $ref: '#/components/schemas/PortfolioSummary'
        createdAt:
          type: string
          format: date-time
          description: 创建时间
        updatedAt:
          type: string
          format: date-time
          description: 更新时间
```

### 2. 代码注释规范

#### Controller注释
```java
/**
 * 游戏会话管理控制器
 * 
 * 提供游戏会话的创建、查询、更新和删除功能。
 * 支持实时游戏数据推送和交易决策处理。
 * 
 * @author TradingSim Team
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/game-sessions")
@Tag(name = "Game Sessions", description = "游戏会话管理API")
public class GameSessionController {
    
    /**
     * 创建新的游戏会话
     * 
     * 根据提供的参数创建一个新的交易模拟游戏会话。
     * 会话创建后处于CREATED状态，需要调用启动接口开始游戏。
     * 
     * @param request 创建游戏会话的请求参数
     * @param user 当前认证用户信息
     * @return 创建成功的游戏会话信息
     * @throws ValidationException 当请求参数验证失败时
     * @throws BusinessException 当业务规则验证失败时
     */
    @PostMapping
    @Operation(summary = "创建游戏会话", description = "创建一个新的交易模拟游戏会话")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    public ResponseEntity<ApiResponse<GameSessionResponse>> createSession(
            @Valid @RequestBody CreateGameSessionRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        // 实现逻辑
    }
}
```

#### Service注释
```java
/**
 * 游戏会话业务服务
 * 
 * 负责游戏会话的核心业务逻辑处理，包括：
 * - 会话生命周期管理
 * - 交易决策处理
 * - 投资组合计算
 * - 评分算法执行
 * 
 * @author TradingSim Team
 */
@Service
@Transactional
@Slf4j
public class GameSessionService {
    
    /**
     * 创建游戏会话
     * 
     * 执行以下步骤：
     * 1. 验证用户权限和配额
     * 2. 验证数据段可用性
     * 3. 创建游戏会话实体
     * 4. 初始化投资组合
     * 5. 发布会话创建事件
     * 
     * @param request 创建请求参数
     * @param userId 用户ID
     * @return 创建的游戏会话信息
     * @throws UserNotFoundException 用户不存在
     * @throws SegmentNotFoundException 数据段不存在
     * @throws QuotaExceededException 超出用户配额
     */
    public GameSessionResponse createSession(CreateGameSessionRequest request, String userId) {
        log.info("Creating game session for user: {}, segment: {}", userId, request.getSegmentId());
        
        // 实现逻辑
        
        log.info("Game session created successfully: {}", session.getId());
        return response;
    }
}
```

---

## 最佳实践

### 1. 代码组织

#### 包结构规范
```
com.tradingsim/
├── api/                    # API层
│   ├── rest/              # REST控制器
│   └── websocket/         # WebSocket处理器
├── application/           # 应用层
│   ├── service/          # 应用服务
│   ├── dto/              # 数据传输对象
│   └── command/          # 命令对象
├── domain/               # 领域层
│   ├── model/           # 领域模型
│   ├── service/         # 领域服务
│   ├── repository/      # 仓储接口
│   └── event/           # 领域事件
├── infrastructure/       # 基础设施层
│   ├── persistence/     # 数据持久化
│   ├── external/        # 外部服务
│   ├── config/          # 配置
│   └── security/        # 安全配置
└── shared/              # 共享组件
    ├── exception/       # 异常定义
    ├── util/           # 工具类
    └── constant/       # 常量定义
```

### 2. 命名规范

#### 类命名
```java
// Controller命名
public class GameSessionController {}
public class UserManagementController {}

// Service命名
public class GameSessionService {}
public class UserApplicationService {}

// Repository命名
public interface GameSessionRepository {}
public interface UserRepository {}

// DTO命名
public class CreateGameSessionRequest {}
public class GameSessionResponse {}
public class UpdateUserProfileRequest {}

// Exception命名
public class GameSessionNotFoundException {}
public class InvalidTradingDecisionException {}
```

#### 方法命名
```java
// 查询方法
public GameSession findById(String id) {}
public List<GameSession> findByUserId(String userId) {}
public Page<GameSession> findByUserIdAndStatus(String userId, GameStatus status, Pageable pageable) {}

// 业务方法
public GameSessionResponse createSession(CreateGameSessionRequest request) {}
public void startGame(String sessionId) {}
public void processDecision(String sessionId, TradingDecision decision) {}

// 验证方法
public boolean isValidSegmentId(String segmentId) {}
public boolean canCreateSession(String userId) {}

// 转换方法
public GameSessionResponse toResponse(GameSession session) {}
public GameSession toEntity(CreateGameSessionRequest request) {}
```

### 3. 配置管理

#### 配置文件组织
```yaml
# application.yml - 基础配置
spring:
  application:
    name: tradingsim-backend
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

---
# application-dev.yml - 开发环境
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/tradingsim
    username: tradingsim
    password: password
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update

logging:
  level:
    com.tradingsim: DEBUG

---
# application-prod.yml - 生产环境
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate

logging:
  level:
    com.tradingsim: INFO
```

#### 配置属性类
```java
@ConfigurationProperties(prefix = "tradingsim.game")
@Data
@Validated
public class GameProperties {
    
    /**
     * K线推送间隔（毫秒）
     */
    @Min(100)
    @Max(5000)
    private int frameIntervalMs = 250;
    
    /**
     * 决策超时时间（秒）
     */
    @Min(5)
    @Max(60)
    private int decisionTimeoutSec = 30;
    
    /**
     * 最大并发游戏会话数
     */
    @Min(1)
    @Max(10000)
    private int maxConcurrentSessions = 1000;
    
    /**
     * 评分配置
     */
    @Valid
    @NotNull
    private ScoringConfig scoring = new ScoringConfig();
    
    @Data
    public static class ScoringConfig {
        /**
         * 评分窗口（分钟）
         */
        private List<Integer> windows = List.of(5, 10, 20);
        
        /**
         * 权重分配
         */
        private List<Double> weights = List.of(0.3, 0.4, 0.3);
        
        /**
         * 风险惩罚系数
         */
        private double riskPenalty = 0.5;
    }
}
```

### 4. 监控和日志

#### 日志规范
```java
@Service
@Slf4j
public class GameSessionService {
    
    public GameSessionResponse createSession(CreateGameSessionRequest request, String userId) {
        // 使用结构化日志
        log.info("Creating game session - userId: {}, segmentId: {}, difficulty: {}", 
                userId, request.getSegmentId(), request.getDifficulty());
        
        try {
            GameSession session = gameSessionFactory.create(request, userId);
            GameSession savedSession = repository.save(session);
            
            // 记录成功日志
            log.info("Game session created successfully - sessionId: {}, userId: {}", 
                    savedSession.getId(), userId);
            
            return mapper.toResponse(savedSession);
            
        } catch (Exception e) {
            // 记录错误日志
            log.error("Failed to create game session - userId: {}, segmentId: {}, error: {}", 
                    userId, request.getSegmentId(), e.getMessage(), e);
            throw e;
        }
    }
}
```

#### 指标收集
```java
@Service
public class GameSessionService {
    
    private final MeterRegistry meterRegistry;
    private final Counter sessionCreatedCounter;
    private final Timer sessionCreationTimer;
    
    public GameSessionService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.sessionCreatedCounter = Counter.builder("game.session.created")
                .description("Number of game sessions created")
                .register(meterRegistry);
        this.sessionCreationTimer = Timer.builder("game.session.creation.time")
                .description("Time taken to create game session")
                .register(meterRegistry);
    }
    
    public GameSessionResponse createSession(CreateGameSessionRequest request, String userId) {
        return sessionCreationTimer.recordCallable(() -> {
            try {
                GameSessionResponse response = doCreateSession(request, userId);
                sessionCreatedCounter.increment(Tags.of("status", "success"));
                return response;
            } catch (Exception e) {
                sessionCreatedCounter.increment(Tags.of("status", "error"));
                throw e;
            }
        });
    }
}
```

---

## 总结

这份API开发指南涵盖了TradingSim项目的核心开发实践：

1. **设计原则**: 契约优先、领域驱动、CQRS模式
2. **API规范**: RESTful设计、WebSocket协议、数据模型
3. **质量保证**: 错误处理、安全实践、性能优化
4. **开发流程**: 测试策略、文档规范、最佳实践

遵循这些指南将确保API的一致性、可维护性和高质量。