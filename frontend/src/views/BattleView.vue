<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter, useRoute } from 'vue-router'
import { battleApi, type RollDiceResponse, type EnemyTurnResult } from '@/api/battle'
import { useDiceScene } from '@/composables/useDiceScene'
import { useCampaignStore } from '@/stores/campaign'
import SkillSelectionView from '@/views/SkillSelectionView.vue'
import type { SkillRewardOption } from '@/types/game'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const campaignStore = useCampaignStore()

// 3D Dice Scene
const diceContainer = ref<HTMLElement | null>(null)
const { rollTo: rollTo3D } = useDiceScene(diceContainer)

// Campaign context from route query
const isCampaignMode = computed(() => route.query.campaignMode === 'true')
const campaignFloor = computed(() => Number(route.query.floor) || 1)
const campaignBattleIndex = computed(() => Number(route.query.battleIndex) || 1)
const campaignTotalBattles = computed(() => Number(route.query.totalBattles) || 1)
const campaignBattleId = computed(() => Number(route.query.battleId) || null)
const campaignBossId = computed(() => (route.query.bossId as string) || null)
const campaignBossName = computed(() => (route.query.bossName as string) || null)

// Battle state
const battleId = ref<number | null>(null)
const playerId = ref(1) // TODO: Get from auth
const floor = ref(1)
const playerHP = ref(100)
const enemyHP = ref(100)
const turnCount = ref(1)
const status = ref<'ONGOING' | 'VICTORY' | 'DEFEAT' | 'DRAW'>('ONGOING')

// Boss state
const bossId = ref<string | null>(null)
const bossName = ref<string | null>(null)
const bossPhase = ref(1)
const bossTotalPhases = ref(1)
const showPhaseTransition = ref(false)
const phaseTransitionQuote = ref('')

// Skill reward state
const showSkillReward = ref(false)
const offeredSkills = ref<SkillRewardOption[]>([])

// Dice state
const playerDice = ref<number[]>([])
const enemyDice = ref<number[]>([])
const isRolling = ref(false)

// Hand result
const playerHand = ref<{ rank: string; rankKR: string; power: number } | null>(null)
const enemyHand = ref<{ rank: string; rankKR: string; power: number } | null>(null)

// Battle log
const battleLog = ref<string[]>([])

// Turn timer (30 seconds)
const TURN_TIME_LIMIT = 30
const timeRemaining = ref(TURN_TIME_LIMIT)
const timerInterval = ref<ReturnType<typeof setInterval> | null>(null)

const timerPercentage = computed(() => (timeRemaining.value / TURN_TIME_LIMIT) * 100)
const timerWarning = computed(() => timeRemaining.value <= 10)

// Campaign header text
const headerText = computed(() => {
  if (!isCampaignMode.value) return `${t('battle.turn')} ${floor.value}`
  if (bossId.value && bossName.value) {
    return `${campaignFloor.value}F - ${bossName.value} ${t('battle.bossPhase', { phase: bossPhase.value })}`
  }
  return `${campaignFloor.value}F - ${t('campaign.battleProgress', { current: campaignBattleIndex.value, total: campaignTotalBattles.value })}`
})

// Boss phase dots
const phaseDots = computed(() => {
  if (!bossId.value || bossTotalPhases.value <= 1) return []
  return Array.from({ length: bossTotalPhases.value }, (_, i) => ({
    index: i + 1,
    completed: i + 1 < bossPhase.value,
    active: i + 1 === bossPhase.value,
    pending: i + 1 > bossPhase.value
  }))
})

// Start battle on mount
onMounted(async () => {
  // Set campaign context
  if (isCampaignMode.value) {
    floor.value = campaignFloor.value
    bossId.value = campaignBossId.value
    bossName.value = campaignBossName.value
    if (campaignBattleId.value) {
      battleId.value = campaignBattleId.value
    }
    // Boss phase info from campaign store
    if (campaignStore.bossPhase) {
      bossPhase.value = campaignStore.bossPhase
    }
    if (campaignStore.bossTotalPhases) {
      bossTotalPhases.value = campaignStore.bossTotalPhases
    }
  }

  await startBattle()
})

onUnmounted(() => {
  stopTimer()
})

// Start a new battle
async function startBattle() {
  try {
    const equippedSkills = route.query.skills
      ? String(route.query.skills).split(',').map(Number).filter(n => !isNaN(n))
      : []

    const response = await battleApi.startBattle({
      playerId: playerId.value,
      battleType: 'PVE',
      floor: floor.value,
      equippedSkills,
      bossId: bossId.value || undefined,
      bossPhase: bossPhase.value || undefined
    })

    battleId.value = response.battleId
    playerHP.value = response.playerHp
    enemyHP.value = response.enemyHp
    turnCount.value = response.turnCount
    status.value = response.status

    // Update boss info from server response
    if (response.bossId) {
      bossId.value = response.bossId
      bossName.value = response.bossName || response.bossId
      bossPhase.value = response.bossPhase || 1
      bossTotalPhases.value = response.bossTotalPhases || 1
    }

    addLog(t('battle.turn') + ' ' + turnCount.value)
    startTimer()
  } catch (error) {
    console.error('Failed to start battle:', error)
    addLog('Failed to start battle. Using offline mode.')
    battleId.value = -1
    startTimer()
  }
}

// Roll dice (calls server API)
async function rollDice() {
  if (isRolling.value || status.value !== 'ONGOING') return
  if (!battleId.value) return

  isRolling.value = true
  stopTimer()

  // Clear previous results
  playerHand.value = null
  enemyHand.value = null
  enemyDice.value = []

  try {
    let response: RollDiceResponse

    if (battleId.value === -1) {
      // Offline mode for testing
      response = simulateRoll()

      // Start 3D dice animation with target result (synchronize with server)
      if (rollTo3D) {
        await rollTo3D({
          die1: response.dice[0],
          die2: response.dice[1],
          die3: response.dice[2]
        })
      }
    } else {
      // Call server API first - dice generated SERVER-SIDE!
      response = await battleApi.rollDice(battleId.value, {
        playerId: playerId.value
      })

      // Start 3D dice animation with server result (synchronize)
      if (rollTo3D) {
        await rollTo3D({
          die1: response.dice[0],
          die2: response.dice[1],
          die3: response.dice[2]
        })
      }
    }

    // Update player dice with animation delay
    await animateDiceRoll(response.dice, 'player')

    // Show player result
    playerDice.value = [...response.dice]
    playerHand.value = response.hand
    addLog(`${t('battle.playerHP')}: ${response.hand.rankKR} (${response.hand.power} ${t('battle.damage') || 'damage'})`)

    // Update enemy HP
    enemyHP.value = response.enemyHp

    // Handle boss phase transition
    if (response.bossPhaseTransition) {
      await handlePhaseTransition(response.bossPhaseTransition)
    }

    // Check for victory
    if (response.status === 'VICTORY') {
      status.value = 'VICTORY'
      addLog(t('battle.victory') + '!')

      // Campaign: handle post-victory flow
      if (isCampaignMode.value) {
        await handleCampaignVictory()
      }
      return
    }

    // Process enemy turn if exists
    if (response.enemyTurn) {
      await processEnemyTurn(response.enemyTurn)
      playerHP.value = response.playerHp
    }

    // Update battle state
    status.value = response.status
    turnCount.value++

    if (status.value === 'DEFEAT') {
      addLog(t('battle.defeat') + '!')
    } else if (status.value === 'DRAW') {
      addLog(t('battle.draw') + '!')
    } else {
      addLog(t('battle.turn') + ' ' + turnCount.value)
      startTimer()
    }
  } catch (error) {
    console.error('Failed to roll dice:', error)
    addLog('Error rolling dice')
  } finally {
    isRolling.value = false
  }
}

// Handle boss phase transition
async function handlePhaseTransition(transition: { bossId: string; newPhase: number; totalPhases: number; quote?: string; newPattern?: string }) {
  // Show phase transition overlay
  phaseTransitionQuote.value = transition.quote || ''
  showPhaseTransition.value = true
  bossPhase.value = transition.newPhase
  bossTotalPhases.value = transition.totalPhases

  // Update campaign store
  campaignStore.handleBossPhaseTransition({
    bossId: transition.bossId,
    newPhase: transition.newPhase,
    totalPhases: transition.totalPhases,
    quote: transition.quote,
    newPattern: transition.newPattern
  })

  // Wait for transition display
  await new Promise(resolve => setTimeout(resolve, 3000))

  // Reset enemy HP to 100 (new phase)
  enemyHP.value = 100

  // Hide transition overlay
  showPhaseTransition.value = false

  addLog(`${t('battle.phaseTransition')} - ${t('battle.bossPhase', { phase: bossPhase.value })}`)
}

// Handle campaign victory
async function handleCampaignVictory() {
  if (!campaignBattleId.value) return

  try {
    const completeResponse = await campaignStore.completeFloorBattle(
      playerId.value,
      campaignBattleId.value
    )

    if (completeResponse?.offeredSkills && completeResponse.offeredSkills.length > 0) {
      offeredSkills.value = completeResponse.offeredSkills
      showSkillReward.value = true
    }
  } catch (e) {
    console.error('Failed to complete floor battle:', e)
  }
}

// Handle skill reward selection
async function onSkillSelected(skill: { id: number }) {
  try {
    await campaignStore.completeFloorBattle(playerId.value, campaignBattleId.value!)
  } catch {
    // already completed above
  }

  // Use campaign API to select skill reward
  const { campaignApi } = await import('@/api/campaign')
  try {
    await campaignApi.selectSkillReward(playerId.value, campaignFloor.value, skill.id)
  } catch (e) {
    console.error('Failed to select skill reward:', e)
  }

  showSkillReward.value = false
  navigateToCampaign()
}

function onSkillRewardClose() {
  showSkillReward.value = false
  navigateToCampaign()
}

// Process enemy turn
async function processEnemyTurn(enemyTurn: EnemyTurnResult) {
  addLog('Enemy turn...')

  // Animate enemy dice
  await animateDiceRoll(enemyTurn.dice, 'enemy')

  enemyDice.value = [...enemyTurn.dice]
  enemyHand.value = enemyTurn.hand

  addLog(`${t('battle.enemyHP')}: ${enemyTurn.hand.rankKR} (${enemyTurn.hand.power} ${t('battle.damage') || 'damage'})`)
}

// Dice animation
async function animateDiceRoll(finalDice: number[], target: 'player' | 'enemy'): Promise<void> {
  const iterations = 10
  const delay = 100

  for (let i = 0; i < iterations; i++) {
    const randomDice = [
      Math.floor(Math.random() * 6) + 1,
      Math.floor(Math.random() * 6) + 1,
      Math.floor(Math.random() * 6) + 1
    ]

    if (target === 'player') {
      playerDice.value = randomDice
    } else {
      enemyDice.value = randomDice
    }

    await new Promise(resolve => setTimeout(resolve, delay))
  }

  // Set final dice
  if (target === 'player') {
    playerDice.value = [...finalDice]
  } else {
    enemyDice.value = [...finalDice]
  }
}

// Timer functions
function startTimer() {
  timeRemaining.value = TURN_TIME_LIMIT
  timerInterval.value = setInterval(() => {
    timeRemaining.value--
    if (timeRemaining.value <= 0) {
      onTimeout()
    }
  }, 1000)
}

function stopTimer() {
  if (timerInterval.value) {
    clearInterval(timerInterval.value)
    timerInterval.value = null
  }
}

function onTimeout() {
  stopTimer()
  addLog('Time out! Auto-rolling...')
  rollDice()
}

// Battle log
function addLog(message: string) {
  battleLog.value.push(message)
  // Keep only last 5 logs
  if (battleLog.value.length > 5) {
    battleLog.value.shift()
  }
}

// Offline simulation for testing
function simulateRoll(): RollDiceResponse {
  const dice: [number, number, number] = [
    Math.floor(Math.random() * 6) + 1,
    Math.floor(Math.random() * 6) + 1,
    Math.floor(Math.random() * 6) + 1
  ]

  const hand = evaluateHand(dice)
  const newEnemyHp = Math.max(0, enemyHP.value - hand.power)

  const enemyDiceRoll: [number, number, number] = [
    Math.floor(Math.random() * 6) + 1,
    Math.floor(Math.random() * 6) + 1,
    Math.floor(Math.random() * 6) + 1
  ]
  const enemyHandResult = evaluateHand(enemyDiceRoll)
  const newPlayerHp = Math.max(0, playerHP.value - enemyHandResult.power)

  let newStatus: 'ONGOING' | 'VICTORY' | 'DEFEAT' | 'DRAW' = 'ONGOING'
  if (newEnemyHp <= 0) newStatus = 'VICTORY'
  else if (newPlayerHp <= 0) newStatus = 'DEFEAT'
  else if (turnCount.value >= 10) newStatus = 'DRAW'

  return {
    dice,
    hash: 'offline',
    hand,
    damage: hand.power,
    playerHp: newPlayerHp,
    enemyHp: newEnemyHp,
    currentTurn: 'PLAYER',
    status: newStatus,
    enemyTurn: newEnemyHp > 0 ? {
      dice: enemyDiceRoll,
      hand: enemyHandResult,
      damage: enemyHandResult.power
    } : undefined
  }
}

// Hand evaluation (Balanced System - PROJECTPLAN.md) - offline mode only
function evaluateHand(dice: [number, number, number]) {
  const sorted = [...dice].sort((a, b) => a - b) as [number, number, number]
  const [a, b, c] = sorted

  if (a === 1 && b === 1 && c === 1) return { rank: 'Ace', rankKR: '에이스', power: 45 }
  if (a === b && b === c && a >= 2) return { rank: 'Triple', rankKR: '트리플', power: 8 + (a * 4) }
  if (a === 4 && b === 5 && c === 6) return { rank: 'Straight', rankKR: '스트레이트', power: 38 }
  if (a === 3 && b === 4 && c === 5) return { rank: 'Strike', rankKR: '스트라이크', power: 30 }
  if (a === 2 && b === 3 && c === 4) return { rank: 'Slash', rankKR: '슬래시', power: 24 }
  if (a === 1 && b === 2 && c === 3) return { rank: 'Storm', rankKR: '스톰', power: 16 }
  if (a === b || b === c) {
    const pairValue = a === b ? a : b
    return { rank: 'Pair', rankKR: '페어', power: 5 + (pairValue * 2) }
  }
  return { rank: 'NoHand', rankKR: '노 핸드', power: a + b + c }
}

// Navigation
function goHome() {
  stopTimer()
  router.push('/')
}

function navigateToCampaign() {
  stopTimer()
  router.push('/campaign')
}

function goToSettings() {
  router.push('/settings')
}

function nextBattle() {
  stopTimer()
  // Navigate back to campaign which will handle starting the next battle
  router.push('/campaign')
}
</script>

<template>
  <div class="battle">
    <!-- Header -->
    <header class="battle-header">
      <button class="nav-btn" @click="isCampaignMode ? navigateToCampaign() : goHome()">
        {{ isCampaignMode ? t('campaign.returnToCampaign') : t('common.home') }}
      </button>
      <span class="floor-info">{{ headerText }}</span>
      <button class="nav-btn" @click="goToSettings">{{ t('common.settings') }}</button>
    </header>

    <!-- Boss Phase Indicator -->
    <div v-if="bossId && phaseDots.length > 0" class="boss-phase-bar">
      <span class="boss-name-label">{{ bossName }}</span>
      <div class="phase-dots">
        <span
          v-for="dot in phaseDots"
          :key="dot.index"
          class="phase-dot"
          :class="{
            'dot-completed': dot.completed,
            'dot-active': dot.active,
            'dot-pending': dot.pending
          }"
        ></span>
      </div>
      <span class="phase-label">{{ t('battle.bossPhase', { phase: bossPhase }) }}</span>
    </div>

    <!-- Turn Timer -->
    <div class="turn-timer" v-if="status === 'ONGOING'">
      <div class="timer-bar" :class="{ warning: timerWarning }" :style="{ width: timerPercentage + '%' }"></div>
      <span class="timer-text">{{ timeRemaining }}s</span>
    </div>

    <!-- Battle Arena -->
    <div class="battle-arena">
      <!-- Enemy Area -->
      <div class="enemy-area">
        <div class="hp-bar">
          <div class="hp-fill enemy" :style="{ width: `${enemyHP}%` }"></div>
          <span class="hp-text">{{ bossName || t('battle.enemyHP') }}: {{ enemyHP }}/100</span>
        </div>
        <div class="dice-area">
          <template v-if="enemyDice.length">
            <span v-for="(d, i) in enemyDice" :key="'e'+i" class="die enemy-die">{{ d }}</span>
          </template>
          <template v-else>
            <span class="die enemy-die">?</span>
            <span class="die enemy-die">?</span>
            <span class="die enemy-die">?</span>
          </template>
        </div>
        <div v-if="enemyHand" class="hand-result enemy-hand">
          {{ enemyHand.rankKR }} ({{ enemyHand.power }})
        </div>
      </div>

      <!-- Battle Log -->
      <div class="battle-log">
        <div class="log-header">{{ t('battle.turn') }} {{ turnCount }}</div>
        <div class="log-entries">
          <p v-for="(log, i) in battleLog" :key="i">{{ log }}</p>
        </div>
      </div>

      <!-- Player Area -->
      <div class="player-area">
        <div v-if="playerHand" class="hand-result player-hand">
          {{ playerHand.rankKR }} ({{ playerHand.power }})
        </div>

        <!-- 3D Dice Scene -->
        <div ref="diceContainer" class="dice-3d-container"></div>

        <!-- 2D Dice (fallback / result display) -->
        <div class="dice-area">
          <template v-if="playerDice.length">
            <span v-for="(d, i) in playerDice" :key="'p'+i" class="die player-die" :class="{ rolling: isRolling }">
              {{ d }}
            </span>
          </template>
          <template v-else>
            <span class="die player-die">?</span>
            <span class="die player-die">?</span>
            <span class="die player-die">?</span>
          </template>
        </div>

        <div class="hp-bar">
          <div class="hp-fill player" :style="{ width: `${playerHP}%` }"></div>
          <span class="hp-text">{{ t('battle.playerHP') }}: {{ playerHP }}/100</span>
        </div>
      </div>
    </div>

    <!-- Controls -->
    <div class="battle-controls">
      <button
        v-if="status === 'ONGOING'"
        class="roll-btn"
        @click="rollDice"
        :disabled="isRolling"
      >
        {{ isRolling ? '...' : t('battle.rollDice') }} (Space)
      </button>
      <div v-else class="battle-result">
        <h2 :class="status.toLowerCase()">
          {{ status === 'VICTORY' ? t('battle.victory') : status === 'DEFEAT' ? t('battle.defeat') : t('battle.draw') }}
        </h2>

        <!-- Campaign mode post-battle buttons -->
        <template v-if="isCampaignMode">
          <button v-if="status === 'VICTORY' && !showSkillReward" class="nav-btn result-btn" @click="navigateToCampaign">
            {{ t('campaign.returnToCampaign') }}
          </button>
          <button v-else-if="status === 'DEFEAT'" class="nav-btn result-btn" @click="navigateToCampaign">
            {{ t('campaign.returnToCampaign') }}
          </button>
          <button v-else-if="status === 'DRAW'" class="nav-btn result-btn" @click="navigateToCampaign">
            {{ t('campaign.returnToCampaign') }}
          </button>
        </template>

        <!-- Default home button -->
        <button v-if="!isCampaignMode" class="nav-btn result-btn" @click="goHome">{{ t('common.home') }}</button>
      </div>
    </div>

    <!-- Boss Phase Transition Overlay -->
    <Teleport to="body">
      <div v-if="showPhaseTransition" class="phase-transition-overlay">
        <div class="phase-transition-content">
          <div class="phase-transition-title">{{ t('battle.phaseTransition') }}</div>
          <div class="phase-transition-boss">{{ bossName }}</div>
          <div v-if="phaseTransitionQuote" class="phase-transition-quote">
            "{{ phaseTransitionQuote }}"
          </div>
          <div class="phase-transition-phase">{{ t('battle.bossPhase', { phase: bossPhase }) }}</div>
        </div>
      </div>
    </Teleport>

    <!-- Skill Reward Modal -->
    <SkillSelectionView
      v-if="showSkillReward && offeredSkills.length > 0"
      :floor="campaignFloor"
      :offered-skills="offeredSkills as any"
      @skill-selected="onSkillSelected"
      @close="onSkillRewardClose"
    />
  </div>
</template>

<style scoped>
.battle {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, var(--color-dark-navy) 0%, #0D0D1A 100%);
}

/* Header */
.battle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid rgba(212, 175, 55, 0.3);
}

.nav-btn {
  padding: 0.5rem 1rem;
  border: 1px solid var(--color-gold);
  background: transparent;
  color: var(--color-gold);
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.nav-btn:hover {
  background: var(--color-gold);
  color: var(--color-bg);
}

.result-btn {
  margin-top: 0.5rem;
}

.floor-info {
  font-family: var(--font-primary);
  color: var(--color-gold);
  font-size: 0.95rem;
  text-align: center;
}

/* Boss Phase Bar */
.boss-phase-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 8px 16px;
  background: rgba(255, 138, 101, 0.1);
  border-bottom: 1px solid rgba(255, 138, 101, 0.2);
}

.boss-name-label {
  font-size: 14px;
  font-weight: bold;
  color: #ff8a65;
}

.phase-dots {
  display: flex;
  gap: 6px;
}

.phase-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #666;
  transition: all 0.3s;
}

.dot-completed {
  background: #4caf50;
  border-color: #4caf50;
}

.dot-active {
  background: #ff8a65;
  border-color: #ff8a65;
  box-shadow: 0 0 8px rgba(255, 138, 101, 0.6);
}

.dot-pending {
  background: transparent;
  border-color: #555;
}

.phase-label {
  font-size: 12px;
  color: #999;
}

/* Timer */
.turn-timer {
  height: 24px;
  background: #1a1a2e;
  position: relative;
  margin: 0.5rem 1rem;
  border-radius: 4px;
  overflow: hidden;
}

.timer-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--color-gold) 0%, var(--color-emerald) 100%);
  transition: width 1s linear;
}

.timer-bar.warning {
  background: linear-gradient(90deg, var(--color-velvet-red) 0%, #FF6B6B 100%);
}

.timer-text {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.8rem;
  color: var(--color-cream);
}

/* Battle Arena */
.battle-arena {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 1rem;
  gap: 1rem;
}

/* HP Bar */
.hp-bar {
  height: 32px;
  background: #1a1a2e;
  border-radius: 4px;
  position: relative;
  overflow: hidden;
}

.hp-fill {
  height: 100%;
  transition: width 0.5s ease;
}

.hp-fill.player {
  background: linear-gradient(90deg, #4CAF50 0%, #8BC34A 100%);
}

.hp-fill.enemy {
  background: linear-gradient(90deg, var(--color-velvet-red) 0%, #FF6B6B 100%);
}

.hp-text {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  font-size: 0.85rem;
  color: var(--color-cream);
  text-shadow: 0 0 4px rgba(0,0,0,0.8);
  z-index: 1;
}

/* 3D Dice Container */
.dice-3d-container {
  width: 100%;
  height: 300px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  margin: 0.5rem 0;
  border: 1px solid rgba(212, 175, 55, 0.2);
}

/* Dice Area */
.dice-area {
  display: flex;
  justify-content: center;
  gap: 1rem;
  padding: 1.5rem;
}

.die {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 1.8rem;
  font-weight: bold;
  transition: transform 0.1s;
}

.player-die {
  background: var(--color-cream);
  color: var(--color-dark-navy);
  box-shadow: 0 4px 8px rgba(0,0,0,0.3);
}

.enemy-die {
  background: #2a2a3e;
  color: var(--color-cream);
  border: 2px solid rgba(212, 175, 55, 0.3);
}

.die.rolling {
  animation: shake 0.1s infinite;
}

@keyframes shake {
  0%, 100% { transform: rotate(-2deg); }
  50% { transform: rotate(2deg); }
}

/* Hand Result */
.hand-result {
  text-align: center;
  padding: 0.5rem;
  font-family: var(--font-primary);
  font-size: 1.1rem;
}

.player-hand {
  color: var(--color-emerald);
}

.enemy-hand {
  color: #FF6B6B;
}

/* Battle Log */
.battle-log {
  background: rgba(0, 0, 0, 0.4);
  border-radius: 8px;
  padding: 1rem;
  min-height: 120px;
}

.log-header {
  color: var(--color-gold);
  font-family: var(--font-primary);
  margin-bottom: 0.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(212, 175, 55, 0.2);
}

.log-entries {
  font-size: 0.85rem;
  color: var(--color-cream);
  opacity: 0.8;
}

.log-entries p {
  margin: 0.25rem 0;
}

/* Controls */
.battle-controls {
  padding: 1.5rem;
  text-align: center;
}

.roll-btn {
  padding: 1rem 4rem;
  font-size: 1.3rem;
  font-family: var(--font-primary);
  border: 2px solid var(--color-gold);
  background: var(--color-gold);
  color: var(--color-dark-navy);
  cursor: pointer;
  transition: all 0.3s ease;
  letter-spacing: 0.1em;
}

.roll-btn:hover:not(:disabled) {
  background: var(--color-cream);
  transform: scale(1.02);
}

.roll-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Battle Result */
.battle-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.battle-result h2 {
  font-family: var(--font-primary);
  font-size: 2.5rem;
  letter-spacing: 0.2em;
}

.battle-result h2.victory {
  color: var(--color-gold);
}

.battle-result h2.defeat {
  color: var(--color-velvet-red);
}

.battle-result h2.draw {
  color: var(--color-gray);
}

/* Phase Transition Overlay */
.phase-transition-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 3000;
  animation: fadeIn 0.5s ease;
}

.phase-transition-content {
  text-align: center;
  animation: slideUp 0.5s ease 0.3s both;
}

.phase-transition-title {
  font-size: 16px;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 4px;
  margin-bottom: 16px;
}

.phase-transition-boss {
  font-size: 48px;
  font-weight: bold;
  color: #ff8a65;
  margin-bottom: 24px;
  letter-spacing: 2px;
}

.phase-transition-quote {
  font-size: 20px;
  color: #fffdd0;
  font-style: italic;
  margin-bottom: 24px;
  max-width: 500px;
  line-height: 1.6;
}

.phase-transition-phase {
  font-size: 24px;
  color: #d4af37;
  font-weight: bold;
  letter-spacing: 2px;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Responsive */
@media (max-width: 600px) {
  .die {
    width: 52px;
    height: 52px;
    font-size: 1.5rem;
  }

  .roll-btn {
    padding: 0.8rem 2rem;
    font-size: 1.1rem;
  }

  .floor-info {
    font-size: 0.8rem;
  }

  .phase-transition-boss {
    font-size: 32px;
  }

  .phase-transition-quote {
    font-size: 16px;
    padding: 0 20px;
  }
}
</style>
