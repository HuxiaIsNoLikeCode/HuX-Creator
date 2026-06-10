import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  createProjectApi,
  getProjectsApi,
  getProjectDetailApi
} from '@/api/project'
import { ElMessage } from 'element-plus'

export const useProjectStore = defineStore('project', () => {
  const projectList = ref([])
  const currentProject = ref(null)
  const loading = ref(false)

  async function fetchProjects() {
    loading.value = true
    try {
      const res = await getProjectsApi()
      projectList.value = res.data
      return res
    } catch (error) {
      ElMessage.error('获取项目列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function fetchProjectDetail(id) {
    loading.value = true
    try {
      const res = await getProjectDetailApi(id)
      currentProject.value = res.data
      return res
    } catch (error) {
      ElMessage.error('获取项目详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function createProject(projectData) {
    loading.value = true
    try {
      const res = await createProjectApi(projectData)
      ElMessage.success('项目创建成功')
      return res
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '创建项目失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  return {
    projectList,
    currentProject,
    loading,
    fetchProjects,
    fetchProjectDetail,
    createProject
  }
})
