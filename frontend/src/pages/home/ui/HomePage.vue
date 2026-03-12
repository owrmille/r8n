<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <main class="home-page">
    <h1>Home</h1>
    <RouterLink class="link" to="/auth">Open Login/Register</RouterLink>
    <RouterLink class="link" to="/dashboard">Open Dashboard</RouterLink>
    <RouterLink class="link" to="/opinion-list">Open Opinion List</RouterLink>
    <RouterLink class="link" to="/requests">Open Requests</RouterLink>
    <RouterLink class="link" to="/account">Open Account</RouterLink>
    <RouterLink class="link" to="/legal/privacy">Open Privacy Policy</RouterLink>
    <RouterLink class="link" to="/legal/terms">Open Terms of Service</RouterLink>
    <RouterLink class="link" to="/api-debug">Open API Debug</RouterLink>
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

.link {
  text-decoration: underline;
}
</style>
