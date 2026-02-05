<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useCosmeticStore } from '@/stores/cosmetic'
import AppNavigation from '@/components/AppNavigation.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const router = useRouter()
const { t } = useI18n()
const cosmeticStore = useCosmeticStore()

const activeTab = ref<'diceSkins' | 'avatars'>('diceSkins')

// TODO: get from auth store
const playerId = ref(1)

const displayItems = computed(() => {
  return activeTab.value === 'diceSkins'
    ? cosmeticStore.ownedDiceSkins
    : cosmeticStore.ownedAvatars
})

const isEmpty = computed(() => {
  return cosmeticStore.ownedDiceSkins.length === 0 && cosmeticStore.ownedAvatars.length === 0
})

async function loadCollection() {
  try {
    await cosmeticStore.loadCollection(playerId.value)
  } catch {
    // error is stored in cosmeticStore.error
  }
}

function goToShop() {
  router.push({ name: 'shop' })
}

onMounted(() => {
  loadCollection()
})
</script>

<template>
  <div class="collection-view">
    <AppNavigation
      :title="t('cosmetics.myCollection')"
      :show-home="true"
      :show-back="true"
      :show-settings="true"
    />

    <main class="main-content">
      <!-- Loading -->
      <LoadingSpinner v-if="cosmeticStore.loading" :message="t('common.loading')" />

      <!-- Error -->
      <div v-else-if="cosmeticStore.error" class="error-state">
        <div class="error-icon">!</div>
        <p class="error-title">{{ t('cosmetics.loadFailed') }}</p>
        <p class="error-detail">{{ cosmeticStore.error }}</p>
        <button class="btn-retry" @click="loadCollection">
          {{ t('common.retry') }}
        </button>
      </div>

      <!-- Empty -->
      <div v-else-if="isEmpty" class="empty-state">
        <div class="empty-icon">&#x1F3B2;</div>
        <p class="empty-title">{{ t('cosmetics.noItems') }}</p>
        <p class="empty-description">{{ t('cosmetics.emptyCollectionHint') }}</p>
        <button class="btn-action" @click="goToShop">
          {{ t('cosmetics.goToShop') }}
        </button>
      </div>

      <!-- Collection Data -->
      <div v-else class="collection-layout">
        <!-- Tab Filter -->
        <div class="tab-filter">
          <button
            class="tab-btn"
            :class="{ active: activeTab === 'diceSkins' }"
            @click="activeTab = 'diceSkins'"
          >
            {{ t('cosmetics.diceSkin') }} ({{ cosmeticStore.ownedDiceSkins.length }})
          </button>
          <button
            class="tab-btn"
            :class="{ active: activeTab === 'avatars' }"
            @click="activeTab = 'avatars'"
          >
            {{ t('cosmetics.avatar') }} ({{ cosmeticStore.ownedAvatars.length }})
          </button>
        </div>

        <!-- Item Grid -->
        <div class="item-grid">
          <div
            v-for="item in displayItems"
            :key="item.id"
            class="item-card"
            :class="{ equipped: item.isEquipped }"
          >
            <div class="item-rarity" :class="`rarity-${item.rarity.toLowerCase()}`">
              {{ item.rarity }}
            </div>
            <div class="item-name">{{ item.name }}</div>
            <div class="item-description">{{ item.description }}</div>
            <div class="item-footer">
              <span v-if="item.isEquipped" class="equipped-badge">
                {{ t('cosmetics.equipped') }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.collection-view {
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

.btn-action {
  margin-top: 12px;
  padding: 12px 32px;
  background: linear-gradient(135deg, #d4af37 0%, #ffd700 100%);
  border: none;
  color: #1b1b27;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  transition: all 0.3s;
  min-height: 44px;
}

.btn-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(212, 175, 55, 0.4);
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

.item-card.equipped {
  border-color: #50c878;
  box-shadow: 0 0 12px rgba(80, 200, 120, 0.15);
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

.equipped-badge {
  font-size: 12px;
  color: #50c878;
  border: 1px solid #50c878;
  border-radius: 4px;
  padding: 2px 8px;
}

/* Responsive */
@media (max-width: 768px) {
  .item-grid {
    grid-template-columns: 1fr;
  }
}
</style>
