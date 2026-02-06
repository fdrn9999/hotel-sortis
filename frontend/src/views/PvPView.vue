<template>
  <div class="pvp-view">
    <!-- Navigation Bar -->
    <AppNavigation
      :title="t('pvp.title')"
      :show-home="true"
      :show-back="true"
      :show-settings="true"
    />

    <div class="pvp-container">
      <h1 class="title">{{ t('pvp.title') }}</h1>

      <!-- 로딩 -->
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>{{ t('common.loading') }}</p>
      </div>

      <!-- PvP 대시보드 -->
      <div v-else-if="rankInfo" class="pvp-dashboard">
        <!-- 티어 배지 -->
        <div class="tier-badge" :style="{ borderColor: tierColor, boxShadow: tierGlow }">
          <div class="tier-icon" :style="{ color: tierColor }">
            {{ tierEmoji }}
          </div>
          <h2 class="tier-name" :style="{ color: tierColor }">
            {{ t(`pvp.tiers.${rankInfo.tier.toLowerCase()}`) }}
          </h2>
          <div class="elo-display">
            <span class="label">{{ t('pvp.elo') }}</span>
            <span class="elo-value">{{ rankInfo.elo }}</span>
          </div>
        </div>

        <!-- 전적 요약 -->
        <div class="stats-summary">
          <div class="stat-item">
            <div class="stat-label">{{ t('pvp.wins') }}</div>
            <div class="stat-value wins">{{ rankInfo.wins }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">{{ t('pvp.losses') }}</div>
            <div class="stat-value losses">{{ rankInfo.losses }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">{{ t('pvp.draws') }}</div>
            <div class="stat-value draws">{{ rankInfo.draws }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">{{ t('pvp.winRate') }}</div>
            <div class="stat-value winrate">{{ winRatePercentage }}%</div>
          </div>
        </div>

        <!-- 버튼들 -->
        <div class="action-buttons">
          <button class="btn-primary btn-large" @click="startMatchmaking">
            {{ t('pvp.matchmaking.start') }}
          </button>

          <button class="btn-secondary" @click="viewRankDetails">
            {{ t('pvp.viewRankDetails') }}
          </button>
        </div>
      </div>

      <!-- 에러 -->
      <div v-else-if="error" class="error-message">
        {{ error }}
        <button class="btn-retry" @click="loadRankInfo">
          {{ t('common.retry') }}
        </button>
      </div>

      <!-- 빈 상태 (데이터 없음) -->
      <div v-else class="empty-state">
        <div class="empty-icon">&#x2694;&#xFE0F;</div>
        <p class="empty-title">{{ t('pvp.noRankData') }}</p>
        <p class="empty-description">{{ t('pvp.noRankDataHint') }}</p>
        <button class="btn-primary btn-large" @click="startMatchmaking">
          {{ t('pvp.matchmaking.start') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getRankInfo, getTierColor } from '@/api/pvp'
import type { RankInfoResponse } from '@/types/game'
import AppNavigation from '@/components/AppNavigation.vue'

const router = useRouter()
const { t } = useI18n()

// State
const loading = ref(true)
const error = ref('')
const rankInfo = ref<RankInfoResponse | null>(null)

// Player ID (temporary - should be from auth in production)
const playerId = ref(1)

// Computed properties
const tierColor = computed(() => {
  if (!rankInfo.value) return '#808080'
  return getTierColor(rankInfo.value.tier)
})

const tierGlow = computed(() => {
  return `0 0 30px ${tierColor.value}80`
})

const tierEmoji = computed(() => {
  if (!rankInfo.value) return '🎖️'
  const emojis: Record<string, string> = {
    BRONZE: '🥉',
    SILVER: '🥈',
    GOLD: '🥇',
    PLATINUM: '💎',
    DIAMOND: '💠',
    MASTER: '👑'
  }
  return emojis[rankInfo.value.tier] || '🎖️'
})

const winRatePercentage = computed(() => {
  if (!rankInfo.value) return 0
  return Math.round(rankInfo.value.winRate * 100)
})

onMounted(() => {
  loadRankInfo()
})

/**
 * Load rank info
 */
const loadRankInfo = async () => {
  loading.value = true
  error.value = ''

  try {
    rankInfo.value = await getRankInfo(playerId.value)
  } catch (err: any) {
    error.value = err.response?.data?.message || t('pvp.loadFailed')
  } finally {
    loading.value = false
  }
}

/**
 * Start matchmaking
 */
const startMatchmaking = () => {
  router.push({ name: 'pvp-matchmaking' })
}

/**
 * View rank details
 */
const viewRankDetails = () => {
  router.push({ name: 'pvp-rank' })
}

</script>

<style scoped>
.pvp-view {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--color-dark-navy) 0%, var(--color-black) 100%);
  color: var(--color-cream);
  padding: 20px;
}

.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.nav-btn {
  background: rgba(var(--color-gold-rgb), 0.2);
  border: 1px solid var(--color-gold);
  color: var(--color-gold);
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.nav-btn:hover {
  background: rgba(var(--color-gold-rgb), 0.3);
  transform: translateY(-2px);
}

.pvp-container {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}

.title {
  font-size: 48px;
  color: var(--color-gold);
  margin-bottom: 40px;
  text-shadow: 0 0 20px rgba(var(--color-gold-rgb), 0.5);
}

.loading {
  padding: 60px 20px;
}

.spinner {
  width: 80px;
  height: 80px;
  border: 8px solid rgba(var(--color-gold-rgb), 0.2);
  border-top-color: var(--color-gold);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 30px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.pvp-dashboard {
  background: rgba(var(--color-dark-navy-rgb), 0.5);
  border: 1px solid var(--color-gold);
  border-radius: 20px;
  padding: 40px;
  backdrop-filter: blur(10px);
}

.tier-badge {
  border: 3px solid;
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 40px;
  transition: all 0.3s;
}

.tier-icon {
  font-size: 80px;
  margin-bottom: 10px;
}

.tier-name {
  font-size: 36px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin-bottom: 20px;
}

.elo-display {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  font-size: 20px;
}

.label {
  color: var(--color-silver);
}

.elo-value {
  color: var(--color-gold);
  font-size: 32px;
  font-weight: bold;
}

.stats-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

.stat-item {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 10px;
  padding: 20px;
}

.stat-label {
  color: var(--color-silver);
  font-size: 14px;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
}

.wins {
  color: var(--color-emerald);
}

.losses {
  color: var(--color-error);
}

.draws {
  color: var(--color-gold-light);
}

.winrate {
  color: var(--color-gold);
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.btn-primary,
.btn-secondary {
  padding: 15px 40px;
  font-size: 20px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: bold;
}

.btn-primary {
  background: linear-gradient(135deg, var(--color-gold) 0%, var(--color-gold-light) 100%);
  color: var(--color-dark-navy);
}

.btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(var(--color-gold-rgb), 0.5);
}

.btn-secondary {
  background: rgba(var(--color-gold-rgb), 0.2);
  border: 1px solid var(--color-gold);
  color: var(--color-gold);
}

.btn-secondary:hover {
  background: rgba(var(--color-gold-rgb), 0.3);
}

.error-message {
  background: rgba(var(--color-velvet-red-rgb), 0.3);
  border: 1px solid var(--color-velvet-red);
  color: var(--color-error);
  padding: 30px;
  border-radius: 10px;
}

.btn-retry {
  margin-top: 20px;
  padding: 10px 30px;
  background: rgba(var(--color-gold-rgb), 0.2);
  border: 1px solid var(--color-gold);
  color: var(--color-gold);
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.btn-retry:hover {
  background: rgba(var(--color-gold-rgb), 0.3);
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  text-align: center;
  gap: 12px;
}

.empty-icon {
  font-size: 64px;
  opacity: 0.6;
}

.empty-title {
  font-size: 20px;
  font-weight: bold;
  color: var(--color-gold);
}

.empty-description {
  font-size: 14px;
  color: rgba(var(--color-cream-rgb), 0.6);
  max-width: 400px;
  margin-bottom: 12px;
}

/* Responsive */
@media (max-width: 768px) {
  .stats-summary {
    grid-template-columns: repeat(2, 1fr);
  }

  .tier-name {
    font-size: 28px;
  }

  .tier-icon {
    font-size: 60px;
  }
}
</style>
