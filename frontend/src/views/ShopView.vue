<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useShopStore } from '@/stores/shop'
import AppNavigation from '@/components/AppNavigation.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const { t } = useI18n()
const shopStore = useShopStore()

const activeTab = ref<'diceSkins' | 'avatars'>('diceSkins')

// TODO: get from auth store
const playerId = ref(1)

const displayItems = computed(() => {
  return activeTab.value === 'diceSkins' ? shopStore.diceSkins : shopStore.avatars
})

const isEmpty = computed(() => shopStore.totalItems === 0)

function rarityClass(rarity: string): string {
  return `rarity-${rarity.toLowerCase()}`
}

async function loadShop() {
  try {
    await shopStore.loadShop(playerId.value)
  } catch {
    // error is stored in shopStore.error
  }
}

onMounted(() => {
  loadShop()
})
</script>

<template>
  <div class="shop-view">
    <AppNavigation
      :title="t('shop.title')"
      :show-home="true"
      :show-back="true"
      :show-settings="true"
    />

    <div class="main-content">
      <!-- Loading -->
      <LoadingSpinner v-if="shopStore.loading" :message="t('shop.loading')" />

      <!-- Error -->
      <div v-else-if="shopStore.error" class="error-state">
        <div class="error-icon">!</div>
        <p class="error-title">{{ t('shop.loadFailed') }}</p>
        <p class="error-detail">{{ shopStore.error }}</p>
        <button class="btn-retry" @click="loadShop">
          {{ t('common.retry') }}
        </button>
      </div>

      <!-- Empty -->
      <div v-else-if="isEmpty" class="empty-state">
        <div class="empty-icon">&#x1F3AA;</div>
        <p class="empty-title">{{ t('shop.noItems') }}</p>
        <p class="empty-description">{{ t('shop.emptyDescription') }}</p>
        <button class="btn-retry" @click="loadShop">
          {{ t('shop.refreshShop') }}
        </button>
      </div>

      <!-- Shop Data -->
      <div v-else class="shop-layout">
        <!-- Balance Panel -->
        <div class="balance-panel">
          <div class="balance-info">
            <span class="balance-label">{{ t('shop.soulStones') }}</span>
            <span class="balance-value">{{ shopStore.playerSoulStones }}</span>
          </div>
        </div>

        <!-- Tab Filter -->
        <div class="tab-filter">
          <button
            class="tab-btn"
            :class="{ active: activeTab === 'diceSkins' }"
            @click="activeTab = 'diceSkins'"
          >
            {{ t('shop.diceSkins') }} ({{ shopStore.diceSkins.length }})
          </button>
          <button
            class="tab-btn"
            :class="{ active: activeTab === 'avatars' }"
            @click="activeTab = 'avatars'"
          >
            {{ t('shop.avatars') }} ({{ shopStore.avatars.length }})
          </button>
        </div>

        <!-- Item Grid -->
        <div class="item-grid">
          <div
            v-for="item in displayItems"
            :key="`${item.cosmeticType}-${item.id}`"
            class="item-card"
            :class="{ owned: item.isOwned }"
          >
            <div class="item-rarity" :class="rarityClass(item.rarity)">
              {{ item.rarity }}
            </div>
            <div class="item-name">{{ item.name }}</div>
            <div class="item-description">{{ item.description }}</div>
            <div class="item-footer">
              <span v-if="item.isOwned" class="owned-badge">{{ t('shop.owned') }}</span>
              <span v-else class="item-price">{{ item.price }} {{ t('shop.soulStones') }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.shop-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #1b1b27 0%, #0d0d0d 100%);
  color: #fffdd0;
}

.main-content {
  padding: 80px 20px 20px;
  max-width: 900px;
  margin: 0 auto;
}

/* Error State */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  text-align: center;
  gap: 12px;
}

.error-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(139, 0, 0, 0.3);
  border: 2px solid #8b0000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
  color: #ff6b6b;
}

.error-title {
  font-size: 20px;
  font-weight: bold;
  color: #ff6b6b;
}

.error-detail {
  font-size: 14px;
  color: rgba(255, 107, 107, 0.7);
  max-width: 400px;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  text-align: center;
  gap: 12px;
}

.empty-icon {
  font-size: 64px;
  opacity: 0.6;
}

.empty-title {
  font-size: 20px;
  font-weight: bold;
  color: #d4af37;
}

.empty-description {
  font-size: 14px;
  color: rgba(255, 253, 208, 0.6);
  max-width: 400px;
}

/* Retry Button */
.btn-retry {
  margin-top: 12px;
  padding: 10px 30px;
  background: rgba(212, 175, 55, 0.2);
  border: 1px solid #d4af37;
  color: #d4af37;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
  min-height: 44px;
}

.btn-retry:hover {
  background: rgba(212, 175, 55, 0.3);
  transform: translateY(-2px);
}

/* Balance Panel */
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

.balance-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.balance-label {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
}

.balance-value {
  font-size: 24px;
  font-weight: bold;
  color: #d4af37;
}

/* Tab Filter */
.tab-filter {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
}

.tab-btn {
  flex: 1;
  padding: 12px 16px;
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.3);
  color: rgba(255, 253, 208, 0.6);
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  min-height: 44px;
}

.tab-btn.active {
  background: rgba(212, 175, 55, 0.2);
  border-color: #d4af37;
  color: #d4af37;
  font-weight: bold;
}

.tab-btn:hover:not(.active) {
  background: rgba(212, 175, 55, 0.15);
  border-color: rgba(212, 175, 55, 0.5);
}

/* Item Grid */
.item-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.item-card {
  background: rgba(27, 27, 39, 0.6);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s;
}

.item-card:hover {
  border-color: #d4af37;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(212, 175, 55, 0.15);
}

.item-card.owned {
  opacity: 0.7;
  border-color: rgba(212, 175, 55, 0.15);
}

.item-rarity {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 8px;
  font-weight: bold;
}

.rarity-common { color: #ffffff; }
.rarity-rare { color: #4169e1; }
.rarity-epic { color: #9932cc; }
.rarity-legendary { color: #ffd700; }

.item-name {
  font-size: 16px;
  font-weight: bold;
  color: #fffdd0;
  margin-bottom: 6px;
}

.item-description {
  font-size: 13px;
  color: rgba(255, 253, 208, 0.5);
  margin-bottom: 12px;
  line-height: 1.4;
}

.item-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.owned-badge {
  font-size: 12px;
  color: #50c878;
  border: 1px solid #50c878;
  border-radius: 4px;
  padding: 2px 8px;
}

.item-price {
  font-size: 14px;
  font-weight: bold;
  color: #d4af37;
}

/* Responsive */
@media (max-width: 768px) {
  .item-grid {
    grid-template-columns: 1fr;
  }
}
</style>
