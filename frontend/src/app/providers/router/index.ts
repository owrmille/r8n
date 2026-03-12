import { createRouter, createWebHistory } from 'vue-router'

import HomePage from '@/pages/home/ui/HomePage.vue'
import ApiDebugPage from '@/pages/api-debug/ui/ApiDebugPage.vue'
import AuthLoginPage from '@/pages/auth-login/ui/AuthLoginPage.vue'
import DashboardPage from '@/pages/dashboard/ui/DashboardPage.vue'
import OpinionListPage from '@/pages/opinion-list/ui/OpinionListPage.vue'
import RequestsPage from '@/pages/access-requests/ui/RequestsPage.vue'
import AccountPage from '@/pages/account/ui/AccountPage.vue'
import PrivacyPage from '@/pages/legal-privacy/ui/PrivacyPage.vue'
import TermsPage from '@/pages/legal-terms/ui/TermsPage.vue'

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
    {
      path: '/auth',
      name: 'auth',
      component: AuthLoginPage,
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardPage,
    },
    {
      path: '/opinion-list',
      name: 'opinion-list',
      component: OpinionListPage,
    },
    {
      path: '/requests',
      name: 'requests',
      component: RequestsPage,
    },
    {
      path: '/account',
      name: 'account',
      component: AccountPage,
    },
    {
      path: '/legal/privacy',
      name: 'legal-privacy',
      component: PrivacyPage,
    },
    {
      path: '/legal/terms',
      name: 'legal-terms',
      component: TermsPage,
    },
  ],
})

export default router
