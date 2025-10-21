// 全局属性设置
import type { App } from 'vue'
import { formatNumber, formatCurrency, formatTime } from './index'

export function setupGlobalProperties(app: App) {
  // 添加全局属性
  app.config.globalProperties.$formatNumber = formatNumber
  app.config.globalProperties.$formatCurrency = formatCurrency
  app.config.globalProperties.$formatTime = formatTime
  
  // 添加全局常量
  app.config.globalProperties.$APP_NAME = 'Trading Simulator'
  app.config.globalProperties.$APP_VERSION = import.meta.env.VITE_APP_VERSION || '1.0.0'
  
  console.log('Global properties registered')
}