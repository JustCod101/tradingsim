<template>
  <div class="profile-view">
    <div class="profile-header">
      <div class="profile-info">
        <el-avatar :size="80" :src="userProfile.avatar">
          <el-icon><User /></el-icon>
        </el-avatar>
        <div class="user-details">
          <h1>{{ userProfile.username }}</h1>
          <p class="user-email">{{ userProfile.email }}</p>
          <div class="user-stats">
            <el-tag type="success">等级 {{ userProfile.level }}</el-tag>
            <el-tag type="info">经验值 {{ userProfile.experience }}</el-tag>
            <el-tag type="warning">排名 #{{ userProfile.rank }}</el-tag>
          </div>
        </div>
      </div>
      <div class="profile-actions">
        <el-button @click="editMode = !editMode" :type="editMode ? 'success' : 'primary'">
          <el-icon><Edit /></el-icon>
          {{ editMode ? '保存' : '编辑资料' }}
        </el-button>
        <el-button @click="showAvatarDialog = true">
          <el-icon><Picture /></el-icon>
          更换头像
        </el-button>
      </div>
    </div>

    <div class="profile-content">
      <el-row :gutter="20">
        <!-- 左侧：个人信息 -->
        <el-col :span="16">
          <el-card class="profile-card">
            <template #header>
              <div class="card-header">
                <span>个人信息</span>
              </div>
            </template>
            
            <el-form :model="userProfile" label-width="100px" :disabled="!editMode">
              <el-form-item label="用户名">
                <el-input v-model="userProfile.username" />
              </el-form-item>
              
              <el-form-item label="邮箱">
                <el-input v-model="userProfile.email" type="email" />
              </el-form-item>
              
              <el-form-item label="真实姓名">
                <el-input v-model="userProfile.realName" />
              </el-form-item>
              
              <el-form-item label="手机号">
                <el-input v-model="userProfile.phone" />
              </el-form-item>
              
              <el-form-item label="生日">
                <el-date-picker
                  v-model="userProfile.birthday"
                  type="date"
                  placeholder="选择日期"
                  style="width: 100%"
                />
              </el-form-item>
              
              <el-form-item label="性别">
                <el-radio-group v-model="userProfile.gender">
                  <el-radio label="male">男</el-radio>
                  <el-radio label="female">女</el-radio>
                  <el-radio label="other">其他</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="所在地">
                <el-input v-model="userProfile.location" />
              </el-form-item>
              
              <el-form-item label="个人简介">
                <el-input
                  v-model="userProfile.bio"
                  type="textarea"
                  :rows="4"
                  placeholder="介绍一下自己..."
                />
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 游戏统计 -->
          <el-card class="profile-card">
            <template #header>
              <div class="card-header">
                <span>游戏统计</span>
                <el-button text @click="refreshStats">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </template>
            
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">{{ gameStats.totalGames }}</div>
                <div class="stat-label">总游戏数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ gameStats.winRate }}%</div>
                <div class="stat-label">胜率</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">${{ formatNumber(gameStats.totalProfit) }}</div>
                <div class="stat-label">总盈利</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ gameStats.avgDecisionTime }}s</div>
                <div class="stat-label">平均决策时间</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ gameStats.bestStreak }}</div>
                <div class="stat-label">最佳连胜</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ gameStats.totalPlayTime }}h</div>
                <div class="stat-label">总游戏时长</div>
              </div>
            </div>
          </el-card>

          <!-- 成就系统 -->
          <el-card class="profile-card">
            <template #header>
              <div class="card-header">
                <span>成就徽章</span>
                <el-tag size="small">{{ achievements.filter(a => a.unlocked).length }}/{{ achievements.length }}</el-tag>
              </div>
            </template>
            
            <div class="achievements-grid">
              <div
                v-for="achievement in achievements"
                :key="achievement.id"
                class="achievement-item"
                :class="{ unlocked: achievement.unlocked }"
                @click="showAchievementDetail(achievement)"
              >
                <el-icon :size="32" :class="achievement.unlocked ? 'text-yellow-500' : 'text-gray-400'">
                  <Trophy v-if="achievement.type === 'trophy'" />
                  <Medal v-else-if="achievement.type === 'medal'" />
                  <Star v-else />
                </el-icon>
                <div class="achievement-name">{{ achievement.name }}</div>
                <div class="achievement-desc">{{ achievement.description }}</div>
                <div v-if="achievement.progress" class="achievement-progress">
                  <el-progress
                    :percentage="(achievement.progress.current / achievement.progress.total) * 100"
                    :show-text="false"
                    :stroke-width="4"
                  />
                  <span class="progress-text">
                    {{ achievement.progress.current }}/{{ achievement.progress.total }}
                  </span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：活动和设置 -->
        <el-col :span="8">
          <!-- 最近活动 -->
          <el-card class="profile-card">
            <template #header>
              <div class="card-header">
                <span>最近活动</span>
              </div>
            </template>
            
            <el-timeline>
              <el-timeline-item
                v-for="activity in recentActivities"
                :key="activity.id"
                :timestamp="formatDate(activity.timestamp)"
                :type="activity.type"
              >
                <div class="activity-content">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div class="activity-desc">{{ activity.description }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-card>

          <!-- 偏好设置 -->
          <el-card class="profile-card">
            <template #header>
              <div class="card-header">
                <span>偏好设置</span>
              </div>
            </template>
            
            <el-form :model="preferences" label-width="80px">
              <el-form-item label="语言">
                <el-select v-model="preferences.language" style="width: 100%">
                  <el-option label="中文" value="zh-CN" />
                  <el-option label="English" value="en-US" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="时区">
                <el-select v-model="preferences.timezone" style="width: 100%">
                  <el-option label="北京时间 (UTC+8)" value="Asia/Shanghai" />
                  <el-option label="纽约时间 (UTC-5)" value="America/New_York" />
                  <el-option label="伦敦时间 (UTC+0)" value="Europe/London" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="隐私">
                <el-switch
                  v-model="preferences.publicProfile"
                  active-text="公开"
                  inactive-text="私密"
                />
              </el-form-item>
              
              <el-form-item label="邮件通知">
                <el-switch v-model="preferences.emailNotifications" />
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 账户安全 -->
          <el-card class="profile-card">
            <template #header>
              <div class="card-header">
                <span>账户安全</span>
              </div>
            </template>
            
            <div class="security-items">
              <div class="security-item">
                <div class="security-info">
                  <div class="security-title">密码</div>
                  <div class="security-desc">上次修改：3个月前</div>
                </div>
                <el-button size="small" @click="showChangePassword = true">修改</el-button>
              </div>
              
              <div class="security-item">
                <div class="security-info">
                  <div class="security-title">两步验证</div>
                  <div class="security-desc">未启用</div>
                </div>
                <el-button size="small" type="primary">启用</el-button>
              </div>
              
              <div class="security-item">
                <div class="security-info">
                  <div class="security-title">登录设备</div>
                  <div class="security-desc">3台设备</div>
                </div>
                <el-button size="small">管理</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog v-model="showAvatarDialog" title="更换头像" width="400px">
      <el-upload
        class="avatar-uploader"
        :show-file-list="false"
        :before-upload="beforeAvatarUpload"
        :on-success="handleAvatarSuccess"
        action="/api/upload/avatar"
      >
        <img v-if="newAvatar" :src="newAvatar" class="avatar-preview" />
        <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
      </el-upload>
      <template #footer>
        <el-button @click="showAvatarDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAvatar">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showChangePassword" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="当前密码">
          <el-input v-model="passwordForm.currentPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePassword = false">取消</el-button>
        <el-button type="primary" @click="changePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Edit,
  Picture,
  Refresh,
  Trophy,
  Medal,
  Star,
  Plus
} from '@element-plus/icons-vue'

// 响应式数据
const editMode = ref(false)
const showAvatarDialog = ref(false)
const showChangePassword = ref(false)
const newAvatar = ref('')

// 用户资料
const userProfile = reactive({
  id: 1,
  username: 'TradingMaster',
  email: 'user@example.com',
  realName: '张三',
  phone: '13800138000',
  birthday: new Date('1990-01-01'),
  gender: 'male',
  location: '北京市',
  bio: '热爱交易的投资者，专注于技术分析和风险管理。',
  avatar: '',
  level: 15,
  experience: 12500,
  rank: 42
})

// 游戏统计
const gameStats = reactive({
  totalGames: 156,
  winRate: 68.5,
  totalProfit: 25680,
  avgDecisionTime: 12.3,
  bestStreak: 8,
  totalPlayTime: 45.2
})

// 成就数据
const achievements = ref([
  {
    id: 1,
    name: '初出茅庐',
    description: '完成第一场游戏',
    type: 'trophy',
    unlocked: true
  },
  {
    id: 2,
    name: '连胜高手',
    description: '连续获胜5场',
    type: 'medal',
    unlocked: true,
    progress: { current: 5, total: 5 }
  },
  {
    id: 3,
    name: '盈利大师',
    description: '累计盈利超过$10,000',
    type: 'star',
    unlocked: true,
    progress: { current: 25680, total: 10000 }
  },
  {
    id: 4,
    name: '速度之王',
    description: '平均决策时间少于10秒',
    type: 'trophy',
    unlocked: false,
    progress: { current: 12.3, total: 10 }
  },
  {
    id: 5,
    name: '百战老兵',
    description: '完成100场游戏',
    type: 'medal',
    unlocked: true,
    progress: { current: 156, total: 100 }
  },
  {
    id: 6,
    name: '排行榜王者',
    description: '进入排行榜前10名',
    type: 'star',
    unlocked: false,
    progress: { current: 42, total: 10 }
  }
])

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    title: '完成游戏',
    description: '在AAPL股票游戏中获得$1,250盈利',
    timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000),
    type: 'success'
  },
  {
    id: 2,
    title: '解锁成就',
    description: '获得"百战老兵"成就',
    timestamp: new Date(Date.now() - 6 * 60 * 60 * 1000),
    type: 'warning'
  },
  {
    id: 3,
    title: '排名提升',
    description: '排名从#45提升到#42',
    timestamp: new Date(Date.now() - 12 * 60 * 60 * 1000),
    type: 'primary'
  },
  {
    id: 4,
    title: '更新资料',
    description: '更新了个人简介',
    timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000),
    type: 'info'
  }
])

// 偏好设置
const preferences = reactive({
  language: 'zh-CN',
  timezone: 'Asia/Shanghai',
  publicProfile: true,
  emailNotifications: true
})

// 密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 生命周期
onMounted(() => {
  loadUserProfile()
})

// 方法
function loadUserProfile() {
  // 模拟加载用户数据
  console.log('Loading user profile...')
}

function refreshStats() {
  ElMessage.success('统计数据已刷新')
}

function showAchievementDetail(achievement: any) {
  ElMessageBox.alert(
    achievement.description,
    achievement.name,
    {
      confirmButtonText: '确定'
    }
  )
}

function formatNumber(num: number): string {
  return num.toLocaleString()
}

function formatDate(date: Date): string {
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  
  if (hours < 1) {
    return '刚刚'
  } else if (hours < 24) {
    return `${hours}小时前`
  } else {
    const days = Math.floor(hours / 24)
    return `${days}天前`
  }
}

function beforeAvatarUpload(file: File) {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

function handleAvatarSuccess(response: any) {
  newAvatar.value = response.url
}

function saveAvatar() {
  if (newAvatar.value) {
    userProfile.avatar = newAvatar.value
    showAvatarDialog.value = false
    ElMessage.success('头像更新成功')
  }
}

function changePassword() {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  
  // 模拟密码修改
  showChangePassword.value = false
  ElMessage.success('密码修改成功')
  
  // 重置表单
  Object.assign(passwordForm, {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
}
</script>

<style scoped>
.profile-view {
  padding: 20px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: var(--el-bg-color);
  border-radius: 8px;
}

.profile-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-details h1 {
  margin: 0 0 5px 0;
  color: var(--el-text-color-primary);
}

.user-email {
  margin: 0 0 10px 0;
  color: var(--el-text-color-secondary);
}

.user-stats {
  display: flex;
  gap: 8px;
}

.profile-actions {
  display: flex;
  gap: 10px;
}

.profile-content {
  margin-top: 20px;
}

.profile-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--el-color-primary);
  margin-bottom: 5px;
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.achievement-item {
  padding: 15px;
  border: 2px solid var(--el-border-color);
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.achievement-item:hover {
  border-color: var(--el-color-primary);
}

.achievement-item.unlocked {
  background: var(--el-color-success-light-9);
  border-color: var(--el-color-success);
}

.achievement-name {
  font-weight: bold;
  margin: 8px 0 4px 0;
}

.achievement-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.achievement-progress {
  margin-top: 8px;
}

.progress-text {
  font-size: 10px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
  display: block;
}

.activity-content {
  margin-bottom: 10px;
}

.activity-title {
  font-weight: bold;
  margin-bottom: 4px;
}

.activity-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.security-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: var(--el-fill-color-lighter);
  border-radius: 6px;
}

.security-title {
  font-weight: bold;
  margin-bottom: 2px;
}

.security-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.avatar-uploader {
  display: flex;
  justify-content: center;
}

:deep(.avatar-uploader .el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 178px;
  height: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.avatar-uploader .el-upload:hover) {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar-preview {
  width: 178px;
  height: 178px;
  object-fit: cover;
}

.text-yellow-500 {
  color: #f59e0b;
}

.text-gray-400 {
  color: #9ca3af;
}
</style>