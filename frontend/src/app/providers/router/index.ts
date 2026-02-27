import { createRouter, createWebHistory } from 'vue-router'

import HomePage from '@/pages/home/ui/HomePage.vue'
import AboutPage from '@/pages/about/ui/AboutPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
    },
    {
      path: '/about',
      name: 'about',
      component: AboutPage,
    },
  ],
})

export default router
