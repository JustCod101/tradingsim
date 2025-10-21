# TradingSim 架构设计文档

## 📋 目录

- [系统概述](#系统概述)
- [架构原则](#架构原则)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [模块设计](#模块设计)
- [数据流设计](#数据流设计)
- [安全架构](#安全架构)
- [性能设计](#性能设计)
- [监控架构](#监控架构)
- [部署架构](#部署架构)

---

## 系统概述

TradingSim 是一个实时交易模拟系统，旨在为用户提供真实的股票交易体验。系统采用现代化的微服务架构，支持高并发、实时数据推送和可扩展性。

### 核心功能
- 🎯 **实时交易模拟**: 基于真实历史数据的交易模拟
- 📊 **实时数据推送**: WebSocket 实时K线数据推送
- 🎮 **游戏化体验**: 关键点检测、决策评分、成就系统
- 📈 **投资组合管理**: 实时投资组合价值计算和风险评估
- 🔍 **数据分析**: 交易历史分析和性能报告

### 设计目标
- **高性能**: 支持1000+并发用户
- **实时性**: 数据推送延迟 < 100ms
- **可扩展**: 模块化设计，易于扩展
- **可观测**: 完整的监控和日志系统
- **易维护**: 清晰的代码结构和文档

---

## 架构原则

### 1. 领域驱动设计 (DDD)
- **领域模型**: 以业务领域为核心的模型设计
- **聚合根**: 明确的聚合边界和一致性保证
- **领域服务**: 复杂业务逻辑的封装
- **仓储模式**: 数据访问的抽象

### 2. 六边形架构 (Hexagonal Architecture)
```
┌─────────────────────────────────────────┐
│              Adapters (外层)              │
│  ┌─────────────────────────────────────┐ │
│  │         Application (应用层)         │ │
│  │  ┌─────────────────────────────────┐ │ │
│  │  │        Domain (领域层)          │ │ │
│  │  │                                 │ │ │
│  │  │  - Models                       │ │ │
│  │  │  - Services                     │ │ │
│  │  │  - Repositories (接口)          │ │ │
│  │  │  - Events                       │ │ │
│  │  └─────────────────────────────────┘ │ │
│  │                                     │ │
│  │  - Application Services             │ │
│  │  - Command/Query Handlers           │ │
│  │  - DTOs                             │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  - REST Controllers                     │
│  - WebSocket Handlers                   │
│  - Database Adapters                    │
│  - External API Clients                 │
└─────────────────────────────────────────┘
```

### 3. CQRS (命令查询责任分离)
- **命令端**: 处理写操作，确保数据一致性
- **查询端**: 优化读操作，支持复杂查询
- **事件驱动**: 通过领域事件实现解耦

### 4. 微服务原则
- **单一职责**: 每个服务专注于特定业务功能
- **自治性**: 服务独立部署和扩展
- **数据隔离**: 每个服务拥有独立的数据存储
- **API优先**: 通过明确定义的API进行通信

---

## 技术栈

### 后端技术栈
```yaml
核心框架:
  - Spring Boot 3.2+: 应用框架
  - Spring WebFlux: 响应式编程
  - Spring Security: 安全框架
  - Spring Data JPA: 数据访问

消息通信:
  - STOMP over WebSocket: 实时通信
  - Spring Messaging: 消息处理

数据存储:
  - PostgreSQL 15+: 主数据库
  - TimescaleDB: 时序数据扩展
  - Redis 7+: 缓存和会话存储

监控观测:
  - Micrometer: 指标收集
  - Prometheus: 指标存储
  - Grafana: 可视化监控
  - Logback: 日志框架

构建部署:
  - Maven: 构建工具
  - Docker: 容器化
  - Docker Compose: 本地开发
```

### 前端技术栈
```yaml
核心框架:
  - Vue 3: 前端框架
  - TypeScript: 类型安全
  - Vite: 构建工具
  - Pinia: 状态管理

UI组件:
  - Element Plus: UI组件库
  - ECharts: 图表库
  - TailwindCSS: 样式框架

通信工具:
  - Axios: HTTP客户端
  - SockJS: WebSocket客户端
  - STOMP.js: STOMP协议客户端

开发工具:
  - ESLint: 代码检查
  - Prettier: 代码格式化
  - Vitest: 单元测试
```

---

## 系统架构

### 整体架构图
```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Web Browser   │  │  Mobile App     │  │  Admin Panel │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway                            │
│              (Nginx / Spring Cloud Gateway)                 │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │  User Service   │  │  Game Service   │  │ Market Data  │ │
│  │                 │  │                 │  │   Service    │ │
│  │ - Authentication│  │ - Session Mgmt  │  │ - Data Feed  │ │
│  │ - User Profile  │  │ - Game Logic    │  │ - Historical │ │
│  │ - Permissions   │  │ - Scoring       │  │ - Real-time  │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                     Data Layer                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   PostgreSQL    │  │     Redis       │  │  TimescaleDB │ │
│  │                 │  │                 │  │              │ │
│  │ - User Data     │  │ - Sessions      │  │ - Market Data│ │
│  │ - Game Sessions │  │ - Cache         │  │ - Metrics    │ │
│  │ - Transactions  │  │ - Real-time     │  │ - Time Series│ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 服务间通信
```
┌─────────────┐    HTTP/REST     ┌─────────────┐
│             │ ──────────────► │             │
│   Client    │                 │   Backend   │
│             │ ◄────────────── │             │
└─────────────┘    WebSocket    └─────────────┘
                   (STOMP)
                       │
                       ▼
              ┌─────────────────┐
              │   Message Bus   │
              │   (In-Memory)   │
              └─────────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │   Data Layer    │
              │ - PostgreSQL    │
              │ - Redis         │
              │ - TimescaleDB   │
              └─────────────────┘
```

---

## 模块设计

### 后端模块结构
```
com.tradingsim/
├── api/                    # API层 (Controllers)
│   ├── rest/              # REST API控制器
│   │   ├── UserController
│   │   ├── GameController
│   │   └── MarketController
│   └── websocket/         # WebSocket处理器
│       ├── GameWebSocketHandler
│       └── MarketDataHandler
│
├── application/           # 应用层 (Application Services)
│   ├── service/          # 应用服务
│   │   ├── UserApplicationService
│   │   ├── GameApplicationService
│   │   └── MarketDataApplicationService
│   ├── dto/              # 数据传输对象
│   └── command/          # 命令对象
│
├── domain/               # 领域层 (Domain Models)
│   ├── model/           # 领域模型
│   │   ├── user/        # 用户聚合
│   │   │   ├── User
│   │   │   ├── UserProfile
│   │   │   └── UserRepository
│   │   ├── game/        # 游戏聚合
│   │   │   ├── GameSession
│   │   │   ├── TradingDecision
│   │   │   ├── Portfolio
│   │   │   └── GameRepository
│   │   └── market/      # 市场数据聚合
│   │       ├── MarketData
│   │       ├── KlineData
│   │       └── MarketDataRepository
│   ├── service/         # 领域服务
│   │   ├── ScoringService
│   │   ├── KeypointDetectionService
│   │   └── PortfolioCalculationService
│   └── event/           # 领域事件
│       ├── GameStartedEvent
│       ├── DecisionMadeEvent
│       └── GameEndedEvent
│
└── infrastructure/       # 基础设施层
    ├── persistence/     # 数据持久化
    │   ├── jpa/        # JPA实现
    │   └── redis/      # Redis实现
    ├── external/       # 外部服务
    │   └── marketdata/ # 市场数据API
    ├── config/         # 配置
    └── security/       # 安全配置
```

### 前端模块结构
```
src/
├── components/          # 可复用组件
│   ├── common/         # 通用组件
│   │   ├── Loading.vue
│   │   ├── ErrorBoundary.vue
│   │   └── Pagination.vue
│   ├── charts/         # 图表组件
│   │   ├── KlineChart.vue
│   │   ├── PortfolioChart.vue
│   │   └── PerformanceChart.vue
│   └── game/           # 游戏相关组件
│       ├── GameBoard.vue
│       ├── DecisionPanel.vue
│       └── ScoreDisplay.vue
│
├── views/              # 页面视图
│   ├── auth/          # 认证页面
│   │   ├── Login.vue
│   │   └── Register.vue
│   ├── game/          # 游戏页面
│   │   ├── GameLobby.vue
│   │   ├── GamePlay.vue
│   │   └── GameResult.vue
│   └── dashboard/     # 仪表盘
│       ├── Overview.vue
│       └── Analytics.vue
│
├── services/          # 业务服务
│   ├── api/          # API服务
│   │   ├── userApi.ts
│   │   ├── gameApi.ts
│   │   └── marketApi.ts
│   ├── websocket/    # WebSocket服务
│   │   └── gameWebSocket.ts
│   └── utils/        # 工具服务
│       ├── auth.ts
│       └── storage.ts
│
├── stores/           # 状态管理
│   ├── user.ts      # 用户状态
│   ├── game.ts      # 游戏状态
│   └── market.ts    # 市场数据状态
│
├── types/            # TypeScript类型定义
│   ├── api.ts       # API类型
│   ├── game.ts      # 游戏类型
│   └── user.ts      # 用户类型
│
└── utils/            # 工具函数
    ├── format.ts    # 格式化工具
    ├── validation.ts # 验证工具
    └── constants.ts  # 常量定义
```

---

## 数据流设计

### 游戏会话数据流
```
1. 用户发起游戏请求
   Client ──POST /api/v1/sessions──► Backend
                                      │
                                      ▼
                              GameApplicationService
                                      │
                                      ▼
                               GameDomainService
                                      │
                                      ▼
                              创建GameSession实体
                                      │
                                      ▼
                               保存到数据库
                                      │
                                      ▼
                              返回会话ID给客户端

2. 建立WebSocket连接
   Client ──WebSocket Connect──► Backend
                                      │
                                      ▼
                            GameWebSocketHandler
                                      │
                                      ▼
                              订阅游戏主题
                                      │
                                      ▼
                            开始推送K线数据

3. 实时数据推送
   MarketDataService ──► MessageBroker ──► WebSocket ──► Client
                              │
                              ▼
                      检测关键点
                              │
                              ▼
                      暂停并等待决策

4. 用户决策处理
   Client ──Decision Message──► Backend
                                      │
                                      ▼
                            DecisionHandler
                                      │
                                      ▼
                            ScoringService
                                      │
                                      ▼
                            更新投资组合
                                      │
                                      ▼
                            推送评分结果
```

### 数据存储策略
```yaml
PostgreSQL (主数据库):
  用途: 业务数据存储
  数据类型:
    - 用户信息
    - 游戏会话
    - 交易记录
    - 投资组合快照
  
  分表策略:
    - 按时间分区 (game_sessions_yyyy_mm)
    - 按用户ID哈希 (user_data_0 ~ user_data_15)

TimescaleDB (时序数据):
  用途: 时序数据存储
  数据类型:
    - 市场K线数据
    - 系统性能指标
    - 用户行为事件
  
  优化策略:
    - 自动分区 (按时间)
    - 数据压缩
    - 数据保留策略

Redis (缓存):
  用途: 高速缓存和会话存储
  数据类型:
    - 用户会话
    - 游戏状态
    - 实时数据缓存
    - 分布式锁
  
  过期策略:
    - 会话数据: 24小时
    - 缓存数据: 1小时
    - 临时数据: 5分钟
```

---

## 安全架构

### 认证授权流程
```
1. 用户登录
   Client ──POST /auth/login──► AuthController
                                      │
                                      ▼
                              验证用户凭据
                                      │
                                      ▼
                              生成JWT Token
                                      │
                                      ▼
                              返回Token给客户端

2. API访问
   Client ──Header: Authorization──► SecurityFilter
                                           │
                                           ▼
                                    验证JWT Token
                                           │
                                           ▼
                                    提取用户信息
                                           │
                                           ▼
                                    检查权限
                                           │
                                           ▼
                                    继续处理请求

3. WebSocket认证
   Client ──Connect with Token──► WebSocketInterceptor
                                         │
                                         ▼
                                  验证Token
                                         │
                                         ▼
                                  建立连接
```

### 安全措施
```yaml
认证安全:
  - JWT Token (RS256签名)
  - Token刷新机制
  - 多设备登录控制
  - 密码强度验证

授权控制:
  - 基于角色的访问控制 (RBAC)
  - 资源级权限控制
  - API访问限流
  - WebSocket连接限制

数据安全:
  - 敏感数据加密存储
  - SQL注入防护
  - XSS攻击防护
  - CSRF保护

传输安全:
  - HTTPS强制
  - WebSocket安全连接
  - 请求签名验证
  - 数据完整性校验

运行时安全:
  - 输入验证和清理
  - 错误信息脱敏
  - 安全日志记录
  - 异常行为监控
```

---

## 性能设计

### 性能目标
```yaml
响应时间:
  - API响应: P95 < 200ms
  - WebSocket延迟: < 50ms
  - 数据库查询: P95 < 100ms
  - 页面加载: < 2s

吞吐量:
  - 并发用户: 1000+
  - API QPS: 10000+
  - WebSocket连接: 5000+
  - 数据推送: 100万条/分钟

可用性:
  - 系统可用性: 99.9%
  - 数据一致性: 强一致性
  - 故障恢复: < 30s
```

### 性能优化策略
```yaml
应用层优化:
  - 连接池配置优化
  - 异步处理
  - 批量操作
  - 缓存策略

数据库优化:
  - 索引优化
  - 查询优化
  - 分区表
  - 读写分离

缓存策略:
  - 多级缓存
  - 缓存预热
  - 缓存更新策略
  - 缓存穿透防护

前端优化:
  - 代码分割
  - 懒加载
  - 资源压缩
  - CDN加速
```

---

## 监控架构

### 监控体系
```
┌─────────────────────────────────────────────────────────────┐
│                      Monitoring Stack                       │
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Grafana   │  │ Prometheus  │  │    AlertManager     │  │
│  │             │  │             │  │                     │  │
│  │ - Dashboard │  │ - Metrics   │  │ - Alert Rules       │  │
│  │ - Alerting  │  │ - Storage   │  │ - Notifications     │  │
│  │ - Analysis  │  │ - Query     │  │ - Escalation        │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│         │                 ▲                        ▲        │
│         │                 │                        │        │
│         └─────────────────┼────────────────────────┘        │
│                           │                                 │
└───────────────────────────┼─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Backend   │  │  Frontend   │  │    Infrastructure   │  │
│  │             │  │             │  │                     │  │
│  │ - Micrometer│  │ - User      │  │ - Node Exporter     │  │
│  │ - Custom    │  │   Metrics   │  │ - Database Metrics  │  │
│  │   Metrics   │  │ - Error     │  │ - System Metrics    │  │
│  │ - Health    │  │   Tracking  │  │ - Network Metrics   │  │
│  │   Checks    │  │             │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 关键指标
```yaml
业务指标:
  - 活跃用户数
  - 游戏会话数
  - 交易决策数
  - 用户留存率
  - 收入指标

技术指标:
  - 响应时间分布
  - 错误率
  - 吞吐量
  - 资源使用率
  - 数据库性能

基础设施指标:
  - CPU使用率
  - 内存使用率
  - 磁盘I/O
  - 网络流量
  - 容器状态
```

---

## 部署架构

### 容器化部署
```yaml
开发环境:
  部署方式: Docker Compose
  服务组件:
    - tradingsim-backend
    - tradingsim-frontend
    - postgresql
    - redis
    - prometheus
    - grafana
  
  特点:
    - 快速启动
    - 本地开发
    - 完整功能

生产环境:
  部署方式: Kubernetes
  服务组件:
    - Backend Pods (3 replicas)
    - Frontend Pods (2 replicas)
    - Database Cluster
    - Redis Cluster
    - Monitoring Stack
  
  特点:
    - 高可用
    - 自动扩缩容
    - 滚动更新
    - 健康检查
```

### CI/CD流水线
```yaml
代码提交:
  触发器: Git Push
  步骤:
    1. 代码检查 (ESLint, SonarQube)
    2. 单元测试
    3. 构建镜像
    4. 安全扫描
    5. 推送到镜像仓库

部署流程:
  触发器: 镜像推送
  步骤:
    1. 拉取最新镜像
    2. 数据库迁移
    3. 滚动更新
    4. 健康检查
    5. 烟雾测试
    6. 监控告警
```

---

## 扩展性设计

### 水平扩展
```yaml
应用层扩展:
  - 无状态设计
  - 负载均衡
  - 会话外部化
  - 微服务拆分

数据层扩展:
  - 读写分离
  - 分库分表
  - 缓存集群
  - 数据分片

基础设施扩展:
  - 容器编排
  - 自动扩缩容
  - 多区域部署
  - CDN加速
```

### 功能扩展
```yaml
插件化架构:
  - SPI接口设计
  - 动态加载
  - 配置驱动
  - 热插拔支持

API版本管理:
  - 版本兼容性
  - 渐进式升级
  - 向后兼容
  - 废弃策略
```

---

## 总结

TradingSim 采用现代化的架构设计，具备以下特点：

1. **模块化设计**: 清晰的分层架构，易于维护和扩展
2. **高性能**: 优化的数据流和缓存策略
3. **高可用**: 完善的监控和故障恢复机制
4. **安全可靠**: 多层次的安全防护
5. **可观测性**: 全面的监控和日志系统

这个架构为项目的长期发展提供了坚实的基础，支持快速迭代和规模化扩展。