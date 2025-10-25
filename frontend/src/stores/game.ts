import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useWebSocketStore } from './websocket'
import { useAppStore } from './app'
import { GameSessionStatus, DecisionType, SegmentDifficulty } from '@/types'
import type { GameSession, GameDecision } from '@/types'
import { request } from '@/services/api'

export const useGameStore = defineStore('game', () => {
  // 状态
  const isConnected = ref(false)
  const isInGame = ref(false)
  const isStarting = ref(false)
  const currentSession = ref<GameSession | null>(null)
  const currentBalance = ref(10000) // 初始资金
  const decisions = ref<GameDecision[]>([])

  // WebSocket 和 App store
  const wsStore = useWebSocketStore()
  const appStore = useAppStore()

  // 计算属性
  const sessionId = computed(() => currentSession.value?.id)
  const score = computed(() => currentSession.value?.score || 0)
  const totalPnl = computed(() => currentSession.value?.totalPnl || 0)
  const currentFrame = computed(() => currentSession.value?.currentFrame || 0)
  const totalFrames = computed(() => currentSession.value?.totalFrames || 0)
  const progress = computed(() => currentSession.value?.progress || 0)
  const winRate = computed(() => currentSession.value?.winRate || 0)

  // 初始化
  async function initialize() {
    try {
      // 监听 WebSocket 连接状态
      wsStore.$subscribe((mutation, state) => {
        isConnected.value = state.isConnected
      })

      // 设置游戏消息处理器
      setupGameMessageHandlers()

      console.log('✅ Game store initialized')
    } catch (error) {
      console.error('❌ Game store initialization failed:', error)
      throw error
    }
  }

  // 设置游戏消息处理器
  function setupGameMessageHandlers() {
    // 这里会在 WebSocket 服务中注册消息处理器
    // wsStore.onMessage('GAME_START', handleGameStarted)
    // wsStore.onMessage('GAME_END', handleGameEnded)
    // wsStore.onMessage('DECISION_RESULT', handleDecisionResult)
    // wsStore.onMessage('FRAME_UPDATE', handleFrameUpdate)
  }

  // 开始新游戏
  async function startGame(stockCode: string , difficulty: string) {
    try {
      isStarting.value = true

      console.log('正在发送游戏会话创建请求...')
      console.log(`请求URL: ${import.meta.env.VITE_API_BASE_URL}/game/sessions?stockCode=${stockCode}&difficulty=${difficulty}`)
      
      // 直接使用axios发送请求，绕过request服务
      const axios = (await import('axios')).default
      const response = await axios({
        method: 'post',
        url: `${import.meta.env.VITE_API_BASE_URL}/game/sessions`,
        params: {
          stockCode,
          difficulty
        },
        headers: {
          'Content-Type': 'application/json'
        }
      })
      
      console.log('收到响应:', response)
      
      // 处理会话创建成功
      console.log('Game session created:', response.data)
      const sessionData = response.data
      
      // 启动游戏会话 - 从CREATED状态变为RUNNING状态
      console.log('正在启动游戏会话...')
      const startResponse = await axios({
        method: 'post',
        url: `${import.meta.env.VITE_API_BASE_URL}/game/sessions/${sessionData.sessionId}/start`,
        headers: {
          'Content-Type': 'application/json'
        }
      })
      
      console.log('游戏会话启动成功:', startResponse.data)
      
      // 使用启动后的会话数据
      const startedSessionData = startResponse.data || sessionData
      
      handleGameStarted({
        id: startedSessionData.sessionId,
        userId: startedSessionData.userId || 'anonymous',
        stockCode: startedSessionData.stockCode || 'AAPL',
        status: GameSessionStatus.STARTED,
        currentFrame: 0,
        totalFrames: startedSessionData.totalFrames || 100,
        score: 0,
        totalPnl: 0,
        longDecisions: 0,
        shortDecisions: 0,
        timeoutCount: 0,
        averageResponseTime: 0,
        seed: startedSessionData.seed || Math.floor(Math.random() * 1000000),
        progress: 0,
        winRate: 0,
        createdAt: startedSessionData.createdAt || new Date().toISOString(),
        updatedAt: startedSessionData.updatedAt || new Date().toISOString(),
        difficulty: startedSessionData.difficulty || SegmentDifficulty.EASY
      })

    } catch (error) {
      console.error('Failed to start game:', error)
      appStore.addError({
        type: 'error',
        title: '游戏启动失败',
        message: '无法启动游戏，请稍后重试'
      })
    } finally {
      isStarting.value = false
    }
  }

  // 结束游戏
  async function endGame() {
    try {
      if (!currentSession.value) return

      // 发送结束游戏消息
      await wsStore.sendMessage(`/game/sessions/${sessionId.value}/finish`, {
        type: 'GAME_END',
        data: {
          sessionId: sessionId.value
        },
        timestamp: new Date().toISOString()
      })

      // 模拟游戏结束
      setTimeout(() => {
        if (currentSession.value) {
          currentSession.value.status = GameSessionStatus.COMPLETED
          currentSession.value.updatedAt = new Date().toISOString()
        }
        isInGame.value = false

        appStore.addNotification({
          type: 'success',
          title: '游戏结束',
          message: `游戏已结束，总分: ${score.value}，盈亏: $${totalPnl.value.toFixed(2)}`
        })
      }, 500)

    } catch (error) {
      console.error('Failed to end game:', error)
      appStore.addError({
        type: 'error',
        title: '游戏结束失败',
        message: '无法正常结束游戏'
      })
    }
  }

  // 做出交易决策
  async function makeDecision(type: DecisionType, price?: number, quantity?: number) {
    try {
      if (!isInGame.value || !currentSession.value) {
        throw new Error('Game not active')
      }

      // 确保获取最新的帧索引
      // 首先尝试从服务器获取最新的会话状态
      try {
        const sessionResponse = await request.get(`/game/sessions/${sessionId.value}`)
        if (sessionResponse.data && sessionResponse.data.currentFrame !== undefined) {
          // 更新本地状态
          currentSession.value.currentFrame = sessionResponse.data.currentFrame
        }
      } catch (e) {
        console.warn('无法获取最新会话状态，使用本地帧索引', e)
      }

      // 使用REST API提交决策
      console.log('提交决策请求:', {
        url: `/game/sessions/${sessionId.value}/decisions`,
        data: {
          frameIndex: currentSession.value.currentFrame,
          decisionType: type,
          price: price || 0,
          quantity: quantity || 1,
          timestampMs: Date.now()
        }
      })
      
      // 记录当前帧索引，用于调试
      console.log('提交决策时的帧索引:', currentSession.value.currentFrame)
      
      // 生成一个临时ID，以防后端没有返回
      const tempId = Date.now().toString()
      let decision: GameDecision;
      
      try {
        const response = await request.post(`/game/sessions/${sessionId.value}/decisions`, {
          frameIndex: currentSession.value.currentFrame, // 直接使用会话对象的currentFrame而不是计算属性
          decisionType: type,
          price: price || 0,
          quantity: quantity || 1,
          timestampMs: Date.now()
        })

        
        // 正确提取响应数据 - response.data包含实际的决策数据
        const decisionData = response.data || {}
        console.log('收到决策响应数据:', decisionData)
        console.log('决策数据类型:', typeof decisionData)
        
        // 确保帧索引一致性 - 添加安全检查
        const responseFrameIndex = decisionData?.frameIndex !== undefined ? decisionData.frameIndex : currentSession.value?.currentFrame || 0
        console.log('使用的帧索引:', responseFrameIndex)
        
        // 使用响应数据创建决策对象
        decision = {
          id: decisionData.decisionId || tempId, // 如果decisionId不存在，使用临时ID
          sessionId: sessionId.value!,
          frameIndex: responseFrameIndex,
          decisionType: decisionData?.decisionType || type,
          price: decisionData?.price || price || 0,
          quantity: quantity || 1,
          pnl: decisionData?.profit || 0, // 使用profit字段作为盈亏
          score: decisionData?.profitPercentage || 0,
          responseTime: decisionData?.responseTimeMs || 0,
          createdAt: decisionData?.createdAt || new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }
        
        console.log('创建的决策对象:', decision)
        console.log('决策盈亏值:', decision.pnl)
      } catch (error) {
        console.error('决策API请求失败:', error)
        
        // 即使API失败，也创建一个本地决策对象
        decision = {
          id: tempId,
          sessionId: sessionId.value!,
          frameIndex: currentFrame.value,
          decisionType: type,
          price: price || 0,
          quantity: quantity || 1,
          pnl: 0,
          score: 0,
          responseTime: 0,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }
      }
      
      handleDecisionResult(decision)

    } catch (error) {
      console.error('Failed to make decision:', error)
      appStore.addError({
        type: 'error',
        title: '决策失败',
        message: error instanceof Error ? error.message : '决策执行失败'
      })
    }
  }

  // 处理游戏开始
  function handleGameStarted(session: GameSession) {
    currentSession.value = session
    currentBalance.value = 10000
    decisions.value = []
    isInGame.value = true

    appStore.addNotification({
      type: 'success',
      title: '游戏开始',
      message: '游戏已成功启动，祝您交易愉快！'
    })
  }

  // 处理决策结果
  function handleDecisionResult(decision: GameDecision) {
    console.log('处理决策结果:', decision)
    console.log('决策盈亏:', decision.pnl)
    console.log('当前余额（更新前）:', currentBalance.value)
    
    decisions.value.push(decision)
    
    if (currentSession.value) {
      // 更新会话统计
      currentSession.value.score += decision.score
      currentSession.value.totalPnl += decision.pnl
      
      console.log('会话总盈亏（更新后）:', currentSession.value.totalPnl)
      
      if (decision.decisionType === DecisionType.LONG) {
        currentSession.value.longDecisions++
      } else if (decision.decisionType === DecisionType.SHORT) {
        currentSession.value.shortDecisions++
      } 
      
      // 更新余额
      const oldBalance = currentBalance.value
      currentBalance.value += decision.pnl
      console.log('余额更新: ', oldBalance, ' + ', decision.pnl, ' = ', currentBalance.value)
      
      // 更新进度
      currentSession.value.currentFrame++
      currentSession.value.progress = currentSession.value.currentFrame / currentSession.value.totalFrames
      
      // 计算胜率
      const profitableDecisions = decisions.value.filter(d => d.pnl > 0).length
      currentSession.value.winRate = profitableDecisions / decisions.value.length
    }

    appStore.addNotification({
      type: decision.pnl > 0 ? 'success' : 'warning',
      title: '决策执行',
      message: `${decision.decisionType} 决策已执行，盈亏: $${decision.pnl.toFixed(2)}`
    })
  }

  // 处理帧更新
  function handleFrameUpdate(frameData: any) {
    if (currentSession.value) {
      currentSession.value.currentFrame = frameData.index
      currentSession.value.progress = frameData.index / currentSession.value.totalFrames
    }
  }

  // 重置游戏状态
  function resetGame() {
    isInGame.value = false
    isStarting.value = false
    currentSession.value = null
    currentBalance.value = 10000
    decisions.value = []
  }

  // 获取游戏历史
  async function getGameHistory() {
    try {
      // 这里应该调用 API 获取历史数据
      return []
    } catch (error) {
      console.error('Failed to get game history:', error)
      return []
    }
  }

  return {
    // 状态
    isConnected,
    isInGame,
    isStarting,
    currentSession,
    currentBalance,
    decisions,
    
    // 计算属性
    sessionId,
    score,
    totalPnl,
    currentFrame,
    totalFrames,
    progress,
    winRate,
    
    // 方法
    initialize,
    startGame,
    endGame,
    makeDecision,
    resetGame,
    getGameHistory,
    
    // 消息处理器
    handleGameStarted,
    handleDecisionResult,
    handleFrameUpdate
  }
})