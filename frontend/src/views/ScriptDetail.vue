<template>
  <div class="script-detail-container">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>脚本详情</h2>
      </div>
      <div class="header-actions">
        <el-dropdown @command="handleExport">
          <el-button>
            <el-icon><Download /></el-icon>
            导出
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="markdown">导出 Markdown</el-dropdown-item>
              <el-dropdown-item command="json">导出 JSON</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div v-loading="pageLoading" class="content-wrapper">
      <template v-if="script">
        <el-card class="overview-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon :size="20"><Document /></el-icon>
                <span>脚本概览</span>
              </div>
              <div class="card-meta">
                <el-tag :type="statusTagType">{{ statusLabel }}</el-tag>
                <el-tag :type="videoTypeTagType">{{ videoTypeLabel }}</el-tag>
                <el-tag>第 {{ script.round }} 轮 · v{{ script.version }}</el-tag>
              </div>
            </div>
          </template>
          <div class="overview-metrics">
            <div class="metric-item">
              <span class="metric-value">{{ script.score || '-' }}</span>
              <span class="metric-label">综合评分</span>
            </div>
            <div class="metric-divider" />
            <div class="metric-item">
              <span class="metric-value">{{ script.wordCount || 0 }}</span>
              <span class="metric-label">字数</span>
            </div>
            <div class="metric-divider" />
            <div class="metric-item">
              <span class="metric-value">{{ script.estimatedDuration || 0 }}s</span>
              <span class="metric-label">预估时长</span>
            </div>
            <div class="metric-divider" />
            <div class="metric-item">
              <span class="metric-value">{{ script.narrativeRhythm || '-' }}</span>
              <span class="metric-label">叙事节奏</span>
            </div>
            <div class="metric-divider" />
            <div class="metric-item">
              <span class="metric-value">{{ scenes.length }}</span>
              <span class="metric-label">场景数</span>
            </div>
          </div>
        </el-card>

        <el-card class="script-text-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon :size="20"><Memo /></el-icon>
                <span>完整脚本文本</span>
              </div>
              <el-button text @click="showFullScript = !showFullScript">
                {{ showFullScript ? '收起' : '展开' }}
                <el-icon class="el-icon--right">
                  <ArrowDown v-if="!showFullScript" />
                  <ArrowUp v-else />
                </el-icon>
              </el-button>
            </div>
          </template>
          <div :class="['script-text', { collapsed: !showFullScript }]">
            <pre>{{ script.content }}</pre>
          </div>
        </el-card>

        <div class="scenes-section">
          <div class="section-title">
            <el-icon :size="20"><Film /></el-icon>
            <span>场景列表</span>
            <el-tag size="small" type="info">共 {{ scenes.length }} 个场景</el-tag>
          </div>

          <el-collapse v-model="activeScenes" @change="handleSceneChange">
            <el-collapse-item
              v-for="scene in scenes"
              :key="scene.id"
              :name="scene.id"
            >
              <template #title>
                <div class="scene-title-bar">
                  <el-tag size="small" type="info" class="scene-number">
                    场景 {{ scene.sceneNumber }}
                  </el-tag>
                  <span class="scene-title-text">{{ scene.title }}</span>
                  <el-tag
                    size="small"
                    :type="visualTypeTagType(scene.visualType)"
                    class="scene-visual-tag"
                  >
                    {{ visualTypeLabel(scene.visualType) }}
                  </el-tag>
                </div>
              </template>

              <div class="scene-content">
                <el-descriptions
                  :column="3"
                  border
                  size="small"
                  class="scene-descriptions"
                >
                  <el-descriptions-item label="场景标题" :span="2">
                    {{ scene.title }}
                  </el-descriptions-item>
                  <el-descriptions-item label="预估时长">
                    {{ scene.estimatedDuration || '-' }}s
                  </el-descriptions-item>
                  <el-descriptions-item label="场景描述" :span="3">
                    {{ scene.description || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="场景地点">
                    {{ scene.location || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="情绪氛围">
                    {{ scene.mood || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="视觉类型">
                    <el-tag size="small" :type="visualTypeTagType(scene.visualType)">
                      {{ visualTypeLabel(scene.visualType) }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="转场方式" :span="3">
                    {{ scene.transition || '-' }}
                  </el-descriptions-item>
                </el-descriptions>

                <el-tabs
                  v-model="sceneTabs[scene.id]"
                  class="scene-tabs"
                >
                  <el-tab-pane label="镜头" :name="`${scene.id}-shots`">
                    <div
                      v-loading="sceneLoading[scene.id]"
                      class="shots-wrapper"
                    >
                      <div
                        v-if="sceneShots[scene.id] && sceneShots[scene.id].length > 0"
                        class="shots-timeline"
                      >
                        <el-timeline>
                          <el-timeline-item
                            v-for="shot in sceneShots[scene.id]"
                            :key="shot.id"
                            :timestamp="`镜头 ${shot.shotNumber} · ${shot.duration || 0}s`"
                            placement="top"
                            :type="shotTimelineType(shot)"
                          >
                            <el-card shadow="never" class="shot-card">
                              <div class="shot-header">
                                <el-tag size="small" type="info">
                                  镜头 {{ shot.shotNumber }}
                                </el-tag>
                                <el-tag
                                  v-if="shot.visualType"
                                  size="small"
                                  :type="visualTypeTagType(shot.visualType)"
                                >
                                  {{ visualTypeLabel(shot.visualType) }}
                                </el-tag>
                              </div>
                              <el-descriptions :column="2" size="small" border>
                                <el-descriptions-item label="镜头描述" :span="2">
                                  {{ shot.description || '-' }}
                                </el-descriptions-item>
                                <el-descriptions-item label="机位角度">
                                  {{ shot.cameraAngle || '-' }}
                                </el-descriptions-item>
                                <el-descriptions-item label="镜头运动">
                                  {{ shot.cameraMovement || '-' }}
                                </el-descriptions-item>
                                <el-descriptions-item label="时长">
                                  {{ shot.duration || 0 }}s
                                </el-descriptions-item>
                                <el-descriptions-item label="转场">
                                  {{ shot.transition || '-' }}
                                </el-descriptions-item>
                                <el-descriptions-item
                                  v-if="shot.dialogue"
                                  label="台词/旁白"
                                  :span="2"
                                >
                                  <div class="shot-dialogue">{{ shot.dialogue }}</div>
                                </el-descriptions-item>
                              </el-descriptions>
                            </el-card>
                          </el-timeline-item>
                        </el-timeline>
                      </div>
                      <el-empty
                        v-else-if="!sceneLoading[scene.id]"
                        description="暂无镜头数据"
                        :image-size="80"
                      />
                    </div>
                  </el-tab-pane>

                  <el-tab-pane label="素材" :name="`${scene.id}-materials`">
                    <div
                      v-loading="sceneLoading[scene.id]"
                      class="materials-wrapper"
                    >
                      <div
                        v-if="sceneMaterials[scene.id] && sceneMaterials[scene.id].length > 0"
                        class="materials-grid"
                      >
                        <el-card
                          v-for="material in sceneMaterials[scene.id]"
                          :key="material.id"
                          shadow="hover"
                          class="material-card"
                        >
                          <div class="material-header">
                            <el-tag
                              size="small"
                              :type="materialTypeTagType(material.materialType)"
                            >
                              {{ materialTypeLabel(material.materialType) }}
                            </el-tag>
                            <span class="material-name">{{ material.name }}</span>
                          </div>
                          <el-descriptions
                            :column="1"
                            size="small"
                            border
                            class="material-descriptions"
                          >
                            <el-descriptions-item label="描述">
                              {{ material.description || '-' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="位置">
                              {{ material.position || '-' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="来源建议">
                              {{ material.sourceSuggestion || '-' }}
                            </el-descriptions-item>
                          </el-descriptions>
                          <div v-if="material.prompt" class="material-prompt">
                            <div class="prompt-label">AI 提示词</div>
                            <div class="prompt-content">{{ material.prompt }}</div>
                          </div>
                        </el-card>
                      </div>
                      <el-empty
                        v-else-if="!sceneLoading[scene.id]"
                        description="暂无素材数据"
                        :image-size="80"
                      />
                    </div>
                  </el-tab-pane>
                </el-tabs>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </template>

      <div v-if="!script && !pageLoading" class="empty-content">
        <el-empty description="暂无脚本数据">
          <el-button type="primary" @click="fetchData">
            重新加载
          </el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useProjectStore } from '@/stores/project'
import { getScriptApi, getSceneShotsApi, getSceneMaterialsApi } from '@/api/content'
import { ElMessage } from 'element-plus'

const route = useRoute()
const projectStore = useProjectStore()

const projectId = route.params.id
const pageLoading = ref(false)
const script = ref(null)
const scenes = ref([])
const activeScenes = ref([])
const showFullScript = ref(false)

const sceneShots = reactive({})
const sceneMaterials = reactive({})
const sceneLoading = reactive({})
const sceneTabs = reactive({})

const project = computed(() => projectStore.currentProject)

const statusMap = { 0: '草稿', 1: '已优化', 2: '最终版' }
const statusLabel = computed(() => statusMap[script.value?.status] || '未知')
const statusTagType = computed(() => ({ 0: 'info', 1: 'warning', 2: 'success' })[script.value?.status] || 'info')

const videoTypeMap = { MARKETING: '营销类', KNOWLEDGE: '知识类', STORY: '故事类' }
const videoTypeLabel = computed(() => videoTypeMap[script.value?.videoType] || script.value?.videoType || '-')
const videoTypeTagType = computed(() => {
  const map = { MARKETING: 'danger', KNOWLEDGE: 'primary', STORY: 'success' }
  return map[script.value?.videoType] || 'info'
})

const visualTypeMap = { ANIMATION: '动画', LIVE: '实拍', GRAPHIC: '图文', MIXED: '混合' }
function visualTypeLabel(type) { return visualTypeMap[type] || type || '-' }
function visualTypeTagType(type) {
  return { ANIMATION: '', LIVE: 'success', GRAPHIC: 'warning', MIXED: 'danger' }[type] || 'info'
}

const materialTypeMap = {
  IMAGE: '图片',
  PERSON: '人物',
  PROP: '道具',
  BACKGROUND: '背景',
  EFFECT: '特效',
  AUDIO: '音频'
}
function materialTypeLabel(type) { return materialTypeMap[type] || type || '-' }
function materialTypeTagType(type) {
  return {
    IMAGE: '',
    PERSON: 'success',
    PROP: 'warning',
    BACKGROUND: 'info',
    EFFECT: 'danger',
    AUDIO: 'primary'
  }[type] || 'info'
}

function shotTimelineType(shot) {
  const types = ['primary', 'success', 'warning', 'danger', '']
  return types[(shot.shotNumber - 1) % types.length]
}

async function fetchData() {
  pageLoading.value = true
  try {
    await projectStore.fetchProjectDetail(projectId)
    const res = await getScriptApi(projectId)
    const data = res.data
    if (data) {
      script.value = data.script || data
      scenes.value = data.scenes || []
      scenes.value.forEach(scene => {
        sceneTabs[scene.id] = `${scene.id}-shots`
      })
    }
  } catch (error) {
    ElMessage.error('加载脚本数据失败')
  } finally {
    pageLoading.value = false
  }
}

async function handleSceneChange(activeNames) {
  for (const sceneId of activeNames) {
    if (!sceneShots[sceneId] && !sceneLoading[sceneId]) {
      await loadSceneData(sceneId)
    }
  }
}

async function loadSceneData(sceneId) {
  sceneLoading[sceneId] = true
  try {
    const [shotsRes, materialsRes] = await Promise.all([
      getSceneShotsApi(sceneId),
      getSceneMaterialsApi(sceneId)
    ])
    sceneShots[sceneId] = shotsRes.data || []
    sceneMaterials[sceneId] = materialsRes.data || []
  } catch (error) {
    ElMessage.error('加载场景数据失败')
    sceneShots[sceneId] = []
    sceneMaterials[sceneId] = []
  } finally {
    sceneLoading[sceneId] = false
  }
}

function handleExport(type) {
  if (!script.value) return
  let content, filename
  if (type === 'markdown') {
    content = buildMarkdown()
    filename = `script_${projectId}.md`
  } else {
    content = JSON.stringify({ script: script.value, scenes: scenes.value }, null, 2)
    filename = `script_${projectId}.json`
  }
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  window.URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

function buildMarkdown() {
  let md = `# 脚本详情\n\n`
  md += `- 评分: ${script.value.score}\n`
  md += `- 字数: ${script.value.wordCount}\n`
  md += `- 预估时长: ${script.value.estimatedDuration}s\n`
  md += `- 视频类型: ${videoTypeLabel.value}\n\n`
  md += `## 完整脚本\n\n${script.value.content}\n\n`
  md += `## 场景列表\n\n`
  scenes.value.forEach(scene => {
    md += `### 场景 ${scene.sceneNumber}: ${scene.title}\n\n`
    md += `- 地点: ${scene.location || '-'}\n`
    md += `- 氛围: ${scene.mood || '-'}\n`
    md += `- 时长: ${scene.estimatedDuration || '-'}s\n`
    md += `- 视觉类型: ${visualTypeLabel(scene.visualType)}\n`
    md += `- 转场: ${scene.transition || '-'}\n\n`
    if (scene.description) md += `${scene.description}\n\n`
  })
  return md
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.script-detail-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;

  h2 {
    font-size: 24px;
    color: #303133;
    font-weight: 600;
  }
}

.header-actions {
  display: flex;
  gap: 12px;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.overview-card {
  border-radius: 12px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
  }

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .card-meta {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
}

.overview-metrics {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 20px 0;

  .metric-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }

  .metric-value {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    line-height: 1;
  }

  .metric-label {
    font-size: 13px;
    color: #909399;
  }

  .metric-divider {
    width: 1px;
    height: 40px;
    background: #ebeef5;
  }
}

.script-text-card {
  border-radius: 12px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.script-text {
  &.collapsed {
    max-height: 200px;
    overflow: hidden;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 60px;
      background: linear-gradient(transparent, #fff);
      pointer-events: none;
    }
  }

  pre {
    margin: 0;
    white-space: pre-wrap;
    word-break: break-word;
    font-size: 14px;
    line-height: 1.8;
    color: #303133;
    font-family: inherit;
  }
}

.scenes-section {
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
  }
}

.scene-title-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;

  .scene-number {
    flex-shrink: 0;
  }

  .scene-title-text {
    font-size: 15px;
    font-weight: 500;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .scene-visual-tag {
    flex-shrink: 0;
  }
}

.scene-content {
  padding: 4px 0;
}

.scene-descriptions {
  margin-bottom: 20px;
}

.scene-tabs {
  margin-top: 8px;
}

.shots-wrapper,
.materials-wrapper {
  min-height: 100px;
  padding: 12px 0;
}

.shots-timeline {
  padding: 0 8px;

  .el-timeline {
    padding-left: 0;
  }
}

.shot-card {
  border-radius: 8px;

  :deep(.el-card__body) {
    padding: 14px;
  }

  .shot-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
  }
}

.shot-dialogue {
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  border-left: 3px solid #409eff;
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
}

.materials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}

.material-card {
  border-radius: 8px;

  :deep(.el-card__body) {
    padding: 16px;
  }

  .material-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;

    .material-name {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
    }
  }

  .material-descriptions {
    margin-bottom: 0;
  }
}

.material-prompt {
  margin-top: 12px;
  padding: 10px 12px;
  background: #f0f9ff;
  border-radius: 6px;
  border: 1px solid #e0f2fe;

  .prompt-label {
    font-size: 12px;
    color: #909399;
    margin-bottom: 6px;
    font-weight: 600;
  }

  .prompt-content {
    font-size: 13px;
    color: #303133;
    line-height: 1.6;
    word-break: break-word;
  }
}

.empty-content {
  padding: 60px 0;
}
</style>
