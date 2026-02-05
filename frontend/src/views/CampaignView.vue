<template>
  <div class="campaign-view">
    <!-- Navigation Bar (CLAUDE.md 3.3.2) -->
    <div class="app-navigation">
      <div class="nav-left">
        <button class="nav-btn home-btn" @click="goToHome" aria-label="Home">
          <span class="icon">&#x1F3E0;</span>
        </button>
        <button v-if="canGoBack" class="nav-btn back-btn" @click="goBack" aria-label="Back">
          <span class="icon">&larr;</span>
        </button>
      </div>
      <h1 class="nav-title">{{ $t('campaign.title') }}</h1>
      <button class="nav-btn settings-btn" @click="goToSettings" aria-label="Settings">
        <span class="icon">&#x2699;</span>
      </button>
    </div>

    <!-- Main Content -->
    <div class="main-content">
      <!-- Loading -->
      <div v-if="campaignStore.loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>{{ $t('common.loading') }}</p>
      </div>

      <!-- Error -->
      <div v-else-if="campaignStore.error" class="error-state">
        <p>{{ $t('common.error') }}: {{ campaignStore.error }}</p>
        <button class="retry-btn" @click="loadProgress">{{ $t('common.retry') }}</button>
      </div>

      <!-- Campaign Map -->
      <div v-else class="campaign-layout">
        <!-- Progress Summary -->
        <div class="progress-summary">
          <div class="stat-item">
            <span class="stat-label">{{ $t('campaign.totalRuns') }}</span>
            <span class="stat-value">{{ campaignStore.totalRuns }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">{{ $t('campaign.victories') }}</span>
            <span class="stat-value victories">{{ campaignStore.totalVictories }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">{{ $t('campaign.defeats') }}</span>
            <span class="stat-value defeats">{{ campaignStore.totalDefeats }}</span>
          </div>
        </div>

        <!-- Floor Tower (scrollable, bottom to top) -->
        <div class="floor-tower" ref="floorTowerRef">
          <div
            v-for="floorData in reversedFloors"
            :key="floorData.floor"
            class="floor-row"
            :class="[
              getFloorTierClass(floorData.floor),
              {
                'floor-locked': floorData.locked,
                'floor-cleared': floorData.cleared,
                'floor-available': !floorData.locked && !floorData.cleared,
                'floor-selected': selectedFloor === floorData.floor
              }
            ]"
            @click="selectFloor(floorData)"
          >
            <!-- Floor Number -->
            <div class="floor-number">
              <span class="floor-num">{{ floorData.floor }}F</span>
            </div>

            <!-- Floor Info -->
            <div class="floor-info">
              <div class="floor-name">
                {{ getFloorTypeName(floorData.floorType) }}
                <span v-if="floorData.bossId" class="boss-label">
                  {{ floorData.bossName || floorData.bossId }}
                </span>
              </div>
              <div class="floor-details">
                <span class="floor-type-icon">{{ getFloorTypeIcon(floorData.floorType) }}</span>
                <span class="battle-count">
                  {{ $t('campaign.battles', { count: floorData.battleCount }) }}
                </span>
                <span v-if="floorData.mutatorId" class="mutator-badge" :title="floorData.mutatorDescription">
                  {{ floorData.mutatorIcon }} {{ floorData.mutatorName }}
                </span>
                <span v-if="floorData.skillRewardRarity" class="reward-badge" :class="`reward-${floorData.skillRewardRarity.toLowerCase()}`">
                  {{ floorData.skillRewardRarity }}
                </span>
              </div>
            </div>

            <!-- Floor Status -->
            <div class="floor-status">
              <span v-if="floorData.locked" class="status-icon locked-icon">&#x1F512;</span>
              <span v-else-if="floorData.cleared" class="status-icon cleared-icon">&#x2714;</span>
              <span v-else class="status-icon available-icon">&#x25B6;</span>
            </div>
          </div>
        </div>

        <!-- Selected Floor Detail Panel -->
        <div v-if="selectedFloorData" class="floor-detail-panel" :class="getFloorTierClass(selectedFloorData.floor)">
          <div class="detail-header">
            <h2>{{ selectedFloorData.floor }}F - {{ getFloorTypeName(selectedFloorData.floorType) }}</h2>
            <span v-if="selectedFloorData.bossId" class="detail-boss-name">
              {{ selectedFloorData.bossName || selectedFloorData.bossId }}
            </span>
          </div>

          <div class="detail-body">
            <p v-if="selectedFloorData.description" class="detail-description">
              {{ selectedFloorData.description }}
            </p>

            <div class="detail-stats">
              <div class="detail-stat">
                <span class="detail-stat-label">{{ $t('campaign.battleCount') }}</span>
                <span class="detail-stat-value">{{ selectedFloorData.battleCount }}</span>
              </div>
              <div class="detail-stat">
                <span class="detail-stat-label">{{ $t('campaign.difficulty') }}</span>
                <span class="detail-stat-value">{{ getDifficultyLabel(selectedFloorData.aiLevel) }}</span>
              </div>
              <div v-if="selectedFloorData.mutatorId" class="detail-stat mutator-detail">
                <span class="detail-stat-label">{{ $t('campaign.mutator') }}</span>
                <span class="detail-stat-value mutator-value">
                  {{ selectedFloorData.mutatorIcon }} {{ selectedFloorData.mutatorName }}
                </span>
                <p class="mutator-description">{{ selectedFloorData.mutatorDescription }}</p>
              </div>
              <div v-if="selectedFloorData.skillRewardRarity" class="detail-stat">
                <span class="detail-stat-label">{{ $t('campaign.reward') }}</span>
                <span class="detail-stat-value reward-text" :class="`reward-${selectedFloorData.skillRewardRarity.toLowerCase()}`">
                  {{ selectedFloorData.skillRewardRarity }} {{ $t('campaign.skillReward') }}
                </span>
              </div>
            </div>
          </div>

          <div class="detail-actions">
            <button
              v-if="selectedFloorData.locked"
              class="action-btn locked-btn"
              disabled
            >
              {{ $t('campaign.floorLocked') }}
            </button>
            <button
              v-else-if="selectedFloorData.cleared"
              class="action-btn cleared-btn"
              @click="startFloor(selectedFloorData.floor)"
            >
              {{ $t('campaign.replayFloor') }}
            </button>
            <button
              v-else
              class="action-btn start-btn"
              @click="startFloor(selectedFloorData.floor)"
            >
              {{ $t('campaign.startFloor') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useCampaignStore } from '@/stores/campaign'
import type { CampaignFloorStatus } from '@/types/game'

const router = useRouter()
const { t } = useI18n()
const campaignStore = useCampaignStore()

// State
const selectedFloor = ref<number | null>(null)
const floorTowerRef = ref<HTMLElement | null>(null)
const playerId = 1 // TODO: Get from auth store

// Computed
const canGoBack = computed(() => window.history.length > 1)

const reversedFloors = computed(() => {
  return [...campaignStore.floors].reverse()
})

const selectedFloorData = computed(() => {
  if (selectedFloor.value === null) return null
  return campaignStore.floors.find(f => f.floor === selectedFloor.value) || null
})

// Methods
function goToHome() {
  router.push('/')
}

function goBack() {
  router.back()
}

function goToSettings() {
  router.push('/settings')
}

function selectFloor(floorData: CampaignFloorStatus) {
  selectedFloor.value = floorData.floor
}

function startFloor(floor: number) {
  router.push({
    path: '/campaign/loadout',
    query: { floor: floor.toString() }
  })
}

function getFloorTierClass(floor: number): string {
  if (floor <= 5) return 'tier-gold'
  if (floor <= 10) return 'tier-purple'
  return 'tier-cosmic'
}

function getFloorTypeName(floorType: string): string {
  switch (floorType) {
    case 'NORMAL': return t('campaign.floorTypes.normal')
    case 'ELITE': return t('campaign.floorTypes.elite')
    case 'BOSS': return t('campaign.floorTypes.boss')
    default: return floorType
  }
}

function getFloorTypeIcon(floorType: string): string {
  switch (floorType) {
    case 'NORMAL': return '\u2694'  // crossed swords
    case 'ELITE': return '\u2B50'  // star
    case 'BOSS': return '\u1F480'  // skull - fallback to text
    default: return '\u2694'
  }
}

function getDifficultyLabel(aiLevel: number): string {
  switch (aiLevel) {
    case 0: return t('campaign.difficulty_basic')
    case 1: return t('campaign.difficulty_standard')
    case 2: return t('campaign.difficulty_advanced')
    case 3: return t('campaign.difficulty_master')
    default: return t('campaign.difficulty_basic')
  }
}

async function loadProgress() {
  await campaignStore.loadCampaignProgress(playerId)
  // Auto-select the highest available floor
  if (campaignStore.floors.length > 0) {
    selectedFloor.value = campaignStore.highestAvailableFloor
    await nextTick()
    scrollToSelectedFloor()
  }
}

function scrollToSelectedFloor() {
  if (!floorTowerRef.value || selectedFloor.value === null) return
  const floorEl = floorTowerRef.value.querySelector(`.floor-selected`)
  if (floorEl) {
    floorEl.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

// Lifecycle
onMounted(async () => {
  await loadProgress()
})
</script>

<style scoped>
.campaign-view {
  min-height: 100vh;
  background: linear-gradient(180deg, var(--color-dark-navy) 0%, #0d0d1a 100%);
  color: #f5f5f5;
}

/* Navigation Bar */
.app-navigation {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: rgba(var(--color-dark-navy-rgb), 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 2px solid var(--color-gold);
  z-index: 1000;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-btn {
  width: 44px;
  height: 44px;
  border: none;
  background: transparent;
  color: var(--color-gold);
  font-size: 22px;
  cursor: pointer;
  transition: transform 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-btn:hover {
  transform: scale(1.1);
}

.nav-btn:active {
  transform: scale(0.95);
}

.nav-title {
  font-size: 20px;
  font-weight: bold;
  color: var(--color-gold);
  margin: 0;
  letter-spacing: 0.05em;
}

/* Main Content */
.main-content {
  margin-top: 60px;
  padding: 16px;
  padding-bottom: 80px;
}

/* Loading / Error */
.loading-state,
.error-state {
  text-align: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  margin: 0 auto 16px;
  border: 3px solid rgba(var(--color-gold-rgb), 0.2);
  border-top-color: var(--color-gold);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.retry-btn {
  margin-top: 16px;
  padding: 12px 24px;
  background: var(--color-gold);
  color: var(--color-dark-navy);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
}

/* Campaign Layout */
.campaign-layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 720px;
  margin: 0 auto;
}

/* Progress Summary */
.progress-summary {
  display: flex;
  justify-content: center;
  gap: 24px;
  padding: 16px;
  background: rgba(45, 45, 63, 0.6);
  border-radius: 8px;
  border: 1px solid rgba(var(--color-gold-rgb), 0.2);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-gold);
}

.stat-value.victories {
  color: var(--color-emerald);
}

.stat-value.defeats {
  color: #f44336;
}

/* Floor Tower */
.floor-tower {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 480px;
  overflow-y: auto;
  padding: 4px;
  scrollbar-width: thin;
  scrollbar-color: rgba(var(--color-gold-rgb), 0.3) transparent;
}

.floor-tower::-webkit-scrollbar {
  width: 6px;
}

.floor-tower::-webkit-scrollbar-track {
  background: transparent;
}

.floor-tower::-webkit-scrollbar-thumb {
  background: rgba(var(--color-gold-rgb), 0.3);
  border-radius: 3px;
}

/* Floor Row */
.floor-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
}

.floor-row::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.05;
  pointer-events: none;
}

.floor-row:hover:not(.floor-locked) {
  transform: translateX(4px);
}

/* Floor Selected */
.floor-row.floor-selected {
  border-color: var(--color-gold);
  box-shadow: 0 0 16px rgba(var(--color-gold-rgb), 0.2);
}

/* Floor States */
.floor-row.floor-locked {
  opacity: 0.45;
  cursor: default;
}

.floor-row.floor-cleared {
  opacity: 0.75;
}

.floor-row.floor-available {
  opacity: 1;
}

/* Tier Colors: 1-5 Gold/Art Deco */
.floor-row.tier-gold {
  background: rgba(var(--color-gold-rgb), 0.08);
}

.floor-row.tier-gold::before {
  background: linear-gradient(90deg, var(--color-gold), var(--color-cream));
}

.floor-row.tier-gold .floor-num {
  color: var(--color-gold);
}

.floor-row.tier-gold.floor-selected {
  background: rgba(var(--color-gold-rgb), 0.15);
  border-color: var(--color-gold);
}

/* Tier Colors: 6-10 Gray/Purple */
.floor-row.tier-purple {
  background: rgba(106, 13, 173, 0.08);
}

.floor-row.tier-purple::before {
  background: linear-gradient(90deg, var(--color-gray), var(--color-purple));
}

.floor-row.tier-purple .floor-num {
  color: #b388ff;
}

.floor-row.tier-purple.floor-selected {
  background: rgba(106, 13, 173, 0.15);
  border-color: #b388ff;
}

/* Tier Colors: 11-15 Cosmic Horror */
.floor-row.tier-cosmic {
  background: rgba(106, 13, 173, 0.1);
}

.floor-row.tier-cosmic::before {
  background: linear-gradient(90deg, var(--color-purple), var(--color-neon-pink));
}

.floor-row.tier-cosmic .floor-num {
  color: var(--color-neon-pink);
}

.floor-row.tier-cosmic.floor-selected {
  background: rgba(255, 16, 240, 0.1);
  border-color: var(--color-neon-pink);
}

/* Floor Number */
.floor-number {
  width: 48px;
  flex-shrink: 0;
  text-align: center;
}

.floor-num {
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 0.02em;
}

/* Floor Info */
.floor-info {
  flex: 1;
  min-width: 0;
}

.floor-name {
  font-size: 15px;
  font-weight: 600;
  color: #f5f5f5;
  margin-bottom: 4px;
}

.boss-label {
  font-size: 13px;
  font-weight: normal;
  color: #ff8a65;
  margin-left: 8px;
}

.floor-details {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #999;
}

.floor-type-icon {
  font-size: 14px;
}

.reward-badge {
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: bold;
  text-transform: uppercase;
}

.reward-rare {
  background: rgba(33, 150, 243, 0.2);
  color: #64b5f6;
  border: 1px solid rgba(33, 150, 243, 0.3);
}

.reward-epic {
  background: rgba(156, 39, 176, 0.2);
  color: #ce93d8;
  border: 1px solid rgba(156, 39, 176, 0.3);
}

.reward-legendary {
  background: rgba(255, 152, 0, 0.2);
  color: #ffb74d;
  border: 1px solid rgba(255, 152, 0, 0.3);
}

/* Mutator Badge */
.mutator-badge {
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 10px;
  background: rgba(138, 43, 226, 0.2);
  color: #bb86fc;
  border: 1px solid rgba(138, 43, 226, 0.3);
  cursor: help;
}

.mutator-detail {
  flex-direction: column;
  align-items: flex-start;
}

.mutator-value {
  color: #bb86fc;
  font-size: 16px;
}

.mutator-description {
  font-size: 12px;
  color: #999;
  margin: 4px 0 0 0;
  font-style: italic;
}

/* Floor Status */
.floor-status {
  width: 32px;
  flex-shrink: 0;
  text-align: center;
}

.status-icon {
  font-size: 18px;
}

.locked-icon {
  color: #666;
}

.cleared-icon {
  color: var(--color-emerald);
}

.available-icon {
  color: var(--color-gold);
}

/* Floor Detail Panel */
.floor-detail-panel {
  background: rgba(45, 45, 63, 0.8);
  border-radius: 12px;
  padding: 20px;
  border: 2px solid rgba(var(--color-gold-rgb), 0.3);
}

.floor-detail-panel.tier-gold {
  border-color: rgba(var(--color-gold-rgb), 0.4);
}

.floor-detail-panel.tier-purple {
  border-color: rgba(179, 136, 255, 0.4);
}

.floor-detail-panel.tier-cosmic {
  border-color: rgba(255, 16, 240, 0.4);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.detail-header h2 {
  font-size: 20px;
  font-weight: bold;
  color: var(--color-gold);
  margin: 0;
}

.detail-boss-name {
  font-size: 14px;
  color: #ff8a65;
  font-style: italic;
}

.detail-description {
  font-size: 14px;
  color: #ccc;
  margin-bottom: 16px;
  line-height: 1.5;
}

.detail-stats {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.detail-stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-stat-label {
  font-size: 11px;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.detail-stat-value {
  font-size: 16px;
  font-weight: bold;
  color: #f5f5f5;
}

.reward-text.reward-rare {
  color: #64b5f6;
}

.reward-text.reward-epic {
  color: #ce93d8;
}

.reward-text.reward-legendary {
  color: #ffb74d;
}

/* Action Buttons */
.detail-actions {
  display: flex;
  justify-content: center;
}

.action-btn {
  padding: 14px 40px;
  font-size: 16px;
  font-weight: bold;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  letter-spacing: 0.05em;
}

.start-btn {
  background: linear-gradient(135deg, var(--color-gold) 0%, #f5e6a8 100%);
  color: var(--color-dark-navy);
  box-shadow: 0 4px 12px rgba(var(--color-gold-rgb), 0.4);
}

.start-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(var(--color-gold-rgb), 0.6);
}

.start-btn:active {
  transform: translateY(0);
}

.cleared-btn {
  background: rgba(76, 175, 80, 0.15);
  color: #81c784;
  border: 2px solid rgba(76, 175, 80, 0.4);
}

.cleared-btn:hover {
  background: rgba(76, 175, 80, 0.25);
}

.locked-btn {
  background: rgba(100, 100, 100, 0.15);
  color: #666;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 600px) {
  .progress-summary {
    gap: 16px;
    padding: 12px;
  }

  .stat-value {
    font-size: 20px;
  }

  .floor-row {
    padding: 10px 12px;
    gap: 8px;
  }

  .floor-num {
    font-size: 16px;
  }

  .floor-name {
    font-size: 14px;
  }

  .floor-tower {
    max-height: 400px;
  }

  .detail-stats {
    gap: 16px;
  }

  .action-btn {
    padding: 12px 32px;
    font-size: 15px;
  }
}
</style>
