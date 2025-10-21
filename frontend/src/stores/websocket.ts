import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import type { WebSocketMessage, WebSocketMessageType } from '@/types'
import { useAppStore } from './app'

export const useWebSocketStore = defineStore('websocket', () => {
  // 状态
  const client = ref<Client | null>(null)
  const isConnected = ref(false)
  const isConnecting = ref(false)
  const reconnectAttempts = ref(0)
  const maxReconnectAttempts = ref(5)
  const reconnectInterval = ref(3000)
  const heartbeatInterval = ref(30000)
  const lastHeartbeat = ref<Date | null>(null)
  const isPaused = ref(false)
  
  // 消息队列
  const messageQueue = ref<WebSocketMessage[]>([])
  const subscriptions = ref<Map<string, any>>(new Map())
  
  // 计算属性
  const connectionStatus = computed(() => {
    if (isConnecting.value) return 'connecting'
    if (isConnected.value) return 'connected'
    return 'disconnected'
  })
  
  const canReconnect = computed(() => {
    return reconnectAttempts.value < maxReconnectAttempts.value
  })
  
  // 获取应用store
  const appStore = useAppStore()
  
  // 初始化WebSocket连接
  async function initialize() {
    try {
      await connect()
      console.log('✅ WebSocket store initialized')
    } catch (error) {
      console.error('❌ WebSocket store initialization failed:', error)
      throw error
    }
  }
  
  // 连接WebSocket
  async function connect() {
    if (isConnected.value || isConnecting.value) {
      return
    }
    
    try {
      isConnecting.value = true
      
      // 创建STOMP客户端
      client.value = new Client({
        webSocketFactory: () => new SockJS(import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080/ws'),
        connectHeaders: {
          // 添加认证头
          Authorization: `Bearer ${localStorage.getItem('auth-token') || ''}`
        },
        debug: (str) => {
          if (import.meta.env.DEV) {
            console.log('STOMP Debug:', str)
          }
        },
        reconnectDelay: reconnectInterval.value,
        heartbeatIncoming: heartbeatInterval.value,
        heartbeatOutgoing: heartbeatInterval.value,
        onConnect: handleConnect,
        onDisconnect: handleDisconnect,
        onStompError: handleError,
        onWebSocketError: handleWebSocketError,
        onWebSocketClose: handleWebSocketClose
      })
      
      // 激活连接
      client.value.activate()
      
    } catch (error) {
      console.error('WebSocket connection failed:', error)
      isConnecting.value = false
      throw error
    }
  }
  
  // 断开连接
  function disconnect() {
    if (client.value) {
      client.value.deactivate()
      client.value = null
    }
    
    isConnected.value = false
    isConnecting.value = false
    reconnectAttempts.value = 0
    
    // 清理订阅
    subscriptions.value.clear()
    
    console.log('WebSocket disconnected')
  }
  
  // 重连
  async function reconnect() {
    if (!canReconnect.value) {
      console.warn('Max reconnect attempts reached')
      appStore.addNotification({
        type: 'error',
        title: '连接失败',
        message: '无法连接到服务器，请刷新页面重试',
        duration: 0
      })
      return
    }
    
    reconnectAttempts.value++
    
    console.log(`Attempting to reconnect (${reconnectAttempts.value}/${maxReconnectAttempts.value})`)
    
    appStore.addNotification({
      type: 'info',
      title: '重新连接中',
      message: `正在尝试重新连接... (${reconnectAttempts.value}/${maxReconnectAttempts.value})`,
      duration: 3000
    })
    
    // 等待一段时间后重连
    setTimeout(() => {
      connect()
    }, reconnectInterval.value)
  }
  
  // 发送消息
  function sendMessage(destination: string, message: any, headers: Record<string, string> = {}) {
    if (!isConnected.value || !client.value) {
      console.warn('WebSocket not connected, queuing message')
      messageQueue.value.push({
        type: 'QUEUED' as WebSocketMessageType,
        data: { destination, message, headers },
        timestamp: new Date().toISOString()
      })
      return
    }
    
    try {
      client.value.publish({
        destination,
        body: JSON.stringify(message),
        headers
      })
    } catch (error) {
      console.error('Failed to send message:', error)
      throw error
    }
  }
  
  // 订阅主题
  function subscribe(destination: string, callback: (message: any) => void) {
    if (!client.value) {
      console.warn('WebSocket client not available')
      return null
    }
    
    try {
      const subscription = client.value.subscribe(destination, (message) => {
        try {
          const data = JSON.parse(message.body)
          callback(data)
        } catch (error) {
          console.error('Failed to parse message:', error)
          callback(message.body)
        }
      })
      
      subscriptions.value.set(destination, subscription)
      return subscription
    } catch (error) {
      console.error('Failed to subscribe:', error)
      throw error
    }
  }
  
  // 取消订阅
  function unsubscribe(destination: string) {
    const subscription = subscriptions.value.get(destination)
    if (subscription) {
      subscription.unsubscribe()
      subscriptions.value.delete(destination)
    }
  }
  
  // 暂停心跳
  function pauseHeartbeat() {
    isPaused.value = true
    if (client.value) {
      // 暂停心跳但保持连接
      client.value.heartbeatOutgoing = 0
    }
  }
  
  // 恢复心跳
  function resumeHeartbeat() {
    isPaused.value = false
    if (client.value) {
      client.value.heartbeatOutgoing = heartbeatInterval.value
    }
  }
  
  // 处理连接成功
  function handleConnect(frame: any) {
    console.log('WebSocket connected:', frame)
    
    isConnected.value = true
    isConnecting.value = false
    reconnectAttempts.value = 0
    lastHeartbeat.value = new Date()
    
    // 发送队列中的消息
    processMessageQueue()
    
    // 通知应用
    appStore.addNotification({
      type: 'success',
      title: '连接成功',
      message: 'WebSocket连接已建立',
      duration: 3000
    })
    
    // 订阅系统消息
    subscribeToSystemMessages()
  }
  
  // 处理连接断开
  function handleDisconnect(frame: any) {
    console.log('WebSocket disconnected:', frame)
    
    isConnected.value = false
    isConnecting.value = false
    
    // 清理订阅
    subscriptions.value.clear()
    
    // 尝试重连
    if (!isPaused.value) {
      reconnect()
    }
  }
  
  // 处理STOMP错误
  function handleError(frame: any) {
    console.error('STOMP error:', frame)
    
    appStore.setError('WebSocket连接错误', frame)
  }
  
  // 处理WebSocket错误
  function handleWebSocketError(event: Event) {
    console.error('WebSocket error:', event)
    
    appStore.setError('WebSocket连接错误')
  }
  
  // 处理WebSocket关闭
  function handleWebSocketClose(event: CloseEvent) {
    console.log('WebSocket closed:', event)
    
    if (event.code !== 1000) {
      // 非正常关闭，尝试重连
      reconnect()
    }
  }
  
  // 处理消息队列
  function processMessageQueue() {
    while (messageQueue.value.length > 0) {
      const queuedMessage = messageQueue.value.shift()
      if (queuedMessage && queuedMessage.data) {
        const { destination, message, headers } = queuedMessage.data
        sendMessage(destination, message, headers)
      }
    }
  }
  
  // 订阅系统消息
  function subscribeToSystemMessages() {
    // 订阅错误消息
    subscribe('/user/queue/errors', (message) => {
      appStore.setError(message.message || '系统错误', message)
    })
    
    // 订阅通知消息
    subscribe('/user/queue/notifications', (message) => {
      appStore.addNotification({
        type: message.type || 'info',
        title: message.title || '通知',
        message: message.message || '',
        duration: message.duration || 5000
      })
    })
    
    // 订阅心跳消息
    subscribe('/topic/heartbeat', (message) => {
      lastHeartbeat.value = new Date()
    })
  }
  
  const setConnectionStatus = (status: 'connected' | 'disconnected' | 'connecting' | 'reconnecting') => {
    // Note: connectionStatus is computed, so we update the underlying state
    if (status === 'connecting') {
      isConnecting.value = true
      isConnected.value = false
    } else if (status === 'connected') {
      isConnecting.value = false
      isConnected.value = true
    } else {
      isConnecting.value = false
      isConnected.value = false
    }
  }

  return {
    // 状态
    isConnected,
    isConnecting,
    connectionStatus,
    reconnectAttempts,
    maxReconnectAttempts,
    canReconnect,
    lastHeartbeat,
    isPaused,
    
    // 方法
    initialize,
    connect,
    disconnect,
    reconnect,
    sendMessage,
    subscribe,
    unsubscribe,
    pauseHeartbeat,
    resumeHeartbeat,
    setConnectionStatus
  }
})