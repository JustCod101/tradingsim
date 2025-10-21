import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useWebSocketStore } from './websocket'
import { useAppStore } from './app'
import { GameSessionStatus, DecisionType } from '@/types'
import type { GameSession, GameDecision } from '@/types'

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
  const totalScore = computed(() => currentSession.value?.totalScore || 0)
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
  async function startGame(segmentId: string) {
    try {
      isStarting.value = true

      // 发送开始游戏消息
      await wsStore.sendMessage('/app/game/start', {
        type: 'GAME_START',
        data: {
          segmentId,
          initialBalance: 10000
        },
        timestamp: new Date().toISOString()
      })

      // 模拟游戏开始（实际应该通过 WebSocket 响应处理）
      setTimeout(() => {
        handleGameStarted({
          id: `session-${Date.now()}`,
          userId: 'user-1',
          segmentId,
          status: GameSessionStatus.STARTED,
          currentFrame: 0,
          totalFrames: 100,
          totalScore: 0,
          totalPnl: 0,
          buyDecisions: 0,
          sellDecisions: 0,
          skipDecisions: 0,
          timeoutCount: 0,
          averageResponseTime: 0,
          seed: Math.floor(Math.random() * 1000000),
          progress: 0,
          winRate: 0,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        })
      }, 1000)

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
      await wsStore.sendMessage('/app/game/end', {
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
          message: `游戏已结束，总分: ${totalScore.value}，盈亏: $${totalPnl.value.toFixed(2)}`
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

      // 发送决策消息
      await wsStore.sendMessage('/app/game/decision', {
        type: 'DECISION_RESULT',
        data: {
          sessionId: sessionId.value,
          frameIndex: currentFrame.value,
          decisionType: type,
          price,
          quantity
        },
        timestamp: new Date().toISOString()
      })

      // 模拟决策执行
      setTimeout(() => {
        const decision: GameDecision = {
          id: `decision-${Date.now()}`,
          sessionId: sessionId.value!,
          frameIndex: currentFrame.value,
          decisionType: type,
          price: price || Math.random() * 200 + 50,
          quantity: quantity || 100,
          pnl: (Math.random() - 0.5) * 1000, // 模拟盈亏
          score: Math.floor(Math.random() * 100),
          responseTime: Math.random() * 5000,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }
        handleDecisionResult(decision)
      }, 500)

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
    decisions.value.push(decision)
    
    if (currentSession.value) {
      // 更新会话统计
      currentSession.value.totalScore += decision.score
      currentSession.value.totalPnl += decision.pnl
      
      if (decision.decisionType === DecisionType.BUY) {
        currentSession.value.buyDecisions++
      } else if (decision.decisionType === DecisionType.SELL) {
        currentSession.value.sellDecisions++
      } else {
        currentSession.value.skipDecisions++
      }
      
      // 更新余额
      currentBalance.value += decision.pnl
      
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
    totalScore,
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