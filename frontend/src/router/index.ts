import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: false }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { requiresAuth: false, guestOnly: true }
    },
    {
      path: '/signup',
      name: 'signup',
      component: () => import('@/views/SignupView.vue'),
      meta: { requiresAuth: false, guestOnly: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/campaign',
      name: 'campaign',
      component: () => import('@/views/CampaignView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/campaign/loadout',
      name: 'campaign-loadout',
      component: () => import('@/views/SkillLoadoutView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/battle',
      name: 'battle',
      component: () => import('@/views/BattleView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/pvp',
      name: 'pvp',
      component: () => import('@/views/PvPView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/pvp/matchmaking',
      name: 'pvp-matchmaking',
      component: () => import('@/views/PvPMatchmakingView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/pvp/rank',
      name: 'pvp-rank',
      component: () => import('@/views/RankView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/pvp/draft',
      name: 'pvp-draft',
      component: () => import('@/views/DraftView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/collection',
      name: 'collection',
      component: () => import('@/views/CollectionView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/shop',
      name: 'shop',
      component: () => import('@/views/ShopView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('@/views/SettingsView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/tutorial',
      name: 'tutorial',
      component: () => import('@/views/TutorialView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/practice',
      name: 'practice',
      component: () => import('@/views/PracticeView.vue'),
      meta: { requiresAuth: false }
    }
  ]
})

// Navigation guard
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  // Routes requiring authentication
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      // Not logged in - redirect to login page
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }

    // Fetch profile if not available
    if (!authStore.user) {
      try {
        await authStore.fetchProfile()
      } catch (error) {
        // Profile fetch failed - logout and redirect to login
        authStore.logout()
        next({ name: 'login', query: { redirect: to.fullPath } })
        return
      }
    }
  }

  // Guest-only pages (login, signup)
  if (to.meta.guestOnly && authStore.isAuthenticated) {
    // Already logged in - redirect to home
    next({ name: 'home' })
    return
  }

  next()
})

export default router
