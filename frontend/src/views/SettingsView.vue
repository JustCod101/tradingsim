<template>
  <div class="settings-view">
    <div class="settings-header">
      <h1>设置</h1>
      <div class="header-actions">
        <el-button @click="resetToDefaults" type="warning">
          <el-icon><RefreshLeft /></el-icon>
          恢复默认
        </el-button>
        <el-button @click="saveSettings" type="primary" :loading="saving">
          <el-icon><Check /></el-icon>
          保存设置
        </el-button>
      </div>
    </div>

    <div class="settings-content">
      <el-tabs v-model="activeTab" tab-position="left">
        <!-- 主题设置 -->
        <el-tab-pane label="主题设置" name="theme">
          <div class="settings-section">
            <h2>主题设置</h2>
            <el-form :model="settings.theme" label-width="120px">
              <el-form-item label="主题模式">
                <el-radio-group v-model="settings.theme.mode">
                  <el-radio label="light">浅色模式</el-radio>
                  <el-radio label="dark">深色模式</el-radio>
                  <el-radio label="auto">跟随系统</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="主色调">
                <el-color-picker v-model="settings.theme.primaryColor" />
                <div class="color-presets">
                  <div
                    v-for="color in colorPresets"
                    :key="color"
                    class="color-preset"
                    :style="{ backgroundColor: color }"
                    @click="settings.theme.primaryColor = color"
                  />
                </div>
              </el-form-item>
              
              <el-form-item label="圆角大小">
                <el-slider
                  v-model="settings.theme.borderRadius"
                  :min="0"
                  :max="20"
                  :step="2"
                  show-input
                />
              </el-form-item>
              
              <el-form-item label="字体大小">
                <el-slider
                  v-model="settings.theme.fontSize"
                  :min="12"
                  :max="18"
                  :step="1"
                  show-input
                />
              </el-form-item>
              
              <el-form-item label="紧凑模式">
                <el-switch v-model="settings.theme.compactMode" />
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 游戏设置 -->
        <el-tab-pane label="游戏设置" name="game">
          <div class="settings-section">
            <h2>游戏设置</h2>
            <el-form :model="gameSettings" label-width="120px">
              <el-form-item label="自动保存">
                <el-switch v-model="gameSettings.autoSave" />
                <span class="form-help">自动保存游戏进度</span>
              </el-form-item>
              
              <el-form-item label="声音效果">
                <el-switch v-model="gameSettings.soundEnabled" />
                <span class="form-help">启用游戏音效</span>
              </el-form-item>
              
              <el-form-item label="动画效果">
                <el-switch v-model="gameSettings.animationEnabled" />
                <span class="form-help">启用界面动画</span>
              </el-form-item>
              
              <el-form-item label="决策时间限制">
                <el-input-number
                  v-model="gameSettings.decisionTimeLimit"
                  :min="5"
                  :max="60"
                  :step="5"
                />
                <span class="form-help">秒</span>
              </el-form-item>
              
              <el-form-item label="初始资金">
                <el-input-number
                  v-model="gameSettings.initialBalance"
                  :min="1000"
                  :max="100000"
                  :step="1000"
                />
                <span class="form-help">美元</span>
              </el-form-item>
              
              <el-form-item label="难度级别">
                <el-select v-model="gameSettings.difficulty">
                  <el-option label="简单" value="EASY" />
                  <el-option label="中等" value="MEDIUM" />
                  <el-option label="困难" value="HARD" />
                  <el-option label="专家" value="EXPERT" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 通知设置 -->
        <el-tab-pane label="通知设置" name="notifications">
          <div class="settings-section">
            <h2>通知设置</h2>
            <el-form :model="notificationSettings" label-width="120px">
              <el-form-item label="桌面通知">
                <el-switch v-model="notificationSettings.desktop" />
                <span class="form-help">允许桌面通知</span>
              </el-form-item>
              
              <el-form-item label="游戏开始">
                <el-switch v-model="notificationSettings.gameStart" />
              </el-form-item>
              
              <el-form-item label="游戏结束">
                <el-switch v-model="notificationSettings.gameEnd" />
              </el-form-item>
              
              <el-form-item label="交易执行">
                <el-switch v-model="notificationSettings.tradeExecution" />
              </el-form-item>
              
              <el-form-item label="排行榜更新">
                <el-switch v-model="notificationSettings.leaderboardUpdate" />
              </el-form-item>
              
              <el-form-item label="通知持续时间">
                <el-slider
                  v-model="notificationSettings.duration"
                  :min="1000"
                  :max="10000"
                  :step="1000"
                  :format-tooltip="formatDuration"
                />
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 数据设置 -->
        <el-tab-pane label="数据设置" name="data">
          <div class="settings-section">
            <h2>数据设置</h2>
            <el-form label-width="120px">
              <el-form-item label="数据缓存">
                <el-button @click="clearCache" type="warning">
                  <el-icon><Delete /></el-icon>
                  清除缓存
                </el-button>
                <span class="form-help">清除本地缓存数据</span>
              </el-form-item>
              
              <el-form-item label="导出数据">
                <el-button @click="exportData">
                  <el-icon><Download /></el-icon>
                  导出游戏数据
                </el-button>
                <span class="form-help">导出个人游戏数据</span>
              </el-form-item>
              
              <el-form-item label="导入数据">
                <el-upload
                  ref="uploadRef"
                  :auto-upload="false"
                  :show-file-list="false"
                  accept=".json"
                  @change="handleFileChange"
                >
                  <el-button>
                    <el-icon><Upload /></el-icon>
                    选择文件
                  </el-button>
                </el-upload>
                <span class="form-help">导入游戏数据备份</span>
              </el-form-item>
              
              <el-form-item label="重置数据">
                <el-button @click="resetData" type="danger">
                  <el-icon><Warning /></el-icon>
                  重置所有数据
                </el-button>
                <span class="form-help">⚠️ 此操作不可恢复</span>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 高级设置 -->
        <el-tab-pane label="高级设置" name="advanced">
          <div class="settings-section">
            <h2>高级设置</h2>
            <el-form :model="advancedSettings" label-width="120px">
              <el-form-item label="调试模式">
                <el-switch v-model="advancedSettings.debugMode" />
                <span class="form-help">启用开发者调试功能</span>
              </el-form-item>
              
              <el-form-item label="性能监控">
                <el-switch v-model="advancedSettings.performanceMonitoring" />
                <span class="form-help">启用性能监控</span>
              </el-form-item>
              
              <el-form-item label="错误报告">
                <el-switch v-model="advancedSettings.errorReporting" />
                <span class="form-help">自动发送错误报告</span>
              </el-form-item>
              
              <el-form-item label="API 端点">
                <el-input v-model="advancedSettings.apiEndpoint" placeholder="https://api.example.com" />
              </el-form-item>
              
              <el-form-item label="WebSocket 端点">
                <el-input v-model="advancedSettings.wsEndpoint" placeholder="wss://ws.example.com" />
              </el-form-item>
              
              <el-form-item label="请求超时">
                <el-input-number
                  v-model="advancedSettings.requestTimeout"
                  :min="1000"
                  :max="30000"
                  :step="1000"
                />
                <span class="form-help">毫秒</span>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  RefreshLeft, 
  Check, 
  Delete, 
  Download, 
  Upload, 
  Warning 
} from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import type { ThemeConfig, AppConfig } from '@/types'

// 响应式数据
const activeTab = ref('theme')
const saving = ref(false)
const uploadRef = ref()

// 获取应用store
const appStore = useAppStore()

// 设置数据
const settings = reactive<{ theme: ThemeConfig }>({
  theme: {
    mode: 'light',
    primaryColor: '#409EFF',
    borderRadius: 4,
    fontSize: 14,
    compactMode: false
  }
})

const gameSettings = reactive({
  autoSave: true,
  soundEnabled: true,
  animationEnabled: true,
  decisionTimeLimit: 30,
  initialBalance: 10000,
  difficulty: 'MEDIUM'
})

const notificationSettings = reactive({
  desktop: true,
  gameStart: true,
  gameEnd: true,
  tradeExecution: true,
  leaderboardUpdate: false,
  duration: 3000
})

const advancedSettings = reactive({
  debugMode: false,
  performanceMonitoring: false,
  errorReporting: true,
  apiEndpoint: '',
  wsEndpoint: '',
  requestTimeout: 10000
})

// 颜色预设
const colorPresets = [
  '#409EFF',
  '#67C23A',
  '#E6A23C',
  '#F56C6C',
  '#909399',
  '#9C27B0',
  '#FF5722',
  '#795548'
]

// 生命周期
onMounted(() => {
  loadSettings()
})

// 方法
function loadSettings() {
  try {
    // 从 localStorage 加载设置
    const savedSettings = localStorage.getItem('app-settings')
    if (savedSettings) {
      const parsed = JSON.parse(savedSettings)
      Object.assign(settings, parsed.settings || {})
      Object.assign(gameSettings, parsed.gameSettings || {})
      Object.assign(notificationSettings, parsed.notificationSettings || {})
      Object.assign(advancedSettings, parsed.advancedSettings || {})
    }
  } catch (error) {
    console.error('Failed to load settings:', error)
  }
}

async function saveSettings() {
  saving.value = true
  try {
    // 保存到 localStorage
    const allSettings = {
      settings,
      gameSettings,
      notificationSettings,
      advancedSettings
    }
    localStorage.setItem('app-settings', JSON.stringify(allSettings))
    
    // 应用主题设置
    applyThemeSettings()
    
    ElMessage.success('设置已保存')
  } catch (error) {
    console.error('Failed to save settings:', error)
    ElMessage.error('保存设置失败')
  } finally {
    saving.value = false
  }
}

function applyThemeSettings() {
  // 应用主题设置到应用
  const root = document.documentElement
  root.style.setProperty('--el-color-primary', settings.theme.primaryColor)
  root.style.setProperty('--el-border-radius-base', `${settings.theme.borderRadius}px`)
  root.style.setProperty('--el-font-size-base', `${settings.theme.fontSize}px`)
  
  // 设置主题模式
  if (settings.theme.mode === 'dark') {
    document.documentElement.classList.add('dark')
  } else if (settings.theme.mode === 'light') {
    document.documentElement.classList.remove('dark')
  } else {
    // 跟随系统
    const isDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    document.documentElement.classList.toggle('dark', isDark)
  }
}

async function resetToDefaults() {
  try {
    await ElMessageBox.confirm(
      '确定要恢复所有设置到默认值吗？此操作不可撤销。',
      '确认重置',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 重置所有设置
    Object.assign(settings.theme, {
      mode: 'light',
      primaryColor: '#409EFF',
      borderRadius: 4,
      fontSize: 14,
      compactMode: false
    })
    
    Object.assign(gameSettings, {
      autoSave: true,
      soundEnabled: true,
      animationEnabled: true,
      decisionTimeLimit: 30,
      initialBalance: 10000,
      difficulty: 'MEDIUM'
    })
    
    Object.assign(notificationSettings, {
      desktop: true,
      gameStart: true,
      gameEnd: true,
      tradeExecution: true,
      leaderboardUpdate: false,
      duration: 3000
    })
    
    Object.assign(advancedSettings, {
      debugMode: false,
      performanceMonitoring: false,
      errorReporting: true,
      apiEndpoint: '',
      wsEndpoint: '',
      requestTimeout: 10000
    })
    
    await saveSettings()
    ElMessage.success('设置已重置为默认值')
  } catch (error) {
    // 用户取消操作
  }
}

async function clearCache() {
  try {
    await ElMessageBox.confirm(
      '确定要清除所有缓存数据吗？',
      '确认清除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 清除缓存
    localStorage.removeItem('game-cache')
    sessionStorage.clear()
    
    ElMessage.success('缓存已清除')
  } catch (error) {
    // 用户取消操作
  }
}

function exportData() {
  try {
    const data = {
      settings,
      gameSettings,
      notificationSettings,
      advancedSettings,
      exportTime: new Date().toISOString()
    }
    
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `tradingsim-settings-${new Date().toISOString().split('T')[0]}.json`
    a.click()
    URL.revokeObjectURL(url)
    
    ElMessage.success('数据导出成功')
  } catch (error) {
    console.error('Failed to export data:', error)
    ElMessage.error('数据导出失败')
  }
}

function handleFileChange(file: any) {
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const data = JSON.parse(e.target?.result as string)
      
      // 验证数据格式
      if (data.settings && data.gameSettings) {
        Object.assign(settings, data.settings)
        Object.assign(gameSettings, data.gameSettings)
        Object.assign(notificationSettings, data.notificationSettings || {})
        Object.assign(advancedSettings, data.advancedSettings || {})
        
        ElMessage.success('数据导入成功')
      } else {
        throw new Error('Invalid data format')
      }
    } catch (error) {
      console.error('Failed to import data:', error)
      ElMessage.error('数据导入失败：文件格式不正确')
    }
  }
  reader.readAsText(file.raw)
}

async function resetData() {
  try {
    await ElMessageBox.confirm(
      '确定要重置所有数据吗？这将删除所有游戏记录、设置和缓存。此操作不可撤销！',
      '危险操作',
      {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    // 清除所有数据
    localStorage.clear()
    sessionStorage.clear()
    
    // 重新加载页面
    window.location.reload()
  } catch (error) {
    // 用户取消操作
  }
}

function formatDuration(value: number): string {
  return `${value / 1000}秒`
}
</script>

<style scoped>
.settings-view {
  padding: 20px;
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.settings-header h1 {
  margin: 0;
  color: var(--el-text-color-primary);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.settings-content {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
  min-height: 600px;
}

.settings-section {
  max-width: 600px;
}

.settings-section h2 {
  margin-bottom: 20px;
  color: var(--el-text-color-primary);
  border-bottom: 2px solid var(--el-border-color);
  padding-bottom: 10px;
}

.form-help {
  margin-left: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.color-presets {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.color-preset {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid var(--el-border-color);
  transition: transform 0.2s;
}

.color-preset:hover {
  transform: scale(1.1);
}

:deep(.el-tabs--left .el-tabs__content) {
  padding-left: 20px;
}

:deep(.el-tabs--left .el-tabs__nav-wrap) {
  margin-right: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-slider) {
  margin-right: 20px;
}
</style>