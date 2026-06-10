<template>
  <div class="generate-container">
    <div class="page-header">
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>内容生成</h2>
    </div>

    <div v-if="generating" class="generating-state">
      <el-card class="progress-card">
        <div class="progress-content">
          <el-icon class="loading-icon" :size="64" color="#409eff">
            <Loading />
          </el-icon>
          <h3>AI 正在为您生成内容...</h3>
          <p>{{ currentStep }}</p>
          <el-progress
            :percentage="progress"
            :stroke-width="8"
            class="progress-bar"
          />
          <div class="step-list">
            <div
              v-for="(step, index) in steps"
              :key="index"
              :class="['step-item', { active: index === currentStepIndex, completed: index < currentStepIndex }]"
            >
              <el-icon v-if="index < currentStepIndex" class="step-icon completed"><CircleCheck /></el-icon>
              <el-icon v-else-if="index === currentStepIndex" class="step-icon active"><Loading /></el-icon>
              <el-icon v-else class="step-icon"><CircleFilled /></el-icon>
              <span>{{ step }}</span>
            </div>
          </div>
          <p class="retry-hint">每步自动保存，中断后可断点续传</p>
        </div>
      </el-card>
    </div>

    <div v-else-if="errorState" class="error-state">
      <el-card class="result-card">
        <div class="result-header">
          <el-icon :size="48" color="#e6a23c"><WarningFilled /></el-icon>
          <h3>生成中断</h3>
          <p>{{ errorMessage }}</p>
        </div>
        <div class="result-actions">
          <el-button type="primary" size="large" @click="handleStart">
            继续生成（断点续传）
          </el-button>
          <el-button size="large" @click="handleRegenerate">
            从头重新生成
          </el-button>
        </div>
      </el-card>
    </div>

    <div v-else-if="completed" class="completed-state">
      <el-card class="result-card">
        <div class="result-header">
          <el-icon :size="48" color="#67c23a"><CircleCheck /></el-icon>
          <h3>内容生成完成！</h3>
          <p>已为您生成完整的视频内容规划方案</p>
        </div>
        <div class="result-actions">
          <el-button type="primary" size="large" @click="$router.push(`/project/${projectId}`)">
            查看完整方案
          </el-button>
          <el-button size="large" @click="handleRegenerate">
            重新生成
          </el-button>
        </div>
      </el-card>
    </div>

    <div v-else class="ready-state">
      <el-card class="ready-card">
        <div class="ready-content">
          <el-icon :size="64" color="#409eff"><MagicStick /></el-icon>
          <h3>准备生成内容</h3>
          <p v-if="project">项目：{{ project.title }}</p>
          <p v-if="existingStep > 0" class="resume-hint">
            检测到已完成 {{ existingStep }}/8 步，点击将继续生成
          </p>
          <el-button type="primary" size="large" @click="handleStart">
            {{ existingStep > 0 ? '继续生成' : '开始生成' }}
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useProjectStore } from '@/stores/project'
import { generateContentApi, getContentApi } from '@/api/content'

const route = useRoute()
const projectStore = useProjectStore()

const projectId = route.params.id
const project = computed(() => projectStore.currentProject)
const generating = ref(false)
const completed = ref(false)
const errorState = ref(false)
const errorMessage = ref('')
const progress = ref(0)
const currentStepIndex = ref(0)
const existingStep = ref(0)

const steps = [
  '步骤1/8: 分析选题方向...',
  '步骤2/8: 生成标题方案...',
  '步骤3/8: 创作开头钩子...',
  '步骤4/8: 编写视频脚本...',
  '步骤5/8: 设计分镜脚本...',
  '步骤6/8: 推荐配套素材...',
  '步骤7/8: 生成提示词...',
  '步骤8/8: 撰写封面文案...'
]

const currentStep = computed(() => steps[currentStepIndex.value] || '处理中...')

let progressTimer = null

function startProgress(fromStep = 0) {
  generating.value = true
  errorState.value = false
  progress.value = fromStep * 12.5
  currentStepIndex.value = fromStep

  progressTimer = setInterval(() => {
    if (progress.value < 90) {
      progress.value += Math.random() * 3
      currentStepIndex.value = Math.min(
        Math.floor(progress.value / 12.5),
        steps.length - 1
      )
    }
  }, 1000)
}

function stopProgress() {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
  progress.value = 100
  currentStepIndex.value = steps.length - 1
}

async function checkExistingProgress() {
  try {
    const res = await getContentApi(projectId)
    if (res.data && res.data.generationStep) {
      existingStep.value = res.data.generationStep
      if (res.data.status === 1) {
        completed.value = true
      }
    }
  } catch (e) {
    existingStep.value = 0
  }
}

async function handleStart() {
  startProgress(existingStep.value)
  try {
    await generateContentApi(projectId, 'DEEPSEEK')
    stopProgress()
    generating.value = false
    completed.value = true
    errorState.value = false
  } catch (error) {
    stopProgress()
    generating.value = false
    errorState.value = true
    errorMessage.value = error.response?.data?.message || error.message || '生成失败，请重试'
    await checkExistingProgress()
  }
}

async function handleRegenerate() {
  existingStep.value = 0
  completed.value = false
  errorState.value = false
  handleStart()
}

onMounted(() => {
  projectStore.fetchProjectDetail(projectId)
  checkExistingProgress()
})

onBeforeUnmount(() => {
  stopProgress()
})
</script>

<style lang="scss" scoped>
.generate-container {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;

  h2 {
    font-size: 24px;
    color: #303133;
    font-weight: 600;
  }
}

.progress-card,
.result-card,
.ready-card {
  border-radius: 16px;
  text-align: center;
}

.progress-content {
  padding: 40px 20px;
}

.loading-icon {
  animation: spin 1.5s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.progress-content h3 {
  margin-top: 24px;
  font-size: 24px;
  color: #303133;
}

.progress-content > p {
  margin-top: 8px;
  color: #909399;
  font-size: 16px;
}

.progress-bar {
  margin: 32px auto;
  max-width: 400px;
}

.step-list {
  margin-top: 40px;
  text-align: left;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  color: #c0c4cc;
  font-size: 14px;
  transition: color 0.3s;

  &.active {
    color: #409eff;
    font-weight: 500;
  }

  &.completed {
    color: #67c23a;
  }
}

.step-icon {
  font-size: 18px;
}

.retry-hint {
  margin-top: 24px;
  color: #909399;
  font-size: 12px;
}

.resume-hint {
  color: #e6a23c;
  font-size: 14px;
  margin-top: 8px;
  margin-bottom: 16px;
}

.result-header {
  padding: 40px 20px;

  h3 {
    margin-top: 16px;
    font-size: 24px;
    color: #303133;
  }

  p {
    margin-top: 8px;
    color: #909399;
  }
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px;
}

.ready-content {
  padding: 60px 20px;

  h3 {
    margin-top: 24px;
    font-size: 24px;
    color: #303133;
  }

  p {
    margin-top: 8px;
    color: #909399;
    margin-bottom: 32px;
  }
}
</style>
