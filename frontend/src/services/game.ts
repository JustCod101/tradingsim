// 游戏相关API服务
import { request } from './api'
import type { 
  GameSession, 
  GameDecision, 
  LeaderboardEntry, 
  OhlcvData,
  GameSegment,
  ApiResponse,
  PaginatedResponse 
} from '@/types'

// 游戏会话API
export const gameSessionApi = {
  // 创建新的游戏会话
  create(segmentId: string): Promise<ApiResponse<GameSession>> {
    return request.post('/api/game/sessions', { segmentId })
  },

  // 获取游戏会话详情
  getById(sessionId: string): Promise<ApiResponse<GameSession>> {
    return request.get(`/api/game/sessions/${sessionId}`)
  },

  // 获取用户的游戏会话列表
  getUserSessions(params?: {
    page?: number
    size?: number
    status?: string
    sortBy?: string
    sortOrder?: 'asc' | 'desc'
  }): Promise<ApiResponse<PaginatedResponse<GameSession>>> {
    return request.get('/api/game/sessions', { params })
  },

  // 开始游戏会话
  start(sessionId: string): Promise<ApiResponse<GameSession>> {
    return request.post(`/api/game/sessions/${sessionId}/start`)
  },

  // 暂停游戏会话
  pause(sessionId: string): Promise<ApiResponse<GameSession>> {
    return request.post(`/api/game/sessions/${sessionId}/pause`)
  },

  // 恢复游戏会话
  resume(sessionId: string): Promise<ApiResponse<GameSession>> {
    return request.post(`/api/game/sessions/${sessionId}/resume`)
  },

  // 结束游戏会话
  finish(sessionId: string): Promise<ApiResponse<GameSession>> {
    return request.post(`/api/game/sessions/${sessionId}/finish`)
  },

  // 删除游戏会话
  delete(sessionId: string): Promise<ApiResponse<void>> {
    return request.delete(`/api/game/sessions/${sessionId}`)
  }
}

// 游戏决策API
export const gameDecisionApi = {
  // 提交交易决策
  submit(sessionId: string, decision: {
    type: 'BUY' | 'SELL' | 'HOLD'
    quantity?: number
    price?: number
    reasoning?: string
  }): Promise<ApiResponse<GameDecision>> {
    return request.post(`/api/game/sessions/${sessionId}/decisions`, decision)
  },

  // 获取会话的决策历史
  getSessionDecisions(sessionId: string, params?: {
    page?: number
    size?: number
    type?: string
  }): Promise<ApiResponse<PaginatedResponse<GameDecision>>> {
    return request.get(`/api/game/sessions/${sessionId}/decisions`, { params })
  },

  // 获取决策详情
  getById(decisionId: string): Promise<ApiResponse<GameDecision>> {
    return request.get(`/api/game/decisions/${decisionId}`)
  },

  // 更新决策（仅在特定状态下允许）
  update(decisionId: string, updates: {
    reasoning?: string
    quantity?: number
  }): Promise<ApiResponse<GameDecision>> {
    return request.put(`/api/game/decisions/${decisionId}`, updates)
  }
}

// 市场数据API
export const marketDataApi = {
  // 获取OHLCV数据
  getOhlcvData(params: {
    symbol: string
    interval: string
    startTime?: number
    endTime?: number
    limit?: number
  }): Promise<ApiResponse<OhlcvData[]>> {
    return request.get('/api/market/ohlcv', { params })
  },

  // 获取实时价格
  getCurrentPrice(symbol: string): Promise<ApiResponse<{
    symbol: string
    price: number
    timestamp: number
    change24h: number
    volume24h: number
  }>> {
    return request.get(`/api/market/price/${symbol}`)
  },

  // 获取技术指标
  getTechnicalIndicators(params: {
    symbol: string
    indicator: string
    period: number
    startTime?: number
    endTime?: number
  }): Promise<ApiResponse<Array<{
    timestamp: number
    value: number
  }>>> {
    return request.get('/api/market/indicators', { params })
  },

  // 获取市场概览
  getMarketOverview(): Promise<ApiResponse<{
    totalMarketCap: number
    totalVolume24h: number
    btcDominance: number
    activeSymbols: number
    topGainers: Array<{
      symbol: string
      price: number
      change24h: number
    }>
    topLosers: Array<{
      symbol: string
      price: number
      change24h: number
    }>
  }>> {
    return request.get('/api/market/overview')
  }
}

// 游戏段落API
export const gameSegmentApi = {
  // 获取所有可用的游戏段落
  getAll(params?: {
    difficulty?: string
    duration?: number
    page?: number
    size?: number
  }): Promise<ApiResponse<PaginatedResponse<GameSegment>>> {
    return request.get('/api/game/segments', { params })
  },

  // 获取段落详情
  getById(segmentId: string): Promise<ApiResponse<GameSegment>> {
    return request.get(`/api/game/segments/${segmentId}`)
  },

  // 获取推荐段落
  getRecommended(userId?: string): Promise<ApiResponse<GameSegment[]>> {
    return request.get('/api/game/segments/recommended', { 
      params: userId ? { userId } : undefined 
    })
  },

  // 获取段落统计信息
  getStats(segmentId: string): Promise<ApiResponse<{
    totalSessions: number
    averageScore: number
    averageDuration: number
    completionRate: number
    difficultyRating: number
    topPerformers: Array<{
      userId: string
      username: string
      score: number
      duration: number
    }>
  }>> {
    return request.get(`/api/game/segments/${segmentId}/stats`)
  }
}

// 排行榜API
export const leaderboardApi = {
  // 获取全球排行榜
  getGlobal(params?: {
    period?: 'daily' | 'weekly' | 'monthly' | 'all'
    page?: number
    size?: number
  }): Promise<ApiResponse<PaginatedResponse<LeaderboardEntry>>> {
    return request.get('/api/leaderboard/global', { params })
  },

  // 获取段落排行榜
  getBySegment(segmentId: string, params?: {
    period?: 'daily' | 'weekly' | 'monthly' | 'all'
    page?: number
    size?: number
  }): Promise<ApiResponse<PaginatedResponse<LeaderboardEntry>>> {
    return request.get(`/api/leaderboard/segments/${segmentId}`, { params })
  },

  // 获取用户排名
  getUserRank(userId: string, params?: {
    period?: 'daily' | 'weekly' | 'monthly' | 'all'
    segmentId?: string
  }): Promise<ApiResponse<{
    rank: number
    score: number
    totalParticipants: number
    percentile: number
  }>> {
    return request.get(`/api/leaderboard/users/${userId}/rank`, { params })
  },

  // 获取用户周围的排名
  getUserContext(userId: string, params?: {
    period?: 'daily' | 'weekly' | 'monthly' | 'all'
    segmentId?: string
    range?: number
  }): Promise<ApiResponse<{
    userRank: number
    entries: LeaderboardEntry[]
  }>> {
    return request.get(`/api/leaderboard/users/${userId}/context`, { params })
  }
}

// 游戏统计API
export const gameStatsApi = {
  // 获取用户游戏统计
  getUserStats(userId: string): Promise<ApiResponse<{
    totalSessions: number
    completedSessions: number
    totalScore: number
    averageScore: number
    bestScore: number
    totalPlayTime: number
    winRate: number
    favoriteSegments: Array<{
      segmentId: string
      segmentName: string
      playCount: number
      averageScore: number
    }>
    recentPerformance: Array<{
      date: string
      sessionsPlayed: number
      averageScore: number
    }>
    achievements: Array<{
      id: string
      name: string
      description: string
      unlockedAt: string
    }>
  }>> {
    return request.get(`/api/game/stats/users/${userId}`)
  },

  // 获取全局游戏统计
  getGlobalStats(): Promise<ApiResponse<{
    totalUsers: number
    totalSessions: number
    totalDecisions: number
    averageSessionDuration: number
    mostPopularSegments: Array<{
      segmentId: string
      segmentName: string
      playCount: number
    }>
    dailyActiveUsers: number
    weeklyActiveUsers: number
    monthlyActiveUsers: number
  }>> {
    return request.get('/api/game/stats/global')
  }
}

// 游戏回放API
export const gameReplayApi = {
  // 创建回放
  create(sessionId: string, speed?: number): Promise<ApiResponse<{
    replayId: string
    sessionId: string
    speed: number
    status: string
    websocketUrl: string
    startTime: string
  }>> {
    return request.post('/api/game/replays', { sessionId, speed })
  },

  // 获取回放详情
  getById(replayId: string): Promise<ApiResponse<{
    replayId: string
    sessionId: string
    speed: number
    status: string
    websocketUrl: string
    startTime: string
    endTime?: string
  }>> {
    return request.get(`/api/game/replays/${replayId}`)
  },

  // 控制回放
  control(replayId: string, action: 'start' | 'pause' | 'resume' | 'stop' | 'seek', params?: {
    timestamp?: number
    speed?: number
  }): Promise<ApiResponse<void>> {
    return request.post(`/api/game/replays/${replayId}/control`, { action, ...params })
  },

  // 删除回放
  delete(replayId: string): Promise<ApiResponse<void>> {
    return request.delete(`/api/game/replays/${replayId}`)
  }
}