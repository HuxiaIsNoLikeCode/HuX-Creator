<template>
  <div class="project-list-container">
    <div class="page-header">
      <h2>我的项目</h2>
      <el-button type="primary" @click="$router.push('/project/create')">
        <el-icon><Plus /></el-icon>
        创建项目
      </el-button>
    </div>

    <el-table
      v-loading="projectStore.loading"
      :data="projectStore.projectList"
      class="project-table"
      @row-click="handleRowClick"
    >
      <el-table-column prop="title" label="项目标题" min-width="200">
        <template #default="{ row }">
          <span class="project-title">{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="platform" label="平台" width="120">
        <template #default="{ row }">
          <el-tag :type="getPlatformTagType(row.platform)" size="small">
            {{ getPlatformLabel(row.platform) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)" size="small">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="duration" label="时长" width="100">
        <template #default="{ row }">
          {{ row.duration }}秒
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click.stop="handleView(row)">
            查看
          </el-button>
          <el-button type="danger" link @click.stop="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!projectStore.loading && projectStore.projectList.length === 0" class="empty-state">
      <el-empty description="暂无项目">
        <el-button type="primary" @click="$router.push('/project/create')">
          创建第一个项目
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/project'
import { deleteProjectApi } from '@/api/project'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()
const projectStore = useProjectStore()

onMounted(() => {
  projectStore.fetchProjects()
})

const platformMap = {
  BILIBILI: 'B站',
  DOUYIN: '抖音',
  XIAOHONGSHU: '小红书'
}

const statusMap = {
  0: '草稿',
  1: '已完成'
}

function getPlatformLabel(platform) {
  return platformMap[platform] || platform
}

function getPlatformTagType(platform) {
  const map = { BILIBILI: 'primary', DOUYIN: 'danger', XIAOHONGSHU: 'warning' }
  return map[platform] || 'info'
}

function getStatusLabel(status) {
  return statusMap[status] || status
}

function getStatusTagType(status) {
  const map = { 0: 'info', 1: 'success' }
  return map[status] || 'info'
}

function handleRowClick(row) {
  router.push(`/project/${row.id}`)
}

function handleView(row) {
  router.push(`/project/${row.id}`)
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除这个项目吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await deleteProjectApi(row.id)
    ElMessage.success('删除成功')
    projectStore.fetchProjects()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style lang="scss" scoped>
.project-list-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h2 {
    font-size: 24px;
    color: #303133;
    font-weight: 600;
  }
}

.project-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.project-title {
  font-weight: 500;
  color: #303133;
  cursor: pointer;

  &:hover {
    color: #409eff;
  }
}

.empty-state {
  padding: 60px 0;
}
</style>
