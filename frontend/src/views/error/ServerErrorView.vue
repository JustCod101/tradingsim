<template>
  <div class="server-error-view">
    <div class="error-container">
      <div class="error-illustration">
        <div class="error-code">500</div>
        <div class="error-icon">
          <el-icon :size="120"><Warning /></el-icon>
        </div>
      </div>
      
      <div class="error-content">
        <h1 class="error-title">服务器错误</h1>
        <p class="error-description">
          抱歉，服务器遇到了一些问题，暂时无法处理您的请求。
          <br>
          我们的技术团队已经收到通知，正在紧急修复中。
        </p>
        
        <div class="error-details" v-if="showDetails">
          <el-collapse>
            <el-collapse-item title="错误详情" name="details">
              <div class="error-info">
                <p><strong>错误时间：</strong>{{ errorTime }}</p>
                <p><strong>错误ID：</strong>{{ errorId }}</p>
                <p><strong>请求路径：</strong>{{ requestPath }}</p>
                <p v-if="errorMessage"><strong>错误信息：</strong>{{ errorMessage }}</p>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
        
        <div class="error-actions">
          <el-button type="primary" size="large" @click="retry">
            <el-icon><Refresh /></el-icon>
            重试
          </el-button>
          <el-button size="large" @click="goHome">
            <el-icon><House /></el-icon>
            返回首页
          </el-button>
          <el-button size="large" @click="reportError">
            <el-icon><ChatDotRound /></el-icon>
            报告问题
          </el-button>
        </div>
        
        <div class="error-suggestions">
          <h3>您可以尝试：</h3>
          <ul>
            <li>
              <el-icon><Refresh /></el-icon>
              刷新页面重新加载
            </li>
            <li>
              <el-icon><Clock /></el-icon>
              稍后再试，问题可能是临时的
            </li>
            <li>
              <el-icon><Monitor /></el-icon>
              检查网络连接是否正常
            </li>
            <li>
              <el-icon><Service /></el-icon>
              联系技术支持获取帮助
            </li>
          </ul>
        </div>
        
        <div class="status-info">
          <el-button text @click="checkStatus">
            <el-icon><InfoFilled /></el-icon>
            查看系统状态
          </el-button>
          <el-button text @click="toggleDetails">
            <el-icon><View /></el-icon>
            {{ showDetails ? '隐藏' : '显示' }}技术详情
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 报告问题对话框 -->
    <el-dialog v-model="showReportDialog" title="报告问题" width="500px">
      <el-form :model="reportForm" label-width="80px">
        <el-form-item label="问题描述">
          <el-input
            v-model="reportForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您遇到的问题..."
          />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input
            v-model="reportForm.email"
            placeholder="用于接收问题处理进度"
          />
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-radio-group v-model="reportForm.priority">
            <el-radio label="low">一般</el-radio>
            <el-radio label="medium">重要</el-radio>
            <el-radio label="high">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReportDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReport" :loading="submitting">
          提交报告
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 系统状态对话框 -->
    <el-dialog v-model="showStatusDialog" title="系统状态" width="600px">
      <div class="status-dashboard">
        <div class="status-item" v-for="service in systemStatus" :key="service.name">
          <div class="service-info">
            <div class="service-name">{{ service.name }}</div>
            <div class="service-desc">{{ service.description }}</div>
          </div>
          <div class="service-status">
            <el-tag
              :type="service.status === 'operational' ? 'success' : 
                    service.status === 'degraded' ? 'warning' : 'danger'"
            >
              {{ getStatusText(service.status) }}
            </el-tag>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showStatusDialog = false">关闭</el-button>
        <el-button type="primary" @click="refreshStatus">刷新状态</el-button>
      </template>
    </el-dialog>
    
    <div class="floating-elements">
      <div class="floating-element" v-for="i in 5" :key="i" :style="getFloatingStyle(i)">
        <el-icon><Warning /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Warning,
  Refresh,
  House,
  ChatDotRound,
  Clock,
  Monitor,
  Service,
  InfoFilled,
  View
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 响应式数据
const showDetails = ref(false)
const showReportDialog = ref(false)
const showStatusDialog = ref(false)
const submitting = ref(false)

// 错误信息
const errorTime = computed(() => new Date().toLocaleString())
const errorId = computed(() => `ERR-${Date.now().toString(36).toUpperCase()}`)
const requestPath = computed(() => route.fullPath)
const errorMessage = computed(() => route.query.message as string || '')

// 报告表单
const reportForm = reactive({
  description: '',
  email: '',
  priority: 'medium'
})

// 系统状态
const systemStatus = ref([
  {
    name: 'API 服务',
    description: '核心业务接口',
    status: 'outage'
  },
  {
    name: 'WebSocket 服务',
    description: '实时数据推送',
    status: 'degraded'
  },
  {
    name: '数据库',
    description: '数据存储服务',
    status: 'operational'
  },
  {
    name: '文件存储',
    description: '静态资源服务',
    status: 'operational'
  },
  {
    name: '缓存服务',
    description: 'Redis 缓存',
    status: 'operational'
  }
])

// 生命周期
onMounted(() => {
  // 自动报告错误（如果有错误信息）
  if (errorMessage.value) {
    console.error('Server Error:', {
      path: requestPath.value,
      message: errorMessage.value,
      time: errorTime.value,
      id: errorId.value
    })
  }
})

// 方法
function retry() {
  window.location.reload()
}

function goHome() {
  router.push('/')
}

function reportError() {
  reportForm.description = `在访问 ${requestPath.value} 时遇到服务器错误`
  if (errorMessage.value) {
    reportForm.description += `\n错误信息：${errorMessage.value}`
  }
  showReportDialog.value = true
}

function toggleDetails() {
  showDetails.value = !showDetails.value
}

function checkStatus() {
  showStatusDialog.value = true
}

async function submitReport() {
  if (!reportForm.description.trim()) {
    ElMessage.warning('请描述您遇到的问题')
    return
  }
  
  submitting.value = true
  
  try {
    // 模拟提交报告
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    ElMessage.success('问题报告已提交，我们会尽快处理')
    showReportDialog.value = false
    
    // 重置表单
    Object.assign(reportForm, {
      description: '',
      email: '',
      priority: 'medium'
    })
  } catch (error) {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

async function refreshStatus() {
  try {
    // 模拟刷新状态
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 随机更新状态
    systemStatus.value.forEach(service => {
      const statuses = ['operational', 'degraded', 'outage']
      const randomStatus = statuses[Math.floor(Math.random() * statuses.length)]
      service.status = randomStatus
    })
    
    ElMessage.success('状态已更新')
  } catch (error) {
    ElMessage.error('刷新失败')
  }
}

function getStatusText(status: string): string {
  const statusMap: Record<string, string> = {
    operational: '正常',
    degraded: '降级',
    outage: '故障'
  }
  return statusMap[status] || '未知'
}

function getFloatingStyle(index: number) {
  const positions = [
    { top: '15%', left: '10%', animationDelay: '0s' },
    { top: '25%', right: '15%', animationDelay: '1.5s' },
    { top: '65%', left: '8%', animationDelay: '3s' },
    { bottom: '25%', right: '12%', animationDelay: '4.5s' },
    { bottom: '15%', left: '25%', animationDelay: '6s' }
  ]
  
  return positions[index - 1] || {}
}
</script>

<style scoped>
.server-error-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.error-container {
  max-width: 800px;
  width: 100%;
  background: white;
  border-radius: 20px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 2;
}

.error-illustration {
  position: relative;
  margin-bottom: 40px;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #ff6b6b;
  line-height: 1;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.error-icon {
  color: var(--el-text-color-secondary);
  opacity: 0.6;
}

.error-content {
  max-width: 600px;
  margin: 0 auto;
}

.error-title {
  font-size: 32px;
  color: var(--el-text-color-primary);
  margin-bottom: 20px;
  font-weight: 600;
}

.error-description {
  font-size: 16px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
  margin-bottom: 30px;
}

.error-details {
  margin-bottom: 30px;
  text-align: left;
}

.error-info p {
  margin-bottom: 8px;
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.error-actions .el-button {
  border-radius: 25px;
  padding: 12px 24px;
  font-weight: 500;
}

.error-suggestions {
  text-align: left;
  max-width: 400px;
  margin: 0 auto 30px;
}

.error-suggestions h3 {
  color: var(--el-text-color-primary);
  margin-bottom: 20px;
  font-size: 18px;
  text-align: center;
}

.error-suggestions ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.error-suggestions li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.status-info {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 20px;
}

.status-dashboard {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.service-info {
  text-align: left;
}

.service-name {
  font-weight: bold;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.service-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.floating-elements {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
}

.floating-element {
  position: absolute;
  color: rgba(255, 255, 255, 0.3);
  animation: float 8s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-30px) rotate(180deg);
  }
}

@media (max-width: 768px) {
  .error-container {
    padding: 40px 20px;
  }
  
  .error-code {
    font-size: 80px;
  }
  
  .error-title {
    font-size: 24px;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .error-actions .el-button {
    width: 200px;
  }
  
  .status-info {
    flex-direction: column;
    gap: 10px;
  }
}
</style>