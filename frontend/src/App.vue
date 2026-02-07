<script setup lang="ts">
import { RouterView } from 'vue-router'
import Toast from '@/components/Toast.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import TierChangeModal from '@/components/TierChangeModal.vue'
import PlayerProfileModal from '@/components/PlayerProfileModal.vue'
import GlobalChatWidget from '@/components/GlobalChatWidget.vue'
import FriendRequestNotification from '@/components/FriendRequestNotification.vue'
import { useGestures } from '@/composables/useGestures'
import { useKeyboardShortcuts } from '@/composables/useKeyboardShortcuts'
import { useSound } from '@/composables/useSound'
import { useAuthStore } from '@/stores/auth'

// Global gesture & keyboard support (CLAUDE.md 3.3.5, 3.3.6)
useGestures()
useKeyboardShortcuts()

// Global sound system initialization (PROJECTPLAN.md Ch.11)
useSound()

// Auth store for conditional chat display
const authStore = useAuthStore()
</script>

<template>
  <RouterView v-slot="{ Component, route }">
    <Transition name="page-fade" mode="out-in">
      <component :is="Component" :key="route.path" />
    </Transition>
  </RouterView>
  <Toast />
  <ConfirmModal />
  <TierChangeModal />
  <PlayerProfileModal />
  <GlobalChatWidget v-if="authStore.isAuthenticated" />
  <FriendRequestNotification v-if="authStore.isAuthenticated" />
</template>

<style scoped>
</style>
