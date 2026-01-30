<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const router = useRouter()
</script>

<template>
  <div class="home">
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
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 2rem;
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
