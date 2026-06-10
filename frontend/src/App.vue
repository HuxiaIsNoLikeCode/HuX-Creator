<template>
  <div id="app">
    <NavBar v-if="showNav" />
    <main :class="{ 'has-nav': showNav }">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '@/components/NavBar.vue'

const route = useRoute()
const noNavRoutes = ['/login', '/register']

const showNav = computed(() => !noNavRoutes.includes(route.path))
</script>

<style lang="scss" scoped>
.has-nav {
  padding-top: 60px;
}
</style>
