// 认证相关API服务
import { request } from './api'
import type { User, ApiResponse } from '@/types'

// 登录请求参数
export interface LoginRequest {
  username: string
  password: string
  rememberMe?: boolean
}

// 注册请求参数
export interface RegisterRequest {
  username: string
  email: string
  password: string
  confirmPassword: string
  inviteCode?: string
}

// 登录响应
export interface LoginResponse {
  user: User
  token: string
  refreshToken: string
  expiresIn: number
}

// 刷新令牌响应
export interface RefreshTokenResponse {
  token: string
  refreshToken: string
  expiresIn: number
}

// 密码重置请求
export interface ResetPasswordRequest {
  email: string
}

// 密码更新请求
export interface UpdatePasswordRequest {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

// 用户资料更新请求
export interface UpdateProfileRequest {
  username?: string
  email?: string
  avatar?: string
}

// 认证API
export const authApi = {
  // 用户登录
  login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return request.post('/api/auth/login', data)
  },

  // 用户注册
  register(data: RegisterRequest): Promise<ApiResponse<LoginResponse>> {
    return request.post('/api/auth/register', data)
  },

  // 用户登出
  logout(): Promise<ApiResponse<void>> {
    return request.post('/api/auth/logout')
  },

  // 刷新访问令牌
  refreshToken(refreshToken: string): Promise<ApiResponse<RefreshTokenResponse>> {
    return request.post('/api/auth/refresh', { refreshToken })
  },

  // 获取当前用户信息
  getCurrentUser(): Promise<ApiResponse<User>> {
    return request.get('/api/auth/me')
  },

  // 更新用户资料
  updateProfile(data: UpdateProfileRequest): Promise<ApiResponse<User>> {
    return request.put('/api/auth/profile', data)
  },

  // 更新密码
  updatePassword(data: UpdatePasswordRequest): Promise<ApiResponse<void>> {
    return request.put('/api/auth/password', data)
  },

  // 请求密码重置
  requestPasswordReset(data: ResetPasswordRequest): Promise<ApiResponse<void>> {
    return request.post('/api/auth/password/reset', data)
  },

  // 确认密码重置
  confirmPasswordReset(token: string, newPassword: string): Promise<ApiResponse<void>> {
    return request.post('/api/auth/password/reset/confirm', { token, newPassword })
  },

  // 验证邮箱
  verifyEmail(token: string): Promise<ApiResponse<void>> {
    return request.post('/api/auth/email/verify', { token })
  },

  // 重新发送验证邮件
  resendVerificationEmail(): Promise<ApiResponse<void>> {
    return request.post('/api/auth/email/resend')
  },

  // 检查用户名是否可用
  checkUsernameAvailability(username: string): Promise<ApiResponse<{ available: boolean }>> {
    return request.get(`/api/auth/check/username/${username}`)
  },

  // 检查邮箱是否可用
  checkEmailAvailability(email: string): Promise<ApiResponse<{ available: boolean }>> {
    return request.get(`/api/auth/check/email/${email}`)
  },

  // 验证邀请码
  validateInviteCode(code: string): Promise<ApiResponse<{ valid: boolean; inviterName?: string }>> {
    return request.get(`/api/auth/invite/${code}/validate`)
  },

  // 启用两步验证
  enableTwoFactor(): Promise<ApiResponse<{
    qrCode: string
    secret: string
    backupCodes: string[]
  }>> {
    return request.post('/api/auth/2fa/enable')
  },

  // 确认两步验证设置
  confirmTwoFactor(code: string): Promise<ApiResponse<void>> {
    return request.post('/api/auth/2fa/confirm', { code })
  },

  // 禁用两步验证
  disableTwoFactor(code: string): Promise<ApiResponse<void>> {
    return request.post('/api/auth/2fa/disable', { code })
  },

  // 生成新的备份码
  generateBackupCodes(): Promise<ApiResponse<{ backupCodes: string[] }>> {
    return request.post('/api/auth/2fa/backup-codes')
  },

  // 获取用户会话列表
  getUserSessions(): Promise<ApiResponse<Array<{
    id: string
    deviceInfo: string
    ipAddress: string
    location: string
    lastActivity: string
    current: boolean
  }>>> {
    return request.get('/api/auth/sessions')
  },

  // 撤销用户会话
  revokeSession(sessionId: string): Promise<ApiResponse<void>> {
    return request.delete(`/api/auth/sessions/${sessionId}`)
  },

  // 撤销所有其他会话
  revokeAllOtherSessions(): Promise<ApiResponse<void>> {
    return request.delete('/api/auth/sessions/others')
  }
}

// 权限检查工具
export const permissionUtils = {
  // 检查用户是否有特定角色
  hasRole(user: User | null, role: string): boolean {
    return user?.roles?.includes(role) ?? false
  },

  // 检查用户是否有任一角色
  hasAnyRole(user: User | null, roles: string[]): boolean {
    return roles.some(role => this.hasRole(user, role))
  },

  // 检查用户是否有所有角色
  hasAllRoles(user: User | null, roles: string[]): boolean {
    return roles.every(role => this.hasRole(user, role))
  },

  // 检查用户是否为管理员
  isAdmin(user: User | null): boolean {
    return this.hasRole(user, 'admin')
  },

  // 检查用户是否为版主
  isModerator(user: User | null): boolean {
    return this.hasAnyRole(user, ['admin', 'moderator'])
  },

  // 检查用户是否为VIP
  isVip(user: User | null): boolean {
    return this.hasAnyRole(user, ['admin', 'moderator', 'vip'])
  }
}

// 令牌管理工具
export const tokenUtils = {
  // 获取访问令牌
  getAccessToken(): string | null {
    return localStorage.getItem('access_token')
  },

  // 设置访问令牌
  setAccessToken(token: string): void {
    localStorage.setItem('access_token', token)
  },

  // 获取刷新令牌
  getRefreshToken(): string | null {
    return localStorage.getItem('refresh_token')
  },

  // 设置刷新令牌
  setRefreshToken(token: string): void {
    localStorage.setItem('refresh_token', token)
  },

  // 清除所有令牌
  clearTokens(): void {
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
  },

  // 检查令牌是否过期
  isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.exp * 1000 < Date.now()
    } catch {
      return true
    }
  },

  // 获取令牌过期时间
  getTokenExpiration(token: string): number | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.exp * 1000
    } catch {
      return null
    }
  },

  // 获取令牌剩余时间（毫秒）
  getTokenTimeRemaining(token: string): number {
    const expiration = this.getTokenExpiration(token)
    return expiration ? Math.max(0, expiration - Date.now()) : 0
  }
}