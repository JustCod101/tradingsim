<template>
  <div class="leaderboard-view">
    <div class="leaderboard-header">
      <h1>排行榜</h1>
      <div class="header-actions">
        <el-button @click="refreshLeaderboard" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <div class="leaderboard-filters">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="总分排行" name="score" />
        <el-tab-pane label="盈亏排行" name="pnl" />
        <el-tab-pane label="胜率排行" name="winRate" />
        <el-tab-pane label="响应时间" name="responseTime" />
      </el-tabs>
    </div>

    <div class="leaderboard-content">
      <!-- 前三名展示 -->
      <div class="top-three">
        <div class="podium">
          <div v-for="(entry, index) in topThree" :key="entry.sessionId" 
               :class="['podium-item', `rank-${index + 1}`]">
            <div class="rank-badge">{{ index + 1 }}</div>
            <div class="user-avatar">
              <el-avatar :size="60">{{ getUserInitial(entry.userId) }}</el-avatar>
            </div>
            <div class="user-info">
              <div class="username">{{ entry.userId }}</div>
              <div class="score">{{ getDisplayValue(entry) }}</div>
              <div class="session-info">会话: {{ entry.sessionId.slice(-8) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 完整排行榜 -->
      <div class="leaderboard-table">
        <el-table
          :data="leaderboardData"
          v-loading="loading"
          stripe
          @row-click="viewEntryDetail"
        >
          <el-table-column label="排名" width="80" align="center">
            <template #default="{ $index }">
              <div class="rank-display">
                <el-icon v-if="$index < 3" class="medal-icon" :class="`medal-${$index + 1}`">
                  <Trophy />
                </el-icon>
                <span v-else>{{ $index + 1 }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="userId" label="用户" width="150">
            <template #default="{ row }">
              <div class="user-cell">
                <el-avatar :size="32">{{ getUserInitial(row.userId) }}</el-avatar>
                <span class="username">{{ row.userId }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="sessionId" label="会话ID" width="120">
            <template #default="{ row }">
              {{ row.sessionId.slice(-8) }}
            </template>
          </el-table-column>
          <el-table-column prop="segmentId" label="段落" width="100" />
          <el-table-column v-if="activeTab === 'score'" prop="totalScore" label="总分" width="100" />
          <el-table-column v-if="activeTab === 'pnl'" label="总盈亏" width="120">
            <template #default="{ row }">
              <span :class="row.totalPnl >= 0 ? 'profit' : 'loss'">
                ${{ row.totalPnl.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'winRate'" prop="winRate" label="胜率" width="100">
            <template #default="{ row }">
              {{ (row.winRate * 100).toFixed(1) }}%
            </template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'responseTime'" prop="averageResponseTime" label="平均响应时间" width="150">
            <template #default="{ row }">
              {{ row.averageResponseTime.toFixed(0) }}ms
            </template>
          </el-table-column>
          <el-table-column prop="totalDecisions" label="总决策数" width="100" />
          <el-table-column prop="timeoutCount" label="超时次数" width="100" />
          <el-table-column prop="completedAt" label="完成时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.completedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click.stop="viewEntryDetail(row)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="排行榜详情"
      width="70%"
      :before-close="closeDetailDialog"
    >
      <div v-if="selectedEntry" class="entry-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="排名">{{ selectedEntry.rank }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ selectedEntry.userId }}</el-descriptions-item>
          <el-descriptions-item label="会话ID">{{ selectedEntry.sessionId }}</el-descriptions-item>
          <el-descriptions-item label="段落ID">{{ selectedEntry.segmentId }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ selectedEntry.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="总盈亏">
            <span :class="selectedEntry.totalPnl >= 0 ? 'profit' : 'loss'">
              ${{ selectedEntry.totalPnl.toFixed(2) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="胜率">{{ (selectedEntry.winRate * 100).toFixed(1) }}%</el-descriptions-item>
          <el-descriptions-item label="平均响应时间">{{ selectedEntry.averageResponseTime.toFixed(0) }}ms</el-descriptions-item>
          <el-descriptions-item label="总决策数">{{ selectedEntry.totalDecisions }}</el-descriptions-item>
          <el-descriptions-item label="超时次数">{{ selectedEntry.timeoutCount }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatDate(selectedEntry.completedAt) }}</el-descriptions-item>
        </el-descriptions>

        <div class="performance-chart">
          <h3>表现分析</h3>
          <div class="chart-container">
            <div class="chart-placeholder">
              <el-icon size="48"><TrendCharts /></el-icon>
              <p>图表功能开发中...</p>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Trophy, TrendCharts } from '@element-plus/icons-vue'
import type { LeaderboardEntry } from '@/types'

// 响应式数据
const loading = ref(false)
const activeTab = ref('score')
const leaderboardData = ref<LeaderboardEntry[]>([])
const detailDialogVisible = ref(false)
const selectedEntry = ref<LeaderboardEntry | null>(null)

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 计算属性
const topThree = computed(() => leaderboardData.value.slice(0, 3))

// 生命周期
onMounted(() => {
  loadLeaderboard()
})

// 方法
async function loadLeaderboard() {
  loading.value = true
  try {
    // 模拟数据
    const mockData: LeaderboardEntry[] = Array.from({ length: 100 }, (_, i) => {
      const baseScore = 1000 - i * 10 + Math.random() * 50
      const basePnl = 2000 - i * 20 + (Math.random() - 0.5) * 500
      const baseWinRate = 0.9 - i * 0.005 + Math.random() * 0.1
      const baseResponseTime = 1000 + i * 10 + Math.random() * 500

      return {
        rank: i + 1,
        sessionId: `session-${Math.floor(Math.random() * 10000)}`,
        userId: `user-${Math.floor(Math.random() * 1000)}`,
        segmentId: `segment-${Math.floor(Math.random() * 10) + 1}`,
        totalScore: Math.max(0, Math.floor(baseScore)),
        totalPnl: basePnl,
        winRate: Math.max(0, Math.min(1, baseWinRate)),
        averageResponseTime: Math.max(500, baseResponseTime),
        totalDecisions: Math.floor(Math.random() * 100) + 50,
        timeoutCount: Math.floor(Math.random() * 10),
        completedAt: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString()
      }
    })

    // 根据当前标签页排序
    sortLeaderboard(mockData)
    
    leaderboardData.value = mockData.slice((pagination.page - 1) * pagination.size, pagination.page * pagination.size)
    pagination.total = mockData.length
  } catch (error) {
    console.error('Failed to load leaderboard:', error)
    ElMessage.error('加载排行榜失败')
  } finally {
    loading.value = false
  }
}

function sortLeaderboard(data: LeaderboardEntry[]) {
  switch (activeTab.value) {
    case 'score':
      data.sort((a, b) => b.totalScore - a.totalScore)
      break
    case 'pnl':
      data.sort((a, b) => b.totalPnl - a.totalPnl)
      break
    case 'winRate':
      data.sort((a, b) => b.winRate - a.winRate)
      break
    case 'responseTime':
      data.sort((a, b) => a.averageResponseTime - b.averageResponseTime)
      break
  }
  
  // 更新排名
  data.forEach((entry, index) => {
    entry.rank = index + 1
  })
}

function refreshLeaderboard() {
  loadLeaderboard()
}

function handleTabChange() {
  pagination.page = 1
  loadLeaderboard()
}

function handleSizeChange(size: number) {
  pagination.size = size
  loadLeaderboard()
}

function handlePageChange(page: number) {
  pagination.page = page
  loadLeaderboard()
}

function viewEntryDetail(entry: LeaderboardEntry) {
  selectedEntry.value = entry
  detailDialogVisible.value = true
}

function closeDetailDialog() {
  detailDialogVisible.value = false
  selectedEntry.value = null
}

function getUserInitial(userId: string): string {
  return userId.charAt(0).toUpperCase()
}

function getDisplayValue(entry: LeaderboardEntry): string {
  switch (activeTab.value) {
    case 'score':
      return entry.totalScore.toString()
    case 'pnl':
      return `$${entry.totalPnl.toFixed(2)}`
    case 'winRate':
      return `${(entry.winRate * 100).toFixed(1)}%`
    case 'responseTime':
      return `${entry.averageResponseTime.toFixed(0)}ms`
    default:
      return entry.totalScore.toString()
  }
}

function formatDate(dateString: string): string {
  return new Date(dateString).toLocaleString('zh-CN')
}
</script>

<style scoped>
.leaderboard-view {
  padding: 20px;
}

.leaderboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.leaderboard-header h1 {
  margin: 0;
  color: var(--el-text-color-primary);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.leaderboard-filters {
  background: var(--el-bg-color);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.top-three {
  margin-bottom: 30px;
}

.podium {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
}

.podium-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border-radius: 12px;
  background: var(--el-bg-color);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  min-width: 200px;
}

.rank-1 {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  transform: scale(1.1);
}

.rank-2 {
  background: linear-gradient(135deg, #c0c0c0, #e5e5e5);
}

.rank-3 {
  background: linear-gradient(135deg, #cd7f32, #daa520);
}

.rank-badge {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--el-color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.user-avatar {
  margin-bottom: 10px;
}

.user-info {
  text-align: center;
}

.username {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 5px;
  color: var(--el-text-color-primary);
}

.score {
  font-size: 18px;
  font-weight: bold;
  color: var(--el-color-primary);
  margin-bottom: 5px;
}

.session-info {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.leaderboard-table {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
}

.rank-display {
  display: flex;
  align-items: center;
  justify-content: center;
}

.medal-icon {
  font-size: 20px;
}

.medal-1 {
  color: #ffd700;
}

.medal-2 {
  color: #c0c0c0;
}

.medal-3 {
  color: #cd7f32;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-weight: 500;
}

.profit {
  color: var(--el-color-success);
  font-weight: bold;
}

.loss {
  color: var(--el-color-danger);
  font-weight: bold;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.entry-detail {
  padding: 20px 0;
}

.performance-chart {
  margin-top: 30px;
}

.performance-chart h3 {
  margin-bottom: 20px;
  color: var(--el-text-color-primary);
}

.chart-container {
  height: 300px;
  border: 2px dashed var(--el-border-color);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
}

.chart-placeholder p {
  margin-top: 10px;
  font-size: 14px;
}
</style>