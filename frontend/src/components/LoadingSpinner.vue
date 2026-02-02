<template>
  <div class="loading-spinner" :class="{ fullscreen: fullscreen }">
    <div class="spinner-container">
      <div class="dice-spinner">
        <div class="dice-face" v-for="i in 3" :key="i" :style="{ animationDelay: `${(i - 1) * 0.2}s` }">
          <span class="pip">{{ dots[i - 1] }}</span>
        </div>
      </div>
      <p v-if="message" class="loading-message">{{ message }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

defineProps<{
  message?: string
  fullscreen?: boolean
}>()

const dots = ref([1, 3, 6])
let interval: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  interval = setInterval(() => {
    dots.value = dots.value.map(() => Math.floor(Math.random() * 6) + 1)
  }, 800)
})

onUnmounted(() => {
  if (interval) clearInterval(interval)
})
</script>

<style scoped>
.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.loading-spinner.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(27, 27, 39, 0.9);
  z-index: 900;
}

.spinner-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.dice-spinner {
  display: flex;
  gap: 12px;
}

.dice-face {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(212, 175, 55, 0.1);
  border: 2px solid rgba(212, 175, 55, 0.4);
  border-radius: 8px;
  animation: dice-bounce 1.2s ease-in-out infinite;
}

.pip {
  font-size: 20px;
  font-weight: bold;
  color: var(--color-gold);
  transition: all 0.3s ease;
}

.loading-message {
  font-size: 14px;
  color: var(--color-text-secondary);
  letter-spacing: 0.05em;
}

@keyframes dice-bounce {
  0%, 100% {
    transform: translateY(0);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-8px);
    opacity: 1;
    border-color: var(--color-gold);
    box-shadow: 0 4px 12px rgba(212, 175, 55, 0.3);
  }
}
</style>
