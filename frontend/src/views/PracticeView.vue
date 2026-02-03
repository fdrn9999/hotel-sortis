<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useDiceRoller, type DiceResult } from '@/composables/useDiceRoller'
import { useCosmeticStore } from '@/stores/cosmetic'
import { SFX, BGM } from '@/composables/useSound'
import DiceRoller from '@/components/DiceRoller.vue'
import HandGuide from '@/components/HandGuide.vue'
import AppNavigation from '@/components/AppNavigation.vue'

const { t } = useI18n()
const router = useRouter()
const cosmeticStore = useCosmeticStore()

// Vue Dice Roller
const diceRoller = useDiceRoller()
const diceResult = ref<DiceResult | null>(null)

// Hand guide ref
const handGuideRef = ref<InstanceType<typeof HandGuide> | null>(null)

// Practice state (no timer, unlimited rolls)
const playerDice = ref<number[]>([])
const playerHand = ref<{ rank: string; rankKR: string; power: number } | null>(null)
const isRolling = ref(false)
const rollHistory = ref<Array<{ dice: number[]; hand: { rank: string; rankKR: string; power: number } }>>([])
const totalRolls = ref(0)

// Stats tracking
const handStats = ref<Record<string, number>>({
  Ace: 0,
  Triple: 0,
  Straight: 0,
  Strike: 0,
  Slash: 0,
  Storm: 0,
  Pair: 0,
  NoHand: 0
})

// Mapping rank names to i18n keys (preserves camelCase for NoHand)
const rankToI18nKey: Record<string, string> = {
  'Ace': 'ace',
  'Triple': 'triple',
  'Straight': 'straight',
  'Strike': 'strike',
  'Slash': 'slash',
  'Storm': 'storm',
  'Pair': 'pair',
  'NoHand': 'noHand'
}

// Keyboard listener for Tab key (HandGuide)
function handleKeyDown(e: KeyboardEvent) {
  if (e.key === 'Tab') {
    e.preventDefault()
    handGuideRef.value?.toggle()
  }
  // Space bar to roll
  if (e.key === ' ' && !isRolling.value) {
    e.preventDefault()
    rollDice()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown)
  BGM.play('menu')
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
  BGM.stop()
})

// Roll dice (practice mode - random, client-side since no real battle)
async function rollDice() {
  if (isRolling.value) return

  isRolling.value = true
  playerHand.value = null
  SFX.diceRoll()

  // Generate random dice (practice mode only - not a real battle)
  const dice: [number, number, number] = [
    Math.floor(Math.random() * 6) + 1,
    Math.floor(Math.random() * 6) + 1,
    Math.floor(Math.random() * 6) + 1
  ]

  // Set result for Vue dice roller
  diceResult.value = {
    die1: dice[0],
    die2: dice[1],
    die3: dice[2]
  }

  // Vue dice animation
  await diceRoller.rollTo(diceResult.value)

  // 2D animation
  await animateDiceRoll(dice)
  SFX.diceLand()

  // Show result
  playerDice.value = [...dice]
  const hand = evaluateHand(dice)
  playerHand.value = hand
  SFX.handComplete(hand.rank)

  // Update stats
  totalRolls.value++
  handStats.value[hand.rank] = (handStats.value[hand.rank] || 0) + 1

  // Add to history (keep last 10)
  rollHistory.value.unshift({ dice: [...dice], hand })
  if (rollHistory.value.length > 10) {
    rollHistory.value.pop()
  }

  isRolling.value = false
}

// Reset stats
function resetStats() {
  totalRolls.value = 0
  rollHistory.value = []
  playerDice.value = []
  playerHand.value = null
  handStats.value = {
    Ace: 0,
    Triple: 0,
    Straight: 0,
    Strike: 0,
    Slash: 0,
    Storm: 0,
    Pair: 0,
    NoHand: 0
  }
}

// Hand evaluation (Balanced System - practice mode)
function evaluateHand(dice: [number, number, number]) {
  const sorted = [...dice].sort((a, b) => a - b) as [number, number, number]
  const [a, b, c] = sorted

  if (a === 1 && b === 1 && c === 1) return { rank: 'Ace', rankKR: t('hands.ace'), power: 45 }
  if (a === b && b === c && a >= 2) return { rank: 'Triple', rankKR: t('hands.triple'), power: 8 + (a * 4) }
  if (a === 4 && b === 5 && c === 6) return { rank: 'Straight', rankKR: t('hands.straight'), power: 38 }
  if (a === 3 && b === 4 && c === 5) return { rank: 'Strike', rankKR: t('hands.strike'), power: 30 }
  if (a === 2 && b === 3 && c === 4) return { rank: 'Slash', rankKR: t('hands.slash'), power: 24 }
  if (a === 1 && b === 2 && c === 3) return { rank: 'Storm', rankKR: t('hands.storm'), power: 16 }
  if (a === b || b === c) {
    const pairValue = a === b ? a : b
    return { rank: 'Pair', rankKR: t('hands.pair'), power: 5 + (pairValue * 2) }
  }
  return { rank: 'NoHand', rankKR: t('hands.noHand'), power: a + b + c }
}

// Calculate percentage for a hand rank
function getPercentage(rank: string): string {
  if (totalRolls.value === 0) return '0.0%'
  return ((handStats.value[rank] / totalRolls.value) * 100).toFixed(1) + '%'
}

async function animateDiceRoll(finalDice: number[]) {
  const iterations = 8
  const delay = 80

  for (let i = 0; i < iterations; i++) {
    playerDice.value = [
      Math.floor(Math.random() * 6) + 1,
      Math.floor(Math.random() * 6) + 1,
      Math.floor(Math.random() * 6) + 1
    ]
    await new Promise(resolve => setTimeout(resolve, delay))
  }

  playerDice.value = [...finalDice]
}

function toggleHandGuide() {
  handGuideRef.value?.toggle()
}

function goHome() {
  router.push('/')
}
</script>

<template>
  <div class="practice-view">
    <!-- Navigation -->
    <AppNavigation
      :title="t('practice.title')"
      :show-home="true"
      :show-back="true"
      :show-settings="true"
      :on-home="goHome"
    />

    <div class="practice-content">
      <!-- Practice Header -->
      <div class="practice-header">
        <p class="practice-desc">{{ t('practice.description') }}</p>
        <div class="practice-badges">
          <span class="badge no-timer">{{ t('practice.noTimer') }}</span>
          <span class="badge unlimited">{{ t('practice.unlimited') }}</span>
        </div>
      </div>

      <!-- Dice Area -->
      <div class="dice-section">
        <!-- Vue Dice Roller -->
        <DiceRoller
          :is-rolling="diceRoller.isRolling.value"
          :result="diceResult"
          :skin="cosmeticStore.equippedDiceSkin"
          class="dice-roller-container"
        />

        <!-- 2D Dice Display -->
        <div class="dice-area">
          <template v-if="playerDice.length">
            <span v-for="(d, i) in playerDice" :key="i" class="die" :class="{ rolling: isRolling }">
              {{ d }}
            </span>
          </template>
          <template v-else>
            <span class="die placeholder">?</span>
            <span class="die placeholder">?</span>
            <span class="die placeholder">?</span>
          </template>
        </div>

        <!-- Hand Result -->
        <div v-if="playerHand" class="hand-result">
          <span class="hand-name">{{ playerHand.rankKR }}</span>
          <span class="hand-power">{{ playerHand.power }} {{ t('practice.damage') }}</span>
        </div>
      </div>

      <!-- Controls -->
      <div class="controls">
        <button
          class="roll-btn"
          :disabled="isRolling"
          @click="rollDice"
        >
          {{ isRolling ? t('battle.rolling') : t('battle.rollDice') }}
        </button>
        <button class="hand-guide-btn" @click="toggleHandGuide" :title="t('handGuide.title') + ' (Tab)'">
          ?
        </button>
      </div>

      <!-- Stats Panel -->
      <div class="stats-panel">
        <div class="stats-header">
          <h3>{{ t('practice.stats') }}</h3>
          <span class="total-rolls">{{ t('practice.totalRolls') }}: {{ totalRolls }}</span>
          <button class="reset-btn" @click="resetStats">{{ t('practice.reset') }}</button>
        </div>

        <div class="stats-grid">
          <div
            v-for="hand in ['Ace', 'Straight', 'Triple', 'Strike', 'Slash', 'Storm', 'Pair', 'NoHand']"
            :key="hand"
            class="stat-item"
            :class="{ active: playerHand?.rank === hand }"
          >
            <span class="stat-name">{{ t(`hands.${rankToI18nKey[hand]}`) }}</span>
            <span class="stat-count">{{ handStats[hand] || 0 }}</span>
            <span class="stat-pct">{{ getPercentage(hand) }}</span>
          </div>
        </div>
      </div>

      <!-- Roll History -->
      <div v-if="rollHistory.length > 0" class="history-panel">
        <h3>{{ t('practice.history') }}</h3>
        <div class="history-list">
          <div
            v-for="(entry, i) in rollHistory"
            :key="i"
            class="history-item"
          >
            <span class="history-dice">
              <span v-for="(d, j) in entry.dice" :key="j" class="mini-die">{{ d }}</span>
            </span>
            <span class="history-hand">{{ entry.hand.rankKR }}</span>
            <span class="history-power">{{ entry.hand.power }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Hand Guide -->
    <HandGuide
      ref="handGuideRef"
      :current-dice="playerDice.length === 3 ? playerDice : undefined"
      :highlight-rank="playerHand?.rank"
    />
  </div>
</template>

<style scoped>
.practice-view {
  min-height: 100vh;
  background: linear-gradient(180deg, #0d0d15 0%, #1b1b27 50%, #0d0d15 100%);
  padding-top: 60px;
}

.practice-content {
  max-width: 600px;
  margin: 0 auto;
  padding: 16px 20px 40px;
}

/* Header */
.practice-header {
  text-align: center;
  margin-bottom: 24px;
}

.practice-desc {
  margin: 0 0 12px;
  font-size: 14px;
  color: rgba(255, 253, 208, 0.7);
  line-height: 1.5;
}

.practice-badges {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.badge.no-timer {
  background: rgba(76, 175, 80, 0.15);
  border: 1px solid rgba(76, 175, 80, 0.3);
  color: #66bb6a;
}

.badge.unlimited {
  background: rgba(59, 130, 246, 0.15);
  border: 1px solid rgba(59, 130, 246, 0.3);
  color: #60a5fa;
}

/* Dice section */
.dice-section {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
}

.dice-roller-container {
  width: 100%;
  min-height: 100px;
  margin-bottom: 8px;
}

.dice-area {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 12px 0;
}

.die {
  width: 60px;
  height: 60px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.3), rgba(212, 175, 55, 0.15));
  border: 2px solid #d4af37;
  border-radius: 10px;
  color: #d4af37;
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.2);
  transition: all 0.3s ease;
}

.die.placeholder {
  opacity: 0.3;
}

.die.rolling {
  animation: dice-shake 0.1s infinite;
}

@keyframes dice-shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-2px) rotate(-2deg); }
  75% { transform: translateX(2px) rotate(2deg); }
}

.hand-result {
  text-align: center;
  padding: 12px 0;
}

.hand-name {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #d4af37;
  margin-bottom: 4px;
}

.hand-power {
  font-size: 16px;
  color: #ff6b6b;
  font-weight: 600;
}

/* Controls */
.controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.roll-btn {
  flex: 1;
  max-width: 280px;
  height: 56px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.3), rgba(212, 175, 55, 0.2));
  border: 2px solid #d4af37;
  border-radius: 12px;
  color: #d4af37;
  font-size: 18px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.roll-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.5), rgba(212, 175, 55, 0.3));
  box-shadow: 0 4px 20px rgba(212, 175, 55, 0.4);
  transform: translateY(-2px);
}

.roll-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.hand-guide-btn {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(59, 130, 246, 0.2);
  border: 2px solid rgba(59, 130, 246, 0.4);
  border-radius: 50%;
  color: #60a5fa;
  font-size: 22px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.hand-guide-btn:hover {
  background: rgba(59, 130, 246, 0.3);
  border-color: rgba(59, 130, 246, 0.6);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

/* Stats panel */
.stats-panel {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.stats-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.stats-header h3 {
  margin: 0;
  font-size: 16px;
  color: #d4af37;
  flex: 1;
}

.total-rolls {
  font-size: 13px;
  color: rgba(255, 253, 208, 0.5);
}

.reset-btn {
  padding: 4px 12px;
  background: rgba(100, 100, 100, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.reset-btn:hover {
  background: rgba(100, 100, 100, 0.5);
  color: #ffffff;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 6px;
  transition: background 0.3s;
}

.stat-item.active {
  background: rgba(212, 175, 55, 0.15);
}

.stat-name {
  flex: 1;
  font-size: 13px;
  color: rgba(255, 253, 208, 0.8);
}

.stat-count {
  font-size: 14px;
  font-weight: 600;
  color: #d4af37;
  min-width: 24px;
  text-align: right;
}

.stat-pct {
  font-size: 12px;
  color: rgba(255, 253, 208, 0.4);
  min-width: 48px;
  text-align: right;
}

/* History panel */
.history-panel {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 16px;
}

.history-panel h3 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #d4af37;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 8px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.02);
}

.history-dice {
  display: flex;
  gap: 4px;
}

.mini-die {
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 4px;
  color: #fffdd0;
  font-size: 12px;
  font-weight: 600;
}

.history-hand {
  flex: 1;
  font-size: 13px;
  color: rgba(255, 253, 208, 0.7);
}

.history-power {
  font-size: 13px;
  font-weight: 600;
  color: #ff6b6b;
}

/* Responsive */
@media (max-width: 480px) {
  .practice-content {
    padding: 12px 12px 32px;
  }

  .die {
    width: 48px;
    height: 48px;
    font-size: 22px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .dice-3d-container {
    height: 110px;
  }
}
</style>
