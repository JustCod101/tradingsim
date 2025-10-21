// 全局组件注册
import type { App } from 'vue'
import GlobalLoading from './GlobalLoading.vue'
import GlobalMessage from './GlobalMessage.vue'

// 这里可以导入需要全局注册的组件
// import SomeGlobalComponent from './SomeGlobalComponent.vue'

export function setupGlobalComponents(app: App) {
  // 注册全局组件
  app.component('GlobalLoading', GlobalLoading)
  app.component('GlobalMessage', GlobalMessage)
  
  console.log('Global components registered')
}