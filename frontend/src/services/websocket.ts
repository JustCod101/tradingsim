// WebSocket服务
import { useWebSocketStore } from '@/stores/websocket'
import { useAppStore } from '@/stores/app'
import type { WebSocketMessage, GameFrame } from '@/types'
import { WebSocketMessageType } from '@/types'

// WebSocket事件处理器类型
export type WSEventHandler = (data: any) => void

// WebSocket服务类
export class WebSocketService {
  private ws: WebSocket | null = null
  private url: string
  private protocols?: string | string[]
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectDelay = 1000
  private heartbeatInterval: NodeJS.Timeout | null = null
  private heartbeatTimeout: NodeJS.Timeout | null = null
  private eventHandlers: Map<string, Set<WSEventHandler>> = new Map()
  private messageQueue: WebSocketMessage[] = []
  private isConnecting = false

  constructor(url: string, protocols?: string | string[]) {
    this.url = url
    this.protocols = protocols
  }

  // 连接WebSocket
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        resolve()
        return
      }

      if (this.isConnecting) {
        reject(new Error('Connection already in progress'))
        return
      }

      this.isConnecting = true

      try {
        this.ws = new WebSocket(this.url, this.protocols)

        this.ws.onopen = () => {
          console.log('WebSocket connected')
          this.isConnecting = false
          this.reconnectAttempts = 0
          this.startHeartbeat()
          this.processMessageQueue()
          this.emit('connect', null)
          resolve()
        }

        this.ws.onmessage = (event) => {
          this.handleMessage(event.data)
        }

        this.ws.onclose = (event) => {
          console.log('WebSocket disconnected:', event.code, event.reason)
          this.isConnecting = false
          this.stopHeartbeat()
          this.emit('disconnect', { code: event.code, reason: event.reason })
          
          if (!event.wasClean && this.reconnectAttempts < this.maxReconnectAttempts) {
            this.scheduleReconnect()
          }
        }

        this.ws.onerror = (error) => {
          console.error('WebSocket error:', error)
          this.isConnecting = false
          this.emit('error', error)
          reject(error)
        }
      } catch (error) {
        this.isConnecting = false
        reject(error)
      }
    })
  }

  // 断开连接
  disconnect(): void {
    if (this.ws) {
      this.stopHeartbeat()
      this.ws.close(1000, 'Client disconnect')
      this.ws = null
    }
  }

  // 发送消息
  send(message: Omit<WebSocketMessage, 'timestamp'>): void {
    const fullMessage: WebSocketMessage = {
      ...message,
      timestamp: new Date().toISOString()
    }

    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(fullMessage))
    } else {
      // 连接未建立时，将消息加入队列
      this.messageQueue.push(fullMessage)
    }
  }

  // 处理接收到的消息
  private handleMessage(data: string): void {
    try {
      const message: WebSocketMessage = JSON.parse(data)
      
      // 处理心跳消息
      if (message.type === WebSocketMessageType.HEARTBEAT) {
        this.resetHeartbeatTimeout()
        return
      }

      // 触发事件处理器
      this.emit(message.type, message.data)
    } catch (error) {
      console.error('Failed to parse WebSocket message:', error)
    }
  }

  // 添加事件监听器
  on(event: string, handler: WSEventHandler): void {
    if (!this.eventHandlers.has(event)) {
      this.eventHandlers.set(event, new Set())
    }
    this.eventHandlers.get(event)!.add(handler)
  }

  // 移除事件监听器
  off(event: string, handler: WSEventHandler): void {
    const handlers = this.eventHandlers.get(event)
    if (handlers) {
      handlers.delete(handler)
      if (handlers.size === 0) {
        this.eventHandlers.delete(event)
      }
    }
  }

  // 触发事件
  private emit(event: string, data: any): void {
    const handlers = this.eventHandlers.get(event)
    if (handlers) {
      handlers.forEach(handler => {
        try {
          handler(data)
        } catch (error) {
          console.error(`Error in WebSocket event handler for ${event}:`, error)
        }
      })
    }
  }

  // 开始心跳
  private startHeartbeat(): void {
    this.heartbeatInterval = setInterval(() => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.send({ 
          type: WebSocketMessageType.HEARTBEAT, 
          data: { timestamp: Date.now() } 
        })
        this.setHeartbeatTimeout()
      }
    }, 30000) // 30秒心跳间隔
  }

  // 停止心跳
  private stopHeartbeat(): void {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
    if (this.heartbeatTimeout) {
      clearTimeout(this.heartbeatTimeout)
      this.heartbeatTimeout = null
    }
  }

  // 设置心跳超时
  private setHeartbeatTimeout(): void {
    this.heartbeatTimeout = setTimeout(() => {
      console.warn('WebSocket heartbeat timeout')
      this.disconnect()
    }, 10000) // 10秒超时
  }

  // 重置心跳超时
  private resetHeartbeatTimeout(): void {
    if (this.heartbeatTimeout) {
      clearTimeout(this.heartbeatTimeout)
      this.heartbeatTimeout = null
    }
  }

  // 安排重连
  private scheduleReconnect(): void {
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts)
    console.log(`Scheduling reconnect in ${delay}ms (attempt ${this.reconnectAttempts + 1})`)
    
    setTimeout(() => {
      this.reconnectAttempts++
      this.connect().catch(error => {
        console.error('Reconnect failed:', error)
      })
    }, delay)
  }

  // 处理消息队列
  private processMessageQueue(): void {
    while (this.messageQueue.length > 0) {
      const message = this.messageQueue.shift()!
      this.ws?.send(JSON.stringify(message))
    }
  }

  // 获取连接状态
  get readyState(): number {
    return this.ws?.readyState ?? WebSocket.CLOSED
  }

  // 检查是否已连接
  get isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN
  }
}

// 游戏WebSocket服务
export class GameWebSocketService extends WebSocketService {
  private sessionId: string | null = null

  constructor() {
    super(`${import.meta.env.VITE_WS_BASE_URL}/game`)
    this.setupGameHandlers()
  }

  // 设置游戏相关的事件处理器
  private setupGameHandlers(): void {
    this.on('connect', () => {
      const wsStore = useWebSocketStore()
      // 连接成功后进行认证
      this.authenticate()
    })

    this.on('disconnect', () => {
      const wsStore = useWebSocketStore()
    })

    this.on('error', (error) => {
      const appStore = useAppStore()
      appStore.addNotification({
        title: 'WebSocket错误',
        message: 'WebSocket连接错误',
        type: 'error'
      })
    })

    this.on(WebSocketMessageType.GAME_START, () => {
      console.log('Game started')
    })

    this.on(WebSocketMessageType.FRAME_UPDATE, (frame: GameFrame) => {
      // 处理游戏帧数据
      this.handleGameFrame(frame)
    })

    this.on(WebSocketMessageType.NOTIFICATION, (notification) => {
      const appStore = useAppStore()
      appStore.addNotification({
        title: notification.title,
        message: notification.message,
        type: notification.type || 'info'
      })
    })
  }

  // 认证
  private authenticate(): void {
    const appStore = useAppStore()
    const token = appStore.currentUser?.token
    
    if (token) {
      this.send({
        type: WebSocketMessageType.CONNECT,
        data: { token }
      })
    }
  }

  // 加入游戏会话
  joinSession(sessionId: string): void {
    this.sessionId = sessionId
    this.send({
      type: WebSocketMessageType.GAME_START,
      data: { sessionId }
    })
  }

  // 离开游戏会话
  leaveSession(): void {
    if (this.sessionId) {
      this.send({
        type: WebSocketMessageType.GAME_END,
        data: { sessionId: this.sessionId }
      })
      this.sessionId = null
    }
  }

  // 提交游戏决策
  submitDecision(decision: {
    type: 'BUY' | 'SELL' | 'HOLD'
    quantity?: number
    price?: number
    reasoning?: string
  }): void {
    if (this.sessionId) {
      this.send({
        type: WebSocketMessageType.DECISION_RESULT,
        data: {
          sessionId: this.sessionId,
          ...decision
        }
      })
    }
  }

  // 处理游戏帧
  private handleGameFrame(frame: GameFrame): void {
    // 这里可以添加游戏帧处理逻辑
    // 例如更新图表、计算指标等
    console.log('Received game frame:', frame)
  }
}

// 回放WebSocket服务
export class ReplayWebSocketService extends WebSocketService {
  private replayId: string | null = null

  constructor() {
    super(`${import.meta.env.VITE_WS_BASE_URL}/replay`)
    this.setupReplayHandlers()
  }

  // 设置回放相关的事件处理器
  private setupReplayHandlers(): void {
    this.on('connect', () => {
      this.authenticate()
    })

    this.on(WebSocketMessageType.FRAME_UPDATE, (frame: GameFrame) => {
      this.handleReplayFrame(frame)
    })
  }

  // 认证
  private authenticate(): void {
    const appStore = useAppStore()
    const token = appStore.currentUser?.token
    
    if (token) {
      this.send({
        type: WebSocketMessageType.CONNECT,
        data: { token }
      })
    }
  }

  // 开始回放
  startReplay(replayId: string): void {
    this.replayId = replayId
    this.send({
      type: WebSocketMessageType.GAME_START,
      data: { replayId }
    })
  }

  // 暂停回放
  pauseReplay(): void {
    if (this.replayId) {
      this.send({
        type: WebSocketMessageType.GAME_PAUSE,
        data: { replayId: this.replayId }
      })
    }
  }

  // 恢复回放
  resumeReplay(): void {
    if (this.replayId) {
      this.send({
        type: WebSocketMessageType.GAME_RESUME,
        data: { replayId: this.replayId }
      })
    }
  }

  // 停止回放
  stopReplay(): void {
    if (this.replayId) {
      this.send({
        type: WebSocketMessageType.GAME_END,
        data: { replayId: this.replayId }
      })
      this.replayId = null
    }
  }

  // 处理回放帧
  private handleReplayFrame(frame: GameFrame): void {
    console.log('Received replay frame:', frame)
  }
}

// 导出服务实例
export const gameWebSocket = new GameWebSocketService()
export const replayWebSocket = new ReplayWebSocketService()