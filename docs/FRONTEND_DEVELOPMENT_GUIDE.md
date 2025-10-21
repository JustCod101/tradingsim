# TradingSim 前端开发指南

## 📋 目录

- [技术栈概览](#技术栈概览)
- [项目结构](#项目结构)
- [开发环境配置](#开发环境配置)
- [组件开发规范](#组件开发规范)
- [状态管理](#状态管理)
- [路由配置](#路由配置)
- [API集成](#api集成)
- [WebSocket集成](#websocket集成)
- [样式和主题](#样式和主题)
- [性能优化](#性能优化)
- [测试策略](#测试策略)
- [构建和部署](#构建和部署)
- [最佳实践](#最佳实践)

---

## 技术栈概览

### 核心技术
```yaml
框架: React 18.2.0
语言: TypeScript 5.0+
构建工具: Vite 4.4+
包管理: npm 9.0+

UI组件库: Ant Design 5.0+
图表库: Apache ECharts 5.4+
状态管理: Zustand 4.4+
路由: React Router 6.15+

WebSocket: SockJS + STOMP
HTTP客户端: Axios 1.5+
工具库: Lodash, Day.js
开发工具: ESLint, Prettier, Husky
```

### 开发工具链
```yaml
IDE: VS Code (推荐)
浏览器: Chrome DevTools
调试: React Developer Tools
性能: Lighthouse, React Profiler
测试: Jest, React Testing Library
```

---

## 项目结构

### 目录组织
```
frontend/
├── public/                 # 静态资源
│   ├── favicon.ico
│   └── index.html
├── src/                   # 源代码
│   ├── components/        # 通用组件
│   │   ├── common/       # 基础组件
│   │   ├── charts/       # 图表组件
│   │   └── layout/       # 布局组件
│   ├── pages/            # 页面组件
│   │   ├── auth/         # 认证页面
│   │   ├── game/         # 游戏页面
│   │   └── dashboard/    # 仪表盘页面
│   ├── hooks/            # 自定义Hooks
│   ├── services/         # API服务
│   ├── stores/           # 状态管理
│   ├── types/            # TypeScript类型定义
│   ├── utils/            # 工具函数
│   ├── styles/           # 样式文件
│   ├── constants/        # 常量定义
│   └── App.tsx           # 应用入口
├── tests/                # 测试文件
├── docs/                 # 文档
├── package.json          # 依赖配置
├── tsconfig.json         # TypeScript配置
├── vite.config.ts        # Vite配置
└── README.md             # 项目说明
```

### 文件命名规范
```yaml
组件文件: PascalCase
  - GameSession.tsx
  - KlineChart.tsx
  - UserProfile.tsx

Hook文件: camelCase + use前缀
  - useGameSession.ts
  - useWebSocket.ts
  - useAuth.ts

工具文件: camelCase
  - formatUtils.ts
  - apiClient.ts
  - constants.ts

类型文件: camelCase + .types后缀
  - game.types.ts
  - api.types.ts
  - common.types.ts
```

---

## 开发环境配置

### 1. 环境变量配置

#### .env.development
```bash
# 开发环境配置
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_BASE_URL=ws://localhost:8080
VITE_APP_TITLE=TradingSim - Development
VITE_LOG_LEVEL=debug
VITE_ENABLE_MOCK=false
```

#### .env.production
```bash
# 生产环境配置
VITE_API_BASE_URL=https://api.tradingsim.com
VITE_WS_BASE_URL=wss://api.tradingsim.com
VITE_APP_TITLE=TradingSim
VITE_LOG_LEVEL=error
VITE_ENABLE_MOCK=false
```

### 2. TypeScript配置

#### tsconfig.json
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"],
      "@/components/*": ["src/components/*"],
      "@/pages/*": ["src/pages/*"],
      "@/hooks/*": ["src/hooks/*"],
      "@/services/*": ["src/services/*"],
      "@/stores/*": ["src/stores/*"],
      "@/types/*": ["src/types/*"],
      "@/utils/*": ["src/utils/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

### 3. Vite配置

#### vite.config.ts
```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          antd: ['antd'],
          charts: ['echarts'],
        },
      },
    },
  },
})
```

---

## 组件开发规范

### 1. 组件结构

#### 函数组件模板
```typescript
import React, { useState, useEffect, useCallback } from 'react';
import { Button, Card, message } from 'antd';
import { GameSession } from '@/types/game.types';
import { useGameSession } from '@/hooks/useGameSession';
import styles from './GameSessionCard.module.css';

interface GameSessionCardProps {
  sessionId: string;
  onSessionStart?: (sessionId: string) => void;
  onSessionEnd?: (sessionId: string) => void;
  className?: string;
}

/**
 * 游戏会话卡片组件
 * 
 * 显示游戏会话的基本信息和操作按钮
 * 支持启动、暂停、结束游戏会话
 */
export const GameSessionCard: React.FC<GameSessionCardProps> = ({
  sessionId,
  onSessionStart,
  onSessionEnd,
  className,
}) => {
  const [loading, setLoading] = useState(false);
  const { session, startSession, endSession, error } = useGameSession(sessionId);

  // 处理会话启动
  const handleStart = useCallback(async () => {
    try {
      setLoading(true);
      await startSession();
      onSessionStart?.(sessionId);
      message.success('游戏会话已启动');
    } catch (err) {
      message.error('启动失败');
    } finally {
      setLoading(false);
    }
  }, [sessionId, startSession, onSessionStart]);

  // 处理会话结束
  const handleEnd = useCallback(async () => {
    try {
      setLoading(true);
      await endSession();
      onSessionEnd?.(sessionId);
      message.success('游戏会话已结束');
    } catch (err) {
      message.error('结束失败');
    } finally {
      setLoading(false);
    }
  }, [sessionId, endSession, onSessionEnd]);

  // 错误处理
  useEffect(() => {
    if (error) {
      message.error(error.message);
    }
  }, [error]);

  if (!session) {
    return <Card loading />;
  }

  return (
    <Card
      className={`${styles.sessionCard} ${className || ''}`}
      title={`会话 ${session.id}`}
      extra={
        <div className={styles.actions}>
          {session.status === 'CREATED' && (
            <Button
              type="primary"
              loading={loading}
              onClick={handleStart}
            >
              启动
            </Button>
          )}
          {session.status === 'ACTIVE' && (
            <Button
              danger
              loading={loading}
              onClick={handleEnd}
            >
              结束
            </Button>
          )}
        </div>
      }
    >
      <div className={styles.content}>
        <div className={styles.info}>
          <span>状态: {session.status}</span>
          <span>难度: {session.difficulty}</span>
          <span>创建时间: {session.createdAt}</span>
        </div>
        {session.portfolio && (
          <div className={styles.portfolio}>
            <span>总价值: ${session.portfolio.totalValue}</span>
            <span>现金: ${session.portfolio.cashBalance}</span>
            <span>盈亏: ${session.portfolio.unrealizedPnl}</span>
          </div>
        )}
      </div>
    </Card>
  );
};

export default GameSessionCard;
```

### 2. Props类型定义

#### 类型定义规范
```typescript
// 基础Props接口
interface BaseComponentProps {
  className?: string;
  style?: React.CSSProperties;
  children?: React.ReactNode;
}

// 具体组件Props
interface KlineChartProps extends BaseComponentProps {
  data: KlineData[];
  width?: number;
  height?: number;
  showVolume?: boolean;
  onDataPointClick?: (point: KlineData) => void;
  loading?: boolean;
}

// 事件处理器类型
type EventHandler<T = void> = (event: T) => void | Promise<void>;
type ClickHandler = EventHandler<React.MouseEvent>;
type ChangeHandler<T> = EventHandler<T>;

// 组件状态类型
interface ComponentState {
  loading: boolean;
  error: Error | null;
  data: any[];
}
```

### 3. 自定义Hooks

#### useGameSession Hook
```typescript
import { useState, useEffect, useCallback } from 'react';
import { GameSession, CreateGameSessionRequest } from '@/types/game.types';
import { gameSessionService } from '@/services/gameSessionService';

interface UseGameSessionReturn {
  session: GameSession | null;
  loading: boolean;
  error: Error | null;
  createSession: (request: CreateGameSessionRequest) => Promise<void>;
  startSession: () => Promise<void>;
  endSession: () => Promise<void>;
  refresh: () => Promise<void>;
}

export const useGameSession = (sessionId?: string): UseGameSessionReturn => {
  const [session, setSession] = useState<GameSession | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  // 获取会话详情
  const fetchSession = useCallback(async () => {
    if (!sessionId) return;

    try {
      setLoading(true);
      setError(null);
      const response = await gameSessionService.getSession(sessionId);
      setSession(response.data);
    } catch (err) {
      setError(err as Error);
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  // 创建会话
  const createSession = useCallback(async (request: CreateGameSessionRequest) => {
    try {
      setLoading(true);
      setError(null);
      const response = await gameSessionService.createSession(request);
      setSession(response.data);
    } catch (err) {
      setError(err as Error);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  // 启动会话
  const startSession = useCallback(async () => {
    if (!sessionId) return;

    try {
      setLoading(true);
      setError(null);
      await gameSessionService.startSession(sessionId);
      await fetchSession(); // 刷新会话状态
    } catch (err) {
      setError(err as Error);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [sessionId, fetchSession]);

  // 结束会话
  const endSession = useCallback(async () => {
    if (!sessionId) return;

    try {
      setLoading(true);
      setError(null);
      await gameSessionService.endSession(sessionId);
      await fetchSession(); // 刷新会话状态
    } catch (err) {
      setError(err as Error);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [sessionId, fetchSession]);

  // 初始化加载
  useEffect(() => {
    fetchSession();
  }, [fetchSession]);

  return {
    session,
    loading,
    error,
    createSession,
    startSession,
    endSession,
    refresh: fetchSession,
  };
};
```

---

## 状态管理

### 1. Zustand Store设计

#### 游戏状态管理
```typescript
import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';
import { GameSession, KlineData, TradingDecision } from '@/types/game.types';

interface GameState {
  // 状态
  currentSession: GameSession | null;
  klineData: KlineData[];
  decisions: TradingDecision[];
  isPlaying: boolean;
  isPaused: boolean;
  currentFrame: number;
  
  // 操作
  setCurrentSession: (session: GameSession | null) => void;
  addKlineData: (data: KlineData) => void;
  addDecision: (decision: TradingDecision) => void;
  setPlaying: (playing: boolean) => void;
  setPaused: (paused: boolean) => void;
  setCurrentFrame: (frame: number) => void;
  reset: () => void;
}

export const useGameStore = create<GameState>()(
  devtools(
    persist(
      (set, get) => ({
        // 初始状态
        currentSession: null,
        klineData: [],
        decisions: [],
        isPlaying: false,
        isPaused: false,
        currentFrame: 0,

        // 操作方法
        setCurrentSession: (session) => 
          set({ currentSession: session }, false, 'setCurrentSession'),

        addKlineData: (data) =>
          set(
            (state) => ({
              klineData: [...state.klineData, data],
              currentFrame: state.currentFrame + 1,
            }),
            false,
            'addKlineData'
          ),

        addDecision: (decision) =>
          set(
            (state) => ({
              decisions: [...state.decisions, decision],
            }),
            false,
            'addDecision'
          ),

        setPlaying: (playing) =>
          set({ isPlaying: playing }, false, 'setPlaying'),

        setPaused: (paused) =>
          set({ isPaused: paused }, false, 'setPaused'),

        setCurrentFrame: (frame) =>
          set({ currentFrame: frame }, false, 'setCurrentFrame'),

        reset: () =>
          set(
            {
              currentSession: null,
              klineData: [],
              decisions: [],
              isPlaying: false,
              isPaused: false,
              currentFrame: 0,
            },
            false,
            'reset'
          ),
      }),
      {
        name: 'game-store',
        partialize: (state) => ({
          currentSession: state.currentSession,
          decisions: state.decisions,
        }),
      }
    ),
    { name: 'GameStore' }
  )
);
```

#### 用户状态管理
```typescript
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { User, AuthToken } from '@/types/auth.types';

interface AuthState {
  user: User | null;
  token: AuthToken | null;
  isAuthenticated: boolean;
  loading: boolean;
  
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  setUser: (user: User) => void;
  setToken: (token: AuthToken) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()(
  devtools(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      loading: false,

      login: async (email, password) => {
        set({ loading: true });
        try {
          const response = await authService.login({ email, password });
          const { user, token } = response.data;
          
          set({
            user,
            token,
            isAuthenticated: true,
            loading: false,
          });
          
          // 保存到localStorage
          localStorage.setItem('auth_token', token.accessToken);
        } catch (error) {
          set({ loading: false });
          throw error;
        }
      },

      logout: () => {
        set({
          user: null,
          token: null,
          isAuthenticated: false,
        });
        localStorage.removeItem('auth_token');
      },

      setUser: (user) => set({ user }),
      setToken: (token) => set({ token }),
      clearAuth: () => set({
        user: null,
        token: null,
        isAuthenticated: false,
      }),
    }),
    { name: 'AuthStore' }
  )
);
```

### 2. 状态选择器

#### 性能优化的选择器
```typescript
import { shallow } from 'zustand/shallow';

// 基础选择器
export const useCurrentSession = () => 
  useGameStore((state) => state.currentSession);

export const useKlineData = () => 
  useGameStore((state) => state.klineData);

// 组合选择器
export const useGameStatus = () =>
  useGameStore(
    (state) => ({
      isPlaying: state.isPlaying,
      isPaused: state.isPaused,
      currentFrame: state.currentFrame,
    }),
    shallow
  );

// 计算选择器
export const useLatestKlineData = () =>
  useGameStore((state) => {
    const data = state.klineData;
    return data.length > 0 ? data[data.length - 1] : null;
  });

export const usePortfolioSummary = () =>
  useGameStore((state) => {
    const session = state.currentSession;
    return session?.portfolio || null;
  });
```

---

## 路由配置

### 1. 路由结构

#### 路由定义
```typescript
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { Layout } from '@/components/layout/Layout';
import { AuthGuard } from '@/components/auth/AuthGuard';
import { LoginPage } from '@/pages/auth/LoginPage';
import { RegisterPage } from '@/pages/auth/RegisterPage';
import { DashboardPage } from '@/pages/dashboard/DashboardPage';
import { GamePage } from '@/pages/game/GamePage';
import { GameSessionPage } from '@/pages/game/GameSessionPage';
import { ProfilePage } from '@/pages/profile/ProfilePage';
import { NotFoundPage } from '@/pages/error/NotFoundPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        index: true,
        element: <Navigate to="/dashboard" replace />,
      },
      {
        path: 'auth',
        children: [
          {
            path: 'login',
            element: <LoginPage />,
          },
          {
            path: 'register',
            element: <RegisterPage />,
          },
        ],
      },
      {
        path: 'dashboard',
        element: (
          <AuthGuard>
            <DashboardPage />
          </AuthGuard>
        ),
      },
      {
        path: 'game',
        element: <AuthGuard />,
        children: [
          {
            index: true,
            element: <GamePage />,
          },
          {
            path: ':sessionId',
            element: <GameSessionPage />,
          },
        ],
      },
      {
        path: 'profile',
        element: (
          <AuthGuard>
            <ProfilePage />
          </AuthGuard>
        ),
      },
      {
        path: '*',
        element: <NotFoundPage />,
      },
    ],
  },
]);
```

### 2. 路由守卫

#### 认证守卫
```typescript
import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';

interface AuthGuardProps {
  children: React.ReactNode;
}

export const AuthGuard: React.FC<AuthGuardProps> = ({ children }) => {
  const { isAuthenticated } = useAuthStore();
  const location = useLocation();

  if (!isAuthenticated) {
    return (
      <Navigate
        to="/auth/login"
        state={{ from: location }}
        replace
      />
    );
  }

  return <>{children}</>;
};
```

#### 路由钩子
```typescript
import { useNavigate, useLocation, useParams } from 'react-router-dom';
import { useCallback } from 'react';

export const useAppNavigation = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const goToGame = useCallback((sessionId?: string) => {
    if (sessionId) {
      navigate(`/game/${sessionId}`);
    } else {
      navigate('/game');
    }
  }, [navigate]);

  const goToDashboard = useCallback(() => {
    navigate('/dashboard');
  }, [navigate]);

  const goBack = useCallback(() => {
    navigate(-1);
  }, [navigate]);

  return {
    goToGame,
    goToDashboard,
    goBack,
    currentPath: location.pathname,
  };
};
```

---

## API集成

### 1. HTTP客户端配置

#### Axios配置
```typescript
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { message } from 'antd';
import { useAuthStore } from '@/stores/authStore';

// API响应类型
interface ApiResponse<T = any> {
  success: boolean;
  data: T;
  error?: {
    code: string;
    message: string;
    details?: any[];
  };
  meta?: {
    timestamp: string;
    requestId: string;
  };
}

// 创建axios实例
const createApiClient = (): AxiosInstance => {
  const client = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // 请求拦截器
  client.interceptors.request.use(
    (config) => {
      const { token } = useAuthStore.getState();
      if (token?.accessToken) {
        config.headers.Authorization = `Bearer ${token.accessToken}`;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  // 响应拦截器
  client.interceptors.response.use(
    (response: AxiosResponse<ApiResponse>) => {
      const { data } = response;
      
      if (!data.success) {
        const errorMessage = data.error?.message || '请求失败';
        message.error(errorMessage);
        return Promise.reject(new Error(errorMessage));
      }
      
      return response;
    },
    (error) => {
      if (error.response?.status === 401) {
        useAuthStore.getState().logout();
        window.location.href = '/auth/login';
      } else if (error.response?.status >= 500) {
        message.error('服务器错误，请稍后重试');
      } else if (error.code === 'ECONNABORTED') {
        message.error('请求超时，请检查网络连接');
      }
      
      return Promise.reject(error);
    }
  );

  return client;
};

export const apiClient = createApiClient();
```

### 2. API服务封装

#### 游戏会话服务
```typescript
import { apiClient } from './apiClient';
import { 
  GameSession, 
  CreateGameSessionRequest, 
  UpdateGameSessionRequest,
  GameSessionListResponse 
} from '@/types/game.types';

class GameSessionService {
  private readonly basePath = '/api/v1/game-sessions';

  // 创建游戏会话
  async createSession(request: CreateGameSessionRequest) {
    const response = await apiClient.post<ApiResponse<GameSession>>(
      this.basePath,
      request
    );
    return response.data;
  }

  // 获取游戏会话详情
  async getSession(sessionId: string) {
    const response = await apiClient.get<ApiResponse<GameSession>>(
      `${this.basePath}/${sessionId}`
    );
    return response.data;
  }

  // 获取用户的游戏会话列表
  async getUserSessions(params?: {
    page?: number;
    size?: number;
    status?: string;
  }) {
    const response = await apiClient.get<ApiResponse<GameSessionListResponse>>(
      this.basePath,
      { params }
    );
    return response.data;
  }

  // 启动游戏会话
  async startSession(sessionId: string) {
    const response = await apiClient.post<ApiResponse<void>>(
      `${this.basePath}/${sessionId}/start`
    );
    return response.data;
  }

  // 暂停游戏会话
  async pauseSession(sessionId: string) {
    const response = await apiClient.post<ApiResponse<void>>(
      `${this.basePath}/${sessionId}/pause`
    );
    return response.data;
  }

  // 结束游戏会话
  async endSession(sessionId: string) {
    const response = await apiClient.post<ApiResponse<void>>(
      `${this.basePath}/${sessionId}/end`
    );
    return response.data;
  }

  // 提交交易决策
  async submitDecision(sessionId: string, decision: {
    action: 'BUY' | 'SELL' | 'HOLD';
    quantity?: number;
    price?: number;
    reason?: string;
  }) {
    const response = await apiClient.post<ApiResponse<void>>(
      `${this.basePath}/${sessionId}/decisions`,
      decision
    );
    return response.data;
  }
}

export const gameSessionService = new GameSessionService();
```

### 3. React Query集成

#### 查询钩子
```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { gameSessionService } from '@/services/gameSessionService';
import { message } from 'antd';

// 查询键工厂
export const gameSessionKeys = {
  all: ['gameSessions'] as const,
  lists: () => [...gameSessionKeys.all, 'list'] as const,
  list: (filters: Record<string, any>) => 
    [...gameSessionKeys.lists(), { filters }] as const,
  details: () => [...gameSessionKeys.all, 'detail'] as const,
  detail: (id: string) => [...gameSessionKeys.details(), id] as const,
};

// 获取游戏会话详情
export const useGameSession = (sessionId: string) => {
  return useQuery({
    queryKey: gameSessionKeys.detail(sessionId),
    queryFn: () => gameSessionService.getSession(sessionId),
    enabled: !!sessionId,
    staleTime: 30 * 1000, // 30秒
    cacheTime: 5 * 60 * 1000, // 5分钟
  });
};

// 获取用户游戏会话列表
export const useUserGameSessions = (params?: {
  page?: number;
  size?: number;
  status?: string;
}) => {
  return useQuery({
    queryKey: gameSessionKeys.list(params || {}),
    queryFn: () => gameSessionService.getUserSessions(params),
    keepPreviousData: true,
  });
};

// 创建游戏会话
export const useCreateGameSession = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: gameSessionService.createSession,
    onSuccess: (data) => {
      // 更新缓存
      queryClient.invalidateQueries({ queryKey: gameSessionKeys.lists() });
      queryClient.setQueryData(
        gameSessionKeys.detail(data.data.id),
        data
      );
      message.success('游戏会话创建成功');
    },
    onError: (error: Error) => {
      message.error(`创建失败: ${error.message}`);
    },
  });
};

// 启动游戏会话
export const useStartGameSession = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: gameSessionService.startSession,
    onSuccess: (_, sessionId) => {
      queryClient.invalidateQueries({ 
        queryKey: gameSessionKeys.detail(sessionId) 
      });
      message.success('游戏已启动');
    },
    onError: (error: Error) => {
      message.error(`启动失败: ${error.message}`);
    },
  });
};
```

---

## WebSocket集成

### 1. WebSocket客户端

#### STOMP客户端封装
```typescript
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuthStore } from '@/stores/authStore';

export type MessageHandler<T = any> = (message: T) => void;

export interface WebSocketMessage {
  type: string;
  sessionId: string;
  timestamp: string;
  sequenceNumber?: number;
  data: any;
}

class WebSocketClient {
  private client: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private messageHandlers: Map<string, MessageHandler[]> = new Map();

  constructor() {
    this.initializeClient();
  }

  private initializeClient() {
    const wsUrl = import.meta.env.VITE_WS_BASE_URL;
    
    this.client = new Client({
      webSocketFactory: () => new SockJS(`${wsUrl}/ws`),
      connectHeaders: this.getConnectHeaders(),
      debug: (str) => {
        if (import.meta.env.DEV) {
          console.log('STOMP Debug:', str);
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = this.onConnect.bind(this);
    this.client.onDisconnect = this.onDisconnect.bind(this);
    this.client.onStompError = this.onError.bind(this);
  }

  private getConnectHeaders() {
    const { token } = useAuthStore.getState();
    return {
      Authorization: token?.accessToken ? `Bearer ${token.accessToken}` : '',
    };
  }

  private onConnect() {
    console.log('WebSocket connected');
  }

  private onDisconnect() {
    console.log('WebSocket disconnected');
  }

  private onError(error: any) {
    console.error('WebSocket error:', error);
  }

  // 连接WebSocket
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (!this.client) {
        reject(new Error('WebSocket client not initialized'));
        return;
      }

      this.client.onConnect = () => {
        this.onConnect();
        resolve();
      };

      this.client.onStompError = (error) => {
        this.onError(error);
        reject(error);
      };

      this.client.activate();
    });
  }

  // 断开连接
  disconnect() {
    if (this.client?.connected) {
      this.client.deactivate();
    }
    this.subscriptions.clear();
    this.messageHandlers.clear();
  }

  // 订阅主题
  subscribe<T = any>(
    destination: string,
    handler: MessageHandler<T>,
    headers?: Record<string, string>
  ): string {
    if (!this.client?.connected) {
      throw new Error('WebSocket not connected');
    }

    const subscriptionId = `sub_${Date.now()}_${Math.random()}`;
    
    const subscription = this.client.subscribe(
      destination,
      (message: IMessage) => {
        try {
          const data = JSON.parse(message.body) as WebSocketMessage;
          handler(data);
        } catch (error) {
          console.error('Failed to parse WebSocket message:', error);
        }
      },
      headers
    );

    this.subscriptions.set(subscriptionId, subscription);
    
    // 注册消息处理器
    if (!this.messageHandlers.has(destination)) {
      this.messageHandlers.set(destination, []);
    }
    this.messageHandlers.get(destination)!.push(handler);

    return subscriptionId;
  }

  // 取消订阅
  unsubscribe(subscriptionId: string) {
    const subscription = this.subscriptions.get(subscriptionId);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(subscriptionId);
    }
  }

  // 发送消息
  send(destination: string, body: any, headers?: Record<string, string>) {
    if (!this.client?.connected) {
      throw new Error('WebSocket not connected');
    }

    this.client.publish({
      destination,
      body: JSON.stringify(body),
      headers,
    });
  }
}

export const webSocketClient = new WebSocketClient();
```

### 2. WebSocket Hooks

#### useWebSocket Hook
```typescript
import { useEffect, useRef, useCallback } from 'react';
import { webSocketClient, MessageHandler, WebSocketMessage } from '@/services/webSocketClient';

interface UseWebSocketOptions {
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: any) => void;
  autoConnect?: boolean;
}

export const useWebSocket = (options: UseWebSocketOptions = {}) => {
  const { onConnect, onDisconnect, onError, autoConnect = true } = options;
  const isConnectedRef = useRef(false);

  const connect = useCallback(async () => {
    try {
      await webSocketClient.connect();
      isConnectedRef.current = true;
      onConnect?.();
    } catch (error) {
      onError?.(error);
    }
  }, [onConnect, onError]);

  const disconnect = useCallback(() => {
    webSocketClient.disconnect();
    isConnectedRef.current = false;
    onDisconnect?.();
  }, [onDisconnect]);

  const subscribe = useCallback(<T = any>(
    destination: string,
    handler: MessageHandler<T>
  ) => {
    return webSocketClient.subscribe(destination, handler);
  }, []);

  const unsubscribe = useCallback((subscriptionId: string) => {
    webSocketClient.unsubscribe(subscriptionId);
  }, []);

  const send = useCallback((destination: string, body: any) => {
    webSocketClient.send(destination, body);
  }, []);

  useEffect(() => {
    if (autoConnect) {
      connect();
    }

    return () => {
      disconnect();
    };
  }, [autoConnect, connect, disconnect]);

  return {
    connect,
    disconnect,
    subscribe,
    unsubscribe,
    send,
    isConnected: isConnectedRef.current,
  };
};
```

#### useGameWebSocket Hook
```typescript
import { useEffect, useCallback } from 'react';
import { useWebSocket } from './useWebSocket';
import { useGameStore } from '@/stores/gameStore';
import { KlineData, GameEvent } from '@/types/game.types';

export const useGameWebSocket = (sessionId: string) => {
  const { addKlineData, setPlaying, setPaused } = useGameStore();
  
  const { subscribe, unsubscribe, send, isConnected } = useWebSocket({
    onConnect: () => {
      console.log('Game WebSocket connected');
    },
    onDisconnect: () => {
      console.log('Game WebSocket disconnected');
    },
    onError: (error) => {
      console.error('Game WebSocket error:', error);
    },
  });

  // 处理K线数据
  const handleKlineFrame = useCallback((message: WebSocketMessage) => {
    if (message.type === 'KLINE_FRAME') {
      const klineData: KlineData = message.data;
      addKlineData(klineData);
    }
  }, [addKlineData]);

  // 处理游戏事件
  const handleGameEvent = useCallback((message: WebSocketMessage) => {
    const event: GameEvent = message.data;
    
    switch (event.type) {
      case 'GAME_STARTED':
        setPlaying(true);
        break;
      case 'GAME_PAUSED':
        setPaused(true);
        break;
      case 'GAME_RESUMED':
        setPaused(false);
        break;
      case 'GAME_ENDED':
        setPlaying(false);
        break;
      case 'KEYPOINT_PAUSE':
        // 处理关键点暂停
        break;
    }
  }, [setPlaying, setPaused]);

  // 提交交易决策
  const submitDecision = useCallback((decision: {
    action: 'BUY' | 'SELL' | 'HOLD';
    quantity?: number;
    price?: number;
    reason?: string;
  }) => {
    send(`/app/session.${sessionId}.decision`, decision);
  }, [sessionId, send]);

  // 订阅游戏数据
  useEffect(() => {
    if (!isConnected || !sessionId) return;

    const subscriptions: string[] = [];

    // 订阅K线数据
    const klineSubscription = subscribe(
      `/topic/session.${sessionId}.frames`,
      handleKlineFrame
    );
    subscriptions.push(klineSubscription);

    // 订阅游戏事件
    const eventSubscription = subscribe(
      `/topic/session.${sessionId}.events`,
      handleGameEvent
    );
    subscriptions.push(eventSubscription);

    return () => {
      subscriptions.forEach(unsubscribe);
    };
  }, [isConnected, sessionId, subscribe, unsubscribe, handleKlineFrame, handleGameEvent]);

  return {
    submitDecision,
    isConnected,
  };
};
```

---

## 样式和主题

### 1. CSS Modules

#### 组件样式
```css
/* GameSessionCard.module.css */
.sessionCard {
  margin-bottom: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.sessionCard:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.actions {
  display: flex;
  gap: 8px;
}

.content {
  padding: 16px 0;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.info span {
  font-size: 14px;
  color: #666;
}

.portfolio {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.portfolio span {
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .portfolio {
    flex-direction: column;
    gap: 8px;
  }
}
```

### 2. Ant Design主题定制

#### 主题配置
```typescript
import { ConfigProvider, theme } from 'antd';
import type { ThemeConfig } from 'antd';

const customTheme: ThemeConfig = {
  algorithm: theme.defaultAlgorithm,
  token: {
    // 主色调
    colorPrimary: '#1890ff',
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#ff4d4f',
    colorInfo: '#1890ff',
    
    // 字体
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    fontSize: 14,
    
    // 圆角
    borderRadius: 6,
    
    // 间距
    padding: 16,
    margin: 16,
    
    // 阴影
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
  },
  components: {
    Button: {
      borderRadius: 6,
      controlHeight: 36,
    },
    Card: {
      borderRadius: 8,
      paddingLG: 24,
    },
    Table: {
      borderRadius: 8,
      headerBg: '#fafafa',
    },
    Input: {
      borderRadius: 6,
      controlHeight: 36,
    },
  },
};

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <ConfigProvider theme={customTheme}>
      {children}
    </ConfigProvider>
  );
};
```

### 3. 暗色主题支持

#### 主题切换
```typescript
import { useState, useEffect } from 'react';
import { ConfigProvider, theme } from 'antd';

export const useTheme = () => {
  const [isDark, setIsDark] = useState(false);

  useEffect(() => {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
      setIsDark(true);
    }
  }, []);

  const toggleTheme = () => {
    const newTheme = !isDark;
    setIsDark(newTheme);
    localStorage.setItem('theme', newTheme ? 'dark' : 'light');
  };

  const themeConfig = {
    algorithm: isDark ? theme.darkAlgorithm : theme.defaultAlgorithm,
    token: {
      colorPrimary: '#1890ff',
      // 其他主题配置
    },
  };

  return {
    isDark,
    toggleTheme,
    themeConfig,
  };
};

export const AppThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { themeConfig } = useTheme();

  return (
    <ConfigProvider theme={themeConfig}>
      {children}
    </ConfigProvider>
  );
};
```

---

## 性能优化

### 1. 组件优化

#### React.memo和useMemo
```typescript
import React, { memo, useMemo, useCallback } from 'react';
import { KlineData } from '@/types/game.types';

interface KlineChartProps {
  data: KlineData[];
  width: number;
  height: number;
  onDataPointClick?: (point: KlineData) => void;
}

export const KlineChart = memo<KlineChartProps>(({
  data,
  width,
  height,
  onDataPointClick,
}) => {
  // 缓存图表配置
  const chartOptions = useMemo(() => {
    return {
      xAxis: {
        type: 'category',
        data: data.map(item => item.timestamp),
      },
      yAxis: {
        type: 'value',
      },
      series: [{
        type: 'candlestick',
        data: data.map(item => [item.open, item.close, item.low, item.high]),
      }],
    };
  }, [data]);

  // 缓存事件处理器
  const handleClick = useCallback((params: any) => {
    const dataIndex = params.dataIndex;
    if (dataIndex >= 0 && dataIndex < data.length) {
      onDataPointClick?.(data[dataIndex]);
    }
  }, [data, onDataPointClick]);

  return (
    <div style={{ width, height }}>
      {/* ECharts组件 */}
    </div>
  );
}, (prevProps, nextProps) => {
  // 自定义比较函数
  return (
    prevProps.width === nextProps.width &&
    prevProps.height === nextProps.height &&
    prevProps.data.length === nextProps.data.length &&
    prevProps.onDataPointClick === nextProps.onDataPointClick
  );
});
```

### 2. 虚拟化列表

#### 大数据列表优化
```typescript
import { FixedSizeList as List } from 'react-window';
import { GameSession } from '@/types/game.types';

interface VirtualizedGameSessionListProps {
  sessions: GameSession[];
  onSessionClick: (session: GameSession) => void;
}

const SessionItem = memo<{
  index: number;
  style: React.CSSProperties;
  data: {
    sessions: GameSession[];
    onSessionClick: (session: GameSession) => void;
  };
}>(({ index, style, data }) => {
  const session = data.sessions[index];
  
  return (
    <div style={style}>
      <div
        className="session-item"
        onClick={() => data.onSessionClick(session)}
      >
        <h4>{session.id}</h4>
        <p>状态: {session.status}</p>
        <p>创建时间: {session.createdAt}</p>
      </div>
    </div>
  );
});

export const VirtualizedGameSessionList: React.FC<VirtualizedGameSessionListProps> = ({
  sessions,
  onSessionClick,
}) => {
  const itemData = useMemo(() => ({
    sessions,
    onSessionClick,
  }), [sessions, onSessionClick]);

  return (
    <List
      height={600}
      itemCount={sessions.length}
      itemSize={120}
      itemData={itemData}
    >
      {SessionItem}
    </List>
  );
};
```

### 3. 代码分割

#### 路由级别的代码分割
```typescript
import { lazy, Suspense } from 'react';
import { Spin } from 'antd';

// 懒加载页面组件
const DashboardPage = lazy(() => import('@/pages/dashboard/DashboardPage'));
const GamePage = lazy(() => import('@/pages/game/GamePage'));
const GameSessionPage = lazy(() => import('@/pages/game/GameSessionPage'));

// 加载组件
const PageLoader = () => (
  <div style={{ 
    display: 'flex', 
    justifyContent: 'center', 
    alignItems: 'center', 
    height: '200px' 
  }}>
    <Spin size="large" />
  </div>
);

// 路由配置
export const router = createBrowserRouter([
  {
    path: '/dashboard',
    element: (
      <Suspense fallback={<PageLoader />}>
        <DashboardPage />
      </Suspense>
    ),
  },
  {
    path: '/game',
    element: (
      <Suspense fallback={<PageLoader />}>
        <GamePage />
      </Suspense>
    ),
  },
  {
    path: '/game/:sessionId',
    element: (
      <Suspense fallback={<PageLoader />}>
        <GameSessionPage />
      </Suspense>
    ),
  },
]);
```

---

## 测试策略

### 1. 单元测试

#### 组件测试
```typescript
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { GameSessionCard } from '@/components/game/GameSessionCard';
import { gameSessionService } from '@/services/gameSessionService';

// Mock服务
jest.mock('@/services/gameSessionService');
const mockGameSessionService = gameSessionService as jest.Mocked<typeof gameSessionService>;

// 测试工具函数
const createTestQueryClient = () => new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

const renderWithProviders = (component: React.ReactElement) => {
  const queryClient = createTestQueryClient();
  return render(
    <QueryClientProvider client={queryClient}>
      {component}
    </QueryClientProvider>
  );
};

describe('GameSessionCard', () => {
  const mockSession = {
    id: 'session_123',
    status: 'CREATED',
    segmentId: 'AAPL_2023_Q1',
    difficulty: 'MEDIUM',
    createdAt: '2024-01-15T10:30:00Z',
    portfolio: {
      totalValue: 100000,
      cashBalance: 50000,
      unrealizedPnl: 0,
    },
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders session information correctly', () => {
    renderWithProviders(
      <GameSessionCard sessionId="session_123" />
    );

    expect(screen.getByText('会话 session_123')).toBeInTheDocument();
    expect(screen.getByText('状态: CREATED')).toBeInTheDocument();
    expect(screen.getByText('难度: MEDIUM')).toBeInTheDocument();
  });

  it('calls onSessionStart when start button is clicked', async () => {
    const onSessionStart = jest.fn();
    mockGameSessionService.startSession.mockResolvedValue({ success: true });

    renderWithProviders(
      <GameSessionCard 
        sessionId="session_123" 
        onSessionStart={onSessionStart}
      />
    );

    const startButton = screen.getByText('启动');
    fireEvent.click(startButton);

    await waitFor(() => {
      expect(mockGameSessionService.startSession).toHaveBeenCalledWith('session_123');
      expect(onSessionStart).toHaveBeenCalledWith('session_123');
    });
  });

  it('shows loading state during API call', async () => {
    mockGameSessionService.startSession.mockImplementation(
      () => new Promise(resolve => setTimeout(resolve, 100))
    );

    renderWithProviders(
      <GameSessionCard sessionId="session_123" />
    );

    const startButton = screen.getByText('启动');
    fireEvent.click(startButton);

    expect(startButton).toHaveAttribute('disabled');
    expect(screen.getByRole('button', { name: /loading/i })).toBeInTheDocument();
  });
});
```

### 2. 集成测试

#### API集成测试
```typescript
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useGameSession } from '@/hooks/useGameSession';
import { server } from '@/mocks/server';
import { rest } from 'msw';

// Mock服务器设置
beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  );
};

describe('useGameSession', () => {
  it('fetches game session successfully', async () => {
    const { result } = renderHook(
      () => useGameSession('session_123'),
      { wrapper: createWrapper() }
    );

    await waitFor(() => {
      expect(result.current.session).toBeDefined();
      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBeNull();
    });

    expect(result.current.session?.id).toBe('session_123');
  });

  it('handles API error correctly', async () => {
    server.use(
      rest.get('/api/v1/game-sessions/session_123', (req, res, ctx) => {
        return res(ctx.status(404), ctx.json({
          success: false,
          error: {
            code: 'GAME_SESSION_NOT_FOUND',
            message: 'Game session not found',
          },
        }));
      })
    );

    const { result } = renderHook(
      () => useGameSession('session_123'),
      { wrapper: createWrapper() }
    );

    await waitFor(() => {
      expect(result.current.error).toBeDefined();
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error?.message).toContain('Game session not found');
  });
});
```

### 3. E2E测试

#### Playwright测试
```typescript
import { test, expect } from '@playwright/test';

test.describe('Game Session Flow', () => {
  test.beforeEach(async ({ page }) => {
    // 登录
    await page.goto('/auth/login');
    await page.fill('[data-testid=email-input]', 'test@example.com');
    await page.fill('[data-testid=password-input]', 'password123');
    await page.click('[data-testid=login-button]');
    await expect(page).toHaveURL('/dashboard');
  });

  test('should create and start a game session', async ({ page }) => {
    // 导航到游戏页面
    await page.click('[data-testid=nav-game]');
    await expect(page).toHaveURL('/game');

    // 创建新会话
    await page.click('[data-testid=create-session-button]');
    await page.selectOption('[data-testid=segment-select]', 'AAPL_2023_Q1');
    await page.selectOption('[data-testid=difficulty-select]', 'MEDIUM');
    await page.fill('[data-testid=initial-capital-input]', '100000');
    await page.click('[data-testid=create-button]');

    // 验证会话创建成功
    await expect(page.locator('[data-testid=session-card]')).toBeVisible();
    await expect(page.locator('[data-testid=session-status]')).toHaveText('CREATED');

    // 启动会话
    await page.click('[data-testid=start-session-button]');
    await expect(page.locator('[data-testid=session-status]')).toHaveText('ACTIVE');

    // 验证K线图显示
    await expect(page.locator('[data-testid=kline-chart]')).toBeVisible();
  });

  test('should submit trading decision', async ({ page }) => {
    // 假设已有活跃会话
    await page.goto('/game/session_123');

    // 等待K线数据加载
    await expect(page.locator('[data-testid=kline-chart]')).toBeVisible();

    // 提交买入决策
    await page.click('[data-testid=buy-button]');
    await page.fill('[data-testid=quantity-input]', '100');
    await page.fill('[data-testid=reason-input]', 'Technical breakout');
    await page.click('[data-testid=submit-decision-button]');

    // 验证决策提交成功
    await expect(page.locator('[data-testid=decision-success-message]')).toBeVisible();
    await expect(page.locator('[data-testid=portfolio-positions]')).toContainText('100');
  });
});
```

---

## 构建和部署

### 1. 构建配置

#### 生产构建优化
```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { visualizer } from 'rollup-plugin-visualizer'

export default defineConfig({
  plugins: [
    react(),
    visualizer({
      filename: 'dist/stats.html',
      open: true,
      gzipSize: true,
    }),
  ],
  build: {
    outDir: 'dist',
    sourcemap: true,
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
      },
    },
    rollupOptions: {
      output: {
        manualChunks: {
          // 第三方库分包
          vendor: ['react', 'react-dom'],
          antd: ['antd', '@ant-design/icons'],
          charts: ['echarts', 'echarts-for-react'],
          utils: ['lodash', 'dayjs', 'axios'],
        },
        // 文件命名
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name.split('.');
          const ext = info[info.length - 1];
          if (/\.(mp4|webm|ogg|mp3|wav|flac|aac)(\?.*)?$/i.test(assetInfo.name)) {
            return `media/[name]-[hash].${ext}`;
          }
          if (/\.(png|jpe?g|gif|svg)(\?.*)?$/.test(assetInfo.name)) {
            return `images/[name]-[hash].${ext}`;
          }
          if (ext === 'css') {
            return `css/[name]-[hash].${ext}`;
          }
          return `assets/[name]-[hash].${ext}`;
        },
      },
    },
    // 压缩阈值
    chunkSizeWarningLimit: 1000,
  },
})
```

### 2. Docker部署

#### Dockerfile
```dockerfile
# 多阶段构建
FROM node:18-alpine AS builder

WORKDIR /app

# 复制package文件
COPY package*.json ./

# 安装依赖
RUN npm ci --only=production

# 复制源代码
COPY . .

# 构建应用
RUN npm run build

# 生产镜像
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制nginx配置
COPY nginx.conf /etc/nginx/nginx.conf

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
```

#### nginx.conf
```nginx
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    # 启用gzip压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/javascript
        application/xml+rss
        application/json;

    server {
        listen 80;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html;

        # 处理SPA路由
        location / {
            try_files $uri $uri/ /index.html;
        }

        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }

        # API代理
        location /api/ {
            proxy_pass http://backend:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # WebSocket代理
        location /ws/ {
            proxy_pass http://backend:8080;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

### 3. CI/CD配置

#### GitHub Actions
```yaml
name: Frontend CI/CD

on:
  push:
    branches: [ main, develop ]
    paths: [ 'frontend/**' ]
  pull_request:
    branches: [ main ]
    paths: [ 'frontend/**' ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      
      - name: Install dependencies
        working-directory: frontend
        run: npm ci
      
      - name: Run linting
        working-directory: frontend
        run: npm run lint
      
      - name: Run type checking
        working-directory: frontend
        run: npm run type-check
      
      - name: Run tests
        working-directory: frontend
        run: npm run test:coverage
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          directory: frontend/coverage

  build:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      
      - name: Install dependencies
        working-directory: frontend
        run: npm ci
      
      - name: Build application
        working-directory: frontend
        run: npm run build
        env:
          VITE_API_BASE_URL: ${{ secrets.PROD_API_URL }}
          VITE_WS_BASE_URL: ${{ secrets.PROD_WS_URL }}
      
      - name: Build Docker image
        run: |
          docker build -t tradingsim-frontend:${{ github.sha }} ./frontend
          docker tag tradingsim-frontend:${{ github.sha }} tradingsim-frontend:latest
      
      - name: Deploy to production
        run: |
          # 部署脚本
          echo "Deploying to production..."
```

---

## 最佳实践

### 1. 代码组织

#### 文件结构最佳实践
```yaml
组件组织:
  - 按功能模块分组，而非技术类型
  - 每个组件一个文件夹，包含组件、样式、测试
  - 使用index.ts文件简化导入路径

命名规范:
  - 组件使用PascalCase
  - 文件名与组件名保持一致
  - Hook使用camelCase + use前缀
  - 常量使用UPPER_SNAKE_CASE

导入顺序:
  1. React相关导入
  2. 第三方库导入
  3. 内部组件导入
  4. 类型导入
  5. 样式导入
```

#### 组件设计原则
```typescript
// ✅ 好的组件设计
interface ButtonProps {
  variant?: 'primary' | 'secondary' | 'danger';
  size?: 'small' | 'medium' | 'large';
  loading?: boolean;
  disabled?: boolean;
  onClick?: () => void;
  children: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'medium',
  loading = false,
  disabled = false,
  onClick,
  children,
}) => {
  return (
    <button
      className={`btn btn-${variant} btn-${size}`}
      disabled={disabled || loading}
      onClick={onClick}
    >
      {loading ? <Spinner /> : children}
    </button>
  );
};

// ❌ 避免的设计
interface BadButtonProps {
  isPrimary?: boolean;
  isSecondary?: boolean;
  isDanger?: boolean;
  isSmall?: boolean;
  isMedium?: boolean;
  isLarge?: boolean;
  // 太多布尔值，难以维护
}
```

### 2. 性能最佳实践

#### 避免不必要的重渲染
```typescript
// ✅ 使用useCallback缓存函数
const GameSessionList: React.FC = () => {
  const [sessions, setSessions] = useState<GameSession[]>([]);

  const handleSessionClick = useCallback((sessionId: string) => {
    navigate(`/game/${sessionId}`);
  }, [navigate]);

  const handleSessionDelete = useCallback((sessionId: string) => {
    setSessions(prev => prev.filter(s => s.id !== sessionId));
  }, []);

  return (
    <div>
      {sessions.map(session => (
        <GameSessionCard
          key={session.id}
          session={session}
          onSessionClick={handleSessionClick}
          onSessionDelete={handleSessionDelete}
        />
      ))}
    </div>
  );
};

// ✅ 使用useMemo缓存计算结果
const PortfolioSummary: React.FC<{ portfolio: Portfolio }> = ({ portfolio }) => {
  const summary = useMemo(() => {
    return {
      totalValue: portfolio.positions.reduce((sum, pos) => sum + pos.marketValue, 0),
      totalPnl: portfolio.positions.reduce((sum, pos) => sum + pos.unrealizedPnl, 0),
      winRate: calculateWinRate(portfolio.trades),
    };
  }, [portfolio]);

  return <div>{/* 渲染摘要 */}</div>;
};
```

### 3. 错误处理最佳实践

#### 错误边界
```typescript
import React, { Component, ErrorInfo, ReactNode } from 'react';
import { Result, Button } from 'antd';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
    
    // 发送错误报告到监控服务
    if (import.meta.env.PROD) {
      // reportError(error, errorInfo);
    }
  }

  private handleRetry = () => {
    this.setState({ hasError: false, error: undefined });
  };

  public render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return (
        <Result
          status="error"
          title="出现了一些问题"
          subTitle="页面遇到了意外错误，请尝试刷新页面或联系技术支持。"
          extra={[
            <Button type="primary" onClick={this.handleRetry} key="retry">
              重试
            </Button>,
            <Button onClick={() => window.location.reload()} key="refresh">
              刷新页面
            </Button>,
          ]}
        />
      );
    }

    return this.props.children;
  }
}
```

### 4. 安全最佳实践

#### XSS防护
```typescript
// ✅ 安全的用户输入处理
import DOMPurify from 'dompurify';

const UserComment: React.FC<{ comment: string }> = ({ comment }) => {
  const sanitizedComment = useMemo(() => {
    return DOMPurify.sanitize(comment);
  }, [comment]);

  return (
    <div dangerouslySetInnerHTML={{ __html: sanitizedComment }} />
  );
};

// ✅ 安全的URL处理
const isValidUrl = (url: string): boolean => {
  try {
    const parsedUrl = new URL(url);
    return ['http:', 'https:'].includes(parsedUrl.protocol);
  } catch {
    return false;
  }
};

const SafeLink: React.FC<{ href: string; children: ReactNode }> = ({ href, children }) => {
  if (!isValidUrl(href)) {
    return <span>{children}</span>;
  }

  return (
    <a href={href} target="_blank" rel="noopener noreferrer">
      {children}
    </a>
  );
};
```

### 5. 可访问性最佳实践

#### 无障碍设计
```typescript
// ✅ 良好的可访问性实践
const AccessibleButton: React.FC<{
  onClick: () => void;
  children: ReactNode;
  ariaLabel?: string;
  disabled?: boolean;
}> = ({ onClick, children, ariaLabel, disabled = false }) => {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      aria-label={ariaLabel}
      aria-disabled={disabled}
      role="button"
      tabIndex={disabled ? -1 : 0}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          onClick();
        }
      }}
    >
      {children}
    </button>
  );
};

// ✅ 表单可访问性
const AccessibleForm: React.FC = () => {
  return (
    <form>
      <div>
        <label htmlFor="email">邮箱地址</label>
        <input
          id="email"
          type="email"
          required
          aria-describedby="email-help"
          aria-invalid={false}
        />
        <div id="email-help">请输入有效的邮箱地址</div>
      </div>
    </form>
  );
};
```

---

## 总结

本指南涵盖了TradingSim前端开发的各个方面，从项目结构到部署配置，从性能优化到安全实践。遵循这些指南可以确保：

1. **代码质量**: 通过TypeScript、ESLint和测试保证代码质量
2. **性能优化**: 通过代码分割、虚拟化和缓存提升性能
3. **用户体验**: 通过响应式设计和无障碍功能提升用户体验
4. **开发效率**: 通过标准化的开发流程和工具提升开发效率
5. **可维护性**: 通过清晰的架构和文档保证项目的可维护性

### 下一步建议

1. 阅读 [项目架构设计文档](./ARCHITECTURE.md) 了解整体架构
2. 参考 [API开发指南](./API_DEVELOPMENT_GUIDE.md) 进行后端集成
3. 查看 [开发环境搭建指南](./DEVELOPMENT_SETUP.md) 配置开发环境
4. 遵循即将创建的贡献指南进行代码贡献

### 获取帮助

- 项目文档: `/docs` 目录
- 问题反馈: GitHub Issues
- 技术讨论: GitHub Discussions
- 代码审查: Pull Request

---

*本文档会随着项目的发展持续更新，请定期查看最新版本。*