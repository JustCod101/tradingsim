// 国际化配置
import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN'
import enUS from './locales/en-US'

// 获取浏览器语言
function getBrowserLanguage(): string {
  const language = navigator.language.toLowerCase()
  if (language.includes('zh')) {
    return 'zh-CN'
  }
  return 'en-US'
}

// 获取存储的语言设置
function getStoredLanguage(): string {
  return localStorage.getItem('app-language') || getBrowserLanguage()
}

const i18n = createI18n({
  legacy: false,
  locale: getStoredLanguage(),
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  },
  globalInjection: true
})

export default i18n

// 切换语言
export function setLanguage(locale: string) {
  i18n.global.locale.value = locale as 'zh-CN' | 'en-US'
  localStorage.setItem('app-language', locale)
  document.documentElement.lang = locale
}

// 获取当前语言
export function getCurrentLanguage() {
  return i18n.global.locale.value
}

// 获取支持的语言列表
export function getSupportedLanguages() {
  return [
    { code: 'zh-CN', name: '简体中文' },
    { code: 'en-US', name: 'English' }
  ]
}