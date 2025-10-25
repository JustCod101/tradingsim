<template>
  <div class="kline-chart-container">
    <!-- 图表容器 -->
    <div ref="chartContainer" class="chart-container"></div>
    
    <!-- 决策按钮 -->
    <div v-if="showDecisionButtons" class="decision-buttons">
      <button 
        @click="makeDecision('LONG')" 
        :disabled="isDecisionLoading"
        class="decision-btn long-btn"
      >
        做多
      </button>
      <button 
        @click="makeDecision('SHORT')" 
        :disabled="isDecisionLoading"
        class="decision-btn short-btn"
      >
        做空
      </button>
    </div>
    
    <!-- 游戏控制 -->
    <div class="game-controls">
      <button 
        v-if="!gameStarted && !gameEnded" 
        @click="startGame"
        class="start-btn"
      >
        开始游戏
      </button>
      
      <!-- 进度条 -->
      <div v-if="gameStarted && !gameEnded" class="progress-container">
        <div class="progress-bar">
          <div 
            class="progress-fill" 
            :style="{ width: progressPercentage + '%' }"
          ></div>
        </div>
        <div class="progress-text">
          {{ currentIndex }} / {{ totalDataLength }} K线
        </div>
      </div>
    </div>
    
    <!-- 决策结果显示 -->
    <div v-if="lastDecisionResult" class="decision-result">
      <div :class="['result-text', lastDecisionResult.result.toLowerCase()]">
        {{ lastDecisionResult.result === 'WIN' ? '盈利!' : '亏损!' }}
      </div>
      <div class="profit-text">
        {{ lastDecisionResult.result === 'WIN' ? '+' : '' }}{{ lastDecisionResult.profit }}
      </div>
    </div>
    
    <!-- 游戏总结 -->
    <div v-if="gameEnded" class="game-summary">
      <h3>游戏结束</h3>
      <div class="summary-stats">
        <div>总盈亏: {{ totalProfit }}</div>
        <div>胜率: {{ winRate }}%</div>
      </div>
      <button @click="resetGame" class="reset-btn">重新开始</button>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="isDecisionLoading" class="loading-overlay">
      <div class="loading-spinner">处理中...</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { createChart, CandlestickSeries, LineSeries } from 'lightweight-charts'
import { request } from '@/services/api'
import { useGameStore } from '@/stores/game'
import { DecisionType } from '@/types'

// 使用游戏store
const gameStore = useGameStore()

// 定义接口
interface KLineData {
  time: string | number
  open: number
  high: number
  low: number
  close: number
}

interface GameData {
  sessionId: string
  fullKLineData: KLineData[]
  keyNodeIndices: number[]
  decisionStartTime?: number
  isGameEnded?: boolean
}

interface DecisionResult {
  result: 'WIN' | 'LOSE'
  profit: number
  nextStopIndex: number
}

// 响应式数据
const chartContainer = ref<HTMLElement>()
const isAnimating = ref(false)
const isPaused = ref(false)
const showDecisionButtons = ref(false)
const isDecisionLoading = ref(false)
const gameStarted = ref(false)
const gameEnded = ref(false)
const lastDecisionResult = ref<DecisionResult | null>(null)

// 图表相关变量
let chart: any = null
let candlestickSeries: any = null
let markerSeries: any = null
let animationInterval: NodeJS.Timeout | null = null

// 游戏数据
const gameData = ref<GameData | null>(null)
const currentIndex = ref(0)
const currentNodeIndex = ref(0)

// 计算属性
const totalProfit = computed(() => {
  return gameStore.totalPnl
})

const winRate = computed(() => {
  return Math.round(gameStore.winRate * 100)
})

const totalDataLength = computed(() => {
  return gameData.value?.fullKLineData.length || 0
})

const progressPercentage = computed(() => {
  if (!gameData.value || totalDataLength.value === 0) return 0
  return Math.round((currentIndex.value / totalDataLength.value) * 100)
})

// 初始化图表
const initChart = () => {
  if (!chartContainer.value) return

  chart = createChart(chartContainer.value, {
    width: chartContainer.value.clientWidth,
    height: 400,
    layout: {
      background: { color: '#ffffff' },
      textColor: '#333',
    },
    grid: {
      vertLines: { color: '#e1e1e1' },
      horzLines: { color: '#e1e1e1' },
    },
    timeScale: {
      timeVisible: true,
      secondsVisible: false,
    },
  })

  // 添加K线图表
  candlestickSeries = chart.addSeries(CandlestickSeries, {
    upColor: '#26a69a',
    downColor: '#ef5350',
    borderVisible: false,
    wickUpColor: '#26a69a',
    wickDownColor: '#ef5350',
  })

  // 添加标记系列用于显示关键节点
  markerSeries = chart.addSeries(LineSeries, {
    color: 'transparent',
    lineWidth: 0,
    crosshairMarkerVisible: false,
    lastValueVisible: false,
    priceLineVisible: false,
  })

  // 响应式调整
  const resizeObserver = new ResizeObserver(() => {
    if (chart && chartContainer.value) {
      chart.applyOptions({
        width: chartContainer.value.clientWidth,
      })
    }
  })

  if (chartContainer.value) {
    resizeObserver.observe(chartContainer.value)
  }
}

// 获取游戏数据
const fetchGameData = async () => {
  try {
    const response = await request.get('/segments') // 获取可用的游戏段
    gameData.value = response.data
    
    // 不在这里设置图表数据，让startGame函数来处理逐根添加
  } catch (error) {
    console.error('获取游戏数据失败:', error)
  }
}

// 初始化游戏会话
const initializeGameSession = async () => {
  try {
    // 创建游戏会话
    const sessionResponse = await request.post('/game/sessions?stockCode=AAPL&difficulty=EASY')
    
    // 生成模拟K线数据
    const mockKLineData = generateMockKLineData()
    
    gameData.value = {
      sessionId: sessionResponse.data.sessionId,
      fullKLineData: mockKLineData,
      keyNodeIndices: [10, 25, 40, 55, 70], // 预设关键节点
      decisionStartTime: Date.now(),
      isGameEnded: false
    }
  } catch (error) {
    console.error('初始化游戏会话失败:', error)
    // 使用模拟数据作为后备
    const mockKLineData = generateMockKLineData()
    gameData.value = {
      sessionId: 'mock-session-' + Date.now(),
      fullKLineData: mockKLineData,
      keyNodeIndices: [10, 25, 40, 55, 70],
      decisionStartTime: Date.now(),
      isGameEnded: false
    }
  }
}

// 开始游戏
const startGame = async () => {
  if (!gameData.value) {
    await initializeGameSession()
  }
  
  if (!gameData.value) return
  
  // 使用游戏store启动游戏
  try {
    await gameStore.startGame('defaultAAPL', 'EASY')
  } catch (error) {
    console.error('启动游戏失败:', error)
  }
  
  gameStarted.value = true
  currentIndex.value = -1  // 从-1开始，这样第一次increment会变成0
  
  // 初始化累积数据数组
  displayedKLineData.value = []
  
  // 清空图表，准备逐根添加
  if (candlestickSeries) {
    candlestickSeries.setData([])
  }
  
  // 开始动画到第一个关键节点
  const firstKeyIndex = gameData.value.keyNodeIndices[0] || 10
  startAnimation(firstKeyIndex)
}

// 生成模拟K线数据
  const generateMockKLineData = (): KLineData[] => {
    const data: KLineData[] = []
    let basePrice = 150 // 起始价格
    const baseTimestamp = Math.floor(new Date('2024-01-01').getTime() / 1000) // Unix时间戳（秒）
    
    for (let i = 0; i < 100; i++) {
      // 模拟价格波动
      const volatility = 0.02 // 2%波动
      const change = (Math.random() - 0.5) * volatility * basePrice
      
      const open = basePrice
      const close = basePrice + change
      const high = Math.max(open, close) + Math.random() * 0.01 * basePrice
      const low = Math.min(open, close) - Math.random() * 0.01 * basePrice
      
      // 使用Unix时间戳，每个K线间隔1小时（3600秒）
      const timestamp = baseTimestamp + i * 3600
      
      data.push({
        time: timestamp,
        open: Number(open.toFixed(2)),
        high: Number(high.toFixed(2)),
        low: Number(low.toFixed(2)),
        close: Number(close.toFixed(2))
      })
      
      basePrice = close
    }
    
    return data
  }

  // 关键节点检测
  const isKeyPoint = (index: number): boolean => {
    if (index < 5) return false // 至少需要5个数据点
    
    const recentData = gameData.value!.fullKLineData.slice(index - 5, index)
    if (recentData.length < 5) return false
    
    // 检测趋势变化
    const prices = recentData.map(d => d.close)
    const isUptrend = prices.every((price, i) => i === 0 || price >= prices[i - 1])
    const isDowntrend = prices.every((price, i) => i === 0 || price <= prices[i - 1])
    
    // 检测价格波动
    const maxPrice = Math.max(...prices)
    const minPrice = Math.min(...prices)
    const volatility = (maxPrice - minPrice) / minPrice
    
    // 关键节点条件：明显的趋势变化或高波动性
    return (isUptrend || isDowntrend) && volatility > 0.02 // 2%以上的波动
  }

// 累积显示的K线数据
const displayedKLineData = ref<KLineData[]>([])

// 开始动画
const startAnimation = (targetIndex: number) => {
  if (!gameData.value || !candlestickSeries) return
  
  isAnimating.value = true
  showDecisionButtons.value = false
  
  animationInterval = setInterval(() => {
    currentIndex.value++
    
    if (currentIndex.value <= targetIndex && currentIndex.value < gameData.value!.fullKLineData.length) {
      // 添加新的K线数据到累积数组
      const newData = gameData.value!.fullKLineData[currentIndex.value]
      displayedKLineData.value.push(newData)
      
      // 使用setData重新设置所有数据，确保历史数据不丢失
      candlestickSeries.setData([...displayedKLineData.value])
      
      // 检查是否到达关键节点
      if (isKeyPoint(currentIndex.value)) {
        // 添加关键节点标记
        if (markerSeries) {
          const currentData = gameData.value!.fullKLineData[currentIndex.value]
          markerSeries.setMarkers([{
            time: currentData.time,
            position: 'aboveBar',
            color: '#f68410',
            shape: 'circle',
            text: '决策点',
            size: 1,
          }])
        }
        
        stopAnimation()
        showDecisionButtons.value = true
        isPaused.value = true
      }
      
      // 检查是否到达目标索引
      if (currentIndex.value === targetIndex) {
        stopAnimation()
        
        // 检查是否还有更多节点
        currentNodeIndex.value++
        if (currentNodeIndex.value < gameData.value!.keyNodeIndices.length) {
          showDecisionButtons.value = true
          isPaused.value = true
        } else {
          // 游戏结束
          gameEnded.value = true
        }
      }
    } else {
      stopAnimation()
      gameEnded.value = true
    }
  }, 200) // 每200ms添加一根K线，更平滑的动画
}

// 停止动画
const stopAnimation = () => {
  if (animationInterval) {
    clearInterval(animationInterval)
    animationInterval = null
  }
  isAnimating.value = false
}

// 做出决策
const makeDecision = async (decision: 'LONG' | 'SHORT') => {
  if (!gameData.value || isDecisionLoading.value) return
  
  isDecisionLoading.value = true
  showDecisionButtons.value = false
  
  const currentPrice = gameData.value.fullKLineData[currentIndex.value]?.close || 0
  
  // 将前端的LONG/SHORT映射为后端的DecisionType
  const decisionTypeMapping = {
    'LONG': DecisionType.LONG,
    'SHORT': DecisionType.SHORT
  }
  
  try {
    // 使用游戏store的决策方法
    await gameStore.makeDecision(
      decisionTypeMapping[decision],
      currentPrice,
      1 // quantity
    )
    
    // 从游戏store获取最新的决策结果
    const latestDecision = gameStore.decisions[gameStore.decisions.length - 1]
    if (latestDecision) {
      const result: DecisionResult = {
        result: latestDecision.pnl > 0 ? 'WIN' : 'LOSE',
        profit: latestDecision.pnl,
        nextStopIndex: currentIndex.value + 10
      }
      
      lastDecisionResult.value = result
      
      // 显示结果一段时间后继续动画
      setTimeout(() => {
        lastDecisionResult.value = null
        startAnimation(result.nextStopIndex)
      }, 2000)
    }
    
  } catch (error) {
    console.error('决策请求失败:', error)
    // 即使API调用失败，也继续游戏
    const fallbackResult: DecisionResult = {
      result: 'LOSE',
      profit: 0,
      nextStopIndex: currentIndex.value + 10
    }
    
    setTimeout(() => {
      startAnimation(fallbackResult.nextStopIndex)
    }, 1000)
  } finally {
    isDecisionLoading.value = false
  }
}

// 重置游戏
const resetGame = () => {
  gameStarted.value = false
  gameEnded.value = false
  currentIndex.value = 0
  currentNodeIndex.value = 0
  lastDecisionResult.value = null
  showDecisionButtons.value = false
  isPaused.value = false
  gameData.value = null
  
  // 重置游戏store状态
  gameStore.resetGame()
  
  // 重置累积数据数组
  displayedKLineData.value = []
  
  if (candlestickSeries) {
    candlestickSeries.setData([])
  }
}

// 生命周期
onMounted(() => {
  initChart()
})

onUnmounted(() => {
  stopAnimation()
  if (chart) {
    chart.remove()
  }
})
</script>

<style scoped>
.kline-chart-container {
  position: relative;
  width: 100%;
  height: 500px;
  border: 1px solid #e1e1e1;
  border-radius: 8px;
  overflow: hidden;
}

.chart-container {
  width: 100%;
  height: 400px;
}

.decision-buttons {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  gap: 20px;
  z-index: 10;
}

.decision-btn {
  padding: 12px 24px;
  font-size: 16px;
  font-weight: bold;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.long-btn {
  background-color: #26a69a;
  color: white;
}

.long-btn:hover:not(:disabled) {
  background-color: #00695c;
}

.short-btn {
  background-color: #ef5350;
  color: white;
}

.short-btn:hover:not(:disabled) {
  background-color: #c62828;
}

.decision-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.game-controls {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
}

.start-btn, .reset-btn {
  padding: 10px 20px;
  font-size: 14px;
  background-color: #1976d2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.start-btn:hover, .reset-btn:hover {
  background-color: #1565c0;
}

.progress-container {
  margin-top: 15px;
  text-align: center;
}

.progress-bar {
  width: 300px;
  height: 8px;
  background-color: #e1e1e1;
  border-radius: 4px;
  overflow: hidden;
  margin: 0 auto 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #26a69a);
  transition: width 0.3s ease;
  border-radius: 4px;
}

.progress-text {
  font-size: 14px;
  color: #666;
}

.decision-result {
  position: absolute;
  top: 20px;
  right: 20px;
  padding: 10px 15px;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.result-text {
  font-weight: bold;
  font-size: 16px;
}

.result-text.win {
  color: #26a69a;
}

.result-text.lose {
  color: #ef5350;
}

.profit-text {
  font-size: 14px;
  margin-top: 4px;
}

.game-summary {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  text-align: center;
  z-index: 10;
}

.summary-stats {
  margin: 15px 0;
}

.summary-stats div {
  margin: 5px 0;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 15;
}

.loading-spinner {
  padding: 20px;
  background-color: white;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>