<template>
  <div class="shop-view">
    <div class="app-navigation">
      <div class="nav-left">
        <button class="nav-btn home-btn" @click="$router.push('/')" aria-label="Home">🏠</button>
        <button v-if="canGoBack" class="nav-btn back-btn" @click="$router.back()" aria-label="Back">←</button>
      </div>
      <h1 class="nav-title">{{ $t('shop.title') }}</h1>
      <button class="nav-btn settings-btn" @click="$router.push('/settings')" aria-label="Settings">⚙</button>
    </div>

    <div class="main-content">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>{{ $t('shop.loading') }}</p>
      </div>

      <div v-else class="shop-layout">
        <div class="balance-panel">
          <div class="balance-info">
            <span class="balance-label">{{ $t('shop.soulStones') }}</span>
            <span class="balance-value">0</span>
          </div>
        </div>

        <p style="text-align: center; padding: 60px;">
          Shop UI - Phase 9 Implementation<br>
          CLAUDE.md 준수: Pay-to-Win 금지, 코스메틱 전용
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const loading = ref(false)
const canGoBack = computed(() => window.history.length > 1)
</script>

<style scoped>
.app-navigation {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: rgba(27, 27, 39, 0.95);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  z-index: 1000;
}

.nav-left { display: flex; gap: 8px; }

.nav-btn {
  width: 44px;
  height: 44px;
  border: none;
  background: transparent;
  color: #d4af37;
  cursor: pointer;
  font-size: 20px;
}

.nav-title {
  font-size: 20px;
  color: #d4af37;
}

.main-content {
  margin-top: 60px;
  padding: 20px;
  min-height: calc(100vh - 60px);
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 16px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(212, 175, 55, 0.2);
  border-top-color: #d4af37;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.balance-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: rgba(212, 175, 55, 0.1);
  border: 2px solid #d4af37;
  border-radius: 12px;
  margin-bottom: 24px;
}

.balance-info { display: flex; align-items: center; gap: 12px; }
.balance-label { font-size: 16px; color: rgba(255, 255, 255, 0.7); }
.balance-value { font-size: 24px; font-weight: bold; color: #d4af37; }
</style>
