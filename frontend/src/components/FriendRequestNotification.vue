<template>
  <Teleport to="body">
    <Transition name="slide-in">
      <div v-if="currentRequest" class="friend-request-notification">
        <div class="notification-content">
          <div class="notification-header">
            <span class="notification-icon">👋</span>
            <span class="notification-title">{{ t('social.friends.newRequest') }}</span>
          </div>
          <div class="notification-body">
            <span class="sender-name">{{ currentRequest.senderUsername || currentRequest.username }}</span>
            <span class="request-message">{{ t('social.friends.wantsToBeYourFriend') }}</span>
          </div>
          <div class="notification-actions">
            <button class="action-btn accept" @click="handleAccept" :disabled="processing">
              {{ t('social.friends.accept') }}
            </button>
            <button class="action-btn decline" @click="handleDecline" :disabled="processing">
              {{ t('social.friends.decline') }}
            </button>
          </div>
        </div>
        <button class="close-btn" @click="dismissCurrent" :aria-label="t('common.close')">
          ✕
        </button>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSocialStore } from '@/stores/social'
import { useAuthStore } from '@/stores/auth'
import { useNotification } from '@/composables/useNotification'
import type { FriendRequest } from '@/types/game'

const { t } = useI18n()
const socialStore = useSocialStore()
const authStore = useAuthStore()
const notification = useNotification()

const playerId = computed(() => authStore.playerId ?? 1)

// Queue of pending notifications
const notificationQueue = ref<FriendRequest[]>([])
const processing = ref(false)

// Current request being displayed
const currentRequest = computed(() => notificationQueue.value[0] || null)

// Event listener for new friend requests
const handleNewRequest = (event: CustomEvent<FriendRequest>) => {
  notificationQueue.value.push(event.detail)
}

onMounted(() => {
  window.addEventListener('friend-request-received', handleNewRequest as EventListener)
})

onUnmounted(() => {
  window.removeEventListener('friend-request-received', handleNewRequest as EventListener)
})

// Action handlers
const handleAccept = async () => {
  if (!currentRequest.value || processing.value) return

  processing.value = true
  try {
    const success = await socialStore.acceptFriendRequest(
      playerId.value,
      currentRequest.value.requestId
    )
    if (success) {
      notification.success(t('social.friends.requestAccepted'))
    }
  } finally {
    processing.value = false
    dismissCurrent()
  }
}

const handleDecline = async () => {
  if (!currentRequest.value || processing.value) return

  processing.value = true
  try {
    const success = await socialStore.declineFriendRequest(
      playerId.value,
      currentRequest.value.requestId
    )
    if (success) {
      notification.info(t('social.friends.requestDeclined'))
    }
  } finally {
    processing.value = false
    dismissCurrent()
  }
}

const dismissCurrent = () => {
  notificationQueue.value.shift()
}

// Expose method for external triggering
defineExpose({
  addRequest: (request: FriendRequest) => {
    notificationQueue.value.push(request)
  }
})
</script>

<style scoped>
.friend-request-notification {
  position: fixed;
  top: 80px;
  right: 20px;
  width: 320px;
  background: rgba(var(--color-dark-navy-rgb), 0.98);
  border: 2px solid var(--color-gold);
  border-radius: 12px;
  padding: 16px;
  z-index: 9000;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
}

.notification-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-icon {
  font-size: 20px;
}

.notification-title {
  font-family: var(--font-primary);
  font-size: 14px;
  font-weight: 600;
  color: var(--color-gold);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.notification-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sender-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-cream);
}

.request-message {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.notification-actions {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}

.action-btn {
  flex: 1;
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn.accept {
  background: var(--color-gold);
  color: var(--color-dark-navy);
}

.action-btn.accept:hover:not(:disabled) {
  background: var(--color-cream);
}

.action-btn.decline {
  background: transparent;
  border: 1px solid var(--color-text-secondary);
  color: var(--color-text-secondary);
}

.action-btn.decline:hover:not(:disabled) {
  border-color: var(--color-error);
  color: var(--color-error);
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 24px;
  height: 24px;
  background: none;
  border: none;
  color: var(--color-text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.close-btn:hover {
  color: var(--color-cream);
}

/* Transition */
.slide-in-enter-active,
.slide-in-leave-active {
  transition: all 0.3s ease;
}

.slide-in-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.slide-in-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style>
