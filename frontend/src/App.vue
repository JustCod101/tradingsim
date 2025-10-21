<template>
  <div id="app" class="app-container">
    <!-- 全局加载指示器 -->
    <GlobalLoading v-if="appStore.isLoading" />
    
    <!-- 全局消息提示 -->
    <GlobalMessage />
    
    <!-- 路由视图 -->
    <router-view v-slot="{ Component, route }">
      <transition
        :name="(route.meta?.transition as string) || 'fade'"
        mode="out-in"
        appear
      >
        <keep-alive :include="keepAliveComponents">
          <component :is="Component" :key="route.fullPath" />
        </keep-alive>
      </transition>
    </router-view>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { useAppStore } from '@/stores/app'
import { useWebSocketStore } from '@/stores/websocket'
import { useThemeStore } from '@/stores/theme'
import GlobalLoading from '@/components/GlobalLoading.vue'
import GlobalMessage from '@/components/GlobalMessage.vue'

// 应用状态
const appStore = useAppStore()
const wsStore = useWebSocketStore()
const themeStore = useThemeStore()

// 开发环境标识
const isDev = computed(() => import.meta.env.DEV)

// 需要缓存的组件
const keepAliveComponents = computed(() => [
  'GameView',
  'LeaderboardView',
  'ProfileView'
])

// 应用初始化
onMounted(async () => {
  try {
    // 初始化应用
    await appStore.initialize()
    
    // 初始化主题
    await themeStore.initialize()
    
    // 初始化 WebSocket 连接
    await wsStore.initialize()
    
    // 注册全局事件监听器
    registerGlobalListeners()
    
    console.log('✅ App initialized successfully')
  } catch (error) {
    console.error('❌ App initialization failed:', error)
    appStore.setError('应用初始化失败，请刷新页面重试')
  }
})

// 应用清理
onUnmounted(() => {
  // 清理 WebSocket 连接
  wsStore.disconnect()
  
  // 移除全局事件监听器
  unregisterGlobalListeners()
})

// 注册全局事件监听器
function registerGlobalListeners() {
  // 监听网络状态变化
  window.addEventListener('online', handleOnline)
  window.addEventListener('offline', handleOffline)
  
  // 监听页面可见性变化
  document.addEventListener('visibilitychange', handleVisibilityChange)
  
  // 监听页面卸载
  window.addEventListener('beforeunload', handleBeforeUnload)
  
  // 监听键盘快捷键
  document.addEventListener('keydown', handleKeydown)
}

// 移除全局事件监听器
function unregisterGlobalListeners() {
  window.removeEventListener('online', handleOnline)
  window.removeEventListener('offline', handleOffline)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('beforeunload', handleBeforeUnload)
  document.removeEventListener('keydown', handleKeydown)
}

// 网络连接恢复
function handleOnline() {
  appStore.setOnline(true)
  wsStore.reconnect()
}

// 网络连接断开
function handleOffline() {
  appStore.setOnline(false)
}

// 页面可见性变化
function handleVisibilityChange() {
  if (document.hidden) {
    // 页面隐藏时暂停 WebSocket 心跳
    wsStore.pauseHeartbeat()
  } else {
    // 页面显示时恢复 WebSocket 心跳
    wsStore.resumeHeartbeat()
  }
}

// 页面卸载前
function handleBeforeUnload(event: BeforeUnloadEvent) {
  // 如果有未保存的游戏状态，提示用户
  if (appStore.hasUnsavedChanges) {
    event.preventDefault()
    event.returnValue = '您有未保存的游戏进度，确定要离开吗？'
    return event.returnValue
  }
}

// 键盘快捷键
function handleKeydown(event: KeyboardEvent) {
  // Ctrl/Cmd + K: 打开快捷命令面板
  if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
    event.preventDefault()
    appStore.toggleCommandPalette()
  }
  
  // F11: 切换全屏
  if (event.key === 'F11') {
    event.preventDefault()
    appStore.toggleFullscreen()
  }
  
  // Esc: 关闭模态框
  if (event.key === 'Escape') {
    appStore.closeModal()
  }
}
</script>

<style lang="scss">
// 全局样式重置
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  font-size: 16px;
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 
               'Helvetica Neue', Arial, 'Noto Sans', sans-serif,
               'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol',
               'Noto Color Emoji';
  background-color: var(--el-bg-color);
  color: var(--el-text-color-primary);
  overflow-x: hidden;
}

// 应用容器
.app-container {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

// 路由过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.3s ease;
}

.slide-left-enter-from {
  transform: translateX(100%);
}

.slide-left-leave-to {
  transform: translateX(-100%);
}

.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.3s ease;
}

.slide-right-enter-from {
  transform: translateX(-100%);
}

.slide-right-leave-to {
  transform: translateX(100%);
}

// 滚动条样式
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: var(--el-fill-color-dark);
  border-radius: 4px;
  
  &:hover {
    background: var(--el-fill-color-darker);
  }
}

// 选择文本样式
::selection {
  background-color: var(--el-color-primary);
  color: white;
}

// 焦点样式
:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}

// 禁用状态
[disabled] {
  cursor: not-allowed !important;
  opacity: 0.6;
}

// 响应式断点
@media (max-width: 768px) {
  html {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  html {
    font-size: 12px;
  }
}
</style>