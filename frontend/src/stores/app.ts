import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { 
  AppConfig, 
  AppError, 
  Notification, 
  Modal,
  User 
} from '@/types'

export const useAppStore = defineStore('app', () => {
  // 状态
  const isLoading = ref(false)
  const loadingText = ref('')
  const isOnline = ref(navigator.onLine)
  const isFullscreen = ref(false)
  const hasUnsavedChanges = ref(false)
  const showCommandPalette = ref(false)
  
  // 配置
  const config = ref<AppConfig>({
    theme: {
      mode: 'auto',
      primaryColor: '#409EFF',
      borderRadius: 4,
      fontSize: 14,
      compactMode: false
    },
    language: 'zh-CN',
    autoSave: true,
    soundEnabled: true,
    animationEnabled: true,
    debugMode: import.meta.env.DEV
  })
  
  // 错误状态
  const errors = ref<AppError[]>([])
  const lastError = computed(() => errors.value[errors.value.length - 1])
  
  // 通知状态
  const notifications = ref<Notification[]>([])
  
  // 模态框状态
  const modals = ref<Modal[]>([])
  const activeModal = computed(() => modals.value[modals.value.length - 1])
  
  // 用户状态
  const currentUser = ref<User | null>(null)
  const isAuthenticated = computed(() => !!currentUser.value)
  
  // 应用信息
  const appInfo = computed(() => ({
    name: import.meta.env.VITE_APP_TITLE || 'TradingSim',
    version: import.meta.env.VITE_APP_VERSION || '1.0.0',
    buildTime: import.meta.env.VITE_BUILD_TIME || new Date().toISOString(),
    environment: import.meta.env.MODE
  }))
  
  // 初始化应用
  async function initialize() {
    try {
      setLoading(true)
      
      // 加载配置
      await loadConfig()
      
      // 检查认证状态
      await checkAuth()
      
      // 初始化其他服务
      await initializeServices()
      
      console.log('✅ App store initialized')
    } catch (error) {
      console.error('❌ App store initialization failed:', error)
      setError('应用初始化失败')
      throw error
    } finally {
      setLoading(false)
    }
  }
  
  // 加载配置
  async function loadConfig() {
    try {
      const savedConfig = localStorage.getItem('app-config')
      if (savedConfig) {
        const parsed = JSON.parse(savedConfig)
        config.value = { ...config.value, ...parsed }
      }
    } catch (error) {
      console.warn('Failed to load config from localStorage:', error)
    }
  }
  
  // 保存配置
  async function saveConfig() {
    try {
      localStorage.setItem('app-config', JSON.stringify(config.value))
    } catch (error) {
      console.warn('Failed to save config to localStorage:', error)
    }
  }
  
  // 检查认证状态
  async function checkAuth() {
    try {
      const token = localStorage.getItem('auth-token')
      if (token) {
        // 验证 token 并获取用户信息
        const { authApi } = await import('@/services/auth')
        const response = await authApi.getCurrentUser()
        if (response.success && response.data) {
          currentUser.value = response.data
        } else {
          // token无效，清除认证信息
          localStorage.removeItem('auth-token')
          localStorage.removeItem('refresh-token')
        }
      }
    } catch (error) {
      console.warn('Auth check failed:', error)
      // 清除无效的认证信息
      localStorage.removeItem('auth-token')
      localStorage.removeItem('refresh-token')
    }
  }
  
  // 初始化服务
  async function initializeServices() {
    // TODO: 初始化其他服务
    // 例如：错误监控、性能监控、分析服务等
  }
  
  // 设置加载状态
  function setLoading(loading: boolean) {
    isLoading.value = loading
  }
  
  // 设置在线状态
  function setOnline(online: boolean) {
    isOnline.value = online
    
    if (online) {
      addNotification({
        type: 'success',
        title: '网络已连接',
        message: '网络连接已恢复',
        duration: 3000
      })
    } else {
      addNotification({
        type: 'warning',
        title: '网络已断开',
        message: '请检查您的网络连接',
        duration: 0 // 不自动关闭
      })
    }
  }
  
  // 设置全屏状态
  function setFullscreen(fullscreen: boolean) {
    isFullscreen.value = fullscreen
  }
  
  // 切换全屏
  async function toggleFullscreen() {
    try {
      if (!document.fullscreenElement) {
        await document.documentElement.requestFullscreen()
        setFullscreen(true)
      } else {
        await document.exitFullscreen()
        setFullscreen(false)
      }
    } catch (error) {
      console.warn('Fullscreen toggle failed:', error)
    }
  }
  
  // 设置未保存更改状态
  function setUnsavedChanges(hasChanges: boolean) {
    hasUnsavedChanges.value = hasChanges
  }
  
  // 切换命令面板
  function toggleCommandPalette() {
    showCommandPalette.value = !showCommandPalette.value
  }
  
  // 错误管理
  function setError(message: string, details?: any) {
    const error: AppError = {
      code: 'APP_ERROR',
      message,
      details,
      timestamp: new Date().toISOString(),
      stack: new Error().stack
    }
    
    errors.value.push(error)
    
    // 限制错误数量
    if (errors.value.length > 10) {
      errors.value.shift()
    }
    
    // 显示错误通知
    addNotification({
      type: 'error',
      title: '错误',
      message,
      duration: 5000
    })
  }
  
  function clearErrors() {
    errors.value = []
  }
  
  // 通知管理
  function addNotification(notification: Omit<Notification, 'id' | 'timestamp'>) {
    const id = `notification-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
    const fullNotification: Notification = {
      ...notification,
      id,
      timestamp: new Date().toISOString()
    }
    
    notifications.value.push(fullNotification)
    
    // 自动移除通知
    if (notification.duration && notification.duration > 0) {
      setTimeout(() => {
        removeNotification(id)
      }, notification.duration)
    }
    
    return id
  }

  function addError(error: Omit<Notification, 'id' | 'timestamp'>) {
    addNotification({
      ...error,
      type: 'error'
    })
  }
  
  function removeNotification(id: string) {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }
  
  function clearNotifications() {
    notifications.value = []
  }
  
  // 模态框管理
  function openModal(modal: Omit<Modal, 'id'>) {
    const id = `modal-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
    const fullModal: Modal = {
      ...modal,
      id
    }
    
    modals.value.push(fullModal)
    return id
  }
  
  function closeModal(id?: string) {
    if (id) {
      const index = modals.value.findIndex(m => m.id === id)
      if (index > -1) {
        modals.value.splice(index, 1)
      }
    } else {
      // 关闭最顶层的模态框
      modals.value.pop()
    }
  }
  
  function closeAllModals() {
    modals.value = []
  }
  
  // 用户管理
  function setCurrentUser(user: User | null) {
    currentUser.value = user
  }
  
  function logout() {
    currentUser.value = null
    localStorage.removeItem('auth-token')
    
    addNotification({
      type: 'info',
      title: '已退出登录',
      message: '您已成功退出登录',
      duration: 3000
    })
  }
  
  // 配置管理
  function updateConfig(updates: Partial<AppConfig>) {
    config.value = { ...config.value, ...updates }
    saveConfig()
  }
  
  function resetConfig() {
    config.value = {
      theme: {
        mode: 'auto',
        primaryColor: '#409EFF',
        borderRadius: 4,
        fontSize: 14,
        compactMode: false
      },
      language: 'zh-CN',
      autoSave: true,
      soundEnabled: true,
      animationEnabled: true,
      debugMode: import.meta.env.DEV
    }
    saveConfig()
  }
  
  return {
      // 状态
      isLoading,
      loadingText,
      isOnline,
      isFullscreen,
      hasUnsavedChanges,
      showCommandPalette,
      config,
      errors,
      lastError,
      notifications,
      modals,
      activeModal,
      currentUser,
      isAuthenticated,
      appInfo,
      
      // 方法
      initialize,
      setLoading,
      setOnline,
      setFullscreen,
      toggleFullscreen,
      setUnsavedChanges,
      toggleCommandPalette,
      setError,
      clearErrors,
      addNotification,
      addError,
      removeNotification,
      clearNotifications,
      openModal,
      closeModal,
      closeAllModals,
      setCurrentUser,
      logout,
      updateConfig,
      resetConfig
    }
})