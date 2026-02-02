import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * Settings Store - 게임 설정 관리
 * CLAUDE.md 3.3.3 규칙 준수: 사운드, 진동, 그래픽, 게임플레이 설정
 */

export interface GameSettings {
  // Sound
  bgmVolume: number
  sfxVolume: number
  muted: boolean

  // Vibration (mobile only)
  vibration: boolean

  // Graphics
  quality: 'low' | 'medium' | 'high'
  animations: boolean

  // Gameplay
  animationSpeed: number // 0.5, 1, 1.5, 2
  battleSkip: boolean

  // Language
  language: 'ko' | 'en' | 'ja' | 'zh'
}

const DEFAULT_SETTINGS: GameSettings = {
  bgmVolume: 70,
  sfxVolume: 80,
  muted: false,
  vibration: true,
  quality: 'high',
  animations: true,
  animationSpeed: 1,
  battleSkip: false,
  language: 'ko'
}

const STORAGE_KEY = 'hotel-sortis-settings'

function loadFromStorage(): GameSettings {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      const parsed = JSON.parse(saved)
      return { ...DEFAULT_SETTINGS, ...parsed }
    }
  } catch {
    // ignore
  }
  return { ...DEFAULT_SETTINGS }
}

export const useSettingsStore = defineStore('settings', () => {
  const settings = ref<GameSettings>(loadFromStorage())

  // Computed
  const isMobile = computed(() => {
    return /Android|iPhone|iPad|iPod/i.test(navigator.userAgent)
  })

  // Save to localStorage
  function saveSettings() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(settings.value))
    } catch {
      // ignore
    }
  }

  // Sound
  function setBGMVolume(volume: number) {
    settings.value.bgmVolume = Math.max(0, Math.min(100, volume))
    saveSettings()
  }

  function setSFXVolume(volume: number) {
    settings.value.sfxVolume = Math.max(0, Math.min(100, volume))
    saveSettings()
  }

  function toggleMute() {
    settings.value.muted = !settings.value.muted
    saveSettings()
  }

  // Vibration
  function toggleVibration() {
    settings.value.vibration = !settings.value.vibration
    saveSettings()
    if (settings.value.vibration && navigator.vibrate) {
      navigator.vibrate(50)
    }
  }

  // Graphics
  function setQuality(quality: 'low' | 'medium' | 'high') {
    settings.value.quality = quality
    saveSettings()
  }

  function toggleAnimations() {
    settings.value.animations = !settings.value.animations
    saveSettings()
  }

  // Gameplay
  function setAnimationSpeed(speed: number) {
    settings.value.animationSpeed = speed
    saveSettings()
  }

  function toggleBattleSkip() {
    settings.value.battleSkip = !settings.value.battleSkip
    saveSettings()
  }

  // Language
  function setLanguage(lang: 'ko' | 'en' | 'ja' | 'zh') {
    settings.value.language = lang
    saveSettings()
  }

  // Reset
  function resetSettings() {
    settings.value = { ...DEFAULT_SETTINGS }
    saveSettings()
  }

  return {
    settings,
    isMobile,
    saveSettings,
    setBGMVolume,
    setSFXVolume,
    toggleMute,
    toggleVibration,
    setQuality,
    toggleAnimations,
    setAnimationSpeed,
    toggleBattleSkip,
    setLanguage,
    resetSettings
  }
})
