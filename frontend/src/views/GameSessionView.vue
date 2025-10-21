<template>
  <div class="game-session-view">
    <div class="session-header">
      <div class="session-info">
        <h1 class="session-title">
          <el-icon><Trophy /></el-icon>
          游戏会话 #{{ sessionId }}
        </h1>
        <div class="session-meta">
          <el-tag v-if="session" :type="getStatusType(session.status)">
            {{ getStatusText(session.status) }}
          </el-tag>
          <span v-if="session" class="session-time">
            {{ formatDate(session.createdAt) }}
          </span>
        </div>
      </div>
      
      <div class="session-actions">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-button v-if="canReplay" type="primary" @click="replaySession">
          <el-icon><VideoPlay /></el-icon>
          回放
        </el-button>
        <el-button @click="shareSession">
          <el-icon><Share /></el-icon>
          分享
        </el-button>
      </div>
    </div>
    
    <div class="session-content" v-loading="loading">
      <div v-if="session" class="session-details">
        <!-- 会话概览 -->
        <el-card class="overview-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><DataAnalysis /></el-icon>
              <span>会话概览</span>
            </div>
          </template>
          
          <div class="overview-grid">
            <div class="overview-item">
              <div class="item-label">初始资金</div>
              <div class="item-value">¥{{ formatCurrency(session.initialBalance) }}</div>
            </div>
            <div class="overview-item">
              <div class="item-label">最终资金</div>
              <div class="item-value">¥{{ formatCurrency(session.finalBalance) }}</div>
            </div>
            <div class="overview-item">
              <div class="item-label">总盈亏</div>
              <div class="item-value" :class="getProfitClass(session.totalProfit)">
                {{ session.totalProfit >= 0 ? '+' : '' }}¥{{ formatCurrency(session.totalProfit) }}
              </div>
            </div>
            <div class="overview-item">
              <div class="item-label">收益率</div>
              <div class="item-value" :class="getProfitClass(session.returnRate)">
                {{ session.returnRate >= 0 ? '+' : '' }}{{ session.returnRate.toFixed(2) }}%
              </div>
            </div>
            <div class="overview-item">
              <div class="item-label">决策次数</div>
              <div class="item-value">{{ session.totalDecisions }}</div>
            </div>
            <div class="overview-item">
              <div class="item-label">正确率</div>
              <div class="item-value">{{ session.accuracy.toFixed(1) }}%</div>
            </div>
            <div class="overview-item">
              <div class="item-label">游戏时长</div>
              <div class="item-value">{{ formatDuration(session.duration) }}</div>
            </div>
            <div class="overview-item">
              <div class="item-label">平均响应时间</div>
              <div class="item-value">{{ session.avgResponseTime.toFixed(1) }}s</div>
            </div>
          </div>
        </el-card>
        
        <!-- 性能图表 -->
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>资金变化曲线</span>
              <div class="chart-controls">
                <el-radio-group v-model="chartType" size="small">
                  <el-radio-button label="balance">资金</el-radio-button>
                  <el-radio-button label="profit">盈亏</el-radio-button>
                  <el-radio-button label="return">收益率</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          
          <div ref="chartContainer" class="chart-container"></div>
        </el-card>
        
        <!-- 决策记录 -->
        <el-card class="decisions-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><List /></el-icon>
              <span>决策记录</span>
              <div class="decisions-stats">
                <el-tag type="success">正确: {{ correctDecisions }}</el-tag>
                <el-tag type="danger">错误: {{ wrongDecisions }}</el-tag>
              </div>
            </div>
          </template>
          
          <el-table
            :data="paginatedDecisions"
            stripe
            style="width: 100%"
            :default-sort="{ prop: 'timestamp', order: 'descending' }"
          >
            <el-table-column prop="timestamp" label="时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.timestamp) }}
              </template>
            </el-table-column>
            
            <el-table-column prop="type" label="决策类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.type === 'BUY' ? 'success' : 'danger'">
                  {{ row.type === 'BUY' ? '买入' : '卖出' }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="price" label="价格" width="120">
              <template #default="{ row }">
                ¥{{ row.price.toFixed(2) }}
              </template>
            </el-table-column>
            
            <el-table-column prop="quantity" label="数量" width="100">
              <template #default="{ row }">
                {{ row.quantity }}
              </template>
            </el-table-column>
            
            <el-table-column prop="profit" label="盈亏" width="120">
              <template #default="{ row }">
                <span :class="getProfitClass(row.profit)">
                  {{ row.profit >= 0 ? '+' : '' }}¥{{ formatCurrency(row.profit) }}
                </span>
              </template>
            </el-table-column>
            
            <el-table-column prop="responseTime" label="响应时间" width="120">
              <template #default="{ row }">
                {{ row.responseTime.toFixed(1) }}s
              </template>
            </el-table-column>
            
            <el-table-column prop="isCorrect" label="结果" width="100">
              <template #default="{ row }">
                <el-icon v-if="row.isCorrect" color="green"><Check /></el-icon>
                <el-icon v-else color="red"><Close /></el-icon>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" @click="viewDecisionDetail(row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="decisions.length"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
        
        <!-- 游戏段落分析 -->
        <el-card class="segments-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Grid /></el-icon>
              <span>游戏段落分析</span>
            </div>
          </template>
          
          <div class="segments-grid">
            <div
              v-for="(segment, index) in session.segments"
              :key="index"
              class="segment-item"
              :class="{ 'segment-active': selectedSegment === index }"
              @click="selectSegment(index)"
            >
              <div class="segment-header">
                <div class="segment-title">段落 {{ index + 1 }}</div>
                <el-tag :type="getDifficultyType(segment.difficulty)">
                  {{ getDifficultyText(segment.difficulty) }}
                </el-tag>
              </div>
              
              <div class="segment-stats">
                <div class="stat-item">
                  <span class="stat-label">决策数</span>
                  <span class="stat-value">{{ segment.decisions }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">正确率</span>
                  <span class="stat-value">{{ segment.accuracy.toFixed(1) }}%</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">盈亏</span>
                  <span class="stat-value" :class="getProfitClass(segment.profit)">
                    {{ segment.profit >= 0 ? '+' : '' }}¥{{ formatCurrency(segment.profit) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      
      <div v-else-if="!loading" class="empty-state">
        <el-empty description="会话不存在或已被删除">
          <el-button type="primary" @click="goBack">返回</el-button>
        </el-empty>
      </div>
    </div>
    
    <!-- 决策详情对话框 -->
    <el-dialog v-model="showDecisionDialog" title="决策详情" width="600px">
      <div v-if="selectedDecision" class="decision-detail">
        <div class="detail-grid">
          <div class="detail-item">
            <label>决策时间：</label>
            <span>{{ formatTime(selectedDecision.timestamp) }}</span>
          </div>
          <div class="detail-item">
            <label>决策类型：</label>
            <el-tag :type="selectedDecision.type === 'BUY' ? 'success' : 'danger'">
              {{ selectedDecision.type === 'BUY' ? '买入' : '卖出' }}
            </el-tag>
          </div>
          <div class="detail-item">
            <label>股票价格：</label>
            <span>¥{{ selectedDecision.price.toFixed(2) }}</span>
          </div>
          <div class="detail-item">
            <label>交易数量：</label>
            <span>{{ selectedDecision.quantity }}</span>
          </div>
          <div class="detail-item">
            <label>响应时间：</label>
            <span>{{ selectedDecision.responseTime.toFixed(1) }}秒</span>
          </div>
          <div class="detail-item">
            <label>决策结果：</label>
            <span :class="selectedDecision.isCorrect ? 'text-success' : 'text-danger'">
              {{ selectedDecision.isCorrect ? '正确' : '错误' }}
            </span>
          </div>
          <div class="detail-item">
            <label>盈亏金额：</label>
            <span :class="getProfitClass(selectedDecision.profit)">
              {{ selectedDecision.profit >= 0 ? '+' : '' }}¥{{ formatCurrency(selectedDecision.profit) }}
            </span>
          </div>
        </div>
        
        <div v-if="selectedDecision.note" class="decision-note">
          <label>决策说明：</label>
          <p>{{ selectedDecision.note }}</p>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showDecisionDialog = false">关闭</el-button>
      </template>
    </el-dialog>
    
    <!-- 分享对话框 -->
    <el-dialog v-model="showShareDialog" title="分享会话" width="500px">
      <div class="share-content">
        <div class="share-options">
          <el-button @click="copyShareLink" :loading="copying">
            <el-icon><Link /></el-icon>
            复制链接
          </el-button>
          <el-button @click="shareToSocial('wechat')">
            <el-icon><ChatDotRound /></el-icon>
            微信分享
          </el-button>
          <el-button @click="shareToSocial('weibo')">
            <el-icon><Share /></el-icon>
            微博分享
          </el-button>
        </div>
        
        <div class="share-preview">
          <h4>分享预览</h4>
          <div class="preview-card">
            <div class="preview-title">我在交易模拟器中的表现</div>
            <div class="preview-stats">
              <span>收益率: {{ session?.returnRate.toFixed(2) }}%</span>
              <span>正确率: {{ session?.accuracy.toFixed(1) }}%</span>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showShareDialog = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  Trophy,
  ArrowLeft,
  VideoPlay,
  Share,
  DataAnalysis,
  TrendCharts,
  List,
  Grid,
  Check,
  Close,
  Link,
  ChatDotRound
} from '@element-plus/icons-vue'
import type { GameSession, GameDecision, SegmentDifficulty } from '@/types'

const router = useRouter()
const route = useRoute()

// 响应式数据
const loading = ref(true)
const session = ref<GameSession | null>(null)
const decisions = ref<GameDecision[]>([])
const chartType = ref<'balance' | 'profit' | 'return'>('balance')
const selectedSegment = ref<number | null>(null)
const showDecisionDialog = ref(false)
const showShareDialog = ref(false)
const selectedDecision = ref<GameDecision | null>(null)
const copying = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(20)

// 图表
const chartContainer = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

// 计算属性
const sessionId = computed(() => route.params.sessionId as string)

const canReplay = computed(() => {
  return session.value?.status === 'COMPLETED'
})

const correctDecisions = computed(() => {
  return decisions.value.filter(d => d.isCorrect).length
})

const wrongDecisions = computed(() => {
  return decisions.value.filter(d => !d.isCorrect).length
})

const paginatedDecisions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return decisions.value.slice(start, end)
})

// 生命周期
onMounted(async () => {
  await loadSessionData()
  await nextTick()
  initChart()
})

// 方法
async function loadSessionData() {
  try {
    loading.value = true
    
    // 模拟加载会话数据
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    session.value = {
      id: sessionId.value,
      userId: 'user-1',
      status: 'COMPLETED',
      initialBalance: 100000,
      finalBalance: 125000,
      totalProfit: 25000,
      returnRate: 25.0,
      totalDecisions: 45,
      accuracy: 73.3,
      duration: 1800, // 30分钟
      avgResponseTime: 2.5,
      createdAt: new Date(Date.now() - 86400000), // 1天前
      updatedAt: new Date(),
      segments: [
        {
          id: 'seg-1',
          difficulty: 'EASY',
          decisions: 15,
          accuracy: 80.0,
          profit: 8000
        },
        {
          id: 'seg-2',
          difficulty: 'MEDIUM',
          decisions: 20,
          accuracy: 70.0,
          profit: 12000
        },
        {
          id: 'seg-3',
          difficulty: 'HARD',
          decisions: 10,
          accuracy: 70.0,
          profit: 5000
        }
      ]
    }
    
    // 生成模拟决策数据
    decisions.value = generateMockDecisions(45)
    
  } catch (error) {
    ElMessage.error('加载会话数据失败')
    console.error('Load session error:', error)
  } finally {
    loading.value = false
  }
}

function generateMockDecisions(count: number): GameDecision[] {
  const decisions: GameDecision[] = []
  let currentBalance = 100000
  
  for (let i = 0; i < count; i++) {
    const type = Math.random() > 0.5 ? 'BUY' : 'SELL'
    const price = 50 + Math.random() * 100
    const quantity = Math.floor(Math.random() * 100) + 1
    const isCorrect = Math.random() > 0.27 // 73% 正确率
    const profit = isCorrect ? Math.random() * 2000 - 500 : -(Math.random() * 1000)
    
    currentBalance += profit
    
    decisions.push({
      id: `decision-${i + 1}`,
      sessionId: sessionId.value,
      type: type as 'BUY' | 'SELL',
      price,
      quantity,
      timestamp: new Date(Date.now() - (count - i) * 60000), // 每分钟一个决策
      responseTime: Math.random() * 5 + 0.5,
      isCorrect,
      profit,
      note: isCorrect ? '决策正确，获得收益' : '决策错误，产生亏损'
    })
  }
  
  return decisions
}

function initChart() {
  if (!chartContainer.value) return
  
  chart = echarts.init(chartContainer.value)
  updateChart()
  
  // 监听图表类型变化
  watch(() => chartType.value, updateChart)
}

function updateChart() {
  if (!chart || !session.value) return
  
  // 生成图表数据
  const data = generateChartData()
  
  const option = {
    title: {
      text: getChartTitle(),
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'normal'
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const point = params[0]
        return `${point.name}<br/>${point.seriesName}: ${formatChartValue(point.value)}`
      }
    },
    xAxis: {
      type: 'category',
      data: data.map((_, index) => `第${index + 1}次`)
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => formatChartValue(value)
      }
    },
    series: [
      {
        name: getChartTitle(),
        type: 'line',
        data: data,
        smooth: true,
        lineStyle: {
          width: 3
        },
        areaStyle: {
          opacity: 0.3
        }
      }
    ],
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    }
  }
  
  chart.setOption(option)
}

function generateChartData(): number[] {
  if (!session.value) return []
  
  const data: number[] = []
  let currentValue = chartType.value === 'balance' ? session.value.initialBalance : 0
  
  decisions.value.forEach((decision, index) => {
    switch (chartType.value) {
      case 'balance':
        currentValue += decision.profit
        data.push(currentValue)
        break
      case 'profit':
        data.push(decision.profit)
        break
      case 'return':
        const returnRate = ((currentValue + decision.profit - session.value!.initialBalance) / session.value!.initialBalance) * 100
        data.push(returnRate)
        currentValue += decision.profit
        break
    }
  })
  
  return data
}

function getChartTitle(): string {
  const titles = {
    balance: '资金余额',
    profit: '单次盈亏',
    return: '累计收益率'
  }
  return titles[chartType.value]
}

function formatChartValue(value: number): string {
  switch (chartType.value) {
    case 'balance':
      return `¥${formatCurrency(value)}`
    case 'profit':
      return `${value >= 0 ? '+' : ''}¥${formatCurrency(value)}`
    case 'return':
      return `${value >= 0 ? '+' : ''}${value.toFixed(2)}%`
    default:
      return value.toString()
  }
}

function goBack() {
  router.back()
}

function replaySession() {
  router.push(`/game/${sessionId.value}/replay`)
}

function shareSession() {
  showShareDialog.value = true
}

function viewDecisionDetail(decision: GameDecision) {
  selectedDecision.value = decision
  showDecisionDialog.value = true
}

function selectSegment(index: number) {
  selectedSegment.value = selectedSegment.value === index ? null : index
}

function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
}

function handleCurrentChange(page: number) {
  currentPage.value = page
}

async function copyShareLink() {
  try {
    copying.value = true
    const shareUrl = `${window.location.origin}/session/${sessionId.value}/share`
    await navigator.clipboard.writeText(shareUrl)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  } finally {
    copying.value = false
  }
}

function shareToSocial(platform: string) {
  const shareText = `我在交易模拟器中获得了${session.value?.returnRate.toFixed(2)}%的收益率！`
  const shareUrl = `${window.location.origin}/session/${sessionId.value}/share`
  
  // 这里可以集成实际的社交分享API
  ElMessage.info(`分享到${platform}功能开发中...`)
}

// 工具函数
function formatDate(date: Date): string {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

function formatTime(date: Date): string {
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(date)
}

function formatCurrency(amount: number): string {
  return Math.abs(amount).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

function formatDuration(seconds: number): string {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟${secs}秒`
  } else {
    return `${secs}秒`
  }
}

function getStatusType(status: string): string {
  const typeMap: Record<string, string> = {
    CREATED: 'info',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return typeMap[status] || 'info'
}

function getStatusText(status: string): string {
  const textMap: Record<string, string> = {
    CREATED: '已创建',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return textMap[status] || status
}

function getDifficultyType(difficulty: SegmentDifficulty): string {
  const typeMap: Record<SegmentDifficulty, string> = {
    EASY: 'success',
    MEDIUM: 'warning',
    HARD: 'danger'
  }
  return typeMap[difficulty]
}

function getDifficultyText(difficulty: SegmentDifficulty): string {
  const textMap: Record<SegmentDifficulty, string> = {
    EASY: '简单',
    MEDIUM: '中等',
    HARD: '困难'
  }
  return textMap[difficulty]
}

function getProfitClass(value: number): string {
  if (value > 0) return 'text-success'
  if (value < 0) return 'text-danger'
  return ''
}
</script>

<style scoped>
.game-session-view {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.session-info {
  flex: 1;
}

.session-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.session-meta {
  display: flex;
  align-items: center;
  gap: 15px;
}

.session-time {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.session-actions {
  display: flex;
  gap: 10px;
}

.session-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.card-header > span {
  font-weight: 600;
  font-size: 16px;
}

.chart-controls {
  margin-left: auto;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.overview-item {
  text-align: center;
  padding: 20px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.item-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.item-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.chart-container {
  height: 400px;
  width: 100%;
}

.decisions-stats {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.segments-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 15px;
}

.segment-item {
  padding: 20px;
  border: 2px solid var(--el-border-color-light);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.segment-item:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.segment-active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.segment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.segment-title {
  font-weight: 600;
  font-size: 16px;
}

.segment-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}

.stat-value {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.decision-detail {
  padding: 20px 0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-item label {
  font-weight: 600;
  color: var(--el-text-color-secondary);
  min-width: 80px;
}

.decision-note {
  padding-top: 15px;
  border-top: 1px solid var(--el-border-color-light);
}

.decision-note label {
  font-weight: 600;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
  display: block;
}

.decision-note p {
  margin: 0;
  color: var(--el-text-color-regular);
  line-height: 1.6;
}

.share-content {
  padding: 20px 0;
}

.share-options {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-bottom: 30px;
}

.share-preview h4 {
  margin-bottom: 15px;
  color: var(--el-text-color-primary);
}

.preview-card {
  padding: 20px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
}

.preview-title {
  font-weight: 600;
  margin-bottom: 10px;
  color: var(--el-text-color-primary);
}

.preview-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.text-success {
  color: var(--el-color-success);
}

.text-danger {
  color: var(--el-color-danger);
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .game-session-view {
    padding: 15px;
  }
  
  .session-header {
    flex-direction: column;
    gap: 20px;
    align-items: stretch;
  }
  
  .session-actions {
    justify-content: center;
  }
  
  .overview-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .segments-grid {
    grid-template-columns: 1fr;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .share-options {
    flex-direction: column;
    align-items: center;
  }
  
  .preview-stats {
    flex-direction: column;
    gap: 5px;
  }
}
</style>