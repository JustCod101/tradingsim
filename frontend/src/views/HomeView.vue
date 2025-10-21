<template>
  <div class="home-view">
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">{{ $t('common.welcome') }}</h1>
        <p class="hero-subtitle">{{ $t('common.description') }}</p>
        <div class="hero-actions">
          <el-button 
            type="primary" 
            size="large"
            @click="startGame"
          >
            {{ $t('nav.game') }}
          </el-button>
          <el-button 
            size="large"
            @click="viewLeaderboard"
          >
            {{ $t('nav.leaderboard') }}
          </el-button>
        </div>
      </div>
    </div>

    <div class="features-section">
      <div class="container">
        <h2 class="section-title">{{ $t('common.features') }}</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <h3>{{ $t('game.realTimeTrading') }}</h3>
            <p>{{ $t('game.realTimeTradingDesc') }}</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><Trophy /></el-icon>
            </div>
            <h3>{{ $t('game.leaderboard') }}</h3>
            <p>{{ $t('game.leaderboardDesc') }}</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <h3>{{ $t('game.analytics') }}</h3>
            <p>{{ $t('game.analyticsDesc') }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { TrendCharts, Trophy, DataAnalysis } from '@element-plus/icons-vue'

const router = useRouter()

const startGame = () => {
  router.push('/game')
}

const viewLeaderboard = () => {
  router.push('/leaderboard')
}
</script>

<style scoped lang="scss">
.home-view {
  min-height: 100vh;
}

.hero-section {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
  padding: 120px 0;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 1.25rem;
  margin-bottom: 2rem;
  opacity: 0.9;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.features-section {
  padding: 80px 0;
  background: var(--bg-color-page);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 600;
  margin-bottom: 3rem;
  color: var(--text-color-primary);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: var(--bg-color);
  padding: 2rem;
  border-radius: 12px;
  text-align: center;
  box-shadow: var(--box-shadow-light);
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-5px);
    box-shadow: var(--box-shadow-base);
  }
}

.feature-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 1rem;
  background: var(--color-primary-light);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  font-size: 24px;
}

.feature-card h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: var(--text-color-primary);
}

.feature-card p {
  color: var(--text-color-regular);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>