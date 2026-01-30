<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const router = useRouter()
</script>

<template>
  <div class="pvp">
    <header class="pvp-header">
      <button class="back-btn" @click="router.push('/')">{{ t('common.back') }}</button>
      <h1>{{ t('menu.pvp') }}</h1>
    </header>
    <main class="pvp-content">
      <p>PvP Ranked - Coming soon</p>
    </main>
  </div>
</template>

<style scoped>
.pvp {
  min-height: 100vh;
  padding: 2rem;
}

.pvp-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.back-btn {
  padding: 0.5rem 1rem;
  border: 1px solid var(--color-gold);
  background: transparent;
  color: var(--color-gold);
  cursor: pointer;
}

.pvp-content {
  text-align: center;
}
</style>
