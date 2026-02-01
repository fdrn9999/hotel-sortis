/**
 * useNotification - Toast 알림 시스템
 *
 * CLAUDE.md 규칙 준수: alert(), confirm(), prompt() 대체
 */

export type ToastType = 'success' | 'error' | 'info'

export interface ToastOptions {
  message: string
  type?: ToastType
  duration?: number
}

/**
 * Toast 알림을 표시하는 composable
 */
export function useNotification() {
  const showToast = (message: string, type: ToastType = 'info', duration = 3000) => {
    // CustomEvent를 사용해 Toast 컴포넌트로 전달
    window.dispatchEvent(
      new CustomEvent('show-toast', {
        detail: { message, type, duration }
      })
    )
  }

  const success = (message: string, duration = 3000) => {
    showToast(message, 'success', duration)
  }

  const error = (message: string, duration = 3000) => {
    showToast(message, 'error', duration)
  }

  const info = (message: string, duration = 3000) => {
    showToast(message, 'info', duration)
  }

  return {
    showToast,
    success,
    error,
    info
  }
}
