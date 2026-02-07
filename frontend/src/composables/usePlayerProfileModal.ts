/**
 * usePlayerProfileModal - Player profile modal system
 *
 * Shows player profile with stats and action buttons
 */

export interface PlayerProfileOptions {
  playerId: number
  showActions?: boolean  // Whether to show Add Friend, Whisper, Block buttons
}

/**
 * Composable for displaying player profile modals
 *
 * @example
 * const { showProfile } = usePlayerProfileModal()
 *
 * // Show profile with actions
 * showProfile({ playerId: 123 })
 *
 * // Show profile without actions (view only)
 * showProfile({ playerId: 123, showActions: false })
 */
export function usePlayerProfileModal() {
  /**
   * Show player profile modal
   */
  const showProfile = (options: PlayerProfileOptions): Promise<boolean> => {
    return new Promise((resolve) => {
      window.dispatchEvent(
        new CustomEvent('show-player-profile', {
          detail: {
            ...options,
            resolver: resolve
          }
        })
      )
    })
  }

  return {
    showProfile
  }
}
