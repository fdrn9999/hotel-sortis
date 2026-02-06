<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast" tag="div">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          class="toast"
          :class="toast.type"
        >
          <div class="toast-icon">
            <span v-if="toast.type === 'success'">✓</span>
            <span v-else-if="toast.type === 'error'">✕</span>
            <span v-else>ℹ</span>
          </div>
          <div class="toast-message">{{ toast.message }}</div>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

export interface Toast {
  id: number
  message: string
  type: 'success' | 'error' | 'info'
  duration: number
}

const toasts = ref<Toast[]>([])
let toastIdCounter = 0

// Add toast function
const addToast = (message: string, type: 'success' | 'error' | 'info' = 'info', duration = 3000) => {
  const id = toastIdCounter++
  toasts.value.push({ id, message, type, duration })

  // Auto-remove after duration
  setTimeout(() => {
    removeToast(id)
  }, duration)
}

// Remove toast
const removeToast = (id: number) => {
  const index = toasts.value.findIndex((t) => t.id === id)
  if (index !== -1) {
    toasts.value.splice(index, 1)
  }
}

// Global event listener
const handleToastEvent = (event: CustomEvent) => {
  const { message, type, duration } = event.detail
  addToast(message, type, duration)
}

onMounted(() => {
  window.addEventListener('show-toast', handleToastEvent as EventListener)
})

onUnmounted(() => {
  window.removeEventListener('show-toast', handleToastEvent as EventListener)
})

// Expose component methods (optional)
defineExpose({ addToast, removeToast })
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 80px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 12px;
  pointer-events: none;
}

.toast {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 300px;
  max-width: 400px;
  padding: 16px 20px;
  background: rgba(var(--color-dark-navy-rgb), 0.98);
  border: 2px solid;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  pointer-events: auto;
  backdrop-filter: blur(10px);
}

.toast.success {
  border-color: var(--color-emerald);
  background: linear-gradient(135deg, rgba(var(--color-emerald-rgb), 0.15) 0%, rgba(var(--color-dark-navy-rgb), 0.98) 100%);
}

.toast.error {
  border-color: #f44336;
  background: linear-gradient(135deg, rgba(244, 67, 54, 0.15) 0%, rgba(var(--color-dark-navy-rgb), 0.98) 100%);
}

.toast.info {
  border-color: var(--color-gold);
  background: linear-gradient(135deg, rgba(var(--color-gold-rgb), 0.15) 0%, rgba(var(--color-dark-navy-rgb), 0.98) 100%);
}

.toast-icon {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 18px;
  font-weight: bold;
}

.toast.success .toast-icon {
  background: rgba(var(--color-emerald-rgb), 0.2);
  color: var(--color-emerald);
}

.toast.error .toast-icon {
  background: rgba(244, 67, 54, 0.2);
  color: #f44336;
}

.toast.info .toast-icon {
  background: rgba(var(--color-gold-rgb), 0.2);
  color: var(--color-gold);
}

.toast-message {
  flex: 1;
  color: var(--color-cream);
  font-size: 14px;
  line-height: 1.5;
}

/* Animation */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(50%) scale(0.8);
}

/* Mobile optimization */
@media (max-width: 480px) {
  .toast-container {
    top: 60px;
    right: 10px;
    left: 10px;
  }

  .toast {
    min-width: unset;
    max-width: unset;
    width: 100%;
  }
}
</style>
