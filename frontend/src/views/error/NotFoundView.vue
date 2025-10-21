<template>
  <div class="not-found-view">
    <div class="error-container">
      <div class="error-illustration">
        <div class="error-code">404</div>
        <div class="error-icon">
          <el-icon :size="120"><DocumentDelete /></el-icon>
        </div>
      </div>
      
      <div class="error-content">
        <h1 class="error-title">页面未找到</h1>
        <p class="error-description">
          抱歉，您访问的页面不存在或已被移除。
          <br>
          请检查网址是否正确，或返回首页继续浏览。
        </p>
        
        <div class="error-actions">
          <el-button type="primary" size="large" @click="goHome">
            <el-icon><House /></el-icon>
            返回首页
          </el-button>
          <el-button size="large" @click="goBack">
            <el-icon><Back /></el-icon>
            返回上页
          </el-button>
          <el-button size="large" @click="refresh">
            <el-icon><Refresh /></el-icon>
            刷新页面
          </el-button>
        </div>
        
        <div class="helpful-links">
          <h3>您可能想要：</h3>
          <ul>
            <li>
              <router-link to="/game">
                <el-icon><Trophy /></el-icon>
                开始游戏
              </router-link>
            </li>
            <li>
              <router-link to="/leaderboard">
                <el-icon><TrendCharts /></el-icon>
                查看排行榜
              </router-link>
            </li>
            <li>
              <router-link to="/profile">
                <el-icon><User /></el-icon>
                个人中心
              </router-link>
            </li>
            <li>
              <router-link to="/settings">
                <el-icon><Setting /></el-icon>
                系统设置
              </router-link>
            </li>
          </ul>
        </div>
      </div>
    </div>
    
    <div class="floating-elements">
      <div class="floating-element" v-for="i in 6" :key="i" :style="getFloatingStyle(i)">
        <el-icon><Star /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  DocumentDelete,
  House,
  Back,
  Refresh,
  Trophy,
  TrendCharts,
  User,
  Setting,
  Star
} from '@element-plus/icons-vue'

const router = useRouter()

function goHome() {
  router.push('/')
}

function goBack() {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/')
  }
}

function refresh() {
  window.location.reload()
}

function getFloatingStyle(index: number) {
  const positions = [
    { top: '10%', left: '10%', animationDelay: '0s' },
    { top: '20%', right: '15%', animationDelay: '1s' },
    { top: '60%', left: '5%', animationDelay: '2s' },
    { bottom: '20%', right: '10%', animationDelay: '3s' },
    { bottom: '10%', left: '20%', animationDelay: '4s' },
    { top: '40%', right: '5%', animationDelay: '5s' }
  ]
  
  return positions[index - 1] || {}
}
</script>

<style scoped>
.not-found-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.error-container {
  max-width: 800px;
  width: 100%;
  background: white;
  border-radius: 20px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 2;
}

.error-illustration {
  position: relative;
  margin-bottom: 40px;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: var(--el-color-primary);
  line-height: 1;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.error-icon {
  color: var(--el-text-color-secondary);
  opacity: 0.6;
}

.error-content {
  max-width: 600px;
  margin: 0 auto;
}

.error-title {
  font-size: 32px;
  color: var(--el-text-color-primary);
  margin-bottom: 20px;
  font-weight: 600;
}

.error-description {
  font-size: 16px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
  margin-bottom: 40px;
}

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-bottom: 50px;
  flex-wrap: wrap;
}

.error-actions .el-button {
  border-radius: 25px;
  padding: 12px 24px;
  font-weight: 500;
}

.helpful-links {
  text-align: left;
  max-width: 400px;
  margin: 0 auto;
}

.helpful-links h3 {
  color: var(--el-text-color-primary);
  margin-bottom: 20px;
  font-size: 18px;
  text-align: center;
}

.helpful-links ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.helpful-links li {
  margin-bottom: 12px;
}

.helpful-links a {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  color: var(--el-text-color-regular);
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.3s;
  background: var(--el-fill-color-lighter);
}

.helpful-links a:hover {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  transform: translateX(5px);
}

.floating-elements {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
}

.floating-element {
  position: absolute;
  color: rgba(255, 255, 255, 0.3);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

@media (max-width: 768px) {
  .error-container {
    padding: 40px 20px;
  }
  
  .error-code {
    font-size: 80px;
  }
  
  .error-title {
    font-size: 24px;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .error-actions .el-button {
    width: 200px;
  }
}
</style>