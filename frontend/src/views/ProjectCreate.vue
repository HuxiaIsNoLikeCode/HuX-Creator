<template>
  <div class="project-create-container">
    <div class="page-header">
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>创建新项目</h2>
    </div>

    <el-card class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="top"
      >
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="项目标题" prop="title">
              <el-input
                v-model="form.title"
                placeholder="请输入视频标题"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="目标平台" prop="platform">
              <el-select v-model="form.platform" placeholder="请选择平台" style="width: 100%">
                <el-option label="B站" value="BILIBILI" />
                <el-option label="抖音" value="DOUYIN" />
                <el-option label="小红书" value="XIAOHONGSHU" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="目标用户" prop="targetUser">
              <el-select v-model="form.targetUser" placeholder="请选择目标用户" style="width: 100%">
                <el-option label="18-24岁 年轻用户" value="18-24" />
                <el-option label="25-34岁 职场人士" value="25-34" />
                <el-option label="35-44岁 中年用户" value="35-44" />
                <el-option label="全年龄段" value="ALL" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="视频时长" prop="duration">
              <el-select v-model="form.duration" placeholder="请选择时长" style="width: 100%">
                <el-option label="60秒（短视频）" :value="60" />
                <el-option label="90秒（标准）" :value="90" />
                <el-option label="180秒（中长视频）" :value="180" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="内容风格" prop="style">
              <el-select v-model="form.style" placeholder="请选择风格" style="width: 100%">
                <el-option label="教学类" value="TEACHING" />
                <el-option label="科普类" value="SCIENCE" />
                <el-option label="震惊类" value="SHOCKING" />
                <el-option label="故事类" value="STORY" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="项目描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="4"
                placeholder="请描述视频的核心内容、主题方向或参考案例"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="视频类型" prop="videoType">
              <el-radio-group v-model="form.videoType">
                <el-radio value="AUTO">自动判断</el-radio>
                <el-radio value="MARKETING">营销类</el-radio>
                <el-radio value="KNOWLEDGE">知识科普</el-radio>
                <el-radio value="STORY">故事类</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="叙事节奏" prop="narrativeRhythm">
              <div class="slider-wrapper">
                <el-slider
                  v-model="form.narrativeRhythm"
                  :min="0.6"
                  :max="0.8"
                  :step="0.05"
                  :format-tooltip="(val) => val.toFixed(2)"
                  style="flex: 1"
                />
                <span class="slider-value">{{ form.narrativeRhythm.toFixed(2) }}</span>
              </div>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="视觉风格" prop="visualStyle">
              <el-select v-model="form.visualStyle" placeholder="请选择视觉风格" style="width: 100%">
                <el-option label="自动" value="AUTO" />
                <el-option label="简约" value="SIMPLE" />
                <el-option label="科技" value="TECH" />
                <el-option label="文艺" value="ARTISTIC" />
                <el-option label="商务" value="BUSINESS" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="生成模式" prop="generationMode">
              <el-radio-group v-model="form.generationMode" class="generation-mode-group">
                <el-radio value="QUICK" class="generation-mode-radio">
                  <span class="mode-title">快速模式</span>
                  <span class="mode-desc">生成速度更快，适合快速出稿</span>
                </el-radio>
                <el-radio value="PROFESSIONAL" class="generation-mode-radio">
                  <span class="mode-title">专业模式</span>
                  <span class="mode-desc">质量更高，适合精品内容制作</span>
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-actions">
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            创建项目
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/project'

const router = useRouter()
const projectStore = useProjectStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  title: '',
  platform: '',
  targetUser: '',
  duration: null,
  style: '',
  description: '',
  videoType: 'AUTO',
  narrativeRhythm: 0.7,
  visualStyle: 'AUTO',
  generationMode: 'QUICK'
})

const rules = {
  title: [
    { required: true, message: '请输入项目标题', trigger: 'blur' }
  ],
  platform: [
    { required: true, message: '请选择目标平台', trigger: 'change' }
  ],
  targetUser: [
    { required: true, message: '请选择目标用户', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请选择视频时长', trigger: 'change' }
  ],
  style: [
    { required: true, message: '请选择内容风格', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入项目描述', trigger: 'blur' }
  ],
  videoType: [
    { required: true, message: '请选择视频类型', trigger: 'change' }
  ],
  narrativeRhythm: [
    { required: true, message: '请设置叙事节奏', trigger: 'change' }
  ],
  visualStyle: [
    { required: true, message: '请选择视觉风格', trigger: 'change' }
  ],
  generationMode: [
    { required: true, message: '请选择生成模式', trigger: 'change' }
  ]
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await projectStore.createProject(form)
        router.push(`/project/${res.data.id}`)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.project-create-container {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;

  h2 {
    font-size: 24px;
    color: #303133;
    font-weight: 600;
  }
}

.form-card {
  border-radius: 12px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.slider-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.slider-value {
  min-width: 40px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
}

.generation-mode-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.generation-mode-radio {
  height: auto !important;
  padding: 12px 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-right: 0 !important;
  transition: border-color 0.2s;

  &:hover {
    border-color: #409eff;
  }

  :deep(.el-radio__input.is-checked + .el-radio__label) {
    color: #303133;
  }
}

.mode-title {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.mode-desc {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
