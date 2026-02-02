import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useSettingsStore } from '@/stores/settings'

/**
 * useKeyboardShortcuts - PC 키보드 단축키 지원
 * CLAUDE.md 3.3.5 규칙 준수: ESC, H, Backspace, M, Space
 */
export function useKeyboardShortcuts() {
  const router = useRouter()
  const settingsStore = useSettingsStore()

  function isTyping(): boolean {
    const el = document.activeElement
    if (!el) return false
    const tag = el.tagName
    return (
      tag === 'INPUT' ||
      tag === 'TEXTAREA' ||
      (el as HTMLElement).isContentEditable
    )
  }

  const onKeyDown = (e: KeyboardEvent) => {
    // ESC: toggle settings
    if (e.key === 'Escape') {
      const currentRoute = router.currentRoute.value
      if (currentRoute.name === 'settings') {
        router.back()
      } else {
        router.push('/settings')
      }
      return
    }

    // Don't process shortcuts while typing
    if (isTyping()) return

    // H: go home
    if (e.key === 'h' || e.key === 'H') {
      router.push('/')
      return
    }

    // Backspace: go back
    if (e.key === 'Backspace') {
      e.preventDefault()
      if (window.history.length > 1) {
        router.back()
      }
      return
    }

    // M: toggle mute
    if (e.key === 'm' || e.key === 'M') {
      settingsStore.toggleMute()
      return
    }
  }

  onMounted(() => {
    window.addEventListener('keydown', onKeyDown)
  })

  onUnmounted(() => {
    window.removeEventListener('keydown', onKeyDown)
  })
}
