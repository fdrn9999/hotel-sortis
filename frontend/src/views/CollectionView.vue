<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import AppNavigation from '@/components/AppNavigation.vue'

const { t } = useI18n()
</script>

<template>
  <div class="collection">
    <!-- Navigation Bar -->
    <AppNavigation
      :title="t('menu.collection')"
      :show-home="true"
      :show-back="true"
      :show-settings="true"
    />

    <main class="collection-content">
      <p>Collection - Coming soon</p>
    </main>
  </div>
</template>

<style scoped>
.collection {
  min-height: 100vh;
  padding: 80px 2rem 2rem;
  background: linear-gradient(135deg, #1a1a27 0%, #2d2d3f 100%);
}

.collection-content {
  text-align: center;
  color: #fffdd0;
  font-size: 18px;
  margin-top: 40px;
}
</style>
