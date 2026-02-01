<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const router = useRouter()
</script>

<template>
  <div class="home">
    <!-- 우측 상단 버튼들 -->
    <div class="top-buttons">
      <button class="icon-btn" @click="router.push('/profile')" :title="t('profile.myProfile')">
        👤
      </button>
      <button class="icon-btn" @click="router.push('/settings')" :title="t('common.settings')">
        ⚙️
      </button>
    </div>

    <header class="home-header">
      <h1 class="title">HOTEL SORTIS</h1>
      <p class="subtitle">"Alea Iacta Est. Sed Quis Iudicat?"</p>
    </header>

    <nav class="menu">
      <button class="menu-btn" @click="router.push('/campaign')">
        {{ t('menu.campaign') }}
      </button>
      <button class="menu-btn" @click="router.push('/pvp')">
        {{ t('menu.pvp') }}
      </button>
      <button class="menu-btn" @click="router.push('/collection')">
        {{ t('menu.collection') }}
      </button>
      <button class="menu-btn" @click="router.push('/shop')">
        {{ t('menu.shop') }}
      </button>
    </nav>
  </div>
</template>

<style scoped>
.home {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 2rem;
}

.top-buttons {
  position: fixed;
  top: 20px;
  right: 20px;
  display: flex;
  gap: 12px;
  z-index: 100;
}

.icon-btn {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(212, 175, 55, 0.1);
  border: 2px solid rgba(212, 175, 55, 0.3);
  border-radius: 8px;
  color: var(--color-gold);
  font-size: 22px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.icon-btn:hover {
  background: rgba(212, 175, 55, 0.2);
  border-color: rgba(212, 175, 55, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.3);
}

.icon-btn:active {
  transform: translateY(0);
}

.home-header {
  text-align: center;
  margin-bottom: 4rem;
}

.title {
  font-family: var(--font-primary);
  font-size: 3.5rem;
  color: var(--color-gold);
  letter-spacing: 0.2em;
  margin-bottom: 1rem;
}

.subtitle {
  font-style: italic;
  color: var(--color-cream);
  opacity: 0.8;
}

.menu {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: 100%;
  max-width: 320px;
}

.menu-btn {
  padding: 1rem 2rem;
  font-family: var(--font-primary);
  font-size: 1.2rem;
  border: 2px solid var(--color-gold);
  background: transparent;
  color: var(--color-gold);
  cursor: pointer;
  transition: all 0.3s ease;
}

.menu-btn:hover {
  background: var(--color-gold);
  color: var(--color-bg);
}
</style>
