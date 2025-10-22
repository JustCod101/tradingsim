<template>
  <div class="login-view">
    <div class="login-container">
      <div class="login-header">
        <div class="logo">
          <el-icon :size="40"><TrendCharts /></el-icon>
          <h1>TradingSim</h1>
        </div>
        <p class="subtitle">欢迎回来，开始您的交易之旅</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名或邮箱"
            size="large"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <div class="login-options">
            <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
            <el-link type="primary" @click="showForgotPassword = true">
              忘记密码？
            </el-link>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-button"
          >
            登录
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="register-link">
            还没有账户？
            <router-link to="/register" class="link">立即注册</router-link>
          </div>
        </el-form-item>
      </el-form>

      <div class="divider">
        <span>或者</span>
      </div>

      <div class="social-login">
        <el-button class="social-button github" @click="loginWithGitHub">
          <el-icon><svg viewBox="0 0 1024 1024"><path d="M512 12.64c-282.752 0-512 229.216-512 512 0 226.208 146.688 418.144 350.08 485.824 25.6 4.736 35.008-11.104 35.008-24.64 0-12.192-0.48-52.544-0.704-95.328-142.464 30.976-172.512-60.416-172.512-60.416-23.296-59.168-56.832-74.912-56.832-74.912-46.464-31.776 3.52-31.136 3.52-31.136 51.392 3.616 78.464 52.768 78.464 52.768 45.664 78.272 119.776 55.648 148.992 42.56 4.576-33.088 17.856-55.68 32.512-68.48-113.728-12.928-233.28-56.864-233.28-253.024 0-55.904 19.936-101.568 52.672-137.408-5.312-12.896-22.848-64.96 4.96-135.488 0 0 42.88-13.76 140.8 52.48 40.832-11.36 84.64-17.024 128.16-17.248 43.488 0.192 87.328 5.888 128.256 17.248 97.728-66.24 140.64-52.48 140.64-52.48 27.872 70.528 10.336 122.592 5.024 135.488 32.832 35.84 52.608 81.504 52.608 137.408 0 196.64-119.776 239.936-233.792 252.64 18.368 15.904 34.72 47.04 34.72 94.816 0 68.512-0.608 123.648-0.608 140.512 0 13.632 9.216 29.6 35.168 24.576C877.472 942.08 1024 750.208 1024 524.64c0-282.784-229.248-512-512-512z"/></svg></el-icon>
          GitHub 登录
        </el-button>
        
        <el-button class="social-button google" @click="loginWithGoogle">
          <el-icon><svg viewBox="0 0 1024 1024"><path d="M881 442.4H519.7v148.5h206.4c-8.9 48-35.9 88.6-76.6 115.8-34.4 23-78.3 36.6-129.9 36.6-99.9 0-184.4-67.5-214.6-158.2-7.6-23-12-47.6-12-72.9s4.4-49.9 12-72.9c30.3-90.6 114.8-158.1 214.6-158.1 56.3 0 106.8 19.4 146.6 57.4l110-110.1c-66.5-62-153.2-100-256.6-100-149.9 0-279.6 86.8-342.7 213.1C59.2 307.5 32 364.8 32 426.6v171.8c0 61.8 27.2 119.1 74.6 157.2 63.1 126.3 192.8 213.1 342.7 213.1 103.4 0 190.1-38 256.6-100l-110-110.1c-39.8 38-90.3 57.4-146.6 57.4-51.6 0-95.5-13.6-129.9-36.6-40.7-27.2-67.7-67.8-76.6-115.8h206.4V442.4z"/></svg></el-icon>
          Google 登录
        </el-button>
      </div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog v-model="showForgotPassword" title="重置密码" width="400px">
      <el-form :model="forgotPasswordForm" label-width="80px">
        <el-form-item label="邮箱">
          <el-input
            v-model="forgotPasswordForm.email"
            placeholder="请输入注册邮箱"
            type="email"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForgotPassword = false">取消</el-button>
        <el-button type="primary" @click="handleForgotPassword">发送重置邮件</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import { User, Lock, TrendCharts } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { authApi } from '@/services/auth'

// 响应式数据
const router = useRouter()
const appStore = useAppStore()
const loginFormRef = ref<InstanceType<typeof ElForm>>()
const loading = ref(false)
const showForgotPassword = ref(false)

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 忘记密码表单
const forgotPasswordForm = reactive({
  email: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 生命周期
onMounted(() => {
  // 检查是否已经登录
  if (appStore.isAuthenticated) {
    router.push('/')
  }
  
  // 自动填充记住的用户名
  const rememberedUsername = localStorage.getItem('remembered-username')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    loginForm.rememberMe = true
  }
})

// 方法
async function handleLogin() {
  if (!loginFormRef.value) return
  
  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return
    
    loading.value = true
    
    // 调用真实的登录API
      const response = await authApi.login({
        usernameOrEmail: loginForm.username,
        password: loginForm.password,
        rememberMe: loginForm.rememberMe
      })
    
    if (response.success) {
      // 记住用户名
      if (loginForm.rememberMe) {
        localStorage.setItem('remembered-username', loginForm.username)
      } else {
        localStorage.removeItem('remembered-username')
      }
      
      // 设置用户信息
      const { user, token, refreshToken } = response.data
      console.log('Login success, setting user:', user)
      appStore.setCurrentUser(user)
      
      // 保存token
      localStorage.setItem('auth-token', token)
      if (refreshToken) {
        localStorage.setItem('refresh-token', refreshToken)
      }
      
      console.log('Auth state after login:', {
        isAuthenticated: appStore.isAuthenticated,
        currentUser: appStore.currentUser
      })
      
      ElMessage.success('登录成功')
      
      // 等待状态更新后再跳转
      await nextTick()
      
      // 跳转逻辑：优先级为 query参数 > sessionStorage > 游戏页面
      const queryRedirect = router.currentRoute.value.query.redirect as string
      const sessionRedirect = sessionStorage.getItem('redirect-after-login')
      const targetPath = queryRedirect || sessionRedirect || '/game'
      
      // 清除保存的重定向路径
      sessionStorage.removeItem('redirect-after-login')
      
      console.log('Redirecting to:', targetPath)
      await router.push(targetPath)
    } else {
      ElMessage.error(response.message || '登录失败')
    }
    
  } catch (error) {
    console.error('Login failed:', error)
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

async function loginWithGitHub() {
  ElMessage.info('GitHub 登录功能开发中...')
}

async function loginWithGoogle() {
  ElMessage.info('Google 登录功能开发中...')
}

async function handleForgotPassword() {
  if (!forgotPasswordForm.email) {
    ElMessage.warning('请输入邮箱地址')
    return
  }
  
  try {
    // 模拟发送重置邮件
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('重置邮件已发送，请查收')
    showForgotPassword.value = false
    forgotPasswordForm.email = ''
  } catch (error) {
    ElMessage.error('发送失败，请稍后重试')
  }
}
</script>

<style scoped>
.login-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 10px;
}

.logo h1 {
  margin: 0;
  color: var(--el-color-primary);
  font-size: 28px;
  font-weight: bold;
}

.subtitle {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.login-form {
  margin-bottom: 20px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.register-link {
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.link {
  color: var(--el-color-primary);
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}

.divider {
  position: relative;
  text-align: center;
  margin: 20px 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--el-border-color);
  z-index: 1;
}

.divider span {
  background: white;
  padding: 0 15px;
  position: relative;
  z-index: 2;
}

.social-login {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.social-button {
  width: 100%;
  height: 44px;
  border: 1px solid var(--el-border-color);
  background: white;
  color: var(--el-text-color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  transition: all 0.3s;
}

.social-button:hover {
  background: var(--el-fill-color-light);
  border-color: var(--el-color-primary);
}

.social-button.github:hover {
  background: #24292e;
  color: white;
  border-color: #24292e;
}

.social-button.google:hover {
  background: #4285f4;
  color: white;
  border-color: #4285f4;
}

.social-button svg {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
}
</style>