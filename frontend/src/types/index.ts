// 基础类型定义
export interface BaseEntity {
  id: string
  createdAt: string
  updatedAt: string
}

// API 响应类型
export interface ApiResponse<T = any> {
  success: boolean
  data: T
  message?: string
  code?: number
  timestamp: string
}

export interface PaginatedResponse<T = any> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

// 游戏会话相关类型
export interface GameSession extends BaseEntity {
  userId: string
  segmentId: string
  status: GameSessionStatus
  currentFrame: number
  totalFrames: number
  totalScore: number
  totalPnl: number
  buyDecisions: number
  sellDecisions: number
  skipDecisions: number
  timeoutCount: number
  averageResponseTime: number
  seed: number
  progress: number
  winRate: number
}

export enum GameSessionStatus {
  CREATED = 'CREATED',
  STARTED = 'STARTED',
  PAUSED = 'PAUSED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  TIMEOUT = 'TIMEOUT'
}

export enum GameSessionAction {
  START = 'START',
  PAUSE = 'PAUSE',
  RESUME = 'RESUME',
  COMPLETE = 'COMPLETE',
  CANCEL = 'CANCEL'
}

// 交易决策相关类型
export interface GameDecision extends BaseEntity {
  sessionId: string
  frameIndex: number
  decisionType: DecisionType
  price?: number
  quantity?: number
  pnl: number
  score: number
  responseTime: number
  clientId?: string
}

export enum DecisionType {
  BUY = 'BUY',
  SELL = 'SELL',
  SKIP = 'SKIP'
}

// OHLCV 数据类型
export interface OhlcvData extends BaseEntity {
  stockCode: string
  timestamp: string
  open: number
  high: number
  low: number
  close: number
  volume: number
  adjustedClose?: number
}

// 游戏段落类型
export interface GameSegment {
  id: string
  stockCode: string
  startTime: string
  endTime: string
  totalFrames: number
  volatility: number
  averageVolume: number
  minPrice: number
  maxPrice: number
  difficulty: SegmentDifficulty
}

export enum SegmentDifficulty {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  HARD = 'HARD',
  EXPERT = 'EXPERT'
}

// 排行榜类型
export interface LeaderboardEntry {
  rank: number
  sessionId: string
  userId: string
  segmentId: string
  totalScore: number
  totalPnl: number
  winRate: number
  averageResponseTime: number
  totalDecisions: number
  timeoutCount: number
  completedAt: string
}

// WebSocket 消息类型
export interface WebSocketMessage<T = any> {
  type: WebSocketMessageType
  data: T
  timestamp: string
  sessionId?: string
}

export enum WebSocketMessageType {
  // 连接相关
  CONNECT = 'CONNECT',
  DISCONNECT = 'DISCONNECT',
  HEARTBEAT = 'HEARTBEAT',
  
  // 游戏相关
  GAME_START = 'GAME_START',
  GAME_PAUSE = 'GAME_PAUSE',
  GAME_RESUME = 'GAME_RESUME',
  GAME_END = 'GAME_END',
  FRAME_UPDATE = 'FRAME_UPDATE',
  DECISION_RESULT = 'DECISION_RESULT',
  SCORE_UPDATE = 'SCORE_UPDATE',
  
  // 系统相关
  ERROR = 'ERROR',
  NOTIFICATION = 'NOTIFICATION'
}

// 游戏帧数据
export interface GameFrame {
  index: number
  timestamp: string
  ohlcv: OhlcvData
  indicators?: Record<string, number>
  keypoint?: boolean
  confidence?: number
}

// 用户相关类型
export interface User {
  id: string
  username: string
  email: string
  avatar?: string
  level: number
  experience: number
  totalSessions: number
  totalScore: number
  winRate: number
  roles?: string[]
  token?: string
  createdAt: string
}

// 主题相关类型
export interface ThemeConfig {
  mode: 'light' | 'dark' | 'auto'
  primaryColor: string
  borderRadius: number
  fontSize: number
  compactMode: boolean
}

// 应用配置类型
export interface AppConfig {
  theme: ThemeConfig
  language: string
  autoSave: boolean
  soundEnabled: boolean
  animationEnabled: boolean
  debugMode: boolean
}

// 错误类型
export interface AppError {
  code: string
  message: string
  details?: any
  timestamp: string
  stack?: string
}

// 通知类型
export interface Notification {
  id: string
  type: 'success' | 'warning' | 'error' | 'info'
  title: string
  message: string
  duration?: number
  actions?: NotificationAction[]
  timestamp: string
}

export interface NotificationAction {
  label: string
  action: () => void
  type?: 'primary' | 'default'
}

// 模态框类型
export interface Modal {
  id: string
  component: string
  props?: Record<string, any>
  options?: ModalOptions
}

export interface ModalOptions {
  title?: string
  width?: string | number
  height?: string | number
  closable?: boolean
  maskClosable?: boolean
  destroyOnClose?: boolean
  centered?: boolean
  zIndex?: number
}

// 图表相关类型
export interface ChartConfig {
  type: 'candlestick' | 'line' | 'bar'
  theme: 'light' | 'dark'
  height: number
  showVolume: boolean
  showIndicators: boolean
  indicators: string[]
  timeRange: string
}

export interface ChartData {
  timestamp: string
  open: number
  high: number
  low: number
  close: number
  volume: number
}

// 表单相关类型
export interface FormRule {
  required?: boolean
  message?: string
  trigger?: string | string[]
  min?: number
  max?: number
  pattern?: RegExp
  validator?: (rule: any, value: any, callback: any) => void
}

export interface FormRules {
  [key: string]: FormRule | FormRule[]
}

// 路由相关类型
export interface RouteMetaData {
  title?: string
  icon?: string
  hidden?: boolean
  keepAlive?: boolean
  requiresAuth?: boolean
  roles?: string[]
  transition?: string
  layout?: string
  [key: string]: any
}

// 工具类型
export type Nullable<T> = T | null
export type Optional<T> = T | undefined
export type DeepPartial<T> = {
  [P in keyof T]?: T[P] extends object ? DeepPartial<T[P]> : T[P]
}
export type DeepReadonly<T> = {
  readonly [P in keyof T]: T[P] extends object ? DeepReadonly<T[P]> : T[P]
}

// 事件类型
export interface EventBusEvents {
  'app:error': AppError
  'app:notification': Notification
  'game:start': { sessionId: string }
  'game:end': { sessionId: string; score: number }
  'websocket:connected': void
  'websocket:disconnected': void
  'websocket:error': Error
  'theme:changed': ThemeConfig
  'user:login': User
  'user:logout': void
}