import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/project',
    name: 'ProjectList',
    component: () => import('@/views/ProjectList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/project/create',
    name: 'ProjectCreate',
    component: () => import('@/views/ProjectCreate.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/project/:id',
    name: 'ProjectDetail',
    component: () => import('@/views/ProjectDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/project/:id/generate',
    name: 'ContentGenerate',
    component: () => import('@/views/ContentGenerate.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/project/:id/generation',
    name: 'GenerationProgress',
    component: () => import('@/views/GenerationProgress.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/project/:id/script',
    name: 'ScriptDetail',
    component: () => import('@/views/ScriptDetail.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
