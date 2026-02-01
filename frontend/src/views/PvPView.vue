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

// 상태
const loading = ref(true)
const error = ref('')
const rankInfo = ref<RankInfoResponse | null>(null)

// 플레이어 ID (임시 - 실제로는 인증에서 가져와야 함)
const playerId = ref(1)

// 계산된 속성
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
 * 랭크 정보 로드
 */
const loadRankInfo = async () => {
  loading.value = true
  error.value = ''

  try {
    rankInfo.value = await getRankInfo(playerId.value)
  } catch (err: any) {
    console.error('Failed to load rank info:', err)
    error.value = err.response?.data?.message || 'Failed to load rank information'
  } finally {
    loading.value = false
  }
}

/**
 * 매칭 시작
 */
const startMatchmaking = () => {
  router.push({ name: 'pvp-matchmaking' })
}

/**
 * 랭크 상세 보기
 */
const viewRankDetails = () => {
  router.push({ name: 'pvp-rank' })
}

</script>

<style scoped>
.pvp-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #1b1b27 0%, #0d0d0d 100%);
  color: #fffdd0;
  padding: 20px;
}

.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.nav-btn {
  background: rgba(212, 175, 55, 0.2);
  border: 1px solid #d4af37;
  color: #d4af37;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.nav-btn:hover {
  background: rgba(212, 175, 55, 0.3);
  transform: translateY(-2px);
}

.pvp-container {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}

.title {
  font-size: 48px;
  color: #d4af37;
  margin-bottom: 40px;
  text-shadow: 0 0 20px rgba(212, 175, 55, 0.5);
}

.loading {
  padding: 60px 20px;
}

.spinner {
  width: 80px;
  height: 80px;
  border: 8px solid rgba(212, 175, 55, 0.2);
  border-top-color: #d4af37;
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
  background: rgba(27, 27, 39, 0.5);
  border: 1px solid #d4af37;
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
  color: #c0c0c0;
}

.elo-value {
  color: #d4af37;
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
  color: #c0c0c0;
  font-size: 14px;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
}

.wins {
  color: #50c878;
}

.losses {
  color: #ff6b6b;
}

.draws {
  color: #ffd700;
}

.winrate {
  color: #d4af37;
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
  background: linear-gradient(135deg, #d4af37 0%, #ffd700 100%);
  color: #1b1b27;
}

.btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(212, 175, 55, 0.5);
}

.btn-secondary {
  background: rgba(212, 175, 55, 0.2);
  border: 1px solid #d4af37;
  color: #d4af37;
}

.btn-secondary:hover {
  background: rgba(212, 175, 55, 0.3);
}

.error-message {
  background: rgba(139, 0, 0, 0.3);
  border: 1px solid #8b0000;
  color: #ff6b6b;
  padding: 30px;
  border-radius: 10px;
}

.btn-retry {
  margin-top: 20px;
  padding: 10px 30px;
  background: rgba(212, 175, 55, 0.2);
  border: 1px solid #d4af37;
  color: #d4af37;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.btn-retry:hover {
  background: rgba(212, 175, 55, 0.3);
}

/* 반응형 */
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
