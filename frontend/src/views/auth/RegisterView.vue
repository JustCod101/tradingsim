<template>
  <div class="register-view">
    <div class="register-container">
      <div class="register-header">
        <div class="logo">
          <el-icon :size="40"><TrendCharts /></el-icon>
          <h1>TradingSim</h1>
        </div>
        <p class="subtitle">创建账户，开始您的交易学习之旅</p>
      </div>

      <el-steps :active="currentStep" align-center class="register-steps">
        <el-step title="基本信息" />
        <el-step title="账户设置" />
        <el-step title="完成注册" />
      </el-steps>

      <!-- 第一步：基本信息 -->
      <div v-show="currentStep === 0" class="step-content">
        <el-form
          ref="basicFormRef"
          :model="registerForm.basic"
          :rules="basicRules"
          class="register-form"
        >
          <el-form-item prop="email">
            <el-input
              v-model="registerForm.basic.email"
              placeholder="邮箱地址"
              size="large"
              :prefix-icon="Message"
              clearable
            />
          </el-form-item>

          <el-form-item prop="username">
            <el-input
              v-model="registerForm.basic.username"
              placeholder="用户名"
              size="large"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="realName">
            <el-input
              v-model="registerForm.basic.realName"
              placeholder="真实姓名"
              size="large"
              :prefix-icon="UserFilled"
              clearable
            />
          </el-form-item>

          <el-form-item prop="phone">
            <el-input
              v-model="registerForm.basic.phone"
              placeholder="手机号码"
              size="large"
              :prefix-icon="Phone"
              clearable
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 第二步：账户设置 -->
      <div v-show="currentStep === 1" class="step-content">
        <el-form
          ref="accountFormRef"
          :model="registerForm.account"
          :rules="accountRules"
          class="register-form"
        >
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.account.password"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.account.confirmPassword"
              type="password"
              placeholder="确认密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item prop="verificationCode">
            <div class="verification-input">
              <el-input
                v-model="registerForm.account.verificationCode"
                placeholder="邮箱验证码"
                size="large"
                :prefix-icon="Key"
                clearable
              />
              <el-button
                :disabled="codeSending || countdown > 0"
                @click="sendVerificationCode"
                class="code-button"
              >
                {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item prop="agreement">
            <el-checkbox v-model="registerForm.account.agreement">
              我已阅读并同意
              <el-link type="primary" @click="showTerms = true">《用户协议》</el-link>
              和
              <el-link type="primary" @click="showPrivacy = true">《隐私政策》</el-link>
            </el-checkbox>
          </el-form-item>
        </el-form>
      </div>

      <!-- 第三步：完成注册 -->
      <div v-show="currentStep === 2" class="step-content success-content">
        <el-result
          icon="success"
          title="注册成功！"
          sub-title="欢迎加入 TradingSim，您的交易学习之旅即将开始"
        >
          <template #extra>
            <el-button type="primary" @click="goToLogin">立即登录</el-button>
            <el-button @click="goToHome">返回首页</el-button>
          </template>
        </el-result>
      </div>

      <!-- 操作按钮 -->
      <div v-show="currentStep < 2" class="form-actions">
        <el-button
          v-if="currentStep > 0"
          @click="prevStep"
          size="large"
          class="action-button"
        >
          上一步
        </el-button>
        <el-button
          v-if="currentStep < 1"
          type="primary"
          @click="nextStep"
          size="large"
          class="action-button"
        >
          下一步
        </el-button>
        <el-button
          v-if="currentStep === 1"
          type="primary"
          :loading="registering"
          @click="handleRegister"
          size="large"
          class="action-button"
        >
          完成注册
        </el-button>
      </div>

      <div v-show="currentStep < 2" class="login-link">
        已有账户？
        <router-link to="/login" class="link">立即登录</router-link>
      </div>
    </div>

    <!-- 用户协议对话框 -->
    <el-dialog v-model="showTerms" title="用户协议" width="600px">
      <div class="terms-content">
        <h3>1. 服务条款</h3>
        <p>欢迎使用 TradingSim 交易模拟平台。通过注册和使用本服务，您同意遵守以下条款和条件。</p>
        
        <h3>2. 用户责任</h3>
        <p>用户应当提供真实、准确的注册信息，并对账户安全负责。禁止使用本平台进行任何违法活动。</p>
        
        <h3>3. 服务内容</h3>
        <p>本平台提供股票交易模拟服务，仅供学习和娱乐使用，不构成任何投资建议。</p>
        
        <h3>4. 免责声明</h3>
        <p>平台不对用户的投资决策承担任何责任，所有交易均为模拟性质。</p>
      </div>
      <template #footer>
        <el-button @click="showTerms = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 隐私政策对话框 -->
    <el-dialog v-model="showPrivacy" title="隐私政策" width="600px">
      <div class="privacy-content">
        <h3>1. 信息收集</h3>
        <p>我们收集您提供的注册信息和使用平台时产生的数据，用于提供更好的服务。</p>
        
        <h3>2. 信息使用</h3>
        <p>收集的信息仅用于平台运营、用户服务和产品改进，不会用于其他商业目的。</p>
        
        <h3>3. 信息保护</h3>
        <p>我们采用行业标准的安全措施保护您的个人信息，防止未经授权的访问和使用。</p>
        
        <h3>4. 信息共享</h3>
        <p>除法律要求外，我们不会与第三方共享您的个人信息。</p>
      </div>
      <template #footer>
        <el-button @click="showPrivacy = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import {
  TrendCharts,
  Message,
  User,
  UserFilled,
  Phone,
  Lock,
  Key
} from '@element-plus/icons-vue'

// 响应式数据
const router = useRouter()
const currentStep = ref(0)
const registering = ref(false)
const codeSending = ref(false)
const countdown = ref(0)
const showTerms = ref(false)
const showPrivacy = ref(false)

const basicFormRef = ref<InstanceType<typeof ElForm>>()
const accountFormRef = ref<InstanceType<typeof ElForm>>()

// 注册表单数据
const registerForm = reactive({
  basic: {
    email: '',
    username: '',
    realName: '',
    phone: ''
  },
  account: {
    password: '',
    confirmPassword: '',
    verificationCode: '',
    agreement: false
  }
})

// 验证规则
const basicRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const accountRules = computed(() => ({
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 20, message: '长度在 8 到 20 个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, message: '密码必须包含大小写字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== registerForm.account.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  agreement: [
    {
      validator: (rule: any, value: boolean, callback: Function) => {
        if (!value) {
          callback(new Error('请阅读并同意用户协议和隐私政策'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}))

// 方法
async function nextStep() {
  if (currentStep.value === 0) {
    if (!basicFormRef.value) return
    
    try {
      const valid = await basicFormRef.value.validate()
      if (valid) {
        currentStep.value++
      }
    } catch (error) {
      console.error('Validation failed:', error)
    }
  }
}

function prevStep() {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

async function sendVerificationCode() {
  if (!registerForm.basic.email) {
    ElMessage.warning('请先输入邮箱地址')
    return
  }
  
  try {
    codeSending.value = true
    
    // 模拟发送验证码
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('验证码已发送到您的邮箱')
    
    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
    
  } catch (error) {
    ElMessage.error('发送验证码失败，请稍后重试')
  } finally {
    codeSending.value = false
  }
}

async function handleRegister() {
  if (!accountFormRef.value) return
  
  try {
    const valid = await accountFormRef.value.validate()
    if (!valid) return
    
    registering.value = true
    
    // 模拟注册API调用
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    ElMessage.success('注册成功！')
    currentStep.value = 2
    
  } catch (error) {
    console.error('Registration failed:', error)
    ElMessage.error('注册失败，请稍后重试')
  } finally {
    registering.value = false
  }
}

function goToLogin() {
  router.push('/login')
}

function goToHome() {
  router.push('/')
}
</script>

<style scoped>
.register-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 500px;
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.register-header {
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

.register-steps {
  margin-bottom: 30px;
}

.step-content {
  min-height: 300px;
  margin-bottom: 20px;
}

.success-content {
  min-height: auto;
}

.register-form {
  margin-bottom: 20px;
}

.verification-input {
  display: flex;
  gap: 10px;
}

.verification-input .el-input {
  flex: 1;
}

.code-button {
  white-space: nowrap;
  min-width: 100px;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.action-button {
  flex: 1;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.login-link {
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

.terms-content,
.privacy-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
}

.terms-content h3,
.privacy-content h3 {
  color: var(--el-color-primary);
  margin-top: 20px;
  margin-bottom: 10px;
}

.terms-content p,
.privacy-content p {
  line-height: 1.6;
  margin-bottom: 15px;
  color: var(--el-text-color-regular);
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

:deep(.el-steps) {
  margin-bottom: 30px;
}

:deep(.el-checkbox__label) {
  font-size: 14px;
  line-height: 1.5;
}
</style>