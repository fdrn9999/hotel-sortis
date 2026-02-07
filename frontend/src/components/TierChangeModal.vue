<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="visible" class="tier-change-overlay" @click.self="close">
        <div class="tier-change-modal" :class="{ promotion: isPromotion, demotion: !isPromotion }">
          <!-- Particle effects for promotion -->
          <div v-if="isPromotion" class="particles">
            <span v-for="i in 20" :key="i" class="particle" :style="particleStyle(i)"></span>
          </div>

          <!-- Header -->
          <div class="modal-header">
            <h2 class="modal-title">
              {{ isPromotion ? t('tier.promotion') : t('tier.demotion') }}
            </h2>
          </div>

          <!-- Tier transition -->
          <div class="tier-transition">
            <div class="tier-from">
              <span class="tier-emoji">{{ getTierEmoji(oldTier) }}</span>
              <span class="tier-name" :style="{ color: getTierColor(oldTier) }">
                {{ t(`pvp.tiers.${oldTier.toLowerCase()}`) }}
              </span>
            </div>

            <div class="arrow" :class="{ up: isPromotion, down: !isPromotion }">
              {{ isPromotion ? '&#x2191;' : '&#x2193;' }}
            </div>

            <div class="tier-to">
              <span class="tier-emoji tier-emoji-new">{{ getTierEmoji(newTier) }}</span>
              <span class="tier-name tier-name-new" :style="{ color: getTierColor(newTier) }">
                {{ t(`pvp.tiers.${newTier.toLowerCase()}`) }}
              </span>
            </div>
          </div>

          <!-- New ELO -->
          <div class="elo-display">
            <span class="elo-label">{{ t('pvp.elo') }}</span>
            <span class="elo-value">{{ newElo }}</span>
          </div>

          <!-- Close button -->
          <button class="btn-close" @click="close">
            {{ t('common.close') }}
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import type { RankTier } from '@/types/game'
import { getTierColor } from '@/api/pvp'
import { SFX } from '@/composables/useSound'

const { t } = useI18n()

// State
const visible = ref(false)
const oldTier = ref<RankTier>('BRONZE')
const newTier = ref<RankTier>('SILVER')
const newElo = ref(1000)

const isPromotion = computed(() => {
  const tierOrder = ['BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND', 'MASTER']
  return tierOrder.indexOf(newTier.value) > tierOrder.indexOf(oldTier.value)
})

// Tier emoji map
function getTierEmoji(tier: RankTier): string {
  const emojis: Record<RankTier, string> = {
    BRONZE: '\uD83E\uDD49',
    SILVER: '\uD83E\uDD48',
    GOLD: '\uD83E\uDD47',
    PLATINUM: '\uD83D\uDC8E',
    DIAMOND: '\uD83D\uDCA0',
    MASTER: '\uD83D\uDC51'
  }
  return emojis[tier] || '\uD83C\uDF96\uFE0F'
}

// Particle style generator for promotion effect
function particleStyle(index: number): Record<string, string> {
  const delay = (index * 0.1) % 2
  const duration = 1.5 + Math.random()
  const left = Math.random() * 100
  const size = 4 + Math.random() * 8

  return {
    left: `${left}%`,
    width: `${size}px`,
    height: `${size}px`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}

// Event handler
function handleShowTierChange(event: CustomEvent) {
  const { oldTier: old, newTier: newT, newElo: elo } = event.detail
  oldTier.value = old
  newTier.value = newT
  newElo.value = elo
  visible.value = true

  // Play sound
  if (isPromotion.value) {
    SFX.victory()
  } else {
    SFX.defeat()
  }
}

function close() {
  visible.value = false
}

onMounted(() => {
  window.addEventListener('show-tier-change', handleShowTierChange as EventListener)
})

onUnmounted(() => {
  window.removeEventListener('show-tier-change', handleShowTierChange as EventListener)
})
</script>

<style scoped>
.tier-change-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10000;
}

.tier-change-modal {
  position: relative;
  background: linear-gradient(135deg, var(--color-dark-navy) 0%, #1a1a2e 100%);
  border: 3px solid var(--color-gold);
  border-radius: 20px;
  padding: 40px 60px;
  text-align: center;
  min-width: 400px;
  max-width: 90vw;
  box-shadow: 0 0 50px rgba(var(--color-gold-rgb), 0.3);
  overflow: hidden;
}

.tier-change-modal.promotion {
  border-color: var(--color-gold);
  box-shadow: 0 0 60px rgba(var(--color-gold-rgb), 0.5);
}

.tier-change-modal.demotion {
  border-color: rgba(var(--color-velvet-red-rgb), 0.6);
  box-shadow: 0 0 40px rgba(var(--color-velvet-red-rgb), 0.3);
}

/* Particles for promotion */
.particles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.particle {
  position: absolute;
  bottom: -20px;
  background: var(--color-gold);
  border-radius: 50%;
  animation: rise 2s ease-out infinite;
}

@keyframes rise {
  0% {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
  100% {
    transform: translateY(-500px) scale(0);
    opacity: 0;
  }
}

.modal-header {
  margin-bottom: 30px;
}

.modal-title {
  font-family: 'Cinzel Decorative', serif;
  font-size: 32px;
  letter-spacing: 3px;
  text-transform: uppercase;
  margin: 0;
}

.promotion .modal-title {
  color: var(--color-gold);
  text-shadow: 0 0 20px rgba(var(--color-gold-rgb), 0.5);
}

.demotion .modal-title {
  color: var(--color-velvet-red);
  text-shadow: 0 0 20px rgba(var(--color-velvet-red-rgb), 0.5);
}

.tier-transition {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30px;
  margin-bottom: 30px;
}

.tier-from,
.tier-to {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.tier-emoji {
  font-size: 48px;
  opacity: 0.6;
}

.tier-emoji-new {
  font-size: 64px;
  opacity: 1;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.tier-name {
  font-size: 18px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.tier-name-new {
  font-size: 24px;
}

.arrow {
  font-size: 48px;
  font-weight: bold;
}

.arrow.up {
  color: var(--color-emerald);
  animation: bounce-up 0.8s ease-in-out infinite;
}

.arrow.down {
  color: var(--color-velvet-red);
  animation: bounce-down 0.8s ease-in-out infinite;
}

@keyframes bounce-up {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes bounce-down {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(10px);
  }
}

.elo-display {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
  font-size: 20px;
}

.elo-label {
  color: var(--color-silver);
}

.elo-value {
  color: var(--color-gold);
  font-size: 28px;
  font-weight: bold;
}

.btn-close {
  padding: 12px 40px;
  background: var(--color-gold);
  border: none;
  border-radius: 8px;
  color: var(--color-dark-navy);
  font-size: 16px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-close:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(var(--color-gold-rgb), 0.4);
}

/* Transition animations */
.modal-fade-enter-active {
  animation: modal-in 0.4s ease-out;
}

.modal-fade-leave-active {
  animation: modal-out 0.3s ease-in;
}

@keyframes modal-in {
  0% {
    opacity: 0;
    transform: scale(0.8);
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
    transform: scale(0.8);
  }
}

/* Responsive */
@media (max-width: 600px) {
  .tier-change-modal {
    padding: 30px 20px;
    min-width: unset;
    width: 90%;
  }

  .modal-title {
    font-size: 24px;
  }

  .tier-transition {
    flex-direction: column;
    gap: 20px;
  }

  .arrow {
    transform: rotate(90deg);
  }

  .arrow.up {
    animation: bounce-right 0.8s ease-in-out infinite;
  }

  .arrow.down {
    animation: bounce-right 0.8s ease-in-out infinite;
  }
}

@keyframes bounce-right {
  0%, 100% {
    transform: rotate(90deg) translateX(0);
  }
  50% {
    transform: rotate(90deg) translateX(10px);
  }
}
</style>
