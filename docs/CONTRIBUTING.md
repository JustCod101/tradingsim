# TradingSim 贡献指南

欢迎为TradingSim项目做出贡献！本指南将帮助您了解如何参与项目开发，包括代码规范、开发流程和最佳实践。

## 📋 目录

- [开始贡献](#开始贡献)
- [开发环境设置](#开发环境设置)
- [代码规范](#代码规范)
- [提交规范](#提交规范)
- [分支策略](#分支策略)
- [Pull Request流程](#pull-request流程)
- [代码审查](#代码审查)
- [测试要求](#测试要求)
- [文档贡献](#文档贡献)
- [问题报告](#问题报告)
- [社区准则](#社区准则)

---

## 开始贡献

### 贡献类型

我们欢迎以下类型的贡献：

- 🐛 **Bug修复**: 修复已知问题
- ✨ **新功能**: 添加新的功能特性
- 📚 **文档改进**: 完善项目文档
- 🎨 **UI/UX改进**: 提升用户界面和体验
- ⚡ **性能优化**: 提升系统性能
- 🧪 **测试增强**: 增加或改进测试覆盖
- 🔧 **工具改进**: 改进开发工具和流程

### 贡献前准备

1. **阅读文档**: 熟悉项目架构和开发指南
   - [项目架构设计](./ARCHITECTURE.md)
   - [开发环境搭建](./DEVELOPMENT_SETUP.md)
   - [API开发指南](./API_DEVELOPMENT_GUIDE.md)
   - [前端开发指南](./FRONTEND_DEVELOPMENT_GUIDE.md)

2. **了解项目状态**: 查看当前的开发计划
   - [TODO清单](../TODO.md)
   - [风险评估](../RISK_ASSESSMENT.md)
   - [GitHub Issues](https://github.com/your-org/tradingsim/issues)

3. **设置开发环境**: 按照开发环境搭建指南配置本地环境

---

## 开发环境设置

### 快速开始

```bash
# 1. Fork并克隆项目
git clone https://github.com/your-username/tradingsim.git
cd tradingsim

# 2. 安装依赖
./scripts/setup.sh

# 3. 启动开发环境
docker-compose up -d

# 4. 验证安装
./scripts/verify-setup.sh
```

### 开发工具配置

#### Git配置
```bash
# 配置用户信息
git config user.name "Your Name"
git config user.email "your.email@example.com"

# 配置提交模板
git config commit.template .gitmessage

# 安装Git hooks
./scripts/install-hooks.sh
```

#### IDE配置

**IntelliJ IDEA**
```yaml
推荐插件:
  - Lombok
  - SonarLint
  - GitToolBox
  - Database Navigator
  - Docker

代码风格:
  - 导入: .idea/codeStyles/Project.xml
  - 检查: .idea/inspectionProfiles/Project_Default.xml
```

**VS Code**
```json
{
  "recommendations": [
    "ms-vscode.vscode-typescript-next",
    "bradlc.vscode-tailwindcss",
    "esbenp.prettier-vscode",
    "ms-vscode.vscode-eslint",
    "ms-vscode.vscode-jest"
  ]
}
```

---

## 代码规范

### Java代码规范

#### 命名规范
```java
// ✅ 类名使用PascalCase
public class GameSessionService {
    
    // ✅ 常量使用UPPER_SNAKE_CASE
    private static final String DEFAULT_SEGMENT_ID = "AAPL_2023_Q1";
    
    // ✅ 变量和方法使用camelCase
    private final GameSessionRepository gameSessionRepository;
    
    // ✅ 方法名应该是动词或动词短语
    public GameSession createGameSession(CreateGameSessionRequest request) {
        // 实现
    }
    
    // ✅ 布尔方法使用is/has/can前缀
    public boolean isSessionActive(String sessionId) {
        // 实现
    }
}
```

#### 代码结构
```java
// ✅ 良好的类结构
@Service
@Slf4j
@RequiredArgsConstructor
public class GameSessionService {
    
    // 1. 静态常量
    private static final int MAX_SESSIONS_PER_USER = 10;
    
    // 2. 依赖注入字段
    private final GameSessionRepository repository;
    private final KlineDataService klineDataService;
    
    // 3. 公共方法
    @Transactional
    public GameSession createSession(CreateGameSessionRequest request) {
        validateRequest(request);
        
        GameSession session = GameSession.builder()
            .segmentId(request.getSegmentId())
            .difficulty(request.getDifficulty())
            .initialCapital(request.getInitialCapital())
            .status(GameSessionStatus.CREATED)
            .build();
            
        return repository.save(session);
    }
    
    // 4. 私有辅助方法
    private void validateRequest(CreateGameSessionRequest request) {
        if (request.getInitialCapital().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Initial capital must be positive");
        }
    }
}
```

#### 异常处理
```java
// ✅ 自定义异常
public class GameSessionNotFoundException extends BusinessException {
    public GameSessionNotFoundException(String sessionId) {
        super(ErrorCode.GAME_SESSION_NOT_FOUND, 
              "Game session not found: " + sessionId);
    }
}

// ✅ 异常处理
@Service
public class GameSessionService {
    
    public GameSession getSession(String sessionId) {
        return repository.findById(sessionId)
            .orElseThrow(() -> new GameSessionNotFoundException(sessionId));
    }
    
    @Transactional
    public void startSession(String sessionId) {
        try {
            GameSession session = getSession(sessionId);
            session.start();
            repository.save(session);
            
            // 发布事件
            eventPublisher.publishEvent(new GameSessionStartedEvent(session));
            
        } catch (Exception e) {
            log.error("Failed to start game session: {}", sessionId, e);
            throw new GameSessionOperationException("Failed to start session", e);
        }
    }
}
```

### TypeScript代码规范

#### 类型定义
```typescript
// ✅ 接口命名使用PascalCase
interface GameSession {
  id: string;
  segmentId: string;
  difficulty: GameDifficulty;
  status: GameSessionStatus;
  createdAt: string;
  portfolio?: Portfolio;
}

// ✅ 枚举使用PascalCase
enum GameSessionStatus {
  CREATED = 'CREATED',
  ACTIVE = 'ACTIVE',
  PAUSED = 'PAUSED',
  COMPLETED = 'COMPLETED',
}

// ✅ 类型别名使用PascalCase
type EventHandler<T = void> = (event: T) => void | Promise<void>;

// ✅ 泛型约束
interface Repository<T extends { id: string }> {
  findById(id: string): Promise<T | null>;
  save(entity: T): Promise<T>;
}
```

#### 组件规范
```typescript
// ✅ 组件Props接口
interface GameSessionCardProps {
  session: GameSession;
  onSessionClick?: (sessionId: string) => void;
  onSessionDelete?: (sessionId: string) => void;
  className?: string;
}

// ✅ 组件实现
export const GameSessionCard: React.FC<GameSessionCardProps> = ({
  session,
  onSessionClick,
  onSessionDelete,
  className,
}) => {
  // Hooks在顶部
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  
  // 事件处理器
  const handleClick = useCallback(() => {
    onSessionClick?.(session.id);
  }, [session.id, onSessionClick]);
  
  const handleDelete = useCallback(async () => {
    try {
      setLoading(true);
      await deleteSession(session.id);
      onSessionDelete?.(session.id);
    } catch (error) {
      console.error('Failed to delete session:', error);
    } finally {
      setLoading(false);
    }
  }, [session.id, onSessionDelete]);
  
  // 渲染
  return (
    <Card className={className} onClick={handleClick}>
      {/* 组件内容 */}
    </Card>
  );
};
```

### SQL规范

#### 命名规范
```sql
-- ✅ 表名使用snake_case
CREATE TABLE game_sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    segment_id VARCHAR(50) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    initial_capital DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- ✅ 索引命名
    INDEX idx_game_sessions_user_id (user_id),
    INDEX idx_game_sessions_status (status),
    INDEX idx_game_sessions_created_at (created_at),
    
    -- ✅ 外键命名
    CONSTRAINT fk_game_sessions_user_id 
        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ✅ 查询格式化
SELECT 
    gs.id,
    gs.segment_id,
    gs.difficulty,
    gs.status,
    u.username,
    COUNT(td.id) as decision_count
FROM game_sessions gs
    INNER JOIN users u ON gs.user_id = u.id
    LEFT JOIN trading_decisions td ON gs.id = td.session_id
WHERE gs.status = 'ACTIVE'
    AND gs.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY gs.id, u.username
ORDER BY gs.created_at DESC
LIMIT 50;
```

---

## 提交规范

### Conventional Commits

我们使用[Conventional Commits](https://www.conventionalcommits.org/)规范：

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

#### 提交类型

```yaml
feat: 新功能
fix: Bug修复
docs: 文档更新
style: 代码格式化（不影响功能）
refactor: 代码重构
perf: 性能优化
test: 测试相关
build: 构建系统或依赖更新
ci: CI配置更新
chore: 其他维护性更改
```

#### 提交示例

```bash
# ✅ 好的提交信息
feat(game): add pause/resume functionality to game sessions
fix(api): handle null pointer exception in portfolio calculation
docs(readme): update installation instructions
test(frontend): add unit tests for GameSessionCard component
refactor(backend): extract common validation logic to utility class

# ✅ 包含详细描述的提交
feat(websocket): implement real-time kline data streaming

- Add WebSocket endpoint for kline data
- Implement STOMP message handling
- Add client-side subscription management
- Include error handling and reconnection logic

Closes #123
```

#### 提交前检查

```bash
# 运行预提交检查
npm run pre-commit

# 包含的检查项：
# - 代码格式化 (Prettier)
# - 代码检查 (ESLint/SonarLint)
# - 类型检查 (TypeScript)
# - 单元测试
# - 提交信息格式检查
```

---

## 分支策略

### Git Flow

我们采用简化的Git Flow策略：

```
main (生产分支)
├── develop (开发分支)
│   ├── feature/user-authentication
│   ├── feature/game-session-management
│   └── feature/real-time-data-streaming
├── hotfix/critical-bug-fix
└── release/v1.0.0
```

#### 分支命名规范

```yaml
功能分支: feature/brief-description
  - feature/user-authentication
  - feature/websocket-integration
  - feature/portfolio-management

修复分支: fix/brief-description
  - fix/session-timeout-issue
  - fix/memory-leak-in-kline-service

热修复分支: hotfix/brief-description
  - hotfix/critical-security-patch
  - hotfix/data-corruption-fix

发布分支: release/version
  - release/v1.0.0
  - release/v1.1.0
```

#### 分支工作流

```bash
# 1. 创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/new-feature

# 2. 开发和提交
git add .
git commit -m "feat: implement new feature"

# 3. 推送分支
git push origin feature/new-feature

# 4. 创建Pull Request
# 通过GitHub/GitLab界面创建PR

# 5. 合并后清理
git checkout develop
git pull origin develop
git branch -d feature/new-feature
```

---

## Pull Request流程

### PR创建清单

在创建Pull Request之前，请确保：

- [ ] 代码已通过所有测试
- [ ] 代码符合项目规范
- [ ] 已添加必要的测试用例
- [ ] 文档已更新（如适用）
- [ ] 提交信息符合规范
- [ ] 分支已与最新的develop同步

### PR模板

```markdown
## 变更描述
简要描述此PR的目的和实现的功能。

## 变更类型
- [ ] Bug修复
- [ ] 新功能
- [ ] 重构
- [ ] 文档更新
- [ ] 性能优化
- [ ] 测试改进

## 测试
- [ ] 单元测试已通过
- [ ] 集成测试已通过
- [ ] 手动测试已完成
- [ ] 新增测试用例

## 检查清单
- [ ] 代码符合项目规范
- [ ] 已添加必要的注释
- [ ] 文档已更新
- [ ] 无破坏性变更
- [ ] 已考虑向后兼容性

## 相关Issue
Closes #123
Related to #456

## 截图（如适用）
如果是UI相关的变更，请提供截图。

## 额外说明
任何需要审查者注意的特殊情况。
```

### 代码审查要求

#### 审查者职责

1. **功能审查**
   - 验证功能是否按预期工作
   - 检查边界条件和错误处理
   - 确认业务逻辑正确性

2. **代码质量审查**
   - 检查代码规范遵循情况
   - 评估代码可读性和可维护性
   - 识别潜在的性能问题

3. **安全审查**
   - 检查输入验证
   - 评估权限控制
   - 识别安全漏洞

#### 审查清单

```yaml
功能性:
  - [ ] 功能按预期工作
  - [ ] 边界条件处理正确
  - [ ] 错误处理完善
  - [ ] 性能影响可接受

代码质量:
  - [ ] 代码清晰易读
  - [ ] 命名规范合理
  - [ ] 注释充分有效
  - [ ] 无重复代码

测试:
  - [ ] 测试覆盖充分
  - [ ] 测试用例有意义
  - [ ] 测试可靠稳定

安全性:
  - [ ] 输入验证完善
  - [ ] 权限控制正确
  - [ ] 无安全漏洞
```

---

## 测试要求

### 测试策略

#### 后端测试

```java
// ✅ 单元测试示例
@ExtendWith(MockitoExtension.class)
class GameSessionServiceTest {
    
    @Mock
    private GameSessionRepository repository;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @InjectMocks
    private GameSessionService service;
    
    @Test
    @DisplayName("Should create game session successfully")
    void shouldCreateGameSessionSuccessfully() {
        // Given
        CreateGameSessionRequest request = CreateGameSessionRequest.builder()
            .segmentId("AAPL_2023_Q1")
            .difficulty(GameDifficulty.MEDIUM)
            .initialCapital(new BigDecimal("100000"))
            .build();
            
        GameSession expectedSession = GameSession.builder()
            .id("session_123")
            .segmentId(request.getSegmentId())
            .difficulty(request.getDifficulty())
            .initialCapital(request.getInitialCapital())
            .status(GameSessionStatus.CREATED)
            .build();
            
        when(repository.save(any(GameSession.class)))
            .thenReturn(expectedSession);
        
        // When
        GameSession result = service.createSession(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("session_123");
        assertThat(result.getStatus()).isEqualTo(GameSessionStatus.CREATED);
        
        verify(repository).save(any(GameSession.class));
    }
    
    @Test
    @DisplayName("Should throw exception when initial capital is negative")
    void shouldThrowExceptionWhenInitialCapitalIsNegative() {
        // Given
        CreateGameSessionRequest request = CreateGameSessionRequest.builder()
            .segmentId("AAPL_2023_Q1")
            .difficulty(GameDifficulty.MEDIUM)
            .initialCapital(new BigDecimal("-1000"))
            .build();
        
        // When & Then
        assertThatThrownBy(() -> service.createSession(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Initial capital must be positive");
    }
}
```

#### 前端测试

```typescript
// ✅ 组件测试示例
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { GameSessionCard } from './GameSessionCard';
import { GameSession, GameSessionStatus } from '@/types/game.types';

describe('GameSessionCard', () => {
  const mockSession: GameSession = {
    id: 'session_123',
    segmentId: 'AAPL_2023_Q1',
    difficulty: 'MEDIUM',
    status: GameSessionStatus.CREATED,
    initialCapital: 100000,
    createdAt: '2024-01-15T10:30:00Z',
  };

  it('renders session information correctly', () => {
    render(<GameSessionCard session={mockSession} />);
    
    expect(screen.getByText('session_123')).toBeInTheDocument();
    expect(screen.getByText('AAPL_2023_Q1')).toBeInTheDocument();
    expect(screen.getByText('MEDIUM')).toBeInTheDocument();
    expect(screen.getByText('CREATED')).toBeInTheDocument();
  });

  it('calls onSessionClick when card is clicked', () => {
    const onSessionClick = jest.fn();
    
    render(
      <GameSessionCard 
        session={mockSession} 
        onSessionClick={onSessionClick} 
      />
    );
    
    fireEvent.click(screen.getByRole('button'));
    
    expect(onSessionClick).toHaveBeenCalledWith('session_123');
  });

  it('shows loading state during async operations', async () => {
    const onSessionDelete = jest.fn().mockImplementation(
      () => new Promise(resolve => setTimeout(resolve, 100))
    );
    
    render(
      <GameSessionCard 
        session={mockSession} 
        onSessionDelete={onSessionDelete} 
      />
    );
    
    fireEvent.click(screen.getByText('Delete'));
    
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
    
    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });
  });
});
```

### 测试覆盖率要求

```yaml
最低覆盖率要求:
  - 后端代码: 80%
  - 前端组件: 75%
  - 工具函数: 90%
  - API接口: 85%

覆盖率检查:
  - 行覆盖率 (Line Coverage)
  - 分支覆盖率 (Branch Coverage)
  - 函数覆盖率 (Function Coverage)
```

### 测试命令

```bash
# 后端测试
./gradlew test                    # 运行所有测试
./gradlew test --tests "*Service*" # 运行特定测试
./gradlew jacocoTestReport        # 生成覆盖率报告

# 前端测试
npm run test                      # 运行所有测试
npm run test:watch               # 监视模式
npm run test:coverage            # 生成覆盖率报告
npm run test:e2e                 # 运行E2E测试

# 集成测试
docker-compose -f docker-compose.test.yml up --abort-on-container-exit
```

---

## 文档贡献

### 文档类型

1. **API文档**: 使用OpenAPI/Swagger规范
2. **代码注释**: 遵循JavaDoc/JSDoc规范
3. **用户文档**: Markdown格式
4. **架构文档**: 包含图表和详细说明

### 文档规范

#### API文档
```java
/**
 * 创建新的游戏会话
 * 
 * @param request 创建游戏会话的请求参数
 * @return 创建成功的游戏会话信息
 * @throws IllegalArgumentException 当请求参数无效时
 * @throws GameSessionLimitExceededException 当用户会话数量超过限制时
 */
@PostMapping("/game-sessions")
@Operation(
    summary = "创建游戏会话",
    description = "为当前用户创建一个新的游戏会话",
    responses = {
        @ApiResponse(
            responseCode = "201",
            description = "会话创建成功",
            content = @Content(schema = @Schema(implementation = GameSession.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数无效"
        )
    }
)
public ResponseEntity<ApiResponse<GameSession>> createGameSession(
    @Valid @RequestBody CreateGameSessionRequest request
) {
    // 实现
}
```

#### 代码注释
```typescript
/**
 * 游戏会话卡片组件
 * 
 * 显示游戏会话的基本信息，包括状态、难度、创建时间等。
 * 支持点击查看详情和删除操作。
 * 
 * @param session - 游戏会话数据
 * @param onSessionClick - 点击会话时的回调函数
 * @param onSessionDelete - 删除会话时的回调函数
 * @param className - 额外的CSS类名
 * 
 * @example
 * ```tsx
 * <GameSessionCard
 *   session={session}
 *   onSessionClick={(id) => navigate(`/game/${id}`)}
 *   onSessionDelete={(id) => deleteSession(id)}
 * />
 * ```
 */
export const GameSessionCard: React.FC<GameSessionCardProps> = ({
  session,
  onSessionClick,
  onSessionDelete,
  className,
}) => {
  // 实现
};
```

---

## 问题报告

### Bug报告模板

```markdown
## Bug描述
简要描述遇到的问题。

## 复现步骤
1. 进入游戏页面
2. 点击"创建会话"按钮
3. 选择难度为"困难"
4. 点击"开始游戏"

## 预期行为
应该成功创建并启动游戏会话。

## 实际行为
页面显示错误信息："无法创建会话"。

## 环境信息
- 操作系统: macOS 14.0
- 浏览器: Chrome 120.0
- 项目版本: v1.0.0
- Node.js版本: 18.17.0

## 错误日志
```
[ERROR] 2024-01-15 10:30:00 - Failed to create game session
java.lang.NullPointerException: Cannot invoke "String.length()" because "segmentId" is null
    at com.tradingsim.service.GameSessionService.createSession(GameSessionService.java:45)
```

## 附加信息
- 问题是否可以稳定复现: 是
- 是否影响其他功能: 否
- 临时解决方案: 刷新页面后重试
```

### 功能请求模板

```markdown
## 功能描述
希望添加游戏会话的暂停和恢复功能。

## 使用场景
用户在游戏过程中可能需要暂时离开，希望能够暂停当前会话，稍后继续游戏。

## 详细需求
1. 在游戏界面添加"暂停"按钮
2. 暂停时保存当前游戏状态
3. 提供"恢复"功能继续游戏
4. 暂停期间不接收新的K线数据

## 接受标准
- [ ] 用户可以暂停活跃的游戏会话
- [ ] 暂停状态下不推送新数据
- [ ] 用户可以恢复暂停的会话
- [ ] 恢复后继续从暂停点开始

## 优先级
中等 - 可以提升用户体验但不是核心功能

## 相关资源
- 设计稿: [链接]
- 相关讨论: #123
```

---

## 社区准则

### 行为准则

我们致力于为所有参与者提供友好、安全和欢迎的环境。请遵循以下准则：

#### 积极行为
- 使用友好和包容的语言
- 尊重不同的观点和经验
- 优雅地接受建设性批评
- 关注对社区最有利的事情
- 对其他社区成员表现出同理心

#### 不当行为
- 使用性化的语言或图像
- 人身攻击或政治攻击
- 公开或私下骚扰
- 未经明确许可发布他人的私人信息
- 其他在专业环境中可能被认为不当的行为

### 沟通渠道

- **GitHub Issues**: 报告Bug和功能请求
- **GitHub Discussions**: 技术讨论和问答
- **Pull Requests**: 代码审查和讨论
- **Email**: 私人或敏感问题

### 冲突解决

如果遇到冲突或不当行为：

1. 首先尝试直接沟通解决
2. 如果无法解决，联系项目维护者
3. 严重情况下，可以通过邮件举报

---

## 发布流程

### 版本号规范

我们使用[语义化版本](https://semver.org/)：

```
MAJOR.MINOR.PATCH

MAJOR: 不兼容的API变更
MINOR: 向后兼容的功能新增
PATCH: 向后兼容的问题修正
```

### 发布清单

```yaml
发布前检查:
  - [ ] 所有测试通过
  - [ ] 代码审查完成
  - [ ] 文档已更新
  - [ ] 变更日志已更新
  - [ ] 版本号已更新
  - [ ] 安全扫描通过

发布步骤:
  1. 创建release分支
  2. 更新版本号和变更日志
  3. 运行完整测试套件
  4. 创建发布标签
  5. 部署到生产环境
  6. 发布公告
```

---

## 获取帮助

### 学习资源

- [项目文档](../README.md)
- [架构设计](./ARCHITECTURE.md)
- [开发指南](./DEVELOPMENT_SETUP.md)
- [API文档](./API_DEVELOPMENT_GUIDE.md)

### 联系方式

- **技术问题**: 创建GitHub Issue
- **功能讨论**: 使用GitHub Discussions
- **安全问题**: security@tradingsim.com
- **其他问题**: contact@tradingsim.com

---

## 致谢

感谢所有为TradingSim项目做出贡献的开发者！您的参与使这个项目变得更好。

### 贡献者

- 查看[贡献者列表](https://github.com/your-org/tradingsim/contributors)
- 特别感谢核心维护团队
- 感谢所有提供反馈和建议的用户

---

*本贡献指南会随着项目的发展持续更新。如有建议或问题，请通过GitHub Issues反馈。*