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
            ${{ formatCurrency(gameStore.totalPnl) }}
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">{{ $t('game.score') }}</div>
          <div class="stat-value">{{ gameStore.score }}</div>
        </div>
      </div>

      <div class="game-chart">
        <KLineChart />
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
import KLineChart from '@/components/KLineChart.vue'

const gameStore = useGameStore()

const profitClass = computed(() => {
  return gameStore.totalPnl >= 0 ? 'profit-positive' : 'profit-negative'
})

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value)
}

const startNewGame = async () => {
  try {
    // 生成一个默认的segmentId，或者从配置中获取
    const stockCode = 'AAPL' // 可以根据需要修改为动态值
    const difficulty = 'EASY' // 可以根据需要修改为动态值
    await gameStore.startGame(stockCode, difficulty)
  } catch (error) {
    console.error('Failed to start game:', error)
  }
}

const endGame = async () => {
  try {
    await gameStore.endGame()
  } catch (error) {
    console.error('Failed to end game:', error)
  }
}
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