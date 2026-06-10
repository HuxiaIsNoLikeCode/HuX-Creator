import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi, registerApi, getUserInfoApi } from '@/api/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  async function login(loginForm) {
    try {
      const res = await loginApi(loginForm)
      token.value = res.data.token
      localStorage.setItem('token', res.data.token)
      await fetchUserInfo()
      ElMessage.success('登录成功')
      router.push('/project')
      return res
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '登录失败')
      throw error
    }
  }

  async function register(registerForm) {
    try {
      const res = await registerApi(registerForm)
      ElMessage.success('注册成功，请登录')
      router.push('/login')
      return res
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '注册失败')
      throw error
    }
  }

  async function fetchUserInfo() {
    try {
      const res = await getUserInfoApi()
      userInfo.value = res.data
      return res
    } catch (error) {
      return null
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    router.push('/login')
  }

  return {
    token,
    userInfo,
    login,
    register,
    fetchUserInfo,
    logout
  }
})
