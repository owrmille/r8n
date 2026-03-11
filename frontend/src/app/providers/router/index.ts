import { createRouter, createWebHistory } from 'vue-router'

import HomePage from '@/pages/home/ui/HomePage.vue'
import ApiDebugPage from '@/pages/api-debug/ui/ApiDebugPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
    },
    {
      path: '/api-debug',
      name: 'api-debug',
      component: ApiDebugPage,
    },
  ],
})

export default router
