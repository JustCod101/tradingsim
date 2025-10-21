<template>
  <div class="history-view">
    <div class="history-header">
      <h1>游戏历史</h1>
      <div class="header-actions">
        <el-button @click="refreshHistory" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="exportHistory">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <div class="history-filters">
      <el-form :model="filters" inline>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="选择状态" clearable>
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="超时" value="TIMEOUT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchHistory">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="history-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">{{ stats.totalSessions }}</div>
              <div class="stat-label">总游戏数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">{{ stats.totalScore }}</div>
              <div class="stat-label">总得分</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">${{ stats.totalPnl.toFixed(2) }}</div>
              <div class="stat-label">总盈亏</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">{{ (stats.winRate * 100).toFixed(1) }}%</div>
              <div class="stat-label">胜率</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="history-table">
      <el-table
        :data="historyData"
        v-loading="loading"
        stripe
        @row-click="viewSessionDetail"
      >
        <el-table-column prop="id" label="会话ID" width="200" />
        <el-table-column prop="segmentId" label="段落ID" width="150" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="得分" width="100" />
        <el-table-column label="盈亏" width="120">
          <template #default="{ row }">
            <span :class="row.totalPnl >= 0 ? 'profit' : 'loss'">
              ${{ row.totalPnl.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="winRate" label="胜率" width="100">
          <template #default="{ row }">
            {{ (row.winRate * 100).toFixed(1) }}%
          </template>
        </el-table-column>
        <el-table-column prop="totalFrames" label="总帧数" width="100" />
        <el-table-column label="决策统计" width="150">
          <template #default="{ row }">
            <div class="decision-stats">
              <span class="buy">买: {{ row.buyDecisions }}</span>
              <span class="sell">卖: {{ row.sellDecisions }}</span>
              <span class="skip">跳过: {{ row.skipDecisions }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click.stop="viewSessionDetail(row)">
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

    <!-- 会话详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="会话详情"
      width="80%"
      :before-close="closeDetailDialog"
    >
      <div v-if="selectedSession" class="session-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="会话ID">{{ selectedSession.id }}</el-descriptions-item>
          <el-descriptions-item label="段落ID">{{ selectedSession.segmentId }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedSession.status)">
              {{ getStatusText(selectedSession.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总得分">{{ selectedSession.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="总盈亏">
            <span :class="selectedSession.totalPnl >= 0 ? 'profit' : 'loss'">
              ${{ selectedSession.totalPnl.toFixed(2) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="胜率">{{ (selectedSession.winRate * 100).toFixed(1) }}%</el-descriptions-item>
          <el-descriptions-item label="总帧数">{{ selectedSession.totalFrames }}</el-descriptions-item>
          <el-descriptions-item label="当前帧">{{ selectedSession.currentFrame }}</el-descriptions-item>
          <el-descriptions-item label="进度">{{ (selectedSession.progress * 100).toFixed(1) }}%</el-descriptions-item>
          <el-descriptions-item label="平均响应时间">{{ selectedSession.averageResponseTime.toFixed(0) }}ms</el-descriptions-item>
          <el-descriptions-item label="超时次数">{{ selectedSession.timeoutCount }}</el-descriptions-item>
          <el-descriptions-item label="随机种子">{{ selectedSession.seed }}</el-descriptions-item>
        </el-descriptions>

        <div class="decision-summary">
          <h3>决策统计</h3>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-statistic title="买入决策" :value="selectedSession.buyDecisions" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="卖出决策" :value="selectedSession.sellDecisions" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="跳过决策" :value="selectedSession.skipDecisions" />
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Download } from '@element-plus/icons-vue'
import type { GameSession, GameSessionStatus } from '@/types'

// 响应式数据
const loading = ref(false)
const historyData = ref<GameSession[]>([])
const detailDialogVisible = ref(false)
const selectedSession = ref<GameSession | null>(null)

// 过滤器
const filters = reactive({
  dateRange: [] as string[],
  status: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 统计数据
const stats = reactive({
  totalSessions: 0,
  totalScore: 0,
  totalPnl: 0,
  winRate: 0
})

// 生命周期
onMounted(() => {
  loadHistory()
  loadStats()
})

// 方法
async function loadHistory() {
  loading.value = true
  try {
    // 模拟数据
    const mockData: GameSession[] = Array.from({ length: 50 }, (_, i) => ({
      id: `session-${i + 1}`,
      userId: 'user-1',
      segmentId: `segment-${Math.floor(Math.random() * 10) + 1}`,
      status: ['COMPLETED', 'CANCELLED', 'TIMEOUT'][Math.floor(Math.random() * 3)] as GameSessionStatus,
      currentFrame: Math.floor(Math.random() * 100),
      totalFrames: 100,
      totalScore: Math.floor(Math.random() * 1000),
      totalPnl: (Math.random() - 0.5) * 2000,
      buyDecisions: Math.floor(Math.random() * 20),
      sellDecisions: Math.floor(Math.random() * 20),
      skipDecisions: Math.floor(Math.random() * 10),
      timeoutCount: Math.floor(Math.random() * 5),
      averageResponseTime: Math.random() * 3000,
      seed: Math.floor(Math.random() * 1000000),
      progress: Math.random(),
      winRate: Math.random(),
      createdAt: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString(),
      updatedAt: new Date().toISOString()
    }))

    historyData.value = mockData.slice((pagination.page - 1) * pagination.size, pagination.page * pagination.size)
    pagination.total = mockData.length
  } catch (error) {
    console.error('Failed to load history:', error)
    ElMessage.error('加载历史数据失败')
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    // 模拟统计数据
    stats.totalSessions = 156
    stats.totalScore = 12450
    stats.totalPnl = 2340.56
    stats.winRate = 0.68
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

function refreshHistory() {
  loadHistory()
  loadStats()
}

function searchHistory() {
  pagination.page = 1
  loadHistory()
}

function resetFilters() {
  filters.dateRange = []
  filters.status = ''
  searchHistory()
}

function exportHistory() {
  ElMessage.info('导出功能开发中...')
}

function handleSizeChange(size: number) {
  pagination.size = size
  loadHistory()
}

function handlePageChange(page: number) {
  pagination.page = page
  loadHistory()
}

function viewSessionDetail(session: GameSession) {
  selectedSession.value = session
  detailDialogVisible.value = true
}

function closeDetailDialog() {
  detailDialogVisible.value = false
  selectedSession.value = null
}

function getStatusType(status: GameSessionStatus): string {
  switch (status) {
    case 'COMPLETED':
      return 'success'
    case 'CANCELLED':
      return 'warning'
    case 'TIMEOUT':
      return 'danger'
    default:
      return 'info'
  }
}

function getStatusText(status: GameSessionStatus): string {
  switch (status) {
    case 'COMPLETED':
      return '已完成'
    case 'CANCELLED':
      return '已取消'
    case 'TIMEOUT':
      return '超时'
    case 'STARTED':
      return '进行中'
    case 'PAUSED':
      return '已暂停'
    default:
      return '未知'
  }
}

function formatDate(dateString: string): string {
  return new Date(dateString).toLocaleString('zh-CN')
}
</script>

<style scoped>
.history-view {
  padding: 20px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.history-header h1 {
  margin: 0;
  color: var(--el-text-color-primary);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.history-filters {
  background: var(--el-bg-color);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.history-stats {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--el-color-primary);
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.history-table {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
}

.decision-stats {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
}

.decision-stats .buy {
  color: var(--el-color-success);
}

.decision-stats .sell {
  color: var(--el-color-danger);
}

.decision-stats .skip {
  color: var(--el-color-warning);
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

.session-detail {
  padding: 20px 0;
}

.decision-summary {
  margin-top: 30px;
}

.decision-summary h3 {
  margin-bottom: 20px;
  color: var(--el-text-color-primary);
}
</style>