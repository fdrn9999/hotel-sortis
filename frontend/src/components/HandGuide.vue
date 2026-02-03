<template>
  <Teleport to="body">
    <Transition name="guide">
      <div v-if="isOpen" class="hand-guide-overlay" @click="handleOverlayClick">
        <div class="hand-guide-panel" @click.stop>
          <!-- Header -->
          <div class="guide-header">
            <h2 class="guide-title">{{ $t('handGuide.title') }}</h2>
            <button class="close-btn" @click="close" :aria-label="$t('common.close')">✕</button>
          </div>

          <!-- Hand ranking table -->
          <div class="guide-content">
            <table class="hand-table">
              <thead>
                <tr>
                  <th class="col-rank">#</th>
                  <th class="col-name">{{ $t('handGuide.hand') }}</th>
                  <th class="col-dice">{{ $t('handGuide.dice') }}</th>
                  <th class="col-power">{{ $t('handGuide.power') }}</th>
                  <th class="col-prob">{{ $t('handGuide.probability') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(hand, index) in hands"
                  :key="hand.rank"
                  class="hand-row"
                  :class="{
                    'highlighted': isHighlighted(hand.rank),
                    'possible': isPossible(hand.rank)
                  }"
                >
                  <td class="col-rank">{{ index + 1 }}</td>
                  <td class="col-name">
                    <span class="hand-name" :class="`rarity-${hand.rarity}`">
                      {{ $t(`hands.${rankToI18nKey[hand.rank]}`) }}
                    </span>
                  </td>
                  <td class="col-dice">
                    <div class="dice-example">
                      <span
                        v-for="(d, i) in hand.example"
                        :key="i"
                        class="mini-die"
                      >{{ d }}</span>
                    </div>
                  </td>
                  <td class="col-power">
                    <span class="power-value">{{ hand.powerDisplay }}</span>
                  </td>
                  <td class="col-prob">
                    <span class="prob-value">{{ hand.probability }}</span>
                  </td>
                </tr>
              </tbody>
            </table>

            <!-- Dynamic highlight info -->
            <div v-if="currentDice && currentDice.length === 3" class="current-dice-info">
              <div class="current-dice-label">{{ $t('handGuide.currentDice') }}</div>
              <div class="current-dice-display">
                <span v-for="(d, i) in currentDice" :key="i" class="current-die">{{ d }}</span>
              </div>
            </div>

            <!-- Tip section -->
            <div class="guide-tip">
              <p>{{ $t('handGuide.tip') }}</p>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'

useI18n()

const props = defineProps<{
  currentDice?: number[]
  highlightRank?: string
}>()

const isOpen = ref(false)

interface HandInfo {
  rank: string
  example: number[]
  powerDisplay: string
  probability: string
  rarity: string
}

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

// All 8 hands with probabilities (PROJECTPLAN.md 2.1.1)
// Order: Ace > Triple > Straight > Strike > Slash > Storm > Pair > NoHand
const hands = computed<HandInfo[]>(() => [
  {
    rank: 'Ace',
    example: [1, 1, 1],
    powerDisplay: '45',
    probability: '0.46%',
    rarity: 'legendary'
  },
  {
    rank: 'Triple',
    example: [6, 6, 6],
    powerDisplay: '16~32',
    probability: '2.31%',
    rarity: 'epic'
  },
  {
    rank: 'Straight',
    example: [4, 5, 6],
    powerDisplay: '38',
    probability: '2.78%',
    rarity: 'epic'
  },
  {
    rank: 'Strike',
    example: [3, 4, 5],
    powerDisplay: '30',
    probability: '2.78%',
    rarity: 'rare'
  },
  {
    rank: 'Slash',
    example: [2, 3, 4],
    powerDisplay: '24',
    probability: '2.78%',
    rarity: 'rare'
  },
  {
    rank: 'Storm',
    example: [1, 2, 3],
    powerDisplay: '16',
    probability: '2.78%',
    rarity: 'common'
  },
  {
    rank: 'Pair',
    example: [5, 5, 3],
    powerDisplay: '7~17',
    probability: '44.44%',
    rarity: 'common'
  },
  {
    rank: 'NoHand',
    example: [1, 3, 5],
    powerDisplay: '3~16',
    probability: '41.67%',
    rarity: 'common'
  }
])

function isHighlighted(rank: string): boolean {
  return props.highlightRank === rank
}

/** Check if a hand is possible given current dice */
function isPossible(rank: string): boolean {
  if (!props.currentDice || props.currentDice.length !== 3) return false

  const sorted = [...props.currentDice].sort((a, b) => a - b)
  const [a, b, c] = sorted

  switch (rank) {
    case 'Ace':
      return a === 1 && b === 1 && c === 1
    case 'Triple':
      return a === b && b === c && a >= 2
    case 'Straight':
      return a === 4 && b === 5 && c === 6
    case 'Strike':
      return a === 3 && b === 4 && c === 5
    case 'Slash':
      return a === 2 && b === 3 && c === 4
    case 'Storm':
      return a === 1 && b === 2 && c === 3
    case 'Pair':
      return (a === b || b === c) && !(a === b && b === c)
    case 'NoHand':
      return a !== b && b !== c &&
        !(a === 1 && b === 2 && c === 3) &&
        !(a === 2 && b === 3 && c === 4) &&
        !(a === 3 && b === 4 && c === 5) &&
        !(a === 4 && b === 5 && c === 6)
    default:
      return false
  }
}

function open() {
  isOpen.value = true
}

function close() {
  isOpen.value = false
}

function toggle() {
  isOpen.value = !isOpen.value
}

function handleOverlayClick() {
  close()
}

defineExpose({ open, close, toggle, isOpen })
</script>

<style scoped>
.hand-guide-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 8000;
  padding: 20px;
}

.hand-guide-panel {
  width: 100%;
  max-width: 560px;
  max-height: 85vh;
  background: linear-gradient(135deg, rgba(27, 27, 39, 0.98) 0%, rgba(40, 40, 60, 0.98) 100%);
  border: 2px solid #d4af37;
  border-radius: 16px;
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.6),
    0 0 30px rgba(212, 175, 55, 0.2);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.guide-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 16px;
  border-bottom: 2px solid rgba(212, 175, 55, 0.3);
  background: linear-gradient(to bottom, rgba(212, 175, 55, 0.1), transparent);
}

.guide-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #d4af37;
  letter-spacing: 0.5px;
}

.close-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(100, 100, 100, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(100, 100, 100, 0.5);
  color: #ffffff;
}

.guide-content {
  padding: 16px 24px 24px;
  overflow-y: auto;
  flex: 1;
}

/* Hand ranking table */
.hand-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 16px;
}

.hand-table th {
  padding: 8px 6px;
  font-size: 12px;
  font-weight: 600;
  color: rgba(212, 175, 55, 0.8);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  text-align: left;
  border-bottom: 1px solid rgba(212, 175, 55, 0.2);
}

.hand-table td {
  padding: 10px 6px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.col-rank {
  width: 30px;
  text-align: center;
  color: rgba(255, 255, 255, 0.4);
  font-size: 12px;
}

.hand-name {
  font-weight: 600;
  font-size: 14px;
}

.rarity-legendary { color: #ff8c00; }
.rarity-epic { color: #a855f7; }
.rarity-rare { color: #3b82f6; }
.rarity-common { color: #fffdd0; }

.dice-example {
  display: flex;
  gap: 4px;
}

.mini-die {
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(212, 175, 55, 0.15);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 4px;
  color: #fffdd0;
  font-size: 13px;
  font-weight: 600;
}

.power-value {
  color: #ff6b6b;
  font-weight: 600;
  font-size: 14px;
}

.prob-value {
  color: rgba(255, 253, 208, 0.6);
  font-size: 13px;
}

/* Highlighted row (current hand or active tutorial hand) */
.hand-row.highlighted {
  background: rgba(212, 175, 55, 0.15);
}

.hand-row.highlighted td {
  border-bottom-color: rgba(212, 175, 55, 0.3);
}

/* Possible hand (given current dice) */
.hand-row.possible {
  background: rgba(76, 175, 80, 0.15);
}

.hand-row.possible .hand-name {
  text-shadow: 0 0 8px rgba(76, 175, 80, 0.5);
}

/* Current dice display */
.current-dice-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(212, 175, 55, 0.08);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 8px;
  margin-bottom: 16px;
}

.current-dice-label {
  font-size: 13px;
  color: rgba(255, 253, 208, 0.7);
}

.current-dice-display {
  display: flex;
  gap: 6px;
}

.current-die {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(212, 175, 55, 0.2);
  border: 2px solid #d4af37;
  border-radius: 6px;
  color: #d4af37;
  font-size: 16px;
  font-weight: 700;
}

/* Tip section */
.guide-tip {
  padding: 12px 16px;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 8px;
}

.guide-tip p {
  margin: 0;
  font-size: 13px;
  color: rgba(255, 253, 208, 0.7);
  line-height: 1.5;
}

/* Transition */
.guide-enter-active,
.guide-leave-active {
  transition: opacity 0.3s ease;
}

.guide-enter-active .hand-guide-panel,
.guide-leave-active .hand-guide-panel {
  transition: all 0.3s ease;
}

.guide-enter-from,
.guide-leave-to {
  opacity: 0;
}

.guide-enter-from .hand-guide-panel {
  transform: scale(0.9) translateY(20px);
  opacity: 0;
}

.guide-leave-to .hand-guide-panel {
  transform: scale(0.95) translateY(10px);
  opacity: 0;
}

/* Responsive */
@media (max-width: 480px) {
  .hand-guide-panel {
    max-width: 100%;
    border-radius: 12px;
  }

  .guide-header {
    padding: 16px 16px 12px;
  }

  .guide-title {
    font-size: 18px;
  }

  .guide-content {
    padding: 12px 16px 20px;
  }

  .hand-table th,
  .hand-table td {
    padding: 8px 4px;
  }

  .hand-name {
    font-size: 12px;
  }

  .mini-die {
    width: 20px;
    height: 20px;
    font-size: 11px;
  }

  .col-prob {
    display: none;
  }
}
</style>
