/**
 * useNotification - Toast notification system
 *
 * CLAUDE.md rule compliance: replaces alert(), confirm(), prompt()
 */

export type ToastType = 'success' | 'error' | 'info'

export interface ToastOptions {
  message: string
  type?: ToastType
  duration?: number
}

/**
 * Composable for displaying toast notifications
 */
export function useNotification() {
  const showToast = (message: string, type: ToastType = 'info', duration = 3000) => {
    // Dispatch CustomEvent to Toast component
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
