<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSettingsStore } from '@/stores/settings'
import { useNotification } from '@/composables/useNotification'
import { SFX } from '@/composables/useSound'
import AppNavigation from '@/components/AppNavigation.vue'

const { t, locale } = useI18n()
const settingsStore = useSettingsStore()
const { success } = useNotification()

// Local copy for editing
const localSettings = ref({ ...settingsStore.settings })

const isMobile = computed(() => settingsStore.isMobile)

// Language options
const languages = [
  { code: 'ko', labelKey: 'languages.ko' },
  { code: 'en', labelKey: 'languages.en' },
  { code: 'ja', labelKey: 'languages.ja' },
  { code: 'zh', labelKey: 'languages.zh' }
]

// Speed options
const speedOptions = [
  { value: 0.5, key: 'speedSlow' },
  { value: 1, key: 'speedNormal' },
  { value: 1.5, key: 'speedFast' },
  { value: 2, key: 'speedVeryFast' }
]

// Quality options
const qualityOptions = [
  { value: 'low', key: 'qualityLow' },
  { value: 'medium', key: 'qualityMedium' },
  { value: 'high', key: 'qualityHigh' }
] as const

// Handlers
function onBGMChange(e: Event) {
  const val = Number((e.target as HTMLInputElement).value)
  localSettings.value.bgmVolume = val
}

function onSFXChange(e: Event) {
  const val = Number((e.target as HTMLInputElement).value)
  localSettings.value.sfxVolume = val
}

function toggleMute() {
  localSettings.value.muted = !localSettings.value.muted
}

function toggleVibration() {
  localSettings.value.vibration = !localSettings.value.vibration
  if (localSettings.value.vibration && navigator.vibrate) {
    navigator.vibrate(50)
  }
}

function toggleAnimations() {
  localSettings.value.animations = !localSettings.value.animations
}

function toggleBattleSkip() {
  localSettings.value.battleSkip = !localSettings.value.battleSkip
}

function setQuality(quality: 'low' | 'medium' | 'high') {
  localSettings.value.quality = quality
}

function setAnimationSpeed(speed: number) {
  localSettings.value.animationSpeed = speed
}

function setLanguage(lang: string) {
  localSettings.value.language = lang as 'ko' | 'en' | 'ja' | 'zh'
}

function saveAll() {
  SFX.buttonClick()
  // Apply all settings
  settingsStore.settings.bgmVolume = localSettings.value.bgmVolume
  settingsStore.settings.sfxVolume = localSettings.value.sfxVolume
  settingsStore.settings.muted = localSettings.value.muted
  settingsStore.settings.vibration = localSettings.value.vibration
  settingsStore.settings.quality = localSettings.value.quality
  settingsStore.settings.animations = localSettings.value.animations
  settingsStore.settings.animationSpeed = localSettings.value.animationSpeed
  settingsStore.settings.battleSkip = localSettings.value.battleSkip
  settingsStore.settings.language = localSettings.value.language
  settingsStore.saveSettings()

  // Apply language globally
  locale.value = localSettings.value.language

  success(t('settings.saved'))
}
</script>

<template>
  <div class="settings-page">
    <AppNavigation
      :title="t('common.settings')"
      :showHome="true"
      :showBack="true"
      :showSettings="false"
    />

    <div class="settings-content">
      <!-- Language -->
      <section class="settings-section">
        <h2 class="section-title">{{ t('common.language') }}</h2>
        <div class="language-grid">
          <button
            v-for="lang in languages"
            :key="lang.code"
            class="lang-btn"
            :class="{ active: localSettings.language === lang.code }"
            @click="setLanguage(lang.code)"
          >
            {{ t(lang.labelKey) }}
          </button>
        </div>
      </section>

      <!-- Sound -->
      <section class="settings-section">
        <h2 class="section-title">{{ t('settings.sound') }}</h2>

        <div class="setting-row">
          <label for="bgmVolume">{{ t('settings.bgmVolume') }}</label>
          <div class="slider-group">
            <input
              id="bgmVolume"
              type="range"
              :value="localSettings.bgmVolume"
              min="0"
              max="100"
              @input="onBGMChange"
              :disabled="localSettings.muted"
              class="slider"
            />
            <span class="slider-value">{{ localSettings.bgmVolume }}%</span>
          </div>
        </div>

        <div class="setting-row">
          <label for="sfxVolume">{{ t('settings.sfxVolume') }}</label>
          <div class="slider-group">
            <input
              id="sfxVolume"
              type="range"
              :value="localSettings.sfxVolume"
              min="0"
              max="100"
              @input="onSFXChange"
              :disabled="localSettings.muted"
              class="slider"
            />
            <span class="slider-value">{{ localSettings.sfxVolume }}%</span>
          </div>
        </div>

        <div class="setting-row">
          <label>{{ t('settings.mute') }}</label>
          <button
            class="toggle-btn"
            :class="{ active: localSettings.muted }"
            @click="toggleMute"
          >
            {{ localSettings.muted ? t('common.on') : t('common.off') }}
          </button>
        </div>
      </section>

      <!-- Vibration (mobile only) -->
      <section v-if="isMobile" class="settings-section">
        <h2 class="section-title">{{ t('settings.vibration') }}</h2>
        <div class="setting-row">
          <label>{{ t('settings.vibration') }}</label>
          <button
            class="toggle-btn"
            :class="{ active: localSettings.vibration }"
            @click="toggleVibration"
          >
            {{ localSettings.vibration ? t('common.on') : t('common.off') }}
          </button>
        </div>
      </section>

      <!-- Graphics -->
      <section class="settings-section">
        <h2 class="section-title">{{ t('settings.graphics') }}</h2>

        <div class="setting-row">
          <label id="quality-label">{{ t('settings.quality') }}</label>
          <div class="option-group" role="radiogroup" aria-labelledby="quality-label">
            <button
              v-for="opt in qualityOptions"
              :key="opt.value"
              class="option-btn"
              :class="{ active: localSettings.quality === opt.value }"
              role="radio"
              :aria-checked="localSettings.quality === opt.value"
              @click="setQuality(opt.value)"
            >
              {{ t(`settings.${opt.key}`) }}
            </button>
          </div>
        </div>

        <div class="setting-row">
          <label>{{ t('settings.animations') }}</label>
          <button
            class="toggle-btn"
            :class="{ active: localSettings.animations }"
            @click="toggleAnimations"
          >
            {{ localSettings.animations ? t('common.on') : t('common.off') }}
          </button>
        </div>
      </section>

      <!-- Gameplay -->
      <section class="settings-section">
        <h2 class="section-title">{{ t('settings.gameplay') }}</h2>

        <div class="setting-row">
          <label id="speed-label">{{ t('settings.animationSpeed') }}</label>
          <div class="option-group" role="radiogroup" aria-labelledby="speed-label">
            <button
              v-for="opt in speedOptions"
              :key="opt.value"
              class="option-btn"
              :class="{ active: localSettings.animationSpeed === opt.value }"
              role="radio"
              :aria-checked="localSettings.animationSpeed === opt.value"
              @click="setAnimationSpeed(opt.value)"
            >
              {{ t(`settings.${opt.key}`) }}
            </button>
          </div>
        </div>

        <div class="setting-row">
          <label>{{ t('settings.battleSkip') }}</label>
          <button
            class="toggle-btn"
            :class="{ active: localSettings.battleSkip }"
            @click="toggleBattleSkip"
          >
            {{ localSettings.battleSkip ? t('common.on') : t('common.off') }}
          </button>
        </div>
      </section>

      <!-- Save Button -->
      <button class="save-btn" @click="saveAll">
        {{ t('settings.saveSettings') }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: var(--color-bg);
  padding-top: 60px;
}

.settings-content {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px 20px 80px;
}

/* Section */
.settings-section {
  margin-bottom: 32px;
  background: rgba(var(--color-gold-rgb), 0.05);
  border: 1px solid rgba(var(--color-gold-rgb), 0.15);
  border-radius: 12px;
  padding: 20px;
}

.section-title {
  font-family: var(--font-primary);
  font-size: 18px;
  color: var(--color-gold);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(var(--color-gold-rgb), 0.2);
  letter-spacing: 0.05em;
}

/* Setting Row */
.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
}

.setting-row + .setting-row {
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.setting-row label {
  font-size: 15px;
  color: var(--color-cream);
  flex-shrink: 0;
  margin-right: 16px;
}

/* Slider */
.slider-group {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  max-width: 260px;
}

.slider {
  flex: 1;
  height: 6px;
  -webkit-appearance: none;
  appearance: none;
  background: rgba(var(--color-gold-rgb), 0.2);
  border-radius: 3px;
  outline: none;
  cursor: pointer;
}

.slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--color-gold);
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(var(--color-gold-rgb), 0.4);
  transition: transform 0.2s ease;
}

.slider::-webkit-slider-thumb:hover {
  transform: scale(1.2);
}

.slider:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.slider-value {
  font-size: 14px;
  color: var(--color-gold);
  width: 42px;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

/* Toggle Button */
.toggle-btn {
  min-width: 64px;
  height: 36px;
  padding: 0 16px;
  border: 2px solid rgba(var(--color-gold-rgb), 0.3);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.05);
  color: var(--color-text-secondary);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  letter-spacing: 0.05em;
}

.toggle-btn.active {
  background: rgba(var(--color-gold-rgb), 0.2);
  border-color: var(--color-gold);
  color: var(--color-gold);
  box-shadow: 0 0 12px rgba(var(--color-gold-rgb), 0.2);
}

.toggle-btn:hover {
  border-color: rgba(var(--color-gold-rgb), 0.6);
}

.toggle-btn:active {
  transform: scale(0.95);
}

/* Option Group */
.option-group {
  display: flex;
  gap: 6px;
}

.option-btn {
  padding: 6px 14px;
  border: 1px solid rgba(var(--color-gold-rgb), 0.2);
  border-radius: 6px;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.option-btn.active {
  background: rgba(var(--color-gold-rgb), 0.15);
  border-color: var(--color-gold);
  color: var(--color-gold);
}

.option-btn:hover {
  border-color: rgba(var(--color-gold-rgb), 0.5);
  color: var(--color-cream);
}

.option-btn:active {
  transform: scale(0.95);
}

/* Language Grid */
.language-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.lang-btn {
  padding: 12px 16px;
  border: 2px solid rgba(var(--color-gold-rgb), 0.2);
  border-radius: 8px;
  background: transparent;
  color: var(--color-cream);
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.lang-btn.active {
  background: rgba(var(--color-gold-rgb), 0.15);
  border-color: var(--color-gold);
  color: var(--color-gold);
  box-shadow: 0 0 12px rgba(var(--color-gold-rgb), 0.15);
}

.lang-btn:hover {
  border-color: rgba(var(--color-gold-rgb), 0.5);
}

.lang-btn:active {
  transform: scale(0.95);
}

/* Save Button */
.save-btn {
  display: block;
  width: 100%;
  padding: 16px 32px;
  margin-top: 16px;
  font-family: var(--font-primary);
  font-size: 16px;
  letter-spacing: 0.1em;
  border: 2px solid var(--color-gold);
  background: var(--color-gold);
  color: var(--color-bg);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.save-btn:hover {
  background: var(--color-cream);
  border-color: var(--color-cream);
  box-shadow: 0 4px 16px rgba(var(--color-gold-rgb), 0.4);
}

.save-btn:active {
  transform: scale(0.98);
}

/* Mobile */
@media (max-width: 480px) {
  .settings-content {
    padding: 16px 12px 80px;
  }

  .settings-section {
    padding: 16px;
  }

  .setting-row {
    flex-wrap: wrap;
    gap: 8px;
  }

  .slider-group {
    max-width: 100%;
    width: 100%;
  }

  .option-group {
    flex-wrap: wrap;
  }

  .option-btn {
    font-size: 12px;
    padding: 5px 10px;
  }

  .language-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
