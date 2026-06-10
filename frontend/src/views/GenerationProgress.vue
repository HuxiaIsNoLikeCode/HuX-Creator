<template>
  <div class="generation-progress-container">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="$router.push(`/project/${projectId}`)">
          <el-icon><ArrowLeft /></el-icon>
          返回项目
        </el-button>
        <h2>内容生成中</h2>
      </div>
    </div>

    <el-card v-if="project" class="project-info-card">
      <div class="project-info">
        <span class="project-title">{{ project.title }}</span>
        <el-tag v-if="progress" :type="statusTagType" size="small">{{ statusLabel }}</el-tag>
      </div>
    </el-card>

    <el-card v-if="progress" class="round-progress-card">
      <div class="round-progress">
        <div class="round-number">
          <span class="current">{{ progress.currentRound }}</span>
          <span class="separator">/</span>
          <span class="max">{{ progress.maxRounds }}</span>
        </div>
        <div class="round-label">当前轮次</div>
        <el-progress
          :percentage="overallPercentage"
          :stroke-width="10"
          :status="progressStatus"
          class="overall-progress"
        />
      </div>
    </el-card>

    <el-card v-if="progress" class="model-stepper-card">
      <div class="stepper-title">模型流水线</div>
      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step
          v-for="model in modelList"
          :key="model.id"
          :title="model.name"
          :description="getStepDescription(model.id)"
        />
      </el-steps>
      <div v-if="progress.currentModelName && isGenerating" class="current-model-info">
        <el-icon class="spinning"><Loading /></el-icon>
        <span>正在执行：{{ progress.currentModelName }}</span>
      </div>
    </el-card>

    <div v-if="roundCards.length > 0" class="round-history">
      <div class="history-title">生成轮次记录</div>
      <div class="round-cards">
        <el-card
          v-for="round in roundCards"
          :key="round.roundNumber"
          class="round-card"
          :class="roundCardClass(round)"
        >
          <div class="round-card-header">
            <div class="round-card-title">
              <span>第 {{ round.roundNumber }} 轮</span>
              <el-tag :type="roundStatusType(round)" size="small">{{ roundStatusLabel(round) }}</el-tag>
            </div>
            <span v-if="round.score !== null" class="round-score">
              {{ round.score }} 分
            </span>
          </div>
          <div v-if="round.mode" class="round-mode">
            <el-tag type="info" size="small" effect="plain">{{ round.mode }}</el-tag>
          </div>
          <div v-if="round.suggestions" class="round-suggestions">
            <div class="suggestions-label">优化建议：</div>
            <div class="suggestions-text">{{ round.suggestions }}</div>
          </div>
        </el-card>
      </div>
    </div>

    <el-card v-if="isCompleted" class="result-card success-card">
      <div class="result-content">
        <el-icon :size="48" color="#67c23a"><CircleCheck /></el-icon>
        <h3>生成完成！</h3>
        <div v-if="progress.score !== null" class="final-score">
          <span class="score-label">最终评分</span>
          <span class="score-value">{{ progress.score }}</span>
          <span class="score-unit">分</span>
        </div>
        <el-tag v-if="progress.passed !== null" :type="progress.passed ? 'success' : 'danger'" size="large">
          {{ progress.passed ? '通过' : '未通过' }}
        </el-tag>
        <div class="result-actions">
          <el-button type="primary" size="large" @click="$router.push(`/project/${projectId}/script`)">
            查看脚本
          </el-button>
          <el-button size="large" @click="$router.push(`/project/${projectId}`)">
            返回项目
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card v-if="isFailed" class="result-card error-card">
      <div class="result-content">
        <el-icon :size="48" color="#f56c6c"><CircleClose /></el-icon>
        <h3>生成失败</h3>
        <p class="error-text">内容生成过程中出现错误，请稍后重试</p>
        <div class="result-actions">
          <el-button type="primary" size="large" @click="handleRetry">
            重新生成
          </el-button>
          <el-button size="large" @click="$router.push(`/project/${projectId}`)">
            返回项目
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card v-if="!progress && !loading" class="result-card">
      <div class="result-content">
        <el-icon :size="48" color="#909399"><QuestionFilled /></el-icon>
        <h3>暂无生成记录</h3>
        <p class="error-text">该项目尚未开始内容生成</p>
        <div class="result-actions">
          <el-button type="primary" size="large" @click="$router.push(`/project/${projectId}`)">
            返回项目
          </el-button>
        </div>
      </div>
    </el-card>

    <div v-if="loading && !progress" class="loading-container">
      <el-icon class="spinning" :size="32" color="#409eff"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { useProjectStore } from '@/stores/project'
import { getGenerationProgressApi, startGenerationApi } from '@/api/content'
import { ElMessage } from 'element-plus'

const route = useRoute()
const projectStore = useProjectStore()

const projectId = route.params.id
const progress = ref(null)
const loading = ref(false)
let pollTimer = null

const project = computed(() => projectStore.currentProject)

const modelList = [
  { id: 1, name: '类型分类', key: 'TypeClassifier' },
  { id: 2, name: '脚本生成', key: 'ScriptGenerator' },
  { id: 3, name: '连贯性检查', key: 'CoherenceChecker' },
  { id: 4, name: '素材标记', key: 'MaterialMarker' },
  { id: 5, name: '评估打分', key: 'Evaluator' }
]

const isGenerating = computed(() => progress.value?.status === 'GENERATING')
const isCompleted = computed(() => progress.value?.status === 'COMPLETED')
const isFailed = computed(() => progress.value?.status === 'FAILED')
const isPending = computed(() => progress.value?.status === 'PENDING')

const activeStep = computed(() => {
  if (!progress.value) return 0
  if (isCompleted.value) return 5
  return progress.value.currentModel - 1
})

const overallPercentage = computed(() => {
  if (!progress.value) return 0
  if (isCompleted.value) return 100
  const roundProgress = ((progress.value.currentRound - 1) / progress.value.maxRounds) * 100
  const modelProgress = ((progress.value.currentModel - 1) / 5) * (100 / progress.value.maxRounds)
  return Math.min(Math.round(roundProgress + modelProgress), 99)
})

const progressStatus = computed(() => {
  if (isCompleted.value) return 'success'
  if (isFailed.value) return 'exception'
  return undefined
})

const statusLabel = computed(() => {
  const map = { PENDING: '等待中', GENERATING: '生成中', COMPLETED: '已完成', FAILED: '已失败' }
  return map[progress.value?.status] || progress.value?.status
})

const statusTagType = computed(() => {
  const map = { PENDING: 'info', GENERATING: '', COMPLETED: 'success', FAILED: 'danger' }
  return map[progress.value?.status] || 'info'
})

const roundCards = computed(() => {
  if (!progress.value?.rounds) return []
  return progress.value.rounds.slice().sort((a, b) => b.roundNumber - a.roundNumber)
})

function getStepDescription(modelId) {
  if (!progress.value) return ''
  if (isCompleted.value) return '已完成'
  if (isFailed.value && progress.value.currentModel === modelId) return '失败'
  if (progress.value.currentModel > modelId) return '已完成'
  if (progress.value.currentModel === modelId && isGenerating.value) return '执行中...'
  return '等待中'
}

function roundCardClass(round) {
  if (round.status === 1) return 'round-success'
  if (round.status === 2) return 'round-fail'
  return 'round-running'
}

function roundStatusType(round) {
  if (round.status === 1) return 'success'
  if (round.status === 2) return 'danger'
  return 'warning'
}

function roundStatusLabel(round) {
  if (round.status === 1) return '已完成'
  if (round.status === 2) return '失败'
  return '进行中'
}

async function fetchProgress() {
  try {
    const res = await getGenerationProgressApi(projectId)
    progress.value = res.data
    if (isCompleted.value || isFailed.value) {
      stopPolling()
    }
  } catch (error) {
    // ignore polling errors
  }
}

function startPolling() {
  stopPolling()
  pollTimer = setInterval(fetchProgress, 5000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

async function handleRetry() {
  loading.value = true
  try {
    await startGenerationApi(projectId, 'DEEPSEEK')
    progress.value = null
    await fetchProgress()
    startPolling()
  } catch (error) {
    ElMessage.error('重新生成失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  loading.value = true
  projectStore.fetchProjectDetail(projectId)
  await fetchProgress()
  loading.value = false
  if (!isCompleted.value && !isFailed.value) {
    startPolling()
  }
})

onBeforeUnmount(() => {
  stopPolling()
})
</script>

<style lang="scss" scoped>
.generation-progress-container {
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  h2 {
    font-size: 24px;
    color: #303133;
    font-weight: 600;
  }
}

.project-info-card {
  margin-bottom: 20px;
  border-radius: 12px;

  .project-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .project-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.round-progress-card {
  margin-bottom: 20px;
  border-radius: 12px;

  .round-progress {
    text-align: center;
    padding: 12px 0;
  }

  .round-number {
    margin-bottom: 8px;

    .current {
      font-size: 48px;
      font-weight: 700;
      color: #409eff;
    }

    .separator {
      font-size: 32px;
      color: #c0c4cc;
      margin: 0 4px;
    }

    .max {
      font-size: 32px;
      font-weight: 400;
      color: #909399;
    }
  }

  .round-label {
    font-size: 14px;
    color: #909399;
    margin-bottom: 16px;
  }

  .overall-progress {
    max-width: 500px;
    margin: 0 auto;
  }
}

.model-stepper-card {
  margin-bottom: 20px;
  border-radius: 12px;

  .stepper-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 24px;
  }

  .current-model-info {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-top: 20px;
    padding: 12px;
    background: #ecf5ff;
    border-radius: 8px;
    color: #409eff;
    font-size: 14px;
    font-weight: 500;
  }
}

.round-history {
  margin-bottom: 20px;

  .history-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
  }

  .round-cards {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
}

.round-card {
  border-radius: 12px;
  border-left: 4px solid #e4e7ed;
  transition: border-color 0.3s;

  &.round-success {
    border-left-color: #67c23a;
  }

  &.round-fail {
    border-left-color: #f56c6c;
  }

  &.round-running {
    border-left-color: #e6a23c;
  }

  .round-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .round-card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .round-score {
    font-size: 24px;
    font-weight: 700;
    color: #409eff;
  }

  .round-mode {
    margin-top: 8px;
  }

  .round-suggestions {
    margin-top: 12px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;

    .suggestions-label {
      font-size: 13px;
      color: #909399;
      font-weight: 500;
      margin-bottom: 4px;
    }

    .suggestions-text {
      font-size: 14px;
      color: #606266;
      line-height: 1.6;
      white-space: pre-wrap;
    }
  }
}

.result-card {
  border-radius: 16px;
  text-align: center;
  margin-bottom: 20px;

  .result-content {
    padding: 40px 20px;
  }

  h3 {
    margin-top: 16px;
    font-size: 24px;
    color: #303133;
  }

  .error-text {
    margin-top: 8px;
    color: #909399;
    font-size: 14px;
  }

  .result-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 24px;
  }
}

.success-card {
  border: 1px solid #e1f3d8;
  background: #f0f9eb;

  .final-score {
    margin: 16px 0;
    display: flex;
    align-items: baseline;
    justify-content: center;
    gap: 4px;

    .score-label {
      font-size: 14px;
      color: #909399;
      margin-right: 8px;
    }

    .score-value {
      font-size: 48px;
      font-weight: 700;
      color: #67c23a;
    }

    .score-unit {
      font-size: 16px;
      color: #909399;
    }
  }
}

.error-card {
  border: 1px solid #fde2e2;
  background: #fef0f0;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px 0;
  color: #909399;
  font-size: 14px;
}

.spinning {
  animation: spin 1.5s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
