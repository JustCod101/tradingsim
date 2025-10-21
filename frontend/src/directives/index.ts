// 全局指令注册
import type { App } from 'vue'

// 权限指令
const permission = {
  mounted(el: HTMLElement, binding: any) {
    const { value } = binding
    if (value && !checkPermission(value)) {
      el.style.display = 'none'
    }
  },
  updated(el: HTMLElement, binding: any) {
    const { value } = binding
    if (value && !checkPermission(value)) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

// 检查权限的辅助函数
function checkPermission(permission: string | string[]): boolean {
  // 这里应该从 store 中获取用户权限进行检查
  // 暂时返回 true
  return true
}

// 防抖指令
const debounce = {
  mounted(el: HTMLElement, binding: any) {
    let timer: NodeJS.Timeout
    el.addEventListener('click', () => {
      if (timer) clearTimeout(timer)
      timer = setTimeout(() => {
        binding.value()
      }, binding.arg || 300)
    })
  }
}

// 节流指令
const throttle = {
  mounted(el: HTMLElement, binding: any) {
    let timer: NodeJS.Timeout | null = null
    el.addEventListener('click', () => {
      if (timer) return
      timer = setTimeout(() => {
        binding.value()
        timer = null
      }, binding.arg || 300)
    })
  }
}

export function setupGlobalDirectives(app: App) {
  app.directive('permission', permission)
  app.directive('debounce', debounce)
  app.directive('throttle', throttle)
  
  console.log('Global directives registered')
}