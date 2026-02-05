<template>
  <nav class="app-navigation">
    <!-- 좌측: 홈 + 뒤로가기 버튼 -->
    <div class="nav-left">
      <button
        v-if="showHome"
        class="nav-btn home-btn"
        @click="handleHome"
        :aria-label="$t('common.home')"
        :title="$t('common.home')"
      >
        🏠
      </button>
      <button
        v-if="showBack && canGoBack"
        class="nav-btn back-btn"
        @click="handleBack"
        :aria-label="$t('common.back')"
        :title="$t('common.back')"
      >
        ←
      </button>
    </div>

    <!-- 중앙: 제목 (선택) -->
    <h1 v-if="title" class="nav-title">{{ title }}</h1>
    <div v-else class="nav-spacer"></div>

    <!-- 우측: 설정 버튼 -->
    <div class="nav-right">
      <button
        v-if="showSettings"
        class="nav-btn settings-btn"
        @click="handleSettings"
        :aria-label="$t('common.settings')"
        :title="$t('common.settings')"
      >
        ⚙️
      </button>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  title?: string
  showHome?: boolean
  showBack?: boolean
  showSettings?: boolean
  onHome?: () => void
  onBack?: () => void
  onSettings?: () => void
}>()

const router = useRouter()
useI18n()

// 뒤로가기 가능 여부
const canGoBack = computed(() => {
  return window.history.length > 1
})

// 홈 버튼 클릭
const handleHome = () => {
  if (props.onHome) {
    props.onHome()
  } else {
    router.push('/')
  }
}

// 뒤로가기 버튼 클릭
const handleBack = () => {
  if (props.onBack) {
    props.onBack()
  } else {
    router.back()
  }
}

// 설정 버튼 클릭
const handleSettings = () => {
  if (props.onSettings) {
    props.onSettings()
  } else {
    router.push('/settings')
  }
}
</script>

<style scoped>
.app-navigation {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: linear-gradient(to bottom, rgba(var(--color-dark-navy-rgb), 0.98), rgba(var(--color-dark-navy-rgb), 0.95));
  backdrop-filter: blur(10px);
  border-bottom: 2px solid rgba(var(--color-gold-rgb), 0.3);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.5);
}

.nav-left,
.nav-right {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 100px;
}

.nav-right {
  justify-content: flex-end;
}

.nav-spacer {
  flex: 1;
}

.nav-title {
  flex: 1;
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-gold);
  text-align: center;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0 16px;
}

.nav-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(var(--color-gold-rgb), 0.1);
  border: 2px solid rgba(var(--color-gold-rgb), 0.3);
  border-radius: 8px;
  color: var(--color-gold);
  font-size: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.nav-btn:hover {
  background: rgba(var(--color-gold-rgb), 0.2);
  border-color: rgba(var(--color-gold-rgb), 0.6);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--color-gold-rgb), 0.3);
}

.nav-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(var(--color-gold-rgb), 0.2);
}

/* 버튼별 스타일 */
.home-btn {
  font-size: 18px;
}

.back-btn {
  font-size: 24px;
  font-weight: bold;
}

.settings-btn {
  font-size: 20px;
}

/* 모바일 최적화 */
@media (max-width: 768px) {
  .app-navigation {
    padding: 0 12px;
  }

  .nav-title {
    font-size: 16px;
    padding: 0 8px;
  }

  .nav-left,
  .nav-right {
    min-width: 80px;
  }
}

@media (max-width: 480px) {
  .app-navigation {
    height: 56px;
    padding: 0 8px;
  }

  .nav-btn {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }

  .back-btn {
    font-size: 20px;
  }

  .nav-title {
    font-size: 14px;
    padding: 0 4px;
  }

  .nav-left,
  .nav-right {
    gap: 4px;
    min-width: 60px;
  }
}
</style>
