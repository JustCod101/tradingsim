import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'

import App from './App.vue'
import router from './router'
import i18n from './i18n'
import { setupGlobalComponents } from './components'
import { setupGlobalDirectives } from './directives'
import { setupGlobalProperties } from './utils/global'

import './styles/index.scss'

// 创建应用实例
const app = createApp(App)

// 注册 Pinia 状态管理
const pinia = createPinia()
app.use(pinia)

// 注册路由
app.use(router)

// 注册国际化
app.use(i18n)

// 注册 Element Plus
app.use(ElementPlus, {
  size: 'default',
  zIndex: 3000,
})

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册全局组件
setupGlobalComponents(app)

// 注册全局指令
setupGlobalDirectives(app)

// 注册全局属性
setupGlobalProperties(app)

// 错误处理
app.config.errorHandler = (err, vm, info) => {
  console.error('Vue Error:', err)
  console.error('Component:', vm)
  console.error('Info:', info)
  
  // 在生产环境中，可以将错误发送到监控服务
  if (import.meta.env.PROD) {
    // TODO: 发送错误到监控服务
  }
}

// 性能监控
if (import.meta.env.DEV) {
  app.config.performance = true
}

// 挂载应用
app.mount('#app')

// 开发环境调试
if (import.meta.env.DEV) {
  ;(window as any).__VUE_APP__ = app
  console.log('🚀 TradingSim Frontend Started')
  console.log('📦 Vue Version:', app.version)
  console.log('🌍 Environment:', import.meta.env.MODE)
}