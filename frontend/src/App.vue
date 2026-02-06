<script setup lang="ts">
import { RouterView } from 'vue-router'
import Toast from '@/components/Toast.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import { useGestures } from '@/composables/useGestures'
import { useKeyboardShortcuts } from '@/composables/useKeyboardShortcuts'
import { useSound } from '@/composables/useSound'

// Global gesture & keyboard support (CLAUDE.md 3.3.5, 3.3.6)
useGestures()
useKeyboardShortcuts()

// Global sound system initialization (PROJECTPLAN.md Ch.11)
useSound()
</script>

<template>
  <RouterView v-slot="{ Component, route }">
    <Transition name="page-fade" mode="out-in">
      <component :is="Component" :key="route.path" />
    </Transition>
  </RouterView>
  <Toast />
  <ConfirmModal />
</template>

<style scoped>
</style>
