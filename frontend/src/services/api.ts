// API服务层
import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'
import type { ApiResponse, PaginatedResponse } from '@/types'

// 扩展AxiosRequestConfig类型
interface CustomAxiosRequestConfig extends AxiosRequestConfig {
  showLoading?: boolean
}

// 创建axios实例
const createApiInstance = (): AxiosInstance => {
  const instance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 30000,
    headers: {
      'Content-Type': 'application/json'
    }
  })

  // 请求拦截器
  instance.interceptors.request.use(
    (config) => {
      const appStore = useAppStore()
      
      // 添加认证token
      if (appStore.currentUser?.token) {
        config.headers.Authorization = `Bearer ${appStore.currentUser.token}`
      }

      // 添加请求ID用于追踪
      config.headers['X-Request-ID'] = generateRequestId()

      // 显示加载状态
      if ((config as CustomAxiosRequestConfig).showLoading !== false) {
        appStore.setLoading(true)
      }

      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )

  // 响应拦截器
  instance.interceptors.response.use(
    (response: AxiosResponse) => {
      const appStore = useAppStore()
      appStore.setLoading(false)

      // 检查业务状态码
      const { code, message } = response.data
      if (code !== 200) {
        ElMessage.error(message || '请求失败')
        return Promise.reject(new Error(message || '请求失败'))
      }

      return response
    },
    (error) => {
      const appStore = useAppStore()
      appStore.setLoading(false)

      // 处理HTTP错误
      if (error.response) {
        const { status, data } = error.response
        
        switch (status) {
          case 401:
            ElMessage.error('登录已过期，请重新登录')
            appStore.logout()
            break
          case 403:
            ElMessage.error('没有权限访问该资源')
            break
          case 404:
            ElMessage.error('请求的资源不存在')
            break
          case 500:
            ElMessage.error('服务器内部错误')
            break
          default:
            ElMessage.error(data?.message || `请求失败 (${status})`)
        }
      } else if (error.request) {
        ElMessage.error('网络连接失败，请检查网络设置')
      } else {
        ElMessage.error('请求配置错误')
      }

      return Promise.reject(error)
    }
  )

  return instance
}

// 生成请求ID
const generateRequestId = (): string => {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
}

// API实例
const api = createApiInstance()

// 通用请求方法
export const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return api.get(url, config).then(res => res.data)
  },

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return api.post(url, data, config).then(res => res.data)
  },

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return api.put(url, data, config).then(res => res.data)
  },

  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return api.delete(url, config).then(res => res.data)
  },

  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return api.patch(url, data, config).then(res => res.data)
  }
}

// 文件上传
export const uploadFile = async (
  file: File,
  onProgress?: (progress: number) => void
): Promise<ApiResponse<{ url: string; filename: string }>> => {
  const formData = new FormData()
  formData.append('file', file)

  return api.post('/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(progress)
      }
    }
  }).then(res => res.data)
}

// 下载文件
export const downloadFile = async (url: string, filename?: string): Promise<void> => {
  const response = await api.get(url, {
    responseType: 'blob'
  } as CustomAxiosRequestConfig)

  const blob = new Blob([response.data])
  const downloadUrl = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = filename || 'download'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(downloadUrl)
}

// 取消请求的控制器
export const createCancelToken = () => {
  return axios.CancelToken.source()
}

// 批量请求
export const batchRequest = async <T = any>(
  requests: Array<() => Promise<ApiResponse<T>>>
): Promise<Array<ApiResponse<T> | Error>> => {
  return Promise.allSettled(requests.map(req => req())).then(results =>
    results.map(result => 
      result.status === 'fulfilled' ? result.value : result.reason
    )
  )
}

// 重试请求
export const retryRequest = async <T = any>(
  requestFn: () => Promise<ApiResponse<T>>,
  maxRetries = 3,
  delay = 1000
): Promise<ApiResponse<T>> => {
  let lastError: Error

  for (let i = 0; i <= maxRetries; i++) {
    try {
      return await requestFn()
    } catch (error) {
      lastError = error as Error
      
      if (i < maxRetries) {
        await new Promise(resolve => setTimeout(resolve, delay * Math.pow(2, i)))
      }
    }
  }

  throw lastError!
}

export default api