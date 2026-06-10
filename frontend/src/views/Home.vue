<template>
  <div class="home-container">
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="gradient-text">HuX Creator</span>
        </h1>
        <p class="hero-subtitle">AI 驱动的视频内容规划平台</p>
        <p class="hero-desc">
          智能生成视频脚本、分镜头、素材建议和发布文案，让创作更高效
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/project/create')">
            <el-icon><Plus /></el-icon>
            创建新项目
          </el-button>
          <el-button size="large" @click="$router.push('/project')">
            查看我的项目
          </el-button>
        </div>
      </div>
    </div>

    <div class="features-section">
      <h2 class="section-title">核心功能</h2>
      <div class="features-grid">
        <div v-for="feature in features" :key="feature.title" class="feature-card">
          <div class="feature-icon">
            <el-icon :size="32"><component :is="feature.icon" /></el-icon>
          </div>
          <h3>{{ feature.title }}</h3>
          <p>{{ feature.desc }}</p>
        </div>
      </div>
    </div>

    <div class="platforms-section">
      <h2 class="section-title">支持平台</h2>
      <div class="platforms-grid">
        <div v-for="platform in platforms" :key="platform.name" class="platform-card">
          <span class="platform-name">{{ platform.name }}</span>
          <span class="platform-desc">{{ platform.desc }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const features = [
  {
    icon: 'Document',
    title: '智能选题分析',
    desc: '基于平台热点和用户画像，智能推荐高潜力选题方向'
  },
  {
    icon: 'Edit',
    title: '脚本生成',
    desc: 'AI 自动生成专业视频脚本，包含开头钩子、正文和结尾'
  },
  {
    icon: 'Film',
    title: '分镜设计',
    desc: '自动生成分镜头脚本，包含画面描述和时间分配'
  },
  {
    icon: 'Picture',
    title: '素材建议',
    desc: '智能推荐配套素材，包括图片、视频片段和音效'
  },
  {
    icon: 'Promotion',
    title: '发布文案',
    desc: '一键生成标题、封面文案和发布描述，优化点击率'
  },
  {
    icon: 'DataAnalysis',
    title: '数据优化',
    desc: '根据历史数据持续优化内容策略，提升传播效果'
  }
]

const platforms = [
  { name: 'B站', desc: '适合 60-180 秒中长视频' },
  { name: '抖音', desc: '适合 60 秒短视频' },
  { name: '小红书', desc: '适合图文和短视频内容' }
]
</script>

<style lang="scss" scoped>
.home-container {
  min-height: 100vh;
}

.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 80px 20px;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 56px;
  margin-bottom: 16px;
  font-weight: 800;
}

.gradient-text {
  background: linear-gradient(135deg, #fff 0%, #e0e7ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 24px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 12px;
}

.hero-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.75);
  margin-bottom: 40px;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;

  .el-button {
    padding: 12px 32px;
    font-size: 16px;
  }
}

.features-section,
.platforms-section {
  padding: 80px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  text-align: center;
  font-size: 32px;
  color: #303133;
  margin-bottom: 48px;
  font-weight: 700;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.feature-card {
  padding: 32px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s, box-shadow 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  .feature-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20px;
    color: #fff;
  }

  h3 {
    font-size: 20px;
    color: #303133;
    margin-bottom: 12px;
    font-weight: 600;
  }

  p {
    color: #606266;
    line-height: 1.6;
    font-size: 14px;
  }
}

.platforms-section {
  background: #f5f7fa;
  padding: 80px 20px;
  max-width: 100%;
}

.platforms-grid {
  display: flex;
  justify-content: center;
  gap: 32px;
  max-width: 900px;
  margin: 0 auto;
}

.platform-card {
  flex: 1;
  padding: 32px;
  background: #fff;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  .platform-name {
    display: block;
    font-size: 24px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 8px;
  }

  .platform-desc {
    color: #909399;
    font-size: 14px;
  }
}
</style>
