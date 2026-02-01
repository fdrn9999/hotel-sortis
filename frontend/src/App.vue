<script setup lang="ts">
import { RouterView } from 'vue-router'
import Toast from '@/components/Toast.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
</script>

<template>
  <RouterView />
  <Toast />
  <ConfirmModal />
</template>

<style scoped>
</style>
