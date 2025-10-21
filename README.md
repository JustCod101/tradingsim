# TradingSim - 实时交易模拟系统

基于 Spring Boot 3 + Vue 3 + TimescaleDB 的高性能交易模拟平台，支持实时K线推送、关键点检测、决策评分和可观测性监控。

## 🚀 快速启动（3步）

```bash
# 1. 克隆并进入项目目录
git clone <your-repo> && cd tradingsim

# 2. 一键启动所有服务
docker compose up -d

# 3. 访问前端界面
open http://localhost:5173
```

## 📋 系统架构

### 技术栈
- **后端**: Spring Boot 3 (Java 17+), WebSocket (STOMP), DDD/六边形架构
- **数据层**: PostgreSQL + TimescaleDB, Redis
- **前端**: Vue 3 + Vite + ECharts
- **监控**: Micrometer → Prometheus + Grafana
- **运维**: Docker Compose
- **压测**: k6

### 核心特性
- ✅ 契约优先设计 (OpenAPI + WebSocket 协议)
- ✅ 实时K线逐帧推送 (250ms间隔，可配置)
- ✅ 关键点检测 SPI (≥2种算法实现)
- ✅ 多维度评分 SPI (≥2种策略实现)
- ✅ 断线重连机制
- ✅ 完整审计日志
- ✅ 可观测性监控
- ✅ 压力测试覆盖

## 🏗️ 项目结构

```
tradingsim/
├── README.md
├── TODO.md
├── RISK_ASSESSMENT.md
├── docker-compose.yml
├── docs/
│   ├── openapi.yaml
│   ├── websocket-protocol.md
│   └── api-examples.md
├── monitoring/
│   ├── docker-compose.yml
│   ├── start-monitoring.sh
│   ├── prometheus.yml
│   └── grafana/
│       ├── dashboards/
│       │   ├── tradingsim-overview.json
│       │   └── tradingsim-business.json
│       └── provisioning/
│           ├── dashboards/dashboard.yml
│           └── datasources/prometheus.yml
├── load-testing/
│   ├── README.md
│   ├── run-tests.sh
│   ├── k6.config.js
│   ├── basic-load-test.js
│   ├── game-session-test.js
│   └── websocket-stress-test.js
├── scripts/
│   ├── sql/
│   │   ├── 01_timescale_schema.sql
│   │   ├── 02_business_schema.sql
│   │   └── 03_sample_data.sql
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/tradingsim/
│       ├── api/          # REST Controllers
│       ├── app/          # Application Services
│       ├── domain/       # Domain Models & Services
│       │   ├── model/
│       │   ├── service/
│       │   ├── event/
│       │   └── repository/
│       ├── infra/        # Infrastructure
│       │   ├── db/
│       │   ├── cache/
│       │   ├── ws/
│       │   └── spi/
│       └── resources/
│           ├── META-INF/services/
│           └── application.yml
└── frontend/
    ├── Dockerfile
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── components/
        ├── views/
        ├── services/
        └── utils/
```

## 🔧 配置说明

### 核心配置项 (可配置)
```yaml
# 应用配置
server.port: 8080                    # 后端端口
frontend.port: 5173                  # 前端端口

# WebSocket 配置
ws.frame.interval.ms: 250            # 推送间隔
ws.decision.timeout.sec: 10          # 决策超时

# 关键点检测
keypoint.min.count: 3                # 最小关键点数
keypoint.max.count: 8                # 最大关键点数

# 评分权重
scoring.windows: [5, 10, 20]         # 评分窗口
scoring.weights: [0.3, 0.4, 0.3]     # 权重分配
risk.penalty.mdd: 0.5                # 最大回撤惩罚
risk.penalty.sigma: 0.3              # 波动率惩罚
risk.penalty.fee: 0.0005             # 手续费

# 数据过滤
segment.volatility.min: 0.01         # 最小波动率
segment.volatility.max: 0.08         # 最大波动率
segment.min.volume: 1000             # 最小成交量

# 随机种子
seed.salt: "demo-salt"               # 种子盐值
```

## 📊 API 接口

### REST API
- `POST /api/v1/sessions` - 创建交易会话
- `GET /api/v1/sessions/{id}` - 获取会话详情
- `GET /api/v1/sessions/{id}/replay` - 获取回放数据

### WebSocket 主题
- `/topic/session.{id}.frames` - 接收K线帧数据
- `/app/session.{id}.decision` - 发送交易决策

### 消息类型
- `KLINE` - K线数据推送
- `PAUSE` - 关键点暂停
- `REWARD` - 评分奖励
- `END` - 会话结束
- `DECISION` - 交易决策 (BUY/SELL/SKIP)

## 🔍 监控指标

### 启动监控栈
```bash
# 启动完整监控栈 (Prometheus + Grafana + Node Exporter)
cd monitoring && ./start-monitoring.sh

# 访问地址:
# - Grafana: http://localhost:3000 (admin/admin)
# - Prometheus: http://localhost:9090
```

### 关键指标
- `game_frames_push_latency_ms` - 帧推送延迟
- `pause_to_decision_latency_ms` - 决策响应延迟
- `ws_active_sessions` - 活跃WebSocket连接数
- `decision_timeout_count` - 决策超时次数
- `scoring_duration_ms{rule_version}` - 评分计算耗时
- `http_requests_total` - HTTP请求总数
- `jvm_memory_used_bytes` - JVM内存使用
- `system_cpu_usage` - 系统CPU使用率

### Grafana 仪表盘
- **系统监控**: 性能指标、资源使用、错误率
- **业务监控**: 用户活动、游戏会话、交易决策

## 🧪 测试验证

### 功能测试
```bash
# 启动系统后运行基础验证
curl -X POST http://localhost:8080/api/v1/sessions \
  -H "Content-Type: application/json" \
  -d '{"segmentId": "demo-001"}'
```

### 负载测试
```bash
# 进入负载测试目录
cd load-testing

# 运行所有测试
./run-tests.sh all

# 运行特定测试
./run-tests.sh basic      # 基础负载测试
./run-tests.sh game       # 游戏会话测试  
./run-tests.sh websocket  # WebSocket压力测试

# 自定义配置
./run-tests.sh basic --base-url http://localhost:8080 --results-dir ./results
```

### 测试类型
- **基础负载测试**: API端点性能和稳定性
- **游戏会话测试**: 完整游戏流程压力测试
- **WebSocket压力测试**: 并发连接和消息传输测试

### 期望指标
- P95 延迟 < 500ms
- 决策超时率 < 5%
- 并发100用户稳定运行
- WebSocket连接成功率 > 95%

## 🔒 安全考虑

- ✅ 严禁向前端泄露未来窗口数据
- ✅ 所有随机逻辑使用固定种子
- ✅ 敏感配置通过环境变量注入
- ✅ WebSocket 连接限流保护
- ✅ 完整的审计日志记录

## 📝 开发指南

### 本地开发
```bash
# 启动依赖服务
docker compose up -d db redis

# 后端开发
cd backend && ./mvnw spring-boot:run

# 前端开发
cd frontend && npm run dev
```

### SPI 扩展
1. 实现 `KeypointDetector` 接口
2. 实现 `ScoringRule` 接口  
3. 在 `META-INF/services/` 注册服务

## ⚠️ 风险管理

### 主要风险类别
- **技术风险**: 数据库性能、WebSocket稳定性、内存泄漏
- **安全风险**: 用户认证、SQL注入、XSS攻击
- **业务风险**: 数据丢失、系统可用性、用户增长
- **运维风险**: 部署失败、配置错误
- **合规风险**: 数据隐私保护

### 详细评估
查看 [RISK_ASSESSMENT.md](./RISK_ASSESSMENT.md) 获取完整的风险评估，包括：
- 风险影响分析和缓解策略
- 监控指标和告警阈值
- 应急响应计划

## 📋 开发计划

### 近期目标
- [ ] 实现用户认证和权限控制
- [ ] 完善游戏核心逻辑
- [ ] 优化前端用户界面
- [ ] 增强安全性措施

### 详细计划
查看 [TODO.md](./TODO.md) 获取完整的开发计划，包括：
- 高/中/低优先级任务分类
- 里程碑规划和时间线
- 技术债务管理
- 进度跟踪

## 📞 支持

如有问题请查看：
1. 日志: `docker compose logs -f`
2. 监控: http://localhost:3000
3. API文档: http://localhost:8080/swagger-ui.html

---
**注意**: 本系统仅用于模拟交易学习，不构成任何投资建议。