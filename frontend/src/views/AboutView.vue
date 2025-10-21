<template>
  <div class="about-view">
    <div class="about-container">
      <!-- 头部横幅 -->
      <div class="hero-section">
        <div class="hero-content">
          <div class="hero-icon">
            <el-icon :size="80"><TrendCharts /></el-icon>
          </div>
          <h1 class="hero-title">交易模拟器</h1>
          <p class="hero-subtitle">专业的股票交易学习平台</p>
          <div class="version-info">
            <el-tag type="info">版本 {{ appVersion }}</el-tag>
            <el-tag type="success">{{ buildDate }}</el-tag>
          </div>
        </div>
        
        <div class="hero-stats">
          <div class="stat-item">
            <div class="stat-number">{{ userCount.toLocaleString() }}+</div>
            <div class="stat-label">注册用户</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ sessionCount.toLocaleString() }}+</div>
            <div class="stat-label">游戏会话</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ decisionCount.toLocaleString() }}+</div>
            <div class="stat-label">交易决策</div>
          </div>
        </div>
      </div>
      
      <!-- 产品介绍 -->
      <el-card class="section-card" shadow="hover">
        <template #header>
          <div class="section-header">
            <el-icon><InfoFilled /></el-icon>
            <span>产品介绍</span>
          </div>
        </template>
        
        <div class="product-intro">
          <p class="intro-text">
            交易模拟器是一个专业的股票交易学习平台，旨在帮助用户在无风险的环境中学习和练习股票交易技能。
            通过真实的市场数据和智能的决策分析，用户可以提升自己的交易水平，积累宝贵的投资经验。
          </p>
          
          <div class="features-grid">
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><DataAnalysis /></el-icon>
              </div>
              <h3>真实数据</h3>
              <p>基于真实历史股票数据，提供最接近实际交易的体验</p>
            </div>
            
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><Trophy /></el-icon>
              </div>
              <h3>智能评估</h3>
              <p>AI驱动的决策分析，实时评估交易表现和风险控制</p>
            </div>
            
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <h3>专业图表</h3>
              <p>丰富的技术指标和图表工具，支持多种分析方法</p>
            </div>
            
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><User /></el-icon>
              </div>
              <h3>个性化学习</h3>
              <p>根据用户水平调整难度，提供个性化的学习路径</p>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 技术栈 -->
      <el-card class="section-card" shadow="hover">
        <template #header>
          <div class="section-header">
            <el-icon><Setting /></el-icon>
            <span>技术栈</span>
          </div>
        </template>
        
        <div class="tech-stack">
          <div class="tech-category">
            <h3>前端技术</h3>
            <div class="tech-items">
              <el-tag v-for="tech in frontendTech" :key="tech.name" :type="tech.type">
                {{ tech.name }} {{ tech.version }}
              </el-tag>
            </div>
          </div>
          
          <div class="tech-category">
            <h3>后端技术</h3>
            <div class="tech-items">
              <el-tag v-for="tech in backendTech" :key="tech.name" :type="tech.type">
                {{ tech.name }} {{ tech.version }}
              </el-tag>
            </div>
          </div>
          
          <div class="tech-category">
            <h3>数据库与存储</h3>
            <div class="tech-items">
              <el-tag v-for="tech in databaseTech" :key="tech.name" :type="tech.type">
                {{ tech.name }} {{ tech.version }}
              </el-tag>
            </div>
          </div>
          
          <div class="tech-category">
            <h3>部署与监控</h3>
            <div class="tech-items">
              <el-tag v-for="tech in deployTech" :key="tech.name" :type="tech.type">
                {{ tech.name }} {{ tech.version }}
              </el-tag>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 开发团队 -->
      <el-card class="section-card" shadow="hover">
        <template #header>
          <div class="section-header">
            <el-icon><UserFilled /></el-icon>
            <span>开发团队</span>
          </div>
        </template>
        
        <div class="team-section">
          <div class="team-grid">
            <div v-for="member in teamMembers" :key="member.name" class="team-member">
              <div class="member-avatar">
                <el-avatar :size="80" :src="member.avatar">
                  {{ member.name.charAt(0) }}
                </el-avatar>
              </div>
              <div class="member-info">
                <h4>{{ member.name }}</h4>
                <p class="member-role">{{ member.role }}</p>
                <p class="member-desc">{{ member.description }}</p>
                <div class="member-links">
                  <el-button
                    v-if="member.github"
                    size="small"
                    text
                    @click="openLink(member.github)"
                  >
                    <el-icon><Link /></el-icon>
                    GitHub
                  </el-button>
                  <el-button
                    v-if="member.email"
                    size="small"
                    text
                    @click="sendEmail(member.email)"
                  >
                    <el-icon><Message /></el-icon>
                    邮箱
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 更新日志 -->
      <el-card class="section-card" shadow="hover">
        <template #header>
          <div class="section-header">
            <el-icon><Document /></el-icon>
            <span>更新日志</span>
          </div>
        </template>
        
        <div class="changelog">
          <el-timeline>
            <el-timeline-item
              v-for="(log, index) in changelog"
              :key="index"
              :timestamp="log.date"
              :type="log.type"
            >
              <el-card>
                <h4>{{ log.version }}</h4>
                <ul>
                  <li v-for="(change, idx) in log.changes" :key="idx">
                    {{ change }}
                  </li>
                </ul>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-card>
      
      <!-- 联系我们 -->
      <el-card class="section-card" shadow="hover">
        <template #header>
          <div class="section-header">
            <el-icon><ChatDotRound /></el-icon>
            <span>联系我们</span>
          </div>
        </template>
        
        <div class="contact-section">
          <div class="contact-grid">
            <div class="contact-item">
              <div class="contact-icon">
                <el-icon><Message /></el-icon>
              </div>
              <div class="contact-info">
                <h4>邮箱支持</h4>
                <p>support@tradingsim.com</p>
                <el-button size="small" @click="sendEmail('support@tradingsim.com')">
                  发送邮件
                </el-button>
              </div>
            </div>
            
            <div class="contact-item">
              <div class="contact-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="contact-info">
                <h4>在线客服</h4>
                <p>工作日 9:00-18:00</p>
                <el-button size="small" @click="openChat">
                  开始对话
                </el-button>
              </div>
            </div>
            
            <div class="contact-item">
              <div class="contact-icon">
                <el-icon><Link /></el-icon>
              </div>
              <div class="contact-info">
                <h4>GitHub</h4>
                <p>github.com/tradingsim</p>
                <el-button size="small" @click="openLink('https://github.com/tradingsim')">
                  访问仓库
                </el-button>
              </div>
            </div>
          </div>
          
          <div class="feedback-section">
            <h3>意见反馈</h3>
            <el-form :model="feedbackForm" label-width="80px">
              <el-form-item label="反馈类型">
                <el-select v-model="feedbackForm.type" placeholder="请选择反馈类型">
                  <el-option label="功能建议" value="feature" />
                  <el-option label="问题报告" value="bug" />
                  <el-option label="使用咨询" value="question" />
                  <el-option label="其他" value="other" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="反馈内容">
                <el-input
                  v-model="feedbackForm.content"
                  type="textarea"
                  :rows="4"
                  placeholder="请详细描述您的反馈..."
                />
              </el-form-item>
              
              <el-form-item label="联系邮箱">
                <el-input
                  v-model="feedbackForm.email"
                  placeholder="用于接收回复（可选）"
                />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="submitFeedback" :loading="submitting">
                  提交反馈
                </el-button>
                <el-button @click="resetFeedback">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-card>
      
      <!-- 法律信息 -->
      <el-card class="section-card" shadow="hover">
        <template #header>
          <div class="section-header">
            <el-icon><Document /></el-icon>
            <span>法律信息</span>
          </div>
        </template>
        
        <div class="legal-section">
          <div class="legal-grid">
            <div class="legal-item">
              <h4>免责声明</h4>
              <p>
                本平台仅供学习和教育目的使用，所有交易均为模拟交易，不涉及真实资金。
                平台提供的数据和分析仅供参考，不构成投资建议。
              </p>
            </div>
            
            <div class="legal-item">
              <h4>隐私政策</h4>
              <p>
                我们重视用户隐私，严格按照相关法律法规保护用户个人信息。
                详细的隐私政策请查看我们的隐私条款。
              </p>
              <el-button size="small" text @click="showPrivacyPolicy">
                查看详情
              </el-button>
            </div>
            
            <div class="legal-item">
              <h4>服务条款</h4>
              <p>
                使用本平台即表示您同意遵守我们的服务条款。
                请仔细阅读相关条款以了解您的权利和义务。
              </p>
              <el-button size="small" text @click="showTermsOfService">
                查看详情
              </el-button>
            </div>
          </div>
          
          <div class="copyright">
            <p>&copy; {{ currentYear }} 交易模拟器. 保留所有权利.</p>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  TrendCharts,
  InfoFilled,
  DataAnalysis,
  Trophy,
  User,
  Setting,
  UserFilled,
  Document,
  ChatDotRound,
  Message,
  Link
} from '@element-plus/icons-vue'

// 响应式数据
const submitting = ref(false)

// 应用信息
const appVersion = ref('1.0.0')
const buildDate = ref('2024-01-15')
const userCount = ref(12580)
const sessionCount = ref(45230)
const decisionCount = ref(892340)

// 反馈表单
const feedbackForm = reactive({
  type: '',
  content: '',
  email: ''
})

// 技术栈数据
const frontendTech = ref([
  { name: 'Vue.js', version: '3.4', type: 'success' },
  { name: 'TypeScript', version: '5.0', type: 'primary' },
  { name: 'Element Plus', version: '2.4', type: 'info' },
  { name: 'ECharts', version: '5.4', type: 'warning' },
  { name: 'Vite', version: '5.0', type: 'success' }
])

const backendTech = ref([
  { name: 'Spring Boot', version: '3.2', type: 'success' },
  { name: 'Java', version: '21', type: 'primary' },
  { name: 'Spring Security', version: '6.2', type: 'info' },
  { name: 'WebSocket', version: 'STOMP', type: 'warning' }
])

const databaseTech = ref([
  { name: 'PostgreSQL', version: '16', type: 'primary' },
  { name: 'Redis', version: '7.2', type: 'danger' },
  { name: 'InfluxDB', version: '2.7', type: 'info' }
])

const deployTech = ref([
  { name: 'Docker', version: '24.0', type: 'primary' },
  { name: 'Kubernetes', version: '1.28', type: 'info' },
  { name: 'Grafana', version: '10.2', type: 'warning' },
  { name: 'Prometheus', version: '2.47', type: 'success' }
])

// 团队成员
const teamMembers = ref([
  {
    name: '张三',
    role: '项目负责人',
    description: '负责项目整体架构设计和技术选型',
    avatar: '',
    github: 'https://github.com/zhangsan',
    email: 'zhangsan@tradingsim.com'
  },
  {
    name: '李四',
    role: '前端开发工程师',
    description: '负责前端界面设计和用户体验优化',
    avatar: '',
    github: 'https://github.com/lisi',
    email: 'lisi@tradingsim.com'
  },
  {
    name: '王五',
    role: '后端开发工程师',
    description: '负责后端API开发和数据库设计',
    avatar: '',
    github: 'https://github.com/wangwu',
    email: 'wangwu@tradingsim.com'
  },
  {
    name: '赵六',
    role: '数据分析师',
    description: '负责交易算法设计和数据分析',
    avatar: '',
    github: 'https://github.com/zhaoliu',
    email: 'zhaoliu@tradingsim.com'
  }
])

// 更新日志
const changelog = ref([
  {
    version: 'v1.0.0',
    date: '2024-01-15',
    type: 'primary',
    changes: [
      '正式发布交易模拟器平台',
      '支持股票交易模拟功能',
      '实现用户注册和登录系统',
      '添加实时数据展示和图表分析',
      '完成排行榜和历史记录功能'
    ]
  },
  {
    version: 'v0.9.0',
    date: '2024-01-01',
    type: 'warning',
    changes: [
      '完成Beta版本测试',
      '优化用户界面和交互体验',
      '修复已知问题和性能优化',
      '添加多语言支持',
      '完善文档和帮助系统'
    ]
  },
  {
    version: 'v0.8.0',
    date: '2023-12-15',
    type: 'info',
    changes: [
      '实现核心交易逻辑',
      '添加WebSocket实时通信',
      '完成数据库设计和API开发',
      '实现用户认证和权限管理',
      '添加基础的前端界面'
    ]
  }
])

// 计算属性
const currentYear = computed(() => new Date().getFullYear())

// 方法
function openLink(url: string) {
  window.open(url, '_blank')
}

function sendEmail(email: string) {
  window.location.href = `mailto:${email}`
}

function openChat() {
  ElMessage.info('在线客服功能开发中...')
}

async function submitFeedback() {
  if (!feedbackForm.content.trim()) {
    ElMessage.warning('请填写反馈内容')
    return
  }
  
  submitting.value = true
  
  try {
    // 模拟提交反馈
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    ElMessage.success('反馈提交成功，感谢您的建议！')
    resetFeedback()
  } catch (error) {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

function resetFeedback() {
  Object.assign(feedbackForm, {
    type: '',
    content: '',
    email: ''
  })
}

function showPrivacyPolicy() {
  ElMessage.info('隐私政策页面开发中...')
}

function showTermsOfService() {
  ElMessage.info('服务条款页面开发中...')
}
</script>

<style scoped>
.about-view {
  min-height: 100vh;
  background: var(--el-bg-color-page);
  padding: 20px;
}

.about-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.hero-section {
  background: linear-gradient(135deg, var(--el-color-primary) 0%, var(--el-color-primary-light-3) 100%);
  border-radius: 20px;
  padding: 60px 40px;
  color: white;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grid" width="10" height="10" patternUnits="userSpaceOnUse"><path d="M 10 0 L 0 0 0 10" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="1"/></pattern></defs><rect width="100" height="100" fill="url(%23grid)"/></svg>');
  animation: float 20s ease-in-out infinite;
}

.hero-content {
  position: relative;
  z-index: 2;
  margin-bottom: 40px;
}

.hero-icon {
  margin-bottom: 20px;
  opacity: 0.9;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 15px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.hero-subtitle {
  font-size: 20px;
  margin: 0 0 25px 0;
  opacity: 0.9;
}

.version-info {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
  position: relative;
  z-index: 2;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-label {
  font-size: 16px;
  opacity: 0.9;
}

.section-card {
  border-radius: 15px;
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
}

.product-intro {
  padding: 20px 0;
}

.intro-text {
  font-size: 16px;
  line-height: 1.8;
  color: var(--el-text-color-regular);
  margin-bottom: 40px;
  text-align: center;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.feature-item {
  text-align: center;
  padding: 30px 20px;
  background: var(--el-fill-color-lighter);
  border-radius: 12px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 48px;
  color: var(--el-color-primary);
  margin-bottom: 20px;
}

.feature-item h3 {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 15px 0;
  color: var(--el-text-color-primary);
}

.feature-item p {
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
  margin: 0;
}

.tech-stack {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.tech-category h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 15px 0;
  color: var(--el-text-color-primary);
}

.tech-items {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
}

.team-member {
  text-align: center;
  padding: 30px 20px;
  background: var(--el-fill-color-lighter);
  border-radius: 12px;
  transition: transform 0.3s ease;
}

.team-member:hover {
  transform: translateY(-3px);
}

.member-avatar {
  margin-bottom: 20px;
}

.member-info h4 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--el-text-color-primary);
}

.member-role {
  font-size: 14px;
  color: var(--el-color-primary);
  margin: 0 0 12px 0;
  font-weight: 500;
}

.member-desc {
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
  margin: 0 0 15px 0;
}

.member-links {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.changelog {
  max-height: 600px;
  overflow-y: auto;
}

.contact-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
  margin-bottom: 40px;
}

.contact-item {
  display: flex;
  gap: 20px;
  padding: 25px;
  background: var(--el-fill-color-lighter);
  border-radius: 12px;
}

.contact-icon {
  font-size: 32px;
  color: var(--el-color-primary);
  flex-shrink: 0;
}

.contact-info h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--el-text-color-primary);
}

.contact-info p {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin: 0 0 15px 0;
}

.feedback-section {
  padding: 30px;
  background: var(--el-fill-color-lighter);
  border-radius: 12px;
}

.feedback-section h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 25px 0;
  color: var(--el-text-color-primary);
}

.legal-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  margin-bottom: 30px;
}

.legal-item {
  padding: 25px;
  background: var(--el-fill-color-lighter);
  border-radius: 12px;
}

.legal-item h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 15px 0;
  color: var(--el-text-color-primary);
}

.legal-item p {
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
  margin: 0 0 15px 0;
}

.copyright {
  text-align: center;
  padding: 20px;
  border-top: 1px solid var(--el-border-color-light);
}

.copyright p {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

@media (max-width: 768px) {
  .about-view {
    padding: 15px;
  }
  
  .hero-section {
    padding: 40px 20px;
  }
  
  .hero-title {
    font-size: 32px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .hero-stats {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .stat-number {
    font-size: 28px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .team-grid {
    grid-template-columns: 1fr;
  }
  
  .contact-grid {
    grid-template-columns: 1fr;
  }
  
  .legal-grid {
    grid-template-columns: 1fr;
  }
  
  .contact-item {
    flex-direction: column;
    text-align: center;
  }
}
</style>