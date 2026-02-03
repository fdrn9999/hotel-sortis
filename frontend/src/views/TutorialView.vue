<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useTutorial, TUTORIAL_DICE_SCRIPTS } from '@/composables/useTutorial'
import { useDiceRoller, type DiceResult } from '@/composables/useDiceRoller'
import { SFX, BGM } from '@/composables/useSound'
import DiceRoller from '@/components/DiceRoller.vue'
import TutorialOverlay from '@/components/TutorialOverlay.vue'
import HandGuide from '@/components/HandGuide.vue'
import AppNavigation from '@/components/AppNavigation.vue'

const { t } = useI18n()
const router = useRouter()

// Tutorial manager
const tutorial = useTutorial()

// Vue Dice Roller
const diceRoller = useDiceRoller()
const diceResult = ref<DiceResult | null>(null)

// Hand guide ref
const handGuideRef = ref<InstanceType<typeof HandGuide> | null>(null)

// Battle state
const playerHP = ref(100)
const enemyHP = ref(100)
const playerDice = ref<number[]>([])
const enemyDice = ref<number[]>([])
const playerHand = ref<{ rank: string; rankKR: string; power: number } | null>(null)
const enemyHand = ref<{ rank: string; rankKR: string; power: number } | null>(null)
const isRolling = ref(false)
const turnCount = ref(1)
const battleOver = ref(false)
const battleLog = ref<string[]>([])

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

// Tutorial overlay state
const overlayVisible = ref(false)
const overlayDialogue = ref('')
const overlaySpeaker = ref('')
const overlayIsLucifuge = ref(false)
const overlayHighlight = ref<string | undefined>(undefined)
const overlayBubblePos = ref<'top' | 'bottom' | 'left' | 'right'>('bottom')
const overlayShowContinue = ref(true)
const canRoll = ref(false)

// Keyboard listener for Tab key (HandGuide)
function handleKeyDown(e: KeyboardEvent) {
  if (e.key === 'Tab') {
    e.preventDefault()
    handGuideRef.value?.toggle()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown)
  BGM.play('menu')
  startTutorial()
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
  BGM.stop()
})

// Start the tutorial sequence
function startTutorial() {
  tutorial.start()
  processCurrentStep()
}

// Process the current tutorial step
function processCurrentStep() {
  const step = tutorial.currentStep.value
  if (!step) {
    onTutorialComplete()
    return
  }

  switch (step.type) {
    case 'dialogue':
      showDialogue(step.dialogueKey || '', step.id.startsWith('lucifuge') || step.id === 'welcome' || step.id === 'farewell' || step.id === 'explain_hotel' || step.id === 'explain_dice' || step.id === 'skill_intro')
      break

    case 'highlight':
      showHighlight(step.dialogueKey || '', step.highlightTarget || '', step.bubblePosition || 'bottom')
      break

    case 'wait_roll':
      overlayVisible.value = false
      canRoll.value = true
      break

    case 'show_hand_result':
      showHandResult()
      break

    case 'force_skill':
      showForceSkill()
      break

    case 'enemy_turn':
      processEnemyTurn()
      break

    case 'battle_end':
      showBattleEnd()
      break

    case 'wait_continue':
      overlayVisible.value = true
      overlayShowContinue.value = true
      break
  }
}

function showDialogue(key: string, isLucifuge: boolean) {
  overlayVisible.value = true
  overlayDialogue.value = t(key)
  overlaySpeaker.value = isLucifuge ? 'Lucifuge Rofocale' : t('tutorial.guide.name')
  overlayIsLucifuge.value = isLucifuge
  overlayHighlight.value = undefined
  overlayShowContinue.value = true
}

function showHighlight(key: string, selector: string, pos: 'top' | 'bottom' | 'left' | 'right') {
  overlayVisible.value = true
  overlayDialogue.value = t(key)
  overlaySpeaker.value = t('tutorial.guide.name')
  overlayIsLucifuge.value = false
  overlayHighlight.value = selector
  overlayBubblePos.value = pos
  overlayShowContinue.value = true
}

function showHandResult() {
  if (!playerHand.value) {
    advanceStep()
    return
  }

  const handKey = rankToI18nKey[playerHand.value.rank] || playerHand.value.rank.toLowerCase()
  overlayVisible.value = true
  overlayDialogue.value = t('tutorial.guide.handExplanation', {
    hand: t(`hands.${handKey}`),
    power: playerHand.value.power
  })
  overlaySpeaker.value = t('tutorial.guide.name')
  overlayIsLucifuge.value = false
  overlayHighlight.value = '.hand-result.player-hand'
  overlayBubblePos.value = 'top'
  overlayShowContinue.value = true
}

function showForceSkill() {
  overlayVisible.value = true
  overlayDialogue.value = t('tutorial.guide.useSkill')
  overlaySpeaker.value = t('tutorial.guide.name')
  overlayIsLucifuge.value = false
  overlayHighlight.value = undefined
  overlayShowContinue.value = true
}

async function processEnemyTurn() {
  overlayVisible.value = false
  canRoll.value = false

  const script = tutorial.getScriptedDice()
  if (!script) {
    advanceStep()
    return
  }

  // Use the enemy dice from the PREVIOUS roll (rollCount - 1)
  const rollIdx = tutorial.rollCount.value - 1
  if (rollIdx < 0) {
    advanceStep()
    return
  }

  const prevScript = TUTORIAL_DICE_SCRIPTS[rollIdx]
  if (!prevScript) {
    advanceStep()
    return
  }

  addLog(t('tutorial.enemyTurnLog'))
  SFX.turnChange()

  // Animate enemy dice
  await animateDiceRoll(prevScript.enemyDice, 'enemy')

  enemyDice.value = [...prevScript.enemyDice]
  const hand = evaluateHand(prevScript.enemyDice)
  enemyHand.value = hand
  SFX.handComplete(hand.rank)

  // Apply damage to player
  const damage = hand.power
  playerHP.value = Math.max(0, playerHP.value - damage)
  if (damage > 0) SFX.damageTaken()

  addLog(`${t('battle.enemyHP')}: ${hand.rankKR} (${hand.power})`)

  // Wait for animation
  await sleep(1500)

  advanceStep()
}

function showBattleEnd() {
  battleOver.value = true
  SFX.victory()

  overlayVisible.value = true
  overlayDialogue.value = t('tutorial.lucifuge.defeat')
  overlaySpeaker.value = 'Lucifuge Rofocale'
  overlayIsLucifuge.value = true
  overlayShowContinue.value = true
}

// Roll dice (tutorial mode - scripted)
async function rollDice() {
  if (isRolling.value || !canRoll.value || battleOver.value) return

  isRolling.value = true
  canRoll.value = false
  SFX.diceRoll()

  const script = tutorial.getScriptedDice()
  if (!script) {
    isRolling.value = false
    return
  }

  // Clear previous results
  playerHand.value = null
  enemyHand.value = null
  enemyDice.value = []

  // Set result for Vue dice roller
  diceResult.value = {
    die1: script.playerDice[0],
    die2: script.playerDice[1],
    die3: script.playerDice[2]
  }

  // Vue dice animation
  await diceRoller.rollTo(diceResult.value)

  // Animate 2D dice
  await animateDiceRoll(script.playerDice, 'player')
  SFX.diceLand()

  // Show result
  playerDice.value = [...script.playerDice]
  const hand = evaluateHand(script.playerDice)
  playerHand.value = hand
  SFX.handComplete(hand.rank)

  // Apply damage to enemy
  const damage = hand.power
  enemyHP.value = Math.max(0, enemyHP.value - damage)
  if (damage > 0) SFX.damageDealt()

  addLog(`${t('battle.playerHP')}: ${hand.rankKR} (${hand.power})`)

  tutorial.recordRoll()
  turnCount.value++

  isRolling.value = false

  // Advance to show_hand_result step
  advanceStep()
}

// Advance to next tutorial step
function advanceStep() {
  tutorial.nextStep()
  processCurrentStep()
}

// Skip entire tutorial
function skipTutorial() {
  tutorial.skip()
  onTutorialComplete()
}

// Tutorial complete - navigate to home/campaign
function onTutorialComplete() {
  overlayVisible.value = false
  router.push('/')
}

// Toggle hand guide
function toggleHandGuide() {
  handGuideRef.value?.toggle()
}

// --- Utility ---

function addLog(message: string) {
  battleLog.value.push(message)
  if (battleLog.value.length > 5) {
    battleLog.value.shift()
  }
}

// Hand evaluation (Balanced System)
function evaluateHand(dice: number[]) {
  const sorted = [...dice].sort((a, b) => a - b)
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

async function animateDiceRoll(finalDice: number[], target: 'player' | 'enemy') {
  const iterations = 8
  const delay = 80

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
    await sleep(delay)
  }

  if (target === 'player') {
    playerDice.value = [...finalDice]
  } else {
    enemyDice.value = [...finalDice]
  }
}

function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

function goHome() {
  router.push('/')
}
</script>

<template>
  <div class="tutorial-battle">
    <!-- Navigation -->
    <AppNavigation
      :title="t('tutorial.title')"
      :show-home="true"
      :show-back="false"
      :show-settings="true"
      :on-home="goHome"
    />

    <!-- Main Battle Area -->
    <div class="battle-content">
      <!-- Floor indicator -->
      <div class="floor-indicator">
        <span class="floor-label">{{ t('tutorial.floor0') }}</span>
        <span class="floor-subtitle">{{ t('tutorial.theLobby') }}</span>
      </div>

      <!-- Enemy Area -->
      <div class="enemy-area">
        <div class="character-label">
          <span class="character-name">Lucifuge Rofocale</span>
        </div>
        <div class="hp-bar">
          <div class="hp-fill enemy" :style="{ width: `${enemyHP}%` }"></div>
          <span class="hp-text">{{ enemyHP }}/100</span>
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

        <!-- Vue Dice Roller -->
        <DiceRoller
          :is-rolling="diceRoller.isRolling.value"
          :result="diceResult"
          class="dice-roller-container"
        />

        <!-- 2D Dice -->
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

        <div class="character-label">
          <span class="character-name">{{ t('tutorial.player') }}</span>
        </div>
        <div class="hp-bar">
          <div class="hp-fill player" :style="{ width: `${playerHP}%` }"></div>
          <span class="hp-text">{{ playerHP }}/100</span>
        </div>
      </div>
    </div>

    <!-- Controls -->
    <div class="battle-controls">
      <button
        class="roll-btn"
        :disabled="!canRoll || isRolling || battleOver"
        @click="rollDice"
      >
        {{ isRolling ? t('battle.rolling') : t('battle.rollDice') }}
      </button>
      <button
        class="hand-guide-btn"
        @click="toggleHandGuide"
        :title="t('handGuide.title') + ' (Tab)'"
      >
        ?
      </button>
    </div>

    <!-- Tutorial Overlay -->
    <TutorialOverlay
      :visible="overlayVisible"
      :dialogue-text="overlayDialogue"
      :speaker-name="overlaySpeaker"
      :is-lucifuge="overlayIsLucifuge"
      :highlight-selector="overlayHighlight"
      :bubble-position="overlayBubblePos"
      :show-continue="overlayShowContinue"
      :show-skip-btn="!battleOver"
      :progress="tutorial.progress.value"
      @next="advanceStep"
      @skip="skipTutorial"
    />

    <!-- Hand Guide -->
    <HandGuide
      ref="handGuideRef"
      :current-dice="playerDice.length === 3 ? playerDice : undefined"
      :highlight-rank="playerHand?.rank"
    />
  </div>
</template>

<style scoped>
.tutorial-battle {
  min-height: 100vh;
  background: linear-gradient(180deg, #0d0d15 0%, #1b1b27 50%, #0d0d15 100%);
  padding-top: 60px;
  display: flex;
  flex-direction: column;
}

.battle-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 16px 20px;
  max-width: 600px;
  margin: 0 auto;
  width: 100%;
}

/* Floor indicator */
.floor-indicator {
  text-align: center;
  padding: 8px 0;
}

.floor-label {
  display: block;
  font-size: 14px;
  color: #d4af37;
  font-weight: 600;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.floor-subtitle {
  display: block;
  font-size: 12px;
  color: rgba(255, 253, 208, 0.5);
  margin-top: 2px;
}

/* Character areas */
.character-label {
  text-align: center;
  margin-bottom: 4px;
}

.character-name {
  font-size: 14px;
  font-weight: 600;
  color: #d4af37;
  letter-spacing: 0.5px;
}

/* HP bars */
.hp-bar {
  position: relative;
  width: 100%;
  height: 28px;
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  overflow: hidden;
  margin: 4px 0;
}

.hp-fill {
  height: 100%;
  border-radius: 14px;
  transition: width 0.5s ease;
}

.hp-fill.player {
  background: linear-gradient(90deg, #4caf50, #66bb6a);
}

.hp-fill.enemy {
  background: linear-gradient(90deg, #f44336, #e57373);
}

.hp-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

/* Dice area */
.dice-area {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 12px 0;
}

.die {
  width: 52px;
  height: 52px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.player-die {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.3), rgba(212, 175, 55, 0.15));
  border: 2px solid #d4af37;
  color: #d4af37;
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.2);
}

.enemy-die {
  background: linear-gradient(135deg, rgba(244, 67, 54, 0.2), rgba(244, 67, 54, 0.1));
  border: 2px solid rgba(244, 67, 54, 0.5);
  color: #e57373;
}

.die.rolling {
  animation: dice-shake 0.1s infinite;
}

@keyframes dice-shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-2px) rotate(-2deg); }
  75% { transform: translateX(2px) rotate(2deg); }
}

/* Vue Dice Roller container */
.dice-roller-container {
  width: 100%;
  min-height: 100px;
  margin: 4px 0;
}

/* Hand result */
.hand-result {
  text-align: center;
  padding: 6px 16px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 16px;
  margin: 4px 0;
}

.player-hand {
  background: rgba(212, 175, 55, 0.15);
  border: 1px solid rgba(212, 175, 55, 0.3);
  color: #d4af37;
}

.enemy-hand {
  background: rgba(244, 67, 54, 0.15);
  border: 1px solid rgba(244, 67, 54, 0.3);
  color: #e57373;
}

/* Battle log */
.battle-log {
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  margin: 8px 0;
  max-height: 80px;
  overflow-y: auto;
}

.log-header {
  font-size: 12px;
  color: #d4af37;
  font-weight: 600;
  margin-bottom: 4px;
}

.log-entries p {
  margin: 0;
  font-size: 12px;
  color: rgba(255, 253, 208, 0.6);
  line-height: 1.4;
}

/* Controls */
.battle-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 16px 20px 32px;
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

.roll-btn:active:not(:disabled) {
  transform: translateY(0);
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
  transform: translateY(-2px);
}

/* Enemy area */
.enemy-area,
.player-area {
  flex-shrink: 0;
}

/* Responsive */
@media (max-width: 480px) {
  .battle-content {
    padding: 8px 12px;
  }

  .die {
    width: 44px;
    height: 44px;
    font-size: 20px;
  }

  .roll-btn {
    height: 48px;
    font-size: 16px;
  }

  .dice-3d-container {
    height: 100px;
  }
}
</style>
