/**
 * useTierChangeModal - Tier change modal system
 *
 * Shows promotion/demotion effects after PvP matches
 */

import type { RankTier } from '@/types/game'

export interface TierChangeOptions {
  oldTier: RankTier
  newTier: RankTier
  newElo: number
}

/**
 * Composable for displaying tier change modals
 *
 * @example
 * const { showTierChange } = useTierChangeModal()
 *
 * // After battle end, if tier changed
 * if (reward.tierChanged) {
 *   showTierChange({
 *     oldTier: reward.oldTier,
 *     newTier: reward.newTier,
 *     newElo: reward.newElo
 *   })
 * }
 */
export function useTierChangeModal() {
  /**
   * Show tier change modal
   */
  const showTierChange = (options: TierChangeOptions): void => {
    window.dispatchEvent(
      new CustomEvent('show-tier-change', {
        detail: options
      })
    )
  }

  return {
    showTierChange
  }
}
