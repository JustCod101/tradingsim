<template>
  <div class="game-view">
    <div class="game-header">
      <div class="game-info">
        <h1>{{ $t('nav.game') }}</h1>
        <div class="game-status">
          <el-tag :type="gameStore.isConnected ? 'success' : 'danger'">
            {{ gameStore.isConnected ? $t('game.connected') : $t('game.disconnected') }}
          </el-tag>
        </div>
      </div>
      <div class="game-actions">
        <el-button 
          v-if="!gameStore.isInGame" 
          type="primary" 
          @click="startNewGame"
          :loading="gameStore.isStarting"
        >
          {{ $t('game.startGame') }}
        </el-button>
        <el-button 
          v-else 
          type="danger" 
          @click="endGame"
        >
          {{ $t('game.endGame') }}
        </el-button>
      </div>
    </div>

    <div v-if="gameStore.isInGame" class="game-content">
      <div class="game-stats">
        <div class="stat-card">
          <div class="stat-label">{{ $t('game.balance') }}</div>
          <div class="stat-value">${{ formatCurrency(gameStore.currentBalance) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">{{ $t('game.profit') }}</div>
          <div class="stat-value" :class="profitClass">
            ${{ formatCurrency(gameStore.totalProfit) }}
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">{{ $t('game.trades') }}</div>
          <div class="stat-value">{{ gameStore.totalTrades }}</div>
        </div>
      </div>

      <div class="game-chart">
        <div ref="chartContainer" class="chart-container"></div>
      </div>

      <div class="trading-panel">
        <el-card>
          <template #header>
            <span>{{ $t('game.trading') }}</span>
          </template>
          <div class="trading-form">
            <el-form :model="tradeForm" label-width="80px">
              <el-form-item :label="$t('game.symbol')">
                <el-select v-model="tradeForm.symbol" style="width: 100%">
                  <el-option 
                    v-for="symbol in availableSymbols" 
                    :key="symbol" 
                    :label="symbol" 
                    :value="symbol"
                  />
                </el-select>
              </el-form-item>
              <el-form-item :label="$t('game.amount')">
                <el-input-number 
                  v-model="tradeForm.amount" 
                  :min="1" 
                  :max="gameStore.currentBalance"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item>
                <el-button 
                  type="success" 
                  @click="placeTrade('buy')"
                  :disabled="!canTrade"
                >
                  {{ $t('game.buy') }}
                </el-button>
                <el-button 
                  type="danger" 
                  @click="placeTrade('sell')"
                  :disabled="!canTrade"
                >
                  {{ $t('game.sell') }}
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </div>
    </div>

    <div v-else class="game-lobby">
      <div class="lobby-content">
        <el-icon class="lobby-icon"><Trophy /></el-icon>
        <h2>{{ $t('game.readyToStart') }}</h2>
        <p>{{ $t('game.gameDescription') }}</p>
        <el-button 
          type="primary" 
          size="large"
          @click="startNewGame"
          :loading="gameStore.isStarting"
        >
          {{ $t('game.startGame') }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useGameStore } from '@/stores/game'
import { Trophy } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const gameStore = useGameStore()
const chartContainer = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const tradeForm = ref({
  symbol: 'AAPL',
  amount: 100
})

const availableSymbols = ['AAPL', 'GOOGL', 'MSFT', 'TSLA', 'AMZN']

const profitClass = computed(() => {
  return gameStore.totalProfit >= 0 ? 'profit-positive' : 'profit-negative'
})

const canTrade = computed(() => {
  return gameStore.isInGame && 
         tradeForm.value.amount > 0 && 
         tradeForm.value.amount <= gameStore.currentBalance
})

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value)
}

const startNewGame = async () => {
  try {
    await gameStore.startGame()
    initChart()
  } catch (error) {
    console.error('Failed to start game:', error)
  }
}

const endGame = async () => {
  try {
    await gameStore.endGame()
    if (chart) {
      chart.dispose()
      chart = null
    }
  } catch (error) {
    console.error('Failed to end game:', error)
  }
}

const placeTrade = async (type: 'buy' | 'sell') => {
  try {
    await gameStore.placeTrade({
      symbol: tradeForm.value.symbol,
      type,
      amount: tradeForm.value.amount
    })
  } catch (error) {
    console.error('Failed to place trade:', error)
  }
}

const initChart = () => {
  if (!chartContainer.value) return

  chart = echarts.init(chartContainer.value)
  
  const option = {
    title: {
      text: 'Stock Price Chart',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'time'
    },
    yAxis: {
      type: 'value',
      scale: true
    },
    series: [{
      name: 'Price',
      type: 'line',
      data: [],
      smooth: true
    }]
  }
  
  chart.setOption(option)
}

onMounted(() => {
  if (gameStore.isInGame) {
    initChart()
  }
})

onUnmounted(() => {
  if (chart) {
    chart.dispose()
  }
})
</script>

<style scoped lang="scss">
.game-view {
  padding: 20px;
  min-height: 100vh;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: var(--bg-color);
  border-radius: 8px;
  box-shadow: var(--box-shadow-light);
}

.game-info {
  display: flex;
  align-items: center;
  gap: 20px;

  h1 {
    margin: 0;
    color: var(--text-color-primary);
  }
}

.game-content {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  height: calc(100vh - 200px);
}

.game-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  grid-column: 1 / -1;
}

.stat-card {
  flex: 1;
  padding: 20px;
  background: var(--bg-color);
  border-radius: 8px;
  box-shadow: var(--box-shadow-light);
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: var(--text-color-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-color-primary);

  &.profit-positive {
    color: var(--color-success);
  }

  &.profit-negative {
    color: var(--color-danger);
  }
}

.game-chart {
  background: var(--bg-color);
  border-radius: 8px;
  box-shadow: var(--box-shadow-light);
  padding: 20px;
}

.chart-container {
  width: 100%;
  height: 400px;
}

.trading-panel {
  background: var(--bg-color);
  border-radius: 8px;
  box-shadow: var(--box-shadow-light);
}

.trading-form {
  .el-form-item {
    margin-bottom: 20px;
  }
}

.game-lobby {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}

.lobby-content {
  text-align: center;
  max-width: 500px;
}

.lobby-icon {
  font-size: 64px;
  color: var(--color-primary);
  margin-bottom: 20px;
}

.lobby-content h2 {
  font-size: 2rem;
  margin-bottom: 16px;
  color: var(--text-color-primary);
}

.lobby-content p {
  font-size: 1.1rem;
  color: var(--text-color-regular);
  margin-bottom: 32px;
  line-height: 1.6;
}

@media (max-width: 1024px) {
  .game-content {
    grid-template-columns: 1fr;
  }
  
  .game-stats {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .game-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .game-stats {
    flex-direction: column;
  }
}
</style>