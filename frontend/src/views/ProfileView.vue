<template>
  <div class="profile-view">
    <!-- Navigation Bar -->
    <AppNavigation
      :title="$t('profile.myProfile')"
      :show-home="true"
      :show-back="true"
      :show-settings="false"
    />

    <div class="profile-container">
      <h1 class="page-title">{{ $t('profile.myProfile') }}</h1>

      <!-- Tier & Stats Section -->
      <div v-if="authStore.user" class="tier-stats-section">
        <!-- Tier Badge with Progress -->
        <div class="tier-badge-container">
          <div class="tier-badge" :style="{ borderColor: tierColor, boxShadow: tierGlow }">
            <span class="tier-emoji">{{ tierEmoji }}</span>
            <span class="tier-name" :style="{ color: tierColor }">
              {{ $t(`pvp.tiers.${currentTier.toLowerCase()}`) }}
            </span>
            <span class="elo-value">{{ authStore.user.elo }}</span>
          </div>
          <!-- Progress to next tier -->
          <div v-if="currentTier !== 'MASTER'" class="tier-progress">
            <div class="progress-label">
              <span>{{ $t('profile.tierProgress') }}</span>
              <span>{{ tierProgress }}%</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: `${tierProgress}%`, background: tierColor }"></div>
            </div>
            <div class="progress-range">
              <span>{{ tierMin }}</span>
              <span>{{ tierMax }}</span>
            </div>
          </div>
          <div v-else class="master-badge">
            {{ $t('profile.masterRank') }}
          </div>
        </div>

        <!-- PvP Stats -->
        <div class="pvp-stats">
          <h3 class="stats-title">{{ $t('profile.pvpStats') }}</h3>
          <div v-if="statsLoading" class="stats-loading">
            <LoadingSpinner />
          </div>
          <div v-else-if="stats" class="stats-grid">
            <div class="stat-item">
              <span class="stat-value wins">{{ stats.wins }}</span>
              <span class="stat-label">{{ $t('pvp.wins') }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-value losses">{{ stats.losses }}</span>
              <span class="stat-label">{{ $t('pvp.losses') }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-value draws">{{ stats.draws }}</span>
              <span class="stat-label">{{ $t('pvp.draws') }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-value winrate">{{ winRatePercentage }}%</span>
              <span class="stat-label">{{ $t('pvp.winRate') }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Match History Section -->
      <div class="match-history-section">
        <h2 class="section-title">{{ $t('profile.matchHistory') }}</h2>
        <div v-if="historyLoading" class="history-loading">
          <LoadingSpinner />
        </div>
        <div v-else-if="matchHistory.length === 0" class="empty-history">
          <p>{{ $t('profile.noMatches') }}</p>
        </div>
        <div v-else class="history-list">
          <div
            v-for="match in matchHistory"
            :key="match.battleId"
            class="history-item"
            :class="match.result.toLowerCase()"
          >
            <div class="match-result">
              <span class="result-badge" :class="match.result.toLowerCase()">
                {{ $t(`pvp.result.${match.result.toLowerCase()}`) }}
              </span>
            </div>
            <div class="match-opponent">
              <span class="opponent-name">vs {{ match.opponentName }}</span>
              <span class="opponent-elo">({{ match.opponentElo }})</span>
            </div>
            <div class="match-elo-change" :class="{ positive: match.eloChange > 0, negative: match.eloChange < 0 }">
              {{ match.eloChange > 0 ? '+' : '' }}{{ match.eloChange }}
            </div>
            <div class="match-date">
              {{ formatDate(match.createdAt) }}
            </div>
          </div>
        </div>
      </div>

      <!-- Profile Info -->
      <div v-if="authStore.user" class="profile-section">
        <h2 class="section-title">{{ $t('profile.accountInfo') }}</h2>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">{{ $t('auth.email') }}</span>
            <span class="value">{{ authStore.user.email }}</span>
          </div>

          <div class="info-item">
            <span class="label">{{ $t('auth.username') }}</span>
            <span class="value">{{ authStore.user.username }}</span>
          </div>

          <div class="info-item">
            <span class="label">{{ $t('profile.soulStones') }}</span>
            <span class="value">{{ authStore.user.soulStones }}</span>
          </div>

          <div class="info-item">
            <span class="label">{{ $t('profile.currentFloor') }}</span>
            <span class="value">{{ authStore.user.currentFloor }}F</span>
          </div>

          <div class="info-item">
            <span class="label">{{ $t('profile.highestFloor') }}</span>
            <span class="value">{{ authStore.user.highestFloorCleared }}F</span>
          </div>
        </div>
      </div>

      <!-- Profile Edit Form -->
      <div class="edit-section">
        <h2 class="section-title">{{ $t('profile.editProfile') }}</h2>

        <form @submit.prevent="handleUpdateProfile" class="edit-form">
          <div class="form-group">
            <label for="username">{{ $t('auth.username') }}</label>
            <input
              id="username"
              v-model="newUsername"
              type="text"
              :placeholder="authStore.user?.username"
              minlength="3"
              maxlength="20"
            />
          </div>

          <div class="form-group">
            <label for="language">{{ $t('auth.preferredLanguage') }}</label>
            <select id="language" v-model="newLanguage">
              <option value="ko">{{ $t('languages.ko') }}</option>
              <option value="en">{{ $t('languages.en') }}</option>
              <option value="ja">{{ $t('languages.ja') }}</option>
              <option value="zh">{{ $t('languages.zh') }}</option>
            </select>
          </div>

          <button type="submit" class="btn-primary" :disabled="isLoading">
            {{ isLoading ? $t('common.loading') : $t('profile.saveChanges') }}
          </button>
        </form>
      </div>

      <!-- Password Change -->
      <div class="password-section">
        <h2 class="section-title">{{ $t('profile.changePassword') }}</h2>

        <form @submit.prevent="handleChangePassword" class="password-form">
          <div class="form-group">
            <label for="currentPassword">{{ $t('profile.currentPassword') }}</label>
            <input
              id="currentPassword"
              v-model="currentPassword"
              type="password"
              required
              autocomplete="current-password"
            />
          </div>

          <div class="form-group">
            <label for="newPassword">{{ $t('profile.newPassword') }}</label>
            <input
              id="newPassword"
              v-model="newPassword"
              type="password"
              required
              minlength="8"
              autocomplete="new-password"
            />
          </div>

          <button type="submit" class="btn-secondary" :disabled="isLoading">
            {{ isLoading ? $t('common.loading') : $t('profile.changePassword') }}
          </button>
        </form>
      </div>

      <!-- Logout Button -->
      <div class="logout-section">
        <button @click="handleLogout" class="btn-danger">
          {{ $t('auth.logout') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from 'vue-i18n'
import { useNotification } from '@/composables/useNotification'
import { useConfirmModal } from '@/composables/useConfirmModal'
import AppNavigation from '@/components/AppNavigation.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import type { PlayerStats, MatchHistoryEntry } from '@/types/game'
import {
  calculateTier,
  getTierColor,
  getMyStats,
  getMatchHistory,
  getTierThresholds,
  calculateTierProgress
} from '@/api/pvp'

const router = useRouter()
const authStore = useAuthStore()
const { t } = useI18n()
const { info, success, error } = useNotification()
const { confirm } = useConfirmModal()

// Form state
const newUsername = ref('')
const newLanguage = ref(authStore.user?.preferredLanguage || 'en')
const currentPassword = ref('')
const newPassword = ref('')
const isLoading = ref(false)

// Stats state
const statsLoading = ref(true)
const stats = ref<PlayerStats | null>(null)
const historyLoading = ref(true)
const matchHistory = ref<MatchHistoryEntry[]>([])

// Computed tier properties
const currentTier = computed(() => {
  if (!authStore.user) return 'BRONZE'
  return calculateTier(authStore.user.elo)
})

const tierColor = computed(() => getTierColor(currentTier.value))

const tierGlow = computed(() => `0 0 20px ${tierColor.value}60`)

const tierEmoji = computed(() => {
  const emojis: Record<string, string> = {
    BRONZE: '\uD83E\uDD49',
    SILVER: '\uD83E\uDD48',
    GOLD: '\uD83E\uDD47',
    PLATINUM: '\uD83D\uDC8E',
    DIAMOND: '\uD83D\uDCA0',
    MASTER: '\uD83D\uDC51'
  }
  return emojis[currentTier.value] || '\uD83C\uDF96\uFE0F'
})

const tierProgress = computed(() => {
  if (!authStore.user) return 0
  return Math.round(calculateTierProgress(authStore.user.elo))
})

const tierThresholds = computed(() => {
  const thresholds = getTierThresholds()
  return thresholds[currentTier.value] || { min: 0, max: 999 }
})

const tierMin = computed(() => tierThresholds.value.min)
const tierMax = computed(() => tierThresholds.value.max)

const winRatePercentage = computed(() => {
  if (!stats.value) return 0
  return Math.round(stats.value.winRate * 100)
})

// Date formatter
function formatDate(dateString: string): string {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString()
}

onMounted(async () => {
  if (authStore.user) {
    newLanguage.value = authStore.user.preferredLanguage
  }

  // Load stats and history
  try {
    stats.value = await getMyStats()
  } catch {
    // Stats load failed silently
  } finally {
    statsLoading.value = false
  }

  try {
    matchHistory.value = await getMatchHistory(10)
  } catch {
    // History load failed silently
  } finally {
    historyLoading.value = false
  }
})

async function handleUpdateProfile() {
  const updates: any = {}

  if (newUsername.value && newUsername.value !== authStore.user?.username) {
    updates.username = newUsername.value
  }

  if (newLanguage.value !== authStore.user?.preferredLanguage) {
    updates.preferredLanguage = newLanguage.value
  }

  if (Object.keys(updates).length === 0) {
    info(t('profile.noChanges'))
    return
  }

  isLoading.value = true

  try {
    await authStore.updateProfile(updates)
    success(t('profile.profileUpdated'))
    newUsername.value = ''
  } catch {
    error(t('profile.updateFailed'))
  } finally {
    isLoading.value = false
  }
}

async function handleChangePassword() {
  if (!currentPassword.value || !newPassword.value) {
    error(t('auth.errors.fillAllFields'))
    return
  }

  if (newPassword.value.length < 8) {
    error(t('auth.errors.weakPassword'))
    return
  }

  isLoading.value = true

  try {
    await authStore.changePassword({
      currentPassword: currentPassword.value,
      newPassword: newPassword.value
    })

    success(t('profile.passwordChanged'))
    currentPassword.value = ''
    newPassword.value = ''
  } catch {
    error(t('profile.passwordChangeFailed'))
  } finally {
    isLoading.value = false
  }
}

async function handleLogout() {
  const confirmed = await confirm({
    title: t('auth.logout'),
    message: t('auth.confirmLogout'),
    confirmText: t('auth.logout'),
    cancelText: t('common.cancel')
  })

  if (confirmed) {
    authStore.logout()
    success(t('auth.logoutSuccess'))
    router.push('/login')
  }
}
</script>

<style scoped>
.profile-view {
  min-height: 100vh;
  padding: 80px 20px 20px;
  background: linear-gradient(135deg, var(--color-dark-navy) 0%, #2d2d3f 100%);
}

.profile-container {
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-family: 'Cinzel Decorative', serif;
  font-size: 36px;
  color: var(--color-gold);
  text-align: center;
  margin-bottom: 40px;
  text-shadow: 0 0 20px rgba(var(--color-gold-rgb), 0.5);
}

/* Tier & Stats Section */
.tier-stats-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.tier-badge-container {
  background: rgba(var(--color-dark-navy-rgb), 0.95);
  border: 2px solid rgba(var(--color-gold-rgb), 0.3);
  border-radius: 8px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.tier-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 30px;
  border: 3px solid;
  border-radius: 16px;
  margin-bottom: 20px;
}

.tier-emoji {
  font-size: 48px;
}

.tier-name {
  font-size: 18px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.elo-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--color-gold);
}

.tier-progress {
  width: 100%;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--color-silver);
  margin-bottom: 6px;
}

.progress-bar {
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-range {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: rgba(var(--color-silver-rgb), 0.6);
  margin-top: 4px;
}

.master-badge {
  font-size: 14px;
  color: var(--color-gold);
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 2px;
}

/* PvP Stats */
.pvp-stats {
  background: rgba(var(--color-dark-navy-rgb), 0.95);
  border: 2px solid rgba(var(--color-gold-rgb), 0.3);
  border-radius: 8px;
  padding: 24px;
}

.stats-title {
  font-size: 16px;
  color: var(--color-gold);
  margin: 0 0 16px 0;
  text-align: center;
}

.stats-loading {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
}

.stat-value.wins { color: var(--color-emerald); }
.stat-value.losses { color: var(--color-error); }
.stat-value.draws { color: var(--color-gold-light); }
.stat-value.winrate { color: var(--color-gold); }

.stat-label {
  font-size: 11px;
  color: var(--color-silver);
  text-transform: uppercase;
}

/* Match History */
.match-history-section {
  background: rgba(var(--color-dark-navy-rgb), 0.95);
  border: 2px solid rgba(var(--color-gold-rgb), 0.3);
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
}

.history-loading {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.empty-history {
  text-align: center;
  color: var(--color-silver);
  padding: 30px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item {
  display: grid;
  grid-template-columns: 80px 1fr 60px 80px;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 8px;
  border-left: 3px solid transparent;
}

.history-item.victory { border-left-color: var(--color-emerald); }
.history-item.defeat { border-left-color: var(--color-error); }
.history-item.draw { border-left-color: var(--color-gold-light); }

.result-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: bold;
  text-transform: uppercase;
}

.result-badge.victory { background: rgba(var(--color-emerald-rgb), 0.2); color: var(--color-emerald); }
.result-badge.defeat { background: rgba(var(--color-velvet-red-rgb), 0.2); color: var(--color-error); }
.result-badge.draw { background: rgba(var(--color-gold-rgb), 0.2); color: var(--color-gold-light); }

.match-opponent {
  display: flex;
  align-items: center;
  gap: 8px;
}

.opponent-name {
  color: var(--color-cream);
  font-size: 14px;
}

.opponent-elo {
  color: var(--color-silver);
  font-size: 12px;
}

.match-elo-change {
  font-size: 14px;
  font-weight: bold;
  text-align: center;
}

.match-elo-change.positive { color: var(--color-emerald); }
.match-elo-change.negative { color: var(--color-error); }

.match-date {
  font-size: 11px;
  color: var(--color-silver);
  text-align: right;
}

/* Profile Section */
.profile-section,
.edit-section,
.password-section {
  background: rgba(var(--color-dark-navy-rgb), 0.95);
  border: 2px solid rgba(var(--color-gold-rgb), 0.3);
  border-radius: 8px;
  padding: 32px;
  margin-bottom: 24px;
}

.section-title {
  font-size: 20px;
  color: var(--color-gold);
  margin: 0 0 24px 0;
  border-bottom: 1px solid rgba(var(--color-gold-rgb), 0.2);
  padding-bottom: 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item .label {
  font-size: 14px;
  color: rgba(var(--color-gold-rgb), 0.7);
  font-weight: 600;
}

.info-item .value {
  font-size: 18px;
  color: var(--color-cream);
  font-weight: 600;
}

.edit-form,
.password-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  color: var(--color-gold);
  font-weight: 600;
}

.form-group input,
.form-group select {
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(var(--color-gold-rgb), 0.3);
  border-radius: 4px;
  color: var(--color-cream);
  font-size: 16px;
  transition: all 0.3s ease;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--color-gold);
  box-shadow: 0 0 12px rgba(var(--color-gold-rgb), 0.3);
}

.form-group select option {
  background: var(--color-dark-navy);
  color: var(--color-cream);
}

.btn-primary,
.btn-secondary,
.btn-danger {
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.btn-primary {
  background: linear-gradient(135deg, var(--color-gold) 0%, #f4d03f 100%);
  color: var(--color-dark-navy);
}

.btn-secondary {
  background: linear-gradient(135deg, var(--color-purple) 0%, #9b30ff 100%);
  color: var(--color-cream);
}

.btn-danger {
  background: linear-gradient(135deg, #c41e3a 0%, #ff1744 100%);
  color: var(--color-cream);
  width: 100%;
}

.btn-primary:hover:not(:disabled),
.btn-secondary:hover:not(:disabled),
.btn-danger:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(var(--color-gold-rgb), 0.4);
}

.btn-primary:disabled,
.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.logout-section {
  margin-top: 32px;
}

/* Mobile optimization */
@media (max-width: 768px) {
  .tier-stats-section {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .profile-section,
  .edit-section,
  .password-section,
  .match-history-section {
    padding: 24px 20px;
  }

  .page-title {
    font-size: 28px;
  }

  .history-item {
    grid-template-columns: 70px 1fr 50px;
    gap: 8px;
  }

  .match-date {
    display: none;
  }
}
</style>
