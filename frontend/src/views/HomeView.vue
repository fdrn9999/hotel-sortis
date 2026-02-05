<script setup lang="ts">
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { SFX, BGM } from '@/composables/useSound'

const { t } = useI18n()
const router = useRouter()

function navigate(path: string) {
  SFX.buttonClick()
  router.push(path)
}

onMounted(() => {
  BGM.play('menu')
})
</script>

<template>
  <div class="home">
    <!-- 우측 상단 버튼들 -->
    <div class="top-buttons">
      <button class="icon-btn" @click="navigate('/profile')" :title="t('profile.myProfile')" :aria-label="t('profile.myProfile')">
        👤
      </button>
      <button class="icon-btn" @click="navigate('/settings')" :title="t('common.settings')" :aria-label="t('common.settings')">
        ⚙️
      </button>
    </div>

    <header class="home-header">
      <h1 class="title">HOTEL SORTIS</h1>
      <p class="subtitle">"Alea Iacta Est. Sed Quis Iudicat?"</p>
    </header>

    <nav class="menu">
      <button class="menu-btn" @click="navigate('/campaign')">
        {{ t('menu.campaign') }}
      </button>
      <button class="menu-btn" @click="navigate('/pvp')">
        {{ t('menu.pvp') }}
      </button>
      <button class="menu-btn" @click="navigate('/collection')">
        {{ t('menu.collection') }}
      </button>
      <button class="menu-btn" @click="navigate('/shop')">
        {{ t('menu.shop') }}
      </button>
    </nav>

    <!-- Secondary menu -->
    <nav class="sub-menu">
      <button class="sub-menu-btn" @click="navigate('/tutorial')">
        {{ t('menu.tutorial') }}
      </button>
      <button class="sub-menu-btn" @click="navigate('/practice')">
        {{ t('menu.practice') }}
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
  background: rgba(var(--color-gold-rgb), 0.1);
  border: 2px solid rgba(var(--color-gold-rgb), 0.3);
  border-radius: 8px;
  color: var(--color-gold);
  font-size: 22px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.icon-btn:hover {
  background: rgba(var(--color-gold-rgb), 0.2);
  border-color: rgba(var(--color-gold-rgb), 0.6);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--color-gold-rgb), 0.3);
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

.sub-menu {
  display: flex;
  gap: 0.75rem;
  margin-top: 1.5rem;
  width: 100%;
  max-width: 320px;
}

.sub-menu-btn {
  flex: 1;
  padding: 0.6rem 1rem;
  font-family: var(--font-primary);
  font-size: 0.9rem;
  border: 1px solid rgba(var(--color-gold-rgb), 0.3);
  background: transparent;
  color: rgba(var(--color-gold-rgb), 0.7);
  cursor: pointer;
  transition: all 0.3s ease;
}

.sub-menu-btn:hover {
  border-color: var(--color-gold);
  color: var(--color-gold);
  background: rgba(var(--color-gold-rgb), 0.1);
}
</style>
