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

.btn-action {
  margin-top: 12px;
  padding: 12px 32px;
  background: linear-gradient(135deg, var(--color-gold) 0%, var(--color-gold-light) 100%);
  border: none;
  color: var(--color-dark-navy);
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  transition: all 0.3s;
  min-height: 44px;
}

.btn-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(var(--color-gold-rgb), 0.4);
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

.item-card.equipped {
  border-color: var(--color-emerald);
  box-shadow: 0 0 12px rgba(var(--color-emerald-rgb), 0.15);
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

.equipped-badge {
  font-size: 12px;
  color: var(--color-emerald);
  border: 1px solid var(--color-emerald);
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
