<template>
  <div class="navbar">
    <div class="navbar-content">
      <div class="navbar-left">
        <router-link to="/" class="logo">
          <el-icon :size="24"><VideoCamera /></el-icon>
          <span>HuX Creator</span>
        </router-link>
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :ellipsis="false"
          class="nav-menu"
          router
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/project">我的项目</el-menu-item>
        </el-menu>
      </div>
      <div class="navbar-right">
        <el-dropdown v-if="userStore.userInfo" @command="handleCommand">
          <div class="user-info">
            <el-avatar :size="32" class="user-avatar">
              {{ userStore.userInfo.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <span class="username">{{ userStore.userInfo.username }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <template v-else>
          <el-button @click="$router.push('/login')">登录</el-button>
          <el-button type="primary" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => {
  if (route.path.startsWith('/project')) return '/project'
  return route.path
})

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
  }
}

onMounted(() => {
  if (userStore.token && !userStore.userInfo) {
    userStore.fetchUserInfo()
  }
})
</script>

<style lang="scss" scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 1000;
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  white-space: nowrap;

  .el-icon {
    color: #409eff;
  }
}

.nav-menu {
  border-bottom: none;

  :deep(.el-menu-item) {
    font-size: 15px;
    height: 60px;
    line-height: 60px;
  }
}

.navbar-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;

  &:hover {
    background: #f5f7fa;
  }
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
}

.username {
  font-size: 14px;
  color: #303133;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
