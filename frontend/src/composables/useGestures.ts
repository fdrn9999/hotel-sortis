import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

/**
 * useGestures - Mobile gesture support
 * CLAUDE.md 3.3.6 rule compliance: swipe back navigation
 */
export function useGestures() {
  const router = useRouter()

  let touchStartX = 0
  let touchStartY = 0
  let touchStartTime = 0

  const onTouchStart = (e: TouchEvent) => {
    touchStartX = e.touches[0].clientX
    touchStartY = e.touches[0].clientY
    touchStartTime = Date.now()
  }

  const onTouchEnd = (e: TouchEvent) => {
    const touchEndX = e.changedTouches[0].clientX
    const touchEndY = e.changedTouches[0].clientY
    const elapsed = Date.now() - touchStartTime

    const deltaX = touchEndX - touchStartX
    const deltaY = touchEndY - touchStartY

    // Only process quick swipes (under 500ms)
    if (elapsed > 500) return

    // Horizontal swipe (min 80px, dominant direction)
    if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 80) {
      if (deltaX > 0 && touchStartX < 40) {
        // Right swipe from left edge: go back
        if (window.history.length > 1) {
          router.back()
        }
      }
    }
  }

  onMounted(() => {
    document.addEventListener('touchstart', onTouchStart, { passive: true })
    document.addEventListener('touchend', onTouchEnd, { passive: true })
  })

  onUnmounted(() => {
    document.removeEventListener('touchstart', onTouchStart)
    document.removeEventListener('touchend', onTouchEnd)
  })
}
