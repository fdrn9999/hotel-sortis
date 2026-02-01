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
    }
  ]
})

// 네비게이션 가드
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 인증이 필요한 라우트
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      // 로그인되지 않았으면 로그인 페이지로
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }

    // 프로필 정보가 없으면 가져오기
    if (!authStore.user) {
      try {
        await authStore.fetchProfile()
      } catch (error) {
        // 프로필 조회 실패 시 로그아웃 및 로그인 페이지로
        authStore.logout()
        next({ name: 'login', query: { redirect: to.fullPath } })
        return
      }
    }
  }

  // 게스트 전용 페이지 (로그인, 회원가입)
  if (to.meta.guestOnly && authStore.isAuthenticated) {
    // 이미 로그인되어 있으면 홈으로
    next({ name: 'home' })
    return
  }

  next()
})

export default router
