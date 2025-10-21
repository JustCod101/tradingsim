import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import type { ThemeConfig } from '@/types'

export const useThemeStore = defineStore('theme', () => {
  // 状态
  const config = ref<ThemeConfig>({
    mode: 'auto',
    primaryColor: '#409EFF',
    borderRadius: 4,
    fontSize: 14,
    compactMode: false
  })
  
  const systemPrefersDark = ref(false)
  
  // 计算属性
  const isDark = computed(() => {
    if (config.value.mode === 'auto') {
      return systemPrefersDark.value
    }
    return config.value.mode === 'dark'
  })
  
  const currentTheme = computed(() => isDark.value ? 'dark' : 'light')
  
  // CSS变量
  const cssVariables = computed(() => ({
    '--el-color-primary': config.value.primaryColor,
    '--el-border-radius-base': `${config.value.borderRadius}px`,
    '--el-font-size-base': `${config.value.fontSize}px`,
    '--app-compact-mode': config.value.compactMode ? '1' : '0'
  }))
  
  // 初始化主题
  async function initialize() {
    try {
      // 加载保存的主题配置
      loadThemeConfig()
      
      // 监听系统主题变化
      setupSystemThemeListener()
      
      // 应用主题
      applyTheme()
      
      // 监听配置变化
      setupConfigWatcher()
      
      console.log('✅ Theme store initialized')
    } catch (error) {
      console.error('❌ Theme store initialization failed:', error)
      throw error
    }
  }
  
  // 加载主题配置
  function loadThemeConfig() {
    try {
      const savedConfig = localStorage.getItem('theme-config')
      if (savedConfig) {
        const parsed = JSON.parse(savedConfig)
        config.value = { ...config.value, ...parsed }
      }
    } catch (error) {
      console.warn('Failed to load theme config:', error)
    }
  }
  
  // 保存主题配置
  function saveThemeConfig() {
    try {
      localStorage.setItem('theme-config', JSON.stringify(config.value))
    } catch (error) {
      console.warn('Failed to save theme config:', error)
    }
  }
  
  // 设置主题模式
  function setMode(mode: 'light' | 'dark' | 'auto') {
    config.value.mode = mode
    applyTheme()
    saveThemeConfig()
  }
  
  // 设置主色调
  function setPrimaryColor(color: string) {
    config.value.primaryColor = color
    applyTheme()
    saveThemeConfig()
  }
  
  // 设置边框圆角
  function setBorderRadius(radius: number) {
    config.value.borderRadius = Math.max(0, Math.min(20, radius))
    applyTheme()
    saveThemeConfig()
  }
  
  // 设置字体大小
  function setFontSize(size: number) {
    config.value.fontSize = Math.max(10, Math.min(20, size))
    applyTheme()
    saveThemeConfig()
  }
  
  // 切换紧凑模式
  function toggleCompactMode() {
    config.value.compactMode = !config.value.compactMode
    applyTheme()
    saveThemeConfig()
  }
  
  // 切换主题模式
  function toggleMode() {
    const modes: Array<'light' | 'dark' | 'auto'> = ['light', 'dark', 'auto']
    const currentIndex = modes.indexOf(config.value.mode)
    const nextIndex = (currentIndex + 1) % modes.length
    setMode(modes[nextIndex])
  }
  
  // 重置主题配置
  function resetConfig() {
    config.value = {
      mode: 'auto',
      primaryColor: '#409EFF',
      borderRadius: 4,
      fontSize: 14,
      compactMode: false
    }
    applyTheme()
    saveThemeConfig()
  }
  
  // 应用主题
  function applyTheme() {
    const root = document.documentElement
    
    // 设置主题类
    root.classList.remove('light', 'dark')
    root.classList.add(currentTheme.value)
    
    // 设置CSS变量
    Object.entries(cssVariables.value).forEach(([key, value]) => {
      root.style.setProperty(key, value)
    })
    
    // 设置Element Plus主题
    if (isDark.value) {
      root.classList.add('dark')
    } else {
      root.classList.remove('dark')
    }
    
    // 更新meta标签
    updateMetaThemeColor()
    
    console.log(`Theme applied: ${currentTheme.value}`)
  }
  
  // 更新meta主题色
  function updateMetaThemeColor() {
    let metaThemeColor = document.querySelector('meta[name=\"theme-color\"]')
    if (!metaThemeColor) {
      metaThemeColor = document.createElement('meta')
      metaThemeColor.setAttribute('name', 'theme-color')
      document.head.appendChild(metaThemeColor)
    }
    
    metaThemeColor.setAttribute('content', config.value.primaryColor)
  }
  
  // 设置系统主题监听器
  function setupSystemThemeListener() {
    if (window.matchMedia) {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      
      // 初始值
      systemPrefersDark.value = mediaQuery.matches
      
      // 监听变化
      const handleChange = (e: MediaQueryListEvent) => {
        systemPrefersDark.value = e.matches
        if (config.value.mode === 'auto') {
          applyTheme()
        }
      }
      
      mediaQuery.addEventListener('change', handleChange)
      
      // 返回清理函数
      return () => {
        mediaQuery.removeEventListener('change', handleChange)
      }
    }
    
    // 如果不支持 matchMedia，返回空函数
    return () => {}
  }
  
  // 设置配置监听器
  function setupConfigWatcher() {
    watch(
      () => config.value,
      () => {
        applyTheme()
      },
      { deep: true }
    )
  }
  
  // 获取预设主题
  function getPresetThemes() {
    return [
      {
        name: '默认蓝',
        primaryColor: '#409EFF',
        preview: '#409EFF'
      },
      {
        name: '成功绿',
        primaryColor: '#67C23A',
        preview: '#67C23A'
      },
      {
        name: '警告橙',
        primaryColor: '#E6A23C',
        preview: '#E6A23C'
      },
      {
        name: '危险红',
        primaryColor: '#F56C6C',
        preview: '#F56C6C'
      },
      {
        name: '信息灰',
        primaryColor: '#909399',
        preview: '#909399'
      },
      {
        name: '紫色',
        primaryColor: '#8B5CF6',
        preview: '#8B5CF6'
      },
      {
        name: '粉色',
        primaryColor: '#EC4899',
        preview: '#EC4899'
      },
      {
        name: '青色',
        primaryColor: '#06B6D4',
        preview: '#06B6D4'
      }
    ]
  }
  
  // 应用预设主题
  function applyPresetTheme(preset: { primaryColor: string }) {
    setPrimaryColor(preset.primaryColor)
  }
  
  // 导出主题配置
  function exportConfig() {
    return JSON.stringify(config.value, null, 2)
  }
  
  // 导入主题配置
  function importConfig(configJson: string) {
    try {
      const imported = JSON.parse(configJson)
      config.value = { ...config.value, ...imported }
      applyTheme()
      saveThemeConfig()
      return true
    } catch (error) {
      console.error('Failed to import theme config:', error)
      return false
    }
  }
  
  return {
    // 状态
    config,
    isDark,
    currentTheme,
    systemPrefersDark,
    cssVariables,
    
    // 方法
    initialize,
    setMode,
    setPrimaryColor,
    setBorderRadius,
    setFontSize,
    toggleCompactMode,
    toggleMode,
    resetConfig,
    applyTheme,
    getPresetThemes,
    applyPresetTheme,
    exportConfig,
    importConfig
  }
})