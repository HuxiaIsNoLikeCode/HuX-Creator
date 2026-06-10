<template>
  <div class="project-detail-container">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="$router.push('/project')" text>
          <el-icon :size="18"><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="title-group">
          <h2>{{ project?.title || '项目详情' }}</h2>
          <el-tag v-if="project?.status !== undefined" :type="getStatusTagType(project.status)" size="small">
            {{ getStatusLabel(project.status) }}
          </el-tag>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleFullGenerate" :loading="generating">
          <el-icon><MagicStick /></el-icon>
          AI生成
        </el-button>
        <el-dropdown @command="handleExport">
          <el-button>
            导出
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="markdown">导出 Markdown</el-dropdown-item>
              <el-dropdown-item command="json">导出 JSON</el-dropdown-item>
              <el-dropdown-item command="word">导出 Word</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="model-bar">
      <span class="model-label">模型：</span>
      <el-select v-model="selectedModel" placeholder="选择模型" style="width: 200px">
        <el-option v-for="m in modelOptions" :key="m.value" :label="m.label" :value="m.value" />
      </el-select>
    </div>

    <div v-if="project" class="project-info-card">
      <el-descriptions :column="4" border>
        <el-descriptions-item label="平台">
          <el-tag :type="getPlatformTagType(project.platform)" size="small">
            {{ getPlatformLabel(project.platform) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="目标用户">{{ project.targetUser || '-' }}</el-descriptions-item>
        <el-descriptions-item label="时长">{{ project.duration }}秒</el-descriptions-item>
        <el-descriptions-item label="视频类型">
          <el-tag size="small">{{ getVideoTypeLabel(project.videoType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="叙事节奏">{{ project.narrativeRhythm || 0.7 }}</el-descriptions-item>
        <el-descriptions-item label="视觉风格">
          <el-tag size="small">{{ getVisualStyleLabel(project.visualStyle) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="生成模式">
          <el-tag :type="project.generationMode === 'PROFESSIONAL' ? 'warning' : 'success'" size="small">
            {{ project.generationMode === 'PROFESSIONAL' ? '专业模式' : '快速模式' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ project.createTime }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="4">{{ project.description || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="sections-wrapper">
      <div
        v-for="section in contentSections"
        :key="section.key"
        class="section-card"
      >
        <div class="section-header">
          <div class="section-title">
            <span class="section-icon">{{ section.emoji }}</span>
            <span>{{ section.title }}</span>
          </div>
          <div class="section-actions">
            <el-button
              v-if="section.key !== 'script'"
              size="small"
              :disabled="!getContentText(section.key)"
              @click="openEditDialog(section)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              v-if="section.key === 'script'"
              size="small"
              @click="scrollToScript"
            >
              <el-icon><View /></el-icon>
              查看脚本
            </el-button>
            <el-button
              v-if="section.key !== 'script'"
              size="small"
              type="primary"
              :loading="generatingSections[section.key]"
              @click="handleSectionGenerate(section)"
            >
              <el-icon><MagicStick /></el-icon>
              AI生成
            </el-button>
          </div>
        </div>
        <div class="section-body">
          <template v-if="section.key === 'script'">
            <div class="script-summary" v-if="scriptData">
              <el-space :size="16">
                <el-tag type="info">共 {{ scriptData.scenes?.length || 0 }} 个场景</el-tag>
                <el-tag type="info">共 {{ totalShots }} 个镜头</el-tag>
                <el-tag type="info">预估 {{ totalDuration }}秒</el-tag>
                <el-tag type="info">{{ totalWordCount }} 字</el-tag>
              </el-space>
            </div>
            <div v-if="scriptData && scriptData.scenes?.length" class="script-content">
              <el-collapse v-model="expandedScenes" @change="handleSceneExpand">
                <el-collapse-item
                  v-for="(scene, idx) in scriptData.scenes"
                  :key="scene.id || idx"
                  :name="scene.id || idx"
                >
                  <template #title>
                    <div class="scene-title-bar">
                      <span class="scene-label">场景{{ idx + 1 }}：</span>
                      <span class="scene-name">{{ scene.title || scene.sceneTitle || '未命名场景' }}</span>
                      <el-tag size="small" type="info" class="scene-tag">{{ scene.visualType || '实拍' }}</el-tag>
                      <el-tag size="small" class="scene-tag">{{ scene.duration || 5 }}秒</el-tag>
                    </div>
                  </template>
                  <div class="scene-detail">
                    <div class="scene-info-grid">
                      <div class="scene-info-item">
                        <span class="info-label">描述</span>
                        <span class="info-value">{{ scene.description || scene.sceneDescription || '-' }}</span>
                      </div>
                      <div class="scene-info-item">
                        <span class="info-label">地点</span>
                        <span class="info-value">{{ scene.location || '-' }}</span>
                      </div>
                      <div class="scene-info-item">
                        <span class="info-label">氛围</span>
                        <span class="info-value">{{ scene.mood || '-' }}</span>
                      </div>
                      <div class="scene-info-item">
                        <span class="info-label">视觉类型</span>
                        <span class="info-value">{{ scene.visualType || '实拍' }}</span>
                      </div>
                    </div>
                    <div class="scene-bg-action">
                      <el-button
                        size="small"
                        type="primary"
                        plain
                        :loading="sceneBgLoading[scene.id]"
                        @click.stop="generateSceneBg(scene)"
                      >
                        <el-icon><Picture /></el-icon>
                        生成背景图
                      </el-button>
                      <span v-if="scene.backgroundPrompt" class="bg-prompt">{{ scene.backgroundPrompt }}</span>
                    </div>

                    <div v-if="sceneLoaded[scene.id]" class="scene-shots-section">
                      <div class="sub-section-title">
                        <el-icon><Film /></el-icon>
                        <span>镜头列表</span>
                      </div>
                      <el-timeline v-if="sceneShots[scene.id]?.length">
                        <el-timeline-item
                          v-for="(shot, shotIdx) in sceneShots[scene.id]"
                          :key="shot.id || shotIdx"
                          :type="shotIdx === 0 ? 'primary' : ''"
                          :hollow="shotIdx !== 0"
                          placement="top"
                        >
                          <div class="shot-card">
                            <div class="shot-header">
                              <span class="shot-label">镜头{{ shotIdx + 1 }}</span>
                              <el-tag size="small" v-if="shot.cameraAngle">{{ shot.cameraAngle }}</el-tag>
                              <el-tag size="small" type="info" v-if="shot.cameraMovement">{{ shot.cameraMovement }}</el-tag>
                              <el-tag size="small" type="warning" v-if="shot.duration">{{ shot.duration }}秒</el-tag>
                            </div>
                            <div class="shot-desc">{{ shot.description || '-' }}</div>
                            <div v-if="shot.dialogue" class="shot-dialogue">
                              <span class="dialogue-label">台词：</span>{{ shot.dialogue }}
                            </div>
                            <div class="shot-frame-actions">
                              <el-button
                                size="small"
                                :loading="frameLoading[`${shot.id}-first`]"
                                @click.stop="generateShotFrame(shot, 'first-frame')"
                              >
                                <el-icon><VideoPlay /></el-icon>
                                生成首帧
                              </el-button>
                              <el-button
                                size="small"
                                :loading="frameLoading[`${shot.id}-last`]"
                                @click.stop="generateShotFrame(shot, 'last-frame')"
                              >
                                <el-icon><VideoPause /></el-icon>
                                生成尾帧
                              </el-button>
                            </div>
                            <div v-if="shot.firstFramePrompt" class="frame-prompt">
                              <el-tag size="small" type="success">首帧</el-tag>
                              <span>{{ shot.firstFramePrompt }}</span>
                            </div>
                            <div v-if="shot.lastFramePrompt" class="frame-prompt">
                              <el-tag size="small" type="warning">尾帧</el-tag>
                              <span>{{ shot.lastFramePrompt }}</span>
                            </div>
                          </div>
                        </el-timeline-item>
                      </el-timeline>
                      <el-empty v-else description="暂无镜头数据" :image-size="60" />
                    </div>

                    <div v-if="sceneLoaded[scene.id]" class="scene-materials-section">
                      <div class="sub-section-title">
                        <el-icon><Files /></el-icon>
                        <span>素材列表</span>
                      </div>
                      <div v-if="sceneMaterials[scene.id]?.length" class="materials-grid">
                        <div
                          v-for="(mat, matIdx) in sceneMaterials[scene.id]"
                          :key="mat.id || matIdx"
                          class="material-card"
                        >
                          <div class="material-header">
                            <el-tag :type="getMaterialTagType(mat.type)" size="small" effect="dark">
                              {{ mat.type || 'OTHER' }}
                            </el-tag>
                            <span class="material-name">{{ mat.name || '未命名素材' }}</span>
                          </div>
                          <div v-if="mat.description" class="material-desc">{{ mat.description }}</div>
                          <div v-if="mat.position" class="material-position">
                            <el-icon><Location /></el-icon>
                            {{ mat.position }}
                          </div>
                          <div class="material-tags" v-if="mat.type === 'PERSON' || mat.type === 'CHARACTER'">
                            <div class="tag-list">
                              <el-tag
                                v-for="(tag, tagIdx) in parseCharacterTags(mat.characterTag)"
                                :key="tagIdx"
                                closable
                                size="small"
                                @close="removeCharacterTag(mat, tagIdx)"
                              >
                                {{ tag }}
                              </el-tag>
                            </div>
                            <div class="tag-input-row">
                              <el-input
                                v-model="tagInputValues[mat.id]"
                                size="small"
                                placeholder="输入角色标签"
                                style="width: 120px"
                                @keyup.enter="addCharacterTag(mat)"
                              />
                              <el-button size="small" text type="primary" @click="addCharacterTag(mat)">
                                添加
                              </el-button>
                            </div>
                          </div>
                        </div>
                      </div>
                      <el-empty v-else description="暂无素材数据" :image-size="60" />
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
            <div v-else-if="!scriptLoading" class="section-empty">
              <el-empty description="暂未生成脚本" :image-size="80">
                <el-button type="primary" @click="handleSectionGenerate(section)" :loading="generatingSections['script']">
                  <el-icon><MagicStick /></el-icon>
                  AI生成
                </el-button>
              </el-empty>
            </div>
            <div v-else v-loading="true" style="min-height: 100px;" />
          </template>
          <template v-else>
            <div v-if="getContentText(section.key)" class="content-display" v-html="renderContent(getContentText(section.key))" />
            <div v-else class="section-empty">
              <span class="empty-text">暂未生成，点击AI生成按钮开始</span>
            </div>
          </template>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="editDialogVisible"
      :title="'编辑 - ' + (editingSection?.title || '')"
      width="720px"
      destroy-on-close
    >
      <el-input
        v-model="editContent"
        type="textarea"
        :rows="16"
        placeholder="输入内容..."
      />
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/project'
import {
  getContentApi,
  getScriptApi,
  getSceneShotsApi,
  getSceneMaterialsApi,
  startGenerationApi,
  optimizeContentApi,
  regenerateStepApi
} from '@/api/content'
import { exportMarkdownApi, exportJsonApi, exportWordApi } from '@/api/export'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const projectStore = useProjectStore()

const projectId = route.params.id
const pageLoading = ref(false)
const generating = ref(false)
const content = ref(null)
const scriptData = ref(null)
const scriptLoading = ref(false)
const selectedModel = ref('DEEPSEEK')
const editDialogVisible = ref(false)
const editingSection = ref(null)
const editContent = ref('')
const editSaving = ref(false)

const expandedScenes = ref([])
const sceneShots = reactive({})
const sceneMaterials = reactive({})
const sceneLoaded = reactive({})
const sceneBgLoading = reactive({})
const frameLoading = reactive({})
const generatingSections = reactive({})
const tagInputValues = reactive({})

const project = computed(() => projectStore.currentProject)

const modelOptions = [
  { label: 'Mimo (小米 · 默认)', value: 'DEEPSEEK' },
  { label: '通义千问 (阿里)', value: 'QWEN' },
  { label: '豆包 (字节)', value: 'DOUBAO' },
  { label: 'Gemini (Google)', value: 'GEMINI' }
]

const contentSections = [
  { key: 'topicAnalysis', title: '选题分析', emoji: '📊', section: 'TOPIC_ANALYSIS', step: 1 },
  { key: 'titleOptions', title: '标题方案', emoji: '✏️', section: 'TITLE_OPTIONS', step: 2 },
  { key: 'hook', title: '开头钩子', emoji: '🪝', section: 'HOOK', step: 3 },
  { key: 'script', title: '视频脚本', emoji: '🎬', section: 'SCRIPT', step: 4 },
  { key: 'coverCopy', title: '封面文案', emoji: '🖼️', section: 'COVER_COPY', step: 8 },
  { key: 'publishCopy', title: '发布文案', emoji: '📢', section: 'PUBLISH_COPY', step: 8 },
  { key: 'imagePrompt', title: '图片提示词', emoji: '🎨', section: 'IMAGE_PROMPT', step: 7 },
  { key: 'videoPrompt', title: '视频提示词', emoji: '🎥', section: 'VIDEO_PROMPT', step: 7 }
]

const totalShots = computed(() => {
  if (!scriptData.value?.scenes) return 0
  let count = 0
  for (const scene of scriptData.value.scenes) {
    const sid = scene.id
    if (sid && sceneShots[sid]) {
      count += sceneShots[sid].length
    }
  }
  return count
})

const totalDuration = computed(() => {
  if (!scriptData.value?.scenes) return 0
  let total = 0
  for (const scene of scriptData.value.scenes) {
    total += parseFloat(scene.duration) || 0
    const sid = scene.id
    if (sid && sceneShots[sid]) {
      for (const shot of sceneShots[sid]) {
        total += parseFloat(shot.duration) || 0
      }
    }
  }
  return total
})

const totalWordCount = computed(() => {
  if (!scriptData.value?.scenes) return 0
  let count = 0
  for (const scene of scriptData.value.scenes) {
    count += (scene.description || scene.sceneDescription || '').length
    count += (scene.title || scene.sceneTitle || '').length
    const sid = scene.id
    if (sid && sceneShots[sid]) {
      for (const shot of sceneShots[sid]) {
        count += (shot.description || '').length
        count += (shot.dialogue || '').length
      }
    }
  }
  return count
})

const platformMap = { BILIBILI: 'B站', DOUYIN: '抖音', XIAOHONGSHU: '小红书' }
const statusMap = { 0: '草稿', 1: '已完成' }

function getContentText(key) {
  if (!content.value) return ''
  const val = content.value[key]
  if (!val) return ''
  if (typeof val === 'string') return val
  return JSON.stringify(val, null, 2)
}

function renderContent(data) {
  if (!data) return ''
  let text = typeof data === 'string' ? data : JSON.stringify(data, null, 2)
  try {
    if (text.trim().startsWith('{') && text.trim().endsWith('}')) {
      const obj = JSON.parse(text)
      return renderJsonObject(obj)
    }
  } catch (e) { /* ignore */ }
  text = text.replace(/```[\w]*\n?([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
  text = text.replace(/^### (.+)$/gm, '<h4>$1</h4>')
  text = text.replace(/^## (.+)$/gm, '<h3>$1</h3>')
  text = text.replace(/^# (.+)$/gm, '<h2>$1</h2>')
  text = text.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  text = text.replace(/\*(.+?)\*/g, '<em>$1</em>')
  text = text.replace(/^[-*] (.+)$/gm, '<li>$1</li>')
  text = text.replace(/(<li>[\s\S]*?<\/li>)+/g, (match) => `<ul>${match}</ul>`)
  text = text.replace(/\n{2,}/g, '</p><p>')
  text = text.replace(/\n/g, '<br>')
  text = '<p>' + text + '</p>'
  text = text.replace(/<p><\/p>/g, '')
  text = text.replace(/<p>(<h[234]>)/g, '$1')
  text = text.replace(/(<\/h[234]>)<[\/]p>/g, '$1')
  text = text.replace(/<p>(<ul>)/g, '$1')
  text = text.replace(/(<\/ul>)<[\/]p>/g, '$1')
  text = text.replace(/<p>(<pre>)/g, '$1')
  text = text.replace(/(<\/pre>)<[\/]p>/g, '$1')
  return text
}

function renderJsonObject(obj) {
  let html = '<div class="json-card">'
  for (const [key, value] of Object.entries(obj)) {
    const label = key.replace(/([A-Z])/g, ' $1').replace(/^./, s => s.toUpperCase()).trim()
    html += `<div class="json-field">`
    html += `<div class="json-label">${label}</div>`
    html += `<div class="json-value">${typeof value === 'object' ? JSON.stringify(value) : String(value)}</div>`
    html += `</div>`
  }
  html += '</div>'
  return html
}

function getPlatformLabel(p) { return platformMap[p] || p || '-' }
function getPlatformTagType(p) { return { BILIBILI: 'primary', DOUYIN: 'danger', XIAOHONGSHU: 'warning' }[p] || 'info' }
function getStatusLabel(s) { return statusMap[s] || s || '-' }
function getStatusTagType(s) { return { 0: 'info', 1: 'success' }[s] || 'info' }
function getVideoTypeLabel(t) { return { MARKETING: '营销类', KNOWLEDGE: '知识科普', STORY: '故事类', AUTO: '自动判断' }[t] || '自动判断' }
function getVisualStyleLabel(s) { return { AUTO: '自动', SIMPLE: '简约', TECH: '科技', ARTISTIC: '文艺', BUSINESS: '商务' }[s] || '自动' }

function getMaterialTagType(type) {
  const map = {
    PERSON: 'danger',
    CHARACTER: 'danger',
    PROP: 'warning',
    IMAGE: 'success',
    BACKGROUND: 'info',
    AUDIO: 'primary'
  }
  return map[type] || ''
}

function parseCharacterTags(tagStr) {
  if (!tagStr) return []
  if (Array.isArray(tagStr)) return tagStr
  try {
    const parsed = JSON.parse(tagStr)
    if (Array.isArray(parsed)) return parsed
  } catch (e) { /* ignore */ }
  return tagStr.split(',').map(t => t.trim()).filter(Boolean)
}

async function addCharacterTag(mat) {
  const inputVal = tagInputValues[mat.id]
  if (!inputVal || !inputVal.trim()) return
  const tags = parseCharacterTags(mat.characterTag)
  if (tags.includes(inputVal.trim())) {
    ElMessage.warning('标签已存在')
    return
  }
  tags.push(inputVal.trim())
  try {
    await request.put(`/frame/material/${mat.id}/tag`, { characterTag: JSON.stringify(tags) })
    mat.characterTag = JSON.stringify(tags)
    tagInputValues[mat.id] = ''
    ElMessage.success('标签添加成功')
  } catch (error) {
    ElMessage.error('标签添加失败')
  }
}

async function removeCharacterTag(mat, tagIdx) {
  const tags = parseCharacterTags(mat.characterTag)
  tags.splice(tagIdx, 1)
  try {
    await request.put(`/frame/material/${mat.id}/tag`, { characterTag: JSON.stringify(tags) })
    mat.characterTag = JSON.stringify(tags)
    ElMessage.success('标签已移除')
  } catch (error) {
    ElMessage.error('标签移除失败')
  }
}

async function generateSceneBg(scene) {
  sceneBgLoading[scene.id] = true
  try {
    await request.post(`/frame/scene/${scene.id}/background`)
    ElMessage.success('背景图生成任务已提交')
  } catch (error) {
    ElMessage.error('背景图生成失败')
  } finally {
    sceneBgLoading[scene.id] = false
  }
}

async function generateShotFrame(shot, type) {
  const key = `${shot.id}-${type}`
  frameLoading[key] = true
  try {
    await request.post(`/frame/shot/${shot.id}/${type}`)
    ElMessage.success(`${type === 'first-frame' ? '首帧' : '尾帧'}生成任务已提交`)
  } catch (error) {
    ElMessage.error(`${type === 'first-frame' ? '首帧' : '尾帧'}生成失败`)
  } finally {
    frameLoading[key] = false
  }
}

async function handleSceneExpand(activeNames) {
  for (const name of activeNames) {
    const scene = scriptData.value?.scenes?.find(s => (s.id || s.sceneId) === name)
    if (!scene) continue
    const sid = scene.id || scene.sceneId
    if (sceneLoaded[sid]) continue
    sceneLoaded[sid] = true
    try {
      const [shotsRes, matsRes] = await Promise.all([
        getSceneShotsApi(sid),
        getSceneMaterialsApi(sid)
      ])
      sceneShots[sid] = shotsRes.data || []
      sceneMaterials[sid] = matsRes.data || []
    } catch (error) {
      sceneShots[sid] = []
      sceneMaterials[sid] = []
    }
  }
}

async function fetchData() {
  pageLoading.value = true
  try {
    await projectStore.fetchProjectDetail(projectId)
    const [contentRes, scriptRes] = await Promise.all([
      getContentApi(projectId),
      getScriptApi(projectId)
    ])
    if (contentRes.data) {
      content.value = contentRes.data
    }
    if (scriptRes.data) {
      scriptData.value = scriptRes.data
    }
  } catch (error) {
    // errors handled in interceptors
  } finally {
    pageLoading.value = false
  }
}

async function handleFullGenerate() {
  try {
    await ElMessageBox.confirm(
      `确定要启动生成吗？\n模型：${modelOptions.find(m => m.value === selectedModel.value)?.label || selectedModel.value}`,
      '确认生成',
      { confirmButtonText: '开始生成', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }
  generating.value = true
  try {
    await startGenerationApi(projectId, selectedModel.value)
    ElMessage.success('生成任务已启动')
    router.push(`/project/${projectId}/generation`)
  } catch (error) {
    ElMessage.error('启动生成失败')
  } finally {
    generating.value = false
  }
}

async function handleSectionGenerate(section) {
  const sectionKey = section.key
  if (sectionKey === 'script') {
    try {
      await ElMessageBox.confirm(
        '确定要生成视频脚本吗？',
        '确认生成',
        { confirmButtonText: '生成', cancelButtonText: '取消', type: 'info' }
      )
    } catch { return }
    generatingSections['script'] = true
    try {
      await startGenerationApi(projectId, selectedModel.value)
      ElMessage.success('脚本生成任务已启动')
      await fetchScriptData()
    } catch (error) {
      ElMessage.error('脚本生成失败')
    } finally {
      generatingSections['script'] = false
    }
    return
  }
  generatingSections[sectionKey] = true
  try {
    const res = await regenerateStepApi(projectId, section.step, selectedModel.value)
    if (res.data) {
      content.value = res.data
    }
    ElMessage.success(`「${section.title}」生成成功`)
  } catch (error) {
    ElMessage.error(`生成失败`)
  } finally {
    generatingSections[sectionKey] = false
  }
}

async function fetchScriptData() {
  scriptLoading.value = true
  try {
    const res = await getScriptApi(projectId)
    if (res.data) {
      scriptData.value = res.data
    }
  } catch (error) {
    // handled
  } finally {
    scriptLoading.value = false
  }
}

function openEditDialog(section) {
  editingSection.value = section
  editContent.value = getContentText(section.key)
  editDialogVisible.value = true
}

async function handleSaveEdit() {
  if (!editingSection.value) return
  editSaving.value = true
  try {
    const res = await optimizeContentApi(
      projectId,
      editingSection.value.section,
      '请直接替换为以下用户提供的内容，不要做任何修改：\n\n' + editContent.value
    )
    if (res.data) {
      content.value = res.data
    }
    editDialogVisible.value = false
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    editSaving.value = false
  }
}

async function handleExport(type) {
  try {
    let res
    let filename
    switch (type) {
      case 'markdown':
        res = await exportMarkdownApi(projectId)
        filename = `${project.value?.title || 'export'}.md`
        break
      case 'json':
        res = await exportJsonApi(projectId)
        filename = `${project.value?.title || 'export'}.json`
        break
      case 'word':
        res = await exportWordApi(projectId)
        filename = `${project.value?.title || 'export'}.docx`
        break
    }
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

function scrollToScript() {
  nextTick(() => {
    const el = document.querySelector('.script-section-anchor')
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.project-detail-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;

  .title-group {
    display: flex;
    align-items: center;
    gap: 10px;

    h2 {
      font-size: 22px;
      color: #303133;
      font-weight: 600;
      margin: 0;
    }
  }
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.model-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e8f4fd 100%);
  border-radius: 8px;
  border: 1px solid #d6eaf8;

  .model-label {
    font-size: 14px;
    color: #606266;
    white-space: nowrap;
    font-weight: 500;
  }
}

.project-info-card {
  margin-bottom: 24px;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.sections-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: #f8f9fb;
  border-bottom: 1px solid #ebeef5;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;

  .section-icon {
    font-size: 20px;
  }
}

.section-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.section-body {
  padding: 20px;
  min-height: 60px;
}

.section-empty {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60px;

  .empty-text {
    color: #c0c4cc;
    font-size: 14px;
  }
}

.content-display {
  line-height: 1.8;
  color: #303133;
  font-size: 14px;

  :deep(strong) {
    color: #409eff;
  }

  :deep(ul) {
    padding-left: 20px;
    margin: 8px 0;

    li {
      margin-bottom: 4px;
    }
  }

  :deep(h2), :deep(h3), :deep(h4) {
    margin: 12px 0 6px;
    color: #303133;
  }

  :deep(pre) {
    background: #1e1e1e;
    color: #d4d4d4;
    padding: 14px;
    border-radius: 8px;
    overflow-x: auto;
    font-size: 13px;
    margin: 10px 0;
  }

  :deep(.json-card) {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  :deep(.json-field) {
    padding: 10px 14px;
    background: #f8f9fa;
    border-radius: 8px;
    border-left: 3px solid #409eff;
  }

  :deep(.json-label) {
    font-size: 12px;
    color: #909399;
    margin-bottom: 4px;
    font-weight: 600;
  }

  :deep(.json-value) {
    font-size: 14px;
    color: #303133;
  }
}

.script-summary {
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #d6eaf8;
}

.script-content {
  margin-top: 4px;
}

.scene-title-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;

  .scene-label {
    font-weight: 600;
    color: #303133;
    font-size: 14px;
  }

  .scene-name {
    color: #606266;
    font-size: 14px;
  }

  .scene-tag {
    margin-left: 4px;
  }
}

.scene-detail {
  padding: 8px 0;
}

.scene-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 16px;
  margin-bottom: 12px;

  .scene-info-item {
    display: flex;
    flex-direction: column;
    gap: 2px;

    .info-label {
      font-size: 12px;
      color: #909399;
      font-weight: 500;
    }

    .info-value {
      font-size: 13px;
      color: #303133;
      line-height: 1.5;
    }
  }
}

.scene-bg-action {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 10px 14px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px dashed #dcdfe6;

  .bg-prompt {
    font-size: 12px;
    color: #909399;
    line-height: 1.4;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.sub-section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.scene-shots-section {
  margin-bottom: 20px;
}

.shot-card {
  padding: 12px 16px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  margin-bottom: 4px;
}

.shot-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;

  .shot-label {
    font-weight: 600;
    font-size: 13px;
    color: #303133;
  }
}

.shot-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 6px;
}

.shot-dialogue {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
  padding: 6px 10px;
  background: #fff;
  border-radius: 4px;
  border-left: 2px solid #e4e7ed;

  .dialogue-label {
    font-weight: 500;
    color: #606266;
  }
}

.shot-frame-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
}

.frame-prompt {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
  padding: 6px 10px;
  background: #fff;
  border-radius: 4px;
  line-height: 1.5;

  span:last-child {
    flex: 1;
    word-break: break-all;
  }
}

.scene-materials-section {
  margin-top: 8px;
}

.materials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}

.material-card {
  padding: 12px 14px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.material-header {
  display: flex;
  align-items: center;
  gap: 8px;

  .material-name {
    font-size: 13px;
    font-weight: 600;
    color: #303133;
  }
}

.material-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.material-position {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #b0b4bb;
}

.material-tags {
  margin-top: 4px;

  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    margin-bottom: 6px;
  }

  .tag-input-row {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

:deep(.el-collapse) {
  border: none;
}

:deep(.el-collapse-item__header) {
  background: #f8f9fb;
  padding: 0 16px;
  border-radius: 8px;
  margin-bottom: 4px;
  font-size: 14px;
  height: 44px;
  line-height: 44px;
}

:deep(.el-collapse-item__wrap) {
  border: none;
  background: transparent;
}

:deep(.el-collapse-item__content) {
  padding: 12px 16px 16px;
}

:deep(.el-timeline) {
  padding-left: 0;
}

:deep(.el-timeline-item__tail) {
  border-left-color: #d6eaf8;
}

:deep(.el-timeline-item__node--primary) {
  background-color: #409eff;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
  color: #606266;
}
</style>
