import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/campaign',
      name: 'campaign',
      component: () => import('@/views/CampaignView.vue')
    },
    {
      path: '/campaign/loadout',
      name: 'campaign-loadout',
      component: () => import('@/views/SkillLoadoutView.vue')
    },
    {
      path: '/battle',
      name: 'battle',
      component: () => import('@/views/BattleView.vue')
    },
    {
      path: '/pvp',
      name: 'pvp',
      component: () => import('@/views/PvPView.vue')
    },
    {
      path: '/collection',
      name: 'collection',
      component: () => import('@/views/CollectionView.vue')
    },
    {
      path: '/shop',
      name: 'shop',
      component: () => import('@/views/ShopView.vue')
    }
  ]
})

export default router
