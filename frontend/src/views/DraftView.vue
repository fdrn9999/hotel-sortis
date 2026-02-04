<!--
  PvP Skill Draft View
  Snake draft order: A-B-B-A-A-B-B-A (8 picks, 4 skills each)
  30 seconds per pick
-->
<template>
  <div class="draft-view">
    <!-- Navigation -->
    <AppNavigation
      :title="t('draft.title')"
      :show-home="false"
      :show-back="false"
      :show-settings="false"
    />

    <!-- Loading State -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
      <p>{{ t('common.loading') }}</p>
    </div>

    <!-- Draft Content -->
    <div v-else-if="draftState" class="draft-content">
      <!-- Header: Timer + Turn Indicator -->
      <header class="draft-header">
        <div class="turn-indicator" :class="{ 'my-turn': isMyTurn }">
          {{ isMyTurn ? t('draft.yourTurn') : t('draft.opponentTurn') }}
        </div>
        <div class="pick-progress">
          {{ t('draft.pickNumber', { current: draftState.pickNumber, total: 8 }) }}
        </div>
        <div class="timer-container">
          <div class="timer-bar" :class="{ warning: timeRemaining <= 10 }" :style="{ width: timerPercentage + '%' }"></div>
          <span class="timer-text">{{ timeRemaining }}s</span>
        </div>
      </header>

      <!-- Main Layout: 3-column grid -->
      <div class="draft-layout">
        <!-- Left Panel: My Picks -->
        <aside class="picks-panel left">
          <h3>{{ isPlayer1 ? t('draft.myPicks') : t('draft.opponentPicks') }}</h3>
          <div class="pick-slots">
            <div
              v-for="i in 4"
              :key="'p1-' + i"
              class="pick-slot"
              :class="{
                filled: draftState.player1Picks[i - 1],
                [draftState.player1Picks[i - 1]?.rarity?.toLowerCase() || '']: draftState.player1Picks[i - 1]
              }"
            >
              <template v-if="draftState.player1Picks[i - 1]">
                <div class="pick-skill">
                  <div class="skill-icon-small">
                    {{ draftState.player1Picks[i - 1].name.charAt(0) }}
                  </div>
                  <div class="skill-info-small">
                    <span class="skill-name-small">{{ draftState.player1Picks[i - 1].name }}</span>
                    <span class="skill-rarity-small" :class="draftState.player1Picks[i - 1].rarity.toLowerCase()">
                      {{ t(`rarity.${draftState.player1Picks[i - 1].rarity.toLowerCase()}`) }}
                    </span>
                  </div>
                </div>
              </template>
              <template v-else>
                <span class="empty-text">{{ t('draft.emptySlot') }}</span>
              </template>
            </div>
          </div>
        </aside>

        <!-- Center: Skill Pool -->
        <main class="skill-pool">
          <div class="skill-grid">
            <div
              v-for="skill in draftState.pool"
              :key="skill.skillId"
              class="skill-card"
              :class="{
                disabled: !isMyTurn || draftState.status !== 'IN_PROGRESS',
                [skill.rarity.toLowerCase()]: true
              }"
              @click="pickSkill(skill.skillId)"
            >
              <div class="skill-icon">
                {{ skill.name.charAt(0) }}
              </div>
              <div class="skill-details">
                <h4 class="skill-name">{{ skill.name }}</h4>
                <span class="skill-rarity-badge" :class="skill.rarity.toLowerCase()">
                  {{ t(`rarity.${skill.rarity.toLowerCase()}`) }}
                </span>
                <p class="skill-description">{{ skill.description }}</p>
                <span class="skill-trigger">{{ skill.triggerType }}</span>
              </div>
            </div>
          </div>
        </main>

        <!-- Right Panel: Opponent Picks -->
        <aside class="picks-panel right">
          <h3>{{ isPlayer1 ? t('draft.opponentPicks') : t('draft.myPicks') }}</h3>
          <div class="pick-slots">
            <div
              v-for="i in 4"
              :key="'p2-' + i"
              class="pick-slot"
              :class="{
                filled: draftState.player2Picks[i - 1],
                [draftState.player2Picks[i - 1]?.rarity?.toLowerCase() || '']: draftState.player2Picks[i - 1]
              }"
            >
              <template v-if="draftState.player2Picks[i - 1]">
                <div class="pick-skill">
                  <div class="skill-icon-small">
                    {{ draftState.player2Picks[i - 1].name.charAt(0) }}
                  </div>
                  <div class="skill-info-small">
                    <span class="skill-name-small">{{ draftState.player2Picks[i - 1].name }}</span>
                    <span class="skill-rarity-small" :class="draftState.player2Picks[i - 1].rarity.toLowerCase()">
                      {{ t(`rarity.${draftState.player2Picks[i - 1].rarity.toLowerCase()}`) }}
                    </span>
                  </div>
                </div>
              </template>
              <template v-else>
                <span class="empty-text">{{ t('draft.emptySlot') }}</span>
              </template>
            </div>
          </div>
        </aside>
      </div>

      <!-- Footer: Ready Button (shown after 8 picks) -->
      <footer v-if="draftState.status === 'PICKS_COMPLETE'" class="draft-footer">
        <div class="ready-status">
          <p v-if="myReadyState">{{ t('draft.waitingForOpponent') }}</p>
          <p v-else>{{ t('draft.pickComplete') }}</p>
        </div>
        <button
          class="ready-btn"
          @click="setReady"
          :disabled="myReadyState"
        >
          {{ myReadyState ? t('draft.waitingForOpponent') : t('draft.ready') }}
        </button>
      </footer>

      <!-- Draft Complete Overlay -->
      <div v-if="draftState.status === 'COMPLETED'" class="complete-overlay">
        <div class="complete-content">
          <h2>{{ t('draft.bothReady') }}</h2>
          <p>{{ t('draft.startingBattle') }}</p>
          <div class="spinner"></div>
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else class="error-state">
      <p>{{ t('draft.loadError') }}</p>
      <button @click="loadDraftState" class="retry-btn">{{ t('common.retry') }}</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useDraftWebSocket } from '@/composables/useDraftWebSocket'
import { useNotification } from '@/composables/useNotification'
import { getDraftState as fetchDraftState } from '@/api/pvp'
import type { DraftState, DraftCompleteMessage } from '@/types/game'
import AppNavigation from '@/components/AppNavigation.vue'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const { error: showError } = useNotification()

// Get battleId from route query
const battleId = computed(() => Number(route.query.battleId) || 0)
// TODO: Get from auth store in production
const playerId = ref(1)

// State
const loading = ref(true)
const draftState = ref<DraftState | null>(null)
const timeRemaining = ref(30)
const timerInterval = ref<ReturnType<typeof setInterval> | null>(null)

// WebSocket
const {
  subscribeDraft,
  unsubscribeAll,
  sendPick,
  sendReady,
  onStateUpdate,
  onPicksComplete,
  onDraftComplete,
  onError
} = useDraftWebSocket(battleId.value, playerId.value)

// Computed
const isPlayer1 = computed(() => draftState.value?.player1Id === playerId.value)

const isMyTurn = computed(() => {
  if (!draftState.value || draftState.value.status !== 'IN_PROGRESS') return false
  const turn = draftState.value.currentTurn
  return (turn === 'player1' && isPlayer1.value) || (turn === 'player2' && !isPlayer1.value)
})

const myReadyState = computed(() => {
  if (!draftState.value) return false
  return isPlayer1.value ? draftState.value.player1Ready : draftState.value.player2Ready
})

const timerPercentage = computed(() => (timeRemaining.value / 30) * 100)

// Methods
const loadDraftState = async () => {
  loading.value = true
  try {
    const state = await fetchDraftState(battleId.value)
    if (state) {
      draftState.value = state
      timeRemaining.value = Math.ceil(state.timeRemaining / 1000)
    }
  } catch (err) {
    console.error('Failed to load draft state:', err)
  } finally {
    loading.value = false
  }
}

const pickSkill = (skillId: number) => {
  if (!isMyTurn.value || draftState.value?.status !== 'IN_PROGRESS') return
  sendPick(skillId)
}

const setReady = () => {
  if (draftState.value?.status !== 'PICKS_COMPLETE') return
  sendReady()
}

const startTimer = () => {
  stopTimer()
  timerInterval.value = setInterval(() => {
    if (timeRemaining.value > 0) {
      timeRemaining.value--
    }
  }, 1000)
}

const stopTimer = () => {
  if (timerInterval.value) {
    clearInterval(timerInterval.value)
    timerInterval.value = null
  }
}

const navigateToBattle = (battleId: number) => {
  router.push({
    name: 'battle',
    query: {
      battleId: battleId.toString(),
      pvpMode: 'true'
    }
  })
}

// Lifecycle
onMounted(async () => {
  // Load initial state via REST
  await loadDraftState()

  // Subscribe to WebSocket updates
  subscribeDraft()

  // Set up event handlers
  onStateUpdate.value = (state: DraftState) => {
    draftState.value = state
    timeRemaining.value = Math.ceil(state.timeRemaining / 1000)
    if (state.status === 'IN_PROGRESS') {
      startTimer()
    } else {
      stopTimer()
    }
  }

  onPicksComplete.value = (state: DraftState) => {
    draftState.value = state
    stopTimer()
  }

  onDraftComplete.value = (msg: DraftCompleteMessage) => {
    // Navigate to battle after short delay
    setTimeout(() => {
      navigateToBattle(msg.battleId)
    }, 1500)
  }

  onError.value = (err) => {
    showError(err.error || t('draft.pickError'))
  }

  // Start timer if draft is in progress
  if (draftState.value?.status === 'IN_PROGRESS') {
    startTimer()
  }
})

onUnmounted(() => {
  stopTimer()
  unsubscribeAll()
})
</script>

<style scoped>
.draft-view {
  min-height: 100vh;
  background: linear-gradient(180deg, #1b1b27 0%, #0D0D1A 100%);
  color: #fffdd0;
}

.loading-overlay {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: calc(100vh - 60px);
  gap: 1rem;
}

.spinner {
  width: 60px;
  height: 60px;
  border: 4px solid rgba(212, 175, 55, 0.2);
  border-top-color: #d4af37;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.draft-content {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px);
}

/* Header */
.draft-header {
  padding: 1rem;
  background: rgba(0, 0, 0, 0.4);
  border-bottom: 2px solid #d4af37;
  text-align: center;
}

.turn-indicator {
  font-size: 1.5rem;
  font-weight: bold;
  color: #808080;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.turn-indicator.my-turn {
  color: #d4af37;
  animation: pulse 1s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.pick-progress {
  font-size: 1rem;
  color: #c0c0c0;
  margin-bottom: 0.5rem;
}

.timer-container {
  position: relative;
  height: 24px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 12px;
  overflow: hidden;
  max-width: 400px;
  margin: 0 auto;
}

.timer-bar {
  height: 100%;
  background: linear-gradient(90deg, #d4af37 0%, #4CAF50 100%);
  transition: width 1s linear;
}

.timer-bar.warning {
  background: linear-gradient(90deg, #C62828 0%, #FF6B6B 100%);
}

.timer-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 0.875rem;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

/* Main Layout */
.draft-layout {
  display: grid;
  grid-template-columns: 280px 1fr 280px;
  gap: 1rem;
  padding: 1rem;
  flex: 1;
  overflow: hidden;
}

/* Pick Panels */
.picks-panel {
  background: rgba(42, 42, 62, 0.6);
  border: 2px solid #d4af37;
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
}

.picks-panel h3 {
  text-align: center;
  color: #d4af37;
  font-size: 1.1rem;
  margin-bottom: 1rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.pick-slots {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  flex: 1;
}

.pick-slot {
  background: rgba(0, 0, 0, 0.3);
  border: 2px dashed #555;
  border-radius: 6px;
  padding: 0.75rem;
  min-height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.pick-slot.filled {
  background: rgba(212, 175, 55, 0.1);
  border: 2px solid #d4af37;
}

.pick-slot.common { border-left: 4px solid #808080; }
.pick-slot.rare { border-left: 4px solid #4169e1; }
.pick-slot.epic { border-left: 4px solid #9370db; }
.pick-slot.legendary { border-left: 4px solid #ffa500; }

.empty-text {
  color: #666;
  font-style: italic;
  font-size: 0.875rem;
}

.pick-skill {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
}

.skill-icon-small {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #3a3a5a, #2a2a4a);
  border: 2px solid #d4af37;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  font-weight: bold;
  color: #d4af37;
  flex-shrink: 0;
}

.skill-info-small {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  overflow: hidden;
}

.skill-name-small {
  font-size: 0.875rem;
  font-weight: bold;
  color: #fffdd0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.skill-rarity-small {
  font-size: 0.75rem;
  text-transform: uppercase;
}

.skill-rarity-small.common { color: #808080; }
.skill-rarity-small.rare { color: #4169e1; }
.skill-rarity-small.epic { color: #9370db; }
.skill-rarity-small.legendary { color: #ffa500; }

/* Skill Pool */
.skill-pool {
  overflow-y: auto;
  padding: 0.5rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.skill-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.skill-card {
  background: rgba(42, 42, 62, 0.8);
  border: 2px solid #555;
  border-radius: 6px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.3s;
}

.skill-card:hover:not(.disabled) {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(212, 175, 55, 0.3);
  border-color: #d4af37;
}

.skill-card.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.skill-card.common { border-left: 4px solid #808080; }
.skill-card.rare { border-left: 4px solid #4169e1; }
.skill-card.epic { border-left: 4px solid #9370db; }
.skill-card.legendary { border-left: 4px solid #ffa500; }

.skill-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #3a3a5a, #2a2a4a);
  border: 2px solid #d4af37;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: bold;
  color: #d4af37;
  margin: 0 auto 0.75rem;
}

.skill-details {
  text-align: center;
}

.skill-name {
  font-size: 1rem;
  color: #fffdd0;
  margin-bottom: 0.25rem;
}

.skill-rarity-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.skill-rarity-badge.common { background: #808080; color: white; }
.skill-rarity-badge.rare { background: #4169e1; color: white; }
.skill-rarity-badge.epic { background: #9370db; color: white; }
.skill-rarity-badge.legendary { background: #ffa500; color: white; }

.skill-description {
  font-size: 0.8rem;
  color: #c0c0c0;
  line-height: 1.4;
  margin-bottom: 0.5rem;
  min-height: 40px;
}

.skill-trigger {
  font-size: 0.7rem;
  color: #888;
  text-transform: uppercase;
}

/* Footer */
.draft-footer {
  padding: 1rem;
  background: rgba(0, 0, 0, 0.4);
  border-top: 2px solid #d4af37;
  text-align: center;
}

.ready-status p {
  color: #c0c0c0;
  margin-bottom: 0.75rem;
}

.ready-btn {
  padding: 1rem 3rem;
  background: linear-gradient(135deg, #d4af37, #f4d03f);
  color: #1b1b27;
  border: none;
  border-radius: 6px;
  font-size: 1.25rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.ready-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 5px 20px rgba(212, 175, 55, 0.5);
}

.ready-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Complete Overlay */
.complete-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.complete-content {
  text-align: center;
}

.complete-content h2 {
  font-size: 2rem;
  color: #d4af37;
  margin-bottom: 1rem;
}

.complete-content p {
  color: #c0c0c0;
  margin-bottom: 1.5rem;
}

/* Error State */
.error-state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: calc(100vh - 60px);
  gap: 1rem;
}

.retry-btn {
  padding: 0.75rem 2rem;
  background: rgba(212, 175, 55, 0.2);
  border: 1px solid #d4af37;
  color: #d4af37;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.retry-btn:hover {
  background: rgba(212, 175, 55, 0.3);
}

/* Mobile Responsive */
@media (max-width: 1024px) {
  .draft-layout {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto 1fr;
  }

  .picks-panel {
    order: 1;
  }

  .picks-panel.left {
    order: 1;
  }

  .picks-panel.right {
    order: 2;
  }

  .skill-pool {
    order: 3;
  }

  .pick-slots {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .pick-slot {
    flex: 1;
    min-width: 100px;
    min-height: 60px;
  }

  .skill-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}

@media (max-width: 600px) {
  .turn-indicator {
    font-size: 1.2rem;
  }

  .skill-card {
    padding: 0.75rem;
  }

  .skill-icon {
    width: 40px;
    height: 40px;
    font-size: 1.25rem;
  }

  .skill-name {
    font-size: 0.9rem;
  }

  .skill-description {
    display: none;
  }
}
</style>
