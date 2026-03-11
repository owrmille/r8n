<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <main class="home-page">
    <h1>Home</h1>
    <RouterLink class="api-debug-link" to="/api-debug">Open API Debug</RouterLink>
  </main>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
  display: grid;
  place-content: center;
  justify-items: center;
  gap: 0.75rem;
}

.api-debug-link {
  text-decoration: underline;
}
</style>
