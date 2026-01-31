<!--
  스킬 효과 시각화 컴포넌트
  CLAUDE.md 스킬 시스템 - 전투 중 스킬 발동 표시
-->
<template>
  <Transition name="skill-effect">
    <div v-if="visible" class="skill-effect-notification" :class="rarityClass">
      <!-- 스킬 아이콘 -->
      <div class="skill-icon">
        <img v-if="skill.iconUrl" :src="skill.iconUrl" :alt="skill.name" />
        <div v-else class="icon-placeholder">
          {{ skill.name.charAt(0) }}
        </div>
      </div>

      <!-- 스킬 정보 -->
      <div class="skill-info">
        <h3 class="skill-name">{{ skill.name }}</h3>
        <p class="skill-trigger">{{ $t(`skillEffects.trigger.${skill.triggerType}`) }}</p>
        <p class="skill-effect-desc">{{ effectDescription }}</p>
      </div>

      <!-- 희귀도 배지 -->
      <div class="rarity-badge" :class="rarityClass">
        {{ $t(`rarity.${skill.rarity.toLowerCase()}`) }}
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { Skill } from '@/types/game'

interface Props {
  skill: Skill
  effectDescription?: string
  duration?: number
  show: boolean
}

const props = withDefaults(defineProps<Props>(), {
  effectDescription: '',
  duration: 3000  // 3초 기본
})

const emit = defineEmits<{
  'hide': []
}>()

// State
const visible = ref(false)

// Computed
const rarityClass = computed(() => props.skill.rarity.toLowerCase())

// Watch
watch(() => props.show, (newVal) => {
  if (newVal) {
    showNotification()
  }
})

// Methods
function showNotification() {
  visible.value = true

  // 자동 숨김 (duration 후)
  setTimeout(() => {
    hideNotification()
  }, props.duration)
}

function hideNotification() {
  visible.value = false
  emit('hide')
}
</script>

<style scoped>
.skill-effect-notification {
  position: fixed;
  top: 120px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px 30px;
  background: linear-gradient(135deg, rgba(27, 27, 39, 0.95), rgba(42, 42, 62, 0.95));
  border: 2px solid #d4af37;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(212, 175, 55, 0.4);
  z-index: 1500;
  min-width: 400px;
  max-width: 600px;
  backdrop-filter: blur(10px);
}

@media (max-width: 768px) {
  .skill-effect-notification {
    min-width: 280px;
    max-width: 90%;
    padding: 15px 20px;
    gap: 12px;
  }
}

.skill-effect-notification.common {
  border-color: #808080;
  box-shadow: 0 10px 30px rgba(128, 128, 128, 0.4);
}

.skill-effect-notification.rare {
  border-color: #4169e1;
  box-shadow: 0 10px 30px rgba(65, 105, 225, 0.4);
}

.skill-effect-notification.epic {
  border-color: #9370db;
  box-shadow: 0 10px 30px rgba(147, 112, 219, 0.4);
}

.skill-effect-notification.legendary {
  border-color: #ffa500;
  box-shadow: 0 10px 30px rgba(255, 165, 0, 0.4);
  animation: legendary-glow 2s ease-in-out infinite;
}

@keyframes legendary-glow {
  0%, 100% {
    box-shadow: 0 10px 30px rgba(255, 165, 0, 0.4);
  }
  50% {
    box-shadow: 0 10px 40px rgba(255, 165, 0, 0.7);
  }
}

.skill-icon {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #d4af37;
}

@media (max-width: 768px) {
  .skill-icon {
    width: 48px;
    height: 48px;
  }
}

.skill-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.icon-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #3a3a5a, #2a2a4a);
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 28px;
  color: #d4af37;
  font-weight: bold;
}

.skill-info {
  flex: 1;
}

.skill-name {
  font-size: 18px;
  color: #fffdd0;
  margin-bottom: 4px;
  font-weight: bold;
}

@media (max-width: 768px) {
  .skill-name {
    font-size: 16px;
  }
}

.skill-trigger {
  font-size: 12px;
  color: #d4af37;
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.skill-effect-desc {
  font-size: 14px;
  color: #cccccc;
  line-height: 1.4;
  margin: 0;
}

@media (max-width: 768px) {
  .skill-effect-desc {
    font-size: 12px;
  }
}

.rarity-badge {
  flex-shrink: 0;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.rarity-badge.common {
  background: #808080;
  color: white;
}

.rarity-badge.rare {
  background: #4169e1;
  color: white;
}

.rarity-badge.epic {
  background: #9370db;
  color: white;
}

.rarity-badge.legendary {
  background: #ffa500;
  color: white;
  animation: legendary-badge-pulse 1.5s ease-in-out infinite;
}

@keyframes legendary-badge-pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* Transition animations */
.skill-effect-enter-active {
  animation: skill-effect-in 0.4s ease-out;
}

.skill-effect-leave-active {
  animation: skill-effect-out 0.3s ease-in;
}

@keyframes skill-effect-in {
  0% {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px) scale(0.9);
  }
  100% {
    opacity: 1;
    transform: translateX(-50%) translateY(0) scale(1);
  }
}

@keyframes skill-effect-out {
  0% {
    opacity: 1;
    transform: translateX(-50%) translateY(0) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px) scale(0.9);
  }
}
</style>
