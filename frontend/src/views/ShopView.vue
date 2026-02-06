<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useShopStore } from '@/stores/shop'
import { useAuthStore } from '@/stores/auth'
import AppNavigation from '@/components/AppNavigation.vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'

const { t } = useI18n()
const shopStore = useShopStore()
const authStore = useAuthStore()

const activeTab = ref<'diceSkins' | 'avatars'>('diceSkins')

// Player ID from auth store (fallback to 1 for offline mode)
const playerId = computed(() => authStore.playerId ?? 1)

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
      <!-- Loading Skeleton -->
      <div v-if="shopStore.loading" class="loading-skeleton">
        <div class="skeleton-balance skeleton-shimmer"></div>
        <div class="skeleton-tabs">
          <div class="skeleton-tab skeleton-shimmer"></div>
          <div class="skeleton-tab skeleton-shimmer"></div>
        </div>
        <SkeletonLoader variant="card" :count="6" />
      </div>

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
  background: linear-gradient(135deg, var(--color-dark-navy) 0%, var(--color-black) 100%);
  color: var(--color-cream);
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
  background: rgba(var(--color-velvet-red-rgb), 0.3);
  border: 2px solid var(--color-velvet-red);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
  color: var(--color-error);
}

.error-title {
  font-size: 20px;
  font-weight: bold;
  color: var(--color-error);
}

.error-detail {
  font-size: 14px;
  color: rgba(var(--color-error-rgb), 0.7);
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
  color: var(--color-gold);
}

.empty-description {
  font-size: 14px;
  color: rgba(var(--color-cream-rgb), 0.6);
  max-width: 400px;
}

/* Retry Button */
.btn-retry {
  margin-top: 12px;
  padding: 10px 30px;
  background: rgba(var(--color-gold-rgb), 0.2);
  border: 1px solid var(--color-gold);
  color: var(--color-gold);
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
  min-height: 44px;
}

.btn-retry:hover {
  background: rgba(var(--color-gold-rgb), 0.3);
  transform: translateY(-2px);
}

/* Balance Panel */
.balance-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: rgba(var(--color-gold-rgb), 0.1);
  border: 2px solid var(--color-gold);
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
  color: var(--color-gold);
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
  background: rgba(var(--color-gold-rgb), 0.1);
  border: 1px solid rgba(var(--color-gold-rgb), 0.3);
  color: rgba(var(--color-cream-rgb), 0.6);
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  min-height: 44px;
}

.tab-btn.active {
  background: rgba(var(--color-gold-rgb), 0.2);
  border-color: var(--color-gold);
  color: var(--color-gold);
  font-weight: bold;
}

.tab-btn:hover:not(.active) {
  background: rgba(var(--color-gold-rgb), 0.15);
  border-color: rgba(var(--color-gold-rgb), 0.5);
}

/* Item Grid */
.item-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.item-card {
  background: rgba(var(--color-dark-navy-rgb), 0.6);
  border: 1px solid rgba(var(--color-gold-rgb), 0.3);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s;
}

.item-card:hover {
  border-color: var(--color-gold);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(var(--color-gold-rgb), 0.15);
}

.item-card.owned {
  opacity: 0.7;
  border-color: rgba(var(--color-gold-rgb), 0.15);
}

.item-rarity {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 8px;
  font-weight: bold;
}

.rarity-common { color: var(--color-white); }
.rarity-rare { color: var(--color-rare); }
.rarity-epic { color: var(--color-epic); }
.rarity-legendary { color: var(--color-gold-light); }

.item-name {
  font-size: 16px;
  font-weight: bold;
  color: var(--color-cream);
  margin-bottom: 6px;
}

.item-description {
  font-size: 13px;
  color: rgba(var(--color-cream-rgb), 0.5);
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
  color: var(--color-emerald);
  border: 1px solid var(--color-emerald);
  border-radius: 4px;
  padding: 2px 8px;
}

.item-price {
  font-size: 14px;
  font-weight: bold;
  color: var(--color-gold);
}

/* Skeleton Loading */
.loading-skeleton {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.skeleton-balance {
  height: 60px;
  border-radius: 12px;
}

.skeleton-tabs {
  display: flex;
  gap: 8px;
}

.skeleton-tab {
  flex: 1;
  height: 44px;
  border-radius: 8px;
}

.skeleton-shimmer {
  background: linear-gradient(
    90deg,
    rgba(var(--color-gold-rgb), 0.08) 0%,
    rgba(var(--color-gold-rgb), 0.15) 50%,
    rgba(var(--color-gold-rgb), 0.08) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite ease-in-out;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Responsive - Tablet */
@media (max-width: 768px) {
  .main-content {
    padding: 70px 16px 16px;
  }

  .item-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .balance-panel {
    padding: 12px;
    margin-bottom: 16px;
  }

  .balance-value {
    font-size: 20px;
  }

  .tab-filter {
    margin-bottom: 16px;
  }

  .tab-btn {
    padding: 10px 12px;
    font-size: 13px;
  }

  .item-card {
    padding: 12px;
  }

  .item-name {
    font-size: 15px;
  }

  .item-description {
    font-size: 12px;
    margin-bottom: 10px;
  }
}

/* Responsive - Mobile */
@media (max-width: 480px) {
  .main-content {
    padding: 66px 12px 12px;
  }

  .item-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .balance-panel {
    flex-direction: column;
    gap: 8px;
    text-align: center;
  }

  .tab-filter {
    flex-direction: column;
    gap: 6px;
  }

  .tab-btn {
    width: 100%;
  }
}
</style>
