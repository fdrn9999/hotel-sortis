<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="visible" class="profile-overlay" @click.self="close">
        <div class="profile-modal">
          <!-- Loading state -->
          <div v-if="loading" class="loading-state">
            <LoadingSpinner />
          </div>

          <!-- Error state -->
          <div v-else-if="error" class="error-state">
            <p>{{ error }}</p>
            <button class="btn-secondary" @click="close">{{ t('common.close') }}</button>
          </div>

          <!-- Profile content -->
          <template v-else-if="profile">
            <!-- Header -->
            <div class="modal-header">
              <div class="avatar-section">
                <div class="avatar">
                  {{ profile.username.charAt(0).toUpperCase() }}
                </div>
              </div>
              <h2 class="player-name">{{ profile.username }}</h2>
              <button class="btn-close-x" @click="close">&times;</button>
            </div>

            <!-- Tier & ELO -->
            <div class="tier-section">
              <div class="tier-badge" :style="{ borderColor: tierColor }">
                <span class="tier-emoji">{{ tierEmoji }}</span>
                <span class="tier-name" :style="{ color: tierColor }">
                  {{ t(`pvp.tiers.${profile.tier.toLowerCase()}`) }}
                </span>
              </div>
              <div class="elo-display">
                <span class="elo-label">{{ t('pvp.elo') }}</span>
                <span class="elo-value">{{ profile.elo }}</span>
              </div>
            </div>

            <!-- Stats -->
            <div class="stats-section">
              <div class="stat-item">
                <span class="stat-value wins">{{ profile.wins }}</span>
                <span class="stat-label">{{ t('pvp.wins') }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-value losses">{{ profile.losses }}</span>
                <span class="stat-label">{{ t('pvp.losses') }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-value draws">{{ profile.draws }}</span>
                <span class="stat-label">{{ t('pvp.draws') }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-value winrate">{{ winRatePercentage }}%</span>
                <span class="stat-label">{{ t('pvp.winRate') }}</span>
              </div>
            </div>

            <!-- Floor info -->
            <div class="floor-section">
              <div class="floor-item">
                <span class="floor-label">{{ t('profile.currentFloor') }}</span>
                <span class="floor-value">{{ profile.currentFloor }}F</span>
              </div>
              <div class="floor-item">
                <span class="floor-label">{{ t('profile.highestFloor') }}</span>
                <span class="floor-value">{{ profile.highestFloorCleared }}F</span>
              </div>
            </div>

            <!-- Action buttons -->
            <div class="action-section" v-if="showActions && !isOwnProfile">
              <button class="btn-action add-friend" @click="handleAddFriend">
                <span class="btn-icon">&#x2795;</span>
                {{ t('social.friends.addFriend') }}
              </button>
              <button class="btn-action whisper" @click="handleWhisper">
                <span class="btn-icon">&#x1F4AC;</span>
                {{ t('social.chat.whisper') }}
              </button>
              <button class="btn-action block" @click="handleBlock">
                <span class="btn-icon">&#x1F6AB;</span>
                {{ t('social.blocks.block') }}
              </button>
            </div>

            <!-- Close button -->
            <button class="btn-close" @click="close">{{ t('common.close') }}</button>
          </template>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import type { PlayerPublicProfile, RankTier } from '@/types/game'
import { getPlayerProfile, getTierColor } from '@/api/pvp'
import { useAuthStore } from '@/stores/auth'
import { useSocialStore } from '@/stores/social'
import { useNotification } from '@/composables/useNotification'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const { t } = useI18n()
const authStore = useAuthStore()
const socialStore = useSocialStore()
const { success, error: showError } = useNotification()

// State
const visible = ref(false)
const loading = ref(false)
const error = ref('')
const profile = ref<PlayerPublicProfile | null>(null)
const showActions = ref(true)
const resolver = ref<((value: boolean) => void) | null>(null)

// Computed
const tierColor = computed(() => profile.value ? getTierColor(profile.value.tier) : '#808080')

const tierEmoji = computed(() => {
  if (!profile.value) return '\uD83C\uDF96\uFE0F'
  const emojis: Record<RankTier, string> = {
    BRONZE: '\uD83E\uDD49',
    SILVER: '\uD83E\uDD48',
    GOLD: '\uD83E\uDD47',
    PLATINUM: '\uD83D\uDC8E',
    DIAMOND: '\uD83D\uDCA0',
    MASTER: '\uD83D\uDC51'
  }
  return emojis[profile.value.tier] || '\uD83C\uDF96\uFE0F'
})

const winRatePercentage = computed(() => {
  if (!profile.value) return 0
  return Math.round(profile.value.winRate * 100)
})

const isOwnProfile = computed(() => {
  return profile.value?.playerId === authStore.playerId
})

// Event handler
async function handleShowProfile(event: CustomEvent) {
  const { playerId, showActions: actions, resolver: res } = event.detail
  showActions.value = actions !== false
  resolver.value = res || null
  visible.value = true
  loading.value = true
  error.value = ''
  profile.value = null

  try {
    profile.value = await getPlayerProfile(playerId)
  } catch (err: any) {
    error.value = err.response?.data?.message || t('profile.loadFailed')
  } finally {
    loading.value = false
  }
}

function close() {
  visible.value = false
  if (resolver.value) {
    resolver.value(false)
    resolver.value = null
  }
}

async function handleAddFriend() {
  if (!profile.value) return
  const myPlayerId = authStore.playerId ?? 1 // TODO: get from auth store
  try {
    await socialStore.sendFriendRequest(myPlayerId, profile.value.playerId)
    success(t('social.friends.requestSent'))
  } catch (err: any) {
    showError(err.response?.data?.message || t('common.error'))
  }
}

function handleWhisper() {
  if (!profile.value) return
  // Dispatch event to open DM with this player
  window.dispatchEvent(
    new CustomEvent('open-dm', {
      detail: {
        playerId: profile.value.playerId,
        username: profile.value.username
      }
    })
  )
  close()
}

async function handleBlock() {
  if (!profile.value) return
  const myPlayerId = authStore.playerId ?? 1 // TODO: get from auth store
  try {
    await socialStore.blockPlayer(myPlayerId, profile.value.playerId)
    success(t('social.blocks.blocked'))
    close()
  } catch (err: any) {
    showError(err.response?.data?.message || t('common.error'))
  }
}

// Wrapper to handle CustomEvent typing
const eventHandler = (event: Event) => handleShowProfile(event as CustomEvent)

onMounted(() => {
  window.addEventListener('show-player-profile', eventHandler)
})

onUnmounted(() => {
  window.removeEventListener('show-player-profile', eventHandler)
})
</script>

<style scoped>
.profile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10000;
}

.profile-modal {
  position: relative;
  background: linear-gradient(135deg, var(--color-dark-navy) 0%, #1a1a2e 100%);
  border: 2px solid var(--color-gold);
  border-radius: 16px;
  padding: 30px;
  min-width: 380px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 0 40px rgba(var(--color-gold-rgb), 0.2);
}

.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  gap: 20px;
  color: var(--color-cream);
}

.modal-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
  position: relative;
}

.btn-close-x {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: var(--color-cream);
  font-size: 24px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-close-x:hover {
  background: rgba(255, 255, 255, 0.2);
}

.avatar-section {
  margin-bottom: 12px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-gold) 0%, var(--color-gold-light) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: bold;
  color: var(--color-dark-navy);
  box-shadow: 0 0 20px rgba(var(--color-gold-rgb), 0.3);
}

.player-name {
  font-family: 'Cinzel Decorative', serif;
  font-size: 24px;
  color: var(--color-gold);
  margin: 0;
  text-align: center;
}

.tier-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(var(--color-gold-rgb), 0.2);
}

.tier-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 20px;
  border: 2px solid;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.3);
}

.tier-emoji {
  font-size: 32px;
}

.tier-name {
  font-size: 14px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.elo-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.elo-label {
  font-size: 12px;
  color: var(--color-silver);
}

.elo-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--color-gold);
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
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
  font-size: 20px;
  font-weight: bold;
}

.stat-value.wins {
  color: var(--color-emerald);
}

.stat-value.losses {
  color: var(--color-error);
}

.stat-value.draws {
  color: var(--color-gold-light);
}

.stat-value.winrate {
  color: var(--color-gold);
}

.stat-label {
  font-size: 11px;
  color: var(--color-silver);
  text-transform: uppercase;
}

.floor-section {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(var(--color-gold-rgb), 0.2);
}

.floor-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.floor-label {
  font-size: 12px;
  color: var(--color-silver);
}

.floor-value {
  font-size: 18px;
  font-weight: bold;
  color: var(--color-cream);
}

.action-section {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  margin-bottom: 20px;
}

.btn-action {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: 1px solid rgba(var(--color-gold-rgb), 0.3);
  background: rgba(0, 0, 0, 0.3);
  color: var(--color-cream);
  font-size: 13px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-action:hover {
  background: rgba(var(--color-gold-rgb), 0.2);
  border-color: var(--color-gold);
}

.btn-action.add-friend:hover {
  border-color: var(--color-emerald);
}

.btn-action.block:hover {
  border-color: var(--color-velvet-red);
}

.btn-icon {
  font-size: 14px;
}

.btn-close {
  width: 100%;
  padding: 12px;
  background: var(--color-gold);
  border: none;
  border-radius: 8px;
  color: var(--color-dark-navy);
  font-size: 14px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-close:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.btn-secondary {
  padding: 10px 24px;
  background: rgba(var(--color-gold-rgb), 0.2);
  border: 1px solid var(--color-gold);
  color: var(--color-gold);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background: rgba(var(--color-gold-rgb), 0.3);
}

/* Transition */
.modal-fade-enter-active {
  animation: modal-in 0.3s ease-out;
}

.modal-fade-leave-active {
  animation: modal-out 0.2s ease-in;
}

@keyframes modal-in {
  0% {
    opacity: 0;
    transform: scale(0.9);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes modal-out {
  0% {
    opacity: 1;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(0.9);
  }
}

/* Responsive */
@media (max-width: 480px) {
  .profile-modal {
    min-width: unset;
    width: 95%;
    padding: 20px;
  }

  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }

  .tier-section {
    flex-direction: column;
  }
}
</style>
