<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { DiceSkin } from '@/types/game'

export interface DiceResult {
  die1: number
  die2: number
  die3: number
}

const props = defineProps<{
  isRolling?: boolean
  result?: DiceResult | null
  skin?: DiceSkin | null
}>()

const emit = defineEmits<{
  (e: 'rollComplete', result: DiceResult): void
}>()

// Animation state
const displayedDice = ref<[number, number, number]>([1, 1, 1])
const isAnimating = ref(false)
const showResult = ref(false)

// Skin styling
const diceStyle = computed(() => {
  if (!props.skin) {
    return {
      '--dice-base-color': '#FFFDD0',
      '--dice-pip-color': '#1b1b27',
      '--dice-glow': 'none'
    }
  }

  const glowValue = props.skin.emissiveColor && (props.skin.emissiveIntensity ?? 0) > 0
    ? `0 0 ${10 * (props.skin.emissiveIntensity ?? 0)}px ${props.skin.emissiveColor}`
    : 'none'

  return {
    '--dice-base-color': props.skin.baseColor || '#FFFDD0',
    '--dice-pip-color': props.skin.pipColor || '#1b1b27',
    '--dice-glow': glowValue
  }
})

// Watch for result changes to trigger animation
watch(() => props.result, (newResult) => {
  if (newResult && props.isRolling) {
    animateRoll(newResult)
  }
})

// Watch for isRolling to start animation
watch(() => props.isRolling, (rolling) => {
  if (rolling) {
    showResult.value = false
    isAnimating.value = true
  }
})

// Animate dice rolling then settle to final result
async function animateRoll(targetResult: DiceResult) {
  isAnimating.value = true
  showResult.value = false

  const targetValues: [number, number, number] = [
    targetResult.die1,
    targetResult.die2,
    targetResult.die3
  ]

  // Phase 1: Fast random tumbling (600ms)
  const fastIterations = 12
  const fastDelay = 50
  for (let i = 0; i < fastIterations; i++) {
    displayedDice.value = [
      Math.floor(Math.random() * 6) + 1,
      Math.floor(Math.random() * 6) + 1,
      Math.floor(Math.random() * 6) + 1
    ]
    await sleep(fastDelay)
  }

  // Phase 2: Slow down (400ms)
  const slowIterations = 4
  const slowDelay = 100
  for (let i = 0; i < slowIterations; i++) {
    displayedDice.value = [
      Math.floor(Math.random() * 6) + 1,
      Math.floor(Math.random() * 6) + 1,
      Math.floor(Math.random() * 6) + 1
    ]
    await sleep(slowDelay)
  }

  // Phase 3: Settle to final result
  displayedDice.value = targetValues
  isAnimating.value = false
  showResult.value = true

  emit('rollComplete', targetResult)
}

function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// Pip positions for dice faces
function getPipPositions(value: number): { x: number; y: number }[] {
  const center = { x: 50, y: 50 }
  const topLeft = { x: 25, y: 25 }
  const topRight = { x: 75, y: 25 }
  const middleLeft = { x: 25, y: 50 }
  const middleRight = { x: 75, y: 50 }
  const bottomLeft = { x: 25, y: 75 }
  const bottomRight = { x: 75, y: 75 }

  switch (value) {
    case 1:
      return [center]
    case 2:
      return [topLeft, bottomRight]
    case 3:
      return [topLeft, center, bottomRight]
    case 4:
      return [topLeft, topRight, bottomLeft, bottomRight]
    case 5:
      return [topLeft, topRight, center, bottomLeft, bottomRight]
    case 6:
      return [topLeft, topRight, middleLeft, middleRight, bottomLeft, bottomRight]
    default:
      return [center]
  }
}

// Expose methods for external control
defineExpose({
  animateRoll
})
</script>

<template>
  <div class="dice-roller" :style="diceStyle as any">
    <div class="dice-container">
      <div
        v-for="(value, index) in displayedDice"
        :key="index"
        class="die"
        :class="{
          'rolling': isAnimating,
          'settled': showResult
        }"
      >
        <div class="die-face">
          <span
            v-for="(pos, pipIndex) in getPipPositions(value)"
            :key="pipIndex"
            class="pip"
            :style="{ left: pos.x + '%', top: pos.y + '%' }"
          ></span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dice-roller {
  --dice-base-color: #FFFDD0;
  --dice-pip-color: #1b1b27;
  --dice-glow: none;

  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.dice-container {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.die {
  width: 64px;
  height: 64px;
  background: linear-gradient(145deg, var(--dice-base-color), color-mix(in srgb, var(--dice-base-color) 85%, #000));
  border-radius: 12px;
  box-shadow:
    0 4px 8px rgba(0, 0, 0, 0.3),
    0 2px 4px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3),
    var(--dice-glow);
  position: relative;
  transition: transform 0.15s ease, box-shadow 0.3s ease;
}

.die.rolling {
  animation: dice-tumble 0.15s infinite ease-in-out;
}

.die.settled {
  animation: dice-land 0.3s ease-out;
}

@keyframes dice-tumble {
  0% {
    transform: rotate(-5deg) scale(1.02);
  }
  25% {
    transform: rotate(3deg) scale(0.98) translateY(-2px);
  }
  50% {
    transform: rotate(-3deg) scale(1.02);
  }
  75% {
    transform: rotate(5deg) scale(0.98) translateY(-2px);
  }
  100% {
    transform: rotate(-5deg) scale(1.02);
  }
}

@keyframes dice-land {
  0% {
    transform: scale(1.1) translateY(-10px);
  }
  50% {
    transform: scale(0.95) translateY(2px);
  }
  100% {
    transform: scale(1) translateY(0);
  }
}

.die-face {
  position: absolute;
  inset: 8px;
}

.pip {
  position: absolute;
  width: 10px;
  height: 10px;
  background: var(--dice-pip-color);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.3);
}

/* Responsive sizing */
@media (max-width: 480px) {
  .dice-container {
    gap: 12px;
  }

  .die {
    width: 52px;
    height: 52px;
    border-radius: 10px;
  }

  .die-face {
    inset: 6px;
  }

  .pip {
    width: 8px;
    height: 8px;
  }
}
</style>
