/**
 * useConfirmModal - Confirm modal system
 *
 * CLAUDE.md rule compliance: replaces confirm()
 */

export interface ConfirmModalOptions {
  title: string
  message: string
  confirmText?: string
  cancelText?: string
}

/**
 * Composable for displaying confirm modals
 *
 * @example
 * const { confirm } = useConfirmModal()
 *
 * const result = await confirm({
 *   title: 'Logout',
 *   message: 'Are you sure you want to logout?',
 *   confirmText: 'Logout',
 *   cancelText: 'Cancel'
 * })
 *
 * if (result) {
 *   // Confirm button clicked
 *   logout()
 * } else {
 *   // Cancel button clicked
 * }
 */
export function useConfirmModal() {
  /**
   * Show confirm modal (Promise-based)
   */
  const confirm = (options: ConfirmModalOptions): Promise<boolean> => {
    return new Promise((resolve) => {
      // Dispatch CustomEvent to ConfirmModal component
      window.dispatchEvent(
        new CustomEvent('show-confirm', {
          detail: {
            ...options,
            resolver: resolve
          }
        })
      )
    })
  }

  return {
    confirm
  }
}
